package logbook.config.bean;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import logbook.constants.AppConstants;
import logbook.dto.ShipFilterDto;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.swt.graphics.RGB;

/**
 * アプリケーションの設定
 *
 */
public final class AppConfigBean {

    /** ポート番号 */
    private int listenPort = 8888;

    /** プロキシ利用 */
    private boolean useProxy;

    /** プロキシホスト */
    private String proxyHost = "localhost";

    /** プロキシポート */
    private int proxyPort = 8080;

    /** 最前面に表示 */
    private boolean onTop = true;

    /** 縮小表示 */
    private boolean minimumLayout;

    /** 音量 */
    private float soundLevel = 0.85f;

    /** 透明度 */
    //private int alpha = 255;

    /** 遠征のリマインド */
    private boolean missionRemind = true;

    /** リマインドのインターバル */
    private int remindInterbal = 120;

    /** 遠征入渠をバルーンで通知する */
    private boolean useBalloon = true;

    /** 報告書の保存先 */
    private String reportPath = new File("").getAbsolutePath();

    /** Prowl による Push 通知設定 */
    private boolean NotifyProwl = false;

    /** Prowl による Push 通知 APIKey */
    private String ProwlAPIKey = "";

    /** NMA による Push 通知設定 */
    private boolean NotifyNMA = false;

    /** NMA による Push 通知 APIKey */
    private String NMAAPIKey = "";

    /** ImKayac による Push 通知設定 */
    private boolean NotifyImKayac = false;

    /** ImKayac による Push 通知 UserName */
    private String ImKayacUserName = "";

    /** ImKayac による Push 通知 Password */
    private String ImKayacPasswd = "";

    /** ImKayac による Push 通知 Password */
    private String ImKayacPrivateKey = "";

    /** 遠征Push通知のPriority */
    private int PushPriorityMission = 0;

    /** 入渠Push通知のPriority */
    private int PushPriorityNdock = 0;

    /** 遠征帰投時にPush通知する */
    private boolean PushMission = true;

    /**　入渠完了時にPush通知する */
    private boolean PushNdock = true;

    /** 出撃ログの保存先 */
    private String battleLogPath = new File("battlelog").getAbsolutePath();

    /** 資源ログの書き込み間隔 */
    private int materialLogInterval = 600;

    /** 遠征帰還や開発・建造時に資材ログ出力する */
    private boolean materialLogDetail = true;

    /** アップデートチェック */
    private boolean updateCheck = true;

    /** 終了時に確認する */
    private boolean checkDoit = true;

    /** タイトルバーに提督名を表示する */
    private boolean nameOnTitlebar = false;

    /** タスクトレイに格納 */
    private boolean hideWindow;

    /** ローカルループバックアドレスからの接続のみ受け入れる */
    private boolean allowOnlyFromLocalhost = true;

    /** 戦闘結果をログ出力するか */
    private boolean printSortieLog = false;

    /** 轟沈をログ出力するか */
    private boolean printSunkLog = false;

    /** 更新系をログ出力するか */
    private boolean printUpdateLog = true;

    /** 遠征-1分前に通知する */
    private boolean noticeDeckmission = true;

    /** 入渠-1分前に通知する */
    private boolean noticeNdock = true;

    /** 画面キャプチャ-保存先 */
    private String capturePath = new File("").getAbsolutePath();

    /** 画面キャプチャ-フォーマット */
    private String imageFormat = "jpg";

    /** 画面キャプチャ-日付のフォルダを作成 */
    private boolean createDateFolder = true;

    /** 補給不足で警告アイコン */
    private boolean warnByNeedSupply = true;

    /** 疲労状態で警告アイコン */
    private boolean warnByCondState = true;

    /** 中破状態で警告アイコン */
    private boolean warnByHalfDamage = true;

    /** 大破状態で致命的アイコン */
    private boolean fatalByBadlyDamage = true;

    /** 大破状態で大破でバルーンツールチップ */
    private boolean balloonBybadlyDamage = true;

    /** 遠征からの帰還時に母港タブを表示 */
    private boolean visibleOnReturnMission = true;

    /** お風呂から上がる時に母港タブを表示 */
    private boolean visibleOnReturnBathwater = true;

    /** 回数を表示 */
    private boolean displayCount;

    /** デフォルト海域 */
    private String defaultSea = "3-2";

    /** デフォルト評価 */
    private String defaultEvaluate = "S勝利";

    /** 索敵表示形式 */
    private int sakutekiMethod = 0;

    /** タスクバー通知を使用する */
    private boolean useTaskbarNotify = true;

    /** 母港の空きがこれ以下で警告表示に変える */
    private int notifyFully = 1;

    /** 母港の空きがこれ以下でバルーン通知 */
    private int shipFullBalloonNotify = 5;

    /** 母港の空きがこれ以下でバルーン通知 */
    private boolean enableShipFullBalloonNotify = false;

    /** 装備の空きがこれ以下でバルーン通知 */
    private int itemFullBalloonNotify = 20;

    /** 装備の空きがこれ以下でバルーン通知 */
    private boolean enableItemFullBalloonNotify = false;

    /** 燃料の色 */
    private RGB fuelColor = cloneRGB(AppConstants.CHART_COLOR_TABLE[0]);

    /** 弾薬の色 */
    private RGB ammoColor = cloneRGB(AppConstants.CHART_COLOR_TABLE[1]);

    /** 鋼材の色 */
    private RGB metalColor = cloneRGB(AppConstants.CHART_COLOR_TABLE[2]);

    /** ボーキの色 */
    private RGB bauxiteColor = cloneRGB(AppConstants.CHART_COLOR_TABLE[3]);

    /** バーナーの色 */
    private RGB burnerColor = cloneRGB(AppConstants.CHART_COLOR_TABLE[4]);

    /** バケツの色 */
    private RGB bucketColor = cloneRGB(AppConstants.CHART_COLOR_TABLE[5]);

    /** 開発の色 */
    private RGB researchColor = cloneRGB(AppConstants.CHART_COLOR_TABLE[6]);

    /** ネジの色 */
    private RGB screwColor = cloneRGB(AppConstants.CHART_COLOR_TABLE[7]);

    /** 開発者オプション-JSONを保存する */
    private boolean storeJson;

    /** 開発者オプション-JSONの保存先 */
    private String storeJsonPath = FilenameUtils.concat(new File("").getAbsolutePath(), "json");

    /** テーブル列を表示する設定(キー:java.lang.Class.getName()) */
    private Map<String, TableConfigBean> tableConfigMap = new HashMap<String, TableConfigBean>();

    /** 艦これ統計データベースへのデータ送信 */
    private boolean sendDatabase = false;

    /** 艦これ統計データベースへ送信するときのアクセスキー */
    private String accessKey = "";

    /** 艦これ統計データベースへのデータ送信をログ出力するか？ */
    private boolean databaseSendLog = false;

    /** ウィンドウの表示状態 */
    private Map<String, WindowConfigBean> windowConfigMap = new HashMap<String, WindowConfigBean>();

    /** マウスが離れてから元の透明度に戻るまでの時間（0.1秒単位） */
    private int opaqueInterval = 6;

    /** タイトルバー以外でもドラッグ&ドロップで移動できるようにする */
    private boolean enableMoveWithDD = true;

    /** 艦娘一覧ウィンドウの名前 */
    private String[] shipTableNames = new String[] {
            "所有艦娘一覧 1",
            "所有艦娘一覧 2",
            "所有艦娘一覧 3",
            "所有艦娘一覧 4"
    };

    /** 艦娘ウィンドウのフィルタ */
    private ShipFilterDto[] shipFilters = new ShipFilterDto[] {
            null, null, null, null
    };

    /** 保存したJSONを読み込ませてテストするためのウィンドウメニューを表示するかどうか */
    private boolean enableTestWindow = false;

    /** 縮小表示の時は他のウィンドウを閉じる */
    private boolean closeWhenMinimized = true;

    /** キャプチャ範囲 [x,y,width,height] */
    private int[] captureRect = null;

    /** 開発報告書を読み込むか */
    private boolean loadCreateItemLog = true;

    /** 建造報告書を読みこむか */
    private boolean loadCreateShipLog = true;

    /** 遠征報告書を読みこむか */
    private boolean loadMissionLog = true;

    /** システムワイドホットキー (Windowsのみ対応) 0:なし, 1:Ctrl+Shift+z, 2:Win+Z */
    private int systemWideHotKey = 0;

    /** TwitterのAccessToken */
    private String twitterToken;
    private String twitterTokenSecret;

    private static RGB cloneRGB(RGB rgb) {
        return new RGB(rgb.red, rgb.green, rgb.blue);
    }

    /**
     * ポート番号を取得します。
     * @return ポート番号
     */
    public int getListenPort() {
        return this.listenPort;
    }

    /**
     * ポート番号を設定します。
     * @param listenPort ポート番号
     */
    public void setListenPort(int listenPort) {
        this.listenPort = listenPort;
    }

    /**
     * プロキシ利用を取得します。
     * @return プロキシ利用
     */
    public boolean isUseProxy() {
        return this.useProxy;
    }

    /**
     * プロキシ利用を設定します。
     * @param useProxy プロキシ利用
     */
    public void setUseProxy(boolean useProxy) {
        this.useProxy = useProxy;
    }

    /**
     * プロキシホストを取得します。
     * @return プロキシホスト
     */
    public String getProxyHost() {
        return this.proxyHost;
    }

    /**
     * プロキシホストを設定します。
     * @param proxyHost プロキシホスト
     */
    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    /**
     * プロキシポートを取得します。
     * @return プロキシポート
     */
    public int getProxyPort() {
        return this.proxyPort;
    }

    /**
     * プロキシポートを設定します。
     * @param proxyPort プロキシポート
     */
    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    /**
     * 最前面に表示を取得します。
     * @return 最前面に表示
     */
    public boolean isOnTop() {
        return this.onTop;
    }

    /**
     * 最前面に表示を設定します。
     * @param onTop 最前面に表示
     */
    public void setOnTop(boolean onTop) {
        this.onTop = onTop;
    }

    /**
     * 縮小表示を取得します。
     * @return 縮小表示
     */
    public boolean isMinimumLayout() {
        return this.minimumLayout;
    }

    /**
     * 縮小表示を設定します。
     * @param minimumLayout 縮小表示
     */
    public void setMinimumLayout(boolean minimumLayout) {
        this.minimumLayout = minimumLayout;
    }

    /**
     * 音量を取得します。
     * @return 音量
     */
    public float getSoundLevel() {
        return this.soundLevel;
    }

    /**
     * 音量を設定します。
     * @param soundLevel 音量
     */
    public void setSoundLevel(float soundLevel) {
        this.soundLevel = soundLevel;
    }

    /**
     * 透明度を取得します。
     * @return 透明度
     */
    //public int getAlpha() {
    //    return this.alpha;
    //}

    /**
     * 透明度を設定します。
     * @param alpha 透明度
     */
    //public void setAlpha(int alpha) {
    //    this.alpha = alpha;
    //}

    /**
     * 遠征のリマインドを取得します。
     * @return 遠征のリマインド
     */
    public boolean isMissionRemind() {
        return this.missionRemind;
    }

    /**
     * 遠征のリマインドを設定します。
     * @param missionRemind 遠征のリマインド
     */
    public void setMissionRemind(boolean missionRemind) {
        this.missionRemind = missionRemind;
    }

    /**
     * リマインドのインターバルを取得します。
     * @return リマインドのインターバル
     */
    public int getRemindInterbal() {
        return this.remindInterbal;
    }

    /**
     * リマインドのインターバルを設定します。
     * @param remindInterbal リマインドのインターバル
     */
    public void setRemindInterbal(int remindInterbal) {
        this.remindInterbal = remindInterbal;
    }

    /**
     * 遠征入渠をバルーンで通知するを取得します。
     * @return 遠征入渠をバルーンで通知する
     */
    public boolean isUseBalloon() {
        return this.useBalloon;
    }

    /**
     * 遠征入渠をバルーンで通知するを設定します。
     * @param useBalloon 遠征入渠をバルーンで通知する
     */
    public void setUseBalloon(boolean useBalloon) {
        this.useBalloon = useBalloon;
    }

    /**
     * 報告書の保存先を取得します。
     * @return 報告書の保存先
     */
    public String getReportPath() {
        return this.reportPath;
    }

    /** Prowl による Push 通知の設定を取得します
     * @return Prowl による Push 通知の設定
     */
    public boolean getNotifyProwl() {
        return this.NotifyProwl;
    }

    /** Prowl による Push 通知の API Key を取得します
     * @return API Key
     */
    public String getProwlAPIKey() {
        return this.ProwlAPIKey;
    }

    /** Prowl による Push 通知を設定します
     * @param Prowl による Push 通知
     */
    public void setNotifyProwl(boolean prowlflg) {
        this.NotifyProwl = prowlflg;
    }

    /** Prowl による Push 通知の API Key を設定します
     * @param API Key
     */
    public void setProwlAPIKey(String apikey) {
        this.ProwlAPIKey = apikey;
    }

    /** NMA による Push 通知の設定を取得します
     * @return NMA による Push 通知の設定
     */
    public boolean getNotifyNMA() {
        return this.NotifyNMA;
    }

    /** NMA による Push 通知の API Key を取得します
     * @return API Key
     */
    public String getNMAAPIKey() {
        return this.NMAAPIKey;
    }

    /** NMA による Push 通知を設定します
     * @param NMA による Push 通知
     */
    public void setNotifyNMA(boolean nmaflg) {
        this.NotifyNMA = nmaflg;
    }

    /** NMA による Push 通知の API Key を設定します
     * @param API Key
     */
    public void setNMAAPIKey(String apikey) {
        this.NMAAPIKey = apikey;
    }

    /** ImKayac による Push 通知の設定を取得します
     * @return ImKayac による Push 通知の設定
     */
    public boolean getNotifyImKayac() {
        return this.NotifyImKayac;
    }

    /** ImKayac による Push 通知の UserName を取得します
     * @return UserName
     */
    public String getImKayacUserName() {
        return this.ImKayacUserName;
    }

    /** ImKayac による Push 通知の Password を取得します
     * @return Password
     */
    public String getImKayacPasswd() {
        return this.ImKayacPasswd;
    }

    /** ImKayac による Push 通知の PrivateKey を取得します
     * @return PrivateKey
     */
    public String getImKayacPrivateKey() {
        return this.ImKayacPrivateKey;
    }

    /** ImKayac による Push 通知を設定します
     * @param ImKayac による Push 通知
     */
    public void setNotifyImKayac(boolean imkayacflg) {
        this.NotifyImKayac = imkayacflg;
    }

    /** ImKayac による Push 通知の UserName を設定します
     * @param UserName
     */
    public void setImKayacUserName(String username) {
        this.ImKayacUserName = username;
    }

    /** ImKayac による Push 通知の Password を設定します
     * @param Password
     */
    public void setImKayacPasswd(String passwd) {
        this.ImKayacPasswd = passwd;
    }

    /** ImKayac による Push 通知の PrivateKey を設定します
     * @param privatekey
     */
    public void setImKayacPrivateKey(String privatekey) {
        this.ImKayacPrivateKey = privatekey;
    }

    /** 遠征帰投時にPush通知する を取得します
     * @return PushMission
     */
    public boolean getPushMission() {
        return this.PushMission;
    }

    /** 入渠完了時にPush通知する を取得します
     * @return PushNdock
     */
    public boolean getPushNdock() {
        return this.PushNdock;
    }

    /** 遠征帰投時にPush通知する を設定します
     * @param PushMission
     */
    public void setPushMission(boolean pushmission) {
        this.PushMission = pushmission;
    }

    /** 入渠完了時にPush通知する を設定します
     * @param PushNdock
     */
    public void setPushNdock(boolean pushndock) {
        this.PushNdock = pushndock;
    }

    /** 遠征Push通知の Priorityを取得します
     * @return priority
     */
    public int getPushPriorityMission() {
        return this.PushPriorityMission;
    }

    /** 入渠Push通知の Priorityを取得します
     * @return priority
     */
    public int getPushPriorityNdock() {
        return this.PushPriorityNdock;
    }

    /** 遠征Push通知のPriorityを設定します
     * @param priority
     */
    public void setPushPriorityMission(int priority) {
        this.PushPriorityMission = priority;
    }

    /** 入渠Push通知のPriorityを設定します
     * @param priority
     */
    public void setPushPriorityNdock(int priority) {
        this.PushPriorityNdock = priority;
    }

    /**
     * 報告書の保存先を設定します。
     * @param reportPath 報告書の保存先
     */
    public void setReportPath(String reportPath) {
        this.reportPath = reportPath;
    }

    /**
     * @return battleLogPath
     */
    public String getBattleLogPath() {
        return this.battleLogPath;
    }

    /**
     * @param battleLogPath セットする battleLogPath
     */
    public void setBattleLogPath(String battleLogPath) {
        this.battleLogPath = battleLogPath;
    }

    /**
     * 資源ログの書き込み間隔を取得します。
     * @return 資源ログの書き込み間隔
     */
    public int getMaterialLogInterval() {
        return this.materialLogInterval;
    }

    /**
     * 資源ログの書き込み間隔を設定します。
     * @param materialLogInterval 資源ログの書き込み間隔
     */
    public void setMaterialLogInterval(int materialLogInterval) {
        this.materialLogInterval = materialLogInterval;
    }

    /**
     * @return materialLogDetail
     */
    public boolean isMaterialLogDetail() {
        return this.materialLogDetail;
    }

    /**
     * @param materialLogDetail セットする materialLogDetail
     */
    public void setMaterialLogDetail(boolean materialLogDetail) {
        this.materialLogDetail = materialLogDetail;
    }

    /**
     * アップデートチェックを取得します。
     * @return アップデートチェック
     */
    public boolean isUpdateCheck() {
        return this.updateCheck;
    }

    /**
     * アップデートチェックを設定します。
     * @param checkUpdate アップデートチェック
     */
    public void setUpdateCheck(boolean checkUpdate) {
        this.updateCheck = checkUpdate;
    }

    /**
     * 終了時に確認するを取得します。
     * @return 終了時に確認する
     */
    public boolean isCheckDoit() {
        return this.checkDoit;
    }

    /**
     * 終了時に確認するを設定します。
     * @param checkDoit 終了時に確認する
     */
    public void setCheckDoit(boolean checkDoit) {
        this.checkDoit = checkDoit;
    }

    /**
     *タイトルバーに提督名を表示するを取得します。
     * @return タイトルバーに提督名を表示する
     */
    public boolean isNameOnTitlebar() {
        return this.nameOnTitlebar;
    }

    /**
     * タイトルバーに提督名を表示するを設定します。
     * @param nameOnTitlebar タイトルバーに提督名を表示する
     */
    public void setNameOnTitlebar(boolean nameOnTitlebar) {
        this.nameOnTitlebar = nameOnTitlebar;
    }

    /**
     * タスクトレイに格納を取得します。
     * @return タスクトレイに格納
     */
    public boolean isHideWindow() {
        return this.hideWindow;
    }

    /**
     * タスクトレイに格納を設定します。
     * @param hideWindow タスクトレイに格納
     */
    public void setHideWindow(boolean hideWindow) {
        this.hideWindow = hideWindow;
    }

    /**
     * ローカルループバックアドレスからの接続のみ受け入れるを取得します。
     * @return ローカルループバックアドレスからの接続のみ受け入れる
     */
    public boolean isAllowOnlyFromLocalhost() {
        return this.allowOnlyFromLocalhost;
    }

    /**
     * ローカルループバックアドレスからの接続のみ受け入れるを設定します。
     * @param allowOnlyFromLocalhost ローカルループバックアドレスからの接続のみ受け入れる
     */
    public void setAllowOnlyFromLocalhost(boolean allowOnlyFromLocalhost) {
        this.allowOnlyFromLocalhost = allowOnlyFromLocalhost;
    }

    /**
     * @return printSortieLog
     */
    public boolean isPrintSortieLog() {
        return this.printSortieLog;
    }

    /**
     * @param printSortieLog セットする printSortieLog
     */
    public void setPrintSortieLog(boolean printSortieLog) {
        this.printSortieLog = printSortieLog;
    }

    /**
     * @return printSunkLog
     */
    public boolean isPrintSunkLog() {
        return this.printSunkLog;
    }

    /**
     * @param printSunkLog セットする printSunkLog
     */
    public void setPrintSunkLog(boolean printSunkLog) {
        this.printSunkLog = printSunkLog;
    }

    /**
     * @return printUpdateLog
     */
    public boolean isPrintUpdateLog() {
        return this.printUpdateLog;
    }

    /**
     * @param printUpdateLog セットする printUpdateLog
     */
    public void setPrintUpdateLog(boolean printUpdateLog) {
        this.printUpdateLog = printUpdateLog;
    }

    /**
     * 遠征-1分前に通知するを取得します。
     * @return 遠征-1分前に通知する
     */
    public boolean isNoticeDeckmission() {
        return this.noticeDeckmission;
    }

    /**
     * 遠征-1分前に通知するを設定します。
     * @param noticeDeckmission 遠征-1分前に通知する
     */
    public void setNoticeDeckmission(boolean noticeDeckmission) {
        this.noticeDeckmission = noticeDeckmission;
    }

    /**
     * 入渠-1分前に通知するを取得します。
     * @return 入渠-1分前に通知する
     */
    public boolean isNoticeNdock() {
        return this.noticeNdock;
    }

    /**
     * 入渠-1分前に通知するを設定します。
     * @param noticeNdock 入渠-1分前に通知する
     */
    public void setNoticeNdock(boolean noticeNdock) {
        this.noticeNdock = noticeNdock;
    }

    /**
     * 画面キャプチャ-保存先を取得します。
     * @return 画面キャプチャ-保存先
     */
    public String getCapturePath() {
        return this.capturePath;
    }

    /**
     * 画面キャプチャ-保存先を設定します。
     * @param capturePath 画面キャプチャ-保存先
     */
    public void setCapturePath(String capturePath) {
        this.capturePath = capturePath;
    }

    /**
     * 画面キャプチャ-フォーマットを取得します。
     * @return 画面キャプチャ-フォーマット
     */
    public String getImageFormat() {
        return this.imageFormat;
    }

    /**
     * 画面キャプチャ-フォーマットを設定します。
     * @param imageFormat 画面キャプチャ-フォーマット
     */
    public void setImageFormat(String imageFormat) {
        this.imageFormat = imageFormat;
    }

    /**
     * 画面キャプチャ-日付のフォルダを作成を取得します。
     * @return 画面キャプチャ-日付のフォルダを作成
     */
    public boolean isCreateDateFolder() {
        return this.createDateFolder;
    }

    /**
     * 画面キャプチャ-日付のフォルダを作成を設定します。
     * @param createDateFolder 画面キャプチャ-日付のフォルダを作成
     */
    public void setCreateDateFolder(boolean createDateFolder) {
        this.createDateFolder = createDateFolder;
    }

    /**
     * 補給不足で警告アイコンを取得します。
     * @return 補給不足で警告アイコン
     */
    public boolean isWarnByNeedSupply() {
        return this.warnByNeedSupply;
    }

    /**
     * 補給不足で警告アイコンを設定します。
     * @param warnByNeedSupply 補給不足で警告アイコン
     */
    public void setWarnByNeedSupply(boolean warnByNeedSupply) {
        this.warnByNeedSupply = warnByNeedSupply;
    }

    /**
     * 疲労状態で警告アイコンを取得します。
     * @return 疲労状態で警告アイコン
     */
    public boolean isWarnByCondState() {
        return this.warnByCondState;
    }

    /**
     * 疲労状態で警告アイコンを設定します。
     * @param warnByCondState 疲労状態で警告アイコン
     */
    public void setWarnByCondState(boolean warnByCondState) {
        this.warnByCondState = warnByCondState;
    }

    /**
     * 大破状態で致命的アイコンを取得します。
     * @return 大破状態で致命的アイコン
     */
    public boolean isFatalBybadlyDamage() {
        return this.fatalByBadlyDamage;
    }

    /**
     * 大破状態で致命的アイコンを設定します。
     * @param fatalBybadlyDamage 大破状態で致命的アイコン
     */
    public void setFatalBybadlyDamage(boolean fatalBybadlyDamage) {
        this.fatalByBadlyDamage = fatalBybadlyDamage;
    }

    /**
     * 中破状態で警告アイコンを取得します。
     * @return 中破状態で警告アイコン
     */
    public boolean isWarnByHalfDamage() {
        return this.warnByHalfDamage;
    }

    /**
     * 中破状態で警告アイコンを設定します。
     * @param warnByHalfDamage 中破状態で警告アイコン
     */
    public void setWarnByHalfDamage(boolean warnByHalfDamage) {
        this.warnByHalfDamage = warnByHalfDamage;
    }

    /**
     * 大破状態で致命的アイコンを取得します。
     * @return 大破状態で致命的アイコン
     */
    public boolean isFatalByBadlyDamage() {
        return this.fatalByBadlyDamage;
    }

    /**
     * 大破状態で致命的アイコンを設定します。
     * @param fatalByBadlyDamage 大破状態で致命的アイコン
     */
    public void setFatalByBadlyDamage(boolean fatalByBadlyDamage) {
        this.fatalByBadlyDamage = fatalByBadlyDamage;
    }

    /**
     * 大破状態で大破でバルーンツールチップを取得します。
     * @return 大破状態で大破でバルーンツールチップ
     */
    public boolean isBalloonBybadlyDamage() {
        return this.balloonBybadlyDamage;
    }

    /**
     * 大破状態で大破でバルーンツールチップを設定します。
     * @param balloonBybadlyDamage 大破状態で大破でバルーンツールチップ
     */
    public void setBalloonBybadlyDamage(boolean balloonBybadlyDamage) {
        this.balloonBybadlyDamage = balloonBybadlyDamage;
    }

    /**
     * 遠征からの帰還時に母港タブを表示を取得します。
     * @return 遠征からの帰還時に母港タブを表示
     */
    public boolean isVisibleOnReturnMission() {
        return this.visibleOnReturnMission;
    }

    /**
     * 遠征からの帰還時に母港タブを表示を設定します。
     * @param visibleOnReturnMission 遠征からの帰還時に母港タブを表示
     */
    public void setVisibleOnReturnMission(boolean visibleOnReturnMission) {
        this.visibleOnReturnMission = visibleOnReturnMission;
    }

    /**
     * お風呂から上がる時に母港タブを表示を取得します。
     * @return お風呂から上がる時に母港タブを表示
     */
    public boolean isVisibleOnReturnBathwater() {
        return this.visibleOnReturnBathwater;
    }

    /**
     * お風呂から上がる時に母港タブを表示を設定します。
     * @param visibleOnReturnBathwater お風呂から上がる時に母港タブを表示
     */
    public void setVisibleOnReturnBathwater(boolean visibleOnReturnBathwater) {
        this.visibleOnReturnBathwater = visibleOnReturnBathwater;
    }

    /**
     * 回数を表示を取得します。
     * @return 回数を表示
     */
    public boolean isDisplayCount() {
        return this.displayCount;
    }

    /**
     * 回数を表示を設定します。
     * @param displayCount 回数を表示
     */
    public void setDisplayCount(boolean displayCount) {
        this.displayCount = displayCount;
    }

    /**
     * デフォルト海域を取得します。
     * @return デフォルト海域
     */
    public String getDefaultSea() {
        return this.defaultSea;
    }

    /**
     * デフォルト海域を設定します。
     * @param defaultSea デフォルト海域
     */
    public void setDefaultSea(String defaultSea) {
        this.defaultSea = defaultSea;
    }

    /**
     * デフォルト評価を取得します。
     * @return デフォルト評価
     */
    public String getDefaultEvaluate() {
        return this.defaultEvaluate;
    }

    /**
     * デフォルト評価を設定します。
     * @param defaultEvaluate デフォルト評価
     */
    public void setDefaultEvaluate(String defaultEvaluate) {
        this.defaultEvaluate = defaultEvaluate;
    }

    /**
     * 索敵値の表示形式を取得します。
     * @return 索敵値の表示形式
     */
    public int getSakutekiMethod() {
        return this.sakutekiMethod;
    }

    /**
     * 索敵値の表示形式を設定します。
     * @param sakutekiMethod 索敵値の表示形式
     */
    public void setSakutekiMethod(int sakutekiMethod) {
        this.sakutekiMethod = sakutekiMethod;
    }

    /**
     * タスクバー通知を使用するを取得します。
     * @return タスクバー通知を使用する
     */
    public boolean isUseTaskbarNotify() {
        return this.useTaskbarNotify;
    }

    /**
     * タスクバー通知を使用するを設定します。
     * @param useTaskbarNotify タスクバー通知を使用する
     */
    public void setUseTaskbarNotify(boolean useTaskbarNotify) {
        this.useTaskbarNotify = useTaskbarNotify;
    }

    /**
     * 母港の空きがこれ以下で警告表示に変えるを取得します。
     * @return 母港の空きがこれ以下で警告表示に変える
     */
    public int getNotifyFully() {
        return this.notifyFully;
    }

    /**
     * 母港の空きがこれ以下で警告表示に変えるを設定します。
     * @param notifyFully 母港の空きがこれ以下で警告表示に変える
     */
    public void setNotifyFully(int notifyFully) {
        this.notifyFully = notifyFully;
    }

    /**
     * 母港の空きがこれ以下でバルーン通知を取得します。
     * @return 母港の空きがこれ以下でバルーン通知
     */
    public int getShipFullBalloonNotify() {
        return this.shipFullBalloonNotify;
    }

    /**
     * 母港の空きがこれ以下でバルーン通知を設定します。
     * @param shipFullBalloonNotify 母港の空きがこれ以下でバルーン通知
     */
    public void setShipFullBalloonNotify(int shipFullBalloonNotify) {
        this.shipFullBalloonNotify = shipFullBalloonNotify;
    }

    /**
     * 母港の空きがこれ以下でバルーン通知を取得します。
     * @return 装備の空きがこれ以下でバルーン通知
     */
    public boolean isEnableShipFullBalloonNotify() {
        return this.enableShipFullBalloonNotify;
    }

    /**
     * 母港の空きがこれ以下でバルーン通知を設定します。
     * @param itemFullBalloonNotify 装備の空きがこれ以下でバルーン通知
     */
    public void setEnableShipFullBalloonNotify(boolean enableShipFullBalloonNotify) {
        this.enableShipFullBalloonNotify = enableShipFullBalloonNotify;
    }

    /**
     * 装備の空きがこれ以下でバルーン通知を取得します。
     * @return 装備の空きがこれ以下でバルーン通知
     */
    public int getItemFullBalloonNotify() {
        return this.itemFullBalloonNotify;
    }

    /**
     * 装備の空きがこれ以下でバルーン通知を設定します。
     * @param itemFullBalloonNotify 装備の空きがこれ以下でバルーン通知
     */
    public void setItemFullBalloonNotify(int itemFullBalloonNotify) {
        this.itemFullBalloonNotify = itemFullBalloonNotify;
    }

    /**
     * 装備の空きがこれ以下でバルーン通知を取得します。
     * @return 装備の空きがこれ以下でバルーン通知
     */
    public boolean isEnableItemFullBalloonNotify() {
        return this.enableItemFullBalloonNotify;
    }

    /**
     * 装備の空きがこれ以下でバルーン通知を設定します。
     * @param itemFullBalloonNotify 装備の空きがこれ以下でバルーン通知
     */
    public void setEnableItemFullBalloonNotify(boolean enableItemFullBalloonNotify) {
        this.enableItemFullBalloonNotify = enableItemFullBalloonNotify;
    }

    /**
     * 燃料の色を取得します。
     * @return 燃料の色
     */
    public RGB getFuelColor() {
        return this.fuelColor;
    }

    /**
     * 燃料の色を設定します。
     * @param fuelColor 燃料の色
     */
    public void setFuelColor(RGB fuelColor) {
        this.fuelColor = fuelColor;
    }

    /**
     * 弾薬の色を取得します。
     * @return 弾薬の色
     */
    public RGB getAmmoColor() {
        return this.ammoColor;
    }

    /**
     * 弾薬の色を設定します。
     * @param ammoColor 弾薬の色
     */
    public void setAmmoColor(RGB ammoColor) {
        this.ammoColor = ammoColor;
    }

    /**
     * 鋼材の色を取得します。
     * @return 鋼材の色
     */
    public RGB getMetalColor() {
        return this.metalColor;
    }

    /**
     * 鋼材の色を設定します。
     * @param metalColor 鋼材の色
     */
    public void setMetalColor(RGB metalColor) {
        this.metalColor = metalColor;
    }

    /**
     * ボーキの色を取得します。
     * @return ボーキの色
     */
    public RGB getBauxiteColor() {
        return this.bauxiteColor;
    }

    /**
     * ボーキの色を設定します。
     * @param bauxiteColor ボーキの色
     */
    public void setBauxiteColor(RGB bauxiteColor) {
        this.bauxiteColor = bauxiteColor;
    }

    /**
     * @return burnerColor
     */
    public RGB getBurnerColor() {
        return this.burnerColor;
    }

    /**
     * @param burnerColor セットする burnerColor
     */
    public void setBurnerColor(RGB burnerColor) {
        this.burnerColor = burnerColor;
    }

    /**
     * @return bucketColor
     */
    public RGB getBucketColor() {
        return this.bucketColor;
    }

    /**
     * @param bucketColor セットする bucketColor
     */
    public void setBucketColor(RGB bucketColor) {
        this.bucketColor = bucketColor;
    }

    /**
     * @return researchColor
     */
    public RGB getResearchColor() {
        return this.researchColor;
    }

    /**
     * @param researchColor セットする researchColor
     */
    public void setResearchColor(RGB researchColor) {
        this.researchColor = researchColor;
    }

    /**
     * @return screwColor
     */
    public RGB getScrewColor() {
        return this.screwColor;
    }

    /**
     * @param screwColor セットする screwColor
     */
    public void setScrewColor(RGB screwColor) {
        this.screwColor = screwColor;
    }

    /**
     * 開発者オプション-JSONを保存するを取得します。
     * @return 開発者オプション-JSONを保存する
     */
    public boolean isStoreJson() {
        return this.storeJson;
    }

    /**
     * 開発者オプション-JSONを保存するを設定します。
     * @param storeJson 開発者オプション-JSONを保存する
     */
    public void setStoreJson(boolean storeJson) {
        this.storeJson = storeJson;
    }

    /**
     * 開発者オプション-JSONの保存先を取得します。
     * @return 開発者オプション-JSONの保存先
     */
    public String getStoreJsonPath() {
        return this.storeJsonPath;
    }

    /**
     * 開発者オプション-JSONの保存先を設定します。
     * @param storeJsonPath 開発者オプション-JSONの保存先
     */
    public void setStoreJsonPath(String storeJsonPath) {
        this.storeJsonPath = storeJsonPath;
    }

    /**
     * テーブル列を表示する設定(キー:java.lang.Class.getName())を取得します。
     * @return テーブル列を表示する設定(キー:java.lang.Class.getName())
     */
    public Map<String, TableConfigBean> getTableConfigMap() {
        return this.tableConfigMap;
    }

    /**
     * テーブル列を表示する設定(キー:java.lang.Class.getName())を設定します。
     * @param visibleColumnMap テーブル列を表示する設定(キー:java.lang.Class.getName())
     */
    public void setTableConfigMap(Map<String, TableConfigBean> tableConfigMap) {
        this.tableConfigMap = tableConfigMap;
    }

    /**
     * @return sendDatabase
     */
    public boolean isSendDatabase() {
        return this.sendDatabase;
    }

    /**
     * @param sendDatabase セットする sendDatabase
     */
    public void setSendDatabase(boolean sendDatabase) {
        this.sendDatabase = sendDatabase;
    }

    /**
     * @return accessKey
     */
    public String getAccessKey() {
        return this.accessKey;
    }

    /**
     * @param accessKey セットする accessKey
     */
    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    /**
     * @return windowConfigMap
     */
    public Map<String, WindowConfigBean> getWindowConfigMap() {
        return this.windowConfigMap;
    }

    /**
     * @param windowConfigMap セットする windowConfigMap
     */
    public void setWindowConfigMap(Map<String, WindowConfigBean> windowConfigMap) {
        this.windowConfigMap = windowConfigMap;
    }

    /**
     * @return opaqueInterval
     */
    public int getOpaqueInterval() {
        return this.opaqueInterval;
    }

    /**
     * @param opaqueInterval セットする opaqueInterval
     */
    public void setOpaqueInterval(int opaqueInterval) {
        this.opaqueInterval = opaqueInterval;
    }

    /**
     * @return enableMoveWithDD
     */
    public boolean isEnableMoveWithDD() {
        return this.enableMoveWithDD;
    }

    /**
     * @param enableMoveWithDD セットする enableMoveWithDD
     */
    public void setEnableMoveWithDD(boolean enableMoveWithDD) {
        this.enableMoveWithDD = enableMoveWithDD;
    }

    /**
     * @return shipTableNames
     */
    public String[] getShipTableNames() {
        return this.shipTableNames;
    }

    /**
     * @param shipTableNames セットする shipTableNames
     */
    public void setShipTableNames(String[] shipTableNames) {
        this.shipTableNames = shipTableNames;
    }

    /**
     * @return shipFilters
     */
    public ShipFilterDto[] getShipFilters() {
        return this.shipFilters;
    }

    /**
     * @param shipFilters セットする shipFilters
     */
    public void setShipFilters(ShipFilterDto[] shipFilters) {
        this.shipFilters = shipFilters;
    }

    /**
     * @return enableTestWindow
     */
    public boolean isEnableTestWindow() {
        return this.enableTestWindow;
    }

    /**
     * @param enableTestWindow セットする enableTestWindow
     */
    public void setEnableTestWindow(boolean enableTestWindow) {
        this.enableTestWindow = enableTestWindow;
    }

    /**
     * @return closeWhenMinimized
     */
    public boolean isCloseWhenMinimized() {
        return this.closeWhenMinimized;
    }

    /**
     * @param closeWhenMinimized セットする closeWhenMinimized
     */
    public void setCloseWhenMinimized(boolean closeWhenMinimized) {
        this.closeWhenMinimized = closeWhenMinimized;
    }

    /**
     * @return databaseSendLog
     */
    public boolean isDatabaseSendLog() {
        return this.databaseSendLog;
    }

    /**
     * @param databaseSendLog セットする databaseSendLog
     */
    public void setDatabaseSendLog(boolean databaseSendLog) {
        this.databaseSendLog = databaseSendLog;
    }

    /**
     * @return captureRect
     */
    public int[] getCaptureRect() {
        return this.captureRect;
    }

    /**
     * @param captureRect セットする captureRect
     */
    public void setCaptureRect(int[] captureRect) {
        this.captureRect = captureRect;
    }

    /**
     * @return loadCreateItemLog
     */
    public boolean isLoadCreateItemLog() {
        return this.loadCreateItemLog;
    }

    /**
     * @param loadCreateItemLog セットする loadCreateItemLog
     */
    public void setLoadCreateItemLog(boolean loadCreateItemLog) {
        this.loadCreateItemLog = loadCreateItemLog;
    }

    /**
     * @return loadCreateShipLog
     */
    public boolean isLoadCreateShipLog() {
        return this.loadCreateShipLog;
    }

    /**
     * @param loadCreateShipLog セットする loadCreateShipLog
     */
    public void setLoadCreateShipLog(boolean loadCreateShipLog) {
        this.loadCreateShipLog = loadCreateShipLog;
    }

    /**
     * @return loadMissionLog
     */
    public boolean isLoadMissionLog() {
        return this.loadMissionLog;
    }

    /**
     * @param loadMissionLog セットする loadMissionLog
     */
    public void setLoadMissionLog(boolean loadMissionLog) {
        this.loadMissionLog = loadMissionLog;
    }

    /**
     * @return systemWideHotKey
     */
    public int getSystemWideHotKey() {
        return this.systemWideHotKey;
    }

    /**
     * @param systemWideHotKey セットする systemWideHotKey
     */
    public void setSystemWideHotKey(int systemWideHotKey) {
        this.systemWideHotKey = systemWideHotKey;
    }

    /**
     * @return twitterToken
     */
    public String getTwitterToken() {
        return this.twitterToken;
    }

    /**
     * @param twitterToken セットする twitterToken
     */
    public void setTwitterToken(String twitterToken) {
        this.twitterToken = twitterToken;
    }

    /**
     * @return twitterTokenSecret
     */
    public String getTwitterTokenSecret() {
        return this.twitterTokenSecret;
    }

    /**
     * @param twitterTokenSecret セットする twitterTokenSecret
     */
    public void setTwitterTokenSecret(String twitterTokenSecret) {
        this.twitterTokenSecret = twitterTokenSecret;
    }
}
