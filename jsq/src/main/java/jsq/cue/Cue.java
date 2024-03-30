package jsq.cue;

import java.io.Serializable;


/** Base class to represent cues. */
public abstract class Cue implements Cloneable, Serializable
{
	/** Cue's name. */
	public String _name = "New Cue";
	/** Indicates this cue can be triggered if next in sequence. */
	public boolean _active = true;
	/** Indicates if this cue triggers automatically when the previous ends. */
	public boolean _follows = false;
	/** Notes associated with this cue for convenience. */
	public String _notes;
	// ToDo: Create member to store trigger-action delay.

	/** Creates a new instance of Cue. */
	public Cue()
	{}

	@Override public Cue clone()
	{
		try { return (Cue) super.clone(); }
		catch (CloneNotSupportedException e) { throw new AssertionError(e); }
	}

	/**
	 * Copies the fields defined in {@code Cue} from one instance to another.
	 * @param dest Destinaton of the copy.
	 * @param src Source of the copy.
	 */
	static protected void Copy(Cue dest, Cue src)
	{
		dest._name = src._name;
		dest._active = src._active;
		dest._follows = src._follows;
	}

	/** Triggers the cue's action */
	abstract void Go();

	/**
	 * @return Abbreviated cue's type name.
	 */
	abstract public String ShortTypeName();

	/**
	 * @return Cue's type name.
	 */
	abstract public String TypeName();
}
