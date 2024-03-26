package jsq.cue;

import jsq.project.Resource;


/** A cue that plays a sound. */
public class PlaySound extends StoppableCue
{
	/** Sound resource to be played by this cue when triggered. */
	public Resource _resource = null;

	// ToDo: Member variable to store the cue's volume?
	// ToDo: Member variables to store loops in the sound clip? Just a bool?

	@Override public PlaySound clone()
	{
		PlaySound dup = (PlaySound) super.clone();
		Cue.Copy(dup, this);
		dup._resource = _resource;
		return dup;
	}

	@Override void Go()
	{
		// ToDo:
		throw new UnsupportedOperationException("Unimplemented method 'Start'");
	}

	@Override void Stop()
	{
		// ToDo:
		throw new UnsupportedOperationException("Unimplemented method 'Stop'");
	}

	@Override public String ShortTypeName()
	{
		return "Play";
	}

	@Override public String TypeName()
	{
		return "Play Sound";
	}
}
