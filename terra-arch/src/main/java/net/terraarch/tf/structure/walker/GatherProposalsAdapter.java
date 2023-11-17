package net.terraarch.tf.structure.walker;

import java.util.ArrayList;
import java.util.List;

import net.terraarch.tf.structure.GatherProposals;
import net.terraarch.tf.structure.GatherProposalsVisitor;
import net.terraarch.tf.structure.GatheredFieldType;
import net.terraarch.tf.structure.StructureDataFile;
import net.terraarch.util.AppendableBuilderReader;

public abstract class GatherProposalsAdapter implements GatherProposalsVisitor {

    
	public final List<GatherProposalsAdapterData> data = new ArrayList<GatherProposalsAdapterData>();
	
	protected int preBytes;
    protected String details;
    protected StructureDataFile sdr;

    protected GatherProposals<?> that;
    
	private AppendableBuilderReader transientValue;
    
    
    public GatheredFieldType getType() {
    	return !data.isEmpty() ? data.get(0).type : null;
    }
    
    public String getNameValue() {
    	return !data.isEmpty() ? data.get(0).nameValue : null;
    }
    
    public int getNameEndPos() {
    	return !data.isEmpty() ? data.get(0).nameEndPos : null;
    }
    
    public String getTypeValue() {
    	return !data.isEmpty() ? data.get(0).categoryType : null;
    }

    public int getTypeEndPos() {
    	return !data.isEmpty() ? data.get(0).typeEndPos : null;
    }
    
    public StructureDataFile getSDR() {
    	return sdr;
    }
    
    @Override
	public void visit(byte[] bytes, int length, long idx) {
		try {
			if (that!=null && that.guv()!=null) {
				that.guv().consume(that, transientValue, preBytes, bytes, length);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
    
    @Override
    public boolean foundInDocument(GatherProposals<?> that, int endPos, AppendableBuilderReader value, int preBytes, GatheredFieldType type) {
    	
    	data.add(new GatherProposalsAdapterData(-1,null,endPos,value.toString(),type));
    	    	
    	this.transientValue = value;
    	this.preBytes = preBytes;
    	this.that = that;
    	return true;
    }

    @Override
    public boolean foundInDocument(GatherProposals<?> that, int typeEndPos, AppendableBuilderReader typeValue,  int endPos, AppendableBuilderReader value, int preBytes, GatheredFieldType type) {

    	data.add(new GatherProposalsAdapterData(typeEndPos,typeValue.toString(),endPos,value.toString(),type));
    	      	
    	this.transientValue = value;
    	this.preBytes = preBytes;
    	this.that = that;
    	return true;
    }
    
	

	@Override
	public void activeRecord(StructureDataFile sdr) {
		if (null!=sdr) {
			this.sdr = sdr;
		} else {
			throw new NullPointerException();
		}
	}
    
	@Override
	public void consume(GatherProposals<?> gatherProposals, AppendableBuilderReader value, int preBytes,
							    	final byte[] bytesBuffer, final int length) {
		
	}
	
}
