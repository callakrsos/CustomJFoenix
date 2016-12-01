/********************************
 *	프로젝트 : JFoenix
 *	패키지   : com.jfoenix.util
 *	작성일   : 2016. 11. 25.
 *	작성자   : KYJ
 *******************************/
package com.jfoenix.util;

import javafx.scene.paint.Color;

/**
 * @author KYJ
 *
 */
public final class FxUtil {

	private FxUtil() {
	}

	/**
	 * WEB 색상 리턴.
	 *
	 * @작성자 : KYJ
	 * @작성일 : 2016. 7. 19.
	 * @param color
	 * @return
	 */
	public static String toWeb(Color color) {
		if (color == null)
			return "BLACK";
		return String.format("#%02X%02X%02X", (int) (color.getRed() * 255), (int) (color.getGreen() * 255), (int) (color.getBlue() * 255));
	}

	/**
	 * RGB Color String
	 * 
	 * @작성자 : KYJ
	 * @작성일 : 2016. 12. 1.
	 * @param color
	 * @return
	 */
	public static String toRgb(Color color) {
		if (color == null)
			return "BLACK";
		return String.format("rgba(%d, %d, %d, 1)", (int) (color.getRed() * 255), (int) (color.getGreen() * 255),
				(int) (color.getBlue() * 255));
	}

	/**
	 * HSB Color String
	 * 
	 * @작성자 : KYJ
	 * @작성일 : 2016. 12. 1.
	 * @param color
	 * @return
	 */
	public static String toHsb(Color color) {
		if (color == null)
			return "BLACK";
		return String.format("hsl(%d, %d%%, %d%%)", (int) (color.getHue()), (int) (color.getSaturation() * 100),
				(int) (color.getBrightness() * 100));
	}
}
