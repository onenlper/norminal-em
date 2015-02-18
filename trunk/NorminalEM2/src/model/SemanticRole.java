package model;

import java.util.ArrayList;
import java.util.HashMap;

public class SemanticRole implements Comparable<SemanticRole>{
	
	public ArrayList<EntityMention> arg0;

	public ArrayList<EntityMention> arg1;
	
	public ArrayList<EntityMention> tmp;
	
	public HashMap<String, ArrayList<EntityMention>> args = new HashMap<String, ArrayList<EntityMention>>();
	
	public EntityMention predicate;
	
	public SemanticRole() {
		this.arg0 = new ArrayList<EntityMention>();
		this.arg1 = new ArrayList<EntityMention>();
		this.tmp = new ArrayList<EntityMention>();
		this.predicate = new EntityMention();
	}

	@Override
	public int compareTo(SemanticRole arg0) {
		return this.predicate.start-((SemanticRole)arg0).predicate.start;
	}

}
