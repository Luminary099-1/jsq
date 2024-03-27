package jsq.command;

import jsq.cue.Cue;
import jsq.project.Project;

/** Command to update a cue's follows flag. */
public class UpdateCueFollows implements Command
{
	/** Cue to rename. */
	protected final Cue _cue;
	/** Cue's new follows flag. */
	protected final boolean _newFlag;
	/** Stores the cue's previous follows flag. */
	protected final boolean _oldFlag;

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
