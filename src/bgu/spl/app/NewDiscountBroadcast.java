package bgu.spl.app;

import bgu.spl.mics.Broadcast;

/**
 * this class implements the Broadcast interface and describes
 * a discount broadcast on a certain shoe sent by the management service.
 *
 */
public class NewDiscountBroadcast implements Broadcast {

	private String fShoe;

	public NewDiscountBroadcast(String shoe){
		fShoe=shoe;
	}
	/**
	 * 
	 * @return the type of shoe on discount
	 */
	public String getShoe(){
		return fShoe;
	}
}
