package jsq.project;

import java.io.File;


/**
 * Renames {@code File} to help distinguish which instances of {@code File} are
 * resources in the project and ensure resources are correctly registered.
 */
public class Resource extends File
{
	/** See the constructor with the same parameters in {@link java.io.File}. */
	Resource(File parent, String child)
	{
		super(parent, child);
	}
}
