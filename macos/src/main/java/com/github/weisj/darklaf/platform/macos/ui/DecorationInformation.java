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
package com.github.weisj.darklaf.platform.macos.ui;

import javax.swing.*;

class DecorationInformation {

    protected final long windowHandle;
    protected final boolean fullWindowContentEnabled;
    protected final boolean transparentTitleBarEnabled;
    protected final boolean useColoredTitleBar;
    protected final JRootPane rootPane;
    protected final boolean titleVisible;
    protected final int titleBarHeight;
    protected final float titleFontSize;

    protected DecorationInformation(final long windowHandle, final boolean fullWindowContentEnabled,
            final boolean transparentTitleBarEnabled, final boolean useColoredTitleBar,
            final JRootPane rootPane,
            final boolean titleVisible, final int titleBarHeight, final float titleFontSize) {
        this.windowHandle = windowHandle;
        this.fullWindowContentEnabled = fullWindowContentEnabled;
        this.transparentTitleBarEnabled = transparentTitleBarEnabled;
        this.useColoredTitleBar = useColoredTitleBar;
        this.rootPane = rootPane;
        this.titleVisible = titleVisible;
        this.titleBarHeight = titleBarHeight;
        this.titleFontSize = titleFontSize;
    }
}
