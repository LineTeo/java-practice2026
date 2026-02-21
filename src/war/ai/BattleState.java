package war.ai;
//　行動判断に使う情報

public class BattleState {
    // ========================================
    // 自分の状態
    // ========================================
    public double selfHP;                  // HP割合（0.0-1.0）
    public int selfActivity;               // 残行動力
    public double selfX, selfY;            // 位置座標
    public double selfAngle;               // 砲塔角度
    
    // ========================================
    // 対象敵の状態
    // ========================================
    public double targetHP;                // 敵HP割合
    public double targetDistance;          // 敵との距離
    public double targetAngle;             // 敵の方向（将来の拡張用）
    public boolean targetInRange;          // 射程内かどうか
    
    // ========================================
    // 味方の状態
    // ========================================
    public int allyCount;                  // 生存味方数
    public int distanceRankFromEnemy;      // 敵からの距離順位（1=最も近い）
    public double closestAllyDistance;     // 最も近い味方との距離
    public double averageAllyHP;           // 味方の平均HP
    
    // ========================================
    // 全体の状況
    // ========================================
    public int turnNumber;                 // 現在のターン数
    public int totalEnemies;               // 総敵数
    public int remainingEnemies;           // 残存敵数
}