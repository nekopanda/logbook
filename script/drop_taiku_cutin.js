load("script/utils.js");


function header() {
	return ["対空カットイン発動艦娘", "対空カットイン種別"];
}

function begin() { }

function getCutinName(kind) {
	switch(kind) {
		case 1: return "高角砲x2/電探";
		case 2: return "高角砲/電探";
		case 3: return "高角砲x2";
		case 4: return "大口径主砲/三式弾/高射装置/電探";
		case 5: return "高角砲+高射装置x2/電探";
		case 6: return "大口径主砲/三式弾/高射装置";
		case 7: return "高角砲/高射装置/電探";
		case 8: return "高角砲+高射装置/電探";
		case 9: return "高角砲/高射装置";
		default: return "知らないカットインです";
	}
}

function body(battle) {
	var shipName = null;
	var kindName = null;
	var p1json = battle.getPhase1().getJson();
	if( p1json != null &&
	    p1json.api_kouku != null &&
		p1json.api_kouku.api_stage2 != null &&
		p1json.api_kouku.api_stage2.api_air_fire != null)
	{
		var air_fire = p1json.api_kouku.api_stage2.api_air_fire;
		var idx = air_fire.api_idx.intValue();
		shipName = (idx >= 6)
			? battle.getDockCombined().getShips().get(idx - 6).getFriendlyName()
			: battle.getDock().getShips().get(idx).getFriendlyName();
		kindName = getCutinName(air_fire.api_kind.intValue());
	}
	return toComparable([shipName, kindName]);
}

function end() { }
