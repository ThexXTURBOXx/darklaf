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
package com.github.weisj.darklaf.util;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;

public final class Scale {
    public static final double SCALE;
    public static final double SCALE_X;
    public static final double SCALE_Y;
    private static final double EPSILON = 0.0001;

    static {
        if (GraphicsEnvironment.isHeadless()) {
            SCALE = 1;
            SCALE_X = 1;
            SCALE_Y = 1;
        } else {
            DisplayMode mode =
                    GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            SCALE_X = mode.getWidth() / (double) screenSize.width;
            SCALE_Y = mode.getHeight() / (double) screenSize.height;
            SCALE = SCALE_X;
        }
    }

    public static double scaleWidth(final double value, final GraphicsConfiguration gc) {
        if (gc == null) return scaleWidth(value);
        AffineTransform transform = gc.getDefaultTransform();
        return transform.getScaleX() * value;
    }

    public static double scaleHeight(final double value, final GraphicsConfiguration gc) {
        if (gc == null) return scaleHeight(value);
        AffineTransform transform = gc.getDefaultTransform();
        return transform.getScaleY() * value;
    }

    public static double inverseScaleWidth(final double value, final GraphicsConfiguration gc) {
        if (gc == null) return inverseScaleWidth(value);
        AffineTransform transform = gc.getDefaultTransform();
        return (1 / transform.getScaleX()) * value;
    }

    public static double inverseScaleHeight(final double value, final GraphicsConfiguration gc) {
        if (gc == null) return inverseScaleHeight(value);
        AffineTransform transform = gc.getDefaultTransform();
        return (1 / transform.getScaleY()) * value;
    }

    public static int scaleWidth(final int i) {
        return (int) (SCALE_X * i);
    }

    public static float scaleWidth(final float f) {
        return (float) (SCALE_X * f);
    }

    public static double scaleWidth(final double d) {
        return SCALE_X * d;
    }

    public static int inverseScaleWidth(final int i) {
        return (int) ((1 / SCALE_X) * i);
    }

    public static float inverseScaleWidth(final float f) {
        return (float) ((1 / SCALE_X) * f);
    }

    public static double inverseScaleWidth(final double d) {
        return (1 / SCALE_X) * d;
    }

    public static int scaleHeight(final int i) {
        return (int) (SCALE_Y * i);
    }

    public static float scaleHeight(final float f) {
        return (float) (SCALE_Y * f);
    }

    public static double scaleHeight(final double d) {
        return SCALE_Y * d;
    }

    public static int inverseScaleHeight(final int i) {
        return (int) ((1 / SCALE_Y) * i);
    }

    public static float inverseScaleHeight(final float f) {
        return (float) ((1 / SCALE_Y) * f);
    }

    public static double inverseScaleHeight(final double d) {
        return (1 / SCALE_Y) * d;
    }

    public static double getScaleX(final Graphics2D g) {
        return g.getTransform().getScaleX();
    }

    public static double getScaleY(final Graphics2D g) {
        return g.getTransform().getScaleY();
    }

    public static double getScaleX(final GraphicsConfiguration gc) {
        if (gc == null) return SCALE_X;
        return gc.getDefaultTransform().getScaleX();
    }

    public static double getScaleY(final GraphicsConfiguration gc) {
        if (gc == null) return SCALE_Y;
        return gc.getDefaultTransform().getScaleY();
    }

    public static Dimension scale(final GraphicsConfiguration gc, final Dimension size) {
        return new Dimension((int) scaleWidth(size.width, gc), (int) scaleHeight(size.height, gc));
    }

    public static Point scale(final GraphicsConfiguration gc, final Point p) {
        return new Point((int) scaleWidth(p.x, gc), (int) scaleHeight(p.y, gc));
    }

    public static Dimension inverseScale(final GraphicsConfiguration gc, final Dimension size) {
        return new Dimension((int) inverseScaleWidth(size.width, gc), (int) inverseScaleHeight(size.height, gc));
    }

    public static Point inverseScale(final GraphicsConfiguration gc, final Point p) {
        try {
            return (Point) gc.getDefaultTransform().inverseTransform(p, p);
        } catch (final NoninvertibleTransformException e) {
            return p;
        }
    }

    public static double scale(final double scale, final double value) {
        return scale * value;
    }

    public static Dimension scale(final double scaleX, final double scaleY, final Dimension size) {
        return new Dimension((int) scale(scaleX, size.width), (int) scale(scaleY, size.height));
    }

    public static boolean equalWithError(final double a, final double b) {
        return Math.abs(a - b) < EPSILON;
    }
}
