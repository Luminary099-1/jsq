package jsq;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;


/** Provides some error handling facilities for the application. */
public class Errors implements UncaughtExceptionHandler
{
	@Override public void uncaughtException(Thread t, Throwable e)
	{
		e.printStackTrace();
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Fatal Error");
		alert.setHeaderText(
			"JSQ has encountered a fatal error. The application will now exit.");
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		String e_text = sw.toString();
		
		TextArea ta = new TextArea(e_text);
		ta.setEditable(false);
		ta.setWrapText(false);

		alert.getDialogPane().setExpandableContent(ta);
		alert.showAndWait();
		Platform.exit();
	}

	/**
	 * Shows the user a dialog indicating an error occurred.
	 * @param title Dialog's title.
	 * @param message Message to explain the error.
	 */
	public static void ErrorDialog(String title, String message)
	{
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle("Failed to launch JSQ");
		alert.setHeaderText("Failed to load the application's interface.");
		alert.showAndWait();
	}
}
