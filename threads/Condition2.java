package nachos.threads;

import java.util.LinkedList;

import nachos.machine.*;

/**
 * An implementation of condition variables that disables interrupt()s for
 * synchronization.
 *
 * <p>
 * You must implement this.
 *
 * @see	nachos.threads.Condition
 */
public class Condition2 {
    /**
     * Allocate a new condition variable.
     *
     * @param	conditionLock	the lock associated with this condition
     *				variable. The current thread must hold this
     *				lock whenever it uses <tt>sleep()</tt>,
     *				<tt>wake()</tt>, or <tt>wakeAll()</tt>.
     */
    public Condition2(Lock conditionLock) {
	this.conditionLock = conditionLock;
	this.threadQueue = new LinkedList<KThread>(); 
    }

    /**
     * Atomically release the associated lock and go to sleep on this condition
     * variable until another thread wakes it using <tt>wake()</tt>. The
     * current thread must hold the associated lock. The thread will
     * automatically reacquire the lock before <tt>sleep()</tt> returns.
     */
    public void sleep() {
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
	
	boolean intStatus = Machine.interrupt().disable();
	
        threadQueue.add(KThread.currentThread());
	conditionLock.release();
	KThread.currentThread().sleep();
	conditionLock.acquire();

	Machine.interrupt().restore(intStatus);
    }

    /**
     * Wake up at most one thread sleeping on this condition variable. The
     * current thread must hold the associated lock.
     */
    public void wake() {
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());

        boolean intStatus = Machine.interrupt().disable();

        KThread thread = threadQueue.pollFirst();
	if (thread != null) {
	    Lib.debug(dbgCond, "Readying " + thread.getName());
            thread.ready();
	} else {
            Lib.debug(dbgCond, "Thread queue is empty.");
	}

	Machine.interrupt().restore(intStatus);
    }

    /**
     * Wake up all threads sleeping on this condition variable. The current
     * thread must hold the associated lock.
     */
    public void wakeAll() {
	while (threadQueue.size() > 0) {
	    wake();
	}
    }

    /**
     * Tests whether Condition2 module is working properly.
     */
    public static void selfTest(){
        final Lock lock = new Lock();
        final Condition2 condition = new Condition2(lock);

        final KThread t1 = new KThread(generateSleepRunnable(lock, condition))
		.setName("[T1 - Condition]");
        final KThread t2 = new KThread(generateSleepRunnable(lock, condition))
		.setName("[T2 - Condition]");	
        final KThread t3 = new KThread(generateSleepRunnable(lock, condition))
		.setName("[T3 - Condition]");
        final KThread t4 = new KThread(new Runnable(){
            public void run(){
                lock.acquire();
		Lib.debug(dbgCond, "Waking one thread");
	       	condition.wake();
		Lib.debug(dbgCond, "Waking all remaining threads");
		condition.wakeAll();
		lock.release();
	    }
	}).setName("[T4 - Condition]");	

	t1.fork();
	t2.fork();
	t3.fork();
        t4.fork();

    }

    private static Runnable generateSleepRunnable(final Lock lock, 
		    final Condition2 condition){
	return new Runnable(){
            public void run(){
		String name = KThread.currentThread().getName();
		Lib.debug(dbgCond, name + " is going to sleep");
		lock.acquire();
                condition.sleep();	
		Lib.debug(dbgCond, name + " has woken up");
	   	lock.release();
	    }
	};
    }

    private static final char dbgCond = 'x';

    private Lock conditionLock;
    private LinkedList<KThread> threadQueue;
}
