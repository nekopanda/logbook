//Author: Nishisonic
//        Nekopanda
//
// データストアへのアクセス
// グローバル変数 data_prefix を使います
// モジュールの名前などで定義しておいてください

ScriptData = Java.type("logbook.scripting.ScriptData");

// 保存データを読みだす
function getData(key){
	return ScriptData.getData(String(data_prefix + key));
}

// データを保存
// シリアライズできないデータはエラーになります
function setData(key,value){
	ScriptData.setData(String(data_prefix + key),value);
}

// データを一時的に格納
// データは航海日誌終了時に失われますが、シリアライズ可・不可を問いません
// getTmpData()はありません。データの読み出しはgetData()。getTmpData()はありません。
function setTmpData(key,value){
	ScriptData.setData(String(data_prefix + key),value,false);
}
