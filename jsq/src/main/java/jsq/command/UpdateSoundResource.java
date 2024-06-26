package jsq.command;

import jsq.cue.PlaySound;
import jsq.project.Project;
import jsq.project.Resource;


/** Command to update a sound cue's sound resource. */
public class UpdateSoundResource extends Command
{
	/** Cue to update. */
	protected final PlaySound _cue;
	/** New sound resource to assign to the cue. */
	protected final Resource _newResource;
	/** Cue's already assigned sound resource. */
	protected final Resource _oldResource;

	/**
	 * Creates a new command to update a sound cue's sound resource.
	 * @param cue PlaySound cue to update.
	 * @param new_resource New sound resource to assign to the cue.
	 */
	public UpdateSoundResource(PlaySound cue, Resource new_resource)
	{
		_cue = cue;
		_newResource = new_resource;
		_oldResource = _cue._resource;
	}

	@Override public void Apply(Project p)
	{
		super.Apply(p);
		if (_oldResource != null) p.DisposeResource(_oldResource);
		p.UseResource(_newResource);
		_cue._resource = _newResource;
	}

	@Override public void Revert(Project p)
	{
		super.Revert(p);
		if (_oldResource != null) p.UseResource(_oldResource);
		p.DisposeResource(_newResource);
		_cue._resource = _oldResource;
	}
}
