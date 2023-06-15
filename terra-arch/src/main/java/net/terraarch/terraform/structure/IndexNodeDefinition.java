package net.terraarch.terraform.structure;

import java.util.Arrays;
import java.util.function.Consumer;

import net.terraarch.terraform.parse.BlockType;
import net.terraarch.util.AppendableBuilderReader;


public class IndexNodeDefinition extends IndexNode implements Comparable<IndexNodeDefinition> {

	private BlockType block_type;
	private int categoryEndPos;
	private byte[] category;
	private int nameEndPos;
	private byte[] name;
	private long parentRevId = -1;
	
	private int endPositionOfBlockName;
	private int endOfBlock;
	
	private String contentKey;
	private byte[] contentData;
	
	public int categoryEndPos() {
		return categoryEndPos;
	}
	public int nameEndPoint() {
		return nameEndPos;
	}
	
	public void setParentRevId(long value) {
		this.parentRevId = value;
	}
	public long getParentRevId() {
		return this.parentRevId;
	}
	
	// "module-source" is the only key today and it contains the source string value of the module.
	public void setContentField(String key, byte[] data) {
		this.contentKey = key;
		this.contentData = data;
		
	}
	
	public String getContentKey() {
		return contentKey;
	}
	
	public byte[] getContentData() {
		return contentData;
	}
	
	public int blockPositionStart() {
		return endPositionOfBlockName - block_type.value().length();		
	}
	
	public int blockPositionStartEnd() {
		return editorOffsetPosition+1;
	}
	
	public int blockPositionEnd() {
		return endOfBlock;
	}
	
	public IndexNodeDefinition getParentDef() {
		return null;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		return true;
	} 
	
	public IndexNodeDefinition(long rev, StructureDataFile sdr, BlockType block_type, int endPos, AppendableBuilderReader value) {
		reset(rev, sdr, block_type, endPos, value);
	}

	public IndexNodeDefinition(long rev, StructureDataFile sdr, BlockType block_type, int typeEndPos, AppendableBuilderReader category, int endPos,
			                    AppendableBuilderReader value) {
		reset(rev, sdr, block_type, typeEndPos, category, endPos, value);
	}

	
	//ensure this is never used until reset is called
	public void invalidate() {
		this.name = null;	
		this.endPositionOfBlockName =  -1;		
		this.endOfBlock = -1;
		this.categoryEndPos = -1;
		this.nameEndPos = -1;		
	}
	
	public void reset(long rev, StructureDataFile sdr, BlockType block_type, 
			          int catEndPos, AppendableBuilderReader category, 
			          int endPos, AppendableBuilderReader value) { //for re-use but will probably be populated with what it already has.

		setRevision(rev);
		
		if (null!=this.name && value.isEqual(this.name)) {
		} else {
			this.name = value.toBytes();
		}
		if (null!=this.category && category.isEqual(this.category)) {
		} else {
			this.category = category.toBytes();
		}
		
		this.categoryEndPos = catEndPos;
		this.nameEndPos = endPos;
		
		this.block_type = block_type;
		
		this.sdr = sdr;
		this.editorOffsetPosition = endPos;
		this.contentKey = null;
		this.contentData = null;
		
	}

	public void reset(long rev, StructureDataFile sdr, BlockType block_type, 
			          int endPos, AppendableBuilderReader value) {

		setRevision(rev);
		
		if (null!=this.name && value.isEqual(this.name)) {
			this.nameEndPos = endPos;
		} else {
			this.name = value.toBytes();
			this.nameEndPos = endPos;
		}
		this.category = null;
		
		this.block_type = block_type;
		
		this.sdr = sdr;
		this.editorOffsetPosition = endPos;
		this.contentKey = null;
		this.contentData = null;
		
	}

	//add usage child inside "this" definition of which is an instance of "that" definition
	public IndexNodeUsage addUsageChild(StructureDataFile sdr, BlockType type, int defNameUsageEndPos, 
			                  AppendableBuilderReader thatDefName) {
		
		int count = sdr.usageCounts[type.ordinal()]++;
		IndexNodeUsage[] temp = sdr.usageLocations[type.ordinal()] = StructureDataFile.growAsNeeded(sdr.usageLocations[type.ordinal()], count);
		
		if (null != temp[count]) {
			temp[count].reset(sdr.nodeRevision.get(), sdr, type, this, defNameUsageEndPos, thatDefName);
		} else {
			temp[count] = new IndexNodeUsage(sdr.nodeRevision.get(), sdr, type, this, defNameUsageEndPos, thatDefName);
		}
		return temp[count];
		
	}

	public IndexNodeUsage addUsageChild(StructureDataFile sdr, BlockType type, int categoryEndPos, 
			                  AppendableBuilderReader category, int endPos, 
			                  AppendableBuilderReader value) {

		int count = sdr.usageCounts[type.ordinal()]++;
		IndexNodeUsage[] temp = sdr.usageLocations[type.ordinal()] = StructureDataFile.growAsNeeded(sdr.usageLocations[type.ordinal()], count);
		
		if (null != temp[count]) {
			temp[count].reset(sdr.nodeRevision.get(), sdr, type, this, categoryEndPos, category, endPos, value);
		} else {			
			temp[count] = new IndexNodeUsage(sdr.nodeRevision.get(), sdr, type, this, categoryEndPos, category, endPos, value);
		}
		return temp[count];
		
	}
	
	public boolean hasUsage(StructureDataFile sdr, BlockType type, byte[] name) {
		IndexNodeUsage[] temp = sdr.usageLocations[type.ordinal()];
		int w = temp.length;
		while (--w >= 0) {
			if (null != temp[w] && temp[w].isMatch(name)) {
				return true;
			}
		}
		return false;
	}
	public boolean hasUsage(StructureDataFile sdr, BlockType type, byte[] catalog, byte[] name) {
		IndexNodeUsage[] temp = sdr.usageLocations[type.ordinal()];
		int w = temp.length;
		while (--w >= 0) {
			if (null != temp[w] && temp[w].isMatch(catalog, name)) {
				return true;
			}
		}
		return false;
	}
	

	public void visitUsages(StructureDataFile sdr, BlockType type, byte[] name, Consumer<IndexNodeUsage> consumer) {
		IndexNodeUsage[] temp = sdr.usageLocations[type.ordinal()];
		int w = temp.length;
		while (--w>=0) {
			if (null!=temp[w] && temp[w].isMatch(name)) {
				consumer.accept(temp[w]);
			}
		}
	}
	public void visitUsages(StructureDataFile sdr, BlockType type, byte[] catalog, byte[] name,
				            Consumer<IndexNodeUsage> consumer) {
				IndexNodeUsage[] temp = sdr.usageLocations[type.ordinal()];
				int w = temp.length;
				while (--w>=0) {
					if (null!=temp[w] && temp[w].isMatch(catalog, name)) {
						consumer.accept(temp[w]);
					}
				}
	}
	
	
	public void setEndOfDef(int ordinal, int endPositionOfBlockName, int closePosition) {
		if (ordinal != block_type.ordinal()) {
			throw new UnsupportedOperationException("internal error");
		}
		this.endPositionOfBlockName =  endPositionOfBlockName;		
		this.endOfBlock = closePosition;
		
	}

	public boolean isEqual(byte[] name) {
		return Arrays.equals(this.name, name);
	}

	public boolean isEqual(byte[] category, byte[] name) {
		
		return Arrays.equals(this.name, name)
				&& Arrays.equals(this.category, category);
		
	}

	public BlockType type() {
		return block_type;
	}

	public byte[] category() {
		return category;
	}

	public byte[] name() {
		return name;
	}
	
	

	@Override
	public int compareTo(IndexNodeDefinition that) {
				
		return this.toString().compareTo(that.toString());
		
	}
	

	
	
}
