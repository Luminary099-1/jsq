package jsq.home;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import jsq.Context;
import jsq.Errors;
import jsq.editor.EditorController;


/** JavaFX controller for the home interface. */
public class HomeController
{
	/** Displays all projects stored in the workspace. */
	@FXML protected ListView<File> _workspaceList;
	/** Field to input the name of a new project. */
	@FXML protected TextField _createNameField;
	/** Triggers the creation of a new project. */
	@FXML protected Button _createButton;
	/** Stores the paths of all projects in the workspace. */
	protected ObservableList<File> _recentProjects;

	/**
	 * Determines whether the provided path likely contains a JSQ project.
	 * @param candidate The directory to examine.
	 * @return {@code true} if the directory is likely a JSQ project;
	 * {@code false} otherwise.
	 */
	protected boolean ValidateProjectFolder(File candidate)
	{
		if (!candidate.isDirectory()) return false;
		File[] children = candidate.listFiles();
		if (children.length != 2) return false;

		boolean has_data = false;
		boolean has_sounds = false;
		for (File child : children)
		{
			if (child.isDirectory() && child.getName().equals("resources"))
				has_sounds = true;
			else if (child.isFile() && child.getName().equals("data.jsq"))
				has_data = true;
		}

		return has_data && has_sounds;
	}
	
	/** JavaFX injectable initialization of the editor's GUI. */
	public void initialize()
	{
		Context._stage.setTitle("JSQ: Home");
		_recentProjects = FXCollections.observableArrayList();
		File[] projects = Context._workspace.listFiles();
		for (File project : projects)
			if (ValidateProjectFolder(project)) _recentProjects.add(project);

		_recentProjects.sort(new Comparator<File>()
			{
				@Override public int compare(File o1, File o2)
				{ return Long.signum(o2.lastModified() - o1.lastModified()); }
			}
		);

		_workspaceList.setItems(_recentProjects);
		_workspaceList.getSelectionModel()
			.setSelectionMode(SelectionMode.SINGLE);
		_workspaceList.setCellFactory(workspace_lv -> new WorkspaceFileCell());
	}

	/**
	 * Opens the project selected in the list of workspace projects.
	 * @throws RuntimeException If the context switch to the editor fails.
	 */
	@FXML protected void OnOpenWorkspace() throws RuntimeException
	{
		MultipleSelectionModel<File> sm = _workspaceList.getSelectionModel();
		if (sm.isEmpty()) return;
		Context._folder = sm.getSelectedItem();
		try { Context.Load();}
		catch (Exception e) { throw new RuntimeException(e); }
		Context.SwitchScene(EditorController.class.getResource("editor.fxml"));
	}

	/** Disables the create project button if there is no name specified. */
	@FXML protected void OnNewNameUpdate()
	{
		_createButton.setDisable(_createNameField.getLength() == 0);
	}

	/**
	 * Creates a new project in the workspace with the name specified. An error
	 * is shown to the user and nothing is created if there is already a project
	 * with the same name.
	 */
	@FXML protected void OnCreateNew()
	{
		File project_dir
			= new File(Context._workspace, _createNameField.getText());

		if (project_dir.isDirectory())
		{
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error Creating a New Project");
			alert.setHeaderText(String.format(
				"A project already exists with the name: %s",
				_createNameField.getText()
			));
			alert.showAndWait();
			return;
		}

		try { Context.InitializeProjectDirectory(project_dir); }
		catch (IOException e)
		{
			Errors.ErrorDialog("Error", 
				"An error prevented the new project from being initialized.");
			return;
		}

		Context._folder = project_dir;
		Context.SwitchScene(EditorController.class.getResource("editor.fxml"));
	}
}
