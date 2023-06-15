package net.terraarch.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.genericeditor.GenericEditorPlugin;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import net.terraarch.util.AppendableBuilderReader;

import net.terraarch.terraform.parse.ParseBuffer;
import net.terraarch.terraform.structure.GatherProposals;
import net.terraarch.terraform.structure.GatheredFieldType;
import net.terraarch.terraform.structure.IndexNodeDefinition;
import net.terraarch.terraform.structure.IndexNodeUsage;
import net.terraarch.terraform.structure.RecordVisitor;
import net.terraarch.terraform.structure.StructureDataFile;
import net.terraarch.terraform.structure.walker.GatherInBoundProposals;
import net.terraarch.terraform.structure.walker.GatherSelection;

import net.terraarch.TerraArchActivator;
import net.terraarch.index.IndexModuleFile;
import net.terraarch.outline.OutlineNode;
import net.terraarch.outline.TerraArchOutlineView;
import net.terraarch.util.FileUtils;

public class JumpToDef extends AbstractHandler {
	
	public static final ILog logger = Platform.getLog(JumpToDef.class);
	
	public final class GatherSelectedItem extends GatherSelection {
		@Override
		public boolean foundInDocument(GatherProposals<?> that, int endPos,
				AppendableBuilderReader value, int preBytes, GatheredFieldType type) {
			super.foundInDocument(that, endPos, value, preBytes, type);
			return true;
		}

		@Override
		public boolean foundInDocument(GatherProposals<?> that, int typeEndPos,
				AppendableBuilderReader typeValue, int endPos, AppendableBuilderReader value,
				int preBytes, GatheredFieldType type) {
			super.foundInDocument(that, typeEndPos, typeValue, endPos, value, preBytes, type);
			return true;
		}
	}

	private final static ParseBuffer parseBuffer = new ParseBuffer();
	
	public JumpToDef() {
		this.setBaseEnabled(true);
	}
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		
		  ISelection selection = HandlerUtil.getCurrentSelection(event);		
		  if (null!=selection && !selection.isEmpty() && (selection instanceof ITextSelection)) {
		
		
		    	IDocumentProvider provider = ((ITextEditor)HandlerUtil.getActiveEditor(event)).getDocumentProvider();
		    	IDocument document = provider.getDocument(HandlerUtil.getActiveEditorInput(event));
		    	IndexModuleFile extractModuleFileService = IndexModuleFile.extractModuleFileService(document);
    			
		    	if (null!=extractModuleFileService) {
									final GatherSelection visitor = new GatherSelectedItem();
					
		    		final boolean colectDefintions = true; //we only go to def if this is a usage
		    		int cursorPosition = ((ITextSelection)selection).getOffset();
					byte[] bodyBytes = document.get().getBytes();
										
					//      allow for sloppy selection, if the cursor is too far left move it right
					if (isFirstPart(bodyBytes, cursorPosition)) {
						cursorPosition  = moveToSecondPart(bodyBytes, cursorPosition);
					}	
					
					
					GatherInBoundProposals<GatherSelection> gp = new GatherInBoundProposals<GatherSelection>(
		    				                                            extractModuleFileService.module,
																		cursorPosition, 
																		visitor, 
																		colectDefintions, TerraArchActivator.getDefault().sdmm);
		    		
		    		//scanning the document for exactly what element we are looking at now.
					if (parseBuffer.tokenizeDocument(bodyBytes, gp)) {
												
						final GatheredFieldType type = visitor.getType();
						//StructureDataFile sdr = visitor.getSDR();
						if (null!=type) {
						
							RecordVisitor<StructureDataFile> selectPos = new RecordVisitor<StructureDataFile>() {
								@Override
								public boolean accept(StructureDataFile sdr) {
									IndexNodeDefinition def;
									if (null==type.blockType || !type.blockType.isCatigorized) {
										def = sdr.lookupDef(type.blockType, visitor.getNameValue().getBytes());
									} else {										
										def = sdr.lookupDef(type.blockType, visitor.getTypeValue().getBytes(), visitor.getNameValue().getBytes());
									}
									if (def!=null) {
										//System.out.println("attempt jump");
										IFile iFile = TerraArchActivator.getIFile(sdr);
										try {
											JumpToDef.selectPositionInEditor(null,
													HandlerUtil.getActiveWorkbenchWindowChecked(event).getActivePage(), 
													iFile, 
													def.blockPositionEnd()-def.blockPositionStart(), 
													def.blockPositionStart(),
													true);
										} catch (ExecutionException e) {
											//logger.error("",e);
											e.printStackTrace();
										}
										return false; //stop looking since we found it.
									} else {
										//System.out.println("no def found ");
									}
									return true; //keep looking in the next file for the
								}
							};
							
							try {
								
								extractModuleFileService.module.visitRecords(selectPos);
								
							} catch (Exception e) {
								e.printStackTrace();
							}
							
							
						} else {
							//System.out.println("unable to find the type");
						}
					} else {
						//System.out.println("error unable to scan file");
					}
		    	} else {
		    		//System.out.println("unable to find index module.");
		    	}
		    
		  }
		return null;
	}


   //if there is no dot to left or if its data. then we are on the first part.
	private boolean isFirstPart(byte[] bodyBytes, int cursorPosition) {
		
		if (cursorPosition >= bodyBytes.length) { //this happens at times
			return false;//cusor is out of bounds so this is not the first part.
		}
		
		
		int p = cursorPosition;
		while (p>=0 && isValidIdentityChar(bodyBytes[p]) ) {
			p--;
		}
		if (p>=0 && '.'==bodyBytes[p]) {
			//check for data
			if (p>=4) {
				if (bodyBytes[p-4]=='d' && bodyBytes[p-3]=='a' && bodyBytes[p-2]=='t' &&bodyBytes[p-1]=='a') {
					if (p==4) {					
						return true;
					} else {
						if (isValidIdentityChar(bodyBytes[p-5])) { //if idenity just ends in data
							return false;
						} else {
							return true;
						}						
					}
					
				} else {
					return false; //not first because we have dot but no data
				}				
			} else {
				return false; //not first because we have no room.				
			}
		} else {
			//not data and no dot
			return true;			
		}
		
	}

	private boolean isValidIdentityChar(int c) {
		return (c>='a' && c<='z') || (c>='A' && c<='Z')  || (c>='0' && c<='9') || (c=='_');
	}

	private int moveToSecondPart(byte[] bodyBytes, int cursorPosition) {
		
		int j = cursorPosition;
		while (j<bodyBytes.length && isValidIdentityChar(bodyBytes[j])) {
			j++;
		}
		
		if (j<bodyBytes.length-1 && '.'==bodyBytes[j]) {
			return j+1;
		}
		return cursorPosition;
	}

	public static boolean visitFileEditor(IWorkbenchPage page, IFile file, Consumer<ITextEditor> consumer)
			{
		if (null==file) {
			throw new NullPointerException();
		}
		List<ITextEditor> eds = new ArrayList<>();
		
		//NOTE: this only finds one when its been opened and difs from disk.
		FileUtils.visitEditors(p-> {
			if ((p instanceof ITextEditor) && (p.getEditorInput() instanceof IFileEditorInput)) {
				IFileEditorInput input = (IFileEditorInput)p.getEditorInput();																						
				final IFile inputFile = input.getFile();
				
				if (inputFile.equals(file)) {
					((Consumer<ITextEditor>) (editor) -> {
								eds.add(editor);						
							}).accept((ITextEditor)p);
				}
			}
		});					
							
		if (!eds.isEmpty()) {
			consumer.accept(eds.get(0));
			return true; //using existing editor
		} else {
			//NOTE: we may get an ErrorEditorPart which is not an instance of ITextError					
			IEditorPart openEditor;
			try {
				openEditor = IDE.openEditor(page, file, false);
				if (openEditor instanceof ITextEditor) {
					consumer.accept((ITextEditor) openEditor);
				}
				return false;
			} catch (PartInitException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static boolean selectPositionInEditor(OutlineNode optionalContextNode, IWorkbenchPage page, IFile optionalIFile,
												 final int itemLen, final int startOfItem, final boolean enable) {
		
		try {
			if (null!=optionalIFile && enable) {
				
					Consumer<ITextEditor> consumer = new Consumer<ITextEditor>() {
						@Override
						public void accept(ITextEditor t) {
							if (null!=t) {
							//				//TODO: A  highlight occurence disabled here.
							//				//PreferenceConstants.EDITOR_MARK_OCCURRENCES
										InstanceScope.INSTANCE
											.getNode(GenericEditorPlugin.BUNDLE_ID) //TerraArchActivator.PLUGIN_ID)
											.putBoolean("org.eclipse.ui.genericeditor.togglehighlight", false);
										
										
										//https://help.eclipse.org/2020-06/index.jsp?topic=%2Forg.eclipse.platform.doc.isv%2Freference%2Fextension-points%2Forg_eclipse_ui_genericeditor_highlightReconcilers.html
										if (null!=optionalContextNode) {
											revealContext(optionalContextNode, itemLen, startOfItem, t);
										}
										t.selectAndReveal(startOfItem, itemLen);
										
										page.activate(t);
							}
						}
					};
				
					return JumpToDef.visitFileEditor(page, optionalIFile, consumer);
			}
		} catch (Throwable e) {
			logger.error("select position",e);
		}  //    */
		return true;
	}
	
	public static void revealContext(OutlineNode node, final int itemLen, final int startOfItem, ITextEditor editor) {
		if (node.parent!=null) {
			//be sure the context is showing if possible
			if (node.node instanceof IndexNodeUsage) {
				IndexNodeDefinition ind = node.node.getParentDef();
				//sanity check which should always be true
				if (node.parent.optionalIFile == TerraArchActivator.getIFile(ind.sdr())) {
					//this scrolls to the top and to the end of the usage
					//the second select might not have to move the doc as a result
					editor.selectAndReveal(ind.blockPositionStart(),
							               itemLen+startOfItem-ind.blockPositionStart());
				}
			}
		}
	}
}
