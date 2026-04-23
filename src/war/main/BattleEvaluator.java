package war.main;

// ======================================================================
// BattleEvaluator.java - Python最適化連携用エントリポイント
// ======================================================================
// 使い方:
//   java -cp <classpath> war.main.BattleEvaluator <json_path> <games>
//
//   引数:
//     json_path  AIConfigのJSONファイルパス（Pythonが生成）
//     games      試合数（例: 200）
//
//   標準出力:
//     WIN_RATE:0.6750
//     （Pythonがこの行をパースして勝率を取得する）
// ======================================================================

import war.ai.AIConfig;

public class BattleEvaluator {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: BattleEvaluator <json_path> <games>");
            System.exit(1);
        }

        String jsonPath = args[0];
        int    games    = Integer.parseInt(args[1]);

        // JSONからAIConfigを読み込む
        AIConfig config = AIConfig.fromJson(jsonPath);

        // シミュレーター実行
        BattleSimulator sim = new BattleSimulator(config);
        double winRate = sim.runBatchSilent(games);

        // Python側が読み取る固定フォーマットで出力
        System.out.printf("WIN_RATE:%.6f%n", winRate);
    }
}
