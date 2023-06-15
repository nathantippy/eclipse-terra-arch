package net.terraarch.terraform.structure;

import net.terraarch.terraform.parse.BlockType;

//this go across ident positions and do not mach to any other enums.
public enum GatheredFieldType {
	
	IDENT("ident",false, null), //do notmove this must remain zero
	LOCAL("local",                      true, BlockType.LOCALS),
	VARIABLE("variable",                true, BlockType.VARIABLE),
	MODULE_NAME("module name",          true, BlockType.MODULE),
	DATASOURCE_NAME("datasource name",  true, BlockType.DATA),
	RESOURCE_NAME("resource name",      true, BlockType.RESOURCE),
	PROVIDER_NAME("provider name",      true, BlockType.PROVIDER), //these true ones must be in the fist 9 positions, single digit ordinals
	
	// BLOCK_TYPES.OUTPUT
	// BLOCK_TYPES.LIFECYCLE
	
	COUNT("count",false, null),
	PATH("path",false, null),
	EACH("each",false, null),
	FOR_ARG("forArg",false, null),
	SELF("self",false, null),
	MODULE_TYPE("moduleType",false,BlockType.MODULE),
	DATASOURCE_TYPE("datasourceType",false,BlockType.DATA),
	RESOURCE_TYPE("resourceType",false,BlockType.RESOURCE),
	PROVIDER_TYPE("providerType",false,BlockType.PROVIDER),
	TERRAFORM("terraform",false, BlockType.TERRAFORM);
	
	public final String title;
	public final boolean supportsRename;
	public final BlockType blockType;
	
	GatheredFieldType(String title, boolean supportsRename, BlockType type) {
		this.title = title;
		this.supportsRename = supportsRename;
		this.blockType = type;
	}
	
}