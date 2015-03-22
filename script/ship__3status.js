load("script/utils.js");
ShipParameters = Java.type("logbook.dto.ShipParameters");

function header() {
	return [	"耐久",
				"燃料",//
				"弾薬",//
				"火力",
				"雷装",
				"対空",
				"装甲",
				"回避",
				"対潜",
				"索敵",
				"運"];
}

function begin(specdiff_) {
	specdiff = specdiff_;
}

function body(ship) {

	var param = new ShipParameters();
	if (specdiff) {
		// 成長の余地 = (装備なしのMAX) + (装備による上昇分) - (装備込の現在値)
		param.add(ship.max);
		param.add(ship.slotParam);
		param.subtract(ship.param);
	}
	else {
		param.add(ship.param);
	}

	return toComparable([
					ship.maxhp,
					ship.fuelMax,
					ship.bullMax,
					param.karyoku,
					param.raisou,
					param.taiku,
					param.soukou,
					param.kaihi,
					param.taisen,
					param.sakuteki,
					param.lucky ]);
}

function end() { }
