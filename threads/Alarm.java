package nachos.threads;

import nachos.machine.*;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
    /**
     * Allocate a new Alarm. Set the machine's timer interrupt handler to this
     * alarm's callback.
     *
     * <p><b>Note</b>: Nachos will not function correctly with more than one
     * alarm.
     */
    public Alarm() {
	waitQueue = new PriorityQueue<TimeThread>();
	Machine.timer().setInterruptHandler(new Runnable() {
		public void run() { timerInterrupt(); }
	    });
    }

    /**
     * The timer interrupt handler. This is called by the machine's timer
     * periodically (approximately every 500 clock ticks). Causes the current
     * thread to yield, forcing a context switch if there is another thread
     * that should be run.
     */
    public void timerInterrupt() {
        boolean intStatus = Machine.interrupt().disable();
	if (waitQueue.isEmpty()) {
            return;
	}

	while (!waitQueue.isEmpty() 
	       && waitQueue.peek().wakeTime() <= System.currentTimeMillis()) {
	    waitQueue.poll().wakeThread().ready();
	}
	Machine.interrupt().restore(intStatus);
	KThread.currentThread().yield();
    }

    /**
     * Put the current thread to sleep for at least <i>x</i> ticks,
     * waking it up in the timer interrupt handler. The thread must be
     * woken up (placed in the scheduler ready set) during the first timer
     * interrupt where
     *
     * <p><blockquote>
     * (current time) >= (WaitUntil called time)+(x)
     * </blockquote>
     *
     * @param	x	the minimum number of clock ticks to wait.
     *
     * @see	nachos.machine.Timer#getTime()
     */
    public void waitUntil(long forMS) {
        
        boolean intStatus = Machine.interrupt().disable();	    
        
        TimeThread timeThread = new TimeThread(KThread.currentThread(),
			forMS);
	waitQueue.insert(timeThread);
	KThread.sleep();

        Machine.interrupt().restore(intStatus);

    }

    /**
     * Tests that the Alarm module works.
     */
    public static void selfTest(){
        final KThread t1 = new KThread(createTimedThread(200))
		.setName("[T1 - Alarm]");
        final KThread t2 = new KThread(createTimedThread(300))
		.setName("[T2 - Alarm]");
        final KThread t3 = new KThread(createTimedThread(500))
		.setName("[T3 - Alarm]");
        final KThread t4 = new KThread(createTimedThread(400))
		.setName("[T4 - Alarm]");
        final KThread t5 = new KThread(createTimedThread(1000))
		.setName("[T5 - Alarm]");
        final KThread t6 = new KThread(createTimedThread(-10000))
		.setName("[T6 - Alarm]");
	t1.fork();
	t1.join();
	t2.fork();
	t3.fork();
	t4.fork();
	t5.fork();
	t5.join();
	t6.fork();
	t6.join();
    }

    private static Runnable createTimedThread(final long forMS){
        return new Runnable(){
            public void run(){
                Lib.debug(dbgAl, KThread.currentThread().getName()
			       + " started alarm for " + forMS + "ms");
		ThreadedKernel.alarm.waitUntil(forMS);
		Lib.debug(dbgAl, KThread.currentThread().getName()
				+ " has been awoken after " + forMS + "ms");
	    }
	};
    }


    private static final char dbgAl = 'l';

    private PriorityQueue<TimeThread> waitQueue;

}
