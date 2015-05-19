
load("script/utils.js");
load("script/util_missioncheck.js");
load("script/ScriptData.js");

data_prefix = "missioncheck_";

DataType = Java.type("logbook.data.DataType");

function doDeck(data) {
	for(i=0; i<data.size(); ++i) {
		var deck = data.get(i);
		var fleetid = deck.api_id.intValue();
		var canMission = "";
		if(fleetid >= 2) {
			setFleet(fleetid);
			var id = deck.api_mission.get(1).intValue();
			var mills = deck.api_mission.get(2).intValue();
			if(mills > 0) {
				canMission = getCanMission(id);
			}
		}
		setTmpData(fleetid, canMission);
	}
}

function update(type, data){
	var json = data.getJsonObject();
	switch(type){
		case DataType.PORT:
			doDeck(json.api_data.api_deck_port);
			break;
		case DataType.DECK:
			doDeck(json.api_data);
			break;
	}
}
