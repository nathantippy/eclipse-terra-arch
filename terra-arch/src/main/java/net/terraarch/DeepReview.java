package net.terraarch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;

import net.terraarch.util.TrieParserReader;
import net.terraarch.util.TrieParserVisitor;
import net.terraarch.tf.parse.BlockType;
import net.terraarch.tf.structure.SDRPosition;
import net.terraarch.tf.structure.StructureDataModule;

import net.terraarch.index.DynamicMarkerSet;
import net.terraarch.quickfix.QuickFixerProviderVersion;
import net.terraarch.quickfix.QuickFixerTerraformVersion;

public class DeepReview {

	public static void runOtherActions(StructureDataModule module) {
		//if this module is clean with a good parse then call further actions
		if (module.isClean()) {
			TerraArchActivator d = TerraArchActivator.getDefault();
			int w = TerraArchActivator.MAX_ACTIONS;
			while(--w>=0) {
				if (null != d.deepReviewActions[w]) {
					d.deepReviewActions[w].accept(module);					
				}
			}					
		}
	}
	/////////////////////////////////////////////////////////////

	public static void deepReview(StructureDataModule module, Collection<String> sfnad) {
	
		/////////////////
		//we assume the module is in sync and fully parsed from the file
		//this code will draw concusions from what has been captured
		/////////////////
		
		
		try {
			DeepReview.runPrimaryActions(module, sfnad);	
			runOtherActions(module);
		} catch (Throwable t) {
			TerraArchActivator.logger.error("unable to finish deep review",t);
			t.printStackTrace();
		}
	}

	public static void runPrimaryActions(final StructureDataModule module, final Collection<String> doneFiles ) {
				
		module.upinLock.lock(); //for module.undefinedProvidersInFile
		try {
			TerraArchActivator.terraformTargetFile = null;
			TerraArchActivator.missingProviderVersions.clear(); 
			
			final int markerRev = TerraArchActivator.markerRevsion.incrementAndGet();
			
			module.visitRecords(sdr -> {		
				
				try {
					final IFile ifile = TerraArchActivator.getIFile(sdr);			
					
					if (null!=ifile) {
						SDRPosition pos = sdr.firstInvalidChar();
						if (null!=pos) {
							DynamicMarkerSet.newMarker(ifile, "Invalid character", 
									pos.startPosition,
									pos.endPosition,
									pos.lineNumber,
									markerRev, true, IMarker.SEVERITY_ERROR, null);
						}
						
					}
					
					if ( (null==doneFiles || null==ifile || !doneFiles.contains(ifile.getFullPath().toString()))) {
						
						//key method for removing deleted files
						if (!sdr.getFile().exists()) {
							/////////////////////////////////////////////////////////
							//only clear if this file is missing and does not exit
							///////////////////////////////////////////////////////
							sdr.clearBlocksKnownInThisFile();	
						}
											
						//clear before we begin parse of this file
						sdr.clearTerraformVersionConstraints();				
						sdr.setDefinedProviders(null);
	
						//this has the side effect of populating looped nodes int sdr
						// in the following recordFileMarks any loops found in sdr are reported.
						StructureDataModule.scanForLoop(module, sdr);
			
						//if we have an editor then we should gather the marks
						
						//must be done AFTER loop detection.
						///////////////////////////////////////////////////////////////////////////////
						//NOTE: this will modify if we have version consraints, so we can not use that while we loop here
						DynamicMarkerSet.recordFileMarks(markerRev, module, sdr, ifile);
							
						sdr.visitNodeDefs(BlockType.MODULE,
								         def->{ 
									        	  try {
										        	    byte[] moduleName = def.name();
										        	    
										        	   // if (null!=def.getContentKey() && "module-source".equals(def.getContentKey())) {
										        	    	//confirm.
										        	    //}
										        	    
										        	    boolean hasContent = (null!=def.getContentData()) && (def.getContentData().length>0);
										        	    
										        	    if (!hasContent) { //this is an error every time in windows !!!
										        	    	TerraArchActivator.logger.error("err in module: "+new String(moduleName,0,moduleName.length)+" it has no source value under "+String.valueOf(def.getContentKey()));
										        	    	
										        	    }					
										        	    //check if already indexed, if not spin up new thread to download.							        	 
														if ( hasContent && module.moduleIndexed(TerraArchActivator.primaryActionsReader, moduleName)<0) {
										        		   			        		   
										        		   int seconds = (int)Math.abs(Math.random()*7);
										        		  // logger.trace("-----------  schedule module "+new String(moduleName,0,moduleName.length)+" fetch in: "+seconds+" sec from "+new String(def.getContentData()));
														   Job.create(
										        				   "Fetch module "+new String(moduleName,0,moduleName.length), 
										        				   new ICoreRunnable() {
										        					   @Override
										        					   public void run(IProgressMonitor mon) throws CoreException {
										        						   module.indexModuleFromSource(def.name(), def.getContentData(), TerraArchActivator.sdmm, 
										        								   TerraArchActivator.getDefault().getCheckoutFolder(), TerraArchActivator.getDefault().storageCache());
										        					
										        					   }
										        				   }							        				   
										        			).schedule(seconds);
										        		   
										        	    };
									        	    
									        	  } catch (Throwable t) {
									        		  TerraArchActivator.logger.error("primary deep review",t);
									        	  }
									        	    
									        	  return true;
								        	    
								        	    
								              });
						
					}
				  } catch (Throwable t) {
	        		  TerraArchActivator.logger.error("primary deep review",t);
	        	  }			
				return true;
			});
			
			///////////////////////////////////
			//above we do a fresh parse to ensure we have all the versions defined
			//collect any data on providers whithout versions
			//////////////////////////////////
			
			final TrieParserReader provReader = new TrieParserReader();
			module.visitRecords(sdr -> {
				
				////////////////////////////////////////////////////////////////////
				//if we have no TF version then find the file we need to put it
				//check this SDR to see if it would be the best place to put it
				///////////////////////////////////////////////////////////////////
				if (!sdr.hasTerraformVersionConstraints()) {
					String name = sdr.getFile().getName();
					if (name.endsWith(".tf")) {
						//if no file keep this one
						if (null == TerraArchActivator.terraformTargetFile) {
							TerraArchActivator.terraformTargetFile = sdr;
						} else {
							if (TerraArchActivator.fileImportance(name) 
									< TerraArchActivator.fileImportance(TerraArchActivator.terraformTargetFile.getFile().getName())) {
								TerraArchActivator.terraformTargetFile = sdr;
							};
						}
					}
				}
				
				////////////////////////////////////////////////////////////////////////
				//if we have no version constraints, for the provider collect it
				//later we will review those collected to report the error and suggest
				//which file may be best for the insert
				///////////////////////////////////////////////////////////////////////
				
				module.undefinedProvidersInFile.clear();
				sdr.visitNodeDefs(def-> {
					try {
						byte[] cat = def.category(); //aws_xxx
						if (null!=cat && cat.length>0) {
							switch(def.type()) {
								case RESOURCE:
								case DATA:
									TrieParserReader.parseSetup(provReader, 
																cat, 0, cat.length, 
																Integer.MAX_VALUE);
									
									if (!module.hasProviderVersionConstraints(provReader)) {//this provider has no version defined.
										int len = 0;
										while (len<cat.length && cat[len]!='_') {
											len++;									
										}	
										//increase the count so we can find which file has this the most.
										long provCount = provReader.parseNext(module.undefinedProvidersInFile);
										if (len < cat.length) {
											
											module.undefinedProvidersInFile.setValue(
													cat, 0, len, Integer.MAX_VALUE, provCount<=0 ? 1 : provCount+1);
										}
										//if this does not match we do nothing becase it changed while we reviewed it.
										//this happens whlie the user is still typing aws_
									
									}
								break;
								default:
							};
						}
					
					  } catch (Throwable t) {
		        		  TerraArchActivator.logger.error("primary deep review",t);
		        	  }		
					
					return true;//keep going we want to count them all
				});
				
				////////////////////////
				//end of SDR processing
				///////////////////////
	
				//collect the missing providers into this map of lists.
				module.undefinedProvidersInFile.visitPatterns(new TrieParserVisitor() {
					@Override
					public void visit(byte[] data, int len, long value) {
						String key = new String(data,0,len).intern();
						//terraform_ looks like a provider but must be skipped.
						if (!"terraform".equals(key) ) {
							List<ProviderFileVersionDTO> list = TerraArchActivator.missingProviderVersions.get(key);
							if (null == list) {
								list = new ArrayList<ProviderFileVersionDTO>();
								//System.out.println("added missing "+key+" pppppppppppppppppppppppppppp");
								TerraArchActivator.missingProviderVersions.put(key,list);
							}
							list.add(new ProviderFileVersionDTO(sdr, value));		
						}						
					}
				});
				
				return true;
			});
			
			
			if (!module.hasTerraformVersionConstraints() && null!=TerraArchActivator.terraformTargetFile) {
			
				DynamicMarkerSet.newMarker(TerraArchActivator.getIFile(TerraArchActivator.terraformTargetFile),
						                    "Terraform version definition missing", 
											0, 1, 0, markerRev,
											true, IMarker.SEVERITY_ERROR, 
											new QuickFixerTerraformVersion());
					
			} 
					
			
			if (!TerraArchActivator.missingProviderVersions.isEmpty()) {
				TerraArchActivator.missingProviderVersions.forEach( (k,v) -> {
			
					Collections.sort(v);
					ProviderFileVersionDTO c = v.get(0);
					//NOTE: build a better comment using the names of all the files?
					//v.forEach(c-> {
						//System.out.println("message: "+message);
						if (null!=c.sdr) { 
								
							IFile iFile = TerraArchActivator.getIFile(c.sdr);
							String message = k+" provider version definition missing";	
							DynamicMarkerSet.newMarker(iFile, message, 
								0, 1, 0, markerRev, true, IMarker.SEVERITY_ERROR, 
								new QuickFixerProviderVersion(module, k));
						}
					//});
				});
			}
			
			
		} finally {
			module.upinLock.unlock();
		}
	}

}
