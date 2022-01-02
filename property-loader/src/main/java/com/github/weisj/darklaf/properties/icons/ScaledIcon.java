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

import javax.swing.*;

import com.github.weisj.darklaf.util.Scale;

public class ScaledIcon implements Icon {

    private final Image img;
    private final Component c;

    public ScaledIcon(final Image img, final Component c) {
        this.img = img;
        this.c = c;
    }

    @Override
    public void paintIcon(final Component c, final Graphics g2, final int x, final int y) {
        Graphics2D g = (Graphics2D) g2;
        g.translate(x, y);
        g.scale(1.0 / Scale.getScaleX(g), 1.0 / Scale.getScaleY(g));
        g.drawImage(img, 0, 0, img.getWidth(c), img.getHeight(c), c);
    }

    @Override
    public int getIconWidth() {
        return (int) (img.getWidth(c) / Scale.getScaleX(c.getGraphicsConfiguration()));
    }

    @Override
    public int getIconHeight() {
        return (int) (img.getHeight(c) / Scale.getScaleY(c.getGraphicsConfiguration()));
    }
}
