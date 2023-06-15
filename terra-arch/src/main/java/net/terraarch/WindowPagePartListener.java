package net.terraarch;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

public final class WindowPagePartListener implements IWindowListener {
	
	private final IPartListener partListenerForCursor;
	public final ILog logger = Platform.getLog(WindowPagePartListener.class); 
	
	public WindowPagePartListener(IPartListener partListenerForCursor) {
		this.partListenerForCursor = partListenerForCursor;
	}
	
	@Override
	public void windowActivated(IWorkbenchWindow win) {
		
		try {
			for(IWorkbenchPage p: win.getPages()) {						
				p.removePartListener(partListenerForCursor);
				p.addPartListener(partListenerForCursor);
									
			}
		} catch (Throwable t) {
			logger.error("windowActivated",t);
		}
	}

	@Override
	public void windowClosed(IWorkbenchWindow arg0) {
	}

	@Override
	public void windowDeactivated(IWorkbenchWindow arg0) {
	}

	@Override
	public void windowOpened(IWorkbenchWindow win) {
		try {
			
			for(IWorkbenchPage p: win.getPages()) {
				p.removePartListener(partListenerForCursor);
				p.addPartListener(partListenerForCursor);
			}
	
		} catch (Throwable t) {
			logger.error("windowOpened",t);
		}
	}
}