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
package com.github.weisj.darklaf.properties.icons;

import java.awt.*;
import java.io.Serializable;

import javax.swing.*;
import javax.swing.plaf.UIResource;

/**
 * Icon that is aware of the current ui theme and adjusts the icon accordingly. Icons are loaded
 * lazily at their point of usage.
 *
 * @author Jannis Weis
 * @since 2019
 */
public class DarkUIAwareIcon implements UIAwareIcon, UIResource, IconLoader.CacheableIcon, Serializable {

    private final DarkUIAwareIcon dual;
    protected final String darkKey;
    protected final String lightKey;
    protected final int w;
    protected final int h;
    protected final Class<?> parentClass;
    protected transient boolean loaded;
    protected transient Icon icon;
    private AwareIconStyle currentStyle;

    private IconLoader.IconKey cacheKey;

    /**
     * Create new ui aware icon.
     *
     * @param darkKey key to load icon for dark mode.
     * @param lightKey key to load icon for light mode.
     * @param w width of icon.
     * @param h height of icon.
     * @param parentClass the class to resolve the path while lazy loading.
     */
    public DarkUIAwareIcon(final String darkKey, final String lightKey, final int w, final int h,
            final Class<?> parentClass) {
        this.darkKey = darkKey;
        this.lightKey = lightKey;
        this.w = w;
        this.h = h;
        this.parentClass = parentClass;
        this.dual = new DarkUIAwareIcon(this);
    }

    private DarkUIAwareIcon(final DarkUIAwareIcon dual) {
        this.darkKey = dual.lightKey;
        this.lightKey = dual.darkKey;
        this.dual = dual;
        this.w = dual.w;
        this.h = dual.h;
        this.parentClass = dual.parentClass;
    }

    public void paintIcon(final Component c, final Graphics g2, final int x, final int y, final double scale) {
        ensureLoaded();
        Graphics2D g = (Graphics2D) g2.create();
        g.translate(x, y);
        g.scale(scale, scale);
        icon.paintIcon(c, g, 0, 0);
        g2.dispose();
    }

    protected void ensureLoaded() {
        if (!isLoaded()) {
            updateStyle();
            loadIcon();
        }
    }

    protected void updateStyle() {
        currentStyle = IconLoader.getAwareStyle();
    }

    protected boolean isLoaded() {
        return loaded && (currentStyle == IconLoader.getAwareStyle());
    }

    public boolean isDark() {
        return currentStyle == AwareIconStyle.DARK;
    }

    protected void loadIcon() {
        if (isDark()) {
            icon = IconLoader.get(parentClass).getIcon(darkKey, w, h);
        } else {
            icon = IconLoader.get(parentClass).getIcon(lightKey, w, h);
        }
        loaded = true;
    }

    @Override
    public void paintIcon(final Component c, final Graphics g, final int x, final int y) {
        ensureLoaded();
        icon.paintIcon(c, g, x, y);
    }

    @Override
    public int getIconWidth() {
        ensureLoaded();
        return icon.getIconWidth();
    }

    @Override
    public int getIconHeight() {
        ensureLoaded();
        return icon.getIconHeight();
    }

    @Override
    public DarkUIAwareIcon getDual() {
        return dual;
    }

    @Override
    public void setCacheKey(final IconLoader.IconKey key) {
        this.cacheKey = key;
    }

    IconLoader.IconKey getCacheKey() {
        return cacheKey;
    }
}
