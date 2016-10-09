import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Obada on 2016-09-12.
 */
public class Server
{
    static ServerSocket serverSocket;

    static Client[] clients;

    static int id;
/*
    Start the server on a specific port
 */
public Server(int port)
{
    try {
        serverSocket = new ServerSocket(port);
        id = 0;
        clients = new Client[10];
    } catch (IOException e) {
        System.err.println("Port is in use");
        System.out.println("Starting the server on port: "+port+1 );
        new Server(port+1);
    }

}

/*
    Encrypt an object(msg,file...etc) with AES 128 bit encryption
 */
public static byte[] encrypt(byte[] Data) throws Exception
{
    Key key = generateKey();
    Cipher c = Cipher.getInstance("AES");
    c.init(Cipher.ENCRYPT_MODE, key);
    byte[] encBytes = c.doFinal(Data);
    return encBytes;
}

/*
    Decrypt a received byte array from a client
 */
public static byte[] decrypt(byte[] encryptedData) throws Exception
{
    Key key = generateKey();
    Cipher c = Cipher.getInstance("AES");
    c.init(Cipher.DECRYPT_MODE, key);
    byte[] decValue = c.doFinal(encryptedData);
    return decValue;
}

/*
    Generate a key for your AES encryption
 */
private static Key generateKey()
{
    //A 16 bytes/128bits key, put your own secret key here
    String Key = "secret1234ewrt54";
    Key key = new SecretKeySpec(Key.getBytes(), "AES");
    return key;
}
/*
    Accept or decline a client connection "Connection in this phase is not encrypted"
 */
public Client auth(Socket connection) throws Exception {
    Client client = new Client(connection, id);
    String msg = "weShouldGetTheStringDecrypted";
    client.sendMsg(msg);
    String reply = client.getRequest();
    //auth succeed increment id
    if (reply.equals(msg))
    {
        id++;
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
                 clients[clientFinal.getId()] = clientFinal;
                 new Thread() {
                     public void run() {
                         clientFinal.listen();
                     }
                 }.start();
                 System.out.println("Server: client listening started");
             } catch (Exception e) {
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
    }

}