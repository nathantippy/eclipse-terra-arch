package net.terraarch.tf.schema;

import net.terraarch.pipe.DataInputBlobReader;
import net.terraarch.pipe.FieldReferenceOffsetManager;
import net.terraarch.pipe.MessageSchema;
import net.terraarch.pipe.Pipe;
import net.terraarch.pipe.PipeReader;
import net.terraarch.pipe.PipeWriter;

public class SourceDataSchema extends MessageSchema<SourceDataSchema> {

	public final static FieldReferenceOffsetManager FROM = new FieldReferenceOffsetManager(
		    new int[]{0xc0400004,0xa8000000,0xa8000001,0xb8000002,0xc0200004,0xc0400002,0xa8000000,0xc0200002,0xc0400005,0xa8000001,0xa8000000,0xb8000003,0x88000000,0xc0200005,0xc0400003,0xb8000003,0x88000000,0xc0200003},
		    (short)0,
		    new String[]{"ModuleBegin","CannonicalName","FolderRoot","MetaData",null,"ModuleEnd","CannonicalName",
		    null,"SourceDataBegin","Location","CannonicalName","Data","Flags",null,"SourceDataContinuation",
		    "Data","Flags",null},
		    new long[]{3, 12, 11, 13, 0, 4, 12, 0, 1, 11, 12, 101, 103, 0, 2, 101, 103, 0},
		    new String[]{"global",null,null,null,null,"global",null,null,"global",null,null,null,null,null,
		    "global",null,null,null},
		    "SourceData.xml",
		    new long[]{2, 2, 0},
		    new int[]{2, 2, 0});


		public SourceDataSchema() { 
		    super(FROM);
		}

		protected SourceDataSchema(FieldReferenceOffsetManager from) { 
		    super(from);
		}

		public static final SourceDataSchema instance = new SourceDataSchema();

		public static final int MSG_MODULEBEGIN_3 = 0x00000000; //Group/OpenTempl/4
		public static final int MSG_MODULEBEGIN_3_FIELD_CANNONICALNAME_12 = 0x01400001; //UTF8/None/0
		public static final int MSG_MODULEBEGIN_3_FIELD_FOLDERROOT_11 = 0x01400003; //UTF8/None/1
		public static final int MSG_MODULEBEGIN_3_FIELD_METADATA_13 = 0x01c00005; //ByteVector/None/2
		public static final int MSG_MODULEEND_4 = 0x00000005; //Group/OpenTempl/2
		public static final int MSG_MODULEEND_4_FIELD_CANNONICALNAME_12 = 0x01400001; //UTF8/None/0
		public static final int MSG_SOURCEDATABEGIN_1 = 0x00000008; //Group/OpenTempl/5
		public static final int MSG_SOURCEDATABEGIN_1_FIELD_LOCATION_11 = 0x01400001; //UTF8/None/1
		public static final int MSG_SOURCEDATABEGIN_1_FIELD_CANNONICALNAME_12 = 0x01400003; //UTF8/None/0
		public static final int MSG_SOURCEDATABEGIN_1_FIELD_DATA_101 = 0x01c00005; //ByteVector/None/3
		public static final int MSG_SOURCEDATABEGIN_1_FIELD_FLAGS_103 = 0x00400007; //IntegerSigned/None/0
		public static final int MSG_SOURCEDATACONTINUATION_2 = 0x0000000e; //Group/OpenTempl/3
		public static final int MSG_SOURCEDATACONTINUATION_2_FIELD_DATA_101 = 0x01c00001; //ByteVector/None/3
		public static final int MSG_SOURCEDATACONTINUATION_2_FIELD_FLAGS_103 = 0x00400003; //IntegerSigned/None/0

		public static void consume(Pipe<SourceDataSchema> input) {
		    while (PipeReader.tryReadFragment(input)) {
		        int msgIdx = PipeReader.getMsgIdx(input);
		        switch(msgIdx) {
		            case MSG_MODULEBEGIN_3:
		                consumeModuleBegin(input);
		            break;
		            case MSG_MODULEEND_4:
		                consumeModuleEnd(input);
		            break;
		            case MSG_SOURCEDATABEGIN_1:
		                consumeSourceDataBegin(input);
		            break;
		            case MSG_SOURCEDATACONTINUATION_2:
		                consumeSourceDataContinuation(input);
		            break;
		            case -1:
		               //requestShutdown();
		            break;
		        }
		        PipeReader.releaseReadLock(input);
		    }
		}

		public static void consumeModuleBegin(Pipe<SourceDataSchema> input) {
		    StringBuilder fieldCannonicalName = PipeReader.readUTF8(input,MSG_MODULEBEGIN_3_FIELD_CANNONICALNAME_12,new StringBuilder(PipeReader.readBytesLength(input,MSG_MODULEBEGIN_3_FIELD_CANNONICALNAME_12)));
		    StringBuilder fieldFolderRoot = PipeReader.readUTF8(input,MSG_MODULEBEGIN_3_FIELD_FOLDERROOT_11,new StringBuilder(PipeReader.readBytesLength(input,MSG_MODULEBEGIN_3_FIELD_FOLDERROOT_11)));
		    DataInputBlobReader<SourceDataSchema> fieldMetaData = PipeReader.inputStream(input, MSG_MODULEBEGIN_3_FIELD_METADATA_13);
		}
		public static void consumeModuleEnd(Pipe<SourceDataSchema> input) {
		    StringBuilder fieldCannonicalName = PipeReader.readUTF8(input,MSG_MODULEEND_4_FIELD_CANNONICALNAME_12,new StringBuilder(PipeReader.readBytesLength(input,MSG_MODULEEND_4_FIELD_CANNONICALNAME_12)));
		}
		public static void consumeSourceDataBegin(Pipe<SourceDataSchema> input) {
		    StringBuilder fieldLocation = PipeReader.readUTF8(input,MSG_SOURCEDATABEGIN_1_FIELD_LOCATION_11,new StringBuilder(PipeReader.readBytesLength(input,MSG_SOURCEDATABEGIN_1_FIELD_LOCATION_11)));
		    StringBuilder fieldCannonicalName = PipeReader.readUTF8(input,MSG_SOURCEDATABEGIN_1_FIELD_CANNONICALNAME_12,new StringBuilder(PipeReader.readBytesLength(input,MSG_SOURCEDATABEGIN_1_FIELD_CANNONICALNAME_12)));
		    DataInputBlobReader<SourceDataSchema> fieldData = PipeReader.inputStream(input, MSG_SOURCEDATABEGIN_1_FIELD_DATA_101);
		    int fieldFlags = PipeReader.readInt(input,MSG_SOURCEDATABEGIN_1_FIELD_FLAGS_103);
		}
		public static void consumeSourceDataContinuation(Pipe<SourceDataSchema> input) {
		    DataInputBlobReader<SourceDataSchema> fieldData = PipeReader.inputStream(input, MSG_SOURCEDATACONTINUATION_2_FIELD_DATA_101);
		    int fieldFlags = PipeReader.readInt(input,MSG_SOURCEDATACONTINUATION_2_FIELD_FLAGS_103);
		}

		public static void publishModuleBegin(Pipe<SourceDataSchema> output, CharSequence fieldCannonicalName, CharSequence fieldFolderRoot, byte[] fieldMetaDataBacking, int fieldMetaDataPosition, int fieldMetaDataLength) {
		        PipeWriter.presumeWriteFragment(output, MSG_MODULEBEGIN_3);
		        PipeWriter.writeUTF8(output,MSG_MODULEBEGIN_3_FIELD_CANNONICALNAME_12, fieldCannonicalName);
		        PipeWriter.writeUTF8(output,MSG_MODULEBEGIN_3_FIELD_FOLDERROOT_11, fieldFolderRoot);
		        PipeWriter.writeBytes(output,MSG_MODULEBEGIN_3_FIELD_METADATA_13, fieldMetaDataBacking, fieldMetaDataPosition, fieldMetaDataLength);
		        PipeWriter.publishWrites(output);
		}
		public static void publishModuleEnd(Pipe<SourceDataSchema> output, CharSequence fieldCannonicalName) {
		        PipeWriter.presumeWriteFragment(output, MSG_MODULEEND_4);
		        PipeWriter.writeUTF8(output,MSG_MODULEEND_4_FIELD_CANNONICALNAME_12, fieldCannonicalName);
		        PipeWriter.publishWrites(output);
		}
		public static void publishSourceDataBegin(Pipe<SourceDataSchema> output, CharSequence fieldLocation, CharSequence fieldCannonicalName, byte[] fieldDataBacking, int fieldDataPosition, int fieldDataLength, int fieldFlags) {
		        PipeWriter.presumeWriteFragment(output, MSG_SOURCEDATABEGIN_1);
		        PipeWriter.writeUTF8(output,MSG_SOURCEDATABEGIN_1_FIELD_LOCATION_11, fieldLocation);
		        PipeWriter.writeUTF8(output,MSG_SOURCEDATABEGIN_1_FIELD_CANNONICALNAME_12, fieldCannonicalName);
		        PipeWriter.writeBytes(output,MSG_SOURCEDATABEGIN_1_FIELD_DATA_101, fieldDataBacking, fieldDataPosition, fieldDataLength);
		        PipeWriter.writeInt(output,MSG_SOURCEDATABEGIN_1_FIELD_FLAGS_103, fieldFlags);
		        PipeWriter.publishWrites(output);
		}
		public static void publishSourceDataContinuation(Pipe<SourceDataSchema> output, byte[] fieldDataBacking, int fieldDataPosition, int fieldDataLength, int fieldFlags) {
		        PipeWriter.presumeWriteFragment(output, MSG_SOURCEDATACONTINUATION_2);
		        PipeWriter.writeBytes(output,MSG_SOURCEDATACONTINUATION_2_FIELD_DATA_101, fieldDataBacking, fieldDataPosition, fieldDataLength);
		        PipeWriter.writeInt(output,MSG_SOURCEDATACONTINUATION_2_FIELD_FLAGS_103, fieldFlags);
		        PipeWriter.publishWrites(output);
		}
}