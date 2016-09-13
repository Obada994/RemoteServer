import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by obada on 2016-09-12.
 */
public class Server
{
    static ServerSocket serverSocket;

    static ArrayList<Client> clients;

public Server(int port)
{
    try {
        serverSocket = new ServerSocket(port);
    } catch (IOException e) {
        System.err.println("Port is in use");
    }

}
/*
    send any kinda file to a client
 */
public void sendFile(String location,Socket socket)
{
    File file = new File(location);
    byte[] bytes = new byte[64*1024];
    try (InputStream input = new FileInputStream(file);
         OutputStream output = socket.getOutputStream())
    {
        int count;
        while((count=input.read(bytes))>0)
        {
            output.write(bytes,0,count);
        }
    } catch (FileNotFoundException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    }
}
/*
    get a file from a client
 */
public void getFile(String location, Socket socket)
{
    File file = new File(location);
    byte[] bytes = new byte[64*1024];
    try (InputStream input = socket.getInputStream();
         OutputStream output = new FileOutputStream(file))
    {
        int count;
        while((count=input.read(bytes))>0)
        {
            output.write(bytes,0,count);
        }
    } catch (FileNotFoundException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    }
}
/*
    list all available files in a directory
 */
public void ls(Client client,String location)//index of the client
{

}
public static void run()
{
    Server server = new Server(3425); // start server
    new Thread()// thread waiting for connections
    {
     public void run()
     {
         while(true)
         {
             System.out.println("Listening for clients....");
             try {
                 clients.add(new Client(serverSocket.accept()));
                 System.out.println("Client connected..." + clients.get(clients.size()-1).socket.getInetAddress());//get the IP address of the connected client
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
    }.start();

}
public static void main(String[] args)
{}
}
