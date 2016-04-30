package bgu.spl.app;

import java.util.logging.Logger;

/**
 * An object which describes a schedule of a single client-purchase at a specific tick.
 *
 */
public class PurchaseSchedule {

	private String fShoeType;
	private int fTick;

	public PurchaseSchedule(String s, int tick){
		fShoeType=s;
		fTick=tick;
	}

	/**
	 * 
	 * @return the type of shoe to purchase
	 */
	public String getShoe(){
		return fShoeType;
	}
	/**
	 * 
	 * @return the tick at which the purchase of a shoe is made.
	 */

	public int getTick(){
		return fTick;
	}
}
