package logbook.constants;

import java.io.File;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.TimeZone;

import org.eclipse.swt.graphics.RGB;

/**
 * アプリケーションで使用する共通の定数クラス
 *
 */
public class AppConstants {

    /**　アプリケーション名 */
    public static final String NAME = "航海日誌";

    /** バージョン */
    public static final String VERSION = "0.7.6";

    /** ホームページ */
    public static final URI HOME_PAGE_URI = URI.create("https://kancolle.sanaechan.net/");

    /** アップデートチェック先 */
    public static final URI UPDATE_CHECK_URI = URI.create("http://kancolle.sanaechan.net/checkversion.txt");

    /** 日付書式 */
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /** 日付書式(時刻のみ) */
    public static final String DATE_SHORT_FORMAT = "HH:mm:ss";

    /** 日付書式(日付のみ) */
    public static final String DATE_DAYS_FORMAT = "yyyy-MM-dd";

    /** 日付書式(ミリ秒を含む) */
    public static final String DATE_LONG_FORMAT = "yyyy-MM-dd HH-mm-ss.SSS";

    /** タイムゾーン(任務が更新される05:00JSTに0:00になるタイムゾーン) */
    public static final TimeZone TIME_ZONE_MISSION = TimeZone.getTimeZone("GMT+04:00");

    /** 疲労赤色 */
    public static final int COND_RED = 19;

    /** 疲労オレンジ色 */
    public static final int COND_ORANGE = 29;

    /** 疲労緑色 */
    public static final int COND_GREEN = 50;

    /** 遠征色 */
    public static final RGB MISSION_COLOR = new RGB(102, 51, 255);

    /** 入渠色 */
    public static final RGB NDOCK_COLOR = new RGB(0, 102, 153);

    /** 疲労赤色 */
    public static final RGB COND_RED_COLOR = new RGB(255, 16, 0);

    /** 疲労オレンジ色 */
    public static final RGB COND_ORANGE_COLOR = new RGB(255, 140, 0);

    /** 疲労緑色 */
    public static final RGB COND_GREEN_COLOR = new RGB(0, 128, 0);

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

    /** 大破(25%) */
    public static final float BADLY_DAMAGE = 0.25f;

    /** 補給(少) */
    public static final float LOW_SUPPLY = 0.77f;

    /** 補給(空) */
    public static final float EMPTY_SUPPLY = 0.33f;

    /** 文字コード(Shift_JIS) */
    public static final Charset CHARSET = Charset.forName("MS932");

    /** アプリケーション設定ファイル  */
    public static final File APP_CONFIG_FILE = new File("./config/internal.xml");

    /** 艦娘設定ファイル  */
    public static final File SHIP_CONFIG_FILE = new File("./config/ship.xml");

    /** 装備一覧設定ファイル  */
    public static final File ITEM_CONFIG_FILE = new File("./config/item.xml");

    /** 装備マスター設定ファイル  */
    public static final File ITEM_MST_CONFIG_FILE = new File("./config/itemmst.xml");

    /** 建造ドック設定ファイル  */
    public static final File KDOCK_CONFIG_FILE = new File("./config/kdock.xml");

    /** 所有艦娘グループ設定ファイル  */
    public static final File GROUP_CONFIG_FILE = new File("./config/group.xml");

    /** 保有資材:燃料 */
    public static final int MATERIAL_FUEL = 1;

    /** 保有資材:弾薬 */
    public static final int MATERIAL_AMMO = 2;

    /** 保有資材:鋼材 */
    public static final int MATERIAL_METAL = 3;

    /** 保有資材:ボーキサイト */
    public static final int MATERIAL_BAUXITE = 4;

    /** 保有資材:バーナー */
    public static final int MATERIAL_BURNER = 5;

    /** 保有資材:高速修復材 */
    public static final int MATERIAL_BUCKET = 6;

    /** 保有資材:開発資材 */
    public static final int MATERIAL_RESEARCH = 7;

    /** /resources/icon/add.png */
    public static final String R_ICON_ADD = "/resources/icon/add.png";

    /** /resources/icon/delete.png */
    public static final String R_ICON_DELETE = "/resources/icon/delete.png";

    /** /resources/icon/error.png */
    public static final String R_ICON_ERROR = "/resources/icon/error.png";

    /** /resources/icon/exclamation.png */
    public static final String R_ICON_EXCLAMATION = "/resources/icon/exclamation.png";

    /** /resources/icon/folder.png */
    public static final String R_ICON_FOLDER = "/resources/icon/folder.png";

    /** /resources/icon/star.png */
    public static final String R_ICON_STAR = "/resources/icon/star.png";

    /** 艦隊タブの艦娘ラベルに設定するツールチップテキスト */
    public static final String TOOLTIP_FLEETTAB_SHIP = "HP:{0}/{1} 燃料:{2}/{3} 弾:{4}/{5}\nNext:{6}exp";

    /** メッセージ 出撃できます。 */
    public static final String MESSAGE_GOOD = "出撃できます。";

    /** メッセージ {0} 出撃はできません。 */
    public static final String MESSAGE_BAD = "{0} 出撃はできません。";

    /** メッセージ 大破している艦娘がいます  */
    public static final String MESSAGE_BADLY_DAMAGE = "大破している艦娘がいます";

    /** メッセージ 入渠中の艦娘がいます  */
    public static final String MESSAGE_BATHWATER = "入渠中の艦娘がいます";

    /** メッセージ 遠征中です。  */
    public static final String MESSAGE_MISSION = "遠征中です。";

    /** メッセージ 疲労している艦娘がいます */
    public static final String MESSAGE_COND = "疲労している艦娘がいます {0}頃に回復します。";

    /** メッセージ 大破している艦娘がいます */
    public static final String MESSAGE_STOP_SORTIE = "大破している艦娘がいます、進撃はできません。";

    /** メッセージ 制空値:{0} */
    public static final String MESSAGE_SEIKU = "制空値:{0}。";

    /** メッセージ  索敵値計:{0} */
    public static final String MESSAGE_SAKUTEKI = "索敵値計:{0}。";

    /** メッセージ  艦隊合計Lv:{0} */
    public static final String MESSAGE_TOTAL_LV = "艦隊合計Lv:{0}。";

    /** 海戦・ドロップ報告書.csv */
    public static final String LOG_BATTLE_RESULT = "海戦・ドロップ報告書.csv";

    /** 海戦・ドロップ報告書_alternativefile.csv */
    public static final String LOG_BATTLE_RESULT_ALT = "海戦・ドロップ報告書_alternativefile.csv";

    /** 建造報告書.csv */
    public static final String LOG_CREATE_SHIP = "建造報告書.csv";

    /** 建造報告書_alternativefile.csv */
    public static final String LOG_CREATE_SHIP_ALT = "建造報告書_alternativefile.csv";

    /** 開発報告書.csv */
    public static final String LOG_CREATE_ITEM = "開発報告書.csv";

    /** 開発報告書_alternativefile.csv */
    public static final String LOG_CREATE_ITEM_ALT = "開発報告書_alternativefile.csv";

    /** 遠征報告書.csv */
    public static final String LOG_MISSION = "遠征報告書.csv";

    /** 遠征報告書.csv */
    public static final String LOG_MISSION_ALT = "遠征報告書_alternativefile.csv";

    /** 資材ログ.csv */
    public static final String LOG_RESOURCE = "資材ログ.csv";

    /** 資材ログ_alternativefile.csv */
    public static final String LOG_RESOURCE_ALT = "資材ログ_alternativefile.csv";
}
