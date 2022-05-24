package sacnReciver;

import java.util.HashMap;

/**
 * Source of sACN Data
 * @author Peter Crall
 *
 */
public class SACNSrc {
	
	/**
	 * ACN CID
	 */
	public long cid;
	/**
	 * sACN Priority of source
	 */
	public int prio;
	/**
	 * Display name
	 */
	public String name;
	/**
	 * Last heard from time<br><br>
	 * unix milisecond timestamp
	 */
	public long lTime;
	/**
	 * Source timeout time
	 */
	public static int TIMEOUT = 0 * 1000;
	
	protected HashMap<Integer, int[]> chPrio;
	
	/**
	 * Creates a new SACNSrc
	 * @param cid : ACN CID of source
	 * @param name : Display name of source
	 * @param prio : sACN priority
	 */
	public SACNSrc(long cid, String name, int prio) {
		this.cid = cid;
		this.prio = prio;
		this.name = name;
		lTime = System.currentTimeMillis();
		chPrio = new HashMap<Integer, int[]>();
	}
	
	/**
	 * If this source has a higher priority, and has not timed out
	 * @param o : the source to check against
	 * @return If this source has a higher priority, and has not timed out
	 */
	public boolean isHigher(SACNSrc o, int uni, int adr) {
		if(chPrio.containsKey(uni)) {
			if(o.chPrio.containsKey(uni)) {
				if(o.chPrio.get(uni)[adr] > chPrio.get(uni)[adr]) return false;
			} else {
				if(o.prio > chPrio.get(uni)[adr]) return false;
			}
		} else {
			if(o.chPrio.containsKey(uni)) {
				if(o.chPrio.get(uni)[adr] > prio) return false;
			} else {
				if(o.prio > prio) return false;
			}
		}
		long cTime = System.currentTimeMillis();
		if(lTime + TIMEOUT < cTime) {
			return false;
		}
		return true;
	}
	
	public void setChPrio(SACNPacket packet) {
		if(packet.startCode != 0xDD) return;
		chPrio.put(packet.universe, packet.dmx); 
	}
	
	/**
	 * Sets last recived time to now
	 */
	public SACNSrc setNow() {
		lTime = System.currentTimeMillis();
		return this;
	}
	
	@Override
	public String toString() {
		String str = "";
		str += "name="+name+";";
		str += "priority="+prio+";";
		return str;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof SACNSrc)) {
			return false;
		}
		SACNSrc o = (SACNSrc)obj;
		if(o.cid != cid) {
			return false;
		}
		return true;
	}
}
