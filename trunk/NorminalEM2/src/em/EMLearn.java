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
import model.Mention;
import model.CoNLL.CoNLLDocument;
import model.CoNLL.CoNLLPart;
import model.CoNLL.CoNLLSentence;
import model.CoNLL.CoNLLWord;
import model.syntaxTree.MyTree;
import model.syntaxTree.MyTreeNode;
import util.Common;
import em.ResolveGroup.Entry;

public class EMLearn {

	// static HashMap<Context, Double> p_context_ = new HashMap<Context,
	// Double>();

	static Parameter numberP;
	static Parameter genderP;
	static Parameter semanticP;

	static Parameter grammaticP;

	static Parameter animacyP;

	static double word2vecSimi = .9;

	static HashMap<String, Double> contextPrior;
	static HashMap<String, Double> contextOverall;
	static HashMap<String, Double> fracContextCount;

	static HashMap<String, Double> contextVals;

	static int maxDistance = 50;

	static int maxDisFeaValue = 10;
	// static int contextSize = 2 * 2 * 2 * 3 * 2 * (maxDisFeaValue + 1);
	public static int qid = 0;

	static int count = 0;

	public static void init() {
		// static HashMap<Context, Double> p_context_ = new HashMap<Context,
		// Double>();
		numberP = new Parameter(1.0 / ((double) EMUtil.Number.values().length));
		genderP = new Parameter(1.0 / ((double) EMUtil.Gender.values().length));
		semanticP = new Parameter(1.0/25318.0);
//		semanticP = new Parameter(1.0/5254.0);
		
		grammaticP = new Parameter(1.0 / 4.0);

		animacyP = new Parameter(
				1.0 / ((double) EMUtil.Animacy.values().length));

		contextPrior = new HashMap<String, Double>();
		contextOverall = new HashMap<String, Double>();
		fracContextCount = new HashMap<String, Double>();
		contextVals = new HashMap<String, Double>();
		qid = 0;
		count = 0;
		Context.contextCache.clear();
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

	public static ArrayList<ResolveGroup> extractGroups(CoNLLPart part) {

		// CoNLLPart goldPart = EMUtil.getGoldPart(part, "train");
		CoNLLPart goldPart = part;
		HashSet<String> goldNEs = EMUtil.getGoldNEs(goldPart);
		HashSet<String> goldPNs = EMUtil.getGoldPNs(goldPart);

		ArrayList<ResolveGroup> groups = new ArrayList<ResolveGroup>();
		for (int i = 0; i < part.getCoNLLSentences().size(); i++) {
			CoNLLSentence s = part.getCoNLLSentences().get(i);
			s.mentions = EMUtil.extractMention(s);

			EMUtil.assignNE(s.mentions, part.getNameEntities());

			ArrayList<Mention> precedMs = new ArrayList<Mention>();

			for (int j = maxDistance; j >= 1; j--) {
				if (i - j >= 0) {
					for (Mention m : part.getCoNLLSentences().get(i - j).mentions) {
						if (goldPNs.contains(m.toName())) {
							continue;
						}
						precedMs.add(m);
					}
				}
			}
			Collections.sort(s.mentions);
			for (int j = 0; j < s.mentions.size(); j++) {
				Mention m = s.mentions.get(j);
				if (goldPNs.contains(m.toName())
						|| goldNEs.contains(m.toName())) {
					continue;
				}
				qid++;

				ArrayList<Mention> ants = new ArrayList<Mention>();
				ants.addAll(precedMs);

				if (j > 0) {
					for (Mention precedM : s.mentions.subList(0, j)) {
						if (goldPNs.contains(precedM.toName())
								|| precedM.end == m.end) {
							continue;
						}
						ants.add(precedM);
					}
				}

				ResolveGroup rg = new ResolveGroup(m);

				Collections.sort(ants);
				Collections.reverse(ants);

				// TODO
				for (int k = 0; k < ants.size(); k++) {
					Mention ant = ants.get(k);
					// add antecedents

					Context context = Context.buildContext(ant, m, part, ants,
							k);
					double simi = Context.getSimi(ant.head, m.head);
					

					Entry entry = new Entry(ant, context);
					rg.entries.add(entry);
					count++;

					Double d = contextPrior.get(context.toString());
					if (d == null) {
						contextPrior.put(context.toString(), 1.0);
					} else {
						contextPrior.put(context.toString(),
								1.0 + d.doubleValue());
					}
					entry.p_c = EMUtil.getP_C(ant, m, part);
				}
				groups.add(rg);
			}
		}
		return groups;
	}

	static int percent = 10;

	private static void extractCoNLL(ArrayList<ResolveGroup> groups) {
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

	private static void extractGigaword(ArrayList<ResolveGroup> groups)
			throws Exception {

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

	public static void estep(ArrayList<ResolveGroup> groups) {
		System.out.println("estep starts:");
		long t1 = System.currentTimeMillis();
		for (ResolveGroup group : groups) {
			double norm = 0;
			for (Entry entry : group.entries) {
				Context context = entry.context;

				double p_number = numberP.getVal(entry.number.name(),
						group.number.name());
				double p_gender = genderP.getVal(entry.gender.name(),
						group.gender.name());
				double p_animacy = animacyP.getVal(entry.animacy.name(),
						group.animacy.name());
				double p_grammatic = grammaticP.getVal(entry.gram.name(),
						group.gram.name());

				double p_semetic = semanticP.getVal(entry.sem, group.sem);

				double p_context = .5;
				Double d = contextVals.get(context.toString());
				if (contextVals.containsKey(context.toString())) {
					p_context = d.doubleValue();
				} else {
					p_context = .5;
					// if(context.toString().startsWith("0")) {
					// p_context = .1;
					// }
				}

				entry.p = p_context * entry.p_c;
				entry.p *= 1 
//						* p_number 
//						* p_gender 
//						* p_animacy 
						* p_semetic
				// * p_grammatic
				;

				norm += entry.p;
			}

			double max = 0;
			if (norm != 0) {
				for (Entry entry : group.entries) {
					entry.p = entry.p / norm;
					if (entry.p > max) {
						max = entry.p;
					}
				}
			}
		}
		System.out.println(System.currentTimeMillis() - t1);
	}

	public static void mstep(ArrayList<ResolveGroup> groups) {
		System.out.println("mstep starts:");
		long t1 = System.currentTimeMillis();
		genderP.resetCounts();
		numberP.resetCounts();
		animacyP.resetCounts();
		contextVals.clear();
		semanticP.resetCounts();
		grammaticP.resetCounts();
		fracContextCount.clear();
		for (ResolveGroup group : groups) {
			for (Entry entry : group.entries) {
				double p = entry.p;
				Context context = entry.context;

				numberP.addFracCount(entry.number.name(), group.number.name(),
						p);
				genderP.addFracCount(entry.gender.name(), group.gender.name(),
						p);
				animacyP.addFracCount(entry.animacy.name(),
						group.animacy.name(), p);

				semanticP.addFracCount(entry.sem, group.sem, p);

				grammaticP
						.addFracCount(entry.gram.name(), group.gram.name(), p);

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

		ArrayList<ResolveGroup> groups = new ArrayList<ResolveGroup>();

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

		modelOut.writeObject(Context.ss);
		modelOut.writeObject(Context.vs);
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

		ApplyEM.run("all");

		// ApplyEM.run("nw");
		// ApplyEM.run("mz");
		// ApplyEM.run("wb");
		// ApplyEM.run("bn");
		// ApplyEM.run("bc");
		// ApplyEM.run("tc");
	}

}
