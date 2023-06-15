package net.terraarch.util;

import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;

public class ReactiveJob implements ICoreRunnable {

    private static final ILog logger = Platform.getLog(ReactiveJob.class);

	private long lastUpdate;
	private final ReactiveJobState state;
	private ReentrantLock lock = new ReentrantLock();
	
	public ReactiveJob(ReactiveJobState state) {
			
		this.lastUpdate = state.lastCall.get();
		this.state = state;
	
	}
	
	@Override
	public void run(IProgressMonitor arg0) throws CoreException {
		try {
			if (lastUpdate == state.lastCall.get()) {
				//no recent changes so do the processing
				state.run();
				if (lock.tryLock()) {
					try {
						if (lastUpdate != state.lastCall.get()) {
							//we got an update while running
							//try again later for the quiet period
							lastUpdate = state.lastCall.get();
							Job.create(state.name(), this).schedule(state.period());
						} else {
							state.hasJob.set(false);
						}
					} finally {
						lock.unlock();
					}
				}
			} else {
				//try again later for the quiet period
				lastUpdate = state.lastCall.get();
				Job.create(state.name(), this).schedule(state.period());
			}
		} catch (Throwable t) {
			logger.error(state.name(),t);
		}
	}

}
