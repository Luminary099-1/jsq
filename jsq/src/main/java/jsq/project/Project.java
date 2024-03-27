package jsq.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import jsq.Context;
import jsq.cue.Cue;


/** Represents the project, containing cues and related settings. */
public class Project
// FixMe: Refactor to hide the cue list and make it a parameter for construction.
{
	/** Stores the project's cue list. */
	public ObservableList<Cue> _cueList = FXCollections.observableArrayList();
	/** Maps the location of resources in the project to their metadata. */
	protected Map<Resource, Metadata> _resources = new HashMap<>();
	/** Maps source files to cache data to avoid hashing duplicate resources. */
	protected Map<File, CacheData> _importCache = new HashMap<>();

	/**
	 * Clears this project's contents without invalidating relationships with
	 * observers.
	 */
	public void Clear()
	{
		_cueList.clear();
		_resources.clear();
		_importCache.clear();
	}

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
		_cueList.clear();
		int size = in.readInt();
		while (-- size >= 0) _cueList.add((Cue) in.readObject());
		
		_resources = (HashMap<Resource, Metadata>) in.readObject();
	}

	/**
	 * Declares a new usage of a resource in the project. The resource is
	 * copied to the project directory if it was not previously registered.
	 * @param source File to introduce as a resource to the project.
	 * @return The newly incorporated resource.
	 */
	public Resource RegisterResource(File source)
	{
		// Look up the source in the cache first and return it if found:
		CacheData cd = _importCache.get(source);
		if (cd != null && cd.TestMatch(source)) return cd._resource;
	
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
				// Cache the resource for future imports of the same file:
				_importCache.put(source, new CacheData(source, resource));
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
		var it = _resources.entrySet().iterator();
		for (; it.hasNext();)
		{
			var m = it.next();
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


/** Stores information about a imported resources for caching purposes. */
class CacheData
{
	/** Last time the file was modified. */
	final long _lastModified;
	/** Length of the file. */
	final long _length;
	/** File's resource in the project. */
	final Resource _resource;

	/**
	 * Creates a new cache object for importing resources into the project.
	 * @param source Source file of a newly created resource.
	 * @param resource Resource created from the source file.
	 */
	CacheData(File source, Resource resource)
	{
		_lastModified = source.lastModified();
		_length = source.length();
		_resource = resource;
	}

	/**
	 * Applies a heuristic to determine if the specified source matches the file
	 * specified by this cache.
	 * @param source File to test for a match.
	 * @return {@code true} if source is already a resource; {@code false}
	 * otherwise.
	 */
	boolean TestMatch(File source)
	{
		return source.lastModified() == _lastModified
			&& source.length() == _length;
	}
}
