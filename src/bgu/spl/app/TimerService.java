package bgu.spl.app;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.MicroService;

/**
 * 
 * This micro-service is our global system timer (handles the clock ticks in the system). It is responsible
 * for counting how much clock ticks passed since the beggining of its execution and notifying every
 * other microservice (thats intersted) about it using the TickBroadcast.
 *
 */
public class TimerService extends MicroService{

	private int fSpeed;
	private int fDuration;
	private int fTick;
	private Timer fTimer;
	private CountDownLatch fCount;


	public TimerService(int speed, int duration, CountDownLatch count){
		super("timer");
		fSpeed=speed;
		fDuration=duration;
		fTick=0;
		fTimer = new Timer();
		fCount=count;
		Logger.getLogger(ShoeStoreRunner.class.getName()).info("Timer created succssfully");
	}
	/**
	 * this method is called once when the event loop starts.
	 * it initializes a schedule according to a given speed and duration.
	 */
	protected void initialize() {
		subscribeBroadcast(TerminateBroadcast.class, terminate->{
			fTimer.cancel();
			this.terminate();
		});
		try {
			fCount.await();
		} catch (InterruptedException e) {
		}
		fTimer.schedule(new TimerTask(){

			@Override
			public void run() {
				if(fTick < fDuration){
					fTick++;
					System.out.println("++++++++++++++++++++++++++++++++++ "+"Tick "+fTick +" ++++++++++++++++++++++++++++++++++++++");
					sendBroadcast((Broadcast) new TickBroadcast(fTick));

				}else{
					sendBroadcast((Broadcast) new TerminateBroadcast());
				}
			}
		}, 0, fSpeed);
	}
	/**
	 * 
	 * @return a broadcast of the current tick in the schedule.
	 */
}
