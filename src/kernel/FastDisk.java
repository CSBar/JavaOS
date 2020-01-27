package kernel;

public class FastDisk extends Disk {
    public FastDisk(int size) {
        super(size);
        if (size < 0 || size >= (1<<15)) {
            throw new DiskException(
                String.format(
                    "Cannot make a FastDisk with %d blocks.  Max size is %d.",
                    size, 1<<15));
        }
    } 

    public void read(int blockNumber, byte buffer[]) {
        System.arraycopy(
            data, blockNumber * BLOCK_SIZE,
            buffer, 0,
            BLOCK_SIZE);
        readCount++;
    } 
    public void write(int blockNumber, byte buffer[]) {
        System.arraycopy(
            buffer, 0,
            data, blockNumber * BLOCK_SIZE,
            BLOCK_SIZE);
        writeCount++;
    } 
    
    @Override
    public void format() {
        data = new byte[DISK_SIZE * BLOCK_SIZE];
        for (int i = 1; i < DISK_SIZE; i++) {
            data[i] = '0';
        }
    }
    
    public int getBlockSize() {
        return BLOCK_SIZE;
    }
    


    @Deprecated
    public synchronized void beginRead(int blockNumber, byte buffer[]) {
        throw new UnsupportedOperationException(
                        "Don't use beginRead.  Use read");
    } // beginRead(int, byte[])

    @Deprecated
    public synchronized void beginWrite(int blockNumber, byte buffer[]) {
        throw new UnsupportedOperationException("Don't use beginWrite. Use write");
    }
}
