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
        int[] rankCount = new int[10];
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
                    else if (lastBattle != null) {
                        BattleResultDto dto = new BattleResultDto(data, lastBattle, null);
                        // ランクが合っているかチェック
                        if (!dto.getRank().equals(lastBattle.getRank().rank())) {
                            if ((lastBattle.getRank().match(dto.getRank())))
                                ;
                            else
                                System.out.println("戦闘結果判定ミス: 正解ランク:" + dto.getRank() + " "
                                        + lastBattle.getRankCalcInfo());
                        }

                        // 判定を特定できない場合の統計
                        if ((lastBattle.getRank().match(dto.getRank()))) {
                            switch (lastBattle.getRank()) {
                            case B_OR_C:
                                rankCount[dto.getRank().equals("B") ? 0 : 1]++;
                                break;
                            case C_OR_B:
                                rankCount[dto.getRank().equals("C") ? 2 : 3]++;
                                break;
                            case D_OR_C:
                                rankCount[dto.getRank().equals("D") ? 4 : 5]++;
                                break;
                            default:
                                break;
                            }
                        }

                        lastBattle = null;
                        ++resultCount;
                    }
                    jsonreader.close();
                }
            }
            System.out.println(resultCount + "件の戦闘結果を処理");
            System.out.println(Arrays.toString(rankCount));
        } catch (IOException e) {
            System.out.println("なんかエラーっぽい");
            e.printStackTrace();
        }
    }
}
