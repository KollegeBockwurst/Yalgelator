package task;

import java.util.logging.Level;
import java.util.logging.Logger;
import lie.Root_System;
import run.Memory;

public class ComputeAllDecompositionsTemplate extends TaskTemplate{
	private final static Logger LOGGER =  Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	public ComputeAllDecompositionsTemplate() {
		super("Berechne Zerlegungen","Intervall, z.B. 3-5","");
	}

	@Override
	public Task createNewTask(String arg, Memory memory) {
		return new Task(arg,memory);
	}
	
	public class Task extends TaskTemplate.Task{

		String status = "";
		public Task(String arg, Memory memory) {
			super(arg, memory);
			LOGGER.log(Level.WARNING,ComputeAllDecompositionsTemplate.this.getName() + " sollte nicht verwendet werden.");
		}

		@Override
		public String getProgress() {
			return status;
		}
		
		@Override
		public void run() {
			String arg = super.getArg();
			int start = 0;
			int stop = -1;
			if(arg != null && arg.length() > 0) {
				String[] split = arg.split("-");
				try {
					start = (split[0].length() > 0) ? Integer.parseInt(split[0]) : 0;
					stop = (split.length > 1 && split[1].length() > 0) ? Integer.parseInt(split[1]) : -1; 
				}
				catch(NumberFormatException e) {
					status = "Arg. fehlerhaft";
					return;
				}
			}
			
			int numberOfCalculations = 0;
			Memory.RootSystemDataPackage[] rootSystemPackages = super.getMemory().getRootSystemPackages();
			for(int i = 0; i < rootSystemPackages.length; i++) {
				if(rootSystemPackages[i].getLongestWord() == null) break;
				Memory.RootSystemDataPackage.WordDataPackage wordPackages[] = rootSystemPackages[i].getWordPackages();
				for(int j = 0; j < wordPackages.length; j++) {
					int length = wordPackages[j].getWord().getLength();
					if(length >= start  && (stop < 0 || start <= stop) && wordPackages[j].getDecomposition() == null) numberOfCalculations++;
				}
			}
			int actualCalculation = 0;
			for(int i = 0; i < rootSystemPackages.length; i++) {

				if(rootSystemPackages[i].getLongestWord() == null) break;
				Root_System.Word longestWord = rootSystemPackages[i].getLongestWord();
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
				
				
				Memory.RootSystemDataPackage.WordDataPackage wordPackages[] = rootSystemPackages[i].getWordPackages();
				for(int j = 0; j < wordPackages.length; j++) {
					if(super.getAbortFlag()) {
						status = "Berechnet: " + actualCalculation + "/" + numberOfCalculations;
						return;
					}
					int length = wordPackages[j].getWord().getLength();
					if(length >= start && (stop < 0 || start <= stop) && wordPackages[j].getDecomposition() == null) {
						actualCalculation++;
						status = "Berechne " + actualCalculation + "/" + numberOfCalculations;
						Root_System.Word[] decomp = wordPackages[j].getWord().getMinimalDecomposition(decompositionWords);
						if(decomp != null) wordPackages[j].setDecomposition(decomp);
					}
				}
			}
			super.setOrdinaryFinishFlag();
			status = "";
			
		}
		
	}
	
}
