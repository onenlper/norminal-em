package supervised;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import em.Context;
import em.EMUtil;

import model.Mention;
import model.CoNLL.CoNLLPart;
import util.Common.Feature;
import util.YYFeature;

public class AnaphorFea extends YYFeature {

	Mention anaphor;
	ArrayList<Mention> cands;
	CoNLLPart part;
	
	public AnaphorFea(boolean train, String name) {
		super(train, name);
		// TODO Auto-generated constructor stub
	}

	public void configure(Mention ana, ArrayList<Mention> cands, CoNLLPart part) {
		this.anaphor = ana;
		this.cands = cands;
		this.part = part;
	}
	
	@Override
	public ArrayList<Feature> getCategoryFeatures() {
		ArrayList<Feature> feas = new ArrayList<Feature>();
		
		return feas;
	}

	@Override
	public ArrayList<HashSet<String>> getStrFeatures() {
		// TODO Auto-generated method stub
		ArrayList<HashSet<String>> feas = new ArrayList<HashSet<String>>();
//		boolean f0 = false;
//		boolean f1 = false;
//		boolean f2 = false;
//		boolean f3 = false;
//		boolean f4 = false;
//		boolean f5 = false;
//		for(Mention cand : cands) {
//			f0 = cand.extent.equals(anaphor.extent) || f0;
//			f1 = cand.head.equals(anaphor.head) || f1;
//			f2 = cand.head.contains(anaphor.head) || anaphor.head.contains(cand.head) || f2;
//			f3 = Context.head5(cand, anaphor, part)==1 || f3;
//			f4 = Context.sieve4Rule(cand, anaphor, part)==1 || f4;
//			f5 = (!"unkonwn".startsWith(EMUtil.getSemantic(cand)) && EMUtil.getSemantic(cand).equals(EMUtil.getSemantic(anaphor))) || f5;
//		}
//		
//		if(f0) {
//			feas.add(new HashSet<String>(Arrays.asList("true_exact")));
//		} else {
//			feas.add(new HashSet<String>(Arrays.asList("false_exact")));
//		}
//		
//		if(f1) {
//			feas.add(new HashSet<String>(Arrays.asList("true_sieve1")));
//		} else {
//			feas.add(new HashSet<String>(Arrays.asList("false_sieve1")));
//		}
//		
//		if(f2) {
//			feas.add(new HashSet<String>(Arrays.asList("true_sieve2")));
//		} else {
//			feas.add(new HashSet<String>(Arrays.asList("false_sieve2")));
//		}
//		
//		if(f3) {
//			feas.add(new HashSet<String>(Arrays.asList("true_sieve3")));
//		} else {
//			feas.add(new HashSet<String>(Arrays.asList("false_sieve3")));
//		}
//		
//		if(f4) {
//			feas.add(new HashSet<String>(Arrays.asList("true_sieve4")));
//		} else {
//			feas.add(new HashSet<String>(Arrays.asList("false_sieve4")));
//		}
//		
//		if(f5) {
//			feas.add(new HashSet<String>(Arrays.asList("true_sieve5")));
//		} else {
//			feas.add(new HashSet<String>(Arrays.asList("false_sieve5")));
//		}
//		
//		feas.add(new HashSet<String>(Arrays.asList("gramm_" + anaphor.gram.name())));
//		
//		feas.add(new HashSet<String>(Arrays.asList("headPOS_" + part.getWord(anaphor.headID).posTag)));
		feas.add(new HashSet<String>(Arrays.asList("headWord_" + EMUtil.getSemantic(anaphor))));
//		feas.add(new HashSet<String>(Arrays.asList("headWord_" + anaphor.head)));
//		feas.add(new HashSet<String>(Arrays.asList("firstPOS_" + part.getWord(anaphor.start).posTag)));
////		feas.add(new HashSet<String>(Arrays.asList("firstWord_" + part.getWord(anaphor.start).word)));
//		
//		boolean conj = false;
//		for(int i=anaphor.start;i<=anaphor.end;i++) {
//			if(part.getWord(i).posTag.equals("CC")) {
//				conj = true;
//			}
//		}
//		feas.add(new HashSet<String>(Arrays.asList("conj_" + conj)));
//		
//		HashSet<String> posSet = new HashSet<String>();
//		for(int i=anaphor.start;i<=anaphor.end;i++) {
//			posSet.add(part.getWord(i).posTag);
//		}
//		feas.add(posSet);
//		
//		feas.add(new HashSet<String>(Arrays.asList("heading_" + (part.getWord(anaphor.start).sentence.getSentenceIdx()==0))));
		
		return feas;
	}

}
