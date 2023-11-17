package net.terraarch.tf.parse.provider;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.terraarch.tf.parse.version.VersionConstraint;
import net.terraarch.tf.parse.version.VersionConstraints;
import net.terraarch.tf.parse.version.VersionDTO;
import net.terraarch.util.TrieParser;
import net.terraarch.util.TrieParserReader;

public class ProviderIndexRecord {  

	private TrieParser aliasParser = null;
	private List<ProviderInstanceDetails> aliasRecord = null;
	private ProviderInstanceDetails details = null;	
	
	private static final Logger logger = LoggerFactory.getLogger(ProviderIndexRecord.class);
	
	public ProviderIndexRecord() {
	}
	
	public String[] providerKey() {
		return details.providerKey();
	}
	
	public boolean isValid() {
		return null!=details;
	}
	
	public String toString() {
		return null==details ? "NullDetails" : details.toString();
	}
	
	public void update(long revision, String name, String sourceNamespace, String sourceName, List<VersionConstraint> constraints) {
		if (null==details) {
			details = new ProviderInstanceDetails();
		}
		details.update(revision,name,sourceNamespace,sourceName,constraints);
	}
	
	public void update(long revision, String name, List<VersionConstraint> constraints) {
		if (null==details) {
			details = new ProviderInstanceDetails();
		}
		details.update(revision,name,constraints);
	}

	public void update(TrieParserReader reader, long revision, String alias, String name, List<VersionConstraint> constraints) {
		try {
			if (null==details) {
				details = new ProviderInstanceDetails();
			}
			details.update(revision,name);
			
			if (null==aliasParser) {
				aliasParser = new TrieParser(32,false);			
				aliasRecord = new ArrayList<ProviderInstanceDetails>();
			}
	        
			int idx = (int)TrieParserReader.query(reader, aliasParser, alias);
			if (idx>=0) {
				aliasRecord.get(idx).update(revision, alias, constraints);
			} else {
				ProviderInstanceDetails details = new ProviderInstanceDetails();
				details.update(revision, alias, constraints);
				aliasRecord.add(details);
				aliasParser.setUTF8Value(alias, aliasRecord.size()-1);
			}
		} catch (Throwable t) {
			logger.error("update",t);
		}
	}

	public void flushOldProviders(long expected) {
		try {
			if (details!=null) {
				if (details.revision!=expected) {
					details = null;
				}
			}
			
			if (null!=aliasRecord) {
				int x = aliasRecord.size();
				while (--x>=0) {
					ProviderInstanceDetails detail = aliasRecord.get(x);
					if (null!=detail && detail.revision!=expected) {
						aliasRecord.set(x, null);
					}
				}
			}
		} catch (Throwable t) {
			logger.error("flushOldProviders",t);
		}
	}

	//select the best most newest version or return null if nothing is found
//	public VersionDTO selectVersion(VersionDTO[] orderedVersionDTOs) {
//		VersionDTO result = null;
//		for (VersionDTO verDTO: orderedVersionDTOs) { //NOTE: rather verbose we need the cached list!!!
//			
//			//find the last match which should be the newest version
//			if (matchesAllConstraints(verDTO, details.constraints)) {
//				result = verDTO;
//			}
//		}
//		return result;
//	}
//
//	public boolean matchesAllConstraints(VersionDTO verDTO, List<VersionConstraint> constraints) {
//		for(VersionConstraint vc: constraints) {
//			if (!vc.isValid(verDTO)) {
//				return false;
//			};
//		}
//		return true;
//	}

	public void keepValid(List<VersionDTO> target) {
		int w = target.size();
		while (--w>=0) {		
			for (VersionConstraint vc: details.constraints) {
				if (!vc.isValid(target.get(w))) {
					target.remove(w);
				}
			}
		}
	}

}
