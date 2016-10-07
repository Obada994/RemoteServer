
import java.io.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.Base64;

/**
 * Created by Obada on 2016-09-12.
 */
 class Client {

    private InputStream input;
    private OutputStream output;
    private Socket socket;

    private int id;
//................................Created for testing only
//   private static Socket sock;
//    private static OutputStream out=null;
//...............................
//    String folder; // will be user later on
//    boolean root = false;// will be user later on
//    String ID;// will be user later on

 Client(Socket socket,int id)
{
    this.socket = socket;
    try {
        input = socket.getInputStream();
        output = socket.getOutputStream();
        this.id = id;
    } catch (IOException e) {
        e.printStackTrace();
    }
}
    /*
       Receive a string encrypted with AES and encoded with base64
     */
    String getRequest() throws Exception
{
    int count;
    byte[] bytes = new byte[32*1024];
    if ((count = input.read(bytes)) > 0) {
        byte[] received = Arrays.copyOfRange(bytes, 0, count);
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
    //will change to public access if I had to send messages from the Server class
    void sendMsg(String msg) throws Exception {
        //decode the msg into base64
        byte[] bytesEncoded = Base64.getEncoder().encode(msg.getBytes());
        //encrypt the byte array
        byte[] encrypted = Server.encrypt(bytesEncoded);
        //send the encrypted array
        output.write(encrypted);
    }

    /*
        listen for requests and handle them , "just some test cases here for files transfer"
     */
    void listen()
{
    String request;
    try {
        while (!(request = getRequest()).equals("")) switch (request) {
            //sending a file to the client
            case "get":
                //the next String contain the path to the file
                sendFile(getRequest());
                break;
            //list the files the server has in this client's folder
            case "ls":
                //will work on the runtime command later..
                sendMsg("will work on this later....");
                break;
            //getting a file from the client
            case "upload":
                getFile("will work on specify a working dir for this client later...");
                break;
            //close connection
            case "close":
                close();
                break;

            default:
                System.out.println(request);

        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}

    /*
        send files to clients Encrypted with AES 128 bit
     */
    private void sendFile(String location) {
        //file to send
        File file = new File(location);
        // wrap OutputStream in an ObjectOutputStream
        ObjectOutputStream outO;
        //bytes array to read bytes from the file
        byte[] bytes=new byte[1024*64];
        //count of bytes read from the file
        int count;
        try (FileInputStream inFile = new FileInputStream(file))
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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /*
           receive files from the clients
         */
    private void getFile(String location)
    {
        //performance testing
        long startTime = System.currentTimeMillis();
        //file to receive
        File file = new File(location);
        //wrap InputStream   in ObjectInputStream
        ObjectInputStream in;
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
            //close the FileOutputStream
            out.close();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        //performance testing
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.out.println("getFile took: "+elapsedTime);
    }

    /*
        close the connection for this client
     */
    void close() throws IOException
    {
    input.close();
    output.close();
    socket.close();
    }
    int getId()
    {return id;}

    // A client sample code to connect and test out our server
//    public static void main(String[] args) throws Exception {
//        System.out.println("trying to connect");
//        sock = new Socket("localhost", 3245);
//        System.out.println("connected!");
//        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
//        String request = "";
////        InputStream input = sock.getInputStream();
//        out = sock.getOutputStream();
//        //....................................
//        while ((request = in.readLine()) != null) {
//            //decode the msg into base64
//            byte[] bytesEncoded = Base64.getEncoder().encode(request.getBytes());
//            //encrypt the byte array
//            byte[] encrypted = Server.encrypt(bytesEncoded);
//            //send the encrypted array
//            out.write(encrypted);
//            if (request.equals("hi"))
//            {
//
//            } else if (request.equals("hi2"))
//            {
//                File file = new File("/home/obada/Desktop/Java.zip");
//                ObjectOutputStream outO = new ObjectOutputStream(out);
//                try (InputStream inFile = new FileInputStream(file)) {
//                    byte[] bytes = new byte[1024 * 64];
//                    int count;
//                    while ((count = inFile.read(bytes)) > 0) {
//                        byte[] fileBytes = Arrays.copyOfRange(bytes, 0, count);
//                        byte[] encrypt = Server.encrypt(fileBytes);
//                        outO.writeObject(encrypt);
//                    }
//                    outO.writeObject(null);
//                    System.out.println("file sent");
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
}