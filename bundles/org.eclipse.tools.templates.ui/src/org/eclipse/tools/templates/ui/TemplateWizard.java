/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tools.templates.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.tools.templates.core.IGenerator;
import org.eclipse.tools.templates.ui.internal.Activator;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

public abstract class TemplateWizard extends BasicNewResourceWizard {

	protected abstract IGenerator getGenerator();
	
	protected void populateModel(Map<String, Object> model) {
		// nothing by default
	}
	
	@Override
	public boolean performFinish() {
		IGenerator generator = getGenerator();
		Map<String, Object> model = new HashMap<>();
		populateModel(model);

		try {
			getContainer().run(true, true, new WorkspaceModifyOperation() {
				@Override
				protected void execute(IProgressMonitor monitor)
						throws CoreException, InvocationTargetException, InterruptedException {
					monitor.beginTask("Generating project", 1); //$NON-NLS-1$
					generator.generate(model, monitor);
					monitor.done();
					getWorkbench().getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							try {
								IWorkbenchPage activePage = getWorkbench().getActiveWorkbenchWindow().getActivePage();
								for (IFile file : generator.getFilesToOpen()) {
									IDE.openEditor(activePage, file);
								}
							} catch (PartInitException e) {
								Activator.log(e);
							}
						}
					});
				}

				@Override
				public ISchedulingRule getRule() {
					return ResourcesPlugin.getWorkspace().getRoot();
				}
			});
		} catch (InterruptedException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		return true;
	}

}
