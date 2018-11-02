package net.coagulate.GPHUD.Interfaces.Outputs;

import java.util.Set;
import net.coagulate.GPHUD.State;

/** For all things that can appear in output.
 * All the formats of output we need.
 *
 * @author Iain Price <gphud@predestined.net>
 */
public interface Renderable {
    /** Render this element as plain text.
     * Used for textual output into Second Life (llSay() etc)
     * @param st
     * @return
     */
    public abstract String asText(State st);

    /** Render this element into simple HTML.
     * Used for non admin HTML interfaces like the HUD's web panel and Admin interface.
     * non admin interface sets rich to false - dont link to admin pages for entities etc.
     * @param st
     * @param rich Rich mode
     * @return
     */
    public abstract String asHtml(State st,boolean rich);
    
    public abstract Set<Renderable> getSubRenderables();
}
