//Version:1.3.8beta
//Author:Nishisonic

//flg + questNoでbooleanを確認（trueなら任務遂行中）
//cnt + questNoで、カウントを数える
//そしてquest_stateEx.jsで表示するといった感じ

load('nashorn:mozilla_compat.js');
load("script/utils.js");
load("script/ScriptData.js");
data_prefix = "questStateEx_";

Calendar = Java.type("java.util.Calendar");
importPackage(Packages.logbook.data);
ApplicationMain = Java.type("logbook.gui.ApplicationMain");
System = Java.type("java.lang.System");

function update(type, data){
	var json = data.getJsonObject();
	switch(type){
		//任務
		case DataType.QUEST_LIST:
			updateCheck();
			var questLastUpdateTime = Calendar.getInstance();
			if(json.api_data.api_list[0] != null) {
				//仕様変更で無限ループ起こると怖いので（起こってもいいなら↓でも良い）
				//for(var i = 0;parseInt(json.api_data.api_list[i]) == -1;i++){
				for(var i = 0;i < 5;i++){
					if(parseInt(json.api_data.api_list[i]) == -1) break;
					var api_no = json.api_data.api_list[i].api_no.intValue();
					var api_state = json.api_data.api_list[i].api_state.intValue();
					var api_type = json.api_data.api_list[i].api_type.intValue();
					setState(api_no, api_state, api_type);
					var api_progress_flag = json.api_data.api_list[i].api_progress_flag.intValue();
					questCountAdjustment(api_no, api_progress_flag, api_type, api_state);
				}
			}
			setData("questLastUpdateTime",questLastUpdateTime);
			break;
		//戦闘
		case DataType.START:
		case DataType.NEXT:
			setData("mapAreaId",json.api_data.api_maparea_id.intValue());
			setData("mapInfoNo",json.api_data.api_mapinfo_no.intValue());
			setData("eventId",json.api_data.api_event_id.intValue());
			break;
		case DataType.BATTLE_RESULT:
		case DataType.COMBINED_BATTLE_RESULT:
			var getLastBattleDto = Packages.logbook.data.context.GlobalContext.getLastBattleDto();
			var getEnemy = getLastBattleDto.getEnemy();
			var getNowEnemyHp = getLastBattleDto.getNowEnemyHp();
			var getShips = getLastBattleDto.getDock().getShips();
			var getNowFriendHp = getLastBattleDto.getNowFriendHp();

			for(var i=0;i<getEnemy.length;i++){
				if(getNowEnemyHp[i] == 0){
					switch(getEnemy[i].type){
						case "補給艦":
							//敵補給艦を３隻撃沈せよ！
							if(getData("flg218")) setData("cnt218",getData("cnt218") + 1);
							//敵輸送船団を叩け！
							if(getData("flg212")) setData("cnt212",getData("cnt212") + 1);
							//海上通商破壊作戦
							if(getData("flg213")) setData("cnt213",getData("cnt213") + 1);
							//ろ号作戦
							if(getData("flg221")) setData("cnt221",getData("cnt221") + 1);
							break;
						case "軽空母":
						case "正規空母":
							//敵空母を３隻撃沈せよ！
							if(getData("flg211")) setData("cnt211",getData("cnt211") + 1);
							//い号作戦
							if(getData("flg220")) setData("cnt220",getData("cnt220") + 1);
							break;
						case "潜水艦":
							//敵潜水艦を制圧せよ！
							if(getData("flg230")) setData("cnt230",getData("cnt230") + 1);
							//海上護衛戦
							if(getData("flg228")) setData("cnt228",getData("cnt228") + 1);
							break;
						default :
							break;
					}
				}
			}
			//あ号作戦（出撃）
			if(getData("flg214")) setData("cntSally214",getData("cntSally214") + 1);
			//追記したから変な位置に
			//あ号作戦（ボス到達）
			if(getData("eventId") == 5){
				if(getData("flg214")) setData("cntBoss214",getData("cntBoss214") + 1);
			}
			//敵艦隊主力を撃滅せよ！
			if(getData("flg216")) setData("cnt216",getData("cnt216") + 1);
			//敵艦隊を10回邀撃せよ！
			if(getData("flg210")) setData("cnt210",getData("cnt210") + 1);
			var winRank = json.api_data.api_win_rank.toString();
			if(winRank == "S"|| winRank == "A"|| winRank == "B"){
				//あ号作戦（S勝利）
				if(winRank == "S"){
					if(getData("flg214")) setData("cntSWin214",getData("cntSWin214") + 1);
				}
				//敵艦隊を撃滅せよ！
				if(getData("flg201")) setData("cnt201",getData("cnt201") + 1);
				//eventId
				//0=初期位置
				//2=資源
				//3=渦潮
				//4=通常戦闘
				//5=ボス戦闘
				//6=気のせいだった
				//7=航空戦
				//8=船団護衛成功
				if(getData("eventId") == 5){
					//あ号作戦（ボス勝利）
					if(getData("flg214")) setData("cntBossWin214",getData("cntBossWin214") + 1);
					switch(getData("mapAreaId")){
						case 1:
							//「水雷戦隊」南西へ！(Ver1.3.3)
							if(getData("mapInfoNo") == 4 && winRank == "S"){
								var cntCL = 1;
								var cntDD = 0;
								var i; //getShips.length - 1が長いので
								if(getShips.get(0).getType() == "軽巡洋艦"){
									for(i = 1;i < getShips.length;i++){
										if(getShips.get(i).getType() == "駆逐艦"){
											cntDD++;
											continue;
										}
										if(getShips.get(i).getType() == "軽巡洋艦"){
											cntCL++;
											continue;
										}
									}
									//軽巡3隻以下、駆逐1隻以上、軽巡と駆逐のみ
									if(cntCL < 4 && cntDD > 0 && (i - 1) == (cntCL + cntDD)){
										if(getData("flg257")) setData("cnt257",getData("cnt257") + 1);
									}
								}
							}
							if(getData("mapInfoNo") == 5 && winRank != "B"){
								//海上輸送路の安全確保に努めよ！
								if(getData("flg261")) setData("cnt261",getData("cnt261") + 1);
								//海上護衛強化月間
								if(getData("flg265")) setData("cnt265",getData("cnt265") + 1);
							}
							break;
						case 2:
							//南西諸島海域の制海権を握れ！
							if(getData("flg226")) setData("cnt226",getData("cnt226") + 1);
							if(getData("mapInfoNo") == 5 && winRank == "S"){
								var check249 = 0;
								var cntCA = 0;
								var cntCL = 0;
								var cntDD = 0;
								for(var i = 0;i < getShips.length;i++){
									//idの方が良かったかな…？
									//同じ艦を二隻以上入れられない特性を生かす
									if(getShips.get(i).getName().indexOf("妙高") != -1) check249++;
									if(getShips.get(i).getName().indexOf("那智") != -1) check249++;
									if(getShips.get(i).getName().indexOf("羽黒") != -1) check249++;
									if(getShips.get(i).getType() == "重巡洋艦"){
										cntCA++;
										continue;
									}
									if(getShips.get(i).getType() == "軽巡洋艦"){
										cntCL++;
										continue;
									}
									if(getShips.get(i).getType() == "駆逐艦"){
										cntDD++;
										continue;
									}
								}
								//「第五戦隊」出撃せよ！(Ver1.3.3)
								if(check249 == 3){
									if(getData("flg249")) setData("cnt249",getData("cnt249") + 1);
								}
								//「水上反撃部隊」突入せよ！(Ver1.3.3)
								if(cntCA == 1 && cntCL == 1 && cntDD == 4 && getShips.get(0).getType() == "駆逐艦"){
									if(getData("flg266")) setData("cnt266",getData("cnt266") + 1);
								}
							}
							break;
						case 3:
							//敵北方艦隊主力を撃滅せよ！
							if(getData("mapInfoNo") >= 3){
									if(getData("flg241")) setData("cnt241",getData("cnt241") + 1);
							}
							break;
						case 4:
							//「空母機動部隊」西へ！
							if(getData("mapInfoNo") == 2 && winRank == "S"){
								var cntCV = 0;
								var cntDD = 0;
								for(var i = 0;i < getShips.length;i++){
									//idの方が良かったかな…？
									//同じ艦を二隻以上入れられない特性を生かす
									if(getShips.get(i).getType().indexOf("空母") > -1){
										cntCV++;
										continue;
									}
									if(getShips.get(i).getType() == "駆逐艦"){
										cntDD++;
										continue;
									}
								}
								if(cntCV == 2 && cntDD == 2){
									if(getData("flg264")) setData("cnt264",getData("cnt264") + 1);
								}
							}
							//敵東方艦隊を撃滅せよ！
							if(getData("flg229")) setData("cnt229",getData("cnt229") + 1);
							//敵東方中枢艦隊を撃破せよ！
							if(getData("mapInfoNo") == 4){
								if(getData("flg242")) setData("cnt242",getData("cnt242") + 1);
							}
							break;
						case 5:
							//「水上打撃部隊」南方へ！
							if(getData("mapInfoNo") == 1 && winRank == "S"){
								var cntSlowBB = 0;
								var cntCL = 0;
								for(var i = 0;i < getShips.length;i++){
									//stype!=8で高速戦艦を弾く
									//indexOf("戦艦")で戦艦以外を弾く
									//∴低速戦艦だけ残る（べた書きが嫌なだけ）
									if(getShips[i].stype != 8 && getShips.get(i).getType().indexOf("戦艦") > -1){
										cntSlowBB++;
										continue;
									}
									if(getShips.get(i).getType() == "軽巡洋艦"){
										cntCL++;
										continue;
									}
								}
								if(cntSlowBB == 3 && cntCL == 1){
									if(getData("flg259")) setData("cnt259",getData("cnt259") + 1);
								}
							}
							//南方海域珊瑚諸島沖の制空権を握れ！
							if(getData("mapInfoNo") == 2 && winRank == "S"){
								if(getData("flg243")) setData("cnt243",getData("cnt243") + 1);
							}
							break;
						case 6:
							//「潜水艦隊」出撃せよ！
							if(getData("mapInfoNo") == 1 && winRank == "S"){
								if(getData("flg256")) setData("cnt256",getData("cnt256") + 1);
							}
							break;
						default:
							break;
					}
				}
			}
			break;
		//開発
		case DataType.CREATE_ITEM:
			//新装備「開発」指令
			if(getData("flg605")) setData("cnt605",getData("cnt605") + 1);
			//装備「開発」集中強化！
			if(getData("flg607")) setData("cnt607",getData("cnt607") + 1);
			break;
		//建造
		case DataType.CREATE_SHIP:
			//新造艦「建造」指令
			if(getData("flg606")) setData("cnt606",getData("cnt606") + 1);
			//艦娘「建造」艦隊強化！
			if(getData("flg608")) setData("cnt608",getData("cnt608") + 1);
			break;
		//解体
		case DataType.DESTROY_SHIP:
			//軍縮条約対応！
			if(getData("flg609")) setData("cnt609",getData("cnt609") + 1);
			break;
		//廃棄
		case DataType.DESTROY_ITEM2:
			//資源の再利用
			if(getData("flg613")) setData("cnt613",getData("cnt613") + 1);
			break;
		//近代化改修
		case DataType.POWERUP:
			var powerup_flag = json.api_data.api_powerup_flag.intValue();
			if(powerup_flag != 0){
				//艦の「近代化改修」を実施せよ！
				if(getData("flg702")) setData("cnt702",getData("cnt702") + 1);
				//「近代化改修」を進め、戦備を整えよ！
				if(getData("flg703")) setData("cnt703",getData("cnt703") + 1);
			}
			break;
		//遠征（帰還）
		case DataType.MISSION_RESULT:
			//0=失敗、1=成功、2=大成功
			var clear_result = json.api_data.api_clear_result.intValue();
			if(clear_result != 0){
				var quest_name = json.api_data.api_quest_name.toString();

				//「遠征」を3回成功させよう！
				if(getData("flg402")) setData("cnt402",getData("cnt402") + 1);
				//「遠征」を10回成功させよう！
				if(getData("flg403")) setData("cnt403",getData("cnt403") + 1);
				//大規模遠征作戦、発令！
				if(getData("flg404")) setData("cnt404",getData("cnt404") + 1);
				//api_no渡してこないので仕方なく
				if(quest_name.indexOf("東京急行") > - 1){
					//南方への輸送作戦を成功させよ！
					if(getData("flg410")) setData("cnt410",getData("cnt410") + 1);
					//南方への鼠輸送を継続実施せよ!
					if(getData("flg411")) setData("cnt411",getData("cnt411") + 1);
				}
			}
			break;
		//補給
		case DataType.CHARGE:
			//艦隊酒保祭り！
			if(getData("flg504")) setData("cnt504",getData("cnt504") + 1);
			break;
		//入渠開始
		case DataType.NYUKYO_START:
			//艦隊大整備！
			if(getData("flg503")) setData("cnt503",getData("cnt503") + 1);
			break;
		//装備改修
		case DataType.REMODEL_SLOT:
			//装備の改修強化
			if(getData("flg619")) setData("cnt619",getData("cnt619") + 1);
			break;
		//演習
		case DataType.PRACTICE_BATTLE_RESULT:
			//「演習」で練度向上！
			if(getData("flg303")) setData("cnt303",getData("cnt303") + 1);
			var PwinRank = json.api_data.api_win_rank.toString();
			if(PwinRank == "S"|| PwinRank == "A"|| PwinRank == "B"){
				//「演習」で他提督を圧倒せよ！
				if(getData("flg304")) setData("cnt304",getData("cnt304") + 1);
				//大規模演習
				if(getData("flg302")) setData("cnt302",getData("cnt302") + 1);
			}
			break;
		default :
			break;
	}
	//任務一覧の更新
	ApplicationMain.main.getQuestTable().update();
}

function updateCheck() {
	//最初は絶対null取得する…はず（それをフラグにして初期化）
	var questLastUpdateTime = getData("questLastUpdateTime");
	if (questLastUpdateTime != null) {
		var nowTime = Calendar.getInstance();
		//5時間マイナスして、0時に更新したように見せる
		nowTime.add(Calendar.HOUR_OF_DAY, -5);
		questLastUpdateTime.add(Calendar.HOUR_OF_DAY, - 5);
		//maxcountを頻繁に更新するように変更(ver1.3.0)
		initializeMaxCount();
		//デイリー
		updateCheckDairy(questLastUpdateTime, nowTime);
		//一日マイナス（こうすることによって、月曜日判定から日曜日判定に変える）
		nowTime.add(Calendar.DAY_OF_MONTH, - 1);
		questLastUpdateTime.add(Calendar.DAY_OF_MONTH, - 1);
		//ウィークリー
		updateCheckWeekly(questLastUpdateTime, nowTime);
		//元に戻す（こうしないと月の判定がおかしくなる）
		nowTime.add(Calendar.DAY_OF_MONTH, 1);
		questLastUpdateTime.add(Calendar.DAY_OF_MONTH, 1);
		//マンスリー
		updateCheckMonthly(questLastUpdateTime, nowTime);
	} else {
		initializeMaxCount();
		initializeDairyCount();
		initializeWeeklyCount();
		initializeMonthlyCount();
	}
}

//5時以降で更新したら初期化
function initializeDairyCount() {
	var id = [201,216,210,211,218,212,226,230,303,304,402,403,503,504,605,606,607,608,609,619,702]; //デイリーid

	for(var i = 0;i < id.length;i++){
		setData("cnt"+ id[i],0);
		setData("flg"+ id[i],false);
	}
}

function initializeWeeklyCount() {
	var id = [220,213,221,228,229,241,242,243,261,302,404,410,411,703,613]; //ウィークリーid（214は除外）

	for(var i = 0;i < id.length;i++){
		setData("cnt"+ id[i],0);
		setData("flg"+ id[i],false);
	}
	setData("flg214",false);
	setData("cntSally214", 0);
	setData("cntSWin214", 0);
	setData("cntBoss214", 0);
	setData("cntBossWin214", 0);
}

function initializeMonthlyCount() {
	var id = [249,256,257,259,264,265,266]; //マンスリーid

	for (var i = 0; i < id.length;i++) {
		setData("cnt"+ id[i],0);
		setData("flg"+ id[i],false);
	}
}

//任務更新判定（一日）
function updateCheckDairy(questLastUpdateTime, nowTime) {
	if (checkDairy(questLastUpdateTime, nowTime)) {
		initializeDairyCount();
	}
}

//任務更新判定（一週間）
function updateCheckWeekly(questLastUpdateTime, nowTime) {
	if (checkWeekly(questLastUpdateTime, nowTime)) {
		initializeWeeklyCount();
	}
}

//任務更新判定（一か月）
function updateCheckMonthly(questLastUpdateTime, nowTime) {
	if (checkMonthly(questLastUpdateTime, nowTime)) {
		initializeMonthlyCount();
	}
}

//trueなら実行

function checkDairy(questLastUpdateTime, nowTime) {
	//同じ日じゃないならtrue
	if(nowTime.get(Calendar.DAY_OF_YEAR) != questLastUpdateTime.get(Calendar.DAY_OF_YEAR)) return true;
	return false;
}

/* 判定方法変更:1.3.7beta */
function checkWeekly(questLastUpdateTime, nowTime){
	//同じ週じゃないならtrue
	if (nowTime.get(Calendar.WEEK_OF_YEAR) != questLastUpdateTime.get(Calendar.WEEK_OF_YEAR)) return true;
	return false;
}

/* 判定方法変更:1.3.7beta */
function checkMonthly(questLastUpdateTime, nowTime) {
	//同じ月じゃないならtrue
	if (nowTime.get(Calendar.MONTH) != questLastUpdateTime.get(Calendar.MONTH)) return true;
	return false;
}

//questType
//1=1回限り
//2=デイリー
//3=ウィークリー
//4=敵空母を３隻撃沈せよ!(日付下一桁0|3|7)
//5=敵輸送船団を叩け!(日付下一桁2|8)
//6=マンスリー

//questState
//1=未受注
//2=遂行中
//3=達成

function setState(questNo ,questState, questType) {
	if (questType != 1) { //1回限りは除外（そんな影響ないけど）
		setData("flg"+ questNo,questState == 2);
	}
}

//地獄のべた書き
//api_noはAndanteさんのソースとスレの情報を参考にしています
function initializeMaxCount(){
	/* デイリー */
	//敵艦隊を撃滅せよ！
	setData("max201",1);
	//敵艦隊主力を撃滅せよ！
	setData("max216",1);
	//敵艦隊を10回邀撃せよ！
	setData("max210",10);
	//敵空母を３隻撃沈せよ！
	setData("max211",3);
	//敵補給艦を３隻撃沈せよ！
	setData("max218",3);
	//敵輸送船団を叩け！
	setData("max212",5);
	//南西諸島海域の制海権を握れ！
	setData("max226",5);
	//敵潜水艦を制圧せよ！
	setData("max230",6);
	//「演習」で練度向上！
	setData("max303",3);
	//「演習」で他提督を圧倒せよ！
	setData("max304",5);
	//「遠征」を3回成功させよう！
	setData("max402",3);
	//「遠征」を10回成功させよう！
	setData("max403",10);
	//艦隊大整備！
	setData("max503",5);
	//艦隊酒保祭り！
	setData("max504",15);
	//新装備「開発」指令
	setData("max605",1);
	//新造艦「建造」指令
	setData("max606",1);
	//装備「開発」集中強化！
	setData("max607",3);
	//艦娘「建造」艦隊強化！
	setData("max608",3);
	//軍縮条約対応！
	setData("max609",2);
	//装備の改修強化
	setData("max619",1);
	//艦の「近代化改修」を実施せよ！
	setData("max702",2);
	/* ウィークリー */
	//あ号作戦
	setData("maxSally214",36);
	setData("maxSWin214",6);
	setData("maxBoss214",24);
	setData("maxBossWin214",12);
	//い号作戦
	setData("max220",20);
	//海上通商破壊作戦
	setData("max213",20);
	//ろ号作戦
	setData("max221",50);
	//海上護衛戦
	setData("max228",15);
	//敵東方艦隊を撃滅せよ！
	setData("max229",12);
	//敵北方艦隊主力を撃滅せよ！
	setData("max241",5);
	//敵東方中枢艦隊を撃破せよ！
	setData("max242",1);
	//南方海域珊瑚諸島沖の制空権を握れ！
	setData("max243",2);
	//海上輸送路の安全確保に努めよ！
	setData("max261",3);
	//大規模演習
	setData("max302",20);
	//大規模遠征作戦、発令！
	setData("max404",30);
	//南方への輸送作戦を成功させよ！
	setData("max410",1);
	//南方への鼠輸送を継続実施せよ!
	setData("max411",6);
	//「近代化改修」を進め、戦備を整えよ！
	setData("max703",15);
	//資源の再利用
	setData("max613",24);
	/* マンスリー */
	//「第五戦隊」出撃せよ！
	setData("max249",1);
	//「潜水艦隊」出撃せよ！
	setData("max256",3);
	//「水雷戦隊」南西へ！
	setData("max257",1);
	//「水上打撃部隊」南方へ！
	setData("max259",1);
	//「空母機動部隊」西へ！(ver1.2.7)
	setData("max264",1);
	//海上護衛強化月間(ver1.2.7)
	setData("max265",10);
	//「水上反撃部隊」突入せよ！
	setData("max266",1);
}

function questCountAdjustment(questNo, questProgressFlag, questType, questState){
	//1回限りとあ号作戦を除去
	//開発系も多少数がおかしくなるので除去（というより対策方法がない） Ver.1.3.8追記
	if(questType != 1 && !(questNo == 214 || questNo == 605 || questNo == 606 || questNo == 607 || questNo == 608)){
		switch(questProgressFlag){
			case 1: //50%
				//カウンタが50%を下回ってるのに、「50%以上」表示になっていたら
				if(getData("cnt" + questNo) < Math.ceil(getData("max" + questNo) * 0.5)){
					//maxの値を半分にして切り上げ
					setData("cnt" + questNo,Math.ceil(getData("max" + questNo) * 0.5));
				//カウンタが80%を超えているのに、「50%以上」表示になっていたら
				} else if(getData("cnt" + questNo) > Math.ceil(getData("max" + questNo) * 0.8)){
					//maxの値を80%したやつを-1する
					setData("cnt" + questNo,Math.ceil(getData("max" + questNo) * 0.8) - 1);
				}
				break;
			case 2: //80%
				//カウンタが80%を下回ってるのに、「80%以上」表示になっていたら
				if(getData("cnt" + questNo) < Math.ceil(getData("max" + questNo) * 0.8)){
					//maxの値を80%にして切り上げ
					setData("cnt" + questNo,Math.ceil(getData("max" + questNo) * 0.8));
				//カウンタが100%を超えたのに、「80%以上」表示になっていたら
				} else if(getData("cnt" + questNo) >= getData("max" + questNo)){
					//maxの値を-1
					setData("cnt" + questNo,getData("max" + questNo) - 1);
				}
				break;
			default : //それ以外
				switch(questState){
					//未受注
					case 1:
					//遂行中
					case 2:
						//カウンタが50%を超えたのに「50%以上」とかの表示がなかったら
						if(getData("cnt" + questNo) >= Math.ceil(getData("max" + questNo) * 0.5)){
							//maxの値を半分にしたやつを-1する
							setData("cnt" + questNo,Math.ceil(getData("max" + questNo) * 0.5) - 1);
						}
						break;
					//達成
					case 3:
						//maxの値に合わせる
						setData("cnt" + questNo,getData("max" + questNo));
						break;
					default :
						break;
				}
				break;
		}
	}
}
