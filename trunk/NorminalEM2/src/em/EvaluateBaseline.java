package em;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import util.Common;

public class EvaluateBaseline {

	public static void main(String args[]) {
		if(args.length!=1) {
			System.out.println("java ~ folder");
			System.exit(1);
		}
//		String path = "key.chinese.test.open.goldMentions";
//		String path = "key.chinese.development.open.systemParse";
//		String path = "/users/yzcchen/chen3/conll12/chinese/key.chinese.test.open";
		String path = "ims-out1.key";
//		String path = "key.chinese.test.open.systemParse";
//		String path = "/users/yzcchen/CoNLL-2012/conll-2012/v4/data/test/data/chinese/annotations/all_chi_test.coref";
		
		HashMap<String, HashMap<String, String>> allSys = EMUtil.extractSysKeys(path);
		HashMap<String, HashMap<String, HashSet<String>>> allKeys = EMUtil.extractGoldKeys();
		
		ArrayList<String> lines = Common.getLines("chinese_list_" + args[0] + "_test");
//		bn-cbs-01-cbs_0129_0
		HashSet<String> set = new HashSet<String>();
		
		HashSet<String> set0 = new HashSet<String>();
		HashSet<String> set1 = new HashSet<String>();
		HashSet<String> set2 = new HashSet<String>();
		HashSet<String> set3 = new HashSet<String>();
		HashSet<String> set4 = new HashSet<String>();
		
		for(int i=0;i<lines.size();i++) { 
			String line = lines.get(i);
			int k = line.indexOf("/annotations/");
			int b = line.lastIndexOf(".");
			String stem = line.substring(k + "/annotations/".length(), b).replace("/", "-");
			if(i%5==0) {
				set0.add(stem);
			} else if(i%5==1) {
				set1.add(stem);
			} else if(i%5==2) {
				set2.add(stem);
			} else if(i%5==3) {
				set3.add(stem);
			} else if(i%5==4) {
				set4.add(stem);
			}
		}
		
		eva(allSys, allKeys, args[0], set0);
		eva(allSys, allKeys, args[0], set1);
		eva(allSys, allKeys, args[0], set2);
		eva(allSys, allKeys, args[0], set3);
		eva(allSys, allKeys, args[0], set4);
		
		set.addAll(set0);
		set.addAll(set1);
		set.addAll(set2);
		set.addAll(set3);
		set.addAll(set4);
		eva(allSys, allKeys, args[0], set);
//		eva(allSys, allKeys, "nw");
//		eva(allSys, allKeys, "mz");
//		eva(allSys, allKeys, "wb");
//		eva(allSys, allKeys, "bn");
//		eva(allSys, allKeys, "bc");
//		eva(allSys, allKeys, "tc");
	}

	private static void eva(HashMap<String, HashMap<String, String>> allSys,
			HashMap<String, HashMap<String, HashSet<String>>> allKeys, String source, HashSet<String> set) {
		double allG = 0;
		double allS = 0;
		double hit = 0;
		for(String p : allKeys.keySet()) {
//			System.out.println(p);
			int k = p.lastIndexOf("_");
			String stem = p.substring(0,  k);
			if(!set.contains(stem)) {
				continue;
			}
			
			if(source.equals("all")) {
				
			} else if(!p.startsWith(source)) {
				continue;
			}
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
		System.out.println("Source: " + source);
		System.out.println("hit: " + hit);
		System.out.println("Gol: " + allG);
		System.out.println("Sys: " + allS);
		System.out.println("=====================");
		System.out.println("Recall: " + r * 100);
		System.out.println("Precis: " + p * 100);
		System.out.println("F-scor: " + f * 100);
		System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
	}

}
