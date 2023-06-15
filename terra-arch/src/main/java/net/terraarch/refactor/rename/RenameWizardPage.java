package net.terraarch.refactor.rename;


import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class RenameWizardPage extends UserInputWizardPage {

	//private final RenameRequests requests; //set this with the valeus from the dialog???
	private Text toValue;
	
	private final String source;
	private final String target;
	private final String defaultText;
	
	
	public RenameWizardPage(String name, String source, String target, String defaultText) {
		super(name);
		this.source = source;
		this.target = target;
		this.defaultText = defaultText;
	}

	
	public String getNewText() {
		return toValue.getText();
	}
	
	@Override
	public void createControl(Composite parent) {
		
		Composite preferencesComposite = new Composite(parent, 0);

		preferencesComposite.setLayout(new GridLayout(1,false));
		preferencesComposite.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, true));
		
		
		Group sourceControlGroup = new Group(preferencesComposite, SWT.NONE);
		sourceControlGroup.setLayout(new GridLayout(3, true));
		
		toValue = buildEditorRow(source, target, defaultText, sourceControlGroup);

		setControl(preferencesComposite);
	}

	public static Text buildEditorRow(String source, String target, String defaultText, Composite sourceControlGroup) {
		Label sourceLabel = new Label(sourceControlGroup, SWT.NONE);
		GridData gridDataSource = new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 1, 1);
		sourceLabel.setLayoutData(gridDataSource);
		sourceLabel.setText(source);
				
		
		Label label = new Label(sourceControlGroup, SWT.NONE);
		label.setText(" replace with ");
		GridData gridDataLabel = new GridData(GridData.BEGINNING, GridData.CENTER, true, false, 1, 1);
		label.setLayoutData(gridDataLabel);
		
		
		Composite editGroup = new Composite(sourceControlGroup, SWT.NONE); 
		editGroup.setLayout(new GridLayout(2,false));		
		Label targetLabel = new Label(editGroup, SWT.NONE);
		targetLabel.setText(target);
		GridData gridDataTarget = new GridData(GridData.END, GridData.CENTER, false, false, 1, 1);
		targetLabel.setLayoutData(gridDataTarget);		
		Text toValue = new Text(editGroup, SWT.BORDER);
		toValue.setToolTipText("Enter new value");
		GridData gridData = new GridData(GridData.BEGINNING, GridData.CENTER, true, false, 1, 1);
		
		gridData.widthHint = 240;
	
		toValue.setLayoutData(gridData);
		toValue.setText(defaultText);
		editGroup.pack();
		return toValue;
	}

	
}
