package net.terraarch.util;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.runtime.jobs.Job;

public abstract class ReactiveJobState {

	public final AtomicBoolean hasJob = new AtomicBoolean();
    public final String name;
    public final int period;
	public final AtomicLong lastCall = new AtomicLong();
	private ReentrantLock lock = new ReentrantLock();
    
	public ReactiveJobState(String name, int period) {
		this.name = name;
		this.period = period;
	}
		
	public abstract void run();

	public String name() {
		return name;
	}

	public long period() {
		return period;
	}

	public void bumpJob() {
		
		if (lock.tryLock()) {
			try {		
				lastCall.set(System.currentTimeMillis());
				//do not create again if it exists...
				if (!hasJob.get()) {
					Job.create(name(), new ReactiveJob(this)).schedule(period() );
					hasJob.set(true);
				}
			} finally {
				lock.unlock();
			}
		}
	}


}
