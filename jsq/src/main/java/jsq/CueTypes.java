package jsq;

import jsq.cue.Cue;
import jsq.cue.PlaySound;
import jsq.cue.Stop;


/** Enumerates the concrete cue types. */
public enum CueTypes
{
	/** {@link jsq.cue.PlaySound} */
	PLAYSOUND ("Play Sound"),
	/** {@link jsq.cue.Stop} */
	STOP ("Stop Cue");

	/** Stores the user-facing name of the cue type. */
	String _name;

	/**
	 * Instantiates a variant of the enum with the prescribed name.
	 * @param name User-facing name describing this instances variant.
	 */
	private CueTypes(String name)
	{
		_name = name;
	}

	/**
	 * @return String containing the user-facing name of this cue type.
	 */
	public String GetName()
	{
		return _name;
	}

	/**
	 * @return A new instance of the concrete cue expressed by this. If this
	 * variant represents in abstract type, for any reason, null is returned.
	 */
	public Cue CreateCue()
	{
		switch (this)
		{
			case PLAYSOUND:	return new PlaySound();
			case STOP:		return new Stop();
			default:		return null;
		}
	}
}
