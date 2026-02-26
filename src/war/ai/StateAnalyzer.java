package war.ai;


import java.util.List;

import war.tank.Tank;

/**
 * StateAnalyzer - 戦場情報収集クラス
 *
 * 自分・敵・味方の状態を収集し、BattleState にまとめる。
 * ThreatEvaluator や ActionSelector はこのクラスの結果を使って判断する。
 */
public class StateAnalyzer {

    /**
     * 現在の戦場状態を収集して BattleState を返す
     *
     * @param self      自分の戦車
     * @param target    攻撃対象の敵戦車（最も近い敵など、呼び出し元が選定する）
     * @param allies    味方戦車リスト（自分を除く）
     * @param enemies   敵戦車リスト（targetを含む全敵）
     * @param turnNumber 現在のターン数
     * @return 収集した戦場状態
     */
    public BattleState analyze(Tank self, Tank target, List<Tank> allies,
                               List<Tank> enemies, int turnNumber) {

        BattleState state = new BattleState();

        // ========================================
        // 1. 自分の状態
        // ========================================
        state.selfHP       = self.getHp();
        state.selfMaxHP    = self.getMaxHp();
        state.selfHPRatio  = (state.selfMaxHP > 0)
                             ? (double) state.selfHP / state.selfMaxHP
                             : 0.0;
        state.selfActivity = self.activity();
        state.selfRange    = self.getRange();
        state.selfX        = self.getX();
        state.selfY        = self.getY();
        state.selfAngle    = self.getAngle();

        // ========================================
        // 2. 対象敵の状態
        // ========================================
        if (target != null) {
            state.targetHP      = target.getHp();
            state.targetMaxHP   = target.getMaxHp();
            state.targetHPRatio = (state.targetMaxHP > 0)
                                  ? (double) state.targetHP / state.targetMaxHP
                                  : 0.0;
            state.targetDistance = calcDistance(self, target);
            state.targetAngle    = calcAngle(self, target);
            state.targetInRange  = state.targetDistance <= state.selfRange;
        } else {
	        state.targetHP      = 0;
	        state.targetMaxHP   = 0;
	        state.targetHPRatio = 0.0;
	        state.targetDistance = 10;
	        state.targetAngle    = 0;
	        state.targetInRange  = false;
        }

        // ========================================
        // 3. 味方の状態
        // ========================================
        state.allyCount = countAlive(allies);

        // 距離順位：target（敵）からみた距離順位
        state.distanceRankFromEnemy = calcDistanceRank(self, allies, target);

        // 最も近い味方との距離
        state.closestAllyDistance = calcClosestAllyDistance(self, allies);

        // 味方の平均HP割合
        state.averageAllyHP = calcAverageHPRatio(allies);

        // ========================================
        // 4. 全体の状況
        // ========================================
        state.turnNumber       = turnNumber;
        state.totalEnemies     = enemies.size();
        state.remainingEnemies = countAlive(enemies);

        return state;
    }

    // ----------------------------------------
    // ユーティリティメソッド
    // ----------------------------------------

    /** 2点間のユークリッド距離 */
    private double calcDistance(Tank a, Tank b) {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * 自分(self)がtarget戦車からみてどっちの方向にいるかを返す（0-360度）。
     * つまり自分が撃てばターゲットのどこに当たるかを示す。
     * 正面が0（度）で、時計回りに真後ろが180度、逆回りはマイナスで範囲は-180～+180。ただし-180の場合は180とする。
     */
    private double calcAngle(Tank self, Tank target) {
        // target からみた　selfの方向
        double dx = target.getX() - self.getX();
        double dy = target.getY() - self.getY();
        double dirToA = Math.toDegrees(Math.atan2(dx, -dy)) + 180; // atan2は0除算を防げる。dx=0もうまく処理する
        double diff =  dirToA-self.getAngle() ;
        while (diff >=  180.0) diff -= 360.0;
        while (diff < -180.0) diff += 360.0;
        if (diff == -180.0) {diff = 180.0;};
        return diff;
    }
    
    /** 生存している戦車の数 */
    private int countAlive(List<Tank> tanks) {
        if (tanks == null) return 0;
        int count = 0;
        for (Tank t : tanks) {
            if (t.isAlive()) count++;
        }
        return count;
    }
    /**
     * 距離順位を計算する
     * target（敵）からみて、self が何番目に近いか（1=最近）
     * allies に自分は含まれている想定
     */
    private int calcDistanceRank(Tank self, List<Tank> allies, Tank target) {
        if (target == null) return 1;

        double selfDist = calcDistance(self, target);
        int rank = 1;

        for (Tank ally : allies) {
            if (ally.isAlive()) {
                double allyDist = calcDistance(ally, target);
                if (allyDist < selfDist) {
                    rank++;
                }
            }
        }
        return rank;
    }

    /** 生存している味方の中で最も近い距離 */
    private double calcClosestAllyDistance(Tank self, List<Tank> allies) {
        double minDist = Double.MAX_VALUE;
        for (Tank ally : allies) {
            if (ally.isAlive()) {
                double d = calcDistance(self, ally);
                if (d < minDist) minDist = d;
            }
        }
        return (minDist == Double.MAX_VALUE) ? 0.0 : minDist;
    }

    /** 生存している味方の平均HP割合（0.0〜1.0） */
    private double calcAverageHPRatio(List<Tank> allies) {
        int aliveCount = 0;
        double totalRatio = 0.0;
        for (Tank ally : allies) {
            if (ally.isAlive() && ally.getMaxHp() > 0) {
                totalRatio += (double) ally.getHp() / ally.getMaxHp();
                aliveCount++;
            }
        }
        return (aliveCount > 0) ? totalRatio / aliveCount : 0.0;
    }
}

