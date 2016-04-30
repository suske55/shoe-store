package bgu.spl.app;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import bgu.spl.mics.impl.MessageBusImpl;

/**
 * 
 * An object in charge of running the events according to a given json file
 *
 */
public class ShoeStoreRunner {

	private final static Logger LOGGER = Logger.getLogger(ShoeStoreRunner.class.getName()); 

	public static void main(String[] args) throws IOException{

		LOGGER.setLevel(Level.ALL);
		Scanner sc = new Scanner(System.in);
		args[0] = sc.nextLine();		
		JsonObject j = null;

		try {
			FileReader reader = new FileReader(args[0]);
			JsonParser parser = new JsonParser();
			JsonElement e = parser.parse(reader);
			j = e.getAsJsonObject();
		}
		catch(FileNotFoundException e){
			e.printStackTrace();
		}

		JsonObject services = j.getAsJsonObject("services");
		JsonArray storage = j.get("initialStorage").getAsJsonArray();
		JsonArray customers = services.getAsJsonArray("customers");
		int numOfMC = 2 + services.get("factories").getAsInt() + services.get("sellers").getAsInt()+ customers.size();
		CountDownLatch count = new CountDownLatch(numOfMC-2);
		ExecutorService executor = Executors.newFixedThreadPool(numOfMC);
		createStorage(storage);
		createManager(services,executor,count);
		createFactories(services,executor,count);
		createSellers(services,executor,count);
		createCustomers(customers,executor,count);
		createTimerService(services,executor,count);

		while(MessageBusImpl.getInstance().getRegistered() != 0){}
		executor.shutdownNow();
		Store.getInstance().print();
	}
	
/****************************************************Creation Methods*******************************************************/

	/**
	 * this method creates the initial shoe storage of the store
	 * 
	 * @param storage the information that was read from the json file which has the data of the initial storge
	 */
	public static void createStorage(JsonArray storage){
		ShoeStorageInfo[] iShoe = new ShoeStorageInfo[storage.size()];
		int i = 0;
		for(JsonElement k : storage){
			String type = ((JsonObject)k).get("shoeType").getAsString();
			int amount = ((JsonObject)k).get("amount").getAsInt();
			iShoe[i] = new ShoeStorageInfo(type , amount, 0);
			i++;
		}
		Store.getInstance().load(iShoe);
	}

	/**
	 * this method creates the management service
	 * 
	 * @param services the json object that hold the details of every micro service
	 * @param executor an executor service that contains all the runnable objects
	 * @param count a countDownLatch that is sent to every micro service to count down 
	 * 		  in order to start the timer after all the micro services are created
	 */
	public static void createManager(JsonObject services, ExecutorService executor, CountDownLatch count){
		JsonObject manager = services.getAsJsonObject("manager");
		JsonArray schedule = manager.getAsJsonArray("discountSchedule");
		List<DiscountSchedule> list = new LinkedList();
		for(JsonElement v : schedule){
			String type = ((JsonObject)v).get("shoeType").getAsString();
			int amount = ((JsonObject)v).get("amount").getAsInt();
			int tick = ((JsonObject)v).get("tick").getAsInt();
			DiscountSchedule d = new DiscountSchedule(type,tick,amount);
			list.add(d);
		}
		Runnable man = new ManagementService(list,0, count);
		Thread m = new Thread(man);
		executor.submit(m);
	}

	/**
	 * this method creates the shoe factory services
	 * 
	 * @param services the json object that hold the details of every micro service
	 * @param executor an executor service that contains all the runnable objects
	 * @param count a countDownLatch that is sent to every micro service to count down 
	 * 		  in order to start the timer after all the micro services are created
	 */
	public static void createFactories(JsonObject services, ExecutorService executor, CountDownLatch count){
		for(int n = 1; n <= services.get("factories").getAsInt(); n++){
			String name = ""+ n;
			Runnable factory = new ShoeFactoryService(name,0,count);
			Thread f = new Thread(factory);
			executor.submit(f);
		}
	}
	
	/**
	 * this method creates the selling services
	 * 
	 * @param services the json object that hold the details of every micro service
	 * @param executor an executor service that contains all the runnable objects
	 * @param count a countDownLatch that is sent to every micro service to count down 
	 * 		  in order to start the timer after all the micro services are created
	 */
	public static void createSellers(JsonObject services, ExecutorService executor, CountDownLatch count){
		for(int n = 1; n <= services.get("sellers").getAsInt(); n++){
			String name = ""+ n;
			Runnable seller = new SellingService(name,0,count);
			Thread s = new Thread(seller);
			executor.submit(s);
		}
	}

	/**
	 * this method creates the website client services
	 * 
	 * @param customers the json object that hold the details of every website client service
	 * @param executor an executor service that contains all the runnable objects
	 * @param count a countDownLatch that is sent to every micro service to count down 
	 * 		  in order to start the timer after all the micro services are created
	 */
	public static void createCustomers(JsonArray customers, ExecutorService executor, CountDownLatch count){
		for (JsonElement z : customers){
			String name = ((JsonObject)z).get("name").getAsString();
			JsonArray wishlist = ((JsonObject)z).get("wishList").getAsJsonArray();
			JsonArray purchaseSchedule = ((JsonObject)z).get("purchaseSchedule").getAsJsonArray();
			Set<String> wList = new HashSet();
			List<PurchaseSchedule> pSchedule = new LinkedList();
			for(JsonElement e : wishlist){
				wList.add(e.getAsString()) ;
			}
			for(JsonElement e : purchaseSchedule){
				String type = ((JsonObject)e).get("shoeType").getAsString();
				int tick = ((JsonObject)e).get("tick").getAsInt();
				pSchedule.add(new PurchaseSchedule(type,tick));
			}
			Runnable client = new WebsiteClientService(name,pSchedule,wList,0,count);
			Thread c = new Thread(client);
			executor.submit(c);
		}
	}

	/**
	 * this method creates the timer service
	 * 
	 * @param services the json object that hold the details of every micro service
	 * @param executor an executor service that contains all the runnable objects
	 * @param count a countDownLatch that is sent to every micro service to count down 
	 * 		  in order to start the timer after all the micro services are created
	 */
	public static void createTimerService(JsonObject services, ExecutorService executor, CountDownLatch count){
		JsonObject time = services.getAsJsonObject("time");
		Runnable timer = new TimerService(((JsonObject)time).get("speed").getAsInt(),((JsonObject)time).get("duration").getAsInt(),count);
		Thread t = new Thread(timer);
		executor.submit(t);
	}
}

