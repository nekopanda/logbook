load("script/utils.js");

function header() {
	return [ "名称", "種別", "個数", "施錠" ];
}

function begin() { }

function body(data) {

	var locked = 0
	for each (var item in data.items) {
		if(item.locked) {
			++locked;
		}
	}

	return toComparable([
					data.info.name,
					data.info.typeName,
					data.items.size(),
					locked | 0 ]);
}

function end() { }
