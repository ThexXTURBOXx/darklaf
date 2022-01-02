/*
 * MIT License
 *
 * Copyright (c) 2019-2021 Jannis Weis
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
 */
package com.github.weisj.darklaf.platform.windows.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.logging.Logger;

import javax.accessibility.AccessibleContext;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.UIResource;

import com.github.weisj.darklaf.platform.decorations.CustomTitlePane;
import com.github.weisj.darklaf.platform.windows.JNIDecorationsWindows;
import com.github.weisj.darklaf.platform.windows.PointerUtil;
import com.github.weisj.darklaf.properties.icons.ScaledIcon;
import com.github.weisj.darklaf.properties.icons.ToggleIcon;
import com.github.weisj.darklaf.util.LogUtil;
import com.github.weisj.darklaf.util.PropertyKey;
import com.github.weisj.darklaf.util.PropertyUtil;
import com.github.weisj.darklaf.util.Scale;
import com.github.weisj.darklaf.util.StringUtil;

/**
 * Swing implementation of the native windows titlebar.
 *
 * @author Jannis Weis
 */
public class WindowsTitlePane extends CustomTitlePane {
    public static final String KEY_RESIZABLE = "resizable";
    public static final String KEY_STATE = "state";
    public static final String KEY_ICON_IMAGE = "iconImage";

    private static final Logger LOGGER = LogUtil.getLogger(WindowsTitlePane.class);
    private static final int PAD = 5;
    private static final int BAR_HEIGHT = 28;
    private static final int BUTTON_WIDTH = 46;
    private static final int ICON_WIDTH = 32;
    private static final int ICON_SIZE = ICON_WIDTH - 3 * PAD;
    private final JRootPane rootPane;

    private boolean titleBarHidden;
    private final MenuBarStealer menuBarStealer;

    private boolean oldResizable;
    private PropertyChangeListener windowPropertyChangeListener;
    private PropertyChangeListener rootPanePropertyChangeListener;
    private WindowListener windowListener;
    private ToggleIcon closeIcon;
    private ToggleIcon maximizeIcon;
    private ToggleIcon restoreIcon;
    private ToggleIcon minimizeIcon;
    private JButton windowIconButton;
    private JButton closeButton;
    private JButton maximizeToggleButton;
    private JButton minimizeButton;
    private Action closeAction;
    private Action restoreAction;
    private Action maximizeAction;
    private Action minimizeAction;
    private JLabel titleLabel;
    private final Window window;
    private long windowHandle;
    private int state;

    private Color inactiveBackground;
    private Color inactiveForeground;
    private Color inactiveHover;
    private Color inactiveClick;
    private Color activeBackground;
    private Color activeForeground;
    private Color border;
    private Border oldBorder;

    private int left;
    private int right;
    private int height;

    private GraphicsConfiguration gc;

    public WindowsTitlePane(final JRootPane root, final int decorationStyle, final Window window) {
        this.rootPane = root;
        this.window = window;
        this.decorationStyle = decorationStyle;
        this.menuBarStealer = new MenuBarStealer(rootPane, this);

        state = -1;
        oldResizable = true;
        installSubcomponents();
        menuBarStealer.install();
        updateTitleBarVisibility();
        installDefaults();
        setLayout(createLayout());
    }

    private void updateTitleBarVisibility() {
        titleBarHidden = PropertyUtil.getBooleanProperty(rootPane, "JRootPane.hideTitleBar");
        rootPane.doLayout();
        rootPane.repaint();
    }

    private static JButton createButton(final Icon icon, final Action action) {
        return createButton(icon, action, false);
    }

    private static JButton createButton(final Icon icon, final Action action, final boolean close) {
        JButton button = new JButton(action);
        button.setRolloverEnabled(true);
        if (close) {
            button.putClientProperty("JButton.borderless.hover",
                    UIManager.getColor("Windows.TitlePane.close.rollOverColor"));
            button.putClientProperty("JButton.borderless.click",
                    UIManager.getColor("Windows.TitlePane.close.clickColor"));
        }
        button.putClientProperty("JButton.noBorderlessOverwrite", true);
        button.setFocusable(false);
        button.setOpaque(false);
        button.setRolloverEnabled(true);
        button.putClientProperty("JButton.variant", "borderlessRectangular");
        button.putClientProperty("paintActive", Boolean.TRUE);
        button.putClientProperty(AccessibleContext.ACCESSIBLE_NAME_PROPERTY, button.getText());
        button.putClientProperty("JToolTip.style", "plain");
        button.setToolTipText(button.getText());
        button.setIcon(icon);
        button.setText(null);
        return button;
    }

    @Override
    public void uninstall(final boolean removeDecorations) {
        uninstallListeners();
        uninstallDecorations(removeDecorations);
        removeAll();
        menuBarStealer.uninstall();
    }

    private void uninstallListeners() {
        if (window != null) {
            window.removeWindowListener(windowListener);
            window.removePropertyChangeListener(windowPropertyChangeListener);
        }
        if (rootPane != null) {
            rootPane.removePropertyChangeListener(rootPanePropertyChangeListener);
        }
    }

    protected void uninstallDecorations(final boolean removeDecorations) {
        if (windowHandle != 0) {
            if (removeDecorations) {
                JNIDecorationsWindows.uninstallDecorations(windowHandle, decorationStyle != JRootPane.NONE);
            }
            windowHandle = 0;
        }
    }

    @Override
    public void install() {
        if (window != null) {
            if (!installDecorations()) return;
            if (window instanceof Frame) {
                titleLabel.setText(StringUtil.orEmpty(((Frame) window).getTitle()));
                setState(((Frame) window).getExtendedState());
            } else {
                setState(0);
            }
            if (window instanceof Dialog) {
                titleLabel.setText(StringUtil.orEmpty(((Dialog) window).getTitle()));
            }
            determineColors();
            setActive(window.isActive());
            installListeners();
            if (getComponentCount() == 0) {
                addComponents();
            }
            updateSystemIcon(getGraphicsConfiguration());
        }
    }

    @Override
    public void setDecorationsStyle(final int style) {
        super.setDecorationsStyle(style);
        if (style == JRootPane.NONE && windowHandle != 0) {
            uninstall(true);
        } else if (windowHandle == 0) {
            install();
        }
    }

    private boolean installDecorations() {
        if (getDecorationStyle() == JRootPane.NONE) return false;
        if (!window.isDisplayable()) return false;
        if (window instanceof Dialog || window instanceof Frame) {
            windowHandle = PointerUtil.getHWND(window);
            if (windowHandle > 0) {
                LOGGER.fine("Installing decorations for window " + windowHandle);
                if (!JNIDecorationsWindows.installDecorations(windowHandle)) {
                    LOGGER.fine("Already installed.");
                }
                updateResizeBehaviour();
                Color color = rootPane.getBackground();
                JNIDecorationsWindows.setBackground(windowHandle, color.getRed(), color.getGreen(), color.getBlue());
            } else {
                uninstall(decorationStyle != JRootPane.NONE);
                return false;
            }
        }
        return true;
    }

    private void installListeners() {
        if (window != null) {
            windowListener = createWindowListener();
            window.addWindowListener(windowListener);
            windowPropertyChangeListener = createWindowPropertyChangeListener();
            rootPanePropertyChangeListener = createRootPanePropertyChangeListener();
            rootPane.addPropertyChangeListener(rootPanePropertyChangeListener);
            window.addPropertyChangeListener(windowPropertyChangeListener);
        }
    }

    private WindowListener createWindowListener() {
        return new WindowsTitlePane.WindowHandler();
    }

    private PropertyChangeListener createWindowPropertyChangeListener() {
        return new WindowPropertyChangeListener();
    }

    private PropertyChangeListener createRootPanePropertyChangeListener() {
        return new RootPanePropertyChangeListener();
    }

    private void installSubcomponents() {
        titleLabel = new JLabel();
        titleLabel.setHorizontalAlignment(JLabel.LEFT);

        createIcons();
        createActions();
        createButtons();

        addComponents();
    }

    private void addComponents() {
        add(windowIconButton);
        add(minimizeButton);
        add(maximizeToggleButton);
        add(closeButton);
        add(titleLabel);
        setComponentZOrder(closeButton, 0);
        setComponentZOrder(maximizeToggleButton, 1);
        setComponentZOrder(minimizeButton, 2);
        setComponentZOrder(windowIconButton, 3);
        setComponentZOrder(titleLabel, 4);
    }

    private void determineColors() {
        switch (getDecorationStyle()) {
            case JRootPane.ERROR_DIALOG:
                activeBackground = UIManager.getColor("Windows.OptionPane.errorDialog.titlePane.background");
                activeForeground = UIManager.getColor("Windows.OptionPane.errorDialog.titlePane.foreground");
                break;
            case JRootPane.QUESTION_DIALOG:
            case JRootPane.COLOR_CHOOSER_DIALOG:
            case JRootPane.FILE_CHOOSER_DIALOG:
                activeBackground = UIManager.getColor("Windows.OptionPane.questionDialog.titlePane.background");
                activeForeground = UIManager.getColor("Windows.OptionPane.questionDialog.titlePane.foreground");
                break;
            case JRootPane.WARNING_DIALOG:
                activeBackground = UIManager.getColor("Windows.OptionPane.warningDialog.titlePane.background");
                activeForeground = UIManager.getColor("Windows.OptionPane.warningDialog.titlePane.foreground");
                break;
            default: // JRootPane.Frame
                activeBackground = UIManager.getColor("Windows.TitlePane.background");
                activeForeground = UIManager.getColor("Windows.TitlePane.foreground");
                break;
        }
        inactiveBackground = UIManager.getColor("Windows.TitlePane.inactiveBackground");
        inactiveForeground = UIManager.getColor("Windows.TitlePane.inactiveForeground");
        inactiveHover = UIManager.getColor("Windows.TitlePane.inactiveBackgroundHover");
        inactiveClick = UIManager.getColor("Windows.TitlePane.inactiveBackgroundClick");
        border = UIManager.getColor("Windows.TitlePane.borderColor");

        // Ensure they don't get overwritten by ui updated.
        activeForeground = new Color(activeForeground.getRGB());
        inactiveForeground = new Color(inactiveForeground.getRGB());
    }

    private void installDefaults() {
        setFont(UIManager.getFont("InternalFrame.titleFont", getLocale()));
    }

    protected JButton createWindowIcon() {
        JButton button = new JButton();
        button.putClientProperty("JButton.noShadowOverwrite", true);
        button.setComponentPopupMenu(createMenu());
        button.addActionListener(e -> button.getComponentPopupMenu().show(button,
                button.getWidth() / 2, button.getHeight() / 2));
        button.setFocusable(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        return button;
    }

    private JPopupMenu createMenu() {
        JPopupMenu menu = new JPopupMenu();
        if (getDecorationStyle() == JRootPane.FRAME) {
            addMenuItems(menu);
        }
        return menu;
    }

    private JMenuItem itemWithDisabledIcon(final JMenuItem item, final Icon disabledIcon) {
        item.setDisabledIcon(disabledIcon);
        return item;
    }

    private void addMenuItems(final JPopupMenu menu) {
        menu.add(itemWithDisabledIcon(new JMenuItem(restoreAction), restoreIcon));
        menu.add(itemWithDisabledIcon(new JMenuItem(minimizeAction), minimizeIcon));
        if (Toolkit.getDefaultToolkit().isFrameStateSupported(Frame.MAXIMIZED_BOTH)) {
            menu.add(itemWithDisabledIcon(new JMenuItem(maximizeAction), maximizeIcon));
        }
        menu.addSeparator();
        menu.add(itemWithDisabledIcon(new JMenuItem(closeAction), closeIcon));
    }

    private void close() {
        Window window = getWindow();
        if (window != null) {
            window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
        }
    }

    private Window getWindow() {
        return window;
    }

    private void minimize() {
        JNIDecorationsWindows.minimize(windowHandle);
    }

    private void maximize() {
        JNIDecorationsWindows.maximize(windowHandle);
    }

    private void restore() {
        JNIDecorationsWindows.restore(windowHandle);
    }

    private void createActions() {
        closeAction = new CloseAction();
        minimizeAction = new MinimizeAction();
        restoreAction = new RestoreAction();
        maximizeAction = new MaximizeAction();
    }

    private void createIcons() {
        minimizeIcon = new ToggleIcon(UIManager.getIcon("Windows.TitlePane.minimize.icon"),
                UIManager.getIcon("Windows.TitlePane.minimizeInactive.icon"));
        maximizeIcon = new ToggleIcon(UIManager.getIcon("Windows.TitlePane.maximize.icon"),
                UIManager.getIcon("Windows.TitlePane.maximizeInactive.icon"));
        restoreIcon = new ToggleIcon(UIManager.getIcon("Windows.TitlePane.restore.icon"),
                UIManager.getIcon("Windows.TitlePane.restoreInactive.icon"));
        closeIcon = new ToggleIcon(UIManager.getIcon("Windows.TitlePane.close.icon"),
                UIManager.getIcon("Windows.TitlePane.closeInactive.icon"));
    }

    private void createButtons() {
        closeButton = createButton(closeIcon, closeAction, true);
        Icon closePressed = UIManager.getIcon("Windows.TitlePane.closeHover.icon");
        closeButton.setRolloverIcon(closePressed);
        closeButton.setPressedIcon(closePressed);

        minimizeButton = createButton(minimizeIcon, minimizeAction);
        maximizeToggleButton = createButton(maximizeIcon, restoreAction);
        windowIconButton = createWindowIcon();
    }

    private LayoutManager createLayout() {
        return new TitlePaneLayout();
    }

    private void setActive(final boolean active) {
        closeIcon.setActive(active);
        titleLabel.setForeground(active ? activeForeground : inactiveForeground);
        minimizeIcon.setActive(active);
        maximizeIcon.setActive(isResizable() && active);
        closeIcon.setActive(active);
        restoreIcon.setActive(isResizable());
        setButtonActive(minimizeButton, active);
        setButtonActive(maximizeToggleButton, isResizable());
        getRootPane().repaint();
    }

    protected void setButtonActive(final JButton button, final boolean active) {
        if (active) {
            button.putClientProperty("JButton.borderless.hover", null);
            button.putClientProperty("JButton.borderless.click", null);
        } else {
            button.putClientProperty("JButton.borderless.hover", inactiveHover);
            button.putClientProperty("JButton.borderless.click", inactiveClick);
        }
    }

    @Override
    public void paintComponent(final Graphics g) {
        if (getFrame() != null) {
            setState(getFrame().getExtendedState());
        }
        updateResizeBehaviour();
        Window window = getWindow();
        boolean active = window == null || window.isActive();
        int width = getWidth();
        int height = getHeight();

        Color background = active ? activeBackground : inactiveBackground;

        g.setColor(background);
        g.fillRect(0, 0, width, height);

        if (isDrawBorder()) {
            g.setColor(border);
            g.fillRect(0, height - 1, width, 1);
        }

        GraphicsConfiguration currentGC = getGraphicsConfiguration();
        if (currentGC != null && currentGC != gc) {
            gc = currentGC;
            updateDragArea(gc);
            updateSystemIcon(gc);
        }
    }

    protected boolean isDrawBorder() {
        return getDecorationStyle() != JRootPane.NONE && menuBarStealer.hasMenuBar();
    }

    @Override
    public JRootPane getRootPane() {
        return rootPane;
    }

    private Frame getFrame() {
        if (window instanceof Frame) {
            return (Frame) window;
        }
        return null;
    }

    private void setState(final int state) {
        setState(state, false);
    }

    protected void updateResizeBehaviour() {
        boolean res = isResizable();
        if (oldResizable != res) {
            oldResizable = res;
            JNIDecorationsWindows.setResizable(windowHandle, res);
        }
    }

    private void setState(final int state, final boolean updateRegardless) {
        Window wnd = getWindow();
        if (wnd != null && getDecorationStyle() == JRootPane.FRAME) {
            if (this.state == state && !updateRegardless) {
                return;
            }
            Frame frame = getFrame();

            if (frame != null) {
                JRootPane rootPane = getRootPane();

                if (((state & Frame.MAXIMIZED_BOTH) != 0)) {
                    oldBorder = rootPane.getBorder();
                    LookAndFeel.uninstallBorder(rootPane);
                } else {
                    Border border = rootPane.getBorder();
                    if (oldBorder != null && (border == null || border instanceof UIResource)) {
                        rootPane.setBorder(oldBorder);
                    }
                }

                if (frame.isResizable()) {
                    maximizeIcon.setActive(true);
                    restoreIcon.setActive(true);
                    if ((state & Frame.MAXIMIZED_BOTH) != 0) {
                        updateToggleButton(restoreAction, restoreIcon);
                        maximizeAction.setEnabled(false);
                        restoreAction.setEnabled(true);
                    } else {
                        updateToggleButton(maximizeAction, maximizeIcon);
                        maximizeAction.setEnabled(true);
                        restoreAction.setEnabled(false);
                    }
                    maximizeToggleButton.setText(null);
                } else {
                    maximizeIcon.setActive(false);
                    restoreIcon.setActive(false);
                    maximizeAction.setEnabled(false);
                    restoreAction.setEnabled(false);
                }
            } else {
                // Not contained in a Frame
                maximizeAction.setEnabled(false);
                restoreAction.setEnabled(false);
                minimizeAction.setEnabled(false);
            }
            closeAction.setEnabled(true);
            this.state = state;
            repaint();
        }
    }

    protected boolean isResizable() {
        if (JRootPane.NONE == getDecorationStyle()) {
            return false;
        } else {
            if (window instanceof Dialog) {
                return ((Dialog) window).isResizable();
            } else if (window instanceof Frame) {
                return ((Frame) window).isResizable();
            }
        }
        return false;
    }

    private void updateToggleButton(final Action action, final Icon icon) {
        maximizeToggleButton.setAction(action);
        maximizeToggleButton.setIcon(icon);
        maximizeToggleButton.setToolTipText(maximizeToggleButton.getText());
        maximizeToggleButton.putClientProperty(AccessibleContext.ACCESSIBLE_NAME_PROPERTY,
                maximizeToggleButton.getText());
        maximizeToggleButton.setText(null);
    }

    protected boolean isLeftToRight(final Window window) {
        return (window == null) ? getRootPane().getComponentOrientation().isLeftToRight()
                : window.getComponentOrientation().isLeftToRight();
    }

    private void updateSystemIcon(final GraphicsConfiguration gc) {
        boolean frame = getDecorationStyle() == JRootPane.FRAME;
        Window window = getWindow();
        if (window == null) {
            windowIconButton.setIcon(null);
            return;
        }

        List<Image> icons = window.getIconImages();
        Icon systemIcon = null;
        if (icons == null || icons.size() == 0) {
            if (frame) {
                systemIcon = UIManager.getIcon("Windows.TitlePane.icon");
            }
        } else if (icons.size() == 1) {
            systemIcon = new ScaledIcon(icons.get(0).getScaledInstance((int) Scale.scaleWidth(ICON_SIZE, gc),
                    (int) Scale.scaleHeight(ICON_SIZE, gc), Image.SCALE_AREA_AVERAGING), this);
        } else {
            systemIcon = new ScaledIcon(getScaledIconImage(this, icons,
                    (int) Scale.scaleWidth(ICON_SIZE, gc),
                    (int) Scale.scaleHeight(ICON_SIZE, gc)), this);
        }
        if (windowIconButton != null) {
            windowIconButton.setIcon(systemIcon);
            SwingUtilities.invokeLater(this::repaint);
        }
    }

    private Image getScaledIconImage(final Component c, final List<Image> images,
            final int width, final int height) {
        Image bestImage = null;
        int dw = 0;
        int dh = 0;
        for (Image image : images) {
            ensureImageLoaded(c, image);
            int dwi = Math.abs(dw - image.getWidth(c));
            int dhi = Math.abs(dh - image.getHeight(c));
            if (bestImage == null || (dwi + dhi < dw + dh)) {
                bestImage = image;
                dw = dwi;
                dh = dhi;
            }
        }
        if (bestImage == null) return null;
        int iw = bestImage.getWidth(null);
        int ih = bestImage.getHeight(null);
        double scaleFactor = Math.min((double) width / (double) iw, (double) height / (double) ih);
        int bestWidth = (int) (scaleFactor * iw);
        int bestHeight = (int) (scaleFactor * ih);
        BufferedImage bimage =
                new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bimage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        try {
            int x = (width - bestWidth) / 2;
            int y = (height - bestHeight) / 2;
            g.drawImage(bestImage, x, y, bestWidth, bestHeight, c);
        } finally {
            g.dispose();
        }
        return bimage;
    }

    @SuppressWarnings("EmptyCatch")
    private void ensureImageLoaded(final Component c, final Image img) {
        MediaTracker tracker = new MediaTracker(c);
        tracker.addImage(img, 0);
        try {
            tracker.waitForAll();
        } catch (final InterruptedException ignored) {
        }
    }

    private class CloseAction extends AbstractAction {
        public CloseAction() {
            super(UIManager.getString("Actions.close", getLocale()), closeIcon);
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            close();
        }
    }

    private class MinimizeAction extends AbstractAction {
        public MinimizeAction() {
            super(UIManager.getString("Actions.minimize", getLocale()), minimizeIcon);
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            minimize();
        }
    }

    private class MaximizeAction extends AbstractAction {
        public MaximizeAction() {
            super(UIManager.getString("Actions.maximize", getLocale()), maximizeIcon);
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            maximize();
        }
    }

    private class RestoreAction extends AbstractAction {
        public RestoreAction() {
            super(UIManager.getString("Actions.restore", getLocale()), restoreIcon);
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            restore();
        }
    }

    private boolean hideTitleBar() {
        if (titleBarHidden) return true;
        String title = titleLabel.getText();
        if (title == null) title = "";
        // e.g. VCLJ achieves fullscreen by hiding the titlebar through jni and setting visibility
        // of the menubar.
        boolean emptyMenuBar = menuBarStealer.isMenuBarEmpty();
        boolean emptyContent = getDecorationStyle() == JRootPane.NONE && emptyMenuBar && title.length() == 0;
        return windowHandle == 0
                || emptyContent
                || (menuBarStealer.hasMenuBar() && !menuBarStealer.getMenuBar().isVisible());
    }

    @Override
    public Dimension getPreferredSize() {
        if (hideTitleBar()) return new Dimension(0, 0);
        int height = computeHeight();
        int width = computeWidth();
        if (isDrawBorder()) height++;
        return new Dimension(width, height);
    }

    private int computeHeight() {
        if (hideTitleBar()) return 0;
        FontMetrics fm = rootPane.getFontMetrics(getFont());
        int height = fm.getHeight() + 7;
        if (menuBarStealer.hasMenuBar()) {
            height = Math.max(height, menuBarStealer.getMenuBar().getMinimumSize().height);
        }
        return Math.max(BAR_HEIGHT, height);
    }

    private int computeWidth() {
        int width = 0;
        Icon windowIcon = windowIconButton.getIcon();
        if (windowIcon != null) {
            width += Math.min(ICON_WIDTH, Math.max(windowIcon.getIconHeight(), windowIcon.getIconWidth()));
            width += 2 * PAD;
        }
        if (menuBarStealer.hasMenuBar()) {
            width += getPreferredMenuSize().width;
            width += PAD;
        }
        boolean frame = getDecorationStyle() == JRootPane.FRAME;
        boolean undecorated = getDecorationStyle() == JRootPane.NONE;
        if (!undecorated) {
            width += BUTTON_WIDTH;
        }
        if (frame) {
            width += BUTTON_WIDTH;
            if (Toolkit.getDefaultToolkit().isFrameStateSupported(Frame.MAXIMIZED_BOTH)) {
                width += BUTTON_WIDTH;
            }
        }
        if (titleLabel != null && !titleLabel.getText().isEmpty()) {
            width += titleLabel.getPreferredSize().width;
            width += PAD;
        }
        return width;
    }

    protected void updateDragArea(final GraphicsConfiguration gc) {
        JNIDecorationsWindows.updateValues(windowHandle,
                (int) Scale.scaleWidth(left, gc), (int) Scale.scaleWidth(right, gc),
                (int) Scale.scaleHeight(height, gc), (int) Scale.scaleHeight(BUTTON_WIDTH, gc));
    }

    private Dimension getPreferredMenuSize() {
        return menuBarStealer.getMenuBar().getPreferredSize();
    }

    private class TitlePaneLayout implements LayoutManager {
        @Override
        public void addLayoutComponent(final String name, final Component c) {}

        @Override
        public void removeLayoutComponent(final Component c) {}

        @Override
        public Dimension preferredLayoutSize(final Container parent) {
            return getPreferredSize();
        }

        @Override
        public Dimension minimumLayoutSize(final Container c) {
            return preferredLayoutSize(c);
        }

        @Override
        public void layoutContainer(final Container c) {
            if (hideTitleBar()) return;
            boolean leftToRight = isLeftToRight(window);
            boolean frame = getDecorationStyle() == JRootPane.FRAME;
            boolean undecorated = getDecorationStyle() == JRootPane.NONE;

            windowIconButton.setBounds(Integer.MIN_VALUE, Integer.MIN_VALUE, 0, 0);
            closeButton.setBounds(Integer.MIN_VALUE, Integer.MIN_VALUE, 0, 0);
            minimizeButton.setBounds(Integer.MIN_VALUE, Integer.MIN_VALUE, 0, 0);
            maximizeToggleButton.setBounds(Integer.MIN_VALUE, Integer.MIN_VALUE, 0, 0);

            int w = getWidth();
            int x;
            int start = 0;
            int y = 0;
            height = computeHeight();
            left = 0;
            right = 0;

            Icon windowIcon = windowIconButton.getIcon();
            if (windowIcon != null) {
                int windowButtonWidth = Math.max(windowIcon.getIconHeight(), windowIcon.getIconWidth());
                windowButtonWidth = Math.min(ICON_WIDTH, windowButtonWidth);
                windowIconButton.setBounds(start + PAD, y, windowButtonWidth, height);
                start += windowButtonWidth + 2 * PAD;
                left = start;
            }

            if (menuBarStealer.hasMenuBar()) {
                int menuWidth = getPreferredMenuSize().width;
                Insets menuInsets = menuBarStealer.getMenuBar().getInsets();
                menuBarStealer.getMenuBar().setBounds(start, y, menuWidth, height + menuInsets.bottom);
                start += menuWidth + PAD;
                left += menuWidth;
            }
            x = w;
            if (!undecorated) {
                x -= BUTTON_WIDTH;
                right += BUTTON_WIDTH;
                closeButton.setBounds(x, y, BUTTON_WIDTH, height);
            }
            if (frame) {
                if (Toolkit.getDefaultToolkit().isFrameStateSupported(Frame.MAXIMIZED_BOTH)) {
                    x -= BUTTON_WIDTH;
                    right += BUTTON_WIDTH;
                    maximizeToggleButton.setBounds(x, y, BUTTON_WIDTH, height);
                }
                x -= BUTTON_WIDTH;
                right += BUTTON_WIDTH;
                minimizeButton.setBounds(x, y, BUTTON_WIDTH, height);
                x -= PAD;
            }
            start = Math.max(start, PAD);
            x = Math.min(getWidth() - PAD, x);
            int labelWidth = x - start;
            int prefLabelWidth = titleLabel.getPreferredSize().width;
            if (prefLabelWidth > labelWidth) {
                int extra = Math.min(prefLabelWidth - labelWidth, 2 * PAD);
                start -= extra / 2;
                labelWidth += extra;
            }
            titleLabel.setBounds(start, 0, labelWidth, height);

            if (!leftToRight) {
                mirror(windowIconButton, w);
                mirror(menuBarStealer.getMenuBar(), w);
                mirror(closeButton, w);
                mirror(minimizeButton, w);
                mirror(maximizeToggleButton, w);
                mirror(titleLabel, w);
                int tmp = left;
                left = right;
                right = tmp;
            }
            updateDragArea(getGraphicsConfiguration());
        }

        private void mirror(final JComponent component, final int w) {
            if (component != null) {
                component.setLocation(w - component.getX() - component.getWidth(), component.getY());
            }
        }
    }

    protected class WindowPropertyChangeListener implements PropertyChangeListener {

        @Override
        public void propertyChange(final PropertyChangeEvent pce) {
            String name = pce.getPropertyName();
            if (KEY_RESIZABLE.equals(name) || KEY_STATE.equals(name)) {
                Frame frame = getFrame();
                if (frame != null) {
                    setState(frame.getExtendedState(), true);
                }
                if (KEY_RESIZABLE.equals(name)) {
                    JNIDecorationsWindows.setResizable(windowHandle, Boolean.TRUE.equals(pce.getNewValue()));
                    getRootPane().repaint();
                }
            } else if (PropertyKey.TITLE.equals(name)) {
                titleLabel.setText(StringUtil.orEmpty(pce.getNewValue()));
                doLayout();
                repaint();
            } else if (PropertyKey.COMPONENT_ORIENTATION.equals(name)) {
                revalidate();
                repaint();
            } else if (KEY_ICON_IMAGE.equals(name)) {
                updateSystemIcon(getGraphicsConfiguration());
                revalidate();
                repaint();
            } else if (PropertyKey.BACKGROUND.equals(name) && pce.getNewValue() instanceof Color) {
                Color color = (Color) pce.getNewValue();
                if (color == null) return;
                JNIDecorationsWindows.setBackground(windowHandle, color.getRed(), color.getGreen(), color.getBlue());
            }
        }
    }

    protected class RootPanePropertyChangeListener implements PropertyChangeListener {

        @Override
        public void propertyChange(final PropertyChangeEvent evt) {
            if ("JRootPane.unifiedMenuBar".equals(evt.getPropertyName())) {
                menuBarStealer.updateMenuBar(true);
            } else if ("JRootPane.hideTitleBar".equals(evt.getPropertyName())) {
                updateTitleBarVisibility();
            }
        }
    }

    protected class WindowHandler extends WindowAdapter {

        @Override
        public void windowActivated(final WindowEvent ev) {
            setActive(true);
        }

        @Override
        public void windowDeactivated(final WindowEvent ev) {
            setActive(false);
        }
    }
}
