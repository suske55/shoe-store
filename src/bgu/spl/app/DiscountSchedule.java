package bgu.spl.app;

import java.util.logging.Logger;

/**
 * An object which describes a schedule of a single discount that the manager will add to a specific
 * shoe at a specific tick.
 *
 */
public class DiscountSchedule {

	private String fShoeType;
	private int fTick;
	private int fMmount;
	
	public DiscountSchedule(String shoe, int tick, int amount){
		fShoeType=shoe;
		fTick=tick;
		fMmount=amount;
	}
	/**
	 * 
	 * @return the type of the shoe
	 */
	public String getShoe(){
		return fShoeType;
	}
	/**
	 * 
	 * @return the tick at which the discount is published
	 */
	public int getTick(){
		return fTick;
	}
	
	/**
	 * 
	 * @return the amount of shoes on discount
	 */
	public int getAmount(){
		return fMmount;
	}
}
