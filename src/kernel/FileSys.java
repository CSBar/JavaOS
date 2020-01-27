package kernel;
import java.util.*;
import java.io.*;
import static java.lang.System.*;


public class FileSys {
    private FastDisk disk;    
    private String currDir;
    private String fileTable[];

    public FileSys(FastDisk disk) {
        this.disk = disk;
        
        currDir = "/";
        
        
        fileTable = new String[100];
        int startBlock = 1; 
        
        byte freeMap[] = new byte[disk.getBlockSize()];
        
        disk.read(0, freeMap);
        
        byte tempBuffer[] = new byte[disk.getBlockSize()];
        for (int i = startBlock; i < fileTable.length; i++) {
            if (freeMap[i] == '1') {
                disk.read(i, tempBuffer);
                
                String fileName = "";
                for (int j = 0; j < 32; j++) {
                    fileName += (char) tempBuffer[j];
                }
                fileTable[i] = fileName;
            }
        }
        
        /*
        for (int i = startBlock; i < fileTable.length; i++) {
            byte fileNameBuffer[] = new byte[32];
            disk.read(i, 32, fileNameBuffer);
            fileTable[i] = new String(fileNameBuffer);
        }*/
        
//        For debugging purposes, print out the fileTable[] array.
//        for (int i = startBlock; i < fileTable.length; i++) {
//            System.out.printf("fileTable[%d] = %s\n", i, fileTable[i]);
//        }
        
        
    } 
        
    public FastDisk getDisk() {
        return disk;
    }
    
    public int getBlockSizeOfDisk() {
        return disk.getBlockSize();
    }
    
    public void setDisk(Disk disk) {
        this.disk = (FastDisk) disk;
    }
    
    public String[] getFileTable() {
        return fileTable;
    }
    
    public void updateFileTable(int targetBlock, String newFileName) {
        fileTable[targetBlock] = newFileName;
    }
        
} 
