package war.ai;

import war.tank.Tank;

/**
 * ThreatEvaluator - 行動判断
 * BattleState の情報と AIConfig のパラメータをもとに、
 * 
 */
public class ThreatEvaluator {

    private final AIConfig config;
    private BattleState state;
 /*
  * コンストラクタ
  */
    public ThreatEvaluator(AIConfig config,BattleState sts) {
        this.config = config;
        this.state = sts;
    }

    // ========================================
    // 公開メソッド
    // =======================================
    public enum Times {
        SINGLE,
        DOUBLE
    }
    
    // ========================================
    // 判断パラメーター AC,DC,AT,DT 算出
    // ========================================
    
    //　ラッパー
    public double calcAC(Times times , Tank self , Tank target) {return getParam(times , self , target);}
    public double calcDC(Times times , Tank self , Tank target) {return getParam(times , target , self) * calcRankThreat(self,target);}
    public double calcAT(Times times , Tank SimuSelf , Tank target) {return getParam(times , SimuSelf , target);}
    public double calcDT(Times times , Tank SimuSelf , Tank target) {return getParam(times , target , SimuSelf) * calcRankThreat(SimuSelf,target);}
    
    //　本体
    public double getParam(Times times, Tank offence, Tank deffence) {
    /* 　  　 offence   deffenc  
    *  AC 　　  self     terget   
    *  DC 　　 target     self 
    *  AT 　　 selfSIM   terget   
    *  DT 　　 target    selfSIM  */

        double damageRng = offence.normalDamage(deffence) * 2 * offence.getRrate();
    	double damageMax = offence.normalDamage(deffence) + damageRng / 2;
    	double damageMin = offence.normalDamage(deffence) - damageRng / 2;

    	double HP = deffence.getHp();
        double hit = offence.HitRate(deffence);
        
        double getResult = 0 ;

        hit = offence.HitRate(deffence);

        switch (times) {
            case SINGLE: //1回で倒せるかどうか
            	getResult = getProbabilityGreaterThan((HP - damageMin)/(2 * damageRng), 3) * hit ;           	
            	break;
            case DOUBLE:
                // 2発で倒す可能性(1発で倒す可能性も含む)
            	// 2発とも当たる可能性			（Hit^2)
            	// 1発だけ当たる可能性			2 * Hit - Hit^2 -Hit^2 = 2 * Hit * (1-Hit)  
            	// 1発も当たらない可能性		(1-Hit)^2 = 1-2*Hit + Hit^2
            	// ダメージのランダム成分ははランダムの3回重ね合わせなので、2回命中時は6回の重ね合わせになる

            	if( HP > damageMin*2) {
            		getResult =  getProbabilityGreaterThan((HP - damageMin * 2)/(4 * damageRng), 6) * hit * hit; //2回当たるケース
            		getResult +=  getProbabilityGreaterThan((HP - damageMin)/(2 * damageRng), 3) * 2 * (1 - hit) * hit; //1回当たるケース
            	
            	} else if ( HP >= damageMin){ 	//HPが最低ダメージの2倍以下だと、2回当たると必ず撃破となる
            		getResult = 1.0 * hit * hit;
            	} else {						//HPが最低ダメージ以下だと、2回外さない限り、必ず撃破となる
            		getResult = 1.0 * (1.0 - (1.0 - hit) * (1.0 - hit)) ;
            	}
                break;
            default:
            	getResult = 0;
        }
        return getResult;
    }


    // ========================================
    // 機会値の内訳計算（privateメソッド）
    // ========================================

    /**
     * 距離順位による脅威値
     * 敵に最も近い＝最も狙われやすい＝脅威が高い
     */
    private double calcRankThreat(Tank self,Tank target) {

    	int count = 0;
        for (double value : this.state.disListFromEnemy) {
            if (value < self.distance(target)) {
                count++;
            }
        }
        // countが0なら最小、countがarray.lengthなら最大（targetより大きい要素がない）
    	
    	switch (count) {
            case 0:  return config.RANK_1_THREAT;
            case 1:  return config.RANK_2_THREAT;
            case 2:  return config.RANK_3_THREAT;
            case 3:  return config.RANK_3_THREAT;
            default: return config.RANK_4_THREAT;
        }   
    }
   
    /******************************************************************
     * 0-1の乱数n個の和がtを超える確率を計算する
     * @param t 閾値 (0 <= t <= 1)
     * @param n 重ね合わせ回数
     * @return 確率 (0.0 ～ 1.0)
     */
    public static double getProbabilityGreaterThan(double t, int n) {
        if (t <= 0) return 1.0;
        if (t >= 1) return 0.0;
        
        t = t * n;

        double cdf = 0.0;
        int maxK = (int) Math.floor(t);

        for (int k = 0; k <= maxK; k++) {
            double term = nCr(n, k) * Math.pow(t - k, n);
            if (k % 2 == 1) {
                cdf -= term;
            } else {
                cdf += term;
            }
        }

        cdf = cdf / factorial(n);
       
     // 1.0 - CDF(t) が「tを超える確率」
        return 1.0 - cdf;
    }

    // 二項係数 nCr
    private static double nCr(int n, int r) {
        if (r < 0 || r > n) return 0;
        if (r == 0 || r == n) return 1;
        if (r > n / 2) r = n - r;

        double res = 1;
        for (int i = 1; i <= r; i++) {
            res = res * (n - i + 1) / i;
        }
        return res;
    }

    // 階乗 n!
    private static double factorial(int n) {
        double res = 1;
        for (int i = 2; i <= n; i++) {
            res *= i;
        }
        return res;
    }
    //******************************************************************

	
	    

}
	
