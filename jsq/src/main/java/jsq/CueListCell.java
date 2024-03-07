package jsq;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import jsq.cue.Cue;


/** List cell with custom graphics to display cues. */
public class CueListCell extends ListCell<Cue>
{
	/** HBox root of this cell's graphic. */
	@FXML protected HBox _root;
	/** Next cue to go indicator. */
	@FXML protected Label _triangle;
	/** Cue's number. */
	@FXML protected Label _number;
	/** Cue's name. */
	@FXML protected Label _name;
	/** Cue's type label. */
	@FXML protected Label _type;
	/** Displays the cue's flags. */
	@FXML protected Label _flags;
	/** Loads the custom graphic for this cell. */
	protected FXMLLoader _loader = null;

	/**
	 * Updates the information displayed by this cell when the observed list is
	 * updated.
	 * @param cue The cue represented by this cell affected by the update.
	 * @param empty {@code true} if {@code this} represents an empty cell;
	 * {@code false} otherwise.
	 */
	@Override protected void updateItem(Cue cue, boolean empty)
	{
		super.updateItem(cue, empty);
		setText(null);

		if (empty || cue == null)
		{
			setGraphic(null);
			return;	
		}

		if (_loader == null)
		{
			FXMLLoader _loader = new FXMLLoader(
				CueListCell.class.getResource("cueListCell.fxml"));
			_loader.setController(this);
			try { _loader.load(); }
			catch (IOException e) { e.printStackTrace(); }
		}

		_triangle.setText(" ");
		_number.setText(Integer.toString(this.getIndex() + 1));
		_name.setText(cue._name);
		_type.setText(cue.ShortTypeName());
		_flags.setText(String.format(
			"%c %c",
			cue._active ? 'A' : ' ',
			cue._follows ? 'F' : ' '
		));
		setGraphic(_root);
	}
}
