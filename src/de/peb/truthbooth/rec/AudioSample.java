package de.peb.truthbooth.rec;

public class AudioSample {
	public final short[] buffer;
	public final long position;
	
	public AudioSample(short[] buffer, long position) {
		this.buffer = buffer;
		this.position = position;
	}
}
