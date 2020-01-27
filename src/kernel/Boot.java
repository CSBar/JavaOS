package kernel;

import java.lang.reflect.*;
import java.util.*;
import static java.lang.System.*;

public class Boot {

private Boot(){}

private static void usage(){
  err.println("usage: Boot" + " <cacheSize> <diskName> <diskSize> <shell>" + " [ <shell parameters> ...]");
    exit(-1);
}

public static void main(String args[]){
  if (args.length < 4) {
      usage();
     }
     
   int cacheSize = Interger.parseInt(args[0]);
   String diskName = args[1];
   int diskSize = Integer.parseInt(args[2]);
   StringBuffer shellCommand = new StringBuffer(agrs[3]);
   
   for (int i = 4; i <args.length; i++){
      shellCommand.append(" ").append(args[i]);
      
              Object disk = null;
        try {
            Class diskClass = Class.forName(diskName);
            Constructor ctor
                = diskClass.getConstructor(new Class[] { Integer.TYPE });
            disk = ctor.newInstance(new Object[] { new Integer(diskSize) });
            if (! (disk instanceof Disk)) {
                err.printf("%s is not a subclass of Disk\n", diskName);
                usage();
            }
            if (!diskName.equals("FastDisk")) {
                new Thread((Disk) disk, "DISK").start();
            }
        } catch (ClassNotFoundException e) {
            err.printf("%s: class not found\n", diskName);
            usage();
        } catch (NoSuchMethodException e) {
            err.printf("%s(int): no such constructor\n", diskName);
            usage();
        } catch (InvocationTargetException e) {
            err.printf("%s: %s\n", diskName, e.getTargetException());
            usage();
        } catch (Exception e) {
            err.printf("%s: %s\n", diskName, e);
            usage();
        }
        out.println("Boot: Starting kernel.");

        Kernel.interrupt(Kernel.INTERRUPT_POWER_ON,
                         cacheSize, 0, disk, shellCommand.toString(), null);

        out.println("Boot: Kernel has stopped.");
        exit(0);
    }
} 

      
