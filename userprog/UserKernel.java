package nachos.userprog;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;

/**
 * A kernel that can support multiple user processes.
 */
public class UserKernel extends ThreadedKernel {
    /**
     * Allocate a new user kernel.
     */
    public UserKernel() {
	super();
    }

    public TranslationEntry[] getPages(int req){
	return frameManager.allocateMany(req);
    }

    public void deallocate(TranslationEntry[] frames){
	kernelLock.acquire();
	for(int i = 0; i < frames.length; i++){
		frameManager.unallocate(frames[i]);
	}
	kernelLock.release();
    }
 



    /**
     * Initialize this kernel. Creates a synchronized console and sets the
     * processor's exception handler.
     */
    public void initialize(String[] args) {
	    
	super.initialize(args);
	console = new SynchConsole(Machine.console());
	
	
	Machine.processor().setExceptionHandler(new Runnable() {
		public void run() { exceptionHandler(); }
	    });
	int numPages = Machine.processor().getNumPhysPages();
	frameManager = new FrameManager();	
	kernelLock = new Lock();

    }

    /**
     * Test the console device.
     */	
    public void selfTest() {
	super.selfTest();
/*	//****************
	UserProcess testP =  new UserProcess();
		testP.selfTest();	
*/
	//****************
    }

    /**
     * Returns the current process.
     *
     * @return	the current process, or <tt>null</tt> if no process is current.
     */
    public static UserProcess currentProcess() {
	if (!(KThread.currentThread() instanceof UThread))
	    return null;
	
	return ((UThread) KThread.currentThread()).process;
    }

    /**
     * The exception handler. This handler is called by the processor whenever
     * a user instruction causes a processor exception.
     *
     * <p>
     * When the exception handler is invoked, interrupts are enabled, and the
     * processor's cause register contains an integer identifying the cause of
     * the exception (see the <tt>exceptionZZZ</tt> constants in the
     * <tt>Processor</tt> class). If the exception involves a bad virtual
     * address (e.g. page fault, TLB miss, read-only, bus error, or address
     * error), the processor's BadVAddr register identifies the virtual address
     * that caused the exception.
     */
    public void exceptionHandler() {
	Lib.assertTrue(KThread.currentThread() instanceof UThread);

	UserProcess process = ((UThread) KThread.currentThread()).process;
	int cause = Machine.processor().readRegister(Processor.regCause);
	process.handleException(cause);
    }

    /**
     * Start running user programs, by creating a process and running a shell
     * program in it. The name of the shell program it must run is returned by
     * <tt>Machine.getShellProgramName()</tt>.
     *
     * @see	nachos.machine.Machine#getShellProgramName
     */
    public void run() {
	    System.out.println("TEST");
	super.run();

	UserProcess process = UserProcess.newUserProcess();
	
	String shellProgram = Machine.getShellProgramName();	
	System.out.println(shellProgram);
	Lib.assertTrue(process.execute(shellProgram, new String[] { }));
	KThread.currentThread().finish();
    }

    /**
     * Terminate this kernel. Never returns.
     */
    public void terminate() {
	super.terminate();
    }
	
	public boolean isAvailable(int request){
		return frameManager.isAvailable(request);
	}

    /** Globally accessible reference to the synchronized console. */
    public static SynchConsole console;
    private Lock kernelLock;
    protected FrameManager frameManager;
    // dummy variables to make javac smarter
    private static Coff dummy1 = null;
}
