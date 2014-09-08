package em;

import java.io.Serializable;
import java.util.ArrayList;

import model.Mention;
import model.CoNLL.CoNLLPart;

public class ResolveGroupEntityModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	Mention anaphor;

	CoNLLPart part;
	
	ArrayList<Mention> cands;
	
	public ArrayList<EntryEntityModel> entries;
	
	public ResolveGroupEntityModel(Mention m, CoNLLPart part) {
		this.part = part;
		this.anaphor = m;
		this.entries = new ArrayList<EntryEntityModel>();
		this.cands = new ArrayList<Mention>();
	}

	public static class EntryEntityModel implements Serializable, Comparable<EntryEntityModel>{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		Context context;
		
		ArrayList<Mention> cluster;
		
		double p;
		
		public EntryEntityModel(Context context, ArrayList<Mention> cluster) {
			this.cluster = cluster;
			this.context = context;
		}

		@Override
		public int compareTo(EntryEntityModel e2) {
			Mention m1 = this.cluster.get(this.cluster.size()-1);
			Mention m2 = e2.cluster.get(e2.cluster.size()-1);
			return m1.compareTo(m2);
		}
	}
}
