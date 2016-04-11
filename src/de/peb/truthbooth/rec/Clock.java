package de.peb.truthbooth.rec;

public class Clock {
	private long start;

	public void setElapsed(long value) {
		start = System.nanoTime() - value;
	}

	public void start() {
		start = System.nanoTime();
	}
	
	public long getElapsed() {
		return System.nanoTime() - start;
	}
}
