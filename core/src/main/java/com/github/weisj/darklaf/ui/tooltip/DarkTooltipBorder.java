/*
 * MIT License
 *
 * Copyright (c) 2019-2022 Jannis Weis
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
package com.github.weisj.darklaf.ui.tooltip;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

import javax.swing.*;
import javax.swing.border.Border;

import com.github.weisj.darklaf.components.border.BubbleBorder;
import com.github.weisj.darklaf.components.border.DropShadowBorder;
import com.github.weisj.darklaf.components.tooltip.ToolTipStyle;
import com.github.weisj.darklaf.graphics.PaintUtil;
import com.github.weisj.darklaf.util.Alignment;
import com.github.weisj.darklaf.util.PropertyUtil;
import com.github.weisj.darklaf.util.graphics.GraphicsContext;

/** @author Jannis Weis */
public class DarkTooltipBorder implements Border, AlignableTooltipBorder {

    private final DropShadowBorder shadowBorder;
    private final BubbleBorder bubbleBorder;
    private final boolean paintShadow;
    private Insets margin;
    private Alignment alignment;
    private boolean showPointer;

    public DarkTooltipBorder() {
        margin = UIManager.getInsets("ToolTip.borderInsets");
        if (margin == null) margin = new Insets(0, 0, 0, 0);
        bubbleBorder = new BubbleBorder(UIManager.getColor("ToolTip.borderColor"));
        bubbleBorder.setThickness(1);
        bubbleBorder.setPointerSize(8);
        bubbleBorder.setPointerWidth(12);
        int borderRadius = UIManager.getInt("Tooltip.borderRadius");
        bubbleBorder.setRadius(borderRadius);
        bubbleBorder.setPointerSide(Alignment.CENTER);
        int shadowSize = UIManager.getInt("ToolTip.shadowSize");
        float opacity = UIManager.getInt("ToolTip.shadowOpacity") / 100.0f;
        Color shadowColor = UIManager.getColor("ToolTip.borderShadowColor");
        shadowBorder = new DropShadowBorder(shadowColor, shadowSize, opacity, borderRadius);
        paintShadow = UIManager.getBoolean("ToolTip.paintShadow");
    }

    public Shape[] getBackgroundShapes(final Component c, final int width, final int height) {
        if (isPlain(c)) {
            return new Area[] {new Area(new Rectangle(0, 0, width, height))};
        }
        Insets ins = shadowBorder.getBorderInsets(null);
        adjustInsets(ins);
        return bubbleBorder.getBubbleShapes(ins.left, ins.top, width - ins.left - ins.right,
                height - ins.top - ins.bottom, bubbleBorder.getThickness() / 3f);
    }

    private int getPointerOffset(final Component c, final Dimension dimension, final int thicknessFactor) {
        if (!showPointer || isPlain(c)) return 0;
        int offset = (int) bubbleBorder.getOffset(dimension.width - 2 * shadowBorder.getShadowSize(), dimension.height)
                + shadowBorder.getShadowSize();
        int thickness = bubbleBorder.getThickness();
        Alignment align = bubbleBorder.getPointerSide();
        if (align.isWest(false)) offset += thicknessFactor * thickness;
        return offset;
    }

    @Override
    public void adjustContentSize(final JToolTip toolTip, final Dimension dim, final Alignment align) {
        if (align == Alignment.EAST || align == Alignment.WEST) {
            dim.height -= getShadowSize(toolTip);
        }
    }

    private void adjustInsets(final Insets si) {
        Alignment align = bubbleBorder.getPointerSide();
        int pointerSize = bubbleBorder.getPointerSize();
        if (align.isSouth()) {
            si.bottom -= pointerSize;
        } else if (align == Alignment.EAST) {
            si.right -= pointerSize;
        } else if (align == Alignment.WEST) {
            si.left -= pointerSize;
        } else if (align.isNorth()) {
            si.top -= pointerSize;
        }
    }

    @Override
    public void paintBorder(final Component c, final Graphics g, final int x, final int y, final int width,
            final int height) {
        if (c instanceof JToolTip && ((JToolTip) c).getTipText() == null) return;
        GraphicsContext context = new GraphicsContext(g);
        if (isPlain(c)) {
            g.setColor(bubbleBorder.getColor());
            PaintUtil.drawRect(g, x, y, width, height, 1);
            return;
        }
        Insets ins = shadowBorder.getBorderInsets(c);
        adjustInsets(ins);
        Area innerArea = bubbleBorder.getBubbleArea(x + ins.left, y + ins.top, width - ins.left - ins.right,
                height - ins.top - ins.bottom, bubbleBorder.getThickness());
        if (paintShadow) {
            paintShadow(c, g, x, y, width, height, innerArea);
        }
        Area outerArea = bubbleBorder.getBubbleArea(x + ins.left, y + ins.top, width - ins.left - ins.right,
                height - ins.top - ins.bottom, 0);
        outerArea.subtract(innerArea);
        bubbleBorder.paintBorder(g, outerArea);
        context.restore();
    }

    public void paintShadow(final Component c, final Graphics g, final int x, final int y, final int width,
            final int height, final Area bubbleArea) {
        Shape oldClip = g.getClip();
        if (bubbleArea.contains(oldClip.getBounds())) return;
        Area clip = new Area(new Rectangle2D.Double(x, y, width, height));
        clip.subtract(bubbleArea);
        ((Graphics2D) g).clip(clip);
        int bw = 1 + bubbleBorder.getThickness();
        shadowBorder.paintBorder(c, g, x + bw, y + bw, width - 2 * bw, height - 2 * bw);
        g.setClip(oldClip);
    }

    @Override
    public Insets getBorderInsets(final Component c) {
        Insets uIns = getUserInsets(c);
        if (isPlain(c)) {
            return new Insets(1 + uIns.top, 1 + uIns.left, 1 + uIns.bottom, 1 + uIns.right);
        }
        Insets ins = new Insets(0, 0, 0, 0);
        Insets bi = bubbleBorder.getBorderInsets(c);
        Insets si = shadowBorder.getBorderInsets(c);
        ins.bottom = Math.max(bi.bottom, si.bottom) + uIns.bottom;
        ins.left = Math.max(bi.left, si.left) + uIns.left;
        ins.right = Math.max(bi.right, si.right) + uIns.right;
        ins.top = Math.max(bi.top, si.top) + uIns.top;
        return ins;
    }

    protected Insets getUserInsets(final Component c) {
        return PropertyUtil.getObject(c, DarkToolTipUI.KEY_INSETS, Insets.class, margin);
    }

    @Override
    public boolean isBorderOpaque() {
        return false;
    }

    @Override
    public void setPointerLocation(final Alignment side, final boolean showPointer) {
        if (showPointer) {
            bubbleBorder.setPointerSide(side);
        }
        this.showPointer = showPointer;
        alignment = side;
    }

    public void setPointerWidth(final int width) {
        bubbleBorder.setPointerWidth(width);
    }

    public void setPointerHeight(final int height) {
        bubbleBorder.setPointerSize(height);
    }

    protected boolean isPlain(final Component c) {
        if (!(c instanceof JComponent)) return false;
        Object prop = ((JComponent) c).getClientProperty(DarkToolTipUI.KEY_STYLE);
        return prop == ToolTipStyle.PLAIN || DarkToolTipUI.VARIANT_PLAIN.equals(prop);
    }

    public int getShadowSize(final Component c) {
        if (isPlain(c)) return 0;
        return shadowBorder.getShadowSize();
    }

    @Override
    public Point alignTooltip(final Component c, final Point p, final Alignment align, final Dimension dim,
            final boolean outside) {
        int factor = outside ? 1 : -1;
        int pointerDist = getDistanceToPointer();
        if (align == Alignment.EAST) {
            p.x -= factor * pointerDist;
            p.y -= factor * pointerDist;
        } else if (align == Alignment.WEST) {
            p.x += factor * pointerDist;
            p.y -= factor * pointerDist;
        } else if (align.isNorth()) {
            p.y += factor * pointerDist;
        } else if (align.isSouth()) {
            p.y -= factor * pointerDist;
        }
        if (align.isEast(false)) {
            p.x -= factor * getPointerOffset(c, dim, factor);
        } else if (align.isWest(false)) {
            p.x += factor * getPointerOffset(c, dim, factor);
        }
        return p;
    }

    private int getDistanceToPointer() {
        if (alignment == Alignment.CENTER) return 0;
        return Math.max(0, shadowBorder.getShadowSize() - bubbleBorder.getPointerSize()) + bubbleBorder.getThickness();
    }
}
