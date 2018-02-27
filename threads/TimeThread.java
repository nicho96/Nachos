public class TimeThread implements Comparable<TimeThread>{
// Declearing Variables
	private KThread wakeThread;
	private Long wakeTime;
// TimeThread Constructor takes a KThread and a time in ms.
	public TimeThread(KThread thread, Long timeinMs){
		wakeThread = thread;
		wakeTime = timeInMs + System.currentTimeMillis();
	}
// Simple compareTo from Comparable interface	
	 public int compareTo(TimeThread other){
		if (other.wakeTime < this.wakeTime) return 1;
		else if (other.wakeTime > this.wakeTime) return -1;
		else return 0;
    	}

}
