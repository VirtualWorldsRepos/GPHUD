package net.coagulate.GPHUD.Modules.User;

import java.util.List;
import net.coagulate.Core.Tools.SystemException;
import static net.coagulate.Core.Tools.UnixTime.fromUnixTime;
import net.coagulate.Core.Tools.UserException;
import net.coagulate.GPHUD.Data.Audit;
import net.coagulate.GPHUD.Data.Avatar;
import net.coagulate.GPHUD.Data.Char;
import net.coagulate.GPHUD.Data.DateTime;
import net.coagulate.GPHUD.Interfaces.Outputs.Table;
import net.coagulate.GPHUD.Interfaces.Outputs.TextSubHeader;
import net.coagulate.GPHUD.Interfaces.Responses.OKResponse;
import net.coagulate.GPHUD.Interfaces.Responses.Response;
import net.coagulate.GPHUD.Interfaces.User.Form;
import net.coagulate.GPHUD.Modules.Argument;
import net.coagulate.GPHUD.Modules.Argument.Arguments;
import net.coagulate.GPHUD.Modules.Command;
import net.coagulate.GPHUD.Modules.Command.Commands;
import net.coagulate.GPHUD.Modules.Modules;
import net.coagulate.GPHUD.Modules.URL.URLs;
import net.coagulate.GPHUD.SafeMap;
import net.coagulate.GPHUD.State;

/** Views an Avatar object.
 *
 * @author Iain Price <gphud@predestined.net>
 */
public abstract class ViewAvatar {

    @URLs(url="/avatars/view/*")
    public static void viewAvatar(State st,SafeMap values) throws UserException, SystemException {
        String split[]=st.getDebasedURL().split("/");
        String id=split[split.length-1];
        Avatar a=Avatar.get(Integer.parseInt(id));
        viewAvatar(st,values,a);
    }

    
    public static void viewAvatar(State st,SafeMap values,Avatar a) throws UserException, SystemException {
        boolean full=false;
        String tz=st.avatar().getTimeZone();
        if (st.avatar()==a) { full=true; }
        if (st.hasPermission("Characters.ViewAll")) { full=true; }
        Form f=st.form;
        f.noForm();
        f.add(new TextSubHeader(a.getName()));
        Table kvtable=new Table(); f.add(kvtable);
        for (Char c:a.getCharacters(st.getInstance())) {
            kvtable.openRow().add("Owned Character").add(c);
        }
        String lastactive=fromUnixTime(a.getLastActive(),tz)+" "+tz;
        kvtable.openRow().add("Last Active").add(lastactive);
        kvtable.openRow().add("Selected Time Zone").add(tz);
        if (st.avatar()!=null && st.avatar()==a) {
            kvtable.add(new Form(st, true, "../settimezone", "Set TimeZone", "timezone",tz));
        }
        //for (String key:kv.keySet()) {
        //    String value=kv.get(key);
        //    kvtable.openRow().add(key).add(value);
        //}
        if (a.getName().equals("SYSTEM")) { f.add("<p><i>SYSTEM is a fake avatar used internally as an 'Invoking Avatar' for commands that usually require an active Avatar/Character, but there is no appropriate caller, e.g. Visitation XP is awarded by the SYSTEM avatar to prevent confusion and clutter in some other character/avatar's audit log</i></p>"); }
        f.add(new TextSubHeader("Audit Trail"));
        f.add(Audit.formatAudit(Audit.getAudit(st.getInstance(),null,a,null),st.avatar().getTimeZone()));
    }

    @URLs(url="/avatars/settimezone")
    public static void setTimeZone(State st,SafeMap value) {
        Modules.simpleHtml(st, "User.SetTZ", value);
    }
    
    @Commands(context = Command.Context.AVATAR,description = "Set displayed timezone for date/time events")
    public static Response setTZ(State st,
            @Arguments(type = Argument.ArgumentType.CHOICE,description = "Prefered Time Zone",mandatory = true,choiceMethod = "getTimeZones")
                String timezone) {
        st.avatar().setTimeZone(timezone);
        return new OKResponse("TimeZone preference updated");
    }
    
    public static List<String> getTimeZones(State st) {
        return DateTime.getTimeZones();
    }
    
}
