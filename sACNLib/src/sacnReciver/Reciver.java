package sacnReciver;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public class Reciver {
	
//	public static int TIMEOUT = 100 * 1000;
//	
//	MulticastSocket socket;Z
	
	private HashMap<String, SACNSrc> srcs;
	private HashMap<Integer, SACNUni> data;
	
	public BlockingQueue<SACNPacket> packetQueue;
	private ReciverRunner recRun;
	private Thread recThread;
	
	public Reciver() {
		srcs = new HashMap<String, SACNSrc>();
		data = new HashMap<Integer, SACNUni>();
		packetQueue = new LinkedBlockingDeque<SACNPacket>();
		recRun = new ReciverRunner(this, true);
		recThread = new Thread(recRun);
		recThread.start();
	}
	
	public Reciver(boolean log) {
		srcs = new HashMap<String, SACNSrc>();
		data = new HashMap<Integer, SACNUni>();
		packetQueue = new LinkedBlockingDeque<SACNPacket>();
		recRun = new ReciverRunner(this, log);
		recThread = new Thread(recRun);
		recThread.start();
	}
	
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
	
	private String printArr(byte[] arr, int m) {
		String str = "[";
		for(int i = 0; i < arr.length && i < m; i++) {
			if(i > 0) str += ",";
			str += arr[i];
		}
		return str+"]";
	}
	
	public int[] getDmx(int universe) {
		if(!data.containsKey(universe)) {
//			System.out.println("I");
			return new int[512];
		}
		return data.get(universe).getDmx();
	}
	public int getDmx(int universe, int adr) {
		if(!data.containsKey(universe)) {
			return 0;
		}
		return data.get(universe).getDmx(adr);
	}

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
		// TODO Auto-generated method stub
		Reciver r = new Reciver();
		System.out.println("Listening . . .");
		while(true) {
			if(r.update()) {
				System.out.println(printArr(r.getDmx(1)));
			}
		}
	}

}
