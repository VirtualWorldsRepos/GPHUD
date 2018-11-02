package net.coagulate.GPHUD.Modules.Configuration;

import net.coagulate.GPHUD.Data.Audit;
import net.coagulate.GPHUD.Interfaces.Responses.ErrorResponse;
import net.coagulate.GPHUD.Interfaces.Responses.OKResponse;
import net.coagulate.GPHUD.Interfaces.Responses.Response;
import net.coagulate.GPHUD.Modules.Argument.ArgumentType;
import net.coagulate.GPHUD.Modules.Argument.Arguments;
import net.coagulate.GPHUD.Modules.Command.Commands;
import net.coagulate.GPHUD.Modules.Command.Context;
import net.coagulate.GPHUD.Modules.Module;
import net.coagulate.GPHUD.Modules.Modules;
import net.coagulate.GPHUD.Modules.URL.URLs;
import net.coagulate.GPHUD.SafeMap;
import net.coagulate.GPHUD.State;
import net.coagulate.Core.Tools.SystemException;
import net.coagulate.Core.Tools.UserException;

/**  Enable or disable modules.  Instance owner only.
 *
 * @author Iain Price <gphud@predestined.net>
 */
public abstract class ModuleControl {

    @Commands(context = Context.AVATAR,description = "Disable the specified module",requiresPermission = "instance.owner")
    public static Response disableModule(State st,
            @Arguments(type = ArgumentType.MODULE,description = "Module to disable")
                Module module) throws UserException, SystemException
    {
        if (!(st.isInstanceOwner())) { return new ErrorResponse("Only the instance owner may disable a module"); }
        if (!module.canDisable()) { return new ErrorResponse("The module "+module.getName()+" does not allow its self to be disabled, it is probably critical to system functionality"); }
        String k=module.getName()+".Enabled";
        st.setKV(st.getInstance(),module.getName()+".Enabled", "false");
        Audit.audit(st, Audit.OPERATOR.AVATAR,null, null, null, "Disable Module", module.getName(), "", "", "Module disabled");
        return new OKResponse("Module "+module.getName()+" has been disabled");
    }
    
    @URLs(url = "/configuration/disablemodule",requiresPermission = "instance.owner")
    public static void disableModuleForm(State st,SafeMap values) throws UserException, SystemException {
        Modules.simpleHtml(st, "Configuration.DisableModule", values);
    }
    @Commands(context = Context.AVATAR,description = "Enabled the specified module",requiresPermission = "instance.owner")
    public static Response enableModule(State st,
            @Arguments(type = ArgumentType.MODULE,description = "Module to enable")
                Module module) throws UserException, Exception
    {
        if (!(st.isInstanceOwner())) { return new ErrorResponse("Only the instance owner may enable a module"); }

        st.setKV(st.getInstance(),module.getName()+".Enabled", "true");
        Audit.audit(st, Audit.OPERATOR.AVATAR,null, null, null, "Enable Module", module.getName(), "", "", "Module enabled");
        return new OKResponse("Module "+module.getName()+" has been enabled");
    }
    
    @URLs(url = "/configuration/enablemodule",requiresPermission = "instance.owner")
    public static void enableModuleForm(State st,SafeMap values) throws UserException, SystemException {
        Modules.simpleHtml(st, "Configuration.EnableModule", values);
    }
}
