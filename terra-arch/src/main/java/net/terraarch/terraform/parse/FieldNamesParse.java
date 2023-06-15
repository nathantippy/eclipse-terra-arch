package net.terraarch.terraform.parse;

import java.util.Arrays;

import net.terraarch.util.AppendableBuilder;
import net.terraarch.util.AppendableBuilderReader;
import net.terraarch.util.TrieParserReader;

/**
 * Base class for the gathering of names to be replaced in the rename refactoring wizzard
 * and the base class for knowledge of all valid names so syntax highlighting and auto complete have
 * a meaningful dictionary of valid choice.
 * 
 * @author nate
 *
 */
public class FieldNamesParse extends ParseState {

	private final TrieParserReader localReader = new TrieParserReader(true);


	protected ExpressionBuilder expressionBuilder = new ExpressionBuilder();//placeholder

	private int    lastIdentifierEndPos = 0;	
	
	
	private int termIdentiferStackNamespace = -1;
	private AppendableBuilder lastText = new AppendableBuilder();
	private int    lastTextEndPos;
	
   //NOTE: this is done here to capture the line position
	private boolean lastAssignmentIsForAliasDefinition = false;
    private final byte[] ALIAS_BYTES = "alias".getBytes();
	
	private int lastBlockDefOrdinal = -1;
	private int lastBlockDefEndPos = -1;
	private boolean localDefInProgress = false;
        
	protected AppendableBuilder lastIdentifier = new AppendableBuilder();

	private final AppendableBuilder providerBuilder = new AppendableBuilder();
    private int lastProviderEndPos = -1;
		
    
	public FieldNamesParse() {
		super("");
	}
    
	protected void reset() {
	 	super.reset();
	 	lastIdentifier.clear();
		lastIdentifierEndPos = 0;
		termIdentiferStackNamespace = -1;
		lastText.clear();
		lastTextEndPos = 0;
		lastBlockDefOrdinal = -1;
		lastAssignmentIsForAliasDefinition = false;
		lastProviderEndPos = -1;
		providerBuilder.clear();
		
		moduleGitURLSourcesToRequest.clear();//do not need this feature here so we must clear to keep from collecting them
		
	}
	
	///////////////////////////////////////////////
	/////////////////////////////////////////////
	//build up expressions
	/////////////////////////////////////////////
	////////////////////////////////////////////  

	
	protected void refUsage(NAMESPACES nameSpace, BlockType type,
			             int nameParts, 
			             int endPos2, AppendableBuilderReader reader2,
			             int endPos1, AppendableBuilderReader reader1) {
		try {
			expressionBuilder.reference(nameSpace, type, nameParts, endPos2, reader2, endPos1, reader1);
		} catch (Exception e) {
			 e.printStackTrace();
		}
	}
	
	//publ/ic void hereDocTextBegin(int filePosition, byte[] quotes) {
	@Override
	public void hereDocTextBegin(int filePosition, byte[] quotes) {
		try {
			super.textBegin(filePosition, quotes);
			expressionBuilder.textBegin(filePosition, quotes);
		} catch (Exception e) {
			 e.printStackTrace();
		}
	}
	@Override
	public void textBegin(int filePosition, byte[] quotes) {
		try {
			super.textBegin(filePosition, quotes);
			expressionBuilder.textBegin(filePosition, quotes);
		} catch (Exception e) {
			 e.printStackTrace();
		}
	}
	@Override
	public void appendTextByte(int b) {
		try {
			super.appendTextByte(b);
			expressionBuilder.appendTextByte(b);
		} catch (Exception e) {
			 e.printStackTrace();
			 System.exit(-1);
		}
	}
	@Override
	public AppendableBuilderReader textEnd(int endPos) {
		try {
		return blockTextProcessing(endPos, expressionBuilder.textEnd(endPos,super.textEnd(endPos)));
		} catch (Exception e) {
			 e.printStackTrace();
			 throw e;
		}
	}
	@Override
	public void iterateOpen(int filePosition, int size) {
		try {
		super.iterateOpen(filePosition, size);
		expressionBuilder.iterateOpen(filePosition, size);
		} catch (Exception e) {
			 e.printStackTrace();
		}
	}
	@Override
	public void iterElse() {
		try {
		super.iterElse();
		expressionBuilder.iterElse();
		} catch (Exception e) {
			 e.printStackTrace();
		}
	}
	@Override
	public void iterEndIf() {
		try {
		super.iterEndIf();
		expressionBuilder.iterEndIf();
		} catch (Exception e) {
			 e.printStackTrace();
		}
	}
	@Override
	public void iterEndFor() {
		try {
		super.iterEndFor();
		expressionBuilder.iterEndFor();
		} catch (Exception e) {
			 e.printStackTrace();
		}
	}
	@Override
	public void iterIf() {
		try {
		super.iterEndIf();
		expressionBuilder.iterEndIf();
		} catch (Exception e) {
			 e.printStackTrace();
		}
	}
	
	@Override
	public void parenClose(int filePosition) {
		try {
		super.parenClose(filePosition);
		expressionBuilder.parenClose(filePosition);
		} catch (Exception e) {
			 e.printStackTrace();;
		}
	}
	@Override
	public void parenOpen(int filePosition) {
		try {
		super.parenOpen(filePosition);
		expressionBuilder.parenOpen(filePosition);
		} catch (Exception e) {
			 e.printStackTrace();;
		}
	}
		
	@Override
	public String pushOperation(int filePosition, String op) {
		try {
		return expressionBuilder.pushOperation(filePosition, super.pushOperation(filePosition, op));
		} catch (Exception e) {
			 e.printStackTrace();
			 throw e;
		}
	}
	
	
	@Override
	public String interpolateOpen(int filePosition, int size) {
		try {
		return expressionBuilder.interpolateOpen(filePosition, size, super.interpolateOpen(filePosition, size));
		} catch (Exception e) {
			 e.printStackTrace();
			 throw e;
		}
	}
	@Override
	public void interpolateClose(int filePosition) {
		try {
		super.interpolateClose(filePosition);
		expressionBuilder.interpolateClose(filePosition);	
		} catch (Exception e) {
			 e.printStackTrace();;
		}
	}
	
	
	@Override
	public int arrayOpen(int filePosition) {
		try {
		return expressionBuilder.arrayOpen(filePosition, super.arrayOpen(filePosition));
		} catch (Exception e) {
			 e.printStackTrace();
			 throw e;
		}
	}
	@Override
	public int arrayClose(int filePosition) {
		try {
		return expressionBuilder.arrayClose(filePosition, super.arrayClose(filePosition));
		} catch (Exception e) {
			 e.printStackTrace();
			 throw e;
		}
	}
	@Override
	public void arrayItem(int filePosition) {
		try {
		super.arrayItem(filePosition);
		expressionBuilder.arrayItem(filePosition);
		} catch (Exception e) {
			 e.printStackTrace();;
		}
	}
	
	
	@Override
	public void numberBegin(int filePosition) {
		try {
		super.numberBegin(filePosition);
		expressionBuilder.numberBegin(filePosition);
		} catch (Exception e) {
			 e.printStackTrace();;
		}
	}
	@Override
	public void pushNumber(int filePosition, long m, int e) {
		try {
		super.pushNumber(filePosition, m, e);
		expressionBuilder.pushNumber(filePosition, m, e);
		} catch (Exception ex) {
			 ex.printStackTrace();
			
		}
		
	}
	@Override
	public void pushNumber(int filePosition, long i) {
		try {
		super.pushNumber(filePosition, i, filePosition);
		expressionBuilder.pushNumber(filePosition, i, filePosition);
		} catch (Exception e) {
			 e.printStackTrace();;
		}
	}
	@Override
	public void pushNumberSciNotation(int filePosition, String text, long i) {
		try {
		super.pushNumberSciNotation(filePosition, text, i);
		expressionBuilder.pushNumberSciNotation(filePosition, text, i);
		} catch (Exception e) {
			 e.printStackTrace();;
		}
		
	}
	@Override
	public String numberComplete(int filePosition) {
		try {
		return expressionBuilder.numberComplete(filePosition,super.numberComplete(filePosition));
		} catch (Exception e) {
			 e.printStackTrace();
			 throw e;
		}
	}
	
	
	@Override
	public void functionEnd(int filePosition) {
		try {
		super.functionEnd(filePosition);
		expressionBuilder.functionEnd(filePosition);
		} catch (Exception e) {
			 e.printStackTrace();;
		}
	}
	@Override
	public void functionEndWithArrayAsArgs(int filePosition) {
		try {
		super.functionEndWithArrayAsArgs(filePosition);
		expressionBuilder.functionEndWithArrayAsArgs(filePosition);
		} catch (Exception e) {
			 e.printStackTrace();;
		}
	}
	@Override
	public void functionPushParam(int filePosition) {
		try {
		super.functionPushParam(filePosition);
		expressionBuilder.functionPushParam(filePosition);
		} catch (Exception e) {
			 e.printStackTrace();;
		}
	}
	
	
	@Override
	public AppendableBuilderReader functionOpen(int filePosition) {
		
		//THIS is for testing, to confirm no one is using a fuction in a variable default
		if (false) {
			if (BlockType.VARIABLE.ordinal() == lastBlockDefOrdinal) {
				
				if (Arrays.equals(lastAssignmentArgumentName, "default".getBytes())) {
				
					throw new RuntimeException("found a function inside variable default definition");
				}
			}
		}
		
		try {
		return expressionBuilder.functionOpen(filePosition,super.functionOpen(filePosition));
		} catch (Exception e) {
			 e.printStackTrace();
			 throw e;
		}
	}

	@Override
	public int forStart(int filePosition) {
		try {
		return expressionBuilder.forStart(filePosition, super.forStart(filePosition));
		} catch (Exception e) {
			 e.printStackTrace();
			 throw e;
		}
	}
	@Override
	protected void collectForArg(int depth, int endPos, AppendableBuilderReader reader) {
		try {
			//the position of these values is key for equiv
		super.collectForArg(depth, endPos, reader);
		expressionBuilder.collectForArg(depth, endPos, reader);
		} catch (Exception e) {
			 e.printStackTrace();;
		}
	}
	
	
//	if (termLayer.resource()<0 && termLayer.provider()<0 && termLayer.namespace()<0) {
//		
//		if (value.lookupExactMatch(localReader, variableTypeParser)>=0) {
//			
//		}
	//TODO: AAAA, Scompare with the DocumentToken logic to support from arguments...
	
	//at the point where this is called
	//protected void startIdentifierUsage(int endPosition, AppendableBuilderReader value, boolean requestProviderAliasUsage) {
	//} 
	   
	   
	@Override
	public void forComma(int filePosition) {
		try {
		super.forComma(filePosition);
		expressionBuilder.forComma(filePosition);
		} catch (Exception e) {
			 e.printStackTrace();;
		}
	}
	@Override
	public void forIn(int filePosition) {
		try {
		super.forIn(filePosition);
		expressionBuilder.forIn(filePosition);
		} catch (Exception e) {
			 e.printStackTrace();;
		}
	}
	@Override
	public int forEnd(int filePosition) {
		try {
		return expressionBuilder.forEnd(filePosition, super.forEnd(filePosition));
		} catch (Exception e) {
			 e.printStackTrace();
			 throw e;
		}
	}
	
	
	@Override
	public void convertLastIdentifierToAssignement(int filePosition) {
		try {
		//start the expression for this argument
		super.convertLastIdentifierToAssignement(filePosition);
		
		expressionBuilder.clear(); //start new expression building
		expressionBuilder.setAssignmentArgument(lastAssignmentArgumentName, lastAssignmentArgumentPosition);
		} catch (Exception e) {
			 e.printStackTrace();
		}
		
	}
	@Override
	public void endOfAssignement(int endPos) {
		try {
		//end the expression for that argument
		super.endOfAssignement(endPos);		
		//the expression is ow finished.
		assignmentProcessing(endPos, expressionBuilder.getExpressionRoot());			
		//we should clear to start building the next one.
		} catch (Exception e) {
			 e.printStackTrace();;
		}
	}
	
	//////////////////////////////////////////////  
	//////////////////////////////////////////////
	//end of build up expressions
	//////////////////////////////////////////////
	//////////////////////////////////////////////
	
	
	@Override
	public int blockOpen(int endPos) {		
		this.lastText.clear();
		return super.blockOpen(endPos);
		
	}
	@Override
	public int blockClose(int endPos) {		
				
		int result = super.blockClose(endPos);
		if (0==result) {
			
			//do no close the "locals" block because we will close each individual local
			if (BlockType.LOCALS.ordinal() != lastBlockDefOrdinal) {			
				if (BlockType.MODULE.ordinal() == lastBlockDefOrdinal) {
					setActiveModuleSource(activeModuleSource); //must call before we close the definition.
				}
				closeDefinition(lastBlockDefOrdinal, lastBlockDefEndPos, endPos);
			}
			
			lastBlockDefOrdinal = -1;
			lastBlockDefEndPos = -1;
			providerBuilder.clear();
			
		}
		return result;
	}


	private void assignmentProcessing(int endPos, TFExpression expressionRoot) {
		if (null!=expressionRoot) { //only set when we have new expression data 
			assignment(lastAssignmentArgumentName, lastAssignmentArgumentPosition, endPos, expressionRoot);
		}
		
		//outside any nested array and outside and internal block structures
		if (0==arrayDepth && blockDepth==1 && localDefInProgress && textQuoteStack.isEmpty()) {
			closeDefinition(BlockType.LOCALS.ordinal(), lastBlockDefEndPos, endPos);
			localDefInProgress = false;
		}
	}

	@Override
	public AppendableBuilderReader identifierNominalEnd(int endPos) { 
		//NOTE: this is always called at end of name but due to [ ] notation there may be nested items in the path
		final int intPos = identPosition();//capture before reset	
		
		//we store this because it may be the type of a blocks
		AppendableBuilderReader value = super.identifierNominalEnd(endPos);
		
		if (isCollectingBlockLabels()) {
			//this string is a block label
			if (blockDepth==0) {				
				/////////////////////////////////
				//gather the variable name {  where the name is NOT surrounted by quotes
				////////////////////////////////
				if (BlockType.VARIABLE.ordinal() == lastBlockDefOrdinal) {
					definitionVariable(endPos, value);					
				} else if(BlockType.RESOURCE.ordinal() == lastBlockDefOrdinal) {
					    //if resource found at depth 0 and a previous text exists
					    //captures a resource with a string followed by an identifier
						if (this.lastText!=null && this.lastText.byteLength()>0) {
							AppendableBuilderReader r = lastText.reader();
							if (!r.isEqual("resource".getBytes())) {
								definitionResource(lastTextEndPos, r, endPos, value);
							}
						}
						//captures a resources with an identifer followed by an indentifier
						if (this.lastIdentifier!=null && this.lastIdentifier.byteLength()>0) {
							AppendableBuilderReader r = lastIdentifier.reader();
							if (!r.isEqual("resource".getBytes())) {
								definitionResource(lastIdentifierEndPos, r, endPos, value);
							}
						}
				 } else if(BlockType.DATA.ordinal() == lastBlockDefOrdinal) {
				    //if resource found at depth 0 and a previous text exists
				    //captures a resource with a string followed by an identifier
					if (this.lastText!=null && this.lastText.byteLength()>0) {
						AppendableBuilderReader r = lastText.reader();
						if (!r.isEqual("data".getBytes())) {
							definitionData(lastTextEndPos, r, endPos, value);
						}
					}
					//captures a resources with an identifer followed by an indentifier
					if (this.lastIdentifier!=null && this.lastIdentifier.byteLength()>0) {
						AppendableBuilderReader r = lastIdentifier.reader();
						if (!r.isEqual("data".getBytes())) {
							definitionData(lastIdentifierEndPos, r, endPos, value);
						}
					}
			    } else if(BlockType.PROVIDER.ordinal() == lastBlockDefOrdinal) {
			    		definitionProvider(1, endPos, value, endPos, value);//name of provider when quotes are missing
			    		value.copyTo(providerBuilder);
			    		lastProviderEndPos = endPos;
			    }
				 
			}
		}
		/////////////////////////////////
		/////////////////////////////////
				
		//this is the defintion of a block
		if ( 0 == blockDepth) {
			
			//only process after we have consumed the previous one
				
			value.parseSetup(localReader);
			
			int id = (int)TrieParserReader.parseNext(localReader, blockTypeParser);
			if (TrieParserReader.parseHasContent(localReader)) {
				id = -1;
			};
			if (-1 != id) {			
				lastBlockDefOrdinal = id;
				lastBlockDefEndPos = endPos;
			}
			
		} else {
			//for all depths 
			captureFieldUsage(endPos, 0, value, intPos);				
			if (0==intPos && 0==arrayDepth) {
				startIdentifierUsage(endPos, value, requestProviderAliasUsage);
			}
			
			
			if (1 == blockDepth) {
				//inside only 1 block 
				if (BlockType.LOCALS.ordinal() == lastBlockDefOrdinal) {				
					//inside the locals block, and only if we are not already parsing one
					if (0==intPos && 0==arrayDepth && false==localDefInProgress) { //top level and never inside an array
						//an identifer without any parts
						//this can only be the defintion of a local
						localDefInProgress = true;
						definitionLocal(endPos, value);
					}
				} else if (BlockType.PROVIDER.ordinal() == lastBlockDefOrdinal) {
					if (0==intPos && 0==arrayDepth) {
						//this is the definition of a new provider
						lastAssignmentIsForAliasDefinition = value.isEqual(ALIAS_BYTES);
					
					}
				} else if (BlockType.TERRAFORM.ordinal() == lastBlockDefOrdinal) {
					
				} else if (BlockType.LIFECYCLE.ordinal() == lastBlockDefOrdinal) {
					if (0==intPos && 0==arrayDepth) {
						//definitionLifcycle(endPos, value);  //cant develop untilw we get package verison.
					}
				}
			}
		}
		this.lastIdentifier.clear();
		AppendableBuilder.copy(value, this.lastIdentifier);						
		this.lastIdentifierEndPos = endPos;

		
		this.termIdentiferStackNamespace = -1;
		
		return value;
	}
	
	
	
	//specific providers in module
	protected void moduleProvider(String moduleName, String name, String ref) {
	};
	//specific provders in resource
	protected void resourceProvider(String resourceType, String resourceName, String name, String ref) {
	};
		
	//////////////////
	//////////////////
	protected void usageForArg(int endPos, AppendableBuilderReader value) {    	
    }
	protected void usageLocal(int endPos, AppendableBuilderReader value) {    	
    }
    protected void usageVariable(int endPos, AppendableBuilderReader value) {    	
    }
    
    protected void assignment(byte[] arg, int start, int end, TFExpression expressionRoot) {
    }
    
    protected void usageModule(int parts, int resourceEndPos, AppendableBuilderReader resource, int endPos, AppendableBuilderReader value) {
    }    
    protected void usageData(int parts, int resourceEndPos, AppendableBuilderReader resource, int endPos, AppendableBuilderReader value) {    	
    }
    
    protected void usageResource(int resourceEndPos, AppendableBuilderReader resource, int endPos, AppendableBuilderReader value) {    	
    }
    protected void usageProvider(int resourceEndPos, AppendableBuilderReader resource, int endPos, AppendableBuilderReader value) {    	
    }
    protected void usageTerraform(int endPos, AppendableBuilderReader value) {
	}
    protected void usageSelf(int endPos, AppendableBuilderReader value) {
	}
    protected void usageEach(int endPos, AppendableBuilderReader value) {
	}
    protected void usagePath(int endPos, AppendableBuilderReader value) {
	}
    protected void usageCount(int endPos, AppendableBuilderReader value) {
	}
    
    protected void startIdentifierUsage(int endPosition, AppendableBuilderReader value, boolean requestProviderAliasUsage) {
	}   
   
    protected void definitionForArg(int endPos, AppendableBuilderReader value) {    	
    }
    protected void definitionLocal(int endPos, AppendableBuilderReader value) {    	
    }
    protected void definitionVariable(int endPos, AppendableBuilderReader value) {    	
    }
    protected void definitionModule(int endPos, AppendableBuilderReader value) {    	
    }
    protected void definitionOutput(int endPos, AppendableBuilderReader value) {    	
    }
    protected void definitionData(int resourceEndPos, AppendableBuilderReader resource, int endPos, AppendableBuilderReader value) {    	
    }
    protected void definitionResource(int resourceEndPos, AppendableBuilderReader resource, int endPos, AppendableBuilderReader value) {  
    }    
    protected void definitionProvider(int parts, int resourceEndPos, AppendableBuilderReader resource, int endPos, AppendableBuilderReader value) {  
    }    
    protected void setActiveModuleSource(byte[] sourceLocation) {
    }
    protected void closeDefinition(int ordinal, int endPositionOfBlockName, int closingPosition) {
    }
    
    
	@Override
	public AppendableBuilderReader identifierGet(int endPos) {
		int identPos = identPosition();
		boolean isTopIdent = identPos==0;//must capture here before clear
		AppendableBuilderReader value = super.identifierGet(endPos);
		
		if (isTopIdent) {			
			
			value.parseSetup(localReader);
			
			int id = (int)TrieParserReader.parseNext(localReader, namespaceParser);
			if (TrieParserReader.parseHasContent(localReader)) {
				id = -1;
			};
			this.termIdentiferStackNamespace = id;
			if (-1==id) {
				startIdentifierUsage(endPos-1, value, requestProviderAliasUsage);
			}
		} else {
			 //subtract 1 because we do not want to include the .
			 //the extra is set to 1 when the previous was [].
			
			captureFieldUsage(endPos-1, lastTermWasIdx()?1:0, value, identPos);	
		}		
		
		this.lastIdentifier.clear();
		AppendableBuilder.copy(value, this.lastIdentifier);
		
		this.lastIdentifierEndPos = endPos-1; //do not include the .	
		return value;
	}


	private void captureFieldUsage(int endPos, int extra, AppendableBuilderReader value, int intPos) {
		
		ParseState.NAMESPACES nameSpace = termIdentiferStackNamespace<0?null:ParseState.NAMESPACES.values()[termIdentiferStackNamespace];
		BlockType blockType = null;
	
		// value is   var.  or local.  or  get the specific type...
		if (ParseState.NAMESPACES.VAR == nameSpace) {
			if (1 == intPos) {
				
				//filter this out if in the validation of its self, this is not a "usage"
				//this filter is important to ensure this is not flagged or navigated as a loop.
				//we also filter out other vars because this may still create a loop.
				// "allows referring only to the variable being validated" from TF docs
				if (BlockType.VARIABLE.ordinal() != lastBlockDefOrdinal) {
					usageVariable(endPos, value);				
				}				
				
				blockType = BlockType.VARIABLE;
			}
		} else if (ParseState.NAMESPACES.LOCAL == nameSpace) {
			if (1 == intPos) {
				usageLocal(endPos, value);
				blockType = BlockType.LOCALS;
			}
		
		} else if (ParseState.NAMESPACES.COUNT == nameSpace) {
			if (1 == intPos) {
				usageCount(endPos, value);		
			}
			
		} else if (ParseState.NAMESPACES.EACH == nameSpace) {
			if (1 == intPos) {
				usageEach(endPos, value);		
			}
			
		} else if (ParseState.NAMESPACES.MODULE == nameSpace) {
			if (2 == intPos || 1 == intPos) { 
				usageModule(intPos, lastIdentifierEndPos, lastIdentifier.reader(), endPos, value);
				blockType = BlockType.MODULE;
			}		
		} else if (ParseState.NAMESPACES.PATH == nameSpace) {
			if (1 == intPos) {
				usagePath(endPos, value);		
     		}
			
		} else if (ParseState.NAMESPACES.SELF == nameSpace) {
			if (1 == intPos) {
				usageSelf(endPos, value);		
			}
			
		} else if (ParseState.NAMESPACES.TERRAFORM == nameSpace) {
			if (1 == intPos) {
				usageTerraform(endPos, value);	
				blockType = BlockType.TERRAFORM;
			}		
		
		} else if (ParseState.NAMESPACES.DATA == nameSpace) {
			if (2 == intPos || 1 == intPos) { 
				usageData(intPos, lastIdentifierEndPos, lastIdentifier.reader(), endPos, value);
				blockType = BlockType.DATA;
			}			
			
			
			
		} else {
			  //data is discovered one step deeper	
			
			if (1 == intPos && (termIdentiferStackNamespace<0)) { //if undefined lastIdent then this must be a resource
				
				//			resource "aws_dyanamo_table" "global-us-west-1" {
				//				  provider  = aws.us-west-1  #example usage
				//				}	
								
				if (requestProviderAliasUsage) {
					usageProvider(lastIdentifierEndPos, lastIdentifier.reader(), endPos, value);
					requestProviderAliasUsage = false;
					blockType = BlockType.PROVIDER;
				} else {
					//System.out.println("field usage: "+value+" "+termIdentiferStackNamespace+" "+intPos);
					usageResource(lastIdentifierEndPos, lastIdentifier.reader(), endPos, value);
					blockType = BlockType.RESOURCE;
				}
			}
						
			
		}
		
		refUsage(nameSpace, blockType, intPos, lastIdentifierEndPos, lastIdentifier.reader(), endPos, value);
		
		
	}
	

		@Override
	public AppendableBuilderReader identifierIdxExpr(int endPos) {
		int identPos = identPosition();
		boolean isTopIdent = identPos==0;//must capture here before clear
		AppendableBuilderReader value = super.identifierIdxExpr(endPos);		
		if (isTopIdent) {			

			value.parseSetup(localReader);
			
			int id = (int)TrieParserReader.parseNext(localReader, namespaceParser);	
			if (TrieParserReader.parseHasContent(localReader)) {
				id = -1;
			};
			if (-1==id) {
				startIdentifierUsage(endPos-1, value, requestProviderAliasUsage);
			}
			this.termIdentiferStackNamespace = id;
		} else {
			//subtract 1 because we do not want ot include the [
			captureFieldUsage(endPos-1, 0, value, identPos);
		}

		this.lastIdentifier.clear();
		AppendableBuilder.copy(value, this.lastIdentifier);
		
		this.lastIdentifierEndPos = endPos-1; //do not include the [
		return value;
	}	
	
	

	private AppendableBuilderReader blockTextProcessing(int endPos, final AppendableBuilderReader text) {
		//names of resources
						
		if (isCollectingBlockLabels()) {
			//this string is a block label
			if (blockDepth==0) {
				
				if (BlockType.VARIABLE.ordinal() == lastBlockDefOrdinal) {					
					definitionVariable(endPos-1, text);					
				} else if (BlockType.DATA.ordinal() == lastBlockDefOrdinal) {
					
					if (this.lastText!=null && this.lastText.byteLength()>0) {
						AppendableBuilderReader r = lastText.reader();
						if (!r.isEqual("data".getBytes())) {
							definitionData(lastTextEndPos, r, endPos-1, text);
						}
					}
					if (this.lastIdentifier!=null && this.lastIdentifier.byteLength()>0) {
						AppendableBuilderReader r = lastIdentifier.reader();
						if (!r.isEqual("data".getBytes())) {						
							definitionData(lastIdentifierEndPos, r, endPos-1, text);
						}
					}
				} else if (BlockType.PROVIDER.ordinal() == lastBlockDefOrdinal) {
					definitionProvider(1, endPos-1, text, endPos-1, text);//name of provider
					text.copyTo(providerBuilder);
			    	lastProviderEndPos = endPos-1;
				} else if (BlockType.MODULE.ordinal() == lastBlockDefOrdinal) {
					definitionModule(endPos-1, text);
				} else if (BlockType.OUTPUT.ordinal() == lastBlockDefOrdinal) {
					definitionOutput(endPos-1, text);					
				} else if(BlockType.RESOURCE.ordinal() == lastBlockDefOrdinal) {
					//if resource found at depth 0 and a previous text exists
					//capture a resource defined by two strings
					if (this.lastText!=null && this.lastText.byteLength()>0) {
						AppendableBuilderReader r = lastText.reader();
						if (!r.isEqual("resource".getBytes())) {
							definitionResource(lastTextEndPos, r, endPos-1, text);
						}
					}
					//capture a resource starting with an identifier then a string
					if (this.lastIdentifier!=null && this.lastIdentifier.byteLength()>0) {
						AppendableBuilderReader r = lastIdentifier.reader();
						if (!r.isEqual("resource".getBytes())) {
							definitionResource(lastIdentifierEndPos, r, endPos-1, text);
						}
					}
				}
				
				this.lastText.clear();
				AppendableBuilder.copy(text, this.lastText);
				this.lastTextEndPos = endPos-1;
				
			} 
		} else {
		    //not collecting labels
			
			//this string is not part of the block name so no need to keep since it will not be changed
			//here is the only place we can collect the alias, if the previous assignment was for alias
			if (BlockType.PROVIDER.ordinal() == lastBlockDefOrdinal
			    && lastAssignmentIsForAliasDefinition
			    && providerBuilder.byteLength()>0) {
								
				definitionProvider(2, lastProviderEndPos, providerBuilder.reader(), endPos-1, text);
				lastAssignmentIsForAliasDefinition = false;//we have consumed this value
				
			}
									
		}
		return text;
	}

    

	
}
