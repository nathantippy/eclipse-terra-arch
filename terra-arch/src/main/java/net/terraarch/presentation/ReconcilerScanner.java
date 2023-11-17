package net.terraarch.presentation;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;


import net.terraarch.tf.parse.ParseBuffer;
import net.terraarch.tf.parse.doc.DocumentTokenMap;
import net.terraarch.tf.parse.doc.ProviderConstraintImpl;
import net.terraarch.tf.parse.doc.ThemeColors;
import net.terraarch.tf.parse.doc.TokenCollector;
import net.terraarch.tf.parse.doc.TokenSelector;
import net.terraarch.tf.parse.doc.TypeColors;
import net.terraarch.tf.structure.StructureDataModule;

import net.terraarch.TerraArchActivator;
import net.terraarch.index.IndexModuleFile;
import net.terraarch.preferences.TerraPreferences;

public final class ReconcilerScanner implements ITokenScanner {
	
		private List<TokenSelector> tokenList;
		private int pos;
		private int len;
		private int off;
		private int end;
		private int size;
		private ParseBuffer buffer = new ParseBuffer();
		private IndexModuleFile activeFileServce;
		private Object lock = new Object();
		private final AtomicBoolean adjustBounds = new AtomicBoolean();

		@Override
		public int getTokenLength() {
			//System.out.println("                        len: "+len);
			return len;
		}

		@Override
		public int getTokenOffset() {
			//System.out.println("         off: "+off);
			return off;
		}

		@Override
		public IToken nextToken() {
			synchronized(lock) {
				if (pos>=tokenList.size()) {
					if (off+len<end) {
						
						off = off+len;
						len = 1;//(size-off);//1;//NOTE: old design (size-off); //we are marking the reset of the file as unknown in red
						end = off+len;//adjust end so we send EOF next time
						return new Token(new TextAttribute(new Color(Display.getCurrent(), LocalRGB.get(TypeColors.UNDEFINED))));
					} else {	
						return Token.EOF;
					}
				}
				TokenSelector block = tokenList.get(pos++);
				len = block.stop - block.start;
				off = block.start;	
				if (len+off > end) {
					len = end-off;//coorect and sure we do not pass end...  required to prevent arg error in eclipse paint
				}
				
				Token result = TokenProvider.get(block.getTokenId());
//				if (null==result) {
//					throw new NullPointerException("no token in TokenBlock at "+(pos-1)+" in array of "+tokens.size());
//				}
				//System.out.println("token: "+ ((TextAttribute)result.getData()).getForeground()  );
				return result;
			}
		}

		@Override
		public void setRange(IDocument doc, int start, int length) {	
			synchronized(lock) {		
				try {
					long now = System.currentTimeMillis();
					activeFileServce = IndexModuleFile.extractModuleFileService(doc);
					boolean colorMatches = true;
					
					boolean moduleIsUnchanged = true;
				
					final ThemeColors textColors = ThemeColors.values()[TerraArchActivator.getDefault().getTextColorsId() ];
					
			//TODO: AAAAAAAAAA testing performance without the cache..		
			//		if (null!=DocumentTokenMap.cacheBody 
			//				&& DocumentTokenMap.cacheBody.equals(documentBody) 
			//				&& (colorMatches = (DocumentTokenMap.cachedResults.textColor() == textColors))
			//				&& ((null==activeFileServce) || (moduleIsUnchanged = (activeFileServce.lastCallUpdate()==DocumentTokenMap.cacheModuleLastUpdate)))
			//				) {
			//			return DocumentTokenMap.cachedResults;
			//		}
					
					adjustBounds.set((!colorMatches) || (!moduleIsUnchanged));		
					
					byte[] data = doc.get().getBytes();	
					final int dataSize = data.length;

					
					StructureDataModule structureDataModule = null;
					String fileStart = null;
					
					if (null != activeFileServce) {
						structureDataModule = activeFileServce.module;
						fileStart = activeFileServce.rawAbsoluteFile.toString();
					}
										
					boolean isDisabled = false;
					
					ProviderConstraintImpl providerConstraintImpl = new ProviderConstraintImpl(
							TerraArchActivator.getDefault().storageCache()
					);
														
					TokenCollector results = parse(textColors, data,
							                       structureDataModule, fileStart, isDisabled,
							                       providerConstraintImpl);
									

					
					IEclipsePreferences node = InstanceScope.INSTANCE.getNode(TerraArchActivator.PLUGIN_ID);			
					//get key but if its not there then use the demo key.

					
					
					//does not process if one is already in progress
					if (null!=results) {
						
						
						//due to module or other global changes we should ignore range and paint it all
						//this is uncommon but important when background services have made changes
						if (adjustBounds.get()) {
							start = 0;
							length = dataSize;
						}
						
						off = start;
						len = 0;
						end = length>0 ? start+length : 0;	//protect against odd errors upon renderng							
						size = dataSize;
						
						//System.out.println("repaint "+off+"->"+end+" size: "+size);
						
						int i = 0;
						while (i<results.tokenBlocks().size() && (results.tokenBlocks().get(i)).stop<start) {
							i++;			
						}
						
						pos = i;
						tokenList = results.tokenBlocks();					
						
					}
				
				}catch (Throwable t) {
					t.printStackTrace();
					Reconciler.logger.error("unknown failure ",t);
				
				}
			}
		}

		private TokenCollector parse(final ThemeColors textColors, byte[] data, StructureDataModule structureDataModule,
				String fileStart, boolean isDisabled, ProviderConstraintImpl providerConstraintImpl) {
			
			DocumentTokenMap dtm = new DocumentTokenMap(structureDataModule, isDisabled, providerConstraintImpl);
			dtm.activeTextColor(textColors);
							
			//NOTE: no need to call file start because this is only used once and created new.
			if (null != fileStart) {
				dtm.fileStart(null, fileStart);
			}						
			
			final boolean isClean = buffer.tokenizeDocument(data, dtm);
			//System.out.println("start paint "+activeFileServce.file+" module "+activeFileServce.module.toString());
			TokenCollector results = null!=dtm ? dtm.tc : null;
			return results;
		}
	}