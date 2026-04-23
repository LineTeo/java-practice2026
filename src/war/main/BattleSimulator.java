package war.main;

// ======================================================================
// BattleSimulator.java（機械学習連携版）
// ======================================================================
// 変更点（オリジナルからの差分）:
//   - コンストラクタに AIConfig を受け取るオーバーロードを追加
//   - runBatchSilent(): 標準出力なしで勝率だけ返すメソッドを追加
//   - EnemyAI3 の初期化に AIConfig を渡すよう変更
// ======================================================================

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import war.ai.AIConfig;
import war.ai.EnemyAI3;
import war.control.PlayerAI;
import war.tank.LightTank;
import war.tank.MediumTank;
import war.tank.Tank;
import war.tank.Tiger2;

/**
 * ヘッドレス対戦シミュレーター（AIConfig注入対応版）。
 */
public class BattleSimulator {

    // ======================================================================
    // 定数
    // ======================================================================

    private static final int FREND_SIDE = 0;
    private static final int ENEMY_SIDE = 1;
    private static final int GRID_SIZE  = 24;
    private static final int MAX_TURNS  = 200;

    // ======================================================================
    // インスタンス変数
    // ======================================================================

    private ArrayList<Tank> tanks;
    private final PlayerAI  playerAI;
    private final EnemyAI3  enemyAI2;

    // ======================================================================
    // コンストラクタ
    // ======================================================================

    /** デフォルトAIConfigで生成（従来互換） */
    public BattleSimulator() {
        this(new AIConfig());
    }

    /**
     * 外部からAIConfigを注入して生成（機械学習連携用）。
     *
     * @param enemyConfig 敵側AIに使わせるパラメータ
     */
    public BattleSimulator(AIConfig enemyConfig) {
        // ★ 敵AIにカスタムConfigを渡す（EnemyAI3側でコンストラクタ追加が必要）
        enemyAI2 = new EnemyAI3(GRID_SIZE - 1, EnemyAI3.Side.PC, enemyConfig);
        playerAI = new PlayerAI(GRID_SIZE - 1);
        tanks    = new ArrayList<>();
    }

    // ======================================================================
    // 公開API
    // ======================================================================

    /**
     * 1試合を実行し、結果を返す。
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

            int endResult = checkGameEnd();
            if (endResult != -1) return buildResult(endResult, turn);

            // --- 敵側ターン ---
            friendlies = getFriendlyTanks();
            enemies    = getEnemyTanks();
            for (Tank enemy : enemies) {
                if (!enemy.isAlive()) continue;
                enemyAI2.takeTurn(enemy, friendlies, enemies);
                enemy.resetAct();
            }

            endResult = checkGameEnd();
            if (endResult != -1) {
                System.out.printf("残弾=%d、HP=%d \n",tanks.get(0).getAmmo(),tanks.get(0).getHp());
            	
            	return buildResult(endResult, turn);
            }
        }

        return buildResult(-1, turn); // 引き分け
    }

    /**
     * N試合を実行し勝率を返す（標準出力なし）。
     * Python最適化ループから呼ばれる際に使用。
     *
     * @param gameCount 試合数
     * @return 敵軍勝率（0.0〜1.0）
     */
    public double runBatchSilent(int gameCount) {
        int enemyWins = 0;
        for (int i = 0; i < gameCount; i++) {
            BattleResult result = runGame();
            if (result.winner == ENEMY_SIDE) enemyWins++;
        }
        return (double) enemyWins / gameCount;
    }

    /**
     * N試合を連続実行し、結果をCSVファイルに保存する（オリジナル互換）。
     */
    public void runBatch(int gameCount, String csvPath) {
        System.out.println("=== バッチ対戦開始: " + gameCount + "試合 ===");

        int friendWins = 0, enemyWins = 0, draws = 0;

        try (PrintWriter pw = new PrintWriter(new FileWriter(csvPath))) {
            pw.println("game_no,winner,turns,friend_survivors,enemy_survivors");

            for (int i = 1; i <= gameCount; i++) {
                BattleResult result = runGame();

                pw.println(result.toCsvRow(i));

                if      (result.winner == FREND_SIDE) friendWins++;
                else if (result.winner == ENEMY_SIDE) enemyWins++;
                else                                  draws++;

                if (i % 100 == 0) {
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
    // 内部処理
    // ======================================================================

    private void initGame() {
        tanks.clear();
        tanks.add(new Tiger2("タイガー",         FREND_SIDE,  3,  3));
        tanks.add(new MediumTank("シャーマン１号", ENEMY_SIDE, 30,  3));
        tanks.add(new LightTank("チャフィー",  ENEMY_SIDE, 20, 20));
        tanks.add(new MediumTank("シャーマン２号", ENEMY_SIDE, 3, 20));
        playerAI.setControlledTank(tanks.get(0));
    }

    private int checkGameEnd() {
        int friends = 0, enemies = 0;
        for (Tank t : tanks) {
            if (!t.isAlive()) continue;
            if (t.getJinei() == FREND_SIDE) friends++;
            else                             enemies++;
        }
        if (friends == 0) return ENEMY_SIDE;
        if (enemies == 0) return FREND_SIDE;
        return -1;
    }

    private BattleResult buildResult(int winner, int turns) {
        int friendSurvivors = 0, enemySurvivors = 0;
        for (Tank t : tanks) {
            if (!t.isAlive()) continue;
            if (t.getJinei() == FREND_SIDE) friendSurvivors++;
            else                             enemySurvivors++;
        }
        return new BattleResult(winner, turns, friendSurvivors, enemySurvivors);
    }

    private ArrayList<Tank> getFriendlyTanks() {
        ArrayList<Tank> list = new ArrayList<>();
        for (Tank t : tanks)
            if (t.getJinei() == FREND_SIDE && t.isAlive()) list.add(t);
        return list;
    }

    private ArrayList<Tank> getEnemyTanks() {
        ArrayList<Tank> list = new ArrayList<>();
        for (Tank t : tanks)
            if (t.getJinei() == ENEMY_SIDE && t.isAlive()) list.add(t);
        return list;
    }

    private void resetSideTanks(int side) {
        for (Tank t : tanks)
            if (t.getJinei() == side && t.isAlive()) t.resetAct();
    }

    // ======================================================================
    // 内部クラス: BattleResult
    // ======================================================================

    public static class BattleResult {
        public final int winner;
        public final int turns;
        public final int friendSurvivors;
        public final int enemySurvivors;

        public BattleResult(int winner, int turns, int friendSurvivors, int enemySurvivors) {
            this.winner          = winner;
            this.turns           = turns;
            this.friendSurvivors = friendSurvivors;
            this.enemySurvivors  = enemySurvivors;
        }

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
        // デフォルト設定でバッチ実行
    	AIConfig config = AIConfig.fromJson("./best_ai_config.json");
        BattleSimulator sim = new BattleSimulator(config);
        sim.runBatch(10000, "battle_results.csv");
    }
}
