/**
 * 
 */
package logbook.test;

import java.io.FileInputStream;
import java.io.IOException;

import logbook.dto.BattleExDto;
import logbook.dto.ResultRank;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;

/**
 * @author Nekopanda
 *
 */
public class BattleDataReadTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            FileInputStream input = new FileInputStream("dump-data.dat");
            int numData = 0;
            int mismatch = 0;
            long before = System.currentTimeMillis();
            Schema<BattleExDto> schema = RuntimeSchema.getSchema(BattleExDto.class);
            LinkedBuffer buffer = LinkedBuffer.allocate(128 * 1024);
            while (input.available() > 0) {
                BattleExDto battle = schema.newMessage();
                ProtostuffIOUtil.mergeDelimitedFrom(input, battle, schema, buffer);
                // ランクが合っているかチェック
                ResultRank estimatedRank = battle.getLastPhase().getEstimatedRank();
                if (!battle.getRank().equals(estimatedRank.rank())) {
                    System.out.println("戦闘結果判定ミス: 正解ランク:" + battle.getRank() + " "
                            + battle.getLastPhase().getRankCalcInfo(battle));
                    ++mismatch;
                }
                ++numData;
            }
            long after = System.currentTimeMillis();
            System.out.println("完了 " + mismatch + "/" + numData + "(" + (after - before) + " ms)");
        } catch (IOException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        }
    }
}
