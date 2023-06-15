package net.terraarch.refactor.rename;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public final class RenameWizardMatchSelectionListener implements SelectionListener {
	private final int textEndEx;
	private final Combo selector;
	private final int prefixEndEx;
	private final Text textOld;
	private final int prefixBegin;
	private final int suffixEndEx;
	private final Label newLabel;
	private final int textBegin;
	private final int suffixBegin;
	private final Label oldLabel;
	private final int regexBegin;
	private final int regexEndEx;

	public RenameWizardMatchSelectionListener(int textEndEx, Combo selector, int prefixEndEx, Text textOld, int prefixBegin,
			int suffixEndEx, Label newLabel, int textBegin, int suffixBegin, Label oldLabel, int regexBegin,
			int regexEndEx) {
		this.textEndEx = textEndEx;
		this.selector = selector;
		this.prefixEndEx = prefixEndEx;
		this.textOld = textOld;
		this.prefixBegin = prefixBegin;
		this.suffixEndEx = suffixEndEx;
		this.newLabel = newLabel;
		this.textBegin = textBegin;
		this.suffixBegin = suffixBegin;
		this.oldLabel = oldLabel;
		this.regexBegin = regexBegin;
		this.regexEndEx = regexEndEx;
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent arg0) {
	}

	@Override
	public void widgetSelected(SelectionEvent arg0) {

		int idx = selector.getSelectionIndex();
		
		if (idx >= textBegin && idx<textEndEx) {
			oldLabel.setText("match:");
			oldLabel.setVisible(true);
			textOld.setVisible(true);
			newLabel.setText("replace:");
		}
		if (idx >= regexBegin && idx<regexEndEx) {
			oldLabel.setText("match:");
			oldLabel.setVisible(true);
			textOld.setVisible(true);
			newLabel.setText("replace:");
		}
		if (idx >= prefixBegin && idx<prefixEndEx) {
		    oldLabel.setVisible(false);
		    textOld.setVisible(false);
		    newLabel.setText("prefix:"); 
		}
		if (idx >= suffixBegin && idx<suffixEndEx) {
			oldLabel.setVisible(false);
			textOld.setVisible(false);
			newLabel.setText("suffix:");
		}
	
	}
}