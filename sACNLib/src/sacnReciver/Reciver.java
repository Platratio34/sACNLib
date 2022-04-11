package sacnReciver;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;

public class Reciver {
	
	public static int TIMEOUT = 100 * 1000;
	
	MulticastSocket socket;
	InputStream in;
	
	private HashMap<String, SACNSrc> srcs;
	private HashMap<Integer, SACNUni> data;
	
	public Reciver() {
		srcs = new HashMap<String, SACNSrc>();
		data = new HashMap<Integer, SACNUni>();
		try {
			socket = new MulticastSocket(5568); //5568
			socket.setReuseAddress(true);
			socket.joinGroup(InetAddress.getByName("239.255.0.1")); 
			socket.setSoTimeout(100000);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
//		socket.close();
	}
	
	public void update() {
		byte[] buff = new byte[65535];
		DatagramPacket p = new DatagramPacket(buff, 65535);
		try {
			socket.receive(p);
			SACNPacket sP = new SACNPacket(buff);
//			System.out.println(sP);
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
//			System.out.println(data.get(sP.universe));
			
		} catch (IOException e) {
			e.printStackTrace();
			socket.close();
			return;
		}
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
			r.update();
			System.out.println(printArr(r.getDmx(0)));
		}
	}

}
