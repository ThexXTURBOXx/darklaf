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
package com.github.weisj.darklaf.components.tabframe;

import java.awt.*;
import java.util.Objects;

import javax.swing.*;

import com.github.weisj.darklaf.ui.tabframe.DarkTabFrameTabLabelUI;
import com.github.weisj.darklaf.util.Alignment;

/**
 * Tab Component for {@link JTabFrame}.
 *
 * @author Jannis Weis
 */
public class TabFrameTabLabel extends JLabel implements TabFrameTab {

    private JTabFrame parent;
    private Alignment orientation;
    private String title;
    private boolean selected;
    private int accelerator;
    private int index;

    /**
     * Create new TabComponent for the frame of {@link JTabFrame}.
     *
     * @param title the title.
     * @param icon the icon.
     * @param orientation the alignment.
     * @param index the index.
     * @param parent the parent layout manager.
     */
    public TabFrameTabLabel(final String title, final Icon icon, final Alignment orientation, final int index,
            final JTabFrame parent) {
        this.index = index;
        this.accelerator = -1;
        this.parent = parent;
        setOrientation(orientation);
        setIcon(icon);
        setTitle(title);
        setText(title);
    }

    @Override
    public void setEnabled(final boolean enabled) {
        if (enabled != isEnabled() && !enabled) {
            getTabFrame().toggleTab(getOrientation(), getIndex(), false);
        }
        super.setEnabled(enabled);
    }

    @Override
    public String getUIClassID() {
        return "TabFrameTabLabelUI";
    }

    public void setUI(final DarkTabFrameTabLabelUI ui) {
        super.setUI(ui);
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public void setIndex(final int index) {
        this.index = index;
    }

    @Override
    public Component getComponent() {
        return this;
    }

    @Override
    public Alignment getOrientation() {
        return orientation;
    }

    @Override
    public void setOrientation(final Alignment a) {
        if (this.orientation == a) return;
        Alignment oldOrientation = this.orientation;
        this.orientation = a;
        firePropertyChange(KEY_ORIENTATION, oldOrientation, orientation);
    }

    @Override
    public boolean isSelected() {
        return selected;
    }

    @Override
    public void setSelected(final boolean selected) {
        if (selected == this.selected) return;
        boolean oldSelected = this.selected;
        this.selected = selected;
        firePropertyChange(KEY_SELECTED, oldSelected, selected);
    }

    @Override
    public int getAccelerator() {
        return accelerator;
    }

    @Override
    public void setAccelerator(final int accelerator) {
        if (this.accelerator == accelerator) return;
        int oldAccelerator = this.accelerator;
        this.accelerator = accelerator;
        firePropertyChange(KEY_ACCELERATOR, oldAccelerator, accelerator);
    }

    @Override
    public JTabFrame getTabFrame() {
        return parent;
    }

    @Override
    public void setTabFrame(final JTabFrame parent) {
        JTabFrame old = this.parent;
        this.parent = parent;
        firePropertyChange(KEY_TAB_FRAME_PARENT, old, parent);
    }

    /**
     * Returns the current title. This needn't be the same as the displayed text. For this use
     * {@link #getText()} instead.
     *
     * @return the current title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set the title of the component.
     *
     * @param title the title
     */
    public void setTitle(final String title) {
        if (Objects.equals(title, this.title)) return;
        String oldTitle = this.title;
        this.title = title;
        firePropertyChange(KEY_TITLE, oldTitle, selected);
    }
}
