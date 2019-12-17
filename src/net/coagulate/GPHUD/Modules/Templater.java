package net.coagulate.GPHUD.Modules;

import net.coagulate.Core.Exceptions.System.SystemImplementationException;
import net.coagulate.Core.Exceptions.SystemException;
import net.coagulate.Core.Exceptions.User.UserInputValidationParseException;
import net.coagulate.Core.Exceptions.UserException;
import net.coagulate.GPHUD.State;
import net.coagulate.SL.SL;

import javax.annotation.Nonnull;
import java.lang.annotation.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;

import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

/**
 * Does the templating and mathematical operations on templated values.
 *
 * @author Iain Price <gphud@predestined.net>
 */
public abstract class Templater {

	private static final Map<String, String> templates = new TreeMap<>();
	private static final Map<String, Method> methods = new TreeMap<>();

	private static void add(final String key, final String description, final Method method) {
		templates.put("--" + key + "--", description);
		methods.put("--" + key + "--", method);
	}

	@Nonnull
	public static Map<String, String> getTemplates(final State st) {
		final Map<String, String> ret = new TreeMap<>(templates);
		for (final Module m : Modules.getModules()) {
			m.addTemplateDescriptions(st, ret);
		}
		return ret;
	}

	@Nonnull
	public static Map<String, Method> getMethods(final State st) {
		final Map<String, Method> ret = new TreeMap<>(methods);
		for (final Module m : Modules.getModules()) {
			m.addTemplateMethods(st, ret);
		}
		return ret;
	}

	public static void register(@Nonnull final Template template, final Method m) {
		add(template.name(), template.description(), m);
	}

	public static Method getMethod(final State st, final String name) { return getMethods(st).get(name); }

	@Nonnull
	public static String template(@Nonnull final State st, @Nonnull String string, final boolean evaluate, final boolean integer) {
		string = template(st, string);
		if ("".equals(string)) { return ""; }
		try {
			if (evaluate && !integer) { return eval(string) + ""; }
			//noinspection ConstantConditions
			if (evaluate && integer) { return "" + ((int) (eval(string))); }
		} catch (@Nonnull final Exception e) {
			SL.report("Expression failed for " + string, e, st);
			st.logger().log(WARNING, "Failed to complete expression evaluation for '" + string + "' - we got error " + e.getMessage(), e);
			throw e;
		}
		return string;
	}

	@Nonnull
	private static String template(@Nonnull final State st, @Nonnull String string) throws UserException {
		final boolean debug = false;
		for (final String subst : getTemplates(st).keySet()) {
			if (string.contains(subst)) {
				String value;
				try { value = getValue(st, subst); } catch (@Nonnull final UserException e) { value = "Error: " + e.getMessage(); }
				string = string.replaceAll(subst, Matcher.quoteReplacement(value));
			}
		}
		return string;
	}

	@Nonnull
	public static String getValue(@Nonnull final State st, final String keyword, final boolean evaluate, final boolean integer) {
		if (evaluate && integer) { return ((int) eval(getValue(st, keyword))) + ""; }
		if (evaluate) { return eval(getValue(st, keyword)) + ""; }
		return getValue(st, keyword);
	}

	@Nonnull
	private static String getValue(@Nonnull final State st, final String keyword) {
		final Method m = getMethods(st).get(keyword);
		if (m != null) {
			try {
				return (String) m.invoke(null, st, keyword);
			} catch (@Nonnull final IllegalAccessException | IllegalArgumentException ex) {
				SL.report("Templating exception", ex, st);
				st.logger().log(SEVERE, "Exception running templater method", ex);
				throw new SystemImplementationException("Templater exceptioned", ex);
			} catch (@Nonnull final InvocationTargetException e) {
				if (e.getCause() instanceof UserException) { throw (UserException) e.getCause(); }
				if (e.getCause() instanceof SystemException) { throw (SystemException) e.getCause(); }
				throw new SystemImplementationException("Unable to invoke target", e);
			}
		}
		throw new SystemImplementationException("No template implementation for " + keyword);
	}

	@Nonnull
	@Template(name = "NAME", description = "Character Name")
	public static String getCharacterName(@Nonnull final State st, final String key) {
		if (st.getCharacterNullable() == null) { return ""; }
		return st.getCharacter().getName();
	}

	// some standard templates

	@Template(name = "AVATAR", description = "Avatar Name")
	public static String getAvatarName(@Nonnull final State st, final String key) {
		if (st.getAvatarNullable() == null) { return ""; }
		return st.getAvatarNullable().getName();
	}

	@Nonnull
	@Template(name = "NEWLINE", description = "Newline character")
	public static String newline(final State st, final String key) { return "\n"; }

	//https://stackoverflow.com/questions/3422673/evaluating-a-math-expression-given-in-string-form
	// boann@stackoverflow.com
	public static double eval(@Nonnull final String str) throws UserException {
		return new Object() {
			int pos = -1, ch;

			void nextChar() {
				ch = (++pos < str.length()) ? str.charAt(pos) : -1;
			}

			boolean eat(final int charToEat) {
				while (ch == ' ') nextChar();
				if (ch == charToEat) {
					nextChar();
					return true;
				}
				return false;
			}

			double parse() {
				nextChar();
				final double x = parseExpression();
				if (pos < str.length())
					throw new UserInputValidationParseException("Unexpected: " + (char) ch + " at position " + pos + " in '" + str + "'");
				return x;
			}

			// Grammar:
			// expression = term | expression `+` term | expression `-` term
			// term = factor | term `*` factor | term `/` factor
			// factor = `+` factor | `-` factor | `(` expression `)`
			//        | number | functionName factor | factor `^` factor

			double parseExpression() {
				double x = parseTerm();
				for (; ; ) {
					if (eat('+')) x += parseTerm(); // addition
					else if (eat('-')) x -= parseTerm(); // subtraction
					else return x;
				}
			}

			double parseTerm() {
				double x = parseFactor();
				for (; ; ) {
					if (eat('*')) x *= parseFactor(); // multiplication
					else if (eat('/')) x /= parseFactor(); // division
					else return x;
				}
			}

			double parseFactor() {
				if (eat('+')) return parseFactor(); // unary plus
				if (eat('-')) return -parseFactor(); // unary minus

				double x;
				final int startPos = pos;
				if (eat('(')) { // parentheses
					x = parseExpression();
					eat(')');
				} else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
					while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
					x = Double.parseDouble(str.substring(startPos, pos));
				} else if (ch >= 'a' && ch <= 'z') { // functions
					while (ch >= 'a' && ch <= 'z') nextChar();
					final String func = str.substring(startPos, pos);
					x = parseFactor();
					switch (func) {
						case "sqrt":
							x = Math.sqrt(x);
							break;
						case "sin":
							x = Math.sin(Math.toRadians(x));
							break;
						case "cos":
							x = Math.cos(Math.toRadians(x));
							break;
						case "tan":
							x = Math.tan(Math.toRadians(x));
							break;
						default:
							throw new UserInputValidationParseException("Unknown function: " + func);
					}
				} else {
					throw new UserInputValidationParseException("Unexpected: " + (char) ch + " at " + pos + " in '" + str + "'");
				}

				if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation

				return x;
			}
		}.parse();
	}


	/**
	 * Defined a method that returns a templateable element.
	 * Must be public static and return a String with a singular State argument
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	@Target(ElementType.METHOD)
	public @interface Template {
		@Nonnull String name();

		@Nonnull String description();
	}

}
