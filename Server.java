import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Scanner;


/**
 * Created by Obada on 2016-09-12.
 */
public class Server
{
    static ServerSocket serverSocket;
    //connected clients
    static Client[] clients;
    //the number of connected clients
    static int Count;

    static File root;
/*
    Start the server on a specific port
 */
public Server(int port)
{
    try {
        serverSocket = new ServerSocket(port);
        Count=0;
        //Max 10 clients
        clients = new Client[10];
        //root folder contains all connected clients folders
        //create root folder on our Desktop
        root = new File(System.getProperty("user.home") + "/Desktop/root");
        //create the directory if it doesn't exist
        if(!root.exists()) root.mkdir();
    } catch (IOException e) {
        System.err.println("Port is in use");
        System.out.println("Starting the server on port: "+port+1 );
        new Server(port+1);
    }

}
/*
   Authenticating with the connecting client
 */
public Client auth(Socket connection) throws Exception {
    Client client = new Client(connection,true);
    SecureRandom random = new SecureRandom();
    //generate a random string, used for authentication and as an ID for the client
    String str = new BigInteger(130,random).toString(32);
    //encrypt and send, if we get the same string back then we'll allow the connection
    client.sendMsg(str);
    //if client can decrypt it then auth success
    String reply = client.getRequest();
    //auth succeed
    if (reply.equals(str))
    {
        //folder for this client on this server
        client.setPath(root.getAbsolutePath());
        //increment Count
        Count++;
        //return the connected client
        return client;
    }
    client.close();
    return null;
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
         //listen for connections
         while(true)
             try {
                 System.out.println("listening for a client");
                 Client client = null;
                 //we'll keep waiting until auth returns a client
                 while (client == null)
                     client = auth(serverSocket.accept());
                 System.out.println("client connected");
                 final Client clientFinal = client;
                 clients[Count-1] = clientFinal;
                 new Thread() {
                     public void run() {
                         clientFinal.listen();
                     }
                 }.start();
                 System.out.println("Server: client listening started");
             }
             catch (Exception e) {
                 e.printStackTrace();
             }
     }
    }.start(); // A thread to listen for connections
//***************************************************************************************************\\

    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    String request,next;
    Scanner scan;
    int index;
    try {
        index = 0;
        String filePath;
        String dirPath;
        while((request=in.readLine())!=null)
        {
            scan = new Scanner(request);
            next = scan.next();
            switch(next)
            {
                case "change-client":
                    index = Integer.parseInt(scan.next());
                break;
                case "upload-to":
                    //send the request to the client on the Server side
                    clients[index].sendMsg(request);
                    //send the file
                    clients[index].sendFile(scan.next());
                    break;
                case "upload-dir-to":
                    //compress the dir
                    Utilities.zipDir(new File(scan.next()),"dir.zip");
                    //notify the server/client
                    clients[index].sendMsg(request);
                    //send the compressed file
                    clients[index].sendFile("dir.zip");
                    break;
                case "upload":
                    //send the request to the client on the Server side
                    clients[index].sendMsg(request);
                    //send the file
                    clients[index].sendFile(scan.next());
                    break;
                case "upload-dir":
                    //compress the dir
                    Utilities.zipDir(new File(scan.next()),"dir.zip");
                    //notify the server/client
                    clients[index].sendMsg(request);
                    //send the compressed file
                    clients[index].sendFile("dir.zip");
                    break;
                default:
                    clients[index].sendMsg(request);
            }
        }
    } catch (IOException e) {
        e.printStackTrace();
    } catch (Exception e) {
        e.printStackTrace();
    }
}
    public static void main(String[] args) throws Exception
    {
        Server server = new Server(1234);
        //run to listen to clients and execute
        server.run();
    }

}