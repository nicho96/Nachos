public class TimeThread implements Comparable<TimeThread>{
	private KThread wakeThread;
	private Long wakeTime;

	public TimeThread(KThread thread, Long timeinMs){
		wakeThread = thread;
		wakeTime = timeInMs + System.currentTimeMillis();
	}
	
	 public int compareTo(TimeThread other){
		if (other.wakeTime < this.wakeTime) return 1;
		else if (other.wakeTime > this.wakeTime) return -1;
		else return 0;
    	}

}
