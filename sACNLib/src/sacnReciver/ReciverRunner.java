package sacnReciver;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import errorHandler.ErrorLogger;
import errorHandler.ErrorType;

public class ReciverRunner implements Runnable {
	
	private Reciver rec;
	
	public static int TIMEOUT = 100 * 1000;
	public static final int PORT = 5568;
	public static final String HOSTNAME = "239.255.0.1";
//	public static final String HOSTNAME = "localhost";
	
	MulticastSocket socket;
	
	private boolean runing;
	public boolean log = true;
	
	private ErrorLogger logger;
	
	public ReciverRunner(Reciver rec, boolean log) {
		this.log = log;
		logger = new ErrorLogger("SACN Reciver");
		this.rec = rec;
		try {
			logMsg("Initializing reciver on " + HOSTNAME+":"+PORT);
			socket = new MulticastSocket(PORT);
			socket.setReuseAddress(true);
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
				runing =  false;
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
				rec.packetQueue.put(sP);
				logMsg(sP.toString());
			} catch(Exception e) {
				logger.logError(e);
				e.printStackTrace();
			}
		}
	}
	
	public void stop() {
		runing = false;
	}
	
	protected void logMsg(String msg) {
		if(log) {
			logger.logInfo(msg);
		}
	}

}