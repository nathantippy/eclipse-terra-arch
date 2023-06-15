package net.terraarch.outline;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import net.terraarch.terraform.parse.BlockType;
import net.terraarch.terraform.parse.doc.ThemeColors;
import net.terraarch.terraform.structure.IndexNode;
import net.terraarch.terraform.structure.IndexNodeDefinition;
import net.terraarch.terraform.structure.IndexNodeUsage;
import net.terraarch.terraform.structure.StructureDataModule;

import net.terraarch.TerraArchActivator;

public class OutlineLabelProvider extends LabelProvider implements IColorProvider {
	
	private static final Color FOUND_IN_COLOR   = new Color(191, 42, 106);
	private static final Color DEFINITION_COLOR = new Color(102, 102, 102);
	

	private static final ILog logger = Platform.getLog(OutlineLabelProvider.class);
	
	private final ImageRegistry ir;
	private final StructureDataModule sdm;
	
	
	public OutlineLabelProvider(ImageRegistry ir, StructureDataModule module) {
		this.ir = ir;
		this.sdm = module;
	}
	
	
	@Override
	public Image getImage(Object element) {
		try {
			Image result = Display.getDefault().getSystemImage(0);
			if (element instanceof OutlineNode) {
				OutlineNode node = (OutlineNode)element;
				
				if (null!=node.imageId()) {

					if (node.type.isCatigorized && 
						node.type!=BlockType.PROVIDER) { //providers may not always have categories
						if (null==node.category || node.category.length==0) {
							return result; //this catigorized node has no catigory
						}
					}
					
					boolean hasUsage = StructureDataModule.hasDefUsages(sdm,
												node.type, node.category, node.name);

					
					String imageId = node.imageId();
					if (!hasUsage) {
						imageId = imageId+"-TOP";
					}
					
				
					Image temp = ir.get(imageId);
					if (null==temp) {
						
						Image base = ir.get(node.imageId());
						if (null==base) {
							return result; //can not load images							
						}
						
						Rectangle bounds = base.getBounds();
						
						Display disp = Display.getDefault();
						Image img = new Image(disp, base.getImageData());
						GC gc = new GC(img);
			
						    
					    if (!hasUsage) {
						    //the TOP corner must be drawn last
						    gc.setForeground(new Color(208, 61,255)); 
						    gc.setBackground(new Color(208, 61,255)); 
						    gc.fillRectangle(0, 0, 4, bounds.height/2);
						  						   
						}

					    gc.dispose();
						temp = img;
						ir.put(imageId, temp);
					}
					result = temp;
				}
			}
			return result;
			
		} catch (Throwable t) {
			logger.error("getImage",t);
			return null;
		}
	}

	@Override
	public String getText(Object element) {
		try {
			if (element instanceof OutlineNode) {
				return ((OutlineNode)element).text(sdm);
			}
			return "unknown";
		} catch (Throwable t) {
			logger.error("getText",t);
			return "";
		}
	}

	@Override
	public Color getBackground(Object element) {
		
		boolean isDefinition = false;
		OutlineNode node = (OutlineNode)element;
		if (node.node instanceof IndexNodeDefinition) { //only at the top
			isDefinition = isDefinitionMatch((IndexNodeDefinition)node.node);
		}
		
		if (isDefinition) {			
			return DEFINITION_COLOR;
		}
		
		return null;//Display.getDefault().getSystemColor(SWT.COLOR_DARK_GREEN);
	}

	

	@Override
	public Color getForeground(Object element) {
		boolean isParent = false;
		OutlineNode node = (OutlineNode)element;
		if (node.node instanceof IndexNodeDefinition) { //only at the top
			isParent = isParentMatch((IndexNodeDefinition)node.node);
		} 
		if (isParent) {
			
			int colorId = TerraArchActivator.getDefault().getTextColorsId();
			ThemeColors colorEnum = ThemeColors.values()[colorId];
			
			switch (colorEnum) {
				case vivid:
					return FOUND_IN_COLOR;
				
				case muted:
					//TODO: B a more dem color for muted?
					return FOUND_IN_COLOR;
			
			}
		}
			
		
		return null;//Display.getDefault().getSystemColor(SWT.COLOR_WHITE);
	}

	//////////////////////////
	
	private final AtomicLong parentRev = new AtomicLong();
	private IndexNode activeItem;
	public long nextParentRev(IndexNode activeItem) {
		this.activeItem = activeItem;
		return parentRev.incrementAndGet();
	}
	private boolean isParentMatch(IndexNodeDefinition def) {
		return parentRev.get() == def.getParentRevId();
	}
	private boolean isDefinitionMatch(IndexNodeDefinition node) {
		IndexNode activeItemLocal = activeItem;
		if (null!=activeItemLocal && (activeItemLocal instanceof IndexNodeUsage) && activeItemLocal.type()==node.type()) {
			if (activeItemLocal.type().isCatigorized) {
				if (!Arrays.equals(activeItemLocal.category(), node.category())) {
					return false;
				}
			}			
			if (Arrays.equals(activeItemLocal.name(), node.name())) {
				return true;
			}
		}
		
		return false;
	}

	/* 
	private EnumMap<BLOCK_TYPES, IndexNode[]> parents = new EnumMap<BLOCK_TYPES, IndexNode[]>(BLOCK_TYPES.class);
	
	private boolean isParentMatch(IndexNodeDefinition def) {
		
		IndexNode[] result = parents.get(def.type());
		if (null!=result) {
			int x = result.length;
			
			if (def.type().isCatigorized) {
				
				while (--x>=0) {
					if (def.isEqual(result[x].category(), result[x].name()) ) {
						return true;
					};				
				}
				
			} else {
			
				while (--x>=0) {
					if (def.isEqual(result[x].name())) {
						return true;
					};				
				}
				
			}
		}
		
		return false;
	}



	public void clearParents() {
		parents.clear();
	}



	public void addParentDef(IndexNodeDefinition parentDef) {
		
		IndexNode[] results = parents.get(parentDef.type());
		
		if (null==results) {
			results = new IndexNode[] {parentDef};
			
		} else {
			IndexNode[] temp = new IndexNode[results.length+1];
			System.arraycopy(results, 0, temp, 0, results.length);
			temp[temp.length-1] = parentDef;
			results = temp;
		}
		parents.put(parentDef.type(), results);
		
	}
	*/
	
}
