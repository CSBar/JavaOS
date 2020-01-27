package kernel;

import main.*;
import static java.lang.System.*;
public class Library {
 
    private Library() {}

    public static final String[] errorMessage = {
        "OK",                           // 0
        "Invalid argument",             // ERROR_BAD_ARGUMENT = -1
        "No such class",                // ERROR_NO_CLASS = -2
        "Class has no main method",     // ERROR_NO_MAIN = -3
        "Command aborted",              // ERROR_BAD_COMMAND = -4
        "Argument out of range",        // ERROR_OUT_OF_RANGE = -5
        "End of file on console input", // ERROR_END_OF_FILE = -6
        "I/O error on console input",   // ERROR_IO = -7
        "Exception in user program",    // ERROR_IN_CHILD = -8
        "No such process"               // ERROR_NO_SUCH_PROCESS = -9
    };

    public static int output(String s) {
        return Kernel.interrupt(Kernel.INTERRUPT_USER,
            Kernel.SYSCALL_OUTPUT, 0, s, null, null);
    } 
    public static int input(StringBuffer result) {
        result.setLength(0);
        return Kernel.interrupt(Kernel.INTERRUPT_USER,
                            Kernel.SYSCALL_INPUT, 0, result, null, null);
    } 
    public static int exec(String command, String args[]) {
        return Kernel.interrupt(Kernel.INTERRUPT_USER,
            Kernel.SYSCALL_EXEC, 0, command, args, null);
    } 
    public static int join(int pid) {
        return Kernel.interrupt(Kernel.INTERRUPT_USER,
            Kernel.SYSCALL_JOIN, pid, null, null, null);
    } 
    
    public static int getBlockSizeOfDisk() {
        return Kernel.interrupt(Kernel.INTERRUPT_USER, Kernel.SYSCALL_GET_BLOCK_SIZE, 0, null, null, null);
    }

    public static int format() {
//        err.println("format system call not implemented yet");
//        return -1;
        
       
        return Kernel.interrupt(Kernel.INTERRUPT_USER, Kernel.SYSCALL_FORMAT, 0, null, null, null);
                
    }
    public static int chdir(String pathname) {
        err.println("chdir system call not implemented yet");
        return -1;
    } 

    public static int create(String pathname) {
//        err.println("create system call not implemented yet");
//        return -1;        
        return Kernel.interrupt(
                Kernel.INTERRUPT_USER, Kernel.SYSCALL_CREATE, 0, null, null, pathname.getBytes());
        
    } 
    public static int read(String pathname, byte[] buffer) {
        
//        err.println("read system call not implemented yet");
//        return -1;
        
        return Kernel.interrupt(
                Kernel.INTERRUPT_USER, Kernel.SYSCALL_READ, 0, null, null, pathname.getBytes());
        
    } 
    public static int write(String pathname, byte[] buffer) {
//        err.println("write system call not implemented yet");
        return Kernel.interrupt(
                Kernel.INTERRUPT_USER, Kernel.SYSCALL_WRITE, 0, pathname, null, buffer);
//        return -1;
    } 
    public static int delete(String pathname) {
        return Kernel.interrupt(
                Kernel.INTERRUPT_USER, Kernel.SYSCALL_DELETE, 0, pathname, null, null);
//        return -1;
    } 
    public static int mkdir(String pathname) {
        err.println("mkdir system call not implemented yet");
        return -1;
    } 
    public static int rmdir(String pathname) {
        err.println("rmdir system call not implemented yet");
        return -1;
    } 
    public static int symlink(String oldName, String newName) {
        err.println("symlink system call not implemented yet");
        return -1;
    } 
    public static int readlink(String pathname, byte[] buffer) {
        err.println("readlink system call not implemented yet");
        return -1;
    } 
    public static int readdir(String pathname, byte[] buffer) {
        return Kernel.interrupt(Kernel.INTERRUPT_USER, Kernel.SYSCALL_READDIR, 0, null, null, null);
//        return -1;
    } 

    public static void shutdown() {
        Kernel.interrupt(
                Kernel.INTERRUPT_USER, Kernel.SYSCALL_SHUTDOWN, 0, null, null, null);
    }
}
