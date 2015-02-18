package em;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import model.Element;
import model.EntityMention;
import model.CoNLL.CoNLLDocument;
import model.CoNLL.CoNLLPart;
import model.CoNLL.CoNLLSentence;
import model.CoNLL.CoNLLWord;
import model.syntaxTree.MyTree;
import model.syntaxTree.MyTreeNode;
import util.Common;
import em.ResolveGroupEntityModel.EntryEntityModel;

public class EMLearnEntityModel {

	static Parameter numberP;
	static Parameter genderP;
	static Parameter semanticP;

	static Parameter grammaticP;

	static Parameter animacyP;

	static HashMap<String, Double> contextPrior;
	static HashMap<String, Double> contextOverall;
	static HashMap<String, Double> fracContextCount;

	static HashMap<String, Double> contextVals;

	static int maxDistance = 100000;

	static int maxDisFeaValue = 10;
	// static int contextSize = 2 * 2 * 2 * 3 * 2 * (maxDisFeaValue + 1);
	public static int qid = 0;

	static int count = 0;

	public static void init() {
		// static HashMap<Context, Double> p_context_ = new HashMap<Context,
		// Double>();
		numberP = new Parameter(1.0 / ((double) EMUtil.Number.values().length));
		genderP = new Parameter(1.0 / ((double) EMUtil.Gender.values().length));
		semanticP = new Parameter(1.0 / 109.0);
		grammaticP = new Parameter(1.0 / 4.0);

		animacyP = new Parameter(
				1.0 / ((double) EMUtil.Animacy.values().length));

		contextPrior = new HashMap<String, Double>();
		contextOverall = new HashMap<String, Double>();
		fracContextCount = new HashMap<String, Double>();
		contextVals = new HashMap<String, Double>();
		qid = 0;
		count = 0;
		ContextEntityModel.contextCache.clear();
	}

	private static ArrayList<Element> getChGoldNE(CoNLLPart part) {
		String documentID = "/users/yzcchen/chen3/CoNLL/conll-2012/v4/data/train/data/chinese/annotations/"
				+ part.docName + ".v4_gold_skel";
		// System.out.println(documentID);
		CoNLLDocument document = new CoNLLDocument(documentID);
		CoNLLPart goldPart = document.getParts().get(part.getPartID());
		// for (Element ner : goldPart.getNameEntities()) {
		// int start = ner.start;
		// int end = ner.end;
		// String ne = ner.content;
		//
		// StringBuilder sb = new StringBuilder();
		// for (int k = start; k <= end; k++) {
		// sb.append(part.getWord(k).word).append(" ");
		// }
		// // System.out.println(sb.toString() + " # " + ne);
		// // System.out.println(goldPart.);
		// }
		return goldPart.getNameEntities();
	}
	
	public static HashMap<String, Integer> origClusterMap = new HashMap<String, Integer>();
	public static HashMap<String, Integer> activeClusterMap = new HashMap<String, Integer>();

	public static ArrayList<ResolveGroupEntityModel> extractGroups(
			CoNLLPart part) {

		// CoNLLPart goldPart = EMUtil.getGoldPart(part, "train");
		CoNLLPart goldPart = part;
		HashSet<String> goldNEs = EMUtil.getGoldNEs(goldPart);
		HashSet<String> goldPNs = EMUtil.getGoldPNs(goldPart);

		ArrayList<ResolveGroupEntityModel> groups = new ArrayList<ResolveGroupEntityModel>();
		for (int i = 0; i < part.getCoNLLSentences().size(); i++) {
			CoNLLSentence s = part.getCoNLLSentences().get(i);
			s.mentions = EMUtil.extractMention(s);

			EMUtil.assignNE(s.mentions, part.getNameEntities());

			for (EntityMention em : s.mentions) {
				em.animacy = EMUtil.getAntAnimacy(em);
				em.gender = EMUtil.getAntGender(em);
				em.number = EMUtil.getAntNumber(em);
				em.semantic = EMUtil.getSemantic(em);
				origClusterMap.put(part.getPartName() + ":" + em.toName(), origClusterMap.size());
			}

			ArrayList<EntityMention> precedMs = new ArrayList<EntityMention>();

			for (int j = maxDistance; j >= 1; j--) {
				if (i - j >= 0) {
					for (EntityMention m : part.getCoNLLSentences().get(i - j).mentions) {
						if (goldPNs.contains(m.toName())) {
							continue;
						}
						precedMs.add(m);
					}
				}
			}
			Collections.sort(s.mentions);
			for (int j = 0; j < s.mentions.size(); j++) {
				EntityMention m = s.mentions.get(j);
				if (goldPNs.contains(m.toName())
						|| goldNEs.contains(m.toName())
						) {
					continue;
				}
				qid++;

				ArrayList<EntityMention> ants = new ArrayList<EntityMention>();
				ants.addAll(precedMs);

				if (j > 0) {
					for (EntityMention precedM : s.mentions.subList(0, j)) {
						if (goldPNs.contains(precedM.toName())
								|| precedM.end == m.end) {
							continue;
						}
						ants.add(precedM);
					}
				}

				EntityMention fake = new EntityMention();
				fake.isFake = true;
				// ants.add(fake);

				ResolveGroupEntityModel rg = new ResolveGroupEntityModel(m,
						part);

				Collections.sort(ants);
				Collections.reverse(ants);

				if (!RuleAnaphorNounDetector.isAnahporic(m, ants, part)) {
					continue;
				}

				// TODO
				for (int k = 0; k < ants.size(); k++) {
					EntityMention ant = ants.get(k);
					// add antecedents

					rg.cands.add(ant);
					// TODO
					count++;
				}
				groups.add(rg);
			}
		}
		return groups;
	}

	static int percent = 1;

	private static void extractCoNLL(ArrayList<ResolveGroupEntityModel> groups) {
		// CoNLLDocument d = new CoNLLDocument("train_auto_conll");

		ArrayList<String> lines = Common.getLines("chinese_list_all_train");

		lines.addAll(Common.getLines("chinese_list_all_development"));

		int docNo = 0;
		for (String line : lines) {
			if (docNo % 10 < percent) {
				CoNLLDocument d = new CoNLLDocument(line
				// .replace("gold_conll", "auto_conll")
						.replace("auto_conll", "gold_conll"));
				for (CoNLLPart part : d.getParts()) {
					// System.out.println(part.docName + " " +
					// part.getPartID());
					groups.addAll(extractGroups(part));
				}
				// System.out.println(i--);
			}
			docNo++;
		}
	}

	private static void extractGigaword(
			ArrayList<ResolveGroupEntityModel> groups) throws Exception {

		String folder = "/users/yzcchen/chen3/zeroEM/parser/";
		int j = 0;
		ArrayList<String> fns = new ArrayList<String>();
		for (File subFolder : (new File(folder)).listFiles()) {
			if (subFolder.isDirectory()
			// && !subFolder.getName().contains("cna")
			) {
				for (File file : subFolder.listFiles()) {
					if (file.getName().endsWith(".text")) {
						String filename = file.getAbsolutePath();
						fns.add(filename);
					}
				}
			}
		}

		for (String filename : fns) {
			System.out.println(filename + " " + (j++));
			System.out.println(groups.size());
			BufferedReader br = new BufferedReader(new FileReader(filename));
			CoNLLPart part = new CoNLLPart();
			int wID = 0;
			String line = "";
			while ((line = br.readLine()) != null) {
				if (line.trim().isEmpty()) {
					// part.setDocument(doc);
					// doc.getParts().add(part);
					part.wordCount = wID;
					part.processDocDiscourse();

					// for(CoNLLSentence s : part.getCoNLLSentences()) {
					// for(CoNLLWord w : s.getWords()) {
					// if(!w.speaker.equals("-") &&
					// !w.speaker.startsWith("PER")) {
					// System.out.println(w.speaker);
					// }
					// }
					// }
					groups.addAll(extractGroups(part));
					part = new CoNLLPart();
					wID = 0;
					continue;
				}
				MyTree tree = Common.constructTree(line);
				CoNLLSentence s = new CoNLLSentence();
				part.addSentence(s);
				s.setStartWordIdx(wID);
				s.syntaxTree = tree;
				ArrayList<MyTreeNode> leaves = tree.leaves;
				for (int i = 0; i < leaves.size(); i++) {
					MyTreeNode leaf = leaves.get(i);
					CoNLLWord word = new CoNLLWord();
					word.orig = leaf.value;
					word.word = leaf.value;
					word.sentence = s;
					word.indexInSentence = i;
					word.index = wID++;
					word.posTag = leaf.parent.value;

					// find speaker
					word.speaker = "-";

					s.addWord(word);
				}
				s.setEndWordIdx(wID - 1);
			}
			part.processDocDiscourse();
			groups.addAll(extractGroups(part));
			br.close();
		}
	}
	
	public static void estep(ArrayList<ResolveGroupEntityModel> groups) {
		System.out.println("estep starts:");
		contextPrior.clear();
		
		activeClusterMap.clear();
		for(String key : origClusterMap.keySet()) {
			activeClusterMap.put(key, origClusterMap.get(key));
		}
		
		long t1 = System.currentTimeMillis();
		for (ResolveGroupEntityModel group : groups) {
			double norm = 0;
			EntityMention anaphor = group.anaphor;

			group.entries.clear();

			HashMap<Integer, ArrayList<EntityMention>> previousClusters = new HashMap<Integer, ArrayList<EntityMention>>();
			for(int k=0;k<group.cands.size();k++) {
				EntityMention cand = group.cands.get(k);
				Integer clusterID = activeClusterMap.get(group.part.getPartName() + ":" + cand.toName());
				ArrayList<EntityMention> cluster = previousClusters.get(clusterID);
				if(cluster==null) {
					cluster = new ArrayList<EntityMention>();
					previousClusters.put(clusterID, cluster);
				}
				cluster.add(cand);
			}
//			System.out.println(group.cands.size() + "#" + previousClusters.size());
			for(Integer key : previousClusters.keySet()) {
				ArrayList<EntityMention> cluster = previousClusters.get(key);
				Collections.sort(cluster);
				ContextEntityModel context = ContextEntityModel.buildContext(
						cluster, anaphor, group.part, group.cands, 0);

				EntryEntityModel e = new EntryEntityModel(context, cluster);
				group.entries.add(e);

				Double d = contextPrior.get(context.toString());
				if (d == null) {
					contextPrior.put(context.toString(), 1.0);
				} else {
					contextPrior.put(context.toString(), 1.0 + d.doubleValue());
				}
			}
			Collections.sort(group.entries);
			Collections.reverse(group.entries);
			
			for (EntryEntityModel entry : group.entries) {
				EntityMention ant = entry.cluster.get(entry.cluster.size()-1);
				ContextEntityModel context = entry.context;

				double p_number = numberP.getVal(ant.number.name(),
						anaphor.number.name());
				double p_gender = genderP.getVal(ant.gender.name(),
						anaphor.gender.name());
				double p_animacy = animacyP.getVal(ant.animacy.name(),
						anaphor.animacy.name());
				double p_grammatic = grammaticP.getVal(ant.gram.name(),
						anaphor.gram.name());

				double p_semetic = semanticP.getVal(ant.semantic,
						anaphor.semantic);

				double p_context = .5;
				Double d = contextVals.get(context.toString());
				if (contextVals.containsKey(context.toString())) {
					p_context = d.doubleValue();
				} else {
					p_context = .5;
				}

				entry.p = p_context;
				entry.p *= 1 * p_number * p_gender * p_animacy * p_semetic
				// * p_grammatic
				;
				norm += entry.p;
			}

			double maxP = -1;
			int maxIdx = -1;
			for (int i=0;i<group.entries.size();i++) {
				EntryEntityModel entry = group.entries.get(i);
				entry.p = entry.p / norm;
				if(entry.p>maxP) {
					maxIdx = i;
					maxP = entry.p;
				}
			}
			
			// reorder activeClusterMap
			if(maxIdx!=-1) {
				EntityMention antecedent = group.entries.get(maxIdx).cluster.get(0);
				int newClusterId = activeClusterMap.get(group.part.getPartName() + ":" + antecedent.toName());
				activeClusterMap.put(group.part.getPartName() + ":" + anaphor.toName(), newClusterId);
			}
		}
		System.out.println(System.currentTimeMillis() - t1);
	}

	public static void mstep(ArrayList<ResolveGroupEntityModel> groups) {
		System.out.println("mstep starts:");
		long t1 = System.currentTimeMillis();
		genderP.resetCounts();
		numberP.resetCounts();
		animacyP.resetCounts();
		contextVals.clear();
		semanticP.resetCounts();
		grammaticP.resetCounts();
		fracContextCount.clear();
		for (ResolveGroupEntityModel group : groups) {

			EntityMention anaphor = group.anaphor;

			for (EntryEntityModel entry : group.entries) {
				EntityMention ant = entry.cluster.get(0);
				double p = entry.p;
				ContextEntityModel context = entry.context;

				numberP.addFracCount(ant.number.name(), anaphor.number.name(),
						p);
				genderP.addFracCount(ant.gender.name(), anaphor.gender.name(),
						p);
				animacyP.addFracCount(ant.animacy.name(),
						anaphor.animacy.name(), p);

				semanticP.addFracCount(ant.semantic, anaphor.semantic, p);

				grammaticP
						.addFracCount(ant.gram.name(), anaphor.gram.name(), p);

				Double d = fracContextCount.get(context.toString());
				if (d == null) {
					fracContextCount.put(context.toString(), p);
				} else {
					fracContextCount.put(context.toString(), d.doubleValue()
							+ p);
				}
			}
		}
		genderP.setVals();
		numberP.setVals();
		animacyP.setVals();
		semanticP.setVals();
		grammaticP.setVals();
		for (String key : fracContextCount.keySet()) {
			double p_context = (EMUtil.alpha + fracContextCount.get(key))
					/ (2.0 * EMUtil.alpha + contextPrior.get(key));
			contextVals.put(key, p_context);
		}
		System.out.println(System.currentTimeMillis() - t1);
	}

	public static void main(String args[]) throws Exception {
		run();
		// System.out.println(match/XallX);
		// Common.outputLines(svmRanks, "svmRank.train");
		// System.out.println("Qid: " + qid);
	}

	private static void run() throws IOException, FileNotFoundException {
		init();

		EMUtil.train = true;

		ArrayList<ResolveGroupEntityModel> groups = new ArrayList<ResolveGroupEntityModel>();

		extractCoNLL(groups);
		// extractGigaword(groups);
		// Common.pause("count:  " + count);
		Common.pause(groups.size());

		int it = 0;
		while (it < 20) {
			System.out.println("Iteration: " + it);
			estep(groups);
			mstep(groups);
			it++;
		}

		numberP.printParameter("numberP");
		genderP.printParameter("genderP");
		animacyP.printParameter("animacyP");
		semanticP.printParameter("semanticP");
		grammaticP.printParameter("grammaticP");

		ObjectOutputStream modelOut = new ObjectOutputStream(
				new FileOutputStream("EMModel"));
		modelOut.writeObject(numberP);
		modelOut.writeObject(genderP);
		modelOut.writeObject(animacyP);
		modelOut.writeObject(semanticP);
		modelOut.writeObject(grammaticP);

		modelOut.writeObject(fracContextCount);
		modelOut.writeObject(contextPrior);

		modelOut.writeObject(ContextEntityModel.ss);
		modelOut.writeObject(ContextEntityModel.vs);
		// modelOut.writeObject(Context.svoStat);

		modelOut.close();

		Common.outputHashMap(contextVals, "contextVals");
		Common.outputHashMap(fracContextCount, "fracContextCount");
		Common.outputHashMap(contextPrior, "contextPrior");
		// ObjectOutputStream svoStat = new ObjectOutputStream(new
		// FileOutputStream(
		// "/dev/shm/svoStat"));
		// svoStat.writeObject(Context.svoStat);
		// svoStat.close();

		// System.out.println(EMUtil.missed);
		System.out.println(EMUtil.missed.size());

		ApplyEMEntityModel.run("all");

		ApplyEMEntityModel.run("nw");
		ApplyEMEntityModel.run("mz");
		ApplyEMEntityModel.run("wb");
		ApplyEMEntityModel.run("bn");
		ApplyEMEntityModel.run("bc");
		ApplyEMEntityModel.run("tc");
	}

}
