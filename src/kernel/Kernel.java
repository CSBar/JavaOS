package kernel;
import java.util.*;
import java.io.*;
import java.lang.reflect.*;
import static java.lang.System.*;

public class Kernel {
    private Kernel() {}
    public static final int INTERRUPT_USER = 0;
    public static final int INTERRUPT_DISK = 1;
    public static final int INTERRUPT_POWER_ON = 2;
    public static final int SYSCALL_OUTPUT = 0;
    public static final int SYSCALL_INPUT = 1;
    public static final int SYSCALL_EXEC = 2;
    public static final int SYSCALL_JOIN = 3;
    public static final int SYSCALL_FORMAT = 4;
    public static final int SYSCALL_CREATE = 5;
    public static final int SYSCALL_READ = 6;
    public static final int SYSCALL_WRITE = 7;
    public static final int SYSCALL_DELETE = 8;
    public static final int SYSCALL_READDIR = 9;
    public static final int SYSCALL_SHUTDOWN = 10;
    public static final int SYSCALL_GET_BLOCK_SIZE = 11;
    public static final int ERROR_BAD_ARGUMENT = -1;
    public static final int ERROR_NO_CLASS = -2;
    public static final int ERROR_NO_MAIN = -3;
    public static final int ERROR_BAD_COMMAND = -4;
    public static final int ERROR_OUT_OF_RANGE = -5;
    public static final int ERROR_END_OF_FILE = -6;
    public static final int ERROR_IO = -7;
    public static final int ERROR_IN_CHILD = -8;
    public static final int ERROR_NO_SUCH_PROCESS = -9;


//  private static Disk disk;
    private static FastDisk disk;
    private static FileSys filesys;
    private static int cacheSize;

    public static int interrupt(int kind, int i1, int i2, Object o1, Object o2, byte a[]){
        try {
            switch (kind) {
            case INTERRUPT_USER:
                switch (i1) {
                case SYSCALL_OUTPUT:
                    return doOutput((String)o1);

                case SYSCALL_INPUT:
                    return doInput((StringBuffer)o1);

                case SYSCALL_EXEC:
                    return doExec((String)o1,(String[])o2);

                case SYSCALL_JOIN:
                    return doJoin(i2);

                
                case SYSCALL_FORMAT:
                    return doFormat();
                    
                case SYSCALL_CREATE:
                    return doCreateFile(a);
                    
                case SYSCALL_READ:
                    return doRead(a);
                    
                case SYSCALL_WRITE:
                    return doWrite(((String) o1).getBytes(), a);
                    
                case SYSCALL_DELETE:
                    return doDelete(((String) o1).getBytes());
                    
                case SYSCALL_READDIR:
                    return doReadDir();
                    
                case SYSCALL_SHUTDOWN:
                    doShutdown();
                    break;
                    
                case SYSCALL_GET_BLOCK_SIZE:
                    return doGetBlockSize();
                    
                default:
                    return ERROR_BAD_ARGUMENT;
                }

            case INTERRUPT_DISK:
                break;

            case INTERRUPT_POWER_ON:
                doPowerOn(i1, o1, o2);
//                doShutdown();
                break;

            default:
                return ERROR_BAD_ARGUMENT;
            } 
        } catch (Exception e) {
            e.printStackTrace();
            return ERROR_BAD_ARGUMENT;
        }
        return 0;
    } 
    private static void doPowerOn(int i1, Object o1, Object o2) {
        cacheSize = i1;
        
//        disk = (Disk)o1;
        disk = (FastDisk)o1;

        filesys = new FileSys(disk);
        
        String shellCommand = (String) o2;

        doOutput("Kernel: Disk is " + filesys.getBlockSizeOfDisk() + " blocks\n");
        doOutput("Kernel: Disk cache size is " + i1 + " blocks\n");

        /* doOutput("Kernel: Loading initial program.\n");
        StringTokenizer st = new StringTokenizer(shellCommand);
        int n = st.countTokens();
        if (n < 1) {
            doOutput("Kernel: No shell specified\n");
            exit(1);
        }
            
        String shellName = st.nextToken();
        String[] args = new String[n - 1];
        for (int i = 1; i < n; i++) {
            args[i - 1] = st.nextToken();
        }
        if (doExecAndWait(shellName, args) < 0) {
            doOutput("Kernel: Unable to start " + shellCommand + "!\n");
            exit(1);
        } else {
            doOutput("Kernel: " + shellCommand + " has terminated.\n");
        }
        */
        Launcher.joinAll();
    } 
    
    private static void doShutdown() {
        
//        disk.flush();
        filesys.getDisk().flush();
       
    } 

  
    private static int doOutput(String msg) {
        out.print(msg);
        return 0;
    } 

    private static BufferedReader br = new BufferedReader(new InputStreamReader(in));

    private static int doInput(StringBuffer sb) {
        try {
            String s = br.readLine();
            if (s==null) {
                return ERROR_END_OF_FILE;
            }
            sb.append(s);
            return 0;
        } catch (IOException t) {
            t.printStackTrace();
            return ERROR_IO;
        }
    } 

    private static int doExecAndWait(String command, String args[]) {
        Launcher l;
        try {
            l = new Launcher(command, args);
        } catch (ClassNotFoundException e) {
            return ERROR_NO_CLASS;
        } catch (NoSuchMethodException e) {
            return ERROR_NO_MAIN;
        } catch (Exception e) {
            e.printStackTrace();
            return ERROR_BAD_COMMAND;
        }
        try {
            l.run();
            l.delete();
            return l.returnCode;
        } catch (Exception e) {
            e.printStackTrace();
            return ERROR_IN_CHILD;
        }
    } 
    
    private static int doExec(String command, String args[]) {
        try {
            Launcher l = new Launcher(command, args);
            l.start();
            return l.pid;
        } catch (ClassNotFoundException e) {
            return ERROR_NO_CLASS;
        } catch (NoSuchMethodException e) {
            return ERROR_NO_MAIN;
        } catch (Exception e) {
            e.printStackTrace();
            return ERROR_BAD_COMMAND;
        }
    } 
    
    private static int doJoin(int pid) {
        return Launcher.joinOne(pid);
    }


    private static int doGetBlockSize() {
        return filesys.getBlockSizeOfDisk();
    }
    

    private static int doFormat() {
//        filesys = new FileSys(new FastDisk(100));
        
        filesys.getDisk().format();
        
        doOutput("Kernel: Disk formatted.\n");
        
        return 0;
    }
    

    private static int doCreateFile(byte pathName[]) {
        
        if (pathName.length > 32) {
            doOutput("Kernel: User error: File name too long!\n");
            return -1;
        }
        
        byte freeMap[] = new byte[filesys.getBlockSizeOfDisk()];
        
        filesys.getDisk().read(0, freeMap);
        
        String pathNameString = new String(pathName);
        for (int i = 1; i < 100; i++) {
            if (freeMap[i] == '1'
                && pathNameString.equals(filesys.getFileTable()[i].trim())) {
                doOutput("Kernel: User error: File name already exists at" + "block " + i + "!\n");
                return -1;
            }
        }
        
        int targetBlock = 1;
        for (int i = 1; i < filesys.getDisk().DISK_SIZE; i++) {
            if (freeMap[i] == '0') {
                targetBlock = i;
                break;
            }
        }
        
        byte pathAndContents[] = new byte[filesys.getBlockSizeOfDisk()];
        arraycopy(pathName, 0, pathAndContents, 0, pathName.length);
        
        filesys.getDisk().write(targetBlock, pathAndContents);
        
        freeMap[targetBlock] = '1';
        
        filesys.getDisk().write(0, freeMap);
        filesys.getFileTable()[targetBlock] = new String(pathName);
        
        doOutput("Kernel: Created file ");
        for (int i = 0; i < pathName.length; i++) {
            doOutput((char) pathName[i] + "");
        }
        doOutput(" at block " + targetBlock + ". \n");
        
        return 0;
    }
    
    private static int doRead(byte pathName[]) {
        
        byte tempBuffer[] = new byte[FastDisk.BLOCK_SIZE];
        
        int targetBlock = findTargetBlock(pathName);
        
        if (targetBlock == -1) {
            doOutput("Kernel: User error: File not found.\n");
            return -1;
        }
        
        filesys.getDisk().read(targetBlock, tempBuffer);
        
        doOutput("Kernel: File name: ");
        for (int i = 0; i < 32; i++) {
            doOutput((char) tempBuffer[i] + "");
        }
        doOutput(" at block " + targetBlock + ". ");
        doOutput("Contents: \n");
        for (int i = 32; i < 512; i++) {
            doOutput((char) tempBuffer[i] + "");
        }
        doOutput("\n");
        return 0;
    }
    
    private static int doWrite(byte pathName[], byte buffer[]) {
        
        int targetBlock = findTargetBlock(pathName);
            
        if (targetBlock == -1) {
            doOutput("Kernel: User error: File not found.\n");
            return -1;
        }
        
        byte fileNameAndBuffer[] = new byte[filesys.getBlockSizeOfDisk()];
        arraycopy(buffer, 0,fileNameAndBuffer, 32,filesys.getBlockSizeOfDisk() - 32);
        
        System.arraycopy(pathName, 0,fileNameAndBuffer, 0, pathName.length);
        
        filesys.getDisk().write(targetBlock, fileNameAndBuffer);
        
        return 0;
    }
    

    private static int doDelete(byte pathName[]) {
        int targetBlock = findTargetBlock(pathName);
        
        if (targetBlock == -1) {
            doOutput("Kernel: User error: File not found.\n");
            return -1;
        }
        
        byte nullByteArray[] = new byte[filesys.getBlockSizeOfDisk()];
        for (int i = 0; i < nullByteArray.length; i++) {
            nullByteArray[i] = '\0';
        }
        
        filesys.getDisk().write(targetBlock, nullByteArray);

        filesys.getFileTable()[targetBlock] = null;
        
        byte freeMap[] = new byte[filesys.getBlockSizeOfDisk()];
        
        filesys.getDisk().read(0, freeMap);
        
        freeMap[targetBlock] = '0';
        filesys.getDisk().write(0, freeMap);
        
        return 0;
        
    }
    

    private static int doReadDir() {
        
        doOutput("Kernel: ");
        for (int i = 1; i < filesys.getFileTable().length; i++) {
            
            doOutput(filesys.getFileTable()[i] == null ? "" : /*"Block " + i + ": " +*/ filesys.getFileTable()[i].trim() + " ");
            
        }
        doOutput("\n");
        
        return 0;
    }

    private static int findTargetBlock(byte pathName[]) {
        int targetBlock = -1;
        
        String pathNameString = new String(pathName);
        for (int i = 1; i < filesys.getDisk().DISK_SIZE; i++) {
            
            String fileTableString = 
                    filesys.getFileTable()[i] == null ?
                    "" :
                    filesys.getFileTable()[i].trim();
            
            if (pathNameString.equals(fileTableString)) {
                targetBlock = i;
                break;
            }
        }
        
        return targetBlock;
        
    }

    static private class Launcher extends Thread {
        static Map<Integer,Launcher> pidMap = new HashMap<Integer,Launcher>();
        static private int nextpid = 1;

        private Method method;
        private Object arglist[];
        private int pid;
        private int returnCode = 0;

        public Launcher(String command, String args[]) throws ClassNotFoundException, NoSuchMethodException{
            if (args==null) {
                args = new String[0];
            }

            Class params[] = new Class[] { args.getClass() };

            Class programClass = Class.forName(command);
            method = programClass.getMethod("main",params); 

            arglist = new Object[] { args };

            pid = nextpid++;
            synchronized (pidMap) {
                pidMap.put(pid, this);
            }
        } 

        public void run() {
            try {
                method.invoke(null,arglist);
            } catch (InvocationTargetException e) {
                out.println("Kernel: User error:");
                e.getTargetException().printStackTrace();

                returnCode = ERROR_IN_CHILD;
            } catch (Exception e) {
                out.printf("Kernel: %s\n", e);
                returnCode = ERROR_IN_CHILD;
            }
        }

        static public void joinAll() {
            for (Launcher l : pidMap.values()) {
                try {
                    l.join();
                } catch (InterruptedException ex) {
                    out.printf("Kernel: join: %s\n", ex);
                    ex.printStackTrace();
                }
            }
        } 

        static public int joinOne(int pid) {
            Launcher l;
            synchronized (pidMap) {
                l = pidMap.remove(pid);
            }
            if (l == null) {
                return ERROR_NO_SUCH_PROCESS;
            }
            try {
                l.join();
            } catch (InterruptedException e) {
                out.printf("Kernel: join: %s\n", e);
            }
            return l.returnCode;
        } 

        public void delete() {
            synchronized (pidMap) {
                pidMap.remove(pid);
            }
        } 
    } 
} 
