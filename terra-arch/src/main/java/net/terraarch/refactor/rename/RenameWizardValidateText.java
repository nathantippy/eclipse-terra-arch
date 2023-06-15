package net.terraarch.refactor.rename;

import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Combo;

public final class RenameWizardValidateText implements VerifyListener {
	private final int regexEndEx;
	private final int regexBegin;
	private final Combo selector;

	public RenameWizardValidateText(int regexEndEx, int regexBegin, Combo selector) {
		this.regexEndEx = regexEndEx;
		this.regexBegin = regexBegin;
		this.selector = selector;
	}

	@Override
	public void verifyText(VerifyEvent ev) {
		
		if ( (selector.getSelectionIndex()>=regexBegin) && (selector.getSelectionIndex()<regexEndEx) ) {
			//regex allows many other chars..					
			return;
		}
		
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