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
import java.util.Calendar;
import java.util.Date;

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

    public static Calendar calendarFromDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    /**
     * オブジェクト配列をテーブルウィジェットに表示できるように文字列に変換します
     * 
     * @param data テーブルに表示する内容
     * @return テーブルに表示する内容
     */
    public static String[] toStringArray(Comparable[] data) {
        String[] ret = new String[data.length];
        for (int i = 0; i < data.length; ++i) {
            if (data[i] == null) {
                ret[i] = "";
            }
            else {
                ret[i] = data[i].toString();
            }
        }
        return ret;
    }

}
