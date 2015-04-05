load("script/utils.js");

function header() {
	return [ "遠征名" ];
}

function begin(fleetid) { }

function body(data) {
	return toComparable([ data.name ]);
}

function end() { }
