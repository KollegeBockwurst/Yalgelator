package algebra;

import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * Stellt ein paar weitere mathematische Methoden bereit
 * @author Yannik
 *
 */
public abstract class ExtraMath {
	private final static Logger LOGGER =  Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	/**
	 * Statische Methode, berechnet die Doppelfakult�t eines Integers, f�r negative Werte und 0 wird 1 zur�ckgegeben
	 * @param argument n
	 * @return n!!
	 */
	public static int doubleFactorial(int argument) {
		int result = 1;
		int start = 1;
		if(argument % 2 == 0) start++;
		for(int i = start; i <= argument; i+= 2) {
			try {
				result = Math.multiplyExact(result, i);
			}
			catch(ArithmeticException e) {
				LOGGER.log(Level.WARNING, "Overflow in doubleFactorial, returns -1 now.");
				return -1;
			}
			}
		return result;
	}
	/**
	 * Statische Methode, berechnet den gr��ten gemeinsamen Teiler zweier ganzen Zahlen rekursiv, ergebnis immer positiv. Keine sinnvolle Berechnung, falls ein Argument 0 ist.
	 * @param a erste ganze Zahl
	 * @param b zweite ganze Zahl
	 * @return ggT(a,b)
	 */
	public static int gcd(int a, int b) {
		int result = (b == 0) ? a : gcd(b, a%b);
		return (result < 0) ? -result : result;
	}
}
