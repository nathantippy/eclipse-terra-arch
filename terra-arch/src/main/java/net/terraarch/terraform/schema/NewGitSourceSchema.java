package net.terraarch.terraform.schema;

import net.terraarch.pipe.FieldReferenceOffsetManager;
import net.terraarch.pipe.MessageSchema;
import net.terraarch.pipe.Pipe;
import net.terraarch.pipe.PipeReader;
import net.terraarch.pipe.PipeWriter;

public class NewGitSourceSchema extends MessageSchema<NewGitSourceSchema> {

	public final static FieldReferenceOffsetManager FROM = new FieldReferenceOffsetManager(
		    new int[]{0xc0400004,0xa8000000,0xa8000001,0xa8000002,0xc0200004},
		    (short)0,
		    new String[]{"Source","URL","Branch","TagId",null},
		    new long[]{1, 11, 12, 13, 0},
		    new String[]{"global",null,null,null,null},
		    "NewGitSource.xml",
		    new long[]{2, 2, 0},
		    new int[]{2, 2, 0});


		public NewGitSourceSchema() { 
		    super(FROM);
		}

		protected NewGitSourceSchema(FieldReferenceOffsetManager from) { 
		    super(from);
		}

		public static final NewGitSourceSchema instance = new NewGitSourceSchema();

		public static final int MSG_SOURCE_1 = 0x00000000; //Group/OpenTempl/4
		public static final int MSG_SOURCE_1_FIELD_URL_11 = 0x01400001; //UTF8/None/0
		public static final int MSG_SOURCE_1_FIELD_BRANCH_12 = 0x01400003; //UTF8/None/1
		public static final int MSG_SOURCE_1_FIELD_TAGID_13 = 0x01400005; //UTF8/None/2

		public static void consume(Pipe<NewGitSourceSchema> input) {
		    while (PipeReader.tryReadFragment(input)) {
		        int msgIdx = PipeReader.getMsgIdx(input);
		        switch(msgIdx) {
		            case MSG_SOURCE_1:
		                consumeSource(input);
		            break;
		            case -1:
		               //requestShutdown();
		            break;
		        }
		        PipeReader.releaseReadLock(input);
		    }
		}

		public static void consumeSource(Pipe<NewGitSourceSchema> input) {
		    StringBuilder fieldURL = PipeReader.readUTF8(input,MSG_SOURCE_1_FIELD_URL_11,new StringBuilder(PipeReader.readBytesLength(input,MSG_SOURCE_1_FIELD_URL_11)));
		    StringBuilder fieldBranch = PipeReader.readUTF8(input,MSG_SOURCE_1_FIELD_BRANCH_12,new StringBuilder(PipeReader.readBytesLength(input,MSG_SOURCE_1_FIELD_BRANCH_12)));
		    StringBuilder fieldTagId = PipeReader.readUTF8(input,MSG_SOURCE_1_FIELD_TAGID_13,new StringBuilder(PipeReader.readBytesLength(input,MSG_SOURCE_1_FIELD_TAGID_13)));
		}

		public static void publishSource(Pipe<NewGitSourceSchema> output, CharSequence fieldURL, CharSequence fieldBranch, CharSequence fieldTagId) {
		        PipeWriter.presumeWriteFragment(output, MSG_SOURCE_1);
		        PipeWriter.writeUTF8(output,MSG_SOURCE_1_FIELD_URL_11, fieldURL);
		        PipeWriter.writeUTF8(output,MSG_SOURCE_1_FIELD_BRANCH_12, fieldBranch);
		        PipeWriter.writeUTF8(output,MSG_SOURCE_1_FIELD_TAGID_13, fieldTagId);
		        PipeWriter.publishWrites(output);
		}
}