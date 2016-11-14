import java.io.*;
import java.util.Arrays;

/**
 * Created by obada on 2016-10-31.
 */
public class RedirectStream extends Thread {
    Client client;
    InputStream in;
    public RedirectStream(InputStream in, Client client)
    {
        this.in = in;
        this.client = client;
    }
    @Override
    public void run()
    {
        int len;
        byte[] buffer= new byte[1024*1024];
        try {
            while ((len = in.read(buffer)) > 0)
                client.sendMsg(new String(Arrays.copyOfRange(buffer, 0, len)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
