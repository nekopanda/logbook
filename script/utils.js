
ComparableArrayType = Java.type("java.lang.Comparable[]");
JsonValue = Java.type("javax.json.JsonValue");

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

// メッセージボックス表示
function alert(str) {
	SWT = Java.type("org.eclipse.swt.SWT");
	MessageBox = Java.type("org.eclipse.swt.widgets.MessageBox");
	Display = Java.type("org.eclipse.swt.widgets.Display");
	var shell = Display.getDefault().getActiveShell();
	var box = new MessageBox(shell,SWT.OK);
	box.setMessage(String(str));
	box.open();
}

function isJsonNull(jsonval) {
	return (jsonval == null) || (jsonval == JsonValue.NULL);
}
