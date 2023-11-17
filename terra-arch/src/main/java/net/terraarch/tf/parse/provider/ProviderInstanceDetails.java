package net.terraarch.tf.parse.provider;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import net.terraarch.tf.parse.version.VersionConstraint;

public class ProviderInstanceDetails {

	public long revision;
    public String sourceNamespace;
    public String sourceName;
    public String name;
    public String[] key;
    public List<VersionConstraint> constraints;
	
//    public void isValid() {
//    	
//    	constraints.forEach(vc-> {
//    		vc.isValid();
//    	});
//    	
//    }
    
    public <T extends Appendable> T toString(T target) {
    	
    	try {

			target.append(sourceNamespace).append(":");
			target.append(sourceName).append(":");
			target.append(name);
			
			constraints.forEach(vc-> {
				try {
					target.append(',');
					target.append(vc.toString());
					//vc.toString(target);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    	
    	return target;
    }
    public String toString() {
    	return toString(new StringBuilder()).toString();
    }
    
    
	public void update(long revision, String name, String sourceNamespace, String sourceName,
		                                        	List<VersionConstraint> constraints) {
		this.revision = revision;
		this.name = name;
		this.sourceName = sourceName;
		this.sourceNamespace = sourceNamespace;
		this.constraints = constraints;
		this.key = new String[] {sourceNamespace, sourceName, name};
	}

	public void update(long revision, String name, List<VersionConstraint> constraints) {
		this.revision = revision;
		this.name = name;
		this.sourceName = null;
		this.sourceNamespace = null;
		this.constraints = constraints;
		this.key = new String[] {"", "", name};
	}

	public void update(long revision, String name) {
		this.revision = revision;
		this.name = name;
		this.sourceName = null;
		this.sourceNamespace = null;
		this.constraints = Collections.emptyList();
		this.key = new String[] {"", "", name};
		
	}
	public String[] providerKey() {
		return key;
	}
	
	
	
	
}
