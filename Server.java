
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Key;
import java.security.SecureRandom;


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
/*
    Start the server on a specific port
 */
public Server(int port)
{
    try {
        serverSocket = new ServerSocket(port);
        Count=0;
        clients = new Client[10];
    } catch (IOException e) {
        System.err.println("Port is in use");
        System.out.println("Starting the server on port: "+port+1 );
        new Server(port+1);
    }

}
/*
    Accept or decline a client connection "Connection in this phase is not encrypted"
 */
public Client auth(Socket connection) throws Exception {
    Client client = new Client(connection);
    SecureRandom random = new SecureRandom();
    //generate a random string, used for authentication and as an ID for the client
    String str = new BigInteger(130,random).toString(32);
    //encrypt and send, if we get the same string back then we'll allow the connection
    client.sendMsg(str);
    String reply = client.getRequest();
    //auth succeed
    if (reply.equals(str))
    {
        client.setPath(client.getPath()+"/"+str);
        File folder = new File(client.getPath());
        //make a direcotry for the connected user/or use and old one
        folder.mkdir();
        //increment Count
        Count++;
        //return the connected client
        client.sendMsg("Welcome!");
        return client;
    }
    File folder = new File(client.getPath());
    try
    {
        //delete the folder for the new client in case auth fails
        folder.delete();
    }catch(Exception e)
    {
        System.out.println("the Folder for the newly unauthorized client contains some other files and that's so odd");
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

}
    public static void main(String[] args) throws Exception
    {
        Server server = new Server(3245);
        server.run();
//        byte[] arr = new byte[1024*1024*500];//500 MBit's
//        new Random().nextBytes(arr);
//        FileOutputStream fileOut = new FileOutputStream("/home/obada/Desktop/big.txt");
//        fileOut.write(arr);
//        fileOut.flush();
//        fileOut.close();
    }

}