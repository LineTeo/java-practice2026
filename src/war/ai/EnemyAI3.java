package war.ai;

//======================================================================
// EnemyAI.java - 敵戦車の行動ロジックを担当するクラス
//======================================================================
// AI2 ⇒　AI3の変更点:
// 戦車の動作をForward Back Left Rightに変更
//
//======================================================================

import java.util.ArrayList;

import war.tank.DummyTank;
import war.tank.Tank;


/**
 * EnemyAI クラス
 * 敵の1ターン分の行動を決定・実行する。
 * ダメージ計算等は Tank クラスへ委譲する。
 */
public class EnemyAI3 {

    // ======================================================================
    // 定数（戦術パラメータ）
    // ======================================================================

	private final AIConfig aiConfig;
	
	
	/** グリッドの最大座標（端の判定に使用） */
    private final int MAX_GRID;

    public enum Side {
        PLAYER,
        PC
    }
    
    private Side playSide;
    // ======================================================================
    // コンストラクタ
    // ======================================================================

    /**
     * @param maxGrid グリッドサイズ - 1（例: 19）
     */
    public EnemyAI3(int maxGrid ,Side activeSide, AIConfig aiConfig) {
        this.MAX_GRID = maxGrid;
        this.playSide = activeSide;
        this.aiConfig = aiConfig;
    }
    public EnemyAI3(int maxGrid ,Side activeSide) {
        this.MAX_GRID = maxGrid;
        this.playSide = activeSide;
        this.aiConfig = new AIConfig();
    }

    // ======================================================================
    // 公開メソッド
    // ======================================================================

    
    
    
    /**
     * 敵戦車 1 両の 1 ターン分の行動を実行する。
     * HP フェーズに応じて戦術を動的に切り替える。
     *
     * @param enemy      行動させる敵戦車
     * @param friendlies 攻撃対象候補の味方戦車リスト
     * @return 0: 正常終了
     */
    public int takeTurn(Tank self, ArrayList<Tank> targets, ArrayList<Tank> friends) {
   
        /**
         * self			行動対象戦車
         * targets 		攻撃対象戦車群（プレーヤー側。まだ1台を想定しているが一応リスト）
         * friendss 		味方戦車群（コンピュータ側）
         *
         */	
    	
//		AIConfig aiConfig = new AIConfig();
    	
    	if (!self.isAlive()) return 0;
        Tank target = selectTarget(self, targets);
        if (target == null) return 0;

    	// *******  情報収集  *******
        StateAnalyzer getstat = new StateAnalyzer();
        
        BattleState stat = getstat.analyze(self, target, friends,targets, 1);
        
        DummyTank cloneSelf = new DummyTank(self);
        BattleState cloneStat = getstat.analyze(cloneSelf, target, friends, targets, 1);

		// フローチャートの実装	
    	ThreatEvaluator dec = new ThreatEvaluator(aiConfig, stat);

    		//　1.ゾーン判断
        //　1.1 AC DC 算出
        
        double AC1 = dec.calcAC(ThreatEvaluator.Times.SINGLE, self, target);
        double AC2 = dec.calcAC(ThreatEvaluator.Times.DOUBLE, self, target);
        double DC1 = dec.calcDC(ThreatEvaluator.Times.SINGLE, self, target);
        double DC2 = dec.calcDC(ThreatEvaluator.Times.DOUBLE, self, target);
        
        progOne(cloneSelf,target.getX(),target.getY());
        double AT1 = dec.calcAT(ThreatEvaluator.Times.SINGLE, cloneSelf, target);
        double AT2 = dec.calcAT(ThreatEvaluator.Times.DOUBLE, cloneSelf, target);
        double DT1 = dec.calcDT(ThreatEvaluator.Times.SINGLE, cloneSelf, target);
        double DT2 = dec.calcDT(ThreatEvaluator.Times.DOUBLE, cloneSelf, target);

//        System.out.printf("%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f\n",AC1,AC2,DC1,DC2,AT1,AT2,DT1,DT2);


        
        
        //　1.ゾーン決定
        
        double z = self.distance(target)/self.getRange();
;
        //debub用


		  



		String ret = "ETC0";

    	if (z < aiConfig.THREAT_ZONE_1) {
    		if (AC2 > aiConfig.P01_Z1_AA_AC_2_O ) {
    			self.attackTarget(target);
    			self.attackTarget(target);
    	        ret = "P1 ATC,ATC";

    			
    		} else if(DT2 < aiConfig.P02_Z1_AE_DT_2_U){
    			self.attackTarget(target);
    			escapeOne(self,target);
    	        ret = "P2 ATC,ESC";
    		} else if(DT2 < aiConfig.P03_Z1_ER_DT_2_U) {
    			escapeOne(self,target);
    			self.repair();
    	        ret = "P3 REP,ESC";
    		} else {
    			escapeOne(self,target);
    			escapeOne(self,target);
    	        ret = "P4 ESC,ESC";
    		}    				
    	} else if (z < aiConfig.THREAT_ZONE_2) {

    		if (AT1 > aiConfig.P05_Z2_CA_AT_1_O ) {
				progOne(self,target.getX(),target.getY());
    			self.attackTarget(target);
    	        ret = "P5 APR,ATC";
    			
    		} else if(DC2 < aiConfig.P06_Z2_AA_DC_2_U){
    			self.attackTarget(target);
    			self.attackTarget(target);
    	        ret = "P6 ATC,ATC";
    		} else if(DT2 < aiConfig.P07_Z2_AE_DT_2_U){
    			escapeOne(self,target);
    			self.repair();
    	        ret = "P7 ESC,REP";
    		} else if(DT2 > aiConfig.P08_Z2_EE_DT_2_O) {
    			escapeOne(self,target);
    			escapeOne(self,target);
    	        ret = "P8 ESC,ESC";
    		} else {
    			escapeOne(self,target);
    			self.repair();    				
    	        ret = "P9 ESC,REP";
    		}
    	} else if(self.getHp() < self.getMaxHp()){
    		self.reloadAmmo(self.getMaxAmmo()-self.getAmmo());
    		self.repair();
	        ret = "ETC1";
    		
    	} else {
    		if(DT2 < aiConfig.P10_Z3_CC_DT_2_U ){
				progOne(self,target.getX(),target.getY());
    	        progOne(cloneSelf,target.getX(),target.getY());
                DT2 = dec.calcDT(ThreatEvaluator.Times.DOUBLE, cloneSelf, target);        
        		if(DT2 < aiConfig.P11_Z3_CA_DT_2_U ){
    				progOne(self,target.getX(),target.getY());
        	        ret = "P11 APR,APR";        			
        		} else  {
        			self.attackTarget(target);
    	        ret = "P12 APR,ATC";
        		}
        	} else {
				progOne(self,target.getX(),target.getY());
    	        ret = "P13 ATC, ATC";
    		}    		
    		
     	}
    	log(self, ret);
        return 0;
    }
    


    // ======================================================================
    // ターゲット選択
    // ======================================================================

    /**
     * 攻撃対象を選定する。
     * HP 割合が低く（倒しやすく）、距離が近い相手を優先する。
     * スコア = HP割合 × 5 + 距離（値が小さいほど優先）
     */
    private Tank selectTarget(Tank enemy, ArrayList<Tank> friendlies) {
        Tank   best      = null;
        double bestScore = Double.MAX_VALUE;

        for (Tank t : friendlies) {
            if (!t.isAlive()) continue;
            double hpRatio = (double) t.getHp() / t.getMaxHp();
            double dist    = enemy.distance(t);
            double score   = hpRatio * 5.0 + dist;
            if (score < bestScore) {
                bestScore = score;
                best      = t;
            }
        }
        return best;
    }

    // ======================================================================
    // ユーティリティ
    // ======================================================================
    /** 戦車が目標に対して有効射程内かどうかを判定する */
//    private boolean isInRange(Tank enemy, Tank target) {
//        return enemy.distance(target) <= (enemy.getRange()*OPT_RANGE_MAX);
//    }

    /** 値を [min, max] の範囲にクランプする */
    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
     

    // 移動アクション
	public void progOne(Tank tank, double x, double y) {
        double nx = clamp(x, 0, MAX_GRID - 1);
        double ny = clamp(y, 0, MAX_GRID - 1);
        
// 方向転換 角度は12時が0度とする0～360度で表現することとXY座標は下方向がY+なので、式が以下のようになる
        double curAngle = tank.getAngle();
        double tarAngle = (int)(((450 - Math.toDegrees(Math.atan2(-y + tank.getY(),x - tank.getX()))) % 360 + 15)/30) * 30.0;
        
        tank.rotate(tarAngle - curAngle);  
        
        // 行動力が残っていて　敵との距離が1マス以上ある場合
        
        if(tank.activity() > 0 && Math.abs(x - tank.getX()) +Math.abs(y - tank.getY()) > 1.1) {
        tank.move(nx,ny);        		
        }
        		
	}

    // 退避アクション
	public void escapeOne(Tank tank, Tank teki) {
		if(tank.activity() <= 0 ) return;

    	//退避方向は敵とは逆方向
        double escWayX = tank.getX() - teki.getX();
        double escWayY = tank.getY() - teki.getY(); 
        double nx = clamp(escWayX + tank.getX(), 0, MAX_GRID -1);
        double ny = clamp(escWayY + tank.getY(), 0, MAX_GRID -1);

		
		//　敵に正面を向ける = 退避方向とは逆方向なので、180度加算する
		
		// 方向転換 角度は12時が0度とする0～360度で表現することとXY座標は下方向がY+なので、式が以下のようになる
        double curAngle = tank.getAngle();
        double tarAngle = (int)(((450 - Math.toDegrees(Math.atan2( - escWayY, escWayX)) + 180 ) % 360 + 15)/30) * 30.0;
        
        tank.rotate(tarAngle - curAngle);        
        if(tank.activity() > 0) tank.move(nx,ny);        		
        	
    }
	
    /** デバッグ用ログ出力 */
    private void log(Tank self, String msg) {
//        System.out.println("[AI] " + self.getName()  + msg + " → " + self.getX() +", "+self.getY());
    }

}
