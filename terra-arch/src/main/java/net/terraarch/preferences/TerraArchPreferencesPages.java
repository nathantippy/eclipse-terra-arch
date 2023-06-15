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


import net.terraarch.terraform.parse.ParseState;
import net.terraarch.terraform.parse.doc.ThemeColors;

import net.terraarch.MarketingMessages;
import net.terraarch.TerraArchActivator;
import net.terraarch.util.BrowserUtils;

public class TerraArchPreferencesPages extends PreferencePage implements IWorkbenchPreferencePage {

	private static final ILog logger = Platform.getLog(TerraArchPreferencesPages.class);

	
	private Text licenseKey;
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
	    
	    boolean keyChanged = !node.get(TerraPreferences.KEY_LICENSE_KEY, "").equals(licenseKey.getText());
		node.put(TerraPreferences.KEY_LICENSE_KEY,licenseKey.getText());		
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
		
		boolean keyChanged = !node.get(TerraPreferences.KEY_LICENSE_KEY, "").equals(licenseKey.getText());
		node.put(TerraPreferences.KEY_LICENSE_KEY,licenseKey.getText());		
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
		isValid |= licenseKeyFields(sourceControlGroup, false);
		versionBuildFields(sourceControlGroup, true);
	   // isValid |= advancedOptions(sourceControlGroup);			
				
		this.setValid(isValid);
		return sourceControlGroup;
	}
	
	public final static String[] buttonTexts = new String[] { "Subscribe at https://TerraArch.net", "Subscribe later"};

	
	private boolean licenseKeyFields(Composite sourceControlGroup, boolean isTrial) {
		Group titleGroup = new Group(sourceControlGroup, SWT.NONE);
        titleGroup.setLayout(new GridLayout(1, false));
        titleGroup.setText("License Key");
        
		licenseKey = new Text(titleGroup, SWT.BORDER);
		licenseKey.setToolTipText("Enter product license key here");
		licenseKey.setCapture(true);
		licenseKey.setFocus();
		
		GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, true, false);
	    gridData.widthHint = 350;
	    licenseKey.setLayoutData(gridData);
		//licenseKey.setText(InstanceScope.INSTANCE.getNode(TerraArchActivator.PLUGIN_ID).get(TerraPreferences.KEY_LICENSE_KEY,TerraPreferences.DEMO_KEY));

		licenseKey.addVerifyListener(new TerraArchPreferencesPageLicenseValidation());
		
		//Listener(eventType, listener); //need to update preferences upon change, on pref save we must write to dynamo 

			
			Label label = new Label(titleGroup, SWT.NONE);
			String customeEditionMessage = ""; //if the demo key is still used then add this comment
			
			Button puchaseButton = new Button(titleGroup, SWT.NONE);
			puchaseButton.setText("visit site");
			puchaseButton.setToolTipText("click here to launch browser and support");
			puchaseButton.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
				}
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					BrowserUtils.launchURL("https://TerraArch.net");
				}
			}
			);
			label.setText(customeEditionMessage+"\n");
		
		
		boolean isValid = true;
		licenseKey.setEnabled(isValid);
		
		return isValid;
	}

	private void addLabel(Group titleGroup, String label, String value, String suffix) {
		if (null!=value && value.trim().length()>0) {
	
		   Label text = new Label(titleGroup, SWT.NONE); 
		   text.setText(label+value+suffix);
		
		}
	}

	
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
        titleGroup.setText("Build");
    	Label buildDetails = new Label(titleGroup, SWT.NONE);
        buildDetails.setText(
        		          "\nMade in USA of U.S. and open source components.");
        		          //https://www.ftc.gov/tips-advice/business-center/guidance/complying-made-usa-standard
	
		Group buttonGroup = new Group(titleGroup, SWT.SHADOW_NONE);
        buttonGroup.setLayout(new RowLayout(SWT.HORIZONTAL));
		  
       	     	     
	    Button cButton = new Button(buttonGroup, 0);
	    cButton.setText("CopyrightAgreement");

        cButton.setToolTipText("view copyright");
        
       	Label feedback = new Label(titleGroup, SWT.NONE);
                
        String updateMessage = isSubscriber ? "\n"+
		          "Optional URLs for available software site:\n"+
		          "  For recent updates use  https://terraarch.net/update\n"+
		          "  For stable updates use  https://terraarch.net/update-lts\n" : "";        
		          
        //TODO: JJ, this will be replaced with new private messaging system
		feedback.setText(
		          "\nSend feedback and requests to:  support@terraarch.net       "+
		          updateMessage
        );
		feedback.setCapture(true);
	}


	
/*
//TODO: GG, turn on off auto insert..	
	private boolean advancedOptions(Composite sourceControlGroup) {
		Group titleGroup = new Group(sourceControlGroup, SWT.NONE);
        titleGroup.setLayout(new RowLayout(SWT.VERTICAL));
        titleGroup.setText("Advanced Options");
              
        
        
        Group autoInsertGroup = new Group(titleGroup, SWT.NONE);
        autoInsertGroup.setLayout(new RowLayout(SWT.VERTICAL));
        
        GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
        gridData.widthHint = 450;         
		autoInsertGroup.setLayoutData(gridData);
        autoInsertGroup.setText("Auto Insert");

        Button disableButton = new Button(autoInsertGroup, SWT.RADIO);
        disableButton.setText("disable");
        Button enableButton = new Button(autoInsertGroup, SWT.RADIO);
		enableButton.setText("enable");

//        for(Preferences.TextColors tc : Preferences.TextColors.values()) {
//            Button aButton = new Button(autoInsertGroup, SWT.RADIO);
//             
//            aButton.setSelection(Preferences.instance.textColors() == tc);    	
//            aButton.setText(tc.name() + "   (" + tc.description() +")");
//            aButton.addSelectionListener(new SelectionAdapter()  {
//							@Override
//							public void widgetSelected(SelectionEvent e) {
//								Preferences.instance.textColors(tc);
//							}            	
//            	});
//        }         
        
        
		boolean isValid = Preferences.instance.state() == Preferences.State.InSync;
		autoInsertGroup.setEnabled(isValid);
		
		return isValid;
			
    	}
	//    */
	
	
}
