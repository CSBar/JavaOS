package kernel;
import java.io.*;
import java.util.*;
import static java.lang.System.*;

public class FileTester {

  private static String[] helpInfo = {
        "help",
        "quit",
        "format",
        "cd pathname",
        "pwd",
        "create pathname",
        "read pathname",
        "write pathname data",
        "writeln pathname",
        "rm pathname",
        "mkdir pathname",
        "rmdir pathname",
        "ln oldpath newpath",
        "readlink pathname",
        "ls [ dirname ]",
    };

    /** Disk block size, as retrieved from the Disk (cheating!). */
    private static int blockSize;

    public static void main(String [] args) {
        // Power on the disk
        Kernel.interrupt(Kernel.INTERRUPT_POWER_ON, 10, 0, new FastDisk(100), null, null);
   // XXX: Script to test commands
  //        args = new String[1];
 //        args[0] = "test1.script";
//        args[0] = "mytest.script";
        
        if (args.length > 1) {
            System.err.println("usage: FileTester [ script-file ]");
            System.exit(0);
        }

 // blockSize = Disk.BLOCK_SIZE;
// This is a bit of a cheat.  We really should have a Kernel call to get this information.
        blockSize = Library.getBlockSizeOfDisk();

        boolean fromFile = (args.length == 1);

        BufferedReader input = null;

        if (fromFile) {
            try {
                input = new BufferedReader(new FileReader(args[0]));
            } catch (FileNotFoundException e) {
                System.err.println("Error: Script file " + args[0] + " not found.");
                System.exit(1);
            }
        } else {
            input = new BufferedReader(new InputStreamReader(System.in));
        }

        for (;;) {
            String cmd = null;
            try {
                // Print out the prompt for the user
                if (!fromFile) {
                    out.printf("--> ");
                    System.out.flush();
                }

                String line = input.readLine();

                if (line == null) {
                    return;
                }
                line = line.trim();
                if (line.length() == 0) {
                    continue;
                }

                if (line.startsWith("//")) {
                    if (fromFile) {
                        out.printf("%s\n", line);
                    }
                    continue;
                }
                if (line.startsWith("/*")) {
                    continue;
                }

                if (fromFile) {
                    out.printf("--> %s\n", line);
                }

                StringTokenizer st = new StringTokenizer(line);
                cmd = st.nextToken();

                int result = 0;
                if (cmd.equalsIgnoreCase("quit")) {
                    Library.shutdown();
                    return;
                } else if (cmd.equalsIgnoreCase("help") || cmd.equals("?")) {
                    help();
                    continue;
                } else if (cmd.equalsIgnoreCase("format")) {
                    result = Library.format();
                } else if (cmd.equalsIgnoreCase("cd")) {
                    result = Library.chdir(st.nextToken());
                } else if (cmd.equalsIgnoreCase("pwd")) {
                    result = pwd();
                } else if (cmd.equalsIgnoreCase("create")) {
                    result = Library.create(st.nextToken());
                } else if (cmd.equalsIgnoreCase("read")) {
                    result = readTest(st.nextToken(), false);
                } else if (cmd.equalsIgnoreCase("write")) {
                    result = writeTest(st.nextToken(), line);
                } else if (cmd.equalsIgnoreCase("writeln")) {
                    result = writeLines(st.nextToken(), input);
                } else if (cmd.equalsIgnoreCase("rm")) {
                    result = Library.delete(st.nextToken());
                } else if (cmd.equalsIgnoreCase("mkdir")) {
                    result = Library.mkdir(st.nextToken());
                } else if (cmd.equalsIgnoreCase("rmdir")) {
                    result = Library.rmdir(st.nextToken());
                } else if (cmd.equalsIgnoreCase("ln")) {
                    String oldName = st.nextToken();
                    String newName = st.nextToken();
                    result = Library.symlink(oldName, newName);
                } else if (cmd.equalsIgnoreCase("readlink")) {
                    result = readTest(st.nextToken(), true);
                } else if (cmd.equalsIgnoreCase("ls")) {
                    if (st.hasMoreTokens()) {
                        result = dumpDir(st.nextToken());
                    } else {
                        result = dumpDir(".");
                    }
                } else {
                    out.printf("unknown command\n");
                    continue;
                }

                if (result != 0) {
                    if (result == -1) {
                        out.printf("*** System call failed\n");
                    } else {
                        out.printf("*** Bad result %d from system call\n",result);
                    }
                }
            } catch (NoSuchElementException e) {
                // Handler for nextToken()
                out.printf("Incorrect number of arguments\n");
                help(cmd);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        } 
    } 
    private static void help() {
        out.printf("Commands are:\n");
        for (int i = 0; i < helpInfo.length; i++) {
            out.printf("    %s\n", helpInfo[i]);
        }
    } 

    private static void help(String cmd) {
        for (int i = 0; i < helpInfo.length; i++) {
            if (helpInfo[i].startsWith(cmd)) {
                out.printf("usage: %s\n", helpInfo[i]);
                return;
            }
        }
        out.printf("unknown command '%s'\n", cmd);
    } 

    private static int readTest(String fname, boolean isLink) {
        byte[] buf = new byte[blockSize];
        int n = isLink
                ? Library.readlink(fname, buf)
                : Library.read(fname, buf);
        boolean needNewline = false;
        if (n < 0) {
            return n;
        }
        for (int i = 0; i < buf.length; i++) {
            if (buf[i] != 0) {
                showChar(buf[i] & 0xff);
                needNewline = (buf[i] != '\n');
            }
        }
        if (needNewline) {
            out.printf("\n");
        }
        return n;
    }
    
    private static int writeTest(String fname, String info) {
        int p;
        p = info.indexOf(' ');
        if (p >= 0) {
            p = info.indexOf(' ', p + 1);
            if (p < 0) {
                p = info.length();
            } else {
                p++;
            }
        } else {
            p = 0;
        }
        byte[] buf = new byte[Math.max(blockSize, info.length() - p)];
        int i = 0;
        while (p < info.length()) {
            buf[i++] = (byte) info.charAt(p++);
        }
        return Library.write(fname, buf);
    } 
    
    private static int writeLines(String fname, BufferedReader in) {
        try {
            byte[] buf = new byte[blockSize];
            int i = 0;
            for (;;) {
                String line = in.readLine();
                if (line == null || line.equals(".")) {
                    break;
                }
                for (int j = 0; j < line.length(); j++) {
                    if (i >= buf.length) {
                        byte[] newBuf = new byte[buf.length * 2];
                        System.arraycopy(buf, 0, newBuf, 0, buf.length);
                        buf = newBuf;
                    }
                    buf[i++] = (byte) line.charAt(j);
                }
                if (i >= buf.length) {
                    break;
                }
                buf[i++] = '\n';
            }
            return Library.write(fname, buf);
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    } 
    
    private static void showChar(int b) {
        if (b >= ' ' && b <= '~') {
            out.printf("%c", (char)b);
            return;
        }
        if (b == '\n') {
            out.printf("\\n\n");
            return;
        }
        if (b == '\\') {
            out.printf("\\\\");
            return;
        }
        out.printf("\\%03o", b);
    } 

    private static int dumpDir(String dirname) {
        byte[] buf = new byte[blockSize];
        int n = Library.readdir(dirname, buf);
        if (n < 0) {
            return n;
        }
        for (int i = 0; i < buf.length; i += 16) {
            int block = ((buf[i] & 0xff) << 8) + (buf [i+1] & 0xff);
            if (block == 0) {
                continue;
            }
            StringBuffer sb = new StringBuffer();
            for (int j = 3; j < 16; j++) {
                if (buf[i + j] == 0) {
                    break;
                }
                sb.append((char) buf[i + j]);
            }
            String fname = sb.toString();
            out.printf("%s %s", block, fname);
            switch (buf[i + 2]) {
            case 'O':
                break;
            case 'D':
                out.printf("/");
                break;
            case 'L':
                out.printf(" -> ");
                byte[] buf1 = new byte[blockSize];
                n = Library.readlink(dirname + "/" + fname, buf1);
                if (n < 0) {
                    return n;
                }
                for (int j = 0; j < buf1.length; j++) {
                    if (buf1[j] == 0) {
                        break;
                    }
                    out.printf("%c", (char) buf1[j]);
                }
                break;
            default:
                out.printf("?type \\%03o?", buf[i + 2]);
                // out.printf("<type %d>", buf[i + 2]);
            }
            out.printf("\n");
        }
        return n;
    } 

    private static int pwd() {
        int rc, dot, dotdot;
        int child = 0;
        String relPath = ".";
        List<String> path = new LinkedList<String>();
        byte[] buf = new byte[blockSize];
        for (;;) {
            rc = Library.readdir(relPath, buf);
            if (rc != 0) {
                out.printf("pwd:  cannot read directory \"%s\"\n", relPath);
                return -1;
            }
            dot = dirSearch(buf, ".");
            if (dot == 0) {
                out.printf("pwd: bad directory \"%s\": no . entry\n",relPath);
                return -1;
            }
            if (child != 0) {
                String cname = dirSearch(buf, child);
                if (cname == null) {
                    out.printf("pwd: bad directory \"%s\": " + " no entry for %d\n", relPath, child);
                    return -1;
                }
                path.add(0, cname);
            }
            dotdot = dirSearch(buf, "..");
            if (dotdot == 0) {
                out.printf("pwd: bad directory \"%s\": no .. entry\n",relPath);
                return -1;
            }
            if (dot == dotdot) {
                break;
            }
            child = dot;
            relPath += "/..";
        }
        if (path.size() == 0) {
            out.printf("/");
        } else {
            for (String s : path) {
                out.printf("/%s", s);
            }
        }
        out.printf("\n");
        return 0;
    } 


    private static int dirSearch(byte[] buf, String s) {
        for (int offset = 0; offset < buf.length; offset += 16) {
            int j;
            for (j = 0; j < 13; j++) {
                if (j >= s.length()
                    || buf[offset + j + 3] != (byte) s.charAt(j))
                {
                    break;
                }
            }
            if (j == s.length() && buf[offset + j + 3] == 0) {
                return (((buf[offset] & 0xff) << 8) + (buf[offset+1] & 0xff));
            }
        }
        return 0;
    } 

    private static String dirSearch(byte[] buf, int n) {
        for (int offset = 0; offset < buf.length; offset += 16) {
            int blk = (((buf[offset] & 0xff) << 8) + (buf[offset+1] & 0xff));
            if (blk == n) {
                int j;
                for (j = 0; j < 13; j++) {
                    if (buf[offset + j + 3] == 0) {
                        break;
                    }
                }
                return new String(buf, offset + 3, j);
            }
        }
        return null;
    } 
  }
