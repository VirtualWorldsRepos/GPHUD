package net.coagulate.GPHUD.Modules.Events;

import java.util.HashSet;
import java.util.Set;
import net.coagulate.GPHUD.Data.Event;
import net.coagulate.GPHUD.Modules.ModuleAnnotation;
import net.coagulate.GPHUD.Modules.SideSubMenu;
import net.coagulate.GPHUD.Modules.StaticSideSubMenu;
import net.coagulate.GPHUD.State;
import net.coagulate.Core.Tools.SystemException;
import net.coagulate.Core.Tools.UserException;

/** Dynamic events menu
 *
 * @author Iain Price <gphud@predestined.net>
 */
public class EventsModule extends ModuleAnnotation {
    
    public EventsModule(String name, ModuleDefinition def) throws SystemException, UserException {
        super(name, def);
    }

    @Override
    public Set<SideSubMenu> getSideSubMenus(State st) {
        Set<Event> events = st.getInstance().getEvents();
        Set<SideSubMenu> ret=new HashSet<>();
        for (Event event:events) {
            ret.add(new StaticSideSubMenu(event.getName(), event.getId(), "/events/view/"+event.getId(), ""));
        }
        return ret;
    }
    
    
}
