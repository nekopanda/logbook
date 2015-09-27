load("script/utils.js");
ShipParameters = Java.type("logbook.dto.ShipParameters");
ShipStatusString = Java.type("logbook.gui.logic.ShipStatusString");

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

function begin(specdiff_, filter_, specdisp_) {
	specdiff = specdiff_;
	specdisp = specdisp_;
}

/*
  specdisp
	0: 通常
	1: 成長の余地
	2: 装備による上昇値分離
*/

function body(ship) {

	var param = new ShipParameters();
	switch(specdisp) {
	case 0:
	case 1:
		if(specdisp == 1) {
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
	case 2:
		param.add(ship.param);
		param.subtract(ship.slotParam);
		var slotParam = ship.slotParam;
		return toComparable([
						ship.maxhp,
						ship.fuelMax,
						ship.bullMax,
						new ShipStatusString(param.karyoku, slotParam.karyoku),
						new ShipStatusString(param.raisou, slotParam.raisou),
						new ShipStatusString(param.taiku, slotParam.taiku),
						new ShipStatusString(param.soukou, slotParam.soukou),
						new ShipStatusString(param.kaihi, slotParam.kaihi),
						new ShipStatusString(param.taisen, slotParam.taisen),
						new ShipStatusString(param.sakuteki, slotParam.sakuteki),
						new ShipStatusString(param.lucky, slotParam.lucky) ]);
	}

}

function end() { }
