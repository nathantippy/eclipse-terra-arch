package net.terraarch.tf.parse.doc;

public enum TypeColors {
	
	COMMENT(         163,163,163, TypeStyle.STYLE_ITALIC),
	BLOCK(           255, 56,141, TypeStyle.STYLE_NONE),
	BLOCK_LABEL(     255,104,169, TypeStyle.STYLE_BOLD),
	
	IDENT_ASSIGN(    255,206, 71, TypeStyle.STYLE_BOLD),	
	IDENT_DEF(       255,206, 71, TypeStyle.STYLE_BOLD), 
	
	FUNCTION(        255,116,56, TypeStyle.STYLE_BOLD),
	OPERATION(       255,116,56, TypeStyle.STYLE_NONE),	
	
	TEXT(             40,240,  0, TypeStyle.STYLE_NONE),
	
	HERE_DOC(         40,240,  0, TypeStyle.STYLE_NONE),
	
	IDENT_NAMESPACE(  0,100,255, TypeStyle.STYLE_NONE),
	IDENT_CATIGORY(  56,138,255, TypeStyle.STYLE_NONE),
	IDENT_USAGE(      56,138,255, TypeStyle.STYLE_BOLD),
	IDENT_DETAIL(     89,176,255, TypeStyle.STYLE_BOLD),
	
	I_TEXT(          208, 61,255, TypeStyle.STYLE_NONE),     // Terraform purple is: 98,60,228 (avoid)
	NUMBER_OR_LITERAL( 0+64,96+64,64+64, TypeStyle.STYLE_NONE),
	UNDEFINED(         255,  0,  0, TypeStyle.STYLE_BOLD) {
		public TypeColors self() {
			//for debugging only, new Exception("we found something undefined").printStackTrace();
	    	return this;
	    }
	},
		
	RAINBOW_BASE( TypeColors.RAINBOW_COLOR_RED_FLOOR, 255-(TypeColors.RAINBOW_COLOR_RANGE), 255, TypeStyle.STYLE_NONE  )
		
	;

    public final int red;
    public final int green;
    public final int blue;
    public final int style;
    
	public static final int RAINBOW_COLOR_STEP      = 22;//exact multiple allows us to get the dark blue
	public static final int RAINBOW_COLOR_RANGE     = RAINBOW_COLOR_STEP*8; // 176; 
	public static final int RAINBOW_COLOR_RED_FLOOR = 90;
    

	
	
	
	
    public TypeColors self() {
    	return this;
    }
    
    TypeColors(int r, int g, int b, int style) {
    	this.red = r;
    	this.green = g;
    	this.blue = b;
    	this.style = style;
    }
  
    
    
}