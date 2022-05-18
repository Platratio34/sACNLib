package sacnReciver;

import java.nio.ByteBuffer;

/**
 * Packet contaning the sACN data
 * @author peter
 *
 */
public class SACNPacket {
	
	/**
	 * Preamble size
	 */
	public int preSize;
	/**
	 * Postamble size
	 */
	public int postSize;
	/**
	 * ACN CID
	 */
	public long cid;
	/**
	 * String name of the source
	 */
	public String sourceName;
	/**
	 * Priority of the data
	 */
	public int priority;
	/**
	 * sACN universe
	 */
	public int universe;
	/**
	 * DMX start code
	 */
	public int startCode;
	/**
	 * DMX data
	 */
	public int[] dmx;
	
	private boolean valid = false;
	
	public SACNPacket(byte[] arr) {
		preSize = (arr[1]&0xff) + (arr[0]&0xff)*0x100;
		postSize = (arr[3]&0xff) + (arr[2])*0x100;
		
		long dataType = (arr[21]&0xff) + (arr[20]&0xff)*0x100 + (arr[19]&0xff)*0x10000 + (arr[18]&0xff)*0x1000000;
		if(dataType != 0x04) {
			valid = false;
			return;
		}
		long vector2 = (arr[43]&0xff) + (arr[42]&0xff)*0x100 + (arr[41]&0xff)*0x10000 + (arr[40]&0xff)*0x1000000;
//		System.out.println(vector2);
		if(vector2 != 0x02) {
			valid = false;
			return;
		}
		
		byte[] cidA = new byte[0x10];
		for(int i = 0; i < cidA.length; i++) {
			cidA[i] = arr[0x16+i];
		}
		cid = ByteBuffer.wrap(cidA).getLong();
		
		sourceName = "";
		int lC = 0;
		for(int i = 0x2C; i < 0x6B; i++) {
			sourceName += (char)arr[i];
			if(arr[i] > 0) {
				lC = sourceName.length();
			}
		}
		sourceName = sourceName.substring(0,lC);
		if(sourceName.contains("Processor")) {
			valid = false;
			return;
		}
		priority = arr[0x6C]&0xff;
		universe = arr[0x71]&0xff;
		universe += (arr[0x72]&0xff) * 0x100;
		universe++;
		
		int dataFormat = arr[0x76]&0xff;
		if(dataFormat != 0xa1) {
			valid = false;
			return;
		}
		
		startCode = arr[0x7D]&0xff;
//		if(startCode != 0x00) {
//			valid = false;
//			return;
//		}
		int dmxLength = (arr[0x7C]&0xff) + (arr[0x7B]&0xff)*0x100 - 1;
		dmx = new int[dmxLength];
		for(int i = 0; i < dmx.length; i++) {
			dmx[i] = arr[0x7E+i] & 0xff;
		}
		valid = true;
	}
	
	/**
	 * Returns the sACN source of the the data
	 * @return
	 */
	public SACNSrc getSrc() {
		return new SACNSrc(cid, sourceName, priority);
	}
	
	@Override
	public String toString() {
		String str = "";
		str += "preSize="+preSize+";";
		str += "postSize="+postSize+";";
		str += "sourceName="+sourceName+";";
		str += "priority="+priority+";";
		str += "universe="+universe+";";
		str += "dmx="+Reciver.printArr(dmx);
		return str;
	}
	public String dispPacket() {
		String str = "{";
		str += "sourceName=\""+sourceName+"\", ";
		str += "CID="+cid+", ";
		str += "priority="+priority+", ";
		str += "universe="+universe+", ";
		str += "dmxStartCode="+startCode+", ";
		str += "dmxSize="+dmx.length;
		return str + "}";
	}
	
	/**
	 * If the packet was a valid sACN (E1.31) packet with DMX data
	 * @return
	 */
	public boolean valid() {
		return valid;
	}
}
