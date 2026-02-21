// ======================================================================
// 戦車ゲームAI実装 - 段階的アプローチ
// ======================================================================

// ======================================================================
// レベル1: ルールベースAI（すぐに実装可能）
// ======================================================================
package war.main;

import java.util.ArrayList;

import war.tank.Tank;

/**
 * TankAI - 戦車の行動を決定するAIクラス
 * まずはこれから始めるのがおすすめ
 */
public class TankAI {
    
    private static final int GRID_SIZE = 20;
    
    /**
     * AIの行動を決定するメインメソッド
     * @param aiTank AI制御の戦車
     * @param playerTank プレイヤーの戦車
     * @param allTanks すべての戦車リスト
     * @return ゲーム終了フラグ（0=継続, 1=終了）
     */
    public int decideAction(Tank aiTank, Tank playerTank, ArrayList<Tank> allTanks) {
        // HP状況を評価
        double healthRatio = (double) aiTank.getHp() / (double) aiTank.getMaxHp();
        double distance = calculateDistance(aiTank, playerTank);
        
        // 意思決定ツリー
        if (healthRatio <= 0.3) {
            // HP低い → 修理
            return executeRepair(aiTank);
        } else if (aiTank.getAmmo() <= 2) {
            // 弾薬少ない → 補給
            return executeReload(aiTank);
        } else if (distance <= 2.0) {
            // 近距離 → 攻撃
            return executeAttack(aiTank, playerTank);
        } else if (healthRatio <= 0.6) {
            // HP中程度 → 安全な位置へ移動しながら攻撃
            return executeTacticalApproach(aiTank, playerTank, allTanks);
        } else {
            // HP高い → 積極的に接近して攻撃
            return executeAggressiveApproach(aiTank, playerTank);
        }
    }
    
    // ----------------------------------------------------------------------
    // 基本行動
    // ----------------------------------------------------------------------
    
    protected int executeRepair(Tank tank) {
        tank.repair();
        return 0; // 継続
    }
    
    protected int executeReload(Tank tank) {
        tank.reloadAmmo(10);
        return 0; // 継続
    }
    
    protected int executeAttack(Tank aiTank, Tank targetTank) {
        // 方向を調整
        adjustDirection(aiTank, targetTank);
        // 攻撃
        aiTank.attackTarget(targetTank);
        return checkGameEnd(targetTank);
    }
    
    // ----------------------------------------------------------------------
    // 戦術的移動
    // ----------------------------------------------------------------------
    
    protected int executeTacticalApproach(Tank aiTank, Tank playerTank, ArrayList<Tank> allTanks) {
        // 安全な位置を探す（他の敵戦車に近い場所）
        Position safePos = findSafestPosition(aiTank, playerTank, allTanks);
        
        // 安全な位置へ1マス移動
        moveTowardsPosition(aiTank, safePos);
        
        // 射程内なら攻撃
        double distance = calculateDistance(aiTank, playerTank);
        if (distance <= aiTank.getRange()) {
            adjustDirection(aiTank, playerTank);
            aiTank.attackTarget(playerTank);
            return checkGameEnd(playerTank);
        }
        
        return 0;
    }
    
    protected int executeAggressiveApproach(Tank aiTank, Tank playerTank) {
        double distance = calculateDistance(aiTank, playerTank);
        
        // 射程内なら攻撃
        if (distance <= aiTank.getRange()) {
            adjustDirection(aiTank, playerTank);
            aiTank.attackTarget(playerTank);
            return checkGameEnd(playerTank);
        }
        
        // 射程外なら接近（2マス移動）
        moveTowardsTarget(aiTank, playerTank);
        moveTowardsTarget(aiTank, playerTank);
        
        // 移動後に射程内に入ったら攻撃
        distance = calculateDistance(aiTank, playerTank);
        if (distance <= aiTank.getRange()) {
            adjustDirection(aiTank, playerTank);
            aiTank.attackTarget(playerTank);
            return checkGameEnd(playerTank);
        }
        
        return 0;
    }
    
    // ----------------------------------------------------------------------
    // ユーティリティメソッド
    // ----------------------------------------------------------------------
    
    protected double calculateDistance(Tank tank1, Tank tank2) {
        double dx = tank1.getX() - tank2.getX();
        double dy = tank1.getY() - tank2.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    private void adjustDirection(Tank shooter, Tank target) {
        double dx = target.getX() - shooter.getX();
        double dy = target.getY() - shooter.getY();
        double angle = Math.toDegrees(Math.atan2(dy, dx));
        
        // 角度を0-360度に正規化
        if (angle < 0) angle += 360;
        
        double currentAngle = shooter.getAngle();
        double rotation = angle - currentAngle;
        
        shooter.rotate(rotation);
    }
    
    private void moveTowardsTarget(Tank mover, Tank target) {
        double dx = target.getX() - mover.getX();
        double dy = target.getY() - mover.getY();
        
        double newX = mover.getX();
        double newY = mover.getY();
        
        // X方向の移動を優先
        if (Math.abs(dx) > Math.abs(dy)) {
            newX += (dx > 0) ? 1 : -1;
        } else {
            newY += (dy > 0) ? 1 : -1;
        }
        
        // グリッド範囲内チェック
        if (newX >= 0 && newX < GRID_SIZE && newY >= 0 && newY < GRID_SIZE) {
            mover.move(newX, newY);
        }
    }
    
    private void moveTowardsPosition(Tank mover, Position pos) {
        double dx = pos.x - mover.getX();
        double dy = pos.y - mover.getY();
        
        double newX = mover.getX();
        double newY = mover.getY();
        
        if (Math.abs(dx) > Math.abs(dy)) {
            newX += (dx > 0) ? 1 : -1;
        } else {
            newY += (dy > 0) ? 1 : -1;
        }
        
        if (newX >= 0 && newX < GRID_SIZE && newY >= 0 && newY < GRID_SIZE) {
            mover.move(newX, newY);
        }
    }
    
    private Position findSafestPosition(Tank aiTank, Tank playerTank, ArrayList<Tank> allTanks) {
        // 他の味方戦車の重心を計算
        int totalX = 0;
        int totalY = 0;
        int count = 0;
        
        for (Tank tank : allTanks) {
            if (tank != aiTank && tank.jinei() == aiTank.jinei() && tank.isAlive()) {
                totalX += tank.getX();
                totalY += tank.getY();
                count++;
            }
        }
        
        if (count > 0) {
            return new Position(totalX / count, totalY / count);
        } else {
            // 味方がいない場合は端に逃げる
            return new Position(
                playerTank.getX() < GRID_SIZE / 2 ? GRID_SIZE - 1 : 0,
                playerTank.getY() < GRID_SIZE / 2 ? GRID_SIZE - 1 : 0
            );
        }
    }
    
    private int checkGameEnd(Tank target) {
        return target.isAlive() ? 0 : 1;
    }
    
    // ----------------------------------------------------------------------
    // 内部クラス
    // ----------------------------------------------------------------------
    
    private static class Position {
        int x, y;
        Position(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}

