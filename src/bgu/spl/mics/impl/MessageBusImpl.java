package bgu.spl.mics.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.Message;
import bgu.spl.mics.MessageBus;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.Request;
import bgu.spl.mics.RequestCompleted;

/**
 * This class implements the MessageBus interface.
 * It is a thread-safe Singleton that is shared by all the 
 * micro-services.
 */
public class MessageBusImpl implements MessageBus {

	/**
	 * In order to guarantee that this will be a thread-safe Singleton,
	 * we keep the constructor private and create an internal private static class - 
	 * MessageBusHolder, with a private static field that is the actual instance of the MessageBusImpl.
	 * If a micro-service wants to use the bus to send a message, it must call the public getInstance()  
	 * method which returns the ONLY instance of the bus.
	 * It is guaranteed that there will be only one instance of the bus due to the fact that the loading of classes
	 * java is done using only one thread and class is loaded only when we reference it.
	 * 
	 */
	private static class MessageBusHolder{
		private static MessageBusImpl instance = new MessageBusImpl();
	}

	/**
	 * 
	 * @return the instance of the singleton message bus implementation
	 */
	public static MessageBusImpl getInstance(){
		return MessageBusHolder.instance;
	}
	
	/**
	 * There are 4 maps:
	 * 1. a map for the requests which is sorted by the type of the request and for every
	 *    type a round-robin queue(explained later).
	 * 2. a map for the broadcasts which is sorted by the type of the broadcast and for every 
	 *    type a LinkedBlockingQueue which organizes the tread activities of the queue.
	 * 3. a map for the that for every request keeps the micro-service that sent it in order to add
	 *    a complete request message the the requesters queue.
	 * 4. a map that saves the message queue for each micro-service.
	 */
	private Map<Class<? extends Request>, RoundRobinQueue<LinkedBlockingQueue>> Requests;
	private Map<Class<? extends Broadcast>, LinkedBlockingQueue<LinkedBlockingQueue>> Broadcasts;
	private Map<Request, MicroService> Requesters;
	private Map<MicroService, LinkedBlockingQueue> Queues;
	private int registered;

	
	private MessageBusImpl(){ 
		this.Requests = new HashMap<>();
		this.Broadcasts = new HashMap<>();
		this.Queues = new HashMap<>();
		this.Requesters = new HashMap<>();
		registered = 0;
	}
	/**
     * subscribes {@code m} to receive {@link Request}s of type {@code type}.
     * for each request a queue is created with the message queues of every micro-service that 
     * has subscribed to this request. this queue is run in a round-robin fashion using an
     * external class RoundRobinQueue.
     * <p>
     * @param type the type to subscribe to
     * @param m    the subscribing micro-service
     */
	public void subscribeRequest(Class<? extends Request> type, MicroService m) { // synchronized
		synchronized(Requests){
			if(Requests.containsKey(type)){
				Requests.get(type).add(Queues.get(m));
			}
			else{
				RoundRobinQueue tmp = new RoundRobinQueue<LinkedBlockingQueue>(Queues.get(m));
				Requests.put(type, tmp);
			}
		}
	}

	/**
     * subscribes {@code m} to receive {@link Broadcast}s of type {@code type}.
     * <p>
     * @param type the type to subscribe to
     * @param m    the subscribing micro-service
     */
	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) { // synchronized?
		synchronized(Broadcasts){
			if(Broadcasts.containsKey(type)){
				Broadcasts.get(type).add(Queues.get(m));
			}
			else{
				LinkedBlockingQueue tmp = new LinkedBlockingQueue<LinkedBlockingQueue>();
				tmp.add(Queues.get(m));
				Broadcasts.put(type, tmp);
			}
		}
	}
	/**
     * Notifying the MessageBus that the request {@code r} is completed and its
     * result was {@code result}.
     * When this method is called, the message-bus will create a RequesTcompleted
     * request with its result and add it the the requesting micro-service.
     * <p>
     * @param <T>    the type of the result expected by the completed request
     * @param r      the completed request
     * @param result the result of the completed request
     */
	@Override
	public <T> void complete(Request<T> r, T result) {
		RequestCompleted tmp = new RequestCompleted(r,result);
		Queues.get(Requesters.get(r)).add(tmp);
	}
	
	/**
     * adds the {@link Broadcast} {@code b} to the message queues of all the
     * micro-services subscribed to {@code b.getClass()}.
     * after which notifies all the micro-services subscribed to this broadcast
     * that a message was added to their queue and they should try to attend to it.
     * <p>
     * @param b the message to add to the queues.
     */
	@Override
	public void sendBroadcast(Broadcast b) {  
		LinkedBlockingQueue<LinkedBlockingQueue> tmp = Broadcasts.get(b.getClass());
		if(tmp !=null){
			for(LinkedBlockingQueue L : tmp){
				L.add(b);
			}
		}
	}
	
	/**
     * adds the {@link Request} {@code r} to the message queue of one of the
     * micro-services subscribed to {@code r.getClass()} in a round-robin
     * fashion.
     * this method is synchronized due the checking if there is a micro-service
     * that is subscribed to this request and can receive it which can be compromised
     * due many micro-services that share this object.
     * <p>
     * @param r         the request to add to the queue.
     * @param requester the {@link MicroService} sending {@code r}.
     * @return true if there was at least one micro-service subscribed to
     *         {@code r.getClass()} and false otherwise.
     */
	@Override
	public synchronized boolean sendRequest(Request<?> r, MicroService requester) { 
		Requesters.put(r, requester);
		boolean ans = false;
		RoundRobinQueue<LinkedBlockingQueue> tmp = Requests.get(r.getClass());
		if(tmp == null || tmp.isEmpty()){
			return ans;
		}
		tmp.getNextQueue().add(r);
		tmp.UpdateNext();
		ans = true;
		return ans;
	}

	/**
	 * allocates a message-queue for the {@link MicroService} {@code m}.
	 * <p>
	 * @param m the micro-service to create a queue for.
	 */
	@Override
	public void register(MicroService m){ 
		Queues.putIfAbsent(m, new LinkedBlockingQueue());	
		registered++;
	}
	
	 /**
     * removes the message queue allocated to {@code m} via the call to
     * {@link #register(bgu.spl.mics.MicroService)} and cleans all references
     * related to {@code m} in this message-bus. If {@code m} was not
     * registered, nothing should happen.
     * this method is synchronized along with the sendRequest method on "this" due to the possibility of 
     * a micro-service unregistering while it is supposed to receive a message.
     * <p>
     * @param m the micro-service to unregister.
     */
	@Override
	public synchronized void unregister(MicroService m) {
		for(RoundRobinQueue<LinkedBlockingQueue> entry : Requests.values()){
			entry.remove(Queues.get(m));
		}
		for(LinkedBlockingQueue<LinkedBlockingQueue> entry : Broadcasts.values()){
			entry.remove(Queues.get(m));
		}
		Queues.remove(m);
		registered--;
	}

	 /**
     * using this method, a <b>registered</b> micro-service can take message
     * from its allocated queue.
     * This method is blocking -meaning that if no messages
     * are available in the micro-service queue it
     * waits until a message became available.
     * The method throws the {@link IllegalStateException} in the case
     * where {@code m} was never registered.
     * <p>
     * @param m the micro-service requesting to take a message from its message
     *          queue
     * @return the next message in the {@code m}'s queue (blocking)
     * @throws InterruptedException if interrupted while waiting for a message
     *                              to became available.
     */
	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		if(!(Queues.containsKey(m))){
			throw new IllegalStateException("This MicroService is not registered");
		}
		return (Message)Queues.get(m).take();
	}
	public synchronized int getRegistered() {
		return registered;
	}

}
