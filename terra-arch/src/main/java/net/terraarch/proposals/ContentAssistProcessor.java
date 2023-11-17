package net.terraarch.proposals;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.custom.StyledText;

import net.terraarch.tf.parse.ParseBuffer;
import net.terraarch.tf.structure.walker.GatherInBoundProposals;

import net.terraarch.TerraArchActivator;
import net.terraarch.index.IndexModuleFile;
import net.terraarch.outline.OutlineFactory;

public class ContentAssistProcessor implements IContentAssistProcessor {

	private static final ILog logger = Platform.getLog(ContentAssistProcessor.class);
	private ParseBuffer parseBuffer = new ParseBuffer();
	
	private GatherInBoundProposals<GatherUniqueVisitor> gp;
    @Override
    public ICompletionProposal[] computeCompletionProposals(final ITextViewer viewer, int cursorPosition) {
    	try {
	    	addCaretFix(viewer);
	    	
	    	//ILog logger = Platform.getLog(ContentAssistProcessor.class);
	    	//logger.info("This is an info message");
	    	//logger.warn("This is a warning message");

	    	    	
	    	IDocument document = viewer.getDocument();
			IndexModuleFile extractModuleFileService = IndexModuleFile.extractModuleFileService(document);
			
			if (null != extractModuleFileService) {
				
				gp = new GatherInBoundProposals<GatherUniqueVisitor>(
								                  extractModuleFileService.module, 
								                  cursorPosition, 
								                  new GatherUniqueVisitor(cursorPosition), 
								                  false, TerraArchActivator.getDefault().sdmm); 
				
				parseBuffer.tokenizeDocument(document.get().getBytes(), gp);    	
		  		
				
				//we still show the proposals, but we may need to show an indication that we do not have it all.
		    	int count = gp.guv().proposals.size();
		    	//logger.error("building proposals list found: "+count+" proposals");
		    	
				return gp.guv().proposals.toArray(new ICompletionProposal[count]);
			} else {
				//logger.error("no proposals use to no IndexModuleFile object");
				return new ICompletionProposal[0];
			}
    	} catch (Throwable t) {
    		logger.error("computeCompletionProposals",t);
    		return new ICompletionProposal[0];
    	}
    }

    //note: in the future add this to other editors?
	public static void addCaretFix(final ITextViewer viewer) {
		try {
		//this is critical due to a bug in eclipse, after content assent used by the mouse
		//the caret is no longer painted, to reolve this we must reset it.		
		 	
		final StyledText textWidget = viewer.getTextWidget();    	
    	viewer.addTextListener(new ITextListener() {
			@Override
			public void textChanged(TextEvent event) {					
				textWidget.setCaret(textWidget.getCaret());
  				viewer.removeTextListener(this);
			}
    	});
		} catch (Throwable t) {
			logger.error("addCaretFix",t);
		}
	}

    @Override
    public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
        return gp.guv().context.toArray(new IContextInformation[gp.guv().context.size()]);
    }

    @Override
    public char[] getCompletionProposalAutoActivationCharacters() { 
        return new char[] { '.','_'}; // eg  var. local. aws_ azure_ 
    }

    @Override
    public char[] getContextInformationAutoActivationCharacters() {
        return null;
    }

    @Override
    public String getErrorMessage() {
        return null;
    }

    @Override
    public IContextInformationValidator getContextInformationValidator() {
        return new IContextInformationValidator() {
			
			@Override
			public boolean isContextInformationValid(int arg0) {
				return true;
			}
			
			@Override
			public void install(IContextInformation arg0, ITextViewer arg1, int arg2) {
			}
		};
    }

}