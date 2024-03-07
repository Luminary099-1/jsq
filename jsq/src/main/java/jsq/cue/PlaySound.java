package jsq.cue;


/** A cue that plays a sound. */
public class PlaySound extends StoppableCue
{
	// ToDo: Member variable to store the cue's volume.
	// ToDo: Member variable to refer to the sound data. Ideally, the media will be stored in the project.
	// ToDo: Member variables to store loops in the sound clip? Just a bool?

	@Override public PlaySound clone()
	{
		PlaySound dup = (PlaySound) super.clone();
		Cue.Copy(dup, this);
		return dup;
	}

	@Override void Go()
	{
		throw new UnsupportedOperationException("Unimplemented method 'Start'");
	}

	@Override void Stop()
	{
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
