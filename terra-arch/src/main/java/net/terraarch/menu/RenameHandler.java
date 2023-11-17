package net.terraarch.menu;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;


import net.terraarch.tf.parse.ParseBuffer;
import net.terraarch.tf.structure.GatheredFieldType;
import net.terraarch.tf.structure.StructureDataModule;
import net.terraarch.tf.structure.walker.GatherInBoundProposals;
import net.terraarch.tf.structure.walker.GatherSelection;

import net.terraarch.TerraArchActivator;
import net.terraarch.index.IndexModuleFile;
import net.terraarch.refactor.rename.RenameRefactor;
import net.terraarch.refactor.rename.RenameWizard;

public class RenameHandler extends AbstractHandler {

	private static final ILog logger = Platform.getLog(RenameHandler.class);
		
	private final static ParseBuffer parseBuffer = new ParseBuffer();
	
	public RenameHandler() {
		this.setBaseEnabled(true);	
	}
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
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
		    	if (null != extractModuleFileService) {
					final StructureDataModule module = extractModuleFileService.module;
			    		
			    		GatherSelection visitor = new GatherSelection();
						GatherInBoundProposals<GatherSelection> gp = new GatherInBoundProposals<GatherSelection>(
								                        module, tsel.getOffset(), visitor, true, 
								                        TerraArchActivator.getDefault().sdmm);
						//System.out.println("scanning the file");
						if (parseBuffer.tokenizeDocument(document.get().getBytes(), gp)) {
							//System.out.println("type: "+String.valueOf(visitor.getTypeValue()));
							GatheredFieldType type = visitor.getType();
							if (null!=type && type.supportsRename) {
					    		//only show the wizard if this file turns to to be clean
					    						    		
								   try {
										RefactoringWizard refactoringWizard = new RenameWizard(
												new RenameRefactor(tsel, visitor, module),
												visitor);
										
										RefactoringWizardOpenOperation op= new RefactoringWizardOpenOperation(refactoringWizard);
										op.run(HandlerUtil.getActiveShell(event), "Rename "+type);
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
