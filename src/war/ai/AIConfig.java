package war.ai;

public class AIConfig {
    // ========================================
    // 脅威値の重み係数（合計1.0）
    // ========================================
    public double THREAT_WEIGHT_DISTANCE = 0.4;      // 距離による脅威
    public double THREAT_WEIGHT_HP = 0.3;            // HP による脅威
    public double THREAT_WEIGHT_RANK = 0.3;          // 順位による脅威
    
    // ========================================
    // 機会値の重み係数（合計1.0）
    // ========================================
    public double OPP_WEIGHT_DISTANCE = 0.6;         // 距離による機会
    public double OPP_WEIGHT_HP = 0.4;               // HP による機会
    
    // ========================================
    // 距離評価パラメータ
    // ========================================
    public double OPTIMAL_RANGE_MIN = 0.5;           // 最適射程下限（射程の50%）
    public double OPTIMAL_RANGE_MAX = 0.7;           // 最適射程上限（射程の70%）
    public double DANGER_RANGE = 0.3;                // 危険距離（射程の30%以内）
    
    // ========================================
    // 距離順位による脅威値（線形補間）
    // ========================================
    public double RANK_1_THREAT = 100.0;             // 1位（最も近い）
    public double RANK_2_THREAT = 50.0;              // 2位
    public double RANK_3_THREAT = 20.0;              // 3位
    public double RANK_4_THREAT = 10.0;              // 4位以降
    
    // ========================================
    // HP による脅威・機会の曲線パラメータ
    // ========================================
    public double HP_CURVE_EXPONENT = 2.0;           // 曲線の急峻さ（1.0=線形, 2.0=二次曲線）
    
    // ========================================
    // 行動選択の閾値
    // ========================================
    public double DEFENSE_THRESHOLD = 10.0;          // 脅威値 - 機会値 > この値 → 守備
    public double ATTACK_THRESHOLD = -10.0;          // 脅威値 - 機会値 < この値 → 攻撃
    
    // ========================================
    // 行動コスト
    // ========================================
    public int MOVE_COST = 1;                        // 移動1マスのコスト
    public int ATTACK_COST = 4;                      // 攻撃のコスト
    public int REPAIR_COST_PER_TURN = 8;             // 修理（全行動力消費）
}