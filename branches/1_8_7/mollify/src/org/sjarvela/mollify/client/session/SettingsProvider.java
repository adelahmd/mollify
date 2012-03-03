/**
 * Copyright (c) 2008- Samuli Järvelä
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 */

package org.sjarvela.mollify.client.session;

import java.util.HashMap;
import java.util.Map;

import org.sjarvela.mollify.client.js.JsObj;

public class SettingsProvider {
	private final String name;
	private final JsObj settings;
	private final Map<String, String> stringValues;

	public SettingsProvider(String name) {
		this.name = name;
		this.settings = getSettings();
		if (settings == null)
			throw new RuntimeException("Mollify not initialized");
		this.stringValues = getStringValues();
	}

	private Map<String, String> getStringValues() {
		Map<String, String> result = new HashMap();

		for (String k : settings.getKeys()) {
			if (k == null || k.trim().length() == 0)
				continue;

			String name = k.trim();
			String value = settings.getAsString(k);
			if (value.length() == 0)
				continue;

			result.put(name, value);
		}
		return result;
	}

	private native JsObj getSettings() /*-{
		if (!$wnd.mollify || !$wnd.mollify.getSettings)
			return null;
		return $wnd.mollify.getSettings();
	}-*/;

	public String getParameter(String name) {
		if (!stringValues.containsKey(name))
			return null;
		return stringValues.get(name);
	}

	public JsObj getParameterAsJsObj(String name) {
		if (!stringValues.containsKey(name))
			return null;
		return settings.getJsObj(name);
	}

	public boolean hasParameter(String param) {
		return stringValues.containsKey(param);
	}
}
