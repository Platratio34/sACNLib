package sacnReciver;

public class SACNUni {
	
	public int[] dmx;
	public SACNSrc[] src;
	
	public SACNUni() {
		dmx = new int[512];
		src = new SACNSrc[512];
	}
	
	public boolean trySetDmx(int adr, int data, SACNSrc source) {
		if(src[adr] == null) {
			dmx[adr] = data;
			src[adr] = source;
		}
		if(src[adr].equals(source)) {
			dmx[adr] = data;
		}
		if(src[adr].isHigher(source)) {
			return false;
		}
		if(source.isHigher(src[adr])) {
			dmx[adr] = data;
			src[adr] = source;
			return true;
		}
		if(dmx[adr] < data) {
			dmx[adr] = data;
			src[adr] = source;
		}
		return true;
	}
	public boolean trySetDmx(int[] data, SACNSrc source) {
		for(int i = 0; i < 512; i++) {
			trySetDmx(i, data[i], source);
		}
		return true;
	}
	
	public int[] getDmx() {
		int[] d = new int[512];
		for(int i = 0; i < 512; i++) {
			d[i] = dmx[i];
		}
		return d;
	}
	public int getDmx(int adr) {
		return dmx[adr];
	}
	
	@Override
	public String toString() {
		return Reciver.printArr(dmx);
	}
}
