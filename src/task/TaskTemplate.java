package task;

import run.Memory;

public abstract class TaskTemplate{
		private final String NAME;
		private final String OPTION_DESCRIPTION;
		private final String OPTION_DEFAULT;
		
		public TaskTemplate(String name) {
			this(name, null, null);
		}
		
		public TaskTemplate(String name, String optionDescription, String optionDefault) {
			this.NAME = name;
			this.OPTION_DESCRIPTION = optionDescription;
			this.OPTION_DEFAULT = optionDefault;
		}
		
		public String getName() {
			return this.NAME;
		}
		
		public String getOptionDescription() {
			return this.OPTION_DESCRIPTION;
		}
		
		public String getOptionDefault() {
			return this.OPTION_DEFAULT;
		}
		
		public abstract Task createNewTask(String arg, Memory memory);
		
		public abstract class Task extends Thread{
			private final String ARG;
			private final Memory MEMORY;
			
			private boolean hasStarted = false;
			private boolean ordinaryFinishFlag = false;
			private boolean abortFlag = false;
			
			public String getArg() {
				return this.ARG;
			}
			
			public Task(String arg, Memory memory) {
				this.ARG = arg;
				this.MEMORY = memory;
			}
			
			public final Memory getMemory() {
				return this.MEMORY;
			}
			
			public final boolean isStarted() {
				return this.hasStarted;
			}
			
			public final boolean isFinished() {
				return !this.isAlive() && this.hasStarted;
			}
			
			public final boolean isAborted() {
				return (this.abortFlag && !this.isAlive()) && !this.ordinaryFinishFlag|| (this.isFinished() && !this.ordinaryFinishFlag); //SoftAbort or HardAbort
			}
			
			public void abort() {
				if(!this.ordinaryFinishFlag) this.abortFlag = true;
			}
			
			public boolean getAbortFlag() {
				return this.abortFlag;
			}

			protected void setOrdinaryFinishFlag() {
				this.ordinaryFinishFlag = true;
			}
			
			public abstract String getProgress();
			
			@Override
			public final String toString() {
				String result = "";
				if(this.isAborted()) result += '\u2020'; //abortedFinish
				else if(this.isFinished()) result += '\u2713'; //ordinaryFinish
				else if(this.isStarted()) result += "-->"; //working
				result += TaskTemplate.this.getName();
				if(this.getArg() != null && this.getArg().length() > 0) {
					result += " [" + this.getArg() + "] ";
				}
				result += " " + this.getProgress();
				return result;
			}
			
			@Override
			public final void start() {
				if(!this.abortFlag)super.start();
				this.hasStarted = true;
			}
		}
	}