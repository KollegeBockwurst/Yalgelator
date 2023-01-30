package task;

import lie.Root_System;
import run.Memory;
import run.Memory.RootSystemDataPackage.WordDataPackage;

public class ComputeWordsFromDecompTemplate2 extends TaskTemplate {

	public ComputeWordsFromDecompTemplate2() {
		super("Berechne Wörter aus Spiegelungen", "Intervall: Anzahl an Spiegelungen", "");
		// TODO Auto-generated constructor stub
	}

	@Override
	public Task createNewTask(String arg, Memory memory) {
		return new Task(arg, memory);
	}

	public class Task extends TaskTemplate.Task{
		String status = "";
		public Task(String arg, Memory memory) {
			super(arg, memory);
		}

		@Override
		public String getProgress() {
			return status;
		}
		
		@Override 
		public void run() {
			String arg = super.getArg();
			int stop = -1;
			if(arg != null && arg.length() > 0) {
				try {
					
					stop = Integer.parseInt(arg); 
				}
				catch(NumberFormatException e) {
					status = "Arg. fehlerhaft";
					return;
				}
			}
			
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
				
				Memory.RootSystemDataPackage.WordDataPackage[] wordPackages = new Memory.RootSystemDataPackage.WordDataPackage[1];
				wordPackages[0] = rootSystemPackages[i].new WordDataPackage(rootSystemPackages[i].getRootSystem().new Word(new int[0]));
				wordPackages[0].setDecomposition(new Root_System.Word[0]);
				Memory.RootSystemDataPackage.WordDataPackage[] lastWordPackages = new Memory.RootSystemDataPackage.WordDataPackage[1];
				lastWordPackages[0] = wordPackages[0];
				wordPackages[0].setDecomposition(new Root_System.Word[0]);
				for(int j = 1; j <= stop || stop == -1; j++) {
					WordDataPackage[] newWordPackages = new WordDataPackage[0];
					boolean foundWord = false;
					for(int k = 0; k < lastWordPackages.length; k++) {
						for(int h = 0; h < decompositionWords.length; h++) {
							Root_System.Word word = lastWordPackages[k].getWord().createLink(decompositionWords[h]).createReducedWord();
							boolean alreadyHere = false;
							for(int z = 0; z < wordPackages.length; z++) {
								if(word.equalsAsReflection(wordPackages[z].getWord())) {
									alreadyHere = true;
									break;
								}
							}
							if(!alreadyHere) {
								foundWord = true;
								WordDataPackage[] nextNewWordPackages = new WordDataPackage[newWordPackages.length + 1];
								WordDataPackage[] nextWordPackages = new WordDataPackage[wordPackages.length + 1];
								for(int z = 0; z < wordPackages.length; z++) {
									nextWordPackages[z] = wordPackages[z];
								}
								nextWordPackages[nextWordPackages.length - 1] = rootSystemPackages[i].new WordDataPackage(word);
								for(int z = 0; z < newWordPackages.length; z++) {
									nextNewWordPackages[z] = newWordPackages[z];
								}
								nextNewWordPackages[nextNewWordPackages.length - 1] = nextWordPackages[nextWordPackages.length - 1];
								Root_System.Word[] oldDecomp = lastWordPackages[k].getDecomposition();
								Root_System.Word[] decomp = new Root_System.Word[oldDecomp.length + 1];
								for(int z = 0; z < oldDecomp.length; z++) {
									decomp[z + 1] = oldDecomp[z];
								}
								decomp[0] = decompositionWords[h];
								nextWordPackages[nextWordPackages.length - 1].setDecomposition(decomp);
								wordPackages = nextWordPackages;
								newWordPackages = nextNewWordPackages;
							}
						}
					}
					if(!foundWord) break;
					lastWordPackages = newWordPackages;
				}
				rootSystemPackages[i].addWordPackages(wordPackages);
			}
			super.setOrdinaryFinishFlag();
			status = "";
		}
		
	}
}
