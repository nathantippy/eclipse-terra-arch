package net.terraarch.presentation;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import net.terraarch.terraform.parse.ParseState;
import net.terraarch.terraform.parse.doc.TokenCollector;
import net.terraarch.terraform.parse.doc.TypeColors;

public class TokenProvider {

	public static int TEXT_VIVID = 0;
    public static int TEXT_MUTED = 1;
	private static Token[][] tokens       = new Token[2][TypeColors.values().length];
	private static Token[][] rainbowCache = new Token[2][TypeColors.RAINBOW_COLOR_RANGE+1];
	
		static {
			ParseState.reportParseErrors = false; 
			
			for(net.terraarch.terraform.parse.doc.TypeColors t : net.terraarch.terraform.parse.doc.TypeColors.values()) {
				net.terraarch.presentation.TokenProvider.tokens[TEXT_VIVID][t.ordinal()] = new Token(new TextAttribute(new Color(Display.getCurrent(), LocalRGB.get(t)), null, t.style));
				
				//System.out.println("Vivid "+t+"  ["+t.color.red+",  "+t.color.green+" , "+t.color.blue+"]");
				
			}

			for(net.terraarch.terraform.parse.doc.TypeColors t : net.terraarch.terraform.parse.doc.TypeColors.values()) {
				int shift = 1;
				
				if (t == net.terraarch.terraform.parse.doc.TypeColors.UNDEFINED) {
					net.terraarch.presentation.TokenProvider.tokens[TEXT_MUTED][t.ordinal()] = new Token(new TextAttribute(new Color(Display.getCurrent(), LocalRGB.get(t)), null, t.style));
			
				} else {
				
					net.terraarch.presentation.TokenProvider.tokens[TEXT_MUTED][t.ordinal()] = new Token(new TextAttribute(new Color(Display.getCurrent(), 
														LocalRGB.muteColor(shift, LocalRGB.get(t))		), null, t.style));
					
				}
			}
			
			/////////////////// 
			//build rainbow cache
			
			int i = net.terraarch.presentation.TokenProvider.rainbowCache[0].length;
			while (--i>=0) {
				RGB rgb = computeRainbowColor(i, LocalRGB.get(net.terraarch.terraform.parse.doc.TypeColors.values()[net.terraarch.terraform.parse.doc.TypeColors.RAINBOW_BASE.ordinal()]));
				
				net.terraarch.presentation.TokenProvider.rainbowCache[TEXT_VIVID][i] = new Token(new TextAttribute(new Color(Display.getCurrent(),rgb)) );
				net.terraarch.presentation.TokenProvider.rainbowCache[TEXT_MUTED][i] = new Token(new TextAttribute(new Color(Display.getCurrent(),LocalRGB.muteColor(1,rgb))) );
							
			}
		}

		private static RGB computeRainbowColor(int colorShift, RGB color) {
			int actualShift = (colorShift*TypeColors.RAINBOW_COLOR_STEP) % (TypeColors.RAINBOW_COLOR_RANGE+1); //0 to range is ok
			//bump up the red as we cross the middle
			int redAdj =  2*Math.max(0, (TypeColors.RAINBOW_COLOR_RANGE>>1)-Math.abs((TypeColors.RAINBOW_COLOR_RANGE>>1)-actualShift));
			int red   = Math.min(255, color.red+redAdj);
			int green = Math.max(0, color.green-(actualShift*4));//green motion must be larger than blue
			int blue  = Math.min(255, color.blue+actualShift);
			
			return new RGB(red ,green,blue);
			
		}
		

		// id map:     <8bits kind><8bits vivid><16bits colorIdx>
		 
		public static Token get(int id) {
			int kind  = 0xFF&(id >>> 24);
			int vivid = 0xFF&(id >>> 16);
			int index = 0xFFFF&id; 
			
			if (0 == kind) {
				return tokens[vivid][index];
			} else {
				return rainbowCache[vivid][index];
			}
		}

		private static int token(int colorOrdinal, int tokenOrdinal) {
			//return tokens[colorOrdinal][tokenOrdinal];
			return TokenCollector.token(colorOrdinal, tokenOrdinal);
		}
		
		private static int rainbowCache(int colorOrdinal, int tokenOrdinal) {
			//return rainbowCache[colorOrdinal][tokenOrdinal];
			return TokenCollector.rainbowCache(colorOrdinal, tokenOrdinal);
		}

}
