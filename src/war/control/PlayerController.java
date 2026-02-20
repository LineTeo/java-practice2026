package war.control;

//======================================================================
// PlayerController.java - プレイヤー操作を担当するクラス
//======================================================================
// TankBattleGame からキーボード・マウス入力処理を分離。
// EnemyAI と対になる「プレイヤー側のコントローラー」として機能する。
//
// 責務:
//   - キーボード入力（矢印キー移動、R:修理、A:補給）の処理
//   - マウスクリックによる攻撃処理
//   - プレイヤー戦車の選択・操作
//
// 将来的に PlayerAI クラスに差し替えることで、
// プレイヤー側も自動操作に切り替え可能な設計。
//======================================================================

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import war.tank.Tank;

/**
 * PlayerController クラス
 * プレイヤーの入力を受け取り、選択中の戦車を操作する。
 */
public class PlayerController {

    // ======================================================================
    // 定数
    // ======================================================================

    /** グリッドのマス数（移動範囲チェック用） */
    private final int GRID_SIZE;

    /** 1マスのピクセルサイズ（マウスクリック座標変換用） */
    private final int CELL_SIZE;

    // ======================================================================
    // インスタンス変数
    // ======================================================================

    /** 現在操作中の戦車 */
    private Tank controlledTank;

    // ======================================================================
    // コンストラクタ
    // ======================================================================

    /**
     * @param gridSize グリッドのマス数（例: 20）
     * @param cellSize 1マスのピクセルサイズ（例: 35）
     */
    public PlayerController(int gridSize, int cellSize) {
        this.GRID_SIZE = gridSize;
        this.CELL_SIZE = cellSize;
    }

    // ======================================================================
    // 戦車の選択・取得
    // ======================================================================

    /**
     * 操作対象の戦車を設定する。
     *
     * @param tank 操作する戦車（null可）
     */
    public void setControlledTank(Tank tank) {
        this.controlledTank = tank;
    }

    /**
     * 現在操作中の戦車を取得する。
     *
     * @return 操作中の戦車（未設定の場合 null）
     */
    public Tank getControlledTank() {
        return controlledTank;
    }

    // ======================================================================
    // キーボード入力処理
    // ======================================================================

    /**
     * キーボード入力を処理する。
     * 矢印キー: 移動 + 砲塔回転
     * R: 修理
     * A: 弾薬補給
     *
     * @param keyCode KeyEvent.VK_* の定数
     * @return true: 入力を処理した / false: 無効な入力または戦車なし
     */
    public boolean handleKeyInput(int keyCode) {
        if (controlledTank == null || !controlledTank.isAlive()) {
            return false;
        }

        switch (keyCode) {
            case KeyEvent.VK_UP:
                return moveUp();

            case KeyEvent.VK_DOWN:
                return moveDown();

            case KeyEvent.VK_LEFT:
                return moveLeft();

            case KeyEvent.VK_RIGHT:
                return moveRight();

            case KeyEvent.VK_R:
                return repair();

            case KeyEvent.VK_A:
                return reloadAmmo();

            default:
                return false;
        }
    }

    // ======================================================================
    // マウス入力処理（攻撃）
    // ======================================================================

    /**
     * マウスクリックを処理する。
     * クリック位置に敵戦車がいれば攻撃する。
     *
     * @param mouseX   クリックされたX座標（ピクセル）
     * @param mouseY   クリックされたY座標（ピクセル）
     * @param allTanks 全戦車リスト（攻撃対象を探すため）
     * @return true: 攻撃を実行した / false: 対象なしor無効
     */
    public boolean handleMouseClick(int mouseX, int mouseY, ArrayList<Tank> allTanks) {
        if (controlledTank == null || !controlledTank.isAlive()) {
            return false;
        }

        // ピクセル座標 → グリッド座標
        int gridX = mouseX / CELL_SIZE;
        int gridY = mouseY / CELL_SIZE;

        // クリック位置にいる戦車を探す
        for (Tank target : allTanks) {
            // 自分自身は除外
            if (target == controlledTank || !target.isAlive()) {
                continue;
            }

            int tankGridX = (int) target.getX();
            int tankGridY = (int) target.getY();

            if (tankGridX == gridX && tankGridY == gridY) {
                // 攻撃実行
                controlledTank.attackTarget(target);
                logAction("攻撃: " + target.getName());
                return true;
            }
        }

        return false; // 対象なし
    }

    // ======================================================================
    // 移動処理（矢印キー）
    // ======================================================================

    /** 上方向（Y - 1）へ移動 */
    private boolean moveUp() {
        if (controlledTank.getY() > 0) {
            setDirection("VK_UP");
            controlledTank.move(controlledTank.getX(), controlledTank.getY() - 1);
            logAction("上へ移動");
            return true;
        }
        return false;
    }

    /** 下方向（Y + 1）へ移動 */
    private boolean moveDown() {
        if (controlledTank.getY() < GRID_SIZE - 1) {
            setDirection("VK_DOWN");
            controlledTank.move(controlledTank.getX(), controlledTank.getY() + 1);
            logAction("下へ移動");
            return true;
        }
        return false;
    }

    /** 左方向（X - 1）へ移動 */
    private boolean moveLeft() {
        if (controlledTank.getX() > 0) {
            setDirection("VK_LEFT");
            controlledTank.move(controlledTank.getX() - 1, controlledTank.getY());
            logAction("左へ移動");
            return true;
        }
        return false;
    }

    /** 右方向（X + 1）へ移動 */
    private boolean moveRight() {
        if (controlledTank.getX() < GRID_SIZE - 1) {
            setDirection("VK_RIGHT");
            controlledTank.move(controlledTank.getX() + 1, controlledTank.getY());
            logAction("右へ移動");
            return true;
        }
        return false;
    }

    // ======================================================================
    // 砲塔の向き調整
    // ======================================================================

    /**
     * 移動方向に応じて砲塔の向きを設定する。
     * 上: 0度、右: 90度、下: 180度、左: 270度
     */
    private void setDirection(String direction) {
        double currentAngle = controlledTank.getAngle();
        double targetAngle;

        switch (direction) {
            case "VK_UP":    targetAngle = 0;   break;
            case "VK_RIGHT": targetAngle = 90;  break;
            case "VK_DOWN":  targetAngle = 180; break;
            case "VK_LEFT":  targetAngle = 270; break;
            default: return;
        }

        // 最短回転角度を計算して回転
        double rotation = targetAngle - currentAngle;
        controlledTank.rotate(rotation);
    }

    // ======================================================================
    // その他のアクション
    // ======================================================================

    /** 修理（R キー） */
    private boolean repair() {
        int result = controlledTank.repair();
        if (result == 0) {
            logAction("修理実行");
            return true;
        }
        return false;
    }

    /** 弾薬補給（A キー） */
    private boolean reloadAmmo() {
        int result = controlledTank.reloadAmmo(10);
        if (result == 0) {
            logAction("弾薬補給 +10");
            return true;
        }
        return false;
    }

    // ======================================================================
    // ユーティリティ
    // ======================================================================

    /** アクションログ出力（デバッグ用） */
    private void logAction(String action) {
        if (controlledTank != null) {
            System.out.println("[Player] " + controlledTank.getName()
                + " → " + action
                + " (残行動力: " + controlledTank.activity() + ")");
        }
    }
}
