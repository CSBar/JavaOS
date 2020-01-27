package kernel;
import java.io.*;
import static java.lang.System.*;

public class Disk implements Runnable {

    public static final int BLOCK_SIZE = 512;
    public final int DISK_SIZE;

    protected int currentBlock = 0;
    protected byte[] data;
    protected boolean busy;
    protected int targetBlock;
    protected int readCount;
    protected int writeCount;
    
    private boolean isWriting;
    private byte[] buffer;
    private boolean requestQueued = false;
    

    static protected class DiskException extends RuntimeException {
        static final long serialVersionUID = 0;
        public DiskException(String s) {
            super("*** YOU CRASHED THE DISK: " + s);
        }
    }


    public Disk(int size) {
        File diskName = new File("DISK");
        if (diskName.exists()) {
            if (diskName.length() != size * BLOCK_SIZE) {
                throw new DiskException(
                    "File DISK exists but is the wrong size");
            }
        }
        this.DISK_SIZE = size;
        if (size < 1) {
            throw new DiskException("A disk must have at least one block!");
        }

        data = new byte[DISK_SIZE * BLOCK_SIZE];
        int count = BLOCK_SIZE;
        try {
            FileInputStream is = new FileInputStream("DISK");
            is.read(data);
            out.printf("Restored %d bytes from file DISK\n", count);
            is.close();
            return;
        } catch (FileNotFoundException e) {
            out.println("Creating new disk");
            this.format();
        } catch (Exception e) {
            e.printStackTrace();
            exit(1);
        }
 //*****************Commented out by Roshan Lamichhane
//        byte[] junk = new byte[BLOCK_SIZE];
//        for (int i = 0; i < BLOCK_SIZE; ) {
//            junk[i++] = 74;
//            junk[i++] = 85;
//            junk[i++] = 78;
//            junk[i++] = 75;
//        }
//        for (int i = 1; i < DISK_SIZE; i++) {
//            arraycopy(
//                junk, 0,
//                data, i * BLOCK_SIZE,
//                BLOCK_SIZE);
//        }
    } // Disk(int)


    public void format() {
        data = new byte[DISK_SIZE * BLOCK_SIZE];
 //*****************Commented out by Roshan Lamichhane
//        byte[] junk = new byte[BLOCK_SIZE];
//        for (int i = 0; i < BLOCK_SIZE; ) {
//            junk[i++] = 74;
//            junk[i++] = 85;
//            junk[i++] = 78;
//            junk[i++] = 75;
//        }
//        for (int i = 1; i < DISK_SIZE; i++) {
//            arraycopy(
//                junk, 0,
//                data, i * BLOCK_SIZE,
//                BLOCK_SIZE);
//        }
    }
    

    public void flush() {
        try {
            out.println("Saving contents to DISK file...");
            FileOutputStream os = new FileOutputStream("DISK");
            os.write(data);
            os.close();
            out.printf("%d read operations and %d write operations performed\n", readCount, writeCount);
        } catch(Exception e) {
            e.printStackTrace();
            exit(1);
        }
    } 
    
    protected void delay(int targetBlock) {
        int sleepTime = 10 + Math.abs(targetBlock - currentBlock) / 5;
        try {
            Thread.sleep(sleepTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
    } 
    
    public synchronized void beginRead(int blockNumber, byte[] buffer) {
        if (blockNumber < 0 || blockNumber >= DISK_SIZE || buffer == null || buffer.length < BLOCK_SIZE){
            throw new DiskException("Illegal disk read request: " + " block number " + blockNumber + " buffer " + buffer);
        }

        if (busy) {
            throw new DiskException("Disk read attempted " + " while the disk was still busy.");
        }

        isWriting = false;
        this.buffer = buffer;
        targetBlock = blockNumber;
        requestQueued = true;
        notify();
    } 

    public synchronized void beginWrite(int blockNumber, byte[] buffer) {
        if ( blockNumber < 0 || blockNumber >= DISK_SIZE || buffer == null || buffer.length < BLOCK_SIZE){
            throw new DiskException("Illegal disk write request: " + " block number " + blockNumber + " buffer " + buffer);
        }

        if (busy) {
            throw new DiskException("Disk write attempted " + " while the disk was still busy.");
        }

        isWriting = true;
        this.buffer = buffer;
        targetBlock = blockNumber;
        requestQueued = true;
        notify();
    } 

    protected synchronized void waitForRequest() {
        while(!requestQueued) {
            try {
                wait();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        requestQueued = false;
        busy = true;
    } 

    protected void finishOperation() {
        synchronized (this) {
            busy = false;
            currentBlock = targetBlock;
        }
        Kernel.interrupt(Kernel.INTERRUPT_DISK,
            0,0,null,null,null);
    } 

    public void run() {
        for (;;) {
            waitForRequest();

            delay(targetBlock);

            if (isWriting) {
                arraycopy(
                    buffer, 0,
                    data, targetBlock * BLOCK_SIZE,
                    BLOCK_SIZE);
                writeCount++;
            } else {
                arraycopy(
                    data, targetBlock * BLOCK_SIZE,
                    buffer, 0,
                    BLOCK_SIZE);
                readCount++;
            }
            finishOperation();
        }
    }
}
