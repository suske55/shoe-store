package bgu.spl.app;

import bgu.spl.mics.Request;

/**
 * request that is sent by the selling service to the store manager so that he
 * will know that he need to order new shoes from a factory.
 *
 */
public class RestockRequest implements Request{

	private String fShoe;

	public RestockRequest(String s){
		fShoe=s;
	}

	/**
	 * 
	 * @return the type of shoe to order
	 */
	public String getShoe(){
		return fShoe;
	}	
}
