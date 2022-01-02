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
package com.github.weisj.darklaf.core.theme;

import java.util.Properties;

import javax.swing.UIDefaults;

import com.github.weisj.darklaf.properties.icons.IconResolver;
import com.github.weisj.darklaf.theme.Theme;
import com.github.weisj.darklaf.theme.info.ColorToneRule;
import com.github.weisj.darklaf.theme.info.PresetIconRule;
import com.github.weisj.darklaf.util.SystemInfo;

public class MyCustomTheme extends Theme {

    @Override
    public void customizeGlobals(final Properties properties, final UIDefaults currentDefaults,
            final IconResolver iconResolver) {
        super.customizeGlobals(properties, currentDefaults, iconResolver);
        /*
         * Properties in the globals file should have a 'globals.' to be applied globally.
         */
        loadCustomProperties("globals", properties, currentDefaults, iconResolver);
    }

    @Override
    public void customizePlatformProperties(final Properties properties, final UIDefaults currentDefaults,
            final IconResolver iconResolver) {
        super.customizePlatformProperties(properties, currentDefaults, iconResolver);
        /*
         * Loading this file (and having it present) is optional.
         */
        final String osSuffix = SystemInfo.isMac ? "mac" : SystemInfo.isWindows ? "windows" : "linux";
        loadCustomProperties(osSuffix, properties, currentDefaults, iconResolver);
    }

    @Override
    public void customizeUIProperties(final Properties properties, final UIDefaults currentDefaults,
            final IconResolver iconResolver) {
        super.customizeUIProperties(properties, currentDefaults, iconResolver);
        /*
         * Loading this file (and having it present) is optional.
         */
        loadCustomProperties("ui", properties, currentDefaults, iconResolver);
    }

    @Override
    protected PresetIconRule getPresetIconRule() {
        /*
         * Use a custom icon com.github.weisj.theme. Colors are defined in my_custom_theme_icons.properties.
         */
        return PresetIconRule.NONE;
    }

    @Override
    public String getPrefix() {
        return "my_custom_theme";
    }

    @Override
    public String getName() {
        return "My Custom Theme Name";
    }

    @Override
    protected Class<? extends Theme> getLoaderClass() {
        return MyCustomTheme.class;
    }

    @Override
    public ColorToneRule getColorToneRule() {
        return ColorToneRule.LIGHT;
    }
}
