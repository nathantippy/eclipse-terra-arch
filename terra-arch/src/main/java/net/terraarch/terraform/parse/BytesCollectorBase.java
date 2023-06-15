package net.terraarch.terraform.parse;

import java.io.File;

import net.terraarch.util.ByteConsumer;
import net.terraarch.util.TrieParserReader;

public class BytesCollectorBase implements ByteConsumer {

	private int linesCounted = 0;
	private int utfBytesOffset = 0;
	private int utfSizeCount = 0;
	
	@Override
	public void consume(byte[] backing, int pos, int len, int mask) {
			int i = len;
			while (--i>=0 ) {				
					byte b = backing[(pos+i)&mask];
					countUTF(0xFF&b);
					if (b=='\n') {
						linesCounted++;
					}
			}
	}

	private void countUTF(int b) {
	
		
		 if (b>=128) {
			 if (--utfSizeCount<0) {	
				 
				 utfSizeCount = NodeProcessor.bytesCompute(b);
				 utfBytesOffset += (--utfSizeCount);
				 
			 }
		 }
		 
	}

	@Override
	public void consume(byte value) {
			countUTF(0xFF&value);
			if ('\n'==value) {
				linesCounted++;
			}
	}
	

	public File finish() {
		return null;
	}

	public int totalNewLinesCaptured() {
		return linesCounted;
	}
	
	public int utfBytesOffset() {
		return utfBytesOffset;
	}
	
}
