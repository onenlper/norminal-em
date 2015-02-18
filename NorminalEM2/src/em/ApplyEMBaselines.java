package em;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import model.Element;
import model.Entity;
import model.EntityMention;
import model.CoNLL.CoNLLDocument;
import model.CoNLL.CoNLLPart;
import model.CoNLL.CoNLLSentence;
import model.CoNLL.CoNLLWord;
import model.syntaxTree.MyTreeNode;
import util.Common;
import edu.stanford.nlp.classify.LinearClassifier;
import em.EMUtil.Grammatic;

public class ApplyEMBaselines {

	String folder;

	Parameter numberP;
	Parameter genderP;
	Parameter animacyP;
	 Parameter personP;
	 Parameter personQP;

	double contextOverall;

	HashMap<String, Double> contextPrior;

	int overallGuessPronoun;

	HashMap<Short, Double> pronounPrior;
	HashMap<Integer, HashMap<Short, Integer>> counts;
	HashMap<Integer, Integer> denomCounts;
	HashMap<Integer, HashSet<Integer>> subSpace;

	HashMap<String, Double> fracContextCount;

	LinearClassifier<String, String> classifier;

	@SuppressWarnings("unchecked")
	public ApplyEMBaselines(String folder) {
		this.folder = folder;
		try {
			ObjectInputStream modelInput = new ObjectInputStream(
					new FileInputStream("EMModel"));
			numberP = (Parameter) modelInput.readObject();
			genderP = (Parameter) modelInput.readObject();
			animacyP = (Parameter) modelInput.readObject();
			personP = (Parameter) modelInput.readObject();
			 personQP = (Parameter) modelInput.readObject();
			 personQP = (Parameter) modelInput.readObject();
			fracContextCount = (HashMap<String, Double>) modelInput
					.readObject();
			contextPrior = (HashMap<String, Double>) modelInput.readObject();

			Context.ss = (HashSet<String>) modelInput.readObject();
			Context.vs = (HashSet<String>) modelInput.readObject();
			// Context.svoStat = (SVOStat)modelInput.readObject();
			modelInput.close();

			// ObjectInputStream modelInput2 = new ObjectInputStream(
			// new FileInputStream("giga2/EMModel"));
			// numberP = (Parameter) modelInput2.readObject();
			// genderP = (Parameter) modelInput2.readObject();
			// animacyP = (Parameter) modelInput2.readObject();
			// personP = (Parameter) modelInput2.readObject();
			// personQP = (Parameter) modelInput2.readObject();
			// fracContextCount = (HashMap<String, Double>) modelInput2
			// .readObject();
			// contextPrior = (HashMap<String, Double>)
			// modelInput2.readObject();

			// modelInput2.close();
			// loadGuessProb();
			EMUtil.loadPredictNE(folder, "dev");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static ArrayList<String> goods = new ArrayList<String>();
	public static ArrayList<String> bads = new ArrayList<String>();

	double good = 0;
	double bad = 0;

	public void test() {
		String dataset = "test";
		ArrayList<String> files = Common.getLines("chinese_list_" + folder
				+ "_" + dataset);

		HashMap<String, ArrayList<EntityMention>> corefResults = new HashMap<String, ArrayList<EntityMention>>();

		// ArrayList<HashSet<String>> goldAnaphorses = new
		// ArrayList<HashSet<String>>();
		HashMap<String, HashMap<String, String>> maps = EMUtil
				.extractSysKeys("key.chinese.test.open.systemParse");
		int all2 = 0;
		for (String key : maps.keySet()) {
			all2 += maps.get(key).size();
		}
		HashMap<String, HashMap<String, HashSet<String>>> goldKeyses = EMUtil
				.extractGoldKeys();

		for (String file : files) {
			System.out.println(file);
			CoNLLDocument document = new CoNLLDocument(file
//			 .replace("auto_conll", "gold_conll")
			);
			for (int k = 0; k < document.getParts().size(); k++) {
				CoNLLPart part = document.getParts().get(k);
				part.setNameEntities(EMUtil.predictNEs.get(part.getDocument().getDocumentID() + "_"
						+ part.getPartID()));
						
				CoNLLPart goldPart = EMUtil.getGoldPart(part, dataset);

				ArrayList<Entity> goldChains = goldPart.getChains();

				HashMap<String, Integer> chainMap = EMUtil
						.formChainMap(goldChains);

				ArrayList<EntityMention> corefResult = new ArrayList<EntityMention>();
				corefResults.put(part.getPartName(), corefResult);

				ArrayList<EntityMention> goldBoundaryNPMentions = EMUtil
						.extractMention(part);
				Collections.sort(goldBoundaryNPMentions);

				ArrayList<EntityMention> candidates = new ArrayList<EntityMention>();
				for (EntityMention m : goldBoundaryNPMentions) {
					if (m.start==m.end && part.getWord(m.end).posTag.equals("PN")) {
						continue;
					}
					candidates.add(m);
				}

				Collections.sort(candidates);

				// ArrayList<Mention> anaphors = getGoldNouns(
				// part.getChains(), part);
//				 ArrayList<Mention> anaphors = getGoldAnaphorNouns(
//				 goldPart.getChains(), goldPart);

				ArrayList<EntityMention> anaphors = new ArrayList<EntityMention>();
				for (EntityMention m : goldBoundaryNPMentions) {
					if (m.start==m.end && part.getWord(m.end).posTag.equals("PN")) {
						continue;
					}
					anaphors.add(m);
				}

				findAntecedent(file, part, chainMap, anaphors,
						candidates);


				HashSet<String> goldPNs = EMUtil.getGoldPNs(goldPart);
				HashSet<String> goldNEs = EMUtil.getGoldNEs(goldPart);
				for (EntityMention m : anaphors) {
					if (goldPNs.contains(m.toName())
							|| goldNEs.contains(m.toName())
							|| goldNEs.contains(m.end + "," + m.end)
							|| m.antecedent == null) {
						continue;
					}
					for(EntityMention i : m.innerMs) {
						if (goldPNs.contains(i.toName())
								|| goldNEs.contains(i.toName())
								|| goldNEs.contains(i.end + "," + i.end)
								) {
							continue;
						}
						i.antecedent = m.antecedent;
						corefResult.add(i);
					}
					corefResult.add(m);
				}
			}
		}
		System.out.println("Good: " + good);
		System.out.println("Bad: " + bad);
		System.out.println("Precission: " + good / (good + bad) * 100);

		evaluate(corefResults, goldKeyses);
		int all = 0;
		for (String key : maps.keySet()) {
			all += maps.get(key).size();
		}
		System.out.println(all + "@@@");
		System.out.println(all2 + "@@@");
	}

	static int all = 0;

	private void findAntecedent(String file, CoNLLPart part,
			HashMap<String, Integer> chainMap, 
			ArrayList<EntityMention> anaphors, ArrayList<EntityMention> allCandidates) {
		for (EntityMention anaphor : anaphors) {
			anaphor.sentenceID = part.getWord(anaphor.start).sentence
					.getSentenceIdx();
			anaphor.s = part.getWord(anaphor.start).sentence;
			anaphor.antecedent = null;
			EntityMention antecedent = null;
			double maxP = -1;
			Collections.sort(allCandidates);

			ArrayList<EntityMention> cands = new ArrayList<EntityMention>();

			for (int h = allCandidates.size() - 1; h >= 0; h--) {
				EntityMention cand = allCandidates.get(h);
				cand.sentenceID = part.getWord(cand.start).sentence
						.getSentenceIdx();
				cand.s = part.getWord(cand.start).sentence;
				if (cand.end < anaphor.start
						&& anaphor.sentenceID - cand.sentenceID <= EMLearn.maxDistance
						&& cand.end != anaphor.end) {
					cands.add(cand);
				}
			}

			for (int i = 0; i < cands.size(); i++) {
				EntityMention cand = cands.get(i);

				if (cand.end != anaphor.end 
						&& cand.head.equals(anaphor.head)
						&& cand.gram == Grammatic.subject
//						(Context.exactMatchSieve1(cand, anaphor, part)==1
//						(Context.sieve4Rule(cand, anaphor, part)==1 || 
//						Context.headSieve1(cand, anaphor, part)==1 ||
//						Context.headSieve2(cand, anaphor, part)==1 ||
//						Context.headSieve3(cand, anaphor, part)==1 || 
//						Context.exactMatchSieve1(cand, anaphor, part)==1)
//						cand.extent.equals(anaphor.extent)
//						EMUtil.getP_C(cand, anaphor, part) != 0
//						&& cand.extent.equals(anaphor.extent)
//						&& Context.wordInclusion(cand, anaphor, part)==1
						) {
					// if (cand.extent.equals(anaphor.extent)) {
					antecedent = cand;
					System.out.println(cand.extent + " # " + anaphor.extent);
					System.out
							.println(cand.toName() + " # " + anaphor.toName());
					System.out.println("Match: " + cand.head);
					break;
				}
			}

			if (antecedent != null) {
				anaphor.antecedent = antecedent;
			}
			if (anaphor.antecedent != null
					&& anaphor.antecedent.end != -1
					&& chainMap.containsKey(anaphor.toName())
					&& chainMap.containsKey(anaphor.antecedent.toName())
					&& chainMap.get(anaphor.toName()).intValue() == chainMap
							.get(anaphor.antecedent.toName()).intValue()) {

				good++;
				String key = part.docName + ":" + part.getPartID() + ":"
						+ anaphor.start + "-" + anaphor.antecedent.start + ","
						+ anaphor.antecedent.end + ":GOOD";
				corrects.add(key);
				// if(antecedent.mType==MentionType.tmporal) {
				// System.out.println(antecedent.extent + "GOOD!");
				// }
				// System.out.println(overtPro + "  " + zero.antecedent.extent);
				// System.out.println("+++");
				// printResult(zero, zero.antecedent, part);
				// System.out.println("Predicate: " +
				// this.getPredicate(zero.V));
				// System.out.println("Object NP: " +
				// this.getObjectNP(zero));
				// System.out.println("===");
				// if (zero.antecedent.MI < 0) {
				// System.out.println("Right!!! " + good + "/" + bad);
				// System.out.println(zero.antecedent.msg);
				// if(!zero.antecedent.isFS) {
				System.out.println("Correct!!! " + good + "/" + bad);
				if (anaphor.antecedent != null) {
					System.out.println(anaphor.antecedent.extent + ":"
							+ anaphor.antecedent.NE + "#"
							+ anaphor.antecedent.number + "#"
							+ anaphor.antecedent.gender + "#"
							+ anaphor.antecedent.person + "#"
							+ anaphor.antecedent.animacy);
					System.out.println(anaphor);
				}
				// System.out.println(overtPro + "#" + bestMSg);
				// System.out.println("它: " + taMSg);
				// }
				// }

//				if (goldKeys.containsKey(anaphor.toName())
//						&& goldKeys.get(anaphor.toName()).contains(
//								antecedent.toName())) {
//
//				} else {
//					System.out.println(anaphor.extent + " " + part.getWord(anaphor.end).posTag);
//					System.out.println(antecedent.extent);
//					Common.pause("");
//				}
			} else if (anaphor.antecedent != null) {
				if (anaphor.antecedent == null) {
					String key = part.docName + ":" + part.getPartID() + ":"
							+ anaphor.start + "-NULL:BAD";
					corrects.add(key);
				} else {
					String key = part.docName + ":" + part.getPartID() + ":"
							+ anaphor.start + "-" + anaphor.antecedent.start
							+ "," + anaphor.antecedent.end + ":BAD";
					corrects.add(key);
				}
				// if(antecedent!=null && antecedent.mType==MentionType.tmporal)
				// {
				// System.out.println(antecedent.extent + "BAD !");
				// }
				bad++;
				System.out.println("Error??? " + good + "/" + bad);
				if (anaphor.antecedent != null) {
					System.out.println(anaphor.antecedent.extent + ":"
							+ anaphor.antecedent.NE + "#"
							+ anaphor.antecedent.number + "#"
							+ anaphor.antecedent.gender + "#"
							+ anaphor.antecedent.person + "#"
							+ anaphor.antecedent.animacy);
					System.out.println(anaphor);
				}
			}
			String conllPath = file;
			int aa = conllPath.indexOf(anno);
			int bb = conllPath.indexOf(".");
			String middle = conllPath.substring(aa + anno.length(), bb);
			String path = prefix + middle + suffix;
			System.out.println(path);
			System.out.println("==========");
			// System.out.println("=== " + file);

			// if (antecedent != null) {
			// CoNLLWord candWord = part.getWord(antecedent.start);
			// CoNLLWord zeroWord = part.getWord(zero.start);
			//
			// String zeroSpeaker = part.getWord(zero.start).speaker;
			// String candSpeaker = part.getWord(antecedent.start).speaker;
			// // if (!zeroSpeaker.equals(candSpeaker)) {
			// // if (antecedent.source.equals("我") &&
			// // zeroWord.toSpeaker.contains(candSpeaker)) {
			// // zero.head = "你";
			// // zero.source = "你";
			// // } else if (antecedent.source.equals("你") &&
			// // candWord.toSpeaker.contains(zeroSpeaker)) {
			// // zero.head = "我";
			// // zero.source = "我";
			// // }
			// // } else {
			// zero.extent = antecedent.extent;
			// zero.head = antecedent.head;
			// // }
			//
			// }
		}
	}

	protected void printResult(EntityMention zero, EntityMention systemAnte, CoNLLPart part) {
		StringBuilder sb = new StringBuilder();
		CoNLLSentence s = part.getWord(zero.start).sentence;
		CoNLLWord word = part.getWord(zero.start);
		for (int i = word.indexInSentence; i < s.words.size(); i++) {
			sb.append(s.words.get(i).word).append(" ");
		}
		System.out.println(sb.toString() + " # " + zero.start);
		// System.out.println(systemAnte != null ? systemAnte.extent + "#"
		// + part.getWord(systemAnte.end + 1).word : "");

		// System.out.println("========");
	}

	public void addEmptyCategoryNode(EntityMention zero) {
		MyTreeNode V = zero.V;
		MyTreeNode newNP = new MyTreeNode();
		newNP.value = "NP";
		int VIdx = V.childIndex;
		V.parent.addChild(VIdx, newNP);

		MyTreeNode empty = new MyTreeNode();
		empty.value = "-NONE-";
		newNP.addChild(empty);

		MyTreeNode child = new MyTreeNode();
		child.value = zero.extent;
		empty.addChild(child);
		child.emptyCategory = true;
		zero.NP = newNP;
	}

	static String prefix = "/shared/mlrdir1/disk1/mlr/corpora/CoNLL-2012/conll-2012-train-v0/data/files/data/chinese/annotations/";
	static String anno = "annotations/";
	static String suffix = ".coref";

	private static ArrayList<EntityMention> getGoldMentions(
			ArrayList<Entity> entities, CoNLLPart goldPart) {
		ArrayList<EntityMention> goldMentions = new ArrayList<EntityMention>();
		for (Entity e : entities) {
			goldMentions.addAll(e.mentions);
		}
		for (EntityMention m : goldMentions) {
			EMUtil.setMentionAttri(m, goldPart);
		}
		return goldMentions;
	}

	private static ArrayList<EntityMention> getGoldAnaphorNouns(
			ArrayList<Entity> entities, CoNLLPart goldPart) {
		ArrayList<EntityMention> goldAnaphors = new ArrayList<EntityMention>();
		for (Entity e : entities) {
			Collections.sort(e.mentions);
			for (int i = 1; i < e.mentions.size(); i++) {
				EntityMention m1 = e.mentions.get(i);
				String pos1 = goldPart.getWord(m1.end).posTag;
				if (pos1.equals("PN") || pos1.equals("NR") || pos1.equals("NT")) {
					continue;
				}
				HashSet<String> ants = new HashSet<String>();
				for (int j = i - 1; j >= 0; j--) {
					EntityMention m2 = e.mentions.get(j);
					String pos2 = goldPart.getWord(m2.end).posTag;
					if (!pos2.equals("PN") && m1.end != m2.end) {
						ants.add(m2.toName());
					}
				}
				if (ants.size() != 0) {
					goldAnaphors.add(m1);
				}
			}
		}
		Collections.sort(goldAnaphors);
		for (EntityMention m : goldAnaphors) {
			EMUtil.setMentionAttri(m, goldPart);
		}
		return goldAnaphors;
	}

	public static void evaluate(HashMap<String, ArrayList<EntityMention>> anaphorses,
			HashMap<String, HashMap<String, HashSet<String>>> goldKeyses) {
		double gold = 0;
		double system = 0;
		double hit = 0;

		for (String key : anaphorses.keySet()) {
			ArrayList<EntityMention> anaphors = anaphorses.get(key);
			HashMap<String, HashSet<String>> keys = goldKeyses.get(key);
			gold += keys.size();
			system += anaphors.size();
			for (EntityMention anaphor : anaphors) {
				EntityMention ant = anaphor.antecedent;
				if (keys.containsKey(anaphor.toName())
						&& keys.get(anaphor.toName()).contains(ant.toName())) {
					hit++;
				}
			}
		}

		double r = hit / gold;
		double p = hit / system;
		double f = 2 * r * p / (r + p);
		System.out.println("============");
		System.out.println("Hit: " + hit);
		System.out.println("Gold: " + gold);
		System.out.println("System: " + system);
		System.out.println("============");
		System.out.println("Recall: " + r * 100);
		System.out.println("Precision: " + p * 100);
		System.out.println("F-score: " + f * 100);
	}

	static ArrayList<String> corrects = new ArrayList<String>();

	public static void main(String args[]) {
		if (args.length != 1) {
			System.err.println("java ~ folder");
			System.exit(1);
		}
		run(args[0]);
		run("nw");
		run("mz");
		run("wb");
		run("bn");
		run("bc");
		run("tc");
	}

	public static void run(String folder) {
		EMUtil.train = false;
		ApplyEMBaselines test = new ApplyEMBaselines(folder);
		test.test();

		// System.out.println(EMUtil.missed);
		System.out.println(EMUtil.missed.size());

		Common.outputLines(goods, "goods");
		Common.outputLines(bads, "bas");

		// Common.outputHashMap(EMUtil.NEMap, "NEMAP");

		// Common.outputHashSet(Context.ss, "miniS");
		// Common.outputHashSet(Context.vs, "miniV");

		// System.out.println(Context.svoStat.unigramAll);
		// System.out.println(Context.svoStat.svoAll);

		// Common.outputLines(corrects, "EM.correct.all");
		Common.pause("!!#");
	}
}
