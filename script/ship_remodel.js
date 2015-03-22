load("script/utils.js");

function header() {
	return [ "改造可能" ];
}

function begin(specdiff) { }

function body(ship) {

	var afterlv = ship.shipInfo.afterlv;
	var canRemodel = (afterlv > 0) && (ship.lv >= afterlv);

	return toComparable([ canRemodel ? "可能" : null ]);
}

function end() { }
