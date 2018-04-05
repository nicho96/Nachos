package nachos.userprog;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;
import java.util.LinkedList;

public class FrameManager{
	int count;
	LinkedList<TranslationEntry> unallocated;
	Lock frameLock;
	TranslationEntry[] frameTable;

	public FrameManager(){
		count = 0;
		unallocated = new LinkedList<TranslationEntry>();
		frameLock = new Lock();
		frameTable = initializeFrames();
		for (TranslationEntry frame : frameTable){
			unallocate(frame);
		}
	}

	public TranslationEntry[] initializeFrames(){
		frameLock.acquire();
		int numPhysPages = Machine.processor().getNumPhysPages();
		frameTable = new TranslationEntry[numPhysPages];
		for(int i = 0; i < numPhysPages; i++){
			frameTable[i] = new TranslationEntry(0, i, false, false, false, false);
		}
		frameLock.release();
		return frameTable;
	}

	public int getCount(){
		return count;
	}

	public void unallocate(TranslationEntry page){
		frameLock.acquire();
		count++;
		page.valid = false;
		unallocated.addLast(page);
		frameLock.release();
	}

	public TranslationEntry allocate(){
		frameLock.acquire();
		count--;
		TranslationEntry frame = unallocated.removeFirst() ;
		frame.valid = true;
		frameLock.release();
		return frame;
	}

	public TranslationEntry[] allocateMany(int request){
		TranslationEntry[] list = new TranslationEntry[request];
		for(int i = 0; i < request; i++){
			list[i] = allocate();
		}
		return list;
	}

	public boolean isAvailable(int request){
		return count >= request;
	}


}
