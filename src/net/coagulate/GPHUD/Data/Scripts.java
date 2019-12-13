package net.coagulate.GPHUD.Data;

import net.coagulate.Core.Database.NoDataException;
import net.coagulate.Core.Database.Results;
import net.coagulate.Core.Database.ResultsRow;
import net.coagulate.Core.Tools.SystemException;
import net.coagulate.Core.Tools.UserException;
import net.coagulate.GPHUD.GPHUD;
import net.coagulate.GPHUD.Interfaces.Inputs.DropDownList;
import net.coagulate.GPHUD.Interfaces.Outputs.HeaderRow;
import net.coagulate.GPHUD.Interfaces.Outputs.Table;
import net.coagulate.GPHUD.State;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Scripts extends TableRow {
	public Scripts(int id) {
		super(id);
	}

	@Nonnull
	public static Table getTable(Instance instance) {
		Results rows = GPHUD.getDB().dq("select id,name,sourceversion,bytecodeversion from scripts order by id asc");
		Table o=new Table();
		o.add(new HeaderRow().add("Name").add("Version").add("Compiled Version"));
		for (ResultsRow row:rows) {
			o.openRow();
			o.add("<a href=\"/GPHUD/configuration/scripting/edit/"+row.getIntNullable("id")+"\">"+row.getStringNullable("name")+"</a>");
			Integer sourceversion=row.getIntNullable("sourceversion");
			Integer bytecodeversion=row.getIntNullable("bytecodeversion");
			if (Objects.equals(sourceversion, bytecodeversion)) {
				o.add((sourceversion == null ? "None" : "" + sourceversion));
				o.add((bytecodeversion == null ? "None" : "" + bytecodeversion));
			} else {
				o.add("<font color=red>"+(sourceversion == null ? "None" : "" + sourceversion)+"</font>");
				o.add("<font color=red>"+(bytecodeversion == null ? "None" : "" + bytecodeversion)+"</font>");
			}
		}
		return o;
	}

	public static void create(@Nonnull State st, String scriptname) {
		Integer existing=GPHUD.getDB().dqi("select count(*) from scripts where name like ? and instanceid=?",scriptname,st.getInstance().getId());
		if (existing>0) { throw new UserException("script with that name already exists"); }
		GPHUD.getDB().d("insert into scripts(instanceid,name) values(?,?)",st.getInstance().getId(),scriptname);
	}

	@Nonnull
	public static Scripts get(int id) {
		return (Scripts) factoryPut("Scripts", id, new Scripts(id));
	}

	@Nonnull
	public static Set<Scripts> getScript(@Nonnull Instance instance) {
		Set<Scripts> scripts=new HashSet<>();
		for (ResultsRow row:GPHUD.getDB().dq("select id from scripts where instanceid=?",instance.getId())) {
			scripts.add(new Scripts(row.getIntNullable("id")));
		}
		return scripts;
	}

	@Nonnull
	public static Scripts find(@Nonnull State st, String commandname) {
		Integer id=GPHUD.getDB().dqi("select id from scripts where instanceid=? and name like ?",st.getInstance().getId(),commandname);
		return new Scripts(id);
	}
	@Nullable
	public static Scripts findOrNull(@Nonnull State st, String commandname) {
		try { return find(st,commandname); }
		catch (NoDataException e) {return null; }
	}

	@Nonnull
	public static DropDownList getList(@Nonnull State st, String listname) {
		DropDownList list=new DropDownList(listname);
		for (ResultsRow row:GPHUD.getDB().dq("select id,name from scripts where instanceid=?",st.getInstance().getId())) {
			list.add(""+row.getIntNullable("id"),row.getStringNullable("name"));
		}
		return list;
	}

	@Nullable
	public String getSource() {
		String script=getString("source");
		if (script==null) { script=""; }
		return  script;
	}
	public int getSourceVersion() {
		Integer a = getIntNullable("sourceversion");
		if (a == null) { return 0; }
		return a;
	}
	public int getByteCodeVersion() {
		Integer a= getIntNullable("bytecodeversion");
		if (a==null) { return 0; }
		return a;
	}

	@Nonnull
	@Override
	public String getIdField() { return "id"; }

	@Override
	public void validate(@Nonnull State st) throws SystemException {
		if (validated) { return; }
		validate();
		if (st.getInstance() != getInstance()) { throw new SystemException("Script / State Instance mismatch"); }
	}

	@Nullable
	public Instance getInstance() {
		return Instance.get(getIntNullable("instanceid"));
	}

	@Nonnull
	@Override
	public String getNameField() { return "name"; }

	@Nonnull
	@Override
	public String getLinkTarget() { return "/GPHUD/configuration/scripting/edit/"+getId(); }

	@Override
	protected int getNameCacheTime() { return 600; }

	@Nullable
	@Override
	public String getKVTable() {
		return null;
	}

	@Nullable
	@Override
	public String getKVIdField() {
		return null;
	}

	@Nonnull
	@Override
	public String getTableName() {
		return "scripts";
	}

	public void setSource(@Nonnull String scriptsource) {
		validate();
		String s=getSource();
		if (scriptsource.equals(s)) { return; }
		int version=getSourceVersion();
		version++;
		d("update scripts set source=?, sourceversion=? where id=?",scriptsource,version,getId());
	}

	public void setBytecode(@Nonnull Byte[] toByteCode, int version) {
		validate();
		d("update scripts set bytecode=?, bytecodeversion=? where id=?",toByteCode,version,getId());
		if (GPHUD.DEV) {
			byte[] compareto = getByteCode();
			if (compareto.length != toByteCode.length) {
				throw new SystemException("Length mismatch, wrote " + toByteCode.length + " and read " + compareto.length);
			}
			for (int i = 0; i < compareto.length; i++) {
				if (compareto[i] != toByteCode[i]) {
					throw new SystemException("Difference at " + i + " - we wrote " + toByteCode[i] + " and read " + compareto[i]);
				}
			}
		}
	}

	@Nullable
	public byte[] getByteCode() {
		validate();
		return getBytes("bytecode");
	}

	public static void test() {
		byte[] b=new byte[255];
		for (int i=0;i<256;i++) { b[i]=((byte)(0xff & i)); }
		GPHUD.getDB().d("insert into scripts(instanceid,name,bytecode) values(?,?,?)",-1," ENCODING TEST ",b);
		byte[] out=GPHUD.getDB().dqbyte("select bytecode from scripts where instanceid=? and name=?",-1," ENCODING TEST ");
		GPHUD.getDB().d("delete from scripts where instanceid=? and name=?",-1," ENCODING TEST ");
		for (int i=0;i<256;i++) {
			if (b[i] != out[i]) {
				throw new SystemException("Comparison error on " + i + " - " + b[i] + " gave " + out[i]);
			}
		}
	}
}
