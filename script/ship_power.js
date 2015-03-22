load("script/utils.js");
ShipParameters = Java.type("logbook.dto.ShipParameters");

function header() {
	return [	"装備命中",
                "砲撃戦火力",
                "雷撃戦火力",
                "対潜火力",
                "夜戦火力",];
}

function begin(specdiff) { }

function body(ship) {

	var hougekiPower = 0;
    switch (ship.stype) {
    case 7: // 軽空母
    case 11: // 正規空母
    case 16: // 水上機母艦
    case 18: // 装甲空母
        // (火力 + 雷装) × 1.5 + 爆装 × 2 + 55
        var rai = ship.slotParam.raig;
        var baku = ship.slotParam.baku;
        hougekiPower = Math.floor((ship.karyoku + rai) * 1.5) + (baku * 2) + 55;
		break;
    default:
        hougekiPower = ship.karyoku + 5;
		break;
    }

    // 対潜 = [ 艦船の対潜 ÷ 5 ] + 装備の対潜 × 2 + 25
    var taisenItem = ship.slotParam.taisen;
    var taisenShip = ship.taisen - taisenItem;
    var taisenPower = Math.floor(taisenShip / 5) + (taisenItem * 2) + 25;

	return toComparable([
                    ship.slotParam.houm, // 装備命中
                    hougekiPower | 0, // 砲撃戦火力
                    (ship.raisou + 5) | 0, // 雷撃戦火力
					taisenPower | 0, // 対潜火力
                    (ship.karyoku + ship.raisou) | 0, // 夜戦火力
		]);
}

function end() { }
