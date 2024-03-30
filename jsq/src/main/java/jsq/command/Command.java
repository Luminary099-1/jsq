package jsq.command;

import jsq.project.Project;


/** Base type to implement the command design pattern to manipulate cues. */
public abstract class Command
// ToDo: Consider making command creation a series of statis methods in cues. This would allow cue fields to be hidden.
{
	/** Indicates whether the command has been applied last. */
	protected boolean _applied = false;
	
	/**
	 * Applies the prescibed change to the project. All overriding
	 * implementations should call super.
	 * @param p The instance of Project to apply the operation to.
	 */
	public void Apply(Project p)
	{
		assert !_applied : "Command applied out of turn.";
		_applied = true;
	}

	/**
	 * Revers the prescibed change that's already been applied to the project.
	 * All overriding implementations should call super.
	 * @param p The instance of Project to revert the applied operation on.
	 */
	public void Revert(Project p)
	{
		assert _applied : "Command reverted out of turn.";
		_applied = false;
	}
}
