package jsq.command;

import jsq.Project;
import jsq.cue.Cue;


/** Command to "delete" a cue. */
public class DeleteCue implements Command
{
	/** Stores the "deleted" cue. */
	protected Cue _deleted;
	/** The index of the "deleted" cue. */
	protected int _index;

	/**
	 * Creates and instance of DeleteCue.
	 * @param index The index of the cue to be deleted.
	 */
	public DeleteCue(int index)
	{
		_index = index;
	}

	@Override public void Apply(Project p)
	{
		_deleted = p._cueList.remove(_index);
	}

	@Override public void Revert(Project p)
	{
		p._cueList.add(_index, _deleted);
	}
	
}
