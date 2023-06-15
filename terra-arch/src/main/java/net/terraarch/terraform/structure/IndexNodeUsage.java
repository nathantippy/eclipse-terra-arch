package net.terraarch.terraform.structure;

import java.util.Arrays;

import net.terraarch.terraform.parse.BlockType;
import net.terraarch.util.AppendableBuilderReader;

public class IndexNodeUsage extends IndexNode implements Comparable<IndexNodeUsage>{

	private IndexNodeDefinition usageUsedInsideDefinition;
	
	//this usage is also an instance of a def
	private byte[] usageInstanceDefCat;   // definition namespace
	private byte[] usageInstanceDefName;  // definition specfic name
	private int defCategoryUsageEndPos;
	private int defNameUsageEndPos;
	private BlockType type;
	
	private int attrEndPos;
	private byte[] attrBytes;
	
	
	public IndexNodeUsage() {
	}
		
	public IndexNodeUsage(long rev, StructureDataFile sdr, BlockType type, IndexNodeDefinition parent,
			int defNameUsageEndPos, //this usage is inside the def of this parent 
            AppendableBuilderReader usageDefName) { //def of this usage instance

		reset(rev, sdr, type, parent, defNameUsageEndPos, usageDefName);
	
	}
	
	public IndexNodeUsage(long rev, StructureDataFile sdr, BlockType type, IndexNodeDefinition parent, int defTypeUsageEndPos, 
			                AppendableBuilderReader definitionType, int defNameUsageEndPos, 
			                AppendableBuilderReader definitionName) {
		
		reset(rev, sdr, type, parent, defTypeUsageEndPos, definitionType, defNameUsageEndPos, definitionName);
				
	}
	
	public int attributePosition() {
		return attrEndPos;
	}
	
	public byte[] attribute() {
		return attrBytes;
	}

    
	public void setAttribute(int endPos, AppendableBuilderReader value) {
		attrBytes=value.toBytes();
		attrEndPos = endPos;
	}
	
	public int blockPositionStart() {
		
		return ((null!=type && type.isCatigorized) 
				 ? defCategoryUsageEndPos - (null!=usageInstanceDefCat ? usageInstanceDefCat.length: 0) 
				 : defNameUsageEndPos - (null!=usageInstanceDefName ? usageInstanceDefName.length: 0));
			
	};
	
	public int blockPositionEnd() {		
		return (defNameUsageEndPos);
	
	
	};
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = null==getParentDef() ? 1 :  getParentDef().hashCode();
		result = prime * result + ((type() == null) ? 0 : type().hashCode());
		result = prime * result + Arrays.hashCode(name());
		if (type().isCatigorized) {
			result = prime * result + Arrays.hashCode(category());
		}
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IndexNode other = (IndexNode) obj;
		if (type() != other.type())
			return false;
		if (!Arrays.equals(name(), other.name()))
			return false;

		if (type().isCatigorized) {
			if (!Arrays.equals(category(), other.category()))
				return false;
		}
		
		if (null!=getParentDef()) {
			if (null==other.getParentDef() || !getParentDef().equals(other.getParentDef())) {
				return false;
			}
		} else {
			//must be null because this one is null.
			if (other.getParentDef()!=null) {
				return false;
			}
		}
		return true;
	}

    @Override
	public IndexNodeDefinition getParentDef() {
	  return usageUsedInsideDefinition; 
	}
	
	public boolean isMatch(byte[] category, byte[] name) {
		return null!=usageInstanceDefCat && Arrays.equals(name, usageInstanceDefName) && Arrays.equals(category, usageInstanceDefCat);
	}

	public boolean isMatch(byte[] name) {
		return null==usageInstanceDefCat && Arrays.equals(name, usageInstanceDefName);
	}
	
	//be sure this is never used unless reset gets called.
    public void invalidate() {
    	this.usageInstanceDefName = null;
    	this.type = null;    	
    }

	public void reset(long rev, StructureDataFile sdr, BlockType type, IndexNodeDefinition parentDef,
			int defTypeUsageEndPos, AppendableBuilderReader definitionType, 
			int defNameUsageEndPos,
			AppendableBuilderReader definitionName) {
		
		setRevision(rev);
		if (null!=this.usageInstanceDefName && definitionName.isEqual(this.usageInstanceDefName)) {
		} else {
			this.usageInstanceDefName = definitionName.toBytes();
		}

		if (null!=this.usageInstanceDefCat && definitionType.isEqual(this.usageInstanceDefCat)) {
		} else {
			this.usageInstanceDefCat = definitionType.toBytes();
		}
		
		this.type = type;
		this.usageUsedInsideDefinition = parentDef;
		this.defCategoryUsageEndPos = defTypeUsageEndPos;
		this.defNameUsageEndPos = defNameUsageEndPos;
		
		this.sdr = sdr;
		this.editorOffsetPosition = defNameUsageEndPos;
		
	}

	public void reset(long rev, StructureDataFile sdr, BlockType type, IndexNodeDefinition parentDef,
			int defNameUsageEndPos, AppendableBuilderReader usageDefName) {
		
		setRevision(rev);
		if (null!=this.usageInstanceDefName && usageDefName.isEqual(this.usageInstanceDefName)) {
		} else {
			this.usageInstanceDefName = usageDefName.toBytes();
		}
		
		this.type = type;
		this.usageUsedInsideDefinition = parentDef;
		this.usageInstanceDefCat = null;
		this.defCategoryUsageEndPos = -1;
		this.defNameUsageEndPos = defNameUsageEndPos;
		
		this.sdr = sdr;
		this.editorOffsetPosition = defNameUsageEndPos;
	}

	public BlockType type() {
		return type;
	}

	public byte[] category() {
		return usageInstanceDefCat;
	}

	public byte[] name() {
		return usageInstanceDefName;
	}


	@Override
	public int compareTo(IndexNodeUsage that) {
		return Integer.compare(this.defNameUsageEndPos, that.defNameUsageEndPos);
	}




}
