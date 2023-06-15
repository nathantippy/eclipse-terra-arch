package net.terraarch.terraform.model;

public class Module {

	String          rev;      //source ref	
	String          canonLoc; //source string
	
	long            lastUpdateUTC;
		
	int[]           usedBy;     //  modules using this module
	int[]           revisions;  //	other revisions of this module
	
	
	String[]        authors; //by frequency? by date
	
	int[]           blocksInModule;
	int[]           usedModules;
	
}
