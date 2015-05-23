load("script/utils.js");

function header() {
	return [ "出撃海域" ];
}

function begin(specdiff) { }

function sallyArea(area) {
	switch(area) {
		case 0: return null;
		case 1: return "通商破壊";
		case 2: return "攻略作戦";
		case 3: return "泊地攻撃";
		default: return "不明";
	}
}

function body(ship) {
	if(ship.json == null || isJsonNull(ship.json.api_sally_area))
		return null;
	return toComparable([ sallyArea(ship.json.api_sally_area.intValue()) ]);
}

function end() { }
