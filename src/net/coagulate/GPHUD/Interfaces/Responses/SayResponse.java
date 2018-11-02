package net.coagulate.GPHUD.Interfaces.Responses;

import java.util.Set;
import net.coagulate.GPHUD.Interfaces.Outputs.Paragraph;
import net.coagulate.GPHUD.Interfaces.Outputs.Renderable;
import net.coagulate.GPHUD.Interfaces.System.Transmission;
import net.coagulate.GPHUD.State;
import org.json.JSONObject;

/**  A response formatted as a "sayas" response for SL.  Returns just the message in HTML.
 *
 * @author Iain Price <gphud@predestined.net>
 */
public class SayResponse implements Response {
    
    String reason; public String getText() { return reason; } public void setText(String text) { this.reason=text; }
    String sayas=null;
    public SayResponse(String r) { reason=r; }
    public SayResponse(String r,String sayas) {
        reason=r;
        this.sayas=sayas;
    }

    @Override
    public JSONObject asJSON(State st) {
        JSONObject json=new JSONObject();
        if (sayas!=null) { json.put("sayas",sayas); json.put("say","/me "+reason); }
        else { json.put("say",reason); }
        return json;
    }

    @Override
    public String asText(State st) {
        if (st.getCharacter()!=null) {
            Transmission t=new Transmission(st.getCharacter(),this.asJSON(st));
            t.start();
        }
        String message;
        if (sayas!=null) { message="\""+sayas+" "+reason+"\""; }
        else { message="\""+reason+"\""; }
        return message;
    }
    @Override
    public String asHtml(State st, boolean rich) {
        return new Paragraph(asText(st)).asHtml(st, rich);
    }

  
    @Override
    public Set<Renderable> getSubRenderables() {
        return null;
    }
}
