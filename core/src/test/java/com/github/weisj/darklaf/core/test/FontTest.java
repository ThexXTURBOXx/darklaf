/*
 * MIT License
 *
 * Copyright (c) 2021 Jannis Weis
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
 *
 */
package com.github.weisj.darklaf.core.test;

import java.awt.Font;
import java.awt.RenderingHints;
import java.awt.font.TextAttribute;
import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.UIManager;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import com.github.weisj.darklaf.ui.DemoResources;
import com.github.weisj.darklaf.util.SystemInfo;

class FontTest extends AbstractImageTest {

    private static final Map<AttributedCharacterIterator.Attribute, Integer> kerning;

    static {
        kerning = Collections.singletonMap(TextAttribute.KERNING, TextAttribute.KERNING_ON);
    }

    public FontTest() {
        super("font");
    }

    @BeforeAll
    static void setup() {
        TestUtils.ensureLafInstalled();
    }

    @Test
    @EnabledOnOs({OS.MAC, OS.WINDOWS, OS.LINUX})
    void testFontChoices() {
        JTextArea textArea = new JTextArea();
        textArea.setText(DemoResources.KERNING_TEST);
        textArea.setSize(textArea.getPreferredSize());
        textArea.doLayout();

        if (SystemInfo.isMac) {
            List<FontSpec> fontSpecs = new ArrayList<>();
            if (SystemInfo.isMacOSMojave) {
                fontSpecs.add(new FontSpec(".AppleSystemUIFont", false));
                fontSpecs.add(new FontSpec(".AppleSystemUIFont", true));
            }
            fontSpecs.add(new FontSpec(".SF NS Text", false));
            fontSpecs.add(new FontSpec(".SF NS Text", true));
            fontSpecs.add(new FontSpec("Helvetica Neue", false));
            fontSpecs.add(new FontSpec("Helvetica Neue", true));
            createImages("macOS", textArea, fontSpecs);
        } else if (SystemInfo.isWindows) {
            List<FontSpec> fontSpecs = new ArrayList<>();
            fontSpecs.add(new FontSpec("Segoe UI", false));
            fontSpecs.add(new FontSpec("Segoe UI", true));
            createImages("windows", textArea, fontSpecs);
        } else if (SystemInfo.isLinux) {
            List<FontSpec> fontSpecs = new ArrayList<>();
            fontSpecs.add(new FontSpec("Ubuntu", false));
            fontSpecs.add(new FontSpec("Ubuntu", true));
            createImages("linux", textArea, fontSpecs);
        }
    }

    private void createImages(final String folder, final JComponent c, final List<FontSpec> fontSpecs) {
        for (FontSpec spec : fontSpecs) {
            c.setFont(createFont(spec.fontName));
            setFractionalMetrics(spec.useFractionalMetrics);
            Assertions.assertNotNull(saveScreenShot(getImagePath(folder, spec), c));
        }
    }

    private String getImagePath(final String folder, final FontSpec spec) {
        return getPath(folder + "/" + spec.getImageName());
    }

    private static void setFractionalMetrics(final boolean enabled) {
        UIManager.put(RenderingHints.KEY_FRACTIONALMETRICS,
                enabled ? RenderingHints.VALUE_FRACTIONALMETRICS_ON : RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
    }

    private static Font createFont(final String name) {
        return new Font(name, Font.PLAIN, 12).deriveFont(kerning);
    }

    private static class FontSpec {
        private final String fontName;
        private final boolean useFractionalMetrics;

        private FontSpec(final String fontName, final boolean useFractionalMetrics) {
            this.fontName = fontName;
            this.useFractionalMetrics = useFractionalMetrics;
        }

        private String getImageName() {
            String name = fontName.replace(".", "").replace(" ", "_").toLowerCase();
            if (useFractionalMetrics) {
                name += "_frac_metrics";
            }
            return name;
        }
    }
}
