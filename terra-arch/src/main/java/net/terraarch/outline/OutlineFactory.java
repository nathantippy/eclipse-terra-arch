package net.terraarch.outline;

import java.io.File;
import java.net.URI;
import java.util.HashMap;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import net.terraarch.tf.structure.StructureDataModule;

import net.terraarch.TerraArchActivator;
import net.terraarch.index.IndexModuleFile;
import net.terraarch.recommendation.RecommendationView;

public class OutlineFactory implements IAdapterFactory {

	private TerraArchOutlineView outlineView = null;
	private static final ILog logger = Platform.getLog(OutlineFactory.class);
			
	@Override	
	public IContentOutlinePage getAdapter(Object adaptableObject, Class required) {
		
		try {
			//System.out.println("outline factory getAdapter called: "+required.toString());
			if (IContentOutlinePage.class.equals(required) 
					&& (adaptableObject instanceof AbstractDecoratedTextEditor)) {
				 
				//only create the view if we have a different module or we have none.
				if (null == outlineView || !outlineView.matchesModule((AbstractDecoratedTextEditor)adaptableObject)) {
					
					AbstractDecoratedTextEditor temp = (AbstractDecoratedTextEditor)adaptableObject;
									
					IDocument document = temp.getDocumentProvider().getDocument(temp.getEditorInput());
					IndexModuleFile imf = IndexModuleFile.extractModuleFileService(document);
					//NOTE: this may be loaded outside our workspace so we must stop to build its index now
					if (null==imf) {
						IEditorInput editor = temp.getEditorInput();
						if (editor instanceof IFileEditorInput) {
							IFileEditorInput ifei = (IFileEditorInput)editor;
							imf = new IndexModuleFile(ifei.getFile());
							document.addDocumentListener(imf);
						} else {
							if (editor instanceof FileStoreEditorInput) {
								FileStoreEditorInput fsei = (FileStoreEditorInput)editor;
								try  {
									URI uri = fsei.getURI();
									File file = new File(uri.getPath());
									imf = new IndexModuleFile(file.getParentFile(), file);
									document.addDocumentListener(imf);
								} catch (Throwable t) {
									logger.error("unable to index this module due to: "+t.getMessage());
									return null;
								}
							} else {
								logger.error("the editor is a suprise instance of "+editor.getClass());
							    return null;
							}
						}
					}
							
					TerraArchActivator activator = TerraArchActivator.getDefault();
					outlineView = new TerraArchOutlineView(imf);
					
					final StructureDataModule activeModule = imf.module;
					activator.addDeepReviewAction(
							TerraArchActivator.DEEP_ACTION_OUTLINE, m->{
						if (m==activeModule) {
							outlineView.syncOutline();//update outline to match imf activeModule due to changes
						}
					});
					
//					//TODO: BB register the recommendations
//					activator.addDeepReviewAction(TerraArchActivator.DEEP_ACTION_RECOMMENDATION, m->{
//						//add recomendations update.
//						if (m==activeModule) {
//							RecommendationView recom = TerraArchActivator.getDefault().getActiveRecommendationView();
//							if (null!=recom) {
//								recom.setRecommendations(activeModule.computeRecommendations(),outlineView);
//							}
//						}
//					});					
				} 
				
				//NOTE: this is important so the editors know where to find the right outline for sync
				//      this also allows the Recomendations view to find this outline.
				TerraArchActivator.getDefault().setActiveOutlineView(outlineView);
								
				return outlineView;
				
			}
			logger.error("Unable to build OutlineView. Looking for "+required.getName()+" and was given "+adaptableObject.getClass().getName());
		} catch (Throwable t) {
			logger.error("getAdapter",t);
		}
		return null;
	}

   @Override
   public Class[] getAdapterList() {
      return new Class[] { IContentOutlinePage.class };
   }

	
}
