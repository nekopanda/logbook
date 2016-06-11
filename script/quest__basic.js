load("script/utils.js");

function header() {
	return [ "表示位置", "種", "分類", "状態", "進捗", "タイトル", "内容", "燃料", "弾薬", "鋼材", "ボーキ" ];
}

function begin() { }

function questCategory(category) {
	switch(category){
		case 1:
			return "編成";
		case 2:
			return "出撃";
		case 3:
			return "演習";
		case 4:
			return "遠征";
		case 5:
			return "補給"; // 入渠も含むが文字数の関係
		case 6:
			return "工廠";
		case 7:
			return "改装";
		case 8:
		default:
			return "その他";
	}
}

function questType(type) {
	switch ( type ) {
		case 1:
			return "日";
		case 2:
			return "週";
		case 3:
			return "月";
		case 4:
			return "単";
		case 5:
			return "他";
		default:
			return "1";
	}
}

function body(quest) {
	return toComparable([
					String("" + quest.getPage() + "-" + quest.getPos()),
					questType(parseInt(quest.json.api_type)),
                    questCategory(parseInt(quest.json.api_category)),
                    quest.getStateString(),
                    quest.getProgressString(),
                    quest.getTitle(),
                    quest.getDetail(),
                    quest.getFuel(),
                    quest.getAmmo(),
                    quest.getMetal(),
                    quest.getBauxite()
			]);
}

function end() { }
