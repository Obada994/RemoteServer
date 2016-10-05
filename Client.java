import org.omg.CORBA.portable.*;

import java.io.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.Socket;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Base64;

/**
 * Created by obada on 2016-09-12.
 */
public class Client {

    InputStream input; // #1
    OutputStream output;// #2
    Socket socket;// #3
//................................testing only
    static Socket sock;
    static OutputStream out=null;
//.......................................................................
    String folder; // will be user later on
    boolean root = false;// will be user later on
    String ID;// will be user later on

public Client(Socket socket)
{
    this.socket = socket;
    try {
        input = socket.getInputStream();
        output = socket.getOutputStream();
    } catch (IOException e) {
        e.printStackTrace();
    }
}

    // A client sample code to connect and test out our server
    public static void main(String[] args) throws Exception {
        System.out.println("trying to connect");
        sock = new Socket("localhost", 3245);
        System.out.println("connected!");
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String request = "";
        InputStream input = sock.getInputStream();
        out = sock.getOutputStream();
        //....................................
        while ((request = in.readLine()) != null) {
            //decode the msg into base64
            byte[] bytesEncoded = Base64.getEncoder().encode(request.getBytes());
            //encrypt the byte array
            byte[] encrypted = Server.encrypt(bytesEncoded);
            //send the encrypted array
            out.write(encrypted);
            if (request.equals("hi"))
            {

            } else if (request.equals("hi2"))
            {
                File file = new File("/home/obada/Desktop/Java.zip");
                ObjectOutputStream outO = new ObjectOutputStream(out);
                try (InputStream inFile = new FileInputStream(file)) {
                    byte[] bytes=new byte[1024*64];
                    int count;
                    while ((count=inFile.read(bytes))>0)
                    {
                        byte[] fileBytes = Arrays.copyOfRange(bytes,0,count);
                        byte[] encrypt = Server.encrypt(fileBytes);
                        outO.writeObject(encrypt);
                    }
                    outO.writeObject(null);
                    System.out.println("file sent");

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*
        Receive a string encrypted with AES and encoded with base64
     */
    public String getRequest() throws Exception
{
    int count = 0;
    byte[] temp = new byte[32*1024];
    if ((count = input.read(temp)) > 0) {
        byte[] received = Arrays.copyOfRange(temp, 0, count);
        //decrypt the byte array
        byte[] decrypted = Server.decrypt(received);
        //decoded with base64
        byte[] valueDecoded = Base64.getDecoder().decode(decrypted);
        //return the string representation
        return new String(valueDecoded);
    }
    //client disconnected
    return "";
}

    /*
        Send a string encoded with base64 and encrypted with AES 128 bit
     */
    public void sendMsg(String msg) throws Exception {
        //decode the msg into base64
        byte[] bytesEncoded = Base64.getEncoder().encode(msg.getBytes());
        //encrypt the byte array
        byte[] encrypted = Server.encrypt(bytesEncoded);
        //send the encrypted array
        output.write(encrypted);
    }

    /*
        listen for requests and handle them
     */
    public void listen()
{
    String request;
    try {
        while (!(request = getRequest()).equals("")) {
            switch (request) {
                //incoming file
                case "hi":
                    System.out.println("listening for a file");
                    getFile("/home/obada/Desktop/new.pdf");
                    break;
                case "hi2":
                    System.out.println("listening for a file");
                    getFile("/home/obada/Desktop/new.zip");
                    break;
                default:
                    System.out.println(request);

            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}

    /*
        send files to clients Encrypted with AES 128 bit
     */
    public void sendFile(String location)
    {
        //file to send
        File file = new File(location);
        //wrap OutputStream in an ObjectOutputStream
        ObjectOutputStream outO;
        //bytes array to read bytes from the file
        byte[] bytes=new byte[1024*64];
        //count of bytes read from the file
        int count;
        try (InputStream inFile = new FileInputStream(file))
        {
            //init ObjectOutputStream
            outO = new ObjectOutputStream(output);
            //write byte arrays to a the output stream
            while ((count=inFile.read(bytes))>0)
            {
                //an array of the bytes read from the file
                byte[] fileBytes = Arrays.copyOfRange(bytes,0,count);
                //encrypted array
                byte[] encrypted = Server.encrypt(fileBytes);
                //write the encrypted array as an object
                outO.writeObject(encrypted);
            }
            //End of file signal
            outO.writeObject(null);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /*
       receive files from the clients
     */
    public void getFile(String location)
    {
        //performance testing
        long startTime = System.currentTimeMillis();
        //file to receive
        File file = new File(location);
        //wrap InputStream in ObjectInputStream
        ObjectInputStream in = null;
        try (OutputStream out = new FileOutputStream(file))
        {
            //init ObjectInputStream
            in = new ObjectInputStream(input);
            byte[] decrypted;
            byte[] bytes;
            //read the byte arrays of the sent file
            while ((bytes= (byte[]) in.readObject())!=null)
            {
                //decrypt them
                decrypted=Server.decrypt(bytes);
                //write the decrypted arrays to the file
                out.write(decrypted);
            }
            //close the stream
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //performance testing
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.out.println("getFile took: "+elapsedTime);
    }

    public void close() throws IOException {
    input.close();
    output.close();
    socket.close();
    }
}
