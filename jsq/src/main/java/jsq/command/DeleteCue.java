package jsq.command;

import jsq.cue.Cue;
import jsq.cue.PlaySound;
import jsq.project.Project;


/** Command to "delete" a cue. */
public class DeleteCue implements Command
{
	/** Cue to "delete". */
	protected Cue _cue;
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
		_cue = p._cueList.remove(_index);
		if (_cue instanceof PlaySound)
		{
			PlaySound ps = (PlaySound) _cue;
			if (ps._resource != null) p.DisposeResource(ps._resource);
		}
	}

	@Override public void Revert(Project p)
	{
		p._cueList.add(_index, _cue);
		if (_cue instanceof PlaySound)
		{
			PlaySound ps = (PlaySound) _cue;
			if (ps._resource != null) p.UseResource(ps._resource);
		}
	}
}
