package em;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import model.Mention;
import model.CoNLL.CoNLLDocument;
import model.CoNLL.CoNLLPart;
import util.Common;

public class RuleAnaphorNounDetector {

	public static boolean isAnahporic(Mention m, ArrayList<Mention> cands,
			CoNLLPart part) {
		boolean anaphor = false;
		
		if(m.extent.startsWith("这")
					|| m.extent.startsWith("那") || m.extent.startsWith("该") || m.extent.startsWith("此")
				) {
//			return true;
		}
		
		for (Mention cand : cands) {
			if ((cand.head.equals(m.head) && 
					Context.wordInclusion(cand, m, part)==1) || Context.sieve4Rule(cand, m, part)==1
					) {
				anaphor = true;
				break;
			}
//			String t1 = EMUtil.getSemantic(cand);
//			String t2 = EMUtil.getSemantic(m);
//			if( t1.equals(t2) && !"unknown".startsWith(t1) && cand.getModifier(part).contains(m.getModifier(part))
//					&& !cand.getModifier(part).trim().isEmpty()) {
//				anaphor = true;
//				break;
//			}
		}
		return anaphor;
	}

	public static void main(String args[]) {
		ArrayList<String> lines = Common.getLines("chinese_list_all_test");
		double hit = 0;
		double gold = 0;
		double sys = 0;

		for (String line : lines) {
			CoNLLDocument doc = new CoNLLDocument(line
//					.replace("auto_conll", "gold_conll")
					);

			for (CoNLLPart part : doc.getParts()) {

				CoNLLPart goldPart = EMUtil.getGoldPart(part, "test");

				HashMap<String, HashSet<String>> goldAnaphors = EMUtil
						.getGoldAnaphorKeys(goldPart.getChains(), goldPart);
				gold += goldAnaphors.size();

				ArrayList<Mention> sysMentions = EMUtil
						.extractMention(part);

				ArrayList<Mention> allCandidates = new ArrayList<Mention>();
				for (Mention m : sysMentions) {
					if (!part.getWord(m.end).posTag.equals("PN")) {
						allCandidates.add(m);
					}
				}

				ArrayList<Mention> anaphors = new ArrayList<Mention>();
				for (Mention m : sysMentions) {
					String pos = part.getWord(m.end).posTag;
					if (pos.equals("NT") || pos.equals("NR")
							|| pos.equals("PN")) {
						continue;
					}
					ArrayList<Mention> cands = new ArrayList<Mention>();
					for (int h = allCandidates.size() - 1; h >= 0; h--) {
						Mention cand = allCandidates.get(h);
						cand.sentenceID = part.getWord(cand.start).sentence
								.getSentenceIdx();
						cand.s = part.getWord(cand.start).sentence;
						if (cand.start < m.start
								&& m.sentenceID - cand.sentenceID <= EMLearn.maxDistance
								&& cand.end != m.end) {
							cands.add(cand);
						}
					}
					if (!isAnahporic(m, cands, part)) {
						
						
						if(goldAnaphors.containsKey(m.toName())) {
							System.out.println(m.extent);
							System.out.println(line);
						}
						
						
						continue;
					}
					anaphors.add(m);
				}
				sys += anaphors.size();

				for (Mention ana : anaphors) {
					if (goldAnaphors.containsKey(ana.toName())) {
						hit += 1;
					} else {
//						System.out.println(ana.extent);
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
