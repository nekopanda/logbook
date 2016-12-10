/**
 * 
 */
package logbook.gui.logic;

import java.text.MessageFormat;
import java.util.List;

import logbook.constants.AppConstants;
import logbook.dto.ItemDto;
import logbook.dto.ShipDto;

/**
 * @author Nekopanda
 *
 */
public class DaihatsuString {

    // 大発補正表
    private static double TOKUDAIHATSU_HOSEI[][] = {
            { 0, 0, 0, 0, 0 },
            { 2, 2, 2, 2, 2 },
            { 4, 4, 4, 4, 4 },
            { 5, 5, 5.2, 5.4, 5.4 },
            { 5.4, 5.6, 5.8, 5.9, 6 }
    };

    // 大発系の数
    private int numDaihatsu = 0;
    // 大発系の★の数合計
    private int totalDaihatsuLevel = 0;
    // 特大発の数
    private int numTokuDihatsu = 0;

    // 大発系による素の補正合計
    private double daihatsuUpBase = 0;
    // ★による上昇
    private double daihatsuUpLevel = 0;
    // 特大発による上昇
    private double daihatsuUpToku = 0;

    public DaihatsuString(List<ShipDto> ships)
    {
        //大発による遠征効率UP
        for (ShipDto shipDto : ships) {
            if (shipDto.getName().equals("鬼怒改二")) {
                this.daihatsuUpBase += 5.0;
            }
            for (ItemDto item : shipDto.getItem2()) {
                if (item != null) {
                    if (item.getName().equals("大発動艇")) {
                        this.daihatsuUpBase += 5.0;
                        ++this.numDaihatsu;
                        this.totalDaihatsuLevel += item.getLevel();
                    }
                    else if (item.getName().equals("大発動艇(八九式中戦車&陸戦隊)")) {
                        this.daihatsuUpBase += 2.0;
                        ++this.numDaihatsu;
                        this.totalDaihatsuLevel += item.getLevel();
                    }
                    else if (item.getName().equals("特二式内火艇")) {
                        this.daihatsuUpBase += 1.0;
                        ++this.numDaihatsu;
                        this.totalDaihatsuLevel += item.getLevel();
                    }
                    else if (item.getName().equals("特大発動艇")) {
                        this.daihatsuUpBase += 5.0;
                        ++this.numDaihatsu;
                        ++this.numTokuDihatsu;
                        this.totalDaihatsuLevel += item.getLevel();
                    }
                }
            }
        }
        //大発による遠征効率UPの上限
        if (this.daihatsuUpBase > 20) {
            this.daihatsuUpBase = 20;
        }
        // 改修による補正
        if (this.numDaihatsu > 0) {
            this.daihatsuUpLevel = (0.01 * this.daihatsuUpBase * this.totalDaihatsuLevel) / this.numDaihatsu;
        }
        // 特大発補正
        this.daihatsuUpToku = TOKUDAIHATSU_HOSEI
                [Math.min(this.numTokuDihatsu, 4)]
                [Math.min(this.numDaihatsu - this.numTokuDihatsu, 4)];
    }

    public double getUp() {
        return this.daihatsuUpBase + this.daihatsuUpLevel + this.daihatsuUpToku;
    }

    @Override
    public String toString() {
        return MessageFormat.format(AppConstants.MESSAGE_TOTAL_DAIHATSU, this.numDaihatsu, this.getUp());
    }
}
