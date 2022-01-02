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
package com.github.weisj.darklaf.ui.cell;

import javax.swing.*;

import com.github.weisj.darklaf.components.tristate.TristateState;
import com.github.weisj.darklaf.ui.tree.DarkTreeCellRendererDelegate;

public class DarkCellRendererTristateButton
        extends DarkCellRendererToggleButton<DarkCellRendererToggleButton.CellTristateButton> {

    public DarkCellRendererTristateButton() {
        this(true);
    }

    public DarkCellRendererTristateButton(final boolean opaque) {
        super(new CellTristateButton(opaque));
    }

    @Override
    protected void setValue(final Object value) {
        Object unwrapped = DarkTreeCellRendererDelegate.unwrapValue(value);
        if (unwrapped instanceof TristateState) {
            getButton().getTristateModel().setState((TristateState) unwrapped);
        }
    }

    @Override
    public CellTristateButton getTreeCellRendererComponent(final JTree tree, final Object value, final boolean selected,
            final boolean expanded, final boolean leaf, final int row, final boolean focus) {
        CellTristateButton renderer =
                super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, focus);
        renderer.setAllowsIndeterminate(!leaf);
        return renderer;
    }
}
