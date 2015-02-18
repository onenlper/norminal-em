package mentionDetect;

import java.util.ArrayList;

import model.EntityMention;
import model.CoNLL.CoNLLPart;

public abstract class MentionDetect {
	public abstract ArrayList<EntityMention>  getMentions(CoNLLPart part);
}
