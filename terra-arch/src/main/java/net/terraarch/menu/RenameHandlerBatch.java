package net.terraarch.menu;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import net.terraarch.tf.structure.GatheredFieldType;
import net.terraarch.tf.structure.StructureDataModule;
import net.terraarch.tf.structure.walker.GatherProposalsAdapterData;

import net.terraarch.index.IndexModuleFile;
import net.terraarch.refactor.rename.RenameRefactorBatch;
import net.terraarch.refactor.rename.RenameWizardBatch;

public class RenameHandlerBatch extends AbstractHandler {

	private static final ILog logger = Platform.getLog(RenameHandlerBatch.class);
		
	//private final static WorkspaceBuffer wb = new WorkspaceBuffer();
	
	public RenameHandlerBatch() {
		//NOTE: we check this after the dialog is shown so potential users can see the benifit.
		this.setBaseEnabled(true);		
	}
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		//Job.getJobManager().`addJobChangeListener(arg0);
		
	    ISelection selection = HandlerUtil.getCurrentSelection(event);
	    if (!selection.isEmpty() && (selection instanceof ITextSelection)) {
	    	//only bring up rename if we have a selection
	    	ITextSelection tsel = (ITextSelection)selection;
	    	if (!tsel.isEmpty()) {
		    	
		    	ITextEditor textEditor = (ITextEditor)HandlerUtil.getActiveEditor(event);
		    	IDocumentProvider provider = textEditor.getDocumentProvider();
		    	IEditorInput input = HandlerUtil.getActiveEditorInput(event);
		    	IDocument document = provider.getDocument(input);
		    
		    	IndexModuleFile extractModuleFileService = IndexModuleFile.extractModuleFileService(document);
		    	if (null!=extractModuleFileService) {
					final StructureDataModule module = extractModuleFileService.module;
					//only do the rename if the project has a clean compile
			    	if (module.isClean()) { 
			    		//GatherSelection visitor = new GatherSelection();

			    		List<GatherProposalsAdapterData<org.eclipse.swt.widgets.Text>> data = new ArrayList<>();
			    		module.visitRecords(sdr-> {
			    			
			    			sdr.getLocalParser().visitPatterns((bytes, len, id)-> {
			    				data.add(new GatherProposalsAdapterData(-1,null,-1,new String(bytes,0,len),GatheredFieldType.LOCAL));
			    			});
			    			sdr.getVariableParser().visitPatterns((bytes, len, id)-> {
			    				data.add(new GatherProposalsAdapterData(-1,null,-1,new String(bytes,0,len),GatheredFieldType.VARIABLE));
			    			});
			    			sdr.getModuleTypeParser().visitPatterns((bytes, len, id)-> {
			    				data.add(new GatherProposalsAdapterData(-1,null,-1,new String(bytes,0,len),GatheredFieldType.MODULE_TYPE));
			    			});			    			
			    			sdr.getResourceTypeParser().visitPatterns((typeBytes, typeLen, typeId)-> {
			    				sdr.getResourceNameParser((int)typeId).visitPatterns((bytes, len, id)-> {
				    				data.add(new GatherProposalsAdapterData(-1,new String(typeBytes,0,typeLen),-1,new String(bytes,0,len),GatheredFieldType.RESOURCE_NAME));
				    			});
			    			});
			    			sdr.getDataTypeParser().visitPatterns((typeBytes, typeLen, typeId)-> {
			    				sdr.getDataNameParser((int)typeId).visitPatterns((bytes, len, id)-> {
				    				data.add(new GatherProposalsAdapterData(-1,new String(typeBytes,0,typeLen),-1,new String(bytes,0,len),GatheredFieldType.DATASOURCE_NAME));
				    			});
			    			});
			    			sdr.getProviderTypeParser().visitPatterns((typeBytes, typeLen, typeId)-> {
			    				sdr.getProviderNameParser((int)typeId).visitPatterns((bytes, len, id)-> {
				    				data.add(new GatherProposalsAdapterData(-1,new String(typeBytes,0,typeLen),-1,new String(bytes,0,len),GatheredFieldType.PROVIDER_NAME));
				    			});
			    			});
			    			return true;			    			
			    		});
			    	
			    		if (!data.isEmpty()) {
							   try {
								    RenameWizardBatch refactoringWizard = new RenameWizardBatch(
															new RenameRefactorBatch(tsel, module),
															data);

								  //  refactoringWizard.setDialogSettings(ID);PageSize(WIDTH, HEIGHT);
								    RefactoringWizardOpenOperation op = new RefactoringWizardOpenOperation(refactoringWizard);
								 //   op.setShellStyle(SWT.CLOSE | SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL);
									op.run(HandlerUtil.getActiveShell(event), "Batch Rename ");
							   
							   } catch (InterruptedException e) {
									// refactoring got cancelled
								}
						}
			    	}
		    	}
		    	
		    	
	    	}
	    }
		return null;
	}

}
