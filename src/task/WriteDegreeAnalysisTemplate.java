package task;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

import lie.Root_System;
import run.Memory;

public class WriteDegreeAnalysisTemplate extends TaskTemplate{
	private final static Logger LOGGER =  Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	public WriteDegreeAnalysisTemplate() {
		super("Schreibe Gradanalyse","Pfad","degreeAnalysis.txt");
	}

	@Override
	public Task createNewTask(String arg, Memory memory) {
		return new Task(arg, memory);
	}
	
	private static String createSpaces(int n) {
		String result = "";
		for(int i = 0; i < n; i++) {
			result += " ";
		}
		return result;
	}
	
	public static String matchSpacing(String s, int n) {
		if(s.length() > n) return s.substring(0, n);
		return s + createSpaces(n - s.length());
	}
	
	public class Task extends TaskTemplate.Task{
		public Task(String arg, Memory memory) {
			super(arg, memory);
		}

		@Override
		public String getProgress() {
			return "";
		}
		
		@Override
		public void run() {
			
			try(BufferedWriter myWriter = Files.newBufferedWriter(new File(super.getArg()).toPath())){
				Memory.RootSystemDataPackage[] rootSystemPackages = super.getMemory().getRootSystemPackages();
				
				//Schleife, die die Wurzelsysteme durchgeht
				for(int i = 0; i < rootSystemPackages.length; i++) {
					
					Root_System rootSystem = rootSystemPackages[i].getRootSystem();
					Memory.RootSystemDataPackage.WordDataPackage[] wordPackages = rootSystemPackages[i].getWordPackages();
					myWriter.write("---- Wurzelsystem " + rootSystem.getNametag() + " ----");
					myWriter.newLine();
					myWriter.newLine();
					
					//Suche die Positionen, an denen die geraden Wörter beginnen:
					int[] startingPositions = new int[0];
					int rsSize = rootSystem.getSize();
					int lastLength = 0;
					int maxDetDegSpacing = 0;
					for(int j = 0; j < wordPackages.length; j++) {
						int length = wordPackages[j].getWord().getLength();
						if(length > 0 && length % 2 == 0 && (lastLength == 0 || length == lastLength +2)) {
							lastLength = length;
							int[] copy = startingPositions;
							startingPositions = new int[copy.length + 1];
							for(int k = 0; k < copy.length; k++) {
								startingPositions[k] = copy[k];
							}
							startingPositions[startingPositions.length - 1] = j;
						}
						int thisDegree = (wordPackages[j].getDeterminant() != null) ? wordPackages[j].getDeterminant().getDegree() : 0;
						int thisSpacing = (thisDegree > 1) ? (int) Math.log10(thisDegree - 1) + 1 : 1;
						if(thisDegree > maxDetDegSpacing) maxDetDegSpacing = thisSpacing;
					}
					int maxRootSpacing = (rsSize > 1) ? (int) Math.log10(rsSize - 1) + 1 : 1;
					
					int[] actualPositions = new int[0];
					//actualPositions[0] = startingPositions[0];
					
					int upCounter = 0;
					int downCounter = 0;
					int sameCounter = 0;
					
					
					while(true) {
						boolean marker = false; //Marker, ob nächstes Wort gefunden wurde
						
						int[] actualReflections = (actualPositions.length > 0) ? wordPackages[actualPositions[actualPositions.length - 1]].getWord().getReflections() : new int[0];
						
						for(int z = 0; z <= actualPositions.length; z++) {
							
							if(startingPositions.length > actualPositions.length - z) {
								for(int j = (z == 0) ? startingPositions[actualPositions.length - z] : actualPositions[actualPositions.length - z] + 1; j < wordPackages.length && wordPackages[j].getWord().getLength() == actualReflections.length + 2 - (2 * z); j++) {
									int[] thisReflections = wordPackages[j].getWord().getReflections();
									marker = true;
									for(int k = 0; k < actualReflections.length - (2*z); k++) {
										if(actualReflections[k] != thisReflections[k]) {
											marker = false;
											break;
										}
									}
									if(marker) {
										int[] copy = actualPositions;
										actualPositions = new int[copy.length - z + 1];
										for(int k = 0; k < copy.length - z; k++) {
											actualPositions[k] = copy[k];
										}
										actualPositions[actualPositions.length -1] = j;
										break;
									}
								}
								
								if(marker) {
									if(z > 0) {
										myWriter.newLine();
										myWriter.write(createSpaces((actualPositions.length - 1) * (maxDetDegSpacing + maxRootSpacing + maxRootSpacing + 7)-1));
									}
									int thisDeg = (wordPackages[actualPositions[actualPositions.length - 1]].getDeterminant() != null) ? wordPackages[actualPositions[actualPositions.length - 1]].getDeterminant().getDegree() : 0;
									if(actualPositions.length > 1) {
										int thatDeg = (wordPackages[actualPositions[actualPositions.length - 2]].getDeterminant() != null) ? wordPackages[actualPositions[actualPositions.length - 2]].getDeterminant().getDegree() : 0;
										if(thisDeg > thatDeg) {
											myWriter.write('\u2197');
											upCounter++;
										}
										else if(thisDeg < thatDeg) {
											myWriter.write('\u2198');
											downCounter++;
										}
										else{
											myWriter.write('\u2192');
											sameCounter++;
										}
									}
									int[] myReflections = wordPackages[actualPositions[actualPositions.length - 1]].getWord().getReflections();
									myWriter.write(" "+matchSpacing(String.valueOf(myReflections[myReflections.length - 2]), maxRootSpacing) + " " + matchSpacing(String.valueOf(myReflections[myReflections.length - 1]), maxRootSpacing)+ " ("+matchSpacing(String.valueOf(thisDeg), maxDetDegSpacing)+") ");
									
									break;
								}
								
								
								
								
							}
							
							
							if(marker) break;
							
						}
						
						if(!marker) break;
						
					}
					myWriter.newLine();
					myWriter.newLine();
					int sum = upCounter+downCounter+sameCounter;
					if(sum > 0) myWriter.write("Determinante erhöht: " + (upCounter*100/sum) + "%, Determinante gleich: " + (sameCounter * 100 / sum) + "%, Determinante verringert: " + (downCounter*100/sum) + "%");
					myWriter.newLine();
					myWriter.newLine();
				}
				LOGGER.log(Level.INFO, "Analyse erfolgreich erstellt.");
			
			} catch (IOException e) {
				LOGGER.log(Level.INFO, "I/O Exception: " + e.getMessage());
				return;
			}
			
			super.setOrdinaryFinishFlag();
			
		}
		
		
			
			
		
	}
}
