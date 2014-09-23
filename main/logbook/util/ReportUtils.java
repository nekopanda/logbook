/**
 * 
 */
package logbook.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

/**
 * @author Nekopanda
 *
 */
public class ReportUtils {

    /**
     * ファイルがロックされているかを確認します
     * 
     * @param file ファイル
     * @return
     * @throws IOException
     */
    public static boolean isLocked(File file) throws IOException {
        if (!file.isFile()) {
            return false;
        }
        try {
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            try {
                FileChannel channel = raf.getChannel();
                FileLock lock = channel.tryLock();
                if (lock == null) {
                    return true;
                }
                lock.release();
                return false;
            } finally {
                raf.close();
            }
        } catch (FileNotFoundException e) {
            return true;
        }
    }

}
