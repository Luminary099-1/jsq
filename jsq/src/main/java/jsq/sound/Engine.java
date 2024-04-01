package jsq.sound;

import java.nio.IntBuffer;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC10;

import jsq.project.Resource;


/** Class to manage the sound subsystem for the application. */
public class Engine
{
	/** Number of available OpenAL sources. */
	final static int _numSources = 128;
	/** Number of available OpenAL buffers. */
	final static int _numBuffers = 96;

	/** Indicates whether the sound system has been intialized. */
	static boolean _initialzed = false;
	/** OpenAL sound playback device. */
	static long _deviceID;
	/** OpenAL context. */
	static long _contextID;
	/** OpenAL buffer IDs. */
	static int _buffers[];
	/** OpenAL source IDs. */
	static int _sources[];
	/** Stores the buffers not currently used by the application. */
	static Stack<Integer> _freeBuffers;
	/** Stores the sources not currently used by the application. */
	static Stack<Integer> _freeSources;
	/** Maps resources to the IDs of buffers already loaded with their data. */
	static Map<Resource, Integer> _resources;
	/** Maps buffer IDs to the number of sound handles using them. */
	static Map<Integer, Integer> _bufferUses;

	/**
	 * Initializes the OpenAL context with the default output device. The number
	 * of OpenAL buffers and sources specified by {@code _numBuffers} and
	 * {@code _numSources} respectively are also created.
	 * @return {@code true} if the initialization was successful; {@code false}
	 * otherwise.
	 */
	public static boolean Initialize()
	// ToDo: Set up the listener as necessary.
	{
		if (_initialzed) return false;

		_deviceID = ALC10.alcOpenDevice((CharSequence) null);
		if (_deviceID == 0) return false;

		_contextID = ALC10.alcCreateContext(_deviceID, (IntBuffer) null);
		if (AL10.alGetError() != ALC10.ALC_NO_ERROR) return false;

		ALC10.alcMakeContextCurrent(_contextID);
		AL10.alGetError();

		_buffers = new int[_numSources];
		AL10.alGenBuffers(_buffers);
		if (AL10.alGetError() != ALC10.ALC_NO_ERROR) return false;

		_sources = new int[_numBuffers];
		AL10.alGenSources(_sources);
		if (AL10.alGetError() != ALC10.ALC_NO_ERROR) return false;
		
		for (int i : _buffers) _freeBuffers.push(i);
		for (int i : _sources) _freeSources.push(i);
		_bufferUses = new TreeMap<>();

		return _initialzed = true;
	}

	// ToDo: Method to get list of available playback devices.
	// ToDo: Initialization method that uses a specified playback device.
	
	/**
	 * Prepares an OpenAL source to play the sound specified by the resource. If
	 * necessary, the data is loaded into a buffer first.
	 * @param resource Sound resource to be prepared to be played.
	 * @return An OpenAL source wrapped in a SoundHandle.
	 */
	public SoundHandle ObtainSound(Resource resource)
	{
		// Create or reuse a buffer for the sound data:
		Integer buffer = _resources.get(resource);
		boolean extant_buffer = false;
		if (buffer == null)
		{
			buffer = _freeBuffers.peek().intValue();
			// ToDo: Load the sound data into the buffer and check for errors.
		}
		else extant_buffer = true;

		// Create a source for the handle:
		int source = _freeSources.peek();
		SoundHandle handle = new SoundHandle(resource, source, buffer);
		AL10.alSourcei(source, AL10.AL_BUFFER, buffer);
		if (AL10.alGetError() == ALC10.ALC_NO_ERROR)
			throw new IllegalArgumentException();
		
		// Commit the allocation after successfully interacting with OpenAL:
		if (!extant_buffer)
		{
			_freeBuffers.pop();
			_resources.put(resource, buffer);
			_bufferUses.put(buffer, 1);
		}
		else _bufferUses.compute(buffer, (k, v) -> { return ++ v; });
		_freeSources.pop();
		return handle;
	}

	/**
	 * Frees the OpenAL source associated with the passed handle. The handle is
	 * itself invalidated and can no longer be used.
	 * @param handle The handle to dispose.
	 */
	public void ReleaseSound(SoundHandle handle)
	// ToDo: Determine if not clearing data/assignments causes any issues.
	{
		handle._valid = false;
		int uses
			= _bufferUses.compute(handle._buffer, (k, v) -> { return -- v; });
		if (uses == 0)
		{
			_freeBuffers.push(handle._buffer);
			_resources.remove(handle._resource);
			_bufferUses.remove(handle._buffer);
		}

		_freeSources.push(handle._source);
	}

	/** Cleans up the OpenAL context. */
	public static void Cleanup()
	// ToDo: Clean up the listener as necessary.
	{
		if (!_initialzed) return;

		_bufferUses.clear();
		_freeBuffers.clear();
		_freeSources.clear();

		AL10.alDeleteSources(_sources);
		AL10.alDeleteBuffers(_buffers);
		ALC10.alcMakeContextCurrent(0);
		ALC10.alcDestroyContext(_contextID);
		ALC10.alcCloseDevice(_deviceID);

		_initialzed = false;
	}
}
