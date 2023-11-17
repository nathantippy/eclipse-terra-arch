package net.terraarch.tf.parse.version;

import java.util.Arrays;
import java.util.function.Consumer;

import net.terraarch.util.AppendableBuilderReader;
import net.terraarch.util.TrieParser;
import net.terraarch.util.TrieParserReader;

public class VersionConstraints {

	
	private static TrieParser versionConstraintParser = buildVPC();
	
	private static TrieParser buildVPC() {
		TrieParser parser = new TrieParser(128, false);
		
		parser.setUTF8Value("=",  VersionOperator.EQUAL.ordinal());
		parser.setUTF8Value("!=", VersionOperator.NOT_EQUAL.ordinal());
		parser.setUTF8Value(">",  VersionOperator.GREATER.ordinal());
		parser.setUTF8Value(">=", VersionOperator.GREATER_OR_EQUAL.ordinal());
		parser.setUTF8Value("<",  VersionOperator.LESSTHAN.ordinal());
		parser.setUTF8Value("<=", VersionOperator.LESSTHAN_OR_EQUALS.ordinal());
		parser.setUTF8Value("~>", VersionOperator.SMART_RANGE.ordinal());
		
		parser.setUTF8Value(",",  20);
		parser.setUTF8Value(".",  21);
		parser.setUTF8Value(" ",  22);
		
		parser.setUTF8Value("%u", 11);
		parser.setUTF8Value("-",  31);
				
		return parser;
	}
	
	//returns fail poisition or -1 on success
	public static int parse(AppendableBuilderReader data, Consumer<VersionConstraint> consumer) {
	    		
		TrieParserReader reader = new TrieParserReader(true);
		int[] versions = new int[8];
		
		data.parseSetup(reader);
				
		StringBuilder tagLabel = new StringBuilder();
		
		int activeVersionPos = 0;
		int activeOperator = VersionOperator.EQUAL.ordinal();//is equals if not set
		
		long idx = -1;
		while (
				TrieParserReader.parseHasContent(reader) &&
				(idx = TrieParserReader.parseNext(reader,versionConstraintParser,-1,-2))>=0) {
			
			
		   if (idx>=2 && idx<=8) {
			   activeOperator = (int)idx;
		   }
		   if (11==idx) {
			   //accumulate #
			   if (activeVersionPos < versions.length) {
				   versions[activeVersionPos++] = (int)TrieParserReader.capturedLongField(reader, 0);
			   } else {
				   return TrieParserReader.bytesConsumedAfterSetup(reader);
			   }
		   }
		   
		   if (31==idx) {
			   int c = -1;
			   while (okChar(c=reader.parseSkipOne())) {
				   tagLabel.append((char)c);
			   };
			   ///System.out.println("tag name: "+tagLabel);
			   if (c!=-1) {
				   reader.moveBack(1);
			   }
		   }
		   
		   if (20==idx) {
			   if (versions.length<2 && activeOperator==VersionOperator.SMART_RANGE.ordinal()) {
				   return TrieParserReader.bytesConsumedAfterSetup(reader);
			   }
			   //push constraint
			   consumer.accept(pushConstraint(activeOperator, versions, activeVersionPos, tagLabel));
			   
			   activeVersionPos = 0;
			   tagLabel.setLength(0);
			   activeOperator = VersionOperator.EQUAL.ordinal();//restore default
		   }
		};
		if (TrieParserReader.parseHasContent(reader) ) {
			return TrieParserReader.bytesConsumedAfterSetup(reader);
		}
		
		//System.out.println("idx:"+idx);
		if (activeVersionPos>0) {
			   if (versions.length<2 && activeOperator==VersionOperator.SMART_RANGE.ordinal()) {
				   return TrieParserReader.bytesConsumedAfterSetup(reader);
			   }   
			   consumer.accept(pushConstraint(activeOperator, versions, activeVersionPos, tagLabel));
		}
        return -1;
	}

	//returns fail poisition or -1 on success
	public static int parse(AppendableBuilderReader data, VersionVisitor visitor) {
		    		
			TrieParserReader reader = new TrieParserReader(true);
			int[] versions = new int[8];
			
			data.parseSetup(reader);
					
			StringBuilder tagLabel = new StringBuilder();
			
			int activeVersionPos = 0;
			
			long idx = -1;
			while (
					TrieParserReader.parseHasContent(reader) &&
					(idx = TrieParserReader.parseNext(reader,versionConstraintParser,-1,-2))>=0) {
			
			
			   if (11==idx) {
				   //accumulate #
				   if (activeVersionPos < versions.length) {
					   versions[activeVersionPos++] = (int)TrieParserReader.capturedLongField(reader, 0);
				   } else {
					   return TrieParserReader.bytesConsumedAfterSetup(reader);
				   }
			   }
			   
			   if (31==idx) {
				   int c = -1;
				   while (okChar(c=reader.parseSkipOne())) {
					   tagLabel.append((char)c);
				   };
				   ///System.out.println("tag name: "+tagLabel);
				   if (c!=-1) {
					   reader.moveBack(1);
				   }
			   }
			   
			   if (20==idx) {
				   //push constraint
				   visitor.accept(versions, tagLabel.toString());
				  
				   activeVersionPos = 0;
				   tagLabel.setLength(0);
			   }
			};
			int result = -1;
			if (TrieParserReader.parseHasContent(reader) ) {
				while (TrieParserReader.parseHasContent(reader)) {
					tagLabel.append((char)reader.parseSkipOne());
				}
				result = TrieParserReader.bytesConsumedAfterSetup(reader);
			}
			visitor.accept(versions, tagLabel.toString());
	        return result;
		}
	
	private static boolean okChar(int c) {
		if (c==' ' || c =='.' || c=='<' || c=='>' || c=='=' || c=='!' || c=='~' || c==',' || c==-1) {
			return false;
		}
		return true;
	}

	private static VersionConstraint pushConstraint(int activeOperator, int[] versions, 
			                           int versionsLen, StringBuilder tagLabel) {
		
		return new VersionConstraint(activeOperator, Arrays.copyOf(versions, versionsLen), tagLabel.toString());
		
	}
	
	
}