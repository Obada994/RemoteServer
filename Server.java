import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.Scanner;


/**
 * Created by Obada on 2016-09-12.
 */
public class Server
{
    private static ServerSocket serverSocket;
    //connected clients
    private static Client[] clients;// to do manage the connected clientss
    //the number of connected clients
    private static int Count;
    // folder the save the uplaoded files by clients
    private static File root;
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
private Client auth(Socket connection) throws Exception {
    Client client = new Client(connection);
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
    Thread to wait for connections and listen to clients
*/
private void listen()
{
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
                 clients[0] = clientFinal;
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
}
private void run()
{
    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    String request,next;
    Scanner scan;
    int index;
    try {
        index = 0;
        String filePath;
        String dirPath;
        String rx = "[^\"\\s]+|\"(\\\\.|[^\\\\\"])*\"";
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
                    //notify the client on the Client side to receive the file
                    clients[index].sendMsg(request);
                    //the path of the file we're uploading
                    filePath = scan.findInLine(rx);
                    // get rid of the quotation marks
                    filePath = filePath.substring(1,filePath.length()-1);
                    //send the file
                    clients[index].sendFile(filePath);
                    break;
                case "upload-dir-to":
                    //the path of the dir we're going to upload
                    dirPath = scan.findInLine(rx);
                    // get rid of quatation marks
                    dirPath = dirPath.substring(1,dirPath.length()-1);
                    //compress the dir and save it in the working dir
                    Utilities.zipDir(new File(dirPath),System.getProperty("java.io.tmpdir")+"/dir.zip");
                    //notify the client on the Client side
                    clients[index].sendMsg(request);
                    //send the compressed file (notice it's on the working dir)
                    clients[index].sendFile("dir.zip");
                    break;
                case "upload":
                    //send the request to the client on the Client side
                    clients[index].sendMsg(request);
                    //uploaded file location
                    filePath = scan.findInLine(rx);
                    // get rid of the quotation marks
                    filePath = filePath.substring(1,filePath.length()-1);
                    //send the file
                    clients[index].sendFile(filePath);
                    break;
                case "upload-dir":
                    //the path of the dir we're going to upload
                    dirPath = scan.findInLine(rx);
                    // get rid of quotation marks
                    dirPath = dirPath.substring(1,dirPath.length()-1);
                    //compress the dir
                    Utilities.zipDir(new File(dirPath),System.getProperty("java.io.tmpdir")+"/dir.zip");
                    //notify the Client
                    clients[index].sendMsg(request);
                    //send the compressed `file
                    clients[index].sendFile(System.getProperty("java.io.tmpdir")+"/dir.zip");
                    break;
                default:
                    clients[index].sendMsg(request);
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}
public static void main(String[] args) throws Exception
{
    Server server = new Server(1234);
    //listen for connections
    server.listen();
    //read input from user to send requests to clients
    server.run();
}

}