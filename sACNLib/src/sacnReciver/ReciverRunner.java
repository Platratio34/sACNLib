package sacnReciver;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;

import errorHandler.ErrorLogger;
import threading.PoolRunnable;
import threading.ThreadPool;

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
	
	private MulticastSocket[] socket;
	
	private boolean runing;
	/**
	 * Log info messages
	 */
	public boolean log = true;
	
	private ErrorLogger logger;
	
//	public BlockingQueue<byte[]> packetQueue;
	private ThreadPool<byte[], SACNPacket> threadPool;
	
	/**
	 * Creats a new ReciverRunner
	 * @param rec : the asscoiated Reciver
	 * @param log : log info messages
	 */
	public ReciverRunner(Reciver rec, boolean log) {
//	    System.out.println();
		this.log = log;
		logger = new ErrorLogger("SACN Reciver", !log);
		logger.showOnError = log;
		this.rec = rec;
//		packetQueue = new LinkedBlockingDeque<byte[]>();
//		threadPool = new Thread[4];
		
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
	    	logMsg(" (error retrieving network interface list)" + e);
	    }
		
		try {
			logMsg("Initializing reciver on " + HOSTNAME+"?.?:"+PORT);
			socket = new MulticastSocket[2];
			for(int i = 0; i < socket.length; i++) {
				socket[i] = new MulticastSocket(PORT);
				socket[i].setReuseAddress(true);
				socket[i].setNetworkInterface(iFace);
	//			System.out.println(InetAddress.getByName(HOSTNAME));
				socket[i].joinGroup(InetAddress.getByName(HOSTNAME+"0."+(i+1)));
//				System.out.println(HOSTNAME+"0."+(i+1));
				socket[i].setSoTimeout(TIMEOUT);
			}
			runing = true;
		} catch (Exception e) {
			socket = null;
			runing = false;
			logger.logError(e);
			e.printStackTrace();
			return;
		}
		threadPool = new ThreadPool<byte[], SACNPacket>(4, new PoolRunnable<byte[], SACNPacket>() {
			@Override
			public SACNPacket run(byte[] buff) {
				return new SACNPacket(buff);
			}
		});
	}

	@Override
	public void run() {
		if(socket == null) {
			try {
				logMsg("Initializing reciver on " + HOSTNAME+"?.?:"+PORT);
				socket = new MulticastSocket[2];
				for(int i = 0; i < socket.length; i++) {
					socket[i] = new MulticastSocket(PORT);
					socket[i].setReuseAddress(true);
					socket[i].joinGroup(InetAddress.getByName(HOSTNAME+"0."+(i+1))); 
					socket[i].setSoTimeout(TIMEOUT);
				}
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
				for(int i = 0; i < socket.length; i++) {
					byte[] buff = new byte[65535];
					DatagramPacket p = new DatagramPacket(buff, 65535);
					socket[i].receive(p);
					if(i == 1) {
						buff[0x71] = 0x01;
					}
					threadPool.putIn(buff);
				}
			} catch(Exception e) {
				if(e instanceof SocketTimeoutException) {
					continue;
				}
				logger.logError(e);
				e.printStackTrace();
			}
			while(threadPool.hasOut()) {
				try {
					SACNPacket p = threadPool.pollOut(10, TimeUnit.MILLISECONDS);
					if(p == null) continue;
					if(!p.valid()) continue;
					rec.packetQueue.put(p);
				} catch(Exception e) {
					e.printStackTrace();
				}
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
