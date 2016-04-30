package bgu.spl.app;

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.HashMap;
import java.util.Iterator;

import bgu.spl.mics.MicroService;

/**
 * this class extends MicroService and represents a micro-service that is responsible 
 * for sending discount broadcasts,handling the restock requests received for the selling services and 
 * sending manufacturing requests to the factory.
 * it also responsible for organizing the store stock.
 *
 */
public class ManagementService extends MicroService {

	/**
	 * this class keeps 3 maps:
	 * 1. a map sorted by a shoe type and a list of manufacturing requests for that shoe.
	 * 2. a map sorted by a manufacturing requests and a list of restock request received
	 *    for that manufacturing order.
	 * 3. a map for following the ticking broadcasts and manufacturing a shoe per scheduled tick.
	 * another object is used to synchronize some of the methods.
	 */
	private Map<String, LinkedList<ManufacturingOrderRequest>> fManReqs;
	private Map<ManufacturingOrderRequest, LinkedList<RestockRequest>> fRequests;
	private Map<Integer, LinkedList<DiscountSchedule>> fSchedules;
	private int fCurrentTick;
	private Object fLock; 
	private CountDownLatch fCount;

	public ManagementService(List<DiscountSchedule> discountSchedule, int tick, CountDownLatch count) {
		super("manager");
		fCurrentTick=tick;
		fManReqs = new HashMap<>();
		fRequests = new HashMap<>();
		fSchedules = new HashMap<>();
		fLock = new Object();
		fCount=count;
		for(int i=0; i<discountSchedule.size(); i++){
			LinkedList<DiscountSchedule> tmp = this.fSchedules.get(discountSchedule.get(i).getTick());
			if(tmp == null){
				tmp = new LinkedList();
				fSchedules.put(discountSchedule.get(i).getTick(), tmp);
			}
			tmp.add(new DiscountSchedule(discountSchedule.get(i).getShoe(),discountSchedule.get(i).getTick(),discountSchedule.get(i).getAmount()));
		}
		Logger.getLogger(ShoeStoreRunner.class.getName()).info("Manager created successfuly");
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
			if(fSchedules.containsKey(fCurrentTick)){
				for(DiscountSchedule d : fSchedules.get(fCurrentTick)){
					Store.getInstance().addDiscount(d.getShoe(), d.getAmount());
					sendBroadcast(new NewDiscountBroadcast(d.getShoe()));
				}
			}
		});
		synchronized(fLock){
			subscribeRequest(RestockRequest.class, req->{
				if(fManReqs.containsKey(req.getShoe())){
					LinkedList<ManufacturingOrderRequest> tmp = fManReqs.get(req.getShoe());
					if(tmp.getLast().getAmount() - fRequests.get(tmp.getLast()).size() > 0){
						fRequests.get(fManReqs.get(req.getShoe()).getLast()).add(req);
						Logger.getLogger(ShoeStoreRunner.class.getName()).info("Manager received RestockRequest for " + req.getShoe() + " at tick " + fCurrentTick);
					}
					else{
						Logger.getLogger(ShoeStoreRunner.class.getName()).info("Manager received RestockRequest for "+ req.getShoe() + " at tick " + fCurrentTick);
						ManufacturingOrderRequest M = new ManufacturingOrderRequest(req.getShoe(),(fCurrentTick%5)+1, fCurrentTick);
						fManReqs.get(req.getShoe()).addLast(M);
						sendNewManufacturingOrder(M,req);
						
					}
				}
				else{
					ManufacturingOrderRequest M = new ManufacturingOrderRequest(req.getShoe(),(fCurrentTick%5)+1, fCurrentTick);				
					LinkedList L = new LinkedList();
					L.add(M);
					fManReqs.put(req.getShoe(),L);
					sendNewManufacturingOrder(M, req);
				}
			});
			fCount.countDown();
		}
	}
	
	
	private synchronized void sendNewManufacturingOrder(ManufacturingOrderRequest M, RestockRequest req){
		LinkedList L1 = new LinkedList();
		L1.add(req);
		fRequests.put(M, L1);
		boolean success = sendRequest(M , man->{
			if(man!=null){
				int shoesToAdd = M.getAmount() - fRequests.get(M).size();
				Store.getInstance().add(req.getShoe(), shoesToAdd);
				Store.getInstance().file((Receipt)man);	
				for(RestockRequest r : fRequests.get(M)){
					complete(r, true);
				}
			}
			else{
				for(RestockRequest r : fRequests.get(M)){
					complete(r, false);
				}
			}
		});
	}
}