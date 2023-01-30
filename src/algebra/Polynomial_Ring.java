package algebra;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Stellt einen Polynomring in mehreren Variablen über einem gegebenen Ring dar
 * @author Yannik
 * @see Ring
 */
public class Polynomial_Ring extends Ring{
	private final static Logger LOGGER =  Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	/**
	 * Speichert Anzahl der Variablen dieses Polynomrings
	 */
	private final int NUMBER_VARIABLES;
	/**
	 * Speichert den zugrundeliegenden Ring
	 */
	private final Ring RING;
	/**
	 * Erstellt einen neuen Polynomring
	 * @param ring zugrundeliegender Ring
	 * @param numberVariables Anzahl der Variablen
	 */
	public Polynomial_Ring(Ring ring, int numberVariables) {
		if(ring == null) {
			LOGGER.log(Level.SEVERE, "Method invoke with null argument.");
			throw new IllegalArgumentException("Method invoke with null argument.");
		}
		if(numberVariables < 0) {
			LOGGER.log(Level.SEVERE, "Negative value for numberVariables.");
			throw new IllegalArgumentException("Negative value for numberVariables.");
		}
		
		this.NUMBER_VARIABLES = numberVariables;
		this.RING = ring;
	}
	/**
	 * Gibt die Anzahl an Variablen in diesem Polynomring zurück
	 * @return NUMBER_VARIABLES
	 */
	public int getNumberVariables() {
		return this.NUMBER_VARIABLES;
	}
	/**
	 * Erzeugt ein neues (konstantes) Element im Polynomring
	 * @param element Element im zugrundeliegenden Ring
	 * @return element in ring[...]
	 */
	public Element createConstantElement(Ring.Element element) {
		if(element == null) return null;
		if(!element.getRing().equals(this.RING)) {
			LOGGER.log(Level.WARNING, "Argument element is not from this Polynomial_Ring's Ring.");
			return null;
		}
		
		Monomial m = new Monomial(new int[this.NUMBER_VARIABLES], element);
		Monomial[] summands = new Monomial[1];
		summands[0] = m;
		return new Element(summands);
	}
	/**
	 * Erzeugt ein neues Element im Polynomring. Das Element besteht aus einer einzigen Variable
	 * @param number Nummer der Variablen, die erstellt werden soll
	 * @return x_{number} in ring[...]
	 */
	public Element createSingleVariable(int number) {
		if(0 > number || number >= this.getNumberVariables()) {
			LOGGER.log(Level.WARNING, "There is no variable with this number");
			return null;
		}
		int[] powers = new int[this.getNumberVariables()];
		powers[number] = 1;
		Monomial m = new Monomial(powers, this.RING.createOne());
		Monomial[] summands = new Monomial[1];
		summands[0] = m;
		return new Element(summands);
	}
	
	@Override
	public Element createOne() {
		return this.createConstantElement(this.RING.createOne());
	}

	@Override
	public Element createZero() {
		return this.createConstantElement(this.RING.createZero());
	}

	@Override
	public Element createRandom() {
		return this.createConstantElement(this.RING.createRandom());
	}
	/**
	 * Stellt ein Element im Polynomring als Summe von Monomen dar
	 * @author Yannik
	 * @see Ring.Element
	 */
	public class Element extends Ring.Element{

		/**
		 * Array mit den Summanden, geordnet mittels der Funktion "isLowerThan" von Monomen, von klein nach groß
		 */
		private final Monomial[] SUMMANDS;
		/**
		 * Erstellt ein neues Polynom aus einer Liste von Summanden. Die Liste wird auf korrekte Ordnung geprüft (sonst gibt es eine Warnung) und bei korrekter Ordnung werden Monome mit gleichem Bigrad zusammengefasst
		 * @param summands Geordnete Liste (von klein nach groß) der Summanden
		 */
		public Element(Monomial[] summands) {
			//Prüfe, ob summands korrekt geordnet & Fasse doppelte Fakultäten zusammen
			int nullCounter = 0;
			for(int i = 0; i < summands.length - 1; i++) {
				if(summands[i+1] == null) LOGGER.log(Level.SEVERE, "Nullpointer in summands argument"); 
				if(!summands[i].isLowerThan(summands[i+1])) {
					LOGGER.log(Level.WARNING, "Array Summands nicht korrekt geordnet");
					break;
				}
				if(summands[i].hasSameBidegree(summands[i+1])) {
					summands[i+1] = summands[i+1].createSum(summands[i]);
					summands[i] = null;
					nullCounter++;
				}
				else if(summands[i].getCoefficient().isZero()) {
					summands[i] = null;
					nullCounter++;
				}
			}
			if(summands.length != 0 && summands[summands.length - 1].getCoefficient().isZero()) {
				summands[summands.length - 1] = null;
				nullCounter++;
			}
			this.SUMMANDS = new Monomial[summands.length - nullCounter];
			
			nullCounter = 0;
			for(int j = 0; j < summands.length; j++) {
				if(summands[j] == null) nullCounter++;
				else this.SUMMANDS[j - nullCounter] = summands[j];
			}
		}
		/**
		 * Gibt die Liste der Summanden dieses monoms zurück
		 * @return
		 */
		public Monomial[] getSummands() {
			return this.SUMMANDS;
		}
		
		/**
		 * Liefert den Grad des Polynoms
		 * @return deg(this)
		 */
		public int getDegree() {
			int deg = 0;
			for(Monomial summand : this.SUMMANDS) {
				int summandDeg = summand.getDegree();
				if(summandDeg > deg) deg = summandDeg;
			}
			return deg;
		}
		
		@Override
		public Element createSum(algebra.Ring.Element summand) {
			super.checkType(summand);
			Monomial[] thisSummands = this.getSummands();
			Monomial[] thatSummands = ((Element)summand).getSummands();
			Monomial[] resultSummands = new Monomial[thisSummands.length + thatSummands.length];
			int thisPointer = 0;
			int thatPointer = 0;
			for(int i = 0; i < resultSummands.length; i++) {
				if(thisPointer<thisSummands.length) {
					if(thatPointer < thatSummands.length) {
						if(thisSummands[thisPointer].isLowerThan(thatSummands[thatPointer])) {
							resultSummands[i] = new Monomial(thisSummands[thisPointer]);
							thisPointer++;
						}
						else {
							resultSummands[i] = new Monomial(thatSummands[thatPointer]);
							thatPointer++;
						}
					}
					else {
						resultSummands[i] = new Monomial(thisSummands[thisPointer]);
						thisPointer++;
					}
				}
				else {
					resultSummands[i] = new Monomial(thatSummands[thatPointer]);
					thatPointer++;
				}
			}
			return new Element(resultSummands);
		}

		@Override
		public Element createProduct(algebra.Ring.Element factor) {
			super.checkType(factor);
			Monomial[] thisSummands = this.getSummands();
			Monomial[] thatSummands = ((Element)factor).getSummands();
			int maximumNumberOfSummands = Math.multiplyExact(this.SUMMANDS.length,thatSummands.length);
			Monomial[] resultSummands = new Monomial[0]; //Größe wird dynamisch angepasst
			
			if(maximumNumberOfSummands == 0) return new Element(resultSummands);
			
			//Pointer zeigt auf alle Elemente, deren Multiplikation als nächstes in Frage kommt, erstes Argument: Position in thisSummand bzw. thatSummand, zweite Position: Durchzählug der Möglichkeiten
			int[][] pointer = new int[2][1];
			Monomial[] products = new Monomial[1];
			
			products[0] = thisSummands[0].createProduct(thatSummands[0]);
			
			for(int i = 0; i < maximumNumberOfSummands; i++) {
				//Finde nächsten Summanden
				
				//min: Position in Pointer, die das kleinste Monomprodukt gibt
				int min = 0;
				//Schleife um Minimum zu finden:
				for(int j = 1; j < pointer[0].length; j++) {
					if(!products[min].isLowerThan(products[j]))min = j;
				}
				//Kleinstes Teilprodukt in Ergebnis schreiben
				if(resultSummands.length > 0 && resultSummands[resultSummands.length - 1].hasSameBidegree(products[min])) resultSummands[resultSummands.length - 1] = resultSummands[resultSummands.length - 1].createSum(products[min]);
				else {
					Monomial[] newResultSummands = new Monomial[resultSummands.length + 1];
					for(int j = 0; j < resultSummands.length; j++) {
						newResultSummands[j] = resultSummands[j];
					}
					newResultSummands[newResultSummands.length - 1] =  products[min];
					resultSummands = newResultSummands;
				}
				products[min] = null;
				
				//Neue Pointer berechnen
				boolean checkNewPointer1 = pointer[0][min]+1 < thisSummands.length;
				boolean checkNewPointer2 = pointer[1][min]+1 < thatSummands.length;
				if(checkNewPointer1) {
					for(int k = 0; k < pointer[0].length; k++) {
						if(k != min && pointer[0][k] <= pointer[0][min] + 1 && pointer[1][k]<=pointer[1][min]) {
							checkNewPointer1 = false;
							break;
						}
					}
				}
				if(checkNewPointer2) {
					for(int k = 0; k < pointer[1].length; k++) {
						if(k != min && pointer[1][k] <= pointer[1][min] + 1 && pointer[0][k]<=pointer[0][min]) {
							checkNewPointer2 = false;
							break;
						}
					}
				}
				if(checkNewPointer1 && checkNewPointer2) {
					int[] thisList = new int[pointer[0].length+1];
					int[] thatList = new int[pointer[1].length+1];
					Monomial[] newProducts = new Monomial[products.length + 1];
					for(int k = 0; k < pointer[0].length; k++) {
						thatList[k] = pointer[1][k];
						if(k != min) {
							thisList[k] = pointer[0][k];
							newProducts[k] = products[k];
						}
						else{
							thisList[k] = pointer[0][min] + 1;
							newProducts[k] = thisSummands[thisList[k]].createProduct(thatSummands[thatList[k]]);
						}
						
					}
					thisList[thisList.length -1] = pointer[0][min];
					thatList[thatList.length -1] = pointer[1][min]+1;
					newProducts[newProducts.length - 1] = thisSummands[thisList[thatList.length -1]].createProduct(thatSummands[thatList[thatList.length -1]]);
					
					products = newProducts;
					pointer[0] = thisList;
					pointer[1] = thatList;
				}
				else if(checkNewPointer1) {
					pointer[0][min]++;
					products[min] = thisSummands[pointer[0][min]].createProduct(thatSummands[pointer[1][min]]);
				}
				else if(checkNewPointer2) {
					pointer[1][min]++;
					products[min] = thisSummands[pointer[0][min]].createProduct(thatSummands[pointer[1][min]]);
				}
				else {
					int[] thisList = new int[pointer[0].length-1];
					int[] thatList = new int[pointer[1].length-1];
					Monomial[] newProducts = new Monomial[products.length - 1];
					for(int k = 0; k < thisList.length; k++) {
						int add = 0;
						if(k >= min) add = 1;
						thisList[k] = pointer[0][k+add];
						thatList[k] = pointer[1][k+add];
						newProducts[k] = products[k+add];
					}
					pointer[0] = thisList;
					pointer[1] = thatList;
					products = newProducts;
				}
				
				
			}
			
			
			return new Element(resultSummands);
		}

		@Override
		public algebra.Ring.Element createNegation() {
			Monomial[] resultSummands = this.getSummands().clone();
			for(int i = 0; i < resultSummands.length; i++) {
				resultSummands[i] = resultSummands[i].createNegation();
			}
			return new Element(resultSummands);
		}

		@Override
		public boolean equals(algebra.Ring.Element element) {
			super.checkType(element);
			Monomial[] thisSummands = this.getSummands();
			Monomial[] thatSummands = ((Element)element).getSummands();
			if(thisSummands.length != thatSummands.length) return false;
			for(int i = 0; i < thisSummands.length; i++) {
				if(!thisSummands[i].equals(thatSummands[i])) return false;
			}
			return true;
		}

		@Override
		public boolean divides(algebra.Ring.Element element) {
			LOGGER.log(Level.INFO, "Method not implemented.");
			return false;
		}

		@Override
		public algebra.Ring.Element clone() {
			return new Element(this.SUMMANDS.clone());
		}
		
		@Override
		public String toString() {
			int gcd = 0;
			if(RING.getClass() == Modular_Arithmetics.class && ((Modular_Arithmetics)RING).getModulo() == 0) {
				for(Monomial summand:this.SUMMANDS) {
					int coefficient = ((Modular_Arithmetics.Element)summand.getCoefficient()).getRepresentation();
					if(coefficient == 0) continue;
					gcd = ExtraMath.gcd(gcd, coefficient);
				}
			}
			String result = "";
			if(gcd != 0) result += gcd + "*";
			result += "{";
			for(int i = 0; i < this.SUMMANDS.length; i++) {
				if(i > 0) result += "+";
				if(gcd == 0) {
					result += this.SUMMANDS[i];
				}
				else {
					int coefficient = ((Modular_Arithmetics.Element)this.SUMMANDS[i].getCoefficient()).getRepresentation() / gcd;
					result += this.SUMMANDS[i].toString(Integer.toString(coefficient));
				}
			}
			return result+'}';
		}
		
	}
	/**
	 * Stellt ein Monom über so vielen Variablen wie der zugrundeliegende Polynomring samt Koeffizient dar
	 * @author Yannik
	 *
	 */
	public class Monomial{
		/**
		 * Speichert die Potenzen der Variablen dieses Moinoms
		 */
		private final int[] POWERS;
		/**
		 * Speichert den Koeffizienten dieses Monoms
		 */
		private final Ring.Element COEFFICIENT;
		/**
		 * Erstellt ein neues Monom aus einem Array von Potenzen und einem Koeffizienten im zugrundeliegenden Ring
		 * @param powers Potenzen
		 * @param coefficient Koeffizient im zugrundeliegenden Ring
		 */
		public Monomial(int[] powers, Ring.Element coefficient) {
			this.POWERS = powers;
			this.COEFFICIENT = coefficient;
			if(powers == null || coefficient == null) {
				LOGGER.log(Level.SEVERE, "Methode invoke with null arguments.");
				throw new IllegalArgumentException("Methode invoke with null arguments.");
			}
			if(powers.length != NUMBER_VARIABLES) {
				LOGGER.log(Level.SEVERE, "Power array has dimension mismatch.");
				throw new IllegalArgumentException("Power array has dimension mismatch.");
			}
			if(!RING.equals(coefficient.getRing())) {
				LOGGER.log(Level.SEVERE, "Coefficient is from wrong Ring.");
				throw new IllegalArgumentException("Coefficient is from wrong Ring.");
			}
			for(int i = 0; i < powers.length; i++) {
				if(powers[i] < 0) {
					LOGGER.log(Level.WARNING, "Monomial does not support negative powers, resetting these to 0.");
					powers[i] = 0;
				}
			}
			
		}
		/**
		 * Gibt den Polynomring zurück, zu dem dieses Monom gehört
		 * @return Polynomring.this
		 */
		public Polynomial_Ring getPolynomialRing() {
			return Polynomial_Ring.this;
		}
		
		/**
		 * Erzeugt eine Kopie eines bestehenden Monoms
		 * @param m Vorlage
		 */
		public Monomial(Monomial m) {
			this(m.getPowers().clone(), m.getCoefficient().clone());
		}
		/**
		 * Gibt die Potenzen dieses Monoms zurück
		 * @return this.POWERS
		 */
		public int[] getPowers() {
			return this.POWERS;
		}
		/**
		 * Gibt den koeffizienten dieses Polynoms zurück
		 * @return this.COEFFICIENT
		 */
		public Ring.Element getCoefficient(){
			return this.COEFFICIENT;
		}
		/**
		 * Prüft, ob beide Monome gleichen Bigrad haben
		 * @param that zweites zu prüfendes Monom
		 * @return true falls gleich, false sonst
		 */
		public boolean hasSameBidegree(Monomial that) {
			if(!this.getPolynomialRing().equals(that.getPolynomialRing())) {
				LOGGER.log(Level.WARNING, "Argument Monomial is from different Polynomial_Ring.");
				return false;
			}
			int[] thisPowers = this.getPowers();
			int[] thatPowers = that.getPowers();
			for(int i = 0; i < thisPowers.length; i++) {
				if(thisPowers[i] != thatPowers[i]) return false;
			}
			return true;
		}
		/**
		 * Prüft, ob dieses Monom kleiner ist als ein zweites wie folgt: verglichen werden die Potenzen, zuerst Variable 0, dann immer weiter (Lexikografische Ordnung)
		 * @param that 
		 * @return true falls this <= that, false falls this > that
		 */
		public boolean isLowerThan(Monomial that) {
			if(!this.getPolynomialRing().equals(that.getPolynomialRing())) {
				LOGGER.log(Level.WARNING, "Argument Monomial is from different Polynomial_Ring.");
				return false;
			}
			int[] thisPowers = this.getPowers();
			int[] thatPowers = that.getPowers();
			for(int i = 0; i < thisPowers.length; i++) {
				if(thisPowers[i] > thatPowers[i]) return false;
				if(thisPowers[i] < thatPowers[i]) break;
			}
			return true;
		}
		
		/**
		 * Prüft, ob beide Monome gleich sind, d.h. gleiche Potenzen und Koeffizient
		 * @param that zweites Monom
		 * @return this == that
		 */
		public boolean equals(Monomial that) {
			if(!this.getPolynomialRing().equals(that.getPolynomialRing())) {
				LOGGER.log(Level.WARNING, "Argument Monomial is from different Polynomial_Ring.");
				return false;
			}
			return (this.getCoefficient().equals(that.getCoefficient())) && this.hasSameBidegree(that);
		}
		
		/**
		 * Liefert das Produkt dieses Monoms mit einem zweiten
		 * @param that zweiter Faktor
		 * @return this*that
		 */
		public Monomial createProduct(Monomial that) {//TOTEST
			if(!this.getPolynomialRing().equals(that.getPolynomialRing())) {
				LOGGER.log(Level.WARNING, "Argument Monomial is from different Polynomial_Ring.");
				return null;
			}
			int[] thisPowers = this.getPowers();
			int[] thatPowers = that.getPowers();
			Ring.Element resultCoefficient = this.COEFFICIENT.createProduct(that.getCoefficient());
			int[] resultPowers = new int[NUMBER_VARIABLES];
			for(int i = 0; i < NUMBER_VARIABLES; i++) {
				resultPowers[i] = Math.addExact(thisPowers[i],thatPowers[i]);
			}
			return new Monomial(resultPowers, resultCoefficient);
		}
		/**
		 * Addiert den Koeffizienten des zweiten Monoms auf dieses Monom drauf, ergibt i.A. nur Sinn, falls beide Potenzen gleich sind
		 * @param that
		 * @return
		 */
		public Monomial createSum(Monomial that) {
			if(!this.hasSameBidegree(that)) return null;
			return new Monomial(this.POWERS, this.COEFFICIENT.createSum(that.getCoefficient()));
		}
		/**
		 * Negiert dieses Monom
		 * @return -this
		 */
		public Monomial createNegation() {
			return new Monomial(this.getPowers(), this.COEFFICIENT.createNegation());
		}
		/**
		 * Liefert den Grad des Monoms
		 * @return deg(this)
		 */
		public int getDegree() {
			if(this.COEFFICIENT.isZero()) return 0;
			int deg = 0;
			for(int i : this.POWERS) {
				deg += i;
			}
			return deg;
		}
		@Override
		public String toString() {
			return this.toString(this.COEFFICIENT.toString());
		}
		/**
		 * Gibt das Monom zurück, aber ersetzt den Koeffizienten durch einen String, wichtig für z.B. rausgeteilte Elemente im Polynomring
		 * @param coefficientReplacement Ersatz für den koeffizienten
		 * @return String representation 
		 */
		public String toString(String coefficientReplacement) {
			String result = "["+coefficientReplacement+":";
			int[] powers = this.getPowers();
			for(int i = 0; i < NUMBER_VARIABLES; i++) {
				if(i > 0) result += ',';
				result += powers[i];
			}
			result += ']';
			return result;			
		}
	}

}
