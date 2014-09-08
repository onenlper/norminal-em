package em;

import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import model.Mention;
import model.CoNLL.CoNLLPart;
import model.syntaxTree.MyTreeNode;
import util.Common;

public class ContextEntityModel implements Serializable {

	/**
         * 
         */
	private static final long serialVersionUID = 1L;
	// short antSenPos; // 3 values
	// short antHeadPos; //
	// short antGram; //
	// short proPos; //
	// short antType;// pronoun, proper, common

	String feaL;

	public static HashMap<String, ContextEntityModel> contextCache = new HashMap<String, ContextEntityModel>();

	public static ContextEntityModel getContext(short[] feas) {
		// long feaL = 0;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < feas.length; i++) {
			// if (feas[i] >= 10) {
			// Common.bangErrorPOS("Can't larger than 10:" + feas[i]
			// + "  Fea:" + i);
			// }
			// feaL += Math.pow(10, i) * feas[i];
			sb.append(feas[i]).append("#");
		}
		if (contextCache.containsKey(sb.toString())) {
			return contextCache.get(sb.toString());
		} else {
			ContextEntityModel c = new ContextEntityModel(sb.toString());
			contextCache.put(sb.toString(), c);
			return c;
		}
	}

	private ContextEntityModel(String feaL) {
		this.feaL = feaL;
	}

	public int hashCode() {
		return this.toString().hashCode();
	}

	public boolean equals(Object obj) {
		ContextEntityModel c2 = (ContextEntityModel) obj;
		return (this.feaL == c2.feaL);
	}

	public String toString() {
		return this.feaL;
	}

	public static SVOStat svoStat;

	static short[] feas = new short[18];

	public static ContextEntityModel buildContext(Mention ant, Mention anaphor,
			CoNLLPart part, ArrayList<Mention> allCands, int mentionDis) {

		// exact match
		int id = 0;
		short[] feas = new short[10];

		// feas[id++] = getIsFake(ant, anaphor, part);
		// feas[id++] = getHasSameHead(allCands, anaphor, part);
		feas[id++] = getDistance(ant, anaphor, part); //
		feas[id++] = isExactMatch(ant, anaphor, part); // 2
		feas[id++] = headMatch(ant, anaphor, part); // 2
		feas[id++] = haveIncompatibleModify(ant, anaphor, part); // 3
		feas[id++] = wordInclusion(ant, anaphor, part);
		
		// feas[id++] = isSameGrammatic(ant, anaphor, part);
		// feas[id++] = isIWithI(ant, anaphor, part); // 2
		// feas[id++] = isSamePredicate(ant, anaphor, part);
		// feas[id++] = getMentionDiss(mentionDis);
		// feas[id++] = modifierMatch(ant, anaphor, part);
		// feas[id++] = isSemanticSame(ant, anaphor, part);
		return getContext(feas);
	}

	public static short wordInclusion(Mention ant, Mention anaphor,
			CoNLLPart part) {
		List<String> removeW = Arrays.asList(new String[] { "这个", "这", "那个",
				"那", "自己", "的", "该", "公司", "这些", "那些", "'s" });
		ArrayList<String> removeWords = new ArrayList<String>();
		removeWords.addAll(removeW);
		HashSet<String> mentionClusterStrs = new HashSet<String>();
		for (int i = anaphor.start; i <= anaphor.end; i++) {
			mentionClusterStrs.add(part.getWord(i).orig.toLowerCase());
			if (part.getWord(i).posTag.equalsIgnoreCase("DT")
					&& i < anaphor.end
					&& part.getWord(i + 1).posTag.equalsIgnoreCase("M")) {
				removeWords.add(part.getWord(i).word);
				removeWords.add(part.getWord(i + 1).word);
			}
		}
		mentionClusterStrs.removeAll(removeWords);

		mentionClusterStrs.remove(anaphor.head.toLowerCase());
		HashSet<String> candidateClusterStrs = new HashSet<String>();
		for (int i = ant.start; i <= ant.end; i++) {
			candidateClusterStrs.add(part.getWord(i).orig.toLowerCase());
		}
		candidateClusterStrs.remove(ant.head.toLowerCase());
		if (candidateClusterStrs.containsAll(mentionClusterStrs))
			return 1;
		else
			return 0;
	}

	private static short isSemanticSame(Mention ana, Mention anaphor,
			CoNLLPart part) {
		String s1 = EMUtil.getSemantic(ana);
		String s2 = EMUtil.getSemantic(anaphor);
		if (s1.equals(s2) && !"unknown".startsWith(s1)) {
			return 1;
		} else {
			return 0;
		}
	}

	private static short modifierMatch(Mention ana, Mention anaphor,
			CoNLLPart part) {
		HashSet<String> m1s = new HashSet<String>(ana.modifyList);
		HashSet<String> m2s = new HashSet<String>(anaphor.modifyList);

		m1s.removeAll(anaphor.modifyList);
		m2s.remove(ana.modifyList);

		if (m1s.size() != 0) {
			return 1;
		} else if (m2s.size() != 0) {
			return 2;
		}
		return 0;
	}

	private static short getMentionDiss(int diss) {
		return (short) (Math.log(diss) / Math.log(4));
	}

	private static short isSamePredicate(Mention ant, Mention anaphor,
			CoNLLPart part) {
		if (ant.gram == anaphor.gram) {
			if (ant.V != null && anaphor.V != null) {
				String v1 = EMUtil.getPredicateNode(ant.V);
				String v2 = EMUtil.getPredicateNode(anaphor.V);
				if (v1 != null && v2 != null && v1.equals(v2)) {
					// System.out.println(v1);
					// Common.bangErrorPOS(v1);
					return 2;
				}
			}
			return 1;
		} else {
			return 0;
		}
	}

	private static short isSameGrammatic(Mention ant, Mention anaphor,
			CoNLLPart part) {
		if (ant.gram == anaphor.gram) {
			return 1;
		} else {
			return 0;
		}
	}

	private static short getHasSameHead(ArrayList<Mention> cands,
			Mention anaphor, CoNLLPart part) {
		// StringBuilder sb = new StringBuilder();
		// sb.append(anaphor.extent).append(":");
		// for(Mention c : cands) {
		// sb.append(c.extent).append("#");
		// }
		// System.out.println(sb.toString().trim());
		// System.out.println("=================");
		// System.out.println(part.getPartName());

		boolean hasSameHead = false;
		for (Mention m : cands) {
			if (m.head.equals(anaphor.head)
					&& m.extent.contains(anaphor.extent)) {
				hasSameHead = true;
			}
		}
		if (hasSameHead) {
			return 1;
		} else {
			return 0;
		}
	}

	private static short getIsFake(Mention ant, Mention anaphor, CoNLLPart part) {
		if (ant.isFake) {
			return 0;
		} else {
			return 1;
		}
	}

	private static short getDistance(Mention ant, Mention anaphor,
			CoNLLPart part) {
		short diss = 0;
		if (ant.isFake) {
			diss = (short) ((part.getWord(anaphor.end).sentence
					.getSentenceIdx() + 1));
		} else {
			diss = (short) (part.getWord(anaphor.end).sentence.getSentenceIdx() - part
					.getWord(ant.end).sentence.getSentenceIdx());
		}
		// if(diss>10) {
		// return 10;
		// } else {
		// return (short) diss;
		// }
		return (short) (Math.log(diss) / Math.log(2));
	}

	private static short isExactMatch(Mention ant, Mention anaphor,
			CoNLLPart part) {
		if (ant.extent.equalsIgnoreCase(anaphor.extent)) {
			return 1;
		} else {
			return 0;
		}
	}

	public static short isAbb(Mention ant, Mention anaphor, CoNLLPart part) {
		if (Common.isAbbreviation(ant.extent, anaphor.extent)) {
			return 1;
		}
		return 0;
	}

	public static short headMatch(Mention ant, Mention anaphor, CoNLLPart part) {
		if (ant.head.equalsIgnoreCase(anaphor.head)) {
			return 1;
		} else {
			return 0;
		}
	}

	public static short isIWithI(Mention ant, Mention anaphor, CoNLLPart part) {
		if (ant.end <= anaphor.start) {
			return 0;
		}
		return 1;
	}

	public static short haveIncompatibleModify(Mention ant, Mention anaphor,
			CoNLLPart part) {
		// if (anaphor.isFake || !ant.head.equalsIgnoreCase(anaphor.head)) {
		// return 0;
		// } else if (ant.head.equals(anaphor.head)) {
		// if (ant.extent.contains(anaphor.extent)) {
		// return 1;
		// } else {
		// return 2;
		// }
		// }

		boolean thisHasExtra = false;
		Set<String> thisWordSet = new HashSet<String>();
		Set<String> antWordSet = new HashSet<String>();
		Set<String> locationModifier = new HashSet<String>(Arrays.asList("东",
				"南", "西", "北", "中", "东面", "南面", "西面", "北面", "中部", "东北", "西部",
				"南部", "下", "上", "新", "旧", "前"));
		for (int i = anaphor.start; i <= anaphor.end; i++) {
			String w1 = part.getWord(i).orig.toLowerCase();
			String pos1 = part.getWord(i).posTag;
			if ((pos1.startsWith("PU") || w1.equalsIgnoreCase(anaphor.head))) {
				continue;
			}
			thisWordSet.add(w1);
		}
		for (int j = ant.start; j <= ant.end; j++) {
			String w2 = part.getWord(j).orig.toLowerCase();
			antWordSet.add(w2);
		}
		for (String w : thisWordSet) {
			if (!antWordSet.contains(w)) {
				thisHasExtra = true;
			}
		}
		boolean hasLocationModifier = false;
		for (String l : locationModifier) {
			if (antWordSet.contains(l) && !thisWordSet.contains(l)) {
				hasLocationModifier = true;
			}
		}
		if (thisHasExtra || hasLocationModifier) {
			return 1;
		}
		return 2;
	}

	private static void moreFea(short antPos, short proPos, short antSynactic,
			short antType, short nearest, short NPClause, short VPClause,
			short[] feas) {
		// feas[1] = nearest;
		feas[2] = antPos;
		feas[3] = antSynactic;
		feas[4] = proPos;
		feas[5] = antType;
		// feas[11] = NPClause;
		// feas[12] = VPClause;
	}

	public static double voP = 0;
	public static double svoP = 0;
	public static double MI = 0;

	public static String message;

	public static HashSet<String> ss = new HashSet<String>();
	public static HashSet<String> vs = new HashSet<String>();

	public static double calMI2(Mention ant, Mention pronoun) {
		if (svoStat == null) {
			svoStat = new SVOStat();
			svoStat.loadMIInfo();
		}
		String v = EMUtil.getFirstVerb(pronoun.V);
		String o = EMUtil.getObjectNP(pronoun.V);

		String s = EMUtil.getAntAnimacy(ant).name();
		double subjC = getValue(svoStat.unigrams, s);
		// System.out.println(subjC + "##" + s + "###" +
		// svoStat.unigrams.size());

		double subjP = (subjC + 1)
				/ (svoStat.unigramAll + svoStat.unigrams.size());

		// if (o != null && svoStat.voCounts.containsKey(v + " " + o)) {
		// double voC = getValue(svoStat.voCounts, v + " " + o);
		// voP = (voC) / (svoStat.svoAll);
		//
		// double svoC = getValue(svoStat.svoCounts, s + " " + v + " " + o);
		// svoP = (svoC) / (svoStat.svoAll);
		//
		// } else {
		if (!svoStat.vCounts.containsKey(v) || svoStat.vCounts.get(v) < 1000) {
			return 1;
		}

		double voC = getValue(svoStat.vCounts, v);
		voP = (voC) / (svoStat.svoAll);

		double svoC = getValue(svoStat.svCounts, s + " " + v);
		svoP = (svoC) / (svoStat.svoAll);
		// }

		// }

		double MI = Math.log(svoP / (voP * subjP));
		// System.out.println(subjP + " " + voP + " " + svoP);
		// System.out.println(MI + s + " " + v + " " + o);
		// System.out.println("======");

		message = subjP + " " + voP + " " + svoP + '\n' + MI + s + " " + v
				+ " " + o + '\n' + "======";
		return MI;
	}

	public static double calMI(Mention ant, Mention pronoun) {
		if (true)
			return 1;
		if (svoStat == null) {
			long start = System.currentTimeMillis();
			ObjectInputStream modelInput;
			// try {
			// modelInput = new ObjectInputStream(new FileInputStream(
			// "/dev/shm/svoStat"));
			// svoStat = (SVOStat) modelInput.readObject();
			svoStat = new SVOStat();
			svoStat.loadMIInfo();
			// } catch (FileNotFoundException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// } catch (IOException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// } catch (ClassNotFoundException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			// System.out.println(System.currentTimeMillis() - start);
		}
		String s = ant.head;
		String pos = ant.s.getWord(ant.headInS).posTag;
		String v = EMUtil.getFirstVerb(pronoun.V);
		String o = EMUtil.getObjectNP(pronoun.V);

		// System.out.println(s + " " + v + " " + o);
		String NE = ant.NE;
		if (ant.NE.equals("OTHER") && EMUtil.NEMap != null
				&& EMUtil.NEMap.containsKey(ant.head)) {
			NE = EMUtil.NEMap.get(ant.head);
		}

		if (NE.equals("PERSON")) {
			s = "他";
			pos = "PN";
		} else if (NE.equals("LOC") || NE.equals("GPE") || NE.equals("ORG")) {
			s = "它";
			pos = "PN";
		}
		// else if(NE.equals("ORG")) {
		// s = "公司";
		// pos = "NN";
		// }

		if (!svoStat.unigrams.containsKey(s + " " + pos)
				|| svoStat.unigrams.get(s + " " + pos) < 15000) {
			return 1;
		}

		if (EMUtil.train) {
			ss.add(s);
			vs.add(v);
		} else if (!ss.contains(s) || vs.contains(v)) {
			// return 1;
		}

		double subjC = getValue(svoStat.unigrams, s + " " + pos);
		double subjP = (subjC + 1)
				/ (svoStat.unigramAll + svoStat.unigrams.size());

		// if (o != null && svoStat.voCounts.containsKey(v + " " + o)) {
		// double voC = getValue(svoStat.voCounts, v + " " + o);
		// voP = (voC) / (svoStat.svoAll);
		//
		// double svoC = getValue(svoStat.svoCounts, s + " " + v + " " + o);
		// svoP = (svoC) / (svoStat.svoAll);
		// } else {
		if (!svoStat.vCounts.containsKey(v) || svoStat.vCounts.get(v) < 1000) {
			return 1;
		}

		double voC = getValue(svoStat.vCounts, v);
		voP = (voC) / (svoStat.svoAll);

		double svoC = getValue(svoStat.svCounts, s + " " + v);
		svoP = (svoC) / (svoStat.svoAll);
		// }

		double MI = Math.log(svoP / (voP * subjP));
		// System.out.println(subjP + " " + voP + " " + svoP);
		// System.out.println(MI + s + " " + v + " " + o);
		// System.out.println("======");

		message = subjP + " " + voP + " " + svoP + '\n' + MI + s + " " + NE
				+ " " + v + " " + o + '\n' + "======";
		return MI;
	}

	public static double getValue(HashMap<String, Integer> map, String key) {
		if (map.containsKey(key)) {
			return map.get(key);
		} else {
			return 0.00000001;
		}
	}

	public static short getClauseType(MyTreeNode node, MyTreeNode root) {
		int IPCounts = node.getXAncestors("IP").size();
		if (IPCounts > 1) {
			// subordinate clause
			return 2;
		} else {
			int totalIPCounts = 0;
			ArrayList<MyTreeNode> frontie = new ArrayList<MyTreeNode>();
			frontie.add(root);
			while (frontie.size() > 0) {
				MyTreeNode tn = frontie.remove(0);
				if (tn.value.toLowerCase().startsWith("ip")) {
					totalIPCounts++;
				}
				frontie.addAll(tn.children);
			}
			if (totalIPCounts > 1) {
				// matrix clause
				return 1;
			} else {
				// independent clause
				return 0;
			}
		}
	}
}
