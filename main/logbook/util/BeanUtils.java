package logbook.util;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.annotation.CheckForNull;

/**
 * JavaBeanのutilです
 *
 */
public final class BeanUtils {

    /**
     * JavaBeanオブジェクトをXML形式でファイルに書き込みます
     * 
     * @param file ファイル
     * @param obj JavaBean
     * @throws IOException IOException
     */
    public static void writeObject(File file, Object obj) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }
            if (!(file.canWrite()))
                throw new IOException("File '" + file + "' cannot be written to");
        } else {
            File parent = file.getParentFile();
            if ((parent != null) &&
                    (!(parent.mkdirs())) && (!(parent.isDirectory()))) {
                throw new IOException("Directory '" + parent + "' could not be created");
            }
        }
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
            XMLEncoder encoder = new XMLEncoder(out);
            encoder.writeObject(obj);
            encoder.close();
        }
    }

    /**
     * <p>
     * XML形式で書き込まれたファイルからJavaBeanオブジェクトを復元します<br>
     * 復元時に型の検査を行います
     * </p>
     * 
     * @param file ファイル
     * @param clazz 期待する型
     * @return オブジェクト
     */
    @CheckForNull
    public static <T> T readObject(File file, Class<T> clazz) {
        if (!file.canRead()) {
            return null;
        }
        try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
            try (XMLDecoder decoder = new XMLDecoder(in)) {
                Object obj = decoder.readObject();
                if (clazz.isInstance(obj)) {
                    return (T) obj;
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
