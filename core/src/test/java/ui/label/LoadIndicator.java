/*
 * MIT License
 *
 * Copyright (c) 2020 Jannis Weis
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package ui.label;

import javax.swing.*;

import ui.ComponentDemo;
import ui.DemoPanel;
import ui.DemoResources;

import com.github.weisj.darklaf.components.loading.LoadingIndicator;

public class LoadIndicator extends LabelDemoBase<LoadingIndicator> {

    public static void main(final String[] args) {
        ComponentDemo.showDemo(new LoadIndicator());
    }

    @Override
    protected JPanel createControlPanel(final DemoPanel panel, final LoadingIndicator label) {
        JPanel controls = super.createControlPanel(panel, label);
        controls.add(new JCheckBox("running") {
            {
                setSelected(label.isRunning());
                addActionListener(e -> label.setRunning(isSelected()));
            }
        });
        return controls;
    }

    protected LoadingIndicator createLabel() {
        return new LoadingIndicator("Load Indicator", DemoResources.FOLDER_ICON, JLabel.LEFT);
    }

    @Override
    public String getTitle() {
        return "Load Indicator Demo";
    }
}
