/*******************************************************************************
 * Copyright (c) 2016, 2018 Obeo.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Obeo - initial API and implementation
 *******************************************************************************/
package org.eclipse.eef.core.api.controllers;

import java.util.List;
import java.util.function.Consumer;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.eef.EEFWidgetAction;

/**
 * The IEEFListController is responsible of supporting all the interactions with the widgets created for a list.
 *
 * @author mbats
 */
public interface IEEFListController extends IEEFOnClickController {

	/**
	 * Sets the enablement of action on selection.
	 *
	 * @param isEnabled
	 *            <code>true</code> when the widget should have its default behavior, <code>false</code> when the widget
	 *            should be in a read only mode.
	 */
	void setEnabled(boolean isEnabled);

	/**
	 * Register a consumer which will be called with the new value of the text when it will change.
	 *
	 * @param consumer
	 *            The consumer of the new value of the text
	 */
	void onNewValue(Consumer<Object> consumer);

	/**
	 * Remove the consumer of the new value of the text.
	 */
	void removeNewValueConsumer();

	/**
	 * Invoked when the user clicks on an action button.
	 *
	 * @param action
	 *            Widget action
	 * @param selection
	 *            The selected elements
	 * @return the status of the action execution
	 */
	IStatus action(EEFWidgetAction action, List<Object> selection);

}
