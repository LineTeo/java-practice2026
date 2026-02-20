package war.graphic;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class TankGame extends JPanel implements KeyListener {
    private static final int GRID_SIZE = 20;
    private static final int CELL_SIZE = 30;
    private static final int PANEL_SIZE = GRID_SIZE * CELL_SIZE;
    
    // 戦車の位置
    private int tankX = 10;
    private int tankY = 10;
    
    // 戦車の向き（0:上, 1:右, 2:下, 3:左）
    private int tankDirection = 0;
    
    public TankGame() {
        setPreferredSize(new Dimension(PANEL_SIZE, PANEL_SIZE));
        setFocusable(true);
        addKeyListener(this);
        setBackground(Color.WHITE);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // アンチエイリアスを有効化
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                            RenderingHints.VALUE_ANTIALIAS_ON);
        
        // グリッドを描画
        drawGrid(g2d);
        
        // 戦車を描画
        drawTank(g2d, tankX, tankY, tankDirection);
    }
    
    // グリッドを描画
    private void drawGrid(Graphics2D g2d) {
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setStroke(new BasicStroke(1));
        
        // 縦線
        for (int i = 0; i <= GRID_SIZE; i++) {
            g2d.drawLine(i * CELL_SIZE, 0, i * CELL_SIZE, PANEL_SIZE);
        }
        
        // 横線
        for (int i = 0; i <= GRID_SIZE; i++) {
            g2d.drawLine(0, i * CELL_SIZE, PANEL_SIZE, i * CELL_SIZE);
        }
        
        // 外枠を太く
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRect(0, 0, PANEL_SIZE, PANEL_SIZE);
    }
    
    // 戦車を描画
    private void drawTank(Graphics2D g2d, int gridX, int gridY, int direction) {
        int x = gridX * CELL_SIZE + CELL_SIZE / 2;
        int y = gridY * CELL_SIZE + CELL_SIZE / 2;
        
        // 保存して回転
        AffineTransform oldTransform = g2d.getTransform();
        g2d.translate(x, y);
        g2d.rotate(Math.toRadians(direction * 90));
        
        int size = CELL_SIZE - 6;
        int halfSize = size / 2;
        
        // 戦車の本体（四角形）
        g2d.setColor(new Color(34, 139, 34)); // 深緑
        g2d.fillRect(-halfSize + 3, -halfSize + 5, size - 6, size - 10);
        
        // 戦車のキャタピラ（左）
        g2d.setColor(new Color(50, 50, 50));
        g2d.fillRect(-halfSize, -halfSize + 3, 5, size - 6);
        
        // 戦車のキャタピラ（右）
        g2d.fillRect(halfSize - 5, -halfSize + 3, 5, size - 6);
        
        // 砲塔（円形）
        g2d.setColor(new Color(60, 179, 113)); // 明るい緑
        int turretSize = size / 2;
        g2d.fillOval(-turretSize / 2, -turretSize / 2, turretSize, turretSize);
        
        // 砲身
        g2d.setColor(new Color(80, 80, 80));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawLine(0, 0, 0, -halfSize - 3);
        
        // 砲身の先端
        g2d.fillOval(-2, -halfSize - 5, 4, 4);
        
        // 縁取り
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRect(-halfSize + 3, -halfSize + 5, size - 6, size - 10);
        g2d.drawOval(-turretSize / 2, -turretSize / 2, turretSize, turretSize);
        
        // 元の座標系に戻す
        g2d.setTransform(oldTransform);
        
        // 座標表示
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        String pos = "(" + gridX + "," + gridY + ")";
        g2d.drawString(pos, x - 15, y + halfSize + 12);
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        
        switch (key) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                tankDirection = 0;
                if (tankY > 0) tankY--;
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                tankDirection = 1;
                if (tankX < GRID_SIZE - 1) tankX++;
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                tankDirection = 2;
                if (tankY < GRID_SIZE - 1) tankY++;
                break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                tankDirection = 3;
                if (tankX > 0) tankX--;
                break;
        }
        
        repaint();
    }
    
    @Override
    public void keyReleased(KeyEvent e) {}
    
    @Override
    public void keyTyped(KeyEvent e) {}
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("戦車ゲーム - 矢印キーまたはWASDで移動");
            TankGame game = new TankGame();
            
            frame.add(game);
            frame.pack();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            
            game.requestFocusInWindow();
        });
    }
}