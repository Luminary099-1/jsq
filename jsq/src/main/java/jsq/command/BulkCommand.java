package jsq.command;

import jsq.project.Project;


/** Command to apply multiple commands of the same type. */
public class BulkCommand<T extends Command> implements Command
// ToDo: Consider refactoring this to use array lists instead of arrays.
{
	/** Stores the commands applied in bulk by this command. */
	protected final T[] _commands;

	/**
	 * Creates a new bulk command.
	 * @param commands The commands to be applied in the same operation. The
	 * commands will be applied in their order of occurrence in the array and
	 * reverted in the reverse order.
	 */
	public BulkCommand(T[] commands)
	{
		_commands = commands;
	}

	@Override public void Apply(Project p)
	{
		for (T command : _commands) command.Apply(p);
	}

	@Override public void Revert(Project p)
	{
		for (int i = _commands.length - 1; i >= 0; -- i) _commands[i].Revert(p);
	}
}
