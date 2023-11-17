package net.terraarch.refactor.rename;

import java.util.List;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Text;

import net.terraarch.tf.structure.GatheredFieldType;

public final class RenameWizardButtonSelection implements SelectionListener {
	/**
	 * 
	 */
	private final RenameWizardPageBatch renameWizardPageBatch;
	private final Text textNew;
	private final int suffixBegin;
	private final int regexBegin;
	private final Text textOld;
	private final List<GatheredFieldType> typeOnSelection;
	private final int prefixEndEx;
	private final int textBegin;
	private final int regexEndEx;
	private final int prefixBegin;
	private final Combo selector;
	private final int textEndEx;
	private final int suffixEndEx;

	public RenameWizardButtonSelection(RenameWizardPageBatch renameWizardPageBatch, Text textNew, int suffixBegin, int regexBegin, Text textOld,
			List<GatheredFieldType> typeOnSelection, int prefixEndEx, int textBegin, int regexEndEx,
			int prefixBegin, Combo selector, int textEndEx, int suffixEndEx) {
		this.renameWizardPageBatch = renameWizardPageBatch;
		this.textNew = textNew;
		this.suffixBegin = suffixBegin;
		this.regexBegin = regexBegin;
		this.textOld = textOld;
		this.typeOnSelection = typeOnSelection;
		this.prefixEndEx = prefixEndEx;
		this.textBegin = textBegin;
		this.regexEndEx = regexEndEx;
		this.prefixBegin = prefixBegin;
		this.selector = selector;
		this.textEndEx = textEndEx;
		this.suffixEndEx = suffixEndEx;
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent arg0) {
	}

	@Override
	public void widgetSelected(SelectionEvent arg0) {
		
		int idx = selector.getSelectionIndex();
		
		if (idx >= textBegin && idx<textEndEx) {
			this.renameWizardPageBatch.data.stream()
			.filter(d-> null==typeOnSelection.get(idx) || d.type==typeOnSelection.get(idx))
			.forEach(d-> d.field.setText( d.field.getText().trim().replace(textOld.getText().trim(), textNew.getText().trim()) ) );
		}
	    if (idx >= regexBegin && idx<regexEndEx) {
	    	this.renameWizardPageBatch.data.stream()
			.filter(d-> null==typeOnSelection.get(idx) || d.type==typeOnSelection.get(idx))
	    	.forEach(d-> d.field.setText( d.field.getText().trim().replaceAll(textOld.getText().trim(), textNew.getText().trim()) ) );
		}
	    if (idx >= prefixBegin && idx<prefixEndEx) {
	    	this.renameWizardPageBatch.data.stream()
			.filter(d-> null==typeOnSelection.get(idx) || d.type==typeOnSelection.get(idx))
	    	.forEach(d->d.field.setText( textNew.getText().trim()+d.field.getText().trim()));
		}
		if (idx >= suffixBegin && idx<suffixEndEx) {
			this.renameWizardPageBatch.data.stream()
			.filter(d-> null==typeOnSelection.get(idx) || d.type==typeOnSelection.get(idx))
			.forEach(d-> d.field.setText(d.field.getText().trim()+textNew.getText().trim()));
		}
	}
}