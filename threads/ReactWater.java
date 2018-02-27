package nachos.threads;
  
import java.util.LinkedList;

import nachos.machine.*;

public class ReactWater { 
// Declaring Variables

	private Lock waterLock;
	private static int hCount, oCount;
	private Condition2 hCondition;
	private Condition2 oCondition;
	private static final char dbgReact = 'r';

// Constructor
/*
*	ReactWater constructor initializes oCount, hCount, waterLock, hCondition and oCondition.
*
*
*/
	public ReactWater(){
		hCount = 0;
		oCount = 0;
		waterLock = new Lock();
		hCondition = new Condition2(waterLock);
		oCondition = new Condition2(waterLock);
	}	

// oReady, hReady and makeWater functions
/*
*	oReady:
*	oReady will check to see if hCount >= 2. if it is, it will wake two hThreads, decrement hCount by 2 and makeWater.
*	if hCount < 2, it will increment oCount by 1 and sleep the thread using oCondition.
*
*/
	public void oReady(){
		waterLock.acquire();
		if (hCount >= 2){ // There are 2 Hydrogen threads that are ready to combine with the current thread calling oReady
			hCondition.wake();
			hCondition.wake();
			hCount -= 2;
		Lib.debug(dbgReact, "hCount--; hCount--; Making Water! -- hCount: " + hCount + "; oCount: " + oCount);
			makeWater(); // Water was made and the 2 Hydrogen threads are awoken. hCount is decremented by 2.		
		} else {
			oCount += 1; // If there are not 2 Hydrogen threads, water is not ready to be made. Increment oCount and sleep the thread.
		Lib.debug(dbgReact,  KThread.currentThread().getName() + " is sleeping. oCount++; -- hCount: " + hCount + "; oCount: " + oCount);
			oCondition.sleep();		
		}
		waterLock.release();
	}

/*
*	hReady:
*	hReady will check to see if hCount > 0 AND oCount > 0. If it is, it will wake one oThread and hThread, decrement hCount and oCount by 1, and makeWater.
*	if not true, it will increment hCount by 1 and sleep the thread using hCondition.
*
*/
	public void hReady(){
		waterLock.acquire();
		if (hCount > 0 && oCount > 0){ // There is at least 1 Hydrogen and Oxygen thread ready to combine with the current thread calling hReady 
			hCondition.wake();
			oCondition.wake();
			hCount -= 1;
			oCount -= 1; 
		Lib.debug(dbgReact, "hCount--; oCount--; Making Water! -- hCount: " + hCount + "; oCount: " + oCount);
			makeWater(); // Water was made and one of each Oxygen and Hydrogen threads are awoken. hCount and oCount are decremented by 1.
		} else {
			hCount += 1; // If there are not one of each thread type, water is not ready to be made. Increment hCount and sleep the thread.
		Lib.debug(dbgReact,  KThread.currentThread().getName() + " is sleeping. hCount++; -- hCount: " + hCount + "; oCount: " + oCount);
			hCondition.sleep();
		}
		waterLock.release();
	}	

/*
*	makeWater:
*	prints "Water was made!"
*
*/
	public void makeWater(){
		System.out.println("Water was made!");
	}

public static final void selfTest(){
        ReactWater re = new ReactWater();
        final KThread t1 = new KThread(createOxygenRunnable(re))
                .setName("[T1 - ReactWater]");

        final KThread t2 = new KThread(createOxygenRunnable(re))
                .setName("[T2 - ReactWater]");

        final KThread t3 = new KThread(createHydrogenRunnable(re))
                .setName("[T3 - ReactWater]");

        final KThread t4 = new KThread(createHydrogenRunnable(re))
                .setName("[T4 - ReactWater]");

        final KThread t5 = new KThread(createOxygenRunnable(re))
                .setName("[T5 - ReactWater]");

        final KThread t6 = new KThread(createOxygenRunnable(re))
                .setName("[T6 - ReactWater]");

        final KThread t7 = new KThread(createOxygenRunnable(re))
                .setName("[T7 - ReactWater]");

        final KThread t8 = new KThread(createHydrogenRunnable(re))
                .setName("[T8 - ReactWater]");

        final KThread t9 = new KThread(createHydrogenRunnable(re))
                .setName("[T9 - ReactWater]");

        final KThread t10 = new KThread(createHydrogenRunnable(re))
                .setName("[T10 - ReactWater]");

        final KThread t11 = new KThread(createHydrogenRunnable(re))
                .setName("[T11 - ReactWater]");

        final KThread t12 = new KThread(createHydrogenRunnable(re))
                .setName("[T12 - ReactWater]");

        t4.fork();
        t2.fork();
        t11.fork();
        t9.fork();
        t3.fork();
        t1.fork();
        t7.fork();
        t8.fork();
        t6.fork();
        t10.fork();
        t5.fork();
	t12.fork();
  }
/*
*	Creates a runnable thread for oReady
*/
 private static Runnable createOxygenRunnable(final ReactWater re){
        return new Runnable(){
            public void run(){
                String name = KThread.currentThread().getName();
		Lib.debug(dbgReact, name + " has called oReady.");
                re.oReady();
            }
        };
    }
	
/*
*	Creates a runnable thread for hReady
*/
 private static Runnable createHydrogenRunnable(final ReactWater re){
        return new Runnable(){
            public void run(){
                String name = KThread.currentThread().getName();
		Lib.debug(dbgReact, name + " has called hReady.");
                re.hReady();
            }
        };
    }








}
