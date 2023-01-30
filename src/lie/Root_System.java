package lie;

import java.util.logging.Level;
import java.util.logging.Logger;

import algebra.ExtraMath;
import algebra.Modular_Arithmetics;
import algebra.Polynomial_Ring;
import algebra.Ring;
import algebra.Ring.Skew_Matrix;
/**
 * Stellt ein Wurzelsystem als Cartanmatrix dar
 * @author Yannik
 */
public class Root_System {
	private final static Logger LOGGER =  Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 
	private final int[][] CARTAN;
	private final boolean IS_POSITIVE_DEFINITE;
	private String nametag = "";
	/**
	 * @param cartan Cartan-Matrix des Wurzelsystems
	 */
	public Root_System(int[][] cartan, String nametag) {
		this.CARTAN = cartan;
		this.nametag = nametag;
		//Überprüfe Matrix
		//Nullpointer?
		if(cartan == null) {
			LOGGER.log(Level.SEVERE, this.getNametag() + ":Can't initialize Root_System with null matrix.");
			throw new IllegalArgumentException("Nullpointer invoke.");
		}
		//Matrixdimension?
		for(int[] row : cartan) {
			if(row == null || row.length < cartan.length) {
				LOGGER.log(Level.SEVERE, this.getNametag() + ": A line in this matrix is too short.");
				throw new IllegalArgumentException("Matrix dimension mismatch.");
			}
			if(row.length > cartan.length) LOGGER.log(Level.WARNING, this.getNametag() + ": A line of cartanmatrix is too long!");
		}
		//2en auf Hauptdiagonale?
		for(int i = 0; i < cartan.length; i++) {
			if(cartan[i][i] != 2) {
				LOGGER.log(Level.WARNING, this.getNametag() + ": Matrix has not only 2's on main diagonal.");
				break;
			}
		}
		//Symmetrisierbar?
		if(this.getSymmetrization() == null) LOGGER.log(Level.WARNING, this.getNametag() + ": Cartan-Matrix is not symmetrizationable.");
		
		//Positiv definit? via Kriterium von Hurwitz
		Modular_Arithmetics Z = new Modular_Arithmetics(0);
		boolean isPositiveDefinite = true;
		for(int i = 0; i < cartan.length; i++) {
			Modular_Arithmetics.Element[][] H_i = new Modular_Arithmetics.Element[i+1][i+1];
			for(int j = 0; j < i+1; j++) {
				for(int k = 0; k < i+1; k++) {
					H_i[j][k] = Z.createElement(cartan[j][k]);
				}
			}
			Modular_Arithmetics.Matrix matrix = Z.new Matrix(H_i);
			if(((Modular_Arithmetics.Element)matrix.getDeterminant()).getRepresentation() <= 0) {
				LOGGER.log(Level.WARNING, this.getNametag() + ": Cartan matrix is not positive definite.");
				isPositiveDefinite = false;
				break;
			}
		}
		this.IS_POSITIVE_DEFINITE = isPositiveDefinite;
	}
	
	/**
	 * Errechnet die Symmetrisierung dieser Cartanmatrix C, d.h. die Hauptdiagonale der Diagonalmatrix D, s.d. D*C symmetrisch ist
	 * @return Hauptdiagonale von D
	 */
	public int[] getSymmetrization() {
		//Dreiecksmatrix mit ggts erstellen
		int[][] triangle = new int[this.CARTAN.length][];
		for(int i = 0; i < triangle.length; i++) {
			triangle[i] = new int[i+1];
			triangle[i][i] = 1;
			for(int j = 0; j < i; j++) {
				if(this.CARTAN[i][j] == 0) {
					if(this.CARTAN[j][i] == 0) continue;
					else return null;
				}
				
				int gcd = ExtraMath.gcd(triangle[i][i] * this.CARTAN[i][j], this.CARTAN[j][i]);
				int factorOld = this.CARTAN[j][i] / gcd;
				triangle[i][j] = triangle[i][i] * this.CARTAN[i][j] / gcd;
				triangle[i][i] *= factorOld;
				for(int k = 0; k < j; k++) {
					triangle[i][k] *= factorOld;
				}
			}
		}
		
		//Ausgabearray
		int[] result = new int[this.CARTAN.length];
		
		//Dreiecksmatrix zusammenführen:
		for(int column1 = 0; column1 < triangle.length; column1++) {
			for(int row1 = 0; row1 < triangle[column1].length; row1++) {
				columnLoop2:
				for(int row2 = row1+1; row2 < triangle[column1].length; row2++) {
					if(triangle[column1][row1] == 0 || triangle[column1][row2] == 0) continue columnLoop2; //Nulleinträge haben keine Aussage
					for(int column2 = column1+1; column2 < triangle.length; column2++) {
						if(triangle[column2][row1] != 0 || triangle[column2][row2] != 0) {
							int a = (triangle[column2][row1] == 0) ? row1 : row2; //a ist jetzt die Zeile mit der 0, falls es eine 0 gibt
							int b = (triangle[column2][row1] == 0) ? row2 : row1;
							
							if(triangle[column2][a] != 0) {
								//Es gibt gar keine 0, d.h. Verhältnisse prüfen
								if(triangle[column1][a] * triangle[column2][b] != triangle[column1][b] * triangle[column2][a]) return null;
							}
							else {
								//Es gibt genau eine 0 (in Zeile a), d.h. 0 ersetzen
								int newEntryNummerator = triangle[column1][a] * triangle[column2][b];
								int newEntryDenomminator = triangle[column1][b];
								
								int gcd = ExtraMath.gcd( newEntryNummerator, newEntryDenomminator);
								int factorColumn2 = newEntryDenomminator / gcd;
								int entryColumn2A = newEntryNummerator / gcd;
								
								for(int row3 = 0; row3 < triangle[column2].length; row3++) {
									triangle[column2][row3] *= factorColumn2;
								}
								
								triangle[column2][a] = entryColumn2A;
							}
							
							continue columnLoop2;
						}
					}
					result[row1] = triangle[column1][row1];
					result[row2] = triangle[column1][row2];
				}
			}
		}

		
		return result;
	}
	/**
	 * Errechnet die Symmetrisierung dieser Cartanmatrix C, d.h. die Hauptdiagonale der Diagonalmatrix D, s.d. D*C symmetrisch ist
	 * Gibt die Einträge über einem vorgegebenen Ring aus
	 * @param ring Ring, der verwendet werden soll
	 * @return Hauptdiagonale von D
	 */
	public Ring.Element[] getSymmetrization(Ring ring){
		Ring.Element[] result = new Ring.Element[this.CARTAN.length];
		int[] integerResult = this.getSymmetrization();
		if(integerResult == null) return null;
		for(int i = 0; i < result.length; i++) {
			result[i] = ring.createOne().createDuplication(integerResult[i]);
		}
		return result;
	}
	/**
	 * Erstellt einen Thread, der alle reduzierten Wörter dieses Wurzelsystems berechnen kann
	 * @param maximumLength maximale Länge der zu berechnenden Wörter
	 * @return AllWordComputer extends Thread
	 */
	public AllWordComputer createAllWordComputer(int maximumLength) {
		return new AllWordComputer(maximumLength);
	}
	/**
	 * Gibt den Nametag dieses Wurzelsystems aus
	 * @return this.nametag
	 */
	public String getNametag() {
		return this.nametag;
	}
	
	/**
	 * Gibt die (rootNumber)-te einfache Wurzel dieses Wurzelsystems zurück
	 * @param rootNumber Nummer der einfachen Wurzel
	 * @return einfache Wurzel
	 */
	public Root getBaseRoot(int rootNumber) {
		int[] vector = new int[this.getCartan().length];
		for(int i = 0; i < vector.length; i++) {
			if(i == rootNumber) vector[i] = 1;
			else vector[i] = 0;
		}
		return new Root(vector);
	}
	/**
	 * Gibt den Rang dieses Wurzelsystems zurück
	 * @return rank(this)
	 */
	public int getSize() {
		return this.CARTAN.length;
	}
	/**
	 * Gibt die Cartanmatrix dieses Wurzelsystems zurück
	 * @return this.CARTAN
	 */
	public int[][] getCartan(){
		return this.CARTAN;
	}
	
	/**
	 * Berechnet die standard Matrix M_B mit Einträge im entsprechneden Polynomring, Einträge über der Hauptdiagonalen werden durch entsprechende Variablen repräsentiert
	 * @return M_B
	 */
	public Polynomial_Ring.Matrix getDefaultM_B(Ring ring) { //TOTEST
		int cartanSize = this.CARTAN.length;
		Polynomial_Ring.Element[][] M_B = new Polynomial_Ring.Element[cartanSize][cartanSize];
		//Benötigen (n*n-n)/2 viele Variablen
		Polynomial_Ring polyRing = new Polynomial_Ring(ring, ((cartanSize * cartanSize) - cartanSize) / 2);
		Ring.Element[] mainDiagonal = this.getSymmetrization(ring);
		int variableCounter = 0;
		for(int i = 0; i < cartanSize; i++) {
			M_B[i][i] = polyRing.createConstantElement(mainDiagonal[i]);
			for(int j = i+1; j < cartanSize; j++) {
				M_B[i][j] = polyRing.createSingleVariable(variableCounter);
				variableCounter++;
			}
			
			for(int j = 0; j < i; j++) {
				M_B[i][j] = polyRing.createConstantElement(mainDiagonal[i].createProduct(ring.createOne().createDuplication(this.CARTAN[i][j]))).createSum(M_B[j][i].createNegation());
			}
		}
		return polyRing.new Matrix(M_B);
	}
	
	@Override 
	public String toString() {
		String result = "";
		int[][] cartan = this.getCartan();
		for(int i = 0; i < cartan.length; i++) {
			for(int j = 0; j < cartan[i].length; j++) {
				result += "" + cartan[i][j] + " ";
			}
			result += "\n";
		}
		return result;
	}

	/**
	 * Stellt eine Wurzel dieses Wurzelsystems dar
	 * @author Yannik
	 */
	public class Root{
		/**
		 * Grundlegende Charakterisierung als int[], wobei int[i] der Anteil der i-ten einfachen Wurzel ist
		 */
		private final int[] VECTOR;
		/**
		 * Gibt an, ob diese Wurzel positiv ist
		 */
		private final boolean IS_POSITIVE;
		/**
		 * Erstellt eine neue Wurzel
		 * @param vector Vektor der Wurzel
		 */
		public Root(int[] vector) {
			this.VECTOR = vector;
			if(vector.length > Root_System.this.getCartan().length) LOGGER.log(Level.WARNING, "Root initializing with a too long vector.");
			if(vector.length < Root_System.this.getCartan().length) LOGGER.log(Level.SEVERE, "Root initializing with a too short vector.");
			boolean checkPositive = false;
			boolean checkNegative = false;
			for(int i = 0; i < Root_System.this.getCartan().length; i++) {
				if(vector[i] > 0) checkPositive = true;
				else if(vector[i] < 0) checkNegative = true;
			}
			if(checkPositive == checkNegative) LOGGER.log(Level.WARNING, "Root does not look like a root.");
			this.IS_POSITIVE = checkPositive;
		}
		/**
		 * Gibt an, ob diese Wurzel positiv ist
		 * @return true, falls Wurzel positiv, false falls nicht
		 */
		public boolean isPositive() {
			return this.IS_POSITIVE;
		}
		/**
		 * Gibt den Vektor der Wurzel an
		 * @return Vektor der Wurzel
		 */
		public int[] getVector() {
			return this.VECTOR;
		}
		/**
		 * Rechnet das Bild dieser Wurzel unter der einfachen Spiegelung der (simpleRoot)-en einfachen Wurzel
		 * @param simpleRoot Nummer der Spiegelung, die betrachtet werden soll
		 * @return Bild dieser Wurzel unter der Spiegelung als (Root)
		 */
		public Root reflectAt(int simpleRoot) {
			if(simpleRoot < 0 || simpleRoot >= Root_System.this.getCartan().length) LOGGER.log(Level.WARNING, "simpleRoot is out of bounds.");
			int[] result = new int[Root_System.this.getCartan().length];
			for(int i = 0; i < result.length; i++) {
				result[i] = this.getVector()[i];
				if(i == simpleRoot) {
					for(int j = 0; j < result.length; j++) {
						result[i] -= Root_System.this.getCartan()[i][j] * this.getVector()[j];
					}
				}	
			}
			return new Root(result);
		}
		/**
		 * Gibt an, ob this und otherRoot die gleiche Wurzel repräsentieren
		 * @param otherRoot andere Wurzel
		 * @return 'otherRoot == this'
		 */
		public boolean equals(Root otherRoot) {
			for(int i = 0; i < Root_System.this.getCartan().length; i++) {
				if(this.VECTOR[i] != otherRoot.getVector()[i]) return false;
			}
			return true;
		}
		
		@Override
		public String toString() {
			String result = "[ ";
			for(int i = 0; i < this.VECTOR.length; i++) {
				result += this.getVector()[i] + " ";
			}
			result+= "]";
			return result;
		}
		
	}
	
	/**
	 * Stellt ein Wort der zugehörigen Weylgruppe dieses Wurzelsystems dar
	 * @author Yannik
	 */
	public class Word{
		final int[] REFLECTIONS;
		
		public Word(int[] reflections) {
			this.REFLECTIONS = reflections;
			if(reflections == null) {
				LOGGER.log(Level.SEVERE, "Method invoke with null argument.");
				throw new IllegalArgumentException("Method invoke with null argument.");
			}
			for(int reflection:reflections) {
				if(reflection < 0 || reflection > Root_System.this.getSize()) {
					LOGGER.log(Level.SEVERE, "Word uses reflections wich are not represented in this Root System.");
					throw new IllegalArgumentException("Argument does not look good.");
				}
			}
		}
		
		public Skew_Matrix createAssoMatrix(Ring.Matrix M_B) {//TOTEST, geht vllt einfacher mit neuen word methoden
			if(CARTAN.length != M_B.getSize()) {
				LOGGER.log(Level.WARNING,"Matrix M_B size mismatch.");
				return null;
			}
			int size = this.REFLECTIONS.length;
			Ring.Element[][] matrix = new Ring.Element[size][size];
			Root[] betas = new Root[size];
			
			for(int i = 0; i < betas.length; i++) {
				betas[i] = Root_System.this.getBaseRoot(this.REFLECTIONS[i]);
				for(int j = i-1; j >= 0; j--) {
					betas[i] = betas[i].reflectAt(this.REFLECTIONS[j]);
				}
			}
			for(int j = 0; j < matrix.length; j++) {
				matrix[j][j] = M_B.getRing().createZero();
				for(int i = 0; i < j; i++) {
					matrix[i][j] = M_B.evaluate(betas[i].getVector(), betas[j].getVector());
					matrix[j][i] = matrix[i][j].createNegation();
				}
			}
			return M_B.getRing().new Skew_Matrix(matrix);
		}
		
		public Root reflectRoot(Root root) { //TOTEST
			Root result = root;
			for(int i = 0; i < this.REFLECTIONS.length; i++) {
				result = result.reflectAt(this.REFLECTIONS[this.REFLECTIONS.length - 1 - i]);
			}
			return result;
		}
		
		public int getLength() {
			return this.REFLECTIONS.length;
		}
		
		public int[] getReflections() {
			return this.REFLECTIONS;
		}
		
		public Word[] getMinimalDecomposition(Word[] decompositionWords) {
			Root_System.Word[] decomp = decompositionWords;
			for(int i = 0; i < decompositionWords.length; i++) {
				for(int j = 0; j < decomp.length; j++) {
					if(decomp[j].equalsAsReflection(this)) {
						int[] intResult = new int[i + 1];
						for(int k = 0; k < i + 1; k++) {
							intResult[intResult.length - 1 - k] = j%decompositionWords.length;
							j /= decompositionWords.length;
						}
						Word[] result = new Word[intResult.length];
						for(int k = 0; k < intResult.length; k++) {
							result[k] = decompositionWords[intResult[k]];
						}
						return result;
					}
				}
				if(decomp.length * decompositionWords.length * 4 > 0 && Runtime.getRuntime().freeMemory() < decomp.length * decompositionWords.length * 4 ) {
					LOGGER.log(Level.FINE, "Prevented OOM exception.");
					return null;
				}
				Root_System.Word[] decomp2 = new Root_System.Word[decomp.length * decompositionWords.length];
				for(int j = 0; j < decomp.length; j++) {
					for(int k = 0; k < decompositionWords.length; k++) {
						if(Runtime.getRuntime().freeMemory() < (decompositionWords[k].getLength() + decomp[j].getLength()) * 100) {
							LOGGER.log(Level.FINE, "Prevented OOM exception.");
							return null;
						}
						decomp2[j*decompositionWords.length + k] = decompositionWords[k].createLink(decomp[j]).createReducedWord();
					}
				}
				decomp = decomp2;
				
			}
			
			return null;
		}
		
		public Word createReducedWord() {
			int[] wordCopy = this.REFLECTIONS.clone();
			int pointer = 0;
			int counter = 0;
			while(pointer < wordCopy.length) {
				if(wordCopy[pointer] == -1) LOGGER.log(Level.WARNING, "Method can't handle its own placeholder '-1' in the argument.");
				Root root = this.getRootSystem().getBaseRoot(wordCopy[pointer]);
				for(int j = pointer - 1; j >= 0; j--) {
					if(wordCopy[j] != -1) {
						root = root.reflectAt(wordCopy[j]);
						if(!root.isPositive()) {
							wordCopy[j] = -1;
							wordCopy[pointer] = -1;
							counter += 2;
							break;
						}
					}
				}
				pointer++;
			}
			int[] result = new int[wordCopy.length - counter];
			counter = 0;
			for(int i = 0; i < wordCopy.length; i++) {
				if(wordCopy[i] == -1) counter++;
				else result[i - counter] = wordCopy[i];
			}
			return new Word(result);
		}
		
		public boolean equalsAsReflection(Word that) {
			return (this.createLink(that.createInverse())).createReducedWord().getLength() == 0;
		}
		
		public Word createInverse() {
			int[] result = new int[this.REFLECTIONS.length];
			for(int i = 0; i < result.length; i++) {
				result[i] = this.REFLECTIONS[this.REFLECTIONS.length - 1 - i];
			}
			return new Word(result);
		}
		/**
		 * Erst this, dann that
		 * @param that
		 * @return
		 */
		public Word createLink(Word that) {
			if(that == null) return this.clone();
			if(!this.getRootSystem().equals(that.getRootSystem())) {
				LOGGER.log(Level.WARNING, "Comparing words from different Root Systems.");
				return null;
			}
			int[] thisReflections = this.getReflections();
			int[] thatReflections = that.getReflections();
			int[] result = new int[thisReflections.length + thatReflections.length];
			for(int i = 0; i < thatReflections.length; i++) {
				result[i] = thatReflections[i];
			}
			for(int i = 0; i < thisReflections.length; i++) {
				result[i+thatReflections.length] = thisReflections[i];
			}
			return new Word(result);
		}
		
		public boolean isReduced() {
			return this.equals(this.createReducedWord());
		}
		
		public boolean equals(Word that) {
			if(!this.getRootSystem().equals(that.getRootSystem())) {
				LOGGER.log(Level.WARNING, "Comparing words from different Root Systems.");
				return false;
			}
			if(this.getLength() != that.getLength()) return false;
			int[] thisReflections = this.getReflections();
			int[] thatReflections = that.getReflections();
			for(int i = 0; i < thisReflections.length; i++) {
				if(thisReflections[i] != thatReflections[i]) return false;
			}
			return true;
		}
		
		public Root_System getRootSystem() {
			return Root_System.this;
		}
		
		public Word clone() {
			return new Word(this.REFLECTIONS.clone());
		}
		
		@Override
		public String toString() {
			String result = "|";
			for(int i = 0; i < this.REFLECTIONS.length; i++) {
				if(i > 0) result += " ";
				result += this.REFLECTIONS[i];
			}
			return result + "|";
		}
	}
	
	/**
	 * Innere Klasse, erbt von Thread, um alle Reduzierten Wörter des Wurzelsystems zu berechnen
	 * @author Yannik
	 *
	 */
	public class AllWordComputer extends Thread{
		final int MAX_LENGTH;
		int numberWordsComputed = 1;
		private int actualCalc = 0; 
		private boolean abortFlag = false;
		private boolean ordinaryFinishFlag = false;
		public Word[] computedWords;
		
		/**
		 * Erzeugt neue Instanz
		 * @param maxLength Längste Wortlänge, die berechnet werden soll
		 */
		public AllWordComputer(int maxLength) {
			this.MAX_LENGTH = maxLength;
		}
		/**
		 * Gibt die berechneten Wörter zurück, die bis jetzt berechnet wurden
		 * @return
		 */
		public Word[] getComputedWords() {
			return computedWords;
		}
		/**
		 * Gibt einen Statustext des aktuellen Prozessen aus
		 * @return Statustext
		 */
		public String getProgress() {
			String result = (!abortFlag) ? "Berechne Länge " + this.actualCalc : "Rechnung abgebrochen bei Länge " + this.actualCalc;
			if(MAX_LENGTH > -1) {
				result += "/"+MAX_LENGTH;
			}
			result += " Wörter berechnet: " + numberWordsComputed;
			return result;
		}
		/**
		 * Setzt eine AbbruchFlag, die die run Methode sobald wie möglich beendet
		 */
		public void abort() {
			abortFlag = true;
		}
		/**
		 * Gibt zurück, ob alle Wörter (bis zur maximalen Länge) berechnet wurden
		 * @return true falls alle berechnet
		 */
		public boolean hasOrdinaryFinished() {
			return this.ordinaryFinishFlag;
		}
		
		@Override
		public void run() {
			if(!Root_System.this.IS_POSITIVE_DEFINITE && MAX_LENGTH < 0) {
				LOGGER.log(Level.WARNING, "Can't compute all words since the matrix was not positive definite.");
				return;
			}
			int numberSimpleRoots = Root_System.this.getSize();
			computedWords = new Word[1];
			Word[] wordsLastLength = new Word[1];
			wordsLastLength[0] = new Word(new int[0]);
			computedWords[0] = new Word(new int[0]);
			for(int i = 0; i < MAX_LENGTH || MAX_LENGTH == -1; i++) {
				actualCalc = i+1;
				Word[] wordsNewLength = new Word[wordsLastLength.length * numberSimpleRoots];
				for(int j = 0; j < wordsLastLength.length; j++) {
					for(int simpleRoot = 0; simpleRoot < numberSimpleRoots; simpleRoot++) {
						int[] reflections = {simpleRoot};
						wordsNewLength[j * numberSimpleRoots + simpleRoot] = wordsLastLength[j].createLink(new Word(reflections));
					}
				}
				wordsLastLength = new Word[0];
				firstGothroughLoop:
				for(int j = 0; j < wordsNewLength.length; j++) {
					if(wordsNewLength[j].isReduced()) {
						for(int k = 0; k < j; k++) {
							if(wordsNewLength[j].equalsAsReflection(wordsNewLength[k])) continue firstGothroughLoop;
						}
						if(this.abortFlag) return;
						numberWordsComputed++;
						Word[] newWords = new Word[computedWords.length + 1];
						Word[] copyWordsLastLength = new Word[wordsLastLength.length + 1];
						for(int k = 0; k < wordsLastLength.length; k++) {
							copyWordsLastLength[k] = wordsLastLength[k];
						}
						for(int k = 0; k < computedWords.length; k++) {
							newWords[k] = computedWords[k];
						}
						newWords[newWords.length - 1] = wordsNewLength[j];
						computedWords = newWords;
						newWords = new Word[wordsLastLength.length + 1];
						for(int k = 0; k < wordsLastLength.length; k++) {
							newWords[k] = wordsLastLength[k];
						}
						newWords[newWords.length - 1] = wordsNewLength[j];
						wordsLastLength = newWords;
					}
				}
				if(wordsLastLength.length == 0) break;
			}
			ordinaryFinishFlag = true;
		}
		
	}
}
