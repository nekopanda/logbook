load("script/utils.js");

function header() {
	return [ "表示位置", "状態", "進捗", "タイトル", "内容", "燃料", "弾薬", "鋼材", "ボーキ" ];
}

function begin() { }

function body(quest) {
	return toComparable([
					String("" + quest.getPage() + "-" + quest.getPos()),
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
