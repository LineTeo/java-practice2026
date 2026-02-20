package war.graphic;

//======================================================================
//TankBattleGame.java - 戦車バトルゲームのメインクラス
//======================================================================
//このクラスは、Swingを使った2Dグラフィックスの戦車バトルゲームです。
//JPanel を継承することで、描画可能なパネルとして機能します。
//KeyListener, MouseListener を実装することで、キーボードとマウスの入力を受け取れます。
//======================================================================

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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import war.tank.HeavyTank;
import war.tank.MediumTank;
import war.tank.Tank;
import war.tank.Tiger;

/**
* TankBattleGame クラス
* - JPanel を継承: 描画可能なパネルとして動作
* - KeyListener 実装: キーボード入力を処理
* - MouseListener 実装: マウスクリックを処理
*/
public class TankBattleGame extends JPanel implements KeyListener, MouseListener {
 // ======================================================================
 // 定数定義 - ゲーム共通定数
 // ======================================================================
 
 private final int FREND_SIDE = 0;	// 味方の識別子　0
 private final int ENEMY_SIDE = 1;	// 敵の識別子　1
	
 // ======================================================================
 // 定数定義 - ゲームのレイアウトを決める基本設定
 // ======================================================================
 
 /** グリッドのマス数（縦横20マス） */
 private static final int GRID_SIZE = 20;
 
 /** 1マスあたりのピクセルサイズ */
 private static final int CELL_SIZE = 35;
 
 /** パネル全体の横幅（20マス × 35ピクセル = 700ピクセル） */
 private static final int PANEL_WIDTH = GRID_SIZE * CELL_SIZE;
 
 /** パネル全体の高さ（ゲームエリア + 情報表示エリア150ピクセル） */
 private static final int PANEL_HEIGHT = GRID_SIZE * CELL_SIZE + 150;
 
 
 // ======================================================================
 // インスタンス変数 - ゲームの状態を保持
 // ======================================================================
 
 /** ゲーム内の全戦車を管理するリスト */
 private ArrayList<Tank> tanks;
 
 /** 現在プレイヤーが操作している戦車 */
 private Tank selectedTank;
 

 /** 現在選択中の戦車のインデックス（TABキーで切り替えるために使用） */
 private int selectedIndex = 0;
 
 /** 情報表示用テキストエリア（現在は未使用） */
 private JTextArea infoArea;
 
 
 // ======================================================================
 // コンストラクタ - ゲームの初期化
 // ======================================================================
 /**
  * TankBattleGame のコンストラクタ
  * パネルのサイズ設定、イベントリスナーの登録、戦車の初期配置を行う
  */
 public TankBattleGame() {
     // --- パネルの基本設定 ---
     
     // パネルのサイズを設定（700 x 850ピクセル）
     setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
     
     // このパネルがキーボード入力を受け取れるようにする
     setFocusable(true);
     
     // キーボードイベントを処理するリスナーを登録
     addKeyListener(this);
     
     // マウスイベントを処理するリスナーを登録
     addMouseListener(this);
     
     // 背景色を薄いベージュ色に設定
     setBackground(new Color(240, 240, 220));
     
     
     // --- 戦車の初期化 ---
     
     // 戦車を格納するArrayListを作成
     tanks = new ArrayList<>();
     
     startGame();
     
     // 最初の戦車（軽戦車A）を選択状態にする 
     selectedTank = tanks.get(0);
 }
 private void startGame() {
	 // ゲーム開始、やり直しの   
	 // 1. 戦車リストをクリア
	    tanks.clear();
	    
	    // 2. 新しい戦車を作成
	    tanks.add(new Tiger("タイガー", FREND_SIDE, 3, 3));
	    tanks.add(new MediumTank("シャーマン１号", ENEMY_SIDE, 16, 3));
	    tanks.add(new HeavyTank("シャーマン２号", ENEMY_SIDE, 3, 16));
	    tanks.add(new MediumTank("シャーマン３号", ENEMY_SIDE, 16, 16));
	    
	    
	}
 // ======================================================================
 // 描画処理 - Swingフレームワークから自動的に呼び出される
 // ======================================================================
 /**
  * paintComponent - 画面を描画するメソッド
  * このメソッドは、repaint() が呼ばれたときや、ウィンドウが再表示されるときに
  * Swingフレームワークによって自動的に呼び出されます。
  * 
  * @param g 描画に使用するGraphicsオブジェクト
  */
 @Override
 protected void paintComponent(Graphics g) {
     // 親クラス（JPanel）の描画処理を呼び出す（背景のクリアなど）
     super.paintComponent(g);
     
     // Graphics を Graphics2D にキャスト（より高度な描画機能を使うため）
     Graphics2D g2d = (Graphics2D) g;
     
     // アンチエイリアシングを有効化（滑らかな描画のため）
     g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                         RenderingHints.VALUE_ANTIALIAS_ON);
     
     // 1. グリッド（マス目）を描画
     drawGrid(g2d);
     
     // 2. すべての戦車を描画
     for (int i = 0; i < tanks.size(); i++) {
         Tank tank = tanks.get(i);
         
         // 生存している戦車のみ描画
         if (tank.isAlive()) {
             // この戦車が選択中かどうかを判定
             boolean isSelected = (i == selectedIndex);
             
             // 戦車を描画
             drawTank(g2d, tank, isSelected);
         }
     } 
     
     // 3. 情報表示パネルを描画（画面下部）
     drawInfoPanel(g2d);
 }
 
 
 // ======================================================================
 // グリッド描画 - マス目の線を描く
 // ======================================================================
 /**
  * グリッド（マス目）を描画するメソッド
  * 
  * @param g2d Graphics2Dオブジェクト
  */
 private void drawGrid(Graphics2D g2d) {
     // --- グリッド線の描画 ---
     
     // 線の色を薄い茶色に設定
     g2d.setColor(new Color(200, 200, 180));
     
     // 線の太さを1ピクセルに設定
     g2d.setStroke(new BasicStroke(1));
     
     // 縦線と横線を描画（21本ずつ、0〜20）
     for (int i = 0; i <= GRID_SIZE; i++) {
         // 縦線: x座標が i * CELL_SIZE の位置に、上から下へ
         g2d.drawLine(i * CELL_SIZE, 0, 
                     i * CELL_SIZE, GRID_SIZE * CELL_SIZE);
         
         // 横線: y座標が i * CELL_SIZE の位置に、左から右へ
         g2d.drawLine(0, i * CELL_SIZE, 
                     GRID_SIZE * CELL_SIZE, i * CELL_SIZE);
     }
     
     // --- 外枠の描画 ---
     
     // 線の色を黒に変更
     g2d.setColor(Color.BLACK);
     
     // 線の太さを3ピクセルに変更（外枠を太くする）
     g2d.setStroke(new BasicStroke(3));
     
     // グリッド全体を囲む四角形を描画
     g2d.drawRect(0, 0, GRID_SIZE * CELL_SIZE, GRID_SIZE * CELL_SIZE);
 }
 
 
 // ======================================================================
 // 戦車描画 - 戦車の見た目を描く
 // ======================================================================
 /**
  * 戦車を描画するメソッド
  * 
  * @param g2d Graphics2Dオブジェクト
  * @param tank 描画する戦車
  * @param isSelected この戦車が選択されているかどうか
  */
 private void drawTank(Graphics2D g2d, Tank tank, boolean isSelected) {
     // --- 戦車の中心位置を計算 ---
     
     // グリッド座標からピクセル座標に変換
     // (tank.getX() は 0〜19 の値、CELL_SIZE/2 でマスの中心に配置)
     int pixelX = (int)(tank.getX() * CELL_SIZE + CELL_SIZE / 2);
     int pixelY = (int)(tank.getY() * CELL_SIZE + CELL_SIZE / 2);
     
     
     // --- 座標変換の準備（回転のため） ---
     
     // 現在の変換状態を保存（後で元に戻すため）
     AffineTransform oldTransform = g2d.getTransform();
     
     // 描画原点を戦車の中心に移動
     g2d.translate(pixelX, pixelY);
     
     // 戦車の角度だけ回転（砲塔の向きに合わせる）
     g2d.rotate(Math.toRadians(tank.getAngle()));
     
     
     // --- 戦車のサイズ計算 ---
     
     int size = CELL_SIZE - 8;      // マスより少し小さく
     int halfSize = size / 2;        // 半径（中心からの距離）
     
     
     // --- 選択中の戦車を強調表示 ---
     
     if (isSelected) {
         // 黄色の円を描画して強調
         g2d.setColor(Color.YELLOW);
         g2d.setStroke(new BasicStroke(3));
         
         // 戦車の周りに少し大きめの円を描く
         g2d.drawOval(-halfSize - 5, -halfSize - 5, size + 10, size + 10);
     }
     
     
     // --- 戦車の色を決定（HPによって変化） ---
     
     // HP割合を計算（0.0 〜 1.0）
     double hpRatio = (double)tank.getHp() / tank.getMaxHp();
     Color tankColor;
     
     if (hpRatio > 0.7) {
         // HP 70%以上: 緑色（健康）
         tankColor = new Color(34, 139, 34);
     } else if (hpRatio > 0.3) {
         // HP 30-70%: 黄土色（注意）
         tankColor = new Color(184, 134, 11);
     } else {
         // HP 30%以下: 赤色（危険）
         tankColor = new Color(178, 34, 34);
     }
     
     
     // --- キャタピラ（履帯）の描画 ---
     
     g2d.setColor(new Color(50, 50, 50));  // 濃い灰色
     
     // 左側のキャタピラ
     g2d.fillRect(-halfSize, -halfSize + 3, 5, size - 6);
     
     // 右側のキャタピラ
     g2d.fillRect(halfSize - 5, -halfSize + 3, 5, size - 6);
     
     
     // --- 戦車本体の描画 ---
     
     g2d.setColor(tankColor);
     
     // 本体の四角形を塗りつぶし
     g2d.fillRect(-halfSize + 3, -halfSize + 5, size - 6, size - 10);
     
     
     // --- 砲塔（タレット）の描画 ---
     
     // 本体より明るい色にする
     g2d.setColor(tankColor.brighter());
     
     int turretSize = size / 2;  // 砲塔は本体の半分のサイズ
     
     // 円形の砲塔を描画
     g2d.fillOval(-turretSize / 2, -turretSize / 2, turretSize, turretSize);
     
     
     // --- 砲身（大砲）の描画 ---
     
     g2d.setColor(new Color(80, 80, 80));  // 灰色
     g2d.setStroke(new BasicStroke(3));     // 太さ3ピクセル
     
     // 中心から上方向（-Y方向）に線を描く
     // 回転しているので、この「上」が砲塔の向きになる
     g2d.drawLine(0, 0, 0, -halfSize - 3);
     
     
     // --- 本体の縁取り ---
     
     g2d.setColor(Color.BLACK);
     g2d.setStroke(new BasicStroke(1.5f));
     
     // 本体の四角形の枠線を描画
     g2d.drawRect(-halfSize + 3, -halfSize + 5, size - 6, size - 10);
     
     
     // --- 座標変換を元に戻す ---
     
     // 回転や移動を元に戻す（これ以降の描画に影響しないように）
     g2d.setTransform(oldTransform);
     
     
     // --- HPバーの描画 ---
     
     // 戦車の上部にHPバーを表示
     drawHealthBar(g2d, pixelX, pixelY - halfSize - 10, size, tank);
     
     
     // --- 名前の描画 ---
     
     g2d.setColor(Color.BLACK);
     g2d.setFont(new Font("MS Gothic", Font.BOLD, 10));
     
     // フォントの幅を計算するためのオブジェクト
     FontMetrics fm = g2d.getFontMetrics();
     
     // 名前の表示幅を取得
     int nameWidth = fm.stringWidth(tank.getName());
     
     // 戦車の下に中央揃えで名前を表示
     g2d.drawString(tank.getName(), pixelX - nameWidth/2, pixelY + halfSize + 15);
 }
 
 
 // ======================================================================
 // HPバー描画 - 戦車の体力を視覚的に表示
 // ======================================================================
 /**
  * HPバーを描画するメソッド
  * 
  * @param g2d Graphics2Dオブジェクト
  * @param x バーの中心X座標
  * @param y バーの上端Y座標
  * @param width バーの幅
  * @param tank 対象の戦車
  */
 private void drawHealthBar(Graphics2D g2d, int x, int y, int width, Tank tank) {
     int barWidth = width;    // バーの幅
     int barHeight = 5;       // バーの高さ
     
     // --- 背景（赤）の描画 ---
     // HPがなくなった部分を赤で表示
     g2d.setColor(Color.RED);
     g2d.fillRect(x - barWidth/2, y, barWidth, barHeight);
     
     // --- 現在HP（緑）の描画 ---
     // HP割合を計算
     double hpRatio = (double)tank.getHp() / tank.getMaxHp();
     
     g2d.setColor(Color.GREEN);
     
     // HPの割合だけバーを塗る
     g2d.fillRect(x - barWidth/2, y, (int)(barWidth * hpRatio), barHeight);
     
     // --- 枠線の描画 ---
     g2d.setColor(Color.BLACK);
     g2d.drawRect(x - barWidth/2, y, barWidth, barHeight);
 }
 
 
 // ======================================================================
 // 情報パネル描画 - 画面下部の情報エリア
 // ======================================================================
 /**
  * 画面下部に情報パネルを描画するメソッド
  * 選択中の戦車のステータスと操作説明を表示
  * 
  * @param g2d Graphics2Dオブジェクト
  */
 private void drawInfoPanel(Graphics2D g2d) {
     // 情報パネルの開始Y座標（グリッドの下）
     int panelY = GRID_SIZE * CELL_SIZE;
     
     // --- 背景の描画 ---
     g2d.setColor(new Color(50, 50, 50));  // 濃い灰色
     g2d.fillRect(0, panelY, PANEL_WIDTH, 150);
     
     
     // --- 選択中の戦車情報 ---
     
     if (selectedTank != null && selectedTank.isAlive()) {
         g2d.setColor(Color.WHITE);
         g2d.setFont(new Font("MS Gothic", Font.BOLD, 14));
         
         int textX = 20;           // テキストの左端位置
         int textY = panelY + 25;  // 最初の行の位置
         
         // 戦車名
         g2d.drawString("【選択中】 " + selectedTank.getName(), textX, textY);
         textY += 20;  // 次の行へ
         
         // HP表示
         g2d.drawString(String.format("HP: %d/%d", 
             selectedTank.getHp(), selectedTank.getMaxHp()), textX, textY);
         textY += 20;
         
         // 攻撃力と防御力
         g2d.drawString(String.format("攻撃力: %d  防御力: %d", 
             selectedTank.getAttack(), selectedTank.getDefense()), textX, textY);
         textY += 20;
         
         // 位置と角度
         g2d.drawString(String.format("位置: (%.1f, %.1f)  角度: %.0f°", 
             selectedTank.getX(), selectedTank.getY(), selectedTank.getAngle()), 
             textX, textY);
         textY += 20;
         
         // 弾薬
         g2d.drawString(String.format("弾薬: %d", selectedTank.getAmmo()), 
             textX, textY);
         textY += 20;
         
         // 行動力
         g2d.drawString(String.format("行動力: %d", selectedTank.activity()), 
             textX, textY);
     }
     
     
     // --- 操作説明（右側） ---
     
     g2d.setColor(Color.YELLOW);
     g2d.setFont(new Font("MS Gothic", Font.PLAIN, 11));
     
     int helpX = PANEL_WIDTH - 280;  // 右寄せの開始位置
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
 // キーボード入力処理 - KeyListenerインターフェースの実装
 // ======================================================================
 /**
  * キーが押されたときに呼ばれるメソッド
  * 
  * @param e KeyEventオブジェクト（どのキーが押されたかの情報を含む）
  */
 @Override
 public void keyPressed(KeyEvent e) {
     // 選択中の戦車がないか、死んでいる場合は何もしない
     if (selectedTank == null || !selectedTank.isAlive()) return;
     
     // 押されたキーのコードを取得
     int key = e.getKeyCode();
     
     // キーによって処理を分岐
     switch (key) {
         case KeyEvent.VK_UP:  // ↑キー
        	 //　上向き
        	 tankDir(selectedTank,"VK_UP");
             // 上端でない場合のみ移動
             if (selectedTank.getY() > 0) {
                 selectedTank.move(selectedTank.getX() + 0, selectedTank.getY() - 1);  // Y座標を-1（上に移動）
             }
             break;
             
         case KeyEvent.VK_DOWN:  // ↓キー
        	 //　下向き
        	 tankDir(selectedTank,"VK_DOWN");
             // 下端でない場合のみ移動
             if (selectedTank.getY() < GRID_SIZE - 1) {
                 selectedTank.move(selectedTank.getX() + 0, selectedTank.getY() + 1);  // Y座標を+1（下に移動）
             }
             break;
             
         case KeyEvent.VK_LEFT:  // ←キー
        	 //　左向き
        	 tankDir(selectedTank,"VK_LEFT");

        	 // 左端でない場合のみ移動
             if (selectedTank.getX() > 0) {
                 selectedTank.move(selectedTank.getX() - 1, selectedTank.getY() + 0);  // X座標を-1（左に移動）
             }
             break;
             
         case KeyEvent.VK_RIGHT:  // →キー
        	 //　右向き
        	 tankDir(selectedTank,"VK_RIGHT");
             // 右端でない場合のみ移動
             if (selectedTank.getX() < GRID_SIZE - 1) {
                 selectedTank.move(selectedTank.getX() + 1, selectedTank.getY() + 0);  // X座標を+1（右に移動）
             }
             break;
/*             
         case KeyEvent.VK_Q:  // Qキー
             // 砲塔を左に15度回転
             selectedTank.rotate(-15);
             break;
             
         case KeyEvent.VK_E:  // Eキー
             // 砲塔を右に15度回転
             selectedTank.rotate(15);
             break;
*/             
         case KeyEvent.VK_SPACE:  // スペースキー
        	 int result = JOptionPane.showConfirmDialog(
        			    this,                           // 親コンポーネント（このパネル）
        			    "ターン終了しますか？", // メッセージ
        			    "ターン終了",                      // ダイアログのタイトル
        			    JOptionPane.YES_NO_OPTION       // Yes/Noボタン
        			);
 
        	 if (result == JOptionPane.NO_OPTION) {
                    break;
        		}
        	 
        	 selectedIndex = selectedIndex + 1;

        	 // 敵戦車のターン
        	 do {
    	    	 selectedTank = tanks.get(selectedIndex);
        	     if (selectedTank != null && selectedTank.isAlive()) {

        	    	 if (enemyTank(selectedTank) == 0) {
            	    	 selectedTank.resetAct();
            	    	 repaint();
            	    	 JOptionPane.showConfirmDialog(
                 		    this,
                 		    selectedIndex + "台目終了",
                 		    "確認",
                 		    JOptionPane.DEFAULT_OPTION
            	    			 );
        	    		 
        	    	 } else {
        	    		 selectedIndex = 0;
        	    		 break;
        	    	 }
                 
        	     }
                 
                 // インデックスを1つ進める（最後まで行ったら0に戻る）
                 selectedIndex = (selectedIndex + 1) % tanks.size();
        	 
        	 } while (!(selectedIndex == 0));
        	 
        	 // 敵戦車のターン終了　プレーヤーターン

        	 selectedTank = tanks.get(selectedIndex);
        	 selectedTank.resetAct();
             
             break;

             
         case KeyEvent.VK_R:  // Rキー
             // 修理（HPを20回復）
             selectedTank.repair();
             break;
             
         case KeyEvent.VK_A:  // Aキー
             // 弾薬補給（10発補充）
             selectedTank.reloadAmmo(10);
             break;
             
//         default:             System.out.println(key);
     }
     
     // 画面を再描画（変更を反映）
     repaint();
     System.out.println("残行動力 " + selectedTank.activity());

     
 }
 
 private void tankDir (Tank tank , String tankDir) {
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
 // 敵戦車のターン 生きてる前提
 // ======================================================================
 /*
  * 敵戦車の行動選択順位
  * １．HP　<  30%		→修理　距離15以下なら退避を優先
  * ２．敵への距離	> 20 敵へ移動（行動力範囲で）
  * ３．攻撃　（行動力の範囲で）
  * ４．移動　（残りの行動力で回避行動。
  */

 private int  enemyTank(Tank eTank) {
	 double health = (double)eTank.getHp()/(double)eTank.getMaxHp();
	 
	 if ( health <= 0.3) {
//		 recoveryAction(eTank);
		 eTank.repair();
		 return 0;
	 } else {
		 return assaultAction( tanks.get(0));
	 }	 
 }
 
 
 //　退避!!!
 //		引数	eTank 脅威対象
 private void recoveryAction(Tank eTank) {
	 int sts = 0;
	 double d =selectedTank.distance(eTank) ;
	 while (selectedTank.distance(eTank) < 15.0 && sts == 0) {
		 sts = selectedTank.escape(eTank);
	 }
	 selectedTank.repair();
	 
	 
 }

 //　突撃!!!

 private int assaultAction(Tank playerTank) {
	 selectedTank.move(playerTank.getX(), playerTank.getY());
	 selectedTank.move(playerTank.getX(), playerTank.getY());
	 selectedTank.attackTarget(playerTank);
	 return gameEndChk();
	 		/* 退避1回目
	        selectedTank.escape(eTank);
	        selectedTank.escape(eTank);
	        */
	 }
 // ======================================================================
 // マウス入力処理 - MouseListenerインターフェースの実装
 // ======================================================================
 /**
  * マウスがクリックされたときに呼ばれるメソッド
  * クリックした位置にいる敵戦車を攻撃する
  * 
  * @param e MouseEventオブジェクト（クリック位置などの情報を含む）
  */
 @Override
 public void mouseClicked(MouseEvent e) {
     // 選択中の戦車がないか、死んでいる場合は何もしない
     if (selectedTank == null || !selectedTank.isAlive()) return;
     
     // クリック位置のピクセル座標をグリッド座標に変換
     int gridX = e.getX() / CELL_SIZE;
     int gridY = e.getY() / CELL_SIZE;
     
     // クリックした位置にいる戦車を探す
     for (Tank target : tanks) {
         // 自分自身でなく、かつ生存している戦車が対象
         if (target != selectedTank && target.isAlive()) {
             // 戦車のグリッド座標を取得
             int tankGridX = (int)target.getX();
             int tankGridY = (int)target.getY();
             
             // クリック位置と戦車の位置が一致したら
             if (tankGridX == gridX && tankGridY == gridY) {
                 // その戦車を攻撃
                 selectedTank.attackTarget(target);
                 
                 // 画面を再描画
                 repaint();

                 // ゲーム終了チェック
                 if (gameEndChk() == 1) {
                	 selectedIndex = 0;
                	 selectedTank = tanks.get(selectedIndex);
                	 selectedTank.resetAct();
                 }
                 
                 // 1つ見つかったら終了
                 return;
             }
         }
     }
	 }
 
 
 // ======================================================================
 // ヘルパーメソッド - ユーティリティ機能
 // ======================================================================
 /**
  * 生存している戦車が存在するかチェックするメソッド
  * 
  * @return 生存戦車がいればtrue、いなければfalse
  */
 private boolean hasAlive_eTank() {
     for ( int i=1 ; i < tanks.size() ; i++ ) {
         if (tanks.get(i).isAlive()) return true;
     }
     return false;
 }
 
 private int gameEndChk() {
	 int f = 0;	//友軍数
	 int e = 0; //敵軍数
	 for (Tank tank : tanks) {
		 if (tank.isAlive() && tank.jinei() == FREND_SIDE){
			 f++;
		 }
		 if (tank.isAlive() && tank.jinei() == ENEMY_SIDE){
			 e++;
		 }
	 }
	 
	 if ( f * e == 0) {
		 String msg;
		 if ( f == 0) {
			 msg = "戦車が破壊されたので負けです";
		 } else {
			 msg = "敵を殲滅しました。勝利です!!";			 
		 }
		 
		 Object[] options = {"再プレイ", "終了"};
		 int result =JOptionPane.showOptionDialog(
				 this,
				 msg,
				 "ゲーム終了",
				 JOptionPane.YES_NO_OPTION,
				 JOptionPane.INFORMATION_MESSAGE,
				 null,
				 options,
				 options[0]);
		 if (result == 0) {  		// 0 = 再プレイ（左のボタン）
			 startGame();
			 repaint();
			 requestFocusInWindow();
			 return 1;
		 } else {            	// 1 = 終了（右のボタン）
			System.exit(0);
			return 1;
		 }
	 } else { return 0; } 

 }
 

 // ======================================================================
 // 未使用のリスナーメソッド - インターフェース実装のため必須
 // ======================================================================
 
 /** キーが離されたとき（未使用） */
 @Override
 public void keyReleased(KeyEvent e) {}
 
 /** キーがタイプされたとき（未使用） */
 @Override
 public void keyTyped(KeyEvent e) {}
 
 /** マウスボタンが押されたとき（未使用） */
 @Override
 public void mousePressed(MouseEvent e) {}
 
 /** マウスボタンが離されたとき（未使用） */
 @Override
 public void mouseReleased(MouseEvent e) {}
 
 /** マウスがパネル内に入ったとき（未使用） */
 @Override
 public void mouseEntered(MouseEvent e) {}
 
 /** マウスがパネル外に出たとき（未使用） */
 @Override
 public void mouseExited(MouseEvent e) {}
 
 
 // ======================================================================
 // mainメソッド - プログラムのエントリーポイント
 // ======================================================================
 /**
  * プログラムの起動メソッド
  * 
  * このメソッドが最初に呼ばれ、ゲームウィンドウを作成・表示します。
  * static なので、クラスのインスタンスがなくても実行できます。
  * 
  * @param args コマンドライン引数（未使用）
  */
 public static void main(String[] args) {
     // SwingUtilities.invokeLater() を使う理由:
     // Swingの描画処理は「イベントディスパッチスレッド」という
     // 専用のスレッドで実行する必要があります。
     // このメソッドを使うことで、GUIの初期化を正しいスレッドで行えます。
     
     SwingUtilities.invokeLater(() -> {
         // --- ウィンドウ（JFrame）の作成 ---
         JFrame frame = new JFrame("戦車バトルゲーム");
         
         // --- ゲームパネル（TankBattleGame）のインスタンス作成 ---
         // ここで自身のクラスのインスタンスを作成！
         // これにより、インスタンス変数（tanks, selectedTank等）が初期化されます
         TankBattleGame game = new TankBattleGame();
         
         // --- パネルをウィンドウに追加 ---
         frame.add(game);
         
         // --- ウィンドウのサイズを内容（パネル）に合わせる ---
         frame.pack();
         
         // --- ウィンドウを閉じたときにプログラムを終了 ---
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         
         // --- ウィンドウを画面中央に配置 ---
         frame.setLocationRelativeTo(null);
         
         // --- ウィンドウを表示 ---
         frame.setVisible(true);
         
         // --- ゲームパネルにキーボードフォーカスを与える ---
         // これをしないと、キーボード入力が受け取れません
         game.requestFocusInWindow();
         
         // --- 初期状態をコンソールに出力 ---
         System.out.println("=== ゲーム開始 ===");
//         for (Tank tank : game.tanks) {
//             tank.displayStatus();
//         }
     });
 }
}

//======================================================================
//まとめ: このクラスの構造
//======================================================================
//
//1. **定数**: ゲームの基本設定（グリッドサイズなど）
//2. **インスタンス変数**: ゲームの状態（戦車のリスト、選択中の戦車）
//3. **コンストラクタ**: ゲームの初期化
//4. **描画メソッド**: paintComponent, drawGrid, drawTank など
//5. **イベント処理**: keyPressed, mouseClicked など
//6. **mainメソッド**: プログラムの起動ポイント
//
//このクラスは MVC パターンの View + Controller の役割を担っています:
//- View: 描画処理（paintComponent等）
//- Controller: 入力処理（keyPressed, mouseClicked）
//- Model: Tankクラス（別ファイル）
//
//======================================================================