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
package com.github.weisj.darklaf.components;

import java.awt.*;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;

import javax.swing.*;

import com.github.weisj.darklaf.ui.util.DarkUIUtil;

/** @author Jannis Weis */
public class ClosableTabbedPane extends JTabbedPane {

    @Override
    public void insertTab(final String title, final Icon icon, final Component component, final String tip,
            final int index) {
        if (notifyVetoableChangeListeners(
                new TabPropertyChangeEvent(this, TabEvent.Type.TAB_OPENED.getCommand(), null, component, index))) {
            return;
        }
        super.insertTab(title, icon, component, tip, index);
        setTabComponentAt(indexOfComponent(component), new ClosableTabComponent(this));
        notifyTabListeners(new TabEvent(this, TabEvent.Type.TAB_OPENED, index, component));
    }

    @Override
    public void removeTabAt(final int index) {
        checkIndex(index);
        Component c = getComponentAt(index);
        if (notifyVetoableChangeListeners(new TabPropertyChangeEvent(this, TabEvent.Type.TAB_CLOSING.getCommand(),
                getComponentAt(index), null, index))) {
            return;
        }
        notifyTabListeners(new TabEvent(this, TabEvent.Type.TAB_CLOSING, index, c));
        super.removeTabAt(index);
        notifyTabListeners(new TabEvent(this, TabEvent.Type.TAB_CLOSED, index, c));
    }

    @Override
    public void setTabComponentAt(final int index, final Component component) {
        if (component instanceof ClosableTabComponent) {
            ((ClosableTabComponent) component).setTabbedPane(this);
            super.setTabComponentAt(index, component);
        } else {
            super.setTabComponentAt(index, new ClosableTabComponent(this, component));
        }
    }

    /**
     * Returns the {@link ClosableTabComponent} at the given index or null if no tab component is set.
     *
     * @param index the index.
     * @return the {@link ClosableTabComponent} at the index.
     */
    public ClosableTabComponent getClosableTabComponent(final int index) {
        return DarkUIUtil.nullableCast(ClosableTabComponent.class, super.getTabComponentAt(index));
    }

    /**
     * Sets whether a given tab is closable.
     *
     * @param index the index of the tab.
     * @param closable true if closable.
     */
    public void setTabClosable(final int index, final boolean closable) {
        ClosableTabComponent tab = getClosableTabComponent(index);
        if (tab != null) tab.setClosable(closable);
    }

    /**
     * Sets whether a given tab is closable.
     *
     * @param index the tab index.
     * @return true if closable.
     */
    public boolean isTabClosable(final int index) {
        ClosableTabComponent tab = getClosableTabComponent(index);
        return tab != null && tab.isClosable();
    }

    private void checkIndex(final int index) {
        int tabCount = getTabCount();
        if (index < 0 || index >= tabCount) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Tab count: " + tabCount);
        }
    }

    private boolean notifyVetoableChangeListeners(final TabPropertyChangeEvent e) {
        try {
            VetoableChangeListener[] listeners = getVetoableChangeListeners();
            for (VetoableChangeListener l : listeners) {
                l.vetoableChange(e);
            }
        } catch (final PropertyVetoException ex) {
            return true;
        }
        return false;
    }

    @Override
    public void setEnabledAt(final int index, final boolean enabled) {
        super.setEnabledAt(index, enabled);
    }

    private void notifyTabListeners(final TabEvent event) {
        TabListener[] listeners = listenerList.getListeners(TabListener.class);
        switch (event.getType()) {
            case TAB_CLOSED:
                for (TabListener l : listeners) {
                    l.tabClosed(event);
                }
                break;
            case TAB_OPENED:
                for (TabListener l : listeners) {
                    l.tabOpened(event);
                }
                break;
            case TAB_CLOSING:
                for (TabListener l : listeners) {
                    l.tabClosing(event);
                }
                break;
        }
    }

    public void addTabListener(final TabListener listener) {
        listenerList.add(TabListener.class, listener);
    }

    public void removeTabListener(final TabListener listener) {
        listenerList.remove(TabListener.class, listener);
    }
}
