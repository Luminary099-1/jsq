package jsq.command;

import java.util.List;

import jsq.project.Project;


/** Command to apply multiple commands of the same type. */
public class BulkCommand<T extends Command> extends Command
{
	/** Stores the commands applied in bulk by this command. */
	protected final List<T> _commands;

	/**
	 * Creates a new bulk command.
	 * @param commands The commands to be applied in the same operation. The
	 * commands will be applied in their order of occurrence in the array and
	 * reverted in the reverse order.
	 */
	public BulkCommand(List<T> commands)
	{
		_commands = commands;
	}

	@Override public void Apply(Project p)
	{
		super.Apply(p);
		for (T command : _commands) command.Apply(p);
	}

	@Override public void Revert(Project p)
	{
		super.Revert(p);
		for (T command : _commands.reversed()) command.Revert(p);
	}
}
