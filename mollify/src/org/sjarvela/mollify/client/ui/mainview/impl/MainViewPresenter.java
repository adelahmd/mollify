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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sjarvela.mollify.client.filesystem.File;
import org.sjarvela.mollify.client.filesystem.FileSystemAction;
import org.sjarvela.mollify.client.filesystem.FileSystemItem;
import org.sjarvela.mollify.client.filesystem.Folder;
import org.sjarvela.mollify.client.filesystem.FolderInfo;
import org.sjarvela.mollify.client.filesystem.handler.DirectoryHandler;
import org.sjarvela.mollify.client.filesystem.handler.FileSystemActionHandler;
import org.sjarvela.mollify.client.filesystem.handler.FileSystemPermissionHandler;
import org.sjarvela.mollify.client.localization.TextProvider;
import org.sjarvela.mollify.client.service.Callback;
import org.sjarvela.mollify.client.service.ConfigurationService;
import org.sjarvela.mollify.client.service.FileSystemService;
import org.sjarvela.mollify.client.service.ServiceError;
import org.sjarvela.mollify.client.service.ServiceErrorType;
import org.sjarvela.mollify.client.service.SessionService;
import org.sjarvela.mollify.client.service.request.listener.ResultListener;
import org.sjarvela.mollify.client.session.SessionManager;
import org.sjarvela.mollify.client.session.user.PasswordHandler;
import org.sjarvela.mollify.client.ui.ViewManager;
import org.sjarvela.mollify.client.ui.common.grid.GridColumn;
import org.sjarvela.mollify.client.ui.common.grid.GridComparator;
import org.sjarvela.mollify.client.ui.common.grid.SelectController;
import org.sjarvela.mollify.client.ui.common.grid.Sort;
import org.sjarvela.mollify.client.ui.dialog.DialogManager;
import org.sjarvela.mollify.client.ui.dnd.DragDataProvider;
import org.sjarvela.mollify.client.ui.dropbox.DropBox;
import org.sjarvela.mollify.client.ui.filelist.DefaultFileItemComparator;
import org.sjarvela.mollify.client.ui.filelist.FileList;
import org.sjarvela.mollify.client.ui.fileupload.FileUploadDialogFactory;
import org.sjarvela.mollify.client.ui.folderselector.FolderListener;
import org.sjarvela.mollify.client.ui.mainview.CreateFolderDialogFactory;
import org.sjarvela.mollify.client.ui.password.PasswordDialogFactory;
import org.sjarvela.mollify.client.ui.permissions.PermissionEditorViewFactory;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;

public class MainViewPresenter implements FolderListener, PasswordHandler,
		FileSystemPermissionHandler, DragDataProvider<FileSystemItem> {
	private final MainViewModel model;
	private final DefaultMainView view;
	private final DialogManager dialogManager;
	private final SessionManager sessionManager;
	private final SessionService sessionService;

	private final FileSystemService fileSystemService;
	private final ConfigurationService configurationService;
	private final FileSystemActionHandler fileSystemActionHandler;
	private final TextProvider textProvider;
	private final PermissionEditorViewFactory permissionEditorViewFactory;
	private final PasswordDialogFactory passwordDialogFactory;
	private final FileUploadDialogFactory fileUploadDialogFactory;
	private final CreateFolderDialogFactory createFolderDialogFactory;
	private final ViewManager viewManager;
	private final DropBox dropBox;
	private boolean exposeFileUrls;

	public MainViewPresenter(DialogManager dialogManager,
			ViewManager viewManager, SessionManager sessionManager,
			MainViewModel model, DefaultMainView view,
			ConfigurationService configurationService,
			FileSystemService fileSystemService, TextProvider textProvider,
			FileSystemActionHandler fileSystemActionHandler,
			PermissionEditorViewFactory permissionEditorViewFactory,
			PasswordDialogFactory passwordDialogFactory,
			FileUploadDialogFactory fileUploadDialogFactory,
			CreateFolderDialogFactory createFolderDialogFactory,
			DropBox dropBox, boolean exposeFileUrls,
			SessionService sessionService) {
		this.dialogManager = dialogManager;
		this.viewManager = viewManager;
		this.sessionManager = sessionManager;
		this.configurationService = configurationService;
		this.fileSystemService = fileSystemService;
		this.sessionService = sessionService;

		this.model = model;
		this.view = view;
		this.textProvider = textProvider;
		this.fileSystemActionHandler = fileSystemActionHandler;
		this.permissionEditorViewFactory = permissionEditorViewFactory;
		this.passwordDialogFactory = passwordDialogFactory;
		this.fileUploadDialogFactory = fileUploadDialogFactory;
		this.createFolderDialogFactory = createFolderDialogFactory;
		this.dropBox = dropBox;
		this.exposeFileUrls = exposeFileUrls;

		this.view.getFileContext()
				.setActionHandler(fileSystemActionHandler);
		this.view.getFileContext().setFilePermissionHandler(this);

		this.view
				.setListSelectController(new SelectController<FileSystemItem>() {
					@Override
					public boolean isSelectable(FileSystemItem t) {
						if (t.isFile())
							return true;
						return !Folder.Parent.equals(t)
								&& !((Folder) t).isRoot();
					}

				});
		this.setListOrder(FileList.COLUMN_NAME, Sort.asc);

		if (model.getSession().isAuthenticationRequired())
			view.getUsername().setText(model.getSession().getLoggedUser());
	}

	public void initialize() {
		if (exposeFileUrls)
			viewManager.getHiddenPanel().add(view.createFileUrlContainer());

		changeToRootDirectory(model.getRootFolders().size() == 1 ? model
				.getRootFolders().get(0) : null);
		if (model.getRootFolders().size() == 0)
			view.hideButtons();
	}

	public void onFileSystemItemSelected(FileSystemItem item, GridColumn column) {
		if (column.equals(FileList.COLUMN_NAME)) {
			if (item.isFile()) {
				view.showFileContext((File) item);
			} else {
				Folder directory = (Folder) item;

				if (directory == Folder.Parent)
					onMoveToParentFolder();
				else
					changeToDirectory(directory);
			}
		}
	}

	public void changeToRootDirectory(Folder root) {
		model.changeToRootDirectory(root, createRefreshListener());
	}

	public void changeToDirectory(Folder directory) {
		model.changeToSubdirectory(directory, createRefreshListener());
	}

	public void reset() {
		view.clear();
	}

	public void reload() {
		model.refreshData(new ResultListener<FolderInfo>() {
			public void onFail(ServiceError error) {
				onError(error, false);
			}

			public void onSuccess(FolderInfo result) {
				refreshView();
			}
		});
	}

	private void refreshView() {
		List<FileSystemItem> allFileItems = model.getAllItems();
		if (model.getFolderModel().canAscend())
			allFileItems.add(0, Folder.Parent);

		view.getList().setContent(allFileItems);
		view.setAddButtonVisible(model.getFolderPermission().canWrite());
		view.refresh();
		if (exposeFileUrls)
			refreshFileUrls(model.getFiles());
	}

	private void refreshFileUrls(List<File> files) {
		String sessionId = sessionManager.getSession().getSessionId();
		Map<String, String> urls = new HashMap();
		for (File f : files)
			urls.put(f.getName(), fileSystemService
					.getDownloadUrl(f, sessionId));
		view.refreshFileUrls(urls);
	}

	public void onMoveToParentFolder() {
		if (!model.getFolderModel().canAscend())
			return;
		model.moveToParentDirectory(createRefreshListener());
	}

	public void onChangeToFolder(int level, Folder directory) {
		model.changeToDirectory(level, directory, createRefreshListener());
	}

	public void onError(ServiceError error, boolean reload) {
		dialogManager.showError(error);

		if (reload)
			reload();
		else
			reset();
	}

	public void openUploadDialog() {
		if (!model.hasFolder() || model.getCurrentFolder().isEmpty())
			return;

		fileUploadDialogFactory.openFileUploadDialog(model.getCurrentFolder(),
				createReloadListener("Upload"));
	}

	public void openNewDirectoryDialog() {
		if (!model.hasFolder() || model.getCurrentFolder().isEmpty())
			return;

		createFolderDialogFactory.openCreateFolderDialog(model
				.getCurrentFolder(), new DirectoryHandler() {
			public void createDirectory(Folder parentFolder, String folderName) {
				fileSystemService.createFolder(parentFolder, folderName,
						createReloadListener("Create folder"));
			}
		});
	}

	private ResultListener createReloadListener(final String operation) {
		return createListener(new Callback() {
			public void onCallback() {
				DeferredCommand.addCommand(new Command() {
					@Override
					public void execute() {
						Log.debug(operation + " complete");
						reload();
					}
				});
			}
		});
	}

	private ResultListener createRefreshListener() {
		return createListener(createRefreshCallback());
	}

	private Callback createRefreshCallback() {
		return new Callback() {
			public void onCallback() {
				refreshView();
			}
		};
	}

	private ResultListener createListener(final Callback callback) {
		return new ResultListener<Object>() {
			public void onFail(ServiceError error) {
				onError(error, true);
			}

			public void onSuccess(Object result) {
				callback.onCallback();
			}
		};
	}

	public void logout() {
		sessionService.logout(new ResultListener<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				dropBox.close();
				sessionManager.endSession();
			}

			@Override
			public void onFail(ServiceError error) {
				onError(error, false);
			}
		});
	}

	public void changePassword() {
		passwordDialogFactory.openPasswordDialog(this);
	}

	public void changePassword(String oldPassword, String newPassword) {
		configurationService.changePassword(oldPassword, newPassword,
				new ResultListener() {
					public void onFail(ServiceError error) {
						if (ServiceErrorType.AUTHENTICATION_FAILED.equals(error
								.getType())) {
							dialogManager.showInfo(textProvider.getStrings()
									.passwordDialogTitle(), textProvider
									.getStrings()
									.passwordDialogOldPasswordIncorrect());
						} else {
							onError(error, false);
						}
					}

					public void onSuccess(Object result) {
						dialogManager.showInfo(textProvider.getStrings()
								.passwordDialogTitle(), textProvider
								.getStrings()
								.passwordDialogPasswordChangedSuccessfully());
					}
				});
	}

	public void setListOrder(GridColumn column, Sort sort) {
		view.getList().setComparator(createComparator(column, sort));
	}

	private GridComparator<FileSystemItem> createComparator(GridColumn column,
			Sort sort) {
		return new DefaultFileItemComparator(column, sort);
	}

	public void onEditPermissions(FileSystemItem item) {
		permissionEditorViewFactory.openPermissionEditor(item);
	}

	public void onEditItemPermissions() {
		permissionEditorViewFactory.openPermissionEditor(null);
	}

	public void onOpenAdministration() {
		viewManager.openUrlInNewWindow(configurationService
				.getAdministrationUrl());
	}

	public void onToggleSelectMode() {
		view.setSelectMode(view.selectModeButton().isDown());
	}

	public void onFileSystemItemSelectionChanged(List<FileSystemItem> selected) {
		model.setSelected(selected);
		view.updateFileSelection(selected);
	}

	public void onSelectAll() {
		view.selectAll();
	}

	public void onSelectNone() {
		view.selectNone();
	}

	public void onCopySelected() {
		fileSystemActionHandler.onAction(model.getSelectedItems(),
				FileSystemAction.copy, null, null, new Callback() {
					@Override
					public void onCallback() {
						view.selectNone();
					}
				});
	}

	public void onMoveSelected() {
		fileSystemActionHandler.onAction(model.getSelectedItems(),
				FileSystemAction.move, null, null, new Callback() {
					@Override
					public void onCallback() {
						view.selectNone();
					}
				});
	}

	public void onDeleteSelected() {
		fileSystemActionHandler.onAction(model.getSelectedItems(),
				FileSystemAction.delete, null, null, new Callback() {
					@Override
					public void onCallback() {
						view.selectNone();
					}
				});
	}

	public void onAddSelectedToDropbox() {
		dropBox.addItems(getSelectedItems());
		view.selectNone();
	}

	public void onToggleDropBox() {
		dropBox.toggle(view.getDropboxLocation());
	}

	@Override
	public List<FileSystemItem> getSelectedItems() {
		return model.getSelectedItems();
	}

}
