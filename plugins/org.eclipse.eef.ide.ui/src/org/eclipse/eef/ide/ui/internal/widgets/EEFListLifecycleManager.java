/*******************************************************************************
 * Copyright (c) 2016, 2026 Obeo.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Obeo - initial API and implementation
 *******************************************************************************/
package org.eclipse.eef.ide.ui.internal.widgets;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.eef.EEFListDescription;
import org.eclipse.eef.EEFWidgetAction;
import org.eclipse.eef.EEFWidgetDescription;
import org.eclipse.eef.common.ui.api.EEFWidgetFactory;
import org.eclipse.eef.common.ui.api.IEEFFormContainer;
import org.eclipse.eef.common.ui.api.SWTUtils;
import org.eclipse.eef.core.api.EditingContextAdapter;
import org.eclipse.eef.core.api.controllers.EEFControllersFactory;
import org.eclipse.eef.core.api.controllers.IEEFListController;
import org.eclipse.eef.core.api.controllers.IEEFWidgetController;
import org.eclipse.eef.ide.ui.api.widgets.AbstractEEFWidgetLifecycleManager;
import org.eclipse.eef.ide.ui.api.widgets.EEFTableSelectionListener;
import org.eclipse.eef.ide.ui.internal.EEFIdeUiPlugin;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.sirius.common.interpreter.api.IInterpreter;
import org.eclipse.sirius.common.interpreter.api.IVariableManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;

/**
 * This class will be used in order to manager the lifecycle of a list widget.
 *
 * @author sbegaudeau
 */
public class EEFListLifecycleManager extends AbstractEEFWidgetLifecycleManager {

	/**
	 * Default height.
	 */
	private static final int DEFAULT_HEIGHT = 34;

	/**
	 * Minimal height of the table widget. 5 Action buttons and almost 7 lines.
	 */
	private static final int TABLE_MINIMAL_HEIGHT = 140; // Consistent with EEFExtMultipleReferenceLifecycleManager

	/**
	 * The description.
	 */
	private EEFListDescription description;

	/**
	 * The widget factory.
	 */
	private EEFWidgetFactory widgetFactory;

	/**
	 * The action buttons.
	 */
	private List<ActionButton> actionButtons = new ArrayList<ActionButton>();

	/**
	 * The default background color of the text field.
	 */
	private Color defaultBackgroundColor;

	/**
	 * The controller.
	 */
	private IEEFListController controller;

	/**
	 * The table viewer used to display the list.
	 */
	private TableViewer tableViewer;

	/**
	 * The listener used to run the onClick expression when the user will click on the table.
	 */
	private EEFTableSelectionListener tableSelectionListener;

	/**
	 * The constructor.
	 *
	 * @param description
	 *            The description
	 * @param variableManager
	 *            The variable manager
	 * @param interpreter
	 *            The interpreter
	 * @param editingContextAdapter
	 *            The editing context adapter
	 */
	public EEFListLifecycleManager(EEFListDescription description, IVariableManager variableManager, IInterpreter interpreter,
			EditingContextAdapter editingContextAdapter) {
		super(variableManager, interpreter, editingContextAdapter);
		this.description = description;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.eef.ide.ui.api.widgets.AbstractEEFWidgetLifecycleManager#createMainControl(org.eclipse.swt.widgets.Composite,
	 *      org.eclipse.eef.common.ui.api.IEEFFormContainer)
	 */
	@Override
	protected void createMainControl(Composite parent, IEEFFormContainer formContainer) {
		widgetFactory = formContainer.getWidgetFactory();
		defaultBackgroundColor = parent.getBackground();

		// this is the parent composite
		Composite list = widgetFactory.createFlatFormComposite(parent);
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;

		list.setLayout(layout);

		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		list.setLayoutData(gridData);

		this.createListWidget(list);
		this.createWidgetActionButtons(list);

		widgetFactory.paintBordersFor(parent);

		this.controller = new EEFControllersFactory().createListController(this.description, this.variableManager, this.interpreter,
				this.editingContextAdapter);
	}

	/**
	 * Create table widget.
	 *
	 * @param parent
	 *            The parent composite
	 */
	private void createListWidget(Composite parent) {
		ScrolledComposite scrolledComposite = widgetFactory.createScrolledComposite(parent, SWT.NONE);
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.horizontalIndent = VALIDATION_MARKER_OFFSET;
		scrolledComposite.setLayoutData(gridData);

		// CHECKSTYLE:OFF
		int style = SWT.READ_ONLY | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER | SWT.MULTI | SWT.VIRTUAL;
		// CHECKSTYLE:ON

		Table table = widgetFactory.createTable(scrolledComposite, style);
		this.tableViewer = new TableViewer(table);

		GridData tableGridData = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		tableGridData.horizontalIndent = VALIDATION_MARKER_OFFSET;
		this.tableViewer.getTable().setLayoutData(tableGridData);

		this.tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		this.tableViewer.setLabelProvider(new EEFListTableLabelProvider(this.description, this.interpreter, this.variableManager));

		scrolledComposite.setContent(table);

		int widgetHeight = DEFAULT_HEIGHT;
		List<EEFWidgetAction> actions = description.getActions();
		if (actions != null && actions.size() > 0) {
			widgetHeight = widgetHeight * (actions.size() + 1);
		}

		final int clientWidth = scrolledComposite.getClientArea().width;
		this.tableViewer.getTable().setSize(clientWidth, Math.max(TABLE_MINIMAL_HEIGHT, widgetHeight));
		tableViewer.getTable().setBackground(defaultBackgroundColor);

		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setAlwaysShowScrollBars(true);
	}

	/**
	 * Create widget action buttons.
	 *
	 * @param parent
	 *            The parent composite
	 */
	private void createWidgetActionButtons(Composite parent) {
		Composite buttons = widgetFactory.createFlatFormComposite(parent);

		GridData gridData = new GridData();
		gridData.verticalAlignment = SWT.BEGINNING;
		buttons.setLayoutData(gridData);

		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;

		buttons.setLayout(layout);

		// Buttons are visible only if an action is defined
		for (EEFWidgetAction action : this.description.getActions()) {
			ActionButton actionButton = new ActionButton(action, buttons, widgetFactory, this.interpreter, this.variableManager);
			actionButtons.add(actionButton);
		}

	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.eef.ide.ui.api.widgets.AbstractEEFWidgetLifecycleManager#aboutToBeShown()
	 */
	@Override
	public void aboutToBeShown() {
		super.aboutToBeShown();

		this.tableSelectionListener = new EEFTableSelectionListener(this.controller);
		this.tableViewer.getTable().addSelectionListener(tableSelectionListener);

		this.controller.onNewValue((value) -> {
			if (value == null) {
				return;
			}
			this.setListValue(value);
		});

		this.actionButtons.forEach(actionButton -> {
			SelectionListener selectionListener = SWTUtils.widgetSelectedAdapter((event) -> {
				if (!this.container.isRenderingInProgress()) {
					List<Object> selections = new ArrayList<Object>();
					IStructuredSelection structuredSelection = (IStructuredSelection) tableViewer.getSelection();
					for (Object selection : structuredSelection.toList()) {
						selections.add(selection);
					}
					IStatus result = controller.action(actionButton.getAction(), selections);
					if (result != null && result.getSeverity() == IStatus.ERROR) {
						EEFIdeUiPlugin.INSTANCE.log(result);
					} else {
						refresh();
					}
				}
			});

			actionButton.addSelectionListener(selectionListener);
		});
	}

	/**
	 * Set the value of the list.
	 *
	 * @param value
	 *            Value to select
	 */
	private void setListValue(Object value) {
		if (!this.tableViewer.getTable().isDisposed()) {
			final ISelection selection = this.tableViewer.getSelection();

			List<Object> values = new ArrayList<Object>();
			if (value instanceof Iterable<?>) {
				for (Object val : (Iterable<?>) value) {
					values.add(val);
				}
			} else {
				values.add(value);
			}
			tableViewer.setInput(values.toArray());
			tableViewer.setSelection(selection);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.eef.ide.ui.api.widgets.AbstractEEFWidgetLifecycleManager#setEnabled(boolean)
	 */
	@Override
	protected void setEnabled(boolean isEnabled) {
		if (this.tableViewer != null && this.tableViewer.getTable() != null && !this.tableViewer.getTable().isDisposed()) {
			this.tableViewer.getTable().setBackground(this.getBackgroundColor(isEnabled));
			// tableViewer is not disabled so user can scroll.
			controller.setEnabled(isEnabled);
		}
		this.actionButtons.stream().filter(actionButton -> !actionButton.getButton().isDisposed())
				.forEach(actionButton -> actionButton.setEnabled(isEnabled));
	}

	/**
	 * Get the background color according to the current valid style.
	 *
	 * @param isEnabled
	 *            <code>true</code> if the widget is enabled, <code>false</code> otherwise
	 *
	 * @return The background color to use in the text field.
	 */
	private Color getBackgroundColor(boolean isEnabled) {
		Color color = defaultBackgroundColor;
		if (!isEnabled) {
			color = widgetFactory.getColors().getInactiveBackground();
		}
		return color;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.eef.ide.ui.api.widgets.AbstractEEFWidgetLifecycleManager#aboutToBeHidden()
	 */
	@Override
	public void aboutToBeHidden() {
		super.aboutToBeHidden();

		this.actionButtons.forEach(ActionButton::removeSelectionListener);

		if (this.tableViewer != null && this.tableViewer.getTable() != null && !this.tableViewer.getTable().isDisposed()) {
			this.tableViewer.getTable().removeSelectionListener(this.tableSelectionListener);
		}
		this.controller.removeNewValueConsumer();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.eef.ide.ui.api.widgets.AbstractEEFWidgetLifecycleManager#getController()
	 */
	@Override
	protected IEEFWidgetController getController() {
		return this.controller;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.eef.ide.ui.api.widgets.AbstractEEFWidgetLifecycleManager#getWidgetDescription()
	 */
	@Override
	protected EEFWidgetDescription getWidgetDescription() {
		return this.description;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.eef.ide.ui.api.widgets.AbstractEEFLifecycleManager#getValidationControl()
	 */
	@Override
	protected Control getValidationControl() {
		return this.tableViewer.getTable().getParent();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.eef.ide.ui.api.widgets.AbstractEEFWidgetLifecycleManager#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
		this.actionButtons.clear();
	}
}
