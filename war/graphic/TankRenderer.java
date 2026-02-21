package war.graphic;

//======================================================================
// TankRenderer.java - ゲームの描画を担当するクラス
//======================================================================
// TankBattleGame から描画関連のメソッドをすべて分離した純粋な View クラス。
// Graphics2D を受け取り、以下を描画する:
//   - グリッド（マス目・外枠）
//   - 戦車（キャタピラ・本体・砲塔・砲身・HPバー・名前）
//   - 情報パネル（画面下部のステータス表示・操作説明）
//
// ゲームロジック（ダメージ計算・移動判定など）は一切持たない。
//======================================================================

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

import war.tank.Tank;

/**
 * TankRenderer クラス
 * ゲーム画面全体の描画ロジックを集約する。
 * TankBattleGame から生成され、paintComponent の中で使用される。
 */
public class TankRenderer {

    // ======================================================================
    // 定数
    // ======================================================================

    /** グリッドのマス数 */
    private final int GRID_SIZE;

    /** 1マスのピクセルサイズ */
    private final int CELL_SIZE;

    /** パネル全体の横幅 */
    private final int PANEL_WIDTH;

    // ======================================================================
    // コンストラクタ
    // ======================================================================

    /**
     * @param gridSize   グリッドのマス数（例: 20）
     * @param cellSize   1マスあたりのピクセルサイズ（例: 35）
     * @param panelWidth パネル全体の横幅（例: 700）
     */
    public TankRenderer(int gridSize, int cellSize, int panelWidth) {
        this.GRID_SIZE   = gridSize;
        this.CELL_SIZE   = cellSize;
        this.PANEL_WIDTH = panelWidth;
    }

    // ======================================================================
    // 公開描画メソッド群
    // ======================================================================

    /**
     * グリッド（マス目と外枠）を描画する。
     *
     * @param g2d Graphics2Dオブジェクト
     */
    public void drawGrid(Graphics2D g2d) {
        // --- グリッド線 ---
        g2d.setColor(new Color(200, 200, 180));
        g2d.setStroke(new BasicStroke(1));

        for (int i = 0; i <= GRID_SIZE; i++) {
            g2d.drawLine(i * CELL_SIZE, 0,
                         i * CELL_SIZE, GRID_SIZE * CELL_SIZE);
            g2d.drawLine(0, i * CELL_SIZE,
                         GRID_SIZE * CELL_SIZE, i * CELL_SIZE);
        }

        // --- 外枠 ---
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRect(0, 0, GRID_SIZE * CELL_SIZE, GRID_SIZE * CELL_SIZE);
    }

    /**
     * 全戦車を描画する。生存中の戦車のみ描く。
     *
     * @param g2d         Graphics2Dオブジェクト
     * @param tanks       全戦車リスト
     * @param selectedIdx 選択中の戦車インデックス
     */
    public void drawAllTanks(Graphics2D g2d, ArrayList<Tank> tanks, int selectedIdx) {
        for (int i = 0; i < tanks.size(); i++) {
            Tank tank = tanks.get(i);
            if (tank.isAlive()) {
                drawTank(g2d, tank, i == selectedIdx);
            }
        }
    }

    /**
     * 1両の戦車を描画する。
     * キャタピラ → 本体 → 砲塔 → 砲身 → HPバー → 名前 の順で重ね描き。
     *
     * @param g2d        Graphics2Dオブジェクト
     * @param tank       描画する戦車
     * @param isSelected 選択中かどうか（trueなら黄色の強調円を表示）
     */
    public void drawTank(Graphics2D g2d, Tank tank, boolean isSelected) {
        // --- グリッド座標 → ピクセル座標（マス中心） ---
        int pixelX = (int)(tank.getX() * CELL_SIZE + CELL_SIZE / 2);
        int pixelY = (int)(tank.getY() * CELL_SIZE + CELL_SIZE / 2);

        // --- 回転描画のために現在の変換を保存 ---
        AffineTransform savedTransform = g2d.getTransform();
        g2d.translate(pixelX, pixelY);
        g2d.rotate(Math.toRadians(tank.getAngle()));

        // --- サイズ計算 ---
        int size     = CELL_SIZE - 8;
        int halfSize = size / 2;

        // --- 選択中の強調（黄色の円） ---
        if (isSelected) {
            g2d.setColor(Color.YELLOW);
            g2d.setStroke(new BasicStroke(3));
            g2d.drawOval(-halfSize - 5, -halfSize - 5, size + 10, size + 10);
        }

        // --- 戦車色（陣営 × HP割合で決定） ---
        Color tankColor = resolveTankColor(tank);

        // --- キャタピラ（左右） ---
        g2d.setColor(new Color(50, 50, 50));
        g2d.fillRect(-halfSize,     -halfSize + 3, 5,     size - 6);
        g2d.fillRect( halfSize - 5, -halfSize + 3, 5,     size - 6);

        // --- 本体 ---
        g2d.setColor(tankColor);
        g2d.fillRect(-halfSize + 3, -halfSize + 5, size - 6, size - 10);

        // --- 砲塔（円形） ---
        g2d.setColor(tankColor.brighter());
        int turretSize = size / 2;
        g2d.fillOval(-turretSize / 2, -turretSize / 2, turretSize, turretSize);

        // --- 砲身（中心から上方向の線） ---
        g2d.setColor(new Color(80, 80, 80));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawLine(0, 0, 0, -halfSize - 3);

        // --- 本体の枠線 ---
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRect(-halfSize + 3, -halfSize + 5, size - 6, size - 10);

        // --- 変換を元に戻す（HPバー・名前は回転させない） ---
        g2d.setTransform(savedTransform);

        // --- HPバー（戦車上部） ---
        drawHealthBar(g2d, pixelX, pixelY - halfSize - 10, size, tank);

        // --- 戦車名（戦車下部・中央揃え） ---
        drawTankName(g2d, tank, pixelX, pixelY, halfSize);
    }

    /**
     * 画面下部の情報パネルを描画する。
     * 左側: 選択中戦車のステータス  右側: 操作説明
     *
     * @param g2d          Graphics2Dオブジェクト
     * @param selectedTank 選択中の戦車（null または死亡時はステータス非表示）
     * @param panelHeight  情報パネルの高さ（ピクセル）
     */
    public void drawInfoPanel(Graphics2D g2d, Tank selectedTank, int panelHeight) {
        int panelY = GRID_SIZE * CELL_SIZE;

        // --- 背景（濃いグレー） ---
        g2d.setColor(new Color(50, 50, 50));
        g2d.fillRect(0, panelY, PANEL_WIDTH, panelHeight);

        // --- 選択戦車のステータス（左側） ---
        if (selectedTank != null && selectedTank.isAlive()) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("MS Gothic", Font.BOLD, 14));

            int textX = 20;
            int textY = panelY + 25;

            g2d.drawString("【選択中】 " + selectedTank.getName(), textX, textY); textY += 20;
            g2d.drawString(String.format("HP: %d/%d",
                selectedTank.getHp(), selectedTank.getMaxHp()), textX, textY);   textY += 20;
            g2d.drawString(String.format("攻撃力: %d  防御力: %d",
                selectedTank.getAttack(), selectedTank.getDefense()), textX, textY); textY += 20;
            g2d.drawString(String.format("位置: (%.1f, %.1f)  角度: %.0f°",
                selectedTank.getX(), selectedTank.getY(), selectedTank.getAngle()),
                textX, textY); textY += 20;
            g2d.drawString(String.format("弾薬: %d", selectedTank.getAmmo()),
                textX, textY); textY += 20;
            g2d.drawString(String.format("行動力: %d", selectedTank.activity()),
                textX, textY);
        }

        // --- 操作説明（右側） ---
        g2d.setColor(Color.YELLOW);
        g2d.setFont(new Font("MS Gothic", Font.PLAIN, 11));

        int helpX = PANEL_WIDTH - 280;
        int helpY = panelY + 20;

        g2d.drawString("【操作】",                              helpX, helpY); helpY += 18;
        g2d.drawString("矢印キー: 移動　　　必要行動力　１",   helpX, helpY); helpY += 18;
        g2d.drawString("クリック: 敵を攻撃  必要行動力　４",   helpX, helpY); helpY += 18;
        g2d.drawString("Ｒ： 修理 　　　　　残存行動力すべて", helpX, helpY); helpY += 18;
        g2d.drawString("スペース: 行動終了",                   helpX, helpY);
    }

    // ======================================================================
    // 非公開ヘルパー
    // ======================================================================

    /**
     * 陣営（友軍/敵）と HP 割合に応じた戦車の塗り色を決定する。
     * 友軍: 緑系 / 敵軍: 青系 をベースに、HP 低下で色が変化する。
     */
    private Color resolveTankColor(Tank tank) {
        double hpRatio    = (double) tank.getHp() / tank.getMaxHp();
        boolean isFriendly = (tank.jinei() == 0);

        if (isFriendly) {
            // 友軍カラー（緑系）
            if (hpRatio > 0.7) return new Color(34, 139, 34);   // 健康: 深緑
            if (hpRatio > 0.3) return new Color(184, 134, 11);  // 注意: 黄土
            return                     new Color(178, 34, 34);   // 危険: 赤
        } else {
            // 敵軍カラー（青系）— 友軍と明確に区別
            if (hpRatio > 0.7) return new Color(30, 100, 180);  // 健康: 青
            if (hpRatio > 0.3) return new Color(120, 80, 180);  // 注意: 紫
            return                     new Color(160, 30, 80);   // 危険: 赤紫
        }
    }

    /**
     * HPバーを戦車の上部に描画する。
     * 背景（赤）の上に現在 HP 分の緑バーを重ねる。
     */
    private void drawHealthBar(Graphics2D g2d, int x, int y, int width, Tank tank) {
        int barHeight = 5;

        // 背景（赤 = 失った HP）
        g2d.setColor(Color.RED);
        g2d.fillRect(x - width / 2, y, width, barHeight);

        // 現在 HP（緑）
        double hpRatio = (double) tank.getHp() / tank.getMaxHp();
        g2d.setColor(Color.GREEN);
        g2d.fillRect(x - width / 2, y, (int)(width * hpRatio), barHeight);

        // 枠線
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x - width / 2, y, width, barHeight);
    }

    /**
     * 戦車名を本体下部に中央揃えで描画する。
     */
    private void drawTankName(Graphics2D g2d, Tank tank,
                               int pixelX, int pixelY, int halfSize) {
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("MS Gothic", Font.BOLD, 10));
        FontMetrics fm        = g2d.getFontMetrics();
        int         nameWidth = fm.stringWidth(tank.getName());
        g2d.drawString(tank.getName(),
                       pixelX - nameWidth / 2,
                       pixelY + halfSize + 15);
    }
}
