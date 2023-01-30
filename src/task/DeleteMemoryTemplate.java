package task;

import run.Memory;

public class DeleteMemoryTemplate extends TaskTemplate{

	public DeleteMemoryTemplate() {
		super("Lösche Speicher");
	}
	
	@Override
	public Task createNewTask(String arg, Memory memory) {
		return new Task(arg, memory);
	}
	
	private class Task extends TaskTemplate.Task{

		public Task(String arg, Memory memory) {
			super(arg, memory);
		}

		@Override
		public String getProgress() {
			return "";
		}
		
		@Override
		public void run() {
			//Resette Speicher
			super.getMemory().reset();
			super.setOrdinaryFinishFlag();
		}
		
	}	
}
