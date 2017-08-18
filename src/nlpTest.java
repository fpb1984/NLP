
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.stemmer.PorterStemmer;

import org.json.JSONArray;
import org.json.JSONObject;

public class nlpTest {

	final static String defaultSentence = "Take this paragraph of text and return an alphabetized list of ALL unique words. A unique word is any form of a word often communicated with essentially the same meaning. For example, fish and fishes could be defined as a unique word by using their stem fish. For each unique word found in this entire paragraph, determine the how many times the word appears in total. Also, provide an analysis of what sentence index position or positions the word is found. The following words should not be included in your analysis or result set: \"a\", \"the\", \"and\", \"of\", \"in\", \"be\", \"also\" and \"as\". Your final result MUST be displayed in a readable console output in the same format as the JSON sample object show below.";
	static String sentence;
	static private String modelParserFilename;
	static private String modelSentenceFilename;
	static Set<String> words = new TreeSet<>();
	final static String propertiesPath = "./nlpTest.properties";
	final static String[] prohibitedWords = {"a", "the", "and", "of", "in", "be", "also", "as"};
	final static String[] acceptedWordTypes = {"NN", "JJ", "RB", "VB"};
	static List<String> prohibitedList;
	static List<String> acceptedWordTypesList;
	
	public static void main(String[] args) {

		JSONObject outputObj;
		
		prohibitedList = Arrays.asList(prohibitedWords);		
		acceptedWordTypesList = Arrays.asList(acceptedWordTypes);
		
		InputStream modelParserIn = null;
		InputStream modelSentenceIn = null;
		try {
			
			getConfigs();
			
			if(modelParserFilename==null){
				modelParserIn = ClassLoader.getSystemClassLoader().getResourceAsStream("en-parser-chunking.bin");
			}else
				modelParserIn = new FileInputStream("./" + modelParserFilename);
			
			if(modelSentenceFilename==null){
				modelSentenceIn = ClassLoader.getSystemClassLoader().getResourceAsStream("en-sent.bin");
			}else
				modelSentenceIn = new FileInputStream("./" + modelSentenceFilename);
							
			if((sentence==null)||(sentence.isEmpty()))
				sentence = defaultSentence;
			
			sentence = sentence.toLowerCase();
				
			ParserModel model = new ParserModel(modelParserIn);
		
			Parser parser = ParserFactory.create(model);
			
			Parse []parses = ParserTool.parseLine(sentence, parser, 1);
			
			for (Parse p : parses)
				getWords(p);
			
			SentenceModel sentenceModel = new SentenceModel(modelSentenceIn);
			SentenceDetectorME detector = new SentenceDetectorME(sentenceModel);  
		    
			String sentences[] = detector.sentDetect(sentence);
			
			outputObj = new JSONObject();
			JSONArray result = new JSONArray();

			for(String word:words){
				
				int totalOccurances = 0;
				List<Integer> sentenceIndexes = new ArrayList<>();
				
				PorterStemmer stemmer = new PorterStemmer();
				String stem = stemmer.stem(word);
				
				for(int i=0; i< sentences.length; i++)
					if(sentences[i].contains(stem)){
						totalOccurances++;
						sentenceIndexes.add(i);
					}
				JSONObject wordDescription = new JSONObject();
				wordDescription.put("word", word);
				wordDescription.put("total-occurances", totalOccurances);
				wordDescription.put("sentence-indexes", sentenceIndexes);
				result.put(wordDescription);		
				
			}						
			
			outputObj.put("results", result);
			
			System.out.println("Sample Output:");
			System.out.println();
			System.out.println(outputObj.toString(1));
			
		}
		catch (IOException e) {
			System.out.println("Please check the model file config");
			e.printStackTrace();
		}
	}
	

	public static void getWords(Parse word) {
	
		if(acceptedWordTypesList.contains(word.getType())) {
	    	String noun = word.getCoveredText().replace(",", "").replace(".", "").replace("?", "").replace(":", "").replace("\"", "");
	    	if(!prohibitedList.contains(noun))
	    		words.add(noun);
	    }
	    for (Parse child : word.getChildren())
	    	getWords(child);
	}
	
	public static void getConfigs(){

	    Properties mainProperties = new Properties();
	    FileInputStream file;
	    try {
			file = new FileInputStream(propertiesPath);
			mainProperties.load(file);
			file.close();
			sentence = mainProperties.getProperty("sentence", null);
			modelParserFilename = mainProperties.getProperty("model.parser.filename", null);
			modelSentenceFilename = mainProperties.getProperty("model.sentence.filename", null);
		} catch (FileNotFoundException e) {
			System.out.println("Couln't find properties file, using defaults");
		} catch (IOException e) {
			System.out.println("Couln't find properties file, using defaults");
		}

	}
	
}
