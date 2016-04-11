package de.peb.truthbooth.rec;

import java.util.Observable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract media feed, providing start/stop/pause/resume functionalities
 * @author Sebastian
 *
 */
public abstract class Feed extends Observable implements Runnable {
	
	private boolean stop;
	private boolean running = false;
	private final long sleepTime;
	private Clock clock;
	
    private static Logger logger = LoggerFactory.getLogger("VideoRecorder");
	
	protected Feed() {
		this(new Clock(), 0);
	}
	
	protected Feed(long sleepTime) {
		this(new Clock(), sleepTime);
	}
	
	protected Feed(Clock clock, long sleepTime) {
		this.sleepTime = sleepTime;
		this.clock = clock;
	}

	protected abstract Object feed();

	protected void init() { }
	protected void deinit() { }
	protected void onPause() { }
	protected void onResume() { }

	public void run() {
		stop = false;
		init();
		clock.start();
		logger.info("Feed thread started");
		while (!stop) {
			Object obj = feed();
			if (obj != null) {
				setChanged();
				notifyObservers(obj);
			}
			if (pause) {
				System.out.println("paused waiting");

				synchronized (pauseLock) {
					try {
						pauseLock.wait();
					} catch (InterruptedException e) {
						//
					}
				}
				System.out.println("paused resuming");
			}
			if (sleepTime > 0) {
				try {
					Thread.sleep(10);
				} catch (Exception e) {
					//
				}
			}
		}
	}
	
	public void setClock(Clock clock) {
		this.clock = clock;
	}
	
	/**
	 * in nanoseconds since the start
	 * @return
	 */
	public long getElapsed() {
		return clock.getElapsed();
	}
	
	public void start() {
		if (pause) {
			resume();
			return;
		}
		if (running) {
			return;
		}
		logger.info("Feed.start()");
		running = true;
		new Thread(this).start();
	}
	
	private boolean pause = false;
	private Object pauseLock = new Object();
	public void pause() {
		pause = true;
		onPause();
	}
	
	public void resume() {
		pause = false;
		onResume();
		synchronized (pauseLock) {
			pauseLock.notifyAll();
		}
	}
	
	public void stop() {
		if (!running) {
			return;
		}
		logger.info("Feed.stop()");
		stop = true;
		deinit();
		running = false;
	}
}
