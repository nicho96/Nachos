package nachos.threads;

import nachos.machine.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator {
    /**
     * Allocate a new communicator.
     */
    public Communicator() {
        communicatorLock = new Lock();
	speakCondition = new Condition2(communicatorLock);
	listenCondition = new Condition2(communicatorLock);
	listenCount = 0;
	speakCount = 0;
    }

    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listener.
     *
     * <p>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listener should receive <i>word</i>.
     *
     * @param	word	the integer to transfer.
     */
    public void speak(int word) {
        communicatorLock.acquire();
	Lib.debug(dbgComm, KThread.currentThread().getName() + " listen count:" + listenCount);
	if (listenCount > 0){
	    Lib.debug(dbgComm, "Setting message to " + word);
	    message = word;
            listenCondition.wake();
	    communicatorLock.release();
	} else {
	    Lib.debug(dbgComm, KThread.currentThread().getName() + " going to sleep.");
	    speakCondition.sleep();
	    Lib.debug(dbgComm, "Setting message to " + word);
	    message = word;
            communicatorLock.release();
	}
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */
    public int listen() {
        communicatorLock.acquire();
	Lib.debug(dbgComm, KThread.currentThread().getName() + " speak count:" + speakCount);
        if (speakCount > 0){
            speakCondition.wake();
	    communicatorLock.release();
            KThread.currentThread().yield();
        } else {
	    listenCount ++;
            listenCondition.sleep();
	    communicatorLock.release();
	    listenCount --;
        }
        return message;
    }

    public static final void selfTest(){
	Communicator communicator = new Communicator();
        final KThread t1 = new KThread(createListenRunnable(communicator))
		.setName("[T1 - Communicator]");
	
        final KThread t2 = new KThread(createSpeakerRunnable(communicator, 6))
		.setName("[T2 - Communicator]");
       
        final KThread t3 = new KThread(createListenRunnable(communicator))
		.setName("[T3 - Communicator]");

        final KThread t4 = new KThread(createSpeakerRunnable(communicator, 8))
		.setName("[T4 - Communicator]");

	t1.fork();
	t3.fork();
	t2.fork();
	t4.fork();
    }

    /**
     * Create a Runnable instance for the listener.
     */
    private static Runnable createListenRunnable(final Communicator comm){
        return new Runnable(){
            public void run(){
		String name = KThread.currentThread().getName();
		Lib.debug(dbgComm, name + " is listening");
                int word = comm.listen();
                Lib.debug(dbgComm, name + " received the word " + word);
	    }
	};
    }

    /**
     * Create a Runnable instance for the speaker. 
     */
    private static Runnable createSpeakerRunnable(final Communicator comm,
	   int word){
        return new Runnable(){
            public void run(){	
		String name = KThread.currentThread().getName();
	        Lib.debug(dbgComm, name + " is speaking with word: " + word);
	        comm.speak(word);
		Lib.debug(dbgComm, name + " has spoken with word: " + word);
	    }
	};
    }

    private static final char dbgComm = 'k';

    private Lock communicatorLock;
    private Condition2 speakCondition;
    private Condition2 listenCondition;
    private int listenCount;
    private int speakCount;
    private int message;


}
