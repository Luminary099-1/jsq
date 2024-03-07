package jsq;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


/** JavaFX app for JSQ. */
public class App extends Application
{
	/**
	 * 
	 * @param stage
	 */
	@Override public void start(Stage stage) throws IOException
	{
		Context._stage = stage;
		FXMLLoader fxmlLoader
			= new FXMLLoader(getClass().getResource("editor.fxml"));
		Parent root_node = fxmlLoader.load();
		Scene _scene = new Scene(root_node, 1280, 720);
		stage.setScene(_scene);
		stage.show();
	}

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) 
	{
		launch();
	}
}
