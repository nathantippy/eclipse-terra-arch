package net.terraarch.tf.parse.doc;

import java.io.File;

import net.terraarch.tf.parse.BlockType;
import net.terraarch.tf.parse.BytesCollectorBase;
import net.terraarch.tf.parse.ParseState;
import net.terraarch.tf.parse.TermLayer;
import net.terraarch.tf.structure.StructureDataModule;
import net.terraarch.util.AppendableBuilderReader;
import net.terraarch.util.TrieParser;

public class DocumentTokenMap extends DocumentMap {
	
	public DocumentTokenMap(StructureDataModule module, boolean isDisabled, ProviderConstraintImpl pci) {
		super(module, isDisabled, pci);	
	}
	
	public final TokenCollector tc = new TokenCollector();
	
	private ThemeColors textColor = ThemeColors.vivid;    

	protected int activeBlockType = -1;
	
    
    public ThemeColors textColor() {
    	return textColor;
    }
    public void activeTextColor(ThemeColors activeColor) {
    	this.textColor = activeColor;
    }
	
	@Override
	public int forEnd(int bytePosition) {
		lastToken = UNDEF_ID;
		int depth = super.forEnd(bytePosition);
	    if (depth>=0) {
	    	forArgStack.get(depth).clear(); //not needed not so empty the parser
	    }
	    tc.popRainbow();
	    
		return depth;
	}
	@Override
	public int forStart(int endPos) {
		lastToken = UNDEF_ID;
	
	//	Token token = net.terraarch.presentation.TextColors.rainbowCache[textColor.ordinal()][rainbowWrapperStack.size()-1];
	//	addToken((new TokenBlock(this.lineNumber, this.lastPosition, endPos,  token)));
        
		TypeColors type = net.terraarch.tf.parse.doc.TypeColors.I_TEXT;
		tc.addToken(this, endPos, type);
	//	addToken((new TokenBlock(textColor.ordinal(), this.lastPosition, endPos, net.terraarch.presentation.TextColors.Types.OPERATION.ordinal())));
		
		this.lastPosition = endPos;
		int depth = super.forStart(endPos);
		while (forArgStack.size()<depth) {
			forArgStack.add(new TrieParser(16, false));
		}
		int idx = 0;
		String message = "Unexpected character in rainbow for start";
		tc.addTokenForStart(this, endPos, idx, message);
		
		return depth;
	}
	@Override
	public void forComma(int endPos) {
		tc.addTokenRainbow(this, endPos);
		lastToken = UNDEF_ID;
		//addToken((new TokenBlock(this.lastPosition, endPos, Types.FOR.ordinal()+base))););
		this.lastPosition = endPos;
		super.forComma(endPos);
	}
	@Override
	public void forIn(int endPos) {
		lastToken = UNDEF_ID;
		tc.addTokenRainbow(this, endPos);

		//addToken((new TokenBlock(this.lastPosition, endPos, Types.FOR.ordinal()+base)));ase)));
		this.lastPosition = endPos;
		super.forIn(endPos);
	}
	@Override
	public void forColon(int endPos) {
		lastToken = UNDEF_ID;
		tc.addTokenRainbow(this, endPos);

		//addToken((new TokenBlock(this.lastPosition, endPos, Types.FOR.ordinal()+base)));
		this.lastPosition = endPos;
		super.forColon(endPos);
	}
	@Override
	public void forIf(int endPos) {
		lastToken = UNDEF_ID;
		tc.addTokenRainbow(this, endPos);

		//addToken((new TokenBlock(this.lastPosition, endPos, Types.FOR.ordinal()+base)));
		this.lastPosition = endPos;
		super.forIf(endPos);
	}
	@Override
	public void forLambda(int endPos) {
		lastToken = UNDEF_ID;
		tc.addTokenRainbow(this, endPos);

		//addToken((new TokenBlock(this.lastPosition, endPos, Types.FOR.ordinal()+base)));
		this.lastPosition = endPos;
		super.forLambda(endPos);
	}
	@Override
	public void forLambdaExpand(int endPos) {
		lastToken = UNDEF_ID;
		tc.addTokenRainbow(this, endPos);

		//addToken((new TokenBlock(this.lastPosition, endPos, Types.FOR.ordinal()+base)));ase)));
		this.lastPosition = endPos;
		super.forLambdaExpand(endPos);
	}
	@Override
	public void forLambdaIf(int endPos) {
		lastToken = UNDEF_ID;
		tc.addTokenRainbow(this, endPos);

		//addToken((new TokenBlock(this.lastPosition, endPos, Types.FOR.ordinal()+base)));base)));
		this.lastPosition = endPos;
		super.forLambdaIf(endPos);
	}
	

	@Override
	public void convertLastIdentifierToAssignement(int endPos) {
		lastToken = UNDEF_ID;
		
		
		if (null!=tc.getLastIdentTokenBlock()) {
			tc.getLastIdentTokenBlock().setSelection(1);
			tc.setLastIdentTokenBlock(null);
		}
		
		
		TypeColors type = net.terraarch.tf.parse.doc.TypeColors.OPERATION;
		tc.addToken(this, endPos, type);
		this.lastPosition = endPos;
		
	}


	@Override
	public AppendableBuilderReader convertLastIdentifierToTuple(int endPos) {
		lastToken = UNDEF_ID;
		AppendableBuilderReader keyOfTuple = super.convertLastIdentifierToTuple(endPos);
		
		
		if (null!=tc.getLastIdentTokenBlock()) {
			tc.getLastIdentTokenBlock().setSelection(2);
			tc.setLastIdentTokenBlock(null);
		}
		
		
		TypeColors type = net.terraarch.tf.parse.doc.TypeColors.OPERATION;
		tc.addToken(this, endPos, type);
		this.lastPosition = endPos;
		return keyOfTuple;
	}

	@Override
	public void interpolateClose(int endPos) {
		//NO interpolation must happen inside of a block name
		lastToken = UNDEF_ID;
		int colorKey = !isInterpolationValidHere() ? net.terraarch.tf.parse.doc.TypeColors.UNDEFINED.self().ordinal() :net.terraarch.tf.parse.doc.TypeColors.I_TEXT.ordinal();
		boolean isErr = !isInterpolationValidHere();
		String message = "Not supported in block identifiers";
		tc.addTokenWithOptionalError(this, colorKey, isErr, message, endPos);
		this.lastPosition = endPos;
		super.interpolateClose(endPos);
	}

	@Override
	public void convertLastIdentifierToBlockLabel(int endPos) {
		//critical super, do not remove
		super.convertLastIdentifierToBlockLabel(endPos);
		lastToken = UNDEF_ID;
		if (blockDepth>0) { //do not change color of the top block
			if (null!=tc.getLastIdentTokenBlock()) {
				tc.getLastIdentTokenBlock().setSelection(1);
				tc.setLastIdentTokenBlock(null);
			}
		}
		TypeColors type = net.terraarch.tf.parse.doc.TypeColors.BLOCK;
		tc.addToken(this, endPos, type);
		this.lastPosition = endPos;
	}
	

	@Override
	public String interpolateOpen(int endPos, int size) {
				
		lastToken = UNDEF_ID;
		int colorKey = !isInterpolationValidHere() ? net.terraarch.tf.parse.doc.TypeColors.UNDEFINED.self().ordinal() : net.terraarch.tf.parse.doc.TypeColors.TEXT.ordinal();
		{
			boolean isErr = !isInterpolationValidHere();
			String message = "Interpolation is not supported here";
			int endPos2 = endPos-size;
			tc.addTokenWithOptionalError(this, colorKey, isErr, message, endPos2);
		}
		this.lastPosition = endPos-size;
		
		int colorKey2 = !isInterpolationValidHere() ? net.terraarch.tf.parse.doc.TypeColors.UNDEFINED.self().ordinal() :net.terraarch.tf.parse.doc.TypeColors.I_TEXT.ordinal();
		{
			boolean isErr = !isInterpolationValidHere();
			String message = "Interpolation is not supported here";
			tc.addTokenWithOptionalError(this, colorKey2, isErr, message, endPos);
		}		
		this.lastPosition = endPos;
		return super.interpolateOpen(endPos, size);
	}
	@Override
	public void iterateOpen(int endPos, int size) {
				
		lastToken = UNDEF_ID;
		int colorKey = !isInterpolationValidHere() ? net.terraarch.tf.parse.doc.TypeColors.UNDEFINED.self().ordinal() : net.terraarch.tf.parse.doc.TypeColors.TEXT.ordinal();
		{
			boolean isErr = !isInterpolationValidHere();
			String message = "Iteration is not supported here";
			int endPos2 = endPos-size;
			tc.addTokenWithOptionalError(this, colorKey, isErr, message, endPos2);
		}
		this.lastPosition = endPos-size;
		
		int colorKey2 = !isInterpolationValidHere() ? net.terraarch.tf.parse.doc.TypeColors.UNDEFINED.self().ordinal() :net.terraarch.tf.parse.doc.TypeColors.I_TEXT.ordinal();
		{
			boolean isErr = !isInterpolationValidHere();
			String message = "Iteration is not supported here";
			tc.addTokenWithOptionalError(this, colorKey2, isErr, message, endPos);
		}
		this.lastPosition = endPos;
		super.iterateOpen(endPos, size);
	}
	public void iterElse() {
		super.iterElse();
	}

	public void iterEndIf() {
		super.iterEndIf();
	}

	public void iterEndFor() {
		super.iterEndFor();
	}

	public void iterIf() {
		super.iterIf();   
	}

	
	@Override
	public AppendableBuilderReader identifierGet(int endPos) {
		lastToken = UNDEF_ID;
		int size = identTermStack.size();
		
		if (size>0) {
		
			TermLayer termLayer = identTermStack.get(identTermStack.size()-1);
			int termLayerNo    = termLayer.get();
			
			//must capture here before clear
			AppendableBuilderReader value = super.identifierGet(endPos); //this changed the get count in the term layer
			
			int id = -1;
			if (termLayer.get()==1) {			
			    id = TermProcessingImpl.namespaceParser(this, termLayer, value);
			}
			if (id>=0) {//can only be positive if this is layer 1		
				
				TypeColors type = net.terraarch.tf.parse.doc.TypeColors.IDENT_NAMESPACE;
				tc.addToken(this, endPos, type);
				
			} else {	
				if (value.byteLength()>0) {
					StringBuilder errorMessage = new StringBuilder();
					boolean isErr = errorMessage.length()>0;
					String messageLocal = errorMessage.toString();
					tc.addTokenIdent(this, endPos, termLayer, termLayerNo, value, errorMessage, isErr, messageLocal); 
				}
			}
			
			this.lastPosition = endPos;
			return value;
		} else {
		
		
			TypeColors type = net.terraarch.tf.parse.doc.TypeColors.IDENT_DETAIL;
			tc.addToken(this, endPos, type);

            //this is for generated arrays not a proper identifer
			AppendableBuilderReader value = super.identifierGet(endPos);	 //this changed the get count in the term layer	

			this.lastPosition = endPos;
			return value;
		
		}
	}
	@Override
	public AppendableBuilderReader identifierIdxExpr(int endPos) {
		lastToken = UNDEF_ID;
	
		int size = identTermStack.size();
		
		if (size>0) {
			TermLayer termLayer = identTermStack.get(size-1);
			final int termLayerNo    = termLayer.get();
			
			//must capture here before clear
			AppendableBuilderReader value = super.identifierIdxExpr(endPos);	 //this changed the get count in the term layer	
			
			int id = -1;
			if (termLayer.get()==1) {
			    id = TermProcessingImpl.namespaceParser(this, termLayer, value);
			}
			if (id>=0) {	
				TypeColors type = net.terraarch.tf.parse.doc.TypeColors.IDENT_NAMESPACE;
				tc.addToken(this, endPos, type);
			} else {	
				if (value.byteLength()>0) {
					StringBuilder errorMessage = new StringBuilder();
					boolean isErr = errorMessage.length()>0;
					String messageLocal = errorMessage.toString();
					tc.addTokenIdent(this, endPos, termLayer, termLayerNo, value, errorMessage, isErr, messageLocal); 
				}
			}
			this.lastPosition = endPos;
			return value;
		} else {
			
			TypeColors type = net.terraarch.tf.parse.doc.TypeColors.IDENT_USAGE;
			tc.addToken(this, endPos, type);

            //this is for generated arrays not a proper identifer
			AppendableBuilderReader value = super.identifierIdxExpr(endPos);	 //this changed the get count in the term layer	

			this.lastPosition = endPos;
			return value;
		
		}
		
	}
	@Override
	public void identifierIdxClose(int endPos) {			
		lastToken = UNDEF_ID;
		int size = identTermStack.size();
		
		if (size>0) {
			TermLayer termLayer = identTermStack.get(size-1);
			final int termLayerNo    = termLayer.get();
			
			int limit = ( termLayer.namespace() == ParseState.NAMESPACES.DATA.ordinal() ) ? 3: 2;
			if (termLayerNo <= limit) {
				TypeColors type = net.terraarch.tf.parse.doc.TypeColors.IDENT_USAGE;
				tc.addToken(this, endPos, type);
			} else {
			    TypeColors type = net.terraarch.tf.parse.doc.TypeColors.IDENT_DETAIL;
				tc.addToken(this, endPos, type);
			}
		} else {
			TypeColors type = net.terraarch.tf.parse.doc.TypeColors.IDENT_USAGE;
			tc.addToken(this, endPos, type);		
		}
		this.lastPosition = endPos;
		super.identifierIdxClose(endPos);
	}

	
	@Override
	public AppendableBuilderReader identifierNominalEnd(int endPos) { 
		final int termStackSize = identTermStack.size();
		lastToken = IDENT_ID;
		
		TermLayer termLayer = 0==termStackSize ? null : identTermStack.get(termStackSize-1);
		
	    int termLayerNo    = 0==termStackSize ? -1 : termLayer.get();
	    boolean lastTermWasIdx = lastTermWasIdx();
	    
	    int extraChar = 0;
	    
		AppendableBuilderReader value = super.identifierNominalEnd(endPos); 

		    long idx1 = value.lookupExactMatch(localReader, literalsParser);
		
			int t1;
			StringBuilder errorMessage1 = new StringBuilder();
			if (idx1>=0) {
			
				if (((int)idx1) == ParseState.LITERALS.SPLAT.ordinal()) {
				    //splat behaves like an interpolation or expansion so we use that color
				    t1=net.terraarch.tf.parse.doc.TypeColors.I_TEXT.ordinal();
				} else {
					t1=net.terraarch.tf.parse.doc.TypeColors.NUMBER_OR_LITERAL.ordinal();
				}
				
			} else {
				//not a literal
				long idx = value.lookupExactMatch(localReader, blockTypeParser);
				
				if (idx>=0 && blockDepth==0) { //only block starts
					t1 = net.terraarch.tf.parse.doc.TypeColors.BLOCK.ordinal();
					//NOTE: if desired we could color variable defs different...
					this.activeBlockType = (int)idx; //for checking later, only for root
				} else {
					//not a block type
					
					if (isInExperimentsBlock()) {
						
						//NOTE: we do not know what "experiaments" will be allowed by hashi so we allow all strings
						//TODO: YY, we may want to restrict this to a safe list
						t1 = net.terraarch.tf.parse.doc.TypeColors.IDENT_NAMESPACE.ordinal();
												
					} else {
						
						if (null!=termLayer && value.byteLength()>0) {
							t1 = TermProcessingImpl.usageColorToken(this, value, termLayerNo, termLayer, errorMessage1);
								
							if (lastTermWasIdx) { //key fix for values after [].xxx
								extraChar=1; //we need an extra char for the ].
							}
													    					    
						} else {
						    t1 = net.terraarch.tf.parse.doc.TypeColors.IDENT_DETAIL.ordinal();
						}
					}
				}
				
			}
				
		
			BlockType blockType = activeBlockType>=0 ? BlockType.values()[activeBlockType] : null;
			StringBuilder errorMessage2 = new StringBuilder();
			
			int t2;
			if (null != blockType  
			    && ((blockDepth==1) || !blockType.topOnly())
			    && null!= blockType.exclusiveValidChildren() ) {
				//we are inside vairable difintition.
							
				long varIdx = value.lookupExactMatch(localReader, blockType.exclusiveValidChildren());
							
				if (varIdx>=0) {
					t2 = net.terraarch.tf.parse.doc.TypeColors.IDENT_ASSIGN.ordinal();
				} else {
				    errorMessage2.append("Undefined identifier for "+blockType+", found '"+value+"'"); 
					t2 = net.terraarch.tf.parse.doc.TypeColors.UNDEFINED.self().ordinal();
				}
								
			} else {
				t2 = net.terraarch.tf.parse.doc.TypeColors.IDENT_ASSIGN.ordinal();
			}
			
			
			int t3 = net.terraarch.tf.parse.doc.TypeColors.NUMBER_OR_LITERAL.ordinal();  //NOTE: this color is for a tuple assignment with :
			
						
			String msg1 = errorMessage1.toString();
			String msg2 = errorMessage2.toString();
			tc.addTokenBlockNomIdent(this, endPos, extraChar, t1, t2, t3, msg1, msg2);
			
			
			if (null==termLayer || termLayer.get()>0) {
				//only modify later with = or : { if this was a single ident.
				
				
				tc.setLastIdentTokenBlock(null);
				
								
			}
			
			
			this.lastPosition = endPos;		
		return value;
	}
	@Override
	public void textBegin(int endPos, byte[] quotes) {
		
		//System.out.println("test begin   ----------------------------------- ");
		
		if (isCollectingBlockLabels()) {
			//this string is a block label
			if (blockDepth==0) {
				TypeColors type = net.terraarch.tf.parse.doc.TypeColors.BLOCK_LABEL;
				tc.addToken(this, endPos, type);
			} else {
				//NOTE: text begin can not start directly after another text end or ident end.
				if (lastToken != TEXT_ID && lastToken != IDENT_ID) {
					TypeColors type = net.terraarch.tf.parse.doc.TypeColors.IDENT_DEF;
					tc.addToken(this, endPos, type);	
				} else {
				     TypeColors type = net.terraarch.tf.parse.doc.TypeColors.UNDEFINED.self();
					String message = "Text may not start here, is } missing somewhere above this point?";
					tc.addTokenTypeWithError(this, type, message, endPos);
				}
				
				
			}
		} else {
			//this string is not part of the block address
			TypeColors type = net.terraarch.tf.parse.doc.TypeColors.TEXT;
			tc.addToken(this, endPos, type);
		}
		lastToken = UNDEF_ID;
		this.lastPosition = endPos;
		super.textBegin(endPos, quotes);
	}
	@Override
	public void hereDocTextBegin(int endPos, byte[] quotes) {
		lastToken = UNDEF_ID;
		if (isCollectingBlockLabels()) {
			//this string is a block label
			if (blockDepth==0) {
				TypeColors type = net.terraarch.tf.parse.doc.TypeColors.BLOCK_LABEL;
				tc.addToken(this, endPos, type);
			} else {
				TypeColors type = net.terraarch.tf.parse.doc.TypeColors.IDENT_DEF;
				tc.addToken(this, endPos, type);
			}
		} else {
			//this string is not part of the block address
			
			if (quotes.length>1 && quotes[0]<=' ') { //heredoc stop must not start with white space
			    TypeColors type = net.terraarch.tf.parse.doc.TypeColors.UNDEFINED.self();
				String message = "Unexpected space before heredoc";
				tc.addTokenTypeWithError(this, type, message, endPos);
			} else {			
				TypeColors type = net.terraarch.tf.parse.doc.TypeColors.HERE_DOC;
				tc.addToken(this, endPos, type);
			}
		}
		
		this.lastPosition = endPos;
		super.hereDocTextBegin(endPos, quotes);
	}
	@Override
	public AppendableBuilderReader textEnd(int endPos) {
		AppendableBuilderReader text = super.textEnd(endPos);
		lastToken = TEXT_ID;				
		if (isCollectingBlockLabels()) {
			//this string is a block label
			if (blockDepth==0) {
				TypeColors type = net.terraarch.tf.parse.doc.TypeColors.BLOCK_LABEL;
				tc.addToken(this, endPos, type);
			} else {
				TypeColors type = net.terraarch.tf.parse.doc.TypeColors.IDENT_DEF;
				tc.addToken(this, endPos, type);
			}
		} else {
			//this string is not part of the block address
			TypeColors type = net.terraarch.tf.parse.doc.TypeColors.TEXT;
			tc.addToken(this, endPos, type);
		}
		
		this.lastPosition = endPos;
		return text;
	}
	


	@Override
	public int blockOpen(int endPos) {
		int level = super.blockOpen(endPos);
		
		if (1 == level) {
			TypeColors type = net.terraarch.tf.parse.doc.TypeColors.BLOCK;
			tc.addToken(this, endPos, type);
		} else {
			// this can be after a block ident or = but never after a " that is an error.
			
			if (lastToken!=TEXT_ID || level==2) { //level 2 is needed to keep this from an error: terraform{ backend "s3" {} }
				TypeColors type = net.terraarch.tf.parse.doc.TypeColors.IDENT_DEF;
				tc.addToken(this, endPos, type);
			} else {			
				TypeColors type = net.terraarch.tf.parse.doc.TypeColors.UNDEFINED.self();
				String message = "The { may not appear here. Is a } missing above this point?";
				tc.addTokenTypeWithError(this, type, message, endPos);
			}
		}
		lastToken = UNDEF_ID;
		
		this.lastPosition = endPos;
		
		
		return level;
	}

	@Override
	public int blockClose(int endPos) {
		lastToken = UNDEF_ID;
		int level = super.blockClose(endPos);
		
		if (0 == level) {
			TypeColors type = net.terraarch.tf.parse.doc.TypeColors.BLOCK;
			tc.addToken(this, endPos, type);
			
			activeBlockType = -1;//clear block root type	
		} else {
			TypeColors type = net.terraarch.tf.parse.doc.TypeColors.IDENT_DEF;
			tc.addToken(this, endPos, type);
		}
				
		this.lastPosition = endPos;
				
		return level;
	}
	
	//////////////////////////////////////////////////////
	//finished types after this point
	////////////////////////////////////////////////////

	@Override
	public void functionEnd(int endPos) {
		super.functionEnd(endPos);
		lastToken = UNDEF_ID;
		tc.rainbowWrapperStack().remove(tc.rainbowWrapperStack().size()-1).setSelection(1);			
		tc.addTokenRainbowEnd(this, endPos);
				
		this.lastPosition = endPos;
	}
	@Override
	public void functionEndWithArrayAsArgs(int endPos) {
		super.functionEndWithArrayAsArgs(endPos);
		lastToken = UNDEF_ID;
		tc.rainbowWrapperStack().remove(tc.rainbowWrapperStack().size()-1).setSelection(1);			
		tc.addTokenRainbowEnd(this, endPos);
		
		this.lastPosition = endPos;
	}

	@Override
	public void functionPushParam(int filePosition) {
		lastToken = UNDEF_ID;
		tc.addTokenRainbow(this, filePosition);
		this.lastPosition = filePosition; 
		super.functionPushParam(filePosition);
	}
	
	@Override
	public AppendableBuilderReader functionOpen(int filePosition) {
		//open function name....
		final AppendableBuilderReader name = super.functionOpen(filePosition);
		lastToken = UNDEF_ID;
		boolean isFunction = name.lookupExactMatch(localReader, functionNameParser)>=0;
		
		if (isFunction) {
			TypeColors type = net.terraarch.tf.parse.doc.TypeColors.FUNCTION;
			int endPos = filePosition-1;
			tc.addTokenSimple(this, type, endPos);
		} else {
			TypeColors type = net.terraarch.tf.parse.doc.TypeColors.UNDEFINED.self();
			String message = "Expected function name but found '"+name+"'";
			int endPos = filePosition-1;
			tc.addTokenTypeWithError(this, type, message, endPos);
		}
		this.lastPosition = filePosition-1;

		int idx = 0;
		String message = "Unexpected character, normal rainbow for context "+"function";
		tc.addTokenBlockRainbowStack(this, filePosition, idx, message);
		this.lastPosition = filePosition;
		
		return name;
	}
	@Override
	public void ternaryCondStart(int endPos) { //rainbow
		super.ternaryCondStart(endPos); // ?
		lastToken = UNDEF_ID;
		
		int idx = 0;
		String message = "Unexpected character, normal rainbow for context "+"ternary";
		tc.addTokenBlockRainbowStack(this, endPos, idx, message);
		
		
		this.lastPosition = endPos;
	}

	@Override
	public void ternaryCondMid(int endPos) { //rainbow
		super.ternaryCondMid(endPos); //  :
		lastToken = UNDEF_ID;
		tc.addTokenRainbow(this, endPos);

		tc.rainbowWrapperStack().get(tc.rainbowWrapperStack().size()-1).setSelection(1); //set the ? now that we have the :	 
		
		this.lastPosition = endPos;
	}
	@Override
	public void ternaryEnd(int endPos) {
		lastToken = UNDEF_ID;
		//we are done with the second expression so bump up one since we are out of the ternary
		tc.rainbowWrapperStack().remove(tc.rainbowWrapperStack().size()-1);			
		
	}
	
	@Override
	public String pushOperation(int endPos, String op) {
		lastToken = UNDEF_ID;
		TypeColors type = net.terraarch.tf.parse.doc.TypeColors.OPERATION;
		tc.addToken(this, endPos, type);
		this.lastPosition = endPos;
		return super.pushOperation(endPos, op);
	}
	
	@Override
	public int arrayOpen(int filePosition) { //rainbow
		lastToken = UNDEF_ID;
		int value = super.arrayOpen(filePosition);
		int idx = 0;
		String message = "Unexpected character, normal rainbow for context "+"array";
		tc.addTokenBlockRainbowStack(this, filePosition, idx, message);
		
		this.lastPosition = filePosition;
		return value;
	}

	@Override
	public int arrayClose(int endPos) { //rainbow
		int value = super.arrayClose(endPos);
		lastToken = UNDEF_ID;
		popAndSelectOne();	
		tc.addTokenRainbowEnd(this, endPos);
		this.lastPosition = endPos;
		
		return value;
	}
	public void popAndSelectOne() {
		tc.rainbowWrapperStack().remove(tc.rainbowWrapperStack().size()-1).setSelection(1);
	}

	@Override
	public void arrayItem(int endPos) { //rainbow
		lastToken = UNDEF_ID;
		super.arrayItem(endPos);
		tc.addTokenRainbow(this, endPos);
		this.lastPosition = endPos;
	}

	
	@Override
	public void parenOpen(int filePosition) { //rainbow
		lastToken = UNDEF_ID;
		super.parenOpen(filePosition);
		int idx = 0;
		String message = "Unexpected character, normal rainbow for context "+"parens";
		tc.addTokenBlockRainbowStack(this, filePosition, idx, message);
		this.lastPosition = filePosition;
	}
		
	@Override
	public void parenClose(int endPos) { //rainbow
		super.parenClose(endPos);
		lastToken = UNDEF_ID;
		tc.rainbowWrapperStack().remove(tc.rainbowWrapperStack().size()-1).setSelection(1);		
		tc.addTokenRainbowEnd(this, endPos);
		this.lastPosition = endPos;
		
	}
	
	@Override
	public String numberComplete(int endPos) {
		lastToken = UNDEF_ID;
		TypeColors type = net.terraarch.tf.parse.doc.TypeColors.NUMBER_OR_LITERAL;
		tc.addToken(this, endPos, type);
		this.lastPosition = endPos;
		return super.numberComplete(endPos);
	}
	
	@Override
	public BytesCollectorBase multiLineComment(int endPos) {
		tc.addToken(this, endPos, net.terraarch.tf.parse.doc.TypeColors.COMMENT);
		this.lastPosition = endPos;
		return super.multiLineComment(endPos);
	}
	
	@Override
	public File multiLineCommentFinish(int endPos) {
		tc.addToken(this, endPos, net.terraarch.tf.parse.doc.TypeColors.COMMENT);
		this.lastPosition = endPos;
		return super.multiLineCommentFinish(endPos);
	}

	@Override
	public BytesCollectorBase singleLineComment(int endPos) {
		tc.addToken(this, endPos, net.terraarch.tf.parse.doc.TypeColors.COMMENT);
		this.lastPosition = endPos;
		return super.singleLineComment(endPos);
	}

	@Override
	public File singleLineCommentFinish(int endPos) {
		tc.addToken(this, endPos, net.terraarch.tf.parse.doc.TypeColors.COMMENT);
		this.lastPosition = endPos;
		return super.singleLineCommentFinish(endPos);
	}
	
	
}
