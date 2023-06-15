package net.terraarch.refactor.rename;

import net.terraarch.util.AppendableBuilderReader;
import net.terraarch.terraform.parse.FieldNamesParse;

public class RenameFieldGatherParse extends FieldNamesParse{

	
	private final FileChangeVisitor fileChangeVisitor;
	private final RenameRequests requests;
	
	
	public RenameFieldGatherParse(RenameRequests requests, FileChangeVisitor fileChangeVisitor) {
		super();
		
		this.requests = requests;
		this.fileChangeVisitor = fileChangeVisitor;
	}


	@Override
	protected void usageLocal(int endPos, AppendableBuilderReader value) {		
		requests.vistLocalReplacements(r -> {
			if (value.isEqual(r.oldName)) {
				fileChangeVisitor.newChange(endPos-value.byteLength(), value.byteLength(), r.newName);
			}
		});
	}

	@Override
	protected void usageVariable(int endPos, AppendableBuilderReader value) {
		requests.vistVarReplacements(r -> {
			if (value.isEqual(r.oldName)) {
				fileChangeVisitor.newChange(endPos-value.byteLength(), value.byteLength(), r.newName);
			}
		});
	}

	@Override
	protected void usageModule(int parts, int resourceEndPos, AppendableBuilderReader resource, int endPos, AppendableBuilderReader value) {
		requests.vistModuleNameReplacements(r -> {
			if (value.isEqual(r.oldName) && resource.isEqual(r.namespace)) {
				fileChangeVisitor.newChange(endPos-value.byteLength(), value.byteLength(), r.newName);
			}
		});
	}

	@Override
	protected void usageData(int parts, int resourceEndPos, AppendableBuilderReader resource, int endPos, AppendableBuilderReader value) {
		requests.vistDataNameReplacements(r -> {
			if (value.isEqual(r.oldName) && resource.isEqual(r.namespace)) {
				fileChangeVisitor.newChange(endPos-value.byteLength(), value.byteLength(), r.newName);
			}					
		});
	}


	@Override
	protected void usageResource(int resourceEndPos, AppendableBuilderReader resource, int endPos, AppendableBuilderReader value) {
		requests.vistResourceNameReplacements(r -> {
			if (value.isEqual(r.oldName) && resource.isEqual(r.namespace)) {
				fileChangeVisitor.newChange(endPos-value.byteLength(), value.byteLength(), r.newName);
			}					
		});
	}

	@Override
	protected void definitionLocal(int endPos, AppendableBuilderReader value) {
		requests.vistLocalReplacements(r -> {
			if (value.isEqual(r.oldName)) {
				fileChangeVisitor.newChange(endPos-value.byteLength(), value.byteLength(), r.newName);
			}
		});
	}

	@Override
	protected void definitionVariable(int endPos, AppendableBuilderReader value) {
		requests.vistVarReplacements(r -> {
			if (value.isEqual(r.oldName)) {
				fileChangeVisitor.newChange(endPos-value.byteLength(), value.byteLength(), r.newName);
			}
		});
	}

	@Override
	protected void definitionModule(int endPos, AppendableBuilderReader value) {
		requests.vistModuleNameReplacements(r -> {
			if (value.isEqual(r.oldName)) {
				fileChangeVisitor.newChange(endPos-value.byteLength(), value.byteLength(), r.newName);
			}
		});
	}

	@Override
	protected void definitionData(int resourceEndPos, AppendableBuilderReader resource, int endPos, AppendableBuilderReader value) {
		requests.vistDataNameReplacements(r -> {
			if (value.isEqual(r.oldName) && resource.isEqual(r.namespace)) {
				fileChangeVisitor.newChange(endPos-value.byteLength(), value.byteLength(), r.newName);
			}
		});
	}

	@Override
	protected void definitionResource(int resourceEndPos, AppendableBuilderReader resource, int endPos, AppendableBuilderReader value) {
		requests.vistResourceNameReplacements(r -> {
			if (value.isEqual(r.oldName) && resource.isEqual(r.namespace)) {
				
				fileChangeVisitor.newChange(endPos-value.byteLength(), value.byteLength(), r.newName);
				
			}
		});
	}

	@Override
	protected void usageProvider(int resourceEndPos, AppendableBuilderReader resource, int endPos, AppendableBuilderReader value) {
		requests.vistProviderNameReplacements(r -> {
			if (value.isEqual(r.oldName) && resource.isEqual(r.namespace)) {
				fileChangeVisitor.newChange(endPos-value.byteLength(), value.byteLength(), r.newName);
			}
		});
	}

	@Override
	protected void definitionProvider(int parts, int resourceEndPos, AppendableBuilderReader resource, int endPos, AppendableBuilderReader value) {
		//if (2==parts) {
			requests.vistProviderNameReplacements(r -> {
				if (value.isEqual(r.oldName) && resource.isEqual(r.namespace)) {
					fileChangeVisitor.newChange(endPos-value.byteLength(), value.byteLength(), r.newName);
				}
			});
	//	}
		
	}
	
	
}
