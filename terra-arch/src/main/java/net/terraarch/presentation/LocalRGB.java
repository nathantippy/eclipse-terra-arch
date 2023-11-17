package net.terraarch.presentation;

import java.util.EnumMap;

import org.eclipse.swt.graphics.RGB;

import net.terraarch.tf.parse.doc.TypeColors;

public class LocalRGB {

	private static EnumMap<TypeColors, RGB> cache = new EnumMap<TypeColors, RGB>(TypeColors.class);
	
	public static RGB get(TypeColors color) {
		RGB result = cache.get(color);
		if (result!=null) {
			return result;
		} else {
			synchronized(cache) {
				result = cache.get(color);
				if (result!=null) {
					return result;
				}		
				result = new RGB(color.red,color.green,color.blue);
				cache.put(color, result);
				return result;		
			}
		}
	}

	public static RGB muteColor(int shift, RGB color) {
		return new RGB((color.red>>shift) + (color.red>>(shift*2)),
				(color.green>>shift) + (color.green>>(shift*2)),
				(color.blue>>shift) + (color.blue>>(shift*2)));
	}
	
}
