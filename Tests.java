import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by obada on 2016-10-23.
 */
public class Tests
{
    public static void main(String[] args) throws IOException {
        FileOutputStream override = new FileOutputStream("C:/users/abdul/Desktop/12.JPG");
        override.write(new byte[]{12,12,12});
        override.close();
    }
}
