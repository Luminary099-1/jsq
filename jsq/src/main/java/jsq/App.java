package jsq;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.json.JSONObject;
import org.json.JSONTokener;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;


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
	// ToDo: Ensure this works on different common platforms.
	{
		_canRun = LoadSettings();
		launch();
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
		File settings_file = new File(home_dir.concat("/jsq.json"));
		JSONObject settings = null;
		try (FileInputStream set_stream = new FileInputStream(settings_file))
		{
			settings = new JSONObject(new JSONTokener(set_stream));
		}
		catch (IOException e) { return false; }

		// Populate the workspace directory:
		if (settings.has("workspace"))
		{
			String workspace_path = settings.getString("workspace");
			if (workspace_path.length() != 0)
				Context._workspaceDir = new File(workspace_path);
		}
		else 
			Context._workspaceDir = new File(home_dir.concat("/jsq_workspace"));

		return true;
	}

	@Override public void start(Stage stage) throws IOException
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
		FXMLLoader fxmlLoader
			= new FXMLLoader(getClass().getResource("editor.fxml"));
		Parent root_node = fxmlLoader.load();
		Scene _scene = new Scene(root_node, 1280, 720);
		stage.setScene(_scene);
		stage.show();
	}
}
