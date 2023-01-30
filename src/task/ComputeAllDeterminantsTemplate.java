package task;

import java.util.logging.Level;
import java.util.logging.Logger;

import algebra.Modular_Arithmetics;
import algebra.Polynomial_Ring;
import algebra.Ring;
import run.Memory;

public class ComputeAllDeterminantsTemplate extends TaskTemplate{
	private final static Logger LOGGER =  Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	public ComputeAllDeterminantsTemplate() {
		super("Berechne Determinanten", "Ring/Längenminimum oder Intervall, z.B. '7/3', '0/3-5'", "0");
	}

	@Override
	public Task createNewTask(String arg, Memory memory) {
		return new Task(arg, memory);
	}
	
	private class Task extends TaskTemplate.Task{
		String status = "";
		int numberOfCalculations = 0;
		int actualCalculation = 0;
		int start = 0;
		int stop = -1;
		Ring ring;
		Ring.Skew_Matrix.PfaffianComputer pfaffianComputer;
		
		
		public Task(String arg, Memory memory) {
			super(arg, memory);
			String[] slashSplit = arg.split("/");
			if(slashSplit.length == 0) {
				status = "Arg. fehlerhaft";
				return;
			}
			try {
				ring = new Modular_Arithmetics(Integer.parseInt(slashSplit[0]));
			}
			catch(NumberFormatException e) {
				status = "Arg. fehlerhaft";
				return;
			}
			if(slashSplit.length > 1 && slashSplit[1].length() > 0) {
				String[] split = slashSplit[1].split("-");
				try {
					start = (split[0].length() > 0) ? Integer.parseInt(split[0]) : 0;
					stop = (split.length > 1 && split[1].length() > 0) ? Integer.parseInt(split[1]) : -1; 
				}
				catch(NumberFormatException e) {
					status = "Arg. fehlerhaft";
					return;
				}
			}
		}
		
		@Override
		public void abort() {
			super.abort();
			pfaffianComputer.abort();
		}

		@Override //TODO
		public String getProgress() {
			if(this.numberOfCalculations > 0) {
				status = "Berechne " + actualCalculation+"/"+numberOfCalculations + ": ";
				Ring.Skew_Matrix.PfaffianComputer myPfaffianComputer = pfaffianComputer;
				if(myPfaffianComputer != null) status += myPfaffianComputer.getProgress();
			}
			return status;
		}
		
		@Override
		public void run() {
	
			
			Memory.RootSystemDataPackage[] rootSystemPackages = super.getMemory().getRootSystemPackages();
			for(int i = 0; i < rootSystemPackages.length; i++) {
				Memory.RootSystemDataPackage.WordDataPackage wordPackages[] = rootSystemPackages[i].getWordPackages();
				for(int j = 0; j < wordPackages.length; j++) {
					int length = wordPackages[j].getWord().getLength();
					if(length >= start && length %2 == 0 && (stop < 0 || length <= stop) && wordPackages[j].getDeterminant() == null) numberOfCalculations++;
				}
			}
			
			for(int i = 0; i < rootSystemPackages.length; i++) {
				Ring.Matrix M_B = rootSystemPackages[i].getRootSystem().getDefaultM_B(ring);
				Memory.RootSystemDataPackage.WordDataPackage wordPackages[] = rootSystemPackages[i].getWordPackages();
				for(int j = 0; j < wordPackages.length; j++) {
					int length = wordPackages[j].getWord().getLength();
					if(length >= start && (stop < 0 || length <= stop) && wordPackages[j].getDeterminant() == null) {
						if(length %2 == 0)actualCalculation++;
						Ring.Skew_Matrix smatrix = wordPackages[j].getWord().createAssoMatrix(M_B);
						pfaffianComputer = smatrix.getPfaffianComputer();
						pfaffianComputer.start();
						try {
							pfaffianComputer.join();
						} catch (InterruptedException e) {
							LOGGER.log(Level.FINE, "InterruptedException: " + e.getMessage());
							return;
						}
						if(super.getAbortFlag()) return;
						Ring.Element pfaffian = pfaffianComputer.getResult();
						Ring.Element determinant = pfaffian.createProduct(pfaffian);
						wordPackages[j].setDeterminant((Polynomial_Ring.Element)determinant);
					}
				}
			}
			super.setOrdinaryFinishFlag();
			status="";
		}
	}
	
}
