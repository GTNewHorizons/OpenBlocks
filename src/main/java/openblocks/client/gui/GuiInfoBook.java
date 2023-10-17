package openblocks.client.gui;

import java.io.File;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import openblocks.OpenBlocks;
import openblocks.client.ChangelogBuilder;
import openblocks.client.ChangelogBuilder.Changelog;
import openblocks.client.ChangelogBuilder.ChangelogSection;
import openblocks.client.gui.page.IntroPage;
import openblocks.common.PlayerInventoryStore;
import openmods.Log;
import openmods.gui.ComponentGui;
import openmods.gui.DummyContainer;
import openmods.gui.component.BaseComposite;
import openmods.gui.component.GuiComponentBook;
import openmods.gui.component.page.PageBase;
import openmods.gui.component.page.PageBase.ActionIcon;
import openmods.gui.component.page.SectionPage;
import openmods.gui.component.page.TitledPage;
import openmods.infobook.PageBuilder;

public class GuiInfoBook extends ComponentGui {

    private GuiComponentBook book;

    public GuiInfoBook() {
        super(new DummyContainer(), 0, 0);
    }

    @Override
    public void initGui() {
        // Nothing can change this value, otherwise client will crash when player picks item
        // this.mc.thePlayer.openContainer = this.inventorySlots;
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;
    }

    private static int alignToEven(final GuiComponentBook book) {
        int index = book.getNumberOfPages();
        if ((index & 1) == 1) {
            book.addPage(PageBase.BLANK_PAGE);
            index++;
        }
        return index;
    }

    @Override
    public void handleKeyboardInput() {
        super.handleKeyboardInput();

        if (Keyboard.getEventKeyState()) {
            switch (Keyboard.getEventKey()) {
                case Keyboard.KEY_PRIOR:
                    book.prevPage();
                    break;
                case Keyboard.KEY_NEXT:
                    book.nextPage();
                    break;
                case Keyboard.KEY_HOME:
                    book.firstPage();
                    break;
                case Keyboard.KEY_END:
                    book.lastPage();
                    break;
            }
        }
    }

    @Override
    protected BaseComposite createRoot() {
        book = new GuiComponentBook();

        book.addPage(PageBase.BLANK_PAGE);
        book.addPage(new IntroPage());
        book.addPage(new TitledPage("openblocks.gui.credits.title", "openblocks.gui.credits.content"));

        final TocPage contentsPage = new TocPage(book, Minecraft.getMinecraft().fontRenderer);
        book.addPage(contentsPage);

        {
            addSectionPage(book, contentsPage, "openblocks.gui.blocks");

            PageBuilder builder = new PageBuilder();
            builder.includeModId(OpenBlocks.MODID);
            builder.createBlockPages();
            builder.insertTocPages(book, 4, 4, 1.5f);
            alignToEven(book);
            builder.insertPages(book);
        }

        {
            addSectionPage(book, contentsPage, "openblocks.gui.items");

            PageBuilder builder = new PageBuilder();
            builder.includeModId(OpenBlocks.MODID);
            builder.createItemPages();
            builder.insertTocPages(book, 4, 4, 1.5f);
            alignToEven(book);
            builder.insertPages(book);
        }

        {
            addSectionPage(book, contentsPage, "openblocks.gui.misc");

            book.addPage(new TitledPage("openblocks.gui.config.title", "openblocks.gui.config.content"));
            book.addPage(
                    new TitledPage("openblocks.gui.restore_inv.title", "openblocks.gui.restore_inv.content")
                            .addActionButton(
                                    10,
                                    133,
                                    getSavePath(),
                                    ActionIcon.FOLDER.icon,
                                    "openblocks.gui.save_folder"));
            book.addPage(new TitledPage("openblocks.gui.bkey.title", "openblocks.gui.bkey.content"));

            if (OpenBlocks.Enchantments.explosive != null)
                book.addPage(new TitledPage("openblocks.gui.unstable.title", "openblocks.gui.unstable.content"));
            if (OpenBlocks.Enchantments.lastStand != null)
                book.addPage(new TitledPage("openblocks.gui.laststand.title", "openblocks.gui.laststand.content"));
            if (OpenBlocks.Enchantments.flimFlam != null)
                book.addPage(new TitledPage("openblocks.gui.flimflam.title", "openblocks.gui.flimflam.content"));

        }

        {
            addSectionPage(book, contentsPage, "openblocks.gui.changelogs");

            createChangelogPages(book);
        }

        book.enablePages();

        xSize = book.getWidth();
        ySize = book.getHeight();

        return book;
    }

    private static void addSectionPage(GuiComponentBook book, TocPage contentsPage, String sectionLabel) {
        final int startIndex = alignToEven(book);

        book.addPage(PageBase.BLANK_PAGE);
        book.addPage(new SectionPage(sectionLabel));
        contentsPage.addTocEntry(sectionLabel, startIndex, startIndex + 2);
    }

    private static File getSavePath() {
        try {
            MinecraftServer server = MinecraftServer.getServer();

            if (server != null) {
                World world = server.worldServerForDimension(0);
                File saveFolder = PlayerInventoryStore.getSaveFolder(world);
                return saveFolder;
            }
        } catch (Throwable t) {
            Log.warn(t, "Failed to get save folder from local server");
        }

        try {
            return Minecraft.getMinecraft().mcDataDir;
        } catch (Throwable t) {
            Log.warn(t, "Failed to get save folder from MC data dir");
        }

        return new File("invalid.path");
    }

    private static void createChangelogPages(final GuiComponentBook book) {
        String prevVersion = null;
        int prevIndex = 0;
        List<ChangelogPage> prevPages = Lists.newArrayList();

        final List<Changelog> changelogs = ChangelogBuilder.readChangeLogs();
        for (int i = 0; i < changelogs.size(); i++) {
            Changelog changelog = changelogs.get(i);
            final String currentVersion = changelog.version;
            int currentPage = book.getNumberOfPages();

            for (ChangelogPage prevPage : prevPages) prevPage.addNextVersionBookmark(book, currentVersion, currentPage);

            prevPages.clear();

            for (ChangelogSection section : changelog.sections) {
                ChangelogPage page = new ChangelogPage(currentVersion, section.title, section.lines);
                book.addPage(page);
                prevPages.add(page);

                if (i > 0) {
                    page.addPrevVersionBookmark(book, prevVersion, prevIndex);
                }
            }

            alignToEven(book);

            prevVersion = currentVersion;
            prevIndex = currentPage;
        }
    }

    @Override
    public void drawScreen(int par1, int par2, float par3) {
        super.drawScreen(par1, par2, par3);
        prepareRenderState();
        GL11.glPushMatrix();
        root.renderOverlay(this.mc, this.guiLeft, this.guiTop, par1 - this.guiLeft, par2 - this.guiTop);
        GL11.glPopMatrix();
        restoreRenderState();
    }
}
