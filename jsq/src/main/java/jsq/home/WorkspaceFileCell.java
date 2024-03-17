package jsq.home;

import java.io.File;

import javafx.scene.control.ListCell;


/**
 * List cell with custom text to display the entries of the workspace projects.
 */
public class WorkspaceFileCell extends ListCell<File>
{
	@Override protected void updateItem(File file, boolean empty)
	{
		super.updateItem(file, empty);
		if (empty || file == null)
		{
			setText(null);
			return;
		}
		setText(file.getName());
	}
}
