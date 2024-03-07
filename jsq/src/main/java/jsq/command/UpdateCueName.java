package jsq.command;

import jsq.Project;
import jsq.cue.Cue;

/** Command to update a cue's name. */
public class UpdateCueName implements Command
{
	/** Cue to rename. */
	Cue _cue;
	/** Cue's new name. */
	String _newName;
	/** Stores the cue's previous name. */
	String _oldName;

	/**
	 * Creates a new instance of UpdateCueName.
	 * @param cue Cue to update.
	 * @param new_name The new name for the specified cue.
	 */
	public UpdateCueName(Cue cue, String new_name)
	{
		_cue = cue;
		_newName = new_name;
		_oldName = _cue._name;
	}

	@Override public void Apply(Project p)
	{
		_cue._name = _newName;
	}

	@Override public void Revert(Project p)
	{
		_cue._name = _oldName;
	}
}
