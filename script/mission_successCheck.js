load("script/utils.js")

Optional = Java.type("java.util.Optional")
Collectors = Java.type("java.util.stream.Collectors")
GlobalContext = Java.type("logbook.data.context.GlobalContext")

var fleetId

function header() {
	return ["成功"]
}

function begin(fleetId) {
	this.fleetId = fleetId
}

function body(data) {
	return toComparable([canMission(data.id) === undefined ? "?" : canMission(data.id) ? "○" : "x"])
}

/**
 * 遠征が成功するかを返します
 *
 * @param {Number} id 遠征ID
 * @return {Boolean|undefined} 登録されてないならundefined、そうでない場合はBooleanで成功かを返す
 */
function canMission(id) {
	var dock = GlobalContext.getDock(this.fleetId)

	if (dock && dock.ships) {
		var ships = Java.from(dock.ships)
		var shipNum = ships.length
		var stypes = dock.ships.stream().collect(Collectors.groupingBy(function (ship) {
			return ship.stype
		}))
		/** 海防艦 */
		var DE = Optional.ofNullable(stypes[1]).orElse([]).length
		/** 駆逐艦 */
		var DD = Optional.ofNullable(stypes[2]).orElse([]).length
		/** 軽巡洋艦 */
		var CL = Optional.ofNullable(stypes[3]).orElse([]).length
		/** 重巡洋艦 */
		var CA = Optional.ofNullable(stypes[5]).orElse([]).length
		/** 軽空母 */
		var CVL = Optional.ofNullable(stypes[7]).orElse([]).length
		/** 航空戦艦 */
		var CVB = Optional.ofNullable(stypes[10]).orElse([]).length
		/** 正規空母 */
		var CV = Optional.ofNullable(stypes[11]).orElse([]).length
		/** 潜水艦 */
		var SS = Optional.ofNullable(stypes[13]).orElse([]).length
		/** 潜水空母 */
		var CVS = Optional.ofNullable(stypes[14]).orElse([]).length
		/** 水上機母艦 */
		var AV = Optional.ofNullable(stypes[16]).orElse([]).length
		/** 装甲空母 */
		var ACV = Optional.ofNullable(stypes[18]).orElse([]).length
		/** 潜水母艦 */
		var AS = Optional.ofNullable(stypes[20]).orElse([]).length
		/** 旗艦 */
		var flagship = ships[0]
		/** 旗艦Lv */
		var flagshipLv = flagship.lv
		/** 旗艦艦種 */
		var flagshipStype = flagship.stype
		/** 艦隊合計Lv */
		var totalLv = ships.map(function (ship) {
			return ship.lv
		}).reduce(function (previous, current) {
			return previous + current
		}, 0)
		/** 火力合計 */
		var firePower = toTotalValue(ships, "houg")
		/** 対空合計 */
		var AA = toTotalValue(ships, "tyku", [10, 11, 41])
		/** 対潜合計 */
		var ASW = toTotalValue(ships, "tais", [10, 11, 41])
		/** 索敵合計 */
		var LOS = toTotalValue(ships, "saku", [10, 11, 41])
		/** ドラム缶所持艦合計 */
		var drumShips = toHasItemShipNum(ships, [75])
		/** ドラム缶合計 */
		var drum = toHasItemSum(ships, [75])

		switch (id) {
			case 1: // 練習航海
				return shipNum >= 2
			case 2: // 長距離練習航海
				return flagshipLv >= 2 && shipNum >= 4
			case 3: // 警備任務
				return flagshipLv >= 3 && shipNum >= 3
			case 4: // 対潜警戒任務
				return flagshipLv >= 3 && shipNum >= 3 && hasFleetEscortForce(stypes)
			case 5: // 海上護衛任務
				return flagshipLv >= 3 && shipNum >= 4 && hasFleetEscortForce(stypes)
			case 6: // 防空射撃演習
				return flagshipLv >= 5 && shipNum >= 4
			case 7: // 観艦式予行
				return flagshipLv >= 5 && shipNum >= 6
			case 8: // 観艦式
				return flagshipLv >= 6 && shipNum >= 6
			case 100: // 兵站強化任務
				return flagshipLv >= 15 && shipNum >= 4 && (DE + DD) >= 3
			case 101: // 海峡警備行動
				return flagshipLv >= 20 && shipNum >= 4 && (DE + DD) >= 4 && (AA >= 70 && ASW >= 180 && LOS >= 73) && totalLv >= 144
			case 102: // 長時間対潜警戒
				return flagshipLv >= 35 && shipNum >= 5 && (CL >= 1 && (DE + DD) >= 3 || hasFleetEscortForce(stypes)) && (AA >= 162 && ASW >= 280 && LOS >= 60) && totalLv >= 185
			case 9: // タンカー護衛任務
				return flagshipLv >= 3 && shipNum >= 4 && hasFleetEscortForce(stypes)
			case 10: // 強行偵察任務
				return flagshipLv >= 3 && shipNum >= 3 && CL >= 2
			case 11: // ボーキサイト輸送任務
				return flagshipLv >= 6 && shipNum >= 4 && (DE + DD) >= 2
			case 12: // 資源輸送任務
				return flagshipLv >= 4 && shipNum >= 4 && (DE + DD) >= 2
			case 13: // 鼠輸送作戦
				return flagshipLv >= 5 && shipNum >= 6 && (CL >= 1 && DD >= 4)
			case 14: // 包囲陸戦隊撤収作戦
				return flagshipLv >= 6 && shipNum >= 6 && (CL >= 1 && DD >= 3)
			case 15: // 囮機動部隊支援作戦
				return flagshipLv >= 6 && shipNum >= 6 && ((CV + CVL + ACV + AV) >= 2 && DD >= 2)
			case 16: // 艦隊決戦援護作戦
				return flagshipLv >= 10 && shipNum >= 6 && (CL >= 1 && DD >= 2)
			case 110: // 南西方面航空偵察作戦
				return flagshipLv >= 40 && shipNum >= 6 && (AV >= 1 && CL >= 1 && (DE + DD) >= 2) && (AA >= 200 && ASW >= 200 && LOS >= 140) && totalLv >= 150
			case 111: // 敵泊地強襲反撃作戦
				return flagshipLv >= 50 && shipNum >= 6 && (CA >= 1 && CL >= 1 && DD >= 3) && firePower >= 360
			case 17: // 敵地偵察作戦
				return flagshipLv >= 20 && shipNum >= 6 && (CL >= 1 && DD >= 3)
			case 18: // 航空機輸送作戦
				return flagshipLv >= 15 && shipNum >= 6 && ((CV + CVL + ACV + AV) >= 3 && DD >= 2)
			case 19: // 北号作戦
				return flagshipLv >= 20 && shipNum >= 6 && (CVB >= 2 && DD >= 2)
			case 20: // 潜水艦哨戒任務
				return shipNum >= 2 && ((SS + CVS) >= 1 && CL >= 1)
			case 21: // 北方鼠輸送作戦
				return flagshipLv >= 15 && shipNum >= 5 && (CL >= 1 && DD >= 4) && totalLv >= 30 && drumShips >= 3
			case 22: // 艦隊演習
				return flagshipLv >= 30 && shipNum >= 6 && (CA >= 1 && CL >= 1 && DD >= 2) && totalLv >= 45
			case 23: // 航空戦艦運用演習
				return flagshipLv >= 50 && shipNum >= 6 && (CVB >= 2 && DD >= 2) && totalLv >= 200
			case 24: // 北方航路海上護衛
				return flagshipLv >= 50 && shipNum >= 6 && (flagshipStype === 3 && (DE + DD) >= 4) && totalLv >= 200
			case 25: // 通商破壊作戦
				return flagshipLv >= 25 && shipNum >= 4 && (CA >= 2 && DD >= 2)
			case 26: // 敵母港空襲作戦
				return flagshipLv >= 30 && shipNum >= 4 && ((CV + CVL + ACV + AV) >= 1 && CL >= 1 && DD >= 2)
			case 27: // 潜水艦通商破壊作戦
				return shipNum >= 2 && (SS + CVS) >= 2
			case 28: // 西方海域封鎖作戦
				return flagshipLv >= 30 && shipNum >= 3 && (SS + CVS) >= 3
			case 29: // 潜水艦派遣演習
				return flagshipLv >= 50 && shipNum >= 3 && (SS + CVS) >= 3
			case 30: // 潜水艦派遣作戦
				return flagshipLv >= 55 && shipNum >= 4 && (SS + CVS) >= 4
			case 31: // 海外艦との接触
				return flagshipLv >= 60 && shipNum >= 4 && (SS + CVS) >= 4 && totalLv >= 200
			case 32: // 遠洋練習航海
				return flagshipLv >= 5 && shipNum >= 3 && (flagshipStype === 21 && DD >= 2)
			case 33: // 前衛支援任務
				return shipNum >= 2 && DD >= 2
			case 34: // 艦隊決戦支援任務
				return shipNum >= 2 && DD >= 2
			case 35: // MO作戦
				return flagshipLv >= 40 && shipNum >= 6 && ((CV + CVL + ACV + AV) >= 2 && CA >= 1 && DD >= 1)
			case 36: // 水上機基地建設
				return flagshipLv >= 30 && shipNum >= 6 && (AV >= 2 && CL >= 1 && DD >= 1)
			case 37: // 東京急行
				return flagshipLv >= 50 && shipNum >= 6 && (CL >= 1 && DD >= 5) && totalLv >= 200 && (drumShips >= 3 && drum >= 4)
			case 38: // 東京急行(弐)
				return flagshipLv >= 65 && shipNum >= 6 && DD >= 5 && totalLv >= 240 && (drumShips >= 4 && drum >= 8)
			case 39: // 遠洋潜水艦作戦
				return flagshipLv >= 3 && shipNum >= 5 && (AS >= 1 && (SS + CVS) >= 4) && totalLv >= 180
			case 40: // 水上機前線輸送
				return flagshipLv >= 25 && shipNum >= 6 && (flagshipStype === 3 && AV >= 2 && DD >= 2) && totalLv >= 150
			default:
				return undefined
		}
	}
	return false
}

/**
 * 特定装備の合計数を返します
 *
 * @param {[logbook.dto.ShipDto]} ships 艦娘リスト
 * @param {[Number]} slotitemId 装備IDリスト
 * @return {Number} 特定装備の合計
 */
function toHasItemSum(ships, slotitemId) {
	var s = slotitemId ? slotitemId : []
	return ships.map(function (ship) {
		return toItemList(ship).filter(function (item) {
			return s.indexOf(item.slotitemId) >= 0
		}).length
	}).reduce(function (previous, current) {
		return previous + current
	}, 0)
}

/**
 * 特定装備を積んだ艦の数を返します
 *
 * @param {[logbook.dto.ShipDto]} ships 艦娘リスト
 * @param {[Number]} slotitemId 装備IDリスト
 * @return {Number} 特定装備を積んだ艦の数
 */
function toHasItemShipNum(ships, slotitemId) {
	var s = slotitemId ? slotitemId : []
	return ships.filter(function (ship) {
		return toItemList(ship).some(function (item) {
			return s.indexOf(item.slotitemId) >= 0
		})
	}).length
}

/**
 * パラメータの合計した値を返します
 *
 * @param {[logbook.dto.ShipDto]} ships 艦娘リスト
 * @param {String} kind 集計するパラメータの種別
 * @param {[Number]} exceptionItemCategory 集計から除外する装備カテゴリ
 * @return {Number} パラメータ合計値
 */
function toTotalValue(ships, kind, exceptionItemCategory) {
	var e = exceptionItemCategory ? exceptionItemCategory : []
	return ships.map(function (ship) {
		return ship.param[kind] - toItemList(ship).filter(function (item) {
			return e.indexOf(item.type2) >= 0
		}).map(function (item) {
			return item.param[kind]
		}).reduce(function (previous, current) {
			return previous + current
		}, 0)
	}).reduce(function (previous, current) {
		return previous + current
	}, 0)
}

/**
 * 艦娘の装備を返します
 *
 * @param {logbook.dto.ShipDto} ship 艦娘
 * @reutrn {[logbook.dto.ItemDto]} 装備
 */
function toItemList(ship) {
	var item2 = Java.from(ship.item2)
	item2.push(ship.slotExItem)
	return item2.filter(function (item) {
		return item
	})
}

/**
 * 護衛隊遠征編成を満たすか
 *
 * @param {{Number:[logbook.dto.ShipDto]}} stypes 艦種IDごとに振り分けられた連想配列
 * @return {Boolean} 護衛隊遠征編成か
 */
function hasFleetEscortForce(stypes) {
	var DE = Optional.ofNullable(stypes[1]).orElse([]).length
	var DD = Optional.ofNullable(stypes[2]).orElse([]).length
	var CL = Optional.ofNullable(stypes[3]).orElse([]).length
	var TV = Optional.ofNullable(stypes[21]).orElse([]).length
	var CVE = Optional.ofNullable(Java.from(stypes[7])).orElse([]).filter(function (ship) {
		return ship.max.taisen > 0
	}).length
	return CL >= 1 && DD >= 2 || DD >= 1 && DE >= 3 || TV >= 1 && DE >= 2 || CVE >= 1 && DE >= 2 || CVE >= 1 && DD >= 2
}

function end() {}
