package net.terraarch.quickfix;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IDocumentProvider;

import net.terraarch.tf.structure.StorageCache;
import net.terraarch.tf.structure.StructureDataModule;

import net.terraarch.TerraArchActivator;
import net.terraarch.index.IndexModuleFile;
import net.terraarch.menu.JumpToDef;

public class QuickFixerProviderVersion implements IMarkerResolutionGenerator {

	private final String provider;
	private final StructureDataModule module;
	
	
	public static class ProviderVersion implements IMarkerResolution {

		public static final ILog logger = Platform.getLog(ProviderVersion.class); 
		final String provider;
		final String version;
		final boolean useNew;
		
		public ProviderVersion(String provider, String version, boolean useNew) {
			this.provider = provider;
			this.version = version;
			this.useNew = useNew;
		}
		
		@Override
		public String getLabel() {
			if (useNew) {
				return provider+" = { ... version = \"~> "+version+"\"  }  ";
			} else {
				return provider+" = \"~> "+version+"\"";
			}
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
					   
					   //for 12 use...
					   /*
					   terraform {
						   required_providers {
						     aws = "~> 1.0"
						   }
						 }
					   */
					   
					   //for 13 and greater we must use this format
					   /* 
					    terraform {
						   required_providers {
						     mycloud = {
						       source  = "mycorp/mycloud"  #  "hashicorp/aws"
						       version = "~> 1.0"
						     }
						   }
						 }
					   */
					   //   module. isValidTerraformVersion
					   //TODO: B REFACTOR THIS INTO THE COMMON LIB
					   String terraformBlock = "terraform {\n";					   
					   int terraformPos = body.indexOf(terraformBlock);
					   
					   
					   String newText = "";
					   int offset = 0;
					   int length = 0;
	
					   if (useNew) {
						   String src = "hashicorp/"+provider;
						   
						   String requiredProvidersBlock = "required_providers {\n";				   
						   						   
						   if (-1==terraformPos) {
							   newText = "terraform {\n  required_providers {\n    "+provider+" = {\n"+
						                            "      source = \""+src+"\"\n"+
									                "      version = \"~> "+version+"\"\n"+
						                            "    }\n  }\n}\n";							   
						   } else {
							   
							   int reqPovPos = body.indexOf(requiredProvidersBlock);
							   if (-1==reqPovPos) {
								   
								   newText = "terraform {\n  required_providers {\n    "+provider+" = {\n"+
				                            "      source = \""+src+"\"\n"+
							                "      version = \"~> "+version+"\"\n"+
				                            "    }\n  }\n";							   
					   			   offset = terraformPos;
					   			   length = terraformBlock.length();
				   			   
							   } else {
								   
								   newText = "required_providers {\n    "+provider+" = {\n"+
				                            "      source = \""+src+"\"\n"+
							                "      version = \"~> "+version+"\"\n"+
				                            "    }\n";							   
					   			   offset = reqPovPos;
					   			   length = requiredProvidersBlock.length();
								   
							   }
						   }
					   } else {
						   //use old v12 style
						   if (-1==terraformPos) {
							   newText = "terraform {\n  required_providers {\n    "+provider+" = "+
						                                                      "\"~> "+version+"\"\n"+
			                                         "    }\n  }\n";	
							   
							   
						   } else {
							   newText = "terraform {\n  required_providers {\n    "+provider+" = "+
									   										"\"~> "+version+"\"\n"+
									   										"    }\n";						   
				   			   offset = terraformPos;
				   			   length = terraformBlock.length();
							   							   
						   }						   
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
	    	    logger.warn("provider version quick fix: ",e);
	    	}
		}
	}
	
	public QuickFixerProviderVersion(StructureDataModule module, String provider) {
		this.provider = provider;
		this.module = module;
	}
	
	@Override
	public IMarkerResolution[] getResolutions(IMarker arg0) {
		
		boolean is12 = false;		
		
		//only look for 12 if we have some constraints
		if (module.hasTerraformVersionConstraints()) {			
			int[] version12_x  = new int[] {0, 12, 0};
			//any version 12 will do we just need to find if one is valid
			int v = 33; //check all the known versions of 12
			while (--v >= 0 && !is12) {
				version12_x[2]=v;
				is12 |= module.isValidTerraformVersion("", version12_x);			
			}
		}
		
		boolean useNew = !is12;//if TF version >=13 but that is a moving target so we assume not 12
		
		final List<IMarkerResolution> resolutions = new ArrayList<IMarkerResolution>();
		StorageCache storageCache = TerraArchActivator.getDefault().storageCache(); //add one for each version
	//	storageCache.visitVersions(provider, ver-> {
	//		resolutions.add(new ProviderVersion(provider,ver, useNew));			
	//	});
		//build reverse array so the new version is at the top of the list
		IMarkerResolution[] local = new IMarkerResolution[resolutions.size()];
		int w = resolutions.size();
		int x = 0;
		while (--w>=0) {
			local[x++]=resolutions.get(w);
		}
		return local;
	}

}
