package net.terraarch.terraform.parse;

import net.terraarch.terraform.parse.ParseState.NAMESPACES;
import net.terraarch.terraform.structure.StructureDataModule;
import net.terraarch.util.AppendableBuilderReader;

public class ExpressionBuilderTree extends ExpressionBuilder {

	private TFExpression root = new TFExpression(null);
	private final StructureDataModule structureDataModule;
	private boolean build = true; //set to false if we do not wish to build anything.
	
	public ExpressionBuilderTree(StructureDataModule structureDataModule) {
		this.structureDataModule = structureDataModule;
	}
	
	public void enableBuild(boolean value) {
		this.build = value;
	}
	
	@Override
	public void clear() {
		root = new TFExpression(null);
	}
	
	@Override
	public TFExpression getExpressionRoot() {		
		return build ? root : null;
	}

	
	////////////////
	
	@Override
	public void textBegin(int filePosition, byte[] quotes) {
		if (build) {
			TFExpression item = new TFExpressionText(root, filePosition, quotes);
			root.add(item);
			root=item;	
		}
	}
	@Override
	public void appendTextByte(int b) {
		if (build) {
			super.appendTextByte(b);
			((TFExpressionText)root).appendTextByte(b);	
		}
	}
	@Override
	public AppendableBuilderReader textEnd(int endPos, AppendableBuilderReader reader) {
		AppendableBuilderReader textEnd = super.textEnd(endPos, reader);
		if (build) {
			((TFExpressionText)root).appendText(textEnd.toBytes());	
			((TFExpressionText)root).finish(endPos);
			root=root.parent;
		}
		return textEnd;
	}

	////////
	
	@Override
	public String interpolateOpen(int filePosition, int size, String text) {
		if (build) {
			TFExpression item = new TFExpressionInterpolate(root, filePosition, size);
			root.add(item);
			root=item;
		}
		return super.interpolateOpen(filePosition, size, text);
	}
	@Override
	public void interpolateClose(int filePosition) {
		if (build) {
			if (root instanceof TFExpressionInterpolate) {
				((TFExpressionInterpolate)root).closePos(filePosition);
			} else {
				//for Iterator
				((TFExpressionIterator)root).closePos(filePosition);
			}
			root=root.parent;
			super.interpolateClose(filePosition);
		}
	}

	//////////

	
	
	@Override
	public void parenOpen(int filePosition) {
		if (build) {
			TFExpression item = new TFExpressionParens(root, filePosition);
			root.add(item);
			root=item;	
		}
	}
	@Override
	public void parenClose(int filePosition) {
		if (build) {
			((TFExpressionParens)root).closePos(filePosition);
			root=root.parent;
		}
	}
		////////
	
	@Override
	public int arrayOpen(int filePosition, int depth) {
		if (build) {
			TFExpression item = new TFExpressionArray(root);
			root.add(item);
			root=item;
		}
		return super.arrayOpen(filePosition, depth);
	}
	
	@Override
	public void arrayItem(int filePosition) {	
		if (build) {
			((TFExpressionArray)root).arrayItem(filePosition);
		}
	}

	@Override
	public int arrayClose(int filePosition, int depth) {
		if (build) {
			root=root.parent;
		}
		return super.arrayClose(filePosition, depth);
	}

	/////////////
	
	@Override
	public void numberBegin(int filePosition) {
		if (build) {
			TFExpression item = new TFExpressionNumber(root, filePosition);
			root.add(item);
			root=item;
		}
		super.numberBegin(filePosition);
	}
	@Override
	public void pushNumber(int filePosition, long m, int e) {
		if (build) {
			((TFExpressionNumber)root).setValue(m,e,filePosition);
			super.pushNumber(filePosition, m, e);
		}
	}
	@Override
	public void pushNumber(int filePosition, long i) {
		if (build) {
			((TFExpressionNumber)root).setValue(i,0,filePosition);
			super.pushNumber(filePosition, i);
		}
	}
	@Override
	public void pushNumberSciNotation(int filePosition, String text, long e) {
		if (build) {
			((TFExpressionNumber)root).setSci(text,e,filePosition);
			super.pushNumberSciNotation(filePosition, text, e);
		}
	}

	@Override
	public String numberComplete(int filePosition, String numberText) {
		if (build) {
			((TFExpressionNumber)root).finish(numberText,filePosition);
			root=root.parent;
		}
		return super.numberComplete(filePosition, numberText);
	}
	
	///////////

	
	 
	@Override
	public AppendableBuilderReader functionOpen(int filePosition, AppendableBuilderReader name) {
		if (build) {
			TFExpression item = new TFExpressionFunction(root, filePosition, name.toBytes());
			root.add(item);
			root=item;	
		}
		return super.functionOpen(filePosition, name);
	}

	@Override
	public void functionPushParam(int filePosition) {
		if (build) {
			((TFExpressionFunction)root).pushParam(filePosition);
			super.functionPushParam(filePosition);
		}
	}
	
	@Override
	public void functionEnd(int filePosition) {
		if (build) {
			((TFExpressionFunction)root).finish(filePosition);
			root=root.parent;
			super.functionEnd(filePosition);
		}
	}

	@Override
	public void functionEndWithArrayAsArgs(int filePosition) {
		if (build) {
			((TFExpressionFunction)root).finishAsArrayArgs(filePosition);
			root=root.parent;
			super.functionEndWithArrayAsArgs(filePosition);
		}
	}

	///////////
	
	public void reference(NAMESPACES nameSpace, BlockType type, int nameParts, int endPos2,
			AppendableBuilderReader reader2, int endPos1, AppendableBuilderReader reader1) {
		if (build) {
			root.add(new TFExpressionReference(root, nameSpace, type, nameParts, endPos2, reader2, endPos1, reader1, structureDataModule));
		}
	}
	
	@Override
	public String pushOperation(int filePosition, String op) {
		if (build) {
			root.add(new TFExpressionOperator(root, filePosition, op));
		}
		return super.pushOperation(filePosition, op);
	}
	
	////////
	
	@Override
	public int forStart(int filePosition, int depth) {
		if (build) {
			TFExpression item = new TFExpressionFor(root, filePosition, depth);
			root.add(item);
			root=item;
		}
		return super.forStart(filePosition, depth);
	}
	@Override
	protected void collectForArg(int depth, int endPos, AppendableBuilderReader reader) {
		if (build) {
			((TFExpressionFor)root).collectForArg(depth, endPos, reader.toBytes());
			super.collectForArg(depth, endPos, reader);
		}
	}
	@Override
	public void forComma(int filePosition) {
		if (build) {
			((TFExpressionFor)root).forComma(filePosition);
			super.forComma(filePosition);
		}
	}
	@Override
	public void forIn(int filePosition) {
		if (build) {
			((TFExpressionFor)root).forIn(filePosition);
			super.forIn(filePosition);
		}
	}
	@Override
	public int forEnd(int filePosition, int depth) {
		if (build) {
			((TFExpressionFor)root).forEnd(filePosition);
			root=root.parent;
		}
		return super.forEnd(filePosition, depth);
	}


	
	/////////
	
	@Override
	public void iterateOpen(int filePosition, int size) {
		if (build) {
			TFExpression item = new TFExpressionIterator(root, filePosition, size);
			root.add(item);
			root=item;
			super.iterateOpen(filePosition, size);
		}
	}

	@Override
	public void iterElse() {
		if (build) {
			((TFExpressionIterator)root).iterElse();
			super.iterElse();
		}
	}

	@Override
	public void iterEndIf() {
		if (build) {
			((TFExpressionIterator)root).iterEndIf();
			super.iterEndIf();
		}
	}

	@Override
	public void iterEndFor() {
		if (build) {
			((TFExpressionIterator)root).iterEndFor();
			super.iterEndFor();
		}
	}

	@Override
	public void iterIf() {
		if (build) {
			((TFExpressionIterator)root).iterIf();
			super.iterIf();
		}
	}


	// */

	
	

}
