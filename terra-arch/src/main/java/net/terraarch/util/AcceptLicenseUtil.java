package net.terraarch.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.prefs.BackingStoreException;

import net.terraarch.util.AppendableBuilder;
import net.terraarch.util.Appendables;
import net.terraarch.terraform.parse.doc.DocumentTokenMap;

import net.terraarch.LicenseDisplayDialog;
import net.terraarch.TerraArchActivator;
import net.terraarch.preferences.TerraPreferences;

public class AcceptLicenseUtil {


	private static final ILog logger = Platform.getLog(AcceptLicenseUtil.class);
	



	public static AppendableBuilder copyrightText() {
		InputStream stream = AcceptLicenseUtil.class.getResourceAsStream("/copyright.txt");			
		AppendableBuilder builder = new AppendableBuilder();
		byte[] buffer = new byte[1024];
		int len = -1;
		try {
				while (-1 != (len=stream.read(buffer))) {
					builder.write(buffer, 0, len);
				}
								
		} catch (Exception e) {
			logger.error("unable to load license",e);
			return null;
		}
		return builder;
	}

	public static AppendableBuilder licenseText() {
		InputStream stream = AcceptLicenseUtil.class.getResourceAsStream("/license.txt");			
		AppendableBuilder builder = new AppendableBuilder();
		byte[] buffer = new byte[1024];
		int len = -1;
		try {
			while (-1 != (len=stream.read(buffer))) {
				builder.write(buffer, 0, len);
			}

		} catch (IOException e) {
			logger.error("unable to load license",e);
			return null;
		}
		return builder;
	}

	public static void unInstall() {
		Bundle bundle = FrameworkUtil.getBundle(DocumentTokenMap.class);
		if (null!=bundle) {
			try {
				bundle.uninstall();
			} catch (BundleException e) {
				logger.error("unable to uninstall"+e.getMessage());
			}
		}
	}
	
	
}
