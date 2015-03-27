load("script/utils.js");

function header() {
	return [ "火力", "命中", "射程", "運", "回避", "爆装", "雷装", "索敵", "対潜", "対空", "装甲" ];
}

function begin() { }

function body(data) {
	var param = data.info.param;
	return toComparable([
					param.houg, param.houm, param.leng, param.luck,
					param.houk, param.baku, param.raig, param.saku,
					param.tais, param.tyku, param.souk ]);
}

function end() { }
