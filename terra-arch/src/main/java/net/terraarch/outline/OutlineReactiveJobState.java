package net.terraarch.outline;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Display;

import net.terraarch.terraform.structure.IndexNode;
import net.terraarch.terraform.structure.IndexNodeDefinition;
import net.terraarch.terraform.structure.StructureDataModule;
import net.terraarch.terraform.structure.StructureDataFile;

import net.terraarch.index.IndexModuleFile;
import net.terraarch.util.ReactiveJobState;

public class OutlineReactiveJobState extends ReactiveJobState {

	
	private final TerraArchOutlineView outlineView;
	public IFile iFile = null;
	public int caretOffset =  0;
	
	private File activeFile;
	private StructureDataFile activeSDR;
	private IndexNode activeDef;
	
	public OutlineReactiveJobState(TerraArchOutlineView outlineView) {
		super("ReactiveCursor", 331 );
		this.outlineView = outlineView;
	}

	@Override
	public void run() {
		
		//System.out.println("update the outline to the cursor position... "+caretOffset);
		
		//logger.info("position of caret is: " + caretOffset + "  "+iFile);
		StructureDataModule module = outlineView.module();
		if (null!=module) {
			
			//lookup only if we changed file
			File temp = iFile.getLocation().toFile();
			if (null==activeFile || !temp.equals(activeFile)) {
				activeFile = temp;
				activeSDR = module.getSDR(activeFile);
			}
			try {
				
				//this search by offset has no knowledge of nested parents
				//if this def is not at the top we much choose which context it will
				//be found within.
				IndexNode newDef = activeSDR.findNodeAtOffset(caretOffset);
				if (null!=newDef && newDef!=activeDef ) {
					
					//System.out.println("----------- def found at cursor pos: "+caretOffset+" was "+newDef);
					OutlineReactiveJobState.positionOutline(outlineView.labelProvider, outlineView.imf, outlineView.treeViewer, newDef);
					activeDef = newDef;
				} 
				
			} catch (Throwable t) {
				t.printStackTrace();
			}
			
		}
		
	}

	static void decorateActiveUsage(OutlineLabelProvider labelProvider, IndexModuleFile imf, IndexNode item) {
		if (labelProvider!=null) {
			final long parentRev =  labelProvider.nextParentRev(item);
			
			StructureDataModule.visitDefUsages(imf.module,
					item.type(), item.category(), item.name(), 
						u-> {						
							u.getParentDef().setParentRevId(parentRev);
							return true;
						} 
			);
		}		
	}

	public static void positionOutline(OutlineLabelProvider labelProvider, IndexModuleFile imf, TreeViewer treeViewer, IndexNode activeNode) {
		
		try {		
			
		if (activeNode instanceof IndexNodeDefinition) {
			IndexNodeDefinition ind = (IndexNodeDefinition)activeNode;
			final OutlineNode toSelect = new OutlineNode(null, ind);
	
			OutlineReactiveJobState.decorateActiveUsage(labelProvider, imf, ind);
						
			final TreeViewer tv = treeViewer;
			if (null != tv) {
				final StructuredSelection structuredSelection = new StructuredSelection(toSelect);
			    			
			    Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						try {
							tv.setSelection(structuredSelection, true);	
							tv.refresh(toSelect, true);
						} catch (SWTException swt) {
							//ignore since the widget was not available
					    } catch (Throwable t) {
							TerraArchOutlineView.logger.error("set selection",t);
						}
					}
				});
			}
		
		
		} else {
			
			IndexNodeDefinition ind = activeNode.getParentDef();
			OutlineNode def = new OutlineNode(null, ind );
			final OutlineNode toSelect = new OutlineNode(def, activeNode);
			final TreeViewer tv = treeViewer;
			final StructuredSelection structuredSelection = new StructuredSelection(toSelect);
			
			OutlineReactiveJobState.decorateActiveUsage(labelProvider, imf, ind);
			
		    Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					try {
						tv.setExpandedElements(def,toSelect); 
						tv.setSelection(structuredSelection, true);
						tv.refresh(true);
					} catch (SWTException swt) {
						//ignore since the widget was not available
					} catch (Throwable t) {
						TerraArchOutlineView.logger.error("set selection",t);
					}
				}
				});
			}
		
		} catch (Throwable t) {
			TerraArchOutlineView.logger.error("positionOutline",t);
		}
	}

}
