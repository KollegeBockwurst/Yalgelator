package task;

import java.util.logging.Level;
import java.util.logging.Logger;

import lie.Root_System;
import run.Memory;

public class ComputeAllWordsTemplate extends TaskTemplate{
	private final static Logger LOGGER =  Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	
	public ComputeAllWordsTemplate() {
		super("Berechne alle Wörter", "max. Länge", "");
	}
	
	@Override
	public Task createNewTask(String arg, Memory memory) {
		return new Task(arg,memory);
	}
	
	private class Task extends TaskTemplate.Task{
		private String status = "";
		private Root_System.AllWordComputer allWordComputer; 
		private int actualRS = 0;
		private int numberRS = 0;
		public Task(String arg, Memory memory) {
			super(arg, memory);
		}
		
		@Override
		public void abort() {
			super.abort();
			if(allWordComputer != null) allWordComputer.abort();
		}

		@Override
		public String getProgress() {
			if(actualRS > 0) {
				if(!super.isFinished() || super.isAborted()) {
					status = "Berechne WS " + actualRS + "/" + numberRS +": ";
					Root_System.AllWordComputer myAllWordComputer = allWordComputer;
					if(myAllWordComputer != null) status += myAllWordComputer.getProgress();
				}
				else status = "";
			}
			return status;
		}
		
		@Override
		public void run() {
			int maxLength = -1;
			if(super.getArg() != null && super.getArg().length() > 0) {
				try {
					maxLength = Integer.parseInt(super.getArg());
				}
				catch(NumberFormatException e) {
					status = "Arg. fehlerhaft";
					return;
				}
			}
			Memory.RootSystemDataPackage[] rootSystemPackages = super.getMemory().getRootSystemPackages();
			numberRS = rootSystemPackages.length;
			for(int i = 0; i < rootSystemPackages.length; i++) {
				if(super.getAbortFlag()) return;
				actualRS = i+1;
				
				allWordComputer = rootSystemPackages[i].getRootSystem().createAllWordComputer(maxLength);
				allWordComputer.start();
				try {
					allWordComputer.join();
				} catch (InterruptedException e) {
					LOGGER.log(Level.FINE, "Interrupted Exception: " + e.getMessage());
					return;
				}
				Root_System.Word[] allWords;
				allWords = allWordComputer.getComputedWords();
				if(allWords == null) {
					LOGGER.log(Level.WARNING, "Can't compute words of root system " + rootSystemPackages[i].getRootSystem().getNametag());
					break;
				}
				rootSystemPackages[i].addWords(allWords);
				
				if(maxLength == -1 && allWordComputer.hasOrdinaryFinished()) {
					rootSystemPackages[i].setLongestWord(allWords[allWords.length - 1]);
				}
				
				if(super.getAbortFlag()) return;
			}
			
			status = "";
			super.setOrdinaryFinishFlag();
		}
	}
	
}
