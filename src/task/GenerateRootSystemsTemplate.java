package task;

import lie.Root_System;
import run.Memory;

public class GenerateRootSystemsTemplate extends TaskTemplate{

	public GenerateRootSystemsTemplate() {
		super("Generiere Wurzelsystem(e)", "Angabe Wurzelsysteme", "A4-7");
	}
	
	public static Root_System createB_n(int n) {
		int[][] cartan = new int[n][n];
		for(int i = 0; i < n; i++) {
			if(i > 0) cartan[i][i-1] = (i == n-1) ? -2 : -1;
			cartan[i][i] = 2;
			if(i + 1 < n) cartan[i][i+1] = -1;
		}
		return new Root_System(cartan, "B_"+n);
	}
	
	public static Root_System createA_n(int n) {
		int[][] cartan = new int[n][n];
		for(int i = 0; i < n; i++) {
			if(i > 0) cartan[i][i-1] = -1;
			cartan[i][i] = 2;
			if(i + 1 < n) cartan[i][i+1] = -1;
		}
		return new Root_System(cartan, "A_"+n);
	}
	
	public static Root_System createC_n(int n) {
		int[][] cartan = new int[n][n];
		for(int i = 0; i < n; i++) {
			if(i > 0) cartan[i][i-1] = -1;
			cartan[i][i] = 2;
			if(i + 1 < n) cartan[i][i+1] = (i == n-2) ? -2 : -1;
		}
		return new Root_System(cartan, "C_"+n);
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
			if(arg == null || arg.length() < 2) {
				status = "Arg. fehlerhaft.";
				return;
			}
			char rsChar = arg.charAt(0);
			String range = arg.substring(1);
			String[] split = range.split("-");
			if(split.length < 1) {
				status = "Arg. fehlerhaft.";
				return;
			}
			int start, stop;
			try {
				start = Integer.parseInt(split[0]);
				stop = (split.length > 1) ? Integer.parseInt(split[1]) : start;
			}
			catch(NumberFormatException e) {
				status = "Arg. fehlerhaft.";
				return;
			}
			
			switch(rsChar) {
			case 'A':
				for(int i = start; i <= stop; i++) {
					super.getMemory().addRootSystem(createA_n(i));
				}
				break;
			case 'B':
				for(int i = start; i <= stop; i++) {
					super.getMemory().addRootSystem(createB_n(i));
				}
				break;
			case 'C':
				for(int i = start; i <= stop; i++) {
					super.getMemory().addRootSystem(createC_n(i));
				}
				break;
			default:
				status = "WS unbekannt.";
				return;
			}
			
			super.setOrdinaryFinishFlag();;
		}
		
	}
	
}
