package war.ai;

//======================================================================
// EnemyAI.java - 敵戦車の行動ロジックを担当するクラス
//======================================================================
// 実装した2つの改善点:
//
//  [1] 射程・位置を考慮した最適攻撃ポジション取り
//      - 射程の 60〜80% の距離を「最適射程帯」として維持する
//      - 近すぎる場合は後退、遠すぎる場合は前進
//      - グリッド端に追い詰められないよう退避方向に中央バイアスをかける
//
//  [2] HP に応じた動的な戦術切り替え（3段階）
//      - HIGH  (HP > 70%): 積極的に接近・連射
//      - MID   (HP 30-70%): 最適射程を維持しつつ堅実に攻撃
//      - LOW   (HP < 30%): 退避してから修理
//======================================================================

import java.util.ArrayList;
import war.tank.Tank;

/**
 * EnemyAI クラス
 * 敵の1ターン分の行動を決定・実行する。
 * ダメージ計算等は Tank クラスへ委譲する。
 */
public class EnemyAI {

    // ======================================================================
    // 定数（戦術パラメータ）
    // ======================================================================

    /** この HP 割合以下で「危険状態」→ 退避・修理フェーズへ */
    private static final double HP_LOW  = 0.30;

    /** この HP 割合以上で「高 HP 状態」→ 積極攻撃フェーズへ */
    private static final double HP_HIGH = 0.70;

    /**
     * 最適射程の下限倍率（射程 × この値）。
     * これより近い場合は「近すぎ」と判断して後退する。
     */
    private static final double OPT_RANGE_MIN = 0.60;

    /**
     * 最適射程の上限倍率（射程 × この値）。
     * これより遠い場合は「遠すぎ」と判断して前進する。
     */
    private static final double OPT_RANGE_MAX = 0.80;

    /** 退避判定距離: 脅威がこの距離以内なら退避を優先する */
    private static final double DANGER_DIST = 8.0;

    /** グリッドの最大座標（端の判定に使用） */
    private final int MAX_GRID;

    // ======================================================================
    // コンストラクタ
    // ======================================================================

    /**
     * @param maxGrid グリッドサイズ - 1（例: 19）
     */
    public EnemyAI(int maxGrid) {
        this.MAX_GRID = maxGrid;
    }

    // ======================================================================
    // 公開メソッド
    // ======================================================================

    /**
     * 敵戦車 1 両の 1 ターン分の行動を実行する。
     * HP フェーズに応じて戦術を動的に切り替える。
     *
     * @param enemy      行動させる敵戦車
     * @param friendlies 攻撃対象候補の味方戦車リスト
     * @return 0: 正常終了
     */
    public int takeTurn(Tank enemy, ArrayList<Tank> friendlies) {
        if (!enemy.isAlive()) return 0;

        Tank target = selectTarget(enemy, friendlies);
        if (target == null) return 0;

        double hpRatio = (double) enemy.getHp() / enemy.getMaxHp();

        if (hpRatio < HP_LOW) {
            // ---- フェーズ LOW: 退避 → 修理 ----
            phaseLow(enemy, target);

        } else if (hpRatio < HP_HIGH) {
            // ---- フェーズ MID: 最適射程を維持しながら堅実攻撃 ----
            phaseMid(enemy, target);

        } else {
            // ---- フェーズ HIGH: 積極接近 → 連射 ----
            phaseHigh(enemy, target);
        }

        return 0;
    }

    // ======================================================================
    // HP フェーズ別行動
    // ======================================================================

    /**
     * LOW フェーズ（HP < 30%）: 退避 → 修理。
     * 脅威が近い場合はまず距離を取り、その後全行動力で修理する。
     */
    private void phaseLow(Tank enemy, Tank threat) {
        log(enemy, "フェーズ: LOW（HP危険）");
        if (enemy.distance(threat) <= DANGER_DIST && enemy.activity() > 0) {
            retreat(enemy, threat);
        }
        if (enemy.activity() > 0) {
            log(enemy, "修理実行");
            enemy.repair();
        }
    }

    /**
     * MID フェーズ（HP 30-70%）: 最適射程帯を維持して堅実に攻撃。
     * 近すぎれば後退、遠すぎれば前進してから攻撃する。
     */
    private void phaseMid(Tank enemy, Tank target) {
        log(enemy, "フェーズ: MID（最適射程維持）");
        adjustToOptimalRange(enemy, target);   // [1] 射程最適化
        if (isInRange(enemy, target)) {
            attackRepeatedly(enemy, target);
        }
    }

    /**
     * HIGH フェーズ（HP > 70%）: 積極的に接近して連射する。
     */
    private void phaseHigh(Tank enemy, Tank target) {
        log(enemy, "フェーズ: HIGH（積極攻撃）");
        approachToRange(enemy, target);
        if (isInRange(enemy, target)) {
            attackRepeatedly(enemy, target);
        }
    }

    // ======================================================================
    // [1] 最適射程ポジション取り
    // ======================================================================

    /**
     * 最適射程帯（射程の 60〜80%）に収まるよう位置を調整する。
     * 近すぎ → 後退、遠すぎ → 前進。
     */
    private void adjustToOptimalRange(Tank enemy, Tank target) {
        double range  = enemy.getRange();
        double optMin = range * OPT_RANGE_MIN;
        double optMax = range * OPT_RANGE_MAX;
        double dist   = enemy.distance(target);

        if (dist < optMin && enemy.activity() >= 1) {
            log(enemy, String.format("後退（現距離 %.1f < 最適下限 %.1f）", dist, optMin));
            retreat(enemy, target);

        } else if (dist > optMax && enemy.activity() >= 1) {
            log(enemy, String.format("前進（現距離 %.1f > 最適上限 %.1f）", dist, optMax));
            approachToRange(enemy, target);
        }
    }

    /**
     * 射程内（射程の上限）まで 1 マスずつ前進する。
     */
    private void approachToRange(Tank enemy, Tank target) {
        while (enemy.activity() >= 1 && !isInRange(enemy, target)) {
            double nx = clamp(target.getX(), 0, MAX_GRID);
            double ny = clamp(target.getY(), 0, MAX_GRID);
            if (enemy.move(nx, ny) != 0) break;
        }
    }

    /**
     * 脅威から離れる方向に退避する。
     * グリッド端に追い詰められないよう中央バイアス（1マス余裕）をかける。
     */
    private void retreat(Tank enemy, Tank threat) {
        int steps = 0;
        while (enemy.activity() >= 1
                && enemy.distance(threat) <= DANGER_DIST
                && steps < 4) {

            double escX = enemy.getX() - (threat.getX() - enemy.getX());
            double escY = enemy.getY() - (threat.getY() - enemy.getY());

            // グリッド端 1マスは踏まないようにクランプ
            escX = clamp(escX, 1, MAX_GRID - 1);
            escY = clamp(escY, 1, MAX_GRID - 1);

            if (enemy.move(escX, escY) != 0) break;
            steps++;
        }
        log(enemy, String.format("退避後: (%.1f, %.1f)", enemy.getX(), enemy.getY()));
    }

    // ======================================================================
    // 攻撃サブルーチン
    // ======================================================================

    /**
     * 行動力と弾薬が続く限り攻撃を繰り返す（連射）。
     */
    private void attackRepeatedly(Tank enemy, Tank target) {
        while (enemy.activity() >= 4
                && enemy.getAmmo() > 0
                && target.isAlive()) {
            log(enemy, String.format("攻撃: %s (距離 %.1f)",
                target.getName(), enemy.distance(target)));
            if (enemy.attackTarget(target) != 0) break;
        }
    }

    // ======================================================================
    // ターゲット選択
    // ======================================================================

    /**
     * 攻撃対象を選定する。
     * HP 割合が低く（倒しやすく）、距離が近い相手を優先する。
     * スコア = HP割合 × 5 + 距離（値が小さいほど優先）
     */
    private Tank selectTarget(Tank enemy, ArrayList<Tank> friendlies) {
        Tank   best      = null;
        double bestScore = Double.MAX_VALUE;

        for (Tank t : friendlies) {
            if (!t.isAlive()) continue;
            double hpRatio = (double) t.getHp() / t.getMaxHp();
            double dist    = enemy.distance(t);
            double score   = hpRatio * 5.0 + dist;
            if (score < bestScore) {
                bestScore = score;
                best      = t;
            }
        }
        return best;
    }

    // ======================================================================
    // ユーティリティ
    // ======================================================================

    /** 戦車が目標に対して有効射程内かどうかを判定する */
    private boolean isInRange(Tank enemy, Tank target) {
        return enemy.distance(target) <= (enemy.getRange() - 1);
    }

    /** 値を [min, max] の範囲にクランプする */
    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /** デバッグ用ログ出力 */
    private void log(Tank enemy, String msg) {
        System.out.println("[AI] " + enemy.getName() + " → " + msg);
    }
}
