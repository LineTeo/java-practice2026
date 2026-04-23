package war.ai;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 脅威評価パラメータクラス。
 *
 * 機械学習連携のため、JSONファイルからパラメータを読み込む機能を追加。
 *
 * 使い方:
 *   // デフォルト値で生成
 *   AIConfig cfg = new AIConfig();
 *
 *   // JSONファイルから読み込み（Python側が生成したパラメータを使用）
 *   AIConfig cfg = AIConfig.fromJson("ai_config.json");
 */
public class AIConfig {

    // ========================================
    // ゾーン判定 敵からの距離
    // ========================================
    public double THREAT_ZONE_1 = 0.3131674262181964;          // 決戦領域
    public double THREAT_ZONE_2 = 0.4695371162081014;          // 戦闘領域

    // ========================================
    // AC DC AT DT の閾値パラメータ
    // ========================================
    public double P01_Z1_AA_AC_2_O = 0.14883511793689416;
    public double P02_Z1_AE_DT_2_U = 0.0557963524115185;
    public double P03_Z1_ER_DT_2_U = 0.5766849761102604;
    public double P05_Z2_CA_AT_1_O = 0.2709426777657023;
    public double P06_Z2_AA_DC_2_U = 0.46789722415558194;
    public double P07_Z2_AE_DT_2_U = 0.8287494640840201;
    public double P08_Z2_EE_DT_2_O = 0.534130312539811;
    public double P10_Z3_CC_DT_2_U = 0.9995123194089797;
    public double P11_Z3_CA_DT_2_U = 0.21976004186125822;

    // ========================================
    // 距離順位による脅威係数
    // ========================================
    public double RANK_1_THREAT = 1.0;
    public double RANK_2_THREAT = 0.7776337621104339;
    public double RANK_3_THREAT = 0.17962003660817152;
    public double RANK_4_THREAT = 0.12375887921562384;

    /*========================================
    public double THREAT_ZONE_1 = 0.3;          // 決戦領域
    public double THREAT_ZONE_2 = 0.7;          // 戦闘領域

    // ========================================
    // AC DC AT DT の閾値パラメータ
    // ========================================
    public double P01_Z1_AA_AC_2_O = 0.9;
    public double P02_Z1_AE_DT_2_U = 0.2;
    public double P03_Z1_ER_DT_2_U = 0.6;
    public double P05_Z2_CA_AT_1_O = 1.0;
    public double P06_Z2_AA_DC_2_U = 0.3;
    public double P07_Z2_AE_DT_2_U = 0.3;
    public double P08_Z2_EE_DT_2_O = 0.7;
    public double P10_Z3_CC_DT_2_U = 0.3;
    public double P11_Z3_CA_DT_2_U = 0.3;

    // ========================================
    // 距離順位による脅威係数
    // ========================================
    public double RANK_1_THREAT = 1.0;
    public double RANK_2_THREAT = 0.4;
    public double RANK_3_THREAT = 0.2;
    public double RANK_4_THREAT = 0.1;
	*/
    
    // ========================================================================
    // JSONファクトリメソッド（外部ライブラリ不使用・手動パース）
    // ========================================================================

    /**
     * JSONファイルからAIConfigを生成する。
     *
     * 依存ライブラリなし。Python側が出力する以下フォーマットに対応:
     * {
     *   "THREAT_ZONE_1": 0.3,
     *   "THREAT_ZONE_2": 0.6,
     *   ...
     * }
     *
     * @param jsonPath JSONファイルのパス
     * @return 読み込んだAIConfigインスタンス
     * @throws RuntimeException ファイル読み込み失敗時
     */
    public static AIConfig fromJson(String jsonPath) {
        AIConfig cfg = new AIConfig();
        try {
            String json = new String(Files.readAllBytes(Paths.get(jsonPath)));
            cfg.THREAT_ZONE_1      = parseDouble(json, "THREAT_ZONE_1",      cfg.THREAT_ZONE_1);
            cfg.THREAT_ZONE_2      = parseDouble(json, "THREAT_ZONE_2",      cfg.THREAT_ZONE_2);
            cfg.P01_Z1_AA_AC_2_O   = parseDouble(json, "P01_Z1_AA_AC_2_O",   cfg.P01_Z1_AA_AC_2_O);
            cfg.P02_Z1_AE_DT_2_U   = parseDouble(json, "P02_Z1_AE_DT_2_U",   cfg.P02_Z1_AE_DT_2_U);
            cfg.P03_Z1_ER_DT_2_U   = parseDouble(json, "P03_Z1_ER_DT_2_U",   cfg.P03_Z1_ER_DT_2_U);
            cfg.P05_Z2_CA_AT_1_O   = parseDouble(json, "P05_Z2_CA_AT_1_O",   cfg.P05_Z2_CA_AT_1_O);
            cfg.P06_Z2_AA_DC_2_U   = parseDouble(json, "P06_Z2_AA_DC_2_U",   cfg.P06_Z2_AA_DC_2_U);
            cfg.P07_Z2_AE_DT_2_U   = parseDouble(json, "P07_Z2_AE_DT_2_U",   cfg.P07_Z2_AE_DT_2_U);
            cfg.P08_Z2_EE_DT_2_O   = parseDouble(json, "P08_Z2_EE_DT_2_O",   cfg.P08_Z2_EE_DT_2_O);
            cfg.P10_Z3_CC_DT_2_U   = parseDouble(json, "P10_Z3_CC_DT_2_U",   cfg.P10_Z3_CC_DT_2_U);
            cfg.P11_Z3_CA_DT_2_U   = parseDouble(json, "P11_Z3_CA_DT_2_U",   cfg.P11_Z3_CA_DT_2_U);
            cfg.RANK_1_THREAT      = parseDouble(json, "RANK_1_THREAT",      cfg.RANK_1_THREAT);
            cfg.RANK_2_THREAT      = parseDouble(json, "RANK_2_THREAT",      cfg.RANK_2_THREAT);
            cfg.RANK_3_THREAT      = parseDouble(json, "RANK_3_THREAT",      cfg.RANK_3_THREAT);
            cfg.RANK_4_THREAT      = parseDouble(json, "RANK_4_THREAT",      cfg.RANK_4_THREAT);
        } catch (IOException e) {
            throw new RuntimeException("AIConfig JSONの読み込みに失敗: " + jsonPath, e);
        }
        return cfg;
    }

    /**
     * JSON文字列から指定キーの double 値を取り出す。
     * キーが見つからない場合はデフォルト値を返す。
     */
    private static double parseDouble(String json, String key, double defaultVal) {
        // "KEY": VALUE の形式を探す（スペース・改行に対応）
        String pattern = "\"" + key + "\"";
        int keyIdx = json.indexOf(pattern);
        if (keyIdx < 0) return defaultVal;

        int colonIdx = json.indexOf(':', keyIdx + pattern.length());
        if (colonIdx < 0) return defaultVal;

        // コロンの後から数値部分を抽出
        int start = colonIdx + 1;
        while (start < json.length() && (json.charAt(start) == ' ' || json.charAt(start) == '\n' || json.charAt(start) == '\r')) {
            start++;
        }
        int end = start;
        while (end < json.length()) {
            char c = json.charAt(end);
            if (c == ',' || c == '}' || c == '\n' || c == '\r') break;
            end++;
        }
        String numStr = json.substring(start, end).trim();
        try {
            return Double.parseDouble(numStr);
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    /**
     * 現在のパラメータをJSON文字列として返す（デバッグ・保存用）。
     */
    public String toJson() {
        return String.format(
            "{\n" +
            "  \"THREAT_ZONE_1\": %.6f,\n" +
            "  \"THREAT_ZONE_2\": %.6f,\n" +
            "  \"P01_Z1_AA_AC_2_O\": %.6f,\n" +
            "  \"P02_Z1_AE_DT_2_U\": %.6f,\n" +
            "  \"P03_Z1_ER_DT_2_U\": %.6f,\n" +
            "  \"P05_Z2_CA_AT_1_O\": %.6f,\n" +
            "  \"P06_Z2_AA_DC_2_U\": %.6f,\n" +
            "  \"P07_Z2_AE_DT_2_U\": %.6f,\n" +
            "  \"P08_Z2_EE_DT_2_O\": %.6f,\n" +
            "  \"P10_Z3_CC_DT_2_U\": %.6f,\n" +
            "  \"P11_Z3_CA_DT_2_U\": %.6f,\n" +
            "  \"RANK_1_THREAT\": %.6f,\n" +
            "  \"RANK_2_THREAT\": %.6f,\n" +
            "  \"RANK_3_THREAT\": %.6f,\n" +
            "  \"RANK_4_THREAT\": %.6f\n" +
            "}",
            THREAT_ZONE_1, THREAT_ZONE_2,
            P01_Z1_AA_AC_2_O, P02_Z1_AE_DT_2_U, P03_Z1_ER_DT_2_U,
            P05_Z2_CA_AT_1_O, P06_Z2_AA_DC_2_U, P07_Z2_AE_DT_2_U,
            P08_Z2_EE_DT_2_O, P10_Z3_CC_DT_2_U, P11_Z3_CA_DT_2_U,
            RANK_1_THREAT, RANK_2_THREAT, RANK_3_THREAT, RANK_4_THREAT
        );
    }

    @Override
    public String toString() {
        return toJson();
    }
}
