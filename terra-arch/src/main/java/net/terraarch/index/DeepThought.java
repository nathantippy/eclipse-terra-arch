package net.terraarch.index;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

import net.terraarch.DeepReview;
import net.terraarch.TerraArchActivator;
import net.terraarch.preferences.TerraArchPreferencesPages;
import net.terraarch.tf.parse.doc.DocumentTokenMap;
import net.terraarch.tf.structure.StructureDataFile;
import net.terraarch.tf.structure.StructureDataModule;
import net.terraarch.util.FileUtils;
import net.terraarch.util.ReactiveJobState;

public final class DeepThought extends ReactiveJobState {
		
			public DeepThought(StructureDataModule module, String name, int period) {
				super(name, period);
				this.module = module;
			}
			
			private final StructureDataModule module;
			private final Set<String> doneFileNames = new HashSet<String>();
	
			@Override
			public void run() {
				DeepReview.deepReview(module, doneFileNames);
				doneFileNames.clear();
			}
	
			public void setDoneEditors(Set<String> processMarksInOpenEditors) {
				doneFileNames.addAll(processMarksInOpenEditors);
			}
	}