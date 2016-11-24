/********************************
 *	프로젝트 : Custom-JFoenix
 *	패키지   : com.jfoenix.controls
 *	작성일   : 2016. 11. 24.
 *	프로젝트 : OPERA
 *	작성자   : KYJ
 *******************************/
package com.jfoenix.controls;

import java.time.LocalDateTime;
import java.time.chrono.Chronology;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.jfoenix.skins.JFXDateTimePickerSkin;
import com.sun.javafx.css.converters.BooleanConverter;
import com.sun.javafx.css.converters.PaintConverter;
import com.sun.javafx.scene.control.skin.resources.ControlResources;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.WritableValue;
import javafx.css.CssMetaData;
import javafx.css.SimpleStyleableBooleanProperty;
import javafx.css.SimpleStyleableObjectProperty;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.geometry.Insets;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Cell;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.Control;
import javafx.scene.control.DateCell;
import javafx.scene.control.Skin;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.LocalDateTimeStringConverter;

/**
 * @author KYJ
 *
 */
public class JFXDateTimePicker extends ComboBoxBase<LocalDateTime> {

	/**
	 * {@inheritDoc}
	 */
	public JFXDateTimePicker() {
		this(null);
		initialize();
	}

	/**
	 * {@inheritDoc}
	 */
	public JFXDateTimePicker(LocalDateTime localDate) {
		setValue(localDate);
		getStyleClass().add(DEFAULT_STYLE_CLASS);
		setAccessibleRole(AccessibleRole.DATE_PICKER);
		setEditable(true);
		initialize();
	}

	private void initialize() {
		this.getStyleClass().add(DEFAULT_STYLE_CLASS);
		setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Skin<?> createDefaultSkin() {
		return new JFXDateTimePickerSkin(this);
	}

	/***************************************************************************
	 *                                                                         *
	 * Properties                                                              *
	 *                                                                         *
	 **************************************************************************/

	/**
	 * the parent node used when showing the data picker content as an overlay,
	 * intead of a popup
	 */
	private ObjectProperty<StackPane> dialogParent = new SimpleObjectProperty<>(null);

	public final ObjectProperty<StackPane> dialogParentProperty() {
		return this.dialogParent;
	}

	public final StackPane getDialogParent() {
		return this.dialogParentProperty().get();
	}

	public final void setDialogParent(final StackPane dialogParent) {
		this.dialogParentProperty().set(dialogParent);
	}

	/**
	 * property that holds the time value if showing the time picker
	 */
	private ObjectProperty<LocalDateTime> lastValidTime = new SimpleObjectProperty<>();

	public final ObjectProperty<LocalDateTime> timeProperty() {
		return this.lastValidTime;
	}

	public final LocalDateTime getTime() {
		return this.timeProperty().get();
	}

	public final void setTime(final LocalDateTime lastValidTime) {
		this.timeProperty().set(lastValidTime);
	}

	/***************************************************************************
	 *                                                                         *
	 * Stylesheet Handling                                                     *
	 *                                                                         *
	 **************************************************************************/

	/**
	 *
	 * Initialize the style class to 'jfx-date-time-picker'.
	 *
	 * This is the selector class from which CSS can be used to style
	 * this control.
	 */
	private static final String DEFAULT_STYLE_CLASS = "jfx-date-time-picker";

	/**
	 * show the popup as an overlay using JFXDialog
	 * NOTE: to show it properly the scene root must be StackPane, or the user must set
	 * the dialog parent manually using the property {{@link #dialogParentProperty()}
	 */
	private StyleableBooleanProperty overLay = new SimpleStyleableBooleanProperty(StyleableProperties.OVERLAY, JFXDateTimePicker.this,
			"overLay", false);

	public final StyleableBooleanProperty overLayProperty() {
		return this.overLay;
	}

	public final boolean isOverLay() {
		return overLay == null ? false : this.overLayProperty().get();
	}

	public final void setOverLay(final boolean overLay) {
		this.overLayProperty().set(overLay);
	}

	/**
	 * the default color used in the data picker content
	 */
	private StyleableObjectProperty<Paint> defaultColor = new SimpleStyleableObjectProperty<Paint>(StyleableProperties.DEFAULT_COLOR,
			JFXDateTimePicker.this, "defaultColor", Color.valueOf("#009688"));

	public Paint getDefaultColor() {
		return defaultColor == null ? Color.valueOf("#009688") : defaultColor.get();
	}

	public StyleableObjectProperty<Paint> defaultColorProperty() {
		return this.defaultColor;
	}

	public void setDefaultColor(Paint color) {
		this.defaultColor.set(color);
	}

	private static class StyleableProperties {

		private static final List<CssMetaData<? extends Styleable, ?>> CHILD_STYLEABLES;

		private static final CssMetaData<JFXDateTimePicker, Paint> DEFAULT_COLOR = new CssMetaData<JFXDateTimePicker, Paint>(
				"-fx-default-color", PaintConverter.getInstance(), Color.valueOf("#5A5A5A")) {
			@Override
			public boolean isSettable(JFXDateTimePicker control) {
				return control.defaultColor == null || !control.defaultColor.isBound();
			}

			@Override
			public StyleableProperty<Paint> getStyleableProperty(JFXDateTimePicker control) {
				return control.defaultColorProperty();
			}
		};

		private static final CssMetaData<JFXDateTimePicker, Boolean> OVERLAY = new CssMetaData<JFXDateTimePicker, Boolean>("-fx-overlay",
				BooleanConverter.getInstance(), false) {
			@Override
			public boolean isSettable(JFXDateTimePicker control) {
				return control.overLay == null || !control.overLay.isBound();
			}

			@Override
			public StyleableBooleanProperty getStyleableProperty(JFXDateTimePicker control) {
				return control.overLayProperty();
			}
		};

		private static final String country = Locale.getDefault(Locale.Category.FORMAT).getCountry();
		private static final CssMetaData<JFXDateTimePicker, Boolean> SHOW_WEEK_NUMBERS = new CssMetaData<JFXDateTimePicker, Boolean>(
				"-fx-show-week-numbers", BooleanConverter.getInstance(),
				(!country.isEmpty() && ControlResources.getNonTranslatableString("DatePicker.showWeekNumbers").contains(country))) {
			@Override
			public boolean isSettable(JFXDateTimePicker n) {
				return n.showWeekNumbers == null || !n.showWeekNumbers.isBound();
			}

			@Override
			public StyleableProperty<Boolean> getStyleableProperty(JFXDateTimePicker n) {
				return (StyleableProperty<Boolean>) (WritableValue<Boolean>) n.showWeekNumbersProperty();
			}
		};

		static {
			final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<CssMetaData<? extends Styleable, ?>>(
					Control.getClassCssMetaData());
			Collections.addAll(styleables, DEFAULT_COLOR, OVERLAY, SHOW_WEEK_NUMBERS);
			CHILD_STYLEABLES = Collections.unmodifiableList(styleables);
		}

		//		static {
		//			final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<CssMetaData<? extends Styleable, ?>>(
		//					Control.getClassCssMetaData());
		//			Collections.addAll(styleables, SHOW_WEEK_NUMBERS);
		//			CHILD_STYLEABLES = Collections.unmodifiableList(styleables);
		//		}

	}

	// inherit the styleable properties from parent
	private List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

	@Override
	public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
		if (STYLEABLES == null) {
			final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<CssMetaData<? extends Styleable, ?>>(
					Control.getClassCssMetaData());
			styleables.addAll(getClassCssMetaData());
			styleables.addAll(super.getClassCssMetaData());
			STYLEABLES = Collections.unmodifiableList(styleables);
		}
		return STYLEABLES;
	}

	public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
		return StyleableProperties.CHILD_STYLEABLES;
	}

	/**
	* The calendar system used for parsing, displaying, and choosing
	* dates in the DatePicker control.
	*
	* <p>The default value is returned from a call to
	* {@code Chronology.ofLocale(Locale.getDefault(Locale.Category.FORMAT))}.
	* The default is usually {@link java.time.chrono.IsoChronology} unless
	* provided explicitly in the {@link java.util.Locale} by use of a
	* Locale calendar extension.
	*
	* Setting the value to <code>null</code> will restore the default
	* chronology.
	*/
	public final ObjectProperty<Chronology> chronologyProperty() {
		return chronology;
	}

	private ObjectProperty<Chronology> chronology = new SimpleObjectProperty<Chronology>(this, "chronology", null);

	public final Chronology getChronology() {
		Chronology chrono = chronology.get();
		if (chrono == null) {
			try {
				chrono = Chronology.ofLocale(Locale.getDefault(Locale.Category.FORMAT));
			} catch (Exception ex) {
				System.err.println(ex);
			}
			if (chrono == null) {
				chrono = IsoChronology.INSTANCE;
			}
			//System.err.println(chrono);
		}
		return chrono;
	}

	public final void setChronology(Chronology value) {
		chronology.setValue(value);
	}

	public final ObjectProperty<StringConverter<LocalDateTime>> converterProperty() {
		return converter;
	}

	private ObjectProperty<StringConverter<LocalDateTime>> converter = new SimpleObjectProperty<StringConverter<LocalDateTime>>(this,
			"converter", null);

	public final void setConverter(StringConverter<LocalDateTime> value) {
		converterProperty().set(value);
	}

	public final StringConverter<LocalDateTime> getConverter() {
		StringConverter<LocalDateTime> converter = converterProperty().get();
		if (converter != null) {
			return converter;
		} else {
			return defaultConverter;
		}
	}

	private static final DateTimeFormatter defaultFormateer = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss");
	// Create a symmetric (format/parse) converter with the default locale.
	private StringConverter<LocalDateTime> defaultConverter = new LocalDateTimeStringConverter(defaultFormateer, defaultFormateer);

	/**
	 * Whether the DatePicker popup should display a column showing
	 * week numbers.
	 *
	 * <p>The default value is specified in a resource bundle, and
	 * depends on the country of the current locale.
	 */
	public final BooleanProperty showWeekNumbersProperty() {
		if (showWeekNumbers == null) {
			String country = Locale.getDefault(Locale.Category.FORMAT).getCountry();
			boolean localizedDefault = (!country.isEmpty()
					&& ControlResources.getNonTranslatableString("DatePicker.showWeekNumbers").contains(country));
			showWeekNumbers = new StyleableBooleanProperty(localizedDefault) {
				@Override
				public CssMetaData<JFXDateTimePicker, Boolean> getCssMetaData() {
					return StyleableProperties.SHOW_WEEK_NUMBERS;
				}

				@Override
				public Object getBean() {
					return JFXDateTimePicker.this;
				}

				@Override
				public String getName() {
					return "showWeekNumbers";
				}
			};
		}
		return showWeekNumbers;
	}

	private BooleanProperty showWeekNumbers;

	public final void setShowWeekNumbers(boolean value) {
		showWeekNumbersProperty().setValue(value);
	}

	public final boolean isShowWeekNumbers() {
		return showWeekNumbersProperty().getValue();
	}

	/**
	 * A custom cell factory can be provided to customize individual
	 * day cells in the DatePicker popup. Refer to {@link DateCell}
	 * and {@link Cell} for more information on cell factories.
	 * Example:
	 *
	 * <pre><code>
	 * final Callback&lt;DatePicker, DateCell&gt; dayCellFactory = new Callback&lt;DatePicker, DateCell&gt;() {
	 *     public DateCell call(final DatePicker datePicker) {
	 *         return new DateCell() {
	 *             &#064;Override public void updateItem(LocalDate item, boolean empty) {
	 *                 super.updateItem(item, empty);
	 *
	 *                 if (MonthDay.from(item).equals(MonthDay.of(9, 25))) {
	 *                     setTooltip(new Tooltip("Happy Birthday!"));
	 *                     setStyle("-fx-background-color: #ff4444;");
	 *                 }
	 *                 if (item.equals(LocalDate.now().plusDays(1))) {
	 *                     // Tomorrow is too soon.
	 *                     setDisable(true);
	 *                 }
	 *             }
	 *         };
	 *     }
	 * };
	 * datePicker.setDayCellFactory(dayCellFactory);
	 * </code></pre>
	 *
	 * @defaultValue null
	 */
	private ObjectProperty<Callback<JFXDateTimePicker, JFXDateTimeCell>> dayCellFactory;

	public final void setDayCellFactory(Callback<JFXDateTimePicker, JFXDateTimeCell> value) {
		dayCellFactoryProperty().set(value);
	}

	public final Callback<JFXDateTimePicker, JFXDateTimeCell> getDayCellFactory() {
		return (dayCellFactory != null) ? dayCellFactory.get() : null;
	}

	public final ObjectProperty<Callback<JFXDateTimePicker, JFXDateTimeCell>> dayCellFactoryProperty() {
		if (dayCellFactory == null) {
			dayCellFactory = new SimpleObjectProperty<Callback<JFXDateTimePicker, JFXDateTimeCell>>(this, "dayCellFactory");
		}
		return dayCellFactory;
	}

}
