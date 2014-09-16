package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import util.Common;

import model.CoNLL.CoNLLPart;
import model.CoNLL.CoNLLSentence;
import model.syntaxTree.MyTreeNode;
import em.EMUtil;
import em.EMUtil.Grammatic;
import em.EMUtil.MentionType;

public class Mention implements Comparable<Mention>, Serializable {

	/**
         *
         */
	
	public ArrayList<Mention> innerMs = new ArrayList<Mention>();
	
	public int seq = 0;
	
	public boolean nested = false;
	
	double th = 0.0;
	
	public HashMap<String, ArrayList<String>> moreModifiers = new HashMap<String, ArrayList<String>>();

	public boolean isFake = false;

	public int PRONOUN_TYPE;

	public MentionType mentionType;

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

	public Mention antecedent;

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
		for(int i=this.start;i<this.end;i++) {
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
		if (this.start == ((Mention) em2).start
				&& this.end == ((Mention) em2).end) {
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

	public Mention() {

	}

	public Mention(int start, int end) {
		this.start = start;
		this.end = end;
	}

	// (14, 15) (20, -1) (10, 20)
	public int compareTo(Mention emp2) {
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

	// enforce one-one map
	public static HashMap<String, Mention> chiSpanMaps = new HashMap<String, Mention>();
	public static HashMap<String, Mention> engSpanMaps = new HashMap<String, Mention>();

	public static HashMap<String, HashSet<String>> headMaps = new HashMap<String, HashSet<String>>();

	public String getReadName() {
		return this.s.part.getPartName() + ":" + this.s.part.lang + ":"
				+ this.start + "," + this.end;
	}

	private Mention getXSpanFromCache() {
		if (this.s.part.lang.equals("chi")) {
			Mention xSpan = chiSpanMaps.get(this.getReadName());
			if (xSpan != null) {
				this.xSpanType = xSpan.xSpanType;
				this.alignProb = xSpan.alignProb;
			}
			return xSpan;
		} else {
			Mention xSpan = engSpanMaps.get(this.getReadName());
			if (xSpan != null) {
				this.xSpanType = xSpan.xSpanType;
				this.alignProb = xSpan.alignProb;
			}
			return xSpan;
		}
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

}
