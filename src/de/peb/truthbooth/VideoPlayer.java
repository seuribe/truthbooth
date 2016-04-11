package de.peb.truthbooth;

/*******************************************************************************
 * Copyright (c) 2008, 2010 Xuggle Inc.  All rights reserved.
 *  
 * This file is part of Xuggle-Xuggler-Main.
 *
 * Xuggle-Xuggler-Main is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Xuggle-Xuggler-Main is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Xuggle-Xuggler-Main.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JFrame;

import com.xuggle.xuggler.Global;
import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IPixelFormat;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.IVideoResampler;
import com.xuggle.xuggler.video.ConverterFactory;
import com.xuggle.xuggler.video.IConverter;

import de.peb.truthbooth.ui.VideoMonitoringPanel;

/**
 * Takes a media container, finds the first video stream, decodes that stream,
 * and then plays the audio and video.
 * 
 * This code does a VERY coarse job of matching time-stamps, and thus the audio
 * and video will float in and out of slight sync. Getting time-stamps
 * syncing-up with audio is very system dependent and left as an exercise for
 * the reader.
 * 
 * @author aclarke
 * 
 */
public class VideoPlayer {

	private static long mSystemVideoClockStartTime;

	private static long mFirstVideoTimestampInStream;

	/**
	 * Takes a media container (file) as the first argument, opens it, plays
	 * audio as quickly as it can, and opens up a Swing window and displays
	 * video frames with <i>roughly</i> the right timing.
	 * 
	 * @param args
	 *            Must contain one string which represents a filename
	 */
	public static void main(String[] args) {

		if (args.length <= 0)
			throw new IllegalArgumentException("must pass in a filename as the first argument");

		if (!IVideoResampler.isSupported(IVideoResampler.Feature.FEATURE_COLORSPACECONVERSION))
			throw new RuntimeException("you must install the GPL version of Xuggler (with IVideoResampler support) for this demo to work");
		
		new VideoPlayer().play(args[0]);
	}

	private IStreamCoder getCoderForType(IContainer container, ICodec.Type type) {
		int numStreams = container.getNumStreams();
		
		for (int i = 0; i < numStreams; i++) {
			IStream stream = container.getStream(i);
			IStreamCoder coder = stream.getStreamCoder();
			
			if (coder.getCodecType() == type) {
				return coder;
			}
		}
		return null;
	}

	private IVideoResampler getVideoResample(IStreamCoder videoCoder) {
		IVideoResampler resampler = null;

		if (videoCoder.getPixelType() != IPixelFormat.Type.BGR24) {
			// if this stream is not in BGR24, we're going to need to
			// convert it. The VideoResampler does that for us.
			resampler = IVideoResampler.make(videoCoder.getWidth(),
					videoCoder.getHeight(), IPixelFormat.Type.BGR24,
					videoCoder.getWidth(), videoCoder.getHeight(),
					videoCoder.getPixelType());

			if (resampler == null)
				throw new RuntimeException("could not create color space resampler");
		}
		return resampler;
	}
	
	public void play(String filename) {
		IContainer container = IContainer.make();

		if (container.open(filename, IContainer.Type.READ, null) < 0)
			throw new IllegalArgumentException("could not open file: " + filename);

		IStreamCoder videoCoder = getCoderForType(container, ICodec.Type.CODEC_TYPE_VIDEO);
		IStreamCoder audioCoder = getCoderForType(container, ICodec.Type.CODEC_TYPE_AUDIO);

		if (videoCoder == null || audioCoder == null)
			throw new RuntimeException("could not find audio or video stream in container");

		SourceDataLine audioOutput = null;
		try {
			 audioOutput = getAudioOutput(audioCoder);
		} catch (LineUnavailableException ex) {
			throw new RuntimeException("unable to open sound device on your system for playing back");
		}

		VideoMonitoringPanel videoPanel = new VideoMonitoringPanel();
		videoPanel.setSize(videoCoder.getWidth(), videoCoder.getHeight());
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setContentPane(videoPanel);
		frame.pack();
		frame.setVisible(true);

		play(container, videoCoder, audioCoder, videoPanel, audioOutput);

		if (videoCoder != null) {
			videoCoder.close();
			videoCoder = null;
		}
		if (audioCoder != null) {
			audioCoder.close();
			audioCoder = null;
		}
		if (container != null) {
			container.close();
			container = null;
		}

		closeSoundLine(audioOutput);
	}
	
	public void play(IContainer container, IStreamCoder videoCoder, IStreamCoder audioCoder, VideoMonitoringPanel videoPanel, SourceDataLine audioOutput) {

		int videoStreamIndex = videoCoder.getStream().getIndex();
		int audioStreamIndex = audioCoder.getStream().getIndex();
		
		if (videoCoder.open(null, null) < 0)
			throw new RuntimeException("could not open video decoder");
		if (audioCoder.open(null, null) < 0)
			throw new RuntimeException("could not open audio decoder");

		IVideoResampler resampler = getVideoResample(videoCoder);

		/*
		 * Now, we start walking through the container looking at each packet.
		 */
		IPacket packet = IPacket.make();
		mFirstVideoTimestampInStream = Global.NO_PTS;
		mSystemVideoClockStartTime = 0;
		
		final int WITDH = videoCoder.getWidth();
		final int HEIGHT = videoCoder.getHeight();
		/*
		 * We allocate a new picture to get the data out of Xuggler
		 */
		IVideoPicture picture = IVideoPicture.make(videoCoder.getPixelType(), WITDH, HEIGHT);
	    IConverter converter = ConverterFactory.createConverter(ConverterFactory.XUGGLER_BGR_24,
	    		(resampler == null ? picture : IVideoPicture.make(resampler.getOutputPixelFormat(), WITDH, HEIGHT)));

		while (container.readNextPacket(packet) >= 0) {
			/*
			 * Now we have a packet, let's see if it belongs to our video stream
			 */
			if (packet.getStreamIndex() == videoStreamIndex && videoPanel != null) {


				/*
				 * Now, we decode the video, checking for any errors.
				 */
				int bytesDecoded = videoCoder.decodeVideo(picture, packet, 0);
				if (bytesDecoded < 0)
					throw new RuntimeException("got error decoding video");

				/*
				 * Some decoders will consume data in a packet, but will not be
				 * able to construct a full video picture yet. Therefore you
				 * should always check if you got a complete picture from the
				 * decoder
				 */
				if (picture.isComplete()) {
					IVideoPicture newPic = picture;
					/*
					 * If the resampler is not null, that means we didn't get
					 * the video in BGR24 format and need to convert it into
					 * BGR24 format.
					 */
					if (resampler != null) {
						// we must resample
						newPic = IVideoPicture.make(resampler.getOutputPixelFormat(), WITDH, HEIGHT);
						if (resampler.resample(newPic, picture) < 0)
							throw new RuntimeException("could not resample video");
					}
					if (newPic.getPixelType() != IPixelFormat.Type.BGR24)
						throw new RuntimeException("could not decode video as BGR 24 bit data");

					long delay = millisecondsUntilTimeToDisplay(newPic);
					// if there is no audio stream; go ahead and hold up the
					// main thread. We'll end
					// up caching fewer video pictures in memory that way.
					try {
						if (delay > 0)
							Thread.sleep(delay);
					} catch (InterruptedException e) {
						return;
					}

					// And finally, convert the picture to an image and display
					// it
//					mScreen.setImage(Utils.videoPictureToImage(newPic));
					videoPanel.setImage(converter.toImage(newPic));
				}
			} else if (packet.getStreamIndex() == audioStreamIndex && audioOutput != null) {
				/*
				 * We allocate a set of samples with the same number of channels
				 * as the coder tells us is in this buffer.
				 * 
				 * We also pass in a buffer size (1024 in our example), although
				 * Xuggler will probably allocate more space than just the 1024
				 * (it's not important why).
				 */
				IAudioSamples samples = IAudioSamples.make(1024, audioCoder.getChannels());

				/*
				 * A packet can actually contain multiple sets of samples (or
				 * frames of samples in audio-decoding speak). So, we may need
				 * to call decode audio multiple times at different offsets in
				 * the packet's data. We capture that here.
				 */
				int offset = 0;

				/*
				 * Keep going until we've processed all data
				 */
				while (offset < packet.getSize()) {
					int bytesDecoded = audioCoder.decodeAudio(samples, packet, offset);
					if (bytesDecoded < 0)
						throw new RuntimeException("got error decoding audio");
					offset += bytesDecoded;
					/*
					 * Some decoder will consume data in a packet, but will not
					 * be able to construct a full set of samples yet. Therefore
					 * you should always check if you got a complete set of
					 * samples from the decoder
					 */
					if (samples.isComplete()) {
						// note: this call will block if Java's sound buffers
						// fill up, and we're
						// okay with that. That's why we have the video
						// "sleeping" occur
						// on another thread.
						playJavaSound(samples, audioOutput);
					}
				}
			}
		}
	}

	private static long millisecondsUntilTimeToDisplay(IVideoPicture picture) {
		/**
		 * We could just display the images as quickly as we decode them, but it
		 * turns out we can decode a lot faster than you think.
		 * 
		 * So instead, the following code does a poor-man's version of trying to
		 * match up the frame-rate requested for each IVideoPicture with the
		 * system clock time on your computer.
		 * 
		 * Remember that all Xuggler IAudioSamples and IVideoPicture objects
		 * always give timestamps in Microseconds, relative to the first decoded
		 * item. If instead you used the packet timestamps, they can be in
		 * different units depending on your IContainer, and IStream and things
		 * can get hairy quickly.
		 */
		long millisecondsToSleep = 0;
		if (mFirstVideoTimestampInStream == Global.NO_PTS) {
			// This is our first time through
			mFirstVideoTimestampInStream = picture.getTimeStamp();
			// get the starting clock time so we can hold up frames
			// until the right time.
			mSystemVideoClockStartTime = System.currentTimeMillis();
			millisecondsToSleep = 0;
		} else {
			long systemClockCurrentTime = System.currentTimeMillis();
			long millisecondsClockTimeSinceStartofVideo = systemClockCurrentTime
					- mSystemVideoClockStartTime;
			// compute how long for this frame since the first frame in the
			// stream.
			// remember that IVideoPicture and IAudioSamples timestamps are
			// always in MICROSECONDS,
			// so we divide by 1000 to get milliseconds.
			long millisecondsStreamTimeSinceStartOfVideo = (picture
					.getTimeStamp() - mFirstVideoTimestampInStream) / 1000;
			final long millisecondsTolerance = 50; // and we give ourselfs 50 ms
													// of tolerance
			millisecondsToSleep = (millisecondsStreamTimeSinceStartOfVideo - (millisecondsClockTimeSinceStartofVideo + millisecondsTolerance));
		}
		return millisecondsToSleep;
	}

	private static SourceDataLine getAudioOutput(IStreamCoder aAudioCoder) throws LineUnavailableException {

		AudioFormat audioFormat = new AudioFormat(
				aAudioCoder.getSampleRate(),
				(int) IAudioSamples.findSampleBitDepth(aAudioCoder.getSampleFormat()), aAudioCoder.getChannels(),
				true, false);/* xuggler defaults to signed 16 bit samples */
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);

		SourceDataLine mLine = (SourceDataLine) AudioSystem.getLine(info);
		mLine.open(audioFormat);
		mLine.start();
		return mLine;
	}

	private static void playJavaSound(IAudioSamples aSamples, SourceDataLine line) {
		/*
		 * We're just going to dump all the samples into the line.
		 */
		byte[] rawBytes = aSamples.getData().getByteArray(0, aSamples.getSize());
		line.write(rawBytes, 0, aSamples.getSize());
	}

	private static void closeSoundLine(SourceDataLine line) {
		if (line != null) {
			line.drain();
			line.close();
		}
	}
}