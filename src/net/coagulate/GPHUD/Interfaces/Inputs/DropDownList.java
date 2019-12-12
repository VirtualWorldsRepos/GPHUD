package net.coagulate.GPHUD.Interfaces.Inputs;

import net.coagulate.GPHUD.Interfaces.Outputs.Renderable;
import net.coagulate.GPHUD.Modules.Command;
import net.coagulate.GPHUD.Modules.Module;
import net.coagulate.GPHUD.Modules.Modules;
import net.coagulate.GPHUD.State;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * A drop down list choice.
 *
 * @author Iain Price <gphud@predestined.net>
 */
public class DropDownList extends Input {
	final String name;
	final Map<String, String> choices = new TreeMap<>();

	public DropDownList(String name) {
		this.name = name;
		add("");
	}

	public static DropDownList getCommandsList(State st, String name) { return getCommandsList(st, name, true); }

	public static DropDownList getCommandsList(State st, String name, boolean allowgenerated) {
		DropDownList commands = new DropDownList(name);
		for (Module mod : Modules.getModules()) {
			for (Command c : mod.getCommands(st).values()) {
				if (allowgenerated || !c.isGenerated()) {
					commands.add(c.getFullName(), c.getFullName() + " - " + c.description());
				}
			}
		}
		return commands;
	}

	public void add(String choice) { choices.put(choice, choice); }

	public void add(String choice, String label) { choices.put(choice, label); }

	@Override
	public String asHtml(State st, boolean rich) {
		String r = "";
		r += "<select name=\"" + name + "\"";
		if (submitonchange) { r+="onchange=\"this.form.submit()\""; }
		r+=">";
		for (Map.Entry<String, String> entry : choices.entrySet()) {
			String option = entry.getKey();
			r += "<option value=\"" + option + "\"";
			if (option.equalsIgnoreCase(value)) { r += " selected"; }
			r += ">" + entry.getValue() + "</option>";
		}
		r += "</select>";
		return r;
	}

	@Override
	public Set<Renderable> getSubRenderables() {
		return null;
	}

	@Override
	public String getName() {
		return name;
	}

	boolean submitonchange=false;
	public DropDownList submitOnChange() { submitonchange=true; return this;}
}
