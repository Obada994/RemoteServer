
import java.io.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.Base64;
import java.util.Scanner;

/**
 * Created by Obada on 2016-09-12.
 */
 class Client
{

    private InputStream input;
    private OutputStream output;
    private Socket socket;
    private  int id;
    private File folder;

    Client(Socket socket,int id)
{
    connect(socket);
    this.id = id;
    folder = new File("/home/obada/Desktop/client"+id);
    folder.mkdir();
}
private void connect(Socket sock)
{
    this.socket = sock;
    try {
        input = socket.getInputStream();
        output = socket.getOutputStream();
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
    byte[] bytes = new byte[64*1024];
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
    String next;
    Scanner scan;
    try {
        while (!(request = getRequest()).equals(""))
        {
            scan = new Scanner(request);
            next = scan.next();
            switch (next)
            {
                //sending a file to the client
                case "get":
                    request = request.substring(4,request.length());
                    //send a notification for the caller client to receive the file
                    sendMsg("upload "+request);
                    //the next String contain the path to the file
                    sendFile(scan.next());
                    break;
                //list the files the server has in this client's folder
                case "ls":
                    //will work on the runtime command later..
                    sendMsg("will work on this later....");
                    break;
                //getting a file from the client
                case "upload":
                    scan.next();
                    getFile("/home/obada/Desktop/client"+id+"/"+scan.next()+"."+scan.next());
                    break;
                //close connection
                case "close":
                    close();
                    break;

                default:
                    System.out.println(request);

            }
            scan.close();
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
        byte[] bytes=new byte[1024*1024];
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
        System.out.println("getting a file");
        //performance testing
        long startTime = System.currentTimeMillis();
        //file to receive
        File file = new File(location);
        //wrap InputStream in an ObjectInputStream
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
                System.out.println("reading...");
                //decrypt them
                decrypted=Server.decrypt(bytes);
                //write the decrypted arrays to the file
                out.write(decrypted);
            }
            //close the FileOutputStream
            out.close();
            System.out.println("got the file");
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
    output.close();
    input.close();
    socket.close();
    }
    int getId()
    {return id;}

    // A client sample code to connect and test out our server
    public static void main(String[] args) throws Exception {
        System.out.println("trying to connect");
        Socket sock = new Socket("localhost", 3245);
        Client client = new Client(sock, 0);
        System.out.println("connected!");
        //auth with server
        client.sendMsg(client.getRequest());
        new Thread() {
            public void run() {
                client.listen();
            }
        }.start();
        System.out.println("Listening....");
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String request;
        String next;
        Scanner scan;
        while ((request = in.readLine()) != null)
        {
            scan = new Scanner(request);
            next = scan.next();
            switch (next)
            {
                case "upload":
                    //send the request to the client(Server)
                    client.sendMsg(request);
                    //receive the file
                    client.sendFile(scan.next());
                    break;
                default:
                    client.sendMsg(request);

            }
            scan.close();

        }
    }
}