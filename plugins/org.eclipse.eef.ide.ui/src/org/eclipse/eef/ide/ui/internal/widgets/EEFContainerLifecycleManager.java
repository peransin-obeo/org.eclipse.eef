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

import org.eclipse.eef.EEFContainerDescription;
import org.eclipse.eef.EEFControlDescription;
import org.eclipse.eef.EEFFillLayoutDescription;
import org.eclipse.eef.EEFGridLayoutDescription;
import org.eclipse.eef.EEFLayoutDescription;
import org.eclipse.eef.EEF_FILL_LAYOUT_ORIENTATION;
import org.eclipse.eef.common.ui.api.EEFWidgetFactory;
import org.eclipse.eef.common.ui.api.IEEFFormContainer;
import org.eclipse.eef.core.api.EditingContextAdapter;
import org.eclipse.eef.ide.ui.api.widgets.IEEFLifecycleManager;
import org.eclipse.sirius.common.interpreter.api.IInterpreter;
import org.eclipse.sirius.common.interpreter.api.IVariableManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * This class will handle the lifecycle of the {@link EEFContainerDescription}.
 *
 * @author sbegaudeau
 */
public class EEFContainerLifecycleManager implements IEEFLifecycleManager {

	/**
	 * The description of the container.
	 */
	private EEFContainerDescription description;

	/**
	 * The interpreter.
	 */
	private IInterpreter interpreter;

	/**
	 * The variable manager.
	 */
	private IVariableManager variableManager;

	/**
	 * The editing context adapter.
	 */
	private EditingContextAdapter contextAdapter;

	/**
	 * The lifecycle managers of the child of the container.
	 */
	private List<IEEFLifecycleManager> lifecycleManagers = new ArrayList<IEEFLifecycleManager>();

	/**
	 * The constructor.
	 *
	 * @param description
	 *            The description of the container
	 * @param variableManager
	 *            The variable manager
	 * @param interpreter
	 *            The interpreter
	 * @param editingContextAdapter
	 *            The editing context adapter
	 */
	public EEFContainerLifecycleManager(EEFContainerDescription description, IVariableManager variableManager, IInterpreter interpreter,
			EditingContextAdapter editingContextAdapter) {
		this.description = description;
		this.variableManager = variableManager;
		this.interpreter = interpreter;
		this.contextAdapter = editingContextAdapter;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.eef.ide.ui.api.widgets.IEEFLifecycleManager#createControl(org.eclipse.swt.widgets.Composite,
	 *      org.eclipse.eef.common.ui.api.IEEFFormContainer)
	 */
	@Override
	public void createControl(Composite parent, IEEFFormContainer formContainer) {
		EEFWidgetFactory widgetFactory = formContainer.getWidgetFactory();

		Composite composite = null;
		GridData gridData = null;

		if (isBorderedContainer()) {
			String borderLabel = getBorderLabel();
			composite = widgetFactory.createGroup(parent, borderLabel);
			gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		} else {
			composite = widgetFactory.createComposite(parent);
			gridData = new GridData(GridData.FILL_HORIZONTAL);
		}

		// Because the parent layout has 3 columns, span this composite over 3 columns.
		gridData.horizontalSpan = 3;
		composite.setLayoutData(gridData);

		int numColumns = 1;
		boolean makeColumnsEqualWidth = true;

		EEFLayoutDescription layout = this.description.getLayout();
		if (layout instanceof EEFFillLayoutDescription) {
			// The vertical layout is the default one, we thus only handle the horizontal one
			EEFFillLayoutDescription fillLayoutDescription = (EEFFillLayoutDescription) layout;
			if (fillLayoutDescription.getOrientation() == EEF_FILL_LAYOUT_ORIENTATION.HORIZONTAL) {
				numColumns = this.description.getControls().size();
			}
		} else if (layout instanceof EEFGridLayoutDescription) {
			EEFGridLayoutDescription gridLayoutDescription = (EEFGridLayoutDescription) layout;
			numColumns = gridLayoutDescription.getNumberOfColumns();
			makeColumnsEqualWidth = gridLayoutDescription.isMakeColumnsWithEqualWidth();
		}

		GridLayout compositeLayout = new GridLayout(numColumns, makeColumnsEqualWidth);
		compositeLayout.marginWidth = 0;
		compositeLayout.marginHeight = 0;
		compositeLayout.horizontalSpacing = 5 // Default margin
				* 2 // section border + widget border
				* 2; // left + right

		composite.setLayout(compositeLayout);

		List<EEFControlDescription> controls = this.description.getControls();
		EEFControlSwitch eefControlSwitch = new EEFControlSwitch(this.interpreter, this.contextAdapter);
		// Create an invisible composite for each column
		for (int columnIndex = 0; columnIndex < numColumns; columnIndex++) {
			Composite column = widgetFactory.createComposite(composite);
			GridData columnLayoutData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
			column.setLayoutData(columnLayoutData);

			// Three columns: label, help, widget
			GridLayout columnLayout = new GridLayout(3, false);
			columnLayout.marginWidth = 0;
			columnLayout.marginHeight = 0;
			// Text fields and areas need a special pixel.
			// Their border is drawn out of bounds.
			columnLayout.marginRight = 1;

			column.setLayout(columnLayout);

			// Pick the right controls for the given column index in the controls flat list
			for (int controlIndex = columnIndex; controlIndex < controls.size(); controlIndex += numColumns) {
				EEFControlDescription eefControlDescription = controls.get(controlIndex);
				this.lifecycleManagers.addAll(eefControlSwitch.doCreate(column, formContainer, eefControlDescription, this.variableManager));
			}
		}

	}

	/**
	 * Get label of the border if a border is drawn for the container.
	 *
	 * @return the label of the border if a border is drawn for the container.
	 */
	protected String getBorderLabel() {
		return ""; //$NON-NLS-1$
	}

	/**
	 * Check if a border around the container should be displayed.
	 *
	 * @return <code>true</code> if the container should have a border, <code>false</code> otherwise.
	 */
	protected boolean isBorderedContainer() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.eef.ide.ui.api.widgets.IEEFLifecycleManager#aboutToBeShown()
	 */
	@Override
	public void aboutToBeShown() {
		this.lifecycleManagers.forEach(IEEFLifecycleManager::aboutToBeShown);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.eef.ide.ui.api.widgets.IEEFLifecycleManager#refresh()
	 */
	@Override
	public void refresh() {
		this.lifecycleManagers.forEach(IEEFLifecycleManager::refresh);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.eef.ide.ui.api.widgets.IEEFLifecycleManager#aboutToBeHidden()
	 */
	@Override
	public void aboutToBeHidden() {
		this.lifecycleManagers.forEach(IEEFLifecycleManager::aboutToBeHidden);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.eef.ide.ui.api.widgets.IEEFLifecycleManager#dispose()
	 */
	@Override
	public void dispose() {
		this.lifecycleManagers.forEach(IEEFLifecycleManager::dispose);
	}

}
