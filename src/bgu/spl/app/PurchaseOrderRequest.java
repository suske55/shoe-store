package bgu.spl.app;

import bgu.spl.mics.Request;

/**
 * this class implements the Request interface and describes a Purchase order
 * that is sent to the management service by a selling service.
 *
 */

public class PurchaseOrderRequest implements Request{

	private String fShoe;
	private String fRequesterName;
	private boolean fDiscount;
	private int fTick;

	public PurchaseOrderRequest(String s, String name, boolean discount, int tick){
		fShoe=s;
		fRequesterName=name;
		fDiscount=discount;
		fTick=tick;
	}

	/**
	 * 
	 * @return the name of the requester of the shoe
	 */
	public String getName(){
		return fRequesterName;
	}

	/**
	 * 
	 * @return the type of shoe to purchase
	 */
	public String getShoe(){
		return fShoe;
	}

	/**
	 * 
	 * @return a boolean that indicates if the requester wishes to purchase the shoe only if it's on discount
	 */
	public boolean getDiscount(){
		return fDiscount;
	}
	/**
	 * 
	 * @return the tick at which the purchase request was sent
	 */
	public int getTick(){
		return fTick;
	}

}
