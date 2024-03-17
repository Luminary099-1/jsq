package jsq.stop_selector;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import jsq.cue.Cue;
import jsq.cue.StoppableCue;


/** List cell that disables itself if not stoppable by a certain stop cue. */
public class StopSelectListCell extends ListCell<Cue>
{
	/** HBox root of this cell's graphic. */
	@FXML protected HBox _root;
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
	
	// ToDo: Ideally, this is a bool with the comparison in the constructor, but getIndex() is invalid until the initialization phase.
	/** Index of the first stop cue whose targets are being selected. */
	int _stoppingIndex;

	/**
	 * Creates a new list cell for the stop cell target selector.
	 * @param stopping_index Index of the first stop cue whose stoppable targets
	 * are being selected.
	 */
	public StopSelectListCell(int stopping_index)
	{
		super();
		_stoppingIndex = stopping_index;
	}

	/**
	 * Updates this cell's contents and disables it if it cannot be stopped.
	 * @param cue The cue being displayed.
	 * @param empty {@code true} if {@code this} represents an empty cell;
	 * {@code false} otherwise.
	 */
	@Override protected void updateItem(Cue cue, boolean empty)
	{
		super.updateItem(cue, empty);
		if (empty || cue == null)
		{
			setGraphic(null);
			return;	
		}

		if (_loader == null)
		{
			FXMLLoader _loader = new FXMLLoader(
				StopSelectListCell.class.getResource("stopSelectCell.fxml"));
			_loader.setController(this);
			try { _loader.load(); }
			catch (IOException e) { e.printStackTrace(); }
		}

		_number.setText(Integer.toString(this.getIndex() + 1));
		_name.setText(cue._name);
		_type.setText(cue.ShortTypeName());
		_flags.setText(String.format(
			"%c %c",
			cue._active ? 'A' : ' ',
			cue._follows ? 'F' : ' '
		));
		setGraphic(_root);

		setDisable(
			getIndex() >= _stoppingIndex || !(cue instanceof StoppableCue));
	}
}
