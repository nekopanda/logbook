load("script/utils.js");

function header() {
	return [ "出撃海域" ];
}

function begin(specdiff) { }

function sallyArea(area) {
	switch(area) {
		case 0: return null;
		case 1: return "札A";
		case 2: return "札B";
		case 3: return "札C";
		case 4: return "札D";
		default: return "不明";
	}
}

function body(ship) {
	if(ship.json == null || isJsonNull(ship.json.api_sally_area))
		return null;
	return toComparable([ sallyArea(ship.json.api_sally_area.intValue()) ]);
}

function end() { }
