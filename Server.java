import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by Obada on 2016-09-12.
 */
public class Server
{
    static ServerSocket serverSocket;

    static ArrayList<Client> clients;
/*
    Start the server on a specific port
 */
public Server(int port)
{
    try {
        serverSocket = new ServerSocket(port);
    } catch (IOException e) {
        System.err.println("Port is in use");
        System.out.println("Starting the server on port: "+port+1 );
        new Server(port+1);
    }

}
/*
    Accept or decline a client connection "Connection in this phase is not encrypted"
 */
public Client auth(Socket connection)
{
    try(PrintWriter out = new PrintWriter(connection.getOutputStream(),true);
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream())))
    {
        out.println("Answer your security question's answer: ");
        String str = in.readLine();
        if(str.equals("RIGHTANSWER"))
            return new Client(connection);
    }
    catch(EOFException e)
    {

    } catch (IOException e) {
        e.printStackTrace();
    }
    return null;
}
/*
    send any kinda file to a client
 */
public void sendFile(String location,Socket socket)
{
    File file = new File(location);// will encrypt later
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
    File file = new File(location);//will decrypt later
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
    send a msg to a client
 */
public void sendMsg(Client client,String msg)
{
    client.sendMsg(msg);
}
/*
    Encrypt an object(msg,file...etc) before sending with AES 128 bit encryption
 */
public static Object encrypt(Object object)// code later
{
    return object;
}
/*
    Decrypt a received object from a client
 */
public static Object decrypt(Object object)// code later
{
    return object;
}
/*
    list all available files in a directory
 */
public void ls(Client client,String location)
{}
/*
    Listen to a client
 */
public void listen(Client client)
{
    String request;
    while((request=client.getRequest())!="")
    {
        handle(request);
    }
}
/*
    handling a clients requests
 */
public void handle(String str)
{

}
/*
    The main run method
 */
public void run()
{
//***********************************************************************************************\\
    new Thread()// thread waiting for connections
    {
     public void run()
     {
         while(true)
         {
             System.out.println("Listening for connections....");
             try {
                 Client client;
                 if((client = auth(serverSocket.accept()))!=null) {
                     clients.add(client); // will work on authentication later
                     System.out.println("Client connected..." + clients.get(clients.size() - 1).socket.getInetAddress());// get the IP address of the connected client
                     new Thread() {
                         public void run() {
                             listen(clients.get(clients.size() - 1));
                         }
                     }.start();//listen to the newly connected client and execute
                 }else
                 {
                    System.out.println("Connection refused");
                 }
             } catch (IOException e)
             {
                 e.printStackTrace();
             }
         }
     }
    }.start(); // A thread to listen for connections
//***************************************************************************************************\\

}
public static void main(String[] args)
{
    Server server = new Server(3245);
    server.run();// example
}
}
