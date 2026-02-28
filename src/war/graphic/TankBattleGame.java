package war.graphic;

//======================================================================
// TankBattleGame.java - 戦車バトルゲームのメインクラス（完全リファクタリング版）
//======================================================================
// 変更履歴:
//   v1: 描画処理 → TankRenderer へ移管
//   v2: 敵AI処理 → EnemyAI へ移管
//   v3: プレイヤー操作 → PlayerController へ移管 ★NEW
//
// このクラスの責務:
//   - ゲームフロー制御（初期化、ターン管理、勝敗判定）
//   - 各コンポーネント（Renderer, PlayerController, EnemyAI）の統合
//   - イベントリスナーの登録・委譲
//
// 将来的な拡張:
//   PlayerController → PlayerAI に差し替えることで、
//   プレイヤー側も自動操作に切り替え可能。
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
import war.control.PlayerController;
import war.tank.HeavyTank;
import war.tank.MediumTank;
import war.tank.Tank;
import war.tank.Tiger;

/**
 * TankBattleGame クラス (ゲームフロー制御 + エントリーポイント)
 * 各責務を専門クラスに委譲し、ゲーム進行のみを管理する。
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

    /** 選択中戦車のインデックス */
    private int selectedIndex = 0;

    /** 描画担当（View） */
    private final TankRenderer renderer;

    /** プレイヤー操作担当（Controller） */
    private final PlayerController playerController;

    /** 敵AI担当 */
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

        // 各コンポーネントを生成
        renderer         = new TankRenderer(GRID_SIZE, CELL_SIZE, PANEL_WIDTH);
        playerController = new PlayerController(GRID_SIZE, CELL_SIZE);
        enemyAI          = new EnemyAI(GRID_SIZE - 1);

        tanks = new ArrayList<>();
        startGame();

        // プレイヤー戦車を PlayerController に設定
        playerController.setControlledTank(tanks.get(0));
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
        renderer.drawInfoPanel(g2d, playerController.getControlledTank(), INFO_HEIGHT);
    }

    // ======================================================================
    // キーボード入力 — PlayerController へ委譲
    // ======================================================================

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        // スペースキー: ターン終了
        if (key == KeyEvent.VK_SPACE) {
            endPlayerTurn();
            return;
        }

        // それ以外のキー入力を PlayerController に委譲
        boolean handled = playerController.handleKeyInput(key);

        if (handled) {
            repaint();
        }
    }

    // ======================================================================
    // マウス入力 — PlayerController へ委譲
    // ======================================================================

    @Override
    public void mouseClicked(MouseEvent e) {
        boolean handled = playerController.handleMouseClick(
            e.getX(), e.getY(), tanks);

        if (handled) {
            repaint();
            // 攻撃後に勝敗判定
            if (gameEndChk() == 1) {
                selectedIndex = 0;
                playerController.setControlledTank(tanks.get(selectedIndex));
                tanks.get(selectedIndex).resetAct();
            }
        }
    }

    // ======================================================================
    // ターン終了 → 敵AI処理
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

        // 敵戦車を順に行動させる
        for (int i = 1; i < tanks.size(); i++) {
            Tank enemy = tanks.get(i);
            if (!enemy.isAlive()) continue;

            enemyAI.takeTurn(enemy, friendlies);
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
        Tank playerTank = tanks.get(selectedIndex);
        playerTank.resetAct();
        playerController.setControlledTank(playerTank);
        repaint();
    }

    // ======================================================================
    // ヘルパーメソッド
    // ======================================================================

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
                playerController.setControlledTank(tanks.get(0));
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
