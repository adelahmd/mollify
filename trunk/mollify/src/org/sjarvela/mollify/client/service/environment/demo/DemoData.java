/**
 * Copyright (c) 2008- Samuli Järvelä
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 */

package org.sjarvela.mollify.client.service.environment.demo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sjarvela.mollify.client.filesystem.Directory;
import org.sjarvela.mollify.client.filesystem.DirectoryDetails;
import org.sjarvela.mollify.client.filesystem.File;
import org.sjarvela.mollify.client.filesystem.FileDetails;
import org.sjarvela.mollify.client.filesystem.FilePermission;
import org.sjarvela.mollify.client.filesystem.js.JsDirectory;
import org.sjarvela.mollify.client.session.ConfigurationInfo;
import org.sjarvela.mollify.client.session.FileSystemInfo;
import org.sjarvela.mollify.client.session.PermissionMode;
import org.sjarvela.mollify.client.session.SessionInfo;
import org.sjarvela.mollify.client.session.SessionSettings;
import org.sjarvela.mollify.client.util.DateTime;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class DemoData {
	private static final String ROOT_1 = "r1";
	private static final String ROOT_2 = "r2";

	private static final String DIR_1A = "r1a";
	private static final String DIR_1B = "r1b";
	private static final String DIR_2A = "r2a";

	private static final String FILE_1A1 = "1a1";
	private static final String FILE_1A2 = "1a2";
	private static final String FILE_1A3 = "1a3";
	private static final String FILE_1A4 = "1a4";
	private static final String FILE_1A5 = "1a5";
	private static final String FILE_1A6 = "1a6";

	private JsArray<JsDirectory> rootDirectories;
	private Map<String, List<Directory>> directories = new HashMap();
	private List<File> files;

	private final PermissionMode permissionMode = PermissionMode.Admin;
	private final SessionSettings settings;
	private final ConfigurationInfo configurationInfo;
	private final FileSystemInfo fileSystemInfo;
	private final boolean multiUser;

	public DemoData(boolean multiUser) {
		this.multiUser = multiUser;
		this.settings = SessionSettings.create(true, true, true, true, true);
		this.fileSystemInfo = FileSystemInfo.create(1024, 1024);
		this.configurationInfo = ConfigurationInfo.create(true);

		createDirectoriesAndFiles();
	}

	private void createDirectoriesAndFiles() {
		rootDirectories = JavaScriptObject.createArray().cast();
		rootDirectories.set(0, JsDirectory.create(ROOT_1, "Folder A"));
		rootDirectories.set(1, JsDirectory.create(ROOT_2, "Folder B"));

		List<Directory> subDirs = new ArrayList();
		directories.put(ROOT_1, subDirs);

		subDirs.add(new Directory(DIR_1A, "Sub folder A"));
		subDirs.add(new Directory(DIR_1B, "Sub folder B"));

		subDirs = new ArrayList();
		directories.put(ROOT_2, subDirs);
		subDirs.add(new Directory(DIR_2A, "Sub folder A"));

		files = new ArrayList();
		files.add(new File(FILE_1A1, "Example.txt", "txt", 128));
		files.add(new File(FILE_1A2, "Picture.gif", "gif", 2228));
		files.add(new File(FILE_1A3, "Picture.png", "png", 64434));
		files.add(new File(FILE_1A4, "Portable Document Format.pdf", "pdf",
				113428));
		files.add(new File(FILE_1A5, "Word Document.doc", "doc", 5634347));
		files.add(new File(FILE_1A6, "Web page.html", "html", 23433231));
	}

	public SessionInfo getSessionInfo(String user) {
		if (!multiUser) {
			return SessionInfo.create(false, false, "", "", permissionMode,
					settings, configurationInfo, fileSystemInfo,
					rootDirectories);
		}

		if (user != null && user.length() > 0)
			return SessionInfo.create(true, true, user, "", permissionMode,
					settings, configurationInfo, fileSystemInfo,
					rootDirectories);
		return SessionInfo.create(true, false, "", "", permissionMode,
				settings, configurationInfo, fileSystemInfo, rootDirectories);
	}

	public List<Directory> getDirectories(Directory dir) {
		if (!directories.containsKey(dir.getId()))
			return new ArrayList();
		return directories.get(dir.getId());
	}

	public List<File> getFiles(Directory dir) {
		return files;
	}

	public DirectoryDetails getDirectoryDetails(Directory directory) {
		return DirectoryDetails.create(FilePermission.ReadWrite, "Mollify demo");
	}

	public FileDetails getFileDetails(File file) {
		Date now = DateTime.getInstance().currentTime();
		return FileDetails.create(now, now, now, "Mollify demo",
				FilePermission.ReadWrite);
	}
}
