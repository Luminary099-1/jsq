package jsq.cue;


/** A cue hose action can be stopped at any time. */
public abstract class StoppableCue extends Cue
{
	/** Stops the cue's action. */
	abstract void Stop();
}
