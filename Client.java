
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
    //ID used on the server side to identify clients
    private  String id;
    //the path for the client's Desktop
    private String path=System.getProperty("user.home") + "/Desktop";
    /*
    this constructor will be called by the server and it doesn't create a folder for the client either on the server side or the client's side
     */
    Client(Socket socket)
{
    connect(socket);
}
/*
this constructor will be called by the client where he can specify the name of the folder on his desktop
 */
private Client(Socket socket,String folder)
{
    //no need for an ID
    id = "";
    path = path+"/"+folder;
    File file = new File(path);
    file.mkdir();
    connect(socket);

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
        byte[] decrypted = Utilities.decrypt(received);
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
        byte[] encrypted = Utilities.encrypt(bytesEncoded);
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
                    //send a notification for the caller client to receive the file,we sent the path not to break the protocol
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
                    //get rid of the uploaded file path in the client pc "we don't need it"
                    scan.next();
                    getFile(path+"/"+scan.next()+"."+scan.next());
                    break;
                case "get-dir":
                    //zip the dir first,the zip folder will be created in the working dir
                    Utilities.zipDir(new File(scan.next()),"dir.zip");
                    System.out.println("zipping completed");
                    //notify client to receive
                    sendMsg("upload "+ request.substring(8,request.length()));
                    System.out.println("notified");
                    //send the file
                    sendFile("dir.zip");
                    break;
                case "upload-dir":
                    //get rid of "upload-dir "
                    scan.next();
                    getFile(path+"/"+scan.next()+"."+scan.next());

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
        System.out.println("Uploading file");
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
                byte[] encrypted = Utilities.encrypt(fileBytes);
                //write the encrypted array as an object
                outO.writeObject(encrypted);
            }
            //End of file signal
            outO.writeObject(null);
            System.out.println("Upload complete!");
        //FileNotFoundException then send a death signal "null" to the receiver client so it doesn't hang
        } catch (Exception e) {
            e.printStackTrace();
            try {
                outO = new ObjectOutputStream(output);
                outO.writeObject(null);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
    /*
           receive files from the clients
    */
    private void getFile(String location)
    {
        System.out.println("Downloading file...");
        //file to write to
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
                //decrypt them
                decrypted=Utilities.decrypt(bytes);
                //write the decrypted arrays to the file
                out.write(decrypted);
            }
            System.out.println("Download complete!");
            //close the FileOutputStream
            out.close();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
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

//    String getId()
//    {return id;}

    void setPath(String path)
    {this.path=path;}

    String getPath()
    {return path;}
    /*
    check if a request fulfill our protocol: <Command><SPACE><Path><SPACE><TitleOfFile><SPACE><ExtensionOfFile>
    note that TitleOfFile is the new name you give to your downloaded(get)/uploaded(upload) file
     */
    private boolean valid(String command)
    {
        try
        {
            //if Scanner crash then the protocol is not fulfilled
            Scanner scan = new Scanner(command);
            String token;
            token = scan.next();
            //check if the first token is a valid command
            if(!token.equals("get") && !token.equals("upload") && !token.equals("upload-dir") && !token.equals("get-dir"))
                throw new Exception("Invalid command");
            token = scan.next();
            if(token.length()==0)
                throw new Exception("Invalid path");
            token = scan.next();
            if(token.length()==0)
                throw new Exception("Invalid name");
            if(token.length()==0)
                throw new Exception("Invalid extension");
        }catch(Exception e)
        {
            help();
            return false;
        }
        return true;
    }
    private void help()
    {
        System.out.println
                (
                "Syntax: <Command> <Path> <FileName> <Extension>\n" +
                "Sample: get /home/<username>/Desktop/FileName.zip newFileName zip\n" +
                "Commands: upload, get"
                );
    }

    // A client sample code to connect and test out our server
    public static void main(String[] args) throws Exception {
        System.out.println("Welcome to MyCloud\nTrying to connect");
        Socket sock = new Socket("localhost", 3245);
        //init client and the download folder
        Client client = new Client(sock, "MyCloud");
        System.out.println("connected!");
        //auth with server
        client.sendMsg(client.getRequest());
        new Thread() {
            public void run() {
                client.listen();
            }
        }.start();
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String request;
        String next;
        Scanner scan;
        while ((request = in.readLine()) != null)
        {
            if(!client.valid(request))
                continue;
            scan = new Scanner(request);
            next = scan.next();
            switch (next)
            {
                case "upload":
                    //send the request to the client(Server)
                    client.sendMsg(request);
                    //send the file
                    client.sendFile(scan.next());
                    break;
                case "dir-change":
                    //change the saving directory
                    client.setPath(scan.next());
                    break;
                case "upload-dir":
                    //compress the dir
                    Utilities.zipDir(new File(scan.next()),"dir.zip");
                    //notify the server/client
                    client.sendMsg(request);
                    //send the compressed file
                    client.sendFile("dir.zip");
                    break;
                default:
                    client.sendMsg(request);

            }
            scan.close();
        }
    }
}