package net.terraarch.util;

import java.util.function.Consumer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import net.terraarch.proposals.ContentAssistProcessor;

public class FileUtils {

	private static final ILog logger = Platform.getLog(FileUtils.class);
	
	public static IFile toIFile(String absolutePath) {
		try {
			return ResourcesPlugin.getWorkspace()
					              .getRoot()
					              .getFileForLocation(Path.fromOSString(absolutePath));
		} catch (Throwable t) {
			logger.error("toIFile",t);
			return null;
		}
	}


	public static void visitEditors(Consumer<IEditorPart> consumer) {
		try {
		IWorkbench workbench = PlatformUI.getWorkbench();
		if (null!=workbench) {
			IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
			if (null!=activeWorkbenchWindow) {
				IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
				if (null!=activePage) {
					IEditorReference[] ref = activePage.getEditorReferences();
					int x = ref.length;
					while (--x>=0) {
					    IEditorPart p = ref[x].getEditor(false);			
						if (null!=p) {
							consumer.accept(p);
						}
					}
				}
			}
		}
		} catch (Throwable t) {
			logger.error("visitEditors",t);

		}
	}
	
	
}
