package net.coagulate.GPHUD.Modules.Instance;

import net.coagulate.Core.Exceptions.System.SystemImplementationException;
import net.coagulate.Core.HTML.Elements.Raw;
import net.coagulate.Core.HTML.Page;
import net.coagulate.Core.Tools.UnixTime;
import net.coagulate.GPHUD.Data.*;
import net.coagulate.GPHUD.Interfaces.Outputs.*;
import net.coagulate.GPHUD.Interfaces.Responses.ErrorResponse;
import net.coagulate.GPHUD.Interfaces.Responses.OKResponse;
import net.coagulate.GPHUD.Interfaces.Responses.Response;
import net.coagulate.GPHUD.Interfaces.User.Form;
import net.coagulate.GPHUD.Modules.Command;
import net.coagulate.GPHUD.Modules.Experience.GenericXP;
import net.coagulate.GPHUD.Modules.Modules;
import net.coagulate.GPHUD.Modules.URL;
import net.coagulate.GPHUD.SafeMap;
import net.coagulate.GPHUD.State;
import net.coagulate.SL.HTTPPipelines.PlainTextMapper;
import net.coagulate.SL.SL;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.http.entity.ContentType;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Set;

public class Reporting {
    @URL.URLs(url="/reporting/download",
              requiresPermission = "Instance.Reporting")
    public static void webDownloadReport(@Nonnull final State state,
                                         @Nonnull final SafeMap parameters) {
        if (!state.isSuperUser()) {
            if (!state.getInstance().spendDownloadCredit()) {
                state.form().add(new Paragraph(new TextError("Sorry, you are out of download credits!")));
                return;
            }
        }
        Page.page().resetRoot();
        //noinspection deprecation
        Page.page().add(new Raw(state.getInstance().getReport()));
        state.suppressOutput(true);
        Page.page().template(new PlainTextMapper.PlainTextTemplate());
        Page.page().addHeader("content-disposition","attachment; filename=\""+state.getInstance().getName()+" - "+UnixTime.fromUnixTime(state.getInstance().lastReport(), state.getAvatar().getTimeZone())+" "+state.getAvatar().getTimeZone()+".csv\"");
        Page.page().contentType(ContentType.create("text/csv"));
    }
    @URL.URLs(url="/reporting/generate",
              requiresPermission = "Instance.Reporting")
    public static void webGenerateReport(@Nonnull final State state,
                                        @Nonnull final SafeMap parameters) {
        Form f=state.form();
        Response success=generateReport(state);
        if (success instanceof ErrorResponse) { f.add(new Paragraph(new TextError(success.asHtml(state,true)))); }
        else { f.add(new Paragraph(new TextOK(success.asHtml(state,true)))); }
        f.add(new Paragraph(new Link("Return to reporting","/GPHUD/reporting")));
    }
    @URL.URLs(url="/reporting",
              requiresPermission = "Instance.Reporting")
    public static void reportingPage(@Nonnull final State state,
                                     @Nonnull final SafeMap parameters) {
        Form f=state.form();
        f.add(new TextHeader("Report Generation"));
        f.add(new Paragraph("Your instance currently has "+state.getInstance().reportCredits()+" reporting credits and "+state.getInstance().downloadCredits()+" download credits."));
        if (state.getInstance().reportCredits()>0 || state.isSuperUser()) {
            f.add(new Paragraph(new Link("Generate New Report","/GPHUD/reporting/generate")));
        }
        int lastReport=state.getInstance().lastReport();
        if (lastReport==0) {
            f.add(new Paragraph("Your instance currently has no generated report"));
        } else {
            f.add(new Paragraph("The last report was generated at " + UnixTime.fromUnixTime(lastReport, state.getAvatar().getTimeZone()) + " " + state.getAvatar().getTimeZone() + " (" + UnixTime.durationRelativeToNow(lastReport) + "ago)"));
            if (state.isSuperUser() || state.getInstance().downloadCredits() > 0) {
                f.add(new Paragraph(new Link("Download this report", "/GPHUD/reporting/download")));
            }
        }
    }

    @Command.Commands(description="Generates an up to date report ready for download",
                      notes="This may take a while, intentionally",
                      requiresPermission = "Instance.Reporting",
                      permitObject = false,
                      permitScripting = false,
                      permitExternal = false,
                      context = Command.Context.AVATAR)
    public static Response generateReport(@Nonnull final State state) {
        if (!state.isSuperUser()) {
            if (!state.getInstance().spendReportCredit()) {
                return new ErrorResponse("Sorry, you have no report generation credits left");
            }
        }
        new Thread(() -> runReport(state)).start();
        return new OKResponse("Report generation in progress, this will probably take "+ UnixTime.duration(state.getInstance().countCharacters()/10,true));
    }

    public static void runReport(State state) {
        Set<Char> set = state.getInstance().getCharacters();
        Set<Attribute> attributes = Attribute.getAttributes(state);
        SL.log("Reporting").info("Beginning report for " + state.getInstance() + " with " + set.size() + " characters x " + attributes.size() + " attributes");
        StringWriter output = new StringWriter();
        try {
            CSVPrinter csv = new CSVPrinter(output, CSVFormat.EXCEL);
            csv.print("ID");
            for (Attribute attribute : attributes) {
                csv.print(attribute.getName());
            }
            csv.println();
            for (Char ch : set) {
                State charState = new State(ch);
                csv.print(ch.getId());
                for (Attribute attribute : attributes) {
                    switch (attribute.getType()) {
                        case SET:
                            csv.print(new CharacterSet(ch, attribute).textList());
                            break;
                        case POOL:
                            csv.print(CharacterPool.sumPool(ch, Modules.getPool(charState, attribute.getName())));
                            break;
                        case GROUP:
                            String subType = attribute.getSubType();
                            if (subType==null) { csv.print(""); }
                            else {
                                CharacterGroup groupMembership = CharacterGroup.getGroup(ch,subType);
                                csv.print(groupMembership == null ? "" : groupMembership.getName());
                            }
                            break;
                        case CURRENCY:
                            csv.print(Currency.find(charState, attribute.getName()).sum(charState));
                            break;
                        case INVENTORY:
                            csv.print(new Inventory(ch, attribute).textList());
                            break;
                        case EXPERIENCE:
                            csv.print(CharacterPool.sumPool(charState, (new GenericXP(attribute.getName()).getPool(charState))));
                            break;
                        case COLOR:
                        case INTEGER:
                        case TEXT:
                        case FLOAT:
                            csv.print(charState.getKV("Characters." + attribute.getName()).toString());
                            break;
                    }
                }
                csv.println();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                }
            }
            state.getInstance().setReport(output.toString());
            SL.log("Reporting").info("Report generation for " + state.getInstance() + " with " + set.size() + " characters x " + attributes.size() + " attributes is now complete.");
        } catch (IOException e) {
            throw new SystemImplementationException("Error writing CSV to StringWriter (!)", e);
        }
    }
}
