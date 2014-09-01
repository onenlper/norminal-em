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
	// static Parameter personP;
	// static Parameter personQP;
	static Parameter animacyP;

	static HashMap<String, Double> contextPrior;
	static HashMap<String, Double> contextOverall;
	static HashMap<String, Double> fracContextCount;

	static int maxDistance = 100000;

	static int maxDisFeaValue = 10;

	static int contextSize = 2 * 2 * 2 * 3 * 2 * (maxDisFeaValue + 1);
	public static int qid = 0;

	static int count = 0;

	public static void init() {
		// static HashMap<Context, Double> p_context_ = new HashMap<Context,
		// Double>();
		numberP = new Parameter(1.0 / ((double) EMUtil.Number.values().length));
		genderP = new Parameter(1.0 / ((double) EMUtil.Gender.values().length));
		// personP = new Parameter(1.0 / ((double)
		// EMUtil.Person.values().length));
		// personQP = new Parameter(1.0 / ((double)
		// EMUtil.Person.values().length));
		animacyP = new Parameter(
				1.0 / ((double) EMUtil.Animacy.values().length));

		contextPrior = new HashMap<String, Double>();
		contextOverall = new HashMap<String, Double>();
		fracContextCount = new HashMap<String, Double>();
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
		// ArrayList<Element> goldNE = getChGoldNE(part);

		// HashMap<String, Integer> chainMap = EMUtil.formChainMap(part
		// .getChains());
		// System.out.println(chainMap.size());
		// System.out.println(part.getChains().size());

		ArrayList<ResolveGroup> groups = new ArrayList<ResolveGroup>();
		for (int i = 0; i < part.getCoNLLSentences().size(); i++) {
			CoNLLSentence s = part.getCoNLLSentences().get(i);
			s.mentions = EMUtil.extractMention(s);

			EMUtil.assignNE(s.mentions, part.getNameEntities());

			ArrayList<Mention> precedMs = new ArrayList<Mention>();

			for (int j = maxDistance; j >= 0; j--) {
				if (i - j >= 0) {
					precedMs.addAll(part.getCoNLLSentences().get(i - j).mentions);
				}
			}

			for (int j = 0; j < s.mentions.size(); j++) {
				Mention m = s.mentions.get(j);
				String headPOS = part.getWord(m.end).posTag;

				if (headPOS.equals("NT") || headPOS.equals("PN")
						|| headPOS.equals("NR")) {
					continue;
				}
				qid++;
				ArrayList<Mention> ants = new ArrayList<Mention>();
				ants.addAll(precedMs);
				if (j > 0) {
					ants.addAll(s.mentions.subList(0, j - 1));
				}
				ResolveGroup rg = new ResolveGroup(m);

				Collections.sort(ants);
				Collections.reverse(ants);
				// TODO
				for (int k = 0; k < ants.size(); k++) {
					Mention ant = ants.get(k);
					// add antecedents

					Context context = Context.buildContext(ant, m, part);

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
					// boolean coref = chainMap.containsKey(m.toName())
					// && chainMap.containsKey(ant.toName())
					// && chainMap.get(m.toName()).intValue() == chainMap
					// .get(ant.toName()).intValue();
				}
				groups.add(rg);
			}
		}
		return groups;
	}

	private static void extractCoNLL(ArrayList<ResolveGroup> groups) {
		// CoNLLDocument d = new CoNLLDocument("train_auto_conll");
		CoNLLDocument d = new CoNLLDocument("train_gold_conll");
		ArrayList<CoNLLPart> parts = new ArrayList<CoNLLPart>();
		parts.addAll(d.getParts());
		int i = parts.size();

		int docNo = 0;
		String previousDoc = "";

		for (CoNLLPart part : parts) {
			// System.out.println(part.docName + " " + part.getPartID());
			if (!part.docName.equals(previousDoc)) {
				docNo++;
				previousDoc = part.docName;
			}
			if (docNo % 10 < percent) {
				groups.addAll(extractGroups(part));
			}
			// System.out.println(i--);
		}
	}

	static int percent = 10;

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
		for (ResolveGroup group : groups) {
			double norm = 0;
			for (Entry entry : group.entries) {
				String ant = entry.head;
				Context context = entry.context;

				double p_number = numberP.getVal(entry.number.name(),
						group.number.name());
				double p_gender = genderP.getVal(entry.gender.name(),
						group.gender.name());
				double p_animacy = animacyP.getVal(entry.animacy.name(),
						group.animacy.name());

				// double p_number = numberP.getVal(entry.head, EMUtil
				// .getNumber(pronoun).name());
				// double p_gender = genderP.getVal(entry.head, EMUtil
				// .getGender(pronoun).name());
				// double p_animacy = animacyP.getVal(entry.head, EMUtil
				// .getAnimacy(pronoun).name());

				double p_context = 1;

				if (fracContextCount.containsKey(context.toString())) {
					p_context = (EMUtil.alpha + fracContextCount.get(context
							.toString()))
							/ (EMLearn.contextSize * EMUtil.alpha + contextPrior
									.get(context.toString()));
				} else {
					p_context = 1.0 / EMLearn.contextSize;
				}

				// System.out.println(p_context);

				entry.p = 1 * p_number * p_gender * p_animacy * p_context * 1;
				norm += entry.p;
			}

			for (Entry entry : group.entries) {
				entry.p = entry.p / norm;
			}
		}
	}

	public static void mstep(ArrayList<ResolveGroup> groups) {
		genderP.resetCounts();
		numberP.resetCounts();
		animacyP.resetCounts();
		// personP.resetCounts();
		// personQP.resetCounts();
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

				// numberP.addFracCount(entry.head,
				// EMUtil.getNumber(pronoun).name(), p);
				// genderP.addFracCount(entry.head,
				// EMUtil.getGender(pronoun).name(), p);
				// animacyP.addFracCount(entry.head,
				// EMUtil.getAnimacy(pronoun).name(), p);

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
		// personP.setVals();
		// personQP.setVals();
	}

	public static void main(String args[]) throws Exception {
		// percent = 0;
		// while(percent<=9) {
		// run();
		// percent++;
		// }
		percent = 10;
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

		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(
				"resolveGroups"));
		out.writeObject(groups);
		out.close();

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
		// personP.printParameter("personP");
		// personQP.printParameter("personQP");

		ObjectOutputStream modelOut = new ObjectOutputStream(
				new FileOutputStream("EMModel"));
		modelOut.writeObject(numberP);
		modelOut.writeObject(genderP);
		modelOut.writeObject(animacyP);
		// modelOut.writeObject(personP);
		// modelOut.writeObject(personQP);

		modelOut.writeObject(fracContextCount);
		modelOut.writeObject(contextPrior);

		modelOut.writeObject(Context.ss);
		modelOut.writeObject(Context.vs);
		// modelOut.writeObject(Context.svoStat);

		modelOut.close();

		// ObjectOutputStream svoStat = new ObjectOutputStream(new
		// FileOutputStream(
		// "/dev/shm/svoStat"));
		// svoStat.writeObject(Context.svoStat);
		// svoStat.close();

		// System.out.println(EMUtil.missed);
		System.out.println(EMUtil.missed.size());

		ApplyEM.run("all");

		ApplyEM.run("nw");
		ApplyEM.run("mz");
		ApplyEM.run("wb");
		ApplyEM.run("bn");
		ApplyEM.run("bc");
		ApplyEM.run("tc");
	}

}
