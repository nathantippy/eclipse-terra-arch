package net.terraarch.preferences;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

import net.terraarch.terraform.parse.ParseState;

import net.terraarch.TerraArchActivator;
import net.terraarch.util.FileUtils;


public final class TerraPreferences {

	
	private static final ILog logger = Platform.getLog(TerraPreferences.class);
	
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


	
	
	public static final TerraPreferences instance = new TerraPreferences();
	 
	public static final String KEY_SIGNED_LICENSE    = ("SignedLicense_"+Integer.toString(TerraArchActivator.LICENSE_VERSION));
	public static final String KEY_SIGNED_COPYRIGHT  = ("SignedCopyright_"+Integer.toString(TerraArchActivator.COPYRIGHT_VERSION));
	public static final String KEY_LICENSE_KEY       = "LicenseKey";
	public static final String KEY_TEXT_COLORS       = "TextColors";

	public boolean isDisableSmartInsert() {
		return true;
	}
	
	
	
	
	
	//public static Preferences instance() {
	//	 return InstanceScope.INSTANCE.getNode(TerraArchActivator.PLUGIN_ID);
	//}
		

	
}
