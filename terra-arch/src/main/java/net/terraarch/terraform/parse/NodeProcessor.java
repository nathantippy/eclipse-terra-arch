package net.terraarch.terraform.parse;

import net.terraarch.util.TrieParserReader;

public enum NodeProcessor {
	
	NoOp { //well tested and used by white space
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {			
			return null;
		}
	},
	EndFor { //well tested and used by white space
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {	
			state.forEnd(bytePosition(reader, state));
			return null;
		}
	},	
	WhiteSpace { //well tested and used by white space
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			state.whiteSpace(bytePosition(reader, state));
			return null;
		}
	},
	IncLineNo { //well tested and used by white space
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			state.incLineNo(bytePosition(reader, state));
			return null;
		}
	},
	SingleLineComment {
		final byte[] goal = "\n".getBytes();
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			return null;
		}
		//this is called from the stack as needed 
		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {							
			BytesCollectorBase singleLineComment = state.singleLineComment(bytePosition(reader, state));
			if (TrieParserReader.parseGather(reader, singleLineComment, goal)) {	
				state.utfBytesAdj(singleLineComment.utfBytesOffset() );	
				state.singleLineCommentFinish(bytePosition(reader, state));
				return null;
			} 
			state.utfBytesAdj(singleLineComment.utfBytesOffset() );		//parseGather has side effect of building this
			return TrieNext.NO_OP;//come back for more.			
		}		
	},
	MultiLineComment {
		final byte[] goal = "*/".getBytes();
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			return null;
		}
		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {							
			BytesCollectorBase multiLineComment = state.multiLineComment(bytePosition(reader, state));
			if (TrieParserReader.parseGather(reader, multiLineComment, goal)) {	
				state.utfBytesAdj(multiLineComment.utfBytesOffset() );	
				state.multiLineCommentFinish(bytePosition(reader, state));
				return null;
			};
			state.utfBytesAdj(multiLineComment.utfBytesOffset() );	//parseGather has side effect of building this
			return TrieNext.NO_OP;//come back for more.			
		}		
	},  
	MoveBackQuoteEnd {
		@Override	
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			reader.moveBack(state.textQuote().length);			
			return null;
		}
	},
	MoveBackOne {
		@Override	
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			reader.moveBack(1);			
			return null;
		}
	},
	MoveBackTwo {
		@Override	
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			reader.moveBack(2);			
			return null;
		}
	},
    MoveBackThree {
		@Override	
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			reader.moveBack(3);			
			return null;
		}
	},
  
    HereDoc {
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			String extractString = extractString(reader);
			
			byte[] stopHereDoc = (extractString+"\n").getBytes();
			state.incLineNo(bytePosition(reader, state));
			state.hereDocTextBegin(bytePosition(reader, state), stopHereDoc);
			return TFConstants.tokenize(reader, state, TFConstants.buildCustomTextStopAccum(state.textQuote()));
		}
		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {
			state.incLineNo(bytePosition(reader, state));
			return TFConstants.tokenize(reader,state,TFConstants.trieTextClose(state.textQuote()));
		}

	},
	
	/////////////////////////////////////////////////////////
	//     FOR
	////////////////////////////////////////////////////////	
    ForExpr { //for<space>
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
		   state.forStart(bytePosition(reader, state));
		   return TFConstants.tokenize(reader, state, TFConstants.trieIdentifierAccum()); 
		}
		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {		
			return TFConstants.tokenize(reader,state,TFConstants.trieForIdentityContinue()); //, or in
		}		
	}, 
    ForComma {
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			state.forComma(bytePosition(reader, state));			
			return TFConstants.tokenize(reader, state, TFConstants.trieSpaceIdentTop());	
		}
		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {		
			return TFConstants.tokenize(reader,state,TFConstants.trieForIdentityContinue()); //, or in
		}	
	},
    ForPostIn {
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			state.forIn(bytePosition(reader, state));
			return TFConstants.tokenize(reader, state, TFConstants.trieExpressionTop());
		}
		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {
			return TFConstants.tokenize(reader,state,TFConstants.trieForColon());
		}
	},
    ForColon {
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			state.forColon(bytePosition(reader, state));
			return TFConstants.tokenize(reader, state, TFConstants.trieExpressionTop());
		}
		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {//either "]" or "if" or "=>"
			return TFConstants.tokenize(reader,state,TFConstants.trieForPostColon());
		}
	},
    ForIf {
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			state.forIf(bytePosition(reader, state));
			return TFConstants.tokenize(reader, state, TFConstants.trieExpressionTop());
		}
		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {
			state.forEnd(bytePosition(reader, state));
			return null;
		}
		
	}, 
    ForTuple {
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			state.forLambda(bytePosition(reader, state));
			return TFConstants.tokenize(reader, state, TFConstants.trieExpressionTop());
		}
		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {		
			return TFConstants.tokenize(reader,state, TFConstants.trieTupleSecond());
		}
	},
    ForTupleExpand {
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			state.forLambdaExpand(bytePosition(reader, state));
			return null;
		}
	}, 
    ForTupleIfExpr {
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			state.forLambdaIf(bytePosition(reader, state));
			return TFConstants.tokenize(reader, state, TFConstants.trieExpressionTop());	
		}
	},
	/////////////////////////////////////////////////////////
	//     END FOR
	////////////////////////////////////////////////////////
  
    //////////////////////////////////////////////////////////////////////////////
    //  ARRAY BEGIN
    //////////////////////////////////////////////////////////////////////////////
    ArrayOpen { // [		
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {	
			if (state.isFileDebug()) {
				System.out.println("ArrayOpen started at position: "+bytePosition(reader, state));
			}
			state.arrayOpen(bytePosition(reader, state));

			return TFConstants.tokenize(reader, state, TFConstants.trieExpressionTop());
			
		}
		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {
			if (state.isFileDebug()) {
				System.out.println("ArrayOpen called followOnProcessing");
			}
			
			
			return TFConstants.tokenize(reader, state, TFConstants.trieArrayContinueOrEnd());
		}
	}, 
    ArrayContinue {		
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			if (state.isFileDebug()) {
				System.out.println("array comma detected");
			}
			state.arrayItem(bytePosition(reader, state));
			return TFConstants.tokenize(reader, state, TFConstants.trieExpressionTop());//no expression is also supported here
		}
		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {	
			if (state.isFileDebug()) {
				System.out.println("ArrayContinue  followOnProcessing");
			}
			return TFConstants.tokenize(reader, state, TFConstants.trieArrayContinueOrEnd());
		}
	}, 
    ArrayClose { //   ]
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			if (state.isFileDebug()) {
				System.out.println("ArrayClose  close parse at: "+bytePosition(reader, state));
			}
			state.arrayClose(bytePosition(reader, state));
			//after array close then we might have [ or even .
			TrieNext result = TFConstants.tokenize(reader,state,TFConstants.trieIdentFollowOn());
			if (state.isFileDebug()) {
				System.out.println("ArrayClose result is null: "+(null==result));
			}
			return result;
		}		
		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {	
			//NOTE: [ can follow array def AND [ can follow [] index		
			return TFConstants.tokenize(reader,state,TFConstants.trieIndexFollowOn());
		}
	},
    //////////////////////////////////////////////////////////////////////////////
    //  ARRAY END
    //////////////////////////////////////////////////////////////////////////////
    
    /////////////////////////////////////////
    // IDENTIFIERS BEGIN
    /////////////////////////////////////////
    IdentifierTopChar {
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			reader.moveBack(1);
			state.identifierTopStart(bytePosition(reader, state));
			return TFConstants.tokenize(reader, state, TFConstants.trieIdentifierAccum());
		}
		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {
			return TFConstants.tokenize(reader,state,TFConstants.trieBinaryOp());
		}	
	},
    IdentifierTop {
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			reader.moveBack(1);
			state.identifierTopStart(bytePosition(reader, state));
			return TFConstants.tokenize(reader, state, TFConstants.trieIdentifierAccum());
		}
	},
    IdentifierStop {
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			reader.moveBack(1);
			state.identifierNominalEnd(bytePosition(reader, state));
			//we are at the end so return 			
			return null;
		}
	}, 
    IdentifierAcceptChar {
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			//NOTE: we do not notify state because we are rolling up chars
			reader.moveBack(1); //we want this char captured			
			int singleByte = reader.parseSkipOne();	
			
			
			do { //while alpha keep going to shorten the stack.
				if (singleByte==-1) {					
					return TFConstants.trieIdentifierAccumNotFirst();
				}			
				state.appendIdentifierByte(singleByte);
				
				singleByte = reader.parseSkipOne();
			} 
			while ( (singleByte>='a' && singleByte<='z')
					|| (singleByte>='A' && singleByte<='Z') 
					|| (singleByte>='0' && singleByte<='9') 
					|| singleByte=='_' || singleByte=='-' || singleByte==-1); 
			reader.moveBack(1);
			
			return TFConstants.tokenize(reader, state, TFConstants.trieIdentifierAccumNotFirst());
		}
	},
    IdentifierAcceptCharSingleIdent {
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			//NOTE: we do not notify state because we are rolling up chars
			reader.moveBack(1); //we want this char captured			
			int singleByte = reader.parseSkipOne();	
			
			
			do { //while alpha keep going to shorten the stack.
				if (singleByte==-1) {					
					return TFConstants.trieIdentifierAccumNotFirst();
				}			
				state.appendIdentifierByte(singleByte);
				
				singleByte = reader.parseSkipOne();
			} 
			while ( (singleByte>='a' && singleByte<='z')
					|| (singleByte>='A' && singleByte<='Z') 
					|| (singleByte>='0' && singleByte<='9') 
					|| singleByte=='_' || singleByte=='-' || singleByte==-1); 
			reader.moveBack(1);
			
			return null;
		}
	},
    IdentifierAcceptSplat {
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			//NOTE: we do not notify state because we are rolling up chars
			reader.moveBack(1); //we want this char captured			
			int c = reader.parseSkipOne();	
			state.appendIdentifierByte(c);		

			return TFConstants.tokenize(reader, state, TFConstants.trieIdentifierSplatStop());
		}
		
	},

    IdentifierIdx {
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			
			state.identifierIdxExpr(bytePosition(reader, state));
			return TFConstants.tokenize(reader, state, TFConstants.trieIndexExpression());
		}
		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {
			return TFConstants.tokenize(reader,state,TFConstants.trieIdxClose());//    ]
		}		
	}, 
    IdentifierGet {
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			state.appendIdentTerm();			
			state.identifierGet(bytePosition(reader, state));
			
			//TODO: AAAAAA, we MUST find ident next after this . or we must fail.
			return TFConstants.tokenize(reader, state, TFConstants.trieIdentifierAccum());
		}	
	}, 
    IdentifierCloseIdx {		
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			state.identifierIdxClose(bytePosition(reader, state));
			return TFConstants.tokenize(reader, state, TFConstants.trieIdentifierAfterBrackets());
		}
		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {
			return TFConstants.tokenize(reader,state,TFConstants.trieIdentFollowOn()); 
		}
	}, 
    /////////////////////////////////////////
    // IDENTIFIERS END
    /////////////////////////////////////////

    
    ///////////////////////////////////////////////////////
    //  FUNCTION BEGIN
    ///////////////////////////////////////////////////////
    Function {
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {// (			
			state.functionOpen(bytePosition(reader, state));
			return TFConstants.tokenize(reader, state, TFConstants.trieExpressionTop());
		}		
		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {			
			return TFConstants.tokenize(reader, state, TFConstants.trieFunctionContinueOrEnd());
		}		
	},  
    FunctionContinue {
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			state.functionPushParam(bytePosition(reader, state));
			//System.out.println("function param ");
			return TFConstants.tokenize(reader, state, TFConstants.trieExpressionTop());
		}
		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {			
			return TFConstants.tokenize(reader, state, TFConstants.trieFunctionContinueOrEnd());
		}	
	}, 
    FunctionEnd { ///    )
 		@Override
 		public TrieNext parse(TrieParserReader reader, ParseState state) {
 			state.functionEnd(bytePosition(reader, state));
 			
 			return null;
 		}
		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {
			//return null;
			return TFConstants.tokenize(reader,state,
					TFConstants.trieFunctionFollowOn()
//					TFConstants.trieIdentFollowOn()
					
					);
		}
 	}, 
    FunctionVarEnd { /////   ...)
 		@Override
 		public TrieNext parse(TrieParserReader reader, ParseState state) {
 			state.functionEndWithArrayAsArgs(bytePosition(reader, state));
 			return null;
 		}
		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {
		//	return null;
			return TFConstants.tokenize(reader,state,
					TFConstants.trieFunctionFollowOn()
					//TFConstants.trieIdentFollowOn()
					);
		}
 	},    
    //////////////////////////////////////////////////////////////////////
    //  FUNCTION END
    //////////////////////////////////////////////////////////////////////
    
    //////////////////////////////////////////////////////////////////////
    //  NUMBER BEGIN
	//			NumericLit = decimal+ ("." decimal+)? (expmark decimal+)?;
	//			decimal    = '0' .. '9';
	//			expmark    = ('e' | 'E') ("+" | "-")?;
    /////////////////////////////////////////////////////////////////////
    NumericLiteral{
 		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			reader.moveBack(1);
			state.numberBegin(bytePosition(reader, state));
			return TFConstants.tokenize(reader,state,TFConstants.trieNumber());
		}
 		
 	},
    NumericLiteralDecimal {		
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			
			long m = TrieParserReader.capturedDecimalMField(reader, 0);
			int e = TrieParserReader.capturedDecimalEField(reader, 0);
			
			state.pushNumber(bytePosition(reader, state),m,e);
			return TFConstants.tokenize(reader,state,TFConstants.trieScientificContinueOrEnd());
		}
	}, 
   NumericLiteralInteger {		
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {			
			long i = TrieParserReader.capturedLongField(reader, 0);
			state.pushNumber(bytePosition(reader, state),i);			
			return TFConstants.tokenize(reader,state,TFConstants.trieScientificContinueOrEnd());
		}
	},
    
    NumericSciUpperPlus {
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			state.pushNumberSciNotation(bytePosition(reader, state), "E+",TrieParserReader.capturedLongField(reader, 0));
			return null;
		}
		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {
			return TFConstants.tokenize(reader,state,TFConstants.trieBinaryOp());
		}
	},
    NumericSciLowerPlus {
 		@Override
 		public TrieNext parse(TrieParserReader reader, ParseState state) {
 			state.pushNumberSciNotation(bytePosition(reader, state), "e+",TrieParserReader.capturedLongField(reader, 0));
 			return null;
 		}
 		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {
 			return TFConstants.tokenize(reader,state,TFConstants.trieBinaryOp());
 		}
 	},
    NumericSciUpperMinus {
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			state.pushNumberSciNotation(bytePosition(reader, state),"E-",TrieParserReader.capturedLongField(reader, 0));
			return null;
		}
		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {
			return TFConstants.tokenize(reader,state,TFConstants.trieBinaryOp());
		}
	},
    NumericSciLowerMinus {
 		@Override
 		public TrieNext parse(TrieParserReader reader, ParseState state) {
 			state.pushNumberSciNotation(bytePosition(reader, state),"e-",TrieParserReader.capturedLongField(reader, 0));
 			return null;
 		}
 		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {
 			return TFConstants.tokenize(reader,state,TFConstants.trieBinaryOp());
 		}
 	},
  
    NumericBinaryOpFollowOn {
 		@Override
 		public TrieNext parse(TrieParserReader reader, ParseState state) { 	
 			state.numberComplete(bytePosition(reader, state));
 			return null;
 		}
 		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {
 			return TFConstants.tokenize(reader,state,TFConstants.trieBinaryOp());
 		}
 	},
    
   //////////////////////////////////////////////////////////////////////
   //  NUMBER END
   /////////////////////////////////////////////////////////////////////
    
   //////////////////////////////////////////////////////////////////////
   //  OPEN BEGIN
   //////////////////////////////////////////////////////////////////////

   OpenParen {
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			state.parenOpen(bytePosition(reader, state));
			return TFConstants.tokenize(reader, state, TFConstants.trieExpressionTop());
		}
		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {
			return TFConstants.tokenize(reader,state,TFConstants.trieParaClose());
		}
	}, 
   CloseParen {
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			state.parenClose(bytePosition(reader, state));
			return TFConstants.tokenize(reader, state, TFConstants.trieBinaryOp());
		}
		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {
			return TFConstants.tokenize(reader,state,TFConstants.trieIdentFollowOn());
		}
	}, 
	//////////////////////////////////////////////////////////////////////
	//  OPEN END
	//////////////////////////////////////////////////////////////////////

	//////////////////////////////////////////////////////////////////////
	//  OPERATOR BEGIN
	//////////////////////////////////////////////////////////////////////
   
   OperNotExpression {
 		@Override
 		public TrieNext parse(TrieParserReader reader, ParseState state) {
 			state.pushOperation(bytePosition(reader, state), state.opNot);
 			return TFConstants.tokenize(reader, state, TFConstants.trieExpressionTop());
 		}
		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {
			return TFConstants.tokenize(reader,state,TFConstants.trieBinaryOp());
		}
 	}, 
   OperSignToggle {
 		@Override
 		public TrieNext parse(TrieParserReader reader, ParseState state) {
 			state.pushOperation(bytePosition(reader, state), state.opSign);
 			return TFConstants.tokenize(reader, state, TFConstants.trieExpressionTop());
 		}
		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {
			return TFConstants.tokenize(reader,state,TFConstants.trieBinaryOp());
		}
 	},  
   ArithmeticAdd {
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			state.pushOperation(bytePosition(reader, state), state.opAdd);
			return TFConstants.tokenize(reader, state, TFConstants.trieExpressionTop());
		}
		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {
			return TFConstants.tokenize(reader,state,TFConstants.trieBinaryOp());
		}
	}, 
   ArithmeticDivide {
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			state.pushOperation(bytePosition(reader, state), state.opDiv);
			return TFConstants.tokenize(reader, state, TFConstants.trieExpressionTop());
		}
		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {
			return TFConstants.tokenize(reader,state,TFConstants.trieBinaryOp());
		}	
	}, 
   CompareEquals {
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			state.pushOperation(bytePosition(reader, state), state.opEq);
			return TFConstants.tokenize(reader, state, TFConstants.trieExpressionTop());
		}
		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {
			return TFConstants.tokenize(reader,state,TFConstants.trieBinaryOp());
		}	
	}, 
   CompareGreater {
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			state.pushOperation(bytePosition(reader, state), state.opGt);
			return TFConstants.tokenize(reader, state, TFConstants.trieExpressionTop());
		}
		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {
			return TFConstants.tokenize(reader,state,TFConstants.trieBinaryOp());
		}	
	}, 
   CompareGreaterOrEqual {
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			state.pushOperation(bytePosition(reader, state), state.opGtE);
			return TFConstants.tokenize(reader, state, TFConstants.trieExpressionTop());
		}
		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {
			return TFConstants.tokenize(reader,state,TFConstants.trieBinaryOp());
		}	
	}, 
   CompareLessthan {
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			state.pushOperation(bytePosition(reader, state), state.opLt);
			return TFConstants.tokenize(reader, state, TFConstants.trieExpressionTop());
		}
		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {
			return TFConstants.tokenize(reader,state,TFConstants.trieBinaryOp());
		}	
	}, 
   CompareLessthanOrEqual {
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			state.pushOperation(bytePosition(reader, state), state.opLtE);
			return TFConstants.tokenize(reader, state, TFConstants.trieExpressionTop());
		}
		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {
			return TFConstants.tokenize(reader,state,TFConstants.trieBinaryOp());
		}	
	}, 
   LogicalAnd {
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			state.pushOperation(bytePosition(reader, state), state.opAnd);
			return TFConstants.tokenize(reader, state, TFConstants.trieExpressionTop());
		}
		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {
			return TFConstants.tokenize(reader,state,TFConstants.trieBinaryOp());
		}	
	}, 
   LogicalOr {
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			state.pushOperation(bytePosition(reader, state), state.opOr);
			return TFConstants.tokenize(reader, state, TFConstants.trieExpressionTop());
		}
		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {
			return TFConstants.tokenize(reader,state,TFConstants.trieBinaryOp());
		}	
	}, 
   ArithmeticMod {
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			state.pushOperation(bytePosition(reader, state), state.opMod);
			return TFConstants.tokenize(reader, state, TFConstants.trieExpressionTop());
		}
		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {
			return TFConstants.tokenize(reader,state,TFConstants.trieBinaryOp());
		}	
	}, 
   ArithmeticMultiply {
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			state.pushOperation(bytePosition(reader, state), state.opMul);
			return TFConstants.tokenize(reader, state, TFConstants.trieExpressionTop());
		}
		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {
			return TFConstants.tokenize(reader,state,TFConstants.trieBinaryOp());
		}	
	}, 
   CompareNotEquals {
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			state.pushOperation(bytePosition(reader, state), state.opNotEq);
			return TFConstants.tokenize(reader, state, TFConstants.trieExpressionTop());
		}
		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {
			return TFConstants.tokenize(reader,state,TFConstants.trieBinaryOp());
		}	
	}, 
   ArithmeticSubtract {
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			state.pushOperation(bytePosition(reader, state), state.opSub);
			return TFConstants.tokenize(reader, state, TFConstants.trieExpressionTop());
		}
		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {
			return TFConstants.tokenize(reader,state,TFConstants.trieBinaryOp());
		}	
	},   
   TernCond {
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			state.ternaryCondStart(bytePosition(reader, state));
			return TFConstants.tokenize(reader, state, TFConstants.trieExpressionTop());
		}
		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {
			return TFConstants.tokenize(reader,state,TFConstants.trieTernThird());
		}	
	}, 
   TernThird {
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			state.ternaryCondMid(bytePosition(reader, state));
			return TFConstants.tokenize(reader, state, TFConstants.trieExpressionTop());
		}
		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {	
			state.ternaryEnd(bytePosition(reader, state));
			return TFConstants.tokenize(reader,state,TFConstants.trieBinaryOp());
		}	
	}, 
	//////////////////////////////////////////////////////////////////////
	//  OPERATOR END
	//////////////////////////////////////////////////////////////////////  


    //////////////////////////////////////////////////////////////////////
    //  BLOCK BEGIN 
    //////////////////////////////////////////////////////////////////////     
    IdentifierThenBlockOrAssignmentChar { 
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {			
				reader.moveBack(1); //we need to specifically find what this is now.
				//may be identifier or expression for object items / tuples
				
				return TFConstants.tokenize(reader, state, TFConstants.trieExpressionTop());
		
		}
		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {	
			//another label or { or =  or : with comments
			return TFConstants.tokenize(reader,state,TFConstants.trieLabelContinueOrBlockOrAssign()); 	///space or  { or =
		}
	},
    
    
    BlockOpen {//  {
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {	
			state.blockOpen(bytePosition(reader, state));
			return TFConstants.tokenize(reader, state, TFConstants.trieSingleItem()); // recursive capture of single item			
		}
		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {					
			return TFConstants.tokenize(reader, state, TFConstants.trieBlockContinueOrClose());
		}
	},
	BlockItemChar {	
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			reader.moveBack(1);
			return TFConstants.tokenize(reader, state, TFConstants.trieSingleItem()); // recursive capture of single item			
		}
		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {			
				return TFConstants.tokenize(reader, state, TFConstants.trieBlockContinueOrClose());		
		}
	},
    BlockClose {
 		@Override
 		public TrieNext parse(TrieParserReader reader, ParseState state) {
 			state.blockClose(bytePosition(reader, state));
 			return null;
 		}
 		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {
 			//block could have been a for expression in { }so we may have follow on
			return TFConstants.tokenize(reader,state,TFConstants.trieIdentFollowOn());
		} 		
 	},
    
    
    
    
    
    Assignment { 
 		@Override
 		public TrieNext parse(TrieParserReader reader, ParseState state) {
 			//last identifier was an assignment identifier
 			state.convertLastIdentifierToAssignement(bytePosition(reader, state));
 			return TFConstants.tokenize(reader, state, TFConstants.trieExpressionTop()); 	
 		}
 		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) { 		
 			state.endOfAssignement(bytePosition(reader, state));//since we return null only calls once 			
 			return null;
 		}
 	},    
    Tuple { //:
 		@Override
 		public TrieNext parse(TrieParserReader reader, ParseState state) {
 			//last identifier was an assignment identifier
 			state.convertLastIdentifierToTuple(bytePosition(reader, state));
 			return TFConstants.tokenize(reader, state, TFConstants.trieExpressionTop()); 	
 		}
 	}, 
//    IdentifierThenBlock {
//		@Override
//		public TrieNext parse(TrieParserReader reader, ParseState state) {
//			return TFConstants.tokenize(reader, state, TFConstants.trieIdentifierAccum());
//		}		
//		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {
//			return TFConstants.tokenize(reader,state,TFConstants.trieLableContinueOrBlock()); 	// space or {
//		}	
//	}, 
    ContinueBlockLabelChar {
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			reader.moveBack(1);
			//this key method tells all consumers that we are building a block
			state.convertLastIdentifierToBlockLabel(bytePosition(reader, state));
			return TFConstants.tokenize(reader, state, TFConstants.trieForIdentifierOrString()); //select a full identifier
		}
		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {
			//TODO: A, this needs to fold back in to pick up multiple string labels?
			return TFConstants.tokenize(reader,state,TFConstants.trieLableContinueOrBlock()); 	// space or {
		}	
	},
   
    //////////////////////////////////////////////////////////////////////
    //  BLOCK END 
    //////////////////////////////////////////////////////////////////////    
        
    //////////////////////////////////////////////////////////////////////
    //  TEXT BEGIN
    //////////////////////////////////////////////////////////////////////
    BeginTextDoubleQuote { 
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			state.textBegin(bytePosition(reader, state), TFConstants.quoteBytes[2]);
			
			if (StringConstraint.SINGLE_TERM_IDENTITY == state.stringConstraint()) {
				 //for a block name and other cases we just want a single ident term
				// state.identifierTopStart(bytePosition(reader, state));
				// return TFConstants.tokenize(reader, state, TFConstants.trieIdentifierAccum());
				 //TODO: AAA, refine this. When it turn it on it breaks modules
				 //return TFConstants.tokenize(reader, state, TFConstants.trieIdentifierAccumTerm());
				
			} else if (StringConstraint.REFERENCE_IDENTIFIER == state.stringConstraint()) {
				 state.identifierTopStart(bytePosition(reader, state));
				 return TFConstants.tokenize(reader, state, TFConstants.trieIdentifierAccum());
			}
			
			return TFConstants.tokenize(reader, state, TFConstants.buildCustomTextStopAccum(state.textQuote()));
		}
		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {
			return TFConstants.tokenize(reader,state,TFConstants.trieTextClose(state.textQuote()));
		}
	},
    BeginTextSingleQuote { 
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			state.textBegin(bytePosition(reader, state), TFConstants.quoteBytes[1]);
            
			if (StringConstraint.SINGLE_TERM_IDENTITY == state.stringConstraint()) {
				 //for a block name and other cases we just want a single ident term
				// state.identifierTopStart(bytePosition(reader, state));
				// return TFConstants.tokenize(reader, state, TFConstants.trieIdentifierAccum());
				  //TODO: A, refine this.. When it turn it on it breaks modules
				 //return TFConstants.tokenize(reader, state, TFConstants.trieIdentifierAccumTerm());
								
			} else if (StringConstraint.REFERENCE_IDENTIFIER == state.stringConstraint()) {
				 state.identifierTopStart(bytePosition(reader, state));
				 return TFConstants.tokenize(reader, state, TFConstants.trieIdentifierAccum());
			}
			return TFConstants.tokenize(reader, state, TFConstants.buildCustomTextStopAccum(state.textQuote()));
		}
		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {
			return TFConstants.tokenize(reader,state,TFConstants.trieTextClose(state.textQuote()));
		}
	},
    EndQuote {
  		@Override
  		public TrieNext parse(TrieParserReader reader, ParseState state) {  
  			state.textEnd(bytePosition(reader, state));
  			return null;
  		}
		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {
			return TFConstants.tokenize(reader,state,TFConstants.trieTextFolllowOn());
		}
  	}, 
    TextStopInterpolate { //found $
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {			
			return TFConstants.tokenize(reader, state, TFConstants.trieTextInterpolate(state.textQuote()));// $ {
		}
		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {
				return TFConstants.tokenize(reader, state, TFConstants.buildCustomTextStopAccum(state.textQuote()));
		}
	},
    TextStopIterate {
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			return TFConstants.tokenize(reader, state, TFConstants.trieTextIterate(state.textQuote()));// % {
		}
		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {
			return TFConstants.tokenize(reader, state, TFConstants.buildCustomTextStopAccum(state.textQuote()));
		}
	}, 
  
    TextAccept {
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {		
			
			//to avoid a stack overflow we must capture all chars in place here until
			//we come to something which may have a different interpretation 
			//These will be the chars  $   %   \  and quoteBytes[state.textQuote()] 
			
			if (TrieParserReader.positionEquals(reader, state.textQuote())) {;
				reader.moveBack(state.textQuote().length);
				return null;
			}
		
			int c = reader.parseSkipOne();	
			if (c>=128) {
				int bytesPerChar = 0;
				while (c>=128) { //grab all UTF8 extended
					if (--bytesPerChar<0) {
						 bytesPerChar = bytesCompute(c);
						 state.utfBytesAdj(--bytesPerChar);
					}
					///     the byte position must be adjusted by this amount
					state.appendTextByte(c);
					c = reader.parseSkipOne();
					if (-1==c) {
						return TFConstants.buildCustomTextStopAccum(state.textQuote());
					}
				}
				reader.moveBack(1);
				if (TrieParserReader.positionEquals(reader, state.textQuote())) {;
					reader.moveBack(state.textQuote().length);
					return null;
				}
				c = reader.parseSkipOne();
			}
			while ( ((c!='$') && (c!='%'))) {
				if (-1==c) {
					if (state.isTemplate && bytePosition(reader, state) == state.templateFileLength) {
						//we detected the end of a template file so close out the text now
						state.textEnd(bytePosition(reader, state));	
						return null; //we reached the end of the template so return null
					}
					return TFConstants.buildCustomTextStopAccum(state.textQuote());
				}
				//only use escape if we are processing text not heredoc
				if (null!=state.textQuote() && state.textQuote().length==1) {
					if ('\\'==c) { //escape so take the next char as is
						c = reader.parseSkipOne();
						if (-1==c) {
							reader.moveBack(1);//need to ensure the escape is captured next time.			
							return TFConstants.buildCustomTextStopAccum(state.textQuote());
						}
						if (c=='n') {
							state.appendTextByte('\\');			
						}
					}				
				} 
				//if heredoc or multi-line string we must count lines
				if (c=='\n') {
					byte[] temp = state.textQuote();					
					if (temp.length==1 && (temp[0]=='\'' || temp[0]=='\"')   ) {
						//this simple string can not contain \n so we must stop here so its flaged as an error.						
						reader.moveBack(1);//put back this char since we are not sure what it is yet			
						return null;
					}
					
					state.incLineNo(bytePosition(reader, state));						
								
				}
				
				state.appendTextByte(c);
				//protecting against extend UTF-8 points which may contain the quote
				if (TrieParserReader.positionEquals(reader, state.textQuote())) {;
					reader.moveBack(state.textQuote().length);
					return null;
				}
				c = reader.parseSkipOne();	
				if (c>=128) {
					int bytesPerChar = 0;
					while (c>=128) { //grab all UTF8 extended
						if (--bytesPerChar<0) {
							 bytesPerChar = bytesCompute(c);
							 state.utfBytesAdj(--bytesPerChar);
						}
						///     the byte position must be adjusted by this amount
						state.appendTextByte(c);				
						c = reader.parseSkipOne();
						if (-1==c) {
							return TFConstants.buildCustomTextStopAccum(state.textQuote());
						}
					}
					reader.moveBack(1);
					if (TrieParserReader.positionEquals(reader, state.textQuote())) {;
						reader.moveBack(state.textQuote().length);
						return null;
					}
					c = reader.parseSkipOne();
				}
			};
					
			reader.moveBack(1);//put back this char since we are not sure what it is yet			
			return TFConstants.tokenize(reader, state, TFConstants.buildCustomTextStopAccum(state.textQuote()));
		}
			
	}, 
    TextInterpExpr {
		@Override
  		public TrieNext parse(TrieParserReader reader, ParseState state) {  
		
			state.interpolateOpen(bytePosition(reader, state),2);
			return TFConstants.tokenize(reader, state, TFConstants.trieExpressionTop());	
		}
		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {
			return TFConstants.tokenize(reader,state,TFConstants.trieInterpClose());
		}		
	},
    TextInterpExprTilda {
		@Override
  		public TrieNext parse(TrieParserReader reader, ParseState state) {  			
			state.interpolateOpen(bytePosition(reader, state),3);
			return TFConstants.tokenize(reader, state, TFConstants.trieExpressionTop());	
		}
		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {
			return TFConstants.tokenize(reader,state,TFConstants.trieInterpClose());
		}		
	},
    TextIterExpr {
		@Override
  		public TrieNext parse(TrieParserReader reader, ParseState state) {  			
			state.iterateOpen(bytePosition(reader, state),2);	
			return TFConstants.tokenize(reader, state, TFConstants.trieIterateTemplate());			
  		}
		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {
			return TFConstants.tokenize(reader,state,TFConstants.trieInterpClose());
		}		
	},
    TextIterExprTilda {
		@Override
  		public TrieNext parse(TrieParserReader reader, ParseState state) {  			
			state.iterateOpen(bytePosition(reader, state),3);	
			return TFConstants.tokenize(reader, state, TFConstants.trieIterateTemplate());			
  		}
		public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {
			return TFConstants.tokenize(reader,state,TFConstants.trieInterpClose());
		}		
	},
    CloseIntrp {
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			state.interpolateClose(bytePosition(reader, state));
			return null;
		}
	},
    TextAcceptExcape {
  		@Override	
  		public TrieNext parse(TrieParserReader reader, ParseState state) {	
  			if (reader.sourceLen<=1) { //exit early for more text
  				reader.moveBack(1);
  				return TFConstants.buildCustomTextStopAccum(state.textQuote());
  			}  		
  			state.appendTextByte(reader.parseSkipOne());	
  			
  			return TFConstants.tokenize(reader, state, TFConstants.buildCustomTextStopAccum(state.textQuote()));
  		}
  	},    
    TextAcceptTwo {
		@Override	
		public TrieNext parse(TrieParserReader reader, ParseState state) {	
			if (reader.sourceLen<=2) { //exit early for more text
				return TFConstants.buildCustomTextStopAccum(state.textQuote());
			}
			reader.moveBack(2);
		
			state.appendTextByte(reader.parseSkipOne());		
			state.appendTextByte(reader.parseSkipOne());
			
			return TFConstants.tokenize(reader, state, TFConstants.buildCustomTextStopAccum(state.textQuote()));
		}
	}, 
    TextAcceptTwoBackOne {
		@Override	
		public TrieNext parse(TrieParserReader reader, ParseState state) {	
			if (reader.sourceLen<=2) { //exit early for more text
				return TFConstants.buildCustomTextStopAccum(state.textQuote());
			}
			reader.moveBack(1);

			state.appendTextByte(reader.parseSkipOne());		
			state.appendTextByte(reader.parseSkipOne());

			return TFConstants.tokenize(reader, state, TFConstants.buildCustomTextStopAccum(state.textQuote()));
		}
	}, 
    ////////////////////////////////////////////////
	//  TEXT END
    ////////////////////////////////////////////////
	EndExpr {
		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			state.endOfExpr(bytePosition(reader, state));
			return null;
		}
	 },
	IterIf {

		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			
			state.iterIf();
			return TFConstants.tokenize(reader, state, TFConstants.trieExpressionTop());
		}
		 
	 }, 
	IterElse {

		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			state.iterElse();
			return null;
		}
		 
	 }, 
	IterEndIf {

		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			state.iterEndIf();
			return null;
		}
		 
	 }, 
	IterEndFor {

		@Override
		public TrieNext parse(TrieParserReader reader, ParseState state) {
			state.iterEndFor();
			state.forEnd(bytePosition(reader, state));
			return null;
		}
		 
	 }
	;
	
	private static int bytePosition(TrieParserReader reader, ParseState state) {
		return TrieParserReader.bytesConsumedAfterSetup(reader) - state.utfBytesAdj();
	}

	public abstract TrieNext parse(TrieParserReader reader, ParseState state);
	
	public static int bytesCompute(int c) {
		int mask = 128;
		int max = 8;
		int results = 0;
		while ((c & mask)!=0 && --max>=0 ) {
				c=c<<1;
				results++;
		}
       return results;		
	}

	public TrieNext followOnProcessing(TrieParserReader reader, ParseState state) {
		return null; 
	}
	
	public String extractString(TrieParserReader reader) {
		StringBuilder builder = new StringBuilder();
		TrieParserReader.capturedFieldBytesAsUTF8(reader, 0, builder);
		return builder.toString().intern();
	}
	
}
