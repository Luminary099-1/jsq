package jsq;

import jsq.cue.Cue;
import jsq.cue.StoppableCue;


/** List cell that disables itself if not stoppable by a certain stop cue. */
public class SelectStopListCell extends CueListCell
// ToDo: Implement a custom node for these list elements.
{
	// ToDo: Ideally, this is a bool with the comparison in the constructor, but getIndex() is invalid until the initialization phase.
	/** Index of the first stop cue whose targets are being selected. */
	int _stoppingIndex;

	/**
	 * Creates a new 
	 * @param stopping_index Index of the stop cue whose stoppable targets are
	 * being selected.
	 */
	public SelectStopListCell(int stopping_index)
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
		if (empty || cue == null) return;		
		setDisable(
			getIndex() >= _stoppingIndex || !(cue instanceof StoppableCue));
	}
}
