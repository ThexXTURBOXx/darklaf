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
package com.github.weisj.darklaf.properties.icons;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;

import com.github.weisj.darklaf.properties.PropertyLoader;
import com.github.weisj.darklaf.properties.parser.ParseResult;
import com.github.weisj.darklaf.properties.parser.Parser;
import com.github.weisj.darklaf.properties.parser.ParserContext;
import com.github.weisj.darklaf.util.ColorUtil;
import com.github.weisj.darklaf.util.LogUtil;
import com.github.weisj.darklaf.util.Pair;
import com.github.weisj.darklaf.util.Types;
import com.kitfox.svg.*;
import com.kitfox.svg.animation.AnimationElement;
import com.kitfox.svg.app.beans.SVGIcon;
import com.kitfox.svg.xml.StyleAttribute;

/**
 * Utility class responsible for patching color definitions in svg icons.
 *
 * @author Jannis Weis
 */
public final class IconColorMapper {
    private static final String INLINE_VALUE_PREFIX = "%";
    private static final Logger LOGGER = LogUtil.getLogger(IconLoader.class);
    private static final Color FALLBACK_COLOR = Color.RED;

    public static void patchColors(final SVGIcon svgIcon) {
        patchColors(svgIcon, UIManager.getDefaults());
    }

    public static void patchColors(final SVGIcon svgIcon, final Map<Object, Object> contextDefaults) {
        patchColors(svgIcon, contextDefaults, null);
    }

    public static void patchColors(final SVGIcon svgIcon, final Map<Object, Object> defaults,
            final Map<Object, Object> contextDefaults) {
        SVGUniverse universe = svgIcon.getSvgUniverse();
        SVGDiagram diagram = universe.getDiagram(svgIcon.getSvgURI());
        LOGGER.finer(() -> "Patching colors of icon " + svgIcon.getSvgURI());
        try {
            loadColors(diagram, defaults, contextDefaults);
        } catch (final SVGElementException e) {
            LOGGER.log(Level.SEVERE, "Failed patching colors. " + e.getMessage(), e);
        }
    }

    private static void loadColors(final SVGDiagram diagram, final Map<Object, Object> defaults,
            final Map<Object, Object> contextDefaults)
            throws SVGElementException {
        SVGRoot root = diagram.getRoot();
        SVGElement defs = diagram.getElement("colors");
        if (defs == null) {
            LOGGER.info(() -> {
                String uri = diagram.getXMLBase().toASCIIString();
                String name = uri.substring(Math.min(uri.lastIndexOf('/') + 1, uri.length() - 1));
                return "Themed icon '" + name
                        + "' has no color definitions. Consider loading it as a standard icon or add missing definitions";
            });
            return;
        }
        List<SVGElement> children = defs.getChildren(null);
        root.removeChild(defs);

        Defs themedDefs = new Defs();
        themedDefs.addAttribute("id", AnimationElement.AT_XML, "colors");
        root.loaderAddChild(null, themedDefs);

        for (SVGElement child : children) {
            if (child instanceof LinearGradient) {
                LinearGradient grad = (LinearGradient) child;
                String id = grad.getId();
                StyleAttribute colorFallbacks = getAttribute("fallback", grad);
                StyleAttribute opacityFallbacks = getAttribute("opacity-fallback", grad);
                String opacityKey = getOpacityKey(grad);

                float opacity = getOpacity(opacityKey, getFallbacks(opacityFallbacks), defaults, contextDefaults);
                float opacity1 = opacity;
                float opacity2 = opacity;
                if (opacity < 0) {
                    opacity = 1;
                    int childCount = grad.getNumChildren();
                    if (childCount > 0) {
                        SVGElement elem = grad.getChild(0);
                        if (elem instanceof Stop) {
                            opacity1 = getStopOpacity((Stop) elem);
                        }
                    }
                    if (childCount > 1) {
                        SVGElement elem = grad.getChild(1);
                        if (elem instanceof Stop) {
                            opacity2 = getStopOpacity((Stop) elem);
                        }
                    }

                    if (opacity1 < 0) opacity1 = opacity;
                    if (opacity2 < 0) opacity2 = opacity;
                }

                Color c = resolveColor(id, getFallbacks(colorFallbacks), FALLBACK_COLOR, defaults, contextDefaults);
                float finalOpacity1 = opacity1;
                float finalOpacity2 = opacity2;
                LOGGER.finest(() -> "Color: " + c + " opacity1: " + finalOpacity1 + " opacity2: " + finalOpacity2);
                ColorResult result =
                        createColor(c, id, opacityKey, new StyleAttribute[] {colorFallbacks, opacityFallbacks},
                                finalOpacity2, opacity2);
                themedDefs.loaderAddChild(null, result.gradient);
                result.finalizer.run();
                int resultRGB = ColorUtil.rgbNoAlpha(result.gradient.getStopColors()[0]);
                int expectedRGB = ColorUtil.rgbNoAlpha(result.color);
                if (expectedRGB != resultRGB) {
                    throw new IllegalStateException("Color not applied. Expected " + result.color + " but received "
                            + result.gradient.getStopColors()[0] + " (rgb " + expectedRGB + " != " + resultRGB + ")");
                }
                LOGGER.finest(() -> Arrays.toString(result.gradient.getStopColors()));
            }
        }
        LOGGER.fine("Patching done");
    }

    public static float getOpacity(final LinearGradient gradient, final Map<Object, Object> propertyMap,
            final Map<Object, Object> contextDefaults) {
        String opacityKey = getOpacityKey(gradient);
        return getOpacity(opacityKey, null, propertyMap, contextDefaults);
    }

    public static Color getColor(final LinearGradient gradient, final Map<Object, Object> propertyMap,
            final Map<Object, Object> contextDefaults) {
        String id = gradient.getId();
        StyleAttribute fallbacks = getAttribute("fallback", gradient);
        return resolveColor(id, getFallbacks(fallbacks), FALLBACK_COLOR, propertyMap, contextDefaults);
    }

    private static Color resolveColor(final String key, final String[] fallbacks, final Color fallbackColor,
            final Map<Object, Object> propertyMap, final Map<Object, Object> contextDefaults) {
        Color color = get(propertyMap, contextDefaults, key, fallbacks, Color.class);

        if (color == null) {
            color = fallbackColor;
            LOGGER.warning("Could not load color with id '" + key + "' fallbacks" + Arrays.toString(fallbacks)
                    + ". Using color '" + fallbackColor + "' instead.");
        }
        return color;
    }

    private static StyleAttribute getAttribute(final String key, final SVGElement child) {
        StyleAttribute attribute = new StyleAttribute();
        attribute.setName(key);
        try {
            child.getStyle(attribute);
        } catch (final SVGException e) {
            return null;
        }
        return attribute;
    }

    private static float getStopOpacity(final Stop stop) {
        StyleAttribute attribute = new StyleAttribute();
        attribute.setName("stop-opacity");
        try {
            stop.getStyle(attribute);
        } catch (final SVGException e) {
            return -1;
        }
        return !attribute.getStringValue().isEmpty() ? attribute.getFloatValue() : -1;
    }

    private static String[] getFallbacks(final StyleAttribute fallbacks) {
        if (fallbacks == null) return new String[0];
        return fallbacks.getStringList();
    }

    private static float getOpacity(final String key, final String[] fallbacks, final Map<Object, Object> propertyMap,
            final Map<Object, Object> contextDefaults) {
        if ((key == null || key.isEmpty()) && (fallbacks == null || fallbacks.length == 0)) return -1;
        // UIManager defaults to 0, if the value isn't an integer (or null).
        Number obj = get(propertyMap, contextDefaults, key, fallbacks, Number.class);
        if (obj instanceof Integer) {
            return obj.intValue() / 100.0f;
        } else if (obj instanceof Long) {
            return obj.intValue() / 100.0f;
        } else if (obj instanceof Float) {
            return obj.floatValue();
        } else if (obj instanceof Double) {
            return obj.floatValue();
        }
        LOGGER.warning(obj + " is an invalid opacity value. Key = '" + key + "'");
        // In this case we default to -1.
        return -1;
    }

    private static String getOpacityKey(final LinearGradient child) {
        StyleAttribute attribute = new StyleAttribute();
        attribute.setName("opacity");
        try {
            child.getStyle(attribute);
        } catch (final SVGException e) {
            e.printStackTrace();
            return null;
        }
        return attribute.getStringValue();
    }

    private static class ColorResult {
        private final LinearGradient gradient;
        private final Runnable finalizer;
        private final Color color;

        private ColorResult(LinearGradient gradient, Runnable finalizer, Color color) {
            this.gradient = gradient;
            this.finalizer = finalizer;
            this.color = color;
        }
    }

    private static ColorResult createColor(final Color c, final String name, final String opacityKey,
            final StyleAttribute[] extraAttributes, final float opacity1, final float opacity2)
            throws SVGElementException {
        LinearGradient grad = new LinearGradient();
        grad.addAttribute("id", AnimationElement.AT_XML, name);
        if (opacityKey != null && !opacityKey.isEmpty()) {
            grad.addAttribute("opacity", AnimationElement.AT_XML, opacityKey);
        }
        if (extraAttributes != null) {
            for (StyleAttribute attribute : extraAttributes) {
                if (attribute != null && !attribute.getStringValue().isEmpty()) {
                    grad.addAttribute(attribute.getName(), AnimationElement.AT_XML, attribute.getStringValue());
                }
            }
        }
        return new ColorResult(grad, () -> {
            String color = toHexString(c);
            BuildableStop stop1 = new BuildableStop(color);
            BuildableStop stop2 = new BuildableStop(color);
            try {
                stop1.addAttribute("stop-color", AnimationElement.AT_XML, color);
                stop1.addAttribute("offset", AnimationElement.AT_XML, "0");
                stop2.addAttribute("stop-color", AnimationElement.AT_XML, color);
                stop2.addAttribute("offset", AnimationElement.AT_XML, "1");
                if (opacity1 != 1) {
                    stop1.addAttribute("stop-opacity", AnimationElement.AT_XML, String.valueOf(opacity1));
                }
                if (opacity2 != 1) {
                    stop2.addAttribute("stop-opacity", AnimationElement.AT_XML, String.valueOf(opacity2));
                }
                grad.loaderAddChild(null, stop1);
                grad.loaderAddChild(null, stop2);
                stop1.build();
                stop2.build();
            } catch (final SVGException e) {
                throw new RuntimeException(e);
            }
        }, ColorUtil.toAlpha(c, opacity1));
    }

    private static class BuildableStop extends Stop {

        private final String color;

        private BuildableStop(final String color) {
            this.color = color;
        }

        @Override
        public boolean getStyle(StyleAttribute attrib, boolean recursive, boolean evalAnimation) throws SVGException {
            if ("stop-color".equals(attrib.getName())) {
                attrib.setStringValue(color);
                return true;
            }
            return super.getStyle(attrib, recursive, evalAnimation);
        }

        @Override
        protected void build() throws SVGException {
            super.build();
        }
    }

    public static Map<Object, Object> getProperties(final SVGIcon svgIcon) {
        SVGUniverse universe = svgIcon.getSvgUniverse();
        SVGDiagram diagram = universe.getDiagram(svgIcon.getSvgURI());
        SVGElement defs = diagram.getElement("colors");
        Map<Object, Object> values = new HashMap<>();
        if (defs != null) {
            List<SVGElement> children = defs.getChildren(null);
            for (SVGElement child : children) {
                if (child instanceof LinearGradient) {
                    LinearGradient grad = (LinearGradient) child;
                    String colorKey = grad.getId();
                    String opacityKey = getOpacityKey(grad);
                    SVGElement c = grad.getChild(0);
                    if (c instanceof Stop) {
                        Stop stop = (Stop) c;
                        StyleAttribute colorAttr = getAttribute("stop-color", stop);
                        Color color = colorAttr != null ? colorAttr.getColorValue() : null;
                        values.put(colorKey, color != null ? color : Color.BLACK);

                        if (opacityKey != null && !opacityKey.isEmpty()) {
                            StyleAttribute opacityAttr = getAttribute("stop-opacity", stop);
                            int opacity = opacityAttr != null ? (int) (100 * opacityAttr.getFloatValue()) : 100;
                            values.put(opacityKey, opacity);
                        }
                    }
                }
            }
        }
        return values;
    }

    public static <T> Pair<Object, T> getEntry(final Map<Object, Object> map, final Map<Object, Object> contextDefaults,
            final Object key, final Object[] fallbacks, final Class<T> type) {
        Object obj = null;
        String refPrefix = PropertyLoader.getReferencePrefix();
        Set<Object> seen = new HashSet<>();
        Object currentKey = key;
        int max = fallbacks != null ? fallbacks.length : 0;
        outer: for (int i = -1; i < max; i++) {
            currentKey = i < 0 ? key : fallbacks[i];
            int retryCount = 5;
            if (i >= 0 && currentKey instanceof String && ((String) currentKey).startsWith(INLINE_VALUE_PREFIX)) {
                ParseResult p = Parser.parse(
                        Parser.createParseResult(Objects.toString(key),
                                ((String) currentKey).substring(INLINE_VALUE_PREFIX.length())),
                        new ParserContext(map, contextDefaults, IconLoader.get()));
                obj = Types.safeCast(p.result, type);
            }
            do {
                if (obj == null) {
                    obj = map.get(currentKey);
                }
                if (contextDefaults != null && (obj == null || seen.contains(obj))) {
                    obj = contextDefaults.get(currentKey);
                }
                seen.add(obj);
                if (obj instanceof String && obj.toString().startsWith(refPrefix)) {
                    currentKey = obj.toString().substring(refPrefix.length());
                    obj = null;
                } else {
                    if (type.isInstance(obj)) {
                        // We found the value
                        break outer;
                    } else {
                        // Further search won't find anything.
                        // The value doesn't explicitly reference other keys.
                        continue outer;
                    }
                }
                retryCount--;
            } while (retryCount > 0);
        }
        return new Pair<>(currentKey, type.cast(obj));
    }

    public static <T> T get(final Map<Object, Object> map, final Map<Object, Object> contextDefaults, final Object key,
            final Object[] fallbacks, final Class<T> type) {
        return getEntry(map, contextDefaults, key, fallbacks, type).getSecond();
    }

    private static String toHexString(final Color color) {
        return "#" + ColorUtil.toHex(color);
    }
}
