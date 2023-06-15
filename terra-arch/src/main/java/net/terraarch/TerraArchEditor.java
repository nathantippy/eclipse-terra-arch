package net.terraarch;

import org.eclipse.ui.internal.genericeditor.ExtensionBasedTextEditor;

@SuppressWarnings("restriction")
public class TerraArchEditor extends ExtensionBasedTextEditor {
		
	@Override
	protected void initializeKeyBindingScopes() {	
		setKeyBindingScopes(new String[] {"net.terraarch.key.context"});

	}

	//   can debug and watch prop changes here
//	@Override
//	protected void initializeEditor() {
//		
//		super.initializeEditor();
//		
//		//this.addPropertyListener(null);
//		
//		//ICursorListener cursorListener = this.getCursorListener();
//		//this.addPropertyListener(null);	
//		IPropertyChangeListener listener = new IPropertyChangeListener() {
//
//			@Override
//			public void propertyChange(PropertyChangeEvent arg0) {
//				System.err.println("prop change: "+arg0);
//			}
//			
//		};
//		this.addPartPropertyListener(listener);
//		
//	}
	
}


