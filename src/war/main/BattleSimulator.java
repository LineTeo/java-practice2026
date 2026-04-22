package war.main;

// ======================================================================
// BattleSimulator.java - 機械学習用・ヘッドレス対戦シミュレーター
// ======================================================================
// 変更履歴:
//   TankBattleGame から派生
//   v4: UI完全削除（JPanel/Swing/ダイアログを除去）
//       外部制御API化（runGame() / runBatch() で呼び出し可能）
//       CSV結果保存機能を追加
//
// 外部からの使い方:
//   // 1試合だけ実行
//   BattleSimulator sim = new BattleSimulator();
//   BattleResult result = sim.runGame();
//
//   // N試合バッチ実行してCSVに保存
//   BattleSimulator sim = new BattleSimulator();
//   sim.runBatch(1000, "results.csv");
// ======================================================================

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import war.ai.EnemyAI3;
import war.control.PlayerAI;
import war.tank.LightTank;
import war.tank.MediumTank;
import war.tank.Tank;
import war.tank.Tiger2;

/**
 * ヘッドレス対戦シミュレーター。
 * UIを持たず、外部から runGame() / runBatch() で制御する。
 */
public class BattleSimulator {

    // ======================================================================
    // 定数
    // ======================================================================

    private static final int FREND_SIDE = 0;
    private static final int ENEMY_SIDE = 1;

    private static final int GRID_SIZE  = 20;
    private static final int MAX_TURNS  = 200;   // 無限ループ防止

    // ======================================================================
    // インスタンス変数
    // ======================================================================

    private ArrayList<Tank> tanks;

    /** プレイヤー側AI */
    private final PlayerAI playerAI;

    /** 敵側AI */
    private final EnemyAI3 enemyAI2;

    // ======================================================================
    // コンストラクタ
    // ======================================================================

    public BattleSimulator() {
        playerAI = new PlayerAI(GRID_SIZE - 1);
        enemyAI2 = new EnemyAI3(GRID_SIZE - 1,EnemyAI3.Side.PC);
        tanks    = new ArrayList<>();
    }

    // ======================================================================
    // 公開API
    // ======================================================================

    /**
     * 1試合を実行し、結果を返す。
     *
     * @return BattleResult（勝者・ターン数・撃破数）
     */
    public BattleResult runGame() {
        initGame();

        int turn = 0;

        while (turn < MAX_TURNS) {
            turn++;

            // --- プレイヤー側ターン ---
            ArrayList<Tank> friendlies = getFriendlyTanks();
            ArrayList<Tank> enemies    = getEnemyTanks();

            playerAI.setTankList(enemies, friendlies);
            playerAI.takeTurn();
            resetSideTanks(FREND_SIDE);

            // 勝敗チェック
            int endResult = checkGameEnd();
            if (endResult != -1) {
                return buildResult(endResult, turn);
            }

            // --- 敵側ターン ---
            friendlies = getFriendlyTanks();
            enemies    = getEnemyTanks();

            for (Tank enemy : enemies) {
                if (!enemy.isAlive()) continue;
                enemyAI2.takeTurn(enemy, friendlies, enemies);
                enemy.resetAct();
            }

            // 勝敗チェック
            endResult = checkGameEnd();
            if (endResult != -1) {
                return buildResult(endResult, turn);
            }
        }

        // 制限ターン到達 → 引き分け
        return buildResult(0, turn);
    }

    /**
     * N試合を連続実行し、結果をCSVファイルに保存する。
     *
     * @param gameCount  実行試合数
     * @param csvPath    出力先CSVパス
     */
    public void runBatch(int gameCount, String csvPath) {
        System.out.println("=== バッチ対戦開始: " + gameCount + "試合 ===");

        int friendWins = 0, enemyWins = 0, draws = 0;

        try (PrintWriter pw = new PrintWriter(new FileWriter(csvPath))) {

            // CSVヘッダー
            pw.println("game_no,winner,turns,friend_survivors,enemy_survivors");

            for (int i = 1; i <= gameCount; i++) {
                BattleResult result = runGame();
                pw.println(result.toCsvRow(i));

                if      (result.winner == FREND_SIDE) {
                	friendWins++;
                    System.out.println("試合完了 / 友軍勝利");
          
                }
                else if (result.winner == ENEMY_SIDE) {
                	enemyWins++;
                System.out.println("試合完了 / 敵軍勝利");
                }
                else                                  draws++;

                // 進捗表示（1000試合ごと）
                if (i % 1000 == 0) {
                    System.out.printf("  %d試合完了 / 友軍勝率: %.1f%%%n",
                        i, (friendWins * 100.0 / i));
                }
            }

        } catch (IOException e) {
            System.err.println("CSV書き込みエラー: " + e.getMessage());
        }

        System.out.println("=== バッチ完了 ===");
        System.out.printf("友軍勝利: %d  敵軍勝利: %d  引き分け: %d%n",
            friendWins, enemyWins, draws);
        System.out.printf("友軍勝率: %.2f%%%n", friendWins * 100.0 / gameCount);
        System.out.println("CSV保存先: " + csvPath);
    }

    // ======================================================================
    // ゲーム内部処理
    // ======================================================================

    /** ゲームを初期化（戦車を生成・配置） */
    private void initGame() {
        tanks.clear();
        tanks.add(new Tiger2("タイガー", FREND_SIDE,  3,  3));
//        tanks.add(new Tiger2("ライオン", ENEMY_SIDE, 16, 16));
        tanks.add(new MediumTank("シャーマン１号", ENEMY_SIDE, 16,  3));
//        tanks.add(new HeavyTank( "シャーマン２号", ENEMY_SIDE, 16,  3));
        tanks.add(new LightTank( "シャーマン３号", ENEMY_SIDE,  3, 16));
        tanks.add(new MediumTank("シャーマン３号", ENEMY_SIDE, 16, 16));

        // PlayerAI に操作対象を設定
        playerAI.setControlledTank(tanks.get(0));
    }

    /**
     * 勝敗チェック。
     * @return FREND_SIDE(0)=友軍勝利, ENEMY_SIDE(1)=敵軍勝利, -1=継続
     */
    private int checkGameEnd() {
        int friends = 0, enemies = 0;
        for (Tank t : tanks) {
            if (!t.isAlive()) continue;
            if (t.getJinei() == FREND_SIDE) friends++;
            else                             enemies++;
        }
        if (friends == 0) return ENEMY_SIDE;
        if (enemies == 0) return FREND_SIDE;
        return -1;  // 継続
    }

    /** 結果オブジェクトを生成する */
    private BattleResult buildResult(int winner, int turns) {
        int friendSurvivors = 0, enemySurvivors = 0;
        for (Tank t : tanks) {
            if (!t.isAlive()) continue;
            if (t.getJinei() == FREND_SIDE) friendSurvivors++;
            else                             enemySurvivors++;
        }
        return new BattleResult(winner, turns, friendSurvivors, enemySurvivors);
    }

    // ======================================================================
    // ヘルパーメソッド
    // ======================================================================

    private ArrayList<Tank> getFriendlyTanks() {
        ArrayList<Tank> list = new ArrayList<>();
        for (Tank t : tanks) {
            if (t.getJinei() == FREND_SIDE && t.isAlive()) list.add(t);
        }
        return list;
    }

    private ArrayList<Tank> getEnemyTanks() {
        ArrayList<Tank> list = new ArrayList<>();
        for (Tank t : tanks) {
            if (t.getJinei() == ENEMY_SIDE && t.isAlive()) list.add(t);
        }
        return list;
    }

    /** 指定陣営の全戦車の行動をリセット */
    private void resetSideTanks(int side) {
        for (Tank t : tanks) {
            if (t.getJinei() == side && t.isAlive()) t.resetAct();
        }
    }

    // ======================================================================
    // 内部クラス: BattleResult（1試合の結果）
    // ======================================================================

    /**
     * 1試合の結果を格納するデータクラス。
     */
    public static class BattleResult {

        /** 勝者陣営 (0=友軍, 1=敵軍, -1=引き分け) */
        public final int winner;

        /** 試合終了ターン数 */
        public final int turns;

        /** 生存友軍数 */
        public final int friendSurvivors;

        /** 生存敵軍数 */
        public final int enemySurvivors;

        public BattleResult(int winner, int turns, int friendSurvivors, int enemySurvivors) {
            this.winner          = winner;
            this.turns           = turns;
            this.friendSurvivors = friendSurvivors;
            this.enemySurvivors  = enemySurvivors;
        }

        /** CSV行フォーマットで返す */
        public String toCsvRow(int gameNo) {
            String winnerLabel = (winner == 0) ? "friend"
                               : (winner == 1) ? "enemy"
                               : "draw";
            return String.format("%d,%s,%d,%d,%d",
                gameNo, winnerLabel, turns, friendSurvivors, enemySurvivors);
        }

        @Override
        public String toString() {
            return String.format("勝者=%s ターン=%d 友軍生存=%d 敵生存=%d",
                (winner == 0) ? "友軍" : (winner == 1) ? "敵軍" : "引き分け",
                turns, friendSurvivors, enemySurvivors);
        }
    }

    // ======================================================================
    // main（動作確認用）
    // ======================================================================

    public static void main(String[] args) {
        BattleSimulator sim = new BattleSimulator();

        // 単発テスト
//        System.out.println("--- 単発テスト ---");
//        BattleResult result = sim.runGame();
//        System.out.println(result);

        // バッチ実行（1000試合）
        System.out.println();
        sim.runBatch(10000, "battle_results.csv");
    }
}
