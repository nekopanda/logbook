/**
 * 
 */
package logbook.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import logbook.dto.BattleExDto;
import logbook.dto.BattlePhaseKind;
import logbook.dto.ResultRank;
import logbook.internal.MasterData;

import org.apache.commons.io.FileUtils;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;

/**
 * @author Nekopanda
 *
 */
public class BattleRankChecker {

    /**
     * @param args
     */
    public static void main(String[] args) {
        boolean init = MasterData.INIT_COMPLETE;

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
            FileOutputStream output = new FileOutputStream("dump-data.dat");
            Schema<BattleExDto> schema = RuntimeSchema.getSchema(BattleExDto.class);
            LinkedBuffer buffer = LinkedBuffer.allocate(128 * 1024);
            BattleExDto battle = null;
            for (int i = 0; i < fileNameList.length; ++i) {
                String fileName = fileNameList[i];
                boolean ignore = false;
                boolean isBattleResult = false;
                BattlePhaseKind kind = BattlePhaseKind.BATTLE;

                if (fileName.endsWith("COMBINED_BATTLE_MIDNIGHT.json")) {
                    kind = BattlePhaseKind.COMBINED_MIDNIGHT;
                }
                else if (fileName.endsWith("COMBINED_BATTLE_SP_MIDNIGHT.json")) {
                    kind = BattlePhaseKind.COMBINED_SP_MIDNIGHT;
                }
                else if (fileName.endsWith("COMBINED_BATTLE.json")) {
                    kind = BattlePhaseKind.COMBINED_BATTLE;
                }
                else if (fileName.endsWith("COMBINED_AIR_BATTLE.json")) {
                    kind = BattlePhaseKind.COMBINED_AIR;
                }
                else if (fileName.endsWith("PRACTICE_BATTLE_MIDNIGHT.json")) {
                    kind = BattlePhaseKind.MIDNIGHT;
                }
                else if (fileName.endsWith("PRACTICE_BATTLE.json")) {
                    kind = BattlePhaseKind.BATTLE;
                }
                else if (fileName.endsWith("BATTLE_NIGHT_TO_DAY.json")) {
                    kind = BattlePhaseKind.NIGHT_TO_DAY;
                }
                else if (fileName.endsWith("BATTLE_SP_MIDNIGHT.json")) {
                    kind = BattlePhaseKind.SP_MIDNIGHT;
                }
                else if (fileName.endsWith("BATTLE_MIDNIGHT.json")) {
                    kind = BattlePhaseKind.MIDNIGHT;
                }
                else if (fileName.endsWith("BATTLE.json")) {
                    kind = BattlePhaseKind.BATTLE;
                }
                else if (fileName.endsWith("BATTLE_RESULT.json")) {
                    isBattleResult = true;
                }
                else if (fileName.endsWith("COMBINED_BATTLE_RESULT.json")) {
                    isBattleResult = true;
                }
                else if (fileName.endsWith("PRACTICE_BATTLE_RESULT.json")) {
                    isBattleResult = true;
                }
                else {
                    ignore = true;
                }

                if (ignore == false) {
                    String jsonString = FileUtils.readFileToString(new File(fileName), Charset.forName("MS932"));
                    JsonReader jsonreader = Json.createReader(new StringReader(jsonString));
                    JsonObject json = jsonreader.readObject();
                    JsonObject data = json.getJsonObject("api_data");
                    if (data == null)
                        continue;
                    if (isBattleResult == false) {
                        if (battle == null) {
                            battle = new BattleExDto(new Date());
                        }
                        battle.addPhase(data, kind);
                    }
                    else if (battle != null) {
                        battle.setResult(data, null);
                        // ランクが合っているかチェック
                        ResultRank estimatedRank = battle.getLastPhase().getEstimatedRank();
                        if (!battle.getRank().equals(estimatedRank.rank())) {
                            System.out.println("戦闘結果判定ミス: 正解ランク:" + battle.getRank() + " "
                                    + battle.getLastPhase().getRankCalcInfo(battle));
                        }

                        ProtostuffIOUtil.writeDelimitedTo(output, battle, schema, buffer);
                        buffer.clear();

                        battle = null;
                        ++resultCount;
                    }
                    jsonreader.close();
                }
            }
            output.close();
            System.out.println(resultCount + "件の戦闘結果を処理");
            System.out.println(Arrays.toString(rankCount));
        } catch (IOException e) {
            System.out.println("なんかエラーっぽい");
            e.printStackTrace();
        }
    }
}
