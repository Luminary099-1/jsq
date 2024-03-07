package jsq;

import javafx.scene.control.ListCell;


/** List cell with custom text to display the entries of the new cue combo. */
public class NewCueCell extends ListCell<CueTypes>
{
	/**
	 * Updates this cells text to the name of the displayed {@code CueType}.
	 * @param type The cue type instance being displayed.
	 * @param empty {@code true} if {@code this} represents an empty cell;
	 * {@code false} otherwise.
	 */
	@Override protected void updateItem(CueTypes type, boolean empty)
	{
		super.updateItem(type, empty);
		if (empty || type == null)
		{
			setText("ERROR");
			return;
		}
		setText(type.GetName());
	}
}
