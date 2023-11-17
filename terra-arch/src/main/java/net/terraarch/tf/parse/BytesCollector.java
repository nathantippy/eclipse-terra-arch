package net.terraarch.tf.parse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import net.terraarch.util.ByteConsumer;

public class BytesCollector extends BytesCollectorBase {

	private final File file;
	private final FileOutputStream fost;
	private int totalNewLinesCaptured = 0;

	public BytesCollector(String suffix) {
		try {
			//write to file.
			file = File.createTempFile("collectedBytes", suffix);
			file.deleteOnExit();
			fost = new FileOutputStream(file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	public int totalNewLinesCaptured() {
		return totalNewLinesCaptured;
	}
	
	@Override
	public void consume(byte[] backing, int pos, int len, int mask) {
		try {		
			int idx = pos & mask;
			int partialLength = 1 + mask - idx;
			//may need to wrap around ringBuffer so this may need to be two copies
			if (partialLength>=len) {
				fost.write(backing, idx, len);								
				totalNewLinesCaptured+= newLineCount(backing, idx, len);
				
			} else {
			    //read from source and write into byteBuffer
				fost.write(backing, idx, partialLength);
				totalNewLinesCaptured+= newLineCount(backing, idx, partialLength);
				fost.write(backing, 0, len - partialLength);
				totalNewLinesCaptured+= newLineCount(backing, 0, len - partialLength);
			}			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private int newLineCount(byte[] backing, int idx, int len) {
		int sum = 0;
		int x = len+idx;
		while (--x>=idx) {
			if ('\n'==backing[x]) {
				sum++;
			}
		}
		return sum;
	}

	@Override
	public void consume(byte value) {
		try {
			fost.write(0xFF & ((int)value));
			if (value == '\n') {
				totalNewLinesCaptured++;
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public File finish() {
		try {
			fost.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		return file;
	}
	
}
