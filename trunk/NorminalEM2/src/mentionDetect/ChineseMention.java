//package mentionDetect;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.HashSet;
//
//import model.Element;
//import model.Entity;
//import model.Mention;
//import model.CoNLL.CoNLLDocument;
//import model.CoNLL.CoNLLPart;
//import model.CoNLL.CoNLLSentence;
//import util.Common;
//import em.EMUtil;
//
//public class ChineseMention {
//
//	public ChineseMention() {
//	}
//
//	public static boolean goldNE = false;
//
//	public ArrayList<Mention> getChineseMention(CoNLLSentence s) {
//		ArrayList<Mention> mentions = new ArrayList<Mention>();
//		CoNLLPart part = s.part;
//		part.setNameEntities(getChNE(part));
//		// part.setNameEntities(this.getChGoldNE(part));
//
//		mentions.addAll(this.getChNPMention(s));
//		removeDuplicateMentions(mentions);
//	
//		EMUtil.assignNE(mentions, part.getNameEntities());
////		EMUtil.pruneChMentions(mentions, part);
//		
//		return mentions;
//	}
//	
//	public ArrayList<Mention> getChineseMention(CoNLLPart part) {
//		ArrayList<Mention> mentions = new ArrayList<Mention>();
//
//		part.setNameEntities(getChNE(part));
//		// part.setNameEntities(this.getChGoldNE(part));
//
//		mentions.addAll(this.getChNPMention(part));
//		removeDuplicateMentions(mentions);
//	
//		EMUtil.assignNE(mentions, part.getNameEntities());
////		EMUtil.pruneChMentions(mentions, part);
//		
//		return mentions;
//	}
//
//	private ArrayList<Element> getChGoldNE(CoNLLPart part) {
//		String documentID = "/users/yzcchen/chen3/CoNLL/conll-2012/v4/data/development/data/chinese/annotations/"
//				+ part.docName + ".v4_gold_skel";
//		// System.out.println(documentID);
//		CoNLLDocument document = new CoNLLDocument(documentID);
//		CoNLLPart goldPart = document.getParts().get(part.getPartID());
//		// for (Element ner : goldPart.getNameEntities()) {
//		// int start = ner.start;
//		// int end = ner.end;
//		// String ne = ner.content;
//		//
//		// StringBuilder sb = new StringBuilder();
//		// for (int k = start; k <= end; k++) {
//		// sb.append(part.getWord(k).word).append(" ");
//		// }
//		// // System.out.println(sb.toString() + " # " + ne);
//		// // System.out.println(goldPart.);
//		// }
//		return goldPart.getNameEntities();
//	}
//
//	HashMap<String, ArrayList<Element>> EMs;
//
//	private ArrayList<Element> getChNE(CoNLLPart part) {
//		String key = part.getDocument().getDocumentID() + "_"
//				+ part.getPartID();
//		ArrayList<Element> elements = EMUtil.predictNEs.get(key);
//		if (elements == null) {
//			elements = new ArrayList<Element>();
//		}
//		for (Element element : elements) {
//			for (int i = element.start; i <= element.end; i++) {
//				part.getWord(i).setRawNamedEntity(element.content);
//			}
//		}
//		return elements;
//	}
//
//	private void setGoldParseTree(CoNLLPart part) {
//		String documentID = "/users/yzcchen/chen3/CoNLL/conll-2012/v4/data/test/data/chinese/annotations/"
//				+ part.getDocument().getDocumentID() + ".v4_auto_conll";
//		System.out.println(documentID);
//		CoNLLDocument document = new CoNLLDocument(documentID);
//		CoNLLPart goldPart = document.getParts().get(part.getPartID());
//
//		for (int i = 0; i < goldPart.getCoNLLSentences().size(); i++) {
//			CoNLLSentence goldSentence = goldPart.getCoNLLSentences().get(i);
//			part.getCoNLLSentences().get(i)
//					.setSyntaxTree(goldSentence.getSyntaxTree());
//		}
//	}
//
//	private void setSystemParseTree(CoNLLPart part) {
//		String documentID = "/users/yzcchen/chen3/CoNLL/conll-2012_system_parse/v4/data/test/data/chinese/annotations/"
//				+ part.getDocument().getDocumentID() + ".v5_auto_conll";
//		System.out.println(documentID);
//		CoNLLDocument document = new CoNLLDocument(documentID);
//		CoNLLPart goldPart = document.getParts().get(part.getPartID());
//
//		for (int i = 0; i < goldPart.getCoNLLSentences().size(); i++) {
//			CoNLLSentence goldSentence = goldPart.getCoNLLSentences().get(i);
//			part.getCoNLLSentences().get(i)
//					.setSyntaxTree(goldSentence.getSyntaxTree());
//		}
//	}
//	
//	private ArrayList<Mention> getChNPMention(CoNLLSentence s) {
//		ArrayList<Mention> npMentions = EMUtil.extractMention(s);
//
//		// MentionDetect md = new GoldBoundaryMentionTest();
//		// npMentions = md.getMentions(part);
//
//		// Gold Mention
//		// MentionDetect md = new GoldMentionTest();
//		// npMentions = md.getMentions(part);
//		CoNLLPart part = s.part;
//		for (int g = 0; g < npMentions.size(); g++) {
//			Mention npMention = npMentions.get(g);
//			int end = npMention.end;
//			int start = npMention.start;
//			StringBuilder sb = new StringBuilder();
//			for (int i = start; i <= end; i++) {
//				sb.append(part.getWord(i).word).append(" ");
//			}
//			npMention.extent = sb.toString().trim().toLowerCase();
//		}
//		return npMentions;
//	}
//
//	private ArrayList<Mention> getChNPMention(CoNLLPart part) {
//		ArrayList<Mention> npMentions = EMUtil.extractMention(part);
//
//		// MentionDetect md = new GoldBoundaryMentionTest();
//		// npMentions = md.getMentions(part);
//
//		// Gold Mention
//		// MentionDetect md = new GoldMentionTest();
//		// npMentions = md.getMentions(part);
//
//		for (int g = 0; g < npMentions.size(); g++) {
//			Mention npMention = npMentions.get(g);
//			int end = npMention.end;
//			int start = npMention.start;
//			StringBuilder sb = new StringBuilder();
//			StringBuilder sb2 = new StringBuilder();
//			for (int i = start; i <= end; i++) {
//				sb.append(part.getWord(i).word).append(" ");
//				sb2.append(part.getWord(i).orig).append(" ");
//			}
//			npMention.extent = sb.toString().trim().toLowerCase();
//		}
//		return npMentions;
//	}
//
//	private void removeDuplicateMentions(ArrayList<Mention> mentions) {
//		HashSet<Mention> mentionsHash = new HashSet<Mention>();
//		mentionsHash.addAll(mentions);
//		mentions.clear();
//		mentions.addAll(mentionsHash);
//	}
//	
//	public static void main(String args[]) {
//		String folder = "all";
//		ArrayList<String> files = Common.getLines("chinese_list_" + args[0]
//				+ "_test");
//		double gold = 0;
//		double sys = 0;
//		double hit = 0;
//		for (String file : files) {
//			System.out.println(file);
//			CoNLLDocument document = new CoNLLDocument(file.replace(
//					"auto_conll", "gold_conll"));
//			
//			for(CoNLLPart part : document.getParts()) {
//				ArrayList<Mention> sysMs = EMUtil.extractMention(part);
//				
//				ArrayList<Mention> goldMs = new ArrayList<Mention>();
//				for(Entity chain : part.getChains()) {
//					for(Mention m : chain.mentions) {
//						goldMs.add(m);
//					}
//				}
//				gold += goldMs.size();
//				sys += sysMs.size();
//				
//				for (Mention g : goldMs) {
//					boolean find = false;
//					for(Mention s : sysMs) {
//						if(g.toName().equals(s.toName())) {
//							hit += 1;
//							find = true;
//							break;
//						}
//					}
//					if (!find) {
////						System.out.println(g.extent);
////						System.out.println(part.getWord(g.end).sentence.getText());
//					}
//				}
//			}
//		}
//		System.out.println("Recall: " + hit/gold);
//	}
//}
