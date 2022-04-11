package sacnReciver;

public class SACNPacket {
	
	public int preSize;
	public int postSize;
	public String sourceName;
	public int priority;
	public int universe;
	public int startCode;
	public int[] dmx;
	
	public SACNPacket(byte[] arr) {
		preSize = arr[1] + arr[0]*0x100;
		postSize = arr[3] + arr[2]*0x100;
		
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
		startCode = arr[0x7D] & 0xff;
		
		dmx = new int[512];
		for(int i = 0; i < 512; i++) {
			dmx[i] = arr[0x7E+i] & 0xff;
		}
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
}
