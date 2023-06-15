package net.terraarch.terraform.structure;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.terraarch.terraform.parse.BlockType;
import net.terraarch.terraform.parse.version.VersionConstraint;
import net.terraarch.util.AppendableBuilder;
import net.terraarch.util.AppendableBuilderReader;
import net.terraarch.util.TrieParser;
import net.terraarch.util.TrieParserReader;

public class StructureDataFile {

    public StructureDataFile(File file, int hashId) {
    	this.file = file; 
    	this.hashId = hashId;
    	this.lock = new ReentrantReadWriteLock();
        this.nodeRevision = new AtomicLong();
    	assert(null!=file);
    }
    
    
	    private static final Logger logger = LoggerFactory.getLogger(StructureDataFile.class);
	    
        //we want to allow paint to happen at any time but we have a corner case when 
        //this file is getting updated and we must not allow its use in that small window
        public final ReadWriteLock lock;
		public final File file;
	    public final int hashId;
	    final AtomicLong nodeRevision; //ensures we only use valid instances
	    
		///////////////////////////////////////////////////////////////
		// these parsers hold all the elements discovered in this file 
		// these are all mutable and erased by clearParsersInThisRecord upon each file parse
		//////////////////////////////////////////////////////////////
        private final TrieParser varParser = new TrieParser(256,false);
        private final TrieParser localParser = new TrieParser(256,false);
        private final TrieParser outputParser = new TrieParser(256,false);

        private final TrieParser moduleTypeParser = new TrieParser(256,false);
        private final TrieParser resourceTypeParser = new TrieParser(256,false); //id for the type    1,2,3,4,5
        private final TrieParser dataTypeParser = new TrieParser(256,false); //id for the type    1,2,3,4,5
        private final TrieParser providerTypeParser = new TrieParser(256,false); //id for the type    1,2,3,4,5
             
      //  private final TrieParser[] moduleNameParser = new TrieParser[16]; //id has (type, nameid), what about multiple names??
        private TrieParser[] resourceNameParser = new TrieParser[16]; //id has (type, nameid), what about multiple names??
        private TrieParser[] dataNameParser = new TrieParser[16]; //id has (type, nameid), what about multiple names??
        private TrieParser[] providerNameParser = new TrieParser[16]; //id has (type, nameid), what about multiple names??

        private static final int INIT_COUNT_OF_BLOCKS_PER_FILE = 1;
        // here are the cursor index locations of any defintion blocks
    	protected final IndexNodeDefinition[][] definitionLocations = new IndexNodeDefinition[BlockType.values().length][INIT_COUNT_OF_BLOCKS_PER_FILE];
    	protected final int[] definitionCounts = new int[BlockType.values().length];
    	// here are the cursor index locations of any usages references
    	protected final IndexNodeUsage[][] usageLocations = new IndexNodeUsage[BlockType.values().length][INIT_COUNT_OF_BLOCKS_PER_FILE];
    	protected final int[] usageCounts = new int[BlockType.values().length];
        
        ///////////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////
                
    	private boolean isClean = true;  //set to false on any parse error so we can allow all text while we edit.  	

		private long lastModification; // file timestamp so we know if its changed while we have it open   

    	
    	
    	/**
    	 * Invalidates and clears old parsed data to ensure none of the old data is still avail on re-parse.
    	 */
        public void clearBlocksKnownInThisFile() {
  
        	int i = BlockType.values().length;
        	while (--i>=0) {
        			definitionCounts[i] = 0;
        			int x = definitionLocations[i].length;
        			while (--x>=0) {
	        			IndexNodeDefinition t = definitionLocations[i][x];
	        			if (t!=null) {
	        				t.invalidate();
	        			}	        			
        			}
        			
        			usageCounts[i] = 0;
        			int z = usageLocations[i].length;
        			while (--z>=0) {
	           			IndexNodeUsage u = usageLocations[i][z];
	        			if (u !=null) {
	        				u.invalidate();
	        			}
        			}
        			        			
        	}
        	
        	varParser.clear();
        	localParser.clear();
        	outputParser.clear();
        	
//        	moduleTypeParser.clear();
//        	int w = moduleNameParser.length;
//        	while (--w>=0) {
//        		if (moduleNameParser[w]!=null) {
//        			moduleNameParser[w].clear();
//        		}
//        	}
        	
        	resourceTypeParser.clear();
        	int x = resourceNameParser.length;
        	while (--x>=0) {
        		if (resourceNameParser[x]!=null) {
        			resourceNameParser[x].clear();
        		}
        	}
        	
        	dataTypeParser.clear();;
        	int y = dataNameParser.length;
        	while (--y>=0) {
        		if (dataNameParser[y]!=null) {
        			dataNameParser[y].clear();
        		}
        	}

        	providerTypeParser.clear();
        	int z = providerNameParser.length;
        	while (--z>=0) {
        		if (providerNameParser[z]!=null) {
        			providerNameParser[z].clear();
        		}
        	}
        	
        }      
        
    	
    	
    	public IndexNodeDefinition lookupDef(BlockType type, byte[] name) {
    		if (null!=type) {
	    		//preselected to the specific type
	    		IndexNodeDefinition[] temp = definitionLocations[type.ordinal()];
	    		int x = temp.length;
	    		while (--x>=0) { //NOTE: in the future trieParser MAY be faster, needs more review
	    			if (null!=temp[x] && temp[x].isRevision(nodeRevision.get()) && temp[x].isEqual(name)  ) {
	    				return temp[x];
	    			};
	    		}
    		}
    		return null;
    	}
    	
    	//scan file for first unsupported chars
    	public SDRPosition firstInvalidChar() {
    		
    		try (InputStream fist = new FileInputStream(file)) {
    			
    			AppendableBuilder builder = new AppendableBuilder();
    			builder.consumeAll(fist);
    			byte[] data = builder.toBytes(); //TODO: CC add this as a method into pronghorn
    			
    			int line = 1;
    			for (int i=0; i<data.length; i++) {
    				int c = data[i];
    				if ('\n' == c) {
    					line++;    					
    				}
    				if ((c<32 && c!=10 && c!=9 && c!=13) || c>126) {
    					return new SDRPosition(line,i,i+1);
    				}
    			}
			} catch (Exception e) {
				return new SDRPosition(0,0,0); //unable to check
			}
    		return null;
    	}
    	
    	public IndexNode findNodeAtOffset(final int offset) {
   
    		//TODO: GG, optimize!!!, binary search would be better than this linear design
    		//    must be faster since this happens as we type to find location in outline
    		
    		int shortestDistance = Integer.MAX_VALUE;
    		IndexNode result = null;
    		
    		try {
	    		int x;
	    		//////////// must find usage if possible first. /////////////////////
	    		x = usageLocations.length;
	    		while (--x>=0) {
	    			boolean first = true;
	    			IndexNodeUsage[] temp = usageLocations[x];
	    			int y = temp.length;
	    			while (--y>=0) {
	    				    IndexNodeUsage indexNodeUsages = temp[y];
	    				    if (null != indexNodeUsages && indexNodeUsages.isRevision(nodeRevision.get()) ) {
	    				    	if (first) {
	    							first = false;
	    						}
	    		
	    				    	if (       offset>=indexNodeUsages.blockPositionStart()
	    	    						&& offset<=indexNodeUsages.blockPositionEnd()) {
	    				    			
	    				    		    shortestDistance = 0;
	    				    			result = indexNodeUsages;
	    				    		
	    	    			    } else {
	    				    	
	    	    			    	int dist = Math.min(
	    				    			Math.abs(offset-indexNodeUsages.blockPositionStart()), 
	    				    			Math.abs(offset-indexNodeUsages.blockPositionEnd()));
	    	    			    	if (dist<=shortestDistance) {
	    	    			    		shortestDistance = dist;
	    	    			    		result = indexNodeUsages;
	    	    			    	}
	    	    			    }
	    				    }
	    			}
	    		}
	    		/////////////////////////////////////////////////////////////////////
	    		x = definitionLocations.length;
	    		while (--x>=0) {
	    			boolean first = true;
	    			IndexNodeDefinition[] temp = definitionLocations[x];
	    			int y = temp.length;
	    			while (--y>=0) {
	    					IndexNodeDefinition indexNodeDefinition = temp[y];
	    					if (null != indexNodeDefinition && indexNodeDefinition.isRevision(nodeRevision.get()) ) {
	    						if (first) {
	    							first = false;
	    						}
	    						
	    						if (   offset>=indexNodeDefinition.blockPositionStart()
		    						&& offset<=indexNodeDefinition.blockPositionStartEnd()) {
								
					    		    shortestDistance = 0;
					    			result = indexNodeDefinition;
					    		
		    					} else {
		    						int dist = Math.min(
		    				    			Math.abs(offset-indexNodeDefinition.blockPositionStart()), 
		    				    			Math.abs(offset-indexNodeDefinition.blockPositionStartEnd()));
	    	    			    	if (dist<=shortestDistance) {
	    	    			    		shortestDistance = dist;
	    	    			    		result = indexNodeDefinition;
	    	    			    	}
		    					}
	    					}
	    			}
	    		}
    		} catch (Throwable t) {
    			logger.error("findNodeAtOffset",t);
    		}
    		/////////////////////////////////////////////////////////////////////
    		return shortestDistance>50 ? null : result; //NOTE: hack for now may do better later
    	}

    	public IndexNodeDefinition lookupDef(BlockType type, byte[] catalog, byte[] name) {
    		//preselected to the specific type
    		IndexNodeDefinition[] temp = definitionLocations[type.ordinal()];
    		int x = temp.length;
    		while (--x>=0) { //TODO: in the future trieParser would be faster
    			if (null!=temp[x] && temp[x].isRevision(nodeRevision.get()) ) {
    				if (null!=catalog && catalog.length>0 && temp[x].isEqual(catalog, name)) {
	    				return temp[x];
	    			}
    			}
    		}
//    		System.out.println("checked "+checked+" unable to find: "+type+" "
//    		                  +(null==catalog?"":new String(catalog,0,catalog.length))+" "
//    				          +new String(name,0,name.length));
    		return null;
    	}

    	
    	//this is only for this file and must be called form the module on every file
    	public boolean visitDefUsages(BlockType type, byte[] name, RecordVisitor<IndexNodeUsage> visitor) {
    		//since all the children for all the defs are here we can do a single scan
    		IndexNodeUsage[] typedChildren = usageLocations[type.ordinal()];
    		for(int x=0;x<typedChildren.length;x++) {
    			if (null!=typedChildren[x] && typedChildren[x].isRevision(nodeRevision.get()) && typedChildren[x].isMatch(name)) {
    				if (!visitor.accept(typedChildren[x])) {
    					return false;
    				};
    			}
    		}
    		return true;
    	}
    	
    	public boolean visitDefUsages(BlockType type, byte[] category, byte[] name, RecordVisitor<IndexNodeUsage> visitor) {
    		//since all the children for all the defs are here we can do a single scan
    		IndexNodeUsage[] typedChildren = usageLocations[type.ordinal()];
    		for(int x=0;x<typedChildren.length;x++) {
    			if (null!=typedChildren[x] && typedChildren[x].isRevision(nodeRevision.get()) && typedChildren[x].isMatch(category, name)) {
    				if (!visitor.accept(typedChildren[x])) {
    					return false;
    				};
    			}
    		}
    		return true;
    	}
    	
    	

    	
   
    	
    	public boolean hasUsagesInsideDefinition(IndexNodeDefinition def) {
    		if (null!=def) {
	    		//since all the children for all the defs are here we can do a single scan
	    		//our child could be of any type so we must scan them all.
    			int t = BlockType.values().length;
	    		while (--t>=0) {
    			    IndexNodeUsage[] typedChildren = usageLocations[t];
    			    for(int x=0;x<typedChildren.length;x++) {
		    			if (null!=typedChildren[x]) {
		    				if (typedChildren[x].getParentDef()!=null&& typedChildren[x].isRevision(nodeRevision.get()) ) {
				    			if (def.equals(typedChildren[x].getParentDef()) ) {
				    				return true;
				    			}
		    				} 
		    			}
		    		}
	    		}
    		}
    		return false;
    	}
    	
    	//
    	public boolean visitUsagesInsideDefinition(IndexNodeDefinition def, RecordVisitor<IndexNodeUsage> visitor) {
    		//def is not null if we are in the right file.
    		if (null!=def) {
	    		//since all the children for all the defs are here we can do a single scan
    			int t = BlockType.values().length; //ordered by block type
	    		while (--t>=0) { 
		    		IndexNodeUsage[] typedChildren = usageLocations[t];
		    		for(int x=0;x<typedChildren.length;x++) {
		    			if (null!=typedChildren[x] 
		    				&& typedChildren[x].getParentDef() == def 
		    				&& typedChildren[x].isRevision(nodeRevision.get()) ) {
		    				    
			    				if (!visitor.accept(typedChildren[x])) {
			    					return false;
			    				};
			    				
		    			}
		    		}
	    		}
    		}
    		return true;
    	}
    
    	
    	//this is only for this file and must be called form the module on every file
    	public boolean hasDefUsages(BlockType type, byte[] name) {
    		//since all the children for all the defs are here we can do a single scan
    		IndexNodeUsage[] typedChildren = usageLocations[type.ordinal()];
    		for(int x=0;x<typedChildren.length;x++) {
    			if (null!=typedChildren[x] && typedChildren[x].isRevision(nodeRevision.get()) && typedChildren[x].isMatch(name)) {
    				return true;
    			}
    		}
    		return false;
    	}
    	
    	public boolean hasDefUsages(BlockType type, byte[] catalog, byte[] name) {
    		//since all the children for all the defs are here we can do a single scan
    		IndexNodeUsage[] typedChildren = usageLocations[type.ordinal()];
    		for(int x=0;x<typedChildren.length;x++) {
    			if (null!=typedChildren[x] && typedChildren[x].isRevision(nodeRevision.get()) && typedChildren[x].isMatch(catalog, name)) {
    				return true;
    			}
    		}
    		return false;
    	}
    	
    	
    	
    	
  
        public String toString() {
        	StringBuilder builder = new StringBuilder();
        	
        	int maxLength = 0;
        	maxLength = Math.max(maxLength, varParser.longestKnown());
        	maxLength = Math.max(maxLength, localParser.longestKnown());
        	maxLength = Math.max(maxLength,	outputParser.longestKnown());
        	maxLength = Math.max(maxLength, moduleTypeParser.longestKnown());
        	maxLength = Math.max(maxLength, resourceTypeParser.longestKnown());
        	maxLength = Math.max(maxLength, dataTypeParser.longestKnown());
        	maxLength = Math.max(maxLength, providerTypeParser.longestKnown());
        	
        	builder.append(" longestKnown: "+maxLength);
        	
        	return builder.toString();
        	
        }
        
  
       		
		public void setLastModified(long value) {
			this.lastModification = value;	
		}		
		public long getLastModified() {
			return this.lastModification;
		}
		
		public boolean isClean(boolean value) {
			return this.isClean = value;
		}
		
		public boolean isClean() {
			return this.isClean;
		}
			
		
		public TrieParser getVariableParser() {
			return varParser;
		}
		
		public TrieParser getLocalParser() {
			return localParser;
		}

	

		public TrieParser getOutputParser() {
			return outputParser;
		}
		

		public synchronized TrieParser getProviderNameParser(int idx) {
			if (idx >= providerNameParser.length) {
				TrieParser[] newNameParser = new TrieParser[idx<<1];
				System.arraycopy(providerNameParser, 0, newNameParser, 0, providerNameParser.length);
				providerNameParser = newNameParser;
			}			
			if (null == providerNameParser[idx]) {
				return providerNameParser[idx] = new TrieParser(64,false);
			} else {		
				return providerNameParser[idx];
			}
		}				
		
		public TrieParser getModuleTypeParser() {
			return moduleTypeParser;
		}		
		
			
		public TrieParser getProviderTypeParser() {
			return providerTypeParser;
		}		
		public TrieParser getResourceTypeParser() {
			return resourceTypeParser;
		}		
		public TrieParser getDataTypeParser() {
			return dataTypeParser;
		}

		public synchronized TrieParser getResourceNameParser(int idx) {
			if (idx >= resourceNameParser.length) {
				TrieParser[] newNameParser = new TrieParser[idx<<1];
				System.arraycopy(resourceNameParser, 0, newNameParser, 0, resourceNameParser.length);
				resourceNameParser = newNameParser;
			}			
			if (null == resourceNameParser[idx]) {
				return resourceNameParser[idx] = new TrieParser(64,false);
			} else {		
				return resourceNameParser[idx];
			}
		}
	
				

		public synchronized TrieParser getDataNameParser(int idx) {
			if (idx >= dataNameParser.length) {
				TrieParser[] newNameParser = new TrieParser[idx<<1];
				System.arraycopy(dataNameParser, 0, newNameParser, 0, dataNameParser.length);
				dataNameParser = newNameParser;
			}			
			if (null == dataNameParser[idx]) {
				return dataNameParser[idx] = new TrieParser(64,false);
			} else {		
				return dataNameParser[idx];
			}
		}


		public File getFile() {
			return file;
		}


		public IndexNodeDefinition defineLocal(int endPos, AppendableBuilderReader value) {
			definitionLocations[BlockType.LOCALS.ordinal()] = StructureDataFile.growAsNeeded(definitionLocations[BlockType.LOCALS.ordinal()], definitionCounts[BlockType.LOCALS.ordinal()]);		
			IndexNodeDefinition localDef = StructureDataFile.buildDef(this, BlockType.LOCALS, definitionLocations[BlockType.LOCALS.ordinal()], definitionCounts[BlockType.LOCALS.ordinal()], endPos, value); 
			value.addToTrieParser(getLocalParser(), definitionCounts[BlockType.LOCALS.ordinal()]++);
			return localDef;
		}

		public IndexNodeDefinition defineVariable(int endPos, AppendableBuilderReader value) {
			definitionLocations[BlockType.VARIABLE.ordinal()] = StructureDataFile.growAsNeeded(definitionLocations[BlockType.VARIABLE.ordinal()], definitionCounts[BlockType.VARIABLE.ordinal()]);
			IndexNodeDefinition localDef = StructureDataFile.buildDef(this, BlockType.VARIABLE, definitionLocations[BlockType.VARIABLE.ordinal()], definitionCounts[BlockType.VARIABLE.ordinal()], endPos, value); 
			value.addToTrieParser(getVariableParser(), definitionCounts[BlockType.VARIABLE.ordinal()]++);
			return localDef;
		}

		public IndexNodeDefinition defineModule(TrieParserReader reader, int endPos, AppendableBuilderReader value, StructureDataModule localModule) {
			
			final int indexPos = definitionCounts[BlockType.MODULE.ordinal()];
			definitionLocations[BlockType.MODULE.ordinal()] = StructureDataFile.growAsNeeded(
								definitionLocations[BlockType.MODULE.ordinal()], indexPos);
			
			IndexNodeDefinition localDef = StructureDataFile.buildDef(this, BlockType.MODULE, 
								definitionLocations[BlockType.MODULE.ordinal()], 
								indexPos,
								endPos, value); 
									
			long idx = value.lookupExactMatch(reader, getModuleTypeParser());
			if (idx<0) {
		
				idx = (int)localModule.checkModuleType(value);
				if (idx == -1) {
					//no other files have this resource defined already (we must use if found)
					idx = localModule.nextModuleTypeIdx();				
				}			
				value.addToTrieParser(getModuleTypeParser(), idx);
			} else {
				
				//TODO: B, dupe detect, why is the module already defined in my record here?
				
			}
			definitionCounts[BlockType.MODULE.ordinal()] = 1+indexPos;
			return localDef;
		}

		public IndexNodeDefinition defineOutput(int endPos, AppendableBuilderReader value) {
			definitionLocations[BlockType.OUTPUT.ordinal()] = StructureDataFile.growAsNeeded(definitionLocations[BlockType.OUTPUT.ordinal()], definitionCounts[BlockType.OUTPUT.ordinal()]);
			IndexNodeDefinition localDef = StructureDataFile.buildDef(this, BlockType.OUTPUT, definitionLocations[BlockType.OUTPUT.ordinal()], definitionCounts[BlockType.OUTPUT.ordinal()], endPos, value); 
			value.addToTrieParser(getOutputParser(), definitionCounts[BlockType.OUTPUT.ordinal()]++);
			return localDef;
		}

		public IndexNodeDefinition defineData(TrieParserReader reader, int resourceEndPos, AppendableBuilderReader resource, int endPos, AppendableBuilderReader value, StructureDataModule localModule) {
			definitionLocations[BlockType.DATA.ordinal()] = StructureDataFile.growAsNeeded(definitionLocations[BlockType.DATA.ordinal()], definitionCounts[BlockType.DATA.ordinal()]);
			IndexNodeDefinition localDef = StructureDataFile.buildDef(this, BlockType.DATA, definitionLocations[BlockType.DATA.ordinal()], definitionCounts[BlockType.DATA.ordinal()], resourceEndPos, resource, endPos, value); 
			long idx = resource.lookupExactMatch(reader, getDataTypeParser());
			if (idx<0) {
		
				idx = (int)localModule.checkDataType(resource);
				if (idx == -1) {
					//no other files have this resource defined already (we must use if found)
					idx = localModule.nextDataTypeIdx();				
				}			
				resource.addToTrieParser(getDataTypeParser(), idx);
			}
			//TODO: B, Doubles Check done here, check if this name exists first and flag as error if it does, add this to the other cases.
			TrieParser dataNameParser2 = getDataNameParser((int)idx);
			value.addToTrieParser(dataNameParser2, definitionCounts[BlockType.DATA.ordinal()]++);
			return localDef;
		}

		public IndexNodeDefinition defineResource(TrieParserReader reader, int resourceEndPos, AppendableBuilderReader resource, int endPos, AppendableBuilderReader value, StructureDataModule localModule) {
			
			definitionLocations[BlockType.RESOURCE.ordinal()] = StructureDataFile.growAsNeeded(definitionLocations[BlockType.RESOURCE.ordinal()], definitionCounts[BlockType.RESOURCE.ordinal()]);
			IndexNodeDefinition localDef = StructureDataFile.buildDef(this, BlockType.RESOURCE, definitionLocations[BlockType.RESOURCE.ordinal()], definitionCounts[BlockType.RESOURCE.ordinal()], resourceEndPos, resource, endPos, value); 
			long idx = resource.lookupExactMatch(reader, getResourceTypeParser());
			if (idx<0) {
				idx = (int)localModule.checkResourceType(resource);
				if (idx == -1) {
					//no other files have this resource defined already (we must use if found)
					idx = localModule.nextResourceTypeIdx();				
				}			
				resource.addToTrieParser(getResourceTypeParser(), idx);
			}
			
			TrieParser resourceNameParser2 = getResourceNameParser((int)idx);

			//TODO: B, Doubles Check done here, check if this name exists first and flag as error if it does, add this to the other cases.
			long result = value.lookupExactMatch(reader, resourceNameParser2);
						
			value.addToTrieParser(resourceNameParser2, definitionCounts[BlockType.RESOURCE.ordinal()]++);
						
			return localDef;
		}

		
		public IndexNodeDefinition defineProvider(int resourceEndPos, AppendableBuilderReader resource, int endPos, AppendableBuilderReader value, StructureDataModule localModule, TrieParserReader reader) {
			definitionLocations[BlockType.PROVIDER.ordinal()] = StructureDataFile.growAsNeeded(definitionLocations[BlockType.PROVIDER.ordinal()], definitionCounts[BlockType.PROVIDER.ordinal()]);
			
			IndexNodeDefinition localDef;
			if (null!=value) {
				localDef = StructureDataFile.buildDef(this, BlockType.PROVIDER, definitionLocations[BlockType.PROVIDER.ordinal()], definitionCounts[BlockType.PROVIDER.ordinal()], resourceEndPos, resource, endPos, value); 
			} else {
				//default provider without any name.	
				localDef = StructureDataFile.buildDef(this, BlockType.PROVIDER, definitionLocations[BlockType.PROVIDER.ordinal()], definitionCounts[BlockType.PROVIDER.ordinal()], resourceEndPos, resource); 
			}
			return defineProviderInternal(resource, value, localModule, reader, localDef);			
		}

		public IndexNodeDefinition defineProviderInternal(AppendableBuilderReader resource,
				AppendableBuilderReader value, StructureDataModule localModule, TrieParserReader reader,
				IndexNodeDefinition localDef) {
			long idx = resource.lookupExactMatch(reader, getProviderTypeParser());
			if (idx<0) {
				idx = (int)localModule.checkProviderType(resource);
				if (idx == -1) {
					//no other files have this resource defined already (we must use if found)
					idx = localModule.nextProviderTypeIdx();				
				}					
				resource.addToTrieParser(getProviderTypeParser(), idx);
			}
			int value2 = definitionCounts[BlockType.PROVIDER.ordinal()]++;
			if (null!=value) {
				//TODO: B, Doubles Check done here, check if this name exists first and flag as error if it does, add this to the other cases.
				TrieParser providerNameParser2 = getProviderNameParser((int)idx);
				value.addToTrieParser(providerNameParser2, value2);
			}
			
			return localDef;
		}

		private static final IndexNodeDefinition buildDef(StructureDataFile sdr, BlockType block_type, IndexNodeDefinition[] input, int idx, int categoryEndPos, AppendableBuilderReader category, int endPos, AppendableBuilderReader value) {
			if (null!=input[idx]) {				
				input[idx].reset(sdr.nodeRevision.get(), sdr, block_type, categoryEndPos, category, endPos, value);
			} else {
				input[idx] = new IndexNodeDefinition(sdr.nodeRevision.get(), sdr, block_type, categoryEndPos, category, endPos, value);
			}
			return input[idx];
		}

		private static final IndexNodeDefinition buildDef(StructureDataFile sdr, BlockType block_type, IndexNodeDefinition[] input, int idx, int endPos, AppendableBuilderReader value) {
			if (null!=input[idx]) {
				input[idx].reset(sdr.nodeRevision.get(), sdr, block_type, endPos, value);
			} else {
				input[idx] = new IndexNodeDefinition(sdr.nodeRevision.get(), sdr, block_type, endPos, value);
			}
			return input[idx];
		}

		public static final IndexNodeDefinition[] growAsNeeded(IndexNodeDefinition[] input, int pos) {
			if (pos<input.length) {
				return input;
			} else {
				IndexNodeDefinition[] output = new IndexNodeDefinition[input.length*2];
				System.arraycopy(input, 0, output, 0, input.length);
				return output;
			}
		}		

		public static final IndexNodeUsage[] growAsNeeded(IndexNodeUsage[] input, int idx) {
			if (idx < input.length) {
				return input;
			} else {
				IndexNodeUsage[] output = new IndexNodeUsage[input.length*2];
				System.arraycopy(input, 0, output, 0, input.length);
				return output;
			}
		}

		private List<IndexNodeUsage> loopedNodes = new ArrayList<>();
		
		public List<IndexNodeUsage> loopedNodes() {
			return loopedNodes;
		}
		public void loopedAdd(IndexNodeUsage indexNode) {
			if (!loopedNodes.contains(indexNode)) {
				loopedNodes.add(indexNode);
			}
		}
		public void loopedClear() {
			loopedNodes.clear();
		}

		public boolean visitNodeDefs(RecordVisitor<IndexNodeDefinition> visitor) {
			int t = BlockType.values().length;
			while (--t>=0) {
				IndexNodeDefinition[] defs = definitionLocations[t];
				if (null!=defs) {
					int d = defs.length;
					while (--d>=0) {
						IndexNodeDefinition indexNodeDefinition = defs[d];
						if (null!=indexNodeDefinition && indexNodeDefinition.isRevision(nodeRevision.get())) {	
							if (!visitor.accept(indexNodeDefinition)) {
								return false;
							}
						}
					}
				}
			}
			return true;
		}
		
		public boolean visitNodeDefs(BlockType type, RecordVisitor<IndexNodeDefinition> visitor) {
				IndexNodeDefinition[] defs = definitionLocations[type.ordinal()];
				if (null!=defs) {
					int d = defs.length;
					while (--d>=0) {
						IndexNodeDefinition indexNodeDefinition = defs[d];
						if (null!=indexNodeDefinition && indexNodeDefinition.isRevision(nodeRevision.get())) {	
							if (!visitor.accept(indexNodeDefinition)) {
								return false;
							}
						}
					}
				}
			return true;
		}

		/////////////////////////////////////////////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////
		private final List<VersionConstraint> terraformConstraints = new ArrayList<>();
		public boolean hasTerraformVersionConstraints() { 
			//System.out.println("-------------------- tf constraints "+terraformConstraints.size());
			return !terraformConstraints.isEmpty();
		}
		public void clearTerraformVersionConstraints() {
			//new Exception("clear Tf ver").printStackTrace();
			terraformConstraints.clear();
		}
		public boolean isValidTerraformVersion(String suffix, int[] version) {
			boolean result = true;
			for(VersionConstraint vc:terraformConstraints) {
				result &= vc.isValid(version, suffix);
			}
			return result;
		}		
		public void setTerraformConstraints(List<VersionConstraint> constraints) {
			if (null!=constraints) {
				constraints.forEach(vc-> {
					if (!terraformConstraints.contains(vc)) {
						terraformConstraints.add(vc);				
					}
				});
			}
		}
        ///////////////////////////////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////
		
		private TrieParser definedProviders;
		public void setDefinedProviders(TrieParser definedProviders) {
			this.definedProviders = definedProviders;
		}
		public TrieParser getDefinedProviders() {
			return definedProviders;
		}
		
		
}
