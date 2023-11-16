package net.terraarch.terraform.parse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.terraarch.util.TrieParser;
import net.terraarch.util.TrieParserReader;

public class ParseBuffer {

	private byte[] buffer;
	private TrieParserReader reader = new TrieParserReader(true);
	private final static Logger logger  = LoggerFactory.getLogger(ParseBuffer.class);
		
	private static byte[] getPow2Buffer(int length, ParseBuffer wb) {
		if (wb.buffer!=null && wb.buffer.length > length) {
			return wb.buffer;
		}
		int size = 1 << (int)Math.ceil(Math.log(length)/Math.log(2));
		wb.buffer = new byte[size];
		return wb.buffer;
	}
	
	private static final void parsePrep(byte[] data, ParseBuffer wb) {
		byte[] buffer = getPow2Buffer(data.length, wb);		
		System.arraycopy(data,  0,  buffer,  0,  data.length);
		
	    
		
		TrieParserReader.parseSetup(wb.reader,  buffer, 0,  data.length,  buffer.length-1);
	}


	public boolean tokenizeDocument(byte[] data,  ParseState tokenMap) {	
		try {	
				parsePrep(data, this);
				tokenMap.setDataSizeLimit(data.length);
				return TFConstants.tokenizeAll(reader, tokenMap);	
		} catch (Throwable t) {
			t.printStackTrace();
			logger.error("internal error",t);
			return false;
		}
	}
	
	public long matchBytes(byte[] data, TrieParser trieParser) {
		try {
			parsePrep(data, this);
			long result = TrieParserReader.parseNext(reader, trieParser);
			if (result>=0) {
				if (TrieParserReader.parseHasContent(reader)) {
					//not a matched because we have some left!!
					return -1;
				};
			}
			return result;
		} catch (Throwable t) {
			logger.debug("internal error",t);
			return -1;
		}
	}
	
	
}
