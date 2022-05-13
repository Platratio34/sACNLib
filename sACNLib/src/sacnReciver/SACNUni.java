package sacnReciver;

/**
 * One universe of sACN data
 * @author Peter Crall
 *
 */
public class SACNUni {
	
	/**
	 * DMX data
	 */
	public int[] dmx;
	/**
	 * Per address source
	 */
	public SACNSrc[] src;
	
	/**
	 * Creates a new SACNUni
	 */
	public SACNUni() {
		dmx = new int[512];
		src = new SACNSrc[512];
	}
	
	/**
	 * Tries to set the data at address, adhearing to priority and source rules
	 * @param adr : the address of the data
	 * @param data : the new data
	 * @param source : the source for the new data
	 * @return If new data overroad the old data
	 */
	public boolean trySetDmx(int adr, int data, SACNSrc source) {
		if(src[adr] == null) {
			dmx[adr] = data;
//			src[adr] = source;
			return true;
		}
		if(src[adr].equals(source)) {
			dmx[adr] = data;
			return true;
		}
		if(src[adr].isHigher(source)) {
			return false;
		}
		if(source.isHigher(src[adr])) {
			dmx[adr] = data;
//			src[adr] = source;
			return true;
		}
		if(dmx[adr] < data) {
			dmx[adr] = data;
//			src[adr] = source;
		}
		return true;
	}
	/**
	 * Tries to set the DMX data, adhearing to priority and source rules
	 * @param data : the new data
	 * @param source : the source for the data
	 * @return true
	 */
	public boolean trySetDmx(int[] data, SACNSrc source) {
		for(int i = 0; i < 512; i++) {
			trySetDmx(i, data[i], source);
		}
		return true;
	}
	
	/**
	 * Returns the DMX data array
	 * @return The DMX data array
	 */
	public int[] getDmx() {
		int[] d = new int[512];
		for(int i = 0; i < 512; i++) {
			d[i] = dmx[i];
		}
		return d;
	}
	/**
	 * Returns the DMX value at address
	 * @param adr : the address to get
	 * @return The DMX value (0-255)
	 */
	public int getDmx(int adr) {
		return dmx[adr];
	}
	
	@Override
	public String toString() {
		return Reciver.printArr(dmx);
	}
}
