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

    public static void main(String[] args) {
        Server server = new Server(3245);
        server.run();// example
//    byte[] tmp = new byte[]{1,2,3};
//    byte[] tmp2 = new byte[]{4,5,6};
//    byte[] res=null;
//    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
//    try {
//        buffer.write(tmp2);
//        buffer.write(tmp);
//        buffer.write(new byte[]{-1});
//        res=buffer.toByteArray();
//        System.out.println(res[res.length-1]);
//    } catch (IOException e) {
//        e.printStackTrace();
//    }


    }

/*
    Accept or decline a client connection "Connection in this phase is not encrypted"
 */
public Client auth(Socket connection)
{
    try (OutputStream out = connection.getOutputStream();
         InputStream in = connection.getInputStream())
    {
        String random = "";
        out.write(encrypt(random.getBytes()));//will generate a random string later
        String answer = "";
        if (answer.equals(random))
            return new Client(connection);
    } catch (EOFException e) {

    } catch (IOException e) {
        e.printStackTrace();
    } catch (Exception e) {
        e.printStackTrace();
    }
    return null;
}

/*
    list all available files in a directory
 */
public void ls(Client client, String location) {
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
         {
             try {
                 System.out.println("listening for a client");
                 Client client = new Client(serverSocket.accept());
                 System.out.println("client connected");
                 new Thread()
                 {
                     public void run() {
                         client.listen();
                     }
                 }.start();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
    }.start(); // A thread to listen for connections
//***************************************************************************************************\\

}
}