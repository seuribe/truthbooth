package de.peb.truthbooth.rec;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Comparator;
import java.util.Observable;
import java.util.Observer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.PriorityBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xuggle.mediatool.IMediaWriter;

import de.peb.truthbooth.TruthBooth;
import de.peb.truthbooth.rec.EncodeTask.AudioEncodeTask;
import de.peb.truthbooth.rec.EncodeTask.VideoEncodeTask;

public class VideoProcessingThread extends Thread implements Observer {

	private final IMediaWriter writer;
    private boolean stop = false;
    private boolean finished = false;
    private boolean acceptTasks = true;
    private int totalQueuedTasks = 0;
    private ObjectOutputStream oos;
	private int videoIndex;
	private int audioIndex;
    private boolean serialize = false;
    private final Queue<EncodeTask> queue;
    private long audioDelay = 0;

    private static Logger logger = LoggerFactory.getLogger("VideoRecorder");

    public VideoProcessingThread(IMediaWriter writer, int videoIndex, int audioIndex, boolean serialize, boolean orderedQueue) {
		this.writer = writer;
        this.serialize = serialize;
		this.videoIndex = videoIndex;
		this.audioIndex = audioIndex;
        try {
            if (serialize) {
                FileOutputStream fos = new FileOutputStream(TruthBooth.userInformation.getDumpFilename());
                oos = new ObjectOutputStream(fos);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
        
        this.queue = orderedQueue ? createOrderedQueue() : new ConcurrentLinkedQueue<EncodeTask>();
    }

    public void setAudioDelay(long newAudioDelay) {
    	this.audioDelay = newAudioDelay;
    }

    private Queue<EncodeTask> createOrderedQueue() {
		return new PriorityBlockingQueue<EncodeTask>(2000, new Comparator<EncodeTask>() {
	        @Override
	        public int compare(EncodeTask o1, EncodeTask o2) {
	            long diff = o1.getTime() - o2.getTime();
	            if (diff < 0) {
	                return -1;
	            } else if (diff == 0) {
	                return 0;
	            } else {
	                return 1;
	            }
	        }
	    });
	}

	public void add(EncodeTask task) {
        if (stop || !acceptTasks) {
            return;
        }

        logger.debug("task " + totalQueuedTasks + " queud, time " + task.getTime());
        synchronized (queue) {
            totalQueuedTasks++;
            queue.add(task);
            queue.notify();
        }
    }

	@Override
	public void update(Observable o, Object obj) {
		if (obj == null) {
			return;
		}
		if (obj instanceof AudioSample) {
			AudioSample sample = (AudioSample)obj;
//			logger.debug("new audio sample observed @" + sample.position);
			AudioEncodeTask aet = new AudioEncodeTask(sample.buffer, sample.position + audioDelay, audioIndex);
			add(aet);
		} else if (obj instanceof ImageSample) {
			ImageSample sample = (ImageSample)obj;
//			logger.debug("new video sample observed @" + sample.time);
			VideoEncodeTask vet = new VideoEncodeTask(sample.image, sample.time, videoIndex);
			add(vet);
		}
	}

    public void run() {
        logger.info("Video Processing Task starting...");
        stop = false;
        long encodedVideoFrames = 0;
    	logger.info(Thread.currentThread().getName() + " started");
        while (!stop) {
        	logger.debug(Thread.currentThread().getName() + " alive");
            EncodeTask task = null;
            while ((task = queue.poll()) != null) {
                logger.debug("Running encode task " + task);
                if (serialize && oos != null) {
                    try {
                        oos.writeObject(task);
                    } catch (IOException ex) {
                        logger.warn("Could not serialize encode task");
                    }
                }
                try {
                    task.encode(writer);
                } catch (Exception e) {
                	acceptTasks = false;
                	logger.error("Unexpected exception while encoding", e);
                }

                if (task instanceof EncodeTask.VideoEncodeTask) {
                    encodedVideoFrames++;
                }
            }
            if (!acceptTasks) {
                break;
            }
            synchronized (queue) {
                try {
                	logger.debug(Thread.currentThread().getName() + " pre queue wait");
                    queue.wait();
                } catch (InterruptedException e) {
                	logger.warn("Interrupted while waiting on queue", e);
                }
            }
        }
        finished = true;
        logger.info("Video Processing finished, clearing up memory. Current: " + Runtime.getRuntime().freeMemory());
        queue.clear();
        System.gc();
        logger.info("Video Processing Task finished - " + encodedVideoFrames + " images encoded, " + totalQueuedTasks + " tasks, memory: " + Runtime.getRuntime().freeMemory());
    	logger.info(Thread.currentThread().getName() + " dead");
    }

    public void cancel() {
        logger.info("Video Processing thread cancelling");
    	acceptTasks = false;
        queue.clear();
    	stop = true;
        synchronized (queue) {
            queue.notify();
        }
    }
    public void acceptNoMoreTasks() {
        logger.info("Video Processing thread accepting no more tasks");
        acceptTasks = false;
    	stop = true;
        synchronized (queue) {
            queue.notify();
        }
    }

    public double progress() {
        return 1f - (float) queue.size() / totalQueuedTasks;
    }

    public boolean hasFinished() {
        synchronized (queue) {
            queue.notify();
        }
        return finished;
    }
}