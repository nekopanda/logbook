/**
 * 
 */
package logbook.test;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Arrays;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import logbook.config.ShipConfig;
import logbook.dto.BattleDto;
import logbook.dto.BattleResultDto;
import logbook.dto.ResultRank;

import org.apache.commons.io.FileUtils;

/**
 * @author Nekopanda
 *
 */
public class BattleRankChecker {

    /**
     * @param args
     */
    public static void main(String[] args) {
        ShipConfig.load();

        File dir = new File(args[0]);
        File[] files = dir.listFiles();
        String[] fileNameList = new String[files.length];
        for (int i = 0; i < files.length; ++i) {
            fileNameList[i] = files[i].getAbsolutePath();
        }
        Arrays.sort(fileNameList);
        int resultCount = 0;
        try {
            BattleDto lastBattle = null;
            for (int i = 0; i < fileNameList.length; ++i) {
                String fileName = fileNameList[i];
                boolean isYasen = fileName.endsWith("BATTLE_MIDNIGHT.json");
                boolean isBattle = fileName.endsWith("BATTLE.json") || isYasen;
                boolean isBattleResult = fileName.endsWith("BATTLE_RESULT.json");
                if (isBattle || isBattleResult) {
                    String jsonString = FileUtils.readFileToString(new File(fileName), Charset.forName("MS932"));
                    JsonReader jsonreader = Json.createReader(new StringReader(jsonString));
                    JsonObject json = jsonreader.readObject();
                    JsonObject data = json.getJsonObject("api_data");
                    if (data == null)
                        continue;
                    if (isBattle) {
                        lastBattle = new BattleDto(data, lastBattle, isYasen);
                    }
                    else {
                        BattleResultDto dto = new BattleResultDto(data, lastBattle, null);
                        // ランクが合っているかチェック
                        if (!dto.getRank().equals(lastBattle.getRank().rank())) {
                            if ((lastBattle.getRank() == ResultRank.B_OR_C) && dto.getRank().equals("B"))
                                ;// 確率的にBになることがある判定だったのでOK
                            else
                                System.out.println("戦闘結果判定ミス: 正解ランク:" + dto.getRank() + " "
                                        + lastBattle.getRankCalcInfo());
                        }
                        lastBattle = null;
                        ++resultCount;
                    }
                    jsonreader.close();
                }
            }
            System.out.println(resultCount + "件の戦闘結果を処理");
        } catch (IOException e) {
            System.out.println("なんかエラーっぽい");
            e.printStackTrace();
        }
    }
}
