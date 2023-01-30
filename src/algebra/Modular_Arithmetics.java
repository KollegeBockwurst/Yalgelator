package algebra;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Stellt den Restklassenring Z/mZ für gegebene ganze Zahl m dar. m = 0 steht damit naturgemäß für Z. Erbt von der Klasse Ring
 * @author Yannik
 * @see Ring
 */
public class Modular_Arithmetics extends Ring{
	private final static Logger LOGGER =  Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 
	/**
	 * Speichert m
	 */
	private final int m;
	
	/**
	 * Erstellt einen neuen Restklassenring
	 * @param m Beschreibt den Restklassenring der Darstellung Z/mZ
	 */
	public Modular_Arithmetics(int m) {
		if(m < 0) m = m * (-1);
		this.m = m;
	}
	/**
	 * Gibt zurück, welcher Moduloring dieser Moduloring ist
	 * @return m in der Darstellung Z/mZ
	 */
	public int getModulo() {
		return m;
	}
	
	/**
	 * Innere Klasse Element beschreibt Elemente des zugehörigen Restklassenrings und erbt von Ring.Element
	 * @author Yannik
	 * @see Ring.Element
	 */
	public class Element extends Ring.Element{
		
		/**
		 * Speichert den Repräsentanten des Elements als Repräsentanten des Faktorrings
		 */
		private final int REPRESENTATION;
		
		/**
		 * Erzeugt ein neues Element im Restklassenring
		 * @param representation Repräsentant des zu erzeugenden Elements
		 */
		public Element(int representation) {
			if(m==0) this.REPRESENTATION = representation;
			else {
				representation = representation % m;
				if(representation < 0) representation += m;
				this.REPRESENTATION = representation;
			}
		}
		/**
		 * Erstellt eine Kopie eines Elements
		 * @param e Vorlage
		 */
		public Element(Element e) {
			this(e.REPRESENTATION);
		}
		/**
		 * Gibt die repräsentierednde ganze Zahl dieses Elements zurück
		 * @return a in a+Z/mZ
		 */
		public int getRepresentation() {
			return this.REPRESENTATION;
		}
		
		@Override
		public Element createSum(Ring.Element element){
			super.checkType(element);
			try {
				int result = Math.addExact(this.REPRESENTATION, ((Element) element).REPRESENTATION);
				return createElement(result);
			}
			catch(ArithmeticException e) {
				LOGGER.log(Level.WARNING, "Integer Overflow while adding Ring.Elements.");
				return null;
			}
		}
		
		@Override
		public Element createProduct(Ring.Element element) {
			super.checkType(element);
			try {
				int result = Math.multiplyExact(this.REPRESENTATION, ((Element) element).REPRESENTATION);
				return createElement(result);
			}
			catch(ArithmeticException e) {
				LOGGER.log(Level.WARNING, "Integer Overflow while multiplying Ring.Elements.");
				return null;
			}
		}
		
		@Override public Element createDuplication(int factor) {
			return new Element(this.REPRESENTATION * factor);
		}
		
		@Override
		public boolean equals(Ring.Element e) {
			super.checkType(e);
			return this.REPRESENTATION == ((Element)e).REPRESENTATION;
		}
		
		@Override 
		public String toString(){
			
			return String.valueOf(this.REPRESENTATION) + ((m==0)? "":(" + Z/" + String.valueOf(m) + "Z"));		
		}

		@Override
		public boolean divides(Ring.Element element) {
			LOGGER.log(Level.WARNING, "Method not implemented.");
			return false;
		}

		@Override
		public Ring.Element createNegation() {
			return createElement(-this.REPRESENTATION);
		}

		@Override
		public algebra.Ring.Element clone() {
			return new Element(this);
		}
	}
	
	/**
	 *  Erzeugt ein neues Element des Restklassenrings und gibt es zurück
	 * @param i Repräsentant im Restklassenring Z/mZ
	 * @return Element mit Repräsentant i
	 */
	public Element createElement(int i) {
		return new Element(i);
	}

	@Override
	public Element createZero() {
		return createElement(0);
		
	}

	@Override
	public Element createOne() {
		return createElement(1);
	}

	@Override
	public algebra.Ring.Element createRandom() {
		return createElement((int)(Math.random()*10 - 5));
	}
	
	
}
