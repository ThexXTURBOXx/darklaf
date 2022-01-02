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
package com.github.weisj.darklaf.components;

import javax.swing.tree.DefaultMutableTreeNode;

import com.github.weisj.darklaf.components.tree.LabeledTreeNode;

/** @author Jannis Weis */
public class SelectableTreeNode extends DefaultMutableTreeNode implements LabeledTreeNode {

    private String label;

    public SelectableTreeNode() {
        this(null, false);
    }

    public SelectableTreeNode(final String label, final boolean isSelected) {
        this(label, isSelected, true);
    }

    public SelectableTreeNode(final String label, final boolean isSelected, final boolean allowsChildren) {
        super();
        parent = null;
        this.allowsChildren = allowsChildren;
        this.userObject = isSelected;
        this.label = label;
    }

    public boolean isSelected() {
        return Boolean.TRUE.equals(getUserObject());
    }

    public void setSelected(final boolean selected) {
        setUserObject(selected);
    }

    @Override
    public String getLabel() {
        return label;
    }

    public void setLabel(final String label) {
        this.label = label;
    }
}
