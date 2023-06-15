package net.terraarch.terraform.model;

public class Argument {

	//this is the single entry point for the specific paramatrized expression so we hold the arguments here
	
	//for calling the expression templates  
	//var.thing.field  
	//local.list[2] 
	//local.list[comp] local."comp"
	//var.a."comp".c[comp].d."comp" 

	// var.thing.child is a reference not a literal so we can search this independently
	
	//name of argument
	int[] nameExprParams; //references
	int nameExprId; //in most cases will point to a literal string
	
	int[] exprParams; //references	
	int exprId; 

	
	
}
