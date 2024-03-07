package jsq.command;

import jsq.Project;


/** Base type to implement the command design pattern to manipulate cues. */
public interface Command
{
	// ToDo: Refactor comand creation to be returned by static methods in the affected cues. Cue fields could then be private.
	// ToDo: Consider throwing exceptions when Apply or Revert are called out of turn.
	
	/**
	 * Applies the prescibed change to the project.
	 * @param p The instance of Project to apply the operation to.
	 */
	void Apply(Project p);

	/**
	 * Revers the prescibed change that's already been applied to the project.
	 * @param p The instance of Project to revert the applied operation on.
	 */
	void Revert(Project p);
}
