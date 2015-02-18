package em;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import model.Entity;
import model.EntityMention;
import model.CoNLL.CoNLLDocument;
import model.CoNLL.CoNLLPart;
import util.Common;

public class Inspect3 {
	
	public static void main(String args[]) {
		
		ArrayList<String> lines = Common.getLines("chinese_list_all_test");
		double allMB = 0;
		double anaNPs = 0;
		
		for(String line : lines) {
			CoNLLDocument doc = new CoNLLDocument(line.replace(".v5_auto_conll", ".v5_auto_mention_boundaries_conll"));
			CoNLLDocument goldDoc = new CoNLLDocument(line.replace("auto_conll", "gold_conll"));
			
			for(int i=0;i<goldDoc.getParts().size();i++) {
				CoNLLPart part = doc.getParts().get(i);
				CoNLLPart goldPart = goldDoc.getParts().get(i);
				
				HashSet<String> goldNEs = EMUtil.getGoldNEs(goldPart);
				HashSet<String> goldPNs = EMUtil.getGoldPNs(goldPart);
				
				ArrayList<Entity> chains = part.getChains();
				ArrayList<String> allCommonNPs = new ArrayList<String>();
				
				for(Entity e : chains) {
					for(EntityMention m : e.mentions) {
						if (goldPNs.contains(m.toName())) {
							continue;
						}
						if(goldNEs.contains(m.toName())
								|| goldNEs.contains(m.end + "," + m.end)
								) {
							continue;
						}
						allCommonNPs.add(m.toName());
					}
				}

				ArrayList<EntityMention> allExtractedNPs = EMUtil.extractMention(goldPart);
				for(EntityMention m : allExtractedNPs) {
					if (goldPNs.contains(m.toName())) {
						continue;
					}
					if(goldNEs.contains(m.toName())
							|| goldNEs.contains(m.end + "," + m.end)
							) {
						continue;
					}
					
					if(!allCommonNPs.contains(m.toName())) {
						System.out.println(m.extent + " @ " + line);
					}
				}
				
				ArrayList<Entity> goldChains = goldPart.getChains();
				HashMap<String, HashSet<String>> goldAnaCommonNPs = EMUtil.getGoldAnaphorKeys(goldChains, goldPart);
				anaNPs+= goldAnaCommonNPs.keySet().size();
				
				allMB += allCommonNPs.size();
			}
		}
		
		System.out.println("anaMentions: " + anaNPs);
		System.out.println("goldMentionBoundaries: " + allMB);
		System.out.println("Percent: " + anaNPs/allMB);
	}
}
