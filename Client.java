import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by obada on 2016-09-12.
 */
public class Client {

    BufferedReader input; // #1
    PrintWriter output;// #2  are only for communication
    Socket socket;// #3

    static String folder; // location of the client's folder on the server
    static boolean root = false;

public Client(Socket socket)
{
    this.socket = socket;
    try {
        input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        output = new PrintWriter(this.socket.getOutputStream());
    } catch (IOException e) {
        e.printStackTrace();
    }
}
public String getRequest()
{
    String str;
    try {
        if((str=input.readLine())!=null)
            return str;
    } catch (IOException e) {
        e.printStackTrace();
    }
    return "";
}
public void close() throws IOException {
    input.close();
    output.close();
    socket.close();
}
}
