package jsq.command;

import jsq.cue.Cue;
import jsq.project.Project;


/** Command to create a new cue. */
public class InsertCue extends Command
{
	/** Index to insert the new cue into the list. */
	protected final int _index;
	/** Cue to be inserted into the list. */
	protected final Cue _cue;

	/**
	 * Creates an instance of InsertCue.
	 * @param index Cue list index where a new blank cue is to be inserted.
	 * @param cue Blank cue to be inserted.
	 */
	public InsertCue(int index, Cue cue)
	{
		_index = index;
		_cue = cue;
	}

	@Override public void Apply(Project p)
	{
		super.Apply(p);
		p._cueList.add(_index, _cue);
	}

	@Override public void Revert(Project p)
	{
		super.Revert(p);
		p._cueList.remove(_index);
	}
}
