package net.terraarch.pipe;

public class MessageSchemaDynamic extends MessageSchema<MessageSchemaDynamic> {

	public MessageSchemaDynamic() {
        super(null);
    }
	
    public MessageSchemaDynamic(FieldReferenceOffsetManager from) {
        super(from);
    }

}
