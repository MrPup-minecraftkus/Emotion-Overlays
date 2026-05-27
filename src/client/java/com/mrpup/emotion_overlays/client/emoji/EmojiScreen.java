package com.mrpup.emotion_overlays.client.emoji;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mrpup.emotion_overlays.client.network.SelectEmojiPacket;
import com.mrpup.emotion_overlays.common.EmojiEntry;
import com.mrpup.emotion_overlays.common.EmojiRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class EmojiScreen extends Screen {

    private static final int CELL      = 28;
    private static final int COLS      = 8;
    private static final int PAD       = 8;
    private static final int TAB_H     = 20;
    private static final int SEARCH_H  = 18;
    private static final int STATUS_H  = 12;
    private static final int GRID_ROWS = 5;

    private int panelW, panelH, panelX, panelY;
    private int gridX, gridY, gridW, gridH;


    private int selectedTab  = 0;
    private int scrollRow    = 0;
    private int hoveredIndex = -1;
    private String searchText = "";
    private boolean searchFocused = false;

    private List<EmojiEntry> currentList = new ArrayList<>();
    private List<String>     catNames;
    private int              totalRows;
    private int tabOffset = 0;
    private static final int VISIBLE_TABS = 4;

    private boolean draggingScroll = false;
    private int     scrollBarX, scrollBarY0, scrollBarH;

    public EmojiScreen() {
        super(Component.literal("Emoji"));
    }

    // Init

    @Override
    protected void init() {
        catNames   = EmojiRegistry.categoryNames();
        panelW     = COLS * CELL + PAD * 2 + 8;
        panelH     = TAB_H + SEARCH_H + 4 + GRID_ROWS * CELL + PAD + STATUS_H + PAD;
        panelX     = (width  - panelW) / 2;
        panelY     = (height - panelH) / 2;

        gridX = panelX + PAD;
        gridY = panelY + TAB_H + SEARCH_H + 4;
        gridW = COLS * CELL;
        gridH = GRID_ROWS * CELL;

        scrollBarX = gridX + gridW + 2;
        scrollBarY0 = gridY;
        scrollBarH  = gridH;

        refreshList();
    }

    private void refreshList() {
        scrollRow = 0;
        if (!searchText.isEmpty()) {
            currentList = EmojiRegistry.search(searchText);
        } else if (!catNames.isEmpty()) {
            currentList = new ArrayList<>(EmojiRegistry.BY_CATEGORY
                    .getOrDefault(catNames.get(selectedTab), List.of()));
        } else {
            currentList = new ArrayList<>(EmojiRegistry.ALL);
        }
        totalRows = (int) Math.ceil((double) currentList.size() / COLS);

        prefetchVisible();
    }

    private void prefetchVisible() {
        int startIdx = scrollRow * COLS;
        int endIdx   = Math.min(startIdx + GRID_ROWS * COLS + COLS, currentList.size());
        EmojiTextureManager.prefetch(currentList.subList(startIdx, endIdx));
    }

    // render

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, width, height, 0x88000000);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {

        super.render(g, mx, my, pt);

        g.fill(panelX - 3, panelY - 3, panelX + panelW + 3, panelY + panelH + 3, 0x66000000);
        g.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xF0111118);
        drawBorder(g, panelX, panelY, panelX + panelW, panelY + panelH, 0xFF3A3A5C);

        renderTabs(g, mx, my);
        renderSearch(g, mx, my);
        renderGrid(g, mx, my);
        renderScrollBar(g, mx, my);
        renderStatus(g);
    }

    private void renderTabs(GuiGraphics g, int mx, int my) {
        if (catNames.isEmpty()) return;

        int arrowW = 20;
        int tabsAreaW = panelW - (arrowW * 2);
        int tabW = tabsAreaW / VISIBLE_TABS;

        boolean hoverLeft = mx >= panelX && mx < panelX + arrowW && my >= panelY && my < panelY + TAB_H;
        g.fill(panelX, panelY, panelX + arrowW, panelY + TAB_H, hoverLeft ? 0xFF333344 : 0xFF1A1A28);
        g.drawCenteredString(font, "<", panelX + arrowW / 2, panelY + (TAB_H - 8) / 2, tabOffset > 0 ? 0xFFFFFFFF : 0xFF555555);

        for (int i = 0; i < VISIBLE_TABS; i++) {
            int index = tabOffset + i;
            if (index >= catNames.size()) break;

            int tx = panelX + arrowW + i * tabW;
            int ty = panelY;
            boolean active = (index == selectedTab) && searchText.isEmpty();
            boolean hovered = mx >= tx && mx < tx + tabW && my >= ty && my < ty + TAB_H;

            g.fill(tx, ty, tx + tabW, ty + TAB_H, active ? 0xFF1E1E30 : hovered ? 0xFF1A1A28 : 0xFF0D0D1A);
            if (active) g.fill(tx, ty + TAB_H - 2, tx + tabW, ty + TAB_H, 0xFFFFDD44);

            String label = catNames.get(index).split(" ")[0];
            g.drawCenteredString(font, label, tx + tabW / 2, ty + (TAB_H - 8) / 2, active ? 0xFFFFFFFF : 0xFF888888);
        }

        int rightArrowX = panelX + panelW - arrowW;
        boolean hoverRight = mx >= rightArrowX && mx < rightArrowX + arrowW && my >= panelY && my < panelY + TAB_H;
        g.fill(rightArrowX, panelY, rightArrowX + arrowW, panelY + TAB_H, hoverRight ? 0xFF333344 : 0xFF1A1A28);
        g.drawCenteredString(font, ">", rightArrowX + arrowW / 2, panelY + (TAB_H - 8) / 2,
                (tabOffset + VISIBLE_TABS < catNames.size()) ? 0xFFFFFFFF : 0xFF555555);
    }

    private void renderSearch(GuiGraphics g, int mx, int my) {
        int sx = panelX + PAD;
        int sy = panelY + TAB_H + 2;
        int sw = panelW - PAD * 2;
        int sh = SEARCH_H - 2;

        g.fill(sx, sy, sx + sw, sy + sh, searchFocused ? 0xFF1E1E30 : 0xFF151520);
        drawBorder(g, sx, sy, sx + sw, sy + sh,
                searchFocused ? 0xFF5555AA : 0xFF333355);

        String display = searchText.isEmpty() && !searchFocused
                ? "§7🔍 Search..." : "🔍 " + searchText + (searchFocused ? "§e|" : "");
        g.drawString(font, display, sx + 4, sy + (sh - 8) / 2, 0xFFCCCCCC, false);
    }

    private void renderGrid(GuiGraphics g, int mx, int my) {
        hoveredIndex = -1;

        g.fill(gridX, gridY, gridX + gridW, gridY + gridH, 0xFF0A0A14);

        int startIdx = scrollRow * COLS;
        int visible  = GRID_ROWS * COLS;

        for (int vi = 0; vi < visible; vi++) {
            int absIdx = startIdx + vi;
            if (absIdx >= currentList.size()) break;

            EmojiEntry entry = currentList.get(absIdx);
            int col = vi % COLS;
            int row = vi / COLS;
            int cx  = gridX + col * CELL;
            int cy  = gridY + row * CELL;

            boolean hovered = mx >= cx && mx < cx + CELL && my >= cy && my < cy + CELL;
            if (hovered) {
                hoveredIndex = absIdx;
                g.fill(cx + 2, cy + 2, cx + CELL - 2, cy + CELL - 2, 0x66FFDD44);
            }

            int iconSize = CELL - 6;
            int offset = 3;

            ResourceLocation tex = EmojiTextureManager.getTexture(entry);
            if (tex != null) {
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
                g.blit(tex, cx + offset, cy + offset, 0, 0, iconSize, iconSize, iconSize, iconSize);
            } else {
                g.drawCenteredString(font, entry.character(), cx + CELL / 2, cy + (CELL - 8) / 2, 0xFFAAAAAA);
                long t = System.currentTimeMillis() / 300;
                if ((t + absIdx) % 3 == 0) {
                    g.drawCenteredString(font, entry.character(), cx + CELL / 2, cy + (CELL - 8) / 2, 0xFFAAAAAA);
                }
            }
        }

        if (hoveredIndex >= 0) {
            EmojiEntry e = currentList.get(hoveredIndex);
            g.renderTooltip(font, Component.literal(e.character() + " " + e.name()), mx, my);
        }
    }

    private void renderScrollBar(GuiGraphics g, int mx, int my) {
        if (totalRows <= GRID_ROWS) return;

        int trackX = scrollBarX;
        int trackH = scrollBarH;
        g.fill(trackX, scrollBarY0, trackX + 4, scrollBarY0 + trackH, 0xFF1A1A2E);

        int thumbH  = Math.max(16, trackH * GRID_ROWS / totalRows);
        int thumbY  = scrollBarY0 + (trackH - thumbH) * scrollRow / Math.max(1, totalRows - GRID_ROWS);
        boolean hot = mx >= trackX && mx < trackX + 6 && my >= thumbY && my < thumbY + thumbH;
        g.fill(trackX, thumbY, trackX + 4, thumbY + thumbH, hot || draggingScroll ? 0xFFFFDD44 : 0xFF555588);
    }

    private void renderStatus(GuiGraphics g) {
        int loaded = EmojiTextureManager.totalLoaded();
        int total  = EmojiTextureManager.totalEmojis();
        int sy     = panelY + panelH - STATUS_H;

        g.fill(panelX, sy, panelX + panelW, sy + STATUS_H, 0xFF0A0A14);

        String msg;
        int    col;

        if (loaded >= total) {
            msg = Component.translatable("gui.emotion_overlays.status.loaded", total).getString();
            col = 0xFF66DD66;
        } else {
            int pct = total > 0 ? loaded * 100 / total : 0;

            msg = Component.translatable("gui.emotion_overlays.status.loading", loaded, total, pct).getString();
            col = 0xFF888888;

            int barW = (panelW - PAD * 2) * loaded / Math.max(1, total);
            g.fill(panelX + PAD, sy + STATUS_H - 3, panelX + PAD + barW, sy + STATUS_H - 1, 0xFF5555AA);
        }

        g.drawCenteredString(font, msg, panelX + panelW / 2, sy + 2, col);
    }

    //Input

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        int imx = (int)mx, imy = (int)my;
        int arrowW = 20;

        if (imy >= panelY && imy < panelY + TAB_H) {
            if (imx >= panelX && imx < panelX + arrowW) {
                if (tabOffset > 0) tabOffset--;
                return true;
            }

            int rightArrowX = panelX + panelW - arrowW;
            if (imx >= rightArrowX && imx < rightArrowX + arrowW) {
                if (tabOffset + VISIBLE_TABS < catNames.size()) tabOffset++;
                return true;
            }

            int tabsAreaX = panelX + arrowW;
            int tabW = (panelW - arrowW * 2) / VISIBLE_TABS;

            if (imx >= tabsAreaX && imx < rightArrowX) {
                int clickedVisibleIdx = (imx - tabsAreaX) / tabW;
                int actualIdx = tabOffset + clickedVisibleIdx;

                if (actualIdx < catNames.size()) {
                    selectedTab = actualIdx;
                    searchText = "";
                    searchFocused = false;
                    refreshList();
                    return true;
                }
            }
        }

        if (!catNames.isEmpty() && imy >= panelY && imy < panelY + TAB_H) {
            int tabW = panelW / catNames.size();
            int ti   = (imx - panelX) / tabW;
            if (ti >= 0 && ti < catNames.size()) {
                selectedTab  = ti;
                searchText   = "";
                searchFocused = false;
                refreshList();
                return true;
            }
        }

        int sx = panelX + PAD, sy = panelY + TAB_H + 2;
        if (imx >= sx && imx < sx + panelW - PAD * 2 && imy >= sy && imy < sy + SEARCH_H) {
            searchFocused = true;
            return true;
        } else {
            searchFocused = false;
        }


        if (imx >= scrollBarX && imx < scrollBarX + 8) {
            draggingScroll = true;
            return true;
        }


        if (hoveredIndex >= 0 && hoveredIndex < currentList.size()) {
            EmojiEntry selected = currentList.get(hoveredIndex);
            ClientPlayNetworking.send(new SelectEmojiPacket(selected.cpHex()));
            onClose();
            return true;
        }

        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        draggingScroll = false;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (draggingScroll && totalRows > GRID_ROWS) {
            double frac = (my - scrollBarY0) / (double) scrollBarH;
            scrollRow = (int) Math.max(0, Math.min(totalRows - GRID_ROWS, frac * totalRows));
            prefetchVisible();
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double scrollX, double scrollY) {
        scrollRow = (int) Math.max(0, Math.min(totalRows - GRID_ROWS, scrollRow - scrollY));
        prefetchVisible();
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (searchFocused) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                if (!searchText.isEmpty()) { searchText = ""; refreshList(); }
                else { searchFocused = false; }
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !searchText.isEmpty()) {
                searchText = searchText.substring(0, searchText.length() - 1);
                refreshList();
                return true;
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char c, int modifiers) {
        if (searchFocused && c >= ' ' && searchText.length() < 32) {
            searchText += c;
            refreshList();
            return true;
        }
        return false;
    }

    @Override
    public boolean isPauseScreen() { return false; }

    //

    private static void drawBorder(GuiGraphics g, int x0, int y0, int x1, int y1, int color) {
        g.fill(x0, y0,     x1, y0 + 1, color);
        g.fill(x0, y1 - 1, x1, y1,     color);
        g.fill(x0, y0,     x0 + 1, y1, color);
        g.fill(x1 - 1, y0, x1,     y1, color);
    }


}
