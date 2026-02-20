package war.main.old;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import war.graphic.TankRenderer;
import war.tank.HeavyTank;
import war.tank.MediumTank;
import war.tank.Tank;
import war.tank.Tiger;

/**
 * TankBattleGame - ゲームのメインクラス（Controller）
 * ゲームロジックと入力処理を担当
 */
public class AI_TankBattle extends JPanel implements KeyListener, MouseListener {
    
    // ======================================================================
    // 定数定義
    // ======================================================================
    
    private final int FREND_SIDE = 0;
    private final int ENEMY_SIDE = 1;
    
    private static final int GRID_SIZE = 20;
    private static final int CELL_SIZE = 35;
    private static final int PANEL_WIDTH = GRID_SIZE * CELL_SIZE;
    private static final int PANEL_HEIGHT = GRID_SIZE * CELL_SIZE + 150;
    
    
    // ======================================================================
    // インスタンス変数
    // ======================================================================
    
    private ArrayList<Tank> tanks;
    private Tank selectedTank;
    private int selectedIndex = 0;
    
    // 描画担当クラス
    private TankRenderer renderer;

    // AI戦車コントロール
    private SmartTankAI tankAI = new SmartTankAI(); 
    
    // ======================================================================
    // コンストラクタ
    // ======================================================================
    
    public AI_TankBattle() {
        // パネルの基本設定
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);
        setBackground(new Color(240, 240, 220));
        
        // 描画クラスの初期化
        renderer = new TankRenderer();
        
        // 戦車の初期化
        
        tanks = new ArrayList<>();
        startGame();
        
        selectedTank = tanks.get(0);
    }
    
    
    private void startGame() {
        tanks.clear();
        
        tanks.add(new Tiger("タイガー", FREND_SIDE, 3, 3));
        tanks.add(new MediumTank("シャーマン１号", ENEMY_SIDE, 16, 3));
        tanks.add(new HeavyTank("シャーマン２号", ENEMY_SIDE, 3, 16));
        tanks.add(new MediumTank("シャーマン３号", ENEMY_SIDE, 16, 16));
    }
    
    
    // ======================================================================
    // 描画処理（描画クラスに委譲）
    // ======================================================================
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                            RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 描画クラスに委譲
        renderer.drawGrid(g2d);
        renderer.drawAllTanks(g2d, tanks, selectedIndex);
        renderer.drawInfoPanel(g2d, selectedTank);
    }
    
    
    // ======================================================================
    // キーボード入力処理
    // ======================================================================
    
    @Override
    public void keyPressed(KeyEvent e) {
        if (selectedTank == null || !selectedTank.isAlive()) return;
        
        int key = e.getKeyCode();
        
        switch (key) {
            case KeyEvent.VK_UP:
                tankDir(selectedTank, "VK_UP");
                if (selectedTank.getY() > 0) {
                    selectedTank.move(selectedTank.getX(), selectedTank.getY() - 1);
                }
                break;
                
            case KeyEvent.VK_DOWN:
                tankDir(selectedTank, "VK_DOWN");
                if (selectedTank.getY() < GRID_SIZE - 1) {
                    selectedTank.move(selectedTank.getX(), selectedTank.getY() + 1);
                }
                break;
                
            case KeyEvent.VK_LEFT:
                tankDir(selectedTank, "VK_LEFT");
                if (selectedTank.getX() > 0) {
                    selectedTank.move(selectedTank.getX() - 1, selectedTank.getY());
                }
                break;
                
            case KeyEvent.VK_RIGHT:
                tankDir(selectedTank, "VK_RIGHT");
                if (selectedTank.getX() < GRID_SIZE - 1) {
                    selectedTank.move(selectedTank.getX() + 1, selectedTank.getY());
                }
                break;
                
            case KeyEvent.VK_SPACE:
                handleEndTurn();
                break;
                
            case KeyEvent.VK_R:
                selectedTank.repair();
                break;
                
            case KeyEvent.VK_A:
                selectedTank.reloadAmmo(10);
                break;
        }
        
        repaint();
        System.out.println("残行動力 " + selectedTank.activity());
    }
    
    
    // ======================================================================
    // ゲームロジック
    // ======================================================================
    private void handleEndTurn() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "ターン終了しますか？",
            "ターン終了",
            JOptionPane.YES_NO_OPTION
        );
        
        if (result == JOptionPane.NO_OPTION) {
            return;
        }
        
        selectedIndex = selectedIndex + 1;
        
        // 敵戦車のターン
        do {
            selectedTank = tanks.get(selectedIndex);
            
            if (selectedTank != null && selectedTank.isAlive()) {
                // ★★★ ここが変更点 ★★★
                if (selectedTank.jinei() == ENEMY_SIDE) {
                    // AIに行動を任せる
                	
                    int gameEndFlag = 0;
					try {
						gameEndFlag = tankAI.decideAction(selectedTank, tanks.get(0), tanks);
					} catch (Exception e) {
						// TODO 自動生成された catch ブロック
						e.printStackTrace();
					}
                    selectedTank.resetAct();
                    repaint();
                    
                    if (gameEndFlag == 1) {
                        selectedIndex = 0;
                        break;
                    }
                    
                    JOptionPane.showConfirmDialog(
                        this,
                        selectedTank.getName() + "のターン終了",
                        "確認",
                        JOptionPane.DEFAULT_OPTION
                    );
                }
            }
            
            selectedIndex = (selectedIndex + 1) % tanks.size();
        } while (selectedIndex != 0);
        
        // プレーヤーターン
        selectedTank = tanks.get(selectedIndex);
        selectedTank.resetAct();
    }

    
    
    private void tankDir(Tank tank, String tankDir) {
        double cr = tank.getAngle();
        switch (tankDir) {
            case "VK_UP":
                tank.rotate(360 - cr);
                break;
            case "VK_RIGHT":
                tank.rotate(90 - cr);
                break;
            case "VK_DOWN":
                tank.rotate(180 - cr);
                break;
            case "VK_LEFT":
                tank.rotate(270 - cr);
                break;
        }
    }
   
    
    // ======================================================================
    // マウス入力処理
    // ======================================================================
    
    @Override
    public void mouseClicked(MouseEvent e) {
        if (selectedTank == null || !selectedTank.isAlive()) return;
        
        int gridX = e.getX() / CELL_SIZE;
        int gridY = e.getY() / CELL_SIZE;
        
        for (Tank target : tanks) {
            if (target != selectedTank && target.isAlive()) {
                int tankGridX = (int)target.getX();
                int tankGridY = (int)target.getY();
                
                if (tankGridX == gridX && tankGridY == gridY) {
                    selectedTank.attackTarget(target);
                    repaint();
                    
                    if (gameEndChk() == 1) {
                        selectedIndex = 0;
                        selectedTank = tanks.get(selectedIndex);
                        selectedTank.resetAct();
                    }
                    
                    return;
                }
            }
        }
    }
    
    
    // ======================================================================
    // ゲーム終了判定
    // ======================================================================
    
    private int gameEndChk() {
        int f = 0;
        int e = 0;
        
        for (Tank tank : tanks) {
            if (tank.isAlive() && tank.jinei() == FREND_SIDE) {
                f++;
            }
            if (tank.isAlive() && tank.jinei() == ENEMY_SIDE) {
                e++;
            }
        }
        
        if (f * e == 0) {
            String msg = (f == 0) ? "戦車が破壊されたので負けです" : "敵を殲滅しました。勝利です!!";
            
            Object[] options = {"再プレイ", "終了"};
            int result = JOptionPane.showOptionDialog(
                this,
                msg,
                "ゲーム終了",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
            );
            
            if (result == 0) {
                startGame();
                repaint();
                requestFocusInWindow();
                return 1;
            } else {
                System.exit(0);
                return 1;
            }
        } else {
            return 0;
        }
    }
    
    
    // ======================================================================
    // 未使用のリスナーメソッド
    // ======================================================================
    
    @Override
    public void keyReleased(KeyEvent e) {}
    @Override
    public void keyTyped(KeyEvent e) {}
    @Override
    public void mousePressed(MouseEvent e) {}
    @Override
    public void mouseReleased(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}

    // ======================================================================
    // main メソッド
    // ======================================================================
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("戦車バトルゲームAI");
            AI_TankBattle game = new AI_TankBattle();
            
            frame.add(game);
            frame.pack();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            
            game.requestFocusInWindow();
            
/*            System.out.println("=== ゲーム開始 ===");
            for (Tank tank : game.tanks) {
                tank.displayStatus();
            }
*/
        });
    }
}
