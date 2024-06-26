package jsq;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import jsq.command.BulkCommand;
import jsq.command.Command;
import jsq.command.DeleteCue;
import jsq.command.InsertCue;
import jsq.cue.Cue;
import jsq.project.Project;
import jsq.project.Resource;


/** Stores the global context of the JSQ application instance. */
public class Context
{
	/** The application's primary stage. */
	public static Stage _stage;
	/** Directory of the workspace where projects are stored. */
	public static File _workspace = null;
	/** Directory of the currently open project. */
	public static File _folder;
	/** Project instance to store the current project's state. */
	protected static Project _project = new Project();
	/** Stack to store operations after they're applied. */
	public static ObservableList<Command> _undoStack
		= FXCollections.observableArrayList();
	/** Stack to store operations after they're undone. */
	public static ObservableList<Command> _redoStack
		= FXCollections.observableArrayList();
	/** Reference to the last command applied when the project was saved. */
	protected static Command _lastSaved = null;
	/** Stores all cues selected by the last copy/cut operation. */
	public static
	ObservableList<Cue> _clipboard = FXCollections.observableArrayList();
	/** Indicates the contents of the clipboard were cut, not copied. */
	protected static boolean _cutClipboard;

	/**
	 * Switches the primary application window to the interface described by the
	 * passed FXML.
	 * @param fxml FXML to load and display on the primary window.
	 * @throws RuntimeException If the scenes's FXML cannot be loaded.
	 */
	public static void SwitchScene(URL fxml) throws RuntimeException
	{
		FXMLLoader loader = new FXMLLoader(fxml);
		Parent root = null;
		try { root = loader.load(); }
		catch (IOException e) { throw new RuntimeException(e); }
		Scene old = _stage.getScene();
		double width = (old != null) ? old.getWidth() : 1280;
		double height = (old != null) ? old.getHeight() : 720;
		
		Scene scene = new Scene(root, width, height);
		_stage.hide();
		_stage.setScene(scene);
		_stage.show();
	}

	/** Sets the stage's title to reflect the currently open project. */
	public static void UpdateStageTitle()
	{
		Context._stage.setTitle(String.format(
			"JSQ: %s%c",
			_folder.getName(),
			(IsSaved()) ? ' ' : '*'
		));
	}

	/**
	 * Applies the provided command. The redo list is cleared.
	 * @param command The command to apply to the project associated with this
	 * context.
	 */
	public static void Apply(Command command)
	{
		_redoStack.clear();
		command.Apply(_project);
		_undoStack.add(command);
		UpdateStageTitle();
	}

	/** Undoes the last applied operation. */
	public static void Undo()
	{
		if (_undoStack.isEmpty()) return;
		Command command = _undoStack.removeLast();
		command.Revert(_project);
		_redoStack.add(command);
		UpdateStageTitle();
	}

	/** Redoes the last undone operation. */
	public static void Redo()
	{
		if (_redoStack.isEmpty()) return;
		Command command = _redoStack.removeLast();
		command.Apply(_project);
		_undoStack.add(command);
		UpdateStageTitle();
	}

	/**
	 * @return {@code true} if the project's last saved state is current in this
	 * context, {@code false} otherwise.
	 */
	public static boolean IsSaved()
	{
		return _undoStack.isEmpty() || _undoStack.getLast() == _lastSaved;
	}

	/**
	 * Creates a new, empty project directory at the specified path.
	 * @param project_dir Path of the new directory.
	 * @throws IOException If an error occurs while initializing the project.
	 */
	public static void
	InitializeProjectDirectory(File project_dir) throws IOException
	{
		project_dir.mkdir();
		new File(project_dir, "resources").mkdir();
		File project_data = new File(project_dir, "data.jsq");
		Project empty_project = new Project();

		try
		(
			FileOutputStream fos = new FileOutputStream(project_data);
			ObjectOutputStream os = new ObjectOutputStream(fos)
		)
		{ empty_project.WriteObject(os); }
		catch (FileNotFoundException e)
		{
			Errors.ErrorDialog("Failed to Initialize Project", 
				"Unable to create a project data file.");
			throw new IOException(e);
		}
		catch (IOException e)
		{
			Errors.ErrorDialog("Failed to Initialize Project", 
				"An error occurred while initializing the project data file.");
			throw new IOException(e);
		}

		Context.Reset();
		_project = empty_project;
	}

	/**
	 * Loads a project state into {@code _project} from the location specified
	 * by the user.dir system variable.
	 * @throws IOException If an error occurs while loading the project.
	 */
	public static void Load() throws IOException
	{
		Reset();
		File data = new File(Context._folder, "data.jsq");

		try
		(
			FileInputStream fis = new FileInputStream(data);
			ObjectInputStream is = new ObjectInputStream(fis)
		)
		{ _project.ReadObject(is); }
		catch (FileNotFoundException e)
		{
			Errors.ErrorDialog("Failed to Load Project", 
				"Unable to open the project data file.");
			throw new IOException(e);
		}
		catch (IOException | ClassNotFoundException e)
		{
			Errors.ErrorDialog("Failed to Load Project", 
				"An error occurred while loading the project data file.");
			throw new IOException(e);
		}
	}

	/**
	 * Saves the current state of {@code _project} to the location specified by
	 * the user.dir system variable.
	 * @throws IOException If an error occurs while saving the project.
	 */
	public static void Save() throws IOException
	{
		File project_data = new File(Context._folder, "data.jsq");

		try
		(
			FileOutputStream fos = new FileOutputStream(project_data);
			ObjectOutputStream os = new ObjectOutputStream(fos)
		)
		{ _project.WriteObject(os); }
		catch (FileNotFoundException e)
		{
			Errors.ErrorDialog("Failed to Save Project", 
				"Unable to open the project data file.");
			throw new IOException(e);
		}
		catch (IOException e)
		{
			Errors.ErrorDialog("Failed to Save Project", 
				"An error occurred while saving the project data file.");
			throw new IOException(e);
		}

		_lastSaved = (!_undoStack.isEmpty()) ? _undoStack.getLast() : null;
		UpdateStageTitle();
	}

	/**
	 * @return A reference to the observable list that stores the cue list. To
	 * be used to populate the appropriate ListView in the UI.
	 */
	public static ObservableList<Cue> GetCueList()
	{
		return _project._cueList;
	}

	/**
	 * Resets the context to a clean and empty state. The current project is not
	 * saved.
	 */
	public static void Reset()
	{
		_undoStack.clear();
		_redoStack.clear();
		_clipboard.clear();
		_project.Clear();
		_lastSaved = null;
	}

	/**
	 * Updates {@code _clipboard} to reflect a cut or copy operation. If the
	 * operation is to cut, then the selected cues are deleted from cue list
	 * while stored in the clipboard.
	 * @param is_cut {@code true} if the operation is cut; {@code false} if the
	 * operation is copy.
	 * @param selected Currently selected indices in the cue list.
	 */
	public static void CutCopy(boolean is_cut, ObservableList<Integer> selected)
	{
		_clipboard.clear();
		for (Integer i : selected) _clipboard.add(_project._cueList.get(i));
		if (!(_cutClipboard = is_cut)) return;

		List<DeleteCue> del_commands = new ArrayList<>(_clipboard.size());
		for (Integer i : selected.reversed())
			del_commands.add(new DeleteCue(i, _project._cueList.get(i)));
		Apply(new BulkCommand<DeleteCue>(del_commands));
	}

	/**
	 * Inserts the cues stored in the {@code _clipboard} into the cue list at
	 * the specified index.
	 * @param index Position in the cue list to insert the cut/copied cues.
	 */
	public static void Paste(int index)
	{
		List<InsertCue> ins_commands = new ArrayList<>(_clipboard.size());

		if (_cutClipboard)
			for (Cue cue : _clipboard.reversed())
				ins_commands.add(new InsertCue(index, cue));
		else 
			for (Cue cue : _clipboard.reversed())
				ins_commands.add(new InsertCue(index, cue.clone()));

		Apply(new BulkCommand<InsertCue>(ins_commands));
	}

	/**
	 * Declares a new useage of a resource in the project.
	 * @param source Absolute path of the resource to incorporate.
	 * @return Newly incorporated resource or {@code null} if the resource could
	 * not be accessed.
	 */
	public static Resource RegisterResource(File source)
	{
		try { return _project.RegisterResource(source); }
		catch (IOException e)
		{
			Errors.ErrorDialog("Import Error", 
				"An error prevented the resource from being imported.");
		}
		return null;
	}

	/**
	 * Returns a resource's original filename.
	 * @param resource A resource in the current project.
	 * @return Resource's name.
	 */
	public static String GetResourceName(Resource resource)
	{
		return _project.GetResourceName(resource);
	}

	/**
	 * Unwinds the undo stack and deletes all subsequently unused project
	 * resources.
	 */
	public static void CleanupResources()
	{
		while (!_undoStack.isEmpty() && _undoStack.getLast() != _lastSaved)
			_undoStack.removeLast().Revert(_project);
		
		_project.CullUnusedResources();
	}
}
