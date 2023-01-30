package task;

import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import lie.Root_System;
import run.Memory;

public class LoadRootSystemsTemplate extends TaskTemplate{
	public LoadRootSystemsTemplate() {
		super("Lade Wurzelsysteme.", "Pfad", "input.txt");
	}

	@Override
	public Task createNewTask(String arg, Memory memory) {
		return new Task(arg, memory);
	}
	
	private class Task extends TaskTemplate.Task{

		private String status = "";
		private Root_System[] addRootSystems = new Root_System[0];
		
		public Task(String arg, Memory memory) {
			super(arg, memory);
			File inputFile = new File(super.getArg());
			if(!inputFile.exists()) {
				this.status = "Datei existiert nicht.";
				return;
			}
			
			Root_System[] rootSystems = new Root_System[0];
			
			try(BufferedReader myReader = new BufferedReader(new FileReader(inputFile))) {
				//Lese erste Zeile
				String line = myReader.readLine();
				//Verwerfe alle Zeilen bis zur ersten Tilde:
				while(line != null && (line.length() == 0 || line.charAt(0) != '~')) {
					line = myReader.readLine();
				}
				line = myReader.readLine();
				
				//Cartanmatrizen auslesen
				String nametag = "";
				int[][] matrix = null;
				int position = 0;
				while(line != null && (line.length() == 0 || line.charAt(0) != '~')) {
					if(line.length() != 0 && line.charAt(0) == '#') {
						nametag = line.replaceAll("#", "");
						matrix = null;
					}
					else if(line != "") {
						String entrys[] = line.split(" ");
						int nothingCounter = 0;
						for(int i = 0; i < entrys.length; i++) {
							if(entrys[i] == "") nothingCounter++;
						}
						int[] intEntrys = new int[entrys.length - nothingCounter];
						nothingCounter = 0;
						for(int i = 0; i < intEntrys.length; i++) {
							while(entrys[i + nothingCounter] == "") {
								nothingCounter++;
							}
							
							try {
								intEntrys[i] = Integer.parseInt(entrys[i + nothingCounter]);
							}
							catch(NumberFormatException e) {
								intEntrys = null;
								break;
							}
						}
						
						if(intEntrys != null) {
							if(matrix == null) {
								matrix = new int[intEntrys.length][intEntrys.length];
								for(int i = 0; i < intEntrys.length; i++) {
									matrix[0][i] = intEntrys[i];
								}
								position=1;
							}
							else if(matrix.length <= intEntrys.length && position < matrix.length) {
								for(int i = 0; i < matrix[position].length; i++) {
									matrix[position][i] = intEntrys[i];
								}
								position++;
								if(position == matrix.length) {
									Root_System[] newRs = new Root_System[rootSystems.length + 1];
									for(int i = 0; i < rootSystems.length; i++) {
										newRs[i] = rootSystems[i];
									}
									newRs[newRs.length - 1] = new Root_System(matrix, nametag);
									rootSystems = newRs;
								}
							}
								
						}
						
					}
					line = myReader.readLine();
				}
			}
			catch(IOException e) {
				this.status = "IO Exception";
				super.abort();
				return;
			}
				
			//Auswahldialog öffnen
			
			JPanel al = new JPanel();
			al.setLayout(new GridLayout(rootSystems.length + 2,1));
			JLabel label = new JLabel(LoadRootSystemsTemplate.this.getName() + ": Welche Wurzeloptionen sollen verwendet werden?");
			al.add(label);
			JCheckBox[] options = new JCheckBox[rootSystems.length + 1];
			options[0] = new JCheckBox("Alle Wurzelsysteme");
			al.add(options[0]);
			for(int i = 0; i < rootSystems.length; i++) {
				options[i+1] = new JCheckBox(rootSystems[i].getNametag());
				al.add(options[i+1]);
			}
			JOptionPane.showMessageDialog(null, al);
			
			
			
			if(options[0].isSelected()) {//Alle Wurzelsysteme laden
				addRootSystems = rootSystems;;
			}
			else {
				for(int i = 1; i < options.length; i++) {
					if(options[i].isSelected()) {
						Root_System[] nextAddRootSystems = new Root_System[addRootSystems.length + 1];
						for(int j = 0; j < addRootSystems.length; j++) {
							nextAddRootSystems[j] = addRootSystems[j];
						}
						nextAddRootSystems[nextAddRootSystems.length - 1] = rootSystems[i - 1];
						addRootSystems = nextAddRootSystems;
					}
				}
			}
		}

		@Override
		public String getProgress() {
			return status;
		}
		
		@Override
		public void run() {
			super.getMemory().addRootSystems(addRootSystems);
			super.setOrdinaryFinishFlag();
		}
	}
}