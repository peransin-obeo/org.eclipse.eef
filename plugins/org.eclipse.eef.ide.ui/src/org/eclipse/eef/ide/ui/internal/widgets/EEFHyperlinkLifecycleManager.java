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
import org.eclipse.eef.EEFHyperlinkDescription;
import org.eclipse.eef.EEFHyperlinkStyle;
import org.eclipse.eef.EEFWidgetAction;
import org.eclipse.eef.EEFWidgetDescription;
import org.eclipse.eef.EEFWidgetStyle;
import org.eclipse.eef.common.ui.api.EEFWidgetFactory;
import org.eclipse.eef.common.ui.api.IEEFFormContainer;
import org.eclipse.eef.common.ui.api.SWTUtils;
import org.eclipse.eef.core.api.EditingContextAdapter;
import org.eclipse.eef.core.api.controllers.EEFControllersFactory;
import org.eclipse.eef.core.api.controllers.IEEFHyperlinkController;
import org.eclipse.eef.core.api.controllers.IEEFWidgetController;
import org.eclipse.eef.ide.ui.api.widgets.AbstractEEFWidgetLifecycleManager;
import org.eclipse.eef.ide.ui.api.widgets.EEFHyperlinkListener;
import org.eclipse.eef.ide.ui.api.widgets.EEFStyleHelper;
import org.eclipse.eef.ide.ui.api.widgets.EEFStyleHelper.IEEFTextStyleCallback;
import org.eclipse.eef.ide.ui.internal.EEFIdeUiPlugin;
import org.eclipse.sirius.common.interpreter.api.IInterpreter;
import org.eclipse.sirius.common.interpreter.api.IVariableManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * This class will be used in order to manager the lifecycle of an hyperlink.
 *
 * @author mbats
 */
public class EEFHyperlinkLifecycleManager extends AbstractEEFWidgetLifecycleManager {

	/**
	 * The description.
	 */
	private EEFHyperlinkDescription description;

	/**
	 * The hyperlink.
	 */
	private StyledText hyperlink;

	/**
	 * The widget factory.
	 */
	private EEFWidgetFactory widgetFactory;

	/**
	 * The action buttons.
	 */
	private List<ActionButton> actionButtons = new ArrayList<ActionButton>();

	/**
	 * The controller.
	 */
	private IEEFHyperlinkController controller;

	/**
	 * The listener on the hyperlink.
	 */
	private MouseListener hyperlinkListener;

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
	public EEFHyperlinkLifecycleManager(EEFHyperlinkDescription description, IVariableManager variableManager, IInterpreter interpreter,
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
		Composite hyperlinkComposite = this.widgetFactory.createFlatFormComposite(parent);
		GridLayout layout = new GridLayout(2, false);
		// Align buttons end with other widgets;
		// In loop line, avoid awkward spaces.
		layout.marginWidth = 0;
		// Avoid empty horizontal line.
		layout.marginHeight = 0;
		hyperlinkComposite.setLayout(layout);

		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		hyperlinkComposite.setLayoutData(gridData);

		this.createHyperlink(hyperlinkComposite);
		this.createWidgetActionButtons(hyperlinkComposite);

		this.controller = new EEFControllersFactory().createHyperlinkController(this.description, this.variableManager, this.interpreter,
				this.editingContextAdapter);
	}

	/**
	 * Create the hyperlink widget.
	 *
	 * @param parent
	 *            The parent composite
	 */
	private void createHyperlink(Composite parent) {
		this.hyperlink = widgetFactory.createStyledText(parent, SWT.READ_ONLY);
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gridData.horizontalIndent = VALIDATION_MARKER_OFFSET;
		this.hyperlink.setLayoutData(gridData);
		this.hyperlink.setEditable(false);
	}

	/**
	 * Creates widget action buttons.
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
			// hyperlinkComposite already provide vertical and horizontal spacing.
			layout.marginHeight = 0;
			layout.marginWidth = 0;

			buttons.setLayout(layout);

			// Buttons are visible only if an action is defined
			for (EEFWidgetAction action : this.description.getActions()) {
				ActionButton actionButton = new ActionButton(action, buttons, this.widgetFactory, this.interpreter, this.variableManager);
				actionButtons.add(actionButton);
			}
		} // else (no action), avoid extra space

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
	 * @see org.eclipse.eef.ide.ui.api.widgets.AbstractEEFWidgetLifecycleManager#getController()
	 */
	@Override
	protected IEEFWidgetController getController() {
		return this.controller;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.eef.ide.ui.internal.widgets.AbstractEEFWidgetLifecycleManager#getWidgetDescription()
	 */
	@Override
	protected EEFWidgetDescription getWidgetDescription() {
		return this.description;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.eef.ide.ui.api.widgets.AbstractEEFWidgetLifecycleManager#aboutToBeShown()
	 */
	@Override
	public void aboutToBeShown() {
		super.aboutToBeShown();

		this.hyperlinkListener = new EEFHyperlinkListener(this, this.hyperlink, this.container, this.controller);
		hyperlink.addMouseListener(hyperlinkListener);

		this.controller.onNewValue((value) -> {
			if (!hyperlink.isDisposed()) {
				if (!(hyperlink.getText() != null && hyperlink.getText().equals(value))) {
					String text = controller.computeDisplayValue(value);
					hyperlink.setText(text);
					hyperlink.setData(value);
				}
				this.setStyle();
				if (!hyperlink.isEnabled()) {
					hyperlink.setEnabled(true);
				}
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
		EEFWidgetStyle widgetStyle = styleHelper.getWidgetStyle(this.description);
		if (widgetStyle instanceof EEFHyperlinkStyle) {
			EEFHyperlinkStyle style = (EEFHyperlinkStyle) widgetStyle;

			IEEFTextStyleCallback callback = new EEFStyledTextStyleCallback(this.hyperlink);
			styleHelper.applyTextStyle(style.getFontNameExpression(), style.getFontSizeExpression(), style.getFontStyleExpression(),
					this.hyperlink.getFont(), style.getBackgroundColorExpression(), null, callback);
		}

		// Sets the default hyperlink style properties
		StyleRange[] styleRanges = hyperlink.getStyleRanges();
		StyleRange styleRange;
		if (styleRanges.length > 0) {
			styleRange = styleRanges[0];
		} else {
			styleRange = new StyleRange();
		}

		if (styleRange != null) {
			styleRange.start = 0;
			styleRange.length = hyperlink.getText().length();
			styleRange.underline = true;
			styleRange.underlineStyle = SWT.UNDERLINE_LINK;
		}
		hyperlink.setStyleRange(styleRange);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.eef.ide.ui.api.widgets.AbstractEEFWidgetLifecycleManager#getValidationControl()
	 */
	@Override
	protected Control getValidationControl() {
		return this.hyperlink;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.eef.ide.ui.api.widgets.AbstractEEFWidgetLifecycleManager#aboutToBeHidden()
	 */
	@Override
	public void aboutToBeHidden() {
		super.aboutToBeHidden();

		if (!hyperlink.isDisposed()) {
			this.hyperlink.removeMouseListener(this.hyperlinkListener);
		}

		this.actionButtons.forEach(ActionButton::removeSelectionListener);

		this.controller.removeNewValueConsumer();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.eef.ide.ui.api.widgets.AbstractEEFWidgetLifecycleManager#setEnabled(boolean)
	 */
	@Override
	protected void setEnabled(boolean isEnabled) {
		if (!this.hyperlink.isDisposed()) {
			this.hyperlink.setEnabled(isEnabled);
		}
		this.actionButtons.stream().filter(actionButton -> !actionButton.getButton().isDisposed())
				.forEach(actionButton -> actionButton.setEnabled(isEnabled));
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
