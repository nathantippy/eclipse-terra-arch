package net.terraarch.terraform.structure;
//
import java.io.File;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import org.eclipse.jgit.api.Git;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.terraarch.terraform.parse.BlockType;
import net.terraarch.terraform.parse.ParseBuffer;
import net.terraarch.terraform.parse.provider.ProviderIndexRecord;
import net.terraarch.terraform.parse.version.VersionConstraint;
import net.terraarch.terraform.parse.version.VersionDTO;
import net.terraarch.util.AppendableBuilder;
import net.terraarch.util.AppendableBuilderReader;
import net.terraarch.util.TrieParser;
import net.terraarch.util.TrieParserReader;



public class StructureDataModule {

	private static final Logger logger = LoggerFactory.getLogger(StructureDataModule.class);
	
	@SuppressWarnings("unchecked")
	private SoftReference<StructureDataFile>[] records = (SoftReference<StructureDataFile>[])new SoftReference[64];
	private final TrieParser pathParser =new TrieParser(2048, 2, false, false, true);
	private long parseEntryCounts = 0;
	private long lastCallUpdate = -1;
	public final File moduleFolder;
	public final int indexPos;
	

	private final TrieParser moduleUsageIndex = new TrieParser();
	

    public int moduleIndexed(TrieParserReader reader, byte[] usedModuleName) {
    	
    	TrieParserReader.parseSetup(reader, usedModuleName, 0, usedModuleName.length, Integer.MAX_VALUE );
    	
    	int result = (int)TrieParserReader.parseNext(reader, moduleUsageIndex);
    	if (result>=0) {
    		if (!TrieParserReader.parseHasContent(reader)) {
    			return result;
    		}    		
    	}
    	return -1;
    	    	
    }
    
    public Object[] computeRecommendations() {
    	    	
    	//deep review slow method to determine desired modules and other features
    	//this is NOT IDE specific but can be used cross platform
//
//    	1. find Data/Resources (modules optional) which are never USED by other components. This must exist because graphs are directed, eg no loops.
//
//    	2. Bring in those use by these which only bring in these or themselves?
//    	Those can also be a var or local (second level to N level)
//
//    	3. at each layer check for isolated clusters we have kept up til now, those are potential modules.  The next layer may be yet another module if desired.
//    	
    	
    	//////////////////////////////
    	//capture all low level nodes
    	/////////////////////////////
    	//NOTE: AT THE FIRST LEVEL WE WANT TO FIND THE "BLOCKS" WHICH ARE OUR "GOALS" THE "SIDE EFFECTS" WE WANT BUILT.
        //      These are not the "Easy things" to build just the desired things. known because they are out or not referenced.
    	final List<List<ModuleProposal>> proposals = new ArrayList<List<ModuleProposal>>();
    	
    	visitRecords(sdr->{
    		//we have a directect graph and other code detects loops which if exist may prevent this from working.
    		//since its all directed and acylic there must be something at the bottom of the graph.
    		//we are looking for data, resources, and modules
    		sdr.visitNodeDefs(v->{
    			final BlockType type = v.type();
    			if (BlockType.RESOURCE == type || BlockType.MODULE == type) { //TODO: AAAAAA, add boolean to turn this on/off or avoid modules all together.
    				//we want the blocks which have no useages, those are building real world artificats
    				//we also include outputs since by defintion they must be at the bottom.
    				if (!hasDefUsages(this, type, v.category(), v.name())) {	
    					addToProposals(proposals, v);    				
    				}
    			} else if (BlockType.OUTPUT == type) {
    				addToProposals(proposals, v);
    			}    			
    			return true;
    		}); 
    		return true;
    	});
    	///////////////////////////////
    	
    	//scan for N layers as we combine everything
    	///////////////////////////////
    	
    	final Set<IndexNodeDefinition> doneDefs = new HashSet<>();
    	for(List<ModuleProposal> local: proposals) {
    		doneDefs.clear();    		
    		for(ModuleProposal t:local) {
    			doneDefs.add(t.current);    			
    		}
    		
	    	List<ModuleProposal> newProposals = new ArrayList<>();
	    	Set<IndexNodeDefinition> newForLevel = new HashSet<>();
	    	int level=0;
	    	do {
	    		level++;
	    		newProposals.clear();
	    		newForLevel.clear();
	    		
		    	int d = local.size();
		    	while (--d >= 0) {	    		
		    		local.get(d).buildNext(newProposals, this, level, doneDefs, newForLevel);    		
		    	}
		    	local.addAll(newProposals);
		    	doneDefs.addAll(newForLevel);
	    	} while (!newProposals.isEmpty());
    	}
    	
    	//TODO: CC, what about identical designs with different names.... imporant
    	//    Required feature to extract a common pattern where we can pass in names...
    	
    	
    	//ModuleProposal can check comonent and chilren.
    	//Note: we have 2 lists of ModuleProposal
    	
    	//given 2 level 0 nodes and two lists for lookup ensure both are the same
    	//
    
    	
    	
    	
    	
    	//NOTE: if one proposal needs a subset of what is provided by another list it as a possible roll up.
    	for(List<ModuleProposal> outer: proposals) { 
    		for(ModuleProposal mp: outer) {
    			if (0==mp.levelNumber) {
    				Set<IndexNodeDefinition> tempSet = new HashSet<>();
    				for(List<ModuleProposal> inner: proposals) { 
    					//do not look at myself
    					if (inner!=outer) {    						
    						tempSet.clear();
    						for(ModuleProposal item:inner) {
    							if (0!=item.levelNumber) {
    								tempSet.add(item.current);
    							} 
    						}
    						if (tempSet.containsAll(mp.insideDefs)) {
    							//add mp to inner as a possible child.
    							mp.addRollInto(inner);
    							for(ModuleProposal item:inner) {
        							if (0==item.levelNumber) {
        								item.addRollFrom(outer);        								
        							} 
        						}
    						}
    					}   					
    				}    			
    			}
    		}
    	}
    	
    	List<String> tempResults = new ArrayList<>();
//    	for(List<ModuleProposal> local: proposals) { 
//    		tempResults.add("------------------------------------------------");
//    		tempResults.add("ID: "+local.hashCode());
//    		System.out.println("-----------------------------");
//    		System.out.println("ID: "+local.hashCode());
//    		for(ModuleProposal mp: local) {
//    			tempResults.add("node: "+mp);
//	    		System.out.println("node: "+mp);
//    		}
//    	}
    	
    	
    	//TODO: AAAA, deep compute and return suggestion objects.
    	
    	return tempResults.toArray(new Object[tempResults.size()]);
    }
    
    //TODO: AAA, MUST HAVE AUTO COMPLETE OF PROVIDERS RESOURCES DEF
    //TODO: AAA, AUTO COMPLETE OF MODULE DEF USAGE
    //TODO: AAA, REFACTOR EXTRACT MODULE FROM SELECTED RESOURCES (EG: HOW TO EXTRACT TYPE SHAPES?)
    //TODO: AAA, SHOW RECOMENDATION IN THE TREE OUTLINNE, IF OUTLINE ISMISSING RECOMENDATIONS FAIL..
    
    //recursive check to find two idential structures.
    public boolean isTypeEqual(ModuleProposal a, List<ModuleProposal> listA,
    		                   ModuleProposal b, List<ModuleProposal> listB ) {
    	
    	if (a.current.type()==b.current.type()) {
    		if (!a.current.type().isCatigorized || 
    			Arrays.equals(a.current.category(),b.current.category())	
    			) {
    		
    			List<IndexNodeDefinition> ia = a.insideDefs;//TODO: AAA,order??
    			List<IndexNodeDefinition> ib = b.insideDefs;
    			if (ia.size() == ib.size()) {
    				int w = ia.size();
    				while (--w>=0) {
    					
    					//find in a list
    					//find in b list
    					
    					//then call isTypeEqual for this...
    					
    				}
    					
    				//TODO: AAA, we must check each pair but how to match them?


    				
    				
    				
    			}
    			
    		}
    	}
    	
    	return false;
    }
    

	public void addToProposals(final List<List<ModuleProposal>> proposals, IndexNodeDefinition v) {
		
		ModuleProposal moduleProposal = new ModuleProposal(this, 0,v);
		
		//if another one has the exact same internal usages then add it to the same list.
		int w = proposals.size();
		while (--w>=0) {
			if (proposals.get(w).get(0).insideDefs.equals(moduleProposal.insideDefs)) {
				proposals.get(w).add(moduleProposal);
				return;
			}
		}
		
		List<ModuleProposal> localList = new ArrayList<>();
		localList.add(moduleProposal);
		proposals.add(localList);
	}
    

    //this is a slow blocking call which may need to download the project
	public void indexModuleFromSource(byte[] moduleName, byte[] sourceLocation, StructureDataModuleManager sdmm, File checkoutFolder, StorageCache storageCache) {
		  		
			if (null==sourceLocation || sourceLocation.length==0) {
				logSourcelessModule(moduleName);
				return;
			}
		
			AppendableBuilder ab = new AppendableBuilder();
			ab.write(sourceLocation);
			AppendableBuilderReader reader = ab.reader();

			String url; //this source is a URL so we must pull it from the web			
			if (null != (url = gitURL(reader)) ) {
								
				File rootFolder = checkoutFolder;
						
				//TODO: BB, we have no HTTPS provider setup, check old code for this !!.
				//TODO: BB, add HTTPS module source to be more general.
								
				/////////////////////////////////////////////
				String ref = "";
				int refIdx = url.indexOf("?ref="); //this must not be part of URL we read from.				
				if (refIdx>=0) {
					ref = url.substring(refIdx+5);
					url = url.substring(0,refIdx);					
				}
				/////////////////////////////////////////////
				String targetFolder = (rootFolder.getAbsoluteFile().toString()
						+File.separatorChar
						+GitUtil.buildFolderPath(url)).replace("/.:","/")
						+File.separatorChar
						+((ref.length()==0) ? "head" : ref);
					
						try {
							Git git = GitUtil.clone(url, targetFolder);//note this is outside the workspace					
							
							if (ref.length()>0) {
								//NOTE: must switch to this branch before we parse the markdown
								try { 
									git.checkout().setName(ref).setForced(true).setCreateBranch(false).call();
								} catch (Throwable th) {
									logger.error("unable to find tag: "+ref,th); //TODO: DD show this as an error to the user to change source=
								}
							}
							//do not force a re-index since this is a versioned taged module
							sdmm.indexModuleFolder(new File(targetFolder), storageCache, false);										
							addModuleIndexed(moduleName, sdmm.pathIndex(targetFolder.getBytes()));
														
						} catch (Throwable e) {
							logger.debug(e.getMessage());
							e.printStackTrace();
						}
				
			} else {
				
				String loc = convertToPlatformSeparator(sourceLocation);
								
				if ('.'==loc.charAt(0)) { 
					//relative to the module
					
					File f = new File(moduleFolder+File.separator+loc);
					logger.error("loading relative module "+f+" exists "+f.exists());
					
					if (f.exists()) {
						sdmm.indexModuleFolder(f, storageCache, true);	//local on drive so update
						int pathIndex = sdmm.pathIndex(f.getAbsolutePath().getBytes());
						if (pathIndex >= 0) {
							addModuleIndexed(moduleName, pathIndex);
						}
					} else {
						logger.error("bad relative file path for module source: "+f.toString()+" absolute: "+f.getAbsolutePath());
					}		
					
				} else if (new File(loc).exists()) {
					
					   //this folder points to an existing module to be parsed
					   File f = new File(loc);
					   sdmm.indexModuleFolder(f, storageCache, true); //local on drive so update
					   int pathIndex = sdmm.pathIndex(f.getAbsolutePath().getBytes());
						if (pathIndex >= 0) {
							addModuleIndexed(moduleName, pathIndex);
						}
						
				} else {
					
					//TODO: AA finish out this feature, check for HTTP files or S3 files...
					
					System.out.println("unknown source is not URL or file path: "+ab.toString());
					logger.error("unknown source is not URL or file path: "+ab.toString());
				}
					
			}
					
		
	}

	private String convertToPlatformSeparator(byte[] sourceLocation) {
		int sc = File.separatorChar;
		int w = sourceLocation.length;
		byte[] tempLoc = new byte[w];
		System.arraycopy(sourceLocation, 0, tempLoc, 0, w);
		while (--w>=0) {
			if ('\\'==tempLoc[w] || '/'==tempLoc[w]) {
				tempLoc[w] = (byte)sc;
			}
		}
		String loc = new String(tempLoc,0,tempLoc.length);
		return loc;
	}

	private void logSourcelessModule(byte[] moduleName) {
		logger.info("the module "+new String(moduleName,0,moduleName.length)+" has no source definition");
	}

	
	//TODO: Z direct download  must end with ...
    // 	 zip
	//   tar.bz2 and tbz2
	//   tar.gz and tgz
	//   tar.xz and txz 
	//  source = "https://example.com/vpc-module.zip"
	//TODO: Z credentials for https arre in  .netrc  https://ec.haxx.se/usingcurl/usingcurl-netrc

	
	//TODO: A, registry direct   <NAMESPACE>/<NAME>/<PROVIDER>
	//                          source = "hashicorp/consul/aws"
	
	private String gitURL(AppendableBuilderReader reader) {
		//TODO: FF, Mercurial   source = "hg::http://example.com/vpc.hg"
		//TODO: FF, public bitbucket only: source = "bitbucket.org/hashicorp/terraform-consul-aws"
		//TODO: FF, ssh test  source = "git@github.com:hashicorp/example.git"
				
		//System.out.println("----------------------------------");
		
		String url = null;
		if (reader.startsWith("git::".getBytes())) { // ref=tags/0.12.0
			url = reader.toString();
			// github.com/muvaki/terraform-google-project
			// git::https://github.com/cloudposse/terraform-null-label.git?ref=tags/0.16.0
		}
		
		//https source = "github.com/hashicorp/example"
		if (reader.startsWith("github.com/".getBytes())) {
			url = "git::https://" + reader.toString();
		}
		if (reader.startsWith("https://github.com/".getBytes())) {
			url = "git::" + reader.toString();
		}

		
		return url;
	}
	
    
    
    public void addModuleIndexed(byte[] usedModuleName, int index) {
    	
    	moduleUsageIndex.setValue(usedModuleName, index);
    	
    }
    
	
	
	//this temp space is needed to watch for undefined providers 
	public final TrieParser undefinedProvidersInFile = new TrieParser(256, 4, false, false);
	public final ReentrantLock upinLock = new ReentrantLock();

	public StructureDataModule(int indexPos, File moduleFolder) {
		this.moduleFolder = moduleFolder;
		this.indexPos = indexPos;
		
		assert(this.moduleFolder.exists());
		assert(this.moduleFolder.isDirectory());
		assert(this.moduleFolder.listFiles().length>=1);
	}
	

	
	private final ParseBuffer sharedBuffer = new ParseBuffer();	
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		builder.append("module file folder: "+moduleFolder).append("\n");
		builder.append("entry counts: "+parseEntryCounts).append("\n");
		int w = records.length;
		while (--w>=0) {
			SoftReference<StructureDataFile> ref = records[w];
			if (null!=ref) {
				StructureDataFile rec = ref.get();
				if (null!=rec) {
					builder.append("  file: "+rec.getFile()).append("\n");
					builder.append("        "+rec.toString()).append("\n");
					
				}
			}
		}
		return builder.toString();
	}

	
	public boolean isClean() {		
		
			int x = (int)parseEntryCounts;
			while (--x >= 0) {
				StructureDataFile temp = records[x]!=null ? records[x].get() : null;
				if (temp!=null && !temp.isClean()) {
					return false; //a null is always clean since it is a deleted file
				}					
			}			
			return parseEntryCounts>0; //only true if we have some entries
	}

	private synchronized StructureDataFile lookupRecord(File file, int id) {
			
		
		
		if (id > this.records.length) {				
		    @SuppressWarnings("unchecked")
			SoftReference<StructureDataFile>[] newArray = (SoftReference<StructureDataFile>[])new SoftReference[id<<1];
			System.arraycopy(records, 0, newArray, 0, records.length);
			records = newArray;
		}
		
		StructureDataFile sdr = this.records[id]!=null ? this.records[id].get() : null;
		if (null == sdr) {	
			sdr = new StructureDataFile(file,id);
			this.records[id] = new SoftReference<StructureDataFile>(sdr);
//			File myFolder = sdr.getFile().getParentFile();
//			if(!myFolder.equals(moduleFolder)) {
//				new Exception("internal error, attempt to add file not beloning to module "+file+" vs "+moduleFolder).printStackTrace();
//			}
		} 
		return sdr;
		
	}
	

	private int lookupPathId(byte[] path) {	
		
		final long id;
		synchronized(sharedBuffer) {
			id = sharedBuffer.matchBytes(path, pathParser);
		}
		if (id >= 0) {
			return (int)id;
		}  else {
	    	long newId = parseEntryCounts++;	    	
	    	pathParser.setValue(path, newId);
	    	return (int)newId;
	    }
	}
	
	

	public StructureDataFile getSDR(File f) {
		return lookupRecord(f, lookupPathId(f.getAbsolutePath().getBytes()));
	}

	
	public long lastCallUpdate() {
		return lastCallUpdate;
	}	
	
	 
	
	public StructureDataFile indexInMemoryData(byte[] data, File file1, StorageCache storageCache) {
	
		StructureDataFile rec = lookupRecord(file1, lookupPathId(file1.getAbsolutePath().getBytes()));
		return parseAndIndex(file1, System.currentTimeMillis(), data, true, rec, storageCache);
		
	}

	
	public StructureDataFile parseAndIndex(File file, long lastModified, byte[] data, boolean isClean, StructureDataFile sdc,
			 StorageCache storageCache) {
		try {
		
			if (data==null) {
				new Exception(file.toString()).printStackTrace();
				return sdc;
			}
			
			synchronized(sharedBuffer) {
				///in this location the parse may become stack based if JVM helps...
				ModuleFileIndexParse gatherStructure = new ModuleFileIndexParse();
				gatherStructure.setTargets(sdc, this);
				sdc.lock.writeLock().lock();
				try {	
					sdc.clearBlocksKnownInThisFile();			
					if (sdc.isClean(isClean && sharedBuffer.tokenizeDocument(data, gatherStructure) )) {
						sdc.setLastModified(lastModified);
					} 
				} catch (Throwable t) {
					t.printStackTrace();
				} finally {
					gatherStructure.postProcessing(this, storageCache);
					sdc.lock.writeLock().unlock();
				}		
			}
	
			lastCallUpdate = System.currentTimeMillis(); //timestamp for module changes NOT file changes those are independent.
		
		} catch (Throwable t) {
			logger.error("parseAndIndex",t);
		}
		return sdc;
	}
	
    private TrieParserReader localReader = new TrieParserReader();
    
	public boolean isValidVar(AppendableBuilderReader value) {
		
		boolean isValid = false;
		try {
			int x = (int)parseEntryCounts;
			while (--x>=0) {
				StructureDataFile sdr = getSDR(x);
				if (null!=sdr) {
					if (!sdr.isClean()) {
						return true; //assume text is valid since parse failed
					}		
					sdr.lock.readLock().lock();
					try {
						if (value.lookupExactMatch(localReader, sdr.getVariableParser())>=0) {
							return true;
						}
					} finally {
						sdr.lock.readLock().unlock();
					}
				}
			}
		} catch (Throwable t) {
			logger.error("isValidVar",t);
		}
		return isValid; //only fail if we have all the files and nothing is found
	}

	public boolean isValidLocal(AppendableBuilderReader value) {
		try {
			int x = (int)parseEntryCounts;
			while (--x>=0) {
				StructureDataFile sdr = getSDR(x);
							
				if (null!=sdr) {
					if (!sdr.isClean()) {
						return true; //assume text is valid since parse failed
					}					
					sdr.lock.readLock().lock();
					try {
						if (value.lookupExactMatch(localReader, sdr.getLocalParser())>=0) {
							return true;
						}
					} finally {
						sdr.lock.readLock().unlock();
					}
				}
			}
		} catch (Throwable t) {
			logger.error("isValidLocal",t);
		}
		return false; //only fail if we have all the files and nothing is found
	}	
	
	
	public void visitLocals(GatherProposalsVisitor visitor) {
		try {
			int x = (int)parseEntryCounts;
			while (--x>=0) {
				StructureDataFile sdr = getSDR(x);
							
				if (null!=sdr) {
					sdr.lock.readLock().lock();
					try {
						visitor.activeRecord(sdr);
						sdr.getLocalParser().visitPatterns(visitor);
					} finally {
						sdr.lock.readLock().unlock();
					}
				}
			}
		} catch (Throwable t) {
			logger.error("visitLocals",t);
		}
	}
	
	public void visitVariables(GatherProposalsVisitor visitor) {
		try {
			int x = (int)parseEntryCounts;
			while (--x>=0) {
				StructureDataFile sdr = getSDR(x);
							
				if (null!=sdr) {
					//System.out.println("        visit patterns from: "+sdr.getFile());
					sdr.lock.readLock().lock();
					try {
						visitor.activeRecord(sdr);
						sdr.getVariableParser().visitPatterns(visitor);
					} finally {
						sdr.lock.readLock().unlock();
					}
				}
			}
		} catch (Throwable t) {
			logger.error("visitVariables",t);
		}
	}

	
	public void visitOutputs(GatherProposalsVisitor visitor) {
		try {
			int x = (int)parseEntryCounts;
			while (--x>=0) {
				StructureDataFile sdr = getSDR(x);
							
				if (null!=sdr) {
					sdr.lock.readLock().lock();
					try {
						visitor.activeRecord(sdr);
						sdr.getOutputParser().visitPatterns(visitor);
					} finally {
						sdr.lock.readLock().unlock();
					}
				}
			}
		} catch (Throwable t) {
			logger.error("visitOutputs",t);
		}
	}
	
	
//	public void visitModules(GatherProposalsVisitor visitor) {
//		try {
//			int x = (int)parseEntryCounts;
//			while (--x>=0) {
//				StructureDataRecord sdr = getSDR(x);
//							
//				if (null!=sdr) {
//					sdr.lock.readLock().lock();
//					try {
//						visitor.activeRecord(sdr);
//						sdr.getModuleParser().visitPatterns(visitor);
//					} finally {
//						sdr.lock.readLock().unlock();
//					}
//				}
//			}
//		} catch (Throwable t) {
//			logger.error("visitModules",t);
//		}
//	}
	
	public void visitModuleTypes(GatherProposalsVisitor visitor) {
		try {
			int x = (int)parseEntryCounts;
			while (--x>=0) {
				StructureDataFile sdr = getSDR(x);
							
				if (null!=sdr) {
					sdr.lock.readLock().lock();
					try {
						visitor.activeRecord(sdr);
						sdr.getModuleTypeParser().visitPatterns(visitor);
					} finally {
						sdr.lock.readLock().unlock();
					}
				}
			}
		} catch (Throwable t) {
			logger.error("visitDataTypes",t);
		}
	}
	
	public void visitDataTypes(GatherProposalsVisitor visitor) {
		try {
			int x = (int)parseEntryCounts;
			while (--x>=0) {
				StructureDataFile sdr = getSDR(x);
							
				if (null!=sdr) {
					sdr.lock.readLock().lock();
					try {
						visitor.activeRecord(sdr);
						sdr.getDataTypeParser().visitPatterns(visitor);
					} finally {
						sdr.lock.readLock().unlock();
					}
				}
			}
		} catch (Throwable t) {
			logger.error("visitDataTypes",t);
		}
	}

	public void visitDataNames(int idx, GatherProposalsVisitor visitor) {
		try {
			int x = (int)parseEntryCounts;
			while (--x>=0) {
				StructureDataFile sdr = getSDR(x);
							
				if (null!=sdr) {
					sdr.lock.readLock().lock();
					try {
						visitor.activeRecord(sdr);
						sdr.getDataNameParser(idx).visitPatterns(visitor);
					} finally {
						sdr.lock.readLock().unlock();
					}
				}
			}
		} catch (Throwable t) {
			logger.error("visitDataNames",t);
		}
	}
	
	public void visitResourceTypes(GatherProposalsVisitor visitor) {
		try {
			int x = (int)parseEntryCounts;
			while (--x>=0) {
				StructureDataFile sdr = getSDR(x);
							
				if (null!=sdr) {
					sdr.lock.readLock().lock();
					try {
						visitor.activeRecord(sdr);
						sdr.getResourceTypeParser().visitPatterns(visitor);
					} finally {
						sdr.lock.readLock().unlock();
					}
				}
			}
		} catch (Throwable t) {
			logger.error("visitResourceTypes",t);
		}
	}

	public void visitProviderTypes(GatherProposalsVisitor visitor) {
		try {
			int x = (int)parseEntryCounts;
			while (--x>=0) {
				StructureDataFile sdr = getSDR(x);
							
				if (null!=sdr) {
					sdr.lock.readLock().lock();
					try {
						visitor.activeRecord(sdr);
						sdr.getProviderTypeParser().visitPatterns(visitor);
					} finally {
						sdr.lock.readLock().unlock();
					}
				}
			}
		} catch (Throwable t) {
			logger.error("visitProviderTypes",t);
		}
	}

	public void visitResourceTypeNames(GatherProposalsVisitor visitor) {
		
		
	}
	
	public void visitResourceNames(int idx, GatherProposalsVisitor visitor) {
		try {
			int x = (int)parseEntryCounts;
			while (--x>=0) {
				StructureDataFile sdr = getSDR(x);
							
				if (null!=sdr) {
					sdr.lock.readLock().lock();
					try {
						visitor.activeRecord(sdr);
						sdr.getResourceNameParser(idx).visitPatterns(visitor);
					} finally {
						sdr.lock.readLock().unlock();
					}
				}
			}
		} catch (Throwable t) {
			logger.error("visitResourceNames",t);
		}
	}
	
	public void visitProviderNames(int idx, GatherProposalsVisitor visitor) {
		try {
			int x = (int)parseEntryCounts;
			while (--x>=0) {
				StructureDataFile sdr = getSDR(x);
							
				if (null!=sdr) {
					sdr.lock.readLock().lock();
					try {
						visitor.activeRecord(sdr);
						sdr.getProviderNameParser(idx).visitPatterns(visitor);
					} finally {
						sdr.lock.readLock().unlock();
					}
				}
			}
		} catch (Throwable t) {
			logger.error("visitProviderNames",t);
		}
	}
	
	public boolean isValidModule(AppendableBuilderReader value) {

		try {
			int x = (int)parseEntryCounts;
			while (--x>=0) {
				StructureDataFile sdr = getSDR(x);
				
				if (null!=sdr) {
					if (!sdr.isClean()) {
						return true; //assume text is valid since parse failed
					}			
					sdr.lock.readLock().lock();
					try {
						if (value.lookupExactMatch(localReader, sdr.getModuleTypeParser())>=0) {
							return true;
						}
					} finally {
						sdr.lock.readLock().unlock();
					}
				}
			}
		
		} catch (Throwable t) {
			logger.error("isValidModule",t);
		}
		return false; //only fail if we have all the files and nothing is found
	}

	public long checkDataType(AppendableBuilderReader value) {
		try {
			int x = (int)parseEntryCounts;
			while (--x>=0) {
				StructureDataFile sdr = getSDR(x);
				if (null!=sdr) {
					if (!sdr.isClean()) {
						return Long.MAX_VALUE; //assume text is valid since parse failed
					}					
					
					sdr.lock.readLock().lock();
					try {
			
						long idx;
						if ((idx = value.lookupExactMatch(localReader, sdr.getDataTypeParser()))>=0) {
							return idx;
						}
					} finally {
						sdr.lock.readLock().unlock();
					}
				}
			}
		} catch (Throwable t) {
			logger.error("checkDataType",t);
		}
		
		return -1; //only fail if we have all the files and nothing is found
	}
	
	public long checkModuleType(AppendableBuilderReader value) {
		try {
			int x = (int)parseEntryCounts;
			while (--x>=0) {
				StructureDataFile sdr = getSDR(x);
				if (null!=sdr) {
					if (!sdr.isClean()) {
						return Long.MAX_VALUE; //assume text is valid since parse failed
					}					
					
					sdr.lock.readLock().lock();
					try {
						
						long idx;
						if ((idx = value.lookupExactMatch(localReader, sdr.getModuleTypeParser()))>=0) {
							return idx;
						}
					} finally {
						sdr.lock.readLock().unlock();
					}
				}
			}
		} catch (Throwable t) {
			logger.error("checkModuleType",t);
		}
		
		return -1; //only fail if we have all the files and nothing is found
	}
	
	public long checkResourceType(AppendableBuilderReader value) {
		try {
			int x = (int)parseEntryCounts;
			while (--x>=0) {
				StructureDataFile sdr = getSDR(x);
				if (null!=sdr) {
					if (!sdr.isClean()) {
						return Long.MAX_VALUE;
					}					
					
					sdr.lock.readLock().lock();
					try {
						
						long idx = value.lookupExactMatch(localReader, sdr.getResourceTypeParser());
						if (idx>=0) {
							return idx;
						}
											
					} finally {
						sdr.lock.readLock().unlock();										
					}				
				}
			}	
		} catch (Throwable t) {
			logger.error("checkResourceType",t);
		}
		
		return -1;
	}
	
	public long checkProviderType(AppendableBuilderReader value) {

		try {
			int x = (int)parseEntryCounts;
			while (--x>=0) {
				StructureDataFile sdr = getSDR(x);
				if (null!=sdr) {
					if (!sdr.isClean()) {
						return Long.MAX_VALUE;
					}					
					
					sdr.lock.readLock().lock();
					try {
						long idx = value.lookupExactMatch(localReader, sdr.getProviderTypeParser());
						if (idx>=0) {
							return idx;
						}
					} finally {
						
						sdr.lock.readLock().unlock();
						
					}
				}
			}
		} catch (Throwable t) {
			logger.error("checkProviderType",t);
		}
		return -1;
	}

	private StructureDataFile getSDR(int x) {
		return null!=records[x] ? records[x].get() : null;
	}
	
	
	public long checkResourceName(long typeIdx, AppendableBuilderReader value) {

		try {
			if (typeIdx == Long.MAX_VALUE) {
				return Long.MAX_VALUE;
			}
		
		
			int x = (int)parseEntryCounts;
			while (--x>=0) {
				StructureDataFile sdr = getSDR(x);
				if (null!=sdr) {
					if (!sdr.isClean()) {
						return Long.MAX_VALUE;
					}					
					sdr.lock.readLock().lock();
					try {
						long idx = value.lookupExactMatch(localReader, sdr.getResourceNameParser((int)typeIdx));
						if (idx>=0) {
							return idx;
						}
						
					} finally {
						sdr.lock.readLock().unlock();
						
					}
				}
			}
		} catch (Throwable t) {
			logger.error("checkResourceName",t);
		}
		return -1;
	}
	

	public long checkProviderName(long typeIdx, AppendableBuilderReader value) {
		try {
			if (typeIdx == Long.MAX_VALUE) {
				return Long.MAX_VALUE;
			}
			
			int x = (int)parseEntryCounts;
			while (--x>=0) {
				StructureDataFile sdr = getSDR(x);
				if (null!=sdr) {
					if (!sdr.isClean()) {
						return Long.MAX_VALUE;
					}			
					sdr.lock.readLock().lock();
					try {
						
						long idx = value.lookupExactMatch(localReader, sdr.getProviderNameParser((int)typeIdx));
						if (idx>=0) {
							return idx;
						}
											
					} finally {
						sdr.lock.readLock().unlock();
					}
				}
			}
		} catch (Throwable t) {
			logger.error("checkProviderName",t);
		}
		return -1;
	}

	public long checkDataName(long typeIdx, AppendableBuilderReader value) {
		try {
			if (typeIdx == Long.MAX_VALUE) {
				return Long.MAX_VALUE;
			}
			
			int x = (int)parseEntryCounts;
			while (--x>=0) {
				StructureDataFile sdr = getSDR(x);
				if (null!=sdr) {
					if (!sdr.isClean()) {
						return Long.MAX_VALUE;
					}				
					
					sdr.lock.readLock().lock();
					try {
						long idx = value.lookupExactMatch(localReader, sdr.getDataNameParser((int)typeIdx));
						if (idx>=0) {
							return idx;
						}
						
					} finally {
						sdr.lock.readLock().unlock();
					}
					
				}
			}
		} catch (Throwable t) {
			logger.error("checkDataName",t);
		}
		return -1;
	}

    //NOTE: never reset these they are used across files
	AtomicInteger providerTypes = new AtomicInteger();
	AtomicInteger dataTypes = new AtomicInteger();
	AtomicInteger resourceTypes = new AtomicInteger();
	AtomicInteger moduleTypes = new AtomicInteger();
		
	public int nextProviderTypeIdx() {
		return providerTypes.getAndIncrement();
	}

	public int nextDataTypeIdx() {
		return dataTypes.getAndIncrement();
	}

	public int nextResourceTypeIdx() {
		return resourceTypes.getAndIncrement();
	}

	public int nextModuleTypeIdx() {
		return moduleTypes.getAndIncrement();
	}

	


	private final List<IndexNode> doneDeepStack = new ArrayList<>();
	


	private static boolean recursiveLoopDetect(StructureDataModule module, IndexNode node, List<IndexNode> doneDeepStack) {
		
		if (null!=node) {
			if (!doneDeepStack.contains(node)) {
				return !visitUsagesInsideDefinition(module, node.type(), node.category(), node.name(), u-> {
						try {
							doneDeepStack.add(node);		
							if (recursiveLoopDetect(module, u, doneDeepStack)) {
								if (doneDeepStack.contains(u)) {
									node.sdr().loopedAdd(u);		
								}
								
								node.looped = true;
								return false; //stop visiting we found a loop.
							} else {
								return true;//contiue visiting
							}
						} finally {
							if (!doneDeepStack.isEmpty()) {
								try {
									doneDeepStack.remove(doneDeepStack.size()-1);
								} catch (IndexOutOfBoundsException t) {
									//ignore this since the stack is already empty
									t.printStackTrace();
								}
							}
						}					
				});
			} else {
				node.looped = true;
				return true;
			}
		} else {
			return false;
		}
		
	}
	

	public static void scanForLoop(StructureDataModule module, StructureDataFile sdr) {
				
		sdr.loopedClear();
		//also skip all defs we have already seen.
		int t = BlockType.values().length;
		while (--t>=0) {
			IndexNodeDefinition[] defs = sdr.definitionLocations[t];
			int x = defs.length;
			while (--x>=0) {
				try {
					final IndexNodeDefinition ind = defs[x];
					module.doneDeepStack.clear();
					if (recursiveLoopDetect( module, ind, module.doneDeepStack)) {
						ind.looped = true;	
					};
				} catch (Throwable th) {
					th.printStackTrace();
				}
			}
		}
	}

	public final AtomicLong providersRevision = new AtomicLong();
	
	
	public boolean hasTerraformVersionConstraints() {
		boolean result = false;
		for(SoftReference<StructureDataFile> sr: records) {			
			StructureDataFile sdr = null;
			if (null!=sr && null!=(sdr=sr.get())) {
				result |= sdr.hasTerraformVersionConstraints();
			}
		}
		return result;
	}	
    public boolean isValidTerraformVersion(String suffix, int[] version) {
		boolean result = true;
		for(SoftReference<StructureDataFile> sr: records) {			
			StructureDataFile sdr = null;
			if (null!=sr && null!=(sdr=sr.get())) {
				result &= sdr.isValidTerraformVersion(suffix,version);
			}
		}
		return result;
    }
    

    //keep 3 maps to hold these providers
    //need to ensure the properties has all the known versions
    //we need to find the newest which passes constraints and download those details    

	public void addProviderVersionConstraints(TrieParserReader reader, String name, List<VersionConstraint> constraints) {
		
		int idx = (int)TrieParserReader.query(reader, providerName, name);
		if (idx>=0) {
			//update
			ProviderIndexRecord pr = providersList.get(idx);
			pr.update(providersRevision.get(), name, constraints);
			
		} else {
			//add new
			ProviderIndexRecord pr = new ProviderIndexRecord();
			pr.update(providersRevision.get(), name, constraints);
			providersList.add(pr);
			providerName.setUTF8Value(name, providersList.size()-1);			
			
		}
		
	}

	public void addProviderVersionConstraints(TrieParserReader reader, String alias, String name, List<VersionConstraint> constraints) {

				int idx = (int)TrieParserReader.query(reader, providerName, name);
				if (idx>=0) {
					//update
					ProviderIndexRecord pr = providersList.get(idx);
					pr.update(reader, providersRevision.get(), alias, name, constraints);
				} else {
					//add new
					ProviderIndexRecord pr = new ProviderIndexRecord();
					pr.update(reader, providersRevision.get(), alias, name, constraints);
					providersList.add(pr);
					providerName.setUTF8Value(name, providersList.size()-1);			
				}
				
	}
	
	
	public void addProviderVersionConstraints(TrieParserReader reader, String name, String sourceNamespace, String sourceName, List<VersionConstraint> constraints) {
		//Lookup existing record, if there update it, if not add a new record
		
		int idx = (int)TrieParserReader.query(reader, providerName, name);
		if (idx>=0) {
			//update
			ProviderIndexRecord pr = providersList.get(idx);
			pr.update(providersRevision.get(), name, sourceNamespace, sourceName, constraints);
			
		} else {
			//add new
			ProviderIndexRecord pr = new ProviderIndexRecord();
			pr.update(providersRevision.get(), name, sourceNamespace, sourceName, constraints);
			providersList.add(pr);
			providerName.setUTF8Value(name, providersList.size()-1);			
			
		}
		
	}
	
	private final TrieParser providerName = new TrieParser(256,false); //lookup to find the record index
	private final List<ProviderIndexRecord> providersList = new ArrayList<>();


	public void flushOldProviders() {
		long expected = providersRevision.get();
		for(ProviderIndexRecord rec: providersList) {
			rec.flushOldProviders(expected);
		}
	}

	public void visitProviderRecords(Consumer<ProviderIndexRecord> consumer) {
		//due to flushOld we need to filter out any null values.
		providersList
		    .stream()
		    .filter(p -> (null!=p && p.isValid()) )
		    .forEach(consumer);
		
	}
	
   //returns true if all the records are visited, false if we stopped early
	public boolean visitRecords(RecordVisitor<StructureDataFile> visitor) {
		for(SoftReference<StructureDataFile> sr: records) {			
			StructureDataFile sdr = null;
			if (null!=sr && null!=(sdr=sr.get())) {
				if (!visitor.accept(sdr)) {
					return false;
				}
			}
		}
		return true;
	}

    ////////////////////////////////////////////////////////////////  
	// OUTLINE VIEW of the module
	// All the methods needed for the tree view are implmented here
	////////////////////////////////////////////////////////////////
	

	
	/////////////////////////////////////////
	///utility methods to lookup the usages and defintions
	/////////////////////////////////////////
	


//	public void visitDefUsages(BLOCK_TYPES type, byte[] name, Consumer<OutlineNodeUsage> visitor) {
//		visitRecords(sdr-> {
//			sdr.visitDefUsages(type, name, visitor);
//		});
//	}
//	
//	public void visitDefUsages(BLOCK_TYPES type, byte[] category, byte[] name, Consumer<OutlineNodeUsage> visitor) {
//		visitRecords(sdr-> {
//			sdr.visitDefUsages(type, category, name, visitor);
//		});
//	}
	


  



//	public static IndexNodeDefinition lookupDef(StructureDataModule sdm, IndexNodeUsage nodeUsage) {
//		if (nodeUsage.type().isCatigorized) {
//			return sdm.lookupDef(nodeUsage.type(), nodeUsage.category(), nodeUsage.name());
//		} else {
//			return sdm.lookupDef(nodeUsage.type(), nodeUsage.name());
//		}
//	}


	public static boolean hasDefUsages(StructureDataModule sdm, BlockType type, byte[] category, byte[] name) {
		if (type.isCatigorized) {
			return !sdm.visitRecords(sdr-> {
				return !sdr.hasDefUsages(type, category, name);
			});
		} else {
			return !sdm.visitRecords(sdr-> {
				return !sdr.hasDefUsages(type, name);
			});
		}
	}



	public static boolean visitDefUsages(StructureDataModule sdm, BlockType type, byte[] category, byte[] name, RecordVisitor<IndexNodeUsage> visitor) {
		if (type.isCatigorized) {
			return sdm.visitRecords(sdr-> {
				return sdr.visitDefUsages(type, category, name, visitor);
			});
		} else {
			return sdm.visitRecords(sdr-> {
				return sdr.visitDefUsages(type, name, visitor);
			});
		}
	}


	public static boolean hasUsagesInsideDefinition(StructureDataModule sdm, BlockType type, byte[] category, byte[] name) {
		if (type.isCatigorized) {
			return !sdm.visitRecords(sdr-> {
				//does this file contain the defintion we are looking for?
				IndexNodeDefinition lookupDef = sdr.lookupDef(type, category, name);
				if (null!=lookupDef) {
					return !sdr.hasUsagesInsideDefinition(lookupDef);
				}
				return true;
			});
		} else {
			return !sdm.visitRecords(sdr-> {
				IndexNodeDefinition lookupDef = sdr.lookupDef(type, name);
				if (null!=lookupDef) {
					return !sdr.hasUsagesInsideDefinition(lookupDef);
				}
				return true;
			});
		}
	}


	public static boolean visitUsagesInsideDefinition(StructureDataModule sdm, BlockType type, byte[] category, byte[] name, RecordVisitor<IndexNodeUsage> visitor) {
		if (type.isCatigorized) {
			return sdm.visitRecords(sdr-> {
				return sdr.visitUsagesInsideDefinition(sdr.lookupDef(type, category, name),visitor);
			});
		} else {
			return sdm.visitRecords(sdr-> {
				return sdr.visitUsagesInsideDefinition(sdr.lookupDef(type, name),visitor);
			});
		}
	}


	public static IndexNodeDefinition lookupDef(StructureDataModule sdm, BlockType type, byte[] category, byte[] name) {
		final IndexNodeDefinition[] lookupDefHolder = new IndexNodeDefinition[1];
		
		if (type.isCatigorized) {
			sdm.visitRecords(sdr-> {
				return null==(lookupDefHolder[0] = sdr.lookupDef(type, category, name));
			});
		} else {
			sdm.visitRecords(sdr-> {
				return null==(lookupDefHolder[0] = sdr.lookupDef(type, name));
			});
		}
		return lookupDefHolder[0];
	}






	public boolean hasProviderVersionConstraints(TrieParserReader populatedReader) {
		for(SoftReference<StructureDataFile> sr: records) {			
			StructureDataFile sdr = null;
			if (null!=sr && null!=(sdr=sr.get())) {
				
				TrieParser defProv = sdr.getDefinedProviders();
				if (null!=defProv) {
					if (populatedReader.parseNext(defProv)>0) {
						return true;
					};
				}
			}
		}
		return false;
	}

	//contains all the cached provider details
	private final Map<String, ProviderRecord> providers = new HashMap<String, ProviderRecord>();

	public boolean hasProvider(String key, VersionDTO selectedVersion) {
		ProviderRecord rec = providers.get(key);
		if (null!=rec) {
			return rec.selectedVersion.equals(selectedVersion);			
		}
		return false;
	}

	public void setProvider(String key, VersionDTO selectedVersion, Provider prov) {
		ProviderRecord rec = new ProviderRecord(selectedVersion,prov);
		providers.put(key, rec);
	}
	
	
}
