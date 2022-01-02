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
package com.github.weisj.darklaf.platform.macos;

import java.awt.*;
import java.util.function.Consumer;

import javax.swing.*;

import com.github.weisj.darklaf.theme.info.*;
import com.github.weisj.darklaf.util.SystemInfo;

public class MacOSThemePreferenceProvider implements ThemePreferenceProvider {

    private final PreferredThemeStyle fallbackStyle =
            new PreferredThemeStyle(ContrastRule.STANDARD, ColorToneRule.LIGHT);
    private final MacOSPreferenceMonitor monitor = new MacOSPreferenceMonitor(this);
    private Consumer<PreferredThemeStyle> callback;

    @Override
    public PreferredThemeStyle getPreference() {
        if (!MacOSLibrary.get().isLoaded()) return fallbackStyle;
        boolean darkMode = JNIThemeInfoMacOS.isDarkThemeEnabled();
        boolean highContrast = JNIThemeInfoMacOS.isHighContrastEnabled();
        Color accentColor = JNIThemeInfoMacOS.getAccentColor();
        Color selectionColor = JNIThemeInfoMacOS.getSelectionColor();
        return create(darkMode, highContrast, accentColor, selectionColor);
    }

    private PreferredThemeStyle create(final boolean darkMode, final boolean highContrast, final Color accentColor,
            final Color selectionColor) {
        ContrastRule contrastRule = highContrast ? ContrastRule.HIGH_CONTRAST : ContrastRule.STANDARD;
        ColorToneRule toneRule = darkMode ? ColorToneRule.DARK : ColorToneRule.LIGHT;
        AccentColorRule accentColorRule = AccentColorRule.fromColor(accentColor, selectionColor);
        return new PreferredThemeStyle(contrastRule, toneRule, accentColorRule);
    }

    void reportPreferenceChange(final boolean dark, final boolean highContrast, final Color accentColor,
            final Color selectionColor) {
        if (callback != null) {
            PreferredThemeStyle style = create(dark, highContrast, accentColor, selectionColor);
            callback.accept(style);
        }
    }

    @Override
    public void setReporting(final boolean reporting) {
        if (reporting && !MacOSLibrary.get().isLoaded()) MacOSLibrary.get().updateLibrary();
        synchronized (monitor) {
            monitor.setRunning(reporting);
        }
    }

    @Override
    public boolean isReporting() {
        return monitor.isRunning();
    }

    @Override
    public void initialize() {
        MacOSLibrary.get().updateLibrary();
        if (MacOSLibrary.get().isLoaded()) {
            /*
             * Patching the app bundle doesn't work anymore in JDK 14. I am not sure what the last version is,
             * where it still works, so for now everything below or equal to JDK 11 will benefit from the newer
             * catalina algorithm. For everything else we fall back to the Mojave style. This should still give
             * a correct answer most of the time.
             */
            JNIThemeInfoMacOS.patchAppBundle(!SystemInfo.isJavaVersionAtLeast("12"));
            SwingUtilities.invokeLater(() -> {
                /* Do nothing. This simply forces native resources to be loaded */
            });
            JNIThemeInfoMacOS.unpatchAppBundle();
        }
    }

    @Override
    public void setCallback(final Consumer<PreferredThemeStyle> callback) {
        this.callback = callback;
    }

    @Override
    public boolean canReport() {
        return true;
    }

    @Override
    public boolean supportsNativeAccentColor() {
        return SystemInfo.isMacOSMojave && MacOSLibrary.get().isLoaded();
    }

    @Override
    public boolean supportsNativeSelectionColor() {
        return SystemInfo.isMacOSMojave && MacOSLibrary.get().isLoaded();
    }

    @Override
    public boolean supportsNativeTheme() {
        return MacOSLibrary.get().isLoaded();
    }
}
