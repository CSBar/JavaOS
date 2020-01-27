//package kernel;
//import java.util.Scanner;
//import minikernel.Disk;
//import minikernel.FastDisk;
//
//public class MyShell {
//    public static FastDisk disk;
//    public static void main(String[] args) {
////        System.out.println("Hello, world!");
////        disk = new FastDisk(100);       
//        try {
//            while (true) {
//                System.out.print("> ");
//                Scanner s = new Scanner(System.in);
//                String input = s.nextLine();
//                String commands[] = input.split("&");
//
//                CommandExecutorThread commandExecutorThread[] = new CommandExecutorThread[commands.length];
//                for (String command : commands) {
//                for (int i = 0; i < commands.length; i++) {
//                    commandExecutorThread[i] = new CommandExecutorThread(i, commands[i].trim());
//                    commandExecutorThread[i].start();
//                }
//
//                for (int i = 0; i < commandExecutorThread.length; i++) {
//                    commandExecutorThread[i].join();
//                }
//            }
//
//        } 
//
//        catch (Exception e) {
//            System.out.println("\n\nInterrupt was detected. MyShell is closing.");
////            e.printStackTrace();
//            System.exit(0);
//        }
//
//    }
//
//}
//
//class CommandExecutorThread extends Thread {
//
//    private int myID = 0;
//    private String command;
//
//    public CommandExecutorThread(int myID, String command) {
//        this.myID = myID;
//        this.command = command;
//    }
//
//    public void run() {
//        System.out.println("myID = " + myID + ". Running command " + command);
//        
//        switch (command) {
//            case "format":
//                break;
//        }
//        
//    }
//
//}
