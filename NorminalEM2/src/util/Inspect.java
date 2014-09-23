package util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import em.ApplyEM;

import model.Entity;
import model.Mention;
import model.CoNLL.CoNLLDocument;
import model.CoNLL.CoNLLPart;

public class Inspect {

	public static void main(String args[]) {
		ArrayList<String> lines = Common.getLines("chinese_list_all_test");
		for (String line : lines) {
			CoNLLDocument d = new CoNLLDocument(line.replace("auto_conll", "gold_conll"));
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

							for (int j = i - 1; j >= 0; j--) {
								Mention m1 = chain.mentions.get(j);
								if (!part.getWord(m1.end).posTag.equals("PN")
										&& m1.end != m2.end) {
									m1.s = part.getWord(m1.end).sentence;
									m2.s = part.getWord(m2.end).sentence;
									int s1 = part.getWord(m1.end).sentence
											.getSentenceIdx();
									int s2 = part.getWord(m2.end).sentence
											.getSentenceIdx();

									if (!part.getWord(m1.end).word.equals(part
											.getWord(m2.end).word)
											&& s1 - s2 == 0) {
										ApplyEM.print(m1, m2, part,
												new HashMap<String, Integer>());
										System.out.println(part.getPartName());
									}
									// .out.println(s2 + " " + s1 + ":" +
									// m2.extent + "#" + m1.extent);
									total += 1.0;
									int distance = s2 - s1;
									if (map.keySet().contains(distance)) {
										map.put(distance, map.get(distance)
												.intValue() + 1);
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
				double val = 100.0 * map.get(key) / total;
				all += val;
				// System.out.println("Distance: " + key + " : " + val + " # " +
				// all);
			}
		}
	}
}
