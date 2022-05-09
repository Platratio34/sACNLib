package sacnReciver;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Enumeration;

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
	public static final String HOSTNAME = "239.255.0.1";
	
	private MulticastSocket socket;
	
	private boolean runing;
	/**
	 * Log info messages
	 */
	public boolean log = true;
	
	private ErrorLogger logger;
	
	/**
	 * Creats a new ReciverRunner
	 * @param rec : the asscoiated Reciver
	 * @param log : log info messages
	 */
	public ReciverRunner(Reciver rec, boolean log) {
	    System.out.println();
		this.log = log;
		logger = new ErrorLogger("SACN Reciver", !log);
		logger.showOnError = log;
		this.rec = rec;
		
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

	                if(ipAddr.startsWith("/10.101.50")){
	                    iFace = intf;
	                    logMsg("==>> Binding to this adapter...");
	                }
	            }
	        }
	    } catch (SocketException e) {
	    	logMsg(" (error retrieving network interface list)" + "");
	    }
		
		try {
			logMsg("Initializing reciver on " + HOSTNAME+":"+PORT);
			socket = new MulticastSocket(PORT);
			socket.setReuseAddress(true);
			socket.setNetworkInterface(iFace);
//			System.out.println(InetAddress.getByName(HOSTNAME));
			socket.joinGroup(InetAddress.getByName(HOSTNAME));
			socket.setSoTimeout(TIMEOUT);
			runing = true;
		} catch (Exception e) {
			socket = null;
			runing =  false;
			logger.logError(e);
			e.printStackTrace();
			return;
		}
	}

	@Override
	public void run() {
		if(socket == null) {
			try {
				logMsg("Initializing reciver on " + HOSTNAME+":"+PORT);
				socket = new MulticastSocket(PORT);
				socket.setReuseAddress(true);
				socket.joinGroup(InetAddress.getByName(HOSTNAME)); 
				socket.setSoTimeout(TIMEOUT);
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
//		System.out.println("Listining for sACN . . .");
		while(runing) {
			byte[] buff = new byte[65535];
			DatagramPacket p = new DatagramPacket(buff, 65535);
			try {
//				logMsg("L");
				socket.receive(p);
				SACNPacket sP = new SACNPacket(buff);
				if(sP.valid()) {
					rec.packetQueue.put(sP);
//					logMsg(sP.toString());
//					System.out.println(sP);
				}
//				logMsg(sP.toString());
			} catch(Exception e) {
				if(e instanceof SocketTimeoutException) {
					continue;
				}
				logger.logError(e);
				e.printStackTrace();
			}
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
	}
	protected void logError(Exception e) {
		if(log) {
			logger.logError(e);
		}
	}

}
