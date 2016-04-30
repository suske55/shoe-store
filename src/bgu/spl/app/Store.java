package bgu.spl.app;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The store object is a Singleton which that represents a passive object
 * that holds a collection of ShoeStorageInfo.
 */
public class Store {

	private static class StoreHolder{
		private static Store instance = new Store();
	}

	public static Store getInstance(){
		return StoreHolder.instance;
	}

	/**
	 * this class has a map sorted by shoe type with the matching storage info.
	 * it keeps a list of receipts filed in the store.
	 * it also keeps an enum.
	 */
	private HashMap<String, ShoeStorageInfo> fShoes;
	private ArrayList<Receipt> fReceipts;
	private BuyResult fB; 
	private Object fTakeLock;
	private Object fFileLock;

	public Store(){
		fShoes = new HashMap<String, ShoeStorageInfo>();
		fReceipts = new ArrayList();
		fFileLock = new Object();
		fTakeLock = new Object();
	}
	/**
	 * 
	 * @param storage the initial storage information of the store stock
	 */
	public void load(ShoeStorageInfo[] storage){
		for(int i=0; i<storage.length; i++){
			fShoes.put(storage[i].getType(), storage[i]);
		}
	}

	/**
	 * 
	 * @param shoeType the shoe type that the client wishes to buy.
	 * @param onlyDiscount whether or not the client wants to purchase the shoe only if its on discount.
	 * @return enum that states the result of the purchase.
	 */
	public BuyResult take (String shoeType , boolean onlyDiscount){
		synchronized(fTakeLock){
			ShoeStorageInfo tmp = fShoes.get(shoeType);
			if (tmp == null){
				tmp = new ShoeStorageInfo(shoeType,0,0);
				fShoes.put(shoeType, tmp);
				
			}
			if(tmp.getAmount()==0){
				if(!onlyDiscount){
					return fB.NOT_IN_STOCK;
				}
				else{
					return fB.NOT_ON_DISCOUNT;
				}
			}
			else if(!(onlyDiscount)){
				if(tmp.getDiscount()==0){
					tmp.addAmount(-1);
					return fB.REGULAR_PRICE;
				}
				else{
					tmp.addAmount(-1);
					tmp.addDiscount(-1);
					return fB.DISCOUNTED_PRICE;
				}
			}
			else{
				if(tmp.getDiscount()==0){
					return fB.NOT_ON_DISCOUNT;
				}
				else{
					tmp.addAmount(-1);
					tmp.addDiscount(-1);
					return fB.DISCOUNTED_PRICE;
				}
			}
		}
	}

	/**
	 * 
	 * @param shoeType the shoe type that is added to the store stock. 
	 * @param amount the amount of shoes to add.
	 */
	public void add (String shoeType , int amount){
		fShoes.get(shoeType).addAmount(amount);		
	}

	/**
	 * 
	 * @param shoeType the shoe type that its discount amount is being updated. 
	 * @param amount the amount of shoes to add.
	 */
	public void addDiscount (String shoeType , int amount){
		if (!(fShoes.containsKey(shoeType))){
			fShoes.put(shoeType, new ShoeStorageInfo(shoeType,0,0));
		}
		fShoes.get(shoeType).addDiscount(amount);		
	}

	/**
	 * 
	 * @param receipt the receipts received after a request is completed.
	 */
	public void file(Receipt receipt){
		synchronized(fFileLock){
			fReceipts.add(receipt);
		}
	}
	public void print(){
		System.out.println("+++++++++++++++++++ Storage +++++++++++++++++++" );
		System.out.println();
		int i=1;
		for(ShoeStorageInfo entry : fShoes.values()){
			System.out.println("Shoe type no. " + i);
			entry.print();
			i++;
		}
		System.out.println("+++++++++++++++++++ Receipts +++++++++++++++++++" );
		System.out.println();
		i=1;
		for(int j=0; j<fReceipts.size(); j++){
			System.out.println("Receipt no. " + i);
			fReceipts.get(j).print();
			i++;
		}
	}

}

