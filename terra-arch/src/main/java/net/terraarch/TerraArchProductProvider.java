package net.terraarch;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.IProductProvider;
import org.eclipse.ui.branding.IProductConstants;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import net.terraarch.terraform.parse.ParseState;

import net.terraarch.presentation.TFPresentationDamager;

//not used but do not delete, we need to find the right Bundle to make this work.
//if not by next week then ship with note that the build number is in the properties..
public class TerraArchProductProvider implements IProductProvider {

	@Override
	public String getName() {
		return "Terra Architect";
	}

	@Override
	public IProduct[] getProducts() {
				
		//IPlatformConfiguration configuration = 
		//                 ConfiguratorUtils.getCurrentPlatformConfiguration();
		//configuration.getConfiguredFeatureEntries()[0]	
		
		return new IProduct[] {new IProduct() {

			@Override
			public String getApplication() {
				return "org.eclipse.ui.ide.workbench";
			}

			@Override
			public Bundle getDefiningBundle() {
				return FrameworkUtil.getBundle(TFPresentationDamager.class);
			}

			@Override
			public String getDescription() {
				return "Terra Architect IDE product";
			}

			@Override
			public String getId() {
				return "terraarch";
			}

			@Override
			public String getName() {
				return "Terra Architect";
			}

			@Override
			public String getProperty(String name) {
			
				switch(name){
					case IProductConstants.ABOUT_IMAGE:
						return "./images/250x330.png";
					case IProductConstants.ABOUT_TEXT:
						return 
		"Terra Architect with other components, an IDE for Terraform development\n"+
		"\n"+
		"Terra Architect and the Terra Architect logo are trademarks of KMF Enterprises LLC, terraarch.net.\n"+
		"Terra Architect logos cannot be altered without KMF Enterprises permission.\n"+
		"Other names may be trademarks of their respective owners.\n"+
		"\n"+
		"This product includes software developed by other open source projects including the Apache Software Foundation, https://www.apache.org/.\n"+
		"\n";
						
					case IProductConstants.WINDOW_IMAGES:
						return "./images/16x16.png,./images/32x32.png,./images/48x48.png,./images/64x64.png,./images/128x128.png,./images/256x256.png";
					default:
						return null;
				}
			}		
		}  };
	}

}
