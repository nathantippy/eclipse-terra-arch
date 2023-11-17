package net.terraarch.tf.structure.walker;


import net.terraarch.tf.structure.GatheredFieldType;

public class GatherProposalsAdapterData<T> implements Comparable<GatherProposalsAdapterData<T>>{
	public final int typeEndPos;
	public final String categoryType;
	public final int nameEndPos;
	public final String nameValue;
	public final GatheredFieldType type;

	public T field;
	
	public GatherProposalsAdapterData(int typeEndPos, String categoryName, int nameEndPos, String nameValue, GatheredFieldType type) {
		this.typeEndPos = typeEndPos;
		this.categoryType = categoryName;
		this.nameEndPos = nameEndPos;
		this.nameValue = nameValue;
		this.type = type;
	}

	@Override
	public int compareTo(GatherProposalsAdapterData<T> that) {
		return Integer.compare(this.type.ordinal(), that.type.ordinal());
	}
	
	
}