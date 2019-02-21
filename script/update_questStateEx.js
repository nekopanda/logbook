load("script/ScriptData.js")
load("script/questinfo.js")

GlobalContext = Java.type("logbook.data.context.GlobalContext")
DataType = Java.type("logbook.data.DataType")
ItemDto = Java.type("logbook.dto.ItemDto")
MaterialDto = Java.type("logbook.dto.MaterialDto")
ResultRank = Java.type("logbook.dto.ResultRank")
ShipDto = Java.type("logbook.dto.ShipDto")
ApplicationMain = Java.type("logbook.gui.ApplicationMain")
Item = Java.type("logbook.internal.Item")

ZonedDateTime = Java.type("java.time.ZonedDateTime")
ZoneId = Java.type("java.time.ZoneId")
Arrays = Java.type("java.util.Arrays")
List = Java.type("java.util.List")
Map = Java.type("java.util.Map")
Optional = Java.type("java.util.Optional")
TreeMap = Java.type("java.util.TreeMap")
Collectors = Java.type("java.util.stream.Collectors")

/**
 * @Override
 * 通信データを処理します
 *
 * @param type データの種類
 * @param data データ
 */
function update(type, data) {
    switch (type) {
        case DataType.QUEST_LIST: // 任務
            updateQuestCount()
            saveQuestState(data)
            adjustQuestCount(data)
            break
        case DataType.PORT: // 母港
            savePortItem(data)
            break
        case DataType.START: // 出撃
            addCountForSortiePart(data)
            break
        case DataType.NEXT: // 次のマス
            addCountForNextPart(data)
            break
        case DataType.BATTLE_RESULT: // 通常戦闘結果
        case DataType.COMBINED_BATTLE_RESULT: // 連合戦闘結果
            addCountForBattleResultPart(data)
            break
        case DataType.CREATE_ITEM: // 開発
            addCountForCreateItemPart(data)
            break
        case DataType.CREATE_SHIP: // 建造
            addCountForCreateShipPart(data)
            break
        case DataType.DESTROY_SHIP: // 解体
            addCountForDestroyShipPart(data)
            break
        case DataType.DESTROY_ITEM2: // 廃棄
            addCountForDestroyItem2Part(data)
            break
        case DataType.POWERUP: // 近代化改修
            addCountForPowerupPart(data)
            break
        case DataType.MISSION_RESULT: // 遠征（帰還）
            addCountForMissionResultPart(data)
            break
        case DataType.CHARGE: // 補給
            addCountForChargePart(data)
            break
        case DataType.NYUKYO_START: // 入渠開始
            addCountForEnteringDockPart(data)
            break
        case DataType.REMODEL_SLOT: // 装備改修
            addCountForRemodelSlotPart(data)
            break
        case DataType.PRACTICE_BATTLE_RESULT: // 演習
            addCountForPracticeBattleResultPart(data)
            break
        default:
            break
    }
    // 装備保存
    storeItemMap()
    // 資材更新
    updateMaterial()
    // 秘書艦更新
    updateSecretary()
    // 任務一覧更新
    ApplicationMain.main.questTable.update()
}

/**
 * 最新のitemMapをScriptData内に保存します
 */
function storeItemMap() {
    setTmpData("itemMap", new TreeMap(GlobalContext.itemMap))
}

/**
 * 保存されたitemMapを取り出します
 *
 * @return {java.util.TreeMap} 装備一覧
 */
function getStoredItemMap() {
    return getData("itemMap")
}

/**
 * 現在の時刻を取得[5時間ずらし]
 * @return {java.time.ZonedDateTime} 現在の時刻
 */
function getNowDateTime() {
    return ZonedDateTime.now(ZoneId.of("Etc/GMT-4")).withHour(0).withMinute(0).withSecond(0).withNano(0)
}

/**
 * 最後に任務を読み込んだ時刻を返す
 * @return {java.time.ZonedDateTime} 最後に読み込んだ時刻
 */
function getLastUpdateQuestTime() {
    return Optional.ofNullable(getData("LastUpdateTime")).orElse(ZonedDateTime.parse("2013-04-23T00:00:00+05:00[Etc/GMT-4]"))
}

/**
 * 最後に任務を読み込んだ時刻を保存
 * @param {java.time.ZonedDateTime} time 最後に読み込んだ時刻
 */
function saveLastUpdateQuestTime(time) {
    setData("LastUpdateTime", time)
}

/**
 * 任務状態を保存
 * @param {logbook.data.ActionData} data data
 */
function saveQuestState(data) {
    var json = data.jsonObject.api_data
    if (json.api_list instanceof List) {
        Java.from(json.api_list).filter(function (quest) {
            return (quest.api_type | 0) !== QUEST_TYPE.ONCE
        }).forEach(function (quest) {
            setData("IsActive" + quest.api_no, (quest.api_state | 0) === QUEST_STATE.ACTIVE || (quest.api_state | 0) === QUEST_STATE.COMPLETE)
        })
    }
}

/**
 * 母港
 * @param {logbook.data.ActionData} data data
 */
function savePortItem(data) {
    // 精鋭「艦戦」隊の新編成
    if (isActive(626) && getQuestCount(626, 1) >= 2 && getQuestCount(626, 2) >= 1) {
        if (!isMatchSecretary(626)) {
            notOrder(626)
            saveQuestCount(626, 0, 1, true)
            saveQuestCount(626, 0, 2, true)
        }
    }
    // 機種転換
    if (isActive(628) && getQuestCount(628) >= 2) {
        if (!isMatchSecretary(628)) {
            notOrder(628)
            saveQuestCount(628, 0, true)
        }
    }
    saveQuestCount(637, isActive(637) && isMatchSecretary(637) ? 1 : 0) //「熟練搭乗員」養成
    var itemMap = GlobalContext.itemMap
    if (itemMap instanceof Map) {
        var itemList = itemMap.entrySet().stream().map(function (item) {
            return item.getValue()
        }).collect(Collectors.groupingBy(function (item) {
            return item.slotitemId
        }))
        saveQuestCount(643, getLength(itemList[168]), 2, true) // 主力「陸攻」の調達[九六式陸攻]
        saveQuestCount(643, getLength(itemList[16]), 3, true) // 主力「陸攻」の調達[九七式艦攻]
        saveQuestCount(645, getLength(itemList[75]), 4, true) // 「洋上補給」物資の調達[ドラム缶(輸送用)]
        saveQuestCount(645, getLength(itemList[36]), 5, true) // 「洋上補給」物資の調達[九一式徹甲弾]
    }
}

/**
 * 海域が一致しているか
 * @param {Number} id 「X」-Y
 * @return {Boolean} 海域が一致しているか
 */
function isEqualArea(id) {
    var map = Optional.ofNullable(GlobalContext.sortieMap).map(function (map) {
        return map.map
    }).orElse([0, 0, 0])
    return map[0] === id
}

/**
 * マップが一致しているか
 * @param {Number} id 「X」-Y
 * @param {Number} no X-「Y」
 * @return {Boolean} マップが一致しているか
 */
function isEqualMap(id, no) {
    var map = Optional.ofNullable(GlobalContext.sortieMap).map(function (map) {
        return map.map
    }).orElse([0, 0, 0])
    return map[0] === id && map[1] === no
}

/**
 * セルが一致しているか
 * @param {Number} id 「X」-Y
 * @param {Number} no X-「Y」
 * @param {Number} cell X-Y-「Z」
 * @return {Boolean} マップが一致しているか
 */
function isEqualCell(id, no, cell) {
    var map = Optional.ofNullable(GlobalContext.sortieMap).map(function (map) {
        return map.map
    }).orElse([0, 0, 0])
    return map[0] === id && map[1] === no && map[2] === cell
}

/**
 * イベントが一致しているか
 * @param {Number} event イベント
 * @return {Boolean} イベントが一致しているか
 */
function isEqualEvent(event) {
    return event === Optional.ofNullable(GlobalContext.sortieMap).map(function (map) {
        return map.eventId
    }).orElse(-1)
}

/**
 * マップとイベントが一致しているか
 * @param {Number} id 「X」-Y
 * @param {Number} no X-「Y」
 * @param {Number} event イベント
 * @return {Boolean} マップとイベントが一致しているか
 */
function isEqualPosition(id, no, event) {
    return isEqualMap(id, no) && isEqualEvent(event)
}

/**
 * 出撃
 * @param {logbook.data.ActionData} data data
 */
function addCountForSortiePart(data) {
    addQuestCount(214, 1, 1) // あ号作戦[出撃]
}

/**
 * 勝利か
 * @param {logbook.dto.ResultRank} rank 勝利ランク
 * @return {Boolean} 勝利か
 */
function isWin(rank) {
    return isWinS(rank) || rank === ResultRank.A || rank === ResultRank.B
}

/**
 * S勝利か
 * @param {logbook.dto.ResultRank} rank 勝利ランク
 * @return {Boolean} S勝利か
 */
function isWinS(rank) {
    return rank === ResultRank.PERFECT || rank === ResultRank.S
}

/**
 * A勝利以上か
 * @param {logbook.dto.ResultRank} rank 勝利ランク
 * @return {Boolean} A勝利以上か
 */
function isWinA(rank) {
    return isWinS(rank) || rank === ResultRank.A
}

/**
 * 次のマス
 * @param {logbook.data.ActionData} data data
 */
function addCountForNextPart(data) {
    if (isEqualPosition(1, 6, EVENT_ID.ESCORT_SUCCESS)) {
        var sortieFleetIdx = Java.from(GlobalContext.isSortie).map(function (sortie, index) {
            return sortie ? index : -1
        }).filter(function (index) {
            return index !== -1
        })[0]
        var stypes = GlobalContext.getDock(sortieFleetIdx + 1).ships.stream().collect(Collectors.groupingBy(function (ship) {
            return ship.stype
        }))
        if (getLength(stypes[SHIP_TYPE.CVB]) === 2 || getLength(stypes[SHIP_TYPE.AO]) === 2) {
            addQuestCount(861) // 強行輸送艦隊、抜錨！
        }
    }
}

/**
 * 戦闘
 * @param {logbook.data.ActionData} data data
 */
function addCountForBattleResultPart(data) {
    var lastBattleDto = GlobalContext.lastBattleDto
    // #region 撃沈処理
    var enemies = lastBattleDto.enemy
    var nowEnemyHp = lastBattleDto.nowEnemyHp
    var sunkList = Arrays.stream(Java.to(Java.from(enemies).map(function (enemy) {
        return enemy.stype
    }).filter(function (stype, i) {
        return nowEnemyHp[i] === 0
    }))).collect(Collectors.groupingBy(function (stype) {
        return stype
    }))
    var ao = getLength(sunkList[SHIP_TYPE.E_AO])
    var cv = getLength(sunkList[SHIP_TYPE.CV]) + getLength(sunkList[SHIP_TYPE.CVL])
    var ss = getLength(sunkList[SHIP_TYPE.SS])
    addQuestCount(212, ao) // 敵輸送船団を叩け！
    addQuestCount(213, ao) // 海上通商破壊作戦
    addQuestCount(218, ao) // 敵補給艦を３隻撃沈せよ！
    addQuestCount(221, ao) // ろ号作戦
    addQuestCount(211, cv) // 敵空母を３隻撃沈せよ！
    addQuestCount(220, cv) // い号作戦
    addQuestCount(228, ss) // 海上護衛戦
    addQuestCount(230, ss) // 敵潜水艦を制圧せよ！
    // #endregion
    var rank = lastBattleDto.rank
    // #region 全般
    if (isWin(lastBattleDto.rank)) {
        addQuestCount(201) // 敵艦隊を撃破せよ！
    }
    addQuestCount(210) // 敵艦隊を10回邀撃せよ！
    if (isEqualEvent(EVENT_ID.NORMAL_BATTLE) || isEqualEvent(EVENT_ID.BOSS_BATTLE) && isWin(lastBattleDto.rank)) {
        addQuestCount(216) // 敵艦隊主力を撃滅せよ！
    }
    if (isWinS(lastBattleDto.rank)) {
        addQuestCount(214, 1, 2) // あ号作戦[S勝利]
    }
    if (isEqualEvent(EVENT_ID.BOSS_BATTLE)) {
        addQuestCount(214, 1, 3) // あ号作戦[ボス]
    }
    if (isEqualEvent(EVENT_ID.BOSS_BATTLE) && isWin(rank)) {
        addQuestCount(214, 1, 4) // あ号作戦[ボス勝利]
    }
    // #endregion
    var ships = lastBattleDto.dock.ships
    var stypes = ships.stream().collect(Collectors.groupingBy(function (ship) {
        return ship.stype
    }))
    var hasCV = (getLength(stypes[SHIP_TYPE.CVL]) + getLength(stypes[SHIP_TYPE.CV]) + getLength(stypes[SHIP_TYPE.ACV])) > 0
    // #region ○-○ボス勝利など
    // ボス戦じゃないなら処理終了
    if (!isEqualEvent(EVENT_ID.BOSS_BATTLE)) return
    // #region 鎮守府海域
    if (isEqualMap(1, 3) && isWinS(rank)) {
        if (hasCV) {
            addQuestCount(894, 1, 1) // 空母戦力の投入による兵站線戦闘哨戒[1-3]
        }
    }
    if (isEqualMap(1, 4) && isWinS(rank)) {
        // 軽巡旗艦、軽巡1~3隻、駆逐1隻以上、軽巡と駆逐のみ
        if (ships.get(0).stype === SHIP_TYPE.CL) {
            var cl = getLength(stypes[SHIP_TYPE.CL])
            var dd = getLength(stypes[SHIP_TYPE.DD])
            if (cl < 4 && dd > 0) {
                var shipNum = cl + dd
                if (ships.size() === shipNum) {
                    addQuestCount(257) // 「水雷戦隊」南西へ！
                }
            }
        }
        if (hasCV) {
            addQuestCount(894, 1, 2) // 空母戦力の投入による兵站線戦闘哨戒[1-4]
        }
    }
    if (isEqualMap(1, 5) && isWinA(rank)) {
        addQuestCount(261) // 海上輸送路の安全確保に努めよ！
        addQuestCount(265) // 海上護衛強化月間
    }
    if (isEqualMap(1, 5) && isWinS(rank)) {
        addQuestCount(893, 1, 1) // 泊地周辺海域の安全確保を徹底せよ！[1-5]
    }
    // #endregion
    // #region 南西諸島海域
    if (isEqualArea(2) && isWin(rank)) {
        addQuestCount(226) // 南西諸島海域の制海権を握れ！
    }
    if (isEqualMap(2, 1) && isWinS(rank)) {
        if (hasCV) {
            addQuestCount(894, 1, 3) // 空母戦力の投入による兵站線戦闘哨戒[2-1]
        }
    }
    if (isEqualMap(2, 2) && isWinS(rank)) {
        if (hasCV) {
            addQuestCount(894, 1, 4) // 空母戦力の投入による兵站線戦闘哨戒[2-2]
        }
    }
    if (isEqualMap(2, 3) && isWinS(rank)) {
        if (hasCV) {
            addQuestCount(894, 1, 5) // 空母戦力の投入による兵站線戦闘哨戒[2-3]
        }
    }
    if (isEqualMap(2, 4) && isWinA(rank)) {
        addQuestCount(854, 1, 1) // 戦果拡張任務！「Z作戦」前段作戦[2-4]
    }
    if (isEqualMap(2, 4) && isWinS(rank)) {
        addQuestCount(822) // 沖ノ島海域迎撃戦
    }
    if (isEqualMap(2, 5) && isWinS(rank)) {
        var num = ships.stream().map(function (ship) {
            return ship.shipInfo.flagship
        }).filter(function (name) {
            return name.equals("みょうこう") || name.equals("なち") || name.equals("はぐろ")
        }).length
        if (num === 3) {
            addQuestCount(249) // 「第五戦隊」出撃せよ！
        }
    }
    if (isEqualMap(2, 5) && isWinS(rank)) {
        // 駆逐旗艦、重巡1隻、軽巡1隻、駆逐4隻
        if (ships.get(0).stype === SHIP_TYPE.DD) {
            if (getLength(stypes[SHIP_TYPE.CA]) === 1 && getLength(stypes[SHIP_TYPE.CL]) === 1 && getLength(stypes[SHIP_TYPE.DD]) === 4) {
                addQuestCount(266) // 「水上反撃部隊」突入せよ！
            }
        }
    }
    // #endregion
    // #region 北方海域
    if (isEqualMap(3, 1) && isWinA(rank)) {
        // 軽巡1隻以上
        if (getLength(stypes[SHIP_TYPE.CL]) > 0) {
            addQuestCount(873, 1, 1) // 北方海域警備を実施せよ！[3-1]
        }
    }
    if (isEqualMap(3, 2) && isWinA(rank)) {
        // 軽巡1隻以上
        if (getLength(stypes[SHIP_TYPE.CL]) > 0) {
            addQuestCount(873, 1, 2) // 北方海域警備を実施せよ！[3-2]
        }
    }
    if (isEqualMap(3, 3) && isWinA(rank)) {
        // 軽巡1隻以上
        if (getLength(stypes[SHIP_TYPE.CL]) > 0) {
            addQuestCount(873, 1, 3) // 北方海域警備を実施せよ！[3-3]
        }
    }
    if ((isEqualMap(3, 3) || isEqualMap(3, 4) || isEqualMap(3, 5)) && isWin(rank)) {
        addQuestCount(241) // 敵北方艦隊主力を撃滅せよ！
    }
    // #endregion
    // #region 東方海域
    if (isEqualArea(4) && isWin(rank)) {
        addQuestCount(229) // 敵東方艦隊を撃滅せよ！
    }
    if (isEqualMap(4, 2) && isWinS(rank)) {
        // 空母2隻、駆逐2隻
        var cv = getLength(stypes[SHIP_TYPE.CVL]) + getLength(stypes[SHIP_TYPE.CV]) + getLength(stypes[SHIP_TYPE.ACV])
        if (cv === 2 && getLength(stypes[SHIP_TYPE.DD]) === 2) {
            addQuestCount(264) // 「空母機動部隊」西へ！
        }
    }
    if (isEqualMap(4, 4) && isWin(rank)) {
        addQuestCount(242) // 敵東方中枢艦隊を撃破せよ！
    }
    // #endregion
    // #region 南方海域
    var newMikawaNum = ships.stream().map(function (ship) {
        return ship.shipInfo.flagship
    }).filter(function (name) {
        return ["ちょうかい", "あおば", "きぬがさ", "かこ", "ふるたか", "てんりゅう", "ゆうばり"].some(function (_name) {
            return name.equals(_name)
        })
    }).length
    if (isEqualMap(5, 1) && isWinS(rank)) {
        if (newMikawaNum >= 4) {
            addQuestCount(888, 1) // 新編成「三川艦隊」、鉄底海峡に突入せよ！[5-1]
        }
        var num = ships.stream().map(function (ship) {
            return ship.shipInfo.json.api_ctype
        }).filter(function (ctype) {
            // 扶桑型or伊勢型or長門型or大和型
            return ctype === 26 || ctype === 2 || ctype === 19 || ctype === 37
        }).length
        if (num === 3 && getLength(stypes[SHIP_TYPE.CL]) === 1) {
            addQuestCount(259) // 「水上打撃部隊」南方へ！
        }
    }
    if (isEqualMap(5, 2) && isWinS(rank)) {
        addQuestCount(243) // 南方海域珊瑚諸島沖の制空権を握れ！
    }
    if (isEqualMap(5, 3) && isWinS(rank)) {
        if (newMikawaNum >= 4) {
            addQuestCount(888, 2) // 新編成「三川艦隊」、鉄底海峡に突入せよ！[5-3]
        }
    }
    if (isEqualMap(5, 4) && isWinS(rank)) {
        if (newMikawaNum >= 4) {
            addQuestCount(888, 3) // 新編成「三川艦隊」、鉄底海峡に突入せよ！[5-4]
        }
        var naganami = ships.stream().anyMatch(function (ship) {
            return ship.shipId === 543
        })
        var no31s = ships.stream().anyMatch(function (ship) {
            // 朝霜改or高波改or沖波改
            return ship.shipId === 344 || ship.shipId === 345 || ship.shipId === 359
        })
        if (naganami && no31s) {
            addQuestCount(875) // 精鋭「三一駆」、鉄底海域に突入せよ！
        }
    }
    // #endregion
    // #region 中部海域
    if (isEqualMap(6, 1) && isWinA(rank)) {
        addQuestCount(854, 1, 2) // 戦果拡張任務！「Z作戦」前段作戦[6-1]
    }
    if (isEqualMap(6, 1) && isWinS(rank)) {
        addQuestCount(256) // 「潜水艦隊」出撃せよ！
    }
    if (isEqualMap(6, 3) && isWinA(rank)) {
        addQuestCount(854, 1, 3) // 戦果拡張任務！「Z作戦」前段作戦[6-3]
    }
    if (isEqualMap(6, 3) && isWinA(rank)) {
        // 水母1隻、軽巡2隻
        if (getLength(stypes[SHIP_TYPE.AV]) === 1 && getLength(stypes[SHIP_TYPE.CL]) === 2) {
            addQuestCount(862) // 前線の航空偵察を実施せよ！
        }
    }
    if (isEqualMap(6, 4) && isWinS(rank)) {
        addQuestCount(854, 1, 4) // 戦果拡張任務！「Z作戦」前段作戦[6-4]
    }
    // #endregion
    // #region 南西海域
    if (isEqualMap(7, 1) && isWinS(rank)) {
        addQuestCount(893, 1, 2) // 泊地周辺海域の安全確保を徹底せよ！[7-1]
    }
    if (isEqualMap(7, 2, 7) && isWinS(rank)) {
        addQuestCount(893, 1, 3) // 泊地周辺海域の安全確保を徹底せよ！[7-2-1]
    }
    if (isEqualMap(7, 2, 15) && isWinS(rank)) {
        addQuestCount(893, 1, 4) // 泊地周辺海域の安全確保を徹底せよ！[7-2-2]
    }
    // #endregion
    // #endregion
}

/**
 * 開発
 * @param {logbook.data.ActionData} data data
 */
function addCountForCreateItemPart(data) {
    addQuestCount(605) // 新装備「開発」指令
    addQuestCount(607) // 装備「開発」集中強化！
}

/**
 * 建造
 * @param {logbook.data.ActionData} data data
 */
function addCountForCreateShipPart(data) {
    addQuestCount(606) // 新造艦「建造」指令
    addQuestCount(608) // 艦娘「建造」艦隊強化！
}

/**
 * 解体
 * @param {logbook.data.ActionData} data data
 */
function addCountForDestroyShipPart(data) {
    var num = String(data.getField("api_ship_id")).split(",").length
    addQuestCount(609, num) // 軍縮条約対応！
}

/**
 * 廃棄
 * @param {logbook.data.ActionData} data data
 */
function addCountForDestroyItem2Part(data) {
    var itemList = getStoredItemMap()
    if (itemList instanceof Map) {
        var destroyItems = String(data.getField("api_slotitem_ids")).split(",").map(function (id) {
            return itemList.get(id | 0)
        })
        var type2 = Arrays.stream(Java.to(destroyItems)).collect(Collectors.groupingBy(function (item) {
            return item.type2
        }))
        var slotitemId = Arrays.stream(Java.to(destroyItems)).collect(Collectors.groupingBy(function (item) {
            return item.slotitemId
        }))
        // 精鋭「艦戦」隊の新編成
        if (isMatchSecretary(626)) {
            addQuestCount(626, getLength(slotitemId[19]), 2)
            addQuestCount(626, getLength(slotitemId[20]), 3)
        }
        // 機種転換
        if (isMatchSecretary(628)) {
            addQuestCount(628, getLength(slotitemId[21]))
        }
        // 対空機銃量産
        addQuestCount(638, getLength(type2[21]))
        // 主力「陸攻」の調達
        addQuestCount(643, getLength(slotitemId[20]), 1) // 零式艦戦21型
        // 「洋上補給」物資の調達
        addQuestCount(645, getLength(slotitemId[35]), 1) // 三式弾
        // 新型艤装の継続研究
        addQuestCount(663, getLength(type2[3]), 1) // 大口径主砲
        // 装備開発力の整備
        addQuestCount(673, getLength(type2[1])) // 小口径主砲
        // 工廠環境の整備
        addQuestCount(674, getLength(type2[21]), 1) // 機銃
        // 運用装備の統合整備
        addQuestCount(675, getLength(type2[6]), 1) // 艦上戦闘機
        addQuestCount(675, getLength(type2[21]), 2) // 機銃
        // 装備開発力の集中整備
        addQuestCount(676, getLength(type2[2]), 1) // 中口径主砲
        addQuestCount(676, getLength(type2[4]), 2) // 副砲
        addQuestCount(676, getLength(slotitemId[75]), 3) // ドラム缶(輸送用)
        // 継戦支援能力の整備
        addQuestCount(677, getLength(type2[3]), 1) // 大口径主砲
        addQuestCount(677, getLength(type2[10]), 2) // 水上偵察機
        addQuestCount(677, getLength(type2[5]) + getLength(type2[32]), 3) // 魚雷
        // 主力艦上戦闘機の更新
        addQuestCount(678, getLength(slotitemId[19]), 1) // 九六式艦戦
        addQuestCount(678, getLength(slotitemId[20]), 2) // 零式艦戦21型
        // 対空兵装の整備拡充
        addQuestCount(680, getLength(type2[21]), 1) // 機銃
        addQuestCount(680, getLength(type2[12]) + getLength(type2[13]) + getLength(type2[93]), 2) // 電探
        // 戦時改修A型高角砲の量産
        if (isMatchSecretary(686)) {
            addQuestCount(686, getLength(slotitemId[3]), 1) // 10cm連装高角砲
            addQuestCount(686, getLength(slotitemId[121]), 2) // 94式高射装置
        }
        // 航空戦力の強化
        addQuestCount(688, getLength(type2[6]), 1) // 艦上戦闘機
        addQuestCount(688, getLength(type2[7]), 2) // 艦上爆撃機
        addQuestCount(688, getLength(type2[8]), 3) // 艦上攻撃機
        addQuestCount(688, getLength(type2[10]), 4) // 水上偵察機
    }
    addQuestCount(613)
}

/**
 * 秘書艦条件に一致しているか
 * @param {Number} id 任務ID
 * @return {Boolean} 一致しているか
 */
function isMatchSecretary(id) {
    var secretary = GlobalContext.secretary
    if (secretary instanceof ShipDto) {
        switch (id) {
            case 318:
                var items = Java.from(secretary.item2)
                items.push(secretary.slotExItem)
                return items.filter(function (item) {
                    return item instanceof ItemDto && item.slotitemId === 145
                }).length >= 2
            case 626:
                if (secretary.shipInfo.flagship.equals("ほうしょう")) {
                    return secretary.item2.stream().filter(function (item) {
                        return item instanceof ItemDto
                    }).anyMatch(function (item) {
                        return item.slotitemId === 20 && item.alv === 7
                    })
                }
                return false
            case 628:
                var stype = secretary.stype
                if (stype === SHIP_TYPE.CV || stype === SHIP_TYPE.ACV || stype === SHIP_TYPE.CVL) {
                    return secretary.item2.stream().filter(function (item) {
                        return item instanceof ItemDto
                    }).anyMatch(function (item) {
                        return item.slotitemId === 96 && item.alv === 7
                    })
                }
                return false
            case 637:
                if (secretary.shipInfo.flagship.equals("ほうしょう")) {
                    return secretary.item2.stream().filter(function (item) {
                        return item instanceof ItemDto
                    }).anyMatch(function (item) {
                        return item.slotitemId === 19 && item.alv === 7 && item.level === 10
                    })
                }
                return false
            case 678:
                var item2 = secretary.item2
                var count = 0
                if (item2.size() > 1) {
                    for (var i = 0; i < 2; i++) {
                        if (item2.get(i) instanceof ItemDto && item2.get(i).slotitemId === 21) {
                            count++
                        }
                    }
                }
                return count === 2
            case 686:
                var ctype = secretary.shipInfo.json.api_ctype
                if ([1, 5, 12].indexOf(ctype) >= 0) { // 綾波型、暁型、吹雪型
                    if (item2.size() > 0) {
                        var item = item2.get(0)
                        return item instanceof ItemDto && item.slotitemId === 294 && item.level === 10
                    }
                }
                return false
        }
    }
    return false
}

/**
 * 近代化改修
 * @param {logbook.data.ActionData} data data
 */
function addCountForPowerupPart(data) {
    var powerup_flag = data.jsonObject.api_data.api_powerup_flag.intValue()
    if (powerup_flag === POWERUP_FLAG.SUCCESS) {
        addQuestCount(702) // 艦の「近代化改修」を実施せよ！
        addQuestCount(703) // 「近代化改修」を進め、戦備を整えよ！
    }
}

/**
 * 遠征
 * @param {logbook.data.ActionData} data data
 */
function addCountForMissionResultPart(data) {
    //0=失敗、1=成功、2=大成功
    var clear_result = data.jsonObject.api_data.api_clear_result.intValue()
    if (clear_result === EXPEDITION.SUCCESS || clear_result === EXPEDITION.GREAT_SUCCESS) {
        var quest_name = data.jsonObject.api_data.api_quest_name.toString()
        addQuestCount(402) //「遠征」を3回成功させよう！
        addQuestCount(403) //「遠征」を10回成功させよう！
        addQuestCount(404) //大規模遠征作戦、発令！
        switch (quest_name) {
            case "警備任務": // ID:02
                addQuestCount(426, 1, 1) // 海上通商航路の警戒を厳とせよ！
                break
            case "対潜警戒任務": // ID:04
                addQuestCount(426, 1, 2) // 海上通商航路の警戒を厳とせよ！
                addQuestCount(428, 1, 1) // 近海に侵入する敵潜を制圧せよ！
                break
            case "海上護衛任務": // ID:05
                addQuestCount(424) // 輸送船団護衛を強化せよ！
                addQuestCount(426, 1, 3) // 海上通商航路の警戒を厳とせよ！
                break
            case "海峡警備行動": // ID:A2
                addQuestCount(428, 1, 2) // 近海に侵入する敵潜を制圧せよ！
                break
            case "長時間対潜警戒": // ID:A3
                addQuestCount(428, 1, 3) // 近海に侵入する敵潜を制圧せよ！
                break
            case "強行偵察任務": // ID:10
                addQuestCount(426, 1, 4) // 海上通商航路の警戒を厳とせよ！
                break
        }
        //api_no渡してこないので仕方なく
        if (quest_name.indexOf("東京急行") > -1) {
            addQuestCount(410) // 南方への輸送作戦を成功させよ！
            addQuestCount(411) // 南方への鼠輸送を継続実施せよ!
        }
    }
}

/**
 * 補給
 * @param {logbook.data.ActionData} data data
 */
function addCountForChargePart(data) {
    addQuestCount(504) // 艦隊酒保祭り！
}

/**
 * 入渠
 * @param {logbook.data.ActionData} data data
 */
function addCountForEnteringDockPart(data) {
    addQuestCount(503) // 艦隊大整備！
}

/**
 * 装備改修
 * @param {logbook.data.ActionData} data data
 */
function addCountForRemodelSlotPart(data) {
    addQuestCount(619) // 装備の改修強化
}

/**
 * 演習
 * @param {logbook.data.ActionData} data data
 */
function addCountForPracticeBattleResultPart(data) {
    var lastBattleDto = GlobalContext.lastBattleDto
    addQuestCount(303) // 「演習」で練度向上！
    var rank = lastBattleDto.rank
    if (isWin(rank)) {
        addQuestCount(304) // 「演習」で他提督を圧倒せよ！
        addQuestCount(302) // 大規模演習
        addQuestCount(311) // 精鋭艦隊演習
        var ships = lastBattleDto.dock.ships
        var stypes = ships.stream().collect(Collectors.groupingBy(function (ship) {
            return ship.stype
        }))
        var cl = getLength(stypes[SHIP_TYPE.CL])
        if (cl >= 2) {
            addQuestCount(318, 1, 1) // 給糧艦「伊良湖」の支援[勝利]
        }
    }
}

/**
 * 資材更新
 */
function updateMaterial() {
    var material = GlobalContext.material
    if (material instanceof MaterialDto) {
        var fuel = material.fuel
        var ammo = material.ammo
        var steel = material.metal
        var bauxite = material.bauxite
        // 燃料
        saveQuestCount(645, fuel, 2, true) // 「洋上補給」物資の調達[燃料]
        // 弾薬
        saveQuestCount(645, ammo, 3, true) // 「洋上補給」物資の調達[弾薬]
        // 鋼材
        saveQuestCount(663, steel, 2, true) // 新型艤装の継続研究[鋼材]
        saveQuestCount(674, steel, 2, true) // 工廠環境の整備[鋼材]
        saveQuestCount(676, steel, 4, true) // 装備開発力の集中整備[鋼材]
        saveQuestCount(677, steel, 4, true) // 継戦支援能力の整備[鋼材]
        saveQuestCount(686, steel, 3, true) // 戦時改修A型高角砲の量産[鋼材]
        // ボーキサイト
        saveQuestCount(675, bauxite, 3, true) // 運用装備の統合整備[ボーキサイト]
        saveQuestCount(678, bauxite, 3, true) // 主力艦上戦闘機の更新[ボーキサイト]
        saveQuestCount(680, bauxite, 3, true) // 対空兵装の整備拡充[ボーキサイト]
        saveQuestCount(688, bauxite, 5, true) // 航空戦力の強化[ボーキサイト]
    }
}

/**
 * 秘書艦更新
 */
function updateSecretary() {
    // 給糧艦「伊良湖」の支援[戦闘糧食]
    saveQuestCount(318, isActive(318) && getQuestCount(318, 1) >= 3 && isMatchSecretary(318) ? 1 : 0, 2, true)
    saveQuestCount(678, isMatchSecretary(678) ? 1 : 0, 4, true) // 主力艦上戦闘機の更新
}

/**
 * 任務回数更新など
 */
function updateQuestCount() {
    var lastUpdateQuestTime = getLastUpdateQuestTime()
    var nowTime = getNowDateTime()
    // デイリー
    if (!lastUpdateQuestTime.equals(nowTime)) {
        resetQuestCountOfDaily()
    }
    // ウィークリー
    if (!lastUpdateQuestTime.minusDays((lastUpdateQuestTime.dayOfWeek.value - 1) % 7).equals(nowTime.minusDays((nowTime.dayOfWeek.value - 1) % 7))) {
        resetQuestCountOfWeekly()
    }
    // マンスリー
    if (!lastUpdateQuestTime.withDayOfMonth(1).equals(nowTime.withDayOfMonth(1))) {
        resetQuestCountOfMonthly()
    }
    // クォータリー
    if (!((Math.floor((lastUpdateQuestTime.month.value + 2) % 3) === Math.floor((nowTime.month.value + 2) % 3)) &&
            (lastUpdateQuestTime.year.value === nowTime.year.value || !(lastUpdateQuestTime.year.value === nowTime.year.value - 1 && lastUpdateQuestTime.month.value === 12)))) {
        resetQuestCountOfQuarterly()
    }
    saveLastUpdateQuestTime(nowTime)
}

/**
 * 任務回数の調整
 * @param {logbook.data.ActionData} data data
 */
function adjustQuestCount(data) {
    var json = data.jsonObject.api_data
    if (json.api_list instanceof List) {
        Java.from(json.api_list).filter(function (quest) {
            return quest.api_type !== QUEST_TYPE.ONCE
        }).filter(function (quest) {
            return quest.api_no in QUEST_DATA
        }).forEach(function (quest) {
            var conditions = QUEST_DATA[quest.api_no]
            conditions.forEach(function (condition, i) {
                switch (quest.api_progress_flag.intValue()) {
                    case QUEST_PROGRESS_FLAG.NONE:
                        switch (quest.api_state.intValue()) {
                            case QUEST_STATE.NOT_ORDER:
                            case QUEST_STATE.ACTIVE:
                                if (condition.isAdjust) {
                                    // 50%↑
                                    if (getQuestCount(quest.api_no, i + 1) >= Math.ceil(condition.max * 0.5)) {
                                        saveQuestCount(quest.api_no, Math.ceil(condition.max * 0.5) - 1, i + 1, true)
                                    }
                                }
                                break
                            case QUEST_STATE.COMPLETE:
                                saveQuestCount(quest.api_no, condition.max, i + 1, true)
                                break
                        }
                        break
                    case QUEST_PROGRESS_FLAG.HALF:
                        if (condition.isAdjust) {
                            // 50%↓
                            if (getQuestCount(quest.api_no, i + 1) < Math.ceil(condition.max * 0.5)) {
                                saveQuestCount(quest.api_no, Math.ceil(condition.max * 0.5), i + 1, true)
                            }
                            // 80%↑
                            if (getQuestCount(quest.api_no, i + 1) > Math.ceil(condition.max * 0.8)) {
                                saveQuestCount(quest.api_no, Math.ceil(condition.max * 0.8) - 1, i + 1, true)
                            }
                        }
                        break
                    case QUEST_PROGRESS_FLAG.EIGHTY:
                        if (condition.isAdjust) {
                            // 80%↓
                            if (getQuestCount(quest.api_no, i + 1) < Math.ceil(condition.max * 0.8)) {
                                saveQuestCount(quest.api_no, Math.ceil(condition.max * 0.8), i + 1, true)
                            }
                            // 100%↑
                            if (getQuestCount(quest.api_no, i + 1) >= condition.max) {
                                saveQuestCount(quest.api_no, condition.max - 1, i + 1, true)
                            }
                        }
                        break
                }
            })
        })
    }
}

/**
 * デイリーのカウントをリセットする
 */
function resetQuestCountOfDaily() {
    Object.keys(QUEST_DATA).map(function (id) {
        return QUEST_DATA[id].map(function (quest, i) {
            return [id, i + 1, quest]
        }).filter(function (data) {
            if (Array.isArray(data[2].reset)) {
                return data[2].reset.some(function (reset) {
                    return reset === RESET.DAILY || reset === RESET.NOT_SATISFY_DAILY && getQuestCount(data[0], data[1]) >= data[2].max
                })
            }
            return data[2].reset === RESET.DAILY || data[2].reset === RESET.NOT_SATISFY_DAILY && getQuestCount(data[0], data[1]) >= data[2].max
        })
    }).reduce(function (acc, val) {
        return acc.concat(val)
    }, []).forEach(function (data) {
        saveQuestCount(data[0], 0, data[1], true)
        var isNotOrder = function (reset) {
            if (Array.isArray(reset)) {
                return reset.some(function (reset) {
                    return reset === RESET.DAILY
                })
            }
            return reset === RESET.DAILY
        }(QUEST_DATA[data[0]][data[1] - 1].reset)
        if (isNotOrder) {
            notOrder(data[0])
        }
    })
}

/**
 * ウィークリーのカウントをリセットする
 */
function resetQuestCountOfWeekly() {
    Object.keys(QUEST_DATA).map(function (id) {
        return QUEST_DATA[id].map(function (quest, i) {
            return [id, i + 1, quest]
        }).filter(function (data) {
            if (Array.isArray(data[2].reset)) {
                return data[2].reset.some(function (reset) {
                    return reset === RESET.WEEKLY || reset === RESET.NOT_SATISFY_WEEKLY && getQuestCount(data[0], data[1]) >= data[2].max
                })
            }
            return data[2].reset === RESET.WEEKLY || data[2].reset === RESET.NOT_SATISFY_WEEKLY && getQuestCount(data[0], data[1]) >= data[2].max
        })
    }).reduce(function (acc, val) {
        return acc.concat(val)
    }, []).forEach(function (data) {
        saveQuestCount(data[0], 0, data[1], true)
        var isNotOrder = function (reset) {
            if (Array.isArray(reset)) {
                return reset.some(function (reset) {
                    return reset === RESET.WEEKLY
                })
            }
            return reset === RESET.WEEKLY
        }(QUEST_DATA[data[0]][data[1] - 1].reset)
        if (isNotOrder) {
            notOrder(data[0])
        }
    })
}

/**
 * マンスリーのカウントをリセットする
 */
function resetQuestCountOfMonthly() {
    Object.keys(QUEST_DATA).map(function (id) {
        return QUEST_DATA[id].map(function (quest, i) {
            return [id, i + 1, quest]
        }).filter(function (data) {
            if (Array.isArray(data[2].reset)) {
                return data[2].reset.some(function (reset) {
                    return reset === RESET.MONTHLY || reset === RESET.NOT_SATISFY_MONTHLY && getQuestCount(data[0], data[1]) >= data[2].max
                })
            }
            return data[2].reset === RESET.MONTHLY || data[2].reset === RESET.NOT_SATISFY_MONTHLY && getQuestCount(data[0], data[1]) >= data[2].max
        })
    }).reduce(function (acc, val) {
        return acc.concat(val)
    }, []).forEach(function (data) {
        saveQuestCount(data[0], 0, data[1], true)
        var isNotOrder = function (reset) {
            if (Array.isArray(reset)) {
                return reset.some(function (reset) {
                    return reset === RESET.MONTHLY
                })
            }
            return reset === RESET.MONTHLY
        }(QUEST_DATA[data[0]][data[1] - 1].reset)
        if (isNotOrder) {
            notOrder(data[0])
        }
    })
}

/**
 * クォータリーのカウントをリセットする
 */
function resetQuestCountOfQuarterly() {
    Object.keys(QUEST_DATA).map(function (id) {
        return QUEST_DATA[id].map(function (quest, i) {
            return [id, i + 1, quest]
        }).filter(function (data) {
            if (Array.isArray(data[2].reset)) {
                return data[2].reset.some(function (reset) {
                    return reset === RESET.QUARTRELY || reset === RESET.NOT_SATISFY_QUARTRELY && getQuestCount(data[0], data[1]) >= data[2].max
                })
            }
            return data[2].reset === RESET.QUARTRELY || data[2].reset === RESET.NOT_SATISFY_QUARTRELY && getQuestCount(data[0], data[1]) >= data[2].max
        })
    }).reduce(function (acc, val) {
        return acc.concat(val)
    }, []).forEach(function (data) {
        saveQuestCount(data[0], 0, data[1], true)
        var isNotOrder = function (reset) {
            if (Array.isArray(reset)) {
                return reset.some(function (reset) {
                    return reset === RESET.QUARTRELY
                })
            }
            return reset === RESET.QUARTRELY
        }(QUEST_DATA[data[0]][data[1] - 1].reset)
        if (isNotOrder) {
            notOrder(data[0])
        }
    })
}

/**
 * 配列長を返す(Null対策)
 *
 * @param {[]} arrays 配列
 * @return {Number} 配列長
 */
function getLength(arrays) {
    return (arrays || []).length
}
