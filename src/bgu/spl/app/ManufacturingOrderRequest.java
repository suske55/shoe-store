package bgu.spl.app;

import bgu.spl.mics.Request;

/**
 * this class implements the Request interface and describes a manufacturing order
 * that is sent to a factory of shoes.
 *
 */

public class ManufacturingOrderRequest implements Request {

	private String fShoe;
	private int fAmount;
	private int fTick;

	public ManufacturingOrderRequest(String s, int amount, int tick){
		fShoe=s;
		fAmount=amount;
		fTick=tick;
	}

	/**
	 * 
	 * @return the type of shoe to manufacture
	 */
	public String getShoe(){
		return fShoe;
	}

	/**
	 * 
	 * @return the amount of shoes to manufacture
	 */
	public int getAmount(){
		return fAmount;
	}
	/**
	 * 
	 * @return the tick at which the manufacture request was sent
	 */
	public int getTick(){
		return fTick;
	}
}
