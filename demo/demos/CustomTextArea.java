/********************************
 *	프로젝트 : JFoenix
 *	패키지   : demos
 *	작성일   : 2016. 12. 9.
 *	프로젝트 : OPERA 
 *	작성자   : KYJ
 *******************************/
package demos;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.control.Skin;
import javafx.scene.control.TextArea;

/**
 * @author KYJ
 *
 */
public class CustomTextArea extends TextArea {

	public CustomTextArea() {
		super();
	}

	public CustomTextArea(String text) {
		super(text);
	}

	@Override
	protected Skin<?> createDefaultSkin() {
		CustomTextAreaSkin customTextAreaSkin = new CustomTextAreaSkin(this);
		return customTextAreaSkin;
	}

	public Bounds getBlankBounds() {
		CustomTextAreaSkin skin = (CustomTextAreaSkin) getSkin();
		return skin.getBlankBounds();
	}

}
