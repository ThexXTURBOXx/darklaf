package com.weis.darklaf.ui.tabbedpane;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class TabbedPaneUtil implements SwingConstants {

    private static final Rectangle EMPTY_RECT = new Rectangle(0, 0, 0, 0);

    public static int getDroppedTabIndex(final Rectangle tabBounds,
                                         @NotNull final JTabbedPane tabbedPane,
                                         final DarkTabbedPaneUI ui,
                                         @NotNull final Point p) {
        var tab = tabbedPane.indexAtLocation(p.x, p.y);
        if (ui != null) {
            if (tab == -1) {
                var bounds = ui.getTabAreaBounds();
                if (bounds.contains(p)) {
                    if (tabbedPane.getTabCount() > 0) {
                        var minb = ui.getTabBounds(tabbedPane, 0);
                        var maxb = ui.getTabBounds(tabbedPane, tabbedPane.getTabCount() - 1);
                        if (tabbedPane.getComponentOrientation().isLeftToRight()) {
                            int x = Math.max(bounds.x, minb.x);
                            bounds.width = Math.min(bounds.x + bounds.width - x, maxb.x + maxb.width - x);
                            bounds.x = x;
                        } else {
                            int x = Math.max(bounds.x, maxb.x);
                            bounds.width = Math.min(bounds.x + bounds.width - x, minb.x + minb.width - x);
                            bounds.x = x;
                        }
                        int y = Math.max(bounds.y, minb.y);
                        bounds.height = Math.min(bounds.y + bounds.height - y, maxb.x + maxb.height - y);
                    }

                    int tabPlacement = tabbedPane.getTabPlacement();
                    if (tabPlacement == TOP || tabPlacement == BOTTOM) {
                        if (tabbedPane.getComponentOrientation().isLeftToRight()) {
                            tab = p.x <= bounds.x + bounds.width / 2 ? 0 : tabbedPane.getTabCount();
                        } else {
                            tab = p.x >= bounds.x + bounds.width / 2 ? 1 : tabbedPane.getTabCount();
                        }
                    } else if (tabPlacement == LEFT || tabPlacement == RIGHT) {
                        tab = p.y <= bounds.y + bounds.height / 2 ? 0 : tabbedPane.getTabCount();
                    }
                }
            } else {
                if (tab < tabbedPane.getTabCount()) {
                    var b = tabbedPane.getBoundsAt(tab);

                    if (tab >= 1 && !ui.scrollableTabLayoutEnabled()) {
                        var prev = tabbedPane.getBoundsAt(tab - 1);
                        if (b.y + b.height < p.y && prev.y <= p.y && p.y <= prev.y + prev.height) {
                            b = prev;
                        }
                    }

                    var sb = (ui.scrollableTabLayoutEnabled()) ? tabBounds : EMPTY_RECT;
                    switch (tabbedPane.getTabPlacement()) {
                        case TOP:
                        case BOTTOM:
                            if (tabbedPane.getComponentOrientation().isLeftToRight()) {
                                if (p.x >= b.x + sb.width + (b.width - sb.width) / 2) {
                                    tab += 1;
                                }
                            } else {
                                if (p.x <= b.x + (b.width - sb.width) / 2) {
                                    tab += 1;
                                }
                            }
                            break;
                        case LEFT:
                        case RIGHT:
                            if (p.y >= b.y + sb.height + (b.height - sb.height) / 2) {
                                tab += 1;
                            }
                            break;
                    }
                }
            }
        } else if (tab == -1) {
            tab = tabbedPane.getTabCount();
        }
        return tab;
    }

    @Contract("_, _, _, _, _, _, _, _ -> param5")
    public static Rectangle getDropRect(@NotNull final DarkTabbedPaneUI ui,
                                        @NotNull final JTabbedPane destTabbedPane, final JTabbedPane source,
                                        final Point mouseLocation, final Rectangle tabBounds,
                                        final int tab, final int sourceIndex, final int lastTab) {
        int tabPlacement = destTabbedPane.getTabPlacement();
        Rectangle destRect = destTabbedPane.getBoundsAt(Math.min(tab, destTabbedPane.getTabCount() - 1));

        if (ui.scrollableTabLayoutEnabled()) {
            boolean lastInSource = false;
            if (destTabbedPane == source && (tab == sourceIndex || (sourceIndex == source.getTabCount() - 1
                    && tab == source.getTabCount()))) {
                lastInSource = true;
                destRect.width = tabBounds.width;
                destRect.height = tabBounds.height;
            }

            switch (tabPlacement) {
                case TOP:
                case BOTTOM:
                    if (destTabbedPane.getComponentOrientation().isLeftToRight()) {
                        if (tab >= destTabbedPane.getTabCount() && !lastInSource) {
                            destRect.x += destRect.width;
                        }
                        tabBounds.x = destRect.x;
                        if (lastTab != -1 && lastTab < tab) {
                            tabBounds.x -= tabBounds.width;
                        }
                    } else {
                        if (tab >= destTabbedPane.getTabCount()) {
                            destRect.x -= 2 * tabBounds.width;
                            if (lastTab < tab) {
                                destRect.x += destRect.width;
                            }
                        }
                        tabBounds.x = destRect.x + tabBounds.width;
                        if (lastTab != -1 && lastTab > tab) {
                            tabBounds.x -= tabBounds.width;
                        }
                    }
                    tabBounds.y = destRect.y + destRect.height - tabBounds.height;
                    break;
                case LEFT:
                case RIGHT:
                    if (tab >= destTabbedPane.getTabCount()) {
                        destRect.y += destRect.height;
                    }
                    tabBounds.y = destRect.y;
                    tabBounds.x = destRect.x + destRect.width - tabBounds.width;
                    if (lastTab != -1 && lastTab < tab) {
                        tabBounds.y -= tabBounds.height;
                    }
                    break;
            }
        } else {
            if (source == destTabbedPane && (tab == sourceIndex || tab == sourceIndex + 1)) {
                tabBounds.setRect(0, 0, 0, 0);
            } else {
                int placement = destTabbedPane.getTabPlacement();
                if (placement == TOP || placement == BOTTOM) {
                    var b = ui.getTabAreaBounds();
                    if (tab == destTabbedPane.getTabCount()) {
                        tabBounds.x = destRect.x + destRect.width / 2;
                        tabBounds.width = Math.min(b.x + b.width - tabBounds.x, tabBounds.width);
                    } else if (tab == 0) {
                        tabBounds.x = destRect.x;
                        tabBounds.width = Math.min(tabBounds.width / 2, destRect.width / 2);
                    } else {
                        var prev = destTabbedPane.getBoundsAt(tab - 1);
                        if (destRect.y + destRect.height <= mouseLocation.y &&
                                prev.y <= mouseLocation.y && mouseLocation.y <= prev.y + prev.height) {
                            destRect.x = prev.x + prev.width;
                            destRect.y = prev.y;
                            destRect.height = prev.height;
                        }

                        tabBounds.x = destRect.x;
                        tabBounds.setLocation(destRect.getLocation());
                        tabBounds.x -= tabBounds.width / 2;
                        if (tabBounds.x < b.x) {
                            int diff = b.x - tabBounds.x;
                            tabBounds.width -= diff;
                            tabBounds.x = b.x;
                        }
                        if (tabBounds.x + tabBounds.width > b.x + b.width) {
                            int diff = tabBounds.width + tabBounds.x - b.width - b.x;
                            tabBounds.width -= diff;
                        }
                    }
                    tabBounds.y = destRect.y + destRect.height - tabBounds.height;
                } else if (placement == LEFT || placement == RIGHT) {
                    //Todo
                }
            }
        }
        return tabBounds;
    }

    public static boolean moveTabs(final JTabbedPane sourcePane, final JTabbedPane tabbedPane,
                                   final int sourceIndex, final int tab) {

        if (tabbedPane == sourcePane && sourceIndex == tab) {
            //Nothing to do. Just select the tab to be sure.
            selectTab(sourcePane, sourceIndex);
            return false;
        }
        if (tab < 0 || tab > tabbedPane.getTabCount()) {
            return false;
        }

        String tabName = sourcePane.getTitleAt(sourceIndex);
        Icon icon = sourcePane.getIconAt(sourceIndex);
        Component comp = sourcePane.getComponentAt(sourceIndex);
        String toolTip = sourcePane.getToolTipTextAt(sourceIndex);
        Color foreground = sourcePane.getForegroundAt(sourceIndex);
        Component tabComp = sourcePane.getTabComponentAt(sourceIndex);

        tabbedPane.insertTab(tabName, icon, comp, toolTip, tab);

        int index = tab;
        if (tabbedPane == sourcePane) {
            if (sourceIndex < index) index--;
        }

        if (tabComp != null) {
            tabbedPane.setTabComponentAt(index, tabComp);
        }
        tabbedPane.setForegroundAt(index, foreground);
        selectTab(tabbedPane, index);
        return true;
    }

    /**
     * Selects the specified tab in the specified tabbed pane.  This method
     * can be overridden by subclasses to do more stuff than simply select
     * the tab.
     *
     * @param tabbedPane The tabbed pane.
     * @param index      The index of the tab to select.
     */
    protected static void selectTab(final JTabbedPane tabbedPane, final int index) {
        SwingUtilities.invokeLater(() -> {
            tabbedPane.setSelectedIndex(index);
            tabbedPane.requestFocus();
        });
    }
}
