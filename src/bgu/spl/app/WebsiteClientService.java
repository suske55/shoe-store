package bgu.spl.app;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import bgu.spl.mics.MicroService;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * This micro-service describes one client connected to the web-site.	
 * it is responsible for sending purchase requests.
 *
 */

public class WebsiteClientService extends MicroService {

	/**
	 * this object keeps a map sorted by ticks and the purchase the client needs to make
	 * at this tick.
	 * it also keep a set of shoes the client wishes to purchase if and when a shoe is on discount 
	 */
	private Map<Integer, LinkedList<String>> fSchedule;
	private Set<String> fWishList;
	private int fCurrentTick;
	private CountDownLatch fCount;

	public WebsiteClientService(String name, List<PurchaseSchedule> purchaseSchedule,Set<String> wishList, int tick, CountDownLatch count) {
		super(name);
		fCurrentTick=tick;
		this.fWishList = wishList;
		fSchedule = new HashMap<>();
		fCount=count;
		for(int i=0; i<purchaseSchedule.size(); i++){
			LinkedList<String> tmp = this.fSchedule.get(purchaseSchedule.get(i).getTick());
			if(tmp == null){
				tmp = new LinkedList();
				fSchedule.put(purchaseSchedule.get(i).getTick(), tmp);
			}
			fSchedule.get(purchaseSchedule.get(i).getTick()).add(purchaseSchedule.get(i).getShoe());

		}
		Logger.getLogger(ShoeStoreRunner.class.getName()).info("website client service " + getName() + " created");
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
		subscribeBroadcast(TickBroadcast.class, tickTock->{
			fCurrentTick = tickTock.getTick();
			if(fSchedule.containsKey(fCurrentTick)){
				for(String s : fSchedule.get(fCurrentTick)){
					sendRequest(new PurchaseOrderRequest(s, getName(), false, fCurrentTick), req->{
							fSchedule.get(((Receipt) req).getRequestTick()).removeFirstOccurrence(s);
							if(fSchedule.get(((Receipt) req).getRequestTick()).isEmpty()){
								fSchedule.remove(((Receipt) req).getRequestTick());
							}
							if(fSchedule.isEmpty() && fWishList.isEmpty()){
								terminate();
							}
						
						
					});
				}
			}
		});
		subscribeBroadcast(NewDiscountBroadcast.class, d->{
			if(fWishList.contains(d.getShoe())){
				sendRequest(new PurchaseOrderRequest(d.getShoe(), getName(), true, fCurrentTick), req->{
					if(req!=null){
						fWishList.remove(d.getShoe());
						if(fSchedule.isEmpty() && fWishList.isEmpty()){
							terminate();
						}
					}
				});
			}
		});
		fCount.countDown();
	}
}
