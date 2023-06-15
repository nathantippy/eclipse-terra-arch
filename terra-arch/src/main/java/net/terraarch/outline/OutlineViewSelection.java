package net.terraarch.outline;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;

final class OutlineViewSelection implements ISelection {
	
	/**
	 * 
	 */
	private final TerraArchOutlineView terraArchOutlineView;
	public ISelection down;

	public OutlineViewSelection(TerraArchOutlineView terraArchOutlineView) {

		this.terraArchOutlineView = terraArchOutlineView;
		if ((this.terraArchOutlineView.composite != null) && (this.terraArchOutlineView.treeViewer != null)) {
			down = this.terraArchOutlineView.treeViewer.getSelection();
		} else {
			down = StructuredSelection.EMPTY;
		}
		
		
	}
	
	
	@Override
	public boolean isEmpty() {
		return //up.isEmpty() && 
				down.isEmpty();
	}
}