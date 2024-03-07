package jsq.command;

import java.util.ArrayList;
import java.util.List;

import jsq.Project;
import jsq.cue.Stop;
import jsq.cue.StoppableCue;


/** Command to update a stop cue's targets. */
public class UpdatedStopped implements Command
{
	/** Cue to update. */
	Stop _cue;
	/** New targets to assign to the cue. */
	ArrayList<StoppableCue> _newTargets;
	/** The cue's already assigned targets. */
	ArrayList<StoppableCue> _oldTargets;

	/**
	 * Creates new command to update the stop targets of a stop cue.
	 * @param cue Stop cue to update.
	 * @param targets New set of target cues to assign to the stop cue.
	 */
	public UpdatedStopped(Stop cue, List<StoppableCue> targets)
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
