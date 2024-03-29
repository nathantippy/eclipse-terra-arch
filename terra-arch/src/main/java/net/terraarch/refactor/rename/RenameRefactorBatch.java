package net.terraarch.refactor.rename;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

import net.terraarch.util.AppendableBuilder;

import net.terraarch.tf.parse.ParseBuffer;
import net.terraarch.tf.structure.StructureDataModule;

import net.terraarch.TerraArchActivator;
import net.terraarch.util.FileUtils;

public class RenameRefactorBatch extends Refactoring {

	private RenameRequests transientRequest;
	
	private final StructureDataModule module;
	private RenameWizardPageBatch newNameHolder;
	
	
	public RenameRefactorBatch(ITextSelection tsel, StructureDataModule module) {
		
		this.module  = module;
	}
	
	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor monitor)
			throws CoreException, OperationCanceledException {
		return new RefactoringStatus(); //no need to do anything else we already check in the handler
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor arg0)
			throws CoreException, OperationCanceledException {
		
		RefactoringStatus result = new RefactoringStatus();
		
		//log why the refactor could not be done
		if (!module.isClean()) {
			result.addError("unable to refactor due to parse errors in this module/project");
		}

		transientRequest = newNameHolder.buildNameRequests();
		
		return result;
	}

	@Override
	public Change createChange(IProgressMonitor arg0) throws CoreException, OperationCanceledException {
		
		CompositeChange cc = new CompositeChange("batch rename");
		Set<String> done = changesFromOpenEditors(module.moduleFolder, cc);
		changesFromFiles(cc, done);
		
		return cc;
	}

	public void changesFromFiles(CompositeChange cc, Set<String> done) {
		module.visitRecords(r-> {

			final IFile file = TerraArchActivator.getIFile(r);
			if (null==file) {//if outside workspace do not support these files
				return false;
			}
			if (!done.contains(file.getName())) {
				//Now process files which are not in memory
				try {
					AppendableBuilder builder = new AppendableBuilder();
					builder.consumeAll(file.getContents());
					
					collectChange(cc, file, builder.toBytes());
							
				} catch (IOException e) {
					e.printStackTrace();
				} catch (CoreException e) {
					e.printStackTrace();
				}
								
			};
			return true;
		});
	}


	@Override
	public String getName() {
		return "Simple Rename";
	}
	
	
	private Set<String> changesFromOpenEditors(final File targetFolder, CompositeChange cc) {
		
		final Set<String> doneFromEditor = new HashSet<String>();				
		
		FileUtils.visitEditors(p-> {
			//all editors even outside project
			if ((p instanceof ITextEditor) && (p.getEditorInput() instanceof IFileEditorInput)) {
					IFileEditorInput input = (IFileEditorInput)p.getEditorInput();																						
					final IFile inputFile = input.getFile();
					final File thisEditorsParentFolder = inputFile.getRawLocation().toFile().getAbsoluteFile().getParentFile();
												
					//only process those in the same module.
					if (targetFolder.equals( thisEditorsParentFolder )) {
						//assert(targetFolder.equals(module.moduleFolder)) : "ony process marks for the module containing the changed file.";
						ITextEditor p1 = (ITextEditor) p;
						IDocument document = p1.getDocumentProvider().getDocument(p1.getEditorInput());
						
						collectChange(cc, inputFile, document.get().getBytes());
						
					}
					
					doneFromEditor.add(input.getFile().getName());//store on list so we do not use disk version.
			} 
		}); //these are open editors in the background
		return doneFromEditor;
	}
	

	private final ParseBuffer parseBuffer = new ParseBuffer();
	private void collectChange(CompositeChange cc, IFile file, byte[] data) {
		TextFileChange result = new TextFileChange( file.getName(), file );
		MultiTextEdit fileChangeRootEdit = new MultiTextEdit();
		result.setEdit( fileChangeRootEdit );
		
		RenameFieldGatherParse rfgp = new RenameFieldGatherParse(transientRequest, (off,len,txt)-> {
			fileChangeRootEdit.addChild( new ReplaceEdit(off,len,txt) );
		});
		rfgp.fileStart(null, file.getFullPath().toFile().getAbsolutePath());
		
		synchronized(parseBuffer) {
			boolean isClean = parseBuffer.tokenizeDocument(data, rfgp);	
		}
    	
		if (fileChangeRootEdit.getChildrenSize()>0) {
			cc.add(result); //only add this change if there are real text changes to be done.
		}
	}

	public void setRenamePage(RenameWizardPageBatch renameWizardPage) {
		this.newNameHolder = renameWizardPage;
	}
	
	
//	private Change createRenameChange() {
//	
//	// create a change object for the file that contains the property
//	// which the user has selected to rename
//	IFile file = info.getSourceFile();
//	
//	TextFileChange result = new TextFileChange( file.getName(), file );
//	// a file change contains a tree of edits, first add the root of them
//	MultiTextEdit fileChangeRootEdit = new MultiTextEdit();
//	result.setEdit( fileChangeRootEdit );
//	
//	// edit object for the text replacement in the file, this is the only child
//	ReplaceEdit edit = new ReplaceEdit( info.getOffset(),
//										info.getOldName().length(),
//										info.getNewName() );
//	
//	
//	//TODO: EEE, need to generate this change ahead of time with the scanner
//	//     one change needs:   IFile,  offsetIdx, oldLength, newTextBody
//	//     repeate as needed...
//	
//	
//	fileChangeRootEdit.addChild( edit );
//	return result;
//}


	
	
}
