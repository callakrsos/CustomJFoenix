/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.jfoenix.skins;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.chrono.Chronology;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

import com.jfoenix.controls.JFXDatePicker;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import javafx.util.converter.NumberStringConverter;

/**
 * @author Shadi Shaheen
 *
 *
 *  TimePicker Bug fix. by kyj. 2016-11-24
 *
 */

public class JFXTimePickerContent extends VBox {

	private static enum TimeUnit{HOURS, MINUTES};

	protected JFXDatePicker datePicker;

	private Color fadedColor = Color.rgb(255, 255, 255, 0.67);
	private double contentCircleRadius = 100;


	private Label selectedHourLabel = new Label();
	private Label selectedMinLabel = new Label();
	private Label periodPMLabel, periodAMLabel;
	private StackPane calendarPlaceHolder = new StackPane();
	private StackPane hoursContent;
	private StackPane minutesContent;

	private Rotate hoursPointerRotate;
	private Rotate minsPointerRotate;

	private ObjectProperty<TimeUnit> unit = new SimpleObjectProperty<>(TimeUnit.HOURS);
	private DoubleProperty angle = new SimpleDoubleProperty(Math.toDegrees(2 * Math.PI/12));
	private StringProperty period = new SimpleStringProperty("AM");
	private ObjectProperty<Rotate> pointerRotate = new SimpleObjectProperty<>();
	private ObjectProperty<Label> timeLabel = new SimpleObjectProperty<>();

	private NumberStringConverter unitConverter = new NumberStringConverter("#00");


	private ObjectProperty<LocalTime> selectedTime = new SimpleObjectProperty<LocalTime>(this, "selectedTime");

	ObjectProperty<LocalTime> displayedTimeProperty() {
		return selectedTime;
	}

	JFXTimePickerContent(final DatePicker datePicker) {
		this.datePicker = (JFXDatePicker) datePicker;
		LocalTime time = this.datePicker.getTime() == null? LocalTime.now() : this.datePicker.getTime();

		getStyleClass().add("date-picker-popup");

		// create the header pane
		getChildren().add(createHeaderPane(time));

		VBox contentHolder = new VBox();
		// create content pane
		contentHolder.getChildren().add(createContentPane(time));
		calendarPlaceHolder.getChildren().add(contentHolder);

		Rectangle clip = new Rectangle();
		clip.widthProperty().bind(calendarPlaceHolder.widthProperty());
		clip.heightProperty().bind(calendarPlaceHolder.heightProperty());
		calendarPlaceHolder.setClip(clip);

		StackPane contentPlaceHolder = new StackPane(calendarPlaceHolder);
		getChildren().add(contentPlaceHolder);

		// add listeners
		unit.addListener((o,oldVal,newVal)->{
			if(newVal == TimeUnit.HOURS){
				angle.set(Math.toDegrees(2 * Math.PI/12));
				pointerRotate.set(hoursPointerRotate);
				timeLabel.set(selectedHourLabel);
			}else if(newVal == TimeUnit.MINUTES){
				angle.set(Math.toDegrees(2 * Math.PI/60));
				pointerRotate.set(minsPointerRotate);
				timeLabel.set(selectedMinLabel);
			}
			swapLabelsColor(selectedHourLabel, selectedMinLabel);
			switchTimeUnit(newVal);
		});

		period.addListener((o,oldVal,newVal)->{
			swapLabelsColor(periodPMLabel, periodAMLabel);
			updateValue();
		});
	}


	/*
	 * header panel represents the selected Date
	 * we keep javaFX original style classes
	 */
	protected StackPane createHeaderPane(LocalTime time) {
		int hour = time.getHour();

		selectedHourLabel.setText((hour%12==0?12:hour%12) + "");
		selectedHourLabel.getStyleClass().add("spinner-label");
		selectedHourLabel.setTextFill(Color.WHITE);
		selectedHourLabel.setFont(Font.font("Roboto",FontWeight.BOLD,42));
		selectedHourLabel.setOnMouseClicked((click)-> unit.set(TimeUnit.HOURS));
		selectedHourLabel.setMinWidth(49);
		selectedHourLabel.setAlignment(Pos.CENTER_RIGHT);
		timeLabel.set(selectedHourLabel);

		selectedMinLabel.setText(unitConverter.toString(time.getMinute()) + "");
		selectedMinLabel.getStyleClass().add("spinner-label");
		selectedMinLabel.setTextFill(fadedColor);
		selectedMinLabel.setFont(Font.font("Roboto",FontWeight.BOLD,42));
		selectedMinLabel.setOnMouseClicked((click)-> unit.set(TimeUnit.MINUTES));

		Label separatorLabel = new Label(":");
		separatorLabel.setPadding(new Insets(0, 0, 4, 0));
		separatorLabel.setTextFill(fadedColor);
		separatorLabel.setFont(Font.font("Roboto",FontWeight.BOLD,42));

		periodPMLabel = new Label("PM");
		periodPMLabel.getStyleClass().add("spinner-label");
		periodPMLabel.setTextFill(fadedColor);
		periodPMLabel.setFont(Font.font("Roboto",FontWeight.BOLD,14));
		periodPMLabel.setOnMouseClicked((click)-> period.set("PM"));

		periodAMLabel = new Label("AM");
		periodAMLabel.getStyleClass().add("spinner-label");
		periodAMLabel.setTextFill(fadedColor);
		periodAMLabel.setFont(Font.font("Roboto",FontWeight.BOLD,14));
		periodAMLabel.setOnMouseClicked((click)-> period.set("AM"));

		// init period value
		if(hour < 12) periodAMLabel.setTextFill(Color.WHITE);
		else periodPMLabel.setTextFill(Color.WHITE);
		period.set(hour < 12? "AM" : "PM");


		VBox periodContainer = new VBox();
		periodContainer.setPadding(new Insets(0,0,0,4));
		periodContainer.getChildren().addAll(periodAMLabel, periodPMLabel);

		// Year label container
		HBox selectedTimeContainer = new HBox();
		selectedTimeContainer.getStyleClass().add("spinner");
		selectedTimeContainer.getChildren().addAll(selectedHourLabel,separatorLabel , selectedMinLabel, periodContainer);
		selectedTimeContainer.setAlignment(Pos.CENTER);
		selectedTimeContainer.setFillHeight(false);

		StackPane headerPanel = new StackPane();
		headerPanel.getStyleClass().add("time-pane");
		headerPanel.setBackground(new Background(new BackgroundFill(this.datePicker.getDefaultColor(), CornerRadii.EMPTY, Insets.EMPTY)));
		headerPanel.setPadding(new Insets(8,24,8,24));
		headerPanel.getChildren().add(selectedTimeContainer);
		return headerPanel;
	}

	protected BorderPane createContentPane(LocalTime time){
		Circle circle = new Circle(contentCircleRadius);
		circle.setFill(Color.rgb(224, 224, 224, 0.67));

		EventHandler<? super MouseEvent> mouseActionHandler = (event)->{
			double dx = event.getX();
			double dy = event.getY();
			double theta = Math.atan2(dy, dx);
			int index = (int) Math.round((180+Math.toDegrees(theta))/angle.get());
			pointerRotate.get().setAngle(index*angle.get());
			int timeValue = (index+9)%12 == 0 ? 12 : (index+9)%12;
			if(unit.get() == TimeUnit.MINUTES) timeValue = (index+45)%60 == 0 ? 0 : (index+45)%60;
			timeLabel.get().setText(unit.get() == TimeUnit.MINUTES? unitConverter.toString(timeValue) : timeValue+"");
			updateValue();
		};

		/*
		 * 시각을 더블클릭하면 닫히는 이벤트를 추가한다.
		 * 
		 * */
		EventHandler<? super MouseEvent> mouseClickHandler = ev -> {
            
			if (MouseButton.PRIMARY == ev.getButton() && ev.getClickCount() == 2) {
				
				JFXDatePickerSkin jfxDatePickerSkin = (JFXDatePickerSkin) datePicker.getSkin();
				jfxDatePickerSkin.getSkinnable().hide();
			}

		};
		
		circle.setOnMousePressed(mouseActionHandler);
		circle.setOnMouseDragged(mouseActionHandler);
		
		circle.setOnMouseClicked(mouseClickHandler);
		hoursContent = createHoursContent(time);
		hoursContent.setMouseTransparent(true);
		minutesContent = createMinutesContent(time);
		minutesContent.setOpacity(0);
		minutesContent.setMouseTransparent(true);

		StackPane contentPane = new StackPane();
		contentPane.getChildren().addAll(circle,hoursContent, minutesContent);
		contentPane.setPadding(new Insets(12));

		BorderPane contentContainer = new BorderPane();
		contentContainer.setCenter(contentPane);
		contentContainer.setMinHeight(50);
		contentContainer.setPadding(new Insets(2,12,2,12));
		return contentContainer;
	}


	private StackPane createMinutesContent(LocalTime time){
		// create minutes content
		StackPane minsPointer = new StackPane();
		Circle selectionCircle = new Circle(contentCircleRadius/6);
		selectionCircle.fillProperty().bind(datePicker.defaultColorProperty());

		Circle minCircle = new Circle(selectionCircle.getRadius()/8);
		minCircle.setFill(Color.rgb(255, 255, 255, 0.87));
		minCircle.setTranslateX(selectionCircle.getRadius() - minCircle.getRadius());
		minCircle.setVisible(time.getMinute()%5!=0);
		selectedMinLabel.textProperty().addListener((o,oldVal,newVal)->{
			if(Integer.parseInt(newVal)%5==0) minCircle.setVisible(false);
			else minCircle.setVisible(true);
		});


		double shift = 9;
		Line line = new Line(shift,0,contentCircleRadius,0);
		line.fillProperty().bind(datePicker.defaultColorProperty());
		line.strokeProperty().bind(line.fillProperty());
		line.setStrokeWidth(1.5);
		minsPointer.getChildren().addAll(line, selectionCircle, minCircle);
		StackPane.setAlignment(selectionCircle, Pos.CENTER_LEFT);
		StackPane.setAlignment(minCircle, Pos.CENTER_LEFT);


		Group pointerGroup = new Group();
		pointerGroup.getChildren().add(minsPointer);
		pointerGroup.setTranslateX((-contentCircleRadius+shift)/2);
		minsPointerRotate = new Rotate(0, contentCircleRadius-shift, selectionCircle.getRadius());
		pointerGroup.getTransforms().add(minsPointerRotate);

		Pane clockLabelsContainer = new Pane();
		// inner circle radius
		double radius = contentCircleRadius-shift-selectionCircle.getRadius();
		for (int i = 0 ; i < 12; i++) {
			StackPane labelContainer = new StackPane();
			int val = ((i+3)*5)%60 == 0 ? 0 : ((i+3)*5)%60;
			Label label = new Label(unitConverter.toString(val)+"");
			label.setFont(Font.font("Roboto",FontWeight.BOLD,12));
			// init label color
			if(val == time.getMinute()) label.setTextFill(Color.rgb(255, 255, 255, 0.87));
			else label.setTextFill(Color.rgb(0, 0, 0, 0.87));
			selectedMinLabel.textProperty().addListener((o,oldVal,newVal)->{
				if(Integer.parseInt(newVal) == Integer.parseInt(label.getText())){
					label.setTextFill(Color.rgb(255, 255, 255, 0.87));
				}else{
					label.setTextFill(Color.rgb(0, 0, 0, 0.87));
				}
			});

			labelContainer.getChildren().add(label);
			double labelSize = (selectionCircle.getRadius()/Math.sqrt(2))*2;
			labelContainer.setMinSize(labelSize,labelSize);

			double angle = 2 * i * Math.PI / 12 ;
			double xOffset = radius * Math.cos(angle);
			double yOffset = radius * Math.sin(angle);
			final double startx = contentCircleRadius + xOffset;
			final double starty = contentCircleRadius + yOffset;
			labelContainer.setLayoutX(startx - labelContainer.getMinWidth()/2);
			labelContainer.setLayoutY(starty - labelContainer.getMinHeight()/2);

			// add label to the parent node
			clockLabelsContainer.getChildren().add(labelContainer);
		}

		minsPointerRotate.setAngle(180 + (time.getMinute()+45)%60 * Math.toDegrees(2 * Math.PI/60));

		return new StackPane(pointerGroup, clockLabelsContainer);
	}

	private StackPane createHoursContent(LocalTime time){
		// create hours content
		StackPane hoursPointer = new StackPane();
		Circle selectionCircle = new Circle(contentCircleRadius/6);
		selectionCircle.fillProperty().bind(datePicker.defaultColorProperty());

		double shift = 9;
		Line line = new Line(shift,0,contentCircleRadius,0);
		line.fillProperty().bind(datePicker.defaultColorProperty());
		line.strokeProperty().bind(line.fillProperty());
		line.setStrokeWidth(1.5);
		hoursPointer.getChildren().addAll(line, selectionCircle);
		StackPane.setAlignment(selectionCircle, Pos.CENTER_LEFT);

		Group pointerGroup = new Group();
		pointerGroup.getChildren().add(hoursPointer);
		pointerGroup.setTranslateX((-contentCircleRadius+shift)/2);
		hoursPointerRotate = new Rotate(0, contentCircleRadius-shift, selectionCircle.getRadius());
		pointerRotate.set(hoursPointerRotate);
		pointerGroup.getTransforms().add(hoursPointerRotate);


		Pane clockLabelsContainer = new Pane();
		// inner circle radius
		double radius = contentCircleRadius-shift-selectionCircle.getRadius();
		for (int i = 0 ; i < 12; i++) {
			// create the label and its container
			int val = (i+3)%12 == 0 ? 12 : (i+3)%12;
			Label label = new Label(val+"");
			label.setFont(Font.font("Roboto",FontWeight.BOLD,12));
			// init color
			if(val == time.getHour()%12 ||(val == 12 && time.getHour()%12 == 0)) label.setTextFill(Color.rgb(255, 255, 255, 0.87));
			else label.setTextFill(Color.rgb(0, 0, 0, 0.87));
			selectedHourLabel.textProperty().addListener((o,oldVal,newVal)->{
				if(Integer.parseInt(newVal) == Integer.parseInt(label.getText())){
					label.setTextFill(Color.rgb(255, 255, 255, 0.87));
				}else{
					label.setTextFill(Color.rgb(0, 0, 0, 0.87));
				}
			});
			// create label container
			StackPane labelContainer = new StackPane();
			labelContainer.getChildren().add(label);
			double labelSize = (selectionCircle.getRadius()/Math.sqrt(2))*2;
			labelContainer.setMinSize(labelSize,labelSize);

			// position the label on the circle
			double angle = 2 * i * Math.PI / 12 ;
			double xOffset = radius * Math.cos(angle);
			double yOffset = radius * Math.sin(angle);
			final double startx = contentCircleRadius + xOffset;
			final double starty = contentCircleRadius + yOffset;
			labelContainer.setLayoutX(startx - labelContainer.getMinWidth()/2);
			labelContainer.setLayoutY(starty - labelContainer.getMinHeight()/2);

			// add label to the parent node
			clockLabelsContainer.getChildren().add(labelContainer);

			// init pointer angle
			if(val == time.getHour()%12 ||(val == 12 && time.getHour()%12 == 0)) hoursPointerRotate.setAngle(180+ Math.toDegrees(angle));
		}
		return new StackPane(pointerGroup, clockLabelsContainer);
	}


	private void swapLabelsColor(Label lbl1 , Label lbl2){
		Paint color = lbl1.getTextFill();
		lbl1.setTextFill(lbl2.getTextFill());
		lbl2.setTextFill(color);
	}


	private void switchTimeUnit(TimeUnit newVal){
		if(newVal == TimeUnit.HOURS){
			Timeline fadeout = new Timeline(new KeyFrame(Duration.millis(320), new KeyValue(minutesContent.opacityProperty(), 0, Interpolator.EASE_BOTH)));
			Timeline fadein = new Timeline(new KeyFrame(Duration.millis(320), new KeyValue(hoursContent.opacityProperty(), 1, Interpolator.EASE_BOTH)));
			new ParallelTransition(fadeout,fadein).play();
		}else{
			Timeline fadeout = new Timeline(new KeyFrame(Duration.millis(320), new KeyValue(hoursContent.opacityProperty(), 0, Interpolator.EASE_BOTH)));
			Timeline fadein = new Timeline(new KeyFrame(Duration.millis(320), new KeyValue(minutesContent.opacityProperty(), 1, Interpolator.EASE_BOTH)));
			new ParallelTransition(fadeout,fadein).play();
		}
	}


	void updateValue(){

		//  TimePicker Bug fix. by kyj.
//		LocalTimeStringConverter converter = new LocalTimeStringConverter(FormatStyle.SHORT);
//		String format = String.format("%02d", 3);
//		String hour = String.format("%02d", Integer.parseInt(selectedHourLabel.getText()));
//		String min = String.format("%02d", Integer.parseInt(selectedMinLabel.getText()));
		String dateString = selectedHourLabel.getText()+":"+selectedMinLabel.getText() +" " +period.get();
//		System.out.println(dateString);


		DateTimeFormatter parseFormat = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(Locale.ENGLISH);
//		DateTimeFormatter ofPattern = DateTimeFormatter.ofPattern();
		LocalTime localTime = LocalTime.parse(dateString, parseFormat);

//		TemporalAccessor parse = ofPattern.parse(dateString);
//		LocalTime localTime = LocalTime.from(parse);//converter.fromString(dateString);
		datePicker.setTime(localTime);
	}

	void init(){
		calendarPlaceHolder.setOpacity(1);
		selectedHourLabel.setTextFill(Color.rgb(255, 255, 255, 0.87));
	}

	void clearFocus() {
		LocalDate focusDate = datePicker.getValue();
		if (focusDate == null) focusDate = LocalDate.now();
		// if (YearMonth.from(focusDate).equals(selectedYearMonth.get())) goToDate(focusDate, true);
	}

	protected boolean isValidDate(Chronology chrono, LocalDate date, int offset, ChronoUnit unit) {
		if (date != null) return isValidDate(chrono, date.plus(offset, unit));
		return false;
	}

	protected boolean isValidDate(Chronology chrono, LocalDate date) {
		try {
			if (date != null) chrono.date(date);
			return true;
		} catch (DateTimeException ex) {
			return false;
		}
	}
}
