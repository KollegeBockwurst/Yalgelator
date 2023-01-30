package task;

import lie.Root_System;
import run.Memory;
import run.Memory.RootSystemDataPackage.WordDataPackage;

public class ComputeWordsFromDecompTemplate extends TaskTemplate {
	public ComputeWordsFromDecompTemplate() {
		super("Berechne Wörter aus Spiegelungen", "Max. Anzahl an Spiegelungen", "");
	}

	@Override
	public Task createNewTask(String arg, Memory memory) {
		return new Task(arg, memory);
		
	}

	public class Task extends TaskTemplate.Task{
		int stop = -1;
		String status = "";
		public Task(String arg, Memory memory) {
			super(arg, memory);
			if(arg != null && arg.length() > 0) {
				try {
					stop = Integer.parseInt(arg); 
				}
				catch(NumberFormatException e) {
					status = "Arg. fehlerhaft";
					super.abort();
				}
			}
		}

		@Override
		public String getProgress() {
			return status;
		}
		
		@Override 
		public void run() {
			
			
			
			Memory.RootSystemDataPackage[] rootSystemPackages = super.getMemory().getRootSystemPackages();
			
			
			for(int i = 0; i < rootSystemPackages.length; i++) {
				Root_System.Word longestWord = rootSystemPackages[i].getLongestWord();
				if(longestWord == null) break;
				Root_System.Word[] decompositionWords = new Root_System.Word[longestWord.getLength()];
				int[] longestReflections = longestWord.getReflections();
				for(int j = 0; j  < decompositionWords.length; j++) {
					int[] array = new int[2*j+1];
					array[j] = longestReflections[j];
					for(int k = 0; k < j; k++) {
						array[k] = longestReflections[k];
						array[array.length - 1 - k] = longestReflections[k];
					}
					decompositionWords[j] = (rootSystemPackages[i].getRootSystem().new Word(array)).createReducedWord();
				}
				
				if(rootSystemPackages[i].getWordPackages(0).length < 1) rootSystemPackages[i].addWord(rootSystemPackages[i].getRootSystem().new Word(new int[0]));
				WordDataPackage[] lastWordPackages = new Memory.RootSystemDataPackage.WordDataPackage[1];
				lastWordPackages[0] = rootSystemPackages[i].new WordDataPackage(rootSystemPackages[i].getRootSystem().new Word(new int[0]));
				lastWordPackages[0].setDecomposition(new Root_System.Word[0]);
				int myStop = (stop == -1 || stop > rootSystemPackages[i].getRootSystem().getSize()) ? rootSystemPackages[i].getRootSystem().getSize() : stop;
				for(int j = 1; j <= myStop; j++) {
					status = "Berechne WS " + rootSystemPackages[i].getRootSystem().getNametag() + " Zerlegungen der Länge " + j;
					WordDataPackage[] newWordPackages = new WordDataPackage[0];
					boolean foundWord = false;
					for(int k = 0; k < lastWordPackages.length; k++) {
						for(int h = 0; h < decompositionWords.length; h++) {
							Root_System.Word word = lastWordPackages[k].getWord().createLink(decompositionWords[h]).createReducedWord();
							WordDataPackage alreadyExistent = null;
							WordDataPackage[] possibleWords = rootSystemPackages[i].getWordPackages(word.getLength());
							for(int z = 0; z < possibleWords.length; z++) {
								if(super.getAbortFlag()) return;
								if(word.equalsAsReflection(possibleWords[z].getWord())) {
									alreadyExistent = possibleWords[z];
									if(alreadyExistent.getWord().getLength() == 0) {
										alreadyExistent.setDecomposition(new Root_System.Word[0]);
									}
									break;
								}
							}
							if(alreadyExistent == null) {
								alreadyExistent = rootSystemPackages[i].new WordDataPackage(word);
								rootSystemPackages[i].addWordPackage(alreadyExistent);
							}
							if(alreadyExistent.getDecomposition() == null) {
								foundWord = true;
								WordDataPackage[] nextNewWordPackages = new WordDataPackage[newWordPackages.length + 1];
								for(int z = 0; z < newWordPackages.length; z++) {
									nextNewWordPackages[z] = newWordPackages[z];
								}
								nextNewWordPackages[nextNewWordPackages.length - 1] = alreadyExistent;
								newWordPackages = nextNewWordPackages;
								Root_System.Word[] oldDecomp = lastWordPackages[k].getDecomposition();
								Root_System.Word[] decomp = new Root_System.Word[oldDecomp.length + 1];
								for(int z = 0; z < oldDecomp.length; z++) {
									decomp[z + 1] = oldDecomp[z];
								}
								decomp[0] = decompositionWords[h];
								alreadyExistent.setDecomposition(decomp);
							}
							
						}
						if(super.getAbortFlag()) return;
					}
					if(!foundWord) break;
					lastWordPackages = newWordPackages;
					
				}
			}
			super.setOrdinaryFinishFlag();
			status = "";
		}
		
	}
}
