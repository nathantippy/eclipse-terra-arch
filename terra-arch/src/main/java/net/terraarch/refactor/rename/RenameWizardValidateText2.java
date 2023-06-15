package net.terraarch.refactor.rename;

import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;

public final class RenameWizardValidateText2 implements VerifyListener {
	@Override
	public void verifyText(VerifyEvent ev) {
		//ensure all the text only uses the supported URL chars
		char[] chars=ev.text.toCharArray();
		int x = chars.length;
		while (--x>=0) {
			char c = chars[x];
			if (   (c>='0' && c<='9')
		        || (c>='A' && c<='Z')
		        || (c>='a' && c<='z')
		        || (c=='-')
		        || (c=='_')
		       ) {
			//this is a valid char				
			} else {
				//does not match a valid char
				ev.doit = false;
				return;					
			}
		};
		ev.doit = true;		
	}
}