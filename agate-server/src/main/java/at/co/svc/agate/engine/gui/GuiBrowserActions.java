package at.co.svc.agate.engine.gui;
import java.util.*;
import java.util.regex.Pattern;
import at.co.svc.agate.core.dsl.model.TestCase;
import at.co.svc.agate.core.dsl.runtime.ExecutionContext;
import at.co.svc.agate.core.interfaces.TestLogger;
/** Called exclusively on the session's Playwright worker. */
public final class GuiBrowserActions {
 private GuiBrowserActions(){}
 public static void execute(TestCase tc,ExecutionContext context,GuiSession session,GuiStepConfig config,TestLogger logger)throws Exception {
  switch(config.op()) {
   case "OPEN_TAB" -> {session.openTab(config.required("tab"),config.required("url"),config.timeout(),logger);logger.info("    >>> GUI        : OPEN_TAB | "+config.required("tab"));}
   case "FIND_TAB" -> {
    if(config.text("tab")==null&&config.text("urlGlob")==null&&config.text("titleGlob")==null)throw new IllegalArgumentException("FIND_TAB requires tab, urlGlob or titleGlob");
    GuiTabResult result=new GuiTabResult(session.findTabs(config.text("tab"),config.text("urlGlob"),config.text("titleGlob")));
    context.storeBuffer(config.required("response"),result);logger.info("    >>> GUI        : FIND_TAB | matches="+result.tabs().size());
   }
   case "CLOSE_TAB" -> {boolean closed=session.closeTab(config.required("tab"));logger.info("    >>> GUI        : CLOSE_TAB | "+config.required("tab")+" | "+(closed?"closed":"already absent"));}
   case "BUFFER_TAB_EXISTS" -> {
    GuiTabResult result=context.getResponse(config.required("response"),GuiTabResult.class);
    if(result==null)throw new IllegalArgumentException("Missing GUI tab response: "+config.required("response"));
    String value=Boolean.toString(!result.tabs().isEmpty()),name=config.required("name");
    tc.addVariable(name,value);context.storeBuffer(name,value);context.setVar(name,value);logger.info("    >>> BUFFER     : ["+name+"] | "+value);
   }
   default -> throw new IllegalArgumentException("Unsupported browser action: "+config.op());
  }
 }
 public static boolean matches(String glob,String value) {
  StringBuilder regex=new StringBuilder("\\A");for(char c:glob.toCharArray())regex.append(c=='*'?".*":c=='?'?".":Pattern.quote(String.valueOf(c)));regex.append("\\z");
  return Pattern.compile(regex.toString(),Pattern.CASE_INSENSITIVE|Pattern.DOTALL).matcher(value).matches();
 }
}
