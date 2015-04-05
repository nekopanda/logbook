load("script/utils.js");
StringBuilder = Java.type("java.lang.StringBuilder");

function header() {
	return [ "改修" ];
}

function begin() { }

function body(data) {
	var count = {};
	for each(var item in data.items) {
		if(count[item.level] == null) {
			count[item.level] = 1;
		}
		else {
			count[item.level]++;
		}
	}
	var levels = [];
	for(var key in count) {
		levels.push(key);
	}
	// 降順ソート
	levels.sort(function(a,b) { return -(a - b); });

	if(levels[0] == 0) return null;

	var sb = new StringBuilder();
	for(var i=0; i<levels.length; ++i) {
        if (i != 0) {
            sb.append(",");
        }
		var level = levels[i];
		if(level == 0) {
			sb.append("無x");
		}
		else {
			sb.append("★").append(level).append("x");
		}
		sb.append(count[level]);
	}
	return toComparable([ sb.toString() ]);
}

function end() { }
