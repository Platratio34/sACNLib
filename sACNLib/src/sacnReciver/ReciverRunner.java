package sacnReciver;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

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
		this.log = log;
		logger = new ErrorLogger("SACN Reciver");
		logger.showOnError = log;
		this.rec = rec;
		try {
			logMsg("Initializing reciver on " + HOSTNAME+":"+PORT);
			socket = new MulticastSocket(PORT);
			socket.setReuseAddress(true);
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
		System.out.println("Listining for sACN . . .");
		while(runing) {
			byte[] buff = new byte[65535];
			DatagramPacket p = new DatagramPacket(buff, 65535);
			try {
//				logMsg("L");
				socket.receive(p);
				SACNPacket sP = new SACNPacket(buff);
				if(sP.valid()) {
					rec.packetQueue.put(sP);
//					System.out.println(sP);
				}
//				logMsg(sP.toString());
			} catch(Exception e) {
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

}
