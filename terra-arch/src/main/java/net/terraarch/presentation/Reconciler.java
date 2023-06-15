package net.terraarch.presentation;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;

//  ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();

public class Reconciler extends PresentationReconciler {

	static final ILog logger = Platform.getLog(Reconciler.class);
		
    public Reconciler() {
		
        this.setDamager(new TFPresentationDamager(), IDocument.DEFAULT_CONTENT_TYPE);
        this.setRepairer(new DefaultDamagerRepairer(new ReconcilerScanner()), IDocument.DEFAULT_CONTENT_TYPE);
        
    }

	   

}