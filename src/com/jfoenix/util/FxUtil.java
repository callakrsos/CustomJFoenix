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
	 * RGB 색상 리턴.
	 *
	 * @작성자 : KYJ
	 * @작성일 : 2016. 7. 19.
	 * @param color
	 * @return
	 */
	public static String toRGBCode(Color color) {
		if (color == null)
			return "BLACK";
		return String.format("#%02X%02X%02X", (int) (color.getRed() * 255), (int) (color.getGreen() * 255), (int) (color.getBlue() * 255));
	}

}
