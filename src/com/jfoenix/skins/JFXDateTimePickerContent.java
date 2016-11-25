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

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MONTHS;
import static java.time.temporal.ChronoUnit.WEEKS;
import static java.time.temporal.ChronoUnit.YEARS;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.Chronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DecimalStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDateTimeCell;
import com.jfoenix.controls.JFXDateTimePicker;
import com.jfoenix.controls.JFXListCell;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXSlider;
import com.jfoenix.svg.SVGGlyph;
import com.jfoenix.transitions.CachedTransition;
import com.jfoenix.util.FxUtil;

import javafx.animation.Animation.Status;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.util.Callback;
import javafx.util.Duration;

/**
 * @author Shadi Shaheen
 *
 */
public class JFXDateTimePickerContent extends VBox {

	protected JFXDateTimePicker datePicker;
	private JFXButton backMonthButton;
	private JFXButton forwardMonthButton;
	private ObjectProperty<Label> selectedYearCell = new SimpleObjectProperty<>(null);
	private Label selectedDateLabel;
	private Label selectedTimeLabel;
	private Label selectedYearLabel;

	private Label monthYearLabel;
	protected GridPane contentGrid;
	private StackPane calendarPlaceHolder = new StackPane();

	// animation
	private CachedTransition showTransition;
	private CachedTransition hideTransition;
	private ParallelTransition tempImageTransition;

	private int daysPerWeek = 7;
	private List<JFXDateTimeCell> weekDaysCells = new ArrayList<>();
	private List<JFXDateTimeCell> weekNumberCells = new ArrayList<>();
	protected List<JFXDateTimeCell> dayCells = new ArrayList<>();
	private LocalDateTime[] dayCellDates;
	private JFXDateTimeCell currentFocusedDayCell = null;

	private ListView<String> yearsListView = new JFXListView<String>() {
		{
			this.getStyleClass().setAll("date-picker-list-view");
			this.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
				@Override
				public ListCell<String> call(ListView<String> listView) {
					return new JFXListCell<String>() {
						boolean mousePressed = false;

						{
							this.getStyleClass().setAll("data-picker-list-cell");
							setOnMousePressed((click) -> {
								mousePressed = true;
							});
							setOnMouseEntered((enter) -> {
								if (!mousePressed)
									setBackground(
											new Background(new BackgroundFill(Color.valueOf("#EDEDED"), CornerRadii.EMPTY, Insets.EMPTY)));
							});
							setOnMouseExited((enter) -> {
								if (!mousePressed)
									setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
							});
							setOnMouseReleased((release) -> {
								if (mousePressed)
									setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
								mousePressed = false;
							});
							setOnMouseClicked((click) -> {
								String selectedItem = yearsListView.getSelectionModel().getSelectedItem();
								if (selectedItem != null && selectedItem.equals(((Label) cellContent).getText())) {
									int offset = Integer.parseInt(((Label) cellContent).getText())
											- Integer.parseInt(selectedYearLabel.getText());
									forward(offset, YEARS, false, false);
									hideTransition.setOnFinished((finish) -> {
										selectedYearCell.set((Label) cellContent);
										yearsListView.scrollTo(this.getIndex() - 2 >= 0 ? this.getIndex() - 2 : this.getIndex());
										hideTransition.setOnFinished(null);
									});
									hideTransition.play();
								}
							});
							selectedYearLabel.textProperty().addListener((o, oldVal, newVal) -> {
								if (!yearsListView.isVisible())
									if (((Label) cellContent).getText().equals(newVal))
										selectedYearCell.set((Label) cellContent);
							});
						}

						public void updateItem(String item, boolean empty) {
							super.updateItem(item, empty);
							if (!empty) {
								cellRippler.setRipplerFill(Color.GREY);
								Label lbl = (Label) cellContent;
								lbl.setAlignment(Pos.CENTER);
								lbl.setTextAlignment(TextAlignment.CENTER);
								lbl.setMaxWidth(Double.MAX_VALUE);
								if (!item.equals(selectedYearLabel.getText())) {
									// default style for each cell
									lbl.setStyle("-fx-font-size: 16; -fx-font-weight: NORMAL;");
									lbl.setTextFill(Color.valueOf("#313131"));
								} else {
									selectedYearCell.set((Label) cellContent);
								}
								setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
							}
						};
					};
				}
			});
		}
	};

	// Date formatters
	final DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM");
	final DateTimeFormatter yearFormatter = DateTimeFormatter.ofPattern("y");
	final DateTimeFormatter weekNumberFormatter = DateTimeFormatter.ofPattern("w");
	final DateTimeFormatter weekDayNameFormatter = DateTimeFormatter.ofPattern("ccc");
	final DateTimeFormatter dayCellFormatter = DateTimeFormatter.ofPattern("d");

	//	final DateTimeFormatter hourCellFormatter = DateTimeFormatter.ofPattern("HH");
	//	final DateTimeFormatter minCellFormatter = DateTimeFormatter.ofPattern("mm");
	//	final DateTimeFormatter secCellFormatter = DateTimeFormatter.ofPattern("ss");

	private ObjectProperty<YearMonth> selectedYearMonth = new SimpleObjectProperty<YearMonth>(this, "selectedYearMonth");

	ObjectProperty<YearMonth> displayedYearMonthProperty() {
		return selectedYearMonth;
	}

	private JFXSlider jfHour;
	private JFXSlider jfMin;
	private JFXSlider jfSec;

	JFXDateTimePickerContent(final JFXDateTimePicker datePicker) {
		this.datePicker = (JFXDateTimePicker) datePicker;
		getStyleClass().add("date-picker-popup");

		LocalDateTime currentDateTime = LocalDateTime.now();
		LocalDateTime date = this.datePicker.getValue();

		selectedYearMonth.set((date != null) ? YearMonth.from(date) : YearMonth.now());
		selectedYearMonth.addListener((observable, oldValue, newValue) -> {
			updateValues();
		});

		// currentDateTime Set.
		if (date != null && date.isSupported(ChronoField.HOUR_OF_DAY)) {
			int defHour = date.get(ChronoField.HOUR_OF_DAY);
			jfHour = createJFXSliderForHour(defHour);
		} else {
			jfHour = createJFXSliderForHour(currentDateTime.get(ChronoField.HOUR_OF_DAY));
		}
		if (date != null && date.isSupported(ChronoField.MINUTE_OF_HOUR)) {
			int defMin = date.get(ChronoField.MINUTE_OF_HOUR);
			jfMin = createNumberSpinnerForMinute(defMin);
		} else {
			int defMin = currentDateTime.get(ChronoField.MINUTE_OF_HOUR);
			jfMin = createNumberSpinnerForMinute(defMin);
		}
		if (date != null && date.isSupported(ChronoField.SECOND_OF_MINUTE)) {
			int defSec = date.get(ChronoField.SECOND_OF_MINUTE);
			jfSec = createNumberSpinnerForSecond(defSec);
		} else {
			int defSec = currentDateTime.get(ChronoField.SECOND_OF_MINUTE);
			jfSec = createNumberSpinnerForSecond(defSec);
		}

		jfHour.valueProperty().addListener((ob, o, n) -> updateHour(n.intValue()));
		jfMin.valueProperty().addListener((ob, o, n) -> updateMinute(n.intValue()));
		jfSec.valueProperty().addListener((ob, o, n) -> updateSecond(n.intValue()));

		// add change listener to change the color of the selected year cell
		selectedYearCell.addListener((o, oldVal, newVal) -> {
			if (oldVal != null) {
				oldVal.setStyle("-fx-font-size: 16; -fx-font-weight: NORMAL;");
				//				oldVal.setFont(Font.font("Roboto", FontWeight.NORMAL, 16));
				oldVal.setTextFill(Color.valueOf("#313131"));
			}
			if (newVal != null) {
				newVal.setStyle("-fx-font-size: 24; -fx-font-weight: BOLD;");
				//				newVal.setFont(Font.font("Roboto", FontWeight.BOLD, 24));
				newVal.setTextFill(this.datePicker.getDefaultColor());
			}
		});

		// create the header pane
		getChildren().add(createHeaderPane());

		contentGrid = new GridPane() {
			@Override
			protected double computePrefWidth(double height) {
				final int nCols = daysPerWeek + (datePicker.isShowWeekNumbers() ? 1 : 0);
				final double leftSpace = snapSpace(getInsets().getLeft());
				final double rightSpace = snapSpace(getInsets().getRight());
				final double hgaps = snapSpace(getHgap()) * (nCols - 1);
				// compute content width
				final double contentWidth = super.computePrefWidth(height) - leftSpace - rightSpace - hgaps;
				return ((snapSize(contentWidth / nCols)) * nCols) + leftSpace + rightSpace + hgaps;
			}

			@Override
			protected void layoutChildren() {
				if (getWidth() > 0 && getHeight() > 0) {
					super.layoutChildren();
				}
			}
		};
		contentGrid.setFocusTraversable(true);
		contentGrid.setStyle("-fx-background-color : transparent; -fx-padding : 0, 12, 12, 12");
		contentGrid.getStyleClass().add("calendar-grid");
		//		contentGrid.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
		//		contentGrid.setPadding(new Insets(0, 12, 12, 12));
		contentGrid.setVgap(0);
		contentGrid.setHgap(0);

		// create week days cells
		createWeekDaysCells();
		// create month days cells
		createDayCells();

		VBox contentHolder = new VBox();
		// create content pane
		contentHolder.getChildren().setAll(createCalendarMonthLabelPane(), contentGrid);
		// add month arrows pane
		calendarPlaceHolder.getChildren().setAll(contentHolder, createCalendarArrowsPane());

		Rectangle clip = new Rectangle();
		clip.widthProperty().bind(calendarPlaceHolder.widthProperty());
		clip.heightProperty().bind(calendarPlaceHolder.heightProperty());
		calendarPlaceHolder.setClip(clip);

		// create years list view
		for (int i = 0; i <= 200; i++)
			yearsListView.getItems().add("" + (1900 + i));
		yearsListView.setVisible(false);
		yearsListView.setOpacity(0);
		yearsListView.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));

		StackPane contentPlaceHolder = new StackPane();
		yearsListView.maxWidthProperty().bind(contentPlaceHolder.widthProperty());
		yearsListView.maxHeightProperty().bind(contentPlaceHolder.heightProperty());
		contentPlaceHolder.getChildren().setAll(calendarPlaceHolder, yearsListView);
		getChildren().add(contentPlaceHolder);

		//시간을 나타내는 필드 input.
		getChildren().add(createTimeContent());

		refresh();

		addEventHandler(KeyEvent.ANY, event -> {
			Node node = getScene().getFocusOwner();
			if (node instanceof JFXDateTimeCell)
				currentFocusedDayCell = (JFXDateTimeCell) node;

			switch (event.getCode()) {
			case HOME:
				// go to the current date
				init();
				goToDate(LocalDateTime.now(), true);
				event.consume();
				break;
			case PAGE_UP:
				if (!backMonthButton.isDisabled())
					forward(-1, MONTHS, true, true);
				event.consume();
				break;
			case PAGE_DOWN:
				if (!forwardMonthButton.isDisabled())
					forward(1, MONTHS, true, true);
				event.consume();
				break;
			case ESCAPE:
				datePicker.hide();
				event.consume();
				break;
			case F4:
			case F10:
			case UP:
			case DOWN:
			case LEFT:
			case RIGHT:
			case TAB:
				break;
			default:
				event.consume();
			}
		});

		// create animation
		showTransition = new CachedTransition(yearsListView, new Timeline(
				new KeyFrame(Duration.millis(0), new KeyValue(yearsListView.opacityProperty(), 0, Interpolator.EASE_BOTH),
						new KeyValue(calendarPlaceHolder.opacityProperty(), 1, Interpolator.EASE_BOTH)),
				new KeyFrame(Duration.millis(500), new KeyValue(yearsListView.opacityProperty(), 0, Interpolator.EASE_BOTH),
						new KeyValue(calendarPlaceHolder.opacityProperty(), 0, Interpolator.EASE_BOTH)),
				new KeyFrame(Duration.millis(1000), new KeyValue(yearsListView.opacityProperty(), 1, Interpolator.EASE_BOTH),
						new KeyValue(calendarPlaceHolder.opacityProperty(), 0, Interpolator.EASE_BOTH),
						new KeyValue(selectedYearLabel.textFillProperty(), Color.WHITE, Interpolator.EASE_BOTH),
						new KeyValue(selectedDateLabel.textFillProperty(), Color.rgb(255, 255, 255, 0.67), Interpolator.EASE_BOTH)))) {
			protected void starting() {
				super.starting();
				yearsListView.setVisible(true);
			};

			{
				setCycleDuration(Duration.millis(320));
				setDelay(Duration.seconds(0));
			}
		};

		hideTransition = new CachedTransition(yearsListView, new Timeline(
				new KeyFrame(Duration.millis(0), new KeyValue(yearsListView.opacityProperty(), 1, Interpolator.EASE_BOTH),
						new KeyValue(calendarPlaceHolder.opacityProperty(), 0, Interpolator.EASE_BOTH)),
				new KeyFrame(Duration.millis(500), new KeyValue(yearsListView.opacityProperty(), 0, Interpolator.EASE_BOTH),
						new KeyValue(calendarPlaceHolder.opacityProperty(), 0, Interpolator.EASE_BOTH)),
				new KeyFrame(Duration.millis(1000), new KeyValue(yearsListView.opacityProperty(), 0, Interpolator.EASE_BOTH),
						new KeyValue(calendarPlaceHolder.opacityProperty(), 1, Interpolator.EASE_BOTH),
						new KeyValue(selectedDateLabel.textFillProperty(), Color.WHITE, Interpolator.EASE_BOTH),
						new KeyValue(selectedYearLabel.textFillProperty(), Color.rgb(255, 255, 255, 0.67), Interpolator.EASE_BOTH)))) {
			@Override
			protected void stopping() {
				super.stopping();
				yearsListView.setVisible(false);
			}

			{
				setCycleDuration(Duration.millis(320));
				setDelay(Duration.seconds(0));
			}
		};

	}

	private void createWeekDaysCells() {
		// create week days names
		for (int i = 0; i < daysPerWeek; i++) {
			JFXDateTimeCell cell = new JFXDateTimeCell();
			cell.getStyleClass().add("day-name-cell");
			cell.setTextFill(Color.valueOf("#9C9C9C"));
			cell.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
			cell.setFont(Font.font("Roboto", FontWeight.BOLD, 12));
			cell.setAlignment(Pos.BASELINE_CENTER);
			weekDaysCells.add(cell);
		}
		// create week days numbers
		for (int i = 0; i < 6; i++) {
			JFXDateTimeCell cell = new JFXDateTimeCell();
			cell.getStyleClass().add("week-number-cell");
			cell.setTextFill(Color.valueOf("#9C9C9C"));
			cell.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
			cell.setFont(Font.font("Roboto", FontWeight.BOLD, 12));
			weekNumberCells.add(cell);
		}
	}

	/*
	 * header panel represents the selected Date
	 * we keep javaFX original style classes
	 */
	protected VBox createHeaderPane() {

		// Year label
		selectedYearLabel = new Label();
		selectedYearLabel.setStyle("-fx-text-fill:white; -fx-font-weight: bold; -fx-font-size : 14; -fx-opiticy : 0.67 ; ");
		selectedYearLabel.getStyleClass().add("spinner-label");
		//		selectedYearLabel.setTextFill(Color.rgb(255, 255, 255, 0.67));
		//		selectedYearLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 14));
		// Year label container
		HBox yearLabelContainer = new HBox();
		yearLabelContainer.getStyleClass().add("spinner");
		yearLabelContainer.getChildren().addAll(selectedYearLabel);
		yearLabelContainer.setAlignment(Pos.CENTER_LEFT);
		yearLabelContainer.setFillHeight(false);
		yearLabelContainer.setOnMouseClicked((click) -> {
			if (!yearsListView.isVisible()) {
				int yearIndex = Integer.parseInt(selectedYearLabel.getText()) - 1900 - 2;
				yearsListView.scrollTo(yearIndex >= 0 ? yearIndex : yearIndex + 2);
				hideTransition.stop();
				showTransition.play();
			}
		});

		// selected date label
		selectedDateLabel = new Label();
		selectedDateLabel.setStyle("-fx-text-fill: white; -fx-font-weight : bold ; -fx-font-size : 28");
		selectedDateLabel.getStyleClass().add("spinner-label");
		//		selectedDateLabel.setTextFill(Color.WHITE);
		//		selectedDateLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 28));

		// selected Time Label
		selectedTimeLabel = new Label();
		selectedTimeLabel.setStyle("-fx-text-fill: white; -fx-font-weight : bold ; -fx-font-size : 28");
		selectedTimeLabel.getStyleClass().add("spinner-label");
		//		selectedTimeLabel.setTextFill(Color.WHITE);
		//		selectedTimeLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 28));

		// selected date label container
		HBox selectedDateContainer = new HBox();
		selectedDateContainer.getStyleClass().add("spinner");
		selectedDateContainer.getChildren().addAll(selectedDateLabel, selectedTimeLabel);
		selectedDateContainer.setAlignment(Pos.CENTER_LEFT);
		selectedDateContainer.setOnMouseClicked((click) -> {
			if (yearsListView.isVisible()) {
				showTransition.stop();
				hideTransition.play();
			}
		});

		VBox headerPanel = new VBox();

		Color defaultColor = (Color) this.datePicker.getDefaultColor();
		String rgbCode = FxUtil.toRGBCode(defaultColor);
		headerPanel.setStyle("-fx-background-color : " + rgbCode + "; -fx-padding : 12, 24, 12 ,24");
		headerPanel.getStyleClass().add("month-year-pane");
		//		headerPanel.setBackground(new Background(new BackgroundFill(defaultColor, CornerRadii.EMPTY, Insets.EMPTY)));
		//		headerPanel.setPadding(new Insets(12, 24, 12, 24));

		headerPanel.getChildren().add(yearLabelContainer);
		headerPanel.getChildren().add(selectedDateContainer);

		return headerPanel;
	}

	/*
	 * methods to create the content of the date picker
	 */
	protected BorderPane createCalendarArrowsPane() {

		SVGGlyph leftChevron = new SVGGlyph(0, "CHEVRON_LEFT",
				"M 742,-37 90,614 Q 53,651 53,704.5 53,758 90,795 l 652,651 q 37,37 90.5,37 53.5,0 90.5,-37 l 75,-75 q 37,-37 37,-90.5 0,-53.5 -37,-90.5 L 512,704 998,219 q 37,-38 37,-91 0,-53 -37,-90 L 923,-37 Q 886,-74 832.5,-74 779,-74 742,-37 z",
				Color.GRAY);
		SVGGlyph rightChevron = new SVGGlyph(0, "CHEVRON_RIGHT",
				"m 1099,704 q 0,-52 -37,-91 L 410,-38 q -37,-37 -90,-37 -53,0 -90,37 l -76,75 q -37,39 -37,91 0,53 37,90 l 486,486 -486,485 q -37,39 -37,91 0,53 37,90 l 76,75 q 36,38 90,38 54,0 90,-38 l 652,-651 q 37,-37 37,-90 z",
				Color.GRAY);
		leftChevron.setFill(Color.valueOf("#313131"));
		leftChevron.setSize(6, 11);
		rightChevron.setFill(Color.valueOf("#313131"));
		rightChevron.setSize(6, 11);

		backMonthButton = new JFXButton();
		backMonthButton.setMinSize(40, 40);
		backMonthButton.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, new CornerRadii(40), Insets.EMPTY)));
		backMonthButton.getStyleClass().add("left-button");
		backMonthButton.setGraphic(leftChevron);
		backMonthButton.setRipplerFill(this.datePicker.getDefaultColor());
		backMonthButton.setOnAction(t -> {
			forward(-1, MONTHS, false, true);
		});

		forwardMonthButton = new JFXButton();
		forwardMonthButton.setMinSize(40, 40);
		forwardMonthButton.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, new CornerRadii(40), Insets.EMPTY)));
		forwardMonthButton.getStyleClass().add("right-button");
		forwardMonthButton.setGraphic(rightChevron);
		forwardMonthButton.setRipplerFill(this.datePicker.getDefaultColor());
		forwardMonthButton.setOnAction(t -> {
			forward(1, MONTHS, false, true);
		});

		BorderPane arrowsContainer = new BorderPane();
		arrowsContainer.setLeft(backMonthButton);
		arrowsContainer.setRight(forwardMonthButton);
		arrowsContainer.setPadding(new Insets(4, 12, 2, 12));
		arrowsContainer.setPickOnBounds(false);
		return arrowsContainer;
	}

	protected BorderPane createCalendarMonthLabelPane() {
		monthYearLabel = new Label();
		monthYearLabel.getStyleClass().add("spinner-label");
		monthYearLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 13));
		monthYearLabel.setTextFill(Color.valueOf("#313131"));

		BorderPane monthContainer = new BorderPane();
		monthContainer.setMinHeight(50);
		monthContainer.setCenter(monthYearLabel);
		monthContainer.setPadding(new Insets(2, 12, 2, 12));
		return monthContainer;
	}

	static final String EVRY_HOUR_FORMAT = "Hour (%d)";
	static final String EVRY_MIN_FORMAT = "Min. (%d)";
	static final String EVRY_SEC_FORMAT = "Sec. (%d)";

	/**
	 * 시간을 나타내는 필드를 표현.
	 * @작성자 : KYJ
	 * @작성일 : 2016. 11. 24.
	 * @return
	 */
	protected VBox createTimeContent() {

		Label lblHour = new Label("Hour");
		Label lblMin = new Label("Minute");
		Label lblSec = new Label("Second");

		HBox hbHour = new HBox(5, lblHour, jfHour);
		HBox hbMin = new HBox(5, lblMin, jfMin);
		HBox hbSec = new HBox(5, lblSec, jfSec);

		hbHour.setAlignment(Pos.CENTER);
		hbMin.setAlignment(Pos.CENTER);
		hbSec.setAlignment(Pos.CENTER);

		lblHour.setStyle("-fx-text-fill:white; -fx-font-weight: bold ; -fx-font-size : 12px");
		//		lblHour.setFont(Font.font("Roboto", FontWeight.BOLD, 12));
		lblHour.setPrefWidth(80d);
		lblSec.getStyleClass().add("date-picker-time-hour");

		lblMin.setStyle("-fx-text-fill:white; -fx-font-weight: bold ; -fx-font-size : 12px");
		//		lblMin.setFont(Font.font("Roboto", FontWeight.BOLD, 12));
		lblMin.setPrefWidth(80d);
		lblSec.getStyleClass().add("date-picker-time-min");

		lblSec.setStyle("-fx-text-fill:white; -fx-font-weight: bold ; -fx-font-size : 12px");
		lblSec.setFont(Font.font("Roboto", FontWeight.BOLD, 12));
		lblSec.setPrefWidth(80d);
		lblSec.getStyleClass().add("date-picker-time-sec");

		VBox vBox = new VBox(5, hbHour, hbMin, hbSec);
		vBox.getStyleClass().add("data-time-picker-time-container");
		vBox.setPrefSize(VBox.USE_COMPUTED_SIZE, VBox.USE_COMPUTED_SIZE);
		vBox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		vBox.setBackground(new Background(new BackgroundFill(this.datePicker.getDefaultColor(), CornerRadii.EMPTY, Insets.EMPTY)));
		vBox.setAlignment(Pos.CENTER);

		vBox.setPadding(new Insets(5));
		return vBox;
	}

	/**
	 * 시(Hour) UP-DOWN Field
	 * @작성자 : KYJ
	 * @작성일 : 2016. 11. 24.
	 * @param defHour
	 * @return
	 */
	private JFXSlider createJFXSliderForHour(int defHour) {
		JFXSlider createJFXSlider = createJFXSlider(0, 23, defHour);
		createJFXSlider.getStyleClass().add("data-time-picker-hour-slider");
		return createJFXSlider;
	}

	/**
	 * 분(Minute) UP-DOWN Field
	 *
	 * @작성자 : KYJ
	 * @작성일 : 2016. 4. 14.
	 * @param defHour
	 *            디폴트 시간
	 * @return
	 */
	private JFXSlider createNumberSpinnerForMinute(int defaultMinute) {
		JFXSlider createJFXSlider = createJFXSlider(0, 59, defaultMinute);
		createJFXSlider.getStyleClass().add("data-time-picker-minute-slider");
		return createJFXSlider;
	}

	/**
	 * 분(Minute) UP-DOWN Field
	 *
	 * @작성자 : KYJ
	 * @작성일 : 2016. 4. 14.
	 * @param defHour
	 *            디폴트 시간
	 * @return
	 */
	private JFXSlider createNumberSpinnerForSecond(int defaultSecond) {
		JFXSlider createJFXSlider = createJFXSlider(0, 59, defaultSecond);
		createJFXSlider.getStyleClass().add("data-time-picker-second-slider");
		return createJFXSlider;
	}

	/**
	 * @작성자 : KYJ
	 * @작성일 : 2016. 11. 24.
	 * @param min
	 * @param max
	 * @param def
	 * @return
	 */
	protected JFXSlider createJFXSlider(int min, int max, int def) {
		JFXSlider jfxSlider = new JFXSlider(min, max, def);
		jfxSlider.setMajorTickUnit(1d);
		jfxSlider.setMinorTickCount(1);
		jfxSlider.setBlockIncrement(1d);

		jfxSlider.setPrefSize(150d, Spinner.USE_COMPUTED_SIZE);
		return jfxSlider;
	}

	void updateContentGrid() {
		contentGrid.getColumnConstraints().clear();
		contentGrid.getChildren().clear();
		int colsNumber = daysPerWeek + (datePicker.isShowWeekNumbers() ? 1 : 0);
		ColumnConstraints columnConstraints = new ColumnConstraints();
		columnConstraints.setPercentWidth(100);
		for (int i = 0; i < colsNumber; i++)
			contentGrid.getColumnConstraints().add(columnConstraints);

		// Week days cells
		for (int i = 0; i < daysPerWeek; i++)
			contentGrid.add(weekDaysCells.get(i), i + colsNumber - daysPerWeek, 1);

		// Week number cells
		if (datePicker.isShowWeekNumbers())
			for (int i = 0; i < 6; i++)
				contentGrid.add(weekNumberCells.get(i), 0, i + 2);

		// Month days cells
		for (int row = 0; row < 6; row++)
			for (int col = 0; col < daysPerWeek; col++)
				contentGrid.add(dayCells.get(row * daysPerWeek + col), col + colsNumber - daysPerWeek, row + 2);
	}

	private void refresh() {
		updateDayNameCells();
		updateValues();
	}

	void updateDayNameCells() {
		int weekFirstDay = WeekFields.of(getLocale()).getFirstDayOfWeek().getValue();
		LocalDate date = LocalDate.of(2009, 7, 12 + weekFirstDay);
		for (int i = 0; i < daysPerWeek; i++) {
			String name = weekDayNameFormatter.withLocale(getLocale()).format(date.plus(i, DAYS));
			// Fix Chinese environment week display incorrectly
			if (weekDayNameFormatter.getLocale() == java.util.Locale.CHINA) {
				name = name.substring(2, 3).toUpperCase();
			} else {
				name = name.substring(0, 1).toUpperCase();
			}
			weekDaysCells.get(i).setText(name);
		}
	}

	void updateValues() {
		updateWeekNumberDateCells();
		updateDayCells();
		updateMonthYearPane();
	}

	void updateHour(int hour) {
		updateTime(hour, -1, -1);
	}

	void updateMinute(int min) {
		updateTime(-1, min, -1);
	}

	void updateSecond(int sec) {
		updateTime(-1, -1, sec);
	}

	private LocalDateTime tmpNow = LocalDateTime.now();

	void updateTime(int hour, int min, int sec) {
		LocalDateTime value = datePicker.getValue();
		if (value == null) {
			value = tmpNow;
		}

		if (hour != -1) {
			value = value.withHour(hour);
		}

		if (min != -1) {
			value = value.withMinute(min);
		}

		if (sec != -1) {
			value = value.withSecond(sec);
		}

		
		selectedTimeLabel.setText(DateTimeFormatter.ofPattern("HH:mm:ss").format(value));
		this.datePicker.setValue(value);
	}

	void updateWeekNumberDateCells() {
		if (datePicker.isShowWeekNumbers()) {
			final Locale locale = getLocale();
			LocalDate firstDayOfMonth = selectedYearMonth.get().atDay(1);
			for (int i = 0; i < 6; i++) {
				LocalDate date = firstDayOfMonth.plus(i, WEEKS);
				String weekNumber = weekNumberFormatter.withLocale(locale).withDecimalStyle(DecimalStyle.of(locale)).format(date);
				weekNumberCells.get(i).setText(weekNumber);
			}
		}
	}

	void updateDayCells() {
		Locale locale = getLocale();
		Chronology chrono = getPrimaryChronology();
		// get the index of the first day of the month
		int firstDayOfWeek = WeekFields.of(getLocale()).getFirstDayOfWeek().getValue();
		int firstOfMonthIndex = selectedYearMonth.get().atDay(1).getDayOfWeek().getValue() - firstDayOfWeek;
		firstOfMonthIndex += firstOfMonthIndex < 0 ? daysPerWeek : 0;
		YearMonth currentYearMonth = selectedYearMonth.get();

		int daysInCurMonth = -1;

		for (int i = 0; i < 6 * daysPerWeek; i++) {
			JFXDateTimeCell dayCell = dayCells.get(i);
			dayCell.getStyleClass().setAll("cell", "date-cell", "day-cell");
			dayCell.setPrefSize(40, 42);
			dayCell.setDisable(false);
			dayCell.setStyle(null);
			dayCell.setGraphic(null);
			dayCell.setTooltip(null);
			dayCell.setTextFill(Color.valueOf("#313131"));
			dayCell.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));

			try {
				if (daysInCurMonth == -1)
					daysInCurMonth = currentYearMonth.lengthOfMonth();

				YearMonth month = currentYearMonth;
				int dayIndex = i - firstOfMonthIndex + 1;

				LocalDateTime date = LocalDateTime.of(month.atDay(dayIndex),
						LocalTime.of((int) jfHour.getValue(), (int) jfMin.getValue(), (int) jfSec.getValue()));

				//				LocalDate date = month.atDay(dayIndex);

				dayCellDates[i] = date;

				// if it's today
				if (date.equals(LocalDateTime.now())) {
					dayCell.setTextFill(this.datePicker.getDefaultColor());
					dayCell.getStyleClass().add("today");
				}
				// if it's the current selected value
				if (date.equals(datePicker.getValue())) {
					dayCell.getStyleClass().add("selected");
					dayCell.setTextFill(Color.WHITE);
					dayCell.setBackground(
							new Background(new BackgroundFill(this.datePicker.getDefaultColor(), new CornerRadii(40), Insets.EMPTY)));
				}

				ChronoLocalDate cDate = chrono.date(date);
				String cellText = dayCellFormatter.withLocale(locale).withChronology(chrono).withDecimalStyle(DecimalStyle.of(locale))
						.format(cDate);
				dayCell.setText(cellText);
				if (i < firstOfMonthIndex) {
					dayCell.getStyleClass().add("previous-month");
					dayCell.setText("");
				} else if (i >= firstOfMonthIndex + daysInCurMonth) {
					dayCell.getStyleClass().add("next-month");
					dayCell.setText("");
				}
				// update cell item
				dayCell.updateItem(date, false);
			} catch (DateTimeException ex) {
				// Disable day cell if its date is out of range
				dayCell.setText("");
				dayCell.setDisable(true);
			}
		}
	}

	protected void updateMonthYearPane() {
		// update date labels
		YearMonth yearMonth = selectedYearMonth.get();
		LocalDateTime value = datePicker.getValue();

		if (value != null) {
			selectedDateLabel.setText(DateTimeFormatter.ofPattern("EEE, MMM yy ").format(value));
			selectedTimeLabel.setText(DateTimeFormatter.ofPattern("HH:mm:ss").format(value));
		} else {
			LocalDateTime now = LocalDateTime.now();
			selectedDateLabel.setText(DateTimeFormatter.ofPattern("EEE, MMM yy ").format(now));
			selectedTimeLabel.setText(DateTimeFormatter.ofPattern("HH:mm:ss").format(now));
		}

		selectedYearLabel.setText(formatYear(yearMonth));
		monthYearLabel.setText(formatMonth(yearMonth) + " " + formatYear(yearMonth));

		Chronology chrono = datePicker.getChronology();

		//로컬타임 새로 적용
		LocalDateTime firstDayOfMonth = LocalDateTime.of(yearMonth.atDay(1),
				LocalTime.of((int) jfHour.getValue(), (int) jfMin.getValue(), (int) jfSec.getValue()));
		//기존 코드 삭제
		//		LocalDateTime firstDayOfMonth = yearMonth.atDay(1);
		backMonthButton.setDisable(!isValidDate(chrono, firstDayOfMonth, -1, DAYS));
		forwardMonthButton.setDisable(!isValidDate(chrono, firstDayOfMonth, +1, MONTHS));
	}

	private String formatMonth(YearMonth yearMonth) {
		try {
			Chronology chrono = getPrimaryChronology();
			ChronoLocalDate cDate = chrono.date(yearMonth.atDay(1));
			String str = monthFormatter.withLocale(getLocale()).withChronology(chrono).format(cDate);
			return str;
		} catch (DateTimeException ex) {
			// Date is out of range.
			return "";
		}
	}

	private String formatYear(YearMonth yearMonth) {
		try {
			Chronology chrono = getPrimaryChronology();
			ChronoLocalDate cDate = chrono.date(yearMonth.atDay(1));
			String str = yearFormatter.withLocale(getLocale()).withChronology(chrono).withDecimalStyle(DecimalStyle.of(getLocale()))
					.format(cDate);
			return str;
		} catch (DateTimeException ex) {
			// Date is out of range.
			return "";
		}
	}

	protected LocalDateTime dayCellDate(JFXDateTimeCell dateCell) {
		assert (dayCellDates != null);
		LocalDateTime localDate = dayCellDates[dayCells.indexOf(dateCell)];
		return localDate.withHour((int)jfHour.getValue()).withMinute((int) jfMin.getValue()).withSecond((int) jfSec.getValue());
//		return localDate;
//				return localDate.atTime((int) jfHour.getValue(), (int) jfMin.getValue(), (int) jfSec.getValue());
	}

	protected void forward(int offset, ChronoUnit unit, boolean focusDayCell, boolean withAnimation) {
		if (withAnimation) {
			if (tempImageTransition == null || tempImageTransition.getStatus().equals(Status.STOPPED)) {
				Pane monthContent = (Pane) calendarPlaceHolder.getChildren().get(0);
				this.getParent().setManaged(false);
				WritableImage temp = monthContent.snapshot(new SnapshotParameters(),
						new WritableImage((int) monthContent.getWidth(), (int) monthContent.getHeight()));
				ImageView tempImage = new ImageView(temp);
				calendarPlaceHolder.getChildren().add(calendarPlaceHolder.getChildren().size() - 2, tempImage);
				TranslateTransition imageTransition = new TranslateTransition(Duration.millis(160), tempImage);
				imageTransition.setToX(-offset * calendarPlaceHolder.getWidth());
				imageTransition.setOnFinished((finish) -> calendarPlaceHolder.getChildren().remove(tempImage));
				monthContent.setTranslateX(offset * calendarPlaceHolder.getWidth());
				TranslateTransition contentTransition = new TranslateTransition(Duration.millis(160), monthContent);
				contentTransition.setToX(0);

				tempImageTransition = new ParallelTransition(imageTransition, contentTransition);
				tempImageTransition.setOnFinished((finish) -> {
					calendarPlaceHolder.getChildren().remove(tempImage);
					this.getParent().setManaged(true);
				});
				tempImageTransition.play();
			}
		}
		YearMonth yearMonth = selectedYearMonth.get();
		JFXDateTimeCell dateCell = currentFocusedDayCell;
		if (dateCell == null || !dayCellDate(dateCell).getMonth().equals(yearMonth.getMonth())) {
			//기존코드 주석
			//			LocalDate atDay = yearMonth.atDay(1);
			LocalDateTime atDay = LocalDateTime.of(yearMonth.atDay(1),
					LocalTime.of((int) jfHour.getValue(), (int) jfMin.getValue(), (int) jfSec.getValue()));
			dateCell = findDayCellOfDate(atDay);
		}

		goToDayCell(dateCell, offset, unit, focusDayCell);
	}

	public void goToDayCell(JFXDateTimeCell dateCell, int offset, ChronoUnit unit, boolean focusDayCell) {
		goToDate(dayCellDate(dateCell).plus(offset, unit), focusDayCell);
	}

	public void goToDate(LocalDateTime date, boolean focusDayCell) {
		if (isValidDate(datePicker.getChronology(), date)) {
			selectedYearMonth.set(YearMonth.from(date));
			if (focusDayCell)
				findDayCellOfDate(date).requestFocus();
		}
	}

	public void selectDayCell(JFXDateTimeCell dateCell) {
		datePicker.setValue(dayCellDate(dateCell));
		datePicker.hide();
	}

	public JFXDateTimeCell findDayCellOfDate(LocalDateTime date) {
		for (int i = 0; i < dayCellDates.length; i++)
			if (date.equals(dayCellDates[i]))
				return dayCells.get(i);
		return dayCells.get(dayCells.size() / 2 + 1);
	}

	void init() {
		calendarPlaceHolder.setOpacity(1);
		selectedDateLabel.setTextFill(Color.WHITE);
		selectedYearLabel.setTextFill(Color.rgb(255, 255, 255, 0.67));
		yearsListView.setOpacity(0);
		yearsListView.setVisible(false);
	}

	void clearFocus() {
		LocalDateTime focusDate = datePicker.getValue();
		if (focusDate == null)
			focusDate = LocalDateTime.now();
		if (YearMonth.from(focusDate).equals(selectedYearMonth.get()))
			goToDate(focusDate, true);
	}

	protected void createDayCells() {
		for (int row = 0; row < 6; row++) {
			for (int col = 0; col < daysPerWeek; col++) {
				JFXDateTimeCell dayCell = createDayCell();
				dayCell.addEventHandler(MouseEvent.MOUSE_CLICKED, click -> {
					// allow date selection on mouse primary button click
					if (click.getButton() != MouseButton.PRIMARY)
						return;
					JFXDateTimeCell selectedDayCell = (JFXDateTimeCell) click.getSource();

					selectDayCell(selectedDayCell);
					currentFocusedDayCell = selectedDayCell;
				});
				// add mouse hover listener
				dayCell.setOnMouseEntered((event) -> {
					if (!dayCell.getStyleClass().contains("selected"))
						dayCell.setBackground(
								new Background(new BackgroundFill(Color.valueOf("#EDEDED"), new CornerRadii(40), Insets.EMPTY)));
				});
				dayCell.setOnMouseExited((event) -> {
					if (!dayCell.getStyleClass().contains("selected"))
						dayCell.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
				});
				dayCell.setAlignment(Pos.BASELINE_CENTER);
				dayCell.setBorder(
						new Border(new BorderStroke(Color.TRANSPARENT, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(5))));
				dayCell.setFont(Font.font("Roboto", FontWeight.BOLD, 12));
				dayCells.add(dayCell);
			}
		}
		dayCellDates = new LocalDateTime[6 * daysPerWeek];
		// position the cells into the grid
		updateContentGrid();
	}

	private JFXDateTimeCell createDayCell() {
		JFXDateTimeCell dayCell = null;
		// call cell factory if set by the user
		if (datePicker.getDayCellFactory() != null)
			dayCell = datePicker.getDayCellFactory().call(datePicker);
		// else create the defaul day cell
		if (dayCell == null)
			dayCell = new JFXDateTimeCell();
		return dayCell;
	}

	/**
	 * this method must be overriden when implementing other Chronolgy
	 */
	protected Chronology getPrimaryChronology() {
		return datePicker.getChronology();
	}

	protected Locale getLocale() {
		// for android compatibility
		return Locale.getDefault(/*Locale.Category.FORMAT*/);
	}

	protected boolean isValidDate(Chronology chrono, LocalDateTime date, int offset, ChronoUnit unit) {
		if (date != null)
			return isValidDate(chrono, date.plus(offset, unit));
		return false;
	}

	protected boolean isValidDate(Chronology chrono, LocalDateTime date) {
		try {
			if (date != null)
				chrono.date(date);
			return true;
		} catch (DateTimeException ex) {
			return false;
		}
	}
}
