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
import java.util.EventObject;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.tree.TreeCellEditor;

public class TreeCellEditorDelegate implements TreeCellEditor {

    protected final TreeCellEditor delegate;

    public TreeCellEditorDelegate(final TreeCellEditor editor) {
        this.delegate = editor;
    }

    @Override
    public Component getTreeCellEditorComponent(final JTree tree, final Object value, final boolean isSelected,
            final boolean expanded, final boolean leaf, final int row) {
        return delegate.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
    }

    public TreeCellEditor getDelegate() {
        return delegate;
    }

    @Override
    public Object getCellEditorValue() {
        return getDelegate().getCellEditorValue();
    }

    @Override
    public boolean isCellEditable(final EventObject anEvent) {
        return getDelegate().isCellEditable(anEvent);
    }

    @Override
    public boolean shouldSelectCell(final EventObject anEvent) {
        return getDelegate().shouldSelectCell(anEvent);
    }

    @Override
    public boolean stopCellEditing() {
        return getDelegate().stopCellEditing();
    }

    @Override
    public void cancelCellEditing() {
        getDelegate().cancelCellEditing();
    }

    @Override
    public void addCellEditorListener(final CellEditorListener l) {
        getDelegate().addCellEditorListener(l);
    }

    @Override
    public void removeCellEditorListener(final CellEditorListener l) {
        getDelegate().removeCellEditorListener(l);
    }
}
