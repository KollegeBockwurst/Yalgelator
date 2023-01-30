package run;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;

import algebra.Ring;
import log.LogAreaHandler;
import task.*;

public class Window extends JFrame implements ActionListener{

	private static final long serialVersionUID = 4066566609685505309L;

	//Log
	private final static Logger LOGGER =  Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	
	//Memory
	private Memory memory = new Memory();
	
	
	//Task variables
	private TaskWorker taskWorker = null;
	private final TaskTemplate[] TASK_TEMPLATES;
	
	//Swing Elements
	private JTextArea statusArea = new JTextArea();
	private final JTextArea LOG_AREA;
	private JCheckBox[] checkBoxes;
	private JTextField[] taskOptionTexts;
	private JButton addTasksButton;
	private JButton deleteTasksButton;
	private JButton startTasksButton;
	private JButton abortTaskButton;
	
	
	public TaskTemplate[] createTemplateRegistration() {
		//Hier Aufgaben registrieren
		TaskTemplate[] templates = new TaskTemplate[10];
		templates[0] = new DeleteMemoryTemplate();
		templates[1] = new LoadRootSystemsTemplate();
		templates[2] = new GenerateRootSystemsTemplate();
		templates[3] = new EnterLongestWordTemplate();
		templates[4] = new ComputeAllWordsTemplate();
		templates[5] = new ComputeWordsFromDecompTemplate();
		templates[6] = new ComputeAllDeterminantsTemplate();
		templates[7] = new ComputeAllDecompositionsTemplate();
		templates[8] = new WriteDataTemplate();
		templates[9] = new WriteDegreeAnalysisTemplate();
		return templates;
	}
	
	public Window(String version, JTextArea logArea) {
		//Neues JFrame erstellen
		super("Yalgator v."+version);
		
		//Swing unabhängige Einstellungen
		this.TASK_TEMPLATES = this.createTemplateRegistration();
		
		//Benötigte Panel erstellen
		JPanel tasksPanel = new JPanel();
		JPanel checkBoxPanel = new JPanel();
		JPanel buttonPanel = new JPanel();
		JPanel textFieldPanel = new JPanel();
		JPanel labelAndCBPanel = new JPanel();
		JPanel checkAndTextPanel = new JPanel();
		
		//Benötigte scrollPane erstellen, TextAreas erstellen
		JScrollPane statusPane = new JScrollPane(statusArea);
		JScrollPane logPane = new JScrollPane(logArea);
		this.LOG_AREA = logArea;
		//Layouts setzen
		this.setLayout(new BorderLayout());
		tasksPanel.setLayout(new BorderLayout());
		labelAndCBPanel.setLayout(new BorderLayout());
		checkAndTextPanel.setLayout(new BorderLayout());
		checkBoxPanel.setLayout(new GridLayout(TASK_TEMPLATES.length, 1));
		buttonPanel.setLayout(new GridLayout(4,1));
		textFieldPanel.setLayout(new GridLayout(TASK_TEMPLATES.length, 1));
		
		//Container zu Containern hinzufügen
		this.add(statusPane, BorderLayout.CENTER);
		this.add(tasksPanel, BorderLayout.LINE_END);
		this.add(logPane, BorderLayout.PAGE_END);
		tasksPanel.add(labelAndCBPanel, BorderLayout.PAGE_START);
		labelAndCBPanel.add(checkAndTextPanel, BorderLayout.PAGE_END);
		checkAndTextPanel.add(checkBoxPanel, BorderLayout.LINE_START);
		checkAndTextPanel.add(textFieldPanel, BorderLayout.LINE_END);
		tasksPanel.add(buttonPanel, BorderLayout.PAGE_END);
		
		//Elemente erstellen und hinzufügen
		//Label
		JLabel tasksLabel = new JLabel("Aufgabe hinzufügen:");
		labelAndCBPanel.add(tasksLabel, BorderLayout.PAGE_START);
		//Buttons
		addTasksButton = new JButton("Aufgaben einreihen");
		buttonPanel.add(addTasksButton);
		deleteTasksButton = new JButton("Aufgaben löschen");
		buttonPanel.add(deleteTasksButton);
		startTasksButton = new JButton("Aufgaben starten");
		buttonPanel.add(startTasksButton);
		abortTaskButton = new JButton("Aktuelle Aufgabe abbrechen");
		buttonPanel.add(abortTaskButton);
		//CheckBoxes und Textfields
		checkBoxes = new JCheckBox[TASK_TEMPLATES.length];
		taskOptionTexts = new JTextField[TASK_TEMPLATES.length];
		for(int i = 0; i < TASK_TEMPLATES.length; i++) {
			checkBoxes[i] = new JCheckBox(TASK_TEMPLATES[i].getName());
			taskOptionTexts[i] = new JTextField((TASK_TEMPLATES[i].getOptionDefault() == null) ? "" : TASK_TEMPLATES[i].getOptionDefault());
			taskOptionTexts[i].setToolTipText((TASK_TEMPLATES[i].getOptionDescription() == null) ? "":TASK_TEMPLATES[i].getOptionDescription());
			taskOptionTexts[i].setVisible(TASK_TEMPLATES[i].getOptionDefault() != null);
			taskOptionTexts[i].setColumns(15);
			checkBoxPanel.add(checkBoxes[i]);
			textFieldPanel.add(taskOptionTexts[i]);
		}
		
		//Einstellungen
		this.setSize(900, 700);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		logArea.setRows(15);
		logArea.setToolTipText("Log Ausgabe");
		logArea.setLineWrap(true);
		statusArea.setLineWrap(true);
		statusArea.setToolTipText("Status Anzeige");
		logArea.setEditable(false);
		statusArea.setEditable(false);
		addTasksButton.addActionListener(this);
		deleteTasksButton.addActionListener(this);
		startTasksButton.addActionListener(this);
		abortTaskButton.addActionListener(this);
		updateStatusArea();
		//Fenster zeigen
		this.setVisible(true);
		LOGGER.log(Level.FINEST, "Window created.");
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == this.addTasksButton) {
			addTasks();
		}
		else if(e.getSource() == this.deleteTasksButton) {
			deleteTasks();
		}
		else if(e.getSource() == this.startTasksButton) {
			startTasks();
		}
		else if(e.getSource() == this.abortTaskButton) {
			abortTask();
		}
	}
	
	public void updateStatusArea() {
		String statusAreaText = memory.toString();
		if(taskWorker != null) {
			statusAreaText += "\n\nAUFGABEN:\n\n";
			statusAreaText += taskWorker;
		}
		statusArea.setText(statusAreaText);
	}
	
	private void addTasks() {
		LOGGER.log(Level.FINEST, "Add Tasks button pressed.");
		for(int i = 0; i < checkBoxes.length; i++) {
			if(checkBoxes[i].isSelected()) {
				if(taskWorker == null) taskWorker = new TaskWorker();
				taskWorker.addTask(TASK_TEMPLATES[i].createNewTask(taskOptionTexts[i].getText(), memory));
				checkBoxes[i].setSelected(false);
			}
		}
		updateStatusArea();
	}
	
	private void deleteTasks() {
		LOGGER.log(Level.FINEST, "Delete Tasks button pressed.");
		if(taskWorker != null) {
			taskWorker.abort();
			try {
				taskWorker.join();
			} catch (InterruptedException e) {
				LOGGER.log(Level.FINE, "Interrupted Exception: " + e.getMessage());
			}
			taskWorker = null;
		}
		updateStatusArea();
		LOG_AREA.setText("");
	}
	
	private void startTasks() {
		LOGGER.log(Level.FINEST, "Start Tasks button pressed.");
		if(taskWorker != null) {
			if(!taskWorker.isAlive()) taskWorker.start();
			else taskWorker.setStartAbility(true);
		}
	}
	
	private void abortTask() {
		LOGGER.log(Level.FINEST, "Abort Task button pressed.");
		if(taskWorker != null) {
			taskWorker.abortActualTask();
		}
		updateStatusArea();
	}
	
 	public static void main(String[] args) {
 		LogAreaHandler logHandler = new LogAreaHandler();
 		logHandler.setLevel(Level.INFO);
 		LOGGER.addHandler(logHandler);
 		
 		try {
			Handler handler = new FileHandler("log.txt");
			handler.setLevel(Level.FINEST);
			LOGGER.addHandler(handler);
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Failed to initialize FileHandler of Logger.");
		}
		LOGGER.setLevel(Level.FINEST);
		LOGGER.log(Level.CONFIG, "Thread number: " + String.valueOf(Ring.THREAD_NUMBER));
		new Window("1.2", logHandler.getLogArea());
	}
	
	private class TaskWorker extends Thread{
		private TaskTemplate.Task[] myTasks = new TaskTemplate.Task[0];
		private final Object pauseLock = new Object();
		private boolean abortFlag = false; 
		int actualTask = 0;
		boolean startTasks = false;
		
		public void abort() {
			abortFlag = true;
			for(int i = actualTask; i < myTasks.length; i++) {
				myTasks[i].abort();
			}
			synchronized (pauseLock) {
				pauseLock.notifyAll();
			}
		}
		
		public void addTask(TaskTemplate.Task task) {
			TaskTemplate.Task[] newMyTasks = new TaskTemplate.Task[myTasks.length + 1];
			for(int i = 0; i < myTasks.length; i++) {
				newMyTasks[i] = myTasks[i];
			}
			newMyTasks[newMyTasks.length - 1] = task;
			myTasks = newMyTasks;
		}
		
		public void abortActualTask() {
			if(actualTask < myTasks.length) {
				myTasks[actualTask].abort();
			}
		}
		
		public void setStartAbility(boolean startTasks) {
			this.startTasks = startTasks;
			synchronized(pauseLock) {
				pauseLock.notifyAll();
			}
		}
		
		@Override
		public String toString() {
			String result = "";
			for(int i = 0; i < myTasks.length; i++) {
				result += myTasks[i].toString() + "\n";
			}
			return result;
		}
		
		@Override
		public void start() {
			if(taskWorker != null && taskWorker != this) return;
			taskWorker = this;
			this.setStartAbility(true);
			super.start();
		}
		
		@Override
		public void run() {
			while(!abortFlag) {
				TaskTemplate.Task[] myTasksCopy = myTasks.clone();
				for(int i = actualTask; i < myTasksCopy.length && startTasks; i++) {
					if(abortFlag) return;
					myTasksCopy[i].start();
					while(myTasksCopy[i].isAlive()){
						try {
							myTasksCopy[i].join(200);
						} catch (InterruptedException e) {
							LOGGER.log(Level.FINE, "Interrupted Exception: " + e.getMessage());
						}
						updateStatusArea();
					}
					actualTask = i+1;
				}
				this.setStartAbility(false);
				synchronized(pauseLock) {
					try {
						pauseLock.wait();
					} catch (InterruptedException e) {
						LOGGER.log(Level.FINE, "Interrupted Exception " + e.getMessage());
					}
				}
			}
			
		}
	}
	
	
	
	
	
}
