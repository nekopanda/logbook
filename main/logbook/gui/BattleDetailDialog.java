/**
 * 
 */
package logbook.gui;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import logbook.constants.AppConstants;
import logbook.dto.AirBattleDto;
import logbook.dto.BattleAtackDto;
import logbook.dto.BattleExDto;
import logbook.dto.BattleExDto.Phase;
import logbook.dto.BattleResultDto;
import logbook.dto.ItemDto;
import logbook.dto.ShipBaseDto;
import logbook.dto.ShipDto;
import logbook.gui.logic.SakutekiString;
import logbook.internal.BattleResultServer;
import logbook.internal.Item;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Nekopanda
 *
 */
public class BattleDetailDialog extends WindowBase {
    /** ロガー */
    private static final Logger LOG = LogManager.getLogger(BattleResultServer.class);

    private BattleResultDto result;
    private BattleExDto detail;
    private Browser browser;
    private String currentHTML;

    private static class HTMLGenerator {
        private final StringBuilder sb = new StringBuilder();
        private final List<String> tagStack = new ArrayList<String>();
        private int nestedCount = 0;

        private static String TAB = "    ";

        public void genHeader(String title) throws IOException {
            this.sb.append("<!DOCTYPE html>").append("\r\n");
            this.sb.append("<html>").append("\r\n");
            this.sb.append("<head>").append("\r\n");
            this.sb.append("<meta charset=\"UTF-8\">").append("\r\n");
            this.sb.append("<title>").append(title).append("</title>").append("\r\n");
            this.sb.append("<style type=\"text/css\">").append("\r\n");
            if (AppConstants.BATTLE_LOG_CSS_FILE.exists()) {
                String css = IOUtils.toString(new FileInputStream(AppConstants.BATTLE_LOG_CSS_FILE));
                this.sb.append(css);
            }
            this.sb.append("</style>").append("\r\n");
            this.sb.append("</head>").append("\r\n");
        }

        public String result() {
            return this.sb.toString();
        }

        private void genIndent() {
            for (int i = 0; i < this.nestedCount; ++i) {
                this.sb.append(TAB);
            }
        }

        private void genClass(String[] cls) {
            if ((cls != null) && (cls.length > 0)) {
                this.sb.append(" class=\"");
                for (int i = 0; i < cls.length; ++i) {
                    if (i > 0) {
                        this.sb.append(" ");
                    }
                    this.sb.append(cls[i]);
                }
                this.sb.append("\"");
            }
        }

        public void begin(String tag, String[] cls) {
            this.tagStack.add(tag);
            this.genIndent();
            this.nestedCount++;
            this.sb.append("<").append(tag);
            this.genClass(cls);
            this.sb.append(">").append("\r\n");
        }

        public void end() {
            this.nestedCount--;
            this.genIndent();
            int tagIndex = this.tagStack.size() - 1;
            this.sb.append("</").append(this.tagStack.get(tagIndex)).append(">").append("\r\n");
            this.tagStack.remove(tagIndex);
        }

        public void inline(String tag, String option, String innerText, String[] cls) {
            this.genIndent();
            this.sb.append("<").append(tag).append(" ").append(option);
            this.genClass(cls);
            this.sb.append(">");
            if (innerText != null) {
                this.sb.append(innerText);
            }
            this.sb.append("</").append(tag).append(">").append("\r\n");
        }

        public void inline(String tag, String innerText, String[] cls) {
            this.genIndent();
            this.sb.append("<").append(tag);
            this.genClass(cls);
            this.sb.append(">");
            if (innerText != null) {
                this.sb.append(innerText);
            }
            this.sb.append("</").append(tag).append(">").append("\r\n");
        }

        public void inline(String tag, String[] cls) {
            this.genIndent();
            this.sb.append("<").append(tag);
            this.genClass(cls);
            this.sb.append(">").append("\r\n");
        }
    }

    public BattleDetailDialog(WindowBase parent) {
        super.createContents(parent, SWT.CLOSE | SWT.TITLE | SWT.MIN | SWT.RESIZE, false);
    }

    /**
     * Open the dialog.
     * @return the result
     */
    @Override
    public void open() {
        if (!this.isWindowInitialized()) {
            this.createContents();
            super.registerEvents();
            // 閉じたときに dispose しない
            this.getShell().addShellListener(new ShellAdapter() {
                @Override
                public void shellClosed(ShellEvent e) {
                    e.doit = false;
                    BattleDetailDialog.this.setVisible(false);
                }
            });
            this.setWindowInitialized(true);
        }
        this.setVisible(true);
        this.getShell().setActive();
    }

    /**
     * Create contents of the dialog.
     */
    private void createContents() {
        Shell shell = this.getShell();
        shell.setLayout(new FillLayout(SWT.VERTICAL));
        this.browser = new Browser(shell, SWT.NONE);
    }

    public void setBattle(BattleResultDto result, BattleExDto detail) {
        Shell shell = this.getShell();
        String title = "会敵報告: " + result.getMapCell().detailedString();
        shell.setText(title);
        try {
            this.currentHTML = generateHTML(title, result, detail);
            this.browser.setText(this.currentHTML);
            this.result = result;
            this.detail = detail;
        } catch (IOException e) {
            LOG.warn("会敵報告作成に失敗: CSSファイル読み込みに失敗しました", e);
        }
    }

    private static String getColSpan(int span) {
        return "colspan\"" + span + "\"";
    }

    private static String getRowSpan(int span) {
        return "rowspan\"" + span + "\"";
    }

    /**
     * パラメータテーブルを生成
     * @param gen
     * @param tableTitle
     * @param ships
     * @param hp
     * @param phaseName
     */
    private static <SHIP extends ShipBaseDto>
            void genParmeters(HTMLGenerator gen, String tableTitle,
                    List<SHIP> ships, int[][] hp, String[] phaseName)
    {
        int numPhases = hp.length / 2;

        gen.begin("table", null);
        gen.inline("caption", tableTitle, new String[] { "param-caption" });

        gen.begin("tr", null);

        gen.inline("th", "", null);
        gen.inline("th", "名前", null);
        gen.inline("th", "cond.", null);
        gen.inline("th", "制空", null);
        gen.inline("th", "索敵", null);
        gen.inline("th", "開始時HP", null);
        gen.inline("th", "", null);
        for (int i = 0; i < numPhases; ++i) {
            gen.inline("th", getRowSpan(2), phaseName[0], null);
            gen.inline("th", "", null);
        }

        gen.inline("th", "火力", null);
        gen.inline("th", "雷装", null);
        gen.inline("th", "対空", null);
        gen.inline("th", "装甲", null);
        gen.inline("th", "回避", null);
        gen.inline("th", "対潜", null);
        gen.inline("th", "索敵", null);
        gen.inline("th", "運", null);
        gen.inline("th", "速力", null);
        gen.inline("th", "射程", null);

        gen.end(); // tr

        int totalSeiku = 0;
        double totalSakuteki = 0;
        int totalNowHp = 0;
        int totalMaxHp = 0;

        for (int i = 0; i < ships.size(); ++i) {
            SHIP ship = ships.get(i);
            int seiku = ship.getSeiku();
            SakutekiString sakuteki = new SakutekiString(ship);
            int nowhp = hp[0][i];
            int maxhp = hp[1][i];

            totalSeiku += seiku;
            totalSakuteki += sakuteki.getValue();
            totalNowHp += nowhp;
            totalMaxHp += maxhp;

            gen.begin("tr", null);

            gen.inline("td", String.valueOf(i + 1), null);
            gen.inline("td", ship.getFriendlyName(), null);

            if (ship instanceof ShipDto) {
                gen.inline("td", String.valueOf(((ShipDto) ship).getCond()), null);
            }
            else {
                gen.inline("td", "", null);
            }

            gen.inline("td", String.valueOf(seiku), null);
            gen.inline("td", sakuteki.toString(), null);
            gen.inline("td", nowhp + "/" + maxhp, null);

            for (int p = 0; p < numPhases; ++p) {
                if (i == 0) {
                    gen.inline("td", getColSpan(ships.size()), "→", null);
                }
                int dam = hp[(p * 2) + 0][i];
                int remain = hp[(p * 2) + 1][i];
                gen.inline("td", String.valueOf(dam), null);
                gen.inline("td", String.valueOf(remain), null);
            }

            gen.inline("td", String.valueOf(i + 1), null);

            gen.inline("td", String.valueOf(ship.getKaryoku()), null);
            gen.inline("td", String.valueOf(ship.getRaisou()), null);
            gen.inline("td", String.valueOf(ship.getTaiku()), null);
            gen.inline("td", String.valueOf(ship.getSoukou()), null);
            gen.inline("td", String.valueOf(ship.getKaihi()), null);
            gen.inline("td", String.valueOf(ship.getTaisen()), null);
            gen.inline("td", String.valueOf(ship.getSakuteki()), null);
            gen.inline("td", String.valueOf(ship.getLucky()), null);
            gen.inline("td", String.valueOf(ship.getParam().getSoku()), null);
            gen.inline("td", String.valueOf(ship.getParam().getLeng()), null);

            gen.end(); // tr
        }

        gen.begin("tr", null);

        gen.inline("td", "", null);
        gen.inline("td", "合計", null);
        gen.inline("td", "", null);
        gen.inline("td", String.valueOf(totalSeiku), null);
        gen.inline("td", String.valueOf(totalSakuteki), null);
        gen.inline("td", totalNowHp + "/" + totalMaxHp, null);

        for (int p = 0; p < numPhases; ++p) {
            gen.inline("td", "→", null);
            gen.inline("td", getRowSpan(2), "", null);
        }

        gen.inline("td", "", null);
        gen.inline("td", getRowSpan(10), "", null);

        gen.end(); // tr

        gen.end(); // table
    }

    /**
     * 装備テーブルを生成
     * @param gen
     * @param ships
     */
    private static <SHIP extends ShipBaseDto>
            void genSlotitemTable(HTMLGenerator gen, List<SHIP> ships)
    {
        gen.begin("table", null);

        List<List<ItemDto>> itemList = new ArrayList<>();
        gen.begin("tr", null);
        for (int i = 0; i < ships.size(); ++i) {
            SHIP ship = ships.get(i);
            gen.inline("td", getRowSpan(2), ship.getFriendlyName(), null);
            itemList.add(ship.getItem());
        }
        gen.end(); // tr

        for (int c = 0; c < 5; ++c) {
            gen.begin("tr", null);
            for (int i = 0; i < ships.size(); ++i) {
                SHIP ship = ships.get(i);
                List<ItemDto> items = itemList.get(i);
                String onSlot = "";
                String itemName = String.valueOf(c + 1) + ". ";
                int[] onSlots = ship.getOnSlot(); // 現在の艦載機搭載数
                int[] maxeq = ship.getShipInfo().getMaxeq(); // 艦載機最大搭載数
                if (c < items.size()) {
                    ItemDto item = items.get(c);
                    if (item != null) {
                        if (item.isPlane()) {
                            String max = (maxeq == null) ? "?" : String.valueOf(maxeq[i]);
                            onSlot = String.valueOf(onSlots[i]) + "/" + max;
                        }
                        itemName += item.getName();
                    }
                }
                gen.inline("td", itemName, null);
                gen.inline("td", onSlot, null);
            }
            gen.end(); // tr
        }

        gen.end(); // table
    }

    /**
     * 会敵情報を生成
     * @param gen
     * @param battle
     */
    private static void genFormation(HTMLGenerator gen, BattleExDto battle) {
        gen.inline("span", "会敵: " + battle.getFormationMatch(), null);
        gen.begin("table", null);
        gen.begin("tr", null);
        gen.inline("th", "", null);
        gen.inline("th", "陣形", null);
        gen.inline("th", "索敵", null);
        gen.end(); // tr
        gen.begin("tr", null);
        gen.inline("td", "自", null);
        gen.inline("td", battle.getFormation()[0], null);
        gen.inline("td", battle.getSakuteki()[0], null);
        gen.end(); // tr
        gen.begin("tr", null);
        gen.inline("td", "敵", null);
        gen.inline("td", battle.getFormation()[1], null);
        gen.inline("td", battle.getSakuteki()[1], null);
        gen.end(); // tr
        gen.end(); // table
    }

    /**
     * 艦載機ロスト表示を生成
     * @param stage
     * @return
     */
    private static String[] getNumPlaneString(int[] stage) {
        if (stage == null) {
            return new String[] { "", "" };
        }
        int flost = stage[0];
        int fall = stage[1];
        int elost = stage[2];
        int eall = stage[3];
        int fremain = fall - flost;
        int eremain = eall - elost;
        return new String[] {
                String.valueOf(fall) + "→" + fremain + " (-" + flost + ")",
                String.valueOf(eall) + "→" + eremain + " (-" + elost + ")"
        };
    }

    /**
     * 触接表示を生成
     * @param touchPlane
     * @return
     */
    private static String[] getTouchPlane(int[] touchPlane) {
        if (touchPlane == null) {
            return new String[] { "", "" };
        }
        String[] ret = new String[2];
        for (int i = 0; i < 2; ++i) {
            if (touchPlane[i] == -1) {
                ret[i] = "なし";
            }
            else {
                ItemDto item = Item.get(touchPlane[i]);
                if (item != null) {
                    ret[i] = item.getName();
                }
                else {
                    ret[i] = "あり（機体不明）";
                }
            }
        }
        return ret;
    }

    private static <T> void copyToOffset(List<? extends T> src, T[] array, int offset) {
        if (src == null) {
            return;
        }
        for (int i = 0; i < src.size(); ++i) {
            array[i + offset] = src.get(i);
        }
    }

    private static void copyToOffset(int[] src, int[] dst, int offset) {
        if (src == null) {
            return;
        }
        System.arraycopy(src, 0, dst, offset, src.length);
    }

    /**
     * ダメージによる変化を計算して表示用文字列を返す
     * @param hp
     * @param target
     * @param damage
     * @param index
     * @return
     */
    private static String doDamge(int[] hp, int[] target, int[] damage, int index) {
        int before = hp[target[index]];
        int after = before - damage[index];
        if (after < 0) {
            after = 0;
        }
        hp[target[index]] = after;
        return String.valueOf(before) + "→" + after;
    }

    /**
     * 「○→○ ダメージ (○→○)」のテーブルを生成
     * @param gen
     * @param atack
     * @param targetShips
     * @param targetHp
     */
    private static void genDamageTableContent(HTMLGenerator gen, BattleAtackDto atack,
            ShipBaseDto[] targetShips, int[] targetHp) {
        gen.begin("tr", null);
        gen.inline("th", "艦", null);
        gen.inline("th", "ダメージ", null);
        gen.inline("th", "残りHP", null);
        gen.end(); // tr

        for (int i = 0; i < atack.damage.length; ++i) {
            gen.begin("tr", null);
            gen.inline("td", targetShips[atack.target[i]].getFriendlyName(), null);
            gen.inline("td", String.valueOf(atack.damage[i]), null);
            gen.inline("td", doDamge(targetHp, atack.target, atack.damage, i), null);
            gen.end(); // tr
        }
    }

    /**
     * 「○→○　ダメージ」のテーブルを生成
     * @param gen
     * @param atack
     * @param originShips
     * @param targetShips
     */
    private static void genAtackTableContent(HTMLGenerator gen, BattleAtackDto atack,
            ShipBaseDto[] originShips, ShipBaseDto[] targetShips)
    {
        gen.begin("tr", null);
        gen.inline("th", "艦", null);
        gen.inline("th", "", null);
        gen.inline("th", "艦", null);
        gen.inline("th", "ダメージ", null);
        gen.end(); // tr

        for (int i = 0; i < atack.origin.length; ++i) {
            gen.begin("tr", null);
            gen.inline("td", originShips[atack.origin[i]].getFriendlyName(), null);
            gen.inline("td", "→", null);
            gen.inline("td", targetShips[atack.target[atack.ot[i]]].getFriendlyName(), null);
            gen.inline("td", String.valueOf(atack.ydam[i]), null);
            gen.end(); // tr
        }
    }

    /**
     * 砲撃戦を生成
     * @param gen
     * @param atacks
     * @param friendShips
     * @param enemyShips
     * @param friendHp
     * @param enemyHp
     */
    private static void genHougekiTableContent(HTMLGenerator gen, List<BattleAtackDto> atacks,
            ShipBaseDto[] friendShips, ShipBaseDto[] enemyShips, int[] friendHp, int[] enemyHp) {
        gen.begin("tr", null);
        gen.inline("th", "", null);
        gen.inline("th", "艦", null);
        gen.inline("th", "", null);
        gen.inline("th", "", null);
        gen.inline("th", "艦", null);
        gen.inline("th", "ダメージ", null);
        gen.inline("th", "残りHP", null);
        gen.end(); // tr

        for (BattleAtackDto atack : atacks) {
            ShipBaseDto[] origin;
            ShipBaseDto[] target;
            int[] targetHp;
            String[] text;
            if (atack.friendAtack) {
                origin = friendShips;
                target = enemyShips;
                targetHp = enemyHp;
                text = new String[] { "自軍", "敵軍" };
            }
            else {
                origin = enemyShips;
                target = friendShips;
                targetHp = friendHp;
                text = new String[] { "敵軍", "自軍" };
            }

            for (int i = 0; i < atack.damage.length; ++i) {
                gen.begin("tr", null);

                if (i == 0) {
                    gen.inline("td", text[0], null);
                    gen.inline("td", origin[atack.target[0]].getFriendlyName(), null);
                }
                else {
                    gen.inline("td", getRowSpan(2), "", null);
                }

                gen.inline("td", "→", null);
                gen.inline("td", text[1], null);
                gen.inline("td", target[atack.target[i]].getFriendlyName(), null);
                gen.inline("td", String.valueOf(atack.damage[i]), null);
                gen.inline("td", doDamge(targetHp, atack.target, atack.damage, i), null);

                gen.end(); // tr
            }
        }
    }

    /**
     * 航空戦を生成
     * @param gen
     * @param air
     * @param title
     * @param friendShips
     * @param enemyShips
     * @param friendHp
     * @param enemyHp
     */
    private static void genAirBattle(HTMLGenerator gen, AirBattleDto air, String title,
            ShipBaseDto[] friendShips, ShipBaseDto[] enemyShips, int[] friendHp, int[] enemyHp)
    {
        if (air == null) {
            return;
        }

        gen.inline("h3", title, null);
        String[] stage1 = getNumPlaneString(air.stage1);
        String[] stage2 = getNumPlaneString(air.stage2);
        String[] touch = getTouchPlane(air.touchPlane);
        gen.begin("table", null);
        gen.begin("tr", null);
        gen.inline("th", getColSpan(2), "", null);
        gen.inline("th", getColSpan(2), "制空権", null);
        gen.inline("th", getRowSpan(2), "艦載機", null);
        gen.inline("th", getColSpan(2), "触接", null);
        gen.end(); // tr
        gen.begin("tr", null);
        gen.inline("th", "ステージ1", null);
        gen.inline("th", "ステージ2", null);
        gen.end(); // tr
        gen.begin("tr", null);
        gen.inline("td", "自", null);
        gen.inline("td", air.seiku, null);
        gen.inline("td", stage1[0], null);
        gen.inline("td", stage2[0], null);
        gen.inline("td", touch[0], null);
        gen.end(); // tr
        gen.begin("tr", null);
        gen.inline("td", "敵", null);
        gen.inline("td", "", null);
        gen.inline("td", stage1[1], null);
        gen.inline("td", stage2[1], null);
        gen.inline("td", touch[1], null);
        gen.end(); // tr
        gen.end(); // table

        if ((air.atacks == null) || (air.atacks.size() == 0)) {
            gen.inline("h4", "航空戦による攻撃はなし", null);
            return;
        }

        for (BattleAtackDto atack : air.atacks) {

            ShipBaseDto[] origin;
            ShipBaseDto[] target;
            int[] targetHp;
            String[] text;
            if (atack.friendAtack) {
                origin = friendShips;
                target = enemyShips;
                targetHp = enemyHp;
                text = new String[] { "自軍", "敵軍ダメージ" };
            }
            else {
                origin = enemyShips;
                target = friendShips;
                targetHp = friendHp;
                text = new String[] { "敵軍", "自軍ダメージ" };
            }

            gen.begin("table", null);
            gen.inline("caption", text[0] + "の攻撃に参加した艦", new String[] { "air-atack-caption" });
            for (int i = 0; i < atack.origin.length; ++i) {
                gen.begin("tr", null);
                gen.inline("td", origin[atack.origin[i]].getFriendlyName(), null);
                gen.end(); // tr
            }
            gen.end(); // table

            gen.begin("table", null);
            gen.inline("caption", text[1], new String[] { "air-damage-caption" });
            genDamageTableContent(gen, atack, target, targetHp);
            gen.end(); // table
        }
    }

    /**
     * 雷撃戦を生成
     * @param gen
     * @param raigeki
     * @param title
     * @param friendShips
     * @param enemyShips
     * @param friendHp
     * @param enemyHp
     */
    private static void genRaigekiBattle(HTMLGenerator gen, List<BattleAtackDto> raigeki, String title,
            ShipBaseDto[] friendShips, ShipBaseDto[] enemyShips, int[] friendHp, int[] enemyHp)
    {
        if ((raigeki == null) || (raigeki.size() == 0)) {
            return;
        }

        gen.inline("h3", title, null);
        for (BattleAtackDto atack : raigeki) {
            ShipBaseDto[] origin;
            ShipBaseDto[] target;
            int[] targetHp;
            String[] text;
            if (atack.friendAtack) {
                origin = friendShips;
                target = enemyShips;
                targetHp = enemyHp;
                text = new String[] { "自軍攻撃", "敵軍ダメージ" };
            }
            else {
                origin = enemyShips;
                target = friendShips;
                targetHp = friendHp;
                text = new String[] { "敵軍攻撃", "自軍ダメージ" };
            }

            // 攻撃
            gen.begin("table", null);
            gen.inline("caption", text[0], new String[] { "raigeki-atack-caption" });
            genAtackTableContent(gen, atack, origin, target);
            gen.end(); // table

            // ダメージ
            gen.begin("table", null);
            gen.inline("caption", text[1], new String[] { "raigeki-damage-caption" });
            genDamageTableContent(gen, atack, target, targetHp);
            gen.end(); // table
        }
    }

    /**
     * フェイズを生成
     * @param gen
     * @param battle
     * @param index
     */
    private static void genPhase(HTMLGenerator gen, BattleExDto battle, int index) {
        BattleExDto.Phase phase = battle.getPhaseList().get(index);

        ShipBaseDto[] friendShips = new ShipBaseDto[12];
        ShipBaseDto[] enemyShips = new ShipBaseDto[6];
        int[] friendHp = new int[12];
        int[] enemyHp = new int[6];

        copyToOffset(battle.getDock().getShips(), friendShips, 0);
        copyToOffset(battle.getDockCombined().getShips(), friendShips, 6);
        copyToOffset(battle.getEnemy(), enemyShips, 0);
        copyToOffset(battle.getStartFriendHp(), friendHp, 0);
        copyToOffset(battle.getStartFriendHpCombined(), friendHp, 6);
        copyToOffset(battle.getStartEnemyHp(), enemyHp, 0);

        List<AirBattleDto> airList = new ArrayList<>();
        List<List<BattleAtackDto>> hougekiList = new ArrayList<>();

        if (phase.getAir() != null)
            airList.add(phase.getAir());
        if (phase.getAir2() != null)
            airList.add(phase.getAir2());
        if (phase.getHougeki1() != null)
            hougekiList.add(phase.getHougeki1());
        if (phase.getHougeki2() != null)
            hougekiList.add(phase.getHougeki2());
        if (phase.getHougeki3() != null)
            hougekiList.add(phase.getHougeki3());

        // 航空戦 → 支援艦隊による攻撃 → 航空戦２回目
        for (int i = 0; i < airList.size(); ++i) {
            genAirBattle(gen, phase.getAir(), "航空戦(" + (i + 1) + "/" + airList.size() + ")",
                    friendShips, enemyShips, friendHp, enemyHp);

            if (i == 0) {
                if (phase.getSupport() != null) {
                    for (BattleAtackDto atack : phase.getSupport()) {
                        gen.begin("table", null);
                        gen.inline("caption", "支援艦隊による攻撃", new String[] { "support-damage-caption" });
                        genDamageTableContent(gen, atack, enemyShips, enemyHp);
                        gen.end(); // table
                    }
                }
            }
        }

        // 夜戦
        if (phase.getHougeki() != null) {
            gen.inline("h3", "砲雷撃", null);
            gen.begin("table", null);
            genHougekiTableContent(gen, phase.getHougeki(), friendShips, enemyShips, friendHp, enemyHp);
            gen.end(); // table
        }

        // 砲撃+雷撃
        for (int i = 0; i < hougekiList.size(); ++i) {
            gen.inline("h3", "砲撃(" + (i + 1) + "/" + hougekiList.size() + ")", null);
            gen.begin("table", null);
            genHougekiTableContent(gen, hougekiList.get(i), friendShips, enemyShips, friendHp, enemyHp);
            gen.end(); // table

            if (battle.isCombined() && (i == 0)) {
                // 連合艦隊の場合はここで雷撃
                genRaigekiBattle(gen, phase.getRaigeki(), "雷撃戦",
                        friendShips, enemyShips, friendHp, enemyHp);
            }
        }

        // 連合艦隊でない時の雷撃
        if (battle.isCombined() == false) {
            genRaigekiBattle(gen, phase.getRaigeki(), "雷撃戦",
                    friendShips, enemyShips, friendHp, enemyHp);
        }
    }

    /**
     * フェイズのダメージ合計を計算
     * @param friend
     * @param enemy
     * @param phase
     */
    private static void computeDamages(int[] friend, int[] enemy, BattleExDto.Phase phase) {
        for (int i = 0; i < friend.length; ++i) {
            friend[i] = 0;
        }
        for (int i = 0; i < enemy.length; ++i) {
            enemy[i] = 0;
        }
        for (BattleAtackDto[] atacks : phase.getAtackSequence()) {
            if (atacks != null) {
                for (BattleAtackDto dto : atacks) {
                    for (int i = 0; i < dto.target.length; ++i) {
                        int target = dto.target[i];
                        int damage = dto.damage[i];
                        if (dto.friendAtack) {
                            enemy[target] += damage;
                        }
                        else {
                            friend[target] += damage;
                        }
                    }
                }
            }
        }
    }

    /**
     * 開始時HPとダメージ -> ダメージと戦闘後のHP
     * @param start
     * @param inDam
     * @param offset
     * @param dam
     * @param after
     */
    private static void storeDamageAndHp(int[] start, int[] inDam, int offset, int[] dam, int[] after) {
        if (start != null) {
            for (int i = 0; i < start.length; ++i) {
                dam[i] = inDam[offset + i];
                after[i] = start[i] - inDam[offset + i];
            }
        }
    }

    /**
     *  パラメータテーブルに表示するHPを計算
     *  配列インデックスは [艦隊][now, max, フェイズ, ダメージ][艦]
     * @param battle
     * @return
     */
    private static int[][][] calcHP(BattleExDto battle) {
        int[][][] hp = new int[3][2 + (battle.getPhaseList().size() * 2)][6];
        int[][] startHp = new int[][] {
                battle.getStartFriendHp(),
                battle.getStartFriendHpCombined(),
                battle.getStartEnemyHp() };

        hp[0][0] = battle.getStartEnemyHp();
        hp[1][0] = battle.getStartFriendHpCombined();
        hp[2][0] = battle.getStartEnemyHp();
        hp[0][1] = battle.getMaxFriendHp();
        hp[1][1] = battle.getMaxFriendHpCombined();
        hp[2][1] = battle.getMaxEnemyHp();

        int[] friendDamages = new int[12];
        int[] enemyDamages = new int[6];
        for (int pi = 0; pi < battle.getPhaseList().size(); ++pi) {
            BattleExDto.Phase phase = battle.getPhaseList().get(pi);
            computeDamages(friendDamages, enemyDamages, phase);
            storeDamageAndHp(startHp[0], friendDamages, 0, hp[0][(pi * 2) + 2], hp[0][(pi * 2) + 3]);
            storeDamageAndHp(startHp[1], friendDamages, 6, hp[1][(pi * 2) + 2], hp[1][(pi * 2) + 3]);
            storeDamageAndHp(startHp[2], enemyDamages, 0, hp[2][(pi * 2) + 2], hp[2][(pi * 2) + 3]);
            startHp = new int[][] { hp[0][(pi * 2) + 3], hp[1][(pi * 2) + 3], hp[2][(pi * 2) + 3] };
        }
        return hp;
    }

    private static String generateHTML(String title, BattleResultDto result, BattleExDto detail)
            throws IOException
    {
        HTMLGenerator gen = new HTMLGenerator();
        gen.genHeader(title);
        gen.begin("body", null);

        String[] sectionTitleClass = new String[] { "sec-title" };

        // タイトル
        String time = new SimpleDateFormat(AppConstants.DATE_FORMAT).format(result.getBattleDate());
        String header = result.getMapCell().detailedString() +
                " 「" + result.getQuestName() + "」で作戦行動中に「" +
                result.getEnemyName() + "」と対峙しました(" + time + ")";
        gen.inline("div", "<h1>" + header + "</h1>", new String[] { "title" });

        // パラメータテーブル生成 //
        gen.inline("div", "<h2>パラメータ</h2>", sectionTitleClass);
        gen.inline("hr", null);

        int[][][] hpList = calcHP(detail);
        String[] phaseName = (detail.getPhase1().isNight() ?
                new String[] { "夜戦後", "昼戦後" } : new String[] { "昼戦後", "夜戦後" });
        genParmeters(gen, detail.getDock().getName(),
                detail.getDock().getShips(), hpList[0], phaseName);
        if (detail.isCombined()) {
            genParmeters(gen, detail.getDockCombined().getName(),
                    detail.getDockCombined().getShips(), hpList[1], phaseName);
        }
        genParmeters(gen, detail.getEnemyName(), detail.getEnemy(), hpList[2], phaseName);

        // 装備を生成 //
        genSlotitemTable(gen, detail.getDock().getShips());
        if (detail.isCombined()) {
            genSlotitemTable(gen, detail.getDockCombined().getShips());
        }
        genSlotitemTable(gen, detail.getEnemy());

        gen.inline("hr", null);

        // 会敵情報 //
        gen.inline("div", "<h2>会敵情報</h2>", sectionTitleClass);
        gen.inline("hr", null);

        genFormation(gen, detail);

        // フェイズ //
        int numPhases = detail.getPhaseList().size();
        for (int i = 0; i < numPhases; ++i) {
            Phase phase = detail.getPhaseList().get(i);
            String phaseTitle = (i + 1) + "/" + numPhases + "フェイズ: " + (phase.isNight() ? "夜戦" : "昼戦");
            gen.inline("div", "<h2>" + phaseTitle + "</h2>", sectionTitleClass);
            gen.inline("hr", null);

            genPhase(gen, detail, i);
        }

        gen.end(); // body
        return gen.result();
    }
}
