package em;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import model.Entity;
import model.EntityMention;
import model.CoNLL.CoNLLDocument;
import model.CoNLL.CoNLLPart;
import util.Common;

public class EvaluateAnaphorBaseline {

	public static void main(String args[]) {
//		String path = "key.chinese.test.open.goldMentions";
//		String path = "key.chinese.development.open.systemParse";
		HashMap<String, ArrayList<String[]>> allSys = extractSysKeys();
		
		ArrayList<String> fnLines = Common
				.getLines("chinese_list_all_test");
		
		HashMap<String, HashMap<String, HashSet<String>>> allKeys = new HashMap<String, HashMap<String, HashSet<String>>>();
		for (String line : fnLines) {
			CoNLLDocument goldDoc = new CoNLLDocument(line.replace(
					"auto_conll", "gold_conll"));
			for(CoNLLPart part : goldDoc.getParts()) {
				HashMap<String, HashSet<String>> keys = EMUtil.getGoldAnaphorKeys(
						part.getChains(), part);
				allKeys.put(part.getPartName(), keys);
			}
		}
		
		double allG = 0;
		double allS = 0;
		double hit = 0;
		for(String p : allKeys.keySet()) {
			HashMap<String, HashSet<String>> keys = allKeys.get(p);
			ArrayList<String[]> sys = allSys.get(p);
			allG += keys.size();
			allS += sys.size();
			for(String[] s : sys) {
				if(keys.containsKey(s[0])) {
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
		System.out.println("Recall: " + r);
		System.out.println("Precis: " + p);
		System.out.println("F-scor: " + f);
	}

	private static HashMap<String, ArrayList<String[]>> extractSysKeys() {
		String path = "/users/yzcchen/chen3/conll12/chinese/key.chinese.test.open";
		CoNLLDocument sysDoc = new CoNLLDocument(path);
		HashMap<String, ArrayList<String[]>> allSys = new HashMap<String, ArrayList<String[]>>();
		for(CoNLLPart part : sysDoc.getParts()) {
			ArrayList<String[]> sys = new ArrayList<String[]>();
			allSys.put(part.getPartName(), sys);
			ArrayList<Entity> chains = part.getChains();
			for(Entity e : chains) {
				Collections.sort(e.mentions);
				for(int i=0;i<e.mentions.size();i++) {
					EntityMention m1 = e.mentions.get(i);
					String pos = part.getWord(m1.end).posTag;
					if(pos.equals("PN") || pos.equals("NR") || pos.equals("NT")) {
						continue;
					}
					
					for(int j=i-1;j>=0;j--) {
						EntityMention m2 = e.mentions.get(j);
						String pos2 = part.getWord(m2.end).posTag;
						if(!pos2.equals("PN") && m2.end!=m1.end) {
							String[] s = new String[2];
							s[0] = m1.toName();
							s[1] = m2.toName();
							sys.add(s);
							break;
						}
						
					}
				}
			}
		}
		return allSys;
	}

}
