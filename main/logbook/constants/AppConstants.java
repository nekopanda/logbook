/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
package logbook.constants;

import java.io.File;
import java.net.URI;
import java.nio.charset.Charset;

import org.eclipse.swt.graphics.RGB;

/**
 * アプリケーションで使用する共通の定数クラス
 *
 */
public class AppConstants {

    /** バージョン */
    public static final String VERSION = "0.5.1";

    /** ホームページ */
    public static final URI HOME_PAGE_URI = URI.create("http://kancolle.sanaechan.net/");

    /** アップデートチェック先 */
    public static final URI UPDATE_CHECK_URI = URI.create("http://kancolle.sanaechan.net/checkversion.txt");

    /** 日付書式 */
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /** 日付書式 */
    public static final String DATE_SHORT_FORMAT = "HH:mm:ss";

    /** 疲労赤色 */
    public static final int COND_RED = 19;

    /** 疲労オレンジ色 */
    public static final int COND_ORANGE = 29;

    /** 遠征色 */
    public static final RGB MISSION_COLOR = new RGB(102, 51, 255);

    /** 入渠色 */
    public static final RGB NDOCK_COLOR = new RGB(0, 102, 153);

    /** 疲労赤色 */
    public static final RGB COND_RED_COLOR = new RGB(255, 16, 0);

    /** 疲労オレンジ色 */
    public static final RGB COND_ORANGE_COLOR = new RGB(255, 140, 0);

    /** 5分前 */
    public static final RGB TIME_IN_5_MIN = new RGB(255, 215, 0);

    /** 10分前 */
    public static final RGB TIME_IN_10_MIN = new RGB(255, 239, 153);

    /** 20分前 */
    public static final RGB TIME_IN_20_MIN = new RGB(255, 247, 203);

    /** テーブル行(偶数行)背景色 */
    public static final RGB ROW_BACKGROUND = new RGB(246, 246, 246);

    /** 小破(75%) */
    public static final float SLIGHT_DAMAGE = 0.75f;

    /** 中破(50%) */
    public static final float HALF_DAMAGE = 0.5f;

    /** 中破(25%) */
    public static final float BADLY_DAMAGE = 0.25f;

    /** 補給(少) */
    public static final float LOW_SUPPLY = 0.99f;

    /** 補給(空) */
    public static final float EMPTY_SUPPLY = 0.79f;

    /** 文字コード(Shift_JIS) */
    public static final Charset CHARSET = Charset.forName("MS932");

    /** アプリケーション設定ファイル  */
    public static final File APP_CONFIG_FILE = new File("./config/internal.xml");

    /** 艦娘設定ファイル  */
    public static final File SHIP_CONFIG_FILE = new File("./config/ship.xml");

    /** 建造ドック設定ファイル  */
    public static final File KDOCK_CONFIG_FILE = new File("./config/kdock.xml");

    /** 所有艦娘グループ設定ファイル  */
    public static final File GROUP_CONFIG_FILE = new File("./config/group.xml");
}
