/********************************
 *	프로젝트 : JFoenix
 *	패키지   : com.jfoenix.controls
 *	작성일   : 2016. 11. 24.
 *	작성자   : KYJ
 *******************************/
package com.jfoenix.controls;

/**
 * @author KYJ
 *
 */
/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

import java.time.LocalDateTime;

import com.jfoenix.skins.DateTimeCellSkin;

import javafx.scene.control.Cell;
import javafx.scene.control.Skin;

/**
 * DateCell is used by {@link DatePicker} to render the individual
 * grid cells in the calendar month. By providing a
 * {@link DatePicker#dayCellFactoryProperty() dayCellFactory}, an
 * application can provide an update method to change each cell's
 * properties such as text, background color, etc.
 *
 * @since JavaFX 8.0
 */
/**
 * Custom . 2016-11-24 LocalDateTime
 * @author KYJ
 *
 */
public class JFXDateTimeCell extends Cell<LocalDateTime> {
	public JFXDateTimeCell() {
		getStyleClass().add(DEFAULT_STYLE_CLASS);
	}

	/** {@inheritDoc} */
	@Override
	public void updateItem(LocalDateTime item, boolean empty) {
		super.updateItem(item, empty);
	}

	/** {@inheritDoc} */
	@Override
	protected Skin<?> createDefaultSkin() {
		return new DateTimeCellSkin(this);
	}

	/***************************************************************************
	 *                                                                         *
	 * Stylesheet Handling                                                     *
	 *                                                                         *
	 **************************************************************************/

	private static final String DEFAULT_STYLE_CLASS = "date-cell";
}
