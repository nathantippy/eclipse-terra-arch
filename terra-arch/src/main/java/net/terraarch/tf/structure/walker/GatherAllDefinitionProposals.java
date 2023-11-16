package net.terraarch.terraform.structure.walker;

import net.terraarch.terraform.structure.GatherProposals;
import net.terraarch.terraform.structure.GatherProposalsVisitor;
import net.terraarch.terraform.structure.GatheredFieldType;
import net.terraarch.terraform.structure.StructureDataModule;
import net.terraarch.util.AppendableBuilderReader;

public class GatherAllDefinitionProposals<T extends GatherProposalsVisitor> extends GatherProposals<T>  {

	private final StructureDataModule sdm;

	public GatherAllDefinitionProposals(StructureDataModule sdm, T visitor) {
		super(visitor);
		this.sdm = sdm;		
	}
	
	@Override
	protected void definitionData(int resourceEndPos, AppendableBuilderReader resource, int endPos,
			AppendableBuilderReader value) {

			{
				int idx = (int)sdm.checkDataType(resource);
	 		    if (gatherVisitor.foundInDocument(this, resourceEndPos, resource, endPos, value, 0, GatheredFieldType.DATASOURCE_NAME)) {
	 		    	sdm.visitDataNames(idx, gatherVisitor);
	 		    }
			}
	}

	@Override
	protected void definitionLocal(int endPos, AppendableBuilderReader value) {
				if (gatherVisitor.foundInDocument(this, endPos, value, 0, GatheredFieldType.LOCAL)) {
					sdm.visitLocals(gatherVisitor);
				}
	}

	@Override
	protected void definitionModule(int endPos, AppendableBuilderReader value) {
			{
				if (gatherVisitor.foundInDocument(this,endPos, value, 0, GatheredFieldType.MODULE_TYPE)) {
					sdm.visitModuleTypes(gatherVisitor);
				}
			}
	}

	@Override
	protected void definitionProvider(int parts, int resourceEndPos, AppendableBuilderReader resource, int endPos,	AppendableBuilderReader value) {
			//if (2==parts) {
				int idx = (int)sdm.checkProviderType(resource);
	 		    if (gatherVisitor.foundInDocument(this, resourceEndPos, resource, endPos, value, 0, GatheredFieldType.PROVIDER_NAME)) {
	 		    	sdm.visitProviderNames(idx, gatherVisitor);
	 		    }
			//}
	}

	@Override
	protected void definitionResource(int resourceEndPos, AppendableBuilderReader resource, int endPos,
			AppendableBuilderReader value) {
		
			 {
				int idx = (int)sdm.checkResourceType(resource);
	 		    if (gatherVisitor.foundInDocument(this, resourceEndPos, resource, endPos, value, 0, GatheredFieldType.RESOURCE_NAME)) {
	 		    	sdm.visitResourceNames(idx, gatherVisitor);
	 		    }
			}
	}

	@Override
	protected void definitionVariable(int endPos, AppendableBuilderReader value) {
			
			{
				if (gatherVisitor.foundInDocument(this,endPos, value, 0, GatheredFieldType.VARIABLE)) {
					sdm.visitVariables(gatherVisitor);
				}
			}		
	}

	
}
