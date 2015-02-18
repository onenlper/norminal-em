package em;

import java.io.Serializable;
import java.util.ArrayList;

import em.EMUtil.Animacy;
import em.EMUtil.Gender;
import em.EMUtil.Grammatic;
import em.EMUtil.Number;

import model.EntityMention;
import model.CoNLL.CoNLLPart;

public class ResolveGroupEntityModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	EntityMention anaphor;

	CoNLLPart part;
	
	ArrayList<EntityMention> cands;
	
	public ArrayList<EntryEntityModel> entries;
	
	
//	Animacy animacy;
//	Gender gender;
//	Number number;
//	Grammatic gram;
//	String sem = "unknown";
	
	
	public ResolveGroupEntityModel(EntityMention m, CoNLLPart part) {
		this.part = part;
		this.anaphor = m;
		this.entries = new ArrayList<EntryEntityModel>();
		this.cands = new ArrayList<EntityMention>();
		
//		this.animacy = EMUtil.getAntAnimacy(m);
//		this.gender = EMUtil.getAntGender(m);
//		this.number = EMUtil.getAntNumber(m);
//		this.sem = EMUtil.getSemantic(m);
//		this.gram = m.gram;
	}

	public static class EntryEntityModel implements Serializable, Comparable<EntryEntityModel>{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		ContextEntityModel context;
		
		ArrayList<EntityMention> cluster;
		
		double p;
		
//		Animacy animacy = Animacy.fake;
//		Gender gender = Gender.fake;
//		Number number = Number.fake;
//		String sem = "unknown";
//		Grammatic gram;
		
		public EntryEntityModel(ContextEntityModel context, ArrayList<EntityMention> cluster) {
			this.cluster = cluster;
			this.context = context;
			
			EntityMention ant = cluster.get(0);
//			this.animacy = EMUtil.getAntAnimacy(ant);
//			this.gender = EMUtil.getAntGender(ant);
//			this.number = EMUtil.getAntNumber(ant);
//			this.sem = EMUtil.getSemantic(ant);
//			this.gram = ant.gram;
		}

		@Override
		public int compareTo(EntryEntityModel e2) {
			EntityMention m1 = this.cluster.get(this.cluster.size()-1);
			EntityMention m2 = e2.cluster.get(e2.cluster.size()-1);
			return m1.compareTo(m2);
		}
	}
}
