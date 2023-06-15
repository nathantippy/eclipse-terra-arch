package net.terraarch;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;

import net.terraarch.outline.TerraArchOutlineView;

public final class CursorPartListener implements IPartListener {
		@Override
		public void partActivated(IWorkbenchPart partRef) {
		}

		@Override
		public void partBroughtToTop(IWorkbenchPart partRef) {
		}

		@Override
		public void partClosed(IWorkbenchPart partRef) {
		}

		@Override
		public void partDeactivated(IWorkbenchPart partRef) {
		}

		@Override
		public void partOpened(IWorkbenchPart partRef) {
			
			try {
				
//				//critical so recommendation view is aware of which outline is showing.
//				if (partRef instanceof RecommendationView) {
//					TerraArchActivator.getDefault().setActiveRecommendationView((RecommendationView)partRef);
//				}
			
//				if (partRef instanceof org.eclipse.ui.internal.views.markers.ProblemsView) {
//					 org.eclipse.ui.internal.views.markers.ProblemsView pv = ( org.eclipse.ui.internal.views.markers.ProblemsView)partRef;
//					 
//					 pv.addPartPropertyListener(l -> {
//						 System.out.println("problems view property: " + l.getProperty());
//					 });
//				//	 System.out.println("------------------startup part: "+partRef.getClass());
//				}
			
//				//this will be active only for this part
//				IContextService serv = partRef.getSite().getService(IContextService.class);
//				//this is open for ALL parts
//			IContextService serv = PlatformUI.getWorkbench().getService(IContextService.class);

		
		
			//was AbstractDecoratedTextEditor but we wanted to narrow this to just TerraArchEditor
			if (partRef instanceof TerraArchEditor) {
				AbstractDecoratedTextEditor adte = (AbstractDecoratedTextEditor)partRef;										
				final IEditorInput ei = adte.getEditorInput();
				if (ei instanceof IFileEditorInput) {
										  
					    StyledText styledText = ((StyledText)adte.getAdapter(Control.class));
				    	
					    //addin listeners
					    styledText.addCaretListener(c -> {

						    //filter this to only TF files....
					    	IFile file = ((IFileEditorInput)ei).getFile();
					    						    	
					    	
						    String ext = file.getFullPath().getFileExtension();					    
						    if ("tf".equals(ext) || "tpl".equals(ext) || "tfvars".equals(ext)) {
					    	
						    	
							    	//when we have focus then update the outline if the outline is open
							    	if (styledText.isFocusControl()) {
							    		    //good usage of the lookup
							    		    final TerraArchOutlineView tempOutlineView = TerraArchActivator.getDefault().getActiveOutlineView();
								    		if (null!=tempOutlineView) {
												tempOutlineView.reactiveCursorJobState.iFile = file;
												tempOutlineView.reactiveCursorJobState.caretOffset = c.caretOffset;
												///////////////////////////////////////////////////////
												//make the outline view sync with our cursor location
												tempOutlineView.reactiveCursorJobState.bumpJob();
											}
							    	}
							 }
					    }
			    		);
					
				}
			}
			
			
		} catch (Throwable t) {
			TerraArchActivator.logger.error("partOpened",t);
		}				
			
		}
	}