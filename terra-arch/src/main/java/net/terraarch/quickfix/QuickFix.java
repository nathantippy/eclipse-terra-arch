package net.terraarch.quickfix;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IDocumentProvider;

import net.terraarch.menu.JumpToDef;

public class QuickFix implements IMarkerResolution {
    String label;
    public QuickFix(String label) {
       this.label = label;
    }
    public String getLabel() {
       return label;
    }
    public void run(IMarker marker) {
    	
    	IPath path = marker.getResource().getLocation();
    	MessageDialog.openInformation(null, "QuickFix Demo",
    			"This quick-fix is not yet implemented: "+String.valueOf(path));

    	try {	  
    	       	   
	    	IWorkspaceRoot ws = ResourcesPlugin.getWorkspace().getRoot();
	  		IFile iFile = ws.getFileForLocation(path);
  		if (null==iFile) {
  			new NullPointerException("path: "+path).printStackTrace();
  			return;
  		}
		
			IWorkbench workbench = PlatformUI.getWorkbench();				
			IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
			JumpToDef.visitFileEditor(activeWorkbenchWindow .getActivePage(),iFile,editor->{
			
				editor.selectAndReveal(0, 1);
				
				   IDocumentProvider dp = editor.getDocumentProvider();
				   IDocument doc = dp.getDocument(editor.getEditorInput());
				   
				   /*
				   terraform {
					   required_providers {
					     aws = ">= 2.52.0"
					   }
					   required_version = "~> 0.12.0"
				   }
				   */ //if terraform {is found use it else inject the full body.
				   
				   
				   int offset = 0;
				   int length = 0;
				   String newText = "#this is some text\n";
				   try {
					   doc.replace(offset, length, newText);
				   } catch (BadLocationException e) {
						throw new RuntimeException(e);
				   }
				  				
			});
				
    	     } catch (Exception e) {
    	    	 e.printStackTrace();
    	     }
    
    }
 }