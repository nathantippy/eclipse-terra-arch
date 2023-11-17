package net.terraarch.tf.parse.doc;

import java.io.Serializable;

public class TokenSelector implements Serializable {
		
	private static final long serialVersionUID = 1L;
	
	public final int start;
	public final int stop;	
	public final int lineNumber;
	private final int[] tokenIds; 
	
	private int selectedToken = 0;
    private String[] errorMesssages; //align with tokens

	
    public TokenSelector(int lineNumber, int start, int stop, int[] token) {
		this.lineNumber = lineNumber+1; //NOTE: off by one should fix
		this.start = start;
		this.stop = stop;
		if (stop<start) {
			throw new UnsupportedOperationException("start: "+start+" stop: "+stop);
		}
		this.tokenIds = token;
	}	
	
    public TokenSelector(int lineNumber, int start, int stop, int token) {
		this.lineNumber = lineNumber+1; //NOTE: off by one should fix
		this.start = start;
		this.stop = stop;
		if (stop<start) {
			throw new UnsupportedOperationException("start: "+start+" stop: "+stop);
		}
	    this.tokenIds = new int[] {token}; //TODO: not GC, may need to revisit.
	}				
	
    public int getTokenId() {
		return tokenIds[selectedToken];
	}

    
	public void setSelection(int idx) { 
		selectedToken = idx;
	}
	

    public TokenSelector err(int idx, String message) {
    	if (null==errorMesssages) {
    		errorMesssages = new String[tokenIds.length];
    	}
        errorMesssages[idx] = message;
    	return this;
    }
    
    public TokenSelector err(boolean isErr, int errIdx, String message) {
    	if (isErr) {	
	    	if (null==errorMesssages) {
	    		errorMesssages = new String[tokenIds.length];
	    	}
	        errorMesssages[errIdx] = message;
    	}
    	return this;
    }
    
    public TokenSelector err(boolean isErr, String message) {
    	//use this method if there is only 1 type but it may or may not be an error as determined earlier
    	if (isErr) {	
    		if (null==errorMesssages) {
	    		errorMesssages = new String[tokenIds.length];
	    	}
	        errorMesssages[0] = message;
    	}
    	return this;
    }
    
    public TokenSelector err(String message) {
        if (null==errorMesssages) {
	  		errorMesssages = new String[tokenIds.length];
     	}
	    errorMesssages[0] = message;
    	return this;
    }	

	public String selectedErrorMessage() {
		return errorMesssages[selectedToken];
	}

	public boolean hasErrorMessages() {
		return null!=errorMesssages;
	}
    
}