package demos;

import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.input.InputMethodRequests;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class TextPopupExam extends Application {

	/* (non-Javadoc)
	 * @see javafx.application.Application#start(javafx.stage.Stage)
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
		CustomTextArea textArea = new CustomTextArea("Hello world.,!");

		textArea.addEventHandler(KeyEvent.KEY_PRESSED, ev -> {
			if (ev.isControlDown() && ev.getCode() == KeyCode.SPACE) {
				System.out.println(textArea.getBlankBounds());
			}
		});
		primaryStage.setScene(new Scene(textArea));
		primaryStage.show();
	}

	/**
	 * @작성자 : KYJ
	 * @작성일 : 2016. 12. 9.
	 * @param args
	 */
	public static void main(String[] args) {
		launch(args);

	}

}
