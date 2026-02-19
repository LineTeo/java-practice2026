# 🧠 戦車ゲーム AI開発ロードマップ

## 🎮 現在の状況

- ターン制の戦車バトルゲーム（20×20マップ）
- CUI版は完全自作
- GUI版は java.awt を使用（表示部分のみAI活用）
- バトルロジックは自作
- 継承を使用した複数戦車クラス構成
- 現在は 1対複数
- 敵AIは固定ロジック（突進して攻撃）

---

# 🚀 フェーズ1：AIを戦車クラスから分離する

## 🎯 目的
AIを差し替え可能にする

### Strategyインターフェース作成

```java
interface TankStrategy {
    Action decide(Tank self, Tank enemy);
}
実装例
class AggressiveStrategy implements TankStrategy {
    public Action decide(Tank self, Tank enemy) {
        return Action.ATTACK;
    }
}

class DefensiveStrategy implements TankStrategy {
    public Action decide(Tank self, Tank enemy) {
        if (self.getHp() < 30) return Action.REPAIR;
        return Action.ATTACK;
    }
}
Tankに持たせる
class Tank {
    TankStrategy strategy;
}
✅ AIの性格を差し替え可能
✅ 実験ができる土台完成

🧮 フェーズ2：スコアリングAI
固定ルールではなく、行動を評価して選ぶ。

double scoreAttack = 0;
double scoreMoveCloser = 0;
double scoreMoveAway = 0;
double scoreRepair = 0;
状況による加点例
if (self.getHp() < 30) {
    scoreRepair += 50;
    scoreMoveAway += 20;
}

if (enemy.getHp() < 20) {
    scoreAttack += 40;
}

if (distance > 5) {
    scoreMoveCloser += 30;
}
最大スコアの行動を選択。

✅ 「考えている」AIになる
✅ 強化学習の基礎になる

📊 フェーズ3：戦闘データを記録する
class BattleStats {
    int totalBattles;
    int wins;
    int totalAttacks;
    int hits;
}
記録するもの：

勝率

命中率

平均ターン数

✅ AIの強さを数値化
✅ 実験が可能になる

🤖 フェーズ4：自己対戦シミュレーション
AI同士を100回以上戦わせる

勝率を表示

スコアパラメータを調整

実験例：

修理スコアを上げる

距離評価を変更

勝率を比較する

✅ AI調整が科学的になる
✅ 振る舞いの変化が見える

🏆 フェーズ5：強化学習へ
要素定義
状態
HP差

距離

行動
ATTACK

MOVE

REPAIR

報酬
勝利：+1

敗北：-1

ここで：

Q学習

Python（NumPy）

機械学習実験

へ進む

🧩 AIの本質
AIとは：

状態 → 評価 → 行動

あなたはすでに
「評価」を自分で設計できる段階にいる。

強化学習は、この評価を自動化する技術。

💪 あなたの強み
オブジェクト指向設計ができる

抽象化が理解できる

拡張性の価値を体感している

作りながら学べる

実験思考がある

これはAI開発に非常に向いている。

🎯 今すぐやること
TankStrategyを実装

固定AIをスコア型に変更

100回自動対戦

勝率を比較

パラメータ調整

🌟 長期ビジョン
適応型AI

戦闘履歴を学習

自己改善型AI

Python移行による機械学習実験
