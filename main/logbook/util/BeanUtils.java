package logbook.util;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

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
        File backup = new File(file.getAbsolutePath() + ".backup");
        if ((file.exists() && (file.length() > 0)) && (!backup.exists() || backup.delete())) {
            // ファイルが存在してかつサイズが0を超える場合、バックアップを削除した後、ファイルをバックアップにリネームする
            file.renameTo(backup);
        }
        try (XMLEncoder encoder = new XMLEncoder(new FileOutputStream(file))) {
            encoder.writeObject(obj);
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
        File target = file;

        if (!target.canRead() || (target.length() <= 0)) {
            // ファイルが読み込めないまたはサイズがゼロの場合バックアップファイルを読み込む
            target = new File(file.getAbsolutePath() + ".backup");
            if (!target.canRead()) {
                // バックアップファイルも読めない場合nullを返す
                return null;
            }
        }
        try (XMLDecoder decoder = new XMLDecoder(new FileInputStream(target))) {
            Object obj = decoder.readObject();
            if (clazz.isInstance(obj)) {
                return (T) obj;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
