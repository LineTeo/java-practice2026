package war.main.old;

// ======================================================================
// レベル3: DQN強化学習AI（最高レベル）
// ======================================================================

import java.util.ArrayList;

import war.tank.Tank;

/**
 * 強化学習で学習したモデルを使用するAI
 * ※事前に学習が必要
 */
public class DQNTankAI {
    
    private DQNAgent agent;
    private static final int STATE_SIZE = 10;
    
    public DQNTankAI(String modelPath) throws Exception {
        agent = new DQNAgent();
        agent.loadModel(modelPath);
    }
    
    public int decideAction(Tank aiTank, Tank playerTank, ArrayList<Tank> allTanks) {
        // 状態ベクトルの作成
        double[] state = createStateVector(aiTank, playerTank, allTanks);
        
        // AIが行動を選択
        Action action = agent.selectAction(state);
        
        // 行動を実行
        return executeAction(aiTank, playerTank, action);
    }
    
    private double[] createStateVector(Tank aiTank, Tank playerTank, 
                                      ArrayList<Tank> allTanks) {
        double[] state = new double[STATE_SIZE];
        
        // 正規化して0-1の範囲に
        state[0] = aiTank.getX() / 20.0;
        state[1] = aiTank.getY() / 20.0;
        state[2] = (double) aiTank.getHp() / aiTank.getMaxHp();
        state[3] = (double) aiTank.getAmmo() / aiTank.getMaxAmmo();
        state[4] = playerTank.getX() / 20.0;
        state[5] = playerTank.getY() / 20.0;
        state[6] = (double) playerTank.getHp() / playerTank.getMaxHp();
        
        double distance = calculateDistance(aiTank, playerTank);
        state[7] = Math.min(distance / 20.0, 1.0);
        
        double angle = calculateAngle(aiTank, playerTank);
        state[8] = angle / 360.0;
        
        // 味方の数
        int allyCount = 0;
        for (Tank tank : allTanks) {
            if (tank.jinei() == aiTank.jinei() && tank.isAlive()) {
                allyCount++;
            }
        }
        state[9] = allyCount / 4.0;
        
        return state;
    }
    
    private int executeAction(Tank aiTank, Tank playerTank, Action action) {
        // ActionはDQNAgentで定義されたenum（前述のコード参照）
        switch (action) {
            case MOVE_FORWARD:
                moveTowardsTarget(aiTank, playerTank);
                break;
            case MOVE_BACKWARD:
                moveAwayFromTarget(aiTank, playerTank);
                break;
            case TURN_LEFT:
                aiTank.rotate(-45);
                break;
            case TURN_RIGHT:
                aiTank.rotate(45);
                break;
            case SHOOT:
                adjustDirection(aiTank, playerTank);
                aiTank.attackTarget(playerTank);
                return checkGameEnd(playerTank);
            case WAIT:
                // 何もしない
                break;
        }
        
        return 0;
    }
    
    private double calculateDistance(Tank tank1, Tank tank2) {
        double dx = tank1.getX() - tank2.getX();
        double dy = tank1.getY() - tank2.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    private double calculateAngle(Tank from, Tank to) {
        double dx = to.getX() - from.getX();
        double dy = to.getY() - from.getY();
        double angle = Math.toDegrees(Math.atan2(dy, dx));
        if (angle < 0) angle += 360;
        return angle;
    }
    
    private void moveTowardsTarget(Tank mover, Tank target) {
        // 前述のコードと同じ
    }
    
    private void moveAwayFromTarget(Tank mover, Tank target) {
        double dx = mover.getX() - target.getX();
        double dy = mover.getY() - target.getY();
        
        int newX = mover.getX();
        int newY = mover.getY();
        
        if (Math.abs(dx) > Math.abs(dy)) {
            newX += (dx > 0) ? 1 : -1;
        } else {
            newY += (dy > 0) ? 1 : -1;
        }
        
        if (newX >= 0 && newX < 20 && newY >= 0 && newY < 20) {
            mover.move(newX, newY);
        }
    }
    
    private void adjustDirection(Tank shooter, Tank target) {
        // 前述のコードと同じ
    }
    
    private int checkGameEnd(Tank target) {
        return target.isAlive() ? 0 : 1;
    }
}


// ======================================================================
// 使用方法まとめ
// ======================================================================

/*
レベル1（ルールベースAI）: すぐに使える
    private TankAI tankAI = new TankAI();
    int result = tankAI.decideAction(selectedTank, tanks.get(0), tanks);

レベル2（スコアベースAI）: より賢い
    private SmartTankAI tankAI = new SmartTankAI();
    int result = tankAI.decideAction(selectedTank, tanks.get(0), tanks);

レベル3（DQN強化学習）: 最高レベル（事前学習必要）
    private DQNTankAI tankAI = new DQNTankAI("model.zip");
    int result = tankAI.decideAction(selectedTank, tanks.get(0), tanks);

推奨: まずレベル1から始めて、動作確認してからレベル2に進む
*/
