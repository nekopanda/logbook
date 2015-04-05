
ComparableArrayType = Java.type("java.lang.Comparable[]");

// javascriptの配列をそのまま返すと遅いので
// Comparable[]に変換しておく
// undefinedはnullに変換される
function toComparable(raw) {
	var ret = new ComparableArrayType(raw.length);
	for(var i=0; i<raw.length; ++i) {
		if(raw[i] == null) {
			ret[i] = null;
		}
		else {
			ret[i] = raw[i];
		}
	}
	return ret;
}
