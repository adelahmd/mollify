/**
 * Copyright (c) 2008- Samuli J�rvel�
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 */

package org.sjarvela.mollify.client.service;

import com.google.gwt.core.client.JsArray;

public interface ResultListener {
	@SuppressWarnings("unchecked")
	public void onSuccess(JsArray result);

	public void onError(ServiceError error);
}
