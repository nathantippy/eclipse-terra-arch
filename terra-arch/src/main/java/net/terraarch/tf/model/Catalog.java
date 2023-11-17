package net.terraarch.tf.model;

import java.util.ArrayList;
import java.util.List;

public class Catalog {
	
	//Our goal is to index the 'low level' details so all the data we need at authoring runtime
	//will be available given a 'small number' of lookup steps.
	
	//possible workflows
	//   lookup blocks based on user selection or context (block name)
	//   lookup other blocks using the same source/ver (source name)
	//   lookup other versions from sources (source name neighbors)
	
	//NOTE: we want to allow for creative usage and lookup of this data; as a result 
	private List<Block>         blocksRef     = new ArrayList<Block>();       //immutable list, index is public
	private List<Expression>	expressionRef = new ArrayList<Expression>();  //immutable list
	private List<Module>        moduleRef     = new ArrayList<Module>();      //immutable list	
	private List<Reference>     referenceRef  = new ArrayList<Reference>();   //immutable list	
	private List<Argument>      argumentRef   = new ArrayList<Argument>();    //immutable list
	
	//this index data can be derived from the above normalized form
	private List<LabelIndex>    blockNamesIndex         = new ArrayList<LabelIndex>(); //sorted by name.label.label
	private List<LabelIndex>    moduleSourceIndex       = new ArrayList<LabelIndex>(); //sorted by source.version
	private List<LabelIndex>    argumentNamesIndex      = new ArrayList<LabelIndex>(); //  //sorted by name or expr,  find recommended exp, find recommended names for expr.			
	private List<LabelIndex>    argumentParamsIndex     = new ArrayList<LabelIndex>(); //  //sorted by params used
	private List<LabelIndex>    argumentExpressionIndex = new ArrayList<LabelIndex>(); //  //sorted by expressions used
	

	
	
}
