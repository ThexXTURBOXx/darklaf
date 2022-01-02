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
package com.github.weisj.darklaf.components.help;

import javax.swing.*;

import com.github.weisj.darklaf.ui.button.DarkButtonUI;

public class HelpButton extends JButton {

    private boolean useColoredIcon;

    public HelpButton() {
        this(null);
    }

    public HelpButton(final String text) {
        super(text);
        init();
    }

    protected void init() {
        putClientProperty(DarkButtonUI.KEY_SQUARE, true);
        putClientProperty(DarkButtonUI.KEY_ROUND, true);
        putClientProperty(DarkButtonUI.KEY_NO_BORDERLESS_OVERWRITE, true);
        setUseColoredIcon(true);
        setIcon(UIManager.getIcon("HelpButton.helpHighlightIcon"));
        setDisabledIcon(UIManager.getIcon("HelpButton.helpDisabledIcon"));
    }

    /**
     * Sets whether the button uses a colored help icon.
     *
     * @param colored true if colored.
     */
    public void setUseColoredIcon(final boolean colored) {
        useColoredIcon = colored;
        setIcon(colored
                ? UIManager.getIcon("HelpButton.helpHighlightIcon")
                : UIManager.getIcon("HelpButton.helpIcon"));
    }

    /**
     * Returns whether the button uses a colored icon.
     *
     * @return true if colored.
     */
    public boolean isUseColoredIcon() {
        return useColoredIcon;
    }
}
