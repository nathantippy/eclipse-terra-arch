package net.terraarch.presentation;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import  org.eclipse.jface.text.presentation.*;

public class TFPresentationDamager implements IPresentationDamager{

	private IDocument doc;
	
	@Override
	public IRegion getDamageRegion(ITypedRegion partition, DocumentEvent event, boolean documentPartitioningChanged) {
		
		//TF supports foreward references so any change may change the highlight of previous lines
		//since we parse everything we can also afford to paint everything.
		return new Region(0, doc.getLength());
	}

	@Override
	public void setDocument(IDocument doc) {
		this.doc = doc;
	}

}
