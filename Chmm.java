package org.eclipse.om2m.test;

public class Chmm {
	/**
	 *  HMM模型
	 *  @param obs 觀測序列
	 *  @param states 隱狀態
	 *  @param start_p 初始狀態
	 *  @param trans_p 轉移狀態
	 *  @param emit_p 發射狀態
	 *  @return 最可能的序列
	 */
	
	public static int[] compute(int[] obs1, int[] obs2, int[] states, double[] start_p
			, double[][] trans_p, double[][] emit_p1 , double[][] emit_p2)
	{
		double[][] V1 = new double[obs1.length][states.length];
		double[][] V2 = new double[obs2.length][states.length];
		int[][] path1 = new int[states.length][obs1.length];
		int[][] path2 = new int[states.length][obs2.length];
		
		for (int y : states)
		{
			V1[0][y] = start_p[y] * emit_p1[y][obs1[0]];
			V2[0][y] = start_p[y] * emit_p2[y][obs2[0]];
			path1[y][0] = y;
			
		}
		
		for (int t = 1; t < obs1.length; ++t)
		{
            int[][] newpath1 = new int[states.length][obs1.length];
            int[][] newpath2 = new int[states.length][obs2.length];
            
            
            for (int y : states)
            {
                double prob1 = -1, prob2 = -1;
                int state1,state2;
                for (int y0 : states)
                {
                	for (int y1 : states){
                        double nprob1 = V1[t - 1][y0] * trans_p[y0][y] * emit_p1[y][obs1[t]] * V2[t - 1][y1] * trans_p[y1][y];
                        double nprob2 = V2[t - 1][y0] * trans_p[y0][y] * emit_p2[y][obs2[t]] * V1[t - 1][y1] * trans_p[y1][y];
                        if (nprob1 > prob1)
                        {
                            prob1 = nprob1;
                            state1 = y0;
                            state2 = y1;
                            // 紀錄最大概率
                            V1[t][y] = prob1;
                            // 紀錄路徑
                            System.arraycopy(path1[state1], 0, newpath1[y], 0, t);
                            newpath1[y][t] = y;
                        }
                        
                        if (nprob2 > prob2)
                        {
                            prob2 = nprob2;
                            state1 = y0;
                            state2 = y1;
                            // 紀錄最大概率
                            V2[t][y] = prob2;
                            // 紀錄路徑
                            System.arraycopy(path2[state1], 0, newpath2[y], 0, t);
                            newpath2[y][t] = y;
                        }
                	}
                }
            }
 
            path1 = newpath1;
		}
		
		double prob = -1;
		int state = 0;
		for (int y : states)
		{
			if (V1[obs1.length - 1][y] > prob)
			{
				prob = V1[obs1.length - 1][y];
				state = y;
			}
		}
		
		for (int y = 0 ; y < obs1.length; y++)
		{
			if (V1[y][state] >= V2[y][state]){
				continue;
			}
			else
				path1[state][y] = path2[state][y];
		}
		
		return path1[state];
	}
	
	
	
	public static int[] HMMcompute(int[] obs, int[] states, double[] start_p, double[][] trans_p, double[][] emit_p)
	{
	    double[][] V = new double[obs.length][states.length];
	    int[][] path = new int[states.length][obs.length];
	 
	    for (int y : states)
	    {
	        V[0][y] = start_p[y] * emit_p[y][obs[0]];
	        path[y][0] = y;
	    }
	 
	    for (int t = 1; t < obs.length; ++t)
	    {
	        int[][] newpath = new int[states.length][obs.length];
	 
	        for (int y : states)
	        {
	            double prob = -1;
	            int state;
	            for (int y0 : states)
	            {
	                double nprob = V[t - 1][y0] * trans_p[y0][y] * emit_p[y][obs[t]];
	                if (nprob > prob)
	                {
	                    prob = nprob;
	                    state = y0;
	                    // 紀錄最大概率
	                    V[t][y] = prob;
	                    // 紀錄路徑
	                    System.arraycopy(path[state], 0, newpath[y], 0, t);
	                    newpath[y][t] = y;
	                 }
	            }
	        }
	 
	        path = newpath;
	    }
	 
	    double prob = -1;
	    int state = 0;
	    for (int y : states)
	    {
	        if (V[obs.length - 1][y] > prob)
	        {
	            prob = V[obs.length - 1][y];
	            state = y;
	        }
	    }
	 
	    return path[state];
	}
	
}
