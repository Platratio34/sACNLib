package sacnReciver;

public class SACNSrc {
	
	public int prio;
	public String name;
	public long lTime;
	public static int TIMEOUT = 30000;
	
	public SACNSrc(String name, int prio) {
		this.prio = prio;
		this.name = name;
		lTime = System.currentTimeMillis();
	}
	
	public boolean isHigher(SACNSrc o) {
		if(o.prio > prio) {
			return false;
		}
		long cTime = System.currentTimeMillis();
		if(lTime + TIMEOUT < cTime) {
			return false;
		}
		return true;
	}
	
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
		if(!o.name.equals(name)) {
			return false;
		}
		return true;
	}
}
