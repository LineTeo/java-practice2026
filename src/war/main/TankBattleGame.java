package war.main;

//======================================================================
// TankBattleGame.java - 戦車バトルゲームのメインクラス（リファクタリング版）
//======================================================================
// 変更点:
//   - 描画処理 → TankRenderer クラスへ完全移管
//   - 敵AI処理 → EnemyAI クラスへ完全移管
//   - このクラスはController（入力受付・ゲームフロー制御）に集中
//======================================================================

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

import war.ai.EnemyAI;
import war.graphic.TankRenderer;
import war.tank.HeavyTank;
import war.tank.MediumTank;
import war.tank.Tank;
import war.tank.Tiger;

/**
 * TankBattleGame クラス (Controller + エントリーポイント)
 * - キーボード / マウス入力を受け付ける
 * - ゲームの進行フロー（ターン管理・勝敗判定）を制御する
 * - 描画は TankRenderer、敵AI判断は EnemyAI に委譲する
 */
public class TankBattleGame extends JPanel implements KeyListener, MouseListener {

    // ======================================================================
    // 定数
    // ======================================================================

    private static final int FREND_SIDE  = 0;
    private static final int ENEMY_SIDE  = 1;

    private static final int GRID_SIZE   = 20;
    private static final int CELL_SIZE   = 35;
    private static final int PANEL_WIDTH = GRID_SIZE * CELL_SIZE;
    private static final int INFO_HEIGHT = 150;
    private static final int PANEL_HEIGHT= PANEL_WIDTH + INFO_HEIGHT;

    // ======================================================================
    // インスタンス変数
    // ======================================================================

    /** 全戦車リスト */
    private ArrayList<Tank> tanks;

    /** 選択中（プレイヤー操作）の戦車 */
    private Tank selectedTank;

    /** 選択中戦車のインデックス */
    private int selectedIndex = 0;

    /** 描画担当オブジェクト（旧: 各 draw〜メソッド） */
    private final TankRenderer renderer;

    /** 敵AI担当オブジェクト（旧: enemyTank / assaultAction 等） */
    private final EnemyAI enemyAI;

    // ======================================================================
    // コンストラクタ
    // ======================================================================

    public TankBattleGame() {
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);
        setBackground(new Color(240, 240, 220));

        renderer = new TankRenderer(GRID_SIZE, CELL_SIZE, PANEL_WIDTH);
        enemyAI  = new EnemyAI(GRID_SIZE - 1);

        tanks = new ArrayList<>();
        startGame();
        selectedTank = tanks.get(0);
    }

    // ======================================================================
    // ゲーム初期化
    // ======================================================================

    private void startGame() {
        tanks.clear();
        tanks.add(new Tiger(     "タイガー",      FREND_SIDE,  3,  3));
        tanks.add(new MediumTank("シャーマン１号", ENEMY_SIDE, 16,  3));
        tanks.add(new HeavyTank( "シャーマン２号", ENEMY_SIDE,  3, 16));
        tanks.add(new MediumTank("シャーマン３号", ENEMY_SIDE, 16, 16));
        selectedIndex = 0;
    }

    // ======================================================================
    // 描画 — TankRenderer へ委譲
    // ======================================================================

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);

        renderer.drawGrid(g2d);
        renderer.drawAllTanks(g2d, tanks, selectedIndex);
        renderer.drawInfoPanel(g2d, selectedTank, INFO_HEIGHT);
    }

    // ======================================================================
    // キーボード入力
    // ======================================================================

    @Override
    public void keyPressed(KeyEvent e) {
        if (selectedTank == null || !selectedTank.isAlive()) return;

        int key = e.getKeyCode();
        switch (key) {
            case KeyEvent.VK_UP:
                setDirection(selectedTank, "VK_UP");
                if (selectedTank.getY() > 0)
                    selectedTank.move(selectedTank.getX(), selectedTank.getY() - 1);
                break;
            case KeyEvent.VK_DOWN:
                setDirection(selectedTank, "VK_DOWN");
                if (selectedTank.getY() < GRID_SIZE - 1)
                    selectedTank.move(selectedTank.getX(), selectedTank.getY() + 1);
                break;
            case KeyEvent.VK_LEFT:
                setDirection(selectedTank, "VK_LEFT");
                if (selectedTank.getX() > 0)
                    selectedTank.move(selectedTank.getX() - 1, selectedTank.getY());
                break;
            case KeyEvent.VK_RIGHT:
                setDirection(selectedTank, "VK_RIGHT");
                if (selectedTank.getX() < GRID_SIZE - 1)
                    selectedTank.move(selectedTank.getX() + 1, selectedTank.getY());
                break;
            case KeyEvent.VK_SPACE:
                endPlayerTurn();
                break;
            case KeyEvent.VK_R:
                selectedTank.repair();
                break;
            case KeyEvent.VK_A:
                selectedTank.reloadAmmo(10);
                break;
            case KeyEvent.VK_Z:
                selectedTank.reloadAmmo(10);
                break;
        }

        repaint();
        System.out.println("残行動力 " + selectedTank.activity());
    }

    // ======================================================================
    // ターン終了 → 敵AI処理 — EnemyAI へ委譲
    // ======================================================================

    /**
     * プレイヤーターン終了処理。
     * 確認ダイアログ後、敵戦車を順に EnemyAI に行動させる。
     */
    private void endPlayerTurn() {
        int confirm = JOptionPane.showConfirmDialog(
            this, "ターン終了しますか？", "ターン終了", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        ArrayList<Tank> friendlies = getFriendlyTanks();

        for (int i = 1; i < tanks.size(); i++) {
            Tank enemy = tanks.get(i);
            if (!enemy.isAlive()) continue;

            enemyAI.takeTurn(enemy, friendlies);  // EnemyAI に行動を委譲
            enemy.resetAct();
            repaint();

            if (gameEndChk() == 1) return;

            JOptionPane.showConfirmDialog(
                this,
                enemy.getName() + " の行動終了",
                "確認",
                JOptionPane.DEFAULT_OPTION
            );
        }

        // プレイヤーターン開始
        selectedIndex = 0;
        selectedTank  = tanks.get(selectedIndex);
        selectedTank.resetAct();
        repaint();
    }

    // ======================================================================
    // マウス入力（攻撃）
    // ======================================================================

    @Override
    public void mouseClicked(MouseEvent e) {
        if (selectedTank == null || !selectedTank.isAlive()) return;

        int gridX = e.getX() / CELL_SIZE;
        int gridY = e.getY() / CELL_SIZE;

        for (Tank target : tanks) {
            if (target == selectedTank || !target.isAlive()) continue;
            if ((int) target.getX() == gridX && (int) target.getY() == gridY) {
                selectedTank.attackTarget(target);
                repaint();
                if (gameEndChk() == 1) {
                    selectedIndex = 0;
                    selectedTank  = tanks.get(selectedIndex);
                    selectedTank.resetAct();
                }
                return;
            }
        }
    }

    // ======================================================================
    // ヘルパーメソッド
    // ======================================================================

    /** 矢印キーに対応する砲塔の向きを設定する */
    private void setDirection(Tank tank, String dir) {
        double cr = tank.getAngle();
        switch (dir) {
            case "VK_UP":    tank.rotate(360 - cr); break;
            case "VK_RIGHT": tank.rotate( 90 - cr); break;
            case "VK_DOWN":  tank.rotate(180 - cr); break;
            case "VK_LEFT":  tank.rotate(270 - cr); break;
        }
    }

    /** 生存中の味方戦車リストを返す（EnemyAI に渡すため） */
    private ArrayList<Tank> getFriendlyTanks() {
        ArrayList<Tank> list = new ArrayList<>();
        for (Tank t : tanks) {
            if (t.jinei() == FREND_SIDE && t.isAlive()) list.add(t);
        }
        return list;
    }

    /** 勝敗を判定し、ゲーム終了ダイアログを表示する */
    private int gameEndChk() {
        int friends = 0, enemies = 0;
        for (Tank t : tanks) {
            if (!t.isAlive()) continue;
            if (t.jinei() == FREND_SIDE) friends++;
            else                          enemies++;
        }

        if (friends * enemies == 0) {
            String msg = (friends == 0) ? "戦車が破壊されたので負けです"
                                        : "敵を殲滅しました。勝利です!!";
            Object[] options = { "再プレイ", "終了" };
            int result = JOptionPane.showOptionDialog(
                this, msg, "ゲーム終了",
                JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE,
                null, options, options[0]);

            if (result == 0) {
                startGame();
                selectedTank = tanks.get(0);
                repaint();
                requestFocusInWindow();
            } else {
                System.exit(0);
            }
            return 1;
        }
        return 0;
    }

    // ======================================================================
    // 未使用リスナーメソッド
    // ======================================================================

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    // ======================================================================
    // main
    // ======================================================================

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("戦車バトルゲーム");
            TankBattleGame game = new TankBattleGame();
            frame.add(game);
            frame.pack();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            game.requestFocusInWindow();
            System.out.println("=== ゲーム開始 ===");
        });
    }
}
