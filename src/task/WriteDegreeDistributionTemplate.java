package task;

import run.Memory;

public class WriteDegreeDistributionTemplate extends TaskTemplate  {

	public WriteDegreeDistributionTemplate() {
		super("Schreibe Grad Verteilung", "Pfad", "degreeDistribution.txt");
	}

	@Override
	public Task createNewTask(String arg, Memory memory) {
		return new Task(arg, memory);
	}
	
	public class Task extends TaskTemplate.Task{

		public Task(String arg, Memory memory) {
			super(arg, memory);
		}

		@Override
		public String getProgress() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public void run() {
			
		}
	}

}
