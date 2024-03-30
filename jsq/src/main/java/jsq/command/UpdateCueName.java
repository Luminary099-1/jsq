package jsq.command;

import jsq.cue.Cue;
import jsq.project.Project;

/** Command to update a cue's name. */
public class UpdateCueName extends Command
{
	/** Cue to rename. */
	protected final Cue _cue;
	/** Cue's new name. */
	protected final String _newName;
	/** Stores the cue's previous name. */
	protected final String _oldName;

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
		super.Apply(p);
		_cue._name = _newName;
	}

	@Override public void Revert(Project p)
	{
		super.Revert(p);
		_cue._name = _oldName;
	}
}
