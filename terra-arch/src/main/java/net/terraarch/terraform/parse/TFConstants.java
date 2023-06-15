package net.terraarch.terraform.parse;

import static net.terraarch.util.TrieParserReader.parseHasContent;

import java.util.HashMap;
import java.util.Map;

import net.terraarch.util.TrieParser;
import net.terraarch.util.TrieParserReader;

/*
Recursive decent parser designed to support continuations
this allows us to start parsing a file before all of it is loaded
then return to where we left off when more data becomes available.
Giant files larger than memory can be parsed using this technique.

This will parse HCL 
https://github.com/hashicorp/hcl/blob/hcl2/hclsyntax/spec.md

*/
public class TFConstants {
	
	/////////////////////////
	//used literals
	/////////////////////////
	
	static final byte[] ws1 = new byte[]{0x20}; //Space
	static final byte[] ws2 = new byte[]{0x09}; //Horizontal tab
	
	
	

	

	
//	ConfigFile   = Body;
//	Body         = (Attribute | Block | OneLineBlock)*;
//	Attribute    = Identifier "=" Expression Newline;
//	Block        = Identifier (StringLit|Identifier)* "{" Newline Body "}" Newline;
//	OneLineBlock = Identifier (StringLit|Identifier)* "{" (Identifier "=" Expression)? "}" Newline;
	
//	Identifier = ID_Start (ID_Continue | '-')*;

	
//    \n         Unicode newline control character
//    \r         Unicode carriage return control character
//    \t         Unicode tab control character
//    \"         Literal quote mark, used to prevent interpretation as end of string
//    \\         Literal backslash, used to prevent interpretation as escape sequence
//    \u00FF     Unicode character from Basic Multilingual Plane (NNNN is four hexadecimal digits)
//    \UNNNNNNNN Unicode character from supplementary planes (NNNNNNNN is eight hexadecimal digits)
//   StringLit = '"' (quoted literals as defined in prose above) '"';
	
//	TemplateExpr = quotedTemplate | heredocTemplate;
//	quotedTemplate = (as defined in prose above);
//	heredocTemplate = (
//	    ("<<" | "<<-") Identifier Newline
//	    (content as defined in prose above)
//	    Identifier Newline
//	);
	
//	Expression = (
//		    ExprTerm |
//		    Operation |
//		    Conditional
//		);
	
//	LiteralValue = (
//			  NumericLit |
//			  "true" |
//			  "false" |
//			  "null"
//			);
	
//	NumericLit = decimal+ ("." decimal+)? (expmark decimal+)?;
//	decimal    = '0' .. '9';
//	expmark    = ('e' | 'E') ("+" | "-")?;
	
//	FunctionCall = Identifier "(" arguments ")";
//	Arguments = (
//	    () ||
//	    (Expression ("," Expression)* ("," | "...")?)
//	);
	
//	ForExpr = forTupleExpr | forObjectExpr;
//	forTupleExpr = "[" forIntro Expression forCond? "]";
//	forObjectExpr = "{" forIntro Expression "=>" Expression "..."? forCond? "}";
//	forIntro = "for" Identifier ("," Identifier)? "in" Expression ":";
//	forCond = "if" Expression;
	
//	Splat = attrSplat | fullSplat;
//	attrSplat = "." "*" GetAttr*;
//	fullSplat = "[" "*" "]" (GetAttr | Index)*;
	
//	ExprTerm = (
//		    LiteralValue |          0-9 true, false null
//		    CollectionValue |       [  {
//		    TemplateExpr |           <<  <<-  "
//		    VariableExpr |          ident
//		    FunctionCall |          ident ( args )
//		    ForExpr |               [ for   { for
//	
//		    ExprTerm Index |        ExprTerm [xpr ]         //this is a follow on
//		    ExprTerm GetAttr |      ExprTerm  .Identifier   //this is a follow on
//		    ExprTerm Splat |        ExprTerm  .*  [*]       //this is a follow on 
//		    "(" Expression ")"
//		);
	
//	CollectionValue = tuple | object;
//	tuple = "[" (
//	    (Expression ("," Expression)* ","?)?
//	) "]";
//	object = "{" (
//	    (objectelem ("," objectelem)* ","?)?
//	) "}";
//	objectelem = (Identifier | Expression) "=" Expression;
	
//	Operation = unaryOp | binaryOp;
//	unaryOp = ("-" | "!") ExprTerm;
//	binaryOp = ExprTerm binaryOperator ExprTerm;
//	binaryOperator = compareOperator | arithmeticOperator | logicOperator;
//	compareOperator = "==" | "!=" | "<" | ">" | "<=" | ">=";
//	arithmeticOperator = "+" | "-" | "*" | "/" | "%";
//	logicOperator = "&&" | "||" | "!";
	
//	Conditional = Expression "?" Expression ":" Expression;
	
	//NOTE: this is the top where it all starts.
	
	private static TrieNext trieTop;	
	public static TrieNext trieTop() {
		if (trieTop!=null) {
			return trieTop;
		} else {
			TrieParser trie = baseParser();
			populateWithWhiteSpace(trie);
			int ordinal = NodeProcessor.IdentifierThenBlockOrAssignmentChar.ordinal();			
			//Positive selection that one of these have happened
			trie.setValue(TFConstants.b("\""), ordinal);         //" enclosure  "			
			trie.setValue(TFConstants.b("'"),  ordinal);         //' enclosure  '	
		
			TFConstants.populateIdentifierAccepts(trie, NodeProcessor.IdentifierThenBlockOrAssignmentChar.ordinal());
			populateWithComments(trie);
						
			trieTop = new TrieNext(trie);//, NodeProcessor.IdentifierThenBlockOrAssignment.ordinal());
			return trieTop;
		}
	}
	
	private static TrieNext trieTemplate;	
	public static TrieNext trieTemplate() {
		if (trieTemplate!=null) {
			return trieTemplate;
		} else {
			
			TrieParser trie = new TrieParser(256, 1, false, false, false);
			
			trie.setValue(b("$"), NodeProcessor.TextStopInterpolate.ordinal()); //  ${  $${
			trie.setValue(b("%"), NodeProcessor.TextStopIterate.ordinal());   //    %{  %%{	
									
			trieTemplate = new TrieNext(trie,  NodeProcessor.TextAccept.ordinal());
			return trieTemplate;
		}
	}
	
	
	private final static byte[] b(String t) {
		return t.getBytes();
	}
	private final static byte[] b(char c)  {
		return new byte[]{(byte)c};
	}
	
	private static TrieNext trieSingleItem;	
	public static TrieNext trieSingleItem() {
		if (trieSingleItem!=null) {
			return trieSingleItem;
		} else {
			TrieParser trie = populateWithSpaceAndComments(baseParser());	//space is the most important and MUST be first
			//a comma inside a block has no known meaning and will be ignored
			trie.setValue(",".getBytes(), CONTINUE_FLAG | NodeProcessor.NoOp.ordinal());
			trie.setValue("for ".getBytes(), NodeProcessor.ForExpr.ordinal());
			trie.setValue("for\n".getBytes(), NodeProcessor.ForExpr.ordinal());
			trie.setValue("}".getBytes(), NodeProcessor.MoveBackOne.ordinal());
									
			//Positive selection that one of these have happened
			int ordinal = NodeProcessor.IdentifierThenBlockOrAssignmentChar.ordinal();
			trie.setValue(TFConstants.b("!"),  ordinal);  //! trieExpression02
			trie.setValue(TFConstants.b("-"),  ordinal); 	//-	trieExpression02
			trie.setValue(TFConstants.b("["),  ordinal);         //[ enclosure  , ]		
			trie.setValue(TFConstants.b("{"),  ordinal);          //{ enclosure  }
			trie.setValue(TFConstants.b("("),  ordinal);          //( enclosure  ) 
			trie.setValue(TFConstants.b("\""), ordinal);         //" enclosure  "			
			trie.setValue(TFConstants.b("'"),  ordinal);          //' enclosure  '	

			TFConstants.populateIdentifierAccepts(trie, NodeProcessor.IdentifierThenBlockOrAssignmentChar.ordinal());
			
			trieSingleItem = new TrieNext(trie);
			return trieSingleItem;
		}
	}

	private static TrieNext trieLabelContinueOrBlockOrAssign;
	public static TrieNext trieLabelContinueOrBlockOrAssign() {
		if (trieLabelContinueOrBlockOrAssign!=null) {
			return trieLabelContinueOrBlockOrAssign;
		} else {
			TrieParser trie = populateWithSpaceAndComments(baseParser());	
			trie.setValue("=".getBytes(), NodeProcessor.Assignment.ordinal());  
			trie.setValue(":".getBytes(), NodeProcessor.Tuple.ordinal()); 
			
			
			trie.setValue("{".getBytes(), NodeProcessor.ContinueBlockLabelChar.ordinal()); 
			//      ContinueBlockLabel
			trie.setValue("\"".getBytes(),  NodeProcessor.ContinueBlockLabelChar.ordinal());         //" enclosure  "			
			trie.setValue("'".getBytes(),   NodeProcessor.ContinueBlockLabelChar.ordinal());
								
			//Identifier accumulators
			trie.setValue(new byte[]{(byte)'-'}, NodeProcessor.ContinueBlockLabelChar.ordinal());
			trie.setValue(new byte[]{(byte)'_'}, NodeProcessor.ContinueBlockLabelChar.ordinal());
			trie.setValue(new byte[]{(byte)'*'}, NodeProcessor.ContinueBlockLabelChar.ordinal());
			
			for(int j='0'; j<='9'; j++) {
				trie.setValue(new byte[]{(byte)j}, NodeProcessor.ContinueBlockLabelChar.ordinal());
			}
			for(int j='a'; j<='z'; j++) {
				trie.setValue(new byte[]{(byte)j}, NodeProcessor.ContinueBlockLabelChar.ordinal());
			}
			for(int j='A'; j<='Z'; j++) {
				trie.setValue(new byte[]{(byte)j}, NodeProcessor.ContinueBlockLabelChar.ordinal());
			}
			

			
			trieLabelContinueOrBlockOrAssign = new TrieNext(trie);
			return trieLabelContinueOrBlockOrAssign;
		}
	}

	private static TrieNext trieLableContinueOrBlock;
	public static TrieNext trieLableContinueOrBlock() {
		if (trieLableContinueOrBlock!=null) {
			return trieLableContinueOrBlock;
		} else {
			TrieParser trie = populateWithSpaceAndComments(baseParser());			
			trie.setValue("{".getBytes(), NodeProcessor.BlockOpen.ordinal());  
			trieLableContinueOrBlock = new TrieNext(trie, NodeProcessor.NoOp.ordinal());//  MoveBackOne.ordinal());
			return trieLableContinueOrBlock;
		}
	}
	
	
	private static TrieNext trieNumber;
	public static TrieNext trieNumber() {	//every expression contains binary operator follow ons, no need to add.
		if (null!=trieNumber) {
			return trieNumber;
		} else {
			TrieParser trie = new TrieParser(256, 1, false, true, false, (byte)'@');
			//must check decimal first
			trie.setValue("@i@.".getBytes(), NodeProcessor.NumericLiteralDecimal.ordinal());   
			trie.setValue("@i".getBytes(), NodeProcessor.NumericLiteralInteger.ordinal()); 
			
			trieNumber = new TrieNext(trie);
			return trieNumber;
		}
	}
	
	/**
	 * TODO: need a generator to to build this from BNF but discovering every possible value at each point..
	 */
	
	private static TrieNext trieExpressionTop;
	public static TrieNext trieExpressionTop() {	//every expression contains binary operator follow ons, no need to add.
		if (null!=trieExpressionTop) {
			return trieExpressionTop;
		} else {			
						
			TrieParser trie = baseParser();
			populateWithWhiteSpace(trie);			

			for(int j='0'; j<='9'; j++) {
				trie.setValue(new byte[]{(byte)j}, NodeProcessor.NumericLiteral.ordinal());
			}
			
			//prefixes
			trie.setValue("!".getBytes(), NodeProcessor.OperNotExpression.ordinal());  //! trieExpression02
			trie.setValue("-".getBytes(), NodeProcessor.OperSignToggle.ordinal()); 	//-	trieExpression02
			
			trie.setValue("<<@b\n".getBytes(), NodeProcessor.HereDoc.ordinal());            //atomic - must fit in pipe
			trie.setValue("<<-@b\n".getBytes(), NodeProcessor.HereDoc.ordinal());           //atomic - must fit in pipe
			trie.setValue(" <<@b\n".getBytes(), NodeProcessor.HereDoc.ordinal());           //atomic - must fit in pipe, added for testing
			trie.setValue(" <<-@b\n".getBytes(), NodeProcessor.HereDoc.ordinal());          //atomic - must fit in pipe, added for testing
					
			trie.setValue("[".getBytes(), NodeProcessor.ArrayOpen.ordinal());         //[ enclosure  , ]		
			trie.setValue("{".getBytes(), NodeProcessor.BlockOpen.ordinal());          //{ enclosure  }
			trie.setValue("(".getBytes(), NodeProcessor.OpenParen.ordinal());          //( enclosure  ) 
					
			trie.setValue("\"".getBytes(), NodeProcessor.BeginTextDoubleQuote.ordinal());         //" enclosure  "			
			trie.setValue("'".getBytes(), NodeProcessor.BeginTextSingleQuote.ordinal());          //' enclosure  '			
			
			trie.setValue("for ".getBytes(), NodeProcessor.ForExpr.ordinal());
			trie.setValue("for\n".getBytes(), NodeProcessor.ForExpr.ordinal());
										
			populateWithComments(trie);
			populateWithIdentStart(trie);
			
			trie.setValue(new byte[]{(byte)')'}, NodeProcessor.MoveBackOne.ordinal());
			trie.setValue(new byte[]{(byte)']'}, NodeProcessor.MoveBackOne.ordinal());			
			trie.setValue(new byte[]{(byte)','}, NodeProcessor.MoveBackOne.ordinal());
			trie.setValue(new byte[]{(byte)'}'}, NodeProcessor.MoveBackOne.ordinal());			
			
			trieExpressionTop = new TrieNext(trie);
			return trieExpressionTop;
		}
	}


	private static void populateWithIdentStart(TrieParser trie) {
		//Ident starts:
		trie.setValue(new byte[]{(byte)'_'}, NodeProcessor.IdentifierTopChar.ordinal());
		trie.setValue(new byte[]{(byte)'*'}, NodeProcessor.IdentifierTopChar.ordinal());			
		for(int j='a'; j<='z'; j++) {
			trie.setValue(new byte[]{(byte)j}, NodeProcessor.IdentifierTopChar.ordinal());
		}
		for(int j='A'; j<='Z'; j++) {
			trie.setValue(new byte[]{(byte)j}, NodeProcessor.IdentifierTopChar.ordinal());
		}
	}

	private static TrieNext trieIndexExpression;
	public static TrieNext trieIndexExpression() {	//every expression contains binary operator follow ons, no need to add.
		if (null!=trieIndexExpression) {
			return trieIndexExpression;
		} else {			
						
			TrieParser trie = baseParser();
			populateWithWhiteSpace(trie);			

			for(int j='0'; j<='9'; j++) {
				trie.setValue(new byte[]{(byte)j}, NodeProcessor.NumericLiteral.ordinal());
			}
			
			//prefixes
			trie.setValue("!".getBytes(), NodeProcessor.OperNotExpression.ordinal());  //! trieExpression02
			trie.setValue("-".getBytes(), NodeProcessor.OperSignToggle.ordinal()); 	//-	trieExpression02
					
			trie.setValue("[".getBytes(), NodeProcessor.ArrayOpen.ordinal());         //[ enclosure  , ]		
			trie.setValue("{".getBytes(), NodeProcessor.BlockOpen.ordinal());          //{ enclosure  }
			trie.setValue("(".getBytes(), NodeProcessor.OpenParen.ordinal());          //( enclosure  ) 
					
			trie.setValue("\"".getBytes(), NodeProcessor.BeginTextDoubleQuote.ordinal());         //" enclosure  "			
			trie.setValue("'".getBytes(), NodeProcessor.BeginTextSingleQuote.ordinal());          //' enclosure  '			
							
			populateWithComments(trie);
			populateWithIdentStart(trie);
					
			
			trieIndexExpression = new TrieNext(trie);
			return trieIndexExpression;
		}
	}


	//("E+%i", "E-%i", "e+%i", "e-%i")
	private static TrieNext trieScientificContinueOrEnd;
	public static TrieNext trieScientificContinueOrEnd() {
		if (null != trieScientificContinueOrEnd) {
			return trieScientificContinueOrEnd;
		} else {
			TrieParser trie = baseParser();
			populateWithWhiteSpace(trie);
						
			trie.setValue("E+@i".getBytes(), NodeProcessor.NumericSciUpperPlus.ordinal());
			trie.setValue("E-@i".getBytes(), NodeProcessor.NumericSciUpperMinus.ordinal());
			trie.setValue("e+@i".getBytes(), NodeProcessor.NumericSciLowerPlus.ordinal());
			trie.setValue("e-@i".getBytes(), NodeProcessor.NumericSciLowerMinus.ordinal());

			trieScientificContinueOrEnd = new TrieNext(trie, NodeProcessor.NumericBinaryOpFollowOn.ordinal());
			return trieScientificContinueOrEnd;
		}
	}
	
	
	private static TrieNext trieArrayContinueOrEnd;
	public static TrieNext trieArrayContinueOrEnd() {
		if (null != trieArrayContinueOrEnd) {
			return trieArrayContinueOrEnd;
		} else {
			TrieParser trie = baseParser();
			populateWithWhiteSpace(trie);			
			populateWithComments(trie);	
			trie.setValue("]".getBytes(),   NodeProcessor.ArrayClose.ordinal());  //      ]
			trie.setValue(",".getBytes(),   NodeProcessor.ArrayContinue.ordinal());//     ,
			trieArrayContinueOrEnd = new TrieNext(trie);
			return trieArrayContinueOrEnd;
		}
	}
	
	
	
	private static TrieNext trieParenClose;
	public static TrieNext trieParaClose() {		
		if (null != trieParenClose) {
			return trieParenClose;
		} else {			
			TrieParser trie = populateWithSpaceAndComments(baseParser());
			trie.setValue(")".getBytes(), NodeProcessor.CloseParen.ordinal());  //  )
			trieParenClose = new TrieNext(trie);
			return trieParenClose;	    	
		}
	}
		
	
	private static TrieNext trieFunctionContinueOrEnd;
	public static TrieNext trieFunctionContinueOrEnd() {
		if (null != trieFunctionContinueOrEnd) {
			return trieFunctionContinueOrEnd;
		} else {
			TrieParser trie = populateWithSpaceAndComments(baseParser());
			trie.setValue(",".getBytes(),       NodeProcessor.FunctionContinue.ordinal()); //  ,  this must be first
			trie.setValue("...)".getBytes(),    NodeProcessor.FunctionVarEnd.ordinal());   //  ...)
			trie.setValue(")".getBytes(),       NodeProcessor.FunctionEnd.ordinal());      //  )
			trieFunctionContinueOrEnd = new TrieNext(trie);
			return trieFunctionContinueOrEnd;
		}
	}
	
	private static TrieNext trieTernThird;
	public static TrieNext trieTernThird() {
		if (null != trieTernThird) {
			return trieTernThird;
		} else {
			TrieParser trie = populateWithSpaceAndComments(baseParser());
			trie.setValue(":".getBytes(),    NodeProcessor.TernThird.ordinal()); 
			trieTernThird = new TrieNext(trie);
	    	return trieTernThird;
		}
	}

//	
//	private static TrieNext trieIdentBegin;
//	public static TrieNext trieIdentBegin() {
//		if (null != trieIdentBegin) {
//			return trieIdentBegin;
//		} else {
//			TrieParser trie = populateWithSpaceAndComments(baseParser());
//			trie.setValue("[".getBytes(),    NodeProcessor.IdentifierIdx.ordinal());
//			trie.setValue(".".getBytes(),    NodeProcessor.IdentifierGet.ordinal()); 
//			trieIdentBegin = new TrieNext(trie);
//	    	return trieIdentBegin;
//		}
//	}
	
	
	private static TrieNext trieIdentFollowOn;
	public static TrieNext trieIdentFollowOn() {
		if (null != trieIdentFollowOn) {
			return trieIdentFollowOn;
		} else {
			TrieParser trie = populateWithSpaceAndComments(baseParser());
			trie.setValue("[".getBytes(),    NodeProcessor.IdentifierIdx.ordinal());
			trie.setValue("...".getBytes(),  NodeProcessor.MoveBackThree.ordinal());
			trie.setValue(".".getBytes(),    NodeProcessor.IdentifierGet.ordinal());
			populateBinaryOp(trie);
			trieIdentFollowOn = new TrieNext(trie, NodeProcessor.NoOp.ordinal());
	    	return trieIdentFollowOn;
		}
	}
	

	private static TrieNext trieFunctionFollowOn;
	public static TrieNext trieFunctionFollowOn() {
		if (null != trieFunctionFollowOn) {
			return trieFunctionFollowOn;
		} else {
			TrieParser trie = populateWithSpaceAndComments(baseParser());
			trie.setValue("[".getBytes(),    NodeProcessor.IdentifierIdx.ordinal());
			trie.setValue("...".getBytes(),  NodeProcessor.MoveBackThree.ordinal());
			trie.setValue(".".getBytes(),    NodeProcessor.IdentifierGet.ordinal());
			populateBinaryOp(trie);
			trieFunctionFollowOn = new TrieNext(trie, NodeProcessor.NoOp.ordinal());
	    	return trieFunctionFollowOn;
		}
	}
	
	private static TrieNext trieIndexFollowOn;
	public static TrieNext trieIndexFollowOn() {
		if (null != trieIndexFollowOn) {
			return trieIndexFollowOn;
		} else {
			TrieParser trie = populateWithSpaceAndComments(baseParser());
			trie.setValue("[".getBytes(),    NodeProcessor.IdentifierIdx.ordinal());
			trieIndexFollowOn = new TrieNext(trie, NodeProcessor.NoOp.ordinal());
	    	return trieIndexFollowOn;
		}
	}
	
	
	private static TrieNext trieIdxClose;
	public static TrieNext trieIdxClose() {
		if (null != trieIdxClose) {
			return trieIdxClose;
		} else {
			TrieParser trie = populateWithSpaceAndComments(baseParser());
			trie.setValue("]".getBytes(), NodeProcessor.IdentifierCloseIdx.ordinal());  //  ]
			trieIdxClose = new TrieNext(trie);
			return trieIdxClose;
		}
	}
	
	
	private static TrieNext trieBlockClose;
	public static TrieNext trieBlockContinueOrClose() {
		if (null != trieBlockClose) {
			return trieBlockClose;
		} else {			
			TrieParser trie = baseParser();
			populateWithWhiteSpace(trie);
			
			populateWithComments(trie);
			trie.setValue("}".getBytes(), NodeProcessor.BlockClose.ordinal());  //  }
			trie.setValue(",".getBytes(), NodeProcessor.BlockItemChar.ordinal());
			int ordinal = NodeProcessor.BlockItemChar.ordinal();  //  ,
			
			//Positive selection that one of these have happened
			trie.setValue(TFConstants.b("["),  ordinal);         //[ enclosure  , ]		
			trie.setValue(TFConstants.b("{"),  ordinal);          //{ enclosure  }
			trie.setValue(TFConstants.b("("),  ordinal);          //( enclosure  ) 
			trie.setValue(TFConstants.b("\""), ordinal);         //" enclosure  "			
			trie.setValue(TFConstants.b("'"),  ordinal);          //' enclosure  '	
			trie.setValue(new byte[]{(byte)'-'}, NodeProcessor.BlockItemChar.ordinal());
			trie.setValue(new byte[]{(byte)'_'}, NodeProcessor.BlockItemChar.ordinal());
					
			trie.setValue(new byte[]{(byte)'*'}, NodeProcessor.BlockItemChar.ordinal());
			
			for(int j='0'; j<='9'; j++) { //req for tuples
				trie.setValue(new byte[]{(byte)j}, NodeProcessor.BlockItemChar.ordinal());
			}
			for(int j='a'; j<='z'; j++) {
				trie.setValue(new byte[]{(byte)j}, NodeProcessor.BlockItemChar.ordinal());
			}
			for(int j='A'; j<='Z'; j++) {
				trie.setValue(new byte[]{(byte)j}, NodeProcessor.BlockItemChar.ordinal());
			}
									
			trieBlockClose = new TrieNext(trie);
			return trieBlockClose;			
		}
	}

	private static void populateWhiteSpace(TrieParser trie) {
		trie.setValue(TFConstants.ws1, CONTINUE_FLAG | NodeProcessor.WhiteSpace.ordinal());
		trie.setValue(TFConstants.ws2, CONTINUE_FLAG | NodeProcessor.WhiteSpace.ordinal());
		trie.setValue(new byte[]{'\r'}, CONTINUE_FLAG | NodeProcessor.WhiteSpace.ordinal());
	}

	private static void populateWithComments(TrieParser trie) {
		
		trie.setValue("#".getBytes(), CONTINUE_FLAG | NodeProcessor.SingleLineComment.ordinal());
		trie.setValue("/*".getBytes(), CONTINUE_FLAG | NodeProcessor.MultiLineComment.ordinal());
		trie.setValue("//".getBytes(), CONTINUE_FLAG |  NodeProcessor.SingleLineComment.ordinal());
	
	}
	
	private static TrieNext[] trieTextClose = new TrieNext[3];
	public static TrieNext trieTextClose(byte[] bytes) {		
    			
		int idx = bytes[0]&1;
		
		if (bytes.length==1 && null != trieTextClose[idx]) {
    		return trieTextClose[idx];
    	} else {			
			TrieParser trie = populateWithSpaceAndComments(baseParser());
						
			trie.setValue(bytes, NodeProcessor.EndQuote.ordinal());
			
			if (bytes.length==1) {
				trieTextClose[idx] = new TrieNext(trie);
				return trieTextClose[idx];
			} else {
				return new TrieNext(trie);
			}
			
    	}    	
	}

	
	
	private static TrieNext trieInterpClose;
	public static TrieNext trieInterpClose() {		
    	if (null != trieInterpClose) {
    		return trieInterpClose;
    	} else {			
			TrieParser trie = populateWithSpaceAndComments(baseParser());			
			trie.setValue("}".getBytes(), NodeProcessor.CloseIntrp.ordinal());  //  }
			trie.setValue("~}".getBytes(), NodeProcessor.CloseIntrp.ordinal());  //  ~}
			trieInterpClose = new TrieNext(trie);
			return trieInterpClose;
    	}
	}
	
		
	private static TrieNext trieBinaryOp;
	public static TrieNext trieBinaryOp() {		
    	if (null != trieBinaryOp) {
    		return trieBinaryOp;
    	} else {
			TrieParser trie = baseParser();
			populateWithWhiteSpace(trie);
			
			//TODO: JJ, due to bug in trie parser the scan for "/" must come before "/*" or it is lost
			//      Need to build test case for this and fix the parser.
			populateBinaryOp(trie);  
            //see above comment			
			populateWithComments(trie);
			
			trieBinaryOp = new TrieNext(trie, NodeProcessor.EndExpr.ordinal());
			return trieBinaryOp;
    	}
	}

	private static TrieNext trieTextFolllowOn;
	public static TrieNext trieTextFolllowOn() {		
    	if (null != trieTextFolllowOn) {
    		return trieTextFolllowOn;
    	} else {
			TrieParser trie = baseParser();
			populateWhiteSpace(trie);
			trie.setValue("/*".getBytes(), CONTINUE_FLAG | NodeProcessor.MultiLineComment.ordinal());
			
			populateCompareOps(trie);  
			trie.setValue("?".getBytes(), NodeProcessor.TernCond.ordinal());            

			trieTextFolllowOn = new TrieNext(trie, NodeProcessor.EndExpr.ordinal());
			return trieTextFolllowOn;
    	}
	}
	
	private static void populateWithWhiteSpace(TrieParser trie) {
		
		populateWhiteSpace(trie);
		trie.setValue(new byte[]{'\n'}, CONTINUE_FLAG | NodeProcessor.IncLineNo.ordinal());

	}
	
	/////////////////////////
	/////////////////////////


	private static void populateBinaryOp(TrieParser trie) {
		populateCompareOps(trie);  
		trie.setValue("&&".getBytes(), NodeProcessor.LogicalAnd.ordinal());  
		trie.setValue("||".getBytes(), NodeProcessor.LogicalOr.ordinal());  
		trie.setValue("/".getBytes(), NodeProcessor.ArithmeticDivide.ordinal());  
		trie.setValue("*".getBytes(), NodeProcessor.ArithmeticMultiply.ordinal());  
		trie.setValue("%".getBytes(), NodeProcessor.ArithmeticMod.ordinal());
		trie.setValue("+".getBytes(), NodeProcessor.ArithmeticAdd.ordinal());  
		trie.setValue("-".getBytes(), NodeProcessor.ArithmeticSubtract.ordinal());
		
		//not binary but used in the same places
		trie.setValue("?".getBytes(), NodeProcessor.TernCond.ordinal());
	}

	private static void populateCompareOps(TrieParser trie) {
		trie.setValue("==".getBytes(), NodeProcessor.CompareEquals.ordinal());  
		trie.setValue("!=".getBytes(), NodeProcessor.CompareNotEquals.ordinal());  
		trie.setValue(">=".getBytes(), NodeProcessor.CompareGreaterOrEqual.ordinal());  
		trie.setValue("<=".getBytes(), NodeProcessor.CompareLessthanOrEqual.ordinal());  
		trie.setValue(">".getBytes(), NodeProcessor.CompareGreater.ordinal());  
		trie.setValue("<".getBytes(), NodeProcessor.CompareLessthan.ordinal());
	}

	private static TrieNext trieForIdentifierOrString;
	
	public static TrieNext trieForIdentifierOrString() {
		
		if (null!=trieForIdentifierOrString) {
			return trieForIdentifierOrString;
		} else {			
			//extraction must be turned off
			TrieParser trie = new TrieParser(256, 1, false, false, false, (byte)'@');
			
			trie.setValue(b("\""), NodeProcessor.BeginTextDoubleQuote.ordinal());         //" enclosure  "			
			trie.setValue(b("'"), NodeProcessor.BeginTextSingleQuote.ordinal());          //' enclosure  '			
			trie.setValue(b('{'), NodeProcessor.MoveBackOne.ordinal());
			
			populateIdentifierAccepts(trie, NodeProcessor.IdentifierTop.ordinal());
			
			trieForIdentifierOrString = new TrieNext(trie); //if not bad then take it
			return trieForIdentifierOrString;			
		}
	}

	private static TrieNext trieSpaceIdentTop;
	public static TrieNext trieSpaceIdentTop() {		
    	if (null != trieSpaceIdentTop) {
    		return trieSpaceIdentTop;
    	} else {
			TrieParser trie = populateWithSpaceAndComments(baseParser());
			populateIdentifierAccepts(trie, NodeProcessor.IdentifierAcceptChar.ordinal());
			trieSpaceIdentTop = new TrieNext(trie);
			return trieSpaceIdentTop;
    	}
	}
	
	private static TrieNext trieIdentifierAccum; 
	public static TrieNext trieIdentifierAccum() {
		if (null!=trieIdentifierAccum) {
			return trieIdentifierAccum;
		} else {			
			//extraction must be turned off
			TrieParser trie = new TrieParser(256, 1, false, false, false, (byte)'@');
				
			trie.setValue(new byte[]{(byte)'_'}, NodeProcessor.IdentifierAcceptChar.ordinal());
			trie.setValue(new byte[]{(byte)'*'}, NodeProcessor.IdentifierAcceptSplat.ordinal());
			
			for(int j='0'; j<='9'; j++) {
				trie.setValue(new byte[]{(byte)j}, NodeProcessor.IdentifierAcceptChar.ordinal());
			}
			for(int j='a'; j<='z'; j++) {
				trie.setValue(new byte[]{(byte)j}, NodeProcessor.IdentifierAcceptChar.ordinal());
			}
			for(int j='A'; j<='Z'; j++) {
				trie.setValue(new byte[]{(byte)j}, NodeProcessor.IdentifierAcceptChar.ordinal());
			}
			
			//this is needed so we can close out the dot which will allow for easier parsing both
			//after this point and allowing for auto complete direclty after the dot.
			identEnd(trie);
			
			trieIdentifierAccum = new TrieNext(trie);
			return trieIdentifierAccum;			
		}		
	}
	
	private static TrieNext trieIdentifierAccumTerm; 
	public static TrieNext trieIdentifierAccumTerm() {
		if (null!=trieIdentifierAccumTerm) {
			return trieIdentifierAccumTerm;
		} else {			
			//extraction must be turned off
			TrieParser trie = new TrieParser(256, 1, false, false, false, (byte)'@');
				
			trie.setValue(new byte[]{(byte)'-'}, NodeProcessor.IdentifierAcceptCharSingleIdent.ordinal());
			trie.setValue(new byte[]{(byte)'_'}, NodeProcessor.IdentifierAcceptCharSingleIdent.ordinal());
					
			for(int j='0'; j<='9'; j++) {
				trie.setValue(new byte[]{(byte)j}, NodeProcessor.IdentifierAcceptCharSingleIdent.ordinal());
			}
			for(int j='a'; j<='z'; j++) {
				trie.setValue(new byte[]{(byte)j}, NodeProcessor.IdentifierAcceptCharSingleIdent.ordinal());
			}
			for(int j='A'; j<='Z'; j++) {
				trie.setValue(new byte[]{(byte)j}, NodeProcessor.IdentifierAcceptCharSingleIdent.ordinal());
			}
			
			//this is needed so we can close out the dot which will allow for easier parsing both
			//after this point and allowing for auto complete direclty after the dot.
			identEnd(trie);
			
			trieIdentifierAccumTerm = new TrieNext(trie);
			return trieIdentifierAccum;			
		}		
	}
	
	
	
	
	
	private static TrieNext trieIdentifierSplatStop; 
	public static TrieNext trieIdentifierSplatStop() {
		if (null!=trieIdentifierSplatStop) {
			return trieIdentifierSplatStop;
		} else {			
			//extraction must be turned off
			TrieParser trie = new TrieParser(256, 1, false, false, false, (byte)'@');
			trie.setValue(new byte[]{(byte)'('}, NodeProcessor.Function.ordinal()); //  function(  )
			continueIdentOrEnd(trie);			
			trieIdentifierSplatStop = new TrieNext(trie);
			return trieIdentifierSplatStop;			
		}		
	}
	

	private static TrieNext trieIdentifierAccumNotFirst;
	public static TrieNext trieIdentifierAccumNotFirst() {
		if (null!=trieIdentifierAccumNotFirst) {
			return trieIdentifierAccumNotFirst;
		} else {			
			//extraction must be turned off
			TrieParser trie = new TrieParser(256, 1, false, false, false, (byte)'@');
			trie.setValue(new byte[]{(byte)'('}, NodeProcessor.Function.ordinal()); //  function(  )
			continueIdentOrEnd(trie);
			int ordinal = NodeProcessor.IdentifierAcceptChar.ordinal();			
			trie.setValue(new byte[]{(byte)'-'}, ordinal);
			trie.setValue(new byte[]{(byte)'_'}, ordinal);
					
			
			for(int j='0'; j<='9'; j++) {
				trie.setValue(new byte[]{(byte)j}, ordinal);
			}
			for(int j='a'; j<='z'; j++) {
				trie.setValue(new byte[]{(byte)j}, ordinal);
			}
			for(int j='A'; j<='Z'; j++) {
				trie.setValue(new byte[]{(byte)j}, ordinal);
			}
			
			trieIdentifierAccumNotFirst = new TrieNext(trie);
			return trieIdentifierAccumNotFirst;			
		}		
	}
	
	private static TrieNext trieIdentifierAfterBrackets;
	public static TrieNext trieIdentifierAfterBrackets() {
		if (null!=trieIdentifierAfterBrackets) {
			return trieIdentifierAfterBrackets;
		} else {			
			//extraction must be turned off
			TrieParser trie = new TrieParser(256, 1, false, false, false, (byte)'@');
			continueIdentOrEnd(trie);			
			trieIdentifierAfterBrackets = new TrieNext(trie);
			return trieIdentifierAfterBrackets;			
		}		
	}
	
	
	private static void populateIdentifierAccepts(TrieParser trie, int ordinal) {
		
		trie.setValue(new byte[]{(byte)'-'}, ordinal);
		trie.setValue(new byte[]{(byte)'_'}, ordinal);
				
		trie.setValue(new byte[]{(byte)'*'}, ordinal);
		
		for(int j='0'; j<='9'; j++) {
			trie.setValue(new byte[]{(byte)j}, ordinal);
		}
		for(int j='a'; j<='z'; j++) {
			trie.setValue(new byte[]{(byte)j}, ordinal);
		}
		for(int j='A'; j<='Z'; j++) {
			trie.setValue(new byte[]{(byte)j}, ordinal);
		}		
	}


	//////////////
	//////////////
	static byte[][] quoteBytes = new byte[][] {null,"'".getBytes(), "\"".getBytes() };
		
	private static TrieNext[] trieTextStopAccum = new TrieNext[3];
	private static Map<byte[], TrieNext> trieTextStopHereCache= new HashMap<>(); //NOTE we may want a simpler memory design
	public static TrieNext buildCustomTextStopAccum(byte[] bytes) {
						
		int idx = null==bytes ? -1 : bytes[0]&1;
		if (null!=bytes && bytes.length==1 && null!=trieTextStopAccum[idx]) {
			return trieTextStopAccum[idx];
		} else {	
			TrieNext next = trieTextStopHereCache.get(bytes);			
			if (next!=null) {
				return next;
			}
			
			TrieParser trie = new TrieParser(256, 1, false, false, false);		
			trie.setValue(b("$"), NodeProcessor.TextStopInterpolate.ordinal()); //  ${  $${  \"
			trie.setValue(b("%"), NodeProcessor.TextStopIterate.ordinal());   //    %{  %%{	 \"
			if (null!=bytes) {
				trie.setValue(bytes, NodeProcessor.MoveBackQuoteEnd.ordinal()); //  close quote
				if (bytes.length>1) {
					trieTextStopHereCache.put(bytes, next = new TrieNext(trie, NodeProcessor.TextAccept.ordinal()));
					return next;
				}
			} 
			if (null!=bytes && bytes.length==1) {
				trieTextStopAccum[idx] =  new TrieNext(trie, NodeProcessor.TextAccept.ordinal());
				return trieTextStopAccum[idx];
			}			
			return new TrieNext(trie, NodeProcessor.TextAccept.ordinal());
		}
	}
	
	
	
	private static TrieNext[] trieTextInterpolate = new TrieNext[3];
	public static TrieNext trieTextInterpolate(byte[] bytes) {
		
		int idx = null==bytes ? -1 : bytes[0]&1;
		if (null!=bytes && bytes.length==1 && null!=trieTextInterpolate[idx]) {
			return trieTextInterpolate[idx];
		} else {			
			TrieParser trie = new TrieParser(256, 1, false, false, false, (byte)'@');			
			trie.setValue("{".getBytes(), NodeProcessor.TextInterpExpr.ordinal());
			trie.setValue("{~".getBytes(), NodeProcessor.TextInterpExprTilda.ordinal());

			trie.setValue("\\".getBytes(), NodeProcessor.TextAcceptExcape.ordinal());
			
			trie.setValue("${".getBytes(), NodeProcessor.TextAcceptTwo.ordinal());		
			
			if (null!=bytes) {
				trie.setValue( bytes, NodeProcessor.MoveBackQuoteEnd.ordinal());
			}
			
			if (null!=bytes && bytes.length==1) {
				trieTextInterpolate[idx] =  new TrieNext(trie, NodeProcessor.TextAcceptTwoBackOne.ordinal());
				return trieTextInterpolate[idx];
			} else {
				return new TrieNext(trie, NodeProcessor.TextAcceptTwoBackOne.ordinal());
			}
		}		
	}	
	
	private static TrieNext[] trieTextIterate = new TrieNext[3];
	public static TrieNext trieTextIterate(byte[] bytes) {
		
		int idx = null==bytes ? -1 : bytes[0]&1;
		if (null!=bytes && bytes.length==1 && null!=trieTextIterate[idx]) {
			return trieTextIterate[idx];
		} else {			
			TrieParser trie = new TrieParser(256, 1, false, false, false, (byte)'@');			
			trie.setValue("{".getBytes(), NodeProcessor.TextIterExpr.ordinal());
			trie.setValue("{~".getBytes(), NodeProcessor.TextIterExprTilda.ordinal());
			
			trie.setValue("\\".getBytes(), NodeProcessor.TextAcceptExcape.ordinal());
							
			trie.setValue("%{".getBytes(), NodeProcessor.TextAcceptTwo.ordinal());
			if (null!=bytes) {
				trie.setValue(bytes, NodeProcessor.MoveBackQuoteEnd.ordinal());
			}
			if (null!=bytes && bytes.length == 1) {
				trieTextIterate[idx] =  new TrieNext(trie, NodeProcessor.TextAcceptTwoBackOne.ordinal());
				return trieTextIterate[idx];
			} else {
				return new TrieNext(trie, NodeProcessor.TextAcceptTwoBackOne.ordinal());	
			}
			
		}		
	}		
	
	
	private static TrieNext trieIterateTemplate;
	public static TrieNext trieIterateTemplate() {	//every expression contains binary operator follow ons, no need to add.
		if (null!=trieIterateTemplate) {
			return trieIterateTemplate;
		} else {			
						
			TrieParser trie = baseParser();
			populateWithWhiteSpace(trie);	
			
			trie.setValue("for ".getBytes(),   NodeProcessor.ForExpr.ordinal()); //shared for % and $  
			trie.setValue("if ".getBytes(),    NodeProcessor.IterIf.ordinal());
			trie.setValue("for\n".getBytes(),   NodeProcessor.ForExpr.ordinal()); //shared for % and $
			trie.setValue("if\n".getBytes(),    NodeProcessor.IterIf.ordinal());			
			trie.setValue("else".getBytes(),   NodeProcessor.IterElse.ordinal());
			trie.setValue("endif".getBytes(),  NodeProcessor.IterEndIf.ordinal());
			trie.setValue("~endif".getBytes(),  NodeProcessor.IterEndIf.ordinal());
			trie.setValue("endfor".getBytes(), NodeProcessor.IterEndFor.ordinal());
			trie.setValue("~endfor".getBytes(), NodeProcessor.IterEndFor.ordinal());
			
			populateWithComments(trie);
			
			trieIterateTemplate = new TrieNext(trie); 
			return trieIterateTemplate;
		}
	}
	
	
	private static void continueIdentOrEnd(TrieParser trie) {
		trie.setValue(new byte[]{(byte)'['}, NodeProcessor.IdentifierIdx.ordinal());
		
		trie.setValue("...".getBytes(),  NodeProcessor.MoveBackThree.ordinal());		
		trie.setValue(new byte[]{(byte)'.'}, NodeProcessor.IdentifierGet.ordinal());
		
		identEnd(trie);
	}


	private static void identEnd(TrieParser trie) {
		trie.setValue(new byte[]{(byte)'{'}, NodeProcessor.IdentifierStop.ordinal());
		trie.setValue(new byte[]{(byte)'?'}, NodeProcessor.IdentifierStop.ordinal());
		trie.setValue(new byte[]{(byte)':'}, NodeProcessor.IdentifierStop.ordinal());
		trie.setValue(new byte[]{(byte)','}, NodeProcessor.IdentifierStop.ordinal());
		trie.setValue(new byte[]{(byte)')'}, NodeProcessor.IdentifierStop.ordinal());
		trie.setValue(new byte[]{(byte)']'}, NodeProcessor.IdentifierStop.ordinal());
		trie.setValue(new byte[]{(byte)'}'}, NodeProcessor.IdentifierStop.ordinal());			
		trie.setValue(new byte[]{(byte)'/'}, NodeProcessor.IdentifierStop.ordinal());  //comment or divide 
		trie.setValue(new byte[]{(byte)'#'}, NodeProcessor.IdentifierStop.ordinal());  //comment			
		trie.setValue(new byte[]{(byte)'~'}, NodeProcessor.IdentifierStop.ordinal());			
		trie.setValue(new byte[]{(byte)'+'}, NodeProcessor.IdentifierStop.ordinal());
		trie.setValue(new byte[]{(byte)'*'}, NodeProcessor.IdentifierStop.ordinal());
		
		//INVALD CHAR(s) WHICH CAN NEVER BE ACCEPTED BUT WE MUST REG AS A STOP SO 
		// THE ERROR WILL BE DETECTED AT THE RIGHT POSITION
		trie.setValue(new byte[]{(byte)';'}, NodeProcessor.IdentifierStop.ordinal());
		trie.setValue(new byte[]{(byte)'`'}, NodeProcessor.IdentifierStop.ordinal());
		trie.setValue(new byte[]{(byte)'\''}, NodeProcessor.IdentifierStop.ordinal());
		trie.setValue(new byte[]{(byte)'"'}, NodeProcessor.IdentifierStop.ordinal());
		trie.setValue(new byte[]{(byte)'^'}, NodeProcessor.IdentifierStop.ordinal());
		
		//trie.setValue(new byte[]{(byte)'-'}, NodeProcessor.IdentifierStop03.ordinal()); //this is a valid part of a label
		trie.setValue(new byte[]{(byte)','}, NodeProcessor.IdentifierStop.ordinal());
		trie.setValue(new byte[]{(byte)'>'}, NodeProcessor.IdentifierStop.ordinal());
		trie.setValue(new byte[]{(byte)'<'}, NodeProcessor.IdentifierStop.ordinal());
		trie.setValue(new byte[]{(byte)'='}, NodeProcessor.IdentifierStop.ordinal());
		trie.setValue(new byte[]{(byte)'!'}, NodeProcessor.IdentifierStop.ordinal());
		trie.setValue(new byte[]{(byte)'$'}, NodeProcessor.IdentifierStop.ordinal());
		trie.setValue(new byte[]{(byte)'|'}, NodeProcessor.IdentifierStop.ordinal());			
		trie.setValue(new byte[]{(byte)'&'}, NodeProcessor.IdentifierStop.ordinal());
		trie.setValue(new byte[]{(byte)'%'}, NodeProcessor.IdentifierStop.ordinal());
		trie.setValue(new byte[]{0x20}, 	 NodeProcessor.IdentifierStop.ordinal());  // ends with Space
		trie.setValue(new byte[]{0x09}, 	 NodeProcessor.IdentifierStop.ordinal());  // ends with Horizontal tab
		trie.setValue(new byte[]{0x0A}, 	 NodeProcessor.IdentifierStop.ordinal());  // ends with Line feed or New line
		trie.setValue(new byte[]{0x0D}, 	 NodeProcessor.IdentifierStop.ordinal());  // ends with Carriage return
	}
	
	private static TrieNext trieForIdentityContinue;
	public static TrieNext trieForIdentityContinue() {
		if (null != trieForIdentityContinue) {
			return trieForIdentityContinue;
		} else {
			TrieParser trie = populateWithSpaceAndComments(baseParser());	
			trie.setValue("in".getBytes(), NodeProcessor.ForPostIn.ordinal());
			trie.setValue(",".getBytes(),  NodeProcessor.ForComma.ordinal());			
			trieForIdentityContinue = new TrieNext(trie);
	    	return trieForIdentityContinue;
		}
	}
	
	
	private static TrieNext trieForColon;
	public static TrieNext trieForColon() {
		if (null != trieForColon) {
			return trieForColon;
		} else {
			TrieParser trie = populateWithSpaceAndComments(baseParser());		
			trie.setValue(":".getBytes(),  NodeProcessor.ForColon.ordinal());
			trie.setValue("}".getBytes(),  NodeProcessor.MoveBackOne.ordinal());  
			trie.setValue("~}".getBytes(),  NodeProcessor.MoveBackTwo.ordinal());  
			
			trieForColon = new TrieNext(trie);
	    	return trieForColon;
		}
	}

 
	private static TrieNext trieForPostColon;
	public static TrieNext trieForPostColon() {
		//either "if" or "=>"  or return
		if (null != trieForPostColon) {
			return trieForPostColon;
		} else {
			TrieParser trie = populateWithSpaceAndComments(baseParser());		
			trie.setValue("if".getBytes(),       NodeProcessor.ForIf.ordinal());
			trie.setValue("=>".getBytes(),       NodeProcessor.ForTuple.ordinal());		
			
			trieForPostColon = new TrieNext(trie, NodeProcessor.EndFor.ordinal());
	    	return trieForPostColon;
		}
	}
	
	private static TrieNext trieTupleSecond;
	public static TrieNext trieTupleSecond() {
		if (null != trieTupleSecond) {
			return trieTupleSecond;
		} else {
			TrieParser trie = populateWithSpaceAndComments(baseParser());
			//can be both so we use continue flag to keep looking until we get something unexpected which is the }
			trie.setValue("...".getBytes(),  CONTINUE_FLAG | NodeProcessor.ForTupleExpand.ordinal()); 
			trie.setValue("if".getBytes(),   CONTINUE_FLAG | NodeProcessor.ForTupleIfExpr.ordinal());		
			trieTupleSecond = new TrieNext(trie, NodeProcessor.EndFor.ordinal());
	    	return trieTupleSecond;
		}
	}

	static TrieParser baseParser() {
		TrieParser trie = new TrieParser(256, 1, false, true, false, (byte)'@');
		return trie;
	}

	static TrieParser populateWithSpaceAndComments(TrieParser trie) {
		populateWithWhiteSpace(trie);
		
		populateWithComments(trie);
		
		return trie;
	}
	
	private static NodeProcessor[] npValues = NodeProcessor.values();

	

	
	static int CONTINUE_FLAG = 1<<12;
	private static int IDX_MASK = CONTINUE_FLAG-1;
	
	public static TrieNext tokenize(TrieParserReader reader, ParseState state, TrieNext trieNext) {

		try {
			assert(null!=trieNext);
			TrieNext stoppedAt = null;
			
			//System.out.println(trieNext.trie);
			
			int startPos = reader.sourcePos;
			int startLen = reader.sourceLen;
			int startBas = reader.sourceBase;
			int startVal =  startLen + startPos - startBas;			
			
			//StringBuilder details = new StringBuilder();
			
			//int readPos = reader.sourcePos;
			int idx = 0;
			TrieNext followOn = null;
			
			do {
				if (!ParseState.isFileHalted(state)) {
					//details.setLength(0);
					//details.append("starting len: "+reader.sourceLen+" at pos "+ TrieParserReader.bytesConsumedAfterSetup(reader));
					
					idx = (int)TrieParserReader.parseNext(reader, trieNext.trie, -2, trieNext.valueIfNotFound);
					//details.append(" got response: "+idx+" post pos: "+TrieParserReader.bytesConsumedAfterSetup(reader));
				} else {
					if (state.isFileDebug()) {
						System.out.println("ERROR ALREADY DETECTED, WE ARE EXITING THE PARSE NOW....");
					}
					return trieNext;//fast exit we are halted mid file
				}
				////////////
				//no new data will appear in this loop, so this must remain the same
				int curVal =  reader.sourceLen + reader.sourcePos - reader.sourceBase;
				if (curVal!=startVal) {					
					reportError(reader, state, startPos, startLen, startBas, startVal, idx, curVal);	
				}
				
				
			} while (idx>=0 
					 && (null == (stoppedAt = npValues[IDX_MASK & idx].parse(reader, state))) 
					 && (0!=(CONTINUE_FLAG&idx)) //continue must also run the follow on 
					 && (null==(followOn = npValues[IDX_MASK & idx].followOnProcessing(reader, state)))
					 && TrieParserReader.parseHasContent(reader)
					);	
			
	//		boolean hasParent = state.hasPostProcessingStack();

			//special processing for continue items only
			if (idx>=0 && (0!=(CONTINUE_FLAG&idx))) {
				
				if (null!=followOn) {
					
			///		return followOn;
					//we know we did the stopped at already 
					if (stoppedAt==null && followOn.trie!=null) {
						
						return followOn;					
					
					} else {
			
						
						//this is a continue yet we exited so the followon must have failed and must be pushed
						state.pushPreProcessing(npValues[IDX_MASK & idx]);
						
					}
					return trieNext;
				} else {
					if (null!=stoppedAt) {
						
						//we know that we did not finish stopped so we must schedule post for later.
						state.pushPreProcessing(npValues[IDX_MASK & idx]);
						return stoppedAt;
					} else {

						//continue, we know that this fully ran but we ran out of content
						//starts next time with trie next.
						return trieNext;
					}
					
				}
			}
			
			TrieNext result = (stoppedAt==null && (idx==-2)) ?
					trieNext 					
					: stoppedAt;
			
			//hack test... we need this but it must be after..
//			if (state.hasPostProcessingStack() && state.peekPP().equals(NodeProcessor.SingleLineComment)) {
////				result = stoppedAt;// hack test.
//				result = null; //TODO: GG, may be a great fix but only if we can stop the stack overflow.
//			}
			
//			if (state.isFileDebug() && result!=null) {
//				
//				//if (result==trieNext) {
//				//	new Exception("unable to process this trie").printStackTrace();
//				//}
//				if (result == stoppedAt) {
//					System.out.println("---- using stopped    "+ParseState.buildExpectedTokensList(stoppedAt));
//				} else {
//					System.out.println("---- using trieNext    "+ParseState.buildExpectedTokensList(trieNext));	
//				}
//				
//				
//				System.out.println(state.hasProcessingStack()+"  "+idx+" set result with stoppedAt: "+(null!=stoppedAt)+" or trieNext "+(null!=trieNext));
//			
//			}
			
			
			
			if (idx>=0) {
		
				if (null == result) {				
					
					TrieNext followOnResult = npValues[IDX_MASK & idx].followOnProcessing(reader, state);
					
					if (null != followOnResult) {
						
						if (result==null && followOnResult.trie!=null) {
							result = followOnResult;
							
//							if (state.isFileDebug()) {
//								System.out.println("replace result with follow on "+(null!=result));
//							}
						} else {
							if (state.isFileDebug()) {
								System.out.println("A "+" "+state.nodeProcessorStackSize()+" "+npValues[IDX_MASK & idx]
								+"  pushed post at position: "+TrieParserReader.bytesConsumedAfterSetup(reader));
								
								//if (state.nodeProcessorStackSize()>0) {
								//	throw new UnsupportedOperationException();							
								//}
							}	
						
							state.pushPostProcessing(npValues[IDX_MASK & idx]);
						}
					}				
				} else {
					if (followOn != null || stoppedAt != null) {
					
//						if (state.isFileDebug()) {
//							System.out.println("B "+state.nodeProcessorStackSize()+" "+npValues[IDX_MASK & idx]+
//							" pushed post at position: "+TrieParserReader.bytesConsumedAfterSetup(reader)+"  followOn:"+(followOn!=null)+" stoppedAt:"+(stoppedAt!=null));
//						}
						state.pushPostProcessing(npValues[IDX_MASK & idx]);
					
					}
				}			
			} else {
				if (-1 == idx && reader.sourceLen>0) {
					
					//System.out.println(state.hasPostProcessingStack()+"  UnableToParse detected here: "+details);
					state.unableToParse(reader, trieNext);	
					
				}
	
			}
			
			
//			if (state.isFileDebug()) {
//				if (null == result) {
//					System.out.println("result is null ");
//				} else {
//					System.out.println("result is looking for: "+ParseState.buildExpectedTokensList(result));
//					
//				}
//			}
			return result;
			
		} catch (StackOverflowError soe) {
			state.unableToParse(reader, trieNext);
			throw soe;
		}
	}

	private static void reportError(TrieParserReader reader, ParseState state, int startPos, int startLen, int startBas,
			int startVal, int idx, int curVal) {
		
		if (ParseState.reportParseErrors) {
		
			StringBuilder msg = new StringBuilder();
			if (idx>=0) {
				msg.append("about to call parse on: "+npValues[IDX_MASK & idx]+"\n");						
			}
			char ch = (char)reader.sourceBacking[startPos&reader.sourceMask];
			msg.append(" char matched: "+ch+"  ");
			
			
			msg.append("started with L:"+startLen+" + P:"+startPos+" - B:"+startBas+" = "+startVal+" ");
			msg.append("but now  L:"+reader.sourceLen+" + P:"+reader.sourcePos+" - B:"+reader.sourceBase+" = "+curVal+" ");
			
			System.out.println(msg);
			
			//new UnsupportedOperationException("CRITICAL ERROR, FILE LENGTH CHANGED").printStackTrace();
			
			//System.out.println(state.localPathLocation+" critical error, exit");
			//System.exit(-1);
		}
	}


	public static boolean tokenizeAll(TrieParserReader reader, ParseState state) {		
		
		TrieNext trie;
		if (state.isTemplate) {
			state.textBegin(0, "TEMPLATE".getBytes());
			trie = trieTemplate();
		} else {
			trie = trieTop();
		}

		//keep last length to ensure we stop if the value does not change.
		int lastContent = 0;
		do {		
			lastContent = reader.sourceLen;
			tokenize(reader, state, trie);
			} while ( (reader.sourceLen<lastContent)
					&& (!ParseState.isFileHalted(state))
					&& (!state.hasPostProcessingsStack())
					&& parseHasContent(reader));
		
		state.fileEnd( TrieParserReader.bytesConsumedAfterSetup(reader)  );
	
		//triple check that the parse was clean
		boolean isClean = !ParseState.isFileHalted(state);
		
	//	System.out.println("remaining bytes "+reader.sourceLen);
		if (!isClean || state.hasPostProcessingsStack() || reader.sourceLen>0) {
			isClean = false; //this is important for editor which consumes the full file at once

			if ( ParseState.reportParseErrors && state.hasPostProcessingsStack()) {
				//not clean shutdown need to investigate
				
				state.reportProcessingStack();
			}
		}
				
		return isClean;
	
	}

	
}
