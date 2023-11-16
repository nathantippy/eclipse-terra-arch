package net.terraarch.terraform.schema;

import net.terraarch.pipe.FieldReferenceOffsetManager;
import net.terraarch.pipe.MessageSchema;
import net.terraarch.pipe.Pipe;
import net.terraarch.pipe.PipeReader;
import net.terraarch.pipe.PipeWriter;

public class NewSourceRequestSchema extends MessageSchema<NewSourceRequestSchema> {

	public final static FieldReferenceOffsetManager FROM = new FieldReferenceOffsetManager(
		    new int[]{0xc0400002,0xa8000000,0xc0200002,0xc0400002,0xa8000000,0xc0200002},
		    (short)0,
		    new String[]{"Request","CanonicalURI",null,"Confirmation","CanonicalURI",null},
		    new long[]{1, 99, 0, 2, 99, 0},
		    new String[]{"global",null,null,"global",null,null},
		    "NewSourceRequest.xml",
		    new long[]{2, 2, 0},
		    new int[]{2, 2, 0});


		public NewSourceRequestSchema() { 
		    super(FROM);
		}

		protected NewSourceRequestSchema(FieldReferenceOffsetManager from) { 
		    super(from);
		}

		public static final NewSourceRequestSchema instance = new NewSourceRequestSchema();

		public static final int MSG_REQUEST_1 = 0x00000000; //Group/OpenTempl/2
		public static final int MSG_REQUEST_1_FIELD_CANONICALURI_99 = 0x01400001; //UTF8/None/0
		public static final int MSG_CONFIRMATION_2 = 0x00000003; //Group/OpenTempl/2
		public static final int MSG_CONFIRMATION_2_FIELD_CANONICALURI_99 = 0x01400001; //UTF8/None/0

		public static void consume(Pipe<NewSourceRequestSchema> input) {
		    while (PipeReader.tryReadFragment(input)) {
		        int msgIdx = PipeReader.getMsgIdx(input);
		        switch(msgIdx) {
		            case MSG_REQUEST_1:
		                consumeRequest(input);
		            break;
		            case MSG_CONFIRMATION_2:
		                consumeConfirmation(input);
		            break;
		            case -1:
		               //requestShutdown();
		            break;
		        }
		        PipeReader.releaseReadLock(input);
		    }
		}

		public static void consumeRequest(Pipe<NewSourceRequestSchema> input) {
		    StringBuilder fieldCanonicalURI = PipeReader.readUTF8(input,MSG_REQUEST_1_FIELD_CANONICALURI_99,new StringBuilder(PipeReader.readBytesLength(input,MSG_REQUEST_1_FIELD_CANONICALURI_99)));
		}
		public static void consumeConfirmation(Pipe<NewSourceRequestSchema> input) {
		    StringBuilder fieldCanonicalURI = PipeReader.readUTF8(input,MSG_CONFIRMATION_2_FIELD_CANONICALURI_99,new StringBuilder(PipeReader.readBytesLength(input,MSG_CONFIRMATION_2_FIELD_CANONICALURI_99)));
		}

		public static void publishRequest(Pipe<NewSourceRequestSchema> output, CharSequence fieldCanonicalURI) {
		        PipeWriter.presumeWriteFragment(output, MSG_REQUEST_1);
		        PipeWriter.writeUTF8(output,MSG_REQUEST_1_FIELD_CANONICALURI_99, fieldCanonicalURI);
		        PipeWriter.publishWrites(output);
		}
		public static void publishConfirmation(Pipe<NewSourceRequestSchema> output, CharSequence fieldCanonicalURI) {
		        PipeWriter.presumeWriteFragment(output, MSG_CONFIRMATION_2);
		        PipeWriter.writeUTF8(output,MSG_CONFIRMATION_2_FIELD_CANONICALURI_99, fieldCanonicalURI);
		        PipeWriter.publishWrites(output);
		}

}