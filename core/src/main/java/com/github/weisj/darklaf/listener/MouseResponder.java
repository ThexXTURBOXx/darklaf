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
package com.github.weisj.darklaf.listener;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.function.Consumer;

public class MouseResponder implements MouseListener {

    private final Consumer<MouseEvent> consumer;

    public MouseResponder(final Consumer<MouseEvent> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void mouseClicked(final MouseEvent e) {
        if (consumer != null) {
            consumer.accept(e);
        }
    }

    @Override
    public void mousePressed(final MouseEvent e) {
        if (consumer != null) {
            consumer.accept(e);
        }
    }

    @Override
    public void mouseReleased(final MouseEvent e) {
        if (consumer != null) {
            consumer.accept(e);
        }
    }

    @Override
    public void mouseEntered(final MouseEvent e) {
        if (consumer != null) {
            consumer.accept(e);
        }
    }

    @Override
    public void mouseExited(final MouseEvent e) {
        if (consumer != null) {
            consumer.accept(e);
        }
    }
}
