import java.io.File;

/**
 * Created by obada on 2016-10-23.
 */
public class Tests
{
    public static void main(String[] args)
    {
        int[] a = new int[]{1,2,3};
        int[] b = null;
        b = a;
        System.out.println(b[0]);
    }
}
