package war.main;
// ======================================================================
// レベル2: 状態評価スコアベースAI（より賢い）
// ======================================================================

import java.util.ArrayList;

import war.tank.Tank;

/**
 * スコア計算により最適な行動を選択するAI
 */
public class SmartTankAI extends TankAI {
    
    /**
     * 全ての可能な行動を評価し、最もスコアが高い行動を選択
     */
    @Override
    public int decideAction(Tank aiTank, Tank playerTank, ArrayList<Tank> allTanks) {
        Action bestAction = findBestAction(aiTank, playerTank, allTanks);
        return executeAction(aiTank, playerTank, allTanks, bestAction);
    }
    
    private Action findBestAction(Tank aiTank, Tank playerTank, ArrayList<Tank> allTanks) {
        double bestScore = Double.NEGATIVE_INFINITY;
        Action bestAction = Action.REPAIR;
        
        // 各行動のスコアを計算
        for (Action action : Action.values()) {
            double score = evaluateAction(aiTank, playerTank, allTanks, action);
            
            if (score > bestScore) {
                bestScore = score;
                bestAction = action;
            }
        }
        
        return bestAction;
    }
    
    private double evaluateAction(Tank aiTank, Tank playerTank, 
                                  ArrayList<Tank> allTanks, Action action) {
        double score = 0.0;
        
        double healthRatio = (double) aiTank.getHp() / (double) aiTank.getMaxHp();
        double distance = calculateDistance(aiTank, playerTank);
        
        switch (action) {
            case REPAIR:
                // HP低いほど修理の価値が高い
                score = (1.0 - healthRatio) * 100;
                // ただし、敵が近いと価値が下がる
                if (distance < 3.0) score *= 0.5;
                break;
                
            case RELOAD:
                // 弾薬が少ないほど価値が高い
                double ammoRatio = (double) aiTank.getAmmo() / (double) aiTank.getMaxAmmo();
                score = (1.0 - ammoRatio) * 80;
                break;
                
            case ATTACK:
                // 射程内で弾薬があれば高スコア
                if (distance <= aiTank.getRange() && aiTank.getAmmo() > 0) {
                    score = 150 - distance * 10; // 近いほど高スコア
                    // 敵のHP低いほど高スコア（トドメを刺す）
                    double enemyHealthRatio = (double) playerTank.getHp() / 
                                              (double) playerTank.getMaxHp();
                    if (enemyHealthRatio < 0.3) score += 50;
                } else {
                    score = -100; // 攻撃不可能なら低スコア
                }
                break;
                
            case APPROACH:
                // 距離があるときに価値が高い
                if (distance > aiTank.getRange()) {
                    score = 70 + (distance - aiTank.getRange()) * 5;
                    // 自分のHP高いほど積極的に
                    score *= healthRatio;
                } else {
                    score = 20; // 既に射程内なら価値低い
                }
                break;
                
            case RETREAT:
                // HP低いときに価値が高い
                score = (1.0 - healthRatio) * 90;
                // 敵が近いほど価値が高い
                if (distance < 3.0) score += 40;
                break;
        }
        
        return score;
    }
    
    private int executeAction(Tank aiTank, Tank playerTank, 
                             ArrayList<Tank> allTanks, Action action) {
        switch (action) {
            case REPAIR:
                return executeRepair(aiTank);
            case RELOAD:
                return executeReload(aiTank);
            case ATTACK:
                return executeAttack(aiTank, playerTank);
            case APPROACH:
                return executeAggressiveApproach(aiTank, playerTank);
            case RETREAT:
                return executeTacticalApproach(aiTank, playerTank, allTanks);
            default:
                return 0;
        }
    }
    
    private enum Action {
        REPAIR,    // 修理
        RELOAD,    // 補給
        ATTACK,    // 攻撃
        APPROACH,  // 接近
        RETREAT    // 後退
    }
}


