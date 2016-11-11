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
    private static ServerSocket serverSocket;
    //connected clients
    private static ArrayList<Client> clients;// to do manage the connected clients
    //the number of connected clients
    private static int index;
    // folder the save the uploaded files by clients
    private static File root;
/*
    Start the server on a specific port
 */
public Server(int port)
{
    try {
        serverSocket = new ServerSocket(port);
        index=0;
        //Max 10 clients
        clients = new ArrayList<>();
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
        //return the connected client
        return client;
    }
    client.close();
    return null;
}
    /*
        The main run method
     */
private void run()
{
    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    String request,next;
    Scanner scan;
    try {
        String filePath;
        String dirPath;
        while((request=in.readLine())!=null)
        {
            scan = new Scanner(request);
            next = scan.next();
            switch(next) {
                case "change-client":
                    index = Integer.parseInt(scan.next());
                    if (index >= clients.size()) {
                        index = 0;
                        System.out.println("no such client, please select one of the below clients");
                        getClients();
                    }
                break;
                case "upload-to":
                    //notify the client on the Client side to receive the file
                    clients.get(index).sendMsg(request);
                    //the path of the file we're uploading
                    filePath = scan.next();
                    //send the file
                    clients.get(index).sendFile(filePath);
                    break;
                case "upload-dir-to":
                    //the path of the dir we're going to upload
                    dirPath = scan.next();
                    //compress the dir and save it in the working dir
                    Utilities.zipDir(new File(dirPath),System.getProperty("java.io.tmpdir")+"/dir.zip");
                    //notify the client on the Client side
                    clients.get(index).sendMsg(request);
                    //send the compressed file (notice it's on the working dir)
                    clients.get(index).sendFile("dir.zip");
                    break;
                case "upload":
                    //send the request to the client on the Client side
                    clients.get(index).sendMsg(request);
                    //to upload file path
                    filePath = scan.next();
                    //send the file
                    clients.get(index).sendFile(filePath);
                    break;
                case "upload-dir":
                    //compress the dir...
                    Utilities.zipDir(new File(scan.next()),System.getProperty("java.io.tmpdir")+"/dir.zip");
                    //notify the Client
                    clients.get(index).sendMsg(request);
                    //send the compressed `file
                    clients.get(index).sendFile(System.getProperty("java.io.tmpdir")+"/dir.zip");
                    break;
                default:
                    clients.get(index).sendMsg(request);
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
        run();
    }
}
private void listen()
{
    //***********************************************************************************************\\
    new Thread()// thread waiting for connections
    {
        public void run()
        {
            //listen for connections
            while(true)
                try {
                    System.out.println("Waiting for connection");
                    Client client = null;
                    //we'll keep waiting until auth returns a client
                    while (client == null)
                        client = auth(serverSocket.accept());
                    System.out.println("client connected");
                    final Client clientFinal = client;
                    clients.add(clientFinal);
                    getClients();
                    new Thread() {
                        public void run() {
                            try {
                                clientFinal.listen();
                            } catch (Exception e)
                            {
                                e.getMessage();
                            }
                            index=0;
                            for(int i=0; i<clients.size(); i++)
                            {
                                if(clients.get(i).getIp().equals(clientFinal.getIp())) {
                                    clients.remove(i);
                                    break;
                                }
                            }
                            System.out.println("[+]User disconnected!");
                            getClients();
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
private void getClients()
{
    for(int i=0; i<clients.size(); i++)
    {
        System.out.println("["+i+"] "+clients.get(i).getIp()+"\\"+clients.get(i).getHost());
    }
}

    public static void main(String[] args) throws Exception
    {
        Server server = new Server(1234);
        //Thread to listen for connections, receive requests from the clients
        server.listen();
        //send orders to clients
        server.run();
    }

}