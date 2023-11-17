package net.terraarch.tf.parse;

public class TermLayer {

	private int ordinal = -1;
	
	private int namespace = -1;
	
	private long resource = -1;
	private long provider = -1;
	private long dataType = -1;
	
	
	
	public TermLayer() {
		this.ordinal = 0;
	}

	public void clear() {
		this.ordinal = -1;	
		this.namespace = -1;
		this.resource = -1;
		this.provider = -1;
			
	}
	
	public void namespace(int f) {
		namespace = f;
	}
	
	public int namespace() {
		return namespace;
	}
	
	public void resource(long r) {
		resource = r;		
	}
	
	public long resource() {
		return resource;
	}
	
	public void provider(long p) {
		provider = p;
	}
	
	public long provider() {
		return provider;
	}
	
	public void dataType(long d) {
		dataType = d;
	}
	
	public long dataType() {
		return dataType;
	}
	
	
	public void inc() {
		this.ordinal++;
	}
	
	public int get() {
		return this.ordinal;
	}
	
}
