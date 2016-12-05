/**
 * 
 */
package logbook.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

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

    private static class FileName {
        public String path;
        public String name;

        public FileName(String path, String name) {
            this.path = path;
            this.name = name;
        }
    }

    private static void listFiles(String path, List<FileName> list)
    {
        File dir = new File(path);
        for (String filename : dir.list()) {
            String filepath = path + File.separator + filename;
            File file = new File(filepath);
            if (file.isDirectory()) {
                listFiles(filepath, list);
            }
            else {
                list.add(new FileName(file.getAbsolutePath(), file.getName()));
            }
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        boolean init = MasterData.INIT_COMPLETE;

        List<FileName> fileNameList = new ArrayList<FileName>();
        listFiles(args[0], fileNameList);
        fileNameList.sort(new Comparator<FileName>() {
            @Override
            public int compare(FileName arg0, FileName arg1) {
                return arg0.name.compareTo(arg1.name);
            }
        });

        int resultCount = 0;
        int[] rankCount = new int[10];
        try {
            FileOutputStream output = new FileOutputStream("dump-data.dat");
            Schema<BattleExDto> schema = RuntimeSchema.getSchema(BattleExDto.class);
            LinkedBuffer buffer = LinkedBuffer.allocate(128 * 1024);
            BattleExDto battle = null;
            for (FileName file : fileNameList) {
                String fileName = file.path;
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
                else if (fileName.endsWith("COMBINED_BATTLE_WATER.json")) {
                    kind = BattlePhaseKind.COMBINED_BATTLE_WATER;
                }
                else if (fileName.endsWith("COMBINED_AIR_BATTLE.json")) {
                    kind = BattlePhaseKind.COMBINED_AIR;
                }
                else if (fileName.endsWith("COMBINED_EACH_BATTLE.json")) {
                    kind = BattlePhaseKind.COMBINED_EACH_BATTLE;
                }
                else if (fileName.endsWith("COMBINED_EACH_BATTLE_WATER.json")) {
                    kind = BattlePhaseKind.COMBINED_EACH_BATTLE_WATER;
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
                    if (fileName.endsWith("PORT.json")) {
                        // 母港に戻ったのでリセット
                        battle = null;
                    }
                    else if (fileName.endsWith("NEXT.json")) {
                        // 移動したのでリセット
                        battle = null;
                    }
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
                        if (resultCount == 10540) {
                            System.out.println("!!!");
                        }
                        battle.addPhase(data, kind);
                    }
                    else if (battle != null) {
                        battle.setResult(data, null);
                        // ランクが合っているかチェック
                        ResultRank estimatedRank = battle.getLastPhase().getEstimatedRank();
                        if (!battle.getRank().rank().equals(estimatedRank.rank())) {
                            System.out.println("戦闘結果判定ミス[" + resultCount + "]: 正解ランク:" + battle.getRank() + " "
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
