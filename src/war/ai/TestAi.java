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
	    final EnemyAI enemyAI = new EnemyAI(19);;
	    
	    
		
        tanks.clear();
        tanks.add(new Tiger(     "タイガー",       FREND_SIDE, 9,  9));
        tanks.add(new MediumTank("シャーマン１号", ENEMY_SIDE, 9,  7));
        tanks.add(new MediumTank("シャーマン２号", ENEMY_SIDE, 8,  6));
        tanks.add(new MediumTank("シャーマン３号", ENEMY_SIDE, 7,  5));
        tanks.add(new MediumTank("シャーマン４号", ENEMY_SIDE, 6,  4));
        tanks.add(new MediumTank("シャーマン５号", ENEMY_SIDE, 5,  3));
        tanks.add(new MediumTank("シャーマン６号", ENEMY_SIDE, 4,  2));
        tanks.add(new MediumTank("シャーマン７号", ENEMY_SIDE, 3,  1));
        tanks.add(new MediumTank("シャーマン８号", ENEMY_SIDE, 2,  0));
        tanks.add(new MediumTank("シャーマン８号", ENEMY_SIDE, 1,  0));
        tanks.add(new MediumTank("シャーマン８号", ENEMY_SIDE, 0,  0));
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
	    
        BattleState BS;
        AIConfig aiConfig = new AIConfig();
        ThreatEvaluator d = new ThreatEvaluator(aiConfig);

        for(int i = 1 ; i < tanks.size(); i++) {
        	System.out.printf("距離C = %3.1f , %.2f ",tanks.get(i).distance(player), d.calcAC(ThreatEvaluator.Times.DOUBLE , tanks.get(i), player));
//        	d.calcAC(ThreatEvaluator.Times.DOUBLE , tanks.get(i), player);
 //       	d.calcDC(ThreatEvaluator.Times.DOUBLE , tanks.get(i), player);

            Tank cloneSelf = new DummyTank(tanks.get(i));
            
        	d.progOne(cloneSelf , player.getX() , player.getY());
        	System.out.printf("距離T = %2.1f, res = %.2f\n",cloneSelf.distance(player),d.calcAC(ThreatEvaluator.Times.SINGLE , cloneSelf, player));
        	
            
        }
        
        	
        	System.out.printf("%.2f\n",(double)d.doubleResponce(270,486));
    	

       
   	
		
	}

}
