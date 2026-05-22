/*******************************************************************************
 * Copyright (c) 2015, 2026 Obeo.
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
import java.util.Optional;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.eef.EEFLabelDescription;
import org.eclipse.eef.EEFLabelStyle;
import org.eclipse.eef.EEFWidgetAction;
import org.eclipse.eef.EEFWidgetDescription;
import org.eclipse.eef.EEFWidgetStyle;
import org.eclipse.eef.common.ui.api.EEFWidgetFactory;
import org.eclipse.eef.common.ui.api.IEEFFormContainer;
import org.eclipse.eef.common.ui.api.SWTUtils;
import org.eclipse.eef.core.api.EditingContextAdapter;
import org.eclipse.eef.core.api.controllers.EEFControllersFactory;
import org.eclipse.eef.core.api.controllers.IEEFLabelController;
import org.eclipse.eef.core.api.controllers.IEEFWidgetController;
import org.eclipse.eef.ide.ui.api.widgets.AbstractEEFWidgetLifecycleManager;
import org.eclipse.eef.ide.ui.api.widgets.EEFStyleHelper;
import org.eclipse.eef.ide.ui.api.widgets.EEFStyleHelper.IEEFTextStyleCallback;
import org.eclipse.eef.ide.ui.internal.EEFIdeUiPlugin;
import org.eclipse.sirius.common.interpreter.api.IInterpreter;
import org.eclipse.sirius.common.interpreter.api.IVariableManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * This class will be used in order to manage the lifecycle of a label.
 *
 * @author mbats
 */
public class EEFLabelLifecycleManager extends AbstractEEFWidgetLifecycleManager {

	/**
	 * This constant is used in order to tell SWT that the label should be 300px wide even if it is not useful. The
	 * layout data should work by themselves but it seems that there is a bug with SWT so, this useless information on
	 * the width of the label make it work. Don't ask me why :)
	 */
	private static final int LABEL_WIDTH_HINT = 300;

	/**
	 * The description.
	 */
	private EEFLabelDescription description;

	/**
	 * The body.
	 */
	private StyledText body;

	/**
	 * The action buttons.
	 */
	private List<ActionButton> actionButtons = new ArrayList<ActionButton>();

	/**
	 * The controller.
	 */
	private IEEFLabelController controller;

	/**
	 * The widget factory.
	 */
	private EEFWidgetFactory widgetFactory;

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
	public EEFLabelLifecycleManager(EEFLabelDescription description, IVariableManager variableManager, IInterpreter interpreter,
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
		this.widgetFactory = formContainer.getWidgetFactory();

		// this is the parent composite
		Composite labelComposite = this.widgetFactory.createFlatFormComposite(parent);
		GridLayout layout = new GridLayout(2, false);
		// Align buttons end with other widgets;
		// In loop line, avoid awkward spaces.
		layout.marginWidth = 0;
		// Avoid empty horizontal line.
		layout.marginHeight = 0;
		labelComposite.setLayout(layout);

		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		labelComposite.setLayoutData(gridData);

		this.createLabel(labelComposite);
		this.createWidgetActionButtons(labelComposite);

		this.controller = new EEFControllersFactory().createLabelController(this.description, this.variableManager, this.interpreter,
				this.editingContextAdapter);
	}

	/**
	 * Create the label widget.
	 *
	 * @param parent
	 *            The parent composite
	 */
	private void createLabel(Composite parent) {
		this.body = this.widgetFactory.createStyledText(parent, SWT.WRAP);
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);

		// TODO Add a linecount property to the label and use it to compute the height of the widget
		gridData.heightHint = this.body.getLineHeight();
		gridData.widthHint = LABEL_WIDTH_HINT;
		gridData.horizontalIndent = VALIDATION_MARKER_OFFSET;
		this.body.setLayoutData(gridData);
		this.body.setEditable(false);
	}

	/**
	 * Create widget action buttons.
	 *
	 * @param parent
	 *            The parent composite
	 */
	private void createWidgetActionButtons(Composite parent) {
		if (!description.getActions().isEmpty()) {
			Composite buttons = this.widgetFactory.createComposite(parent);

			GridData gridData = new GridData();
			gridData.grabExcessHorizontalSpace = false;
			buttons.setLayoutData(gridData);

			GridLayout layout = new GridLayout(this.description.getActions().size(), true);
			// labelComposite already provide vertical spacing.
			layout.marginHeight = 0;
			layout.marginWidth = 0;

			buttons.setLayout(layout);

			// Buttons are visible only if an action is defined
			for (EEFWidgetAction action : this.description.getActions()) {
				ActionButton actionButton = new ActionButton(action, buttons, this.widgetFactory, this.interpreter, this.variableManager);
				actionButtons.add(actionButton);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.eef.ide.ui.api.widgets.AbstractEEFWidgetLifecycleManager#getLabelVerticalAlignment()
	 */
	@Override
	protected int getLabelVerticalAlignment() {
		return GridData.VERTICAL_ALIGN_CENTER;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.eef.ide.ui.api.widgets.IEEFLifecycleManager#aboutToBeShown()
	 */
	@Override
	public void aboutToBeShown() {
		super.aboutToBeShown();

		this.controller.onNewValue((value) -> {
			if (!body.isDisposed()) {
				if (!(body.getText() != null && body.getText().equals(value))) {
					body.setText(Optional.ofNullable(value).orElse("")); //$NON-NLS-1$
				}
				this.setStyle();
			}
		});

		this.actionButtons.forEach(actionButton -> {
			SelectionListener selectionListener = SWTUtils.widgetSelectedAdapter((event) -> {
				if (!this.container.isRenderingInProgress()) {
					IStatus result = controller.action(actionButton.getAction());
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
	 * Set the style.
	 */
	private void setStyle() {
		EEFStyleHelper styleHelper = new EEFStyleHelper(this.interpreter, this.variableManager);
		EEFWidgetStyle style = styleHelper.getWidgetStyle(this.description);
		if (style instanceof EEFLabelStyle) {
			EEFLabelStyle labelStyle = (EEFLabelStyle) style;

			IEEFTextStyleCallback callback = new EEFStyledTextStyleCallback(this.body);
			styleHelper.applyTextStyle(labelStyle.getFontNameExpression(), labelStyle.getFontSizeExpression(), labelStyle.getFontStyleExpression(),
					this.body.getFont(), labelStyle.getBackgroundColorExpression(), labelStyle.getForegroundColorExpression(), callback);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.eef.ide.ui.api.widgets.AbstractEEFWidgetLifecycleManager#setEnabled(boolean)
	 */
	@Override
	protected void setEnabled(boolean isEnabled) {
		if (!this.body.isDisposed()) {
			this.body.setEnabled(isEnabled);
		}
		this.actionButtons.stream().filter(actionButton -> !actionButton.getButton().isDisposed())
				.forEach(actionButton -> actionButton.setEnabled(isEnabled));
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.eef.ide.ui.api.widgets.IEEFLifecycleManager#aboutToBeHidden()
	 */
	@Override
	public void aboutToBeHidden() {
		super.aboutToBeHidden();

		this.actionButtons.forEach(ActionButton::removeSelectionListener);

		this.controller.removeNewValueConsumer();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.eef.ide.ui.api.widgets.IEEFLifecycleManager#dispose()
	 */
	@Override
	protected IEEFWidgetController getController() {
		return this.controller;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.eef.ide.ui.api.widgets.AbstractEEFWidgetLifecycleManager#getController()
	 */
	@Override
	protected EEFWidgetDescription getWidgetDescription() {
		return this.description;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.eef.ide.ui.api.widgets.AbstractEEFWidgetLifecycleManager#getValidationControl()
	 */
	@Override
	protected Control getValidationControl() {
		return this.body;
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
