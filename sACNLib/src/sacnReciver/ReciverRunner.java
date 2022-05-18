package sacnReciver;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Enumeration;

import dataManagment.JsonObj;
import errorHandler.ErrorLogger;

/**
 * Recives and interprests sACN packets<br>
 * Queues the packets in a Reciver
 * @author Peter Crall
 *
 */
public class ReciverRunner implements Runnable {
	
	private Reciver rec;
	
	/**
	 * Recive timeout time<br>
	 * 100 seconds default
	 */
	public static int TIMEOUT = 100 * 1000;
	/**
	 * Recive port for sACN
	 */
	public static final int PORT = 5568;
	/**
	 * Recive address for sACN
	 */
	public static final String HOSTNAME = "239.255.";
	public String host;
	
	private MulticastSocket socket;
	
	private boolean runing;
	/**
	 * Log info messages
	 */
	public boolean log = true;
	
	private ErrorLogger logger;
	
//	public BlockingQueue<byte[]> packetQueue;
	public int universe;
	
	private static final String CONFIG_FILENAME = "sACN_reciver.cfg";
	private JsonObj cfg;
	
	/**
	 * Creats a new ReciverRunner
	 * @param rec : the asscoiated Reciver
	 * @param log : log info messages
	 */
	public ReciverRunner(Reciver rec, boolean log, int universe) {
		cfg = JsonObj.parseP(CONFIG_FILENAME);
//	    System.out.println();
		this.universe = universe;
		this.log = log;
		logger = new ErrorLogger("SACN Reciver", !log);
		logger.showOnError = log;
		this.rec = rec;
//		packetQueue = new LinkedBlockingDeque<byte[]>();
//		threadPool = new Thread[4];
		JsonObj inter = null;
		if(cfg.hasKey("interface")) {
			inter = cfg.getKey("interface");
		} else {
			inter = new JsonObj();
			cfg.setKey("interface", inter);
			inter.setKey("ipStart", "10.101.50");
		}
		NetworkInterface iFace = null;
		try {
	        logMsg("Full list of Network Interfaces:");
	        for (Enumeration<NetworkInterface> en =
	              NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {

	            NetworkInterface intf = en.nextElement();
	            logMsg("    " + intf.getName() + " " +
	                                                intf.getDisplayName() + "");

	            for (Enumeration<InetAddress> enumIpAddr =
	                     intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {

	                String ipAddr = enumIpAddr.nextElement().toString();

	                logMsg("        " + ipAddr);
	                
	                if(inter.hasKey("ipStart")) {
		                if(ipAddr.startsWith( "/"+inter.getKey("ipStart").string() )){
		                    iFace = intf;
		                    logMsg("==>> Binding to this adapter...");
		                }
	                } else if(inter.hasKey("name")) {
	                	if(intf.getName().equals( inter.getKey("name").string() )) {
	                		iFace = intf;
		                    logMsg("==>> Binding to this adapter...");
	                	}
	                }
	            }
	        }
	    } catch (SocketException e) {
	    	logMsg(" (error retrieving network interface list)" + e);
	    }
		int u = universe - 0;
		host = HOSTNAME + (u/255) + "." + (u%255);
		try {
			logMsg("Initializing reciver on " + host+":"+PORT);
//			socket = new MulticastSocket[2];
//			for(int i = 0; i < socket.length; i++) {
				socket = new MulticastSocket(PORT);
				socket.setReuseAddress(true);
				socket.setNetworkInterface(iFace);
	//			System.out.println(InetAddress.getByName(HOSTNAME));
				socket.joinGroup(InetAddress.getByName(host));
//				System.out.println(HOSTNAME+"0."+(i+1));
				socket.setSoTimeout(TIMEOUT);
//			}
			runing = true;
		} catch (Exception e) {
			socket = null;
			runing = false;
			logger.logError(e);
			e.printStackTrace();
			return;
		}
	}

	@Override
	public void run() {
		if(socket == null) {
			try {
				logMsg("Initializing reciver on " + host+":"+PORT);
//				socket = new MulticastSocket[2];
//				for(int i = 0; i < socket.length; i++) {
					socket = new MulticastSocket(PORT);
					socket.setReuseAddress(true);
					socket.joinGroup(InetAddress.getByName(host)); 
					socket.setSoTimeout(TIMEOUT);
//				}
				runing = true;
			} catch (Exception e) {
				socket = null;
				runing = false;
				logger.logError(e);
				e.printStackTrace();
				return;
			}
		}
		if(!runing) {
			return;
		}
		logMsg("Listining for sACN . . .");
		while(runing) {
			try {
//				for(int i = 0; i < socket.length; i++) {
					byte[] buff = new byte[65535];
					DatagramPacket p = new DatagramPacket(buff, 65535);
					socket.receive(p);
//					System.out.print(buff[0x71]);
//					if(universe == 2) {
//						logMsg("a");
						int u = universe - 1;
//						buff[0x71] = 0x01;
//						System.out.println(universe);
						buff[0x71] = (byte)(u%0x100);
						buff[0x72] = (byte)(u/0x100);
//					}
//					System.out.println(" - " + buff[0x71]);
					rec.threadPool.putIn(buff);
//				}
			} catch(Exception e) {
				if(!(e instanceof SocketTimeoutException)) {
					logger.logError(e);
					e.printStackTrace();
				}
			}
//			while(threadPool.hasOut()) {
//				try {
//					SACNPacket p = threadPool.pollOut(10, TimeUnit.MILLISECONDS);
//					if(p == null) continue;
////					logMsg("not null");
//					if(!p.valid()) continue;
//					rec.packetQueue.put(p);
////					logMsg("Pased " + p);
//				} catch(Exception e) {
//					e.printStackTrace();
//				}
//			}
		}
	}
	
	/**
	 * Stops the runable
	 */
	public void stop() {
		runing = false;
	}
	
	protected void logMsg(String msg) {
		if(log) {
			logger.logInfo(msg);
		}
//		System.out.println(msg);
	}
	protected void logError(Exception e) {
		if(log) {
			logger.logError(e);
		}
	}

}
