package bgu.spl.mics.impl;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * this class represents a queue for a certain type of request of message
 * queues of subscribed micro-services.
 * this queue works in a round-robin fashion, meaning that after a micro-service
 * gets a message added to its queue it "goes to the back of the line" and the next micro-service
 * will get the next message added to its queue and so on.
 *
 * @param <E>
 */

public class RoundRobinQueue<E> extends ArrayList  {

	LinkedBlockingQueue fNextToRecieve;
	int i;
	
	public RoundRobinQueue(LinkedBlockingQueue queue){
		super();
		fNextToRecieve = queue;
		i=0;
		this.add(i, fNextToRecieve);
	}
	
	/**
	 * updates the index of the next micro-service message queue to 
	 * receive a message while making sure that if we reached the end of the queue,
	 * we return to the head of the queue.
	 * 
	 */
	public void UpdateNext(){
		if (i >= (size()-1)){
			i = -1;
		}
		i++;
		fNextToRecieve = (LinkedBlockingQueue)this.get((i%size()));
	}
	
	/**
	 * 
	 * @return next micro-service queue to receive a message.
	 */
	public LinkedBlockingQueue getNextQueue(){
		return this.fNextToRecieve;
	}
}
