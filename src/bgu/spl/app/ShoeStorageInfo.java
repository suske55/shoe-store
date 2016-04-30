package bgu.spl.app;

import java.util.logging.Logger;

/**
 * An object which represents information about a single type of shoe in the store
 *    
 *
 */
public class ShoeStorageInfo {

	private String fShoeType;
	private int fAmountOnStorage;
	private int fDiscountedAmount;

	public ShoeStorageInfo(String type, int amount, int discount){
		fShoeType = type;
		fAmountOnStorage = amount;
		fDiscountedAmount = discount;
	}

	/**
	 * 
	 * @return the type of shoe 
	 */
	public String getType(){
		return fShoeType;
	}

	/**
	 * 
	 * @return the amount of shoes of this type in the store
	 */
	public int getAmount(){
		return fAmountOnStorage;
	}
	/**
	 * 
	 * @param update the amount of shoes to add to the store stock
	 */
	public void addAmount(int update){
		fAmountOnStorage = fAmountOnStorage + update;
	}

	/**
	 * 
	 * @return the amount of shoes of this type that can be purchased on discount
	 */
	public int getDiscount(){
		return fDiscountedAmount;
	}
	/**
	 * 
	 * @param update the updated number shoes on discount
	 */
	public void addDiscount(int update){
		fDiscountedAmount = fDiscountedAmount + update;
		if (fDiscountedAmount > fAmountOnStorage){
			fDiscountedAmount = fAmountOnStorage;
		}
		
	}
	public void print(){
		System.out.println("        ShoeType: " + fShoeType);
		System.out.println("        Amount in storage: " + fAmountOnStorage);
		System.out.println("        Discounted amount: " + fDiscountedAmount);
		System.out.println();
	}
}
