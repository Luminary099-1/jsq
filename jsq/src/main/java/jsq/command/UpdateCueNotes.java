package jsq.command;

import jsq.cue.Cue;
import jsq.project.Project;


/** Command to update the notes associated with a cue. */
public class UpdateCueNotes extends Command
{
	/** Cue to update. */
	protected final Cue _cue;
	/** Cue's new notes. */
	protected final String _newNotes;
	/** Cue's previous notes. */
	protected final String _oldNotes;

	/**
	 * Creates a new command to update the notes associated with a cue.
	 * @param cue Cue to update.
	 * @param new_notes Notes to assign to the cue.
	 */
	public UpdateCueNotes(Cue cue, String new_notes)
	{
		_cue = cue;
		_oldNotes = cue._notes;
		_newNotes = new_notes;
	}

	@Override public void Apply(Project p)
	{
		super.Apply(p);
		_cue._notes = _newNotes;
	}

	@Override public void Revert(Project p)
	{
		super.Revert(p);
		_cue._notes = _oldNotes;
	}
}
