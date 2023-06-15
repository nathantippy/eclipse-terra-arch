package net.terraarch.util;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;

public class BrowserUtils {

	private static final ILog logger = Platform.getLog(BrowserUtils.class);

	public static void launchURL(String url) {
	    if(Desktop.isDesktopSupported()){
	        try {
	            Desktop.getDesktop().browse(new URI(url));
	        } catch (IOException | URISyntaxException e) {
	           logger.error("unable to use desktop browser to open "+url+" "+e);
	        }
	    }else{
	        try {
	            Runtime.getRuntime().exec("xdg-open " + url);
	        } catch (IOException e) {
	        	logger.error("unable to use xdg-open "+url+" "+e);
	        }
	    }
	}
    
    
}
