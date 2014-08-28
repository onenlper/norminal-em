package dict;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import util.Common;

public class EnDictionary {

	private static EnDictionary dict;
	
	public static EnDictionary getInstance() {
		if(dict==null) {
			dict = new EnDictionary();
		}
		return dict;
	}
	
	private EnDictionary() {
		personPronouns.addAll(this.animatePronouns);
		allPronouns.addAll(firstPersonPronouns);
		allPronouns.addAll(secondPersonPronouns);
		allPronouns.addAll(thirdPersonPronouns);
		allPronouns.addAll(otherPronouns);
		stopWords.addAll(allPronouns);
		if(true) {
			return;
		}
		// load gender number dictionary
		ArrayList<String> lines = Common.getLines("gender.data");
		for (String line : lines) {
			String split[] = line.split("\t");
			ArrayList<String> tokens = new ArrayList<String>(Arrays.asList(split[0].split(" ")));
			String[] countStr = split[1].split(" ");
			int[] counts = new int[4];
			counts[0] = Integer.parseInt(countStr[0]);
			counts[1] = Integer.parseInt(countStr[1]);
			counts[2] = Integer.parseInt(countStr[2]);
			counts[3] = Integer.parseInt(countStr[3]);
			bigGenderNumber.put(tokens, counts);
		}
		loadDemonymLists("dict/demonyms.txt");
		loadStateAbbreviation("dict/state-abbreviations.txt");
		adjectiveNation.addAll(demonymSet);
		adjectiveNation.removeAll(demonyms.keySet());
	}

	public final Set<String> neutralWords = Common.readFile2Set("dict/neutral.unigrams.txt");
	public final Set<String> femaleWords = Common.readFile2Set("dict/female.unigrams.txt");
	public final Set<String> maleWords = Common.readFile2Set("dict/male.unigrams.txt");

	public final Set<String> pluralWords = Common.readFile2Set("dict/plural.unigrams.txt");
	public final Set<String> singularWords = Common.readFile2Set("dict/singular.unigrams.txt");

	public final Set<String> inanimateWords = Common.readFile2Set("dict/inanimate.unigrams.txt");
	public final Set<String> animateWords = Common.readFile2Set("dict/animate.unigrams.txt");

	public final Map<String, Set<String>> demonyms = new HashMap<String, Set<String>>();
	public final Set<String> demonymSet = new HashSet<String>();
	public final Set<String> adjectiveNation = new HashSet<String>();

	private void loadDemonymLists(String demonymFile) {
		ArrayList<String> lines = Common.getLines(demonymFile);
		for (String oneline : lines) {
			String[] line = oneline.split("\t");
			if (line[0].startsWith("#"))
				continue;
			Set<String> set = new HashSet<String>();
			for (String s : line) {
				set.add(s.toLowerCase());
				demonymSet.add(s.toLowerCase());
			}
			demonyms.put(line[0].toLowerCase(), set);
			adjectiveNation.addAll(demonymSet);
			adjectiveNation.removeAll(demonyms.keySet());
		}
	}

	public void loadStateAbbreviation(String statesFile) {
		ArrayList<String> lines = Common.getLines(statesFile);
		for (String line : lines) {
			String[] tokens = line.split("\t");
			statesAbbreviation.put(tokens[1], tokens[0]);
			statesAbbreviation.put(tokens[2], tokens[0]);
		}
	}

	public final Map<String, String> statesAbbreviation = new HashMap<String, String>();

	public final Set<String> temporals = new HashSet<String>(Arrays.asList("second", "minute", "hour", "day", "week",
			"month", "year", "decade", "century", "millennium", "monday", "tuesday", "wednesday", "thursday", "friday",
			"saturday", "sunday", "now", "yesterday", "tomorrow", "age", "time", "era", "epoch", "morning", "evening",
			"day", "night", "noon", "afternoon", "semester", "trimester", "quarter", "term", "winter", "spring",
			"summer", "fall", "autumn", "season", "january", "february", "march", "april", "may", "june", "july",
			"august", "september", "october", "november", "december"));

	public final Set<String> personPronouns = new HashSet<String>();
	public final Set<String> allPronouns = new HashSet<String>();
	public final Set<String> quantifiers = new HashSet<String>(Arrays.asList("not", "every", "any", "none",
			"everything", "anything", "nothing", "all", "enough"));
	public final Set<String> parts = new HashSet<String>(Arrays.asList("half", "one", "two", "three", "four", "five",
			"six", "seven", "eight", "nine", "ten", "hundred", "thousand", "million", "billion", "tens", "dozens",
			"hundreds", "thousands", "millions", "billions", "group", "groups", "bunch", "number", "numbers", "pinch",
			"amount", "amount", "total", "all", "mile", "miles", "pounds"));
	public final Set<String> nonWords = new HashSet<String>(Arrays.asList("mm", "hmm", "ahem", "um", "ha", "er", "-rrb-", "-lrb-"));

	public final HashMap<ArrayList<String>, int[]> bigGenderNumber = new HashMap<ArrayList<String>, int[]>();

	public final Set<String> femalePronouns = new HashSet<String>(Arrays.asList(new String[] { "her", "hers",
			"herself", "she" }));
	public final Set<String> malePronouns = new HashSet<String>(Arrays.asList(new String[] { "he", "him", "himself",
			"his" }));
	public final Set<String> neutralPronouns = new HashSet<String>(Arrays.asList(new String[] { "it", "its", "itself",
			"where", "here", "there", "which" }));
	public final Set<String> possessivePronouns = new HashSet<String>(Arrays.asList(new String[] { "my", "your", "his",
			"her", "its", "our", "their", "whose" }));
	public final Set<String> otherPronouns = new HashSet<String>(Arrays.asList(new String[] { "who", "whom", "whose",
			"where", "when", "which" }));
	public final Set<String> thirdPersonPronouns = new HashSet<String>(Arrays.asList(new String[] { "he", "him",
			"himself", "his", "she", "her", "herself", "hers", "her", "it", "itself", "its", "one", "oneself", "one's",
			"they", "them", "themself", "themselves", "theirs", "their", "they", "them", "'em", "themselves" }));
	public final Set<String> secondPersonPronouns = new HashSet<String>(Arrays.asList(new String[] { "you", "yourself",
			"yours", "your", "yourselves" }));
	public final Set<String> firstPersonPronouns = new HashSet<String>(Arrays.asList(new String[] { "i", "me",
			"myself", "mine", "my", "we", "us", "ourself", "ourselves", "ours", "our" }));
	public final Set<String> moneyPercentNumberPronouns = new HashSet<String>(Arrays
			.asList(new String[] { "it", "its" }));
	public final Set<String> dateTimePronouns = new HashSet<String>(Arrays.asList(new String[] { "when" }));
	public final Set<String> organizationPronouns = new HashSet<String>(Arrays.asList(new String[] { "it", "its",
			"they", "their", "them", "which" }));
	public final Set<String> locationPronouns = new HashSet<String>(Arrays.asList(new String[] { "it", "its", "where",
			"here", "there" }));
	public final Set<String> inanimatePronouns = new HashSet<String>(Arrays.asList(new String[] { "it", "itself",
			"its", "where", "when" }));
	public final Set<String> unknownAnimatePronouns = new HashSet<String>(Arrays.asList(new String[] {"they", "them",
			"themself", "themselves", "theirs", "their", "they", "them", "themselves",}));
	public final Set<String> animatePronouns = new HashSet<String>(Arrays.asList(new String[] { "i", "me", "myself",
			"mine", "my", "we", "us", "ourself", "ourselves", "ours", "our", "you", "yourself", "yours", "your",
			"yourselves", "he", "him", "himself", "his", "she", "her", "herself", "hers", "her", "one", "oneself",
			"one's", "they", "them", "themself", "themselves", "theirs", "their", "they", "them", "'em", "themselves",
			"who", "whom", "whose" }));
	public final Set<String> indefinitePronouns = new HashSet<String>(Arrays.asList(new String[] { "another",
			"anybody", "anyone", "anything", "each", "either", "enough", "everybody", "everyone", "everything", "less",
			"little", "much", "neither", "no one", "nobody", "nothing", "one", "other", "plenty", "somebody",
			"someone", "something", "both", "few", "fewer", "many", "others", "several", "all", "any", "more", "most",
			"none", "some", "such" }));
	public final Set<String> relativePronouns = new HashSet<String>(Arrays.asList(new String[] { "that", "who",
			"which", "whom", "where", "whose" }));
	public final Set<String> GPEPronouns = new HashSet<String>(Arrays.asList(new String[] { "it", "itself", "its",
			"they", "where" }));
	public final Set<String> pluralPronouns = new HashSet<String>(Arrays.asList(new String[] { "we", "us", "ourself",
			"ourselves", "ours", "our", "yourself", "yourselves", "they", "them", "themself", "themselves", "theirs",
			"their" }));
	public final Set<String> singularPronouns = new HashSet<String>(Arrays.asList(new String[] { "i", "me", "myself",
			"mine", "my", "yourself", "he", "him", "himself", "his", "she", "her", "herself", "hers", "her", "it",
			"itself", "its", "one", "oneself", "one's" }));
	public final Set<String> facilityVehicleWeaponPronouns = new HashSet<String>(Arrays.asList(new String[] { "it",
			"itself", "its", "they", "where" }));
	public final Set<String> miscPronouns = new HashSet<String>(Arrays.asList(new String[] { "it", "itself", "its",
			"they", "where" }));
	public final Set<String> titles = new HashSet<String>(Arrays.asList(new String[] { "mr.", "mrs.", "miss",
			"ms."}));
	public final Set<String> reflexivePronouns = new HashSet<String>(Arrays.asList(new String[] { "myself", "yourself",
			"yourselves", "himself", "herself", "itself", "ourselves", "themselves", "oneself" }));
	public final Set<String> transparentNouns = new HashSet<String>(Arrays.asList(new String[] { "bunch", "group",
			"breed", "class", "ilk", "kind", "half", "segment", "top", "bottom", "glass", "bottle", "box", "cup",
			"gem", "idiot", "unit", "part", "stage", "name", "division", "label", "group", "figure", "series",
			"member", "members", "first", "version", "site", "side", "role", "largest", "title", "fourth", "third",
			"second", "number", "place", "trio", "two", "one", "longest", "highest", "shortest", "head", "resident",
			"collection", "result", "last" }));

	public final Set<String> stopWords = new HashSet<String>(Arrays.asList(new String[] { "a", "an", "the", "of", "at",
			"on", "upon", "in", "to", "from", "out", "as", "so", "such", "or", "and", "those", "this", "these", "that",
			"for", ",", "is", "was", "am", "are", "'s", "been", "were" }));
	public final Set<String> notOrganizationPRP = new HashSet<String>(Arrays.asList(new String[] { "i", "me", "myself",
			"mine", "my", "yourself", "he", "him", "himself", "his", "she", "her", "herself", "hers", "here" }));
	
	public final Set<String> reportVerb = new HashSet<String>(Arrays.asList("accuse", "acknowledge", "add", "admit",
			"advise", "agree", "alert", "allege", "announce", "answer", "apologize", "argue", "ask", "assert",
			"assure", "beg", "blame", "boast", "caution", "charge", "cite", "claim", "clarify", "command", "comment",
			"compare", "complain", "concede", "conclude", "confirm", "confront", "congratulate", "contend",
			"contradict", "convey", "counter", "criticize", "debate", "decide", "declare", "defend", "demand",
			"demonstrate", "deny", "describe", "determine", "disagree", "disclose", "discount", "discover", "discuss",
			"dismiss", "dispute", "disregard", "doubt", "emphasize", "encourage", "endorse", "equate", "estimate",
			"expect", "explain", "express", "extoll", "fear", "feel", "find", "forbid", "forecast", "foretell",
			"forget", "gather", "guarantee", "guess", "hear", "hint", "hope", "illustrate", "imagine", "imply",
			"indicate", "inform", "insert", "insist", "instruct", "interpret", "interview", "invite", "issue",
			"justify", "learn", "maintain", "mean", "mention", "negotiate", "note", "observe", "offer", "oppose",
			"order", "persuade", "pledge", "point", "point out", "praise", "pray", "predict", "prefer", "present",
			"promise", "prompt", "propose", "protest", "prove", "provoke", "question", "quote", "raise", "rally",
			"read", "reaffirm", "realise", "realize", "rebut", "recall", "reckon", "recommend", "refer", "reflect",
			"refuse", "refute", "reiterate", "reject", "relate", "remark", "remember", "remind", "repeat", "reply",
			"report", "request", "respond", "restate", "reveal", "rule", "say", "see", "show", "signal", "sing",
			"slam", "speculate", "spoke", "spread", "state", "stipulate", "stress", "suggest", "support", "suppose",
			"surmise", "suspect", "swear", "teach", "tell", "testify", "think", "threaten", "told", "uncover",
			"underline", "underscore", "urge", "voice", "vow", "warn", "welcome", "wish", "wonder", "worry", "write", 
			"表示", "讲起", "说话", "说", "指出", "介绍", "认为", "密布", "觉得", "汇给", "低吟", "想", "介绍", "以为", "惊叫",
			"回忆", "宣告", "报道", "透露", "谈", "感慨", "反映", "宣布", "指"));

}
