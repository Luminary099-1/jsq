package jsq.command;

import jsq.Project;
import jsq.cue.Cue;

/** Command to update a cue's follows flag. */
public class UpdateCueFollows implements Command
{
	/** Cue to rename. */
	Cue _cue;
	/** Cue's new follows flag. */
	boolean _newFlag;
	/** Stores the cue's previous follows flag. */
	boolean _oldFlag;

	/**
	 * Creates a new instance of UpdateCueFollows.
	 * @param cue Cue to update.
	 * @param new_name The new follows flag for the specified cue.
	 */
	public UpdateCueFollows(Cue cue, boolean new_flag)
	{
		_cue = cue;
		_newFlag = new_flag;
		_oldFlag = _cue._follows;
	}

	@Override public void Apply(Project p)
	{
		_cue._follows = _newFlag;
	}

	@Override public void Revert(Project p)
	{
		_cue._follows = _oldFlag;
	}
}