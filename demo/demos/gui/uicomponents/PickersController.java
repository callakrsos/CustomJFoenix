package demos.gui.uicomponents;

import javax.annotation.PostConstruct;

import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXDateTimePicker;

import io.datafx.controller.FXMLController;
import io.datafx.controller.flow.FlowException;
import io.datafx.controller.util.VetoException;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

@FXMLController(value = "/resources/fxml/ui/Pickers.fxml" , title = "Material Design Example")
public class PickersController {

	@FXML private StackPane root;
	@FXML private JFXDatePicker dateOverlay;
	@FXML private JFXDatePicker timeOverlay;
	@FXML private HBox hboxRuntime;
	
	@PostConstruct
	public void init() throws FlowException, VetoException {
		dateOverlay.setDialogParent(root);
		timeOverlay.setDialogParent(root);
		
		
		//2016-11-24 Add by kyj. 
		JFXDateTimePicker e = new JFXDateTimePicker();
		e.setTextAlignment(Pos.CENTER);
		hboxRuntime.getChildren().add(e);
	}
}
