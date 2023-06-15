package net.terraarch.index;
import java.lang.reflect.Field;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.IDocument;

public class IndexUtil {
	
	private static final ILog logger = Platform.getLog(IndexUtil.class);



	public static IndexModuleFile extractModuleFileService(IDocument doc) {
		//reflect into doc, to find the module assoicated directly 
		if (doc instanceof AbstractDocument) {
			try {
				Field[] fields = AbstractDocument.class.getDeclaredFields();
				int x = -1;
				while (++x < fields.length ) {
					final Field field = fields[x];
					if (field.getType() == ListenerList.class) {								
						field.setAccessible(true);
						for(Object item: (ListenerList<?>)field.get(doc)) {
							if (item instanceof IndexModuleFile) {
								return ((IndexModuleFile)item);
							}
						}
					}
				}
			} catch (SecurityException e) {
				logger.error("unable to lookup module ",e);
			} catch (IllegalArgumentException e) {
				logger.error("unable to lookup module ",e);
			} catch (IllegalAccessException e) {
				logger.error("unable to lookup module ",e);
			}
		}
		//////////////////////////////////////////////////////////////////////////////////////
		//this editor was launched "possibly on startup" but it never had a listener attached
		//this happens when a file is opened which is NOT inside the workspace, in that case
		//the project can not be resolved so everything is assumed OK.
		//////////////////////////////////////////////////////////////////////////////////////
		
	    return null;
	}
}

