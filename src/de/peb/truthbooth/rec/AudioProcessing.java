package de.peb.truthbooth.rec;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import javax.sound.sampled.TargetDataLine;

import de.peb.truthbooth.TruthBooth;

public class AudioProcessing {

	private static short[] toShortArray(byte[] buffer) {
		if (buffer.length == 0) {
			return new short[0];
		}
		ByteBuffer buf = ByteBuffer.wrap(buffer);
		short[] ret = new short[buffer.length / 2];
		buf.asShortBuffer().get(ret);
		return ret;
	}
	
	public static void emptyBuffer(TargetDataLine line) {
		byte[] buffer = new byte[line.available()];
		line.read(buffer, 0, buffer.length);
	}

	public static short[] readAudio(TargetDataLine line) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[TruthBooth.AUDIO_BUFFER_SIZE];
		int read = line.read(buffer, 0, TruthBooth.AUDIO_BUFFER_SIZE);
		baos.write(buffer, 0, read);
	
		return toShortArray(baos.toByteArray());
	}

	protected static void dump(short[] audioBuffer) {
	    StringBuffer sb = new StringBuffer(400);
	    sb.append("buffer: ");
	    for (int i = 0; i < 100; i++) {
	        sb.append(audioBuffer[i]).append(" ");
	    }
	}

}
