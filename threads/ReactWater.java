package nachos.threads;
  
import java.util.LinkedList;

import nachos.machine.*;

public class ReactWater { 
// Declaring Variables

	Lock waterLock;
	private static int hCount, oCount;
	Condition2 hCondition;
	Condition2 oCondition;
// Constructor
	public ReactWater(){
		hCount = 0;
		oCount = 0;
		waterLock = new Lock();
		hCondition = new Condition2(waterLock);
		oCondition = new Condition2(waterLock);
	}	
// oReady, hReady and makeWater functions
	public void oReady(){
		if (hCount >= 2){
			waterLock.acquire();
			hCondition.wake();
			waterLock.acquire();
			hCondition.wake();
			hCount -= 2;
			makeWater();		
		} else {
			oCount += 1;
			waterLock.acquire();
			oCondition.sleep();		
		}
		waterLock.release();
	}
	public void hReady(){
		if (hCount > 0 && oCount > 0){
			waterLock.acquire();
			hCondition.wake();
			waterLock.acquire();
			oCondition.wake();
			hCount -= 1;
			oCount -= 1; 
			makeWater();
		} else {
			hCount += 1;
			waterLock.acquire();
			hCondition.sleep();
		}
		waterLock.release();
	}	
	public void makeWater(){
		System.out.println("Water was made!");
	}


 public static void selfTest() {
	ReactWater r1 = new ReactWater();
	r1.oReady();
	r1.oReady();
	r1.hReady();

}
	









}
