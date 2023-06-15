package net.terraarch.index;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

import net.terraarch.terraform.structure.StructureDataModule;
import net.terraarch.terraform.parse.doc.DocumentTokenMap;
import net.terraarch.terraform.structure.StructureDataFile;

import net.terraarch.DeepReview;
import net.terraarch.TerraArchActivator;
import net.terraarch.preferences.TerraPreferences;
import net.terraarch.util.AcceptLicenseUtil;
import net.terraarch.util.FileUtils;
import net.terraarch.util.ReactiveJobState;

public class IndexModuleFile implements IDocumentListener {
	
	public final class DeepThought extends ReactiveJobState {
		public DeepThought(String name, int period) {
			super(name, period);
		}
		
		private final Set<String> doneFileNames = new HashSet<String>();

		@Override
		public void run() {
			DeepReview.deepReview(module, doneFileNames);
			doneFileNames.clear();
		}

		public void setDoneEditors(Set<String> processMarksInOpenEditors) {
			doneFileNames.addAll(processMarksInOpenEditors);
		}
	}



	private static final ILog logger = Platform.getLog(IndexModuleFile.class);
		
	    private final IFile file; //this field will only be populated when this was inside the worksapce project
		public final StructureDataModule module;
		public final File rawAbsoluteFile;		
	
		public final DeepThought deepReviewJobState = new DeepThought("DeepReview", 421);
		
		public IndexModuleFile(IFile f) {
			///////////////////////////////////////////////////////////////////////////////////
			//created outside of UI so we have more time to do work and prepare the editor here
			///////////////////////////////////////////////////////////////////////////////////
			
						
			//this is important so we get this information upon touching any file of the project
			//it must be done now so its complete before we trigger on a document change
			//in this frst pass we must index everything we do know
			//System.out.println("index module for file "+f);//paint should have data...
			File file2 = f.getRawLocation().toFile();
			if (null==file2) {
				logger.error("unable to find file: "+f);
			}
			File myFolder = file2.getParentFile();
			this.file = f;
						
			TerraArchActivator.getDefault();
			this.module = TerraArchActivator.sdmm.indexModuleFolder(myFolder, TerraArchActivator.getDefault().storageCache(), true);
			if (null==this.module) {
				logger.error("unable to build module index from file: "+f);
			}
			
			//in this second pass we use the indexed data above to mark what may be missing
			this.rawAbsoluteFile = file2.getAbsoluteFile();
			if (null==this.rawAbsoluteFile) {
				logger.error("unable to find absolute file: IndexModuleFile");
				throw new NullPointerException();
			}
		
			deepReviewJobState.bumpJob();
			//TerraArchActivator.deepReview(module, null);
			
			TerraPreferences.invalidateAllTextInAllEditors(); //repaint so we can avoid any red lines
			
		}
		
		public IndexModuleFile(File folderOutsideWorkspace, File specificFile) {
			if (!folderOutsideWorkspace.isDirectory()) {
				logger.error("this constructor may only take directories, got: "+folderOutsideWorkspace);
				throw new UnsupportedOperationException("this constructor may only take directories, got: "+folderOutsideWorkspace);
			}
			
			TerraArchActivator.getDefault();
			this.module = TerraArchActivator.sdmm.indexModuleFolder(folderOutsideWorkspace, TerraArchActivator.getDefault().storageCache(), true);
			if (null==this.module) {
				logger.error("unable to build module index from folder: "+folderOutsideWorkspace);
			}
			
			//in this second pass we use the indexed data above to mark what may be missing
			this.rawAbsoluteFile = specificFile.getAbsoluteFile();
			if (null==this.rawAbsoluteFile) {
				logger.error("unable to find absolute file: IndexModuleFile");
				throw new NullPointerException();
			}
			this.file = null;
			//TerraArchActivator.deepReview(module, null);
			deepReviewJobState.bumpJob();
			
			TerraPreferences.invalidateAllTextInAllEditors(); //repaint so we can avoid any red lines
			
		}

		@Override
		public void documentChanged(DocumentEvent event) {
			try {
			    //Appendables.appendEpochTime(System.out.append("document changed: "), System.currentTimeMillis()).append("\n");
					
				//important indexing logic for text highlighting of failures
				//always update and modify last update to in-memory
				//////////////////
				//just parse this single file/document for new index data
				StructureDataFile sdr = module.indexInMemoryData(event.getDocument().get().getBytes(), rawAbsoluteFile,
						                   TerraArchActivator.getDefault().storageCache());
				
				//now that we have indexed this document and its changes we much check the open ediors for related issues.
				deepReviewJobState.setDoneEditors(processOpenEditors(e-> {
					processOpenEditor(e);
				}));
				//review all the files on disk, and all open editors including this one
				//this is a slow background task
					
				deepReviewJobState.bumpJob();
				
			} catch (Throwable t) {
				t.printStackTrace();
				logger.error("documentChanged",t);
			}
		}

			
		
		private Set<String> processOpenEditors(Consumer<ITextEditor> consumer) {
			
			final Set<String> shortFileNamesDone = new HashSet<String>();				
			try {
				//invalidate all the paint on the open editors
				final File targetFolder = rawAbsoluteFile.getParentFile();
				
				FileUtils.visitEditors(p-> {
					//all editors even outside project
					if ((p instanceof ITextEditor) && (p.getEditorInput() instanceof IFileEditorInput)) {
							IFileEditorInput input = (IFileEditorInput)p.getEditorInput();																						
							final IFile inputFile = input.getFile();
							final File thisEditorsParentFolder = inputFile.getRawLocation().toFile().getAbsoluteFile().getParentFile();
							// Appendables.appendEpochTime(System.out.append(" AA: "), System.currentTimeMillis()).append("\n");
											
							//only process those in the same module.
							if (targetFolder.equals(thisEditorsParentFolder )) {
								assert(targetFolder.equals(module.moduleFolder)) : "ony process the module containing the changed file.";
							
								//Appendables.appendEpochTime(System.out.append(" BB: "), System.currentTimeMillis()).append("\n");
									
								processOpenEditor((ITextEditor) p);
							}
							
							//this includes those not in this module since they did not change and we do not want to relaod them
							//  eg.    this is relative to the workspace.   /apis/main.tf
							//    we must use this longer form because we must avoid collide of 2 equal file names
							shortFileNamesDone.add(input.getFile().getFullPath().toString());//store on list so we do not use disk version.
					} 
				}); //these are open editors in the background
			} catch (Throwable t) {
				logger.error("processMarksInOpenEditors",t);
			}
			return shortFileNamesDone;
		}


		public void processOpenEditor(ITextEditor iTextEditor) {
				
			try {	
				//RECORD MARKS IN THIS FILE AND OPEN EDITORS FOR THIS PROJECT, MUST BE DONE HERE
				//cant record marks from disk editions since the other tabs have modified data
				if (null!=module) {
					
					IFileEditorInput iFileEditorInput = (IFileEditorInput)iTextEditor.getEditorInput();
					IFile iFile = iFileEditorInput.getFile();
					
					StructureDataFile sdr = module.getSDR(iFile.getLocation().toFile());
					
					try {
						IDocument document = iTextEditor.getDocumentProvider().getDocument(iFileEditorInput);
						
						if (null != document) { //only process if the document is found.
							//clear before we begin parse of this modified document
							sdr.clearTerraformVersionConstraints();				
							sdr.setDefinedProviders(null);
													
							StructureDataModule.scanForLoop(module, sdr);
							final int markerRev = TerraArchActivator.markerRevsion.incrementAndGet();
						
							DocumentTokenMap resultsMap = DynamicMarkerSet.recordNewMarks(markerRev, iFile, document.get().getBytes(), module, sdr.loopedNodes());
						
							sdr.setTerraformConstraints(resultsMap.terraformVersionConstraints());
							sdr.setDefinedProviders(resultsMap.definedProviders());	
						}
					} catch (Throwable t) {
						logger.error("open editor: "+iTextEditor.getTitle(),t);
					}
					
				}
				//never invalidate your own working editor, that is already covered
				//this is null when this was  "outside" the project workspace
				if (null != this.file) {
					if (!((IFileEditorInput)iTextEditor.getEditorInput()).getFile().equals(file)) {
							forceRepaint(iTextEditor); //VERY FAST AND CRITICAL SO THE OTHER EDITORS DUMP STALE INFO
					} //for a paint but its not on screen so it can happen later
				}
			} catch (Throwable t) {
				logger.error("processOpenEditor",t);
			}
		}

		private void forceRepaint(IEditorPart p) {
			ITextOperationTarget target = (ITextOperationTarget)p.getAdapter(ITextOperationTarget.class);
			if (target instanceof ITextViewer) {
				 ITextViewer viewer = (ITextViewer) target;
				 viewer.invalidateTextPresentation();
			}
		}

		@Override
		public void documentAboutToBeChanged(DocumentEvent event) {
		}

		public long lastCallUpdate() {			
			return module.lastCallUpdate();
		}



		public static IndexModuleFile extractModuleFileService(IDocument doc) {
			//reflect into doc, to find the module assoicated directly 
			if (doc instanceof AbstractDocument) {
				try {
					Field[] fields = AbstractDocument.class.getDeclaredFields();
					int x = -1;
					while (++x < fields.length ) {
						final Field field = fields[x];
						if (field.getType() == ListenerList.class) {								
							field.setAccessible(true);
							for(Object item: (ListenerList<?>)field.get(doc)) {
								if (item instanceof IndexModuleFile) {
									return ((IndexModuleFile)item);
								}
							}
						}
					}
				} catch (SecurityException e) {
					logger.error("unable to lookup module ",e);
				} catch (IllegalArgumentException e) {
					logger.error("unable to lookup module ",e);
				} catch (IllegalAccessException e) {
					logger.error("unable to lookup module ",e);
				}
			}
			//////////////////////////////////////////////////////////////////////////////////////
			//this editor was launched "possibly on startup" but it never had a listener attached
			//this happens when a file is opened which is NOT inside the workspace, in that case
			//the project can not be resolved so everything is assumed OK.
			//////////////////////////////////////////////////////////////////////////////////////
			
		    return null;
		}
		
		
	}