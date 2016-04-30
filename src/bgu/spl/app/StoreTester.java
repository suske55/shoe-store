package bgu.spl.app;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import bgu.spl.app.Store;

public class StoreTester {
	

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		Store.getInstance().print();
	}

	@Before
	public void setUp() throws Exception {
		ShoeStorageInfo[] stock = {new ShoeStorageInfo("sandals", 3, 0),
								   new ShoeStorageInfo("boots", 7, 0),
								   new ShoeStorageInfo("sneakers", 1, 1),
								   new ShoeStorageInfo("flip-flops", 8, 0),
								   new ShoeStorageInfo("kafkafim", 0, 0),
								   new ShoeStorageInfo("shkafkafim", 9, 0),
								   new ShoeStorageInfo("air-jordans", 5, 0),
								   new ShoeStorageInfo("shoresh", 8, 0)
		};
		Store.getInstance().load(stock);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetInstance() {
		assertTrue(Store.getInstance()!=null);
	}

	@Test
	public void testTake() {
		assertEquals(Store.getInstance().take("timberlands", false), BuyResult.NOT_IN_STOCK);
		assertEquals(Store.getInstance().take("sneakers", true), BuyResult.DISCOUNTED_PRICE);
		assertEquals(Store.getInstance().take("sneakers", true), BuyResult.NOT_ON_DISCOUNT);
		assertEquals(Store.getInstance().take("air-jordans", false), BuyResult.REGULAR_PRICE);
		
	}

	@Test
	public void testAdd() {
		assertEquals(Store.getInstance().take("kafkafim", false), BuyResult.NOT_IN_STOCK);
		Store.getInstance().add("kafkafim", 3);
		assertEquals(Store.getInstance().take("kafkafim", false), BuyResult.REGULAR_PRICE);
	}

	@Test
	public void testAddDiscount() {
		assertEquals(Store.getInstance().take("boots", true), BuyResult.NOT_ON_DISCOUNT);
		Store.getInstance().addDiscount("boots", 4);
		assertEquals(Store.getInstance().take("boots", true), BuyResult.DISCOUNTED_PRICE);
	}

}