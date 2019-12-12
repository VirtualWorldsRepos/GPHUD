package net.coagulate.GPHUD.Modules.Introspection;

import net.coagulate.Core.Database.DBConnection;
import net.coagulate.Core.Tools.UserException;
import net.coagulate.GPHUD.GPHUD;
import net.coagulate.GPHUD.Interfaces.Outputs.*;
import net.coagulate.GPHUD.Interfaces.User.Form;
import net.coagulate.GPHUD.Modules.SideSubMenu;
import net.coagulate.GPHUD.Modules.URL.URLs;
import net.coagulate.GPHUD.SafeMap;
import net.coagulate.GPHUD.State;

import java.util.*;

/**
 * Introspect the SQL audit logs.
 * For superadmin only :P
 *
 * @author Iain Price <gphud@predestined.net>
 */
public class SQL {
	@URLs(url = "/introspection/sql")
	@SideSubMenu.SideSubMenus(name = "SQL", priority = 9999)
	public static void sqlIndex(State st, SafeMap values) throws UserException {
		if (!DBConnection.sqlLogging()) {
			st.form.add(new TextError("SQL auditing is disabled in this installation."));
			return;
		}

		if (!st.isSuperUser()) {
			st.form.add(new TextError("You are not permitted to view this information"));
			return;
		}

		Map<String, Integer> count = new HashMap<>();
		Map<String, Long> time = new HashMap<>();
		Map<String, Double> per = new HashMap<>();

		GPHUD.getDB().getSqlLogs(count, time, per);

		Map<Integer, Set<Row>> bycount = new TreeMap<>(Collections.reverseOrder());
		Map<Long, Set<Row>> bytime = new TreeMap<>(Collections.reverseOrder());
		Map<Double, Set<Row>> byper = new TreeMap<>(Collections.reverseOrder());

		for (Map.Entry<String, Integer> entry : count.entrySet()) {
			String sql = entry.getKey();
			int c = entry.getValue();
			long t = time.get(sql);
			Row newrow = new Row();
			newrow.add(c);
			newrow.add(sql);
			newrow.add(t + "ms");
			if (c>0) { newrow.add(t / c + "ms"); }
			Set<Row> rowset = new HashSet<>();
			if (bycount.containsKey(c)) { rowset = bycount.get(c); }
			rowset.add(newrow);
			bycount.put(c, rowset);
			rowset = new HashSet<>();
			if (bytime.containsKey(t)) { rowset = bytime.get(t); }
			rowset.add(newrow);
			bytime.put(t, rowset);
			double avg = t / c;
			rowset = new HashSet<>();
			if (byper.containsKey(avg)) { rowset = byper.get(avg); }
			rowset.add(newrow);
			byper.put(avg, rowset);
		}


		Form f = st.form;
		f.add(new TextSubHeader("By per call execution time"));
		Table t = new Table();
		f.add(t);
		t.add(new HeaderRow().add("Count").add("Query").add("Total time").add("Avg time"));
		for (Set<Row> set : byper.values()) {
			for (Row r : set) { t.add(r); }
		}
		f.add(new TextSubHeader("By total execution time"));
		t = new Table();
		f.add(t);
		t.add(new HeaderRow().add("Count").add("Query").add("Total time").add("Avg time"));
		for (Set<Row> set : bytime.values()) {
			for (Row r : set) { t.add(r); }
		}
		f.add(new TextSubHeader("By execution count"));
		t = new Table();
		f.add(t);
		t.add(new HeaderRow().add("Count").add("Query").add("Total time").add("Avg time"));
		for (Set<Row> set : bycount.values()) {
			for (Row r : set) { t.add(r); }
		}


	}
}
