package war.ai;

import java.util.ArrayList;
import java.util.List;

import war.control.PlayerController;
import war.tank.HeavyTank;
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
        tanks.add(new Tiger(     "タイガー",       FREND_SIDE,  9,  9));
        tanks.add(new MediumTank("シャーマン１号", ENEMY_SIDE,  9,  5));
        tanks.add(new HeavyTank( "シャーマン２号", ENEMY_SIDE, 17,  1));
        tanks.add(new MediumTank("シャーマン３号", ENEMY_SIDE, 15,  9));
        tanks.add(new MediumTank("シャーマン４号", ENEMY_SIDE, 16, 13));
        tanks.add(new HeavyTank( "シャーマン５号", ENEMY_SIDE,  9, 18));
        tanks.add(new MediumTank("シャーマン６号", ENEMY_SIDE,  2, 16));
        tanks.add(new MediumTank("シャーマン７号", ENEMY_SIDE,  2,  9));
        tanks.add(new HeavyTank( "シャーマン８号", ENEMY_SIDE,  2,  2));
        selectedIndex = 0;
 	    
	    
        // プレイヤー戦車を PlayerController に設定
        playerController.setControlledTank(tanks.get(0));

        
        StateAnalyzer stat = new StateAnalyzer();
	    ArrayList<Tank> comTanks = new ArrayList<>();
	    ArrayList<Tank> plyTanks = new ArrayList<>();
        
	    comTanks.clear();
	    comTanks.addAll(List.of(tanks.get(1), tanks.get(2), tanks.get(3)));
	    plyTanks.clear();
	    plyTanks.add(tanks.get(0));
	    
        BattleState BS;
        AIConfig aiConfig = new AIConfig();
        ThreatEvaluator d = new ThreatEvaluator(aiConfig);

        for(int i = 1 ; i < 9 ; i++) {
        	BS = stat.analyze(tanks.get(i), tanks.get(0), comTanks, plyTanks, 1) ;
//        	System.out.println(BS.toString());
        	System.out.printf("脅威値 = %.1f, 機会値 = %.1f%n", d.calcThreat(BS), d.calcOpportunity(BS));
        	
        }
        
        

	}

}
