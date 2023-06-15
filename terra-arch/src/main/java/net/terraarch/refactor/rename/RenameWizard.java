package net.terraarch.refactor.rename;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

import net.terraarch.terraform.structure.walker.GatherSelection;

public class RenameWizard extends RefactoringWizard {

	final GatherSelection selection;
	final RenameWizardPage renameWizardPage;
	
	public RenameWizard(RenameRefactor refactoring, GatherSelection selection) {
		super(refactoring, DIALOG_BASED_USER_INTERFACE | CHECK_INITIAL_CONDITIONS_ON_OPEN);
		this.selection = selection;
		//NOTE: we already did all this work so we are going to skip it on open, the flag is not to be used above
		this.setInitialConditionCheckingStatus(new RefactoringStatus());
		//this is very quick and does not need any progress monitoring
		this.setNeedsProgressMonitor(false);		
		this.setChangeCreationCancelable(false);
		
		this.renameWizardPage = new RenameWizardPage(
				"RenameWizardInputPage",
				GatherSelection.buildSourceDescription(new StringBuilder(), selection.data.get(0)).toString(),
				GatherSelection.buildTargetDescription(new StringBuilder(), selection.data.get(0)).toString(),
				selection.getNameValue());
		
	
		refactoring.setRenamePage(this.renameWizardPage);
		
	}
	

	@Override
	protected void addUserInputPages() {
		
		renameWizardPage.setTitle("Rename Wizard");
		
		renameWizardPage.setDescription("Enter the new value for this "+selection.getType().title+" field '"+selection.getNameValue()+"'\n"
				                      + "All usages and the defintion will be replaced");
		
		addPage(renameWizardPage);		
	}

}
