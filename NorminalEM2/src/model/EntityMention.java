package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import util.Common;

import model.CoNLL.CoNLLPart;
import model.CoNLL.CoNLLSentence;
import model.syntaxTree.MyTreeNode;
//import align.DocumentMap.Unit;
import em.EMUtil;
import em.EMUtil.Grammatic;
import em.EMUtil.MentionType;

public class EntityMention implements Comparable<EntityMention>, Serializable {

	/**
         *
         */
	public static boolean ace = true; 
	
	public int headStart=-1;
	public int headEnd=-1;
	
	public int getS() {
		if(ace) {
			return this.headCharStart;
		} else {
			return this.start;
		}
	}

	public int getE() {
		if(ace) {
			return this.headCharEnd;
		} else {
			return this.end;
		}
	}

	public String type;
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public String refID;
	
	public String getRefID() {
		return refID;
	}

	public void setRefID(String refID) {
		this.refID = refID;
	}
	
	public int extentCharStart;
	public int extendCharEnd;
	
	public int headCharStart;
	public int headCharEnd;
	
	public String ACEType;
	
	public String ACESubtype;
	
	public String semClass;
	
	public String subType;
	
	public ArrayList<EntityMention> innerMs = new ArrayList<EntityMention>();

	public int seq = 0;

//	public ArrayList<Unit> units = new ArrayList<Unit>();

	public boolean nested = false;

	double th = 0.5;

	public HashMap<String, ArrayList<String>> moreModifiers = new HashMap<String, ArrayList<String>>();

	public boolean isFake = false;

	public int PRONOUN_TYPE;

//	public MentionType mentionType;

	public boolean isNNP = false;

	public ArrayList<String> modifyList = new ArrayList<String>();

	public boolean isProperNoun = false;

	public boolean isPronoun = false;

	public boolean generic = false;

	public static int assignMode = 0;

	public int xSpanType = 0;

	public double alignProb = 0;

	public boolean isAZP = false;

	private static final long serialVersionUID = 1L;
	public int start = -1;
	public int end = -1;
	public String extent = "";

	public Entity entity;

	public EntityMention antecedent;

	public String msg;

	public double MI;

	public boolean isCC = false;

	public boolean notInChainZero;

	public String semantic = "unknown";

	public int sentenceID;

	public CoNLLSentence s;

	public String head = "";

	public int entityIndex;

	public int startInS;
	public int endInS;

	public int headInS;

	public int headID;

	public EMUtil.Grammatic gram = Grammatic.other;
	public EMUtil.MentionType mType;

	public EMUtil.Number number;
	public EMUtil.Gender gender;
	public EMUtil.Person person;
	public EMUtil.Animacy animacy;

	public EMUtil.PersonEng personEng;

	public MyTreeNode V;

	public MyTreeNode NP;

	public String NE = "OTHER";

	public boolean isFS = false;

	public boolean isBest = false;

	// TODO
	public boolean isQuoted = false;

	public int getSentenceID() {
		return sentenceID;
	}

	public String getModifier(CoNLLPart part) {
		StringBuilder sb = new StringBuilder();
		for (int i = this.start; i < this.end; i++) {
			sb.append(part.getWord(i).word).append(" ");
		}
		return sb.toString().trim();
	}

	public void setSentenceID(int sentenceID) {
		this.sentenceID = sentenceID;
	}

	public int hashCode() {
		if (this.s != null && this.s.part != null) {
			String str = this.s.part.getPartName() + "#" + this.start + ","
					+ this.end;
			return str.hashCode();
		} else {
			String str = this.start + "," + this.end;
			return str.hashCode();
		}

	}

	public boolean equals(Object em2) {
		if (this.start == ((EntityMention) em2).start
				&& this.end == ((EntityMention) em2).end) {
			return true;
		} else {
			return false;
		}
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public String getExtent() {
		return extent;
	}

	public void setExtent(String extent) {
		this.extent = extent;
	}

	public String getHead() {
		return head;
	}

	public void setHead(String head) {
		this.head = head;
	}

	public EntityMention() {

	}

	public EntityMention(int start, int end) {
		this.start = start;
		this.end = end;
	}

	// (14, 15) (20, -1) (10, 20)
	public int compareTo(EntityMention emp2) {
		int diff = this.start - emp2.start;
		if (diff == 0)
			// return emp2.end - this.end;
			return this.end - emp2.end;
		else
			return diff;
		// if(this.getE()!=-1 && emp2.getE()!=-1) {
		// int diff = this.getE() - emp2.getE();
		// if(diff==0) {
		// return this.getS() - emp2.getS();
		// } else
		// return diff;
		// } else if(this.getE()==-1 && emp2.headEnd!=-1){
		// int diff = this.getS() - emp2.getE();
		// if(diff==0) {
		// return -1;
		// } else
		// return diff;
		// } else if(this.headEnd!=-1 && emp2.headEnd==-1){
		// int diff = this.getE() - emp2.getS();
		// if(diff==0) {
		// return 1;
		// } else
		// return diff;
		// } else {
		// return this.getS()-emp2.getS();
		// }
	}

	public String toName() {
		String str = this.start + "," + this.end;
		return str;
	}

	public String toString() {
		String str = this.start + "," + this.end;
		return str;
	}

	public String getReadName() {
		return this.s.part.getPartName() + ":" + this.s.part.lang + ":"
				+ this.start + "," + this.end;
	}

	private boolean ccStruct() {
		boolean cc = false;
		for (int i = this.startInS; i <= this.endInS; i++) {
			String tag = this.s.getWord(i).posTag;
			if (tag.equalsIgnoreCase("CC")) {
				cc = true;
				break;
			}
		}
		return cc;
	}

	// find mapped span
//	public EntityMention getXSpan() {
//		EntityMention xSpan = this.getXSpanFromCache();
//		if (xSpan == null && assignMode >= 1 && assignMode <= 4) {
//			assignXSpan();
//		}
//
//		if (xSpan != null && this.s.part.lang.equals("chi")) {
//			// mappedChiMs.add(this.getMK());
//		}
//
//		return xSpan;
//	}

	// enforce one-one map
	public static HashMap<String, EntityMention> chiSpanMaps = new HashMap<String, EntityMention>();
	public static HashMap<String, EntityMention> engSpanMaps = new HashMap<String, EntityMention>();

	public static HashMap<String, HashSet<String>> headMaps = new HashMap<String, HashSet<String>>();

	private EntityMention getXSpanFromCache() {
		if (this.s.part.getDocument().language.startsWith("chi")) {
			EntityMention xSpan = chiSpanMaps.get(this.getReadName());
			if (xSpan != null) {
				this.xSpanType = xSpan.xSpanType;
				this.alignProb = xSpan.alignProb;
			}
			return xSpan;
		} else {
			EntityMention xSpan = engSpanMaps.get(this.getReadName());
			if (xSpan != null) {
				this.xSpanType = xSpan.xSpanType;
				this.alignProb = xSpan.alignProb;
			}
			return xSpan;
		}
	}

//	private EntityMention assignXSpan() {
//		EntityMention xSpan = null;
//		if (assignMode == 1) {
////			xSpan = this.getExactMatchXSpan();
//			if (xSpan != null) {
//				// xSpan.xSpanType = 1;
//				this.xSpanType = xSpan.xSpanType;
//				this.alignProb = xSpan.alignProb;
//			}
//		}
//		if (assignMode == 2) {
//			// xSpan = this.getPartialMatchXSpan();
//			if (xSpan != null) {
//				xSpan.xSpanType = 5;
//				this.xSpanType = xSpan.xSpanType;
//				this.alignProb = xSpan.alignProb;
//			}
//		}
//		if (assignMode == 3) {
//			// xSpan = this.getSameTextMapSpan();
//			if (xSpan != null) {
//				xSpan.xSpanType = 6;
//				this.xSpanType = xSpan.xSpanType;
//				this.alignProb = xSpan.alignProb;
//			}
//		}
//		if (assignMode == 4) {
//			xSpan = this.getCreatedSpan();
//			if (xSpan != null) {
//				xSpan.xSpanType = 7;
//				this.xSpanType = xSpan.xSpanType;
//				this.alignProb = xSpan.alignProb;
//			}
//		}
//
//		if (xSpan != null) {
//			boolean put = false;
//			// System.out.println(this.s.part.getDocument().language);
//			// System.out.println("TTT: " +
//			// this.s.part.getDocument().language.startsWith("chi"));
//			// Mention mm = engSpanMaps.get(xSpan.getReadName());
//			// if(mm!=null) {
//			// System.out.println("XMS: " + mm.extent + "#");
//			// }
//
//			if (this.s.part.getDocument().language.startsWith("eng")
//					&& (chiSpanMaps.get(xSpan.getReadName()) == null || chiSpanMaps
//							.get(xSpan.getReadName()).getReadName()
//							.equals(this.getReadName()))) {
//				put = true;
//			} else if (this.s.part.getDocument().language.startsWith("chi")
//					&& (engSpanMaps.get(xSpan.getReadName()) == null || engSpanMaps
//							.get(xSpan.getReadName()).getReadName()
//							.equals(this.getReadName()))) {
//				put = true;
//			}
//			if (put) {
//				if (this.s.part.lang.equals("eng")) {
//					engSpanMaps.put(this.getReadName(), xSpan);
//					chiSpanMaps.put(xSpan.getReadName(), this);
//				} else {
//					chiSpanMaps.put(this.getReadName(), xSpan);
//					engSpanMaps.put(xSpan.getReadName(), this);
//				}
//				String head = this.head;
//				HashSet<String> xHeads = headMaps.get(head);
//				if (xHeads == null) {
//					xHeads = new HashSet<String>();
//					headMaps.put(head, xHeads);
//				}
//				String xHead = xSpan.head.toLowerCase();
//				xHeads.add(xHead);
//			}
//		}
//		return xSpan;
//	}

//	private EntityMention getExactMatchXSpan() {
//		// match head id
//		EntityMention xSpan = null;
//		Unit headUnit = null;
//		for (Unit u : this.units) {
//			if (u.getToken().equalsIgnoreCase(this.head)) {
//				// System.out.println(this.head + "#" + this.extent);
//				headUnit = u;
//				break;
//			}
//		}
//
//		if (headUnit != null) {
//			// ordered
//			ArrayList<Unit> xUnits = headUnit.getMapUnit();
//			loop: for (int i = 0; i < xUnits.size(); i++) {
//				Unit xUnit = xUnits.get(i);
//				double prob = 1;
//				if (headUnit.getMapProb().size() != 0) {
//					prob = headUnit.getMapProb().get(i);
//					if (prob < th) {
//						continue;
//					}
//				}
//				// System.out.println("HEE?" + xUnit.mentions.size());
//				for (EntityMention xs : xUnit.mentions) {
//					String head = xs.head;
//					if (head.equals(xUnit.getToken())
//							&& xs.ccStruct() == this.ccStruct()) {
//						xSpan = xs;
//						xSpan.xSpanType = (int) Math.ceil((prob / 0.25));
//						xSpan.alignProb = prob;
//						// TODO
//						break loop;
//					}
//				}
//			}
//		}
//		return xSpan;
//	}

//	public EntityMention getCreatedSpan() {
//		EntityMention xSpan = null;
//		int leftID = Integer.MAX_VALUE;
//		int rightID = -1;
//		double probAlign = 1;
//		CoNLLSentence xS = null;
//		for (Unit u : this.units) {
//			ArrayList<Unit> xUnits = u.getMapUnit();
//			for (int i = 0; i < xUnits.size(); i++) {
//				Unit xU = xUnits.get(i);
//				double prob = u.getMapProb().get(i);
//				if (prob >= th) {
//					probAlign = Math.min(probAlign, prob);
//					leftID = Math.min(leftID, xU.indexInSentence);
//					rightID = Math.max(rightID, xU.indexInSentence);
//					xS = xU.sentence;
//				}
//			}
//		}
//		if (rightID != -1) {
//			xSpan = xS.getSpan(leftID, rightID);
//			xSpan.alignProb = probAlign;
//		}
//		return xSpan;
//	}

}
