import java.io.*;
/**
 * Created by obada on 2016-10-27.
 */
 class Executor {

    private Process p;

    /*
    properties[0] is OS
    properties[1] is the CWD
     */
    Executor(String[] properties,Client client)
    {
        String p1,p2;
        //check if it's windows, linux...etc
        if(properties[0].charAt(0)=='L'){p1="bash";p2="-C";}
        else {p1="cmd.exe";p2="/k";}
        //open up a process builder
        ProcessBuilder pb = new ProcessBuilder(p1, p2);
        //Shell directory
        pb.directory(new File(properties[1]));
        p = null;
        try
        {
            p = pb.start();
        } catch (Exception e) {
            e.printStackTrace();
            //if the process can't start it's probably a directory problem, change the directory to the home and restart the process
            properties[1] = System.getProperty("user.home");
            pb.directory(new File(properties[1]));
            try {
                p = pb.start();
            } catch (IOException e1) {
                //if the process keep on crashing just print the error stack
                e1.printStackTrace();
            }
        }
        String str="";
        //redirect the input and the error stream of the process to client
//        RedirectStream error = new RedirectStream(p.getErrorStream(),client);
        RedirectStream input = new RedirectStream(p.getInputStream(),client);

        //start the threads
//        error.start();
        input.start();
        try(PrintWriter pw = new PrintWriter(new OutputStreamWriter(p.getOutputStream()), true))
        {
            //only send the current directory if you're on linux (on WIN is done automatically)
            if(properties[0].charAt(0)!='W')
            client.sendMsg(properties[1]+">");
            //read commands from user
            while(!(str=client.getRequest()).equals("close"))
            {
                //if we wanna cd and the OS is linux
                if(str.contains("cd") && properties[0].charAt(0)=='L')
                    throw(new Exception("CAN'T CD ON LINUX"));
                pw.println(str);
            }
            client.sendMsg("Terminal closed...");
            // connection and Thread catches
        } catch (IOException e) {
            try {
                close();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        //this catch will only happen when we CD on linux
        } catch (Exception e) {
            String cwd=str.substring(3,str.length());
            // Destroy the current process
            try {
                close();
                //update the CWD
                properties[1] = cwd;
                //start a new process with the updated CWD
                new Executor(properties,client);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
        try {
            close();
            //catch statement for close() only
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
    private void close() throws InterruptedException {
        //this will make main thread wait till process (console) will finish (will be closed)
        p.waitFor();
    }
}