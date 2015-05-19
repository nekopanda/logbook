//Ver1.0.2
//Author: twk@2ch

load("script/utils.js");
load("script/util_missioncheck.js");

function header() {
	return [ "成功" ];
}

function begin(fleetid) { 
	setFleet(fleetid);
}

function body(data) {
	return toComparable([ getCanMission(data.id) ]);
}

function end() { }
