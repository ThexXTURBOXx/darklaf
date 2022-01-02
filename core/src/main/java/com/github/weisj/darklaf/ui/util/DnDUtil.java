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
package com.github.weisj.darklaf.ui.util;

import java.awt.*;
import java.awt.image.BufferedImage;

import com.github.weisj.darklaf.graphics.PaintUtil;
import com.github.weisj.darklaf.util.ImageUtil;
import com.github.weisj.darklaf.util.graphics.ScaledImage;

public final class DnDUtil {

    public static BufferedImage createDragImage(final Component c, final int lw, final Color borderColor) {
        return createDragImage(c, new Rectangle(0, 0, c.getWidth(), c.getHeight()), lw, borderColor);
    }

    public static BufferedImage createDragImage(final Component c, final Rectangle bounds, final int lw,
            final Color borderColor) {
        ScaledImage tabImage = ImageUtil.scaledImageFromComponent(c, bounds);
        int w = tabImage.getWidth(c);
        int h = tabImage.getHeight(c);

        Graphics g = tabImage.getGraphics();
        g.setColor(borderColor);
        PaintUtil.drawRect(g, 0, 0, w, h, lw);
        g.dispose();
        // The DnD api expects a BufferedImage. So we need to unwrap the delegate.
        return tabImage.getDelegate();
    }
}
