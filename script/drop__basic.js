
ComparableArrayType = Java.type("java.lang.Comparable[]");
AppConstants = Java.type("logbook.constants.AppConstants");

function hasTaihaInFleet(nowhp, maxhp) {
	if ((nowhp == null) || (maxhp == null)) {
		return false;
	}
	for (var i = 0; i < nowhp.length; ++i) {
		var rate = nowhp[i] / maxhp[i];
		if (rate <= AppConstants.BADLY_DAMAGE) {
			return true;
		}
	}
	return false;
}

function header() {
	return ["大破艦", "旗艦", "旗艦(第二艦隊)", "MVP", "MVP(第二艦隊)"];
}

function begin() { }

// 基本的にjavascriptは遅いので注意
// なるべくJavaの型を使って型変換が起こらないようにすべし
// パフォーマンス例
// 56,038件の出撃ログの読み込みにかかった時間(Java 1.8.0_31使用時)
// このスクリプトを使わなかった時: 12,425ms
// javascriptの配列を返した場合: 24,820ms（+12,395ms）
// Javaの配列を返した場合: 14,457ms（+2,032ms）
// javascriptの配列を使うと型変換が必要になってスクリプトの動作速度が5倍以上遅くなる

function body(battle) {
	//var ret = [null,null,null,null,null]; // これをやると遅くなる
	var ret = new ComparableArrayType(5);
	// 大破艦があるか
	var lastPhase = battle.getLastPhase();
	var taiha = (hasTaihaInFleet(lastPhase.getNowFriendHp(), battle.getMaxFriendHp()) ||
			hasTaihaInFleet(lastPhase.getNowFriendHpCombined(), battle.getMaxFriendHpCombined()));
	ret[0] = taiha ? "あり" : null;

	// 旗艦
	ret[1] = battle.getDock().getShips().get(0).getFriendlyName();
	if (battle.isCombined()) {
		ret[2] = battle.getDockCombined()
				.getShips().get(0).getFriendlyName();
	}

	// MVP
	if (battle.getMvp() != -1) { // 敗北Eの時はMVPなし
		ret[3] = battle.getDock().getShips().get(battle.getMvp() - 1).getFriendlyName();
	}
	if (battle.isCombined()) {
		if (battle.getMvpCombined() != -1) { // 敗北Eの時はMVPなし
			ret[4] = battle.getDockCombined().getShips()
					.get(battle.getMvpCombined() - 1).getFriendlyName();
		}
	}
	return ret;
}

function end() { }
