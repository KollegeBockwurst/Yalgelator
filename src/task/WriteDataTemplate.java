package task;

import java.awt.GridLayout;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import algebra.Polynomial_Ring;
import lie.Root_System;
import run.Memory;

public class WriteDataTemplate extends TaskTemplate {
	private final static Logger LOGGER =  Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	public WriteDataTemplate() {
		super("Schreibe Daten","Pfad","result.txt");
	}

	@Override
	public Task createNewTask(String arg, Memory memory) {
		return new Task(arg, memory);
	}
	
	public class Task extends TaskTemplate.Task{
		private final File resultFile;
		JCheckBox box1 = new JCheckBox("Drucke ungerade Wörter");
		JCheckBox box2 = new JCheckBox("Drucke vollständige Determinanten");
		JCheckBox box3 = new JCheckBox("Drucke vollständige Zerlegung");
		
		public Task(String arg, Memory memory) {
			super(arg, memory);
			resultFile = new File(arg);
			JPanel al = new JPanel();
			al.setLayout(new GridLayout(4,1));
			JLabel label = new JLabel(WriteDataTemplate.this.getName() + ": Welche Ausgabeoptionen sollen verwendet werden?");
			al.add(label);
			al.add(box1);
			al.add(box2);
			al.add(box3);
			
			JOptionPane.showMessageDialog(null, al);
		}

		@Override
		public String getProgress() {
			return "";
		}
		
		@Override public void run() {
			try(BufferedWriter myWriter = Files.newBufferedWriter(resultFile.toPath())){
				Memory.RootSystemDataPackage[] rootSystemPackages = super.getMemory().getRootSystemPackages();
				myWriter.append("Ergebnis der Berechnungen für " + rootSystemPackages.length + " Wurzelsysteme:");
				myWriter.newLine();
				myWriter.newLine();
				for(int i = 0; i < rootSystemPackages.length; i++) {
					myWriter.newLine();
					myWriter.write("--- Wurzelsystem "+ rootSystemPackages[i].getRootSystem().getNametag() + " ---");
					myWriter.newLine();
					int actualSize = -1;
					Memory.RootSystemDataPackage.WordDataPackage[] wordPackages= rootSystemPackages[i].getWordPackages();
					for(int j = 0; j < wordPackages.length; j++) {
						Root_System.Word word = wordPackages[j].getWord();
						if(!box1.isSelected() && word.getLength() % 2 != 0) continue;
						if(word.getLength() != actualSize) {
							actualSize = word.getLength();
							myWriter.newLine();
							myWriter.write("Wörter der Länge " + actualSize);
							myWriter.newLine();
						}
						myWriter.write(word.toString() + " ");
						Polynomial_Ring.Element determinant = wordPackages[j].getDeterminant();
						if(determinant != null) {
							myWriter.write("DetGrad: " + determinant.getDegree() + " " + ((box2.isSelected()) ? ("Det: " + determinant + " "):""));
						}
						Root_System.Word[] decomposition = wordPackages[j].getDecomposition();
						if(decomposition != null) {
							myWriter.write("ZerlegungGrad: " + decomposition.length + " ");
							if(box3.isSelected()) {
								myWriter.write("Zerlegung: ");
								for(int k = 0; k < decomposition.length; k++) {
									myWriter.write(decomposition[k].toString());
									if(k < decomposition.length - 1) myWriter.write(" " + '\u2218' + " ");
								}
							}
						}
						myWriter.newLine();
					}
				}
				myWriter.newLine();
				LOGGER.log(Level.INFO, "Wrote result file successfully.");
			}
			catch(IOException e) {
				LOGGER.log(Level.INFO, "Exception while trying to create a result file: " + e.getMessage());
				return;
			}
			
			super.setOrdinaryFinishFlag();
			
			
		}
	}
}
