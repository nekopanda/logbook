load("script/utils.js");
StringBuilder = Java.type("java.lang.StringBuilder");

function header() {
	return [ "改修・熟練#改修" ];
}

function begin() { }

function levelsToString(levels, count, mark, sb) {
	if(levels[0] == 0) return;

	for(var i=0; i<levels.length; ++i) {
	        if (i != 0) {
	            sb.append(",");
	        }
		var level = levels[i];
		if(level == 0) {
			sb.append("無x");
		}
		else {
			sb.append(mark).append(level).append("x");
		}
		sb.append(count[level]);
	}
}

function body(data) {
	var count = {};
	var acount = {};
	for each(var item in data.items) {
		if(count[item.level] == null) {
			count[item.level] = 1;
		}
		else {
			count[item.level]++;
		}
		if(acount[item.alv] == null) {
			acount[item.alv] = 1;
		}
		else {
			acount[item.alv]++;
		}
	}
	var levels = [];
	var alevels = [];
	for(var key in count) {
		levels.push(key);
	}
	for(var key in acount) {
		alevels.push(key);
	}
	// 降順ソート
	levels.sort(function(a,b) { return -(a - b); });
	alevels.sort(function(a,b) { return -(a - b); });

	var sb = new StringBuilder();
	levelsToString(levels, count, "★", sb);
	levelsToString(alevels, acount, "☆", sb);

	return toComparable([ sb.toString() ]);
}

function end() { }
