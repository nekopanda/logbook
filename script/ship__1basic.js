load("script/utils.js");
GlobalContext = Java.type("logbook.data.context.GlobalContext");
ShipOrder = Java.type("logbook.gui.logic.ShipOrder");
IntegerPair = Java.type("logbook.gui.logic.IntegerPair");
TimeString = Java.type("logbook.gui.logic.TimeString");
HpString = Java.type("logbook.gui.logic.HpString");
TimeLogic = Java.type("logbook.gui.logic.TimeLogic");

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
                "艦ID",//
                "現在",//
                "疲労",
                "回復",
                "HP", //
                "燃料",//
                "弾薬",//
                "修理時間",//
                "修理燃料",//
                "修理鋼材",//
                "損傷",//
                "HP1あたり", //
                "Lv",
                "Next",
                "経験値"];
}

function begin(specdiff) {
	var ships = ShipOrder.getOrderedShipList();
	ordermap = {};
	for each (var ship in ships) {
		ordermap[ship.ship.getId()] = ship.sortNumber;
	}
    missionShips = GlobalContext.getMissionShipSet();
    ndockShips = GlobalContext.getNDockShipSet();
}

function getPageNumber(index) {
	return new IntegerPair((index / 10) + 1, (index % 10) + 1, "-");
}

function body(ship) {
	var order = ordermap[ship.id]

    var fleet = null;
    if (ship.isFleetMember()) {
		// String同士を足した結果はStringにならないので明示的にStringにする必要がある
        fleet = String(ship.fleetid + "-" + (ship.fleetpos + 1));
    }

    var now = null;
    if (missionShips.contains(ship.id)) {
        now = "遠征中";
    }
    else if (ndockShips.contains(ship.id)) {
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
                    ship.charId,
                    now,
                    ship.cond,
                    (ship.cond < 49) ? new TimeString(ship.condClearTime.time) : null,
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
                    ship.exp]);
}

function end() { }
