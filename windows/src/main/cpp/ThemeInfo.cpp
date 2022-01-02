/*
 * MIT License
 *
 * Copyright (c) 2020-2021 Jannis Weis
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
#include "com_github_weisj_darklaf_platform_windows_JNIThemeInfoWindows.h"
#include "Registry.h"

#include <string>
#include <thread>
#include <atomic>
#include <windows.h>
#include <winuser.h>

constexpr auto DARK_MODE_PATH = "Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize";
constexpr auto DARK_MODE_KEY = "AppsUseLightTheme";

constexpr auto FONT_SCALE_PATH = "Software\\Microsoft\\Accessibility";
constexpr auto FONT_SCALE_KEY = "TextScaleFactor";

constexpr auto HIGH_CONTRAST_PATH = "Control Panel\\Accessibility\\HighContrast";
constexpr auto HIGH_CONTRAST_THEME_KEY = "High Contrast Scheme";
constexpr auto HIGH_CONTRAST_LIGHT_THEME = "High Contrast White";

constexpr auto ACCENT_COLOR_PATH = "SOFTWARE\\Microsoft\\Windows\\DWM";
constexpr auto ACCENT_COLOR_KEY = "ColorizationColor";

constexpr auto DARK_MODE_DEFAULT_VALUE = false;
constexpr auto HIGH_CONTRAST_DEFAULT_VALUE = false;
constexpr auto FONT_SCALE_DEFAULT_VALUE = 100;
constexpr auto ACCENT_COLOR_DEFAULT_VALUE = 0;

static bool IsHighContrastMode() {
    HIGHCONTRAST info = { 0, 0, 0 };
    info.cbSize = sizeof(HIGHCONTRAST);
    BOOL ok = SystemParametersInfo(SPI_GETHIGHCONTRAST, 0, &info, 0);
    if (ok) {
        return info.dwFlags & HCF_HIGHCONTRASTON;
    }
    return HIGH_CONTRAST_DEFAULT_VALUE;
}

static bool IsDarkMode() {
    try {
        bool appsUseDark = (0 == RegGetDword(HKEY_CURRENT_USER, DARK_MODE_PATH, DARK_MODE_KEY));
        bool isHighContrast = IsHighContrastMode();
        if (!isHighContrast) return appsUseDark;

        std::string themeValue = RegGetString(HKEY_CURRENT_USER, HIGH_CONTRAST_PATH, HIGH_CONTRAST_THEME_KEY);
        return (strcmp(themeValue.c_str(), HIGH_CONTRAST_LIGHT_THEME) != 0);
    } catch (LONG) {
        return DARK_MODE_DEFAULT_VALUE;
    }
}

static unsigned int GetTextScaleFactor() {
    try {
        return RegGetDword(HKEY_CURRENT_USER, FONT_SCALE_PATH, FONT_SCALE_KEY);
    } catch (LONG) {
        return FONT_SCALE_DEFAULT_VALUE;
    }
}

static bool RegisterRegistryEvent(const LPCSTR subKey, HANDLE event) {
    HKEY hKey;
    REGSAM flags = KEY_NOTIFY;
    ModifyFlags(flags);
    DWORD res = RegOpenKeyExA(HKEY_CURRENT_USER, subKey, 0, flags, &hKey);
    if (res == ERROR_SUCCESS) {
        LSTATUS status = RegNotifyChangeKeyValue(hKey, FALSE, REG_NOTIFY_CHANGE_LAST_SET, event, TRUE);
        return status == ERROR_SUCCESS;
    } else {
        return FALSE;
    }
}

static int GetAccentColor() {
    try {
        return RegGetDword(HKEY_CURRENT_USER, ACCENT_COLOR_PATH, ACCENT_COLOR_KEY);
    } catch (LONG) {
        return ACCENT_COLOR_DEFAULT_VALUE;
    }
}

JNIEXPORT jboolean JNICALL
Java_com_github_weisj_darklaf_platform_windows_JNIThemeInfoWindows_isDarkThemeEnabled(JNIEnv*, jclass) {
    return static_cast<jboolean>(IsDarkMode());
}

JNIEXPORT jboolean JNICALL
Java_com_github_weisj_darklaf_platform_windows_JNIThemeInfoWindows_isHighContrastEnabled(JNIEnv*, jclass) {
    return static_cast<jboolean>(IsHighContrastMode());
}

JNIEXPORT jlong JNICALL
Java_com_github_weisj_darklaf_platform_windows_JNIThemeInfoWindows_getFontScaleFactor(JNIEnv*, jclass) {
    return static_cast<jlong>(GetTextScaleFactor());
}

JNIEXPORT jint JNICALL
Java_com_github_weisj_darklaf_platform_windows_JNIThemeInfoWindows_getAccentColor(JNIEnv*, jclass) {
    return static_cast<jint>(GetAccentColor());
}

struct EventHandler {
    JavaVM *jvm;
    JNIEnv *env;
    jobject callback;
    HANDLE eventHandle;
    std::thread notificationLoop;
    std::atomic<bool> running = FALSE;

    void runCallBack() {
        jclass runnableClass = env->GetObjectClass(callback);
        jmethodID runMethodId = env->GetMethodID(runnableClass, "run", "()V");
        if (runMethodId) {
            env->CallVoidMethod(callback, runMethodId);
        }
    }

    bool awaitPreferenceChange() {
        if (!RegisterRegistryEvent(FONT_SCALE_PATH, eventHandle)) return FALSE;
        if (!RegisterRegistryEvent(DARK_MODE_PATH, eventHandle)) return FALSE;
        if (!RegisterRegistryEvent(HIGH_CONTRAST_PATH, eventHandle)) return FALSE;
        if (!RegisterRegistryEvent(ACCENT_COLOR_PATH, eventHandle)) return FALSE;
        return WaitForSingleObject(eventHandle, INFINITE) != WAIT_FAILED;
    }

    void run() {
        int getEnvStat = jvm->GetEnv((void**) &env, JNI_VERSION_1_6);
        if (getEnvStat == JNI_EDETACHED) {
            if (jvm->AttachCurrentThread((void**) &env, NULL) != 0) return;
        } else if (getEnvStat == JNI_EVERSION) {
            return;
        }
        while (running && awaitPreferenceChange()) {
            if (running) {
                runCallBack();
                if (env->ExceptionCheck()) {
                    env->ExceptionDescribe();
                    break;
                }
            }
        }
        jvm->DetachCurrentThread();
    }

    void stop() {
        running = FALSE;
        SetEvent(eventHandle);
        notificationLoop.join();
    }

    EventHandler(JavaVM *jvm_, jobject callback_, HANDLE eventHandle_) {
        jvm = jvm_;
        callback = callback_;
        eventHandle = eventHandle_;
        running = TRUE;
        notificationLoop = std::thread(&EventHandler::run, this);
    }
};

JNIEXPORT jlong JNICALL
Java_com_github_weisj_darklaf_platform_windows_JNIThemeInfoWindows_createEventHandler(JNIEnv *env, jclass, jobject callback) {
    JavaVM *jvm;
    if (env->GetJavaVM(&jvm) == JNI_OK) {
        jobject callbackRef = env->NewGlobalRef(callback);
        HANDLE event = CreateEvent(NULL, FALSE, FALSE, NULL);
        EventHandler* eventHandler = new EventHandler(jvm, callbackRef, event);
        return reinterpret_cast<jlong>(eventHandler);
    }
    return static_cast<jlong>(0);
}

JNIEXPORT void JNICALL
Java_com_github_weisj_darklaf_platform_windows_JNIThemeInfoWindows_deleteEventHandler(JNIEnv *env, jclass, jlong eventHandler) {
    EventHandler *handler = reinterpret_cast<EventHandler *>(eventHandler);
    if (handler) {
        env->DeleteGlobalRef(handler->callback);
        handler->stop();
        delete handler;
    }
}
