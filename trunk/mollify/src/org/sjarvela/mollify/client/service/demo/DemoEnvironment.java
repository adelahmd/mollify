package org.sjarvela.mollify.client.service.demo;

import org.sjarvela.mollify.client.request.file.FileUploadHandler;
import org.sjarvela.mollify.client.service.FileSystemService;
import org.sjarvela.mollify.client.service.ServiceEnvironment;
import org.sjarvela.mollify.client.service.SessionService;
import org.sjarvela.mollify.client.session.ClientSettings;

import com.allen_sauer.gwt.log.client.Log;

public class DemoEnvironment implements ServiceEnvironment {
	static final String MOLLIFY_PACKAGE_URL = "http://mollify.googlecode.com/files/mollify_0.7.2.tar.gz";
	private static final String PARAM_MULTI_USER = "multi-user";

	private DemoSessionService sessionService;
	private DemoData data;
	private FileSystemService fileSystemService;
	private FileUploadHandler demoFileUploadHandler;

	public void initialize(ClientSettings settings) {
		Log.info("Mollify Demo");

		this.data = new DemoData(settings.getBool(PARAM_MULTI_USER, true));
		this.sessionService = new DemoSessionService(data);
		this.fileSystemService = new DemoFileService(data);
		this.demoFileUploadHandler = new DemoFileUploadHandler();
	}

	public SessionService getSessionService() {
		return sessionService;
	}

	public FileSystemService getFileSystemService() {
		return fileSystemService;
	}

	public FileUploadHandler getFileUploadHandler() {
		return demoFileUploadHandler;
	}

}
