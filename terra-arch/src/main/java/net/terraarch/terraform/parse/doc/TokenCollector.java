package net.terraarch.terraform.parse.doc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.terraarch.terraform.parse.TermLayer;
import net.terraarch.util.AppendableBuilderReader;


public class TokenCollector implements Serializable  {
	
    private TokenSelector lastIdentTokenBlock = null;    
	private final List<TokenSelector> tokenBlocks = new ArrayList<TokenSelector>();
	private final List<TokenSelector> rainbowWrapperStack = new ArrayList<TokenSelector>();
	
	public List<TokenSelector> tokenBlocks() {
		return tokenBlocks;
	}
	public List<TokenSelector> rainbowWrapperStack() {
		return rainbowWrapperStack;
	}
		
    public TokenSelector getLastIdentTokenBlock() {
		return lastIdentTokenBlock;
	}
	public void setLastIdentTokenBlock(TokenSelector lastIdentTokenBlock) {
		this.lastIdentTokenBlock = lastIdentTokenBlock;
	}
	
	
	private static TokenSelector addToken(DocumentTokenMap that, TokenSelector token) {
		//safety to ensure that the list never contains an overlap
		if (token.stop >= token.start && token.start>=that.tokenLastPos()) {
			that.tc.tokenBlocks.add(token);	    		
		}
		that.tokenLastPos(token.stop);
		if (token.stop > that.getDataSizeLimit()) {
			new UnsupportedOperationException("Token stop: "+token.stop+" out of bounds, file len "+that.getDataSizeLimit()).printStackTrace();;
		}
		return token;
	}

	//////////////////////////////////////////////////////
	
	static TokenSelector tokenBlockBuilderNominalIdent(DocumentTokenMap documentTokenMap, int endPos, int extraChar, int t1, int t2, int t3) {
		int[] tokenOrdinals = { t1, t2, t3 };
		int w = tokenOrdinals.length;
		int[] result = new int[w]; //TODO: this is not GC free, we should revisit.
		while (--w >= 0) {
			result[w] = token(documentTokenMap.textColor().ordinal(),tokenOrdinals[w]);
		}
		return new TokenSelector(documentTokenMap.lineNumber(), documentTokenMap.lastPosition-extraChar, endPos, result);
	}

	static TokenSelector tokenBlockBuilderRainbow(DocumentTokenMap that, int endPos) {
		int colorOrdinal = that.textColor().ordinal();
		int[] rainbowTokens = new int[] {token(colorOrdinal,TypeColors.UNDEFINED.ordinal()),
			                rainbowCache(colorOrdinal,that.tc.rainbowWrapperStack.size())
		};
		return new TokenSelector(that.lineNumber(), that.lastPosition, endPos, rainbowTokens);
	}
	
	static TokenSelector tokenBlockBuilderIdent(DocumentTokenMap documentTokenMap, int endPos, TermLayer termLayer, final int termLayerNo, AppendableBuilderReader value, StringBuilder errorMessage) {
		return new TokenSelector(documentTokenMap.lineNumber(), documentTokenMap.lastPosition, endPos,				
				token(documentTokenMap.textColor().ordinal(),
				TermProcessingImpl.usageColorToken(documentTokenMap, value, termLayerNo, termLayer, errorMessage))				
				);
	}

	static TokenSelector tokenBlockBuilderOrd(DocumentTokenMap documentTokenMap, int endPos, int ord) {
		return new TokenSelector(documentTokenMap.lineNumber(), documentTokenMap.lastPosition, endPos, new int[] {token(documentTokenMap.textColor().ordinal(),ord)});
	}

	static TokenSelector rainbowTokenBlockEnd(DocumentTokenMap documentTokenMap, int endPos) {
		return new TokenSelector(documentTokenMap.lineNumber(),  documentTokenMap.lastPosition, endPos, rainbowCache(documentTokenMap.textColor().ordinal(),documentTokenMap.tc.rainbowWrapperStack.size()));
	}

	static TokenSelector rainbowTokenBlock(DocumentTokenMap documentTokenMap, int endPos) {
		return new TokenSelector(documentTokenMap.lineNumber(), documentTokenMap.lastPosition, endPos, rainbowCache(documentTokenMap.textColor().ordinal(),documentTokenMap.tc.rainbowWrapperStack.size()-1));
	}


	public static int token(int colorOrdinal, int tokenOrdinal) {
		//return tokens[colorOrdinal][tokenOrdinal];
		return  (colorOrdinal<<16) | (0xFFFF&tokenOrdinal);
	}
	
	public static int rainbowCache(int colorOrdinal, int tokenOrdinal) {
		//return rainbowCache[colorOrdinal][tokenOrdinal];
		return (1<<24) | (colorOrdinal<<16) | (0xFFFF&tokenOrdinal);
	}
	
	//////////////////////////////////////////////////

	public void addToken(DocumentTokenMap documentTokenMap, int endPos, TypeColors type) {
		TokenCollector.addToken(documentTokenMap, TokenCollector.tokenBlockBuilderOrd(documentTokenMap, endPos, type.ordinal()));
	}

	public void addTokenRainbow(DocumentTokenMap documentTokenMap, int endPos) {
		TokenCollector.addToken(documentTokenMap, TokenCollector.rainbowTokenBlock(documentTokenMap, endPos));
	}

	public void addTokenRainbowEnd(DocumentTokenMap documentTokenMap, int endPos) {
		TokenCollector.addToken(documentTokenMap, TokenCollector.rainbowTokenBlockEnd(documentTokenMap, endPos));
	}

	public void addTokenWithOptionalError(DocumentTokenMap documentTokenMap, int colorKey, boolean isErr, String message, int endPos2) {
		TokenCollector.addToken(documentTokenMap, TokenCollector.tokenBlockBuilderOrd(documentTokenMap, endPos2, colorKey).err(isErr, message) );
	}

	public void addTokenTypeWithError(DocumentTokenMap documentTokenMap, TypeColors type, String message, int endPos) {
		TokenCollector.addToken(documentTokenMap, (TokenCollector.tokenBlockBuilderOrd(documentTokenMap, endPos, type.ordinal()).err(message)));
	}

	public void addTokenSimple(DocumentTokenMap documentTokenMap, TypeColors type, int endPos) {
		TokenCollector.addToken(documentTokenMap, TokenCollector.tokenBlockBuilderOrd(documentTokenMap, endPos, type.ordinal()));
	}

	public void addTokenIdent(DocumentTokenMap documentTokenMap, int endPos, TermLayer termLayer, final int termLayerNo, AppendableBuilderReader value, StringBuilder errorMessage, boolean isErr, String messageLocal) {
		TokenCollector.addToken(documentTokenMap, TokenCollector.tokenBlockBuilderIdent(documentTokenMap, endPos, termLayer, termLayerNo, value, errorMessage).err(isErr, messageLocal));
	}

	public void addTokenForStart(DocumentTokenMap that, int endPos, int idx, String message) {
		that.tc.rainbowWrapperStack.add(TokenCollector.tokenBlockBuilderRainbow(that, endPos).err(idx, message) );
	}

	public void addTokenBlockRainbowStack(DocumentTokenMap that, int endPos, int idx, String message) {
		TokenSelector tb = (tokenBlockBuilderRainbow(that, endPos).err(idx, message)   );
		addToken(that, tb);
		that.tc.rainbowWrapperStack.add(tb);
	}

	public void addTokenBlockNomIdent(DocumentTokenMap documentTokenMap, int endPos, int extraChar, int t1, int t2, int t3, String msg1, String msg2) {
		this.lastIdentTokenBlock = TokenCollector.addToken(documentTokenMap, TokenCollector.tokenBlockBuilderNominalIdent(documentTokenMap, endPos, extraChar, t1, t2, t3)
												 .err(msg1.length()>0, 0, msg1)
												 .err(msg2.length()>0, 1, msg2));
	}
	public void popRainbow() {
		if (rainbowWrapperStack().size()>0) {
	    	rainbowWrapperStack().remove(rainbowWrapperStack().size()-1);	
	    }
	}
	
}
