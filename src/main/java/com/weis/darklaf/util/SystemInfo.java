package com.weis.darklaf.util;


import org.jetbrains.annotations.Contract;

/**
 * @author Konstantin Bulenkov
 */
public class SystemInfo {
    public static final String X86 = "32";
    public static final String X64 = "64";
    public static final String OS_NAME = System.getProperty("os.name");
    public static final String OS_VERSION = System.getProperty("os.version").toLowerCase();
    public static final String JAVA_VERSION = System.getProperty("java.version");
    public static final String JAVA_RUNTIME_VERSION = System.getProperty("java.runtime.version");
    protected static final String _OS_NAME;
    public static final boolean isWindows;
    public static final boolean isOS2;
    public static final boolean isMac;
    public static final boolean isLinux;
    public static final boolean isUnix;
    public static final boolean isFileSystemCaseSensitive;
    public static final boolean isMacOSLion;
    public static final boolean isAppleJvm;
    public static final boolean isOracleJvm;
    public static final boolean isSunJvm;
    public static final String jreArchitecture = System.getProperty("sun.arch.data.model");
    public static final boolean isX86;
    public static final boolean isX64;
    public static final boolean isUndefined;

    @Contract(pure = true)
    public SystemInfo() {
    }

    public static boolean isOsVersionAtLeast(String version) {
        return compareVersionNumbers(OS_VERSION, version) >= 0;
    }

    private static boolean isTiger() {
        return isMac && !OS_VERSION.startsWith("10.0")
                && !OS_VERSION.startsWith("10.1")
                && !OS_VERSION.startsWith("10.2")
                && !OS_VERSION.startsWith("10.3");
    }

    private static boolean isLeopard() {
        return isMac && isTiger() && !OS_VERSION.startsWith("10.4");
    }

    private static boolean isSnowLeopard() {
        return isMac && isLeopard() && !OS_VERSION.startsWith("10.5");
    }

    private static boolean isLion() {
        return isMac && isSnowLeopard() && !OS_VERSION.startsWith("10.6");
    }

    private static boolean isMountainLion() {
        return isMac && isLion() && !OS_VERSION.startsWith("10.7");
    }

    public static int compareVersionNumbers(String v1, String v2) {
        if (v1 == null && v2 == null) {
            return 0;
        } else if (v1 == null) {
            return -1;
        } else if (v2 == null) {
            return 1;
        } else {
            String[] part1 = v1.split("[.\\_-]");
            String[] part2 = v2.split("[._\\-]");

            int idx;
            for(idx = 0; idx < part1.length && idx < part2.length; ++idx) {
                String p1 = part1[idx];
                String p2 = part2[idx];
                int cmp;
                if (p1.matches("\\d+") && p2.matches("\\d+")) {
                    cmp = (Integer.valueOf(p1)).compareTo(Integer.valueOf(p2));
                } else {
                    cmp = part1[idx].compareTo(part2[idx]);
                }

                if (cmp != 0) {
                    return cmp;
                }
            }

            if (part1.length == part2.length) {
                return 0;
            } else {
                return part1.length > idx ? 1 : -1;
            }
        }
    }

    public static boolean isJavaVersionAtLeast(String v) {
        return com.bulenkov.iconloader.util.StringUtil.compareVersionNumbers(JAVA_RUNTIME_VERSION, v) >= 0;
    }

    private static boolean isOracleJvm() {
        String vendor = getJavaVmVendor();
        return vendor != null && com.bulenkov.iconloader.util.StringUtil.containsIgnoreCase(vendor, "Oracle");
    }

    private static boolean isSunJvm() {
        String vendor = getJavaVmVendor();
        return vendor != null && com.bulenkov.iconloader.util.StringUtil.containsIgnoreCase(vendor, "Sun") && com.bulenkov.iconloader.util.StringUtil.containsIgnoreCase(vendor, "Microsystems");
    }

    private static boolean isAppleJvm() {
        String vendor = getJavaVmVendor();
        return vendor != null && com.bulenkov.iconloader.util.StringUtil.containsIgnoreCase(vendor, "Apple");
    }

    public static String getJavaVmVendor() {
        return System.getProperty("java.vm.vendor");
    }

    static {
        _OS_NAME = OS_NAME.toLowerCase();
        isWindows = _OS_NAME.startsWith("windows");
        isOS2 = _OS_NAME.startsWith("os/2") || _OS_NAME.startsWith("os2");
        isMac = _OS_NAME.startsWith("mac");
        isLinux = _OS_NAME.startsWith("linux");
        isUnix = !isWindows && !isOS2;
        isFileSystemCaseSensitive = isUnix && !isMac;
        isMacOSLion = isLion();
        isAppleJvm = isAppleJvm();
        isOracleJvm = isOracleJvm();
        isSunJvm = isSunJvm();
        isX64 = X64.equals(jreArchitecture);
        isX86 = X86.equals(jreArchitecture);
        isUndefined = !isX86 & !isX64;
    }
}