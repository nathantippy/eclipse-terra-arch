package net.terraarch.terraform.schema;

import net.terraarch.pipe.DataInputBlobReader;
import net.terraarch.pipe.FieldReferenceOffsetManager;
import net.terraarch.pipe.MessageSchema;
import net.terraarch.pipe.Pipe;
import net.terraarch.pipe.PipeReader;
import net.terraarch.pipe.PipeWriter;

public class NewFileSourceSchema extends MessageSchema<NewFileSourceSchema> {
	
	public final static FieldReferenceOffsetManager FROM = new FieldReferenceOffsetManager(
		    new int[]{0xc0400004,0xa8000000,0xa8000001,0xb8000002,0xc0200004},
		    (short)0,
		    new String[]{"SourceFile","FileName","CannonicalName","MetaData",null},
		    new long[]{1, 11, 12, 13, 0},
		    new String[]{"global",null,null,null,null},
		    "NewFileSource.xml",
		    new long[]{2, 2, 0},
		    new int[]{2, 2, 0});


		public NewFileSourceSchema() { 
		    super(FROM);
		}

		protected NewFileSourceSchema(FieldReferenceOffsetManager from) { 
		    super(from);
		}

		public static final NewFileSourceSchema instance = new NewFileSourceSchema();

		public static final int MSG_SOURCEFILE_1 = 0x00000000; //Group/OpenTempl/4
		public static final int MSG_SOURCEFILE_1_FIELD_FILENAME_11 = 0x01400001; //UTF8/None/0
		public static final int MSG_SOURCEFILE_1_FIELD_CANNONICALNAME_12 = 0x01400003; //UTF8/None/1
		public static final int MSG_SOURCEFILE_1_FIELD_METADATA_13 = 0x01c00005; //ByteVector/None/2

		public static void consume(Pipe<NewFileSourceSchema> input) {
		    while (PipeReader.tryReadFragment(input)) {
		        int msgIdx = PipeReader.getMsgIdx(input);
		        switch(msgIdx) {
		            case MSG_SOURCEFILE_1:
		                consumeSourceFile(input);
		            break;
		            case -1:
		               //requestShutdown();
		            break;
		        }
		        PipeReader.releaseReadLock(input);
		    }
		}

		public static void consumeSourceFile(Pipe<NewFileSourceSchema> input) {
		    StringBuilder fieldFileName = PipeReader.readUTF8(input,MSG_SOURCEFILE_1_FIELD_FILENAME_11,new StringBuilder(PipeReader.readBytesLength(input,MSG_SOURCEFILE_1_FIELD_FILENAME_11)));
		    StringBuilder fieldCannonicalName = PipeReader.readUTF8(input,MSG_SOURCEFILE_1_FIELD_CANNONICALNAME_12,new StringBuilder(PipeReader.readBytesLength(input,MSG_SOURCEFILE_1_FIELD_CANNONICALNAME_12)));
		    DataInputBlobReader<NewFileSourceSchema> fieldMetaData = PipeReader.inputStream(input, MSG_SOURCEFILE_1_FIELD_METADATA_13);
		}

		public static void publishSourceFile(Pipe<NewFileSourceSchema> output, CharSequence fieldFileName, CharSequence fieldCannonicalName, byte[] fieldMetaDataBacking, int fieldMetaDataPosition, int fieldMetaDataLength) {
		        PipeWriter.presumeWriteFragment(output, MSG_SOURCEFILE_1);
		        PipeWriter.writeUTF8(output,MSG_SOURCEFILE_1_FIELD_FILENAME_11, fieldFileName);
		        PipeWriter.writeUTF8(output,MSG_SOURCEFILE_1_FIELD_CANNONICALNAME_12, fieldCannonicalName);
		        PipeWriter.writeBytes(output,MSG_SOURCEFILE_1_FIELD_METADATA_13, fieldMetaDataBacking, fieldMetaDataPosition, fieldMetaDataLength);
		        PipeWriter.publishWrites(output);
		}
}