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

    String folder; // location of the client's folder on the server
    boolean root = false;
    String ID;

public Client(Socket socket)
{
    this.socket = socket;
    try {
        input = socket.getInputStream();
        output = socket.getOutputStream(); // true for auto flushing
    } catch (IOException e) {
        e.printStackTrace();
    }
}

    //A client sample code to connect and test out our server
    public static void main(String[] args) throws Exception {
        System.out.println("trying to connect");
        Socket socket = new Socket("localhost", 3245);
        System.out.println("connected!");
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String request = "";
        InputStream input = socket.getInputStream();
        OutputStream output = socket.getOutputStream();
        //....................................
        while ((request = in.readLine()) != null) {
            //decode the msg into base64
            byte[] bytesEncoded = Base64.getEncoder().encode(request.getBytes());
            //encrypt the byte array
            byte[] encrypted = Server.encrypt(bytesEncoded);
            //send the encrypted array
            output.write(encrypted);
            if (request.equals("hi")) {
                //send a file
                File file = new File("/home/obada/Desktop/Java.pdf");
                byte[] temp = new byte[1024 * 1024];
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] fileBytes;
                try (InputStream i = new FileInputStream(file);) {
                    int count;
                    while ((count = i.read(temp)) > 0) {
                        //create a sub array of the full array
                        byte[] tmp = Arrays.copyOfRange(temp, 0, count);
                        //store the array in buffer
                        buffer.write(tmp);
                    }
                    System.out.println("done with buffering");
                    //store the whole file bytes in one array
                    fileBytes = buffer.toByteArray();
                    //encrypt it
                    byte[] encrytped = Server.encrypt(fileBytes);
                    //send it
                    output.write(encrytped);
                    output.write(new byte[]{-1});

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (request.equals("hi2")) {
                File file = new File("/home/obada/Desktop/Java.pdf");
                final FileChannel channel = new FileInputStream(file).getChannel();
                MappedByteBuffer buff = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
                byte[] fileBytes = buff.array();
                byte[] encr = Server.encrypt(fileBytes);
                output.write(encr);
                output.write(new byte[]{-1});
            }
        }
    }

    /*
        Receive a string encrypted with AES and encoded with base64
     */
    public String getRequest() throws Exception
{
    int count = 0;
    byte[] temp = new byte[100];
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
        listen for requests and handle them , just some test cases here for files transfer
     */
    public void listen()
{
    String request = "";
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
                    getFile("/home/obada/Desktop/new.pdf");
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
        send any kinda file to a client
     */
    public void sendFile(String location) {
        File file = new File(location);
        byte[] bytes = new byte[64 * 1024];
        //the end signal of a file
        byte end = -1;
        try (InputStream input = new FileInputStream(file);) {
            int count;
            while ((count = input.read(bytes)) > 0) {
                //create a sub array of the full array
                bytes = Arrays.copyOfRange(bytes, 0, count);
                //encrypt it
                byte[] encrytped = Server.encrypt(bytes);
                //send it
                output.write(encrytped);
            }
            //send the end signal
            output.write(end);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    get a file from a client
    bug here with encryption: when you get the encrypted byte array divided in 2 arrays it's not possible to decrypt it
    temp solution is to make the buffered byte array as big as the file we're sending or bigger
 */
    public void getFile(String location) {
        System.out.println("getting a file");
        File file = new File(location);
        byte[] temp = new byte[1024 * 1024];
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] fileBytes;
        try (OutputStream out = new FileOutputStream(file)) {
            int count;
            while ((count = input.read(temp)) > 0) {
                //done for testing
                System.out.println(count);
                //tmp array with the downloaded bytes
                byte[] tmp = Arrays.copyOfRange(temp, 0, count);
                //if the end of the stream is the end signal then do the last iteration and quit the loop or you'll stuck here forever
                if (tmp[tmp.length - 1] == -1) {
                    System.out.println("last array of the file");
                    //create a new array without the signal value
                    tmp = Arrays.copyOfRange(tmp, 0, count - 1);
                    //add it to buffer
                    buffer.write(tmp);
                    //add all the bytes to one array p.s: it's encrypted
                    fileBytes = buffer.toByteArray();
                    //decrypt it
                    byte[] decrypted = Server.decrypt(fileBytes);
                    //write it to the file
                    out.write(decrypted);
                    //quit the while loop
                    break;
                }
                buffer.write(tmp);
            }
            System.out.println("file received");
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() throws IOException {
    input.close();
    output.close();
    socket.close();
}
}
