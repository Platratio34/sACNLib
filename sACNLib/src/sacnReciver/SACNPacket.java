package sacnReciver;

import java.nio.ByteBuffer;

public class SACNPacket {
	
	public int preSize;
	public int postSize;
	public String sourceName;
	public int priority;
	public int universe;
	public int startCode;
	public int[] dmx;
	
	private boolean valid = false;
	
	public SACNPacket(byte[] arr) {
		preSize = arr[1] + arr[0]*0x100;
		postSize = arr[3] + arr[2]*0x100;
		
		long idVector = (arr[21]&0xff) + (arr[20]&0xff)*0x100 + (arr[19]&0xff)*0x10000 + (arr[18]&0xff)*0x1000000;
		if(idVector != 0x04) {
			valid = false;
			return;
		}
		long vector2 = (arr[43]&0xff) + (arr[42]&0xff)*0x100 + (arr[41]&0xff)*0x10000 + (arr[40]&0xff)*0x1000000;
//		System.out.println(vector2);
		if(vector2 != 0x02) {
			valid = false;
			return;
		}
		if((arr[118]&0xff) != 0xa1) {
			valid = false;
			return;
		}
		
//		System.out.println(arr[125]&0xff);
		if((arr[125]&0xff) != 0x00) {
			valid = false;
			return;
		}
		
		sourceName = "";
		int lC = 0;
		for(int i = 0x2C; i < 0x6B; i++) {
			sourceName += (char)arr[i];
			if(arr[i] > 0) {
				lC = sourceName.length();
			}
		}
		sourceName = sourceName.substring(0,lC);
		priority = arr[0x6C] & 0xff;
		universe = arr[0x71] & 0xff;
		universe++;
		startCode = arr[0x7D] & 0xff;
		
		dmx = new int[512];
		for(int i = 0; i < 512; i++) {
			dmx[i] = arr[0x7E+i] & 0xff;
		}
		valid = true;
	}
	
	public SACNSrc getSrc() {
		return new SACNSrc(sourceName, priority);
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

	public boolean valid() {
		return valid;
	}
}
