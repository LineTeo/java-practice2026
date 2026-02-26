package war.ai;

/**
 * ThreatEvaluator - 脅威・機会評価クラス
 *
 * BattleState の情報と AIConfig のパラメータをもとに、
 * 脅威値（threat）と機会値（opportunity）を計算する。
 * ActionSelector はこの結果をもとに行動を判断する。
 */
public class ThreatEvaluator {

    private final AIConfig config;

 /*
  * コンストラクタ
  */
    public ThreatEvaluator(AIConfig config) {
        this.config = config;
    }

    // ========================================
    // 公開メソッド
    // ========================================

    /**
     * 脅威値を計算して返す
     * 距離・HP・距離順位の3要素で構成される
     */
    public double calcThreat(BattleState state) {
        double threat = 0.0;
        threat += calcDistanceThreat(state) * config.THREAT_WEIGHT_DISTANCE;
        threat += calcHPThreat(state)       * config.THREAT_WEIGHT_HP;
        threat += calcRankThreat(state)     * config.THREAT_WEIGHT_RANK;

 //       return clamp(threat); // 100以上で不感帯が生じるので廃止
        return threat;
    }

    /**
     * 機会値を計算して返す
     * 距離・HPの2要素で構成される
     */
    public double calcOpportunity(BattleState state) {
        double opportunity = 0.0;
        opportunity += calcDistanceOpportunity(state) * config.OPP_WEIGHT_DISTANCE;
        opportunity += calcHPOpportunity(state)       * config.OPP_WEIGHT_HP;
        return opportunity;
    }

    // ========================================
    // 脅威値の内訳計算（privateメソッド）
    // ========================================

    /**
     * 距離による脅威値（0.0〜100.0）
     * 危険距離内は最大、それ以外は距離に応じて線形減衰
     */
    private double calcDistanceThreat(BattleState state) {
        if (state.selfRange <= 0) return 0.0;
        double distRatio = state.targetDistance / state.selfRange;

        if (distRatio <= config.DANGER_RANGE) {
            return 100.0;
        }
        return Math.max(0.0, 100.0 * (1.0 - distRatio));
    }

    /**
     * HPによる脅威値（0.0〜100.0）
     * HPが低いほど脅威が高い（非線形曲線）
     */
    private double calcHPThreat(BattleState state) {
        return 100.0 * Math.pow(1.0 - state.selfHPRatio, config.HP_CURVE_EXPONENT);
    }

    /**
     * 距離順位による脅威値
     * 敵に最も近い＝最も狙われやすい＝脅威が高い
     */
    private double calcRankThreat(BattleState state) {
        switch (state.distanceRankFromEnemy) {
            case 1:  return config.RANK_1_THREAT;
            case 2:  return config.RANK_2_THREAT;
            case 3:  return config.RANK_3_THREAT;
            default: return config.RANK_4_THREAT;
        }
    }

    // ========================================
    // 機会値の内訳計算（privateメソッド）
    // ========================================

    /**
     * 距離による機会値（0.0〜100.0）
     * 最適射程内で最大、近すぎ・遠すぎで減少
     */
    private double calcDistanceOpportunity(BattleState state) {
        if (state.selfRange <= 0) return 0.0;
        double distRatio = state.targetDistance / state.selfRange;

        if (distRatio >= config.OPTIMAL_RANGE_MIN && distRatio <= config.OPTIMAL_RANGE_MAX) {
            // 最適射程内は最大
            return 100.0;
        } else if (distRatio < config.OPTIMAL_RANGE_MIN) {
            // 近すぎる：最適射程下限に向かって線形増加
            return 100.0 * (distRatio / config.OPTIMAL_RANGE_MIN);
        } else {
            // 遠すぎる：射程外に向かって線形減少
            double remain = 1.0 - config.OPTIMAL_RANGE_MAX;
            if (remain <= 0) return 0.0;  // ゼロ除算防止
            return Math.max(0.0, 100.0 * (1.0 - (distRatio - config.OPTIMAL_RANGE_MAX) / remain));
        }
    }

    /**
     * HPによる機会値（0.0〜100.0）
     * HPが高いほど攻撃的になれる（非線形曲線）
     */
    private double calcHPOpportunity(BattleState state) {
        return 100.0 * Math.pow(state.selfHPRatio, config.HP_CURVE_EXPONENT);
    }

    // ========================================
    // ユーティリティ
    // ========================================

    /** 値を 0.0〜100.0 に収める */
    private double clamp(double value) {
        return Math.min(100.0, Math.max(0.0, value));
    }
}