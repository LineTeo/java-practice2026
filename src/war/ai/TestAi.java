package war.ai;

import java.util.ArrayList;
import java.util.List;

import war.control.PlayerController;
import war.tank.DummyTank;
import war.tank.MediumTank;
import war.tank.Tank;
import war.tank.Tiger;

public class TestAi {

	public static void main(String[] args) {
		// TODO 自動生成されたメソッド・スタブ
		
	    final int FREND_SIDE  = 0;
	    final int ENEMY_SIDE  = 1;
		
	    // ======================================================================
	    // インスタンス変数
	    // ======================================================================

	    /** 全戦車リスト */
	    ArrayList<Tank> tanks = new ArrayList<>();

	    /** 選択中戦車のインデックス */
	    int selectedIndex = 0;

	    /** 描画担当（View） */
//	    final TankRenderer renderer;

	    /** プレイヤー操作担当（Controller） */
	    final PlayerController playerController = new PlayerController(20, 35);

	    /** 敵AI担当 */
	    final EnemyAI2 enemyAI2 = new EnemyAI2(19);;
	    
	    		
        tanks.clear();
        tanks.add(new Tiger(     "タイガー",       FREND_SIDE, 9,  3));
        tanks.add(new MediumTank("シャーマン１号", ENEMY_SIDE, 3,  16));
        tanks.add(new MediumTank("シャーマン２号", ENEMY_SIDE, 9,  13));
        tanks.add(new MediumTank("シャーマン３号", ENEMY_SIDE, 15,  9));
        selectedIndex = 0;
 	    
	    
        // プレイヤー戦車を PlayerController に設定
        playerController.setControlledTank(tanks.get(0));

        
        StateAnalyzer stat = new StateAnalyzer();
	    ArrayList<Tank> comTanks = new ArrayList<>();
	    ArrayList<Tank> plyTanks = new ArrayList<>();
        
	    comTanks.clear();
	    comTanks.addAll(List.of(tanks.get(1), tanks.get(2), tanks.get(3)));
	    
	    final Tank player = tanks.get(0);
	    
	    plyTanks.clear();
	    plyTanks.add(player);
	    
        AIConfig aiConfig = new AIConfig();
        
        DummyTank dummy = new DummyTank(tanks.get(1));
        BattleState BS = stat.analyze(dummy, player, comTanks, plyTanks, 1);

        ThreatEvaluator d = new ThreatEvaluator(aiConfig, BS);
        
    	double x , y ;

        // 敵戦車を順に行動させる
        for (int i = 1; i < tanks.size(); i++) {
            Tank enemy = tanks.get(i);
            if (!enemy.isAlive()) continue;

            enemyAI2.takeTurn(enemy, plyTanks, comTanks);
//            enemyAI.takeTurn(enemy, friendlies,enemyes);

            enemy.resetAct();
        }

    	

        
        
/*        	
    	
        for(double i = 1 ; i < 100; i++) {
        	x = player.getX() - i / 10 ;
        	y = player.getY() - i / 10 ;
        	
        	dummy.setXY(x,y);
        	System.out.printf("%.2f ",dummy.distance(player));        	
        	System.out.printf(", %.2f",d.calcAT(ThreatEvaluator.Times.SINGLE, dummy, player));        	
        	System.out.printf(", %.2f",d.calcAT(ThreatEvaluator.Times.DOUBLE, dummy, player));        	
        	System.out.printf(", %.2f",d.calcDT(ThreatEvaluator.Times.SINGLE, dummy, player));        	
        	System.out.printf(", %.2f",d.calcDT(ThreatEvaluator.Times.DOUBLE, dummy, player));        	
        	BS = stat.analyze(dummy, player, comTanks, plyTanks, 1);
        	System.out.printf(", %2d",BS.distanceRankFromEnemy); 
        	
        	d.progOne(dummy , player.getX() , player.getY());
        	System.out.printf(", %.2f",d.calcAT(ThreatEvaluator.Times.SINGLE, dummy, player));        	
        	
        	BS = stat.analyze(dummy, player, comTanks, plyTanks, 1);
        	System.out.printf(", %2d\n",BS.distanceRankFromEnemy);        	
        	
        	System.out.printf("距離C = %3.1f , %.2f ",tanks.get(i).distance(player), d.calcAC(ThreatEvaluator.Times.DOUBLE , tanks.get(i), player));
//        	d.calcAC(ThreatEvaluator.Times.DOUBLE , tanks.get(i), player);
 //       	d.calcDC(ThreatEvaluator.Times.DOUBLE , tanks.get(i), player);

            Tank cloneSelf = new DummyTank(tanks.get(i));
            
        	d.progOne(cloneSelf , player.getX() , player.getY());
        	System.out.printf("距離T = %2.1f, res = %.2f\n",cloneSelf.distance(player),d.calcAC(ThreatEvaluator.Times.SINGLE , cloneSelf, player));
       	
            
        }
*/
    }
}
