package war.main.old;//パッケージ演習3

import java.util.Random;

import war.tank.Sherman;
import war.tank.Sherman2;
import war.tank.Tank;
import war.tank.Tiger;
public class MainProject {

    public static void main(String[] args) {
    	Random rnd = new Random();
    	
    	// 味方戦車を作成
        Tiger tank1 = new Tiger("タイガー", 0, 0);
        
        // 敵戦車を3台作成
        Tank[] tekitank = new Tank[3];
        
        tekitank[0] = new Sherman("シャーマン1号", 100, 100);
        tekitank[1] = new Sherman("シャーマン2号", 80, 90);
        tekitank[2] = new Sherman("シャーマン3号", 60, 00);
        int s2=rnd.nextInt(3);
        tekitank[s2] = new Sherman2(tekitank[s2].getName(), tekitank[s2].getX(), tekitank[s2].getY());

        // 初期状態表示
        tank1.displayStatus();

        
        System.out.println("\n===== 戦闘開始 =====\n");

        // 戦闘シミュレーション

        int  a= 0;
        do {
            // 自分の攻撃
        	tank1.sokkyo(tekitank);
            System.out.print("\nどれを攻撃？");
            int n = 1;
            for (int i = 0 ; i < tekitank.length ; i++) {
            	if (tekitank[i].isAlive()) {
                    System.out.print("  " + n +" : " + tekitank[i].getName());
            		n++;
            	}
            }
            System.out.print(" > ");
            	
            int ans = new java.util.Scanner(System.in).nextInt();
            System.out.println("\n");
        	
            switch (ans) {
            	case 1,2,3 ->{
            		if (tekitank[ans-1].isAlive()) {
            			tank1.attackTarget(tekitank[ans-1]);
            		} else {
                		System.out.println("\n破壊済み\n");            			
            		}
            	}
            	default -> {
            		System.out.println("\n操作ミス\n");
            	}
            }
            
            //　敵の攻撃
            a = 0; 			//敵全体の生存確認指標
            for (int i = 0 ; i < tekitank.length ; i++) {
            	a = a + tekitank[i].getHp();
            	if (tekitank[i].isAlive()) {
                	tekitank[i].attackTarget(tank1);            		
            	}
            }
        } while (a > 0 && tank1.isAlive());
        
        System.out.println("\n======================");
        System.out.println("==                  ==");

        if ( tank1.isAlive()) {
            System.out.println("==    大勝利!!！    ==");
        	
        } else {
            System.out.println("==    負けました    ==");
        	
        }
        System.out.println("==                  ==");
        System.out.println("======================\n\n");
        
        System.out.println("\n===== 自軍最終状態 =====\n");
        tank1.displayStatus();

        System.out.println("\n===== 敵軍最終状態 =====\n");

        for (int i = 0 ; i < tekitank.length ; i++) {
        	tekitank[i].displayStatus();
        }
    }

}
