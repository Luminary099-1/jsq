package jsq;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import jsq.cue.Cue;


/** Represents the project, containing cues and related settings. */
public class Project
{
	/** Stores the project's cue list. */
	public ObservableList<Cue> _cueList = FXCollections.observableArrayList();

	/**
	 * Clears this project's contents without invalidating relationships with
	 * observers.
	 */
	public void Clear()
	{
		_cueList.clear();
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
	public void ReadObject(ObjectInputStream in)
		throws ClassNotFoundException, IOException
	{
		int size = in.readInt();
		_cueList.clear();
		for (int i = 0; i < size; ++ i) _cueList.add((Cue) in.readObject());
	}
}
