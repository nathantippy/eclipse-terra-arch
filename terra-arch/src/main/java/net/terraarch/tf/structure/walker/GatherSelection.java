package net.terraarch.terraform.structure.walker;

import net.terraarch.terraform.structure.GatherProposals;
import net.terraarch.terraform.structure.GatheredFieldType;
import net.terraarch.util.AppendableBuilderReader;


public class GatherSelection extends GatherProposalsAdapter {

	@Override
	public boolean foundInDocument(GatherProposals<?> that, int endPos, AppendableBuilderReader value, int preBytes, GatheredFieldType type) {
			super.foundInDocument(that, endPos, value, preBytes, type);
			return false;
	}

	@Override
	public boolean foundInDocument(GatherProposals<?> that, int typeEndPos, AppendableBuilderReader typeValue, int endPos, AppendableBuilderReader value, int preBytes, GatheredFieldType type) {
			super.foundInDocument(that, typeEndPos, typeValue, endPos, value, preBytes, type);
			return false;
	}
		
	
	public static StringBuilder buildSourceDescription(StringBuilder label, GatherProposalsAdapterData d) {
		switch (d.type) {
			case MODULE_TYPE:
				label.append("module.").append(d.nameValue);
			break;	
		    case LOCAL:
				label.append("local.").append(d.nameValue);
			break;	
			case VARIABLE:
				label.append("var.").append(d.nameValue);
			break;
			case RESOURCE_NAME:
				label.append(d.categoryType).append(".").append(d.nameValue);
			break;
			case DATASOURCE_NAME:
				label.append("data.").append(d.categoryType).append(".").append(d.nameValue);
			break;
			case PROVIDER_NAME:
				label.append(d.categoryType).append(".").append(d.nameValue);
			break;
			default:
				throw new UnsupportedOperationException();
			
		}
		return label;
	}

	
	public static StringBuilder buildTargetDescription(StringBuilder label, GatherProposalsAdapterData d) {
		switch (d.type) {
			case MODULE_TYPE:
				label.append("module.");
			break;	
		    case LOCAL:
				label.append("local.");
			break;	
			case VARIABLE:
				label.append("var.");
			break;
			case RESOURCE_NAME:
				label.append(d.categoryType).append(".");
			break;
			case DATASOURCE_NAME:
				label.append("data.").append(d.categoryType).append(".");
			break;
			case PROVIDER_NAME:
				label.append(d.categoryType).append(".");
			break;
			default:
				throw new UnsupportedOperationException();
			
		}
		return label;
	}
	
}
