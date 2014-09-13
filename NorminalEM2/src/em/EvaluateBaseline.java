package em;

import java.util.HashMap;
import java.util.HashSet;

public class EvaluateBaseline {

	public static void main(String args[]) {
//		String path = "key.chinese.test.open.goldMentions";
//		String path = "key.chinese.development.open.systemParse";
//		String path = "/users/yzcchen/chen3/conll12/chinese/key.chinese.test.open";
//		String path = "ims-out1.key";
		String path = "key.chinese.test.open.systemParse";
//		String path = "/users/yzcchen/CoNLL-2012/conll-2012/v4/data/test/data/chinese/annotations/all_chi_test.coref";
		
		HashMap<String, HashMap<String, String>> allSys = EMUtil.extractSysKeys(path);
		HashMap<String, HashMap<String, HashSet<String>>> allKeys = EMUtil.extractGoldKeys();
		
		double allG = 0;
		double allS = 0;
		double hit = 0;
		for(String p : allKeys.keySet()) {
			HashMap<String, HashSet<String>> keys = allKeys.get(p);
			HashMap<String, String> sys = allSys.get(p);
			allG += keys.size();
			allS += sys.size();
			for(String s : sys.keySet()) {
				if(keys.containsKey(s) && keys.get(s).contains(sys.get(s))) {
					hit++;
				}
			}
		}
		double r = hit/allG;
		double p = hit/allS;
		double f = 2*r*p/(r+p);
		System.out.println("hit: " + hit);
		System.out.println("Gol: " + allG);
		System.out.println("Sys: " + allS);
		System.out.println("=====================");
		System.out.println("Recall: " + r * 100);
		System.out.println("Precis: " + p * 100);
		System.out.println("F-scor: " + f * 100);
	}

}
