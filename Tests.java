import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by obada on 2016-10-23.
 */
public class Tests
{
    public static void main(String[] args) throws Exception
    {
        Client c = new Client(new Socket("localhost",1234),"TESTS");
        c.sendMsg(c.getRequest());//auth
        Thread.sleep(1000);
        new Thread(){public void run(){c.listen();}}.start(); //listen
        c.sendMsg("upload \"C:/users/abdul/Desktop/uplaoded 1.txt\" testupload txt");
        c.sendFile("C:/users/abdul/Desktop/uplaoded 1.txt");
        Thread.sleep(1000);
        c.sendMsg("get \"C:/users/abdul/Desktop/test.txt\" test txt");
        Thread.sleep(1000);
        c.sendMsg("upload-dir \"C:/users/abdul/Desktop/CV\" testdir zip");
        Utilities.zipDir(new File("/home/obada/Desktop/ass4"),System.getProperty("java.io.tmpdir")+"/dir.zip");
        c.sendFile(System.getProperty("java.io.tmpdir")+"/dir.zip");
        Thread.sleep(1000);
        c.sendMsg("get-dir \"C:/users/abdul/Desktop/CV\" testgetdir zip");
        Thread.sleep(1000);
        c.sendMsg("upload-to \"C:/users/abdul/Desktop/uplaoded 1.txt\" upload-to txt to \"C:/users/abdul/Desktop\"");
        c.sendFile("C:/users/abdul/Desktop/uplaoded 1.txt");
        Thread.sleep(1000);
        c.sendMsg("upload-dir-to \"C:/users/abdul/Desktop/CV\" upload-dto zip to \"C:/users/abdul/Desktop\"");
        Utilities.zipDir(new File("C:/users/abdul/Desktop/CV"),System.getProperty("java.io.tmpdir")+"/dir.zip");
        c.sendFile(System.getProperty("java.io.tmpdir")+"/dir.zip");
        Thread.sleep(1000);
        c.sendMsg("cmd");
//        String rx = "[^\"\\s]+|\"(\\\\.|[^\\\\\"])*\"";
//        String token;
//        BufferedReader buff = new BufferedReader(new InputStreamReader(System.in));
//        String str=buff.readLine();
//        Scanner in = new Scanner(str);
//        System.out.println(str);
//        token = in.findInLine(rx);
//        System.out.println(token);
//        File file = new File(token.substring(1,token.length()-1));
//        System.out.println(file.exists());
//        System.out.println(in.findInLine(rx));
    }
}
