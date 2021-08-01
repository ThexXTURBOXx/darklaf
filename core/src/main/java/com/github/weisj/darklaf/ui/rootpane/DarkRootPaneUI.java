/*
 * MIT License
 *
 * Copyright (c) 2021 Jannis Weis
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package com.github.weisj.darklaf.ui.rootpane;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.beans.PropertyChangeEvent;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicRootPaneUI;

import com.github.weisj.darklaf.LafManager;
import com.github.weisj.darklaf.platform.DecorationsHandler;
import com.github.weisj.darklaf.platform.decorations.CustomTitlePane;
import com.github.weisj.darklaf.ui.util.DarkUIUtil;
import com.github.weisj.darklaf.util.PropertyKey;
import com.github.weisj.darklaf.util.PropertyUtil;

/** @author Jannis Weis */
public class DarkRootPaneUI extends BasicRootPaneUI implements HierarchyListener {

    protected static final String KEY_PREFIX = "JRootPane.";
    public static final String HIDE_TITLEBAR = KEY_PREFIX + "hideTitleBar";
    public static final String KEY_NO_DECORATIONS_UPDATE = KEY_PREFIX + "noDecorationsUpdate";
    public static final String KEY_NO_DECORATIONS = KEY_PREFIX + "noDecorations";
    public static final String KEY_UNIFIED_MENUBAR = KEY_PREFIX + "unifiedMenuBar";
    public static final String KEY_COLORED_TITLE_BAR = KEY_PREFIX + "coloredTitleBar";
    protected static final String[] borderKeys = new String[] {"RootPane.border", "RootPane.frameBorder",
            "RootPane.plainDialogBorder", "RootPane.informationDialogBorder", "RootPane.errorDialogBorder",
            "RootPane.colorChooserDialogBorder", "RootPane.fileChooserDialogBorder",
            "RootPane.questionDialogBorder", "RootPane.warningDialogBorder"};

    private Window window;
    private CustomTitlePane titlePane;
    private LayoutManager layoutManager;
    private LayoutManager oldLayout;
    private JRootPane rootPane;

    private int windowDecorationsStyle = -1;

    public static ComponentUI createUI(final JComponent comp) {
        return new DarkRootPaneUI();
    }

    @Override
    public void installUI(final JComponent c) {
        super.installUI(c);
        rootPane = (JRootPane) c;
        windowDecorationsStyle = rootPane.getWindowDecorationStyle();
        updateClientDecoration();
        installBorder(rootPane);
        installListeners(rootPane);
    }

    @Override
    protected void installDefaults(final JRootPane c) {
        super.installDefaults(c);
        PropertyUtil.installBooleanProperty(c, KEY_UNIFIED_MENUBAR, "TitlePane.unifiedMenuBar");
        PropertyUtil.installBooleanProperty(c, KEY_COLORED_TITLE_BAR, "macos.coloredTitleBar");
        LookAndFeel.installColors(c, "RootPane.background", "RootPane.foreground");
    }

    protected void installBorder(final JRootPane root) {
        if (root == null) return;
        LookAndFeel.installBorder(root, borderKeys[windowDecorationsStyle]);
    }

    @Override
    public void uninstallUI(final JComponent c) {
        super.uninstallUI(c);
        uninstallClientDecorations(rootPane);
        uninstallBorder(rootPane);
        uninstallListeners(rootPane);
        layoutManager = null;
        rootPane = null;
    }

    private static void uninstallBorder(final JRootPane root) {
        LookAndFeel.uninstallBorder(root);
    }

    @Override
    public void propertyChange(final PropertyChangeEvent e) {
        super.propertyChange(e);
        String propertyName = e.getPropertyName();
        if (PropertyKey.ANCESTOR.equals(propertyName) || KEY_NO_DECORATIONS.equals(propertyName)) {
            updateWindow(rootPane.getParent());
        }
    }

    protected int decorationsStyleFromWindow(final Window window, final int windowDecorationsStyle) {
        if (DarkUIUtil.isUndecorated(window) || PropertyUtil.getBooleanProperty(rootPane, KEY_NO_DECORATIONS))
            return JRootPane.NONE;
        if (windowDecorationsStyle != JRootPane.NONE) return windowDecorationsStyle;
        if (window instanceof JFrame) return JRootPane.FRAME;
        if (window instanceof JDialog) return JRootPane.PLAIN_DIALOG;
        return windowDecorationsStyle;
    }

    private void uninstallClientDecorations(final JRootPane root) {
        if (root != null) {
            uninstallBorder(root);
            root.removeHierarchyListener(this);
            if (titlePane != null) {
                boolean removeDecorations = !LafManager.isInstalled() || !LafManager.isDecorationsEnabled();
                titlePane.uninstall(removeDecorations);
                setTitlePane(root, null);
            }
            uninstallLayout(root);
            int style = root.getWindowDecorationStyle();
            if (style == JRootPane.NONE) {
                root.repaint();
                root.revalidate();
            }
        }
        if (window != null) {
            window.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
        window = null;
    }

    private void uninstallLayout(final JRootPane root) {
        if (oldLayout != null) {
            root.setLayout(oldLayout);
            oldLayout = null;
        }
    }

    private void installClientDecorations(final JRootPane root) {
        updateWindow(rootPane.getParent());
        int style = decorationsStyleFromWindow(window,
                windowDecorationsStyle < 0 ? root.getWindowDecorationStyle() : windowDecorationsStyle);
        CustomTitlePane titlePane = DecorationsHandler.getSharedInstance().createTitlePane(root, style, window);
        installLayout(root);
        setTitlePane(root, titlePane);
        if (titlePane != null) titlePane.setDecorationsStyle(windowDecorationsStyle);
    }

    @Override
    protected void installListeners(final JRootPane root) {
        root.addPropertyChangeListener(this);
        root.addHierarchyListener(this);
    }

    @Override
    protected void uninstallListeners(final JRootPane root) {
        root.removePropertyChangeListener(this);
        root.removeHierarchyListener(this);
    }

    private void setTitlePane(final JRootPane root, final CustomTitlePane titlePane) {
        JLayeredPane layeredPane = root.getLayeredPane();
        JComponent oldTitlePane = getTitlePane();

        if (oldTitlePane != null) {
            layeredPane.remove(oldTitlePane);
        }
        if (titlePane != null) {
            layeredPane.add(titlePane, JLayeredPane.FRAME_CONTENT_LAYER);
            titlePane.setVisible(true);
        }
        this.titlePane = titlePane;
    }

    private void updateWindow(final Component parent) {
        window = DarkUIUtil.getWindow(parent);
        windowDecorationsStyle = decorationsStyleFromWindow(window, windowDecorationsStyle);
        if (titlePane != null) titlePane.setDecorationsStyle(windowDecorationsStyle);
        installBorder(rootPane);
    }

    private void installLayout(final JRootPane root) {
        if (layoutManager == null) {
            layoutManager = createLayoutManager();
        }
        oldLayout = root.getLayout();
        root.setLayout(layoutManager);
    }

    protected CustomTitlePane getTitlePane() {
        return titlePane;
    }

    protected LayoutManager createLayoutManager() {
        return new DarkSubstanceRootLayout();
    }

    @Override
    public void hierarchyChanged(final HierarchyEvent e) {
        if (rootPane == null) return;
        Component parent = rootPane.getParent();
        if (parent == null) {
            return;
        }
        if (parent.getClass().getName().startsWith("org.jdesktop.jdic.tray")
                || (parent.getClass().getName().equals("javax.swing.Popup$HeavyWeightWindow"))) {
            SwingUtilities.invokeLater(() -> {
                if (rootPane != null) {
                    rootPane.removeHierarchyListener(this);
                }
            });
        }
        if (e.getChangeFlags() == HierarchyEvent.PARENT_CHANGED) {
            if (DarkUIUtil.getWindow(rootPane) != window) {
                updateClientDecoration();
            }
        }
    }

    protected void updateClientDecoration() {
        if (decorationsEnabled(rootPane)) {
            uninstallClientDecorations(rootPane);
            if (DecorationsHandler.getSharedInstance().isCustomDecorationSupported()) {
                installClientDecorations(rootPane);
            }
        }
    }

    protected boolean decorationsEnabled(final JRootPane rootPane) {
        return !(rootPane.getParent() instanceof JInternalFrame)
                && !PropertyUtil.getBooleanProperty(rootPane, KEY_NO_DECORATIONS_UPDATE)
                && rootPane.getParent() != null;
    }
}
