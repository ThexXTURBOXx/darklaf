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
package com.github.weisj.darklaf.delegate;

import java.awt.*;

import javax.swing.*;

/** @author Jannis Weis */
public class ScrollLayoutManagerDelegate extends ScrollPaneLayout {
    private final ScrollPaneLayout delegate;

    public ScrollLayoutManagerDelegate(final ScrollPaneLayout delegate) {
        if (delegate == null) {
            throw new IllegalArgumentException("Delegate is null");
        }
        this.delegate = delegate;
    }

    public ScrollPaneLayout getDelegate() {
        return delegate;
    }

    @Override
    public void addLayoutComponent(final String name, final Component comp) {
        getDelegate().addLayoutComponent(name, comp);
    }

    @Override
    public void syncWithScrollPane(final JScrollPane sp) {
        getDelegate().syncWithScrollPane(sp);
    }

    @Override
    public void removeLayoutComponent(final Component comp) {
        getDelegate().removeLayoutComponent(comp);
    }

    @Override
    public int getVerticalScrollBarPolicy() {
        return getDelegate().getVerticalScrollBarPolicy();
    }

    @Override
    public void setVerticalScrollBarPolicy(final int x) {
        getDelegate().setVerticalScrollBarPolicy(x);
    }

    @Override
    public int getHorizontalScrollBarPolicy() {
        return getDelegate().getHorizontalScrollBarPolicy();
    }

    @Override
    public void setHorizontalScrollBarPolicy(final int x) {
        getDelegate().setHorizontalScrollBarPolicy(x);
    }

    @Override
    public JViewport getViewport() {
        return getDelegate().getViewport();
    }

    @Override
    public JScrollBar getHorizontalScrollBar() {
        return getDelegate().getHorizontalScrollBar();
    }

    @Override
    public JScrollBar getVerticalScrollBar() {
        return getDelegate().getVerticalScrollBar();
    }

    @Override
    public JViewport getRowHeader() {
        return getDelegate().getRowHeader();
    }

    @Override
    public JViewport getColumnHeader() {
        return getDelegate().getColumnHeader();
    }

    @Override
    public Component getCorner(final String key) {
        return getDelegate().getCorner(key);
    }

    @Override
    public Dimension preferredLayoutSize(final Container parent) {
        return getDelegate().preferredLayoutSize(parent);
    }

    @Override
    public Dimension minimumLayoutSize(final Container parent) {
        return getDelegate().minimumLayoutSize(parent);
    }

    @Override
    public void layoutContainer(final Container parent) {
        getDelegate().layoutContainer(parent);
    }

    @SuppressWarnings("deprecation")
    @Override
    public Rectangle getViewportBorderBounds(final JScrollPane scrollpane) {
        return getDelegate().getViewportBorderBounds(scrollpane);
    }
}
