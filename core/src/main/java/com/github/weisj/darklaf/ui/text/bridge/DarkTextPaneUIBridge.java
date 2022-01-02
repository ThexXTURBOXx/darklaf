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
package com.github.weisj.darklaf.ui.text.bridge;

import com.github.weisj.darklaf.ui.text.DarkEditorPaneUI;
import com.github.weisj.darklaf.ui.text.dummy.DummyEditorPane;
import com.github.weisj.darklaf.ui.text.dummy.DummyTextPaneUI;
import com.github.weisj.darklaf.ui.text.dummy.DummyTextUIMethods;
import com.github.weisj.darklaf.util.value.WeakShared;

/** @author Jannis Weis */
public abstract class DarkTextPaneUIBridge extends DarkEditorPaneUI {

    private static final WeakShared<DummyEditorPane> sharedDummyTextPane = new WeakShared<>(DummyEditorPane::new);
    private static final WeakShared<DummyTextUIMethods> sharedDummyUI = new WeakShared<>(DummyTextPaneUI::new);

    @Override
    protected DummyTextUIMethods createDummyUI() {
        return sharedDummyUI.get();
    }

    @Override
    protected DummyEditorPane createDummyEditorPane() {
        return sharedDummyTextPane.get();
    }
}
