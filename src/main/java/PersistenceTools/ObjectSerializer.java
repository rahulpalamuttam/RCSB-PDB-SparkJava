package PersistenceTools;

import java.io.*;

/**
 * Used to serialize objects and store it as files
 * Created by rahul on 3/17/15.
 * <p/>
 * TODO :: Need to look into Scala pickling
 */
public class ObjectSerializer {
    /**
     * Writes the serialized object to a file
     */
    public static void writeObject(Object object, String objectFile) {
        try {
            OutputStream file = new FileOutputStream(objectFile);
            OutputStream buffer = new BufferedOutputStream(file, 1024 * 1024);
            ObjectOutput output = new ObjectOutputStream(buffer);
            output.writeObject(object);
            output.close();
            buffer.close();
            file.close();
            System.gc();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Loads the serialized object from objectFile.
     */
    public static Object loadObject(String objectFile) {
        Object HashTable = null;
        try {
            InputStream file = new FileInputStream(objectFile);
            InputStream buffer = new BufferedInputStream(file, 1024 * 1024);
            ObjectInput input = new ObjectInputStream(buffer);
            HashTable = input.readObject();
            input.close();
            buffer.close();
            file.close();
            System.gc();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return HashTable;
    }
}
