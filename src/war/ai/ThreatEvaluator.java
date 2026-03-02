package war.ai;

import war.tank.Tank;

/**
 * ThreatEvaluator - 行動判断
 * BattleState の情報と AIConfig のパラメータをもとに、
 * 
 */
public class ThreatEvaluator {

    private final AIConfig config;

 /*
  * コンストラクタ
  */
    public ThreatEvaluator(AIConfig config) {
        this.config = config;
    }

    // ========================================
    // 公開メソッド
    // ========================================

    /**
     * 脅威値を計算して返す
     * 距離・HP・距離順位の3要素で構成される
     */
    public double calcThreat(BattleState state) {
        double threat = 0.0;
        threat += calcDistanceThreat(state) * config.THREAT_WEIGHT_DISTANCE;
        threat += calcHPThreat(state)       * config.THREAT_WEIGHT_HP;
        threat += calcRankThreat(state)     * config.THREAT_WEIGHT_RANK;

 //       return clamp(threat); // 100以上で不感帯が生じるので廃止
        return threat;
    }

    /**
     * 機会値を計算して返す
     * 距離・HPの2要素で構成される
     */
    public double calcOpportunity(BattleState state) {
        double opportunity = 0.0;
        opportunity += calcDistanceOpportunity(state) * config.OPP_WEIGHT_DISTANCE;
        opportunity += calcHPOpportunity(state)       * config.OPP_WEIGHT_HP;
        return opportunity;
    }

    // ========================================
    // 脅威値の内訳計算（privateメソッド）
    // ========================================

    /**
     * 距離による脅威値（0.0〜100.0）
     * 危険距離内は最大、それ以外は距離に応じて線形減衰
     */
    private double calcDistanceThreat(BattleState state) {
        if (state.selfRange <= 0) return 0.0;
        double distRatio = state.targetDistance / state.selfRange;

        if (distRatio <= config.DANGER_RANGE) {
            return 100.0;
        }
        return Math.max(0.0, 100.0 * (1.0 - distRatio));
    }

    /**
     * HPによる脅威値（0.0〜100.0）
     * HPが低いほど脅威が高い（非線形曲線）
     */
    private double calcHPThreat(BattleState state) {
        return 100.0 * Math.pow(1.0 - state.selfHPRatio, config.HP_CURVE_EXPONENT);
    }

    /**
     * 距離順位による脅威値
     * 敵に最も近い＝最も狙われやすい＝脅威が高い
     */
    private double calcRankThreat(BattleState state) {
        switch (state.distanceRankFromEnemy) {
            case 1:  return config.RANK_1_THREAT;
            case 2:  return config.RANK_2_THREAT;
            case 3:  return config.RANK_3_THREAT;
            default: return config.RANK_4_THREAT;
        }
    }

    // ========================================
    // 機会値の内訳計算（privateメソッド）
    // ========================================

    /**
     * 距離による機会値（0.0〜100.0）
     * 最適射程内で最大、近すぎ・遠すぎで減少
     */
    private double calcDistanceOpportunity(BattleState state) {
        if (state.selfRange <= 0) return 0.0;
        double distRatio = state.targetDistance / state.selfRange;

        if (distRatio >= config.OPTIMAL_RANGE_MIN && distRatio <= config.OPTIMAL_RANGE_MAX) {
            // 最適射程内は最大
            return 100.0;
        } else if (distRatio < config.OPTIMAL_RANGE_MIN) {
            // 近すぎる：最適射程下限に向かって線形増加
            return 100.0 * (distRatio / config.OPTIMAL_RANGE_MIN);
        } else {
            // 遠すぎる：射程外に向かって線形減少
            double remain = 1.0 - config.OPTIMAL_RANGE_MAX;
            if (remain <= 0) return 0.0;  // ゼロ除算防止
            return Math.max(0.0, 100.0 * (1.0 - (distRatio - config.OPTIMAL_RANGE_MAX) / remain));
        }
    }

    /**
     * HPによる機会値（0.0〜100.0）
     * HPが高いほど攻撃的になれる（非線形曲線）
     */
    private double calcHPOpportunity(BattleState state) {
        return 100.0 * Math.pow(state.selfHPRatio, config.HP_CURVE_EXPONENT);
    }

    // ========================================
    // ユーティリティ
    // ========================================

    /** 値を 0.0〜100.0 に収める */
    private double clamp(double value) {
        return Math.min(100.0, Math.max(0.0, value));
    }



    public enum Times {
        SINGLE,
        DOUBLE
    }
    
    // ========================================
    // 判断パラメーター算出
    // ========================================
    
    //　ラッパー
    public double calcAC(Times times , Tank self , Tank target) {
    	return getParam(times , self , target);
    }
    
    public double calcDC(Times times , Tank self , Tank target) {
    	return getParam(times , target , self);
    }
    public double calcAT(Times times , Tank SimuSelf , Tank target) {

    	return getParam(times , SimuSelf , target);
    }
    public double calcDT(Times times , Tank SimuSelf , Tank target) {
    	return getParam(times , target , SimuSelf);
    }
    
    //　本体
    
    public double getParam(Times times, Tank offence, Tank deffence) {
    /*
    *　  　　　offence   deffenc  
    *  AC 　　  self     terget   
    *  DC 　　 target     self 
    *  AT 　　 selfSIM   terget   
    *  DT 　　 target    selfSIM  
    */

        double damageRng = offence.normalDamage(deffence) * 2 * offence.getRrate();
    	double damageMax = offence.normalDamage(deffence) + damageRng / 2;
    	double damageMin = offence.normalDamage(deffence) - damageRng / 2;

//    	System.out.printf("Min %.2f , Rng %.2f , ",damageMin, damageRng);
    	
    	
    	double HP = deffence.getHp();
        
        double hit = offence.HitRate(deffence);

        
        double getResult = 0 ;

        hit = offence.HitRate(deffence);

        switch (times) {
            case SINGLE: //1回で倒せるかどうか
                if (damageMax > HP) { 				//　　まず最大ダメージがHPを超えている
                	getResult = Math.max(0.0, Math.min(1 - (HP - damageMin)/(2 * damageRng), 1.0))  * hit;
                                   	
                } else {
                    getResult = 0;
                }
                break;

            case DOUBLE:
                // 2発で倒す可能性(1発で倒す可能性も含む

            	if( HP > damageMin*2) {
            		getResult =  doubleResponce(HP-damageMin*2,damageRng) * hit * hit; //2回当たるケース
            		getResult += Math.max(0.0, Math.min(1 - (HP - damageMin)/(2 * damageRng), 1.0))  * 2*(1-hit)*hit;
            	} else {
            		getResult = 1.0 * hit;;
            	}
                break;
                
            default:
            	getResult = 0;
        }
        
        return getResult;
        }

        


    /**
     * 0〜rangeの実数（乱数）を2回足したとき、target以上になる確率を計算する
     * (連続型一様分布の和：幾何学的確率による算出)
     */
    public double doubleResponce(double target, double range) {
        // 1. 範囲外のガード
        if (target <= 0) return 1.0;
        if (target >= range * 2) return 0.0;
        if (range <= 0) return 0.0;

        // 2. 面積（幾何学的確率）による計算
        // 全体の面積は正方形 (range * range)
        double totalArea = Math.pow(range, 2);
        double prob;

        if (target <= range) {
            // 目標値が range 以下のとき
            // 全体から「左下の三角形（target未満）」の面積を引く
            double lowerTriangleArea = Math.pow(target, 2) / 2.0;
            prob = (totalArea - lowerTriangleArea) / totalArea;
        } else {
            // 目標値が range より大きいとき
            // 「右上の三角形（target以上）」の面積を直接求める
            // 三角形の辺の長さ = (range * 2) - target
            double upperTriangleSide = (range * 2) - target;
            double upperTriangleArea = Math.pow(upperTriangleSide, 2) / 2.0;
            prob = upperTriangleArea / totalArea;
        }
        return prob; 
    }

    
    /**
     *  ダミーシミュレーター
     *  移動後のシミュレーションを行うための一連のメソッド
     */
     
    // 移動シミュレーター
	public void progOne(Tank tank, double x, double y) {
		tank. resetAct();
    	while(tank.activity() > 4) {
    		tank.move(x,y);
    	}
		
	}

}
	
