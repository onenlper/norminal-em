package em;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import model.EntityMention;
import model.CoNLL.CoNLLDocument;
import model.CoNLL.CoNLLPart;
import util.Common;

public class RuleAnaphorNounDetector {

	public static boolean isAnahporic(EntityMention anaphor,
			ArrayList<EntityMention> cands, CoNLLPart part) {
		boolean isAnaphor = false;

		for (EntityMention cand : cands) {
			if (Context.sieve4Rule(cand, anaphor, part) == 1
					|| Context.headSieve2(cand, anaphor, part) == 1
//					|| Context.headSieve1(cand, anaphor, part) == 1
//					|| Context.headSieve3(cand, anaphor, part) == 1 
//					|| Context.exactMatchSieve1(cand, anaphor, part) == 1)
					) {
				anaphor.antecedent = cand;
				isAnaphor = true;
				break;
			}
			if (cand.head.equals(anaphor.head)) {
				double r = Math.random();
				if (r < .0) {
					return true;
				}
			}
		}
		return isAnaphor;
	}

	public static boolean isAnahporic2(EntityMention anaphor,
			ArrayList<EntityMention> cands, CoNLLPart part) {
		boolean isAnaphor = false;
		// System.out.println(EMUtil.getSemantic(anaphor));
		if (EMUtil.getSemantic(anaphor).startsWith("J")) {
			return false;
		}

		for (int i = anaphor.start; i <= anaphor.end; i++) {
			if (part.getWord(i).posTag.equals("PU")) {
				// return false;
			}
		}

		if (part.getWord(anaphor.start).posTag.startsWith("P")) {
			// return false;
		}

		for (EntityMention cand : cands) {
			if ((Context.sieve4Rule(cand, anaphor, part) == 1
					|| Context.headSieve1(cand, anaphor, part) == 1
					|| Context.headSieve2(cand, anaphor, part) == 1
					|| Context.headSieve3(cand, anaphor, part) == 1 || Context
						.exactMatchSieve1(cand, anaphor, part) == 1)) {
				// if(cand.head.equals(anaphor.head) &&
				// anaphor.extent.contains(cand.extent)) {
				isAnaphor = true;
				break;
			}
			// String t1 = EMUtil.getSemantic(cand);
			// String t2 = EMUtil.getSemantic(m);
			// if( t1.equals(t2) && !"unknown".startsWith(t1) &&
			// cand.getModifier(part).contains(m.getModifier(part))
			// && !cand.getModifier(part).trim().isEmpty()) {
			// anaphor = true;
			// break;
			// }
		}
		return isAnaphor;
	}

	public static void main(String args[]) {
		ArrayList<String> lines = Common.getLines("chinese_list_all_test");
		double hit = 0;
		double gold = 0;
		double sys = 0;

		HashMap<String, HashMap<String, String>> maps = EMUtil
				.extractSysKeys("key.chinese.test.open.systemParse");

		for (String line : lines) {
			CoNLLDocument doc = new CoNLLDocument(line
			// .replace("auto_conll", "gold_conll")
			);

			for (CoNLLPart part : doc.getParts()) {

				CoNLLPart goldPart = EMUtil.getGoldPart(part, "test");
				HashSet<String> goldNEs = EMUtil.getGoldNEs(goldPart);
				HashSet<String> goldPNs = EMUtil.getGoldPNs(goldPart);
				HashMap<String, HashSet<String>> goldAnaphors = EMUtil
						.getGoldAnaphorKeys(goldPart.getChains(), goldPart);
				gold += goldAnaphors.size();

				ArrayList<EntityMention> sysMentions = EMUtil.extractMention(part);

				ArrayList<EntityMention> allCandidates = new ArrayList<EntityMention>();
				for (EntityMention m : sysMentions) {
					if (!part.getWord(m.end).posTag.equals("PN")) {
						allCandidates.add(m);
					}
				}

				ArrayList<EntityMention> anaphors = new ArrayList<EntityMention>();
				for (EntityMention anaphor : sysMentions) {
					String pos = part.getWord(anaphor.end).posTag;
					if (pos.equals("PN") && anaphor.end == anaphor.start) {
						continue;
					}
					ArrayList<EntityMention> cands = new ArrayList<EntityMention>();
					for (int h = allCandidates.size() - 1; h >= 0; h--) {
						EntityMention cand = allCandidates.get(h);
						cand.sentenceID = part.getWord(cand.start).sentence
								.getSentenceIdx();
						cand.s = part.getWord(cand.start).sentence;
						if (cand.start < anaphor.start
								&& anaphor.sentenceID - cand.sentenceID <= EMLearn.maxDistance
								&& cand.end != anaphor.end) {
							cands.add(cand);
						}
					}
					if (isAnahporic(anaphor, cands, part)) {
						if (goldNEs.contains(anaphor.toName())
								|| goldPNs.contains(anaphor.toName())) {
							continue;
						}
						anaphors.add(anaphor);
						for (EntityMention m : anaphor.innerMs) {
							if (goldNEs.contains(m.toName())
									|| goldPNs.contains(m.toName())) {
								continue;
							}
							anaphors.add(m);
						}
					}

				}
				// HashMap<String, String> map = maps.get(part.getPartName());
				// sys += map.size();
				// for(String k : map.keySet()) {
				// if(goldAnaphors.containsKey(k)) {
				// hit += 1;
				// }
				// }

				sys += anaphors.size();
				for (EntityMention ana : anaphors) {
					if (goldAnaphors.containsKey(ana.toName())) {
						hit += 1;
					} else {
						// System.out.println(ana.extent);
					}
				}
			}
		}
		System.out.println("Hit: " + hit);
		System.out.println("Gold: " + gold);
		System.out.println("System:	" + sys);
		System.out.println("===============");
		double r = hit / gold * 100;
		double p = hit / sys * 100;
		double f = 2 * r * p / (r + p);
		System.out.println("Recall: " + r);
		System.out.println("Precision: " + p);
		System.out.println("Recall: " + f);
	}
}
