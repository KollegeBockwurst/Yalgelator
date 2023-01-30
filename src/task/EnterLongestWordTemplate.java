package task;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import lie.Root_System;
import run.Memory;

public class EnterLongestWordTemplate extends TaskTemplate {
	private final static Logger LOGGER =  Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	public EnterLongestWordTemplate() {
		super("Längste Wörter eingeben");
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
			Memory.RootSystemDataPackage[] rSPackages = super.getMemory().getRootSystemPackages();
			JLabel label = new JLabel(EnterLongestWordTemplate.this.getName() + ": Bitte geben Sie die längsten Wörter der jeweiligen Wurzelsysteme an:");
			JLabel[] rSLabel = new JLabel[rSPackages.length];
			JTextField[] textFields = new JTextField[rSPackages.length];
			JPanel mainPanel = new JPanel();
			JPanel labelPanel = new JPanel();
			JPanel textPanel = new JPanel();
			
			mainPanel.setLayout(new BorderLayout());
			labelPanel.setLayout(new GridLayout(rSPackages.length, 1));
			textPanel.setLayout(new GridLayout(rSPackages.length, 1));
			
			mainPanel.add(label, BorderLayout.PAGE_START);
			mainPanel.add(labelPanel, BorderLayout.LINE_START);
			mainPanel.add(textPanel, BorderLayout.LINE_END);
			
			for(int i = 0; i < rSPackages.length; i++) {
				rSLabel[i] = new JLabel(rSPackages[i].getRootSystem().getNametag());
				textFields[i] = new JTextField();
				textFields[i].setColumns(50);
				Root_System.Word longestWord = rSPackages[i].getLongestWord();
				if(longestWord != null) {
					int[] reflections = longestWord.getReflections();
					String wordText = "";
					for(int j = 0; j < longestWord.getLength(); j++) {
						if(j > 0) wordText += " ";
						wordText += reflections[j];
					}
					textFields[i].setText(wordText);	
				}
				labelPanel.add(rSLabel[i]);
				textPanel.add(textFields[i]);
			}
			
			JOptionPane.showMessageDialog(null, mainPanel);
			if(super.getAbortFlag()) return;
			for(int i = 0; i < rSPackages.length; i++) {
				String input = textFields[i].getText();
				if(input.length() == 0) break;
				String[] split = input.split(" ");
				int[] reflections = new int[split.length];
				for(int j =0; j < reflections.length; j++) {
					try {
						reflections[j] = Integer.parseInt(split[j]);
					}
					catch(NumberFormatException e) {
						LOGGER.log(Level.INFO, "Das eingegebene Wort von WS " + rSPackages[i].getRootSystem().getNametag() + " konnte nicht geparsed werden.");
						break;
					}
				}
				Root_System.Word longestWord;
				try {
					longestWord = rSPackages[i].getRootSystem().new Word(reflections);
				}
				catch(IllegalArgumentException e) {
					LOGGER.log(Level.INFO, "Das eingegebene Wort von WS " + rSPackages[i].getRootSystem().getNametag() + " ist kein Wort in diesem WS.");
					break;
				}
				rSPackages[i].setLongestWord(longestWord);
			}
			super.setOrdinaryFinishFlag();
		}
		
	}
}
