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

import logbook.data.DataType;

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

    @SuppressWarnings("incomplete-switch")
    public static boolean isShipUpdate(DataType type) {
        switch (type) {
        case CHARGE:
        case CHANGE:
        case PORT:
        case SHIP2:
        case SHIP3:
        case MISSION_RESULT:
        case NDOCK:
        case DECK:
        case GET_SHIP:
        case DESTROY_SHIP:
        case POWERUP:
        case LOCK_SHIP:
        case NYUKYO_START:
        case NYUKYO_SPEEDCHANGE:
        case SLOT_EXCHANGE_INDEX:

            // 戦闘結果を反映させるため戦闘でも更新
        case BATTLE:
        case BATTLE_MIDNIGHT:
        case BATTLE_SP_MIDNIGHT:
        case BATTLE_NIGHT_TO_DAY:
        case COMBINED_AIR_BATTLE:
        case COMBINED_BATTLE:
        case COMBINED_BATTLE_MIDNIGHT:
        case COMBINED_BATTLE_SP_MIDNIGHT:
        case COMBINED_BATTLE_WATER:

            // 艦娘の入手
        case BATTLE_RESULT:
        case COMBINED_BATTLE_RESULT:
            return true;
        }
        return false;
    }

}
