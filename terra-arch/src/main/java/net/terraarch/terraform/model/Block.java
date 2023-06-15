package net.terraarch.terraform.model;

public class Block {
		
	String[]        title;
	
	int[]           blocks;//	contained blocks, most often maps/objects
	int             module;// the optional module this is in
	int[]           arguments;//  indexs to arguments
	boolean         hasComments;
}
