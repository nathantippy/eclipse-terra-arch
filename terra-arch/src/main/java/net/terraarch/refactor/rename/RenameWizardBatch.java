package net.terraarch.refactor.rename;

import java.util.List;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.swt.SWT;

import net.terraarch.tf.structure.walker.GatherProposalsAdapterData;
import net.terraarch.tf.structure.walker.GatherSelection;

public class RenameWizardBatch extends RefactoringWizard {

	final RenameWizardPageBatch renameWizardPage;
	
	public RenameWizardBatch(RenameRefactorBatch refactoring, List<GatherProposalsAdapterData<org.eclipse.swt.widgets.Text>> data) {
		super(refactoring, DIALOG_BASED_USER_INTERFACE | CHECK_INITIAL_CONDITIONS_ON_OPEN);
		//NOTE: we already did all this work so we are going to skip it on open, the flag is not to be used above
		this.setInitialConditionCheckingStatus(new RefactoringStatus());
		//this is very quick and does not need any progress monitoring
		this.setNeedsProgressMonitor(false);		
		this.setChangeCreationCancelable(false);
		
		this.renameWizardPage = new RenameWizardPageBatch("RenameWizardBatchInputPage", data);
				
	    
		refactoring.setRenamePage(this.renameWizardPage);
		
		
	}
	

	@Override
	protected void addUserInputPages() {
		
		renameWizardPage.setTitle("Rename Wizard");
		
		renameWizardPage.setDescription("Enter the new value for each field\n"
				                      + "All usages and the defintion will be replaced");
		
		addPage(renameWizardPage);		
	}

}
