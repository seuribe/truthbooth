package de.peb.truthbooth.rec;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IRational;
import com.xuggle.xuggler.IStreamCoder;

import de.peb.truthbooth.TruthBooth;

public class VideoRecorder {

	public static final String PRESET = "superfast";
	
	private static final ICodec.ID VIDEO_CODEC = ICodec.ID.CODEC_ID_H264;
    private static final ICodec.ID AUDIO_CODEC = ICodec.ID.CODEC_ID_AAC;

    private VideoProcessingThread vpt;
    
    private static Logger logger = LoggerFactory.getLogger("TruthBooth.VideoRecorder");

    private IMediaWriter writer;
    private FeedSynchronizer sync;
    
    private Lock lock = new ReentrantLock();
    private Condition finished = lock.newCondition();
    
    public VideoRecorder(String outputFile, WebcamFeed videoFeed, AudioFeed audioFeed) {
        logger.info("Start recording to " + outputFile);
        writer = ToolFactory.makeWriter(outputFile);

    	int audioIndex = writer.addAudioStream(0, 0, AUDIO_CODEC, TruthBooth.AUDIO_CHANNELS, TruthBooth.AUDIO_RATE);
        int videoIndex = 1;
        if (TruthBooth.userInformation.recordVideo()) {
            videoIndex = writer.addVideoStream(1, 1, VIDEO_CODEC, IRational.make(TruthBooth.VIDEO_FPS), TruthBooth.VIDEO_WIDTH, TruthBooth.VIDEO_HEIGHT);
            IStreamCoder coder = writer.getContainer().getStream(videoIndex).getStreamCoder();
            coder.setProperty("preset", PRESET);
            coder.setProperty("qp", 0);
        }

        vpt = new VideoProcessingThread(writer, videoIndex, audioIndex, false, false);

        sync = new FeedSynchronizer();
        sync.add(audioFeed);
        if (TruthBooth.userInformation.recordVideo()) {
        	sync.add(videoFeed);
        }
    }
    	
	public void start(long recordTimeMS, Runnable onStarted, Runnable onEndRecording) {
    	logger.info("start");
//        vpt.setAudioDelay(100);
        vpt.setPriority(Thread.MIN_PRIORITY);
    	vpt.start();

    	sync.addObserver(vpt);
        sync.start();

        onStarted.run();

        logger.info("Starting processing thread");
    	lock.lock();
    	try {
        	finished.await(recordTimeMS, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			// interrupted before finishing time by user action
		} finally {
			lock.unlock();
		}

        vpt.acceptNoMoreTasks();
        sync.stop();
        sync.deleteObservers();
    	
		onEndRecording.run();

        logger.info("Recording finished, processing...");

        while (!vpt.hasFinished()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        logger.info("Processing finished");

        try {
            logger.info("Closing writer");
            writer.close();
        } catch (Exception e) {
        	logger.error("Error closing writer", e);
        }
    }

	public void forceFileClose() {
        try {
            logger.info("Closing writer");
            writer.close();
        } catch (Exception e) {
        	logger.error("Error closing writer", e);
        }
	}
	
    public void pause() {
    	logger.info("pause");

    	sync.pause();
    }
    
    public void resume() {
    	logger.info("resume");

    	sync.resume();
    }
    
	public void stop() {
    	logger.info("stop");
        sync.stop();
        sync.deleteObservers();

        if (vpt != null) {
        	vpt.cancel();
    	}

        lock.lock();
        try {
        	finished.signal();
        } finally {
        	lock.unlock();
        }
    }
}
