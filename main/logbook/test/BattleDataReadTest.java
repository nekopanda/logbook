/**
 * 
 */
package logbook.test;

import java.io.FileInputStream;
import java.io.IOException;

import logbook.dto.ResultRank;
import logbook.proto.LogbookEx.BattleExDtoPb;

/**
 * @author Nekopanda
 *
 */
public class BattleDataReadTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO 自動生成されたメソッド・スタブ
        try {
            FileInputStream input = new FileInputStream("dump-data.dat");
            int numData = 0;
            int mismatch = 0;
            while (input.available() > 0) {
                BattleExDtoPb battle;
                battle = BattleExDtoPb.parseDelimitedFrom(input);
                String rank = battle.getRank();
                String estimated = ResultRank.fromProto(
                        battle.getPhaseList(battle.getPhaseListCount() - 1).getEstimatedRank()).rank();
                if (!rank.equals(estimated)) {
                    ++mismatch;
                }
                ++numData;
            }
            System.out.println("完了 " + mismatch + "/" + numData);
        } catch (IOException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        }
    }
}
