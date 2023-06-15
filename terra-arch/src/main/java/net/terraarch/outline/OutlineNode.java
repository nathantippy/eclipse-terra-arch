package net.terraarch.outline;

import java.util.Arrays;

import org.eclipse.core.resources.IFile;

import net.terraarch.util.Appendables;
import net.terraarch.terraform.parse.BlockType;
import net.terraarch.terraform.structure.IndexNode;
import net.terraarch.terraform.structure.IndexNodeDefinition;
import net.terraarch.terraform.structure.IndexNodeUsage;
import net.terraarch.terraform.structure.StructureDataModule;

import net.terraarch.TerraArchActivator;

public class OutlineNode implements Comparable<OutlineNode> {

	public final OutlineNode parent;
	public final IndexNode node; //we need this to match or outline lookup.
	
	public final BlockType type;
	public final byte[] category;
	public final byte[] name;
	public final boolean isLooped;
	public final int loopLength;
	
	public final IFile optionalIFile;
	public final int editorOffsetPosition;
	
	private final int itemLen;
		
	public OutlineNode(OutlineNode parent, IndexNode node) {
		super();
		
		//This is null when used as top of the menu
		this.parent = parent;  //part of equals
		
		
		boolean foundLoop = false;
		OutlineNode temp = parent;
		int depth = 0;
		while (null!=temp && !foundLoop) {
			depth++;
			if (temp.type == node.type()) {
				if (Arrays.equals(temp.name, node.name())) {
					if (temp.type.isCatigorized) {
						foundLoop = Arrays.equals(temp.category, node.category());
					} else {
						foundLoop = true;
					}
				}
			}
			temp = temp.parent;
		}
		this.isLooped = foundLoop;
		this.loopLength = depth;		
		
		this.node = node;
		
		this.type = node.type();//part of equals
		this.name = node.name();//part of equals
		this.category = node.category(); //part of equals
		
		this.editorOffsetPosition = node.editorOffsetPosition; //part of equals for same child inside def

		this.optionalIFile = TerraArchActivator.getIFile(node.sdr()); 
						
		this.itemLen = (node instanceof IndexNodeDefinition ? 0 :
					      ( type.categoryLength + (type.isCatigorized ? category.length+1 : 0))
				        ) + name.length;
		
	}

	public int itemLength() {
		return itemLen;
	}

	public int itemStart() {
		return editorOffsetPosition-itemLen;
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		if (type!=null && type.isCatigorized) {
			result = prime * result + Arrays.hashCode(category);
		}
		result = prime * result + editorOffsetPosition;
		result = prime * result + Arrays.hashCode(name);
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof OutlineNode)) {
			return false;
		}
		OutlineNode other = (OutlineNode) obj;
		if (type!=null && type.isCatigorized) {
			if (!Arrays.equals(category, other.category))
				return false;
		}
		if (type != other.type)
			return false;
		if (editorOffsetPosition != other.editorOffsetPosition)
			return false;
		if (!Arrays.equals(name, other.name))
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		return true;
	}

	@Override
	public int compareTo(OutlineNode that) {
		
		if (null == this.node.getParentDef()) {
			return this.node.toString().compareTo(that.node.toString());
		} else {
			return Integer.compare(this.node.editorOffsetPosition, that.node.editorOffsetPosition);
		}
	}
	


	public String text(StructureDataModule sdm) {
		
		StringBuilder text = new StringBuilder();
		
		if (null == parent) {
			//add this to problems?
			//top level, eg Unused locals and variables are 
			//suspect and perhapse should be removed by the author.
			if (BlockType.LOCALS  == type || 
				BlockType.VARIABLE == type) {
				
				if (!StructureDataModule.hasDefUsages(sdm, type, category, name)) {
					text.append("UNUSED ");
				}
			}		
		} else {
			if (isLooped) {
				text.append("LOOPS to ");				
			}
		}
		////////////////////////////////////////////
		////////////////////////////////////////////
		text.append(node.toString());
		////////////////////////////////////////////
		////////////////////////////////////////////
		if (node instanceof IndexNodeUsage) {
			//slow so we hope the caller of text is cacheing, if not we should cache here..
			String suffix = ContentProviderTopDown.buildIndexOfCount(sdm, (IndexNodeUsage) node).toString();
			text.append(" ").append(suffix);
		}
		return text.toString();
	}
	
	public String imageId() {
		
		if (isLooped) {//this method will double check and clear.
			//use ERROR graphic
			return "err-loop";
		} else {		
			BlockType type = node.type();
			if (type==BlockType.DATA ||
				type==BlockType.RESOURCE ||
				type==BlockType.MODULE ||
				type==BlockType.LOCALS ||
				type==BlockType.OUTPUT ||
				type==BlockType.PROVIDER ||
				type==BlockType.VARIABLE) {
				return type.name();
			} else {
				return null;
			}
		}	
	
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		int c = 0; //hack test
		OutlineNode temp = this;
		while (null!=temp) {
			c++;
			temp = temp.parent;
		}
		builder.append(" "+c+"  ");
		
		
		builder.append(type.label());
		if (null!=category && type.isCatigorized) {
			Appendables.appendUTF8(builder, category, 0, category.length, Integer.MAX_VALUE);
			builder.append(" . "); //wider space to help readability
		}
		Appendables.appendUTF8(builder, name, 0, name.length, Integer.MAX_VALUE);
		
		return builder.toString();
	}
	
	public String toPathString() {
		
		if (parent == null) {
			return toString();
		} else {
			return parent.toPathString()+" ||| "+toString();
		}
		
	}


	
}
