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
	messageSet = false;
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
        
	speakCount ++;
        while (listenCount == 0 || messageSet){
            speakCondition.sleep();
	}

	message = word;
	messageSet = true;
        listenCondition.wake();
	speakCount --;
	communicatorLock.release();

    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */
    public int listen() {
        communicatorLock.acquire();
        
	listenCount ++;
        if (speakCount > 0) {
            speakCondition.wake();
	}

        while (!messageSet) {
            listenCondition.sleep();
	}

	listenCount --;

	int localMessage = message;
        messageSet = false;
	speakCondition.wake();
	communicatorLock.release();
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

        final KThread t5 = new KThread(createListenRunnable(communicator))
		.setName("[T5 - Communicator]");

        final KThread t6 = new KThread(createSpeakerRunnable(communicator, 10))
		.setName("[T6 - Communicator]");

	t1.fork();
	t3.fork();
	t5.fork();
	t2.fork();
	t4.fork();
	t6.fork();
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
    private boolean messageSet;

}
