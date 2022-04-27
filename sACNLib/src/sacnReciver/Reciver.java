package sacnReciver;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * Reciver for SACN packet<br>
 * Uses a ReciverRunner on a sperat thread for reciving the packets<br>
 * Use update() to pull new packets from the ReciverRunner before getting data
 * @author Peter Crall
 *
 */
public class Reciver {
	
//	public static int TIMEOUT = 100 * 1000;
//	
//	MulticastSocket socket;Z
	
	private HashMap<String, SACNSrc> srcs;
	private HashMap<Integer, SACNUni> data;
	
	/**
	 * Queue of packets from the ReciverRunner
	 */
	public BlockingQueue<SACNPacket> packetQueue;
	private ReciverRunner recRun;
	private Thread recThread;
	
	/**
	 * Creates a new Reciver
	 */
	public Reciver() {
		srcs = new HashMap<String, SACNSrc>();
		data = new HashMap<Integer, SACNUni>();
		packetQueue = new LinkedBlockingDeque<SACNPacket>();
		recRun = new ReciverRunner(this, true);
		recThread = new Thread(recRun);
		recThread.start();
	}
	/**
	 * Creates a new Reciver
	 * @param log : if the reciver should log info messages
	 */
	public Reciver(boolean log) {
		srcs = new HashMap<String, SACNSrc>();
		data = new HashMap<Integer, SACNUni>();
		packetQueue = new LinkedBlockingDeque<SACNPacket>();
		recRun = new ReciverRunner(this, log);
		recThread = new Thread(recRun);
		recThread.start();
	}
	
	/**
	 * Pulls packets from the Queue
	 * @return If any packets were pulled
	 */
	public boolean update() {
		SACNPacket sP = null;
		boolean rt = false;
		try {
			sP = packetQueue.poll(10, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		while(sP != null) {
			rt = true;
			try {
				SACNSrc src;
				if(srcs.containsKey(sP.sourceName)) {
					src = srcs.get(sP.sourceName).setNow();
				} else {
					src = sP.getSrc();
					srcs.put(src.name, src);
				}
				
				if(!data.containsKey(sP.universe)) {
					data.put(sP.universe, new SACNUni());
				}
				data.get(sP.universe).trySetDmx(sP.dmx, src);
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				sP = packetQueue.poll(10, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				sP = null;
				e.printStackTrace();
			}
		}
		return rt;
	}
	
	@SuppressWarnings("unused")
	private String printArr(byte[] arr, int m) {
		String str = "[";
		for(int i = 0; i < arr.length && i < m; i++) {
			if(i > 0) str += ",";
			str += arr[i];
		}
		return str+"]";
	}
	
	/**
	 * Gets a univers of DMX as an integer aray
	 * @param universe : the universe number (1-high)
	 * @return An integer array of the data in the selected universe
	 */
	public int[] getDmx(int universe) {
		if(!data.containsKey(universe)) {
//			System.out.println("I");
			return new int[512];
		}
		return data.get(universe).getDmx();
	}
	/**
	 * Gets the DMX data at a particular address in a universe
	 * @param universe : target universe
	 * @param adr : target address
	 * @return The DMX value at univers.adr
	 */
	public int getDmx(int universe, int adr) {
		if(!data.containsKey(universe)) {
			return 0;
		}
		return data.get(universe).getDmx(adr);
	}
	
	/**
	 * Returns a string of a DMX universe<br>
	 * Intended for printing the universe to a console or file
	 * @param uni : the universe to display
	 * @return A string containing the universe
	 */
	public String printDmx(int uni) {
		return printArr(getDmx(uni));
	}
	
	public static String printArr(Object[] arr) {
		String str = "[";
		for(int i = 0; i < arr.length; i++) {
			if(i > 0) str += ",";
			str += arr[i];
		}
		return str + "]";
	}
	public static String printArr(int[] arr) {
		String str = "[";
		for(int i = 0; i < arr.length; i++) {
			if(i > 0) str += ",";
			str += arr[i];
		}
		return str + "]";
	}
	
	public static void main(String[] args) {
		Reciver r = new Reciver();
		System.out.println("Listening . . .");
		while(true) {
			if(r.update()) {
				System.out.println(printArr(r.getDmx(1)));
			}
		}
	}

}
