package jsq.command;

import jsq.Project;
import jsq.cue.Cue;

/** Command to update a cue's active flag. */
public class UpdateCueActive implements Command
{
	/** Cue to rename. */
	Cue _cue;
	/** Cue's new active flag. */
	boolean _newFlag;
	/** Stores the cue's previous active flag. */
	boolean _oldFlag;

	/**
	 * Creates a new instance of UpdateCueActive.
	 * @param cue Cue to update.
	 * @param new_name The new active flag for the specified cue.
	 */
	public UpdateCueActive(Cue cue, boolean new_flag)
	{
		_cue = cue;
		_newFlag = new_flag;
		_oldFlag = _cue._active;
	}

	@Override public void Apply(Project p)
	{
		_cue._active = _newFlag;
	}

	@Override public void Revert(Project p)
	{
		_cue._active = _oldFlag;
	}
}
