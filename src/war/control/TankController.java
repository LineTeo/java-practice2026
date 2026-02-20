package war.control;

//======================================================================
// TankController.java - 戦車操作の統一インターフェース
//======================================================================
// PlayerController と PlayerAI（未実装）が実装すべきインターフェース。
// このインターフェースを介することで、プレイヤー操作と AI 操作を
// 実行時に切り替え可能にする（Strategy パターン）。
//
// 使用例:
//   TankController controller = new PlayerController(...);
//   // または
//   TankController controller = new PlayerAI(...);
//
//   controller.setControlledTank(tank);
//   controller.takeTurn();  // プレイヤー入力待機 or AI判断
//======================================================================

import war.tank.Tank;

/**
 * TankController インターフェース
 * 戦車を操作するコントローラーが実装すべきメソッドを定義する。
 */
public interface TankController {

    /**
     * 操作対象の戦車を設定する。
     *
     * @param tank 操作する戦車
     */
    void setControlledTank(Tank tank);

    /**
     * 現在操作中の戦車を取得する。
     *
     * @return 操作中の戦車（未設定の場合 null）
     */
    Tank getControlledTank();

    /**
     * 1ターン分の行動を実行する。
     *
     * - PlayerController: 入力待機状態となり、ユーザーが操作
     * - PlayerAI: 自動で最適行動を判断・実行
     *
     * @return 0: 正常終了
     */
    int takeTurn();
}
