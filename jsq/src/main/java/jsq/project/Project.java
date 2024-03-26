package jsq.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import jsq.Context;
import jsq.cue.Cue;


/** Represents the project, containing cues and related settings. */
public class Project
{
	/** Stores the project's cue list. */ // ToDo: Make this protected.
	public ObservableList<Cue> _cueList = FXCollections.observableArrayList();
	/** Maps the location of resources in the project to their metadata. */
	static Map<Resource, Metadata> _resources = new HashMap<>();

	/**
	 * Clears this project's contents without invalidating relationships with
	 * observers.
	 */
	public void Clear()
	{
		_cueList.clear();
		_resources.clear();
	}

	// ToDo: Determine if the methods below fail to preserve "shared" instances.

	/**
	 * Essentially implements writeObject() to override the serialization
	 * behavior for this class, but outside of the serialization system to
	 * avoid damaging observation relationships.
	 * @param out Stream to write this instance to.
	 * @throws IOException If serialization fails for any reason.
	 */
	public void WriteObject(ObjectOutputStream out) throws IOException
	{
		out.writeInt(_cueList.size());
		for (Cue cue : _cueList) out.writeObject(cue);

		HashMap<Resource, Metadata> used_resources = new HashMap<>();
		_resources.forEach(
			(v, k) -> { if (k._uses != 0) used_resources.put(v, k); }
		);
		out.writeObject(used_resources);
	}

	/**
	 * Essentially implements readObject() to override the serialization
	 * behavior for this class, but outside of the serialization system to
	 * avoid damaging observation relationships.
	 * @param in Stream to read an instace from.
	 * @throws ClassNotFoundException If the stream contains a class not defined
	 * in the current JVM environment.
	 * @throws IOException If the deserialization fails for any other reason.
	 */
	@SuppressWarnings("unchecked") public void ReadObject(ObjectInputStream in)
		throws ClassNotFoundException, IOException
	{
		int size = in.readInt();
		_cueList.clear();
		for (int i = 0; i < size; ++ i) _cueList.add((Cue) in.readObject());
		
		_resources = (HashMap<Resource, Metadata>) in.readObject();
	}

	/**
	 * Declares a new usage of a resource in the project. The resource is
	 * copied to the project directory if it was not previously registered.
	 * @param source File to introduce as a resource to the project.
	 * @return The newly incorporated resource.
	 */
	public Resource RegisterResource(File source)
	// ToDo: Cache paths imported during the session to avoid hashing twice.
	{
		try (FileInputStream is = new FileInputStream(source))
		{
			String file_str = String.format(
				"resources/%s.%s",
				DigestUtils.md5Hex(is),
				FilenameUtils.getExtension(source.toString())
			);
			Resource resource = new Resource(Context._folder, file_str);
			Metadata data = new Metadata(source.getName());
			if (!_resources.containsKey(resource))
			{
				Files.copy(source.toPath(), resource.toPath());
				_resources.put(resource, data);
			}
			return resource;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Increments the reference count for the specified resource.
	 * @param source Resource being used.
	 */
	public void UseResource(Resource resource)
	{
		_resources.compute(resource,
			(k, v) -> {
				++ v._uses;
				return v;
			}
		);
	}

	/**
	 * Decrements the reference count for the specified resource.
	 * @param resource Resource being disposed.
	 */
	public void DisposeResource(Resource resource)
	{
		_resources.compute(resource,
			(k, v) -> {
				-- v._uses;
				return v;
			}
		);
	}

	/**
	 * Returns the original filename of the specified resource.
	 * @param resource Resource to name.
	 * @return Resource's filename. If no such resource exists, {@code null} is
	 * returned.
	 */
	public String GetResourceName(Resource resource)
	{
		Metadata data = _resources.get(resource);
		return (data == null) ? null : data._name;
	}

	/**
	 * Deletes all unused resources from the project directory and removes them
	 * from the project. All resources which have reference counts of 0 are
	 * considered unused.
	 */
	public void CullUnusedResources()
	{
		Iterator<Entry<Resource, Metadata>> it
			= _resources.entrySet().iterator();
		for (; it.hasNext();)
		{
			Entry<Resource, Metadata> m = it.next();
			if (m.getValue()._uses == 0)
			{
				try { Files.delete(m.getKey().toPath()); }
				catch (IOException e) { e.printStackTrace(); }
				it.remove();
			}
		}
	}
}


/** Stores some metadata about a resource in the project. */
class Metadata implements Serializable
{
	/** The number of dependencies on this resource. */
	int _uses = 0;
	/** The resource's original filename. */
	final String _name;

	/**
	 * Creates a new instance of a resource with {@code _uses} set to 0.
	 * @param name The resource's original filename.
	 */
	Metadata(String name)
	{
		_name = name;
	}
}
