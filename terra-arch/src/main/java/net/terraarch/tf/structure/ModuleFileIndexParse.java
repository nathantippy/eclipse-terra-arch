package net.terraarch.tf.structure;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.terraarch.tf.parse.BlockType;
import net.terraarch.tf.parse.FieldNamesParse;
import net.terraarch.tf.parse.ParseState;
import net.terraarch.tf.parse.TFExpression;
import net.terraarch.tf.parse.version.VersionConstraint;
import net.terraarch.util.AppendableBuilderReader;
import net.terraarch.util.TrieParserReader;



public class ModuleFileIndexParse extends FieldNamesParse  {

	private static final Logger logger = LoggerFactory.getLogger(ModuleFileIndexParse.class);
			
	public StructureDataFile target;
	private StructureDataModule module;

	public TrieParserReader reader = new TrieParserReader();

	public IndexNodeDefinition activeDef = null;

	private long revision = -1; //TODO: AAAAAAAAAA needs to be used somewhere??
	
	/**
	 * This is for building the full module and file indexes on startup and each time the doc is modified
	 */
	public ModuleFileIndexParse() {
		super();
	}

    protected void startIdentifierUsage(int endPosition, AppendableBuilderReader value, boolean requestProviderAliasUsage) {
    	
	}   
	
	public void setTargets(StructureDataFile sdr, StructureDataModule structureDataModule) {
		this.target = sdr;
		this.module = structureDataModule;
//		expressionBuilder = new ExpressionBuilderTree(structureDataModule);
		
		super.reset();

		//System.out.println("GatherFieldContext: "+sdc.getFile());
		
		int x = this.target.definitionCounts.length;
		while (--x>=0) {
			this.target.definitionCounts[x] = 0;		
		}
		
		int w = this.target.usageCounts.length;
		while (--w>=0) {
			this.target.usageCounts[w]=0;
		}
		
		//this is important so we can tell what to delete after the update
		revision = sdr.nodeRevision.incrementAndGet();
		module.providersRevision.incrementAndGet();
		
		ParseState.clearModuleHalted(this);
		ParseState.clearFileHalted(this);	
	
		this.fileStart(sdr.getFile().getAbsolutePath(), sdr.getFile().getAbsolutePath());
	}

	@Override
	protected void providerVersionConstraints(String name, List<VersionConstraint> constraints, int falurePosition,
			int len, int pos) {
		if (falurePosition<0) {
			module.addProviderVersionConstraints(reader, name, constraints);
		}
	}
	//https://www.tf.io/docs/configuration/modules.html#providers-within-modules

	@Override
	protected void providerVersionConstraints(String alias, String name, List<VersionConstraint> constraints,
			int falurePosition, int len, int pos) {
		if (falurePosition<0) {
			module.addProviderVersionConstraints(reader, alias, name, constraints);
		}
	}

	@Override
	protected void providerVersionConstraints(String name, String sourceNamespace, String sourceName,
			List<VersionConstraint> constraints, int falurePosition, int len, int pos) {
		if (falurePosition<0) {
			module.addProviderVersionConstraints(reader, name, sourceNamespace, sourceName, constraints);
		}
	}

	@Override
	protected void terraformVersionConstraints(List<VersionConstraint> constraints, int falurePosition, int len, int pos) {
		if (falurePosition<0) {
			target.setTerraformConstraints(constraints);
		}
	}

	@Override
	protected void definitionLocal(int endPos, AppendableBuilderReader value) {	
		activeDef = target.defineLocal(endPos, value);
		//System.out.println(" open new def local: "+activeDef);
	}

	@Override
	protected void definitionVariable(int endPos, AppendableBuilderReader value) {
		activeDef = target.defineVariable(endPos, value);
	}

	@Override
	protected void definitionModule(int endPos, AppendableBuilderReader value) {
		activeDef = target.defineModule(reader, endPos, value, module);
	}

	@Override
	protected void definitionOutput(int endPos, AppendableBuilderReader value) {
		activeDef = target.defineOutput(endPos, value);
	}

	@Override
	protected void definitionData(int categoryEndPos, AppendableBuilderReader category, int endPos, AppendableBuilderReader value) {
		activeDef = target.defineData(reader, categoryEndPos, category, endPos, value, module);
	}

	@Override
	protected void definitionResource(int categoryEndPos, AppendableBuilderReader category, int endPos, AppendableBuilderReader value) {
		activeDef = target.defineResource(reader, categoryEndPos, category, endPos, value, module);
	}

//	
//	if (null!=activeDef) {
//		
//		if (1==parts) {
//			activeDataNode = activeDef.addUsageChild(target, BlockType.DATA, //	resourceEndPos, resource,
//			endPos, 
//				value);
//		}
//		if (2==parts) {
//			
//			//activeDef.addUsageChild(target, BlockType.MODULE, resourceEndPos, resource, endPos, value);
//			activeDataNode.reset(target.nodeRevision.get(), target, BlockType.DATA, activeDef, resourceEndPos, resource, endPos, value);
//			activeDataNode=null;
//		}
//		
//		
//		//System.out.println("recorded a usage for DATA: "+resource.toString()+"."+value.toString()+" inside "+activeDef);
////		activeDef.addUsageChild(target, BlockType.DATA, resourceEndPos, resource, endPos, value);
//	} else {
//		System.out.println("ERROR WE DROPPED A DATA USAGE: "+value);
//	}
	
	@Override
	protected void definitionProvider(int parts, int resourceEndPos, AppendableBuilderReader resource, int endPos,	AppendableBuilderReader value) {
		//if (null==activeDef) {
			if (1==parts) {
				activeDef = target.defineProvider(endPos, value, endPos, null, module, reader);			
			} else {
				activeDef.reset(target.nodeRevision.get(), target, BlockType.PROVIDER, resourceEndPos, resource, endPos, value);
				target.defineProviderInternal(resource, value, module, reader, activeDef);
			}
		//} else {
		//	System.out.println("ERROR WE DROPPED A DATA USAGE: "+value);
		//}
	}

	@Override
	protected void setActiveModuleSource(byte[] sourceLocation) {
		activeDef.setContentField("module-source", sourceLocation);
	}
	
	@Override
	protected void closeDefinition(int ordinal, int endPositionOfBlockName, int closePosition) {
		if (null!=activeDef && ordinal==activeDef.type().ordinal()) {///check to avoid any throw
		   activeDef.setEndOfDef(ordinal,endPositionOfBlockName,closePosition);	
		}
		activeDef = null;
	}

	@Override
	protected void assignment(byte[] name, int start, int stop, TFExpression expressionRoot) {
		
		//TODO: BBBB, collect these but only wehen updated and how do we clear??
		//activeDef.addAssignment
		
	}
	
	IndexNodeUsage activeDataNode;
	@Override
	protected void usageData(int parts, int resourceEndPos, AppendableBuilderReader resource, int endPos,
			AppendableBuilderReader value) {
		
		if (null!=activeDef) {
			
			if (1==parts) {
				activeDataNode = activeDef.addUsageChild(target, BlockType.DATA, //	resourceEndPos, resource,
														endPos, 
															value);
			}
			if (2==parts) {
				
				//activeDef.addUsageChild(target, BlockType.MODULE, resourceEndPos, resource, endPos, value);
				activeDataNode.reset(target.nodeRevision.get(), target, BlockType.DATA, activeDef, resourceEndPos, resource, endPos, value);
				activeDataNode=null;
			}
			
			
			//System.out.println("recorded a usage for DATA: "+resource.toString()+"."+value.toString()+" inside "+activeDef);
	//		activeDef.addUsageChild(target, BlockType.DATA, resourceEndPos, resource, endPos, value);
		} else {
			System.out.println("ERROR WE DROPPED A DATA USAGE: "+value);
		}
	}

	@Override
	protected void usageLocal(int defNameUsageEndPos, AppendableBuilderReader definitionName) {
		if (null!=activeDef) {
			//System.out.println("recorded a local usage for: "+definitionName.toString());
			activeDef.addUsageChild(target, BlockType.LOCALS, defNameUsageEndPos, definitionName);
		}
	}

	IndexNodeUsage activeModuleNode;
	@Override
	protected void usageModule(int parts, int resourceEndPos, AppendableBuilderReader resource, int endPos, AppendableBuilderReader value) {
		
		if (null!=activeDef) {
			
			if (1==parts) {
				activeModuleNode = activeDef.addUsageChild(target, BlockType.MODULE, endPos, value);
			}
			if (2==parts) {
				//NOTE: special case  Module is not the same as Data.
				activeModuleNode.reset(target.nodeRevision.get(), target, BlockType.MODULE, activeDef, resourceEndPos, resource);
				activeModuleNode.setAttribute(endPos, value);
				activeModuleNode=null;
			}
		
		} else {
			System.out.println("ERROR WE DROPPED A MODULE USAGE "+resource+" Module: "+value+" resEnd: "+resourceEndPos+" end: "+endPos+" parts: "+parts);
			
			new Exception("Dropped module, track").printStackTrace();
		}
	}

//	@Override
//	protected IndexNodeUsage usageModulePartial(int endPos, AppendableBuilderReader value) {
//		if (null!=activeDef) {
//			return activeDef.addUsageChild(target, BlockType.MODULE, endPos, value);
//		} else {
//			System.out.println("ERROR WE DROPPED A MODULE USAGE: "+value);
//			return null;
//		}
//	}
	
	
	@Override
	protected void usageResource(int resourceEndPos, AppendableBuilderReader resource, int endPos,
			AppendableBuilderReader value) {
		if (null!=activeDef) {
			//System.out.println("recorded a usage for RESOURCE: "+resource.toString()+"."+value.toString()+" inside "+activeDef);
			activeDef.addUsageChild(target, BlockType.RESOURCE, resourceEndPos, resource, endPos, value);
		} else {
			logger.warn("In index, dropped a resource usage: "+value+" at "+endPos+" in "+this.localPathLocation);
		}
	}

	@Override
	protected void usageVariable(int defNameUsageEndPos, AppendableBuilderReader definitionName) {
		if (null!=activeDef) {
			activeDef.addUsageChild(target, BlockType.VARIABLE, defNameUsageEndPos, definitionName);
		}
	}
	
	private String keyBuilder(String[] providerIdent) {
		if (3 == providerIdent.length) {
			if (   (null == providerIdent[0] || providerIdent[0].trim().length()==0)
				&& (null == providerIdent[1] || providerIdent[1].trim().length()==0)						
			) {
				return providerIdent[2];
			}
		}
		logger.error("support for namespace is not yet implemented: "+Arrays.toString(providerIdent));
		//throw new UnsupportedOperationException("support for namspace is not yet implmented");
		return null;
	}

	public void postProcessing(StructureDataModule sdm, StorageCache storageCache) {
		
		module.flushOldProviders(); //this deep filter is needed due to aliases and old revisions

	}



	
}
