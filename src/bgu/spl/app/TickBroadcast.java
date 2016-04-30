package bgu.spl.app;

/**
 * An object that represents the current tick that an action has occurred in.
 */
import bgu.spl.mics.Broadcast;

public class TickBroadcast implements Broadcast {

	private int fTick;
	
	public TickBroadcast(int tick){
		fTick=tick;
	}
	
	public int getTick(){
		return fTick;
	}
}
