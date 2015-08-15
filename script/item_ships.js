load("script/utils.js");
StringBuilder = Java.type("java.lang.StringBuilder");

function header() {
	return [ "装備してる艦娘", "余り" ];
}

function begin() { }

function body(data) {
	var slotitemId = data.info.id;

	function ShipItem(ship) {
		// この艦娘が装備している個数を計算
		var levels = [];
		var counts = {};
		var alevels = [];
		var acounts = {};
		var itemlv = [];
		this.count = 0;
		for each(var item in ship.item2) {
			if(item == null) continue;
			if(item.slotitemId == slotitemId) {
				if(item.level in counts) {
					counts[item.level]++;
				}
				else {
					levels.push(item.level);
					counts[item.level] = 1;
				}
				if(item.alv in acounts) {
					acounts[item.alv]++;
				}
				else {
					alevels.push(item.alv);
					acounts[item.alv] = 1;
				}
				this.count++;
				itemlv.push(item.level + item.alv);
			}
		}
		// 降順ソート
		itemlv.sort(function(a,b){ return b - a; });
		levels.sort(function(a,b){ return b - a; });
		alevels.sort(function(a,b){ return b - a; });

		// ソート順作成
		this.sortno = 0;
		for(var i=0; i<5; ++i) {
			this.sortno *= 20;
			if(i < itemlv.length) {
				 this.sortno += itemlv[i] + 1;
			}
		}

		this.output = function (sb) {
			sb.append(ship.friendlyName);
			if(levels[0] > 0) {
				for(var i=0; i<levels.length; ++i) {
					var level = levels[i];
					if(level == 0) {
						sb.append("無x");
					}
					else {
						sb.append("★").append(level).append("x");
					}
					sb.append(counts[level]);
				}
			}
			else if(alevels[0] > 0) {
				for(var i=0; i<alevels.length; ++i) {
					var level = alevels[i];
					if(level == 0) {
						sb.append("無x");
					}
					else {
						sb.append("☆").append(level).append("x");
					}
					sb.append(acounts[level]);
				}
			}
			else {
				sb.append("x").append(this.count | 0);
			}
		}
	}

	var ships = [];
	for each(var ship in data.ships) {
		ships.push(new ShipItem(ship));
	}
	// 降順ソート
	ships.sort(function(a, b) { return -(a.sortno - b.sortno); });

	var count = 0;
	var sb = new StringBuilder();
	for(var i=0; i<ships.length; ++i) {
	        if (i != 0) {
	            sb.append(",");
	        }
	        ships[i].output(sb);
		count += ships[i].count;
	}
	// 長すぎるときは切り詰める
	if (sb.length() > 150) {
	sb.setLength(150);
	sb.append("...");
	}

	var remain = data.items.size() - count;

	return toComparable([ sb.toString(), remain | 0 ]);
}

function end() { }
