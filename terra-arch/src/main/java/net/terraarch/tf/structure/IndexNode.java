package net.terraarch.tf.structure;


import net.terraarch.tf.parse.BlockType;
import net.terraarch.util.Appendables;

public abstract class IndexNode {

	abstract public BlockType type();
	abstract public byte[] category();
	abstract public byte[] name();

	//public IFile optionalIFile = null;
	public int editorOffsetPosition = -1;
	protected StructureDataFile sdr;
	
	protected long revision = -1;
	

	public StructureDataFile sdr() {
		return sdr;
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
//		if (null!=optionalIFile) { //TODO: FFFF, convert this so outline shows file name as roollover..
//			builder.append("");
//			builder.append(optionalIFile.getName());
//			builder.append(": ");
//		}
				
		
		if (isLooped()) {
			builder.append(" LOOP "); //must be here NOT in the buildStringName
		}
		buildStringName(builder);
		
		return builder.toString();
	}
	

	public boolean isRevision(final long rev) {
		return rev == revision;
	}
	public void setRevision(final long rev) {
		this.revision = rev;
	}
	
	public void buildStringName(StringBuilder builder) {
		builder.append(type().label());
		if (type().isCatigorized) {
			if (category()!=null && category().length>0) {
				Appendables.appendUTF8(builder, category(), 0, category().length, Integer.MAX_VALUE);
				builder.append(" . "); //wider space to help readability
			}
		}
		Appendables.appendUTF8(builder, name(), 0, name().length, Integer.MAX_VALUE);
	}
	
		
	public abstract IndexNodeDefinition getParentDef();
	
	public abstract int blockPositionStart();	
	public abstract int blockPositionEnd();
	public abstract void invalidate();
	
	boolean looped = false;
	public boolean isLooped() {
		try {
			if (looped) {
				//NOTE: confirm this is still looped.			
				if (this instanceof IndexNodeUsage) {
					 //this is linear and less than a dozzen hops, its not slow.
					 loopDetection(((IndexNodeUsage)this),this.getParentDef());
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return looped;
	};
	protected void isLooped(boolean looped) {
		this.looped = looped;
	};
	
//	//help reduce the noise?, need more investigation.
//	InstanceScope.INSTANCE
//		.getNode(GenericEditorPlugin.BUNDLE_ID) //TerraArchActivator.PLUGIN_ID)
//		.putBoolean("org.eclipse.ui.genericeditor.togglehighlight", false);
//	
	
	protected boolean loopDetection(IndexNodeUsage source, IndexNode parent) {
		
		if (null==parent || (parent.looped && !source.looped)) { //do not call isLooped method
			return false;
		}		
		if (parent.type() == type()) {
			if (type().isCatigorized) {
				if (source.isMatch(parent.category(), parent.name())) {
					parent.isLooped(true);
					source.isLooped(true);
					return true;
				}
			} else {
				if (source.isMatch(parent.name())) {
					parent.isLooped(true);
					source.isLooped(true);
					return true;
				}
			}
		}		
		
		if (!loopDetection(source, parent.getParentDef())) {
			parent.isLooped(false);
			return false;
		} else {
			parent.isLooped(true);
			return true;
		}
	}
	
	
}
