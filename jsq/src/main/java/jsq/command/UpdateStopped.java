package jsq.command;

import java.util.ArrayList;
import java.util.List;

import jsq.cue.Stop;
import jsq.cue.StoppableCue;
import jsq.project.Project;


/** Command to update a stop cue's targets. */
public class UpdateStopped implements Command
{
	/** Cue to update. */
	protected final Stop _cue;
	/** New targets to assign to the cue. */
	protected final ArrayList<StoppableCue> _newTargets;
	/** Cue's already assigned targets. */
	protected final ArrayList<StoppableCue> _oldTargets;

	/**
	 * Creates new command to update the stop targets of a stop cue.
	 * @param cue Stop cue to update.
	 * @param targets New set of target cues to assign to the stop cue.
	 */
	public UpdateStopped(Stop cue, List<StoppableCue> targets)
	{
		_cue = cue;
		_newTargets = new ArrayList<StoppableCue>(targets);
		_oldTargets = _cue._targets;
	}

	@Override public void Apply(Project p)
	{
		_cue._targets = _newTargets;
	}

	@Override public void Revert(Project p)
	{
		_cue._targets = _oldTargets;
	}
}
