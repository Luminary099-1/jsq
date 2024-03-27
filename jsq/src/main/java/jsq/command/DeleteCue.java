package jsq.command;

import jsq.cue.Cue;
import jsq.cue.PlaySound;
import jsq.project.Project;
import jsq.project.Resource;


/** Command to "delete" a cue. */
public class DeleteCue implements Command
{
	/** Cue to "delete". */
	protected final Cue _cue;
	/** Index of the "deleted" cue. */
	protected final int _index;
	/** Resource held by the cue, if applicable. */
	protected final Resource _resource;

	/**
	 * Creates and instance of DeleteCue.
	 * @param index Index of the cue to be deleted.
	 * @param cue Cue to delete.
	 */
	public DeleteCue(int index, Cue cue)
	{
		_index = index;
		_cue = cue;
		if (_cue instanceof PlaySound) _resource = ((PlaySound) cue)._resource;
		else _resource = null;
	}

	@Override public void Apply(Project p)
	{
		p._cueList.remove(_index);
		if (_resource != null) p.DisposeResource(_resource);
	}

	@Override public void Revert(Project p)
	{
		p._cueList.add(_index, _cue);
		if (_resource != null) p.UseResource(_resource);
	}
}
