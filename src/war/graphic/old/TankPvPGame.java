package war.graphic;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import war.tank.HeavyTank;
import war.tank.LightTank;
import war.tank.MediumTank;
import war.tank.Tank;

/**
 * TankPvPGame - 2人対戦用戦車バトルゲーム
 * 既存のTankクラス（LightTank, MediumTank, HeavyTank）を変更せずに使用します。
 */
public class TankPvPGame extends JPanel implements KeyListener {
    private static final int GRID_SIZE = 20;
    private static final int CELL_SIZE = 35;
    private static final int PANEL_WIDTH = GRID_SIZE * CELL_SIZE;
    private static final int PANEL_HEIGHT = GRID_SIZE * CELL_SIZE + 180; // 情報表示エリアを少し拡大
    
    // プレイヤーごとの戦車リスト
    private ArrayList<Tank> p1Tanks;
    private ArrayList<Tank> p2Tanks;
    
    private int p1SelectedIndex = 0;
    private int p2SelectedIndex = 0;
    
    public TankPvPGame() {
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setFocusable(true);
        addKeyListener(this);
        setBackground(new Color(230, 235, 230));
        
        // プレイヤー1の戦車 (左上付近)
        p1Tanks = new ArrayList<>();
        p1Tanks.add(new LightTank("P1:軽戦車A", 2, 2));
        p1Tanks.add(new MediumTank("P1:中戦車B", 2, 5));
        
        // プレイヤー2の戦車 (右下付近)
        p2Tanks = new ArrayList<>();
        p2Tanks.add(new HeavyTank("P2:重戦車C", 17, 17));
        p2Tanks.add(new LightTank("P2:軽戦車D", 17, 14));
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        drawGrid(g2d);
        
        // P1の戦車描画 (緑系)
        for (int i = 0; i < p1Tanks.size(); i++) {
            Tank tank = p1Tanks.get(i);
            if (tank.isAlive()) {
                drawTank(g2d, tank, i == p1SelectedIndex, new Color(34, 139, 34), Color.GREEN);
            }
        }
        
        // P2の戦車描画 (青系)
        for (int i = 0; i < p2Tanks.size(); i++) {
            Tank tank = p2Tanks.get(i);
            if (tank.isAlive()) {
                drawTank(g2d, tank, i == p2SelectedIndex, new Color(30, 144, 255), Color.CYAN);
            }
        }
        
        drawInfoPanel(g2d);
        
        // 勝敗判定の表示
        checkGameOver(g2d);
    }
    
    private void drawGrid(Graphics2D g2d) {
        g2d.setColor(new Color(200, 200, 180));
        g2d.setStroke(new BasicStroke(1));
        for (int i = 0; i <= GRID_SIZE; i++) {
            g2d.drawLine(i * CELL_SIZE, 0, i * CELL_SIZE, GRID_SIZE * CELL_SIZE);
            g2d.drawLine(0, i * CELL_SIZE, GRID_SIZE * CELL_SIZE, i * CELL_SIZE);
        }
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRect(0, 0, GRID_SIZE * CELL_SIZE, GRID_SIZE * CELL_SIZE);
    }
    
    private void drawTank(Graphics2D g2d, Tank tank, boolean isSelected, Color baseColor, Color selectColor) {
        int pixelX = (int)(tank.getX() * CELL_SIZE + CELL_SIZE / 2);
        int pixelY = (int)(tank.getY() * CELL_SIZE + CELL_SIZE / 2);
        
        AffineTransform oldTransform = g2d.getTransform();
        g2d.translate(pixelX, pixelY);
        g2d.rotate(Math.toRadians(tank.getAngle()));
        
        int size = CELL_SIZE - 8;
        int halfSize = size / 2;
        
        if (isSelected) {
            g2d.setColor(Color.YELLOW);
            g2d.setStroke(new BasicStroke(3));
            g2d.drawOval(-halfSize - 5, -halfSize - 5, size + 10, size + 10);
        }
        
        // キャタピラ
        g2d.setColor(new Color(50, 50, 50));
        g2d.fillRect(-halfSize, -halfSize + 3, 5, size - 6);
        g2d.fillRect(halfSize - 5, -halfSize + 3, 5, size - 6);
        
        // 本体
        g2d.setColor(baseColor);
        g2d.fillRect(-halfSize + 3, -halfSize + 5, size - 6, size - 10);
        
        // 砲塔
        g2d.setColor(baseColor.brighter());
        int turretSize = size / 2;
        g2d.fillOval(-turretSize / 2, -turretSize / 2, turretSize, turretSize);
        
        // 砲身
        g2d.setColor(new Color(80, 80, 80));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawLine(0, 0, 0, -halfSize - 3);
        
        g2d.setTransform(oldTransform);
        
        // HPバー
        drawHealthBar(g2d, pixelX, pixelY - halfSize - 10, size, tank);
        
        // 名前
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 10));
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(tank.getName(), pixelX - fm.stringWidth(tank.getName())/2, pixelY + halfSize + 15);
    }
    
    private void drawHealthBar(Graphics2D g2d, int x, int y, int width, Tank tank) {
        g2d.setColor(Color.RED);
        g2d.fillRect(x - width/2, y, width, 5);
        double ratio = (double)tank.getHp() / tank.getMaxHp();
        g2d.setColor(Color.GREEN);
        g2d.fillRect(x - width/2, y, (int)(width * ratio), 5);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x - width/2, y, width, 5);
    }
    
    private void drawInfoPanel(Graphics2D g2d) {
        int panelY = GRID_SIZE * CELL_SIZE;
        g2d.setColor(new Color(40, 40, 40));
        g2d.fillRect(0, panelY, PANEL_WIDTH, 180);
        
        g2d.setFont(new Font("Monospaced", Font.BOLD, 12));
        
        // P1 Info
        drawPlayerInfo(g2d, "Player 1 (Green)", p1Tanks.get(p1SelectedIndex), 20, panelY + 25, Color.GREEN);
        drawControls(g2d, "WASD:移動, Q/E:回転, F:攻撃, R:修理, SPACE:切替", 20, panelY + 140, Color.LIGHT_GRAY);
        
        // P2 Info
        drawPlayerInfo(g2d, "Player 2 (Blue)", p2Tanks.get(p2SelectedIndex), PANEL_WIDTH / 2 + 20, panelY + 25, Color.CYAN);
        drawControls(g2d, "矢印:移動, < / >:回転, ENTER:攻撃, BACK:修理, CTRL:切替", PANEL_WIDTH / 2 + 20, panelY + 140, Color.LIGHT_GRAY);
        
        // Divider
        g2d.setColor(Color.GRAY);
        g2d.drawLine(PANEL_WIDTH / 2, panelY + 10, PANEL_WIDTH / 2, panelY + 120);
    }
    
    private void drawPlayerInfo(Graphics2D g2d, String label, Tank tank, int x, int y, Color color) {
        g2d.setColor(color);
        g2d.drawString(label, x, y);
        if (tank.isAlive()) {
            g2d.setColor(Color.WHITE);
            g2d.drawString("Name : " + tank.getName(), x, y + 20);
            g2d.drawString(String.format("HP   : %d/%d", tank.getHp(), tank.getMaxHp()), x, y + 40);
            g2d.drawString(String.format("Attack: %d  Defense: %d", tank.getAttack(), tank.getDefense()), x, y + 60);
            g2d.drawString(String.format("Pos  : (%.0f, %.0f)  Ammo: %d", tank.getX(), tank.getY(), tank.getAmmo()), x, y + 80);
        } else {
            g2d.setColor(Color.RED);
            g2d.drawString("SELECTED TANK DESTROYED!", x, y + 20);
        }
    }
    
    private void drawControls(Graphics2D g2d, String text, int x, int y, Color color) {
        g2d.setColor(color);
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2d.drawString(text, x, y);
    }
    
    private void checkGameOver(Graphics2D g2d) {
        boolean p1Alive = p1Tanks.stream().anyMatch(Tank::isAlive);
        boolean p2Alive = p2Tanks.stream().anyMatch(Tank::isAlive);
        
        if (!p1Alive || !p2Alive) {
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.fillRect(0, 0, PANEL_WIDTH, GRID_SIZE * CELL_SIZE);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("SansSerif", Font.BOLD, 40));
            String msg = !p1Alive ? "PLAYER 2 WINS!" : "PLAYER 1 WINS!";
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(msg, (PANEL_WIDTH - fm.stringWidth(msg))/2, (GRID_SIZE * CELL_SIZE)/2);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        
        // Player 1 Controls
        Tank p1 = p1Tanks.get(p1SelectedIndex);
        if (p1.isAlive()) {
            if (key == KeyEvent.VK_W && p1.getY() > 0) p1.move(0, -1);
            if (key == KeyEvent.VK_S && p1.getY() < GRID_SIZE - 1) p1.move(0, 1);
            if (key == KeyEvent.VK_A && p1.getX() > 0) p1.move(-1, 0);
            if (key == KeyEvent.VK_D && p1.getX() < GRID_SIZE - 1) p1.move(1, 0);
            if (key == KeyEvent.VK_Q) p1.rotate(-15);
            if (key == KeyEvent.VK_E) p1.rotate(15);
            if (key == KeyEvent.VK_F) attackClosest(p1, p2Tanks);
            if (key == KeyEvent.VK_R) p1.repair(10);
        }
        if (key == KeyEvent.VK_SPACE) {
            p1SelectedIndex = (p1SelectedIndex + 1) % p1Tanks.size();
        }
        
        // Player 2 Controls
        Tank p2 = p2Tanks.get(p2SelectedIndex);
        if (p2.isAlive()) {
            if (key == KeyEvent.VK_UP && p2.getY() > 0) p2.move(0, -1);
            if (key == KeyEvent.VK_DOWN && p2.getY() < GRID_SIZE - 1) p2.move(0, 1);
            if (key == KeyEvent.VK_LEFT && p2.getX() > 0) p2.move(-1, 0);
            if (key == KeyEvent.VK_RIGHT && p2.getX() < GRID_SIZE - 1) p2.move(1, 0);
            if (key == KeyEvent.VK_COMMA) p2.rotate(-15);
            if (key == KeyEvent.VK_PERIOD) p2.rotate(15);
            if (key == KeyEvent.VK_ENTER) attackClosest(p2, p1Tanks);
            if (key == KeyEvent.VK_BACK_SPACE) p2.repair(10);
        }
        if (key == KeyEvent.VK_CONTROL) {
            p2SelectedIndex = (p2SelectedIndex + 1) % p2Tanks.size();
        }
        
        repaint();
    }
    
    private void attackClosest(Tank attacker, ArrayList<Tank> enemies) {
        Tank closest = null;
        double minDist = Double.MAX_VALUE;
        for (Tank enemy : enemies) {
            if (enemy.isAlive()) {
                double dist = Math.pow(attacker.getX() - enemy.getX(), 2) + Math.pow(attacker.getY() - enemy.getY(), 2);
                if (dist < minDist) {
                    minDist = dist;
                    closest = enemy;
                }
            }
        }
        if (closest != null) {
            attacker.attackTarget(closest);
        }
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Tank PvP Battle");
            TankPvPGame game = new TankPvPGame();
            frame.add(game);
            frame.pack();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            game.requestFocusInWindow();
        });
    }
}
