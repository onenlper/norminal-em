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

	String anaphorName;
	
	ArrayList<EntryEntityModel> entries;
	
	public ResolveGroupEntityModel(Mention m, CoNLLPart part) {
		this.anaphorName = part.getPartName() + ":" + m.toName();
		this.entries = new ArrayList<EntryEntityModel>();
	}

	public static class EntryEntityModel implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		Context context;
		
		String antName;
		
		boolean isFake;
		double p;
		
		public EntryEntityModel(Mention ant, Context context, CoNLLPart part) {
			this.antName = part.getPartName() + ":" + ant.toName();
			this.context = context;
			this.isFake = ant.isFake;
		}
	}
}
