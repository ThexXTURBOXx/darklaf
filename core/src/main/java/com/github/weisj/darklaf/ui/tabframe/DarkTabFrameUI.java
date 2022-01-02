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
package com.github.weisj.darklaf.ui.tabframe;

import java.awt.*;
import java.awt.dnd.DropTarget;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.TooManyListenersException;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;

import org.pbjar.jxlayer.plaf.ext.transform.DefaultTransformModel;
import org.pbjar.jxlayer.plaf.ext.transform.TransformUtils;

import com.github.weisj.darklaf.components.border.MutableLineBorder;
import com.github.weisj.darklaf.components.tabframe.*;
import com.github.weisj.darklaf.components.uiresource.JPanelUIResource;
import com.github.weisj.darklaf.util.Alignment;
import com.github.weisj.darklaf.util.Pair;

/**
 * UI class for {@link JTabFrame}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class DarkTabFrameUI extends TabFrameUI implements AWTEventListener {

    protected static final TabFrameTransferHandler TRANSFER_HANDLER = new TabFrameTransferHandler.UIResource();
    private final Rectangle calcRect = new Rectangle();

    private JLayer<JComponent> rotatePaneLeft;
    private JLayer<JComponent> rotatePaneRight;
    private JTabFrame tabFrame;
    private JComponent dropComponentTop;
    private JComponent dropComponentBottom;
    private JComponent dropComponentRight;
    private JComponent dropComponentLeft;
    private MutableLineBorder topBorder;
    private MutableLineBorder bottomBorder;
    private MutableLineBorder leftBorder;
    private MutableLineBorder rightBorder;
    private Color lineColor;
    private TabFrameLayout layout;
    private int tabHeight;
    private Color dragBorderColor;
    private final Dimension dropSize = new Dimension();
    private Alignment sourceAlign;
    private int sourceIndex;
    private Alignment destAlign;
    private int destIndex;

    public static ComponentUI createUI(final JComponent c) {
        return new DarkTabFrameUI();
    }

    @Override
    public void installUI(final JComponent c) {
        tabFrame = (JTabFrame) c;
        installDefaults();
        installComponents();
        installListeners();
        installDnD();
    }

    protected void installDefaults() {
        layout = createLayout();
        tabFrame.setLayout(layout);
        lineColor = UIManager.getColor("TabFrame.line");
        tabHeight = UIManager.getInt("TabFrame.tabHeight");
        topBorder = new MutableLineBorder(0, 0, 1, 0, lineColor);
        bottomBorder = new MutableLineBorder(1, 0, 0, 0, lineColor);
        rightBorder = new MutableLineBorder(0, 0, 1, 0, lineColor);
        leftBorder = new MutableLineBorder(0, 0, 1, 0, lineColor);

        dropComponentTop = new JPanelUIResource();
        dropComponentBottom = new JPanelUIResource();
        dropComponentLeft = new JPanelUIResource();
        dropComponentRight = new JPanelUIResource();

        dragBorderColor = UIManager.getColor("TabFrame.dragBorderColor");
        Color dropColor = UIManager.getColor("TabFrame.dropBackground");

        dropComponentTop.setBackground(dropColor);
        dropComponentBottom.setBackground(dropColor);
        dropComponentLeft.setBackground(dropColor);
        dropComponentRight.setBackground(dropColor);

        tabFrame.getTopTabContainer().setBorder(topBorder);
        tabFrame.getBottomTabContainer().setBorder(bottomBorder);
        tabFrame.getRightTabContainer().setBorder(rightBorder);
        tabFrame.getLeftTabContainer().setBorder(leftBorder);
    }

    private void installComponents() {
        DefaultTransformModel rightTransformModel = new DefaultTransformModel();
        rightTransformModel.setQuadrantRotation(1);
        rightTransformModel.setScaleToPreferredSize(true);
        rotatePaneRight = TransformUtils.createTransformJLayer(tabFrame.getRightTabContainer(), rightTransformModel);

        DefaultTransformModel leftTransformModel = new DefaultTransformModel();
        leftTransformModel.setQuadrantRotation(3);
        leftTransformModel.setScaleToPreferredSize(true);
        rotatePaneLeft = TransformUtils.createTransformJLayer(tabFrame.getLeftTabContainer(), leftTransformModel);

        tabFrame.add(tabFrame.getTopTabContainer());
        tabFrame.add(tabFrame.getBottomTabContainer());
        tabFrame.add(rotatePaneLeft);
        tabFrame.add(rotatePaneRight);

        tabFrame.getTopTabContainer().add(dropComponentTop);
        tabFrame.getBottomTabContainer().add(dropComponentBottom);
        tabFrame.getLeftTabContainer().add(dropComponentLeft);
        tabFrame.getRightTabContainer().add(dropComponentRight);
    }

    protected void installListeners() {
        Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.MOUSE_EVENT_MASK);
    }

    protected void installDnD() {
        tabFrame.setTransferHandler(TRANSFER_HANDLER);
        try {
            DropTarget dropTarget = tabFrame.getDropTarget();
            if (dropTarget != null) {
                dropTarget.addDropTargetListener(TRANSFER_HANDLER);
                dropTarget.setActive(tabFrame.isDndEnabled());
            }
        } catch (final TooManyListenersException e) {
            throw new IllegalStateException("Can't install DnD support. A valid drop target is already registered", e);
        }
    }

    protected TabFrameLayout createLayout() {
        return new TabFrameLayout(tabFrame, this);
    }

    @Override
    public void uninstallUI(final JComponent c) {
        tabFrame.remove(tabFrame.getTopTabContainer());
        tabFrame.remove(tabFrame.getBottomTabContainer());
        tabFrame.remove(rotatePaneLeft);
        tabFrame.remove(rotatePaneRight);
        layout = null;
        lineColor = null;
        topBorder = null;
        bottomBorder = null;
        leftBorder = null;
        rightBorder = null;
        rotatePaneLeft = null;
        rotatePaneRight = null;
        uninstallListeners();
        if (tabFrame.getTransferHandler() instanceof TabFrameTransferHandler.UIResource) {
            tabFrame.setTransferHandler(null);
            if (tabFrame.getDropTarget() != null) {
                tabFrame.getDropTarget().removeDropTargetListener(TRANSFER_HANDLER);
                tabFrame.getDropTarget().setActive(false);
            }
        }
    }

    protected void uninstallListeners() {
        Toolkit.getDefaultToolkit().removeAWTEventListener(this);
    }

    @Override
    public int getTabSize(final JTabFrame tabFrame) {
        return tabHeight;
    }

    @Override
    public void clearTargetIndicator() {
        destIndex = -10;
        destAlign = null;
        dropSize.setSize(0, 0);
        tabFrame.doLayout();
    }

    @Override
    public void clearSourceIndicator() {
        sourceIndex = -10;
        sourceAlign = null;
        tabFrame.doLayout();
    }

    @Override
    public Color getDragBorderColor() {
        return dragBorderColor;
    }

    @Override
    public void setSourceIndicator(final Alignment a, final int tabIndex) {
        sourceAlign = a;
        sourceIndex = tabIndex;
        destIndex = tabIndex;
        destAlign = a;
        tabFrame.doLayout();
    }

    @Override
    public void setTargetIndicator(final Alignment a, final int tabIndex) {
        destAlign = a;
        destIndex = tabIndex;
        tabFrame.doLayout();
    }

    @Override
    public JTabFrame.TabFramePosition getTabIndexAt(final JTabFrame tabFrame, final Point p) {
        return getTabIndexAtImpl(tabFrame, p).getFirst();
    }

    @Override
    public int getTabWidth(final JTabFrame tabFrame, final Alignment a, final int index) {
        return layout.getTabWidth(tabFrame.getTabComponentAt(a, index).getComponent());
    }

    public JComponent getTopContainer() {
        return tabFrame.getTopTabContainer();
    }

    public JComponent getBottomContainer() {
        return tabFrame.getBottomTabContainer();
    }

    public JComponent getLeftContainer() {
        return rotatePaneLeft;
    }

    public JComponent getRightContainer() {
        return rotatePaneRight;
    }

    @Override
    public int getTabHeight(final JTabFrame tabFrame, final Alignment a, final int index) {
        return tabFrame.getTabSize();
    }

    @Override
    public Rectangle getTabContainerBounds(final JTabFrame tabFrame, final Alignment a) {
        switch (a) {
            case NORTH:
            case NORTH_EAST:
                Rectangle rect = getTopContainer().getBounds();
                rect.x = 0;
                rect.width = tabFrame.getWidth();
                return rect;
            case EAST:
            case SOUTH_EAST:
                return getRightContainer().getBounds();
            case SOUTH:
            case SOUTH_WEST:
                Rectangle rect2 = getTopContainer().getBounds();
                rect2.x = 0;
                rect2.width = tabFrame.getWidth();
                return rect2;
            case WEST:
            case NORTH_WEST:
                return getLeftContainer().getBounds();
            default:
            case CENTER:
                return tabFrame.getContentPane().getComponent().getBounds();
        }
    }

    protected Pair<JTabFrame.TabFramePosition, Point> getTabIndexAtImpl(final JTabFrame tabFrame, final Point p) {
        Component tabComp = null;
        Alignment a = null;
        Point pos = null;
        if (!layout.isDraggedOver(Alignment.NORTH)) {
            getTopContainer().getBounds(calcRect);
            if (p.y >= calcRect.y && p.y <= calcRect.y + calcRect.height) {
                tabComp = getTopContainer();
                a = Alignment.NORTH;
                pos = SwingUtilities.convertPoint(tabFrame, p, tabComp);
                if (pos.x > tabComp.getWidth() / 2) {
                    a = Alignment.NORTH_EAST;
                }
            }
        }
        if (tabComp == null && !layout.isDraggedOver(Alignment.SOUTH)) {
            getBottomContainer().getBounds(calcRect);
            if (p.y >= calcRect.y && p.y <= calcRect.y + calcRect.height) {
                tabComp = getBottomContainer();
                a = Alignment.SOUTH;
                pos = SwingUtilities.convertPoint(tabFrame, p, tabComp);
                if (pos.x <= tabComp.getWidth() / 2) {
                    a = Alignment.SOUTH_WEST;
                }
            }
        }
        if (tabComp == null && !layout.isDraggedOver(Alignment.WEST)) {
            getLeftContainer().getBounds(calcRect);
            if (p.x >= calcRect.x && p.x <= calcRect.x + calcRect.width) {
                tabComp = getLeftContainer();
                a = Alignment.WEST;
                pos = SwingUtilities.convertPoint(tabFrame, p, tabComp);
                if (pos.y < tabComp.getHeight() / 2) {
                    a = Alignment.NORTH_WEST;
                }
                int tmp = pos.x;
                pos.x = tabComp.getHeight() - pos.y;
                pos.y = tmp;
            }
        }
        if (tabComp == null && !layout.isDraggedOver(Alignment.EAST)) {
            getRightContainer().getBounds(calcRect);
            if (p.x >= calcRect.x && p.x <= calcRect.x + calcRect.width) {
                tabComp = getRightContainer();
                a = Alignment.EAST;
                pos = SwingUtilities.convertPoint(tabFrame, p, tabComp);
                if (pos.y > tabComp.getHeight() / 2) {
                    a = Alignment.SOUTH_EAST;
                }
                int tmp = pos.x;
                // noinspection SuspiciousNameCombination
                pos.x = pos.y;
                pos.y = tmp;
            }
        }
        if (tabComp == null) {
            JTabFrame.TabFramePosition tab = maybeRestoreTabContainer(tabFrame, p);
            if (tab.getAlignment() != null) {
                return new Pair<>(tab, pos);
            }
        } else {
            layout.setDraggedOver(false);
        }
        if (tabComp == null) {
            return new Pair<>(new JTabFrame.TabFramePosition(null, -1), pos);
        }
        List<TabFrameTab> tabs = tabFrame.tabsForAlignment(a);
        for (TabFrameTab tab : tabs) {
            Rectangle rect = getTabRect(tab, a, tabComp, true);
            if (rect.contains(pos)) {
                return new Pair<>(new JTabFrame.TabFramePosition(a, tab.getIndex()), pos);
            }
        }
        return new Pair<>(new JTabFrame.TabFramePosition(a, -1), pos);
    }

    @Override
    public JTabFrame.TabFramePosition getNearestTabIndexAt(final JTabFrame tabFrame, final Point pos) {
        return getNearestTabIndexAtImpl(tabFrame, pos).getFirst();
    }

    @Override
    public JTabFrame.TabFramePosition getDropPosition(final JTabFrame tabFrame, final Point p) {
        Pair<JTabFrame.TabFramePosition, Point> res = getNearestTabIndexAtImpl(tabFrame, p);
        JTabFrame.TabFramePosition tab = res.getFirst();
        if (tab.getAlignment() != null) {
            Alignment a = tab.getAlignment();
            int index = tab.getIndex();
            if (index >= 0) {
                Rectangle rect =
                        getTabRect(tabFrame.getTabComponentAt(a, index), a, tabFrame.getTabContainer(a), false);
                Point pos = res.getSecond();
                if (isForward(a)) {
                    if (pos.x <= rect.x + rect.width / 2 && pos.x >= rect.x) {
                        tab.setIndex(tab.getIndex() - 1);
                    }
                } else {
                    if (pos.x >= rect.x + rect.width / 2) {
                        tab.setIndex(tab.getIndex() - 1);
                    }
                }
            }
        }
        return tab;
    }

    protected JTabFrame.TabFramePosition maybeRestoreTabContainer(final JTabFrame tabFrame, final Point p) {
        Alignment a = null;
        int size = tabFrame.getTabSize();
        int threshold = size;
        int index = -10;
        if (tabFrame.getTopTabCount() == 0) {
            if (layout.isDraggedOver(Alignment.NORTH)) threshold *= 2;
            if (p.y < threshold) {
                a = p.x >= tabFrame.getWidth() / 2 ? Alignment.NORTH_EAST : Alignment.NORTH;
            }
            if (p.y < size) index = -1;
        }
        if (a == null && tabFrame.getBottomTabCount() == 0) {
            if (layout.isDraggedOver(Alignment.SOUTH)) threshold *= 2;
            if (p.y > tabFrame.getHeight() - threshold) {
                a = p.x >= tabFrame.getWidth() / 2 ? Alignment.SOUTH : Alignment.SOUTH_WEST;
            }
            if (p.y > tabFrame.getHeight() - size) index = -1;
        }
        if (a == null && tabFrame.getLeftTabCount() == 0) {
            if (layout.isDraggedOver(Alignment.WEST)) threshold *= 2;
            if (p.x < threshold) {
                a = p.y >= tabFrame.getHeight() / 2 ? Alignment.WEST : Alignment.NORTH_WEST;
            }
            if (p.x < size) index = -1;
        }
        if (a == null && tabFrame.getRightTabCount() == 0) {
            if (layout.isDraggedOver(Alignment.EAST)) threshold *= 2;
            if (p.x > tabFrame.getWidth() - threshold) {
                a = p.y >= tabFrame.getHeight() / 2 ? Alignment.SOUTH_EAST : Alignment.EAST;
            }
            if (p.x > tabFrame.getWidth() - size) index = -1;
        }
        layout.setDraggedOver(false);
        if (a != null) {
            layout.setDraggedOver(a, true);
        }
        return new JTabFrame.TabFramePosition(a, index);
    }

    @Override
    public void setDropSize(final int width, final int height) {
        dropSize.setSize(width, height);
    }

    protected Rectangle getTabRect(final TabFrameTab tab, final Alignment a, final Component tabComp,
            final boolean includeDropRect) {
        tab.getComponent().getBounds(calcRect);
        SwingUtilities.convertRectangle(tab.getComponent(), calcRect, tabComp);
        if (includeDropRect && a == destAlign) {
            if (tab.getIndex() == destIndex && destIndex >= 0) {
                if (isForward(a)) {
                    calcRect.width += dropSize.width;
                } else {
                    calcRect.x -= dropSize.width;
                    calcRect.width += dropSize.width;
                }
            }
        }
        return calcRect;
    }

    protected boolean isForward(final Alignment a) {
        switch (a) {
            case NORTH:
            case EAST:
            case WEST:
            case SOUTH_WEST:
                return true;
            case NORTH_WEST:
            case SOUTH:
            case NORTH_EAST:
            case SOUTH_EAST:
            default:
                return false;
        }
    }

    protected Pair<JTabFrame.TabFramePosition, Point> getNearestTabIndexAtImpl(final JTabFrame tabFrame,
            final Point pos) {
        Pair<JTabFrame.TabFramePosition, Point> res = getTabIndexAtImpl(tabFrame, pos);
        JTabFrame.TabFramePosition tab = res.getFirst();
        if (tab.getAlignment() != null && tab.getIndex() == -1) {
            Point p = res.getSecond();
            Alignment a = tab.getAlignment();
            if (tabFrame.getTabCountAt(a) == 0) {
                tab.setIndex(-1);
                return res;
            }
            int w = a == destAlign && destIndex == -1 ? dropSize.width : 0;
            Component comp = getTabContainer(a);
            switch (a) {
                case NORTH:
                case SOUTH_WEST:
                    if (p.x > getLeftContainer().getWidth() + w) {
                        tab.setIndex(tabFrame.getTabCountAt(a) - 1);
                    }
                    break;
                case NORTH_EAST:
                case SOUTH:
                    if (p.x < comp.getWidth() - getRightContainer().getWidth() - w) {
                        tab.setIndex(tabFrame.getTabCountAt(a) - 1);
                    }
                    break;
                case EAST:
                case WEST:
                    if (p.x > w) {
                        tab.setIndex(tabFrame.getTabCountAt(a) - 1);
                    }
                    break;
                case SOUTH_EAST:
                case NORTH_WEST:
                    if (p.x < comp.getHeight() - w) {
                        tab.setIndex(tabFrame.getTabCountAt(a) - 1);
                    }
                    break;
                case CENTER:
                    throw new IllegalStateException();
            }
        }
        return res;
    }

    protected Component getTabContainer(final Alignment a) {
        switch (a) {
            case NORTH:
            case NORTH_EAST:
                return getTopContainer();
            case EAST:
            case SOUTH_EAST:
                return getRightContainer();
            case SOUTH:
            case SOUTH_WEST:
                return getBottomContainer();
            case WEST:
            case NORTH_WEST:
                return getLeftContainer();
            default:
                return null;
        }
    }

    public Alignment getSourceAlign() {
        return sourceAlign;
    }

    public int getSourceIndex() {
        return sourceIndex;
    }

    public Alignment getDestAlign() {
        return destAlign;
    }

    public int getDestIndex() {
        return destIndex;
    }

    @Override
    public void eventDispatched(final AWTEvent event) {
        if (event.getID() == MouseEvent.MOUSE_PRESSED) {
            MouseEvent e = (MouseEvent) event;
            Component comp = e.getComponent().getComponentAt(e.getPoint());
            if (comp instanceof TabFramePopup || comp instanceof PopupContainer || comp instanceof JTabFrame) {
                comp.requestFocusInWindow();
            }
        }
    }

    public Dimension getDropSize() {
        return dropSize;
    }

    public JComponent getDropComponent(final Alignment a) {
        switch (a) {
            default:
            case CENTER:
            case NORTH:
            case NORTH_EAST:
                return getDropComponentTop();
            case EAST:
            case SOUTH_EAST:
                return getDropComponentRight();
            case SOUTH:
            case SOUTH_WEST:
                return getDropComponentBottom();
            case WEST:
            case NORTH_WEST:
                return getDropComponentLeft();
        }
    }

    public JComponent getDropComponentTop() {
        return dropComponentTop;
    }

    public JComponent getDropComponentRight() {
        return dropComponentRight;
    }

    public JComponent getDropComponentBottom() {
        return dropComponentBottom;
    }

    public JComponent getDropComponentLeft() {
        return dropComponentLeft;
    }
}
