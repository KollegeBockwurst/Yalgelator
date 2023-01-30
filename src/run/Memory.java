package run;

import algebra.Polynomial_Ring;
import java.util.logging.Level;
import java.util.logging.Logger;
import lie.Root_System;

public class Memory {
	private final static Logger LOGGER =  Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	private RootSystemDataPackage[] rootSystemPackages = new RootSystemDataPackage[0];
	
	public RootSystemDataPackage[] getRootSystemPackages() {
		return this.rootSystemPackages;
	}
	
	public void reset() {
		rootSystemPackages = new RootSystemDataPackage[0];
	}
	
	public void addRootSystems(Root_System[] rootSystems) {
		int oldLength = rootSystemPackages.length;
		RootSystemDataPackage[] nextRootSystemPackages = new RootSystemDataPackage[oldLength + rootSystems.length];
		for(int i = 0; i < oldLength; i++) {
			nextRootSystemPackages[i] = rootSystemPackages[i];
		}
		for(int i = 0; i < rootSystems.length; i++) {
			nextRootSystemPackages[oldLength + i] = new RootSystemDataPackage(rootSystems[i]);
		}
		rootSystemPackages = nextRootSystemPackages;
	}
	
	public void addRootSystem(Root_System rootSystem) {
		Root_System[] rootSystems = new Root_System[1];
		rootSystems[0] = rootSystem;
		this.addRootSystems(rootSystems);
	}
	
	public void addRootSystemPackages(RootSystemDataPackage[] newRootSystemPackages) {
		int oldLength = rootSystemPackages.length;
		RootSystemDataPackage[] nextRootSystemPackages = new RootSystemDataPackage[oldLength + newRootSystemPackages.length];
		for(int i = 0; i < oldLength; i++) {
			 nextRootSystemPackages[i] = rootSystemPackages[i];
		}
		for(int i = 0; i < newRootSystemPackages.length; i++) {
			 nextRootSystemPackages[oldLength + i] = newRootSystemPackages[i];
		}
		rootSystemPackages =  nextRootSystemPackages;
	}
	
	public class RootSystemDataPackage{
		private final Root_System ROOT_SYSTEM;
		private WordDataPackage[] wordPackages = new WordDataPackage[0];
		private int[] wordPackagesLengthIndex = new int[0];
		private Root_System.Word longestWord = null;
		

		public RootSystemDataPackage(Root_System rootSystem) {
			this.ROOT_SYSTEM = rootSystem;
		}
		
		public Root_System getRootSystem() {
			return this.ROOT_SYSTEM;
		}
		
		public Root_System.Word getLongestWord(){
			return this.longestWord;
		}
		
		public WordDataPackage[] getWordPackages() {
			return this.wordPackages;
		}
		
		public WordDataPackage[] getWordPackages(int length) {
			if(length < 0 || length >= wordPackagesLengthIndex.length) return new WordDataPackage[0];
			int startIndex = wordPackagesLengthIndex[length];
			int stopIndex = (length + 1 < wordPackagesLengthIndex.length) ? wordPackagesLengthIndex[length + 1] : wordPackages.length;
			WordDataPackage[] result = new WordDataPackage[stopIndex - startIndex];
			for(int i = startIndex; i < stopIndex; i++) {
				result[i - startIndex] = this.wordPackages[i];
			}
			return result;
		}
		
		public void setLongestWord(Root_System.Word longestWord) {
			this.longestWord = longestWord;
		}
		
		public void addWords(Root_System.Word[] words) {
			for(int i = 0; i < words.length; i++) {
				addWord(words[i]);
			}
		}
		
		public void addWord(Root_System.Word word) {
			addWordPackage(new WordDataPackage(word));
		}
		
		public void addWordPackages(WordDataPackage[] newWordPackages) {
			for(int i = 0; i < newWordPackages.length; i++) {
				this.addWordPackage(newWordPackages[i]);
			}
		}

		public void addWordPackage(WordDataPackage wordPackage) {
			int length = wordPackage.getWord().getLength();
			WordDataPackage[] nextWordPackages = new WordDataPackage[wordPackages.length + 1];
			for(int i = 0; i < wordPackages.length; i++) {
				nextWordPackages[(wordPackages[i].getWord().getLength() <= length) ? i : i+1] = wordPackages[i];
			}
			for(int i = length + 1; i < wordPackagesLengthIndex.length; i++) {
				wordPackagesLengthIndex[i]++;
			}
			if(wordPackagesLengthIndex.length <= length) { //Index erweitern
				int[] nextWordPackagesLengthIndex = new int[length + 1];
				for(int i = 0; i < wordPackagesLengthIndex.length; i++) {
					nextWordPackagesLengthIndex[i] = wordPackagesLengthIndex[i];
				}
				for(int i = wordPackagesLengthIndex.length; i < nextWordPackagesLengthIndex.length; i++) {
					nextWordPackagesLengthIndex[i] = wordPackages.length;
				}
				this.wordPackagesLengthIndex = nextWordPackagesLengthIndex;
			}
			
			
			nextWordPackages[(length + 1 < wordPackagesLengthIndex.length) ? wordPackagesLengthIndex[length + 1] - 1 : nextWordPackages.length - 1] = wordPackage;
			
			
			this.wordPackages = nextWordPackages;
		}
		
		public class WordDataPackage{
			private final Root_System.Word WORD;
			private Polynomial_Ring.Element determinant;
			private Root_System.Word[] decomposition;
			
			public WordDataPackage(Root_System.Word word) {
				if(word == null) {
					LOGGER.log(Level.SEVERE, "Illeagal Argument (null)");
					throw new IllegalArgumentException();
				}
				this.WORD = word.createReducedWord();
			}
			
			public Root_System.Word getWord(){
				return this.WORD;
			}
			
			public Polynomial_Ring.Element getDeterminant(){
				return this.determinant;
			}
			
			public Root_System.Word[] getDecomposition(){
				return this.decomposition;
			}
			
			public void setDeterminant(Polynomial_Ring.Element determinant) {
				this.determinant = determinant;
			}
			
			public void setDecomposition(Root_System.Word[] decomposition) {
				this.decomposition = decomposition;
			}
			
		}
	}
	
	@Override
	public String toString() {
		//Kopiere um keine Veränderungen durch andere Threads zu bekommen
		RootSystemDataPackage[] copyRootSystemPackages = this.rootSystemPackages.clone();
		String result = "SPEICHER:\n\n";
		result += "Wurzelsysteme: " + this.rootSystemPackages.length + " geladen.\n";
		int numberWords = 0;
		int numberLongestWords = 0;
		int numberDeterminants = 0;
		int numberDecompositions = 0;
		for(int i = 0; i < copyRootSystemPackages.length; i++) {
			numberWords += copyRootSystemPackages[i].getWordPackages().length;
			if(copyRootSystemPackages[i].getLongestWord() != null) numberLongestWords++;
			RootSystemDataPackage.WordDataPackage[] copyWordPackage = copyRootSystemPackages[i].getWordPackages().clone();
			for(int j = 0; j < copyWordPackage.length; j++) {
				if(copyWordPackage[j].getDeterminant() != null) numberDeterminants++;
				if(copyWordPackage[j].getDecomposition() != null) numberDecompositions++;
			}
		}
		result += "Wörter: " + numberWords + " geladen.\n";
		result += "Längste Wörter: " + numberLongestWords + " geladen.\n";
		result += "Determinanten: " + numberDeterminants + " geladen.\n";
		result += "Zerlegungen: " + numberDecompositions + " geladen.\n";
		return result;
	}
}
