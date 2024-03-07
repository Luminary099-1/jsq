package jsq.cue;

import java.util.ArrayList;


/** A cue that stops the actions of other cues when triggered. */
public class Stop extends Cue
{
	/** The cues that will be stopped when this cue is triggered. */
	public ArrayList<StoppableCue> _targets = new ArrayList<StoppableCue>();

	/** Creates a new instance of Stop without any targets. */
	public Stop()
	{}

	/**
	 * Creates a new instance of Stop.
	 * @param targets The cues that will be stopped when this cue is triggered.
	 */
	public Stop(ArrayList<StoppableCue> targets)
	{
		_targets = targets;
	}

	@Override public Stop clone()
	{
		Stop dup = (Stop) super.clone();
		Cue.Copy(dup, this);
		dup._targets = _targets;
		return dup;
	}
	
	@Override void Go()
	{
		for (StoppableCue c : _targets) c.Stop();
	}

	@Override public String ShortTypeName()
	{
		return "Stop";
	}

	@Override public String TypeName()
	{
		return "Stop Cue";
	}
}
