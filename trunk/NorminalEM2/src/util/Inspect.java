package util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import model.Entity;
import model.Mention;
import model.CoNLL.CoNLLDocument;
import model.CoNLL.CoNLLPart;

public class Inspect {

	public static void main(String args[]) {
		CoNLLDocument d = new CoNLLDocument("train_gold_conll");
		ArrayList<CoNLLPart> parts = new ArrayList<CoNLLPart>();
		parts.addAll(d.getParts());
		int i = parts.size();

		double total = 0;
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		for (CoNLLPart part : parts) {
			ArrayList<Entity> chains = part.getChains();
			for (Entity chain : chains) {
				Collections.sort(chain.mentions);
				for (i = 1; i < chain.mentions.size(); i++) {
					Mention m2 = chain.mentions.get(i);
					
					if (part.getWord(m2.end).posTag.equals("NN")) {

						for (int j=i-1;j>=0;j--) {
							Mention m1 = chain.mentions.get(j);
							if (!part.getWord(m1.end).posTag.equals("PN") && m1.end!=m2.end) {
								int s1 = part.getWord(m1.end).sentence.getSentenceIdx();
								int s2 = part.getWord(m2.end).sentence.getSentenceIdx();
								
								System.out.println(s2 + " "  + s1 + ":" + m2.extent + "#" + m1.extent);
								total += 1.0;
								int distance = s2 - s1;
								if (map.keySet().contains(distance)) {
									map.put(distance, map.get(distance).intValue()+1);
								} else {
									map.put(distance, 1);
								}
								break;
							}
						}
					}
				}
			}
		}
		ArrayList<Integer> lst = new ArrayList<Integer>(map.keySet());
		Collections.sort(lst);
		double all = 0.0;
		for (Integer key : lst) {
			double val = 100.0 * map.get(key)/total;
			all += val;
			System.out.println("Distance: " + key + " : " + val + " # " + all);
		}
	}
}
