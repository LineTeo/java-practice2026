package war.ai;
//脅威評価パラメータ

public class pAiConfig extends AIConfig{
    // ========================================
    // ゾーン判定 敵からの距離
    // ========================================
    public double THREAT_ZONE_1 = 0.3;      		// 決戦領域
    public double THREAT_ZONE_2 = 0.6;      		// 戦闘領域
    public double THREAT_ZONE_3 = 0.3;      		// 回復領域
    
    // ========================================
    // AC DC AT DTの閾値パラメータ
    // 命名規則　詳しくは仕様表を参照
    //   　P0*-------Z*------AA------AC---------n------O
    // 仕様表のNo  ゾーン 2回の行動 パラメータ 回数  以上/以下
    // ========================================
    public double P01_Z1_AA_AC_2_O = 0.9;
    public double P02_Z1_AE_DT_2_U = 0.2;
    public double P03_Z1_ER_DT_2_U = 0.6;
    public double P05_Z2_CA_AT_1_O = 1.0;
    public double P06_Z2_AA_DC_2_U = 0.3;
    public double P07_Z2_AE_DT_2_U = 0.3;
    public double P08_Z2_EE_DT_2_O = 0.7;
    public double P10_Z3_CC_DT_2_U = 0.3;
    public double P11_Z3_CA_DT_2_U = 0.3; 
    
    // ========================================
    // 距離順位による脅威係数（DC,DTの計算に使用）
    // ========================================
    public double RANK_1_THREAT = 1.0;             // 1位（最も近い）
    public double RANK_2_THREAT = 0.4;              // 2位
    public double RANK_3_THREAT = 0.2;              // 3,4位
    public double RANK_4_THREAT = 0.1;              // 5位以降
    
    
    
    // 以下は現時点では使わない予定
    
    // ========================================
    // 距離評価パラメータ
    // ========================================
    public double OPTIMAL_RANGE_MIN = 0.5;           // 最適射程下限（射程の50%）
    public double OPTIMAL_RANGE_MAX = 0.7;           // 最適射程上限（射程の70%）
    public double DANGER_RANGE = 0.3;                // 危険距離（射程の30%以内）
    
    // ========================================
    // 行動コスト
    // ========================================
    public int MOVE_COST = 1;                        // 移動1マスのコスト
    public int ATTACK_COST = 4;                      // 攻撃のコスト
    public int REPAIR_COST_PER_TURN = 8;             // 修理コスト最大値（残行動力に比例して修理）
}