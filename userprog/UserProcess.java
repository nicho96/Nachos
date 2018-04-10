package nachos.userprog;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.ArrayList;

import java.io.EOFException;

/**
 * Encapsulates the state of a user process that is not contained in its
 * user thread (or threads). This includes its address translation state, a
 * file table, and information about the program being executed.
 *
 * <p>
 * This class is extended by other classes to support additional functionality
 * (such as additional syscalls).
 *
 * @see	nachos.vm.VMProcess
 * @see	nachos.network.NetProcess
 */
public class UserProcess {
    /**
     * Allocate a new process.
     */
    public UserProcess() {
		processId = globalThreadID ++;
		pageLock = new Lock();
		memoryLock = new Lock();
		int numPhysPages = Machine.processor().getNumPhysPages();
		pageTable = new TranslationEntry[numPhysPages];
		for (int i=0; i<numPhysPages; i++)
	    	pageTable[i] = new TranslationEntry(i,i, true,false,false,false);

		// First time, open the console files
		if(openFiles[0] == null) {
			openFiles[0] = UserKernel.console.openForReading();
		   	openFiles[1] = UserKernel.console.openForWriting();
		}

		localOpenFiles[0] = openFiles[0];
		localOpenFiles[1] = openFiles[1];

		referenceCount[0] += 1;
		referenceCount[1] += 1;
	}

    public void selfTest(){

	    System.out.println("TESTING FOR TASK 2." + '\n'+ "***************************");

		//Basic single page read/write test
		readWriteTest();

		//Write more than a pages worth of bytes to memory
		writeMultiPageTest();

		//Read more than a pages worth of bytes from memory	readMultiPageTest();

		//Write more than numPages (max 8) pages worth of bytes to memory
		writeMoreThanMaxTest();

		//Read more than numPages (max 8) pages worth of bytes from memory
		readMoreThanMaxTest();

	    System.out.println("***************************" +  '\n' + "TESTING FOR TASK 2 COMPLETE." );
    }

	public void readWriteTest(){
		byte[] data = {'T','E','S','T',' ','F','O','R',' ','T', 'A', 'S', 'K', '2'};
		byte[] buffer = new byte[14];

		System.out.println("Writing \"" + new String(data) + "\" to virtual memory");

		System.out.println("Reading from virtual memory");
		readVirtualMemory(0,buffer,0,14);

		System.out.println("Basic Read/Write Test: " + new String(buffer));
	}

	public int writeMultiPageTest(){
		System.out.println("Writing to more than 1 page: ");
		System.out.println("Trying to write " + (pageSize+3) + " bytes");
		byte[] overFlow = new byte[pageSize + 3];

		for(int i = 0; i < pageSize; i++)
			overFlow[i] = (byte)(65);

		overFlow[pageSize] = 'Y';
		overFlow[pageSize+1] = 'E';
		overFlow[pageSize+2] = 'S';
		int bytesWritten = writeVirtualMemory(0, overFlow, 0, overFlow.length);

		System.out.println("Bytes Written: " + bytesWritten);

		return bytesWritten;
	}

	public int readMultiPageTest(){
		System.out.println("Reading from more than 1 page: ");
		System.out.println("Trying to read " + (pageSize+3) + " bytes");
		byte[] overFlow = new byte[pageSize + 3];
		int bytesRead = readVirtualMemory(0,overFlow,0,overFlow.length);

		byte[] last3 = new byte[3];
		last3[0] = overFlow[pageSize];
		last3[1] = overFlow[pageSize+1];
		last3[2] = overFlow[pageSize+2];

		//Last3 should be empty since the remaining 3 bytes exceed vpn0
		System.out.println("Bytes Read: " + bytesRead);
		System.out.println("Read OverFlow Test: " + new String(last3));

		for(int i = 0; i < last3.length; ++i)
			last3[i] = 0;

		//Read the first 3 bytes of vpn 1, should read YES
		bytesRead = readVirtualMemory(pageSize, last3, 0, last3.length);
		System.out.println("Read Next Page Test: " + new String(last3));

		return bytesRead;
	}

	public int writeMoreThanMaxTest(){
		System.out.println("Writing to more than " + numPages + "(numPages) pages: ");
		System.out.println("Trying to write " + (pageSize*numPages+1) + " bytes");

		byte[] tooBig = new byte[(pageSize*numPages)+1];
		for(int i = 0; i < tooBig.length; i++){
			tooBig[i] = (byte)(66);
		}

		int bytesWritten = writeVirtualMemory(0, tooBig, 0, tooBig.length);
		System.out.println("Bytes Written: " + bytesWritten);

		return bytesWritten;
	}

	public int readMoreThanMaxTest(){
		System.out.println("Reading more than " + numPages + "(numPages) pages: ");
		System.out.println("Trying to read " + (pageSize*numPages+1) + " bytes");

		byte[] tooBig = new byte[(pageSize*numPages)+1];

		int bytesRead = readVirtualMemory(0, tooBig, 0, tooBig.length);
		System.out.println("Bytes Read: " + bytesRead);

		return bytesRead;
	}


    /**
 /    * Allocate and return a new process of the correct class. The class name
     * is specified by the <tt>nachos.conf</tt> key
     * <tt>Kernel.processClassName</tt>.
     *
     * @return	a new process of the correct class.
     */
    public static UserProcess newUserProcess() {
		return (UserProcess)Lib.constructObject(Machine.getProcessClassName());
    }

    /**
     * Execute the specified program with the specified arguments. Attempts to
     * load the program, and then forks a thread to run it.
     *
     * @param	name	the name of the file containing the executable.
     * @param	args	the arguments to pass to the executable.
     * @return	<tt>true</tt> if the program was successfully executed.
     */
    public boolean execute(String name, String[] args) {
		if (!load(name, args))
			return false;
		UThread t = new UThread(this);
		t.setName(name);
		t.fork();
		t.join();
		return true;
    }

    /**
     * Save the state of this process in preparation for a context switch.
     * Called by <tt>UThread.saveState()</tt>.
     */
    public void saveState() {
    }

    /**
     * Restore the state of this process after a context switch. Called by
     * <tt>UThread.restoreState()</tt>.
     */
    public void restoreState() {
	Machine.processor().setPageTable(pageTable);
    }

    /**
     * Read a null-terminated string from this process's virtual memory. Read
     * at most <tt>maxLength + 1</tt> bytes from the specified address, search
     * for the null terminator, and convert it to a <tt>java.lang.String</tt>,
     * without including the null terminator. If no null terminator is found,
     * returns <tt>null</tt>.
     *
     * @param	vaddr	the starting virtual address of the null-terminated
     *			string.
     * @param	maxLength	the maximum number of characters in the string,
     *				not including the null terminator.
     * @return	the string read, or <tt>null</tt> if no null terminator was
     *		found.
     */
    public String readVirtualMemoryString(int vaddr, int maxLength) {

	Lib.assertTrue(maxLength >= 0);

	byte[] bytes = new byte[maxLength+1];

	int bytesRead = readVirtualMemory(vaddr, bytes);

	for (int length=0; length<bytesRead; length++) {
	    if (bytes[length] == 0)
			return new String(bytes, 0, length);
	}

	return null;
    }

    /**
     * Transfer data from this process's virtual memory to all of the specified
     * array. Same as <tt>readVirtualMemory(vaddr, data, 0, data.length)</tt>.
     *
     * @param	vaddr	the first byte of virtual memory to read.
     * @param	data	the array where the data will be stored.
     * @return	the number of bytes successfully transferred.
     */
    public int readVirtualMemory(int vaddr, byte[] data) {
	return readVirtualMemory(vaddr, data, 0, data.length);
    }

    /**
     * Transfer data from this process's virtual memory to the specified array.
     * This method handles address translation details. This method must
     * <i>not</i> destroy the current process if an error occurs, but instead
     * should return the number of bytes successfully copied (or zero if no
     * data could be copied).
     *
     * @param	vaddr	the first byte of virtual memory to read.
     * @param	data	the array where the data will be stored.
     * @param	offset	the first byte to write in the array.
     * @param	length	the number of bytes to transfer from virtual memory to
     *			the array.
     * @return	the number of bytes successfully transferred.
     */
    public int readVirtualMemory(int vaddr, byte[] data, int offset,
				 int length) {
	Lib.assertTrue(offset >= 0 && length >= 0 && offset+length <= data.length);
	memoryLock.acquire();
	numPages = 8;
	int vPage = Processor.pageFromAddress(vaddr);
	int pgOffset = Processor.offsetFromAddress(vaddr);
	int bytesLeftToCopy = length;
	int buffOffset = offset;
	byte[] memory = Machine.processor().getMemory();
	while(bytesLeftToCopy > 0 && vPage < numPages ){
		int bytesToEndOfPage = pageSize - pgOffset;
		int bytesToCopy = Math.min(bytesToEndOfPage, bytesLeftToCopy);
		int physAddr = Processor.makeAddress(pageTable[vPage].ppn, pgOffset);
		System.arraycopy(memory, physAddr, data, buffOffset, bytesToCopy);
		bytesLeftToCopy -= bytesToCopy;
		vPage++;
		buffOffset += bytesToCopy;
		pgOffset = 0;
	}
	int bytesCopied = length - bytesLeftToCopy;
	memoryLock.release();
	return bytesCopied;
    }

    /**
     * Transfer all data from the specified array to this process's virtual
     * memory.
     * Same as <tt>writeVirtualMemory(vaddr, data, 0, data.length)</tt>.
     *
     * @param	vaddr	the first byte of virtual memory to write.
     * @param	data	the array containing the data to transfer.
     * @return	the number of bytes successfully transferred.
     */
    public int writeVirtualMemory(int vaddr, byte[] data) {
	return writeVirtualMemory(vaddr, data, 0, data.length);
    }

    /**
     * Transfer data from the specified array to this process's virtual memory.
     * This method handles address translation details. This method must
     * <i>not</i> destroy the current process if an error occurs, but instead
     * should return the number of bytes successfully copied (or zero if no
     * data could be copied).
     *
     * @param	vaddr	the first byte of virtual memory to write.
     * @param	data	the array containing the data to transfer.
     * @param	offset	the first byte to transfer from the array.
     * @param	length	the number of bytes to transfer from the array to
     *			virtual memory.
     * @return	the number of bytes successfully transferred.
     */
    public int writeVirtualMemory(int vaddr, byte[] data, int offset,
				  int length) {

	Lib.assertTrue(offset >= 0 && length >= 0 && offset+length <= data.length);
	memoryLock.acquire();
	numPages = 8;
	int vPage = Processor.pageFromAddress(vaddr);
	int pgOffset = Processor.offsetFromAddress(vaddr);
	int bytesLeftToCopy = length;
	int buffOffset = offset;
	byte[] memory = Machine.processor().getMemory();
	while(bytesLeftToCopy > 0 && vPage < numPages ){
		int bytesToEndOfPage = pageSize - pgOffset;
		int bytesToCopy = Math.min(bytesToEndOfPage, bytesLeftToCopy);
		int physAddr = Processor.makeAddress(pageTable[vPage].ppn, pgOffset);
		System.arraycopy(data, buffOffset, memory, physAddr, bytesToCopy);
		bytesLeftToCopy -= bytesToCopy;
		vPage++;
		buffOffset += bytesToCopy;
		pgOffset = 0;
	}
	int bytesCopied = length - bytesLeftToCopy;
	memoryLock.release();
	return bytesCopied;
    }

    /**
     * Load the executable with the specified name into this process, and
     * prepare to pass it the specified arguments. Opens the executable, reads
     * its header information, and copies sections and arguments into this
     * process's virtual memory.
     *
     * @param	name	the name of the file containing the executable.
     * @param	args	the arguments to pass to the executable.
     * @return	<tt>true</tt> if the executable was successfully loaded.
     */
    private boolean load(String name, String[] args) {
	Lib.debug(dbgProcess, "UserProcess.load(\"" + name + "\")");

	OpenFile executable = ThreadedKernel.fileSystem.open(name, false);
	if (executable == null) {
	    Lib.debug(dbgProcess, "\topen failed");
	    return false;
	}

	try {
	    coff = new Coff(executable);
	}
	catch (EOFException e) {
	    executable.close();
	    Lib.debug(dbgProcess, "\tcoff load failed");
	    return false;
	}

	// make sure the sections are contiguous and start at page 0
	numPages = 0;
	for (int s=0; s<coff.getNumSections(); s++) {
	    CoffSection section = coff.getSection(s);
	    if (section.getFirstVPN() != numPages) {
		coff.close();
		Lib.debug(dbgProcess, "\tfragmented executable");
		return false;
	    }
	    numPages += section.getLength();
	}

	// make sure the argv array will fit in one page
	byte[][] argv = new byte[args.length][];
	int argsSize = 0;
	for (int i=0; i<args.length; i++) {
	    argv[i] = args[i].getBytes();
	    // 4 bytes for argv[] pointer; then string plus one for null byte
	    argsSize += 4 + argv[i].length + 1;
	}
	if (argsSize > pageSize) {
	    coff.close();
	    Lib.debug(dbgProcess, "\targuments too long");
	    return false;
	}

	// program counter initially points at the program entry point
	initialPC = coff.getEntryPoint();

	// next comes the stack; stack pointer initially points to top of it
	numPages += stackPages;
	initialSP = numPages*pageSize;

	// and finally reserve 1 page for arguments
	numPages++;

	if (!loadSections())
	    return false;

	// store arguments in last page
	int entryOffset = (numPages-1)*pageSize;
	int stringOffset = entryOffset + args.length*4;

	this.argc = args.length;
	this.argv = entryOffset;

	for (int i=0; i<argv.length; i++) {
	    byte[] stringOffsetBytes = Lib.bytesFromInt(stringOffset);
	    Lib.assertTrue(writeVirtualMemory(entryOffset,stringOffsetBytes) == 4);

	    entryOffset += 4;
	    Lib.assertTrue(writeVirtualMemory(stringOffset, argv[i]) ==
		       argv[i].length);
	    stringOffset += argv[i].length;
	    Lib.assertTrue(writeVirtualMemory(stringOffset,new byte[] { 0 }) == 1);
	    stringOffset += 1;
	}
	return true;
    }

    /**
     * Allocates memory for this process, and loads the COFF sections into
     * memory. If this returns successfully, the process will definitely be
     * run (this is the last step in process initialization that can fail).
     *
     * @return	<tt>true</tt> if the sections were successfully loaded.
     */
    protected boolean loadSections() {
	if (numPages > Machine.processor().getNumPhysPages()) {
	    coff.close();
	    Lib.debug(dbgProcess, "\tinsufficient physical memory");
	    return false;
	}
	if(((UserKernel)Kernel.kernel).isAvailable(numPages)){
	pageLock.acquire();
	pageTable = ((UserKernel)Kernel.kernel).getPages(numPages);
	for(int i = 0; i < pageTable.length; i++)
			pageTable[i].vpn = i;

	// load sections
	for (int s=0; s<coff.getNumSections(); s++) {
	    CoffSection section = coff.getSection(s);

	    Lib.debug(dbgProcess, "\tinitializing " + section.getName()
		      + " section (" + section.getLength() + " pages)");

	    for (int i=0; i<section.getLength(); i++) {
		int vpn = section.getFirstVPN()+i;
		section.loadPage(i, pageTable[vpn].ppn);
	    }
	}

	pageLock.release();
	}
	else {
		coff.close();
		return false;
	}
	return true;
    }

    /**
     * Release any resources allocated by <tt>loadSections()</tt>.
     */
    protected void unloadSections() {
 	    ((UserKernel)Kernel.kernel).deallocate(pageTable);
    }


    /**
     * Initialize the processor's registers in preparation for running the
     * program loaded into this process. Set the PC register to point at the
     * start function, set the stack pointer register to point at the top of
     * the stack, set the A0 and A1 registers to argc and argv, respectively,
     * and initialize all other registers to 0.
     */
    public void initRegisters() {
	Processor processor = Machine.processor();

	// by default, everything's 0
	for (int i=0; i<processor.numUserRegisters; i++)
	    processor.writeRegister(i, 0);

	// initialize PC and SP according
	processor.writeRegister(Processor.regPC, initialPC);
	processor.writeRegister(Processor.regSP, initialSP);

	// initialize the first two argument registers to argc and argv
	processor.writeRegister(Processor.regA0, argc);
	processor.writeRegister(Processor.regA1, argv);
    }

	// Members to handle storing file reference
	private static OpenFile[] openFiles = new OpenFile[16];
	private static boolean[] unlinkedFiles = new boolean[16];
	private static int[] referenceCount = new int[16];
	private OpenFile[] localOpenFiles = new OpenFile[16];

	public void join(){
		joinLock.acquire();
		while (!exited) {
			joinCondition.sleep();
		}
		joinLock.release();
	}

    /**
     * Handle the halt() system call.
     */
    private int handleHalt() {
		if (processId == 0) {
			Machine.halt();
			return 0;
		}
		Lib.assertNotReached("Machine.halt() did not halt machine!");
		return -1;
    }

	private int handleExit(int statusCode) {

		Lib.debug(dbgSyscall, "Exit Status for " + KThread.currentThread().getName() + ": " + statusCode);

		joinLock.acquire();

		// Disown children
		for(UserProcess child : children) {
			child.pProcess = null;
		}
		children = null;

		for(int i = 0; i < 16; i++) {
			handleClose(i);
		}

		exitCode = statusCode;
		exited = true;

		joinCondition.wakeAll();
		unloadSections();

		joinLock.release();

		if (processId == 0) {
			handleHalt();
		}
		KThread.finish();
		return 0;
	}

	private int handleExec(int namePtr, int argc, int argvPtr) {
		String nameStr = readVirtualMemoryString(namePtr, MAX_NAME_LENGTH);
		if (nameStr != null) {
			int[] argPtrs = new int[argc];

			for (int i = 0; i < argc; i++) {
				byte[] buffer = new byte[4];
				readVirtualMemory(argvPtr + i * 4, buffer, 0, 4);
				argPtrs[i] = ByteBuffer.wrap(buffer).getInt();
			}

			String[] argStr = new String[argc];

			for (int i = 0; i < argc; i++) {
				byte[] buffer = new byte[MAX_NAME_LENGTH];
				argStr[i] = readVirtualMemoryString(argPtrs[i], MAX_NAME_LENGTH);
			}

			UserProcess child = new UserProcess();
			child.pProcess = this;
			processTable.put(child.processId, child);
			children.add(child);
			child.execute(nameStr, argStr);

			return child.processId;
		}
		return 0;
	}

	private int handleJoin(int pid, int statusPtr) {
		if (processTable.get(pid) != null) {
			UserProcess child = processTable.get(pid);
            Lib.debug(dbgSyscall, "Syscall> Joining PID: " + pid);
			child.join();
            Lib.debug(dbgSyscall, "Syscall> Joined PID: " + pid);
			ByteBuffer bb = ByteBuffer.allocate(4);
			bb.putInt(child.exitCode);
			byte[] buffer = new byte[4];
			bb.get(buffer);
			processTable.remove(pid);
			writeVirtualMemory(statusPtr, buffer);
			return 1;
		}
		return -1;
	}

	private int handleCreat(int namePtr) {
		String nameStr = readVirtualMemoryString(namePtr, MAX_NAME_LENGTH);
        Lib.debug(dbgSyscall, "Syscall> Creating file: " + nameStr);
		return addToFileRef(ThreadedKernel.fileSystem.open(nameStr, true));
	}

	/**
	 * Adds a file to the local and global file reference
	 * tables.
	 */
	private int addToFileRef(OpenFile file) {
		if (file != null) {
            Lib.debug(dbgSyscall, "Syscall> Successfully opened file: " + file.getName());
			int firstAvailable = -1;
			for (int i = 0; i < 16; i++) {

				// Check if position is available
				if (openFiles[i] == null) {
					if (firstAvailable == -1) {
						firstAvailable = i;
					}
				} else

				// Check if file exists in global file reference table
				if (openFiles[i].getName().equals(file.getName())) {
					localOpenFiles[i] = openFiles[i];
					referenceCount[i] += 1;
					return i;
				}
			}

			// Check if there is space to open the file
			if (firstAvailable != -1) {
				openFiles[firstAvailable] = file;
				localOpenFiles[firstAvailable] = file;
				referenceCount[firstAvailable] += 1;
				return firstAvailable;
			}
		}
        Lib.debug(dbgSyscall, "Syscall> Failed to open file");
		return -1;
	}

	private int handleOpen(int namePtr) {
		String nameStr = readVirtualMemoryString(namePtr, MAX_NAME_LENGTH);
		return addToFileRef(ThreadedKernel.fileSystem.open(nameStr, false));
	}

	private int handleRead(int fDesc, int destPtr, int readSize) {
		if (fDesc >= 0 && fDesc < 16 && openFiles[fDesc] != null) {
			byte[] buffer = new byte[readSize];
			int bytesRead = openFiles[fDesc].read(0, buffer, 0, readSize);

			// No bytes were read
			if (bytesRead == -1) {
				return -1;
			}

			int bytesWritten = writeVirtualMemory(destPtr, buffer, 0, bytesRead);

            Lib.debug(dbgSyscall, "Syscall> Successfully read " + bytesWritten
                        + " bytes from file with descriptor: " + fDesc);
			return bytesWritten;
		}
        Lib.debug(dbgSyscall, "Syscall> Error while handling read: fDesc=" + fDesc
                    + ", destPtr=" + destPtr + ", writeSize=" + readSize);
		return -1;
	}

	private int handleWrite(int fDesc, int destPtr, int writeSize) {
		if (fDesc >= 0 && fDesc < 16 && openFiles[fDesc] != null) {
			byte[] buffer = new byte[writeSize];
			int bytesRead = readVirtualMemory(destPtr, buffer, 0, writeSize);

			// Check if no bytes were read from virtual memory.
			if (bytesRead == 0) {
				return 0;
			}

			int bytesWritten = openFiles[fDesc].write(buffer, 0, bytesRead);

			// If mismatch between bytes read and bytes written, return error
			if (bytesWritten != bytesRead) {
				return -1;
			}
            Lib.debug(dbgSyscall, "Syscall> Successfully written " + bytesWritten
                        + " bytes to file with descriptor: " + fDesc);
			return bytesWritten;
		}
        Lib.debug(dbgSyscall, "Syscall> Error while handling write: fDesc=" + fDesc
                    + ", destPtr=" + destPtr + ", writeSize=" + writeSize);
		return -1;
	}

	private int handleClose(int fDesc) {
		OpenFile file = openFiles[fDesc];
   		if (file != null) {

			//If there are no more references to the file
			if (referenceCount[fDesc] == 0) {

				// If file is marked for unlinking, delete it
				if (unlinkedFiles[fDesc]) {
					ThreadedKernel.fileSystem.remove(file.getName());
				}

				 file.close();
				 openFiles[fDesc] = null; // Deallocate this fileReference
				 localOpenFiles[fDesc] = null;
			}
			return 0;
		}
		return -1;
	}

	private int handleUnlink(int namePtr) {
        String nameStr = readVirtualMemoryString(namePtr, MAX_NAME_LENGTH);
		OpenFile file = ThreadedKernel.fileSystem.open(nameStr, false);

		if (file == null) {
			return -1;
		}

		for (int i = 0; i < 16; i++) {
			if (openFiles[i].getName().equals(file.getName())) {
				unlinkedFiles[i] = true;
				return 0;
			}
		}

		ThreadedKernel.fileSystem.remove(file.getName());
		return 0;

	}

    private static final int
        syscallHalt = 0,
	syscallExit = 1,
	syscallExec = 2,
	syscallJoin = 3,
	syscallCreate = 4,
	syscallOpen = 5,
	syscallRead = 6,
	syscallWrite = 7,
	syscallClose = 8,
	syscallUnlink = 9;

    /**
     * Handle a syscall exception. Called by <tt>handleException()</tt>. The
     * <i>syscall</i> argument identifies which syscall the user executed:
     *
     * <table>
     * <tr><td>syscall#</td><td>syscall prototype</td></tr>
     * <tr><td>0</td><td><tt>void halt();</tt></td></tr>
     * <tr><td>1</td><td><tt>void exit(int status);</tt></td></tr>
     * <tr><td>2</td><td><tt>int  exec(char *name, int argc, char **argv);
     * 								</tt></td></tr>
     * <tr><td>3</td><td><tt>int  join(int pid, int *status);</tt></td></tr>
     * <tr><td>4</td><td><tt>int  creat(char *name);</tt></td></tr>
     * <tr><td>5</td><td><tt>int  open(char *name);</tt></td></tr>
     * <tr><td>6</td><td><tt>int  read(int fd, char *buffer, int size);
     *								</tt></td></tr>
     * <tr><td>7</td><td><tt>int  write(int fd, char *buffer, int size);
     *								</tt></td></tr>
     * <tr><td>8</td><td><tt>int  close(int fd);</tt></td></tr>
     * <tr><td>9</td><td><tt>int  unlink(char *name);</tt></td></tr>
     * </table>
     *
     * @param	syscall	the syscall number.
     * @param	a0	the first syscall argument.
     * @param	a1	the second syscall argument.
     * @param	a2	the third syscall argument.
     * @param	a3	the fourth syscall argument.
     * @return	the value to be returned to the user.
     */
    public int handleSyscall(int syscall, int a0, int a1, int a2, int a3) {
	switch (syscall) {
	case syscallHalt:
	    return handleHalt(); // DONE, UNTESTED
	case syscallExit:
		return handleExit(a0);	// DONE, UNTESTED
	case syscallExec:
		return handleExec(a0, a1, a2); // DONE, UNTESTED
	case syscallJoin:
	   return handleJoin(a0, a1); // DONE, UNTESTED
	case syscallCreate:
	   return handleCreat(a0); // DONE, UNTESTED
	case syscallOpen:
	   return handleOpen(a0); // DONE, UNTESTED
	case syscallRead:
       return handleRead(a0, a1, a2); // DONE, UNTESTED
	case syscallWrite:
       return handleWrite(a0, a1, a2); // DONE, UNTESTED
    case syscallClose:
	   return handleClose(a0); // DONE, UNTESTED
	case syscallUnlink:
	   return handleUnlink(a0);

	default:
	    Lib.debug(dbgProcess, "Unknown syscall " + syscall);
	    Lib.assertNotReached("Unknown system call!");
	}
	return 0;
    }

    /**
     * Handle a user exception. Called by
     * <tt>UserKernel.exceptionHandler()</tt>. The
     * <i>cause</i> argument identifies which exception occurred; see the
     * <tt>Processor.exceptionZZZ</tt> constants.
     *
     * @param	cause	the user exception that occurred.
     */
    public void handleException(int cause) {
	Processor processor = Machine.processor();

	switch (cause) {
	case Processor.exceptionSyscall:
	    int result = handleSyscall(processor.readRegister(Processor.regV0),
				       processor.readRegister(Processor.regA0),
				       processor.readRegister(Processor.regA1),
				       processor.readRegister(Processor.regA2),
				       processor.readRegister(Processor.regA3)
				       );
	    processor.writeRegister(Processor.regV0, result);
	    processor.advancePC();
	    break;

	default:
	    Lib.debug(dbgProcess, "Unexpected exception: " +
		      Processor.exceptionNames[cause]);
	    Lib.assertNotReached("Unexpected exception");
	}
    }

    /** The program being run by this process. */
    protected Coff coff;

    /** This process's page table. */
    protected TranslationEntry[] pageTable;
    /** The number of contiguous pages occupied by the program. */
    protected int numPages;

    /** The number of pages in the program's stack. */
    protected final int stackPages = 8;

    private int initialPC, initialSP;
    private int argc, argv;
    private Lock pageLock;
    private Lock memoryLock;
    private static final int pageSize = Processor.pageSize;
    private static final char dbgProcess = 'a';
    private static final char dbgSyscall = 's';

	private static final int MAX_NAME_LENGTH = 256;
	private static int globalThreadID = 0;
	private static HashMap<Integer, UserProcess> processTable =
		new HashMap<Integer, UserProcess>();

	private Lock joinLock = new Lock();
	private Condition2 joinCondition = new Condition2(joinLock);
	private ArrayList<UserProcess> children = new ArrayList<UserProcess>();
	private UserProcess pProcess;
	protected int processId;
	protected int exitCode;
	protected boolean exited;
}
