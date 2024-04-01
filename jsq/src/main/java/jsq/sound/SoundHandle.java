package jsq.sound;

import org.lwjgl.openal.AL10;

import jsq.project.Resource;


/** Class to wrap an OpenAL source for immediate playback. */
public class SoundHandle implements Comparable<SoundHandle>
// ToDo: Determine if this should throw errors when play/stop fails?
{
	/** Resource this handle was loaded from. */
	final Resource _resource;
	/** OpenAL buffer ID associated with this handle. */
	final int _buffer;
	/** penAL source ID associated with this handle. */
	final int _source;
	/** Indicates whether this handle has not yet had its resources released. */
	boolean _valid = true;;

	/**
	 * Creates a new sound handle.
	 * @param resource Resource this handle will play.
	 * @param buffer OpenAL buffer ID.
	 * @param source OpenAL source ID.
	 */
	SoundHandle(Resource resource, int buffer, int source)
	{
		_resource = resource;
		_buffer = buffer;
		_source = source;
	}

	/** Plays the sound associated with the handle. */
	public void Play()
	{
		if (!_valid)
			throw new AssertionError("Sound handle has already been released.");
		AL10.alSourcePlay(_source);
		AL10.alGetError();
	}

	/** Stops the playback of the sound associated with this handle. */
	public void Stop()
	{
		if (!_valid)
			throw new AssertionError("Sound handle has already been released.");
		AL10.alSourceStop(_source);
		AL10.alGetError();
	}

	@Override public int compareTo(SoundHandle o)
	{
		return (_buffer > o._buffer)
			? Integer.signum(_source - o._source)
			: -1;
	}
}
