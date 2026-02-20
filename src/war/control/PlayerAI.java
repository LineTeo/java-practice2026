package war.control;

//======================================================================
// PlayerAI.java - プレイヤー側の自動操作AIクラス（スケルトン）
//======================================================================
// TankController インターフェースを実装し、
// PlayerController の代わりに使用することで、
// プレイヤー側の戦車を自動操作できる。
//
// 使用例（TankBattleGame 内で切り替え）:
//   // プレイヤー操作
//   TankController playerController = new PlayerController(20, 35);
//
//   // AI操作に切り替え
//   TankController playerController = new PlayerAI(20);
//
// このスケルトン実装では EnemyAI と同じロジックを使用しているが、
// 独自の戦術（より慎重、より攻撃的など）を実装することも可能。
//======================================================================

import java.util.ArrayList;
import war.ai.EnemyAI;
import war.tank.Tank;

/**
 * PlayerAI クラス
 * プレイヤー側戦車を自動操作する AI。
 * TankController インターフェースを実装。
 */
public class PlayerAI implements TankController {

    // ======================================================================
    // インスタンス変数
    // ======================================================================

    /** 操作する戦車 */
    private Tank controlledTank;

    /** 敵戦車リスト（攻撃対象） */
    private ArrayList<Tank> enemies;

    /** AI ロジック（EnemyAI を流用） */
    private final EnemyAI aiLogic;

    // ======================================================================
    // コンストラクタ
    // ======================================================================

    /**
     * @param gridSize グリッドのマス数（例: 20）
     */
    public PlayerAI(int gridSize) {
        this.aiLogic = new EnemyAI(gridSize - 1);
    }

    // ======================================================================
    // TankController インターフェースの実装
    // ======================================================================

    @Override
    public void setControlledTank(Tank tank) {
        this.controlledTank = tank;
    }

    @Override
    public Tank getControlledTank() {
        return controlledTank;
    }

    /**
     * 敵戦車リストを設定する（takeTurn 実行前に呼ぶ必要がある）。
     *
     * @param enemies 攻撃対象の敵戦車リスト
     */
    public void setEnemies(ArrayList<Tank> enemies) {
        this.enemies = enemies;
    }

    @Override
    public int takeTurn() {
        if (controlledTank == null || !controlledTank.isAlive()) {
            return 0;
        }

        if (enemies == null || enemies.isEmpty()) {
            System.out.println("[PlayerAI] 敵戦車リストが未設定です");
            return 0;
        }

        // EnemyAI のロジックを流用して行動
        System.out.println("[PlayerAI] " + controlledTank.getName() + " の自動操作開始");
        int result = aiLogic.takeTurn(controlledTank, enemies);

        // 行動終了（行動力リセットは TankBattleGame 側で行う）
        return result;
    }

    // ======================================================================
    // 将来的な拡張ポイント
    // ======================================================================

    /**
     * プレイヤー専用の戦術を実装する場合はここに記述。
     * 例:
     *   - より慎重な立ち回り（HP 50% で退避開始）
     *   - 優先ターゲットの変更（HP 低い敵ではなく攻撃力高い敵を優先）
     *   - 補給タイミングの最適化
     */
    // private void executePlayerStrategy() { ... }
}
