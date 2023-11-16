package net.terraarch.terraform.structure;

import net.terraarch.terraform.parse.FieldNamesParse;

public abstract class GatherProposals<T extends GatherProposalsVisitor> extends FieldNamesParse {

	protected final T gatherVisitor;
	
	protected GatherProposals(T guv) {
		this.gatherVisitor = guv;
	}
	

	public T guv() {
		return gatherVisitor;		
	}
	
	
}
