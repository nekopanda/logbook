package logbook.dto;

/**
 * 出撃統計の結果
 */
public final class BattleAggDetailsDto extends AbstractDto {
    /** S勝利 */
    private int s;
    /** A勝利 */
    private int a;
    /** B勝利 */
    private int b;
    /** C敗北 */
    private int c;
    /** D敗北 */
    private int d;

    /** ボスS勝利 */
    private int bossS;
    /** ボスA勝利 */
    private int bossA;
    /** ボスB勝利 */
    private int bossB;
    /** ボスC敗北 */
    private int bossC;
    /** ボスD敗北 */
    private int bossD;

    /**
     * 統計に加算します
     * 
     * @param isBoss ボスマス
     * @param rank ランク
     */
    public void add(String rank, boolean isBoss) {
        switch (rank) {
        case "S":
            this.s++;
            if (isBoss)
                this.bossS++;
            break;
        case "A":
            this.a++;
            if (isBoss)
                this.bossA++;
            break;
        case "B":
            this.b++;
            if (isBoss)
                this.bossB++;
            break;
        case "C":
            this.c++;
            if (isBoss)
                this.bossC++;
            break;
        case "D":
            this.d++;
            if (isBoss)
                this.bossD++;
            break;
        }
    }

    /**
     * @return S勝利
     */
    public int getS() {
        return this.s;
    }

    /**
     * @return ボスS勝利
     */
    public int getBossS() {
        return this.bossS;
    }

    /**
     * @return A勝利
     */
    public int getA() {
        return this.a;
    }

    /**
     * @return ボスA勝利
     */
    public int getBossA() {
        return this.bossA;
    }

    /**
     * @return B勝利
     */
    public int getB() {
        return this.b;
    }

    /**
     * @return ボスB勝利
     */
    public int getBossB() {
        return this.bossB;
    }

    /**
     * @return C敗北
     */
    public int getC() {
        return this.c;
    }

    /**
     * @return ボスC敗北
     */
    public int getBossC() {
        return this.bossC;
    }

    /**
     * @return D敗北
     */
    public int getD() {
        return this.d;
    }

    /**
     * @return ボスD敗北
     */
    public int getBossD() {
        return this.bossD;
    }

    /**
     * @return 勝利計
     */
    public int getWin() {
        return this.getS() + this.getA() + this.getB();
    }

    /**
     * @return ボス勝利計
     */
    public int getBossWin() {
        return this.getBossS() + this.getBossA() + this.getBossB();
    }
}
