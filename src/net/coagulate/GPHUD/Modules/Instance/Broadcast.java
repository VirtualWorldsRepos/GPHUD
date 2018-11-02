package net.coagulate.GPHUD.Modules.Instance;

import net.coagulate.GPHUD.Interfaces.Responses.OKResponse;
import net.coagulate.GPHUD.Interfaces.Responses.Response;
import net.coagulate.GPHUD.Modules.Argument.ArgumentType;
import net.coagulate.GPHUD.Modules.Argument.Arguments;
import net.coagulate.GPHUD.Modules.Command.Commands;
import net.coagulate.GPHUD.Modules.Command.Context;
import net.coagulate.GPHUD.State;

/** Send admin message.   Just a command stub.
 *
 * @author Iain Price <gphud@predestined.net>
 */
public class Broadcast {
    
    @Commands(context = Context.ANY,description = "Send admin message",requiresPermission = "instance.SendAdminMessages")
    public static Response admin(State st,
            @Arguments(description = "Message to broadcast",type = ArgumentType.TEXT_ONELINE)
                String sendmessage)
    {
        String message="(From ";
        String avfrom="";
        if (st.avatar()!=null) { avfrom=st.avatar().getName(); message+=avfrom; }
        if (st.getCharacter()!=null) { if (st.getCharacter().getName().equals(avfrom)) { } else { message+="/"+st.getCharacter().getName(); } }
        message+=") : "+sendmessage;
        int sent=st.getInstance().broadcastAdmins(st,message);
        return new OKResponse("Sent to "+sent+" admins");
    }
}
