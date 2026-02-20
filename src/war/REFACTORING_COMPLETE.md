# 戦車ゲーム リファクタリング解説（完全版）

## 変更の概要

### リファクタリング前
```
TankBattleGame (1500行超)
├─ 描画処理 (drawGrid, drawTank, drawHealthBar...)
├─ プレイヤー操作 (keyPressed, mouseClicked...)
└─ 敵AI (enemyTank, assaultAction...)
```

### リファクタリング後
```
TankBattleGame (200行)
├─ TankRenderer      (描画専任)
├─ PlayerController  (プレイヤー操作専任) ★NEW
└─ EnemyAI           (敵AI専任)
```

---

## 新クラス構成

| クラス | パッケージ | 責務 | 行数 |
|---|---|---|---|
| `TankBattleGame` | `war.graphic` | ゲームフロー制御 | 約200行 |
| `TankRenderer` | `war.graphic` | 描画（View） | 約300行 |
| `PlayerController` | `war.control` | プレイヤー操作（Controller） | 約250行 |
| `EnemyAI` | `war.ai` | 敵戦術AI | 約280行 |
| `TankController` | `war.control` | 統一インターフェース | 約30行 |
| `PlayerAI` | `war.control` | プレイヤー自動操作（スケルトン） | 約100行 |

---

## 1. TankRenderer (`war.graphic`)

### 責務
**描画ロジックをすべて集約した純粋な View 層。**

```java
// グリッド、戦車、HPバー、情報パネルを描画
renderer.drawGrid(g2d);
renderer.drawAllTanks(g2d, tanks, selectedIndex);
renderer.drawInfoPanel(g2d, selectedTank, INFO_HEIGHT);
```

### 改善点
- 友軍（緑系）と敵軍（青系）で色を区別
- ゲームロジックを一切持たない

---

## 2. PlayerController (`war.control`)

### 責務
**プレイヤーのキーボード・マウス入力を処理し、戦車を操作する。**

```java
// TankBattleGame から入力を委譲
playerController.handleKeyInput(keyCode);
playerController.handleMouseClick(mouseX, mouseY, tanks);
```

### 主なメソッド

| メソッド | 説明 |
|---|---|
| `setControlledTank(tank)` | 操作対象の戦車を設定 |
| `handleKeyInput(keyCode)` | 矢印キー移動、R:修理、A:補給 |
| `handleMouseClick(x, y, tanks)` | クリック位置の敵を攻撃 |

### 分離のメリット
- **AI への差し替えが容易**
  ```java
  // プレイヤー操作
  PlayerController controller = new PlayerController(20, 35);
  
  // AI 操作に切り替え（将来）
  PlayerAI controller = new PlayerAI(20);
  ```

---

## 3. EnemyAI (`war.ai`)

### 責務
**敵戦車の戦術判断と実行。**

### 実装した2つの改善

#### [1] 射程・位置を考慮した最適攻撃ポジション取り
```
最適射程帯 = 射程 × 60〜80%
  近すぎ → retreat() で後退
  遠すぎ → approachToRange() で前進
  端に追い詰められないよう中央バイアス付きクランプ
```

#### [2] HP に応じた動的な戦術切り替え
```
HIGH (HP > 70%) → 積極接近して連射
MID  (HP 30-70%) → 最適射程を維持して堅実攻撃
LOW  (HP < 30%)  → 退避してから修理
```

---

## 4. TankController インターフェース (`war.control`)

### 責務
**プレイヤー操作と AI 操作を統一するインターフェース。**

```java
public interface TankController {
    void setControlledTank(Tank tank);
    Tank getControlledTank();
    int takeTurn();  // プレイヤー: 入力待機 / AI: 自動判断
}
```

### Strategy パターン
```java
// 実行時に切り替え可能
TankController controller;

if (isPlayerControlled) {
    controller = new PlayerController(20, 35);
} else {
    controller = new PlayerAI(20);
}

controller.setControlledTank(tank);
controller.takeTurn();
```

---

## 5. PlayerAI (`war.control`) — スケルトン実装

### 責務
**プレイヤー側戦車を自動操作する AI（将来の拡張用）。**

現在は `EnemyAI` のロジックを流用しているが、独自の戦術実装も可能:
- より慎重な立ち回り（HP 50% で退避）
- 優先ターゲットの変更（攻撃力高い敵を優先）
- 補給タイミングの最適化

---

## TankBattleGame の変更点

### 削除したメソッド（→ 各クラスへ移管）

| 旧メソッド | 移管先 |
|---|---|
| `drawGrid()` | `TankRenderer.drawGrid()` |
| `drawTank()` | `TankRenderer.drawTank()` |
| `drawHealthBar()` | `TankRenderer`（private） |
| `drawInfoPanel()` | `TankRenderer.drawInfoPanel()` |
| `keyPressed()` の処理 | `PlayerController.handleKeyInput()` |
| `mouseClicked()` の処理 | `PlayerController.handleMouseClick()` |
| `enemyTank()` | `EnemyAI.takeTurn()` |
| `assaultAction()` | `EnemyAI`（内部処理） |
| `recoveryAction()` | `EnemyAI`（内部処理） |
| `tankDir()` | `PlayerController.setDirection()` |

### 残った責務
- ゲーム初期化 (`startGame()`)
- ターン管理 (`endPlayerTurn()`)
- 勝敗判定 (`gameEndChk()`)
- イベントリスナーの委譲

---

## ファイル構成

```
war/
├── graphic/
│   ├── TankBattleGame.java   ← ゲームフロー制御
│   └── TankRenderer.java     ← 描画専任
├── control/
│   ├── TankController.java   ← 統一インターフェース ★NEW
│   ├── PlayerController.java ← プレイヤー操作 ★NEW
│   └── PlayerAI.java         ← プレイヤー自動操作 ★NEW
├── ai/
│   └── EnemyAI.java          ← 敵戦術AI
└── tank/
    ├── Tank.java
    ├── Tiger.java
    ├── MediumTank.java
    └── HeavyTank.java
```

---

## コンパイル・実行

```bash
# プロジェクトルートで
javac war/tank/*.java \
      war/graphic/TankRenderer.java \
      war/control/*.java \
      war/ai/EnemyAI.java \
      war/graphic/TankBattleGame.java

# 実行
java war.graphic.TankBattleGame
```

---

## 将来的な拡張例

### プレイヤー側を AI に切り替える

```java
// TankBattleGame.java のコンストラクタで
// PlayerController の代わりに PlayerAI を使用

// 現在（プレイヤー操作）
playerController = new PlayerController(GRID_SIZE, CELL_SIZE);

// AI 操作に切り替え
PlayerAI playerAI = new PlayerAI(GRID_SIZE);
playerAI.setEnemies(getEnemyTanks());
playerController = playerAI;  // TankController 型で統一
```

### AI 同士の対戦

```java
// 両陣営を AI 化
PlayerAI friendlyAI = new PlayerAI(GRID_SIZE);
EnemyAI  enemyAI    = new EnemyAI(GRID_SIZE - 1);

// ターン交互実行
friendlyAI.takeTurn();
enemyAI.takeTurn();
```

---

## 設計の利点

### 1. 単一責任の原則（SRP）
各クラスが1つの責務のみを持つ。

### 2. 開放閉鎖の原則（OCP）
新しい AI 戦術や操作方法を、既存コードを変更せず追加可能。

### 3. Strategy パターン
`TankController` インターフェースで操作方法を実行時に切り替え可能。

### 4. テスト容易性
各クラスを独立してテスト可能（View / Controller / AI）。

### 5. 保守性
1500行 → 200行のメインクラスになり、変更箇所の特定が容易。
