package de.peb.truthbooth.rec;

import java.util.ArrayList;
import java.util.List;
import java.util.Observer;

public class FeedSynchronizer {
	
	private List<Feed> feeds;
	
	private Clock clock;
	
	public FeedSynchronizer() {
		this.clock = new Clock();
		this.feeds = new ArrayList<Feed>();
	}
	
	public void add(Feed feed) {
		if (feed == null) {
			return;
		}
			
		feeds.add(feed);
		feed.setClock(clock);
	}
	
	public void start() {
		clock.start();
		for (Feed feed : feeds) {
			feed.start();
		}
	}
	
	public void stop() {
		for (Feed feed : feeds) {
			feed.stop();
		}
	}
	
	long elapsed;
	public void pause() {
		elapsed = clock.getElapsed();
		for (Feed feed : feeds) {
			feed.pause();
		}
	}

	public void resume() {
		clock.setElapsed(elapsed);
		for (Feed feed : feeds) {
			feed.resume();
		}
	}

	public void deleteObservers() {
		for (Feed feed : feeds) {
			feed.deleteObservers();
		}
	}
	public void addObserver(Observer obs) {
		for (Feed feed : feeds) {
			feed.addObserver(obs);
		}
	}
}
