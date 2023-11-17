package net.terraarch.tf.parse.doc;

import net.terraarch.tf.parse.BlockType;
import net.terraarch.tf.parse.ParseState;
import net.terraarch.tf.parse.TermLayer;
import net.terraarch.util.AppendableBuilderReader;
import net.terraarch.util.TrieParser;

public class TermProcessingImpl {

	static int termTwoProcessing(DocumentTokenMap that, AppendableBuilderReader value, TermLayer termLayer, StringBuilder errorMessage) {
		int result;
		
		if (value.byteLength()>0) {
			if (termLayer.dataType()>=0) {
	
				 long nameIdx = that.module.checkDataName(termLayer.dataType(), value);
		         if (nameIdx>=0) {
		        	   result = net.terraarch.tf.parse.doc.TypeColors.IDENT_USAGE.ordinal();
		         } else {
		         	   errorMessage.append("Unable to find data block named '"+value+"'");
		        	   result = net.terraarch.tf.parse.doc.TypeColors.UNDEFINED.self().ordinal();
		        }
			   
			} else {
				errorMessage.append("Undefined identifier");
				result = net.terraarch.tf.parse.doc.TypeColors.UNDEFINED.self().ordinal();
				
			}
		} else {
		    errorMessage.append("Expected an identifier");
			result = net.terraarch.tf.parse.doc.TypeColors.UNDEFINED.self().ordinal();
			
		}
		return result;
	}

	static int termZeroProcessing( DocumentTokenMap that,
			             AppendableBuilderReader value, TermLayer termLayer, StringBuilder errorMessage) {
		int result=-1;
		if (value.byteLength()>0) {
			if (termLayer.resource()<0 && termLayer.provider()<0 && termLayer.namespace()<0) {
				
				if (value.lookupExactMatch(that.localReader, ParseState.variableTypeParser)>=0) {
					result = net.terraarch.tf.parse.doc.TypeColors.IDENT_USAGE.ordinal();
				} else {		
					///check if its a for arg
					for(TrieParser parser: that.forArgStack) {
				        
						if (value.lookupExactMatch(that.localReader, parser)>=0) {
							result = net.terraarch.tf.parse.doc.TypeColors.IDENT_DETAIL.ordinal();
						}
					};
					if (-1 == result) {
						//non quoted name of a block
						if (that.blockDepth()<=0) {
							result = net.terraarch.tf.parse.doc.TypeColors.BLOCK_LABEL.ordinal();
						} else {
							//check if this a valid resource
							if (null!=that.module && that.module.checkResourceType(value)>=0) {
								result = net.terraarch.tf.parse.doc.TypeColors.IDENT_DETAIL.ordinal();
							} else {
								//check if this is inside a lifecycle and ignore_changes, do not invalidate for now, 
								//TODO: BB  check provider details for valid lifecycle use ...
								if (that.activeBlockType == BlockType.LIFECYCLE.ordinal()+1 && that.blockDepth()==2) {
									result = net.terraarch.tf.parse.doc.TypeColors.IDENT_DETAIL.ordinal();
								} else {						
									if (that.isUnderLifecycleChild()) {
									  result = net.terraarch.tf.parse.doc.TypeColors.IDENT_USAGE.ordinal();							
									} else {						
									  //this identifier is not recognized as anything
									  result = net.terraarch.tf.parse.doc.TypeColors.UNDEFINED.self().ordinal();
									}		
								}
							}
						}	
					}
				}
			} else {
				//self and path are namespaces and would appear here
				result = net.terraarch.tf.parse.doc.TypeColors.IDENT_CATIGORY.ordinal();
			}
		} else {
		    errorMessage.append("Expected an identifier");
			result = net.terraarch.tf.parse.doc.TypeColors.UNDEFINED.self().ordinal();
		}
		return result;
	}

	static int termOneProcessing(DocumentTokenMap that, AppendableBuilderReader value, TermLayer termLayer, StringBuilder errorMessage) {
		int result=-1;
				
		if (value.byteLength()>0) {
			
			if (termLayer.namespace() == ParseState.NAMESPACES.VAR.ordinal() ) {
				if (that.module.isValidVar(value)) {
				    result = net.terraarch.tf.parse.doc.TypeColors.IDENT_USAGE.ordinal();
				} else {
			        errorMessage.append("Undefined var field '"+value+"'"); //TODO: FF, can we make a guess for which var was intended
					result = net.terraarch.tf.parse.doc.TypeColors.UNDEFINED.self().ordinal();
				}	
			} else if (termLayer.namespace() == ParseState.NAMESPACES.LOCAL.ordinal()) {
				if (that.module.isValidLocal(value)) {
					result = net.terraarch.tf.parse.doc.TypeColors.IDENT_USAGE.ordinal();
				} else {
				    errorMessage.append("Undefined local field '"+value+"'"); //TODO: FF, can we make a guess for which local was intended
					result = net.terraarch.tf.parse.doc.TypeColors.UNDEFINED.self().ordinal();
				}				
				
			} else if (termLayer.namespace() == ParseState.NAMESPACES.MODULE.ordinal() ) {
				if (that.module.isValidModule(value)) {
					result = net.terraarch.tf.parse.doc.TypeColors.IDENT_USAGE.ordinal();	
				} else {
					errorMessage.append("Undefined module field '"+value+"'"); //TODO: FF, can we make a guess and better suggestion.
					result = net.terraarch.tf.parse.doc.TypeColors.UNDEFINED.self().ordinal();
				
				}
				
			} else if (termLayer.namespace() == ParseState.NAMESPACES.TERRAFORM.ordinal() ) {			
				
				boolean ok = value.lookupExactMatch(that.localReader, ParseState.terraformFieldsParser)>=0;
				if (ok) {
					result = net.terraarch.tf.parse.doc.TypeColors.IDENT_USAGE.ordinal();
				} else {
					errorMessage.append("Undefined path field '"+value+"'"); //TODO: FF, can we make a guess and better suggestion.
					result = net.terraarch.tf.parse.doc.TypeColors.UNDEFINED.self().ordinal();
				}
				
			} else if (termLayer.namespace() == ParseState.NAMESPACES.PATH.ordinal() ) {
				
			    boolean ok = value.lookupExactMatch(that.localReader, ParseState.pathTypeParser)>=0;
				if (ok) {
					result = net.terraarch.tf.parse.doc.TypeColors.IDENT_USAGE.ordinal();
				} else {
					errorMessage.append("Undefined path field '"+value+"'"); //TODO: FF, can we make a guess and better suggestion.
					result = net.terraarch.tf.parse.doc.TypeColors.UNDEFINED.self().ordinal();
				}
	
							
			} else if (termLayer.namespace() == ParseState.NAMESPACES.COUNT.ordinal() ) {
				if (value.isEqual("index".getBytes())) {
					result = net.terraarch.tf.parse.doc.TypeColors.IDENT_USAGE.ordinal();
				} else {
				    errorMessage.append("Undefined count field '"+value+"', this should probably be 'index'");
					result = net.terraarch.tf.parse.doc.TypeColors.UNDEFINED.self().ordinal();
				}
				
			} else if (termLayer.namespace() == ParseState.NAMESPACES.SELF.ordinal() ) {
	
				//self can be used inside a resource only at this time.
			    boolean isValidContext = (BlockType.RESOURCE.ordinal() == that.activeBlockType);
				
				boolean acceptAllSelfRef = true; //TODO: BBB, requires the fields for this resource. rewrite so it knows about the other fields defined here not just arn...
	            if (isValidContext && (acceptAllSelfRef || value.isEqual("arn".getBytes()))) {
	            	result = (net.terraarch.tf.parse.doc.TypeColors.IDENT_USAGE).ordinal();
	            } else {
	                errorMessage.append("Undefined self identifier '"+value+"'");
	            	result = (net.terraarch.tf.parse.doc.TypeColors.UNDEFINED.self()).ordinal();
	            }
	            				
			} else if (termLayer.namespace() == ParseState.NAMESPACES.EACH.ordinal() ) {
	
				if (value.isEqual("key".getBytes()) || value.isEqual("value".getBytes())) {
				 	result = (net.terraarch.tf.parse.doc.TypeColors.IDENT_USAGE).ordinal();
				
				} else {
				    errorMessage.append("Undefined \"for_each\" identifier '"+value+"' can only be 'key' or 'value'");
				    result = (net.terraarch.tf.parse.doc.TypeColors.UNDEFINED.self()).ordinal();				
				}
				
			} else if (termLayer.namespace() == ParseState.NAMESPACES.DATA.ordinal() ) {
				final long dataTypeIdx = that.module.checkDataType(value);
				
				if (dataTypeIdx>=0) {
					result = net.terraarch.tf.parse.doc.TypeColors.IDENT_CATIGORY.ordinal();
				} else {
				    errorMessage.append("Undefined data identifier '"+value+"'");
					result = net.terraarch.tf.parse.doc.TypeColors.UNDEFINED.self().ordinal();
				}
				termLayer.dataType(dataTypeIdx);
				
			} else if (termLayer.resource()>=0) {
									  
				if (that.module.checkResourceName(termLayer.resource(), value)>=0) {
			 	   result = net.terraarch.tf.parse.doc.TypeColors.IDENT_USAGE.ordinal();
			    } else {
			 	   errorMessage.append("Undefined resource identifier '"+value+"'");
			 	   result = net.terraarch.tf.parse.doc.TypeColors.UNDEFINED.self().ordinal();
			    }		
			} else if (termLayer.provider()>=0) {
				
				 if (that.module.checkProviderName(termLayer.provider(), value)>=0) {
			    	   result = net.terraarch.tf.parse.doc.TypeColors.IDENT_USAGE.ordinal();
			     } else {
			           errorMessage.append("Undefined provider identifier '"+value+"'");
			    	   result = net.terraarch.tf.parse.doc.TypeColors.UNDEFINED.self().ordinal();
			    }
									
			} else {
								
				//unknown values and identifiers directly following functions appear here.
				//the namespace will be -1 when this is a function
				if (-1 == termLayer.namespace()) {
					result = net.terraarch.tf.parse.doc.TypeColors.IDENT_USAGE.ordinal();
				} else {
					
					//if (isUnderLifecycleChild()) {
						//TODO: AA  we must restrict this to valid self child elements but for now we allow all.
					//	result = net.terraarch.presentation.TextColors.tokens[textColor.ordinal()][net.terraarch.presentation.TextColors.Types.IDENT_USAGE.ordinal()];
						
					//} else {
									
						errorMessage.append("Undefined identifier '"+value+"'");
						result = net.terraarch.tf.parse.doc.TypeColors.UNDEFINED.self().ordinal();
					//}
				}
				
			}
		} else {
			
			//if (true ) {
			//	result = net.terraarch.presentation.TextColors.tokens[textColor.ordinal()][net.terraarch.presentation.TextColors.Types.IDENT_USAGE.ordinal()];
				
			//} else {
			
				errorMessage.append("Expected an identifier");
				result = net.terraarch.tf.parse.doc.TypeColors.UNDEFINED.self().ordinal();
			//}
			
		}		
		
		return result;
	}

	public static int usageColorToken(DocumentTokenMap that, AppendableBuilderReader value, int termLayerDepth, TermLayer termLayer, StringBuilder errorMessage) {
		int result;
		
		int limit = ( termLayer.namespace() == ParseState.NAMESPACES.DATA.ordinal() ) ? 3: 2;
		if ((termLayerDepth < limit)) {						
					
			
			//this method also does usage color for resources so the term layer must be 1 when checking var local etc
			//if clean we can check for errors else we do not show any errors
			boolean haveValidModuleIndex = (that.isModuleClean);
			
			if (haveValidModuleIndex) {				
				if (0 == termLayerDepth) {
					result = termZeroProcessing(that, value, termLayer, (errorMessage));			
				} else if (1 == termLayerDepth) {
					result = termOneProcessing(that, value, termLayer, (errorMessage));
				} else if (2 == termLayerDepth) {
					result = termTwoProcessing(that, value, termLayer, (errorMessage));
				} else {
					result = net.terraarch.tf.parse.doc.TypeColors.IDENT_CATIGORY.ordinal();
				}				
			} else {
				//default
				result = net.terraarch.tf.parse.doc.TypeColors.IDENT_CATIGORY.ordinal();
			}
		} else {
			result = net.terraarch.tf.parse.doc.TypeColors.IDENT_DETAIL.ordinal();
		}
		
		return result;
	}

	static int namespaceParser(DocumentTokenMap that, TermLayer termLayer, AppendableBuilderReader value) {
		
		long idx = value.lookupExactMatch(that.localReader, ParseState.namespaceParser);
		
		if (idx==-1) {//this was not a normal namespace so it msut be a resource or ....
			if ( (null != that.module) ) { //if at all possible check the types
				termLayer.resource(that.module.checkResourceType(value));
				termLayer.provider(that.module.checkProviderType(value));					
			} else {
				termLayer.resource(-1); 
				termLayer.provider(-1);
			}
		}
		
		termLayer.namespace((int)idx);
		return (int)idx;
	}

}
