/********************************
 *	프로젝트 : JFoenix
 *	패키지   : com.jfoenix.controls.behavior
 *	작성일   : 2016. 11. 24.
 *	작성자   : KYJ
 *******************************/
package com.jfoenix.controls.behavior;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.WEEKS;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.LEFT;
import static javafx.scene.input.KeyCode.RIGHT;
import static javafx.scene.input.KeyCode.SPACE;
import static javafx.scene.input.KeyCode.UP;
import static javafx.scene.input.KeyEvent.KEY_RELEASED;

import java.util.ArrayList;
import java.util.List;

import com.jfoenix.controls.DateTimeCell;
import com.jfoenix.skins.JFXDateTimePickerContent;
import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.KeyBinding;
import com.sun.javafx.scene.control.skin.DatePickerContent;
import com.sun.javafx.scene.traversal.Direction;

/**
 * @author KYJ
 *
 */
/*
 * Copyright (c) 2013, 2014, Oracle and/or its affiliates. All rights reserved.
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

import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.control.DateCell;

/**
 * Behaviors for LocalDate based cells types. Simply defines methods
 * that subclasses implement so that CellSkinBase has API to call.
 *
 */
public class DateTimeCellBehavior extends BehaviorBase<DateTimeCell> {

	/**************************************************************************
	 *                          Setup KeyBindings                             *
	 *************************************************************************/
	protected static final List<KeyBinding> DATE_CELL_BINDINGS = new ArrayList<KeyBinding>();

	static {
		DATE_CELL_BINDINGS.add(new KeyBinding(UP, "TraverseUp"));
		DATE_CELL_BINDINGS.add(new KeyBinding(DOWN, "TraverseDown"));
		DATE_CELL_BINDINGS.add(new KeyBinding(LEFT, "TraverseLeft"));
		DATE_CELL_BINDINGS.add(new KeyBinding(RIGHT, "TraverseRight"));
		DATE_CELL_BINDINGS.add(new KeyBinding(ENTER, KEY_RELEASED, "SelectDate"));
		DATE_CELL_BINDINGS.add(new KeyBinding(SPACE, KEY_RELEASED, "SelectDate"));
	}

	public DateTimeCellBehavior(DateTimeCell dateCell) {
		super(dateCell, DATE_CELL_BINDINGS);
	}

	@Override
	public void callAction(String name) {
		DateTimeCell cell = getControl();
		JFXDateTimePickerContent dpc = findDatePickerContent(cell);

		if (dpc != null) {
			switch (name) {
			case "SelectDate":
				dpc.selectDayCell(cell);
				break;
			default:
				super.callAction(name);
			}
			return;
		}
		super.callAction(name);
	}

	@Override
	public void traverse(final Node node, final Direction dir) {
		boolean rtl = (node.getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT);

		switch (dir) {
		case UP:
		case DOWN:
		case LEFT:
		case RIGHT:
			if (node instanceof DateTimeCell) {
				JFXDateTimePickerContent dpc = findDatePickerContent(node);
				if (dpc != null) {
					DateTimeCell cell = (DateTimeCell) node;
					switch (dir) {
					case UP:
						dpc.goToDayCell(cell, -1, WEEKS, true);
						break;
					case DOWN:
						dpc.goToDayCell(cell, +1, WEEKS, true);
						break;
					case LEFT:
						dpc.goToDayCell(cell, rtl ? +1 : -1, DAYS, true);
						break;
					case RIGHT:
						dpc.goToDayCell(cell, rtl ? -1 : +1, DAYS, true);
						break;
					}
					return;
				}
			}
		}
		super.traverse(node, dir);
	}

	protected JFXDateTimePickerContent findDatePickerContent(Node node) {
		Node parent = node;
		while ((parent = parent.getParent()) != null && !(parent instanceof JFXDateTimePickerContent))
			;
		return (JFXDateTimePickerContent) parent;
	}
}
