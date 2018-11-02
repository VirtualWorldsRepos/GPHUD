package net.coagulate.GPHUD.Modules.Introspection;

import java.util.Map;
import net.coagulate.GPHUD.Interfaces.Outputs.Colour;
import net.coagulate.GPHUD.Interfaces.Outputs.HeaderRow;
import net.coagulate.GPHUD.Interfaces.Outputs.Row;
import net.coagulate.GPHUD.Interfaces.Outputs.Table;
import net.coagulate.GPHUD.Interfaces.Outputs.TextHeader;
import net.coagulate.GPHUD.Interfaces.Outputs.TextSubHeader;
import net.coagulate.GPHUD.Interfaces.User.Form;
import net.coagulate.GPHUD.Modules.KV;
import net.coagulate.GPHUD.Modules.Module;
import net.coagulate.GPHUD.Modules.Modules;
import net.coagulate.GPHUD.Modules.SideSubMenu.SideSubMenus;
import net.coagulate.GPHUD.Modules.URL.URLs;
import net.coagulate.GPHUD.SafeMap;
import net.coagulate.GPHUD.State;

/** API Introspection.
 *
 * @author Iain Price <gphud@predestined.net>
 */
public abstract class KVMap {
    @URLs(url = "/introspection/kvmap")
    @SideSubMenus(name = "KeyValue Map",priority = 10)
    public static void kvmap(State st,SafeMap values) { 
        Form f=st.form;
        f.add(new TextHeader("KV Mappings"));
        Table t=new Table(); f.add(t);
        for (Module m:Modules.getModules()) {
            Map<String, KV> kvmap = m.getKVDefinitions(st);
            if (!kvmap.isEmpty()) {
                t.openRow();
                t.add(new HeaderRow().add(new TextSubHeader(m.getName())));
                t.add(new HeaderRow().add("Name").add("Scope").add("Type").add("Hierarchy").add("Permission").add("Default").add("Description").add("ConveyedAs").add("DoesTemplates"));
                for (String key:kvmap.keySet()) {
                    KV kv=kvmap.get(key);
                    Row r=new Row();
                    if (kv.hidden()) { r.setbgcolor("#e0e0e0"); }
                    t.add(r);
                    r.add(kv.name());
                    r.add(kv.scope().toString());
                    r.add(kv.type().toString());
                    r.add(kv.hierarchy().toString());
                    r.add(kv.editpermission());
                    r.add(kv.defaultvalue());
                    r.add(kv.description());
                    r.add(kv.conveyas());
                    r.add(kv.template()+"");
                    if (kv.isGenerated()) { r.add(new Colour("blue","Generated")); } else { r.add(""); }
                }
            }
        }
    }

    
}
