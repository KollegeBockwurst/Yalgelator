package algebra;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Stellt einen kommutativen Ring mit Eins dar
 * @author Yannik
 *
 */
public abstract class Ring {
	private final static Logger LOGGER =  Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 
	public static int THREAD_NUMBER =  Runtime.getRuntime().availableProcessors();
	/**
	 * Gibt ein Element des Rings zurück
	 * @return Eins im Ring 
	 */
	public abstract Element createOne();
	/**
	 * Gibt ein Element des Rings zurück
	 * @return Null im Ring
	 */
	public abstract Element createZero();
	/**
	 * Gibt ein (in welchem Rahmen auch immer erzeugtes) "zufälliges" Element des Rings zurück.
	 * @return "zufälliges Ringelement"
	 */
	public abstract Element createRandom();
	/**
	 * Erstellt eine Einheitsmatrix
	 * @param size Größe der Einheitsmatrix
	 * @return I_{size}
	 */
	public Matrix createIdentity(int size) {
		if(size < 0) return null; 
		Element[][] entrys = new Element[size][size];
		for(int i = 0; i < size; i++) {
			for(int j = 0; j < size; j++) {
				entrys[i][j] = (i == j) ? this.createOne() : this.createZero();
			}
		}
		return new Matrix(entrys);
	}
	/**
	 * Erstellt eine Nullmatrix
	 * @param size Größe der Nullmatrix
	 * @return 0
	 */
	public Matrix createZeroMatrix(int size) {
		if(size < 0) return null; 
		Element[][] entrys = new Element[size][size];
		for(int i = 0; i < size; i++) {
			for(int j = 0; j < size; j++) {
				entrys[i][j] = this.createZero();
			}
		}
		return new Matrix(entrys);
	}
	
	/**
	 * Innere Klasse, repräsentiert Elemente des Rings
	 * @author Yannik
	 *
	 */
	public abstract class Element{
		/**
		 * Liest den Ring (Objekt der Klasse Ring) aus, zu dem das Element gehört 		
		 * @return Ring, zu dem dieses Element gehört
		 */
		public Ring getRing() {
			return Ring.this;
		}
		
		/**
		 * Prüft, ob ein zweites Element zu dem gleichen Ring wie dieses Element gehört
		 * @param e zu prüfendes zweites Element
		 */
		public void checkType(Element e) {
			if(!this.getRing().equals(e.getRing())){
				LOGGER.log(Level.SEVERE, "You're trying to compare elements of different rings.");
				throw new IllegalArgumentException("You're trying to compare elements of different rings.");
			}
		}
		/**
		 * Addiert ein zweites Element zu diesem Element hinzu und gibt das Ergebnis als neu erzeugtes Element aus
		 * @param summand zweiter Summand
		 * @return Ergebnis der Addition, neu erzeugtes Element des Rings
		 */
		public abstract Element createSum(Element summand);
		/**
		 * Multipliziert ein zweites Element zu diesem Element und gibt gibt das Ergebnis als neu erzeugtes Element aus
		 * @param factor zweiter Faktor
		 * @return Ergebnis der Multiplikation , neu erzeugtes Element des Rings
		 */
		public abstract Element createProduct(Element factor);
		/**
		 * Gibt das additiv Inverse diesen Elements als neu erzeugtes Element zurück
		 * @return Additiv Inverses dieses Elements
		 */
		public abstract Element createNegation();
		/**
		 * Multilpiziert das Ringelement mit einer ganzen Zahl
		 * @return this * i
		 * @factor Ganze Zahl
		 */
		public Element createDuplication(int factor){
			Element r = this;
			for(int i = 1; i < factor; i++) {
				r = r.createSum(this);
			}
			for(int i = 0; i >= factor; i--) {
				r = r.createSum(this.createNegation());
			}
			return r;
		};
		/**
		 * Prüft, ob zwei Elemente des gleichen Rings gleich sind. Vergleicht man Elemente verschiedener Ringe wird ein Fehler erzeugt
		 * @param element zu vergleichendes Element
		 * @return Bool mit dem Ergebnis des Vergleichs
		 */
		public abstract boolean equals(Element element);
		/**
		 * Prüft, ob ein Element ein Teiler eines zweiten Elements des gleichen RIngs ist. Vergleicht man Elemente verschiedener Ringe wird ein Fehler erzeugt
		 * @param element Dividend
		 * @return Bool mit Ergebnis des VErgleichs
		 */
		public abstract boolean divides(Element element);
		
		/**
		 * Prüft, ob das Element das Nullelement ist.
		 * @return Bool mit Ergbnis der Operation
		 */
		public boolean isZero() {
			return this.equals(this.getRing().createZero());
		};
		/**
		 * Prüft, ob das Element das Einselement ist
		 * @return Bool mit Ergebnis der Operation
		 */
		public boolean isOne() {	
			return this.equals(this.getRing().createOne());
		};
		/**
		 * Erzeugt eine Kopie des Elements
		 * @return Kopie von this
		 */
		public abstract Element clone();
	};
	
	/**
	 * Innere Klasse Matrix, repräsentiert eine quadratische Matrix über diesem Ring
	 * @author Yannik
	 *
	 */
	public class Matrix {
		/**
		 * Größe der Matrix
		 */
		private final int SIZE;
		/**
		 * Einträge der Matrix, ENTRYS[i] - erste Zeile, ENTRYS[i][j] Eintrag ite Zeile, jte Spalte
		 */
		private final Element[][] ENTRYS;
		
		/**
		 * Erzeugt die Kopie einer Matrix
		 * @param m Vorlage
		 */
		public Matrix(Matrix m) {
			if(m.getRing() != this.getRing()) {
				LOGGER.log(Level.SEVERE, "Can't initialize a copy a Matrix overaa different ring.");
				throw new IllegalArgumentException("Exception while triing to initialize a matrix.");
			}
			this.SIZE = m.getSize();
			Element[][] mEntrys = m.getEntrys();
			Element[][] myEntrys = new Element[this.SIZE][this.SIZE];
			for(int i = 0; i < this.SIZE; i++) {
				for(int j = 0; j < this.SIZE; j++) {
					myEntrys[i][j] = mEntrys[i][j].clone();
				}
			}
			this.ENTRYS = myEntrys;
		}
		/**
		 * Die Einträge werden auf richtige Dimension geprüft und Ringzugehörigkeit
		 * @param entrys Einträge (Elemente) der Matrix
		 */
		public Matrix(Element[][] entrys) {
			this.SIZE = entrys.length;
			this.ENTRYS = entrys;
			for(Element[] row: entrys) {
				if(row == null || row.length < entrys.length) {
					LOGGER.log(Level.SEVERE, "Matrix can't initialized because a line is too short.");
					throw new IllegalArgumentException("Fatal exception while trying to initialize a matrix.");
				}
				if(row.length > entrys.length) LOGGER.log(Level.WARNING, "A Line of matrix is too long.");
				for(Element entry:row) {
					if(entry == null || !entry.getRing().equals(this.getRing())) {
						LOGGER.log(Level.SEVERE, "Matrix can't initialized because an entry comes from a wrong ring.");
						throw new IllegalArgumentException("Fatal exception while trying to initialize a matrix.");
					}
				}
			}
		}
		
		/**
		 * Liest den Ring (Objekt der Klasse Ring) aus, zu dem die Matrix gehört 		
		 * @return Ring, zu dem dieses Element gehört
		 */
		public Ring getRing() {
			return Ring.this;
		}
		/**
		 * Gibt die Größe der quadratischen Matrix zurück
		 * @return this.SIZE
		 */
		public int getSize() {
			return this.SIZE;
		}
		/**
		 * Gibt die Einträge der Matrix zurück
		 * @return this.ENTRYS
		 */
		public Element[][] getEntrys(){
			return this.ENTRYS;
		}
		/**
		 * Gibt einen Eintrag der Matrix zurück
		 * @param row Zeile des Eintrags
		 * @param column Spalte des Eintrags
		 * @return this.ENTRYS[row][column]
		 */
		public Element getEntry(int row, int column) {
			if(row < 0 || column < 0 || row >= this.ENTRYS.length || column >= this.ENTRYS[row].length) return null;
			return this.ENTRYS[row][column];
		}
		/**
		 * Berechnet die Determinante der Matrix mittels Rekursion und Laplaceschem Entwicklungssatz
		 * @return Determinante der Matrix
		 */
		public Ring.Element getDeterminant(){
			int size = this.getSize();
			if(size == 1) return this.getEntry(0, 0);
			Ring.Element result = this.getRing().createZero();
			for(int k = 0; k < size; k++) {
				Ring.Element[][] entrys2 = new Ring.Element[size-1][size-1];
				for(int i = 0; i < size -1; i++) {
					for(int j = 0; j < size - 1; j++) {
						entrys2[i][j] = this.getEntry(i + 1, j + ((j >= k)? 1:0));
					}
				}
				Matrix matrix2 = new Matrix(entrys2);
				Ring.Element summand = matrix2.getDeterminant();
				if(k%2 == 1) summand = summand.createNegation();
				summand = summand.createProduct(this.getEntry(0, k));
				result = result.createSum(summand);
			}
			return result;
		}		
		/**
		 * Erzeugt das Matrixprodukt zweier Matrizen 
		 * @param that rechter FAktor der Multiplikation
		 * @return this*that
		 */
		public Matrix createProduct(Matrix that) {
			Ring leftRing = this.getRing();
			Ring rightRing = that.getRing();
			Element[][] thisEntrys = this.getEntrys();
			Element[][] thatEntrys = that.getEntrys();
			int size = this.getSize();
			if(!leftRing.equals(rightRing)) LOGGER.log(Level.WARNING,"Trying to compare matrices over different Rings?");
			
			if(size != that.getSize()) LOGGER.log(Level.WARNING,"These matrices don't have the same size, maybe multiplication won't work.");
			
			Ring.Element[][] entrys = new Ring.Element[size][size];
			for(int i = 0; i < this.SIZE; i++) {
				for(int j = 0; j < this.SIZE; j++) {
					entrys[i][j] = leftRing.createZero();
					for(int k = 0; k < size; k++) {
						entrys[i][j] = entrys[i][j].createSum(thisEntrys[i][k].createProduct(thatEntrys[k][j]));
					}
				}
			}
			return new Matrix(entrys);
		}
		
		/**
		 * Berechnet die induzierte Abb. Z^n x Z^n -> R
		 * @param before Modulelement vor der Matrix, dargestellt als Array
		 * @param after Modulelement nach der Matrix, dargestellt als Array
		 * @return before*this*after
		 */
		public Ring.Element evaluate (int[] before, int[] after){ //TOTEST
			if(before == null || after == null || before.length != this.getSize() || after.length != this.getSize()) LOGGER.log(Level.WARNING,"Method invoke with nullpointers or crazy dimensions for evaluation..l");
			Ring.Element r = this.getRing().createZero();
			for(int i = 0; i < this.getSize(); i++) {
				Ring.Element s = this.getRing().createZero();
				for(int j = 0; j < this.getSize(); j++) {
					s = s.createSum(this.getEntry(i, j).createDuplication(after[j]));
				}
				r = r.createSum(s.createDuplication(before[i]));
			}
			return r;
		}
		
		@Override
		public String toString() {
			String result = "";
			for(int i = 0; i < this.SIZE; i++) {
				for(int j = 0; j < this.SIZE; j++) {
					result += this.ENTRYS[i][j] + " ";
				}
				result += "\n";
			}
			return result;
		}
	}
	
	/**
	 * Stellt eine quadratische schiefsymmetrische MAtrix über diesem Ring dar
	 * @author Yannik
	 *
	 */
	public class Skew_Matrix extends Matrix{
		
		/**
		 * Erzeugt eine neue schiefsymmetrische Matrix, die Einträge müssen bereits schiefsymmetrisch sein und werden NICHT schiefsymmetrisch gemacht
		 * Erzeugt eine Warnung, wenn die Matrix nicht schiefsymmetrisch ist
		 * @param entrys Einträge der MAtrix, erstes Argument: Zeile, zweites Argument: Spalte
		 */
		public Skew_Matrix(Element[][] entrys) {
			super(entrys);
			for(int i = 0; i < this.getSize(); i++) {
				for(int j = 0; j <= i; j++) {
					if(!this.getEntry(i, j).equals(this.getEntry(j, i).createNegation())) LOGGER.log(Level.WARNING, "Skew-symmetric matrix initialized with non-skew-symmetric values.");
				}
			}
		}
		/**
		 * Erzeugt die Kopie einer Skew_Matrix
		 * @param m
		 */
		public Skew_Matrix(Skew_Matrix m) {
			super(m);
		}
		
		public PfaffianComputer getPfaffianComputer() {
			return new PfaffianComputer();
		}
		
		/**
		 * Berechnet die Determinante über die Pfaffsche Determinante, falls die Pfaffsche bereits bestimmt wurde, sonst über die Elternmethode
		 */
		@Override
		public Ring.Element getDeterminant(){
			LOGGER.log(Level.INFO, "Computing Skew_Matrix Determinant without using permormanter pfaffian algorithm.");
			return super.getDeterminant();
		}
		
		public class PfaffianComputer extends Thread{
			private final PermutationCalculator[] PERMUTATION_CALCULATORS;
			private boolean abortFlag = false;
			private boolean ordinaryFinishFlag = false;
			
			public PfaffianComputer() {
				PERMUTATION_CALCULATORS = new PermutationCalculator[THREAD_NUMBER];
				int df = ExtraMath.doubleFactorial(Skew_Matrix.this.getSize() - 1); //Anzahl an Summanden (bzw Anzahl an möglichen Paar-Partitionen) bestimmen
				for(int i = 0; i < THREAD_NUMBER; i++) {
					PERMUTATION_CALCULATORS[i] = new PermutationCalculator(i*df/THREAD_NUMBER, (i+1) * df / THREAD_NUMBER, Skew_Matrix.this);
				}
			}
			
			@Override
			public void start() {
				if(!abortFlag) super.start();
			}
			
			public void abort() {
				for(int i = 0; i < PERMUTATION_CALCULATORS.length; i++) {
					PERMUTATION_CALCULATORS[i].abort();
				}
				abortFlag = true;
			}
			
			public Ring.Element getResult(){
				if(!ordinaryFinishFlag) return null;
				Element pfaffian = Skew_Matrix.this.getRing().createZero();
				for(int i = 0; i < PERMUTATION_CALCULATORS.length; i++) {
					pfaffian = pfaffian.createSum(PERMUTATION_CALCULATORS[i].getResult());
				}
				return pfaffian;
			}
			
			public String getProgress() {
				int progress = 0;
				for(int i = 0; i < THREAD_NUMBER; i++) {
					progress += PERMUTATION_CALCULATORS[i].getProgress();
				}
				progress /= THREAD_NUMBER;
				return progress + "%";
			}
			
			@Override
			public void run() {
				for(int i = 0; i < THREAD_NUMBER; i++) {
					PERMUTATION_CALCULATORS[i].start();
				}
				for(int i = 0; i < THREAD_NUMBER; i++) {
					try {
						PERMUTATION_CALCULATORS[i].join();
					} catch (InterruptedException e) {
						LOGGER.log(Level.FINE, "Interrupted Exception: " + e.getMessage());
						return;
					}
				}
				ordinaryFinishFlag = true;
			}
			
			private class PermutationCalculator extends Thread{
				private boolean abortFlag = false;
				private boolean ordinaryFinishFlag = false;
				private final int startI;
				private final int stopI;
				private final Skew_Matrix smatrix;
				private int status;
				private Ring.Element result;
				
				public PermutationCalculator(int startI, int stopI, Skew_Matrix smatrix) {
					this.startI = startI;
					status = startI;
					this.stopI = stopI;
					this.smatrix = smatrix;
					this.result = this.smatrix.getRing().createZero();
				}
				
				public void abort() {
					abortFlag = true;
				}
				
				/**
				 * Gibt den Fortschritt dieses PermutationCalculators bei der Berechnung seines Teils der Pfaffschen zurück
				 * @return Fortschritt zwischen 0 und 100
				 */
				public int getProgress() {
					if(stopI - startI == 0) return 100;
					int progress = (stopI - startI > 10000) ? (status - startI)/ ((stopI - startI) / 100):(status - startI) * 100 / (stopI - startI);
					return progress;
				}
				
				/**
				 * Gibt das Ergebnis des Threads zurück
				 * @return
				 */
				public Ring.Element getResult(){
					if(ordinaryFinishFlag) return result;
					else return null;
				}
				
				@Override
				public void run() {
					if(smatrix.getSize() %2 != 0) {
						ordinaryFinishFlag = true;
						return;
					}
					for(int i = startI; i<stopI && !abortFlag; i++) { //Jeden Summand durchlaufen
						status = i;
						int copyOfI = i;
						int[] permutation = new int[smatrix.getSize()];
						boolean[] used = new boolean[permutation.length];
						goingThrough:
						for(int j = 0; j < permutation.length; j++) { //SChleife berechnet i-te Paar-Partition als Permutation
							if(j%2 == 0) { //wenn wir auf einer informatisch geraden Position sind, muss die kleinste noch verfügbare Zahl genommen werden
								for(int k = 0; k < used.length; k++) {
									if(!used[k]) {
										used[k] = true;
										permutation[j] = k;
										continue goingThrough;
									}
								}
							}
							int counter = copyOfI % (smatrix.getSize() - j); //auf informatisch ungerader Position können wir uns eine der verbliebenen Zahlen aussuchen (und nehmen die, die uns durch i und MOdulorechnung vorgegeben ist)
							copyOfI /= (smatrix.getSize() - j);
							for(int k = 0; k < used.length; k++){
								if(!used[k]) counter--;
								if(counter == -1) {
									used[k] = true;
									permutation[j] = k;
									continue goingThrough;
								}
							}
						}
						boolean signum = true;
						for(int h = 0; h < permutation.length; h++) { //Berechnen Signum der Permutation
							for(int u = h+1; u < permutation.length; u++) {
								if(permutation[h] > permutation[u]) signum = !signum;
							}
						}
						Ring.Element summand = smatrix.getRing().createOne();
						for(int k = 0; k < permutation.length / 2; k++) { //Berechnen Summanden (s. Wikipedia)
							summand = summand.createProduct(smatrix.getEntry(permutation[2*k], permutation[2*k+1]));
						}
						if(!signum) summand = summand.createNegation();
						result = result.createSum(summand); //Addieren Summanden
					}
					ordinaryFinishFlag = true;
				}
			}
			
			
		}
		
		
		
	}
}
