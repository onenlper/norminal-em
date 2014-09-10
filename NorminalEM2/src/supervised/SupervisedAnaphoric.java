package supervised;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import model.Mention;
import model.CoNLL.CoNLLDocument;
import model.CoNLL.CoNLLPart;
import util.Common;
import em.EMLearn;
import em.EMUtil;

public class SupervisedAnaphoric {

	public static void main(String args[]) {
		EMUtil.loadPredictNE("all", "test");
		ArrayList<String> lines = Common.getLines("chinese_list_all_test");
		HashSet<String> goodAns = Common.readFile2Set("goodAnaphors");
		
		ArrayList<String> allMs = new ArrayList<String>();
		
		ArrayList<String> svms = new ArrayList<String>();
		for(String line : lines) {
			CoNLLDocument document = new CoNLLDocument(line);
			for (int k = 0; k < document.getParts().size(); k++) {
				CoNLLPart part = document.getParts().get(k);
				part.setNameEntities(EMUtil.predictNEs.get(part.getDocument()
						.getDocumentID() + "_" + part.getPartID()));
				
				CoNLLPart goldPart = EMUtil.getGoldPart(part, "test");
				HashSet<String> goldPNs = EMUtil.getGoldPNs(goldPart);
				HashSet<String> goldNEs = EMUtil.getGoldNEs(goldPart);
				
				ArrayList<Mention> goldBoundaryNPMentions = EMUtil
						.extractMention(part);
				Collections.sort(goldBoundaryNPMentions);

				ArrayList<Mention> candidates = new ArrayList<Mention>();
				for (Mention m : goldBoundaryNPMentions) {
					if (m.start==m.end && part.getWord(m.start).posTag.equals("PN")) {
						continue;
					}
					candidates.add(m);
				}

				Collections.sort(candidates);

				ArrayList<Mention> anaphors = new ArrayList<Mention>();
				for (Mention anaphor : goldBoundaryNPMentions) {
					if (anaphor.start == anaphor.end
							&& part.getWord(anaphor.end).posTag.equals("PN")) {
						continue;
					}
					if(goldNEs.contains(anaphor.toName())) {
						continue;
					}
					
					ArrayList<Mention> cands = new ArrayList<Mention>();
					for (int h = candidates.size() - 1; h >= 0; h--) {
						Mention cand = candidates.get(h);
						cand.sentenceID = part.getWord(cand.start).sentence
								.getSentenceIdx();
						cand.s = part.getWord(cand.start).sentence;
						if (cand.start < anaphor.start
								&& cand.end != anaphor.end) {
							cands.add(cand);
						}
					}
					
					String key = part.getPartName() + ":" + anaphor.toName();
					allMs.add(key);
					String fea = createInstance(anaphor, cands, part, goodAns.contains(key));
					svms.add(fea);
				}
			}
		}
		System.out.println(all);
		System.out.println(pos);
		System.out.println(pos/all);
		Common.outputLines(svms, "svm.anaphor.train");
		Common.outputLines(allMs, "allMs");
		fea.freeze();
	}
	
	static double all = 0;
	static double pos = 0;
	
	static AnaphorFea fea = new AnaphorFea(true, "anaphor");
	
	public static String createInstance(Mention anaphor, ArrayList<Mention> cands, CoNLLPart part, boolean label) {
		all ++;
		if(label) {
			pos += 1;
		}
		
		fea.configure(anaphor, cands, part);
		
		String svmStr = fea.getSVMFormatString();
		if(label) {
			return "+1 " + svmStr;
		} else {
			return "-1 " + svmStr;
		}
	}
}
