package logbook.constants;

import java.io.File;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;

/**
 * アプリケーションで使用する共通の定数クラス
 *
 */
public class AppConstants {

    /**　アプリケーション名 */
    public static final String NAME = "航海日誌";

    /** 派生版の名前 */
    public static final String SUFFIX = "拡張版";

    /** バージョン */
    public static final String VERSION = "2.1.9";

    /** ホームページ */
    public static final URI HOME_PAGE_URI = URI.create("http://nekopanda.blog.jp/");

    /** アップデートチェック先 */
    public static final URI UPDATE_CHECK_URI = URI
            .create("http://nekopandanet.sakura.ne.jp/logbook/version/okversions.txt");

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

    /** 戦闘ログファイルの名前 */
    public static final String BATTLE_LOGFILE_DATE_FORMAT = DATE_DAYS_FORMAT;

    /** 疲労赤色 */
    public static final int COND_RED = 20;

    /** 疲労オレンジ色 */
    public static final int COND_ORANGE = 30;

    /** 疲労緑色(偽) */
    public static final int COND_DARK_GREEN = 50;

    /** 疲労緑色 */
    public static final int COND_GREEN = 53;

    /** 疲労緑色 */
    public static final int COND_YELLOW = 75;

    /** 遠征色 */
    public static final RGB MISSION_COLOR = new RGB(102, 51, 255);

    /** 入渠色 */
    public static final RGB NDOCK_COLOR = new RGB(0, 102, 153);

    /** 疲労赤色 */
    public static final RGB COND_RED_COLOR = new RGB(255, 16, 0);

    /** 疲労オレンジ色 */
    public static final RGB COND_ORANGE_COLOR = new RGB(255, 140, 0);

    /** 疲労緑色(偽) */
    public static final RGB COND_DARK_GREEN_COLOR = new RGB(0, 60, 0);

    /** 疲労緑色 */
    public static final RGB COND_GREEN_COLOR = new RGB(0, 128, 0);

    /** テーブルの疲労度色 */
    public static final RGB[] COND_TABLE_COLOR = new RGB[] {
            new RGB(122, 206, 255), // #7aceff (100-85)
            new RGB(146, 255, 255), // #92ffff (84-76)
            new RGB(137, 240, 171), // #89f0ab (75-63)
            new RGB(170, 255, 143), // #aaff8f (62-50)
            new RGB(228, 255, 220), // #e4ffdc (49-40)
            new RGB(254, 207, 143), // #fecf8f (39-30)
            new RGB(252, 137, 94), // #fc895e (29-20)
            new RGB(253, 140, 143) // #fd8c8f (19-0)
    };

    public static final int[] COND_TABLE = new int[] {
            85,
            76,
            63,
            50,
            40,
            30,
            20,
            0
    };

    public static final RGB[][] CHART_COLOR_TABLE = new RGB[][] {
            new RGB[] { new RGB(0x00, 0x80, 0x00), new RGB(213, 94, 0) }, // 燃料 赤
            new RGB[] { new RGB(0x66, 0x33, 0x00), new RGB(0, 0, 0) }, // 弾薬 黒
            new RGB[] { new RGB(0x80, 0x80, 0x80), new RGB(0, 114, 178) }, // 鋼材  青
            new RGB[] { new RGB(0xCC, 0x33, 0x00), new RGB(86, 180, 233) }, // ボーキ 水色
            new RGB[] { new RGB(0xA5, 0x2A, 0x2A), new RGB(240, 228, 66) }, // バーナー 黄色
            new RGB[] { new RGB(0xF0, 0x80, 0x80), new RGB(230, 159, 0) }, // バケツ　オレンジ
            new RGB[] { new RGB(0x48, 0x76, 0xFF), new RGB(0, 158, 115) }, // 開発 緑
            new RGB[] { new RGB(0x00, 0xAB, 0xB2), new RGB(204, 121, 167) } // ネジ 紫
    };

    /** 無傷の色 */
    public static final RGB[] MUKIZU_SHIP_COLOR = new RGB[] { new RGB(149, 255, 165), null };

    /** 小破の色 */
    public static final RGB[] SYOHA_SHIP_COLOR = new RGB[] { new RGB(230, 255, 0), new RGB(241, 255, 163) };

    /** 中破の色 */
    public static final RGB[] TYUHA_SHIP_COLOR = new RGB[] { new RGB(255, 140, 0), new RGB(255, 252, 20) };
    public static final int[] TYUHA_FORGROUNG_COLOR = new int[] { SWT.COLOR_WHITE, SWT.COLOR_BLACK };

    /** 大破の色 */
    public static final RGB[] TAIHA_SHIP_COLOR = new RGB[] { new RGB(255, 16, 0), new RGB(213, 94, 0) };

    /** 轟沈の色 */
    public static final RGB SUNK_SHIP_COLOR = new RGB(77, 166, 223);

    /** 退避の色 */
    public static final RGB ESCAPED_SHIP_COLOR = new RGB(178, 178, 178);

    /** 敗北の色 */
    public static final RGB LOSE_BATTLE_COLOR = new RGB(230, 10, 20);

    /** 5分前 */
    public static final RGB TIME_IN_5_MIN = new RGB(255, 215, 0);

    /** 10分前 */
    public static final RGB TIME_IN_10_MIN = new RGB(255, 239, 153);

    /** 20分前 */
    public static final RGB TIME_IN_20_MIN = new RGB(255, 247, 203);

    /** 疲労回復1分前 */
    public static final RGB COND_IN_3_MIN = new RGB(0, 255, 37);

    /** 疲労回復5分前 */
    public static final RGB COND_WAITING = new RGB(149, 255, 165);

    /** 泊地中理中 */
    public static final RGB AKASHI_REPAIR_COLOR = new RGB(168, 211, 255);

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

    /** 艦載機装備アイテムのタイプID */
    public static final int[] PLANE_ITEM_TYPES = new int[] { 6, 7, 8, 9, 10, 11, 25, 26, 41 };

    /** 文字コード(Shift_JIS) */
    public static final Charset CHARSET = Charset.forName("MS932");

    /** アプリケーション設定ファイル  */
    public static final File APP_CONFIG_FILE = new File("./config/internal.xml");

    /** ユーザーゲームデータファイル  */
    public static final File USER_DATA_CONFIG = new File("./config/user.xml");

    /** マスターゲームデータ保存ファイル  */
    public static final File MASTER_DATA_CONFIG = new File("./config/master.xml");

    /** 建造ドック設定ファイル  */
    public static final File KDOCK_CONFIG_FILE = new File("./config/kdock.xml");

    /** 所有艦娘グループ設定ファイル  */
    public static final File GROUP_CONFIG_FILE = new File("./config/group.xml");

    /** 敵データファイル  */
    public static final File ENEMY_DATA_FILE = new File("./config/EnemyFleetRecord.csv");

    /** 旧敵データファイル  */
    public static final File ENEMY_DATA_OLD_FILE = new File("./config/KCRDB-enemyid.csv");

    /** 艦パラメータファイル  */
    public static final File SHIP_PARAMETER_FILE = new File("./config/ShipParameterRecord.csv");

    /** スクリプトデータファイル  */
    public static final File SCRIPT_DATA_FILE = new File("./config/scriptdata.zip");

    /** 出撃ログ表示用CSSファイル */
    public static final File BATTLE_LOG_CSS_FILE = new File("./config/battle-log.css");

    /** テンプレート */
    public static final File BATTLE_LOG_CSS_TMPL_FILE = new File("./templates/battle-log.css");

    /** 多重起動検出用 */
    public static final File LOCK_FILE = new File("./config/lock");

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

    /** 保有資材:ネジ */
    public static final int MATERIAL_SCREW = 8;

    public static final int USEITEM_UNKNOWN = -1;
    public static final int USEITEM_BUCKET = 1;
    public static final int USEITEM_BURNER = 2;
    public static final int USEITEM_RESEARCH = 3;
    public static final int USEITEM_SCREW = 4;

    /** /resources/icon/add.png */
    public static final String R_ICON_ADD = "/resources/icon/add.png";

    /** /resources/icon/delete.png */
    public static final String R_ICON_DELETE = "/resources/icon/delete.png";

    /** /resources/icon/error.png */
    public static final String R_ICON_ERROR = "/resources/icon/error.png";

    /** /resources/icon/error_mono.png */
    public static final String R_ICON_ERROR_MONO = "/resources/icon/error_mono.png";

    /** /resources/icon/exclamation.png */
    public static final String R_ICON_EXCLAMATION = "/resources/icon/exclamation.png";

    /** /resources/icon/exclamation_mono.png */
    public static final String R_ICON_EXCLAMATION_MONO = "/resources/icon/exclamation_mono.png";

    /** /resources/icon/folder.png */
    public static final String R_ICON_FOLDER = "/resources/icon/folder.png";

    /** /resources/icon/star.png */
    public static final String R_ICON_STAR = "/resources/icon/star.png";

    /** /resources/icon/heart.png */
    public static final String R_ICON_LOCKED = "/resources/icon/heart.png";

    /** /resources/icon/arrow-left.png */
    public static final String R_ICON_LEFT = "/resources/icon/arrow-left.png";

    /** /resources/icon/arrow-right.png */
    public static final String R_ICON_RIGHT = "/resources/icon/arrow-right.png";

    /** 航海日誌のロゴ */
    public static final String LOGO = "/resources/logo.png";

    /** Twitterのロゴ */
    public static final String TWITTER = "/resources/twitter.png";

    /** 艦隊タブの艦娘ラベルに設定するツールチップテキスト */
    public static final String TOOLTIP_FLEETTAB_SHIP = "HP:{0}/{1} 燃料:{2}/{3} 弾:{4}/{5}\nNext:{6}exp";

    /** メッセージ 出撃できます。 */
    public static final String MESSAGE_GOOD = "出撃できます。";

    /** メッセージ 進撃できます。 */
    public static final String MESSAGE_GO_NEXT = "進撃できます。";

    /** メッセージ {0} 出撃はできません。 */
    public static final String MESSAGE_BAD = "{0} 出撃はできません。";

    /** メッセージ 出撃中です。  */
    public static final String MESSAGE_SORTIE = "出撃中です。";

    /** メッセージ 連合艦隊に */
    public static final String MESSAGE_IN_COMBINED = "連合艦隊に";

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

    /** メッセージ 連合艦隊 */
    public static final String MESSAGE_COMBINED = "連合艦隊編成中。";

    /** メッセージ 制空値:{0} */
    public static final String MESSAGE_SEIKU = "制空値:{0}。";

    /** メッセージ 索敵:{0}+{1} */
    public static final String MESSAGE_SAKUTEKI = "索敵:{0}。";

    /** メッセージ  艦隊合計Lv:{0} */
    public static final String MESSAGE_TOTAL_LV = "艦隊合計Lv:{0}。";

    /** Push 通知のアプリケーション名*/
    public static final String PUSH_NOTIFY_APPNAME = "航海日誌";

    /** Prowl のアクセス先 URI */
    public static final String PUSH_NOTIFY_PROWL_URI = "https://api.prowlapp.com/publicapi/add";

    /** NMA のアクセス先 URI */
    public static final String PUSH_NOTIFY_NMA_URI = "https://www.notifymyandroid.com/publicapi/notify";

    /** im.kayac.com のアクセス先 URI */
    public static final String PUSH_NOTIFY_IMKAYAC_URI = "http://im.kayac.com/api/post/";

    /** メッセージ  ドラム缶:{0} ({1}隻) */
    public static final String MESSAGE_TOTAL_DRAM = "ドラム缶:{0} ({1}隻)。";

    /** メッセージ  大発:{0} (+{1}%) */
    public static final String MESSAGE_TOTAL_DAIHATSU = "大発:{0} (+{1}%)";

    /** メッセージ  前回の遠征:{0} (+{1}%) */
    public static final String MESSAGE_PREVIOUS_MISSION = "前回の遠征:{0}";

    /** タイトルバーに表示するデフォルトテキスト */
    public static final String TITLEBAR_TEXT = NAME + SUFFIX + " " + VERSION;

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

    /** お風呂に入りたい艦娘一覧の初期カラム順 */
    public static final Map<String, Integer> BATHTABLE_COLUMN_MAP = new HashMap<String, Integer>() {
        {
            this.put("No.", 0);
            this.put("ID", 1);
            this.put("艦隊", 2);
            this.put("疲労", 3);
            this.put("修理順", 4);
            this.put("名前", 5);
            this.put("Lv", 6);
            this.put("HP", 7);
            this.put("修理時間", 8);
            this.put("修理に必要な燃料", 9);
            this.put("修理に必要な鋼材", 10);
            this.put("損傷", 11);
            this.put("HP1あたり", 12);
        }
    };

    /** 艦種に関する表示情報 */
    public static final Map<Integer, String> SHIP_TYPE_INFO = new TreeMap<Integer, String>() {
        {
            this.put(1, "#"); // "#"は非表示
            this.put(8, "巡洋戦艦");
            this.put(12, "#");
            this.put(15, "#");
        }
    };

    public static final String[] SHIP_CATEGORY_NAMES = new String[] {
            "駆逐艦",
            "軽巡",
            "雷巡",
            "重巡",
            "航巡",
            "戦艦・航戦",
            "空母",
            "潜水艦",
            "その他"
    };

    public static final int[][] SHIP_CATEGORY_TYPES = new int[][] {
            new int[] { 2 }, // 駆逐艦
            new int[] { 3 }, // 軽巡洋艦
            new int[] { 4 }, // 重雷装巡洋艦
            new int[] { 5 }, // 重巡洋艦
            new int[] { 6 }, // 航空巡洋艦
            new int[] { 8, 9, 10 }, // 戦艦
            new int[] { 7, 11, 16, 18 }, // 空母
            new int[] { 13, 14 }, // 潜水艦
            new int[] { 1, 12, 15, 17, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30 }, // その他
    };

    /** ドロップ報告書用スクリプト */
    public static final File SCRIPT_DIR = new File("./script");

    public static final String TABLE_SCRIPT_PREFIX = "table";

    public static final String TABLE_STYLE_SUFFIX = "style";

    public static final String DROPTABLE_PREFIX = "drop";

    public static final String ITEMTABLE_PREFIX = "item";

    public static final String ITEMIDTABLE_PREFIX = "itemid";

    public static final String SHIPTABLE_PREFIX = "ship";

    public static final String MISSIONTABLE_PREFIX = "mission";

    public static final String SHIPGROUPTABLE_PREFIX = "shipgroup";

    public static final String CREATEITEMTABLE_PREFIX = "createitem";

    public static final String CREATESHIPTABLE_PREFIX = "createship";

    public static final String MISSIONRESULTTABLE_PREFIX = "missionresult";

    public static final String QUESTTABLE_PREFIX = "quest";

    public static final String RESOURCECHAR_PREFIX = "resource";

    /** ウィンドウ名 */
    public static final String[] WINDOW_NAME_LIST = new String[] {
            "キャプチャ",
            "ドロップ報告書",
            "建造報告書",
            "開発報告書",
            "遠征報告書",
            "遠征一覧",
            "装備一覧",
            "艦娘一覧1",
            "艦娘一覧2",
            "艦娘一覧3",
            "艦娘一覧4",
            "お風呂に入りたい艦娘",
            "任務一覧",
            "戦況",
            "戦況-横",
            "自軍敵軍パラメータ",
            "経験値計算機",
            "演習経験値計算機",
            "グループエディタ",
            "資材チャート",
            "出撃統計",
            "#1",
            "#2",
            "#3",
            "#4",
            "ツール"
    };

    public static final String[] SHORT_WINDOW_NAME_LIST = new String[] {
            "Cap",
            "ドロ",
            "建造",
            "開発",
            "遠征",
            "一覧",
            "装備",
            "艦1",
            "艦2",
            "艦3",
            "艦4",
            "風呂",
            "任務",
            "戦況",
            "戦横",
            "パラ",
            "経験",
            "演習",
            "グル",
            "資材",
            "統計",
            "#1",
            "#2",
            "#3",
            "#4",
            "ロー"
    };
}