load("script/utils.js");
GlobalContext = Java.type("logbook.data.context.GlobalContext");
ShipOrder = Java.type("logbook.gui.logic.ShipOrder");
IntegerPair = Java.type("logbook.gui.logic.IntegerPair");
TimeString = Java.type("logbook.gui.logic.TimeString");
HpString = Java.type("logbook.gui.logic.HpString");
TimeLogic = Java.type("logbook.gui.logic.TimeLogic");
Ship = Java.type("logbook.internal.Ship");

function header() {
	return [	"ID",
				"鍵",//
				"艦隊",
				"Lv順", //
				"艦種順", //
				"NEW順", //
				"修理順", //
				"名前",
				"艦種",
				"初期艦",//
				"現在",//
				"疲労",
				"回復",
				"HP", //
				"燃料#現在の燃料",//
				"弾薬#現在の弾薬",//
				"修理時間",//
				"燃料#修理に必要な燃料",//
				"鋼材#修理に必要な鋼材",//
				"損傷",//
				"HP1あたり", //
				"Lv",
				"Next",
				"経験値",
				"速力"];
}

function begin(specdiff) {
	var ships = ShipOrder.getOrderedShipList();
	ordermap = {};
	for each (var ship in ships) {
		ordermap[ship.ship.getId()] = ship.sortNumber;
	}
	missionShips = GlobalContext.getMissionShipSet();
	ndockMap = GlobalContext.getNDockCompleteTimeMap();
}

function getPageNumber(index) {
	return new IntegerPair((index / 10) + 1, (index % 10) + 1, "%d-%d");
}

function getSokuryoku(soku) {
	switch(soku) {
		case 0: return "陸上";
		case 5: return "低速";
		case 10: return "高速";
		default: return "不明";
	}
}

function body(ship) {
	var order = ordermap[ship.id]

	var fleet = null;
	if (ship.isFleetMember()) {
		// String同士を足した結果はStringにならないので明示的にStringにする必要がある
		fleet = String(ship.fleetid + "-" + (ship.fleetpos + 1));
	}

	var origName = null;
	if(ship.charId != 0) {
		var origShip = Ship.get(ship.charId);
		if(origShip != null) {
			origName = origShip.name;
		}
	}

	var now = null;
	if (missionShips.contains(ship.id)) {
		now = "遠征中";
	}
	else if (ndockMap.containsKey(ship.id)) {
		now = "入渠中";
	}

	var dockTime = ship.docktime;
	// HP1あたりの時間
	var unitSeconds = dockTime / (ship.maxhp - ship.nowhp) / 1000;

	// 損傷
	var damage = null;
	if (ship.isBadlyDamage()) {
		damage = "大破";
	} else if (ship.isHalfDamage()) {
		damage = "中破";
	} else if (ship.isSlightDamage()) {
		damage = "小破";
	}

	var condClearTime = ship.getCondClearTime(GlobalContext.getCondTiming(), ndockMap.get(ship.id));

	return toComparable([
					ship.id,
					ship.locked ? "♥" : "",
					fleet,
					getPageNumber(order[0]),
					getPageNumber(order[1]),
					getPageNumber(order[2]),
					getPageNumber(order[3]),
					ship.name,
					ship.type,
					origName,
					now,
					ship.cond,
					(ship.cond < 49) ? new TimeString(condClearTime) : null,
					new HpString(ship.nowhp, ship.maxhp),
					new HpString(ship.fuel, ship.fuelMax),
					new HpString(ship.bull, ship.bullMax),
					dockTime > 0 ? new TimeLogic(ship.docktime) : null,
					dockTime > 0 ? ship.dockfuel : null,
					dockTime > 0 ? ship.dockmetal : null,
					damage,
					dockTime > 0 ? TimeLogic.fromSeconds(unitSeconds) : null,
					ship.lv,
					ship.next,
					ship.exp,
					getSokuryoku(ship.param.soku)]);
}

function end() { }
