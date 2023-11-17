package net.terraarch.tf.parse.doc;

import java.io.Serializable;
import java.util.List;

import net.terraarch.tf.parse.version.VersionConstraint;

public class ProviderDTO implements Serializable {

	public final String sourceNamespace; 
	public final String sourceName;
	public final List<VersionConstraint> constraints;
    
    public ProviderDTO(final String sourceNamespace, final String sourceName, final List<VersionConstraint> constraints) {
    	this.sourceNamespace = sourceNamespace;
    	this.sourceName = sourceName;
    	this.constraints = constraints;    	
    }
	
}
