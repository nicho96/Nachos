package nachos.threads;

public class TimeThread implements Comparable<TimeThread>{
// Declearing Variables
	private KThread wakeThread;
	private long wakeTime;
// TimeThread Constructor takes a KThread and a time in ms.
	public TimeThread(KThread thread, Long timeMS){
		wakeThread = thread;
		wakeTime = timeMS + Machine.timer().getTime();
	}
// Simple compareTo from Comparable interface	
	 public int compareTo(TimeThread other){
		if (other.wakeTime < this.wakeTime) return -1;
		else if (other.wakeTime > this.wakeTime) return 1;
		else return 0;
    	}

	public long wakeTime(){
            return wakeTime;
	}

	public KThread wakeThread(){
            return wakeThread;
	}

}
