package bgu.spl.app;

/**
 * An object representing a receipt that should be sent to a client after buying a shoe (when the client�s
 * PurchaseRequest completed).
 *
 */
public class Receipt {

	private String fSeller;
	private String fCustomer;
	private String fShoeType;
	private boolean fDiscount;
	private int fIssuedTick;
	private int fRequestTick;
	private int fAmountSold;

	/**
	 * 
	 * @param s the name of the seller
	 * @param c the name of the customer
	 * @param sh the type of shoe supplied
	 * @param d determines whether or not to shoe is on discount
	 * @param i the tick of when the request was sent  
	 * @param r the tick of when the request was received  
	 * @param a the amount of shoes sold
	 */
	public Receipt(String seller, String customer, String shoe, boolean discount, int issued, int request, int amount){
		fSeller=seller;
		fCustomer=customer;
		fShoeType=shoe;
		fDiscount=discount;
		fIssuedTick=issued;
		fRequestTick=request;
		fAmountSold=amount;
	}

	public void print(){
		System.out.println("        Seller: " + fSeller);
		System.out.println("        Customer: " + fCustomer);
		System.out.println("        Shoe type: " + fShoeType);
		System.out.println("        Discount: " + fDiscount);
		System.out.println("        Issued tick: " + fIssuedTick);
		System.out.println("        Request tick: " + fRequestTick);
		System.out.println("        Amount sold: " + fAmountSold);
		System.out.println();
	}

	public int getRequestTick(){
		return fRequestTick;
	}
}
