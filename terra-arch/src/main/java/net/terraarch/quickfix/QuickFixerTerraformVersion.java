package net.terraarch.quickfix;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IDocumentProvider;

import net.terraarch.TerraArchActivator;
import net.terraarch.index.IndexModuleFile;
import net.terraarch.menu.JumpToDef;

public class QuickFixerTerraformVersion implements IMarkerResolutionGenerator  {

	public static class TerraformVersion implements IMarkerResolution {
		public static final ILog logger = Platform.getLog(TerraformVersion.class); 

		final String version;
		public TerraformVersion(String version) {
			this.version = version;
		}
		
		@Override
		public String getLabel() {
			return "terraform{ required_version = \""+version+"\"}";
		}

		@Override
		public void run(IMarker marker) {
				
			try {	  
				
			    IPath path = marker.getResource().getLocation();	   	    	       	   
		    	IWorkspaceRoot ws = ResourcesPlugin.getWorkspace().getRoot();
		  		IFile iFile = ws.getFileForLocation(path); 					
				IWorkbench workbench = PlatformUI.getWorkbench();				
				IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
				JumpToDef.visitFileEditor(activeWorkbenchWindow .getActivePage(),iFile,editor->{
				
					
					   IDocumentProvider dp = editor.getDocumentProvider();
					   IDocument doc = dp.getDocument(editor.getEditorInput());

					   
					   String body = doc.get();
					   
					   /*
					   terraform {
						   required_providers {
						     aws = ">= 2.52.0"
						   }
						   required_version = "~> 0.12.0"
					   }
					    */ //if terraform {is found use it else inject the full body.

					   String open = "terraform {\n";					   
					   int pos = body.indexOf(open);
					   					   
					   String newText; 
					   int offset = 0;
					   int length = 0;
					   
					   if (pos==-1) {
						   newText = "terraform {\n  required_version = \""+version+"\"\n}\n";
					   } else {
						   //we have existing terraform block						   
						   offset = pos;						   
						   length = open.length();						   
						   newText = "terraform {\n  required_version = \""+version+"\"\n";
					   }
					   try {
						   doc.replace(offset, length, newText);						   
						   editor.selectAndReveal(offset, newText.length());
						   dp.changed(doc);
					   } catch (BadLocationException e) {
						   throw new RuntimeException(e);
					   }			   
					   
					  				
				});
					
	    	} catch (Exception e) {
	    		logger.warn("Terraform quick fix error: ",e);
	    	}
	    
		}
		
	}
	
	
	@Override
	public IMarkerResolution[] getResolutions(IMarker arg0) {

		return new IMarkerResolution[] {	
				new TerraformVersion("~> 1.0.0"),
				new TerraformVersion("~> 0.15.0"), //NOTE: update once 16 is started
				new TerraformVersion("~> 0.14.0"),
				//NOTE: remove these older versions in 2022
				new TerraformVersion("~> 0.13.0"),
                new TerraformVersion("~> 0.12.0") //oldest version we will support with fixes
				                		
		
		};
	}

}
