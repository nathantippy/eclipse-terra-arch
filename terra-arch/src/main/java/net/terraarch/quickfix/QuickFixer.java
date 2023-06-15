package net.terraarch.quickfix;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;

public class QuickFixer implements IMarkerResolutionGenerator {
	
	private static IMarkerResolution[] empty = new IMarkerResolution[0]; 
		
    public IMarkerResolution[] getResolutions(IMarker mk) {
    	   	
       try {
    	   IMarkerResolutionGenerator fixes = (IMarkerResolutionGenerator)mk.getAttribute("QuickFixGenerator");
    	   return fixes!=null ? fixes.getResolutions(mk) : empty;
       }
       catch (CoreException e) {
           return empty;
       }
    }
    
 }