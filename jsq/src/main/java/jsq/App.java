package jsq;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.json.JSONObject;
import org.json.JSONTokener;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import jsq.home.HomeController;


/** JavaFX app for JSQ. */
public class App extends Application
{
	/** Indicates the success of the application's initialization. */
	static protected boolean _canRun;

	/**
	 * JVM entry point. Starts the JSQ application after loading its settings.
	 * @param args The command-line arguments passed to the program.
	 */
	public static void main(String[] args)
	{
		_canRun = LoadSettings();
		launch();
	}

	/**
	 * If necessary, creates a directory for the workspace at the specified
	 * default location.
	 * @param home_dir The home directory of the current user (parent
	 * directory).
	 * @return {@code true} if the directory was created successfully or found
	 * to already exist; {@code false} otherwise.
	 */
	protected static boolean SetDefaultWorkspace(String home_dir)
	{
		File workspace_dir = new File(home_dir, "/jsq_workspace");
		Context._workspace = workspace_dir;
		if (!workspace_dir.isDirectory())
			return workspace_dir.mkdir();
		else return true;
	}

	/**
	 * Loads the application settings from the "jsq.json" file stored in the
	 * user's home directory. If the file does not exist, the relevant values in
	 * Context are initialized to their defaults.
	 * @return {@code true} if the settings were loaded successfully;
	 * {@code false} otherwise.
	 */
	protected static boolean LoadSettings()
	{
		String home_dir = System.getenv("HOME");
		if (home_dir == null) return false;
		File settings_file = new File(home_dir, "/jsq.json");
		JSONObject settings = null;
		try (FileInputStream set_stream = new FileInputStream(settings_file))
		{
			settings = new JSONObject(new JSONTokener(set_stream));
		}
		catch (IOException e)
		{
			// Unable to read file, so apply the default settings:
			return SetDefaultWorkspace(home_dir);
		}

		// Grab the workspace directory setting:
		if (settings.has("workspace"))
		{
			String workspace_path = settings.getString("workspace");
			File workspace_dir = new File(workspace_path);
			if (workspace_path.length() == 0 || !workspace_dir.isDirectory())
				return false;
			Context._workspace = workspace_dir;
			return true;
		}
		else return SetDefaultWorkspace(home_dir);
	}

	@Override public void start(Stage stage)
	{
		if (!_canRun)
		{
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Failed to launch JSQ");
			alert.setHeaderText(
				"Failed to initialize the application's settings.");
			alert.showAndWait();
			Platform.exit();
			return;
		}

		Context._stage = stage;
		Context.SwitchScene(HomeController.class.getResource("home.fxml"));
		stage.show();
	}
}
