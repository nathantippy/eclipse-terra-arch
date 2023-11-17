package net.terraarch.tf.structure.walker;

import net.terraarch.tf.parse.ParseState;
import net.terraarch.tf.structure.GatherProposals;
import net.terraarch.tf.structure.GatherProposalsVisitor;
import net.terraarch.tf.structure.GatheredFieldType;
import net.terraarch.tf.structure.StructureDataModule;
import net.terraarch.tf.structure.StructureDataModuleManager;
import net.terraarch.util.AppendableBuilderReader;
import net.terraarch.util.TrieParserReader;


public class GatherInBoundProposals<T extends GatherProposalsVisitor> extends GatherProposals<T>   {

	private final StructureDataModule sdm;
	public final int cursorPosition;
	
	private final boolean includeDefs; //set to false for auto complete proposals
    private final StructureDataModuleManager sdmm;
    
	public GatherInBoundProposals(StructureDataModule sdm, int cursorPosition, T visitor, boolean includeDefs, StructureDataModuleManager sdmm) {
		super(visitor);
		this.sdm = sdm;		
		this.cursorPosition = cursorPosition;
		this.includeDefs = includeDefs;
		this.sdmm = sdmm;
		
	}
	
	
	private boolean isInBounds(int startPosInclude, int endPosInclude) {
		assert(startPosInclude <= endPosInclude);
		return cursorPosition>=startPosInclude && cursorPosition<=endPosInclude;
	
	}	
	
	@Override
	protected void startIdentifierUsage(int endPos, AppendableBuilderReader value, boolean requestProviderAliasUsage) {
		
		//System.out.println("ident usage value:"+value);
		int startPos; 
		if (isInBounds(startPos = (endPos-value.byteLength()), endPos)) {
			if (gatherVisitor.foundInDocument(this,endPos, value, cursorPosition-startPos, GatheredFieldType.IDENT)) {
				if (requestProviderAliasUsage) {
					sdm.visitProviderTypes(gatherVisitor);
				} else {
					sdm.visitResourceTypes(gatherVisitor);
				}
			}
		}	
	}  
	
	
	@Override
	protected void definitionData(int resourceEndPos, AppendableBuilderReader resource, int endPos,
			AppendableBuilderReader value) {

		if (includeDefs) {
			int startPos; 
	
			if (isInBounds(startPos = (resourceEndPos-resource.byteLength()),  resourceEndPos)) {
				if (gatherVisitor.foundInDocument(this, resourceEndPos, resource, cursorPosition-startPos, GatheredFieldType.DATASOURCE_TYPE)) {
					sdm.visitDataTypes(gatherVisitor);
				}	
				
			} else if (isInBounds(startPos = (endPos-value.byteLength()),  endPos)) {
				int idx = (int)sdm.checkDataType(resource);
	 		    if (gatherVisitor.foundInDocument(this, resourceEndPos, resource, endPos, value, cursorPosition-startPos, GatheredFieldType.DATASOURCE_NAME)) {
	 		    	sdm.visitDataNames(idx, gatherVisitor);
	 		    }
			}
		}
	}

	@Override
	protected void definitionLocal(int endPos, AppendableBuilderReader value) {
		if (includeDefs) {
			int startPos; 
			if (isInBounds(startPos = (endPos-value.byteLength()), endPos)) {
				if (gatherVisitor.foundInDocument(this,endPos, value, cursorPosition-startPos, GatheredFieldType.LOCAL)) {
					sdm.visitLocals(gatherVisitor);
				}
			}	
		}
	}

	@Override
	protected void definitionModule(int endPos, AppendableBuilderReader value) {
		if (includeDefs) {
			int startPos; 
			if (isInBounds(startPos = (endPos-value.byteLength()),  endPos)) {
				if (gatherVisitor.foundInDocument(this,endPos, value, cursorPosition-startPos, GatheredFieldType.MODULE_TYPE)) {
					sdm.visitModuleTypes(gatherVisitor);
				}
			}
		} else {
			//auto complete is here for children fields if the module has been defined
			//     expect auto comple in module name should offer the fields.
			
			if (isInBounds((endPos-value.byteLength()),  endPos)) {
				TrieParserReader reader = new TrieParserReader();
				int moduleIdx = sdm.moduleIndexed(reader, value.toBytes());
				if (moduleIdx>=0) {					
					StructureDataModule selectedModule =  sdmm.moduleByIdx(moduleIdx);
					if (null!=selectedModule) {
						selectedModule.visitVariables(gatherVisitor);
					}
				}
			}
			
			
		}
	}

	@Override
	protected void definitionOutput(int endPos, AppendableBuilderReader value) {
		if (includeDefs) {
			int startPos; 
			if (isInBounds(startPos = (endPos-value.byteLength()),  endPos)) {
				if (gatherVisitor.foundInDocument(this,endPos, value, cursorPosition-startPos, GatheredFieldType.MODULE_NAME)) {
					sdm.visitOutputs(gatherVisitor);
				}
			}
		}
	}
	
	@Override
	protected void definitionProvider(int parts, int resourceEndPos, AppendableBuilderReader resource, int endPos,	AppendableBuilderReader value) {
		//if (2==parts) {
			if (includeDefs) {
				int startPos; 
				
				if (isInBounds(startPos = (resourceEndPos-resource.byteLength()),  resourceEndPos)) {
		            if (gatherVisitor.foundInDocument(this, resourceEndPos, resource, cursorPosition-startPos, GatheredFieldType.PROVIDER_TYPE)) {
		            	sdm.visitProviderTypes(gatherVisitor);
		            }
				} else if (isInBounds(startPos = (endPos-value.byteLength()),  endPos)) {
					int idx = (int)sdm.checkProviderType(resource);
		 		    if (gatherVisitor.foundInDocument(this, resourceEndPos, resource, endPos, value, cursorPosition-startPos, GatheredFieldType.PROVIDER_NAME)) {
		 		    	sdm.visitProviderNames(idx, gatherVisitor);
		 		    }
				}
			}
	//}
	}

	@Override
	protected void definitionResource(int resourceEndPos, AppendableBuilderReader resource, int endPos,
			AppendableBuilderReader value) {

			int startPos; 
			if (isInBounds(startPos = (resourceEndPos-resource.byteLength()),  resourceEndPos)) {
				if (gatherVisitor.foundInDocument(this, resourceEndPos, resource, cursorPosition-startPos, GatheredFieldType.RESOURCE_TYPE)) {
					//this is based on if its in the doc and what types are used but 
					//this is NOT what we want, what we want are all the possible legal resources...
	            	if (includeDefs) {
	            		//this is used by the rename wizards
	            		sdm.visitResourceTypes(gatherVisitor);
	            	} else {
	            		
	            		//NOTE: pull in the providers here and update the value.
	            		
	            //		sdm.addProviderVersionConstraints(reader, revision, name, constraints);
	            //		sdm.visitProviderTypes(visitor);
	            	
//	            		sdm.visitProviderRecords(pir-> {
//	            			//System.out.println("provider: "+pir.toString());
//	            		});
//	            		
//	            		//this is used by the auto-complete logic
//	            		System.out.println("added hello option, TODO: this is the provider lookup here...");
//	            		gatherVisitor.visit("hello".getBytes(), 5, 0);
	            	}
	            }
				
			} else if (isInBounds(startPos = (endPos-value.byteLength()),  endPos)) {
				
				if (includeDefs) {
            		//this is used by the rename wizards
					int idx = (int)sdm.checkResourceType(resource);
		 		    if (gatherVisitor.foundInDocument(this, resourceEndPos, resource, endPos, value, cursorPosition-startPos, GatheredFieldType.RESOURCE_NAME)) {
		 		    	sdm.visitResourceNames(idx, gatherVisitor);
		 		    }
				}
			}
		
	}

	@Override
	protected void definitionVariable(int endPos, AppendableBuilderReader value) {
		if (includeDefs) {
			int startPos; 
			if (isInBounds(startPos = (endPos-value.byteLength()),  endPos)) {
				if (gatherVisitor.foundInDocument(this,endPos, value, cursorPosition-startPos, GatheredFieldType.VARIABLE)) {
					sdm.visitVariables(gatherVisitor);
				}
			}		
		}
	}

	
	
	
	@Override
	protected void usageLocal(int endPosExcluded, AppendableBuilderReader value) {
		int startPos; 
		if (isInBounds(startPos = (endPosExcluded-value.byteLength()), endPosExcluded)) {
			if (gatherVisitor.foundInDocument(this,endPosExcluded, value, cursorPosition-startPos, GatheredFieldType.LOCAL)) {
				sdm.visitLocals(gatherVisitor);
			}
		}		
	}


	@Override
	protected void usageVariable(int endPosExcluded, AppendableBuilderReader value) {
		int startPos; 
		if (isInBounds(startPos = (endPosExcluded-value.byteLength()),  endPosExcluded)) {
			if (gatherVisitor.foundInDocument(this,endPosExcluded, value, cursorPosition-startPos, GatheredFieldType.VARIABLE)) {
				sdm.visitVariables(gatherVisitor);
			}
		}		
	}


	private static final byte[] choice_index = "index".getBytes();
	
	@Override
	protected void usageCount(int endPos, AppendableBuilderReader value) {
		int startPos; 
		if (isInBounds(startPos = (endPos-value.byteLength()), endPos)) {
			if (gatherVisitor.foundInDocument(this,endPos, value, cursorPosition-startPos, GatheredFieldType.COUNT)) {
				gatherVisitor.visit(choice_index, choice_index.length,  1);
			}
		}
	}

	@Override
	protected void usagePath(int endPos, AppendableBuilderReader value) {
		int startPos; 
		if (isInBounds(startPos = (endPos-value.byteLength()), endPos)) {
			if (gatherVisitor.foundInDocument(this,endPos, value, cursorPosition-startPos, GatheredFieldType.PATH)) {
				ParseState.pathTypeParser.visitPatterns(gatherVisitor);
			}
		}
	}
	
	private static final byte[] choice_key = "key".getBytes();	
	private static final byte[] choice_value = "value".getBytes();
	
	@Override
	protected void usageEach(int endPos, AppendableBuilderReader value) {
		int startPos; 
		if (isInBounds(startPos = (endPos-value.byteLength()), endPos)) {
			if (gatherVisitor.foundInDocument(this,endPos, value, cursorPosition-startPos, GatheredFieldType.EACH)) {
				gatherVisitor.visit(choice_key,   choice_key.length,    1);
				gatherVisitor.visit(choice_value, choice_value.length,  2);
			}
			
		}
	}

	@Override
	protected void usageForArg(int endPos, AppendableBuilderReader value) {
		int startPos; 
		if (isInBounds(startPos = (endPos-value.byteLength()), endPos)) {
			if (gatherVisitor.foundInDocument(this,endPos, value, cursorPosition-startPos, GatheredFieldType.FOR_ARG)) {
			
				//TODO: J, how can we get the known args??? we need to go up the stack to find it..
				
				
			}
			
		}
	}

	@Override
	protected void usageSelf(int endPos, AppendableBuilderReader value) {
		int startPos; 
		if (isInBounds(startPos = (endPos-value.byteLength()), endPos)) {
			if (gatherVisitor.foundInDocument(this,endPos, value, cursorPosition-startPos, GatheredFieldType.SELF)) {
				
				
				//TODO: AAAA, we must look to outer object and get all the fields,  this will require the provider version
				
				
			};
		}
	}

	@Override
	protected void usageTerraform(int endPos, AppendableBuilderReader value) {
		int startPos; 
		if (isInBounds(startPos = (endPos-value.byteLength()), endPos)) {
			if (gatherVisitor.foundInDocument(this,endPos, value, cursorPosition-startPos, GatheredFieldType.TERRAFORM)) {
				ParseState.terraformFieldsParser.visitPatterns(gatherVisitor);
			};
		}
	}
	

	@Override
	protected void usageModule(int parts, int resourceEndPos, AppendableBuilderReader resource, int endPos, AppendableBuilderReader value) {
		
			int startPos; 

			if (isInBounds(startPos = (resourceEndPos-resource.byteLength()),  resourceEndPos)) {
				//this is for data.something
				if (gatherVisitor.foundInDocument(this, resourceEndPos, resource, cursorPosition-startPos, GatheredFieldType.MODULE_TYPE)) {
					sdm.visitModuleTypes(gatherVisitor);
				}	
				
			} 
			else 
			if (isInBounds(startPos = (endPos-value.byteLength()),  endPos)) {
				int idx = (int)sdm.checkModuleType(resource);
				
				if (idx>=0 && gatherVisitor.foundInDocument(this, resourceEndPos, resource, endPos, value, 
	 		    		          cursorPosition-startPos, GatheredFieldType.MODULE_NAME)) {
					
					TrieParserReader reader = new TrieParserReader();
					
					int moduleIdx = sdm.moduleIndexed(reader, resource.toBytes());
					if (moduleIdx>=0) {
						
						StructureDataModule selectedModule =  sdmm.moduleByIdx(moduleIdx);
						if (null!=selectedModule) {
							selectedModule.visitOutputs(gatherVisitor);
						}
					}
	 		    	
	 		    } else if (idx==-1 && value.byteLength()==0) {	 		    		 		    	
	 		    	//this is for data.
	 		    	if (gatherVisitor.foundInDocument(this, endPos, value, 
	 		    			      cursorPosition-startPos, GatheredFieldType.MODULE_TYPE)) {
	 					sdm.visitModuleTypes(gatherVisitor);
	 				}	
	 		    }
			}
	}
	
	
	@Override
	protected void usageData(int parts, int resourceEndPos, AppendableBuilderReader resource, int endPos, AppendableBuilderReader value) {
		if (2==parts) {
			int startPos; 
	
			if (isInBounds(startPos = (resourceEndPos-resource.byteLength()),  resourceEndPos)) {
				//this is for data.something
				if (gatherVisitor.foundInDocument(this, resourceEndPos, resource, cursorPosition-startPos, GatheredFieldType.DATASOURCE_TYPE)) {
					sdm.visitDataTypes(gatherVisitor);
				}	
				
			} else if (isInBounds(startPos = (endPos-value.byteLength()),  endPos)) {
				int idx = (int)sdm.checkDataType(resource);
	 		    if (idx>=0 && gatherVisitor.foundInDocument(this, resourceEndPos, resource, endPos, value, cursorPosition-startPos, GatheredFieldType.DATASOURCE_NAME)) {
	 		    	//this is for data.type.something
	 		    	sdm.visitDataNames(idx, gatherVisitor);
	 		    } else if (idx==-1 && value.byteLength()==0) {
	 		    	//this is for data.
	 		    	if (gatherVisitor.foundInDocument(this, endPos, value, cursorPosition-startPos, GatheredFieldType.DATASOURCE_TYPE)) {
	 					sdm.visitDataTypes(gatherVisitor);
	 				}	
	 		    }
			}
		}
	}

	@Override
	protected void usageResource(int resourceEndPos, AppendableBuilderReader resource, int endPos, AppendableBuilderReader value) {
		int startPos; 
	
		if (isInBounds(startPos = (resourceEndPos-resource.byteLength()),  resourceEndPos)) {
            if (gatherVisitor.foundInDocument(this, resourceEndPos, resource, cursorPosition-startPos, GatheredFieldType.RESOURCE_TYPE)) {
            	sdm.visitResourceTypes(gatherVisitor);
            }
			
		} else if (isInBounds(startPos = (endPos-value.byteLength()),  endPos)) {
			int idx = (int)sdm.checkResourceType(resource);
 		    if (idx>=0 && gatherVisitor.foundInDocument(this, resourceEndPos, resource, endPos, value, cursorPosition-startPos, GatheredFieldType.RESOURCE_NAME)) {
 		    	sdm.visitResourceNames(idx, gatherVisitor);
 		    }
		}
		
	}

	@Override
	protected void usageProvider(int resourceEndPos, AppendableBuilderReader resource,
			                     int endPos, AppendableBuilderReader value) {
		
		int startPos; 
		
		if (isInBounds(startPos = (resourceEndPos-resource.byteLength()),  resourceEndPos)) {
            if (gatherVisitor.foundInDocument(this, resourceEndPos, resource, cursorPosition-startPos, GatheredFieldType.PROVIDER_TYPE)) {
            	sdm.visitProviderTypes(gatherVisitor);
            }
		} else if (isInBounds(startPos = (endPos-value.byteLength()),  endPos)) {
			int idx = (int)sdm.checkProviderType(resource);
 		    if (idx>=0 && gatherVisitor.foundInDocument(this, resourceEndPos, resource, endPos, value, cursorPosition-startPos, GatheredFieldType.PROVIDER_NAME)) {
 		    	sdm.visitProviderNames(idx, gatherVisitor);
 		    }
		}
	}
	
	
}
