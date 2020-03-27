load("script/utils.js");
HpString = Java.type("logbook.gui.logic.HpString");
SeikuString= Java.type("logbook.gui.logic.SeikuString");
SakutekiString = Java.type("logbook.gui.logic.SakutekiString");

function header() {
	return [	"制空",
				"索敵#索敵2-5計算値", //
				"装備1",
				"艦載機1", //
				"装備2",
				"艦載機2", //
				"装備3",
				"艦載機3", //
				"装備4",
				"艦載機4",
				"装備5",
				"艦載機5",
				"補助" ,
				"補助装備" ];
}

function begin(specdiff) { }

function body(ship) {

	 // 艦載機数
	var slotItems = ship.item2;
	var slotNames = new Array(5);
	var onSlotString = new Array(5);
	var slotExName = (ship.slotExItem != null) ? ship.slotExItem.friendlyName : null;
	var slotEx = ship.hasSlotEx() ? "◎" : "×";
	var onSlot = ship.onSlot;
	var maxEq = ship.shipInfo.maxeq;
	var slotNum = ship.slotNum;
	for (var i = 0; i < slotNum; ++i) {
		var item = slotItems.get(i);
		if (ship.canEquipPlane()) { // 飛行機を装備できる場合だけ
			var cur = (((item != null) && item.isPlane()) ? onSlot[i] : 0);
			var max = (maxEq != null ? maxEq[i] : 0);
			onSlotString[i] = new HpString(cur, max);
		}
		if (item != null) {
			slotNames[i] = item.friendlyName;
		}
	}

	return toComparable([
					new SeikuString(ship),
					new SakutekiString(ship),
					slotNames[0],
					onSlotString[0],
					slotNames[1],
					onSlotString[1],
					slotNames[2],
					onSlotString[2],
					slotNames[3],
					onSlotString[3],
					slotNames[4],
					onSlotString[4],
					slotEx ,
					slotExName ]);
}

function end() { }
