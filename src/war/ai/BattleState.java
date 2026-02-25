package war.ai;
//　行動判断に使う情報

/**
 * BattleState - 戦場の現在状態を保持するデータクラス
 *
 * StateAnalyzer が収集した情報をまとめる。
 * ThreatEvaluator・ActionSelector・BattleLogger に渡して使う。
 */
public class BattleState {

    // ========================================
    // 自分の状態
    // ========================================
    public int    selfHP;           // 現在HP
    public int    selfMaxHP;        // 最大HP
    public double selfHPRatio;      // HP割合（0.0〜1.0）
    public int    selfActivity;     // 残行動力
    public double selfRange;        // 射程
    public double selfX, selfY;     // 位置座標
    public double selfAngle;        // 砲塔角度（度）

    // ========================================
    // 対象敵の状態
    // ========================================
    public int    targetHP;         // 敵の現在HP
    public int    targetMaxHP;      // 敵の最大HP
    public double targetHPRatio;    // 敵のHP割合（0.0〜1.0）
    public double targetDistance;   // 敵との距離
    public double targetAngle;      // 敵の方向（度）
    public boolean targetInRange;   // 射程内かどうか

    // ========================================
    // 味方の状態
    // ========================================
    public int    allyCount;                // 生存味方数（自分を除く）
    public int    distanceRankFromEnemy;    // 敵からの距離順位（1=最も近い）
    public double closestAllyDistance;      // 最も近い味方との距離
    public double averageAllyHP;            // 味方の平均HP割合（0.0〜1.0）

    // ========================================
    // 全体の状況
    // ========================================
    public int turnNumber;          // 現在のターン数
    public int totalEnemies;        // 総敵数
    public int remainingEnemies;    // 残存敵数

    @Override
    public String toString() {
        return String.format(
            "BattleState\n" + 
            "{selfHP=%d/%d(%.0f%%), activity=%d, range=%.1f, ]\n" +
            "{targetHP=%d/%d(%.0f%%), distance=%.1f, angle=%.1f, ]\n" +
            "[targetInRange=%b, rank=%d, allies=%d, turn=%d}",
            selfHP, selfMaxHP, selfHPRatio * 100,selfActivity, selfRange,
            targetHP,targetMaxHP,targetHPRatio * 100 ,targetDistance,targetAngle,
            targetInRange,distanceRankFromEnemy, allyCount, turnNumber
        );
    }
}