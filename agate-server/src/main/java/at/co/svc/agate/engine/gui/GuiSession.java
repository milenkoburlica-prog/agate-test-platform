package at.co.svc.agate.engine.gui;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

public class GuiSession {
    private Playwright playwright;
    private Browser browser;
    private Page page;

    public Playwright getPlaywright() { return playwright; }
    public void setPlaywright(Playwright playwright) { this.playwright = playwright; }

    public Browser getBrowser() { return browser; }
    public void setBrowser(Browser browser) { this.browser = browser; }

    public Page getPage() { return page; }
    public void setPage(Page page) { this.page = page; }
}