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
      
      // Create A Disk Drive and make it start spinning
      
