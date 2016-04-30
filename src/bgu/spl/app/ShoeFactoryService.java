package bgu.spl.app;

import bgu.spl.mics.MicroService;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;
import java.util.HashMap;

/**
 * this class extends MicroService and represents a factory that is responsible 
 * for handling manufacturing requests and purchase requests 
 *
 */ 

public class ShoeFactoryService extends MicroService{

	private int fCurrentTick;
	private int fNumOfShoes;
	private LinkedBlockingQueue<ManufacturingOrderRequest> fOrders;
	private ManufacturingOrderRequest fMr;
	private CountDownLatch fCount;


	public ShoeFactoryService(String name, int tick, CountDownLatch count){
		super(name);
		fCurrentTick=tick;
		fNumOfShoes=0;
		fOrders = new LinkedBlockingQueue();
		fMr=null;
		fCount=count;
		Logger.getLogger(ShoeStoreRunner.class.getName()).info("factory " + name + " created");
	}

	/**
	 * this method is called once when the event loop starts.
	 * it initializes the subscribing the the relevant messages and 
	 * transferring them via the message bus.
	 * it also describes the actions that occur due to subscribing and sending 
	 * message.
	 */

	@Override
	protected void initialize() {
		subscribeBroadcast(TerminateBroadcast.class, terminate->{
			this.terminate();
		});
		subscribeRequest(ManufacturingOrderRequest.class, man->{
			try {
				fOrders.put(man);
			} catch (Exception e) {
			}
			Logger.getLogger(ShoeStoreRunner.class.getName()).info("factory " + getName() + " got a manufacturing request of " + man.getAmount() + " " + man.getShoe());
			if(fMr == null){
				if(!(fOrders.isEmpty())){
					fMr = fOrders.remove();
				}
			}
		});
		subscribeBroadcast(TickBroadcast.class, tickTock->{
			fCurrentTick = tickTock.getTick();
			if(fMr == null){
				if(!(fOrders.isEmpty())){
					fMr = fOrders.poll();
				}
			}
			else if(fNumOfShoes==fMr.getAmount()){
				Logger.getLogger(ShoeStoreRunner.class.getName()).info("factory " + getName()+ " completed manufacturing request for " + fMr.getAmount() + " " + fMr.getShoe());
				complete(fMr, new Receipt("factory "+ this.getName(), "Store", fMr.getShoe(), false, fCurrentTick, fMr.getTick(), fNumOfShoes));
				fMr = null;
				fNumOfShoes=0;
				if(!(fOrders.isEmpty())){
					fMr=fOrders.poll();
					fNumOfShoes++;
				}
			}
			else{
				fNumOfShoes++;
			}
		});
		fCount.countDown();
	}
}
