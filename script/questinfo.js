/**
 * 任務進捗詳細 Ver.2.2.1
 * Author:Nishisonic,Nekopanda
 * LastUpdate:2019/02/21
 */

data_prefix = "QSE.Ver2."

/** 艦種 */
var SHIP_TYPE = {
    /** 海防艦 */
    DE: 1,
    /** 駆逐艦 */
    DD: 2,
    /** 軽巡洋艦 */
    CL: 3,
    /** 重雷装巡洋艦 */
    CLT: 4,
    /** 重巡洋艦 */
    CA: 5,
    /** 航空巡洋艦 */
    CVA: 6,
    /** 軽空母 */
    CVL: 7,
    /** 巡洋戦艦(高速戦艦) */
    BC: 8,
    /** 戦艦 */
    BB: 9,
    /** 航空戦艦 */
    CVB: 10,
    /** 正規空母 */
    CV: 11,
    /** 超弩級戦艦 */
    // BB:12,
    /** 潜水艦 */
    SS: 13,
    /** 潜水空母 */
    CVS: 14,
    /** 補給艦(敵) */
    E_AO: 15,
    /** 水上機母艦 */
    AV: 16,
    /**揚陸艦 */
    LHA: 17,
    /** 装甲空母 */
    ACV: 18,
    /** 工作艦 */
    AR: 19,
    /** 潜水母艦 */
    AS: 20,
    /** 練習巡洋艦 */
    TV: 21,
    /** 補給艦 */
    AO: 22,
}

/** 任務種別 */
var QUEST_TYPE = {
    /** デイリー */
    DAILY: 1,
    /** ウィークリー */
    WEEKLY: 2,
    /** マンスリー */
    MONTHLY: 3,
    /** 単発 */
    ONCE: 4,
    /** その他 */
    OTHERS: 5,
}

/** 任務状態 */
var QUEST_STATE = {
    /** 未受注 */
    NOT_ORDER: 1,
    /** 遂行中 */
    ACTIVE: 2,
    /**達成 */
    COMPLETE: 3,
}

/**任務進捗状況 */
var QUEST_PROGRESS_FLAG = {
    /** 空白(達成含) */
    NONE: 0,
    /** 50%以上 */
    HALF: 1,
    /** 80%以上 */
    EIGHTY: 2,
}

/** マス */
var EVENT_ID = {
    /** 初期位置 */
    INITIAL_POSITION: 0,
    /** 存在せず */
    NONE: 1,
    /** 資源 */
    MATERIAL: 2,
    /** 渦潮 */
    MAELSTROM: 3,
    /** 通常戦闘 */
    NORMAL_BATTLE: 4,
    /** ボス戦闘 */
    BOSS_BATTLE: 5,
    /** 気のせいだった */
    BATTLE_AVOIDED: 6,
    /** 航空戦or航空偵察 */
    AIR: 7,
    /** 船団護衛成功 */
    ESCORT_SUCCESS: 8,
    /** 揚陸地点 */
    LANDING_POINT: 9,
    /** 空襲戦 */
    AIR_RAID_BATTLE: 10,
}

/** 近代化改修 */
var POWERUP_FLAG = {
    /** 成功 */
    SUCCESS: 1,
    /** 失敗 */
    FAILURE: 0,
}

/** 遠征 */
var EXPEDITION = {
    /** 失敗 */
    FAILURE: 0,
    /** 成功 */
    SUCCESS: 1,
    /** 大成功 */
    GREAT_SUCCESS: 2,
}

/** リセット */
var RESET = {
    /** なし */
    NONE: 0,
    /** デイリー */
    DAILY: 1,
    /** ウィークリー */
    WEEKLY: 2,
    /** マンスリー */
    MONTHLY: 3,
    /** クォータリー */
    QUARTRELY: 4,
    /** カウント=最大値でないならデイリー */
    NOT_SATISFY_DAILY: -1,
    /** カウント=最大値でないならウィークリー */
    NOT_SATISFY_WEEKLY: -2,
    /** カウント=最大値でないならマンスリー */
    NOT_SATISFY_MONTHLY: -3,
    /** カウント=最大値でないならクォータリー */
    NOT_SATISFY_QUARTRELY: -4,
}

/** 任務 */
var QUEST_DATA = {
    // #region 出撃
    /** [201]敵艦隊を撃破せよ！ */
    201: [
        /** 戦闘勝利 */
        new QuestData(1, true, true, RESET.DAILY),
    ],
    /** [210]敵艦隊を10回邀撃せよ！ */
    210: [
        /** 戦闘 */
        new QuestData(10, true, true, RESET.DAILY),
    ],
    /** [211]敵空母を3隻撃沈せよ！ */
    211: [
        /** (軽)空母撃沈 */
        new QuestData(3, true, true, RESET.DAILY),
    ],
    /** [212]敵輸送船団を叩け！ */
    212: [
        /** 補給艦撃沈 */
        new QuestData(5, true, true, RESET.DAILY),
    ],
    /** [213]海上通商破壊作戦 */
    213: [
        /** 補給艦撃沈 */
        new QuestData(20, true, true, RESET.WEEKLY),
    ],
    /** [214]あ号作戦 */
    214: [
        /** 出撃 */
        new QuestData(36, false, true, RESET.WEEKLY, "出撃"),
        /** S勝利 */
        new QuestData(6, false, true, RESET.WEEKLY, "S勝利"),
        /** ボス戦闘 */
        new QuestData(24, false, true, RESET.WEEKLY, "ボス戦闘"),
        /** ボス勝利 */
        new QuestData(12, false, true, RESET.WEEKLY, "ボス勝利"),
    ],
    /** [216]敵艦隊主力を撃滅せよ！ */
    216: [
        /** 道中戦闘またはボス勝利 */
        new QuestData(1, true, true, RESET.DAILY),
    ],
    /** [218]敵輸送船団を叩け！ */
    218: [
        /** 補給艦撃沈 */
        new QuestData(3, true, true, RESET.DAILY),
    ],
    /** [220]い号作戦 */
    220: [
        /** (軽)空母撃沈 */
        new QuestData(20, true, true, RESET.WEEKLY),
    ],
    /** [221]ろ号作戦 */
    221: [
        /** 補給艦撃沈 */
        new QuestData(50, true, true, RESET.WEEKLY),
    ],
    /** [226]南西諸島海域の制海権を握れ！ */
    226: [
        /** 南西海域(2-X)ボス勝利 */
        new QuestData(5, true, true, RESET.DAILY),
    ],
    /** [228]海上護衛戦 */
    228: [
        /** 潜水艦撃沈 */
        new QuestData(15, true, true, RESET.WEEKLY),
    ],
    /** [229]敵東方艦隊を撃滅せよ！ */
    229: [
        /** 東方海域(4-X)ボス勝利 */
        new QuestData(12, true, true, RESET.WEEKLY),
    ],
    /** [230]敵潜水艦を制圧せよ！ */
    230: [
        /** 潜水艦撃沈 */
        new QuestData(6, true, true, RESET.DAILY),
    ],
    /** [241]敵北方艦隊主力を撃滅せよ！ */
    241: [
        /** 北方海域(3-3~5)ボス勝利 */
        new QuestData(5, true, true, RESET.WEEKLY),
    ],
    /** [242]敵東方中枢艦隊を撃破せよ！ */
    242: [
        /** 敵東方中枢艦隊(4-4ボス)勝利 */
        new QuestData(1, true, true, RESET.WEEKLY),
    ],
    /** [243]南方海域珊瑚諸島沖の制空権を握れ！ */
    243: [
        /** 敵機動部隊本隊(5-2ボス)S勝利 */
        new QuestData(2, true, true, RESET.WEEKLY),
    ],
    /** [249]「第五戦隊」出撃せよ！ */
    249: [
        /** 敵主力艦隊(2-5ボス)S勝利 */
        new QuestData(1, true, true, RESET.MONTHLY),
    ],
    /** [256]「潜水艦隊」出撃せよ！ */
    256: [
        /** 敵回航中空母(6-1ボス)S勝利 */
        new QuestData(3, true, true, RESET.MONTHLY),
    ],
    /** [257]「水雷戦隊」南西へ！ */
    257: [
        /** 敵機動部隊(1-4ボス)S勝利 */
        new QuestData(1, true, true, RESET.MONTHLY),
    ],
    /** [259]「水上打撃部隊」南方へ！ */
    259: [
        /** 敵前線司令艦隊(5-1ボス)S勝利 */
        new QuestData(1, true, true, RESET.MONTHLY),
    ],
    /** [261]海上輸送路の安全確保に努めよ！ */
    261: [
        /** 敵通商破壊主力艦隊(1-5ボス)A勝利 */
        new QuestData(3, true, true, RESET.WEEKLY),
    ],
    /** [264]「空母機動部隊」西へ！ */
    264: [
        /** 東方主力艦隊(4-2ボス)S勝利 */
        new QuestData(1, true, true, RESET.MONTHLY),
    ],
    /** [265]海上護衛強化月間 */
    265: [
        /** 敵通商破壊主力艦隊(1-5ボス)A勝利 */
        new QuestData(10, true, true, RESET.MONTHLY),
    ],
    /** [266]「水上反撃部隊」突入せよ！ */
    266: [
        /** 敵主力艦隊(2-5ボス)S勝利 */
        new QuestData(1, true, true, RESET.MONTHLY),
    ],
    /** [822]沖ノ島海域迎撃戦 */
    822: [
        /** 敵侵攻中核艦隊(2-4ボス)S勝利 */
        new QuestData(2, true, true, RESET.QUARTRELY),
    ],
    /** [854]戦果拡張任務！「Z作戦」前段作戦 */
    854: [
        /** 敵侵攻中核艦隊(2-4ボス)A勝利 */
        new QuestData(1, false, true, RESET.QUARTRELY, "2-4"),
        /** 敵回航中空母(6-1ボス)A勝利 */
        new QuestData(1, false, true, RESET.QUARTRELY, "6-1"),
        /** 留守泊地旗艦艦隊(6-3ボス)A勝利 */
        new QuestData(1, false, true, RESET.QUARTRELY, "6-3"),
        /** 離島守備隊(6-4ボス)S勝利 */
        new QuestData(1, false, true, RESET.QUARTRELY, "6-4"),
    ],
    /** [861]強行輸送艦隊、抜錨！ */
    861: [
        /**  */
        new QuestData(2, true, true, RESET.QUARTRELY),
    ],
    /** [862]前線の航空偵察を実施せよ！ */
    862: [
        /** 留守泊地旗艦艦隊(6-3ボス)A勝利 */
        new QuestData(2, true, true, RESET.QUARTRELY),
    ],
    /** [873]北方海域警備を実施せよ！ */
    873: [
        /** 敵北方侵攻艦隊(3-1ボス)A勝利 */
        new QuestData(1, false, true, RESET.QUARTRELY, "3-1"),
        /** 敵キス島包囲艦隊(3-2ボス)A勝利 */
        new QuestData(1, false, true, RESET.QUARTRELY, "3-2"),
        /** 深海棲艦泊地艦隊(3-3ボス)A勝利 */
        new QuestData(1, false, true, RESET.QUARTRELY, "3-3"),
    ],
    /** [875]精鋭「三一駆」、鉄底海域に突入せよ！ */
    875: [
        /** 敵補給部隊本体(5-4ボス)S勝利 */
        new QuestData(2, true, true, RESET.QUARTRELY),
    ],
    /** [888]新編成「三川艦隊」、鉄底海峡に突入せよ！ */
    888: [
        /** 敵前線司令艦隊(5-1ボス戦)S勝利 */
        new QuestData(1, false, true, RESET.QUARTRELY, "5-1"),
        /** 敵サーモン方面主力艦隊(5-3ボス戦)S勝利 */
        new QuestData(1, false, true, RESET.QUARTRELY, "5-3"),
        /** 敵補給部隊本体(5-4ボス)S勝利 */
        new QuestData(1, false, true, RESET.QUARTRELY, "5-4"),
    ],
    /** [893]泊地周辺海域の安全確保を徹底せよ！ */
    893: [
        /** 敵通商破壊主力潜水艦隊(1-5 ボス)S 勝利 */
        new QuestData(3, false, true, RESET.QUARTRELY, "1-5"),
        /** 深海潜水艦隊集団 旗艦戦隊(7-1 ボス)S 勝利 */
        new QuestData(3, false, true, RESET.QUARTRELY, "7-1"),
        /** セレベス海方面 旗艦哨戒潜水艦(7-2-G ボス)S 勝利 */
        new QuestData(3, false, true, RESET.QUARTRELY, "7-2-1"),
        /** 深海任務部隊 主力機動部隊群(7-2-M ボス)S 勝利 */
        new QuestData(3, false, true, RESET.QUARTRELY, "7-2-2"),
    ],
    /** [894]空母戦力の投入による兵站線戦闘哨戒 */
    894: [
        /** 敵主力艦隊(1-3 ボス)S 勝利 */
        new QuestData(1, false, true, RESET.QUARTRELY, "1-3"),
        /** 敵機動部隊(1-4 ボス)S 勝利 */
        new QuestData(1, false, true, RESET.QUARTRELY, "1-4"),
        /** 敵主力艦隊(2-1 ボス)S 勝利 */
        new QuestData(1, false, true, RESET.QUARTRELY, "2-1"),
        /** 敵通商破壊機動部隊 主力艦隊(2-2 ボス)S 勝利 */
        new QuestData(1, false, true, RESET.QUARTRELY, "2-2"),
        /** 敵主力打撃群(2-3 ボス)S 勝利 */
        new QuestData(1, false, true, RESET.QUARTRELY, "2-3"),
    ],
    // #endregion
    // #region 演習
    /** [302]大規模演習 */
    302: [
        /**  */
        new QuestData(20, true, true, RESET.WEEKLY),
    ],
    /** [303]「演習」で練度向上！ */
    303: [
        /**  */
        new QuestData(3, true, true, RESET.DAILY),
    ],
    /** [304]「演習」で他提督を圧倒せよ！ */
    304: [
        /** 勝利 */
        new QuestData(5, true, true, RESET.DAILY),
    ],
    /** [311]精鋭艦隊演習 */
    311: [
        /** 勝利 */
        new QuestData(7, true, true, RESET.DAILY),
    ],
    /** [318]給糧艦「伊良湖」の支援 */
    318: [
        /** 勝利 */
        new QuestData(3, false, true, [RESET.NOT_SATISFY_DAILY, RESET.MONTHLY], "勝利"),
        /** 戦闘糧食 */
        new QuestData(1, false, false, RESET.NONE, "糧食"),
    ],
    // #endregion
    // #region 遠征
    /** [402]「遠征」を3回成功させよう！ */
    402: [
        /** 成功 */
        new QuestData(3, true, true, RESET.DAILY),
    ],
    /** [403]「遠征」を10回成功させよう！ */
    403: [
        /** 成功 */
        new QuestData(10, true, true, RESET.DAILY),
    ],
    /** [404]大規模遠征作戦、発令！ */
    404: [
        /** 成功 */
        new QuestData(30, true, true, RESET.WEEKLY),
    ],
    /** [410]南方への輸送作戦を成功させよ！ */
    410: [
        /** 「東京急行」or「東京急行(弐)」成功 */
        new QuestData(1, true, true, RESET.WEEKLY),
    ],
    /** [411]南方への鼠輸送を継続実施せよ！ */
    411: [
        /** 「東京急行」or「東京急行(弐)」成功 */
        new QuestData(6, true, true, RESET.WEEKLY),
    ],
    /** [424]輸送船団護衛を強化せよ！ */
    424: [
        /** 「海上護衛任務」成功 */
        new QuestData(4, true, true, RESET.MONTHLY),
    ],
    /** [426]海上通商航路の警戒を厳とせよ！ */
    426: [
        /** 「警備任務」成功 */
        new QuestData(1, false, true, RESET.QUARTRELY, "警備"),
        /** 「対潜警戒任務」成功 */
        new QuestData(1, false, true, RESET.QUARTRELY, "対潜"),
        /** 「海上護衛任務」成功 */
        new QuestData(1, false, true, RESET.QUARTRELY, "海上"),
        /** 「強行偵察任務」成功 */
        new QuestData(1, false, true, RESET.QUARTRELY, "偵察"),
    ],
    /** [428]近海に侵入する敵潜を制圧せよ！ */
    428: [
        /** 「対潜警戒任務」成功 */
        new QuestData(2, false, true, RESET.QUARTRELY, "対潜"),
        /** 「海峡警備行動」成功 */
        new QuestData(2, false, true, RESET.QUARTRELY, "海峡"),
        /** 「長時間対潜警戒」成功 */
        new QuestData(2, false, true, RESET.QUARTRELY, "長時間"),
    ],
    // #endregion
    // #region 補給/入渠
    /** [503]艦隊大整備！ */
    503: [
        /** 入渠 */
        new QuestData(5, true, true, RESET.DAILY),
    ],
    /** [504]艦隊酒保祭り！ */
    504: [
        /** 補給(一括は1回) */
        new QuestData(15, true, true, RESET.DAILY),
    ],
    // #endregion
    // #region 工廠
    /** [605]新装備「開発」指令 */
    605: [
        /** 開発 */
        new QuestData(1, true, true, RESET.DAILY),
    ],
    /** [606]新造艦「建造」指令 */
    606: [
        /** 建造 */
        new QuestData(1, true, true, RESET.DAILY),
    ],
    /** [607]装備「開発」集中強化！ */
    607: [
        /** 開発 */
        new QuestData(3, false, true, RESET.DAILY),
    ],
    /** [608]新造艦「建造」指令 */
    608: [
        /** 建造 */
        new QuestData(3, false, true, RESET.DAILY),
    ],
    /** [609]軍縮条約対応！ */
    609: [
        /** 解体(一括は別々) */
        new QuestData(2, true, true, RESET.DAILY),
    ],
    /** [613]資源の再利用 */
    613: [
        /** 廃棄(一括は1回) */
        new QuestData(24, true, true, RESET.WEEKLY),
    ],
    /** [619]装備の改修強化 */
    619: [
        /** 改修(失敗可) */
        new QuestData(1, true, true, RESET.DAILY),
    ],
    /** [626]精鋭「艦戦」隊の新編成 */
    626: [
        /** 「零式艦戦21型」廃棄 */
        new QuestData(2, false, true, RESET.MONTHLY, "21型"),
        /** 「九六式艦戦」廃棄 */
        new QuestData(1, false, true, RESET.MONTHLY, "96式"),
    ],
    /** [628]機種転換 */
    628: [
        /** 「零式艦戦52型」廃棄 */
        new QuestData(2, true, true, RESET.MONTHLY),
    ],
    /** [637]「熟練搭乗員」養成 */
    637: [
        /** 練度maxかつ改修max「九六式艦戦」搭載「鳳翔」旗艦 */
        new QuestData(1, false, false, RESET.NONE),
    ],
    /** [638]対空機銃量産 */
    638: [
        /** 「機銃」廃棄(一括は別々) */
        new QuestData(6, true, true, RESET.WEEKLY),
    ],
    /** [643]主力「陸攻」の調達 */
    643: [
        /** 「零式艦戦21型」廃棄(一括は別々) */
        new QuestData(2, false, true, RESET.QUARTRELY, "21型"),
        /** 「九六式陸攻」用意 */
        new QuestData(1, false, false, RESET.NONE, "陸攻"),
        /** 「九七式艦攻」用意 */
        new QuestData(2, false, false, RESET.NONE, "艦攻"),
    ],
    /** [645]「洋上補給」物資の調達 */
    645: [
        /** 「三式弾」廃棄(一括は別々) */
        new QuestData(1, false, true, RESET.MONTHLY, "三式弾"),
        /** 「燃料」用意 */
        new QuestData(750, false, false, RESET.NONE, "燃料"),
        /** 「弾薬」用意 */
        new QuestData(750, false, false, RESET.NONE, "弾薬"),
        /** 「ドラム缶(輸送用)」用意 */
        new QuestData(2, false, false, RESET.NONE, "ドラム"),
        /** 「九一式徹甲弾」用意 */
        new QuestData(1, false, false, RESET.NONE, "徹甲弾"),
    ],
    /** [663]新型艤装の継続研究 */
    663: [
        /** 「大口径主砲」廃棄(一括は別々) */
        new QuestData(10, false, true, RESET.QUARTRELY, "主砲"),
        /** 「鋼材」用意 */
        new QuestData(18000, false, false, RESET.NONE, "鋼材"),
    ],
    /** [673]装備開発力の整備 */
    673: [
        /** 「小口径主砲」廃棄(一括は別々) */
        new QuestData(4, true, true, RESET.DAILY),
    ],
    /** [674]工廠環境の整備 */
    674: [
        /** 「機銃」廃棄(一括は別々) */
        new QuestData(3, true, true, RESET.DAILY, "機銃"),
        /** 「鋼材」用意 */
        new QuestData(300, false, false, RESET.NONE, "鋼材"),
    ],
    /** [675]運用装備の統合整備 */
    675: [
        /** 「艦上戦闘機」廃棄(一括は別々) */
        new QuestData(6, true, true, RESET.QUARTRELY, "艦戦"),
        /** 「機銃」廃棄(一括は別々) */
        new QuestData(4, true, true, RESET.QUARTRELY, "機銃"),
        /** 「ボーキサイト」用意 */
        new QuestData(800, false, false, RESET.NONE, "ボーキ"),
    ],
    /** [676]装備開発力の集中整備 */
    676: [
        /** 「中口径主砲」廃棄(一括は別々) */
        new QuestData(3, false, true, RESET.WEEKLY, "主砲"),
        /** 「副砲」廃棄(一括は別々) */
        new QuestData(3, false, true, RESET.WEEKLY, "副砲"),
        /** 「ドラム缶(輸送用)」廃棄(一括は別々) */
        new QuestData(1, false, true, RESET.WEEKLY, "ドラム"),
        /** 「鋼材」用意 */
        new QuestData(2400, false, false, RESET.NONE, "鋼材"),
    ],
    /** [677]継戦支援能力の整備 */
    677: [
        /** 「大口径主砲」廃棄(一括は別々) */
        new QuestData(4, false, true, RESET.WEEKLY, "主砲"),
        /** 「水上偵察機」廃棄(一括は別々) */
        new QuestData(2, false, true, RESET.WEEKLY, "水偵"),
        /** 「魚雷」廃棄(一括は別々) */
        new QuestData(3, false, true, RESET.WEEKLY, "魚雷"),
        /** 「鋼材」用意 */
        new QuestData(3600, false, false, RESET.NONE, "鋼材"),
    ],
    /** [678]主力艦上戦闘機の更新 */
    678: [
        /** 「九六式艦戦」廃棄(一括は別々) */
        new QuestData(3, false, true, RESET.QUARTRELY, "96式"),
        /** 「零式艦戦21型」廃棄(一括は別々) */
        new QuestData(5, false, true, RESET.QUARTRELY, "21型"),
        /** 「ボーキサイト」用意 */
        new QuestData(4000, false, false, RESET.NONE, "ボーキ"),
        /** 秘書艦一番及び二番スロットに「零式艦戦52型」装備 */
        new QuestData(1, false, false, RESET.NONE, "配置"),
    ],
    /** [680]対空兵装の整備拡充 */
    680: [
        /** 「機銃」廃棄(一括は別々) */
        new QuestData(4, false, true, RESET.QUARTRELY, "機銃"),
        /** 「電探」廃棄(一括は別々) */
        new QuestData(4, false, true, RESET.QUARTRELY, "電探"),
        /** 「ボーキサイト」用意 */
        new QuestData(1500, false, false, RESET.NONE, "ボーキ"),
    ],
    /** [686]戦時改修A型高角砲の量産 */
    686: [
        /** 「10cm連装高角砲」廃棄(一括は別々) */
        new QuestData(4, false, true, RESET.QUARTRELY, "高角砲"),
        /** 「94式高射装置」廃棄(一括は別々) */
        new QuestData(1, false, true, RESET.QUARTRELY, "高射"),
        /** 「鋼材」用意 */
        new QuestData(900, false, false, RESET.NONE, "鋼材"),
    ],
    /** [688]航空戦力の強化 */
    688: [
        /** 「艦戦」廃棄(一括は別々) */
        new QuestData(3, false, true, RESET.QUARTRELY, "艦戦"),
        /** 「艦爆」廃棄(一括は別々) */
        new QuestData(3, false, true, RESET.QUARTRELY, "艦爆"),
        /** 「艦攻」廃棄(一括は別々) */
        new QuestData(3, false, true, RESET.QUARTRELY, "艦攻"),
        /** 「水偵」廃棄(一括は別々) */
        new QuestData(3, false, true, RESET.QUARTRELY, "水偵"),
        /** 「ボーキサイト」用意 */
        new QuestData(1800, false, false, RESET.NONE, "ﾎﾞｰｷ"),
    ],
    // #endregion
    // #region 近代化改修
    /** [702]艦の「近代化改修」を実施せよ！ */
    702: [
        /** 近代化改修成功 */
        new QuestData(2, true, true, RESET.DAILY),
    ],
    /** [703]「近代化改修」を進め、戦備を整えよ！ */
    703: [
        /** 近代化改修成功 */
        new QuestData(15, true, true, RESET.WEEKLY),
    ],
    // #endregion
}

/**
 * 任務
 * @param {Number} max 最大回数
 * @param {Boolean} isAdjust 調整対象か
 * @param {Boolean} canManual 手動変更出来るか
 * @param {Number} reset リセットタイミング(1:デイリー、2:ウィークリー、3:マンスリー、4:クォータリー)
 * @param {String} title 手動変更などに表示する題目、省略した場合は表示されない(=null)
 */
function QuestData(max, isAdjust, canManual, reset, title) {
    this.max = max
    this.isAdjust = isAdjust
    this.canManual = canManual
    this.reset = reset
    this.title = title === undefined ? null : title
}

/**
 * 任務フラグを未受注にする
 * @param {Number} id 任務ID
 */
function notOrder(id) {
    setData("IsActive" + id, false)
}

/**
 * 任務カウントを取得
 * @param {Number} id 任務ID
 * @param {Number} suffix 接尾辞
 */
function getQuestCount(id, suffix) {
    var key = "Count" + id + "_" + (suffix === undefined ? 1 : suffix)
    return Optional.ofNullable(getData(key)).orElse(0)
}

/**
 * 任務カウントを加算
 * @param {Number} id 任務ID
 * @param {Number} count 加算数
 * @param {Number} suffix 接尾辞
 */
function addQuestCount(id, count, suffix) {
    saveQuestCount(id, getQuestCount(id, suffix) + (count === undefined ? 1 : count), suffix)
}

/**
 * 任務カウントを保存
 * @param {Number} id 任務ID
 * @param {Number} count カウント
 * @param {Number} suffix 接尾辞
 * @param {Boolean} isRewrite 強制的に書き換えるか(デフォルト:false)
 */
function saveQuestCount(id, count, suffix, isRewrite) {
    var s = suffix === undefined ? 1 : suffix
    var key = "Count" + id + "_" + s
    if (isActive(id) || (isRewrite === undefined ? false : isRewrite)) {
        setData(key, Math.min(QUEST_DATA[id][s - 1].max, count))
    }
}

/**
 * IDの任務が遂行中か
 * @param {Number} id 任務ID
 * @return {Boolean} 遂行中か
 */
function isActive(id) {
    return getData("IsActive" + id)
}

/**
 * 進捗状況を返す
 * @param {Number} id 任務ID
 */
function getQuestRate(id) {
    return Optional.ofNullable(getData("Rate" + id)).orElse(0)
}

/**
 * 進捗状況を保存
 * @param {Number} id 任務ID
 * @param {Number} rate レート
 */
function saveQuestRate(id, rate) {
    setData("Rate" + id, rate)
}
