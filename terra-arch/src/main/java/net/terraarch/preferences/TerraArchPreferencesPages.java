package net.terraarch.preferences;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.osgi.service.prefs.BackingStoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

import net.terraarch.tf.parse.ParseState;
import net.terraarch.TerraArchActivator;
import net.terraarch.util.FileUtils;
import net.terraarch.tf.parse.ParseState;
import net.terraarch.tf.parse.doc.ThemeColors;
import net.terraarch.TerraArchActivator;
import net.terraarch.util.BrowserUtils;

public class TerraArchPreferencesPages extends PreferencePage implements IWorkbenchPreferencePage {

	
	private static final ILog logger = Platform.getLog(TerraArchPreferencesPages.class);

	public static final String KEY_TEXT_COLORS       = "TextColors";

	private Button[] textColorButtons;
	
	public TerraArchPreferencesPages() {
		super();
	}
	
	@Override
	public void init(IWorkbench arg0) {
	
	}	
	
	@Override
	public boolean performOk()	{ // NOTE: this is for the "Apply and Close" button outside our page
	    IEclipsePreferences node = InstanceScope.INSTANCE.getNode(TerraArchActivator.PLUGIN_ID);
	 
	    if (saveTextColors(node)) {	    
	    	TerraPreferences.invalidateAllTextInAllEditors();
	    }
		try {
			node.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		} 
		return super.performOk();
	}


	@Override
	protected void performApply() { // NOTE: this is for the "Apply" button inside our page		
		IEclipsePreferences node = InstanceScope.INSTANCE.getNode(TerraArchActivator.PLUGIN_ID);
		
		if (saveTextColors(node)) {
			TerraPreferences.invalidateAllTextInAllEditors();
		}
		try {
			node.flush();
			
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
		super.performApply();
	}
	
	private boolean saveTextColors(IEclipsePreferences node) {
		int t = textColorButtons.length;
		while (--t>=0) {
			if (textColorButtons[t].getSelection()) {
				boolean result = (t != node.getInt(TerraPreferences.KEY_TEXT_COLORS, -1)); //true if we require a repaint				
				node.putInt(TerraPreferences.KEY_TEXT_COLORS,ThemeColors.values()[t].ordinal());
				return result;
			}
		}
		return true;
	}

	@Override
	protected void performDefaults() {
		//licenseKey.setText(InstanceScope.INSTANCE.getNode(TerraArchActivator.PLUGIN_ID).get(TerraPreferences.KEY_LICENSE_KEY,TerraPreferences.DEMO_KEY));
		colorRadioToDefault(); 
		super.performDefaults();
	}


	@Override
	protected Control createContents(Composite parent) {
		
	    Composite sourceControlGroup = new Composite(parent, SWT.NONE);
				
		sourceControlGroup.setLayout(new GridLayout(1, true));
		GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gridData.widthHint = 550;
		sourceControlGroup.setLayoutData(gridData);
		
		boolean isValid = true;
		isValid |= textColorFields(sourceControlGroup);
		versionBuildFields(sourceControlGroup, true);
				
		this.setValid(isValid);
		return sourceControlGroup;
	}
	
	//public final static String[] buttonTexts = new String[] { "Donate at https://TerraArch.net", "Subscribe later"};
	
	private boolean textColorFields(Composite sourceControlGroup) {
		
		Group titleGroup = new Group(sourceControlGroup, SWT.NONE);
        titleGroup.setLayout(new RowLayout(SWT.VERTICAL));
        
        GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
        gridData.widthHint = 450;
         
		titleGroup.setLayoutData(gridData);
        titleGroup.setText("Text Color");
        
        ThemeColors[] values = ThemeColors.values();
		textColorButtons = new Button[values.length];
        for(int i = 0; i<values.length; i++) { //TerraPreferences.TextColors tc : TerraPreferences.TextColors.values()) {
            ThemeColors tc = values[i];
            textColorButtons[i] = new Button(titleGroup, SWT.RADIO);
            textColorButtons[i].setText(tc.name() + "   (" + tc.description() +")");
        }         

        colorRadioToDefault();         
 
        boolean isValid = true;
		titleGroup.setEnabled(isValid);
		return isValid;
	}


	private void colorRadioToDefault() {
		ThemeColors[] values = ThemeColors.values();
		
		final ThemeColors textColors = ThemeColors.values()[InstanceScope.INSTANCE.getNode(TerraArchActivator.PLUGIN_ID).getInt(TerraPreferences.KEY_TEXT_COLORS, 0)];
		for(int i = 0; i<values.length; i++) { //TerraPreferences.TextColors tc : TerraPreferences.TextColors.values()) {
			textColorButtons[i].setSelection(textColors == values[i]);   
        }
	}

	
	private void versionBuildFields(Composite sourceControlGroup, boolean isSubscriber) {
		Group titleGroup = new Group(sourceControlGroup, SWT.NONE);
        titleGroup.setLayout(new RowLayout(SWT.VERTICAL));
        titleGroup.setText("Information");
        
	        
       	Label feedback = new Label(titleGroup, SWT.NONE);
		feedback.setText(
	             "\nFor new updates use: https://terraarch.net/update     \n"
        );
		
		Button issuesButton = new Button(titleGroup, SWT.NONE);
		issuesButton.setText("   report issues on github   ");
		issuesButton.setToolTipText("click here to launch browser and report an issue.");
		issuesButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				BrowserUtils.launchURL("https://github.com/nathantippy/eclipse-terra-arch/issues");
			}
		});
		
	
		Button supportButton = new Button(titleGroup, SWT.NONE);
		supportButton.setText("   donate here   ");
		supportButton.setToolTipText("click here to launch browser and support us.");
		supportButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				BrowserUtils.launchURL("https://github.com/sponsors/kmf-lab");
			}
		});
		
    	Label buildDetails = new Label(titleGroup, SWT.NONE);
        buildDetails.setText("\nMade in USA of U.S. and open source components.");
        //https://www.ftc.gov/tips-advice/business-center/guidance/complying-made-usa-standard
				
	}

	public static  void invalidateAllTextInAllEditors() {
		try {
			
			FileUtils.visitEditors(e->{
				IEditorPart p = e;
				IEditorInput i = p.getEditorInput();
				if ((p instanceof ITextEditor) && (i instanceof IFileEditorInput)) {
						ITextEditor editor = (ITextEditor)p;
						IFileEditorInput input = (IFileEditorInput)i;
						
						ITextOperationTarget target = (ITextOperationTarget)editor.getAdapter(ITextOperationTarget.class);
						if (target instanceof ITextViewer) {
							ITextViewer viewer = (ITextViewer) target;
							viewer.invalidateTextPresentation();
						}
				}		
			});

		} catch (Throwable t) {
			logger.error("invalidateAllTextInAllEditors",t);
		}
	}
	
}
