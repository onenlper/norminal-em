package em;

import java.io.Serializable;
import java.util.ArrayList;

import model.Mention;
import em.EMUtil.Animacy;
import em.EMUtil.Gender;
import em.EMUtil.Number;
import em.EMUtil.Person;

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
	
	public ResolveGroup(Mention m) {
		this.anaphor = m.extent;
		this.entries = new ArrayList<Entry>();
		
		this.animacy = EMUtil.getAntAnimacy(m);
		this.gender = EMUtil.getAntGender(m);
		this.number = EMUtil.getAntNumber(m);
		
	}

	public static class Entry implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		Context context;
		String head;
		
		Animacy animacy;
		Gender gender;
		Number number;
		
		double p;

		public Entry(Mention ant, Context context) {
			this.head = ant.head;
			this.context = context;
			
			this.animacy = EMUtil.getAntAnimacy(ant);
			this.gender = EMUtil.getAntGender(ant);
			this.number = EMUtil.getAntNumber(ant);
		}
	}
}
