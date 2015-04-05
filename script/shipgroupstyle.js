load("script/utils.js");

TableItem = Java.type("org.eclipse.swt.widgets.TableItem");
SWT = Java.type("org.eclipse.swt.SWT");
SWTResourceManager = Java.type("org.eclipse.wb.swt.SWTResourceManager");

GlobalContext = Java.type("logbook.data.context.GlobalContext");
AppConstants = Java.type("logbook.constants.AppConstants");
ReportUtils = Java.type("logbook.util.ReportUtils");

var condIndex = 12;
function begin(header) {
    missionShips = GlobalContext.getMissionShipSet();
    ndockShips = GlobalContext.getNDockShipSet();
    for (var i = 1; i < header.length; ++i) {
        if (header[i].equals("疲労")) {
            condIndex = i;
            break;
        }
    }
}

function getTableCondColor(cond) {
    for (var i = 0; i < AppConstants.COND_TABLE_COLOR.length; ++i) {
        if (cond >= AppConstants.COND_TABLE[i]) {
            return SWTResourceManager.getColor(AppConstants.COND_TABLE_COLOR[i]);
        }
    }
    // 0より小さいってあり得ないけど
    return SWTResourceManager.getColor(AppConstants.COND_RED_COLOR);
}

function create(table, data, index) {
    // 艦娘
    var ship = data[0].get();

    var item = new TableItem(table, SWT.NONE);

    item.setData(ship);

    // 偶数行に背景色を付ける
    if ((index % 2) != 0) {
        item.setBackground(SWTResourceManager.getColor(AppConstants.ROW_BACKGROUND));
    }

    // 疲労
    item.setBackground(condIndex, getTableCondColor(ship.cond));

    // 遠征
    if (missionShips.contains(ship.id)) {
        item.setForeground(SWTResourceManager.getColor(AppConstants.MISSION_COLOR));
    }
    // 入渠
    if (ndockShips.contains(ship.id)) {
        item.setForeground(SWTResourceManager.getColor(AppConstants.NDOCK_COLOR));
    }
    // Lv1の艦娘をグレー色にする
    /* ちょっとこれをやる意味がよく分からないのでコメントアウト
    if (ship.getLv() == 1) {
        item.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_GRAY));
    }
    */

    item.setText(ReportUtils.toStringArray(data));

    return item;
}

function end() { }
