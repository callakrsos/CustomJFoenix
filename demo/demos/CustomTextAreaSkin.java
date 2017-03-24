/********************************
 *	프로젝트 : JFoenix
 *	패키지   : demos
 *	작성일   : 2016. 12. 9.
 *	프로젝트 : OPERA 
 *	작성자   : KYJ
 *******************************/
package demos;

import com.sun.javafx.scene.control.skin.TextAreaSkin;

import javafx.geometry.Bounds;
import javafx.scene.control.TextArea;

/**
 * @author KYJ
 *
 */
public class CustomTextAreaSkin extends TextAreaSkin {

	public CustomTextAreaSkin(TextArea textArea) {
		super(textArea);
	}

	public Bounds getBlankBounds() {
		return getCaretBounds();

	}
}
