package bgu.spl.app;


import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.impl.MessageBusImpl;

/**
 * this class extends MicroService and represents a seller that is responsible 
 * for handling the purchase requests received by the customer and 
 * sending restock requests to the management service.
 *
 */  

public class SellingService extends MicroService {

	private int fCurrentTick;
	private CountDownLatch fCount;
	
	public SellingService(String name, int t, CountDownLatch count) {
		super(name);
		fCurrentTick=t;
		fCount=count;
		Logger.getLogger(ShoeStoreRunner.class.getName()).info("selling service " + getName() + " created");
	}

	/**
	 * this method is called once when the event loop starts.
	 * it initializes the subscribing the the relevant messages and 
	 * transferring them via the message bus.
	 * it also describes the actions that occur due to subscribing and sending 
	 * message.
	 */ 
	
	protected void initialize() {
		subscribeBroadcast(TerminateBroadcast.class, terminate->{
			this.terminate();
		});
		subscribeBroadcast(TickBroadcast.class, tickTock->{
			fCurrentTick = tickTock.getTick();
		});
		subscribeRequest(PurchaseOrderRequest.class, req -> {
			BuyResult result = Store.getInstance().take(req.getShoe(), req.getDiscount());
			if(result==BuyResult.REGULAR_PRICE) {
				Receipt r = new Receipt(getName(), req.getName(), req.getShoe(), false, fCurrentTick, req.getTick(), 1);
				Store.getInstance().file(r);
				Logger.getLogger(ShoeStoreRunner.class.getName()).info("seller " + getName() + " sold " + req.getShoe() + " at reqular price");
				complete(req,r);
			}
			else if(result==BuyResult.DISCOUNTED_PRICE){
				Receipt r = new Receipt(getName(), req.getName(), req.getShoe(), true, fCurrentTick, req.getTick(),1);
				Store.getInstance().file(r);
				Logger.getLogger(ShoeStoreRunner.class.getName()).info("seller " + getName() + " sold " + req.getShoe() + " at discounted price");
				complete(req,r);
			}
			else if(result==BuyResult.NOT_ON_DISCOUNT){
				Logger.getLogger(ShoeStoreRunner.class.getName()).info("seller " + getName() + " couldn't sell " + req.getShoe() + " , not on discount");
				complete(req,null);
			}
			else if(result==BuyResult.NOT_IN_STOCK){
				sendRequest(new RestockRequest(req.getShoe()), V->{
					if((Boolean)V){
						Receipt r = new Receipt(getName(), req.getName(), req.getShoe(), false, fCurrentTick, req.getTick(), 1);
						Store.getInstance().file(r);
						Logger.getLogger(ShoeStoreRunner.class.getName()).info("seller " + getName() + " sold " + req.getShoe() + " at reqular price");
						complete(req,r);
					}
					else{
						Logger.getLogger(ShoeStoreRunner.class.getName()).info("seller " + getName() + " couldn't sell " + req.getShoe() + " , not in stock");
						complete(req,null);
					}
				});
			}
		});	
		fCount.countDown();
	}
}
