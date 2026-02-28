package war.tank;

import java.util.Random;

public abstract class Tank {
    // 戦車の基本属性
   String name;
    private int hp;              // 体力
    private int maxHp;           // 最大体力
    private int attack;          // 攻撃力
    private int MAX_RNG;		//　最大射程
    private int AHD;	  	    //　命中率100%の範囲    
    private int defense;         // 防御力
    private double speed;           // 速度    
    private double x, y;         // 位置座標
    private double angle;        // 砲塔の向き（度）
    private int ammo;            // 残弾数
    private int maxAmmo;		 // 最大弾薬数
    private boolean isAlive;     // 生存状態
    private String  modelType;   // 機種名
    private int activePoint;     // 行動力
    private int jinei;			 // 陣営識別コード
    private long serialNo;		 // インスタンスID

    
    //　行動力関連定数群
    final private int MAX_ACT = 8;			//最大行動力
    final private double rRate = 0.25; 		//ばらつき係数
    protected int REP_CST = 8;	//攻撃時消費行動力
    protected int ATC_CST = 4;	//攻撃時消費行動力
    protected int CHG_CST = 2;	//補給時消費行動力
    protected int MOV_CST = 1;	//移動、回転消費行動力

    
    // コンストラクタ
    public Tank(String name, int jinei, int hp, int attack, int defense, double x, double y) {
        this.name = name;
        this.hp = hp;
        this.maxHp = hp;
        this.attack = attack;
        this.MAX_RNG = 12;
        this.AHD = 2;
        this.defense = defense;
        this.x = x;
        this.y = y;
        this.speed = 1.0;
        this.angle = 0;
        this.ammo = 50;
        this.maxAmmo =50;
        this.isAlive = true;
        this.activePoint = MAX_ACT;
        this.jinei = jinei;	
        this.serialNo = System.nanoTime();  // ナノ秒単位のタイムスタンプをＩＤにする

    }
    
    @Override
    public String toString() {
    	return this.name +" 陣営" + this.jinei + "残耐力" + this.hp;
    }
    
    
    //抽象メソッド 機種名の入力
    public abstract void typeName();
    
    // 攻撃メソッド
    public int attackTarget(Tank target) {
        if (!this.isAlive  || this.activePoint < ATC_CST || this.ammo <= 0) { 						//弾切れ、または相手が死んでるときは除外
            System.out.println(this.name + "は攻撃できません！");
            return -1;
        }

        System.out.println(this.name + "が" + target.getName() + "を攻撃！");
        this.ammo--;												//弾消費
        
        // 命中判定
        if (meichu(target)){
        	System.out.println("命中" + target.getName() + "を攻撃！");
            // ダメージ計算（攻撃力 - 相手の防御力）
//            int damage = Math.max(1, this.attack - target.getDefense());
            target.takeDamage(damage(target));
        } else {
            System.out.println("はずれ");        	
        }
        this.activePoint -= ATC_CST;
        return 0;
    }
    
    
    // ダメージを受けるメソッド
    public void takeDamage(int damage) {
        this.hp -= damage;
        System.out.println(this.name + "は" + damage + "のダメージを受けた！（残りHP: " + this.hp + "）");
        
        if (this.hp <= 0) {
            this.hp = 0;
            this.isAlive = false;
            System.out.println(this.name + "は破壊された！");
        }
    }

    /* 移動メソッド
     * 目標座標に向けてスピード分移動する
     * 1回の移動で移動できない場合は途中で止まる。
    */
    public int move(double targetX, double targetY) {
        if (!this.isAlive || this.activePoint < MOV_CST ) return -1;

/*******************************************
 * 		直線距離ベースの移動
 * 
        double distance = this.speed/Math.sqrt(Math.pow(targetX - this.x, 2) + Math.pow(targetY - this.y, 2));
        if (distance > 1 ) { distance = 1;}
        
        double dx = (targetX - this.x) * distance;
        double dy = (targetY - this.y) * distance;
        this.x += dx;
        this.y += dy;
        System.out.println(this.name + "が移動: (" + this.x + ", " + this.y + ")");
        this.activePoint -= MOV_CST;

*******************************************/
        
//		マス目ベースの移動
        final int count = (int)this.speed;
        for (int i = 0; i < count ; i++) {
        	if (Math.abs(targetX - this.x) >= Math.abs(targetY - this.y)) { // X距離、Ｙ距離の遠い方のマス目を移動
        		if (targetX - this.x >= 0) {
        			this.x += 1;
        		} else {
        			this.x -= 1;        			
        		}
        		
        	} else {
        		if (targetY - this.y >= 0) {
        			this.y += 1;
        		} else {
        			this.y -= 1;        			
        		}
        	}
        }
        this.activePoint -= MOV_CST;
        return 0;
    }

    // 退避ソッド　相手から離れる方向に逃げる
    public int  escape(Tank teki) {
        if (!this.isAlive || this.activePoint < MOV_CST ) return -1;
        //敵から離れる方向を判断
        
        double escTgtX = this.x - (teki.getX() - this.x) ;
        double escTgtY = this.y - (teki.getY() - this.y) ;
        
        this.move(escTgtX, escTgtY);
                
        return 0;
    }

    
    // 回転メソッド（砲塔を回す）
    public int  rotate(double degrees) {
    	if (degrees == 0) return 0;
        if (!this.isAlive || this.activePoint < MOV_CST ) return -1;
        
        this.angle = (this.angle + degrees) % 360;
        System.out.println(this.name + "が砲塔を回転: " + this.angle + "度");
        this.activePoint -= MOV_CST;
        return 0;
    }

    // 修理メソッド
    public int repair() {
        if (!this.isAlive ) return -1;
        
        /* 修理関数 
         * 残行動力をすべて使ってHPを回復する
         * 最大行動力（＝最大修理コストREP_CST)で修理したときに50％回復する設計
         * */
        double amount = (double)this.activePoint / REP_CST * this.maxHp / 2;

        // 修理後のhpがmaxHpを超えないように回復
        this.hp = (int)Math.min(this.maxHp, this.hp + amount);
        
        //経過メッセージ（デバッグ用）
        System.out.println(this.name + "を修理！（HP: " + this.hp + "/" + this.maxHp + "）");
        
        //行動力は全部消費
        this.activePoint = 0;
        return 0;
    }

    // 弾薬補給メソッド
    public int reloadAmmo(int amount) {
        if (!this.isAlive || this.activePoint < CHG_CST ) return -1;
        this.ammo += amount;
        System.out.println(this.name + "が弾薬補給！（弾薬: " + this.ammo + "）");
        this.activePoint -= CHG_CST;
        return 0;
    }
    
	
	
    

    // ゲッター
    public String getName() { return name; }
    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }
    public int getAttack() { return attack; }
    public int getDefense() { return defense; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getAngle() { return angle; }
    public int getAmmo() { return ammo; }
    public boolean isAlive() { return isAlive; }
    public int activity() { return activePoint; }
    public int jinei() { return jinei; }
    public long serial() { return serialNo; }
    public int getRange() { return MAX_RNG; }
    public int getMaxAmmo() { return maxAmmo; }
    public double getRrate() { return rRate; }


    
    
    // セッター
    protected void setMaxHp(int n) {maxHp = n; return; }
    protected void setAttack(int n) { attack = n; return ; }
    protected void setDefense(int n) { defense = n; return; }
    public void setType(String n) { modelType = n; return; }
    public void resetAct() { activePoint = MAX_ACT; return;  }

    
    // 戦車の状態を表示
    public void displayStatus() {
        System.out.println("===== " + name + " =====");
        System.out.println("HP: " + hp + "/" + maxHp);
        System.out.println("攻撃力: " + attack);
        System.out.println("防御力: " + defense);
        System.out.println("位置: (" + x + ", " + y + ")");
        System.out.println("弾薬: " + ammo);
        System.out.println("状態: " + (isAlive ? "稼働中" : "破壊"));
    }
    
	Random rnd = new Random();

	
	//攻撃判定メソッド群
    // 測距メソッド
    
	public double distance(Tank target) {	
		return Math.sqrt(distanceSQ(target));
	}

	private double distanceSQ(Tank target) {				//距離の二乗を返すメソッド
		double temp;
		temp = Math.pow(target.x-this.x , 2.0) + Math.pow(target.y-this.y , 2.0);
		return temp;
	}
	

	public double normalDamage(Tank target) {  //基準与ダメージ（ランダム要素を含まない）
		final double atackRange = 20.0;
		final double baranceParam = 0.5;
		return  Math.pow((atackRange - distance(target)) , 2.0) * this.attack * baranceParam;
	}
	
	private int damage(Tank target) {	//ランダム要素を加味し、実際に攻撃で与えるダメージ返すメソッド
		
		
  		return (int)( normalDamage(target)*((1-this.rRate) + 2 * this.rRate * Math.random())); 
	}
	

	private boolean meichu2(Tank target) {					//標準命中判定 旧版
//		double k=(1-1/Math.pow((21-distanceSQ(target)),0.1))*(1-1/Math.pow(21, 0.1))+Math.random();
		final double K =(1-1/Math.pow(MAX_RNG, 0.1));
				
		double p =(1-1/Math.pow(MAX_RNG - distance(target) , 0.1)) / K ;
        System.out.println("命中率: " + p *100 +"%");
        
		if (p + Math.random() >1) {  //距離の二乗の逆数の 0～5倍を判定パラメータとする
			return true;
		} else {
			return false;
		}
	}
	
	private boolean meichu(Tank target) {					//命中判定で、命中率を分割
	     
		double p = HitRate(target);
	     
         System.out.println("命中率: " + p *100 +"%");
	     
         if (Math.random() < p) return true;
		 return false;
		 
		 
		}	
	

	public double HitRate(Tank target) {					//命中率のみ分割してをpublic化 
		
		double D = distance(target);						//敵との距離
		double p;
	    
		// xが0以下の場合はTrue、xがL以上の場合はfalseを返す（範囲外の制御）
	    if (D <= AHD) {
	    	p =  1.0;
	    } else  if(D >= MAX_RNG) {
	    	p = -1.0;
		} else {
			p = 0.5 * (Math.cos((Math.PI / (MAX_RNG-AHD)) * (D - AHD)) + 1.0) ;
		}
		/**
	     * コサイン曲線を用いた0.0 から 1.0 の間の減衰値曲線を利用
	     * 式: Y = 0.5 * (cos(PI / L * X) + 1.0)
	     * ランダム値がこの値以下なら命中
	     * 
	     */
	     
		 return p;
		 
		}	
	

	//インスタンス比較用
	@Override
    public boolean equals(Object obj) {
        // 1. 同じ参照なら即座にtrue
        if (this == obj) {
            return true;
        }
        
        // 2. nullチェック
        if (obj == null) {
            return false;
        }
        
        // 3. クラスのチェック（Tankまたはそのサブクラスか）
        if (!(obj instanceof Tank)) {
            return false;
        }
        
        // 4. Tankにキャストしてから比較
        Tank other = (Tank) obj;
        return this.serialNo == other.serialNo;
    }
    
    @Override
    public int hashCode() {
        return Long.hashCode(serialNo);
    }


}
