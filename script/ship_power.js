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
//	case 16: // 水上機母艦  ※水上機母艦は砲撃艦のためコメントアウト
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

	// 対潜 = [ 艦船の対潜 ÷ 5 ] + 装備の対潜 × 2 + 対潜基本値(爆雷攻撃艦=25,艦載機運用艦=10) 
	var taisenItem = ship.slotParam.taisen;
	var taisenShip = ship.taisen - taisenItem;
	var taisenBasicPower = 0;
	
	switch (ship.stype) {
	case 2: // 駆逐艦
	case 3: // 軽巡洋艦
	case 21: // 練習巡洋艦
		// 爆雷攻撃艦＝対潜基本値25
		taisenBasicPower = 25;
		break;
	case 6: // 航空巡洋艦
	case 7: // 軽空母
	case 10: // 航空戦艦
	case 16: // 水上機母艦
	case 17: // 揚陸艦
	case 19: // 工作艦
		// 艦載機運用艦＝対潜基本値10
		taisenBasicPower = 10;
		break;
	default:
		taisenBasicPower = 0;
		break;
	}
	
			
	var taisenPower = Math.floor(taisenShip / 5) + (taisenItem * 2) + taisenBasicPower;

	return toComparable([
					ship.slotParam.houm, // 装備命中
					hougekiPower | 0, // 砲撃戦火力
					(ship.raisou + 5) | 0, // 雷撃戦火力
					taisenPower | 0, // 対潜火力
					(ship.karyoku + ship.raisou) | 0, // 夜戦火力
		]);
}

function end() { }
