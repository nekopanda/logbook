
load("script/utils.js");
load("script/util_missioncheck.js");
load("script/ScriptData.js");

data_prefix = "missioncheck_";

DataType = Java.type("logbook.data.DataType");
GlobalContext = Java.type("logbook.data.context.GlobalContext");

function doCheck() {
	var current = GlobalContext.deckMissions;
	var previous = GlobalContext.previousMissions;
	for(i=0; i<3; ++i) {
		var fleetid = i+2;
		setFleet(fleetid);
		var canCurrent = "";
		var canPrevious = "";
		if(current[i].mission != null) {
			canCurrent = getCanMission(current[i].missionId);
		}
		if(previous[i].mission != null) {
			canPrevious = getCanMission(previous[i].missionId);
		}
		setTmpData(fleetid, canCurrent);
		setTmpData(fleetid + "p", canPrevious);
	}
}

function update(type, data){
	var json = data.getJsonObject();
	switch(type){
		case DataType.PORT:
		case DataType.DECK:
		case DataType.CHANGE:
		case DataType.MISSION_RESULT:
		case DataType.POWERUP:
			doCheck();
			break;
	}
}
