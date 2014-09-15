package em;

import java.io.Serializable;
import java.util.ArrayList;

import model.Mention;
import model.CoNLL.CoNLLPart;
import em.EMUtil.Animacy;
import em.EMUtil.Gender;
import em.EMUtil.Grammatic;
import em.EMUtil.Number;

public class ResolveGroup implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

//	String pronoun;
	
	String anaphor;
	
	ArrayList<Entry> entries;
	Animacy animacy;
	Gender gender;
	Number number;
	Grammatic gram;
	String sem = "unknown";
	String cilin = "null";
	
	public ResolveGroup(Mention m, CoNLLPart part) {
		this.anaphor = m.extent;
		this.entries = new ArrayList<Entry>();
		
		this.animacy = EMUtil.getAntAnimacy(m);
		this.gender = EMUtil.getAntGender(m);
		this.number = EMUtil.getAntNumber(m);
		this.sem = EMUtil.getSemantic(m);
		this.gram = m.gram;
		this.cilin = EMUtil.getCilin(m);
	}

	public static class Entry implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		Context context;
		String head;
		
		Animacy animacy = Animacy.fake;
		Gender gender = Gender.fake;
		Number number = Number.fake;
		String sem = "unknown";
		Grammatic gram;
		
		public static double p_fake_decay = .4;
		
		String cilin = "null";
		
		boolean isFake = false;
		double p;

		double p_c;
		
		public Entry(Mention ant, Context context, CoNLLPart part) {
			this.head = ant.head;
			this.context = context;
			this.isFake = ant.isFake;
			if(!ant.isFake) {
				this.animacy = EMUtil.getAntAnimacy(ant);
				this.gender = EMUtil.getAntGender(ant);
				this.number = EMUtil.getAntNumber(ant);
				this.sem = EMUtil.getSemantic(ant);
			}
			this.gram = ant.gram;
			this.cilin = EMUtil.getCilin(ant);
		}
	}
}
