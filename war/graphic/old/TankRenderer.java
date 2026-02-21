package war.graphic;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

import war.tank.Tank;

/**
 * TankRenderer - 戦車ゲームの描画を担当するクラス
 * すべての描画処理をここに集約
 */
public class TankRenderer {
    
    // ======================================================================
    // 定数定義
    // ======================================================================
    
    /** グリッドのマス数（縦横20マス） */
    private static final int GRID_SIZE = 20;
    
    /** 1マスあたりのピクセルサイズ */
    private static final int CELL_SIZE = 35;
    
    /** パネル全体の横幅 */
    private static final int PANEL_WIDTH = GRID_SIZE * CELL_SIZE;
    
    
    // ======================================================================
    // グリッド描画
    // ======================================================================
    
    /**
     * グリッド（マス目）を描画する
     * 
     * @param g2d Graphics2Dオブジェクト
     */
    public void drawGrid(Graphics2D g2d) {
        // グリッド線の描画
        g2d.setColor(new Color(200, 200, 180));
        g2d.setStroke(new BasicStroke(1));
        
        for (int i = 0; i <= GRID_SIZE; i++) {
            // 縦線
            g2d.drawLine(i * CELL_SIZE, 0, 
                        i * CELL_SIZE, GRID_SIZE * CELL_SIZE);
            // 横線
            g2d.drawLine(0, i * CELL_SIZE, 
                        GRID_SIZE * CELL_SIZE, i * CELL_SIZE);
        }
        
        // 外枠の描画
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRect(0, 0, GRID_SIZE * CELL_SIZE, GRID_SIZE * CELL_SIZE);
    }
    
    
    // ======================================================================
    // 戦車描画
    // ======================================================================
    
    /**
     * 戦車を描画する
     * 
     * @param g2d Graphics2Dオブジェクト
     * @param tank 描画する戦車
     * @param isSelected この戦車が選択されているか
     */
    public void drawTank(Graphics2D g2d, Tank tank, boolean isSelected) {
        // 戦車の中心位置を計算
        int pixelX = (int)(tank.getX() * CELL_SIZE + CELL_SIZE / 2);
        int pixelY = (int)(tank.getY() * CELL_SIZE + CELL_SIZE / 2);
        
        // 座標変換の準備（回転のため）
        AffineTransform oldTransform = g2d.getTransform();
        g2d.translate(pixelX, pixelY);
        g2d.rotate(Math.toRadians(tank.getAngle()));
        
        // 戦車のサイズ計算
        int size = CELL_SIZE - 8;
        int halfSize = size / 2;
        
        // 選択中の戦車を強調表示
        if (isSelected) {
            g2d.setColor(Color.YELLOW);
            g2d.setStroke(new BasicStroke(3));
            g2d.drawOval(-halfSize - 5, -halfSize - 5, size + 10, size + 10);
        }
        
        // 戦車の色を決定（HPによって変化）
        Color tankColor = getTankColor(tank);
        
        // キャタピラの描画
        drawTracks(g2d, size, halfSize);
        
        // 戦車本体の描画
        drawBody(g2d, tankColor, size, halfSize);
        
        // 砲塔の描画
        drawTurret(g2d, tankColor, size);
        
        // 砲身の描画
        drawBarrel(g2d, halfSize);
        
        // 本体の縁取り
        drawBodyOutline(g2d, size, halfSize);
        
        // 座標変換を元に戻す
        g2d.setTransform(oldTransform);
        
        // HPバーの描画
        drawHealthBar(g2d, pixelX, pixelY - halfSize - 10, size, tank);
        
        // 名前の描画
        drawTankName(g2d, tank, pixelX, pixelY, halfSize);
    }
    
    
    /**
     * HPに応じた戦車の色を取得
     */
    private Color getTankColor(Tank tank) {
        double hpRatio = (double)tank.getHp() / tank.getMaxHp();
        
        if (hpRatio > 0.7) {
            return new Color(34, 139, 34);      // 緑色（健康）
        } else if (hpRatio > 0.3) {
            return new Color(184, 134, 11);     // 黄土色（注意）
        } else {
            return new Color(178, 34, 34);      // 赤色（危険）
        }
    }
    
    
    /**
     * キャタピラを描画
     */
    private void drawTracks(Graphics2D g2d, int size, int halfSize) {
        g2d.setColor(new Color(50, 50, 50));
        
        // 左側のキャタピラ
        g2d.fillRect(-halfSize, -halfSize + 3, 5, size - 6);
        
        // 右側のキャタピラ
        g2d.fillRect(halfSize - 5, -halfSize + 3, 5, size - 6);
    }
    
    
    /**
     * 戦車本体を描画
     */
    private void drawBody(Graphics2D g2d, Color tankColor, int size, int halfSize) {
        g2d.setColor(tankColor);
        g2d.fillRect(-halfSize + 3, -halfSize + 5, size - 6, size - 10);
    }
    
    
    /**
     * 砲塔を描画
     */
    private void drawTurret(Graphics2D g2d, Color tankColor, int size) {
        g2d.setColor(tankColor.brighter());
        int turretSize = size / 2;
        g2d.fillOval(-turretSize / 2, -turretSize / 2, turretSize, turretSize);
    }
    
    
    /**
     * 砲身を描画
     */
    private void drawBarrel(Graphics2D g2d, int halfSize) {
        g2d.setColor(new Color(80, 80, 80));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawLine(0, 0, 0, -halfSize - 3);
    }
    
    
    /**
     * 本体の縁取りを描画
     */
    private void drawBodyOutline(Graphics2D g2d, int size, int halfSize) {
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRect(-halfSize + 3, -halfSize + 5, size - 6, size - 10);
    }
    
    
    /**
     * 戦車の名前を描画
     */
    private void drawTankName(Graphics2D g2d, Tank tank, int pixelX, int pixelY, int halfSize) {
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("MS Gothic", Font.BOLD, 10));
        
        FontMetrics fm = g2d.getFontMetrics();
        int nameWidth = fm.stringWidth(tank.getName());
        
        g2d.drawString(tank.getName(), pixelX - nameWidth/2, pixelY + halfSize + 15);
    }
    
    
    // ======================================================================
    // HPバー描画
    // ======================================================================
    
    /**
     * HPバーを描画する
     * 
     * @param g2d Graphics2Dオブジェクト
     * @param x バーの中心X座標
     * @param y バーの上端Y座標
     * @param width バーの幅
     * @param tank 対象の戦車
     */
    public void drawHealthBar(Graphics2D g2d, int x, int y, int width, Tank tank) {
        int barWidth = width;
        int barHeight = 5;
        
        // 背景（赤）
        g2d.setColor(Color.RED);
        g2d.fillRect(x - barWidth/2, y, barWidth, barHeight);
        
        // 現在HP（緑）
        double hpRatio = (double)tank.getHp() / tank.getMaxHp();
        g2d.setColor(Color.GREEN);
        g2d.fillRect(x - barWidth/2, y, (int)(barWidth * hpRatio), barHeight);
        
        // 枠線
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x - barWidth/2, y, barWidth, barHeight);
    }
    
    
    // ======================================================================
    // 情報パネル描画
    // ======================================================================
    
    /**
     * 画面下部に情報パネルを描画する
     * 
     * @param g2d Graphics2Dオブジェクト
     * @param selectedTank 選択中の戦車
     */
    public void drawInfoPanel(Graphics2D g2d, Tank selectedTank) {
        int panelY = GRID_SIZE * CELL_SIZE;
        
        // 背景の描画
        g2d.setColor(new Color(50, 50, 50));
        g2d.fillRect(0, panelY, PANEL_WIDTH, 150);
        
        // 選択中の戦車情報
        if (selectedTank != null && selectedTank.isAlive()) {
            drawTankInfo(g2d, selectedTank, panelY);
        }
        
        // 操作説明
        drawControls(g2d, panelY);
    }
    
    
    /**
     * 戦車の詳細情報を描画
     */
    private void drawTankInfo(Graphics2D g2d, Tank tank, int panelY) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("MS Gothic", Font.BOLD, 14));
        
        int textX = 20;
        int textY = panelY + 25;
        
        // 戦車名
        g2d.drawString("【選択中】 " + tank.getName(), textX, textY);
        textY += 20;
        
        // HP表示
        g2d.drawString(String.format("HP: %d/%d", 
            tank.getHp(), tank.getMaxHp()), textX, textY);
        textY += 20;
        
        // 攻撃力と防御力
        g2d.drawString(String.format("攻撃力: %d  防御力: %d", 
            tank.getAttack(), tank.getDefense()), textX, textY);
        textY += 20;
        
        // 位置と角度
        g2d.drawString(String.format("位置: (%.1f, %.1f)  角度: %.0f°", 
            tank.getX(), tank.getY(), tank.getAngle()), textX, textY);
        textY += 20;
        
        // 弾薬
        g2d.drawString(String.format("弾薬: %d", tank.getAmmo()), textX, textY);
        textY += 20;
        
        // 行動力
        g2d.drawString(String.format("行動力: %d", tank.activity()), textX, textY);
    }
    
    
    /**
     * 操作説明を描画
     */
    private void drawControls(Graphics2D g2d, int panelY) {
        g2d.setColor(Color.YELLOW);
        g2d.setFont(new Font("MS Gothic", Font.PLAIN, 11));
        
        int helpX = PANEL_WIDTH - 280;
        int helpY = panelY + 20;
        
        g2d.drawString("【操作】", helpX, helpY);
        helpY += 18;
        g2d.drawString("矢印キー: 移動　　　必要行動力　１", helpX, helpY);
        helpY += 18;
        g2d.drawString("クリック: 敵を攻撃  必要行動力　４", helpX, helpY);
        helpY += 18;
        g2d.drawString("Ｒ： 修理 　　　　　残存行動力すべて", helpX, helpY);
        helpY += 18;
        g2d.drawString("スペース: 行動終了", helpX, helpY);
    }
    
    
    // ======================================================================
    // ユーティリティメソッド
    // ======================================================================
    
    /**
     * すべての戦車を描画する
     * 
     * @param g2d Graphics2Dオブジェクト
     * @param tanks 戦車のリスト
     * @param selectedIndex 選択中の戦車のインデックス
     */
    public void drawAllTanks(Graphics2D g2d, ArrayList<Tank> tanks, int selectedIndex) {
        for (int i = 0; i < tanks.size(); i++) {
            Tank tank = tanks.get(i);
            
            if (tank.isAlive()) {
                boolean isSelected = (i == selectedIndex);
                drawTank(g2d, tank, isSelected);
            }
        }
    }
}