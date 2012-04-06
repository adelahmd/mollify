/**
 * Copyright (c) 2008- Samuli Järvelä
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 */

package org.sjarvela.mollify.client.ui.mainview.impl;

import org.sjarvela.mollify.client.localization.TextProvider;
import org.sjarvela.mollify.client.plugin.ClientInterface;
import org.sjarvela.mollify.client.service.FileSystemService;
import org.sjarvela.mollify.client.session.ClientSettings;
import org.sjarvela.mollify.client.ui.mainview.MainView.ViewType;

public class DefaultFileListWidgetFactory implements FileListWidgetFactory {

	private final TextProvider textProvider;
	// private final DragAndDropManager dragAndDropManager;
	private final boolean thumbnails;
	private final FileSystemService service;
	private final boolean experimental;
	private final ClientSettings settings;
	private final ClientInterface pluginEnvironment;

	public DefaultFileListWidgetFactory(TextProvider textProvider,
			ClientSettings settings, FileSystemService service,
			ClientInterface pluginEnvironment) {
		this.textProvider = textProvider;
		// this.dragAndDropManager = dragAndDropManager;
		this.settings = settings;
		this.service = service;
		this.pluginEnvironment = pluginEnvironment;
		this.experimental = settings.getBool("experimental-list", false);
		this.thumbnails = settings.getBool("icon-view-thumbnails", false);
	}

	@Override
	public FileWidget create(ViewType type) {
		if (ViewType.list.equals(type)) {
			if (experimental)
				return new CellTableFileList(textProvider);
			return new FileListWithExternalColumns(textProvider,
					pluginEnvironment, settings.getJsObj("list-view-columns"));
		}
		return new FileGridWidget(thumbnails, service,
				ViewType.gridSmall.equals(type));
	}

}
