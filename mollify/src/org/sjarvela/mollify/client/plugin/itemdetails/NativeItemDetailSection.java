/**
 * Copyright (c) 2008- Samuli Järvelä
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 */

package org.sjarvela.mollify.client.plugin.itemdetails;

import org.sjarvela.mollify.client.ui.fileitemcontext.ItemDetailsSection;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.ui.Widget;

public class NativeItemDetailSection implements ItemDetailsSection {
	private final String title;
	@SuppressWarnings("unused")
	private final JavaScriptObject cb;

	public NativeItemDetailSection(String title, JavaScriptObject callback) {
		this.title = title;
		this.cb = callback;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void onOpen(Widget content) {
		invokeNativeHandler(content.getElement().getId());
	}

	private final native JavaScriptObject invokeNativeHandler(String elementId) /*-{
		var cb = this.@org.sjarvela.mollify.client.plugin.itemdetails.NativeItemDetailSection::cb;
		cb(elementId);
	}-*/;
}
