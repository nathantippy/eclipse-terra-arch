package net.terraarch.refactor.rename;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import net.terraarch.terraform.structure.GatheredFieldType;
import net.terraarch.terraform.structure.walker.GatherProposalsAdapterData;
import net.terraarch.terraform.structure.walker.GatherSelection;

public class RenameWizardPageBatch extends UserInputWizardPage {

	final List<GatherProposalsAdapterData<org.eclipse.swt.widgets.Text>> data;
	
	public RenameWizardPageBatch(String name, List<GatherProposalsAdapterData<org.eclipse.swt.widgets.Text>> data) {
		super(name);
		this.data = data;
		
	}
	
	@Override
	public void createControl(Composite parent) {
		
		Composite preferencesComposite = new Composite(parent, 0);

		preferencesComposite.setLayout(new GridLayout(1,true));
		GridData prefCompGridData = new GridData(GridData.FILL_BOTH  );
		preferencesComposite.setLayoutData(prefCompGridData);
		buildReplacementControlls(preferencesComposite);
	
		
		ScrolledComposite scrolledComposite = new ScrolledComposite(preferencesComposite, SWT.BORDER 
				| SWT.V_SCROLL 
				//| SWT.H_SCROLL 
				);
		
		GridData scrollGridData = new GridData(GridData.FILL_BOTH);
		scrolledComposite.setLayoutData(scrollGridData);
		scrolledComposite.setAlwaysShowScrollBars(true);
		
		
		Composite sourceControlGroup = new Composite(
				scrolledComposite
				//preferencesComposite
				, SWT.NO_REDRAW_RESIZE);
		sourceControlGroup.setLayout(new GridLayout(3, false));
		sourceControlGroup.setLayoutData( new GridData(GridData.FILL_BOTH) );
		
		//Map Text fields back to specific values..	
		Collections.sort(data);
		
		//AtomicInteger x = new AtomicInteger();
		data.forEach(d-> {
			//keep the text field for later so we can grab the data	
			
					d.field = RenameWizardPage.buildEditorRow(
								GatherSelection.buildSourceDescription(
										new StringBuilder(), d).toString(), 
										GatherSelection.buildTargetDescription(new StringBuilder(), d).toString(), d.nameValue,
										sourceControlGroup
							);
		});
		sourceControlGroup.pack();
		Point computeSize = sourceControlGroup.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		scrolledComposite.setMinSize(computeSize);
		
		scrolledComposite.setContent(sourceControlGroup);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.pack();
		
		setControl(preferencesComposite);
		preferencesComposite.pack();
		
		//this.getShell().pack();
		//this.getShell().layout();
		//very import to ensure we can see the full width at all times
		Point computeSizeTop = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
		this.getShell().setMinimumSize(computeSizeTop.x+40, 400);
				
	}

	private void buildReplacementControlls(Composite preferencesComposite) {
		Group replacementsControlGroup = new Group(preferencesComposite, SWT.NONE);
		replacementsControlGroup.setLayout(new GridLayout(5, false));
		replacementsControlGroup.setLayoutData(new GridData(GridData.END, GridData.BEGINNING, true, false, 1, 1));
		
		//space, select(addPrefix), disabled,  text,  go
		//space, select(replace),   old,       new,   go
		Label label = new Label(replacementsControlGroup, SWT.NONE);
		label.setText("  ");
		label.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, true, false, 1, 1));
		
		
		final int default_selection = 0;
     	final Combo selector = new Combo(replacementsControlGroup, SWT.READ_ONLY);
		
		List<GatheredFieldType> typeOnSelection = new ArrayList<GatheredFieldType>();//starting at zero to match itemIdx
	
		int textBegin;
		selector.add("replace text",                          textBegin=typeOnSelection.size());
		typeOnSelection.add(null);
								
		addTypesToSelector(selector, typeOnSelection,
				"replace text in variables", 
				"replace text in locals", 
				"replace text in resources", 
				"replace text in datasources", 
				"replace text in modules", 
				"replace text in provider alias");

		
		int textEndEx;
		int regexBegin;
		selector.add("replace regex", regexBegin = typeOnSelection.size());
		typeOnSelection.add(null);
		textEndEx = regexBegin;
				
		addTypesToSelector(selector, typeOnSelection,
				"replace regex in variables", 
				"replace regex in locals", 
				"replace regex in resources", 
				"replace regex in datasources", 
				"replace regex in modules", 
				"replace regex in provider alias");
		
				
		int regexEndEx;
		int prefixBegin;
		selector.add("add prefix",                          prefixBegin=typeOnSelection.size());
		typeOnSelection.add(null);
		regexEndEx = prefixBegin;
		
		
		addTypesToSelector(selector, typeOnSelection,
				"add prefix to variables", 
				"add prefix to locals", 
				"add prefix to resources", 
				"add prefix to datasources", 
				"add prefix to modules", 
				"add prefix to provider alias");
		
	
		int prefixEndEx;
		int suffixBegin;
		selector.add("add suffix",                          suffixBegin = typeOnSelection.size());
		typeOnSelection.add(null);
		prefixEndEx = suffixBegin;
		

		addTypesToSelector(selector, typeOnSelection,
				"add suffix to variables", 
				"add suffix to locals", 
				"add suffix to resources", 
				"add suffix to datasources", 
				"add suffix to modules", 
				"add suffix to provider alias");
		
		
		int suffixEndEx = typeOnSelection.size();			
		
		selector.setLayoutData(new GridData(GridData.CENTER, GridData.CENTER, false, false, 1, 1));
		
		
		//////////////////////////////////////////////////////////////////////////////
		//////////////////////////////////////////////////////////////////////////////
		final Composite oldComp = new Composite(replacementsControlGroup, 0);
		oldComp.setLayout(new GridLayout(2, false));
		oldComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL, GridData.BEGINNING, true, false, 1, 1));
		
		final Label oldLabel = new Label(oldComp, SWT.NONE);
		oldLabel.setText("match:");
		oldLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL, GridData.CENTER, true, true, 1, 1));
		final Text textOld = new Text(oldComp, SWT.BORDER);
		textOld.addVerifyListener(new RenameWizardValidateText(regexEndEx, regexBegin, selector));

		GridData textOldGridData = new GridData(GridData.FILL_HORIZONTAL, GridData.CENTER, true, false, 1, 1);
		textOldGridData.widthHint = 160;
		textOld.setLayoutData(textOldGridData);

		/////////////////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////
		final Composite newComp = new Composite(replacementsControlGroup, 0);
		newComp.setLayout(new GridLayout(2, false));
		newComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL, GridData.BEGINNING, true, false, 1, 1));
		
		final Label newLabel = new Label(newComp, SWT.NONE);
		newLabel.setText("replace:");
		GridData newLabelGridData = new GridData(GridData.FILL_HORIZONTAL, GridData.CENTER, true, true, 1, 1);
		newLabel.setLayoutData(newLabelGridData);
		newLabelGridData.widthHint = 80;
		
		
		final Text textNew = new Text(newComp, SWT.BORDER);
		textNew.addVerifyListener(new RenameWizardValidateText2());
		GridData textNewGridData = new GridData(GridData.FILL_HORIZONTAL, GridData.CENTER, true, false, 1, 1);
		textNewGridData.widthHint = 260;
		textNew.setLayoutData(textNewGridData);
		
		/////////////////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////
		
		final Button button = new Button(replacementsControlGroup, SWT.NONE);
		button.setText("Apply To Wizard Fields");
		button.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false, 1, 1));
		button.addSelectionListener(new RenameWizardButtonSelection(this, textNew, suffixBegin, regexBegin, textOld, typeOnSelection, prefixEndEx,
				textBegin, regexEndEx, prefixBegin, selector, textEndEx, suffixEndEx)
		);
		
		///////////////////////
		///////////////////////
		
		selector.addSelectionListener(new RenameWizardMatchSelectionListener(textEndEx, selector, prefixEndEx, textOld, prefixBegin, suffixEndEx, newLabel,
				textBegin, suffixBegin, oldLabel, regexBegin, regexEndEx)
		);
		selector.select(default_selection);
		
	}

	private void addTypesToSelector(final Combo selector, List<GatheredFieldType> typeOnSelection, String a, String b,
			String c, String d, String e, String f) {
		if (hasInstance(data, GatheredFieldType.VARIABLE)) { //ONLY SHOW THE VALID OPTIONS BASED ON WHAT IS PRESENT
			selector.add(a,         typeOnSelection.size());
			typeOnSelection.add(GatheredFieldType.VARIABLE);
		}
		if (hasInstance(data, GatheredFieldType.LOCAL)) {//ONLY SHOW THE VALID OPTIONS BASED ON WHAT IS PRESENT
			selector.add(b,            typeOnSelection.size());
			typeOnSelection.add(GatheredFieldType.LOCAL);
		}
		if (hasInstance(data, GatheredFieldType.RESOURCE_NAME)) {//ONLY SHOW THE VALID OPTIONS BASED ON WHAT IS PRESENT
			selector.add(c,         typeOnSelection.size());
			typeOnSelection.add(GatheredFieldType.RESOURCE_NAME);
		}
		if (hasInstance(data, GatheredFieldType.DATASOURCE_NAME)) {//ONLY SHOW THE VALID OPTIONS BASED ON WHAT IS PRESENT
			selector.add(d,       typeOnSelection.size());
			typeOnSelection.add(GatheredFieldType.DATASOURCE_NAME);
		}
		if (hasInstance(data, GatheredFieldType.MODULE_TYPE)) {//ONLY SHOW THE VALID OPTIONS BASED ON WHAT IS PRESENT
			selector.add(e,           typeOnSelection.size());
			typeOnSelection.add(GatheredFieldType.MODULE_TYPE);
		}
		if (hasInstance(data, GatheredFieldType.PROVIDER_NAME)) {//ONLY SHOW THE VALID OPTIONS BASED ON WHAT IS PRESENT
			selector.add(f,    typeOnSelection.size());
			typeOnSelection.add(GatheredFieldType.PROVIDER_NAME);
		}
	}

	private boolean hasInstance(List<GatherProposalsAdapterData<org.eclipse.swt.widgets.Text>> data, GatheredFieldType check) {		
		for(GatherProposalsAdapterData<?> item: data) {
			if (item.type == check) {
				return true;
			}
		}
		return false;
	}


	public RenameRequests buildNameRequests() {
		final RenameRequests result = new RenameRequests();

		data.forEach(d -> {
			switch (d.type) {
				case LOCAL:
					result.addLocalReplacement(d.nameValue, d.field.getText());
					break;
				case VARIABLE:
					result.addVariableReplacement(d.nameValue, d.field.getText());
					break;
				case MODULE_TYPE:
					result.addModuleReplacement(d.nameValue, d.field.getText());
					break;
				case DATASOURCE_NAME:
					result.addDataReplacement(d.categoryType, d.nameValue, d.field.getText());
					break;
				case RESOURCE_NAME:
					result.addResourceReplacement(d.categoryType, d.nameValue, d.field.getText());
					break;
				case PROVIDER_NAME:
					result.addProviderReplacement(d.categoryType, d.nameValue, d.field.getText());
					break;
				default:
					break;
			}
			
		});
		
		return result;
	}
	
}
