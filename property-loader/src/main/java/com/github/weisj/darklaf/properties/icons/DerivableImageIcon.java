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
package com.github.weisj.darklaf.properties.icons;

import java.awt.*;
import java.awt.image.ImageObserver;
import java.net.URL;
import java.util.Locale;
import java.util.function.Supplier;
import java.util.logging.Logger;

import javax.accessibility.*;
import javax.swing.*;

import com.github.weisj.darklaf.util.LazyValue;
import com.github.weisj.darklaf.util.LogUtil;

public class DerivableImageIcon
        implements DerivableIcon<DerivableImageIcon>, IconLoader.CacheableIcon, ImageSource, Accessible {

    private static final Logger LOGGER = LogUtil.getLogger(DerivableImageIcon.class);
    private static final int DEFAULT_SCALING_MODE = Image.SCALE_DEFAULT;
    private final int scalingMode;

    private int width;
    private int height;

    private final LazyValue<Image> original;
    private final LazyImageValue image;
    private String description;
    private AccessibleContext accessibleContext;
    private IconLoader.IconKey iconKey;

    /**
     * Create a new derivable image icon.
     *
     * @param imagePath the image path.
     */
    public DerivableImageIcon(final String imagePath) {
        this(() -> Toolkit.getDefaultToolkit().getImage(imagePath), -1, -1);
    }

    /**
     * Create a new derivable image icon.
     *
     * @param imagePath the image path.
     * @param width the icon width.
     * @param height the icon height.
     */
    public DerivableImageIcon(final String imagePath, final int width, final int height) {
        this(imagePath, width, height, DEFAULT_SCALING_MODE);
    }

    /**
     * Create a new derivable image icon.
     *
     * @param imagePath the image path.
     * @param width the icon width.
     * @param height the icon height.
     * @param scalingMode One of {@link Image#SCALE_DEFAULT}, {@link Image#SCALE_FAST},
     *        {@link Image#SCALE_REPLICATE}, {@link Image#SCALE_AREA_AVERAGING},
     *        {@link Image#SCALE_SMOOTH}
     */
    public DerivableImageIcon(final String imagePath, final int width, final int height, final int scalingMode) {
        this(() -> Toolkit.getDefaultToolkit().getImage(imagePath), width, height, scalingMode);
    }

    /**
     * Create a new derivable image icon.
     *
     * @param url the url to load the image from.
     */
    public DerivableImageIcon(final URL url) {
        this(url, -1, -1, DEFAULT_SCALING_MODE);
    }

    /**
     * Create a new derivable image icon.
     *
     * @param url the url to load the image from.
     * @param width the icon width.
     * @param height the icon height.
     */
    public DerivableImageIcon(final URL url, final int width, final int height) {
        this(url, width, height, DEFAULT_SCALING_MODE);
    }

    /**
     * Create a new derivable image icon.
     *
     * @param url the url to load the image from.
     * @param width the icon width.
     * @param height the icon height.
     * @param scalingMode One of {@link Image#SCALE_DEFAULT}, {@link Image#SCALE_FAST},
     *        {@link Image#SCALE_REPLICATE}, {@link Image#SCALE_AREA_AVERAGING},
     *        {@link Image#SCALE_SMOOTH}
     */
    public DerivableImageIcon(final URL url, final int width, final int height, final int scalingMode) {
        this(() -> Toolkit.getDefaultToolkit().getImage(url), width, height, scalingMode);
    }

    public DerivableImageIcon(final Supplier<Image> imageSupplier) {
        this(imageSupplier, -1, -1, DEFAULT_SCALING_MODE);
    }

    public DerivableImageIcon(final Supplier<Image> imageSupplier, final int width, final int height) {
        this(imageSupplier, width, height, DEFAULT_SCALING_MODE);
    }

    public DerivableImageIcon(final Supplier<Image> imageSupplier, final int width, final int height,
            final int scalingMode) {
        this.width = width;
        this.height = height;
        this.scalingMode = scalingMode;
        this.original = new LazyValue<>(imageSupplier);
        this.image = new LazyImageValue(this);
    }

    /**
     * Create a new derivable image icon.
     *
     * @param icon the source image icon.
     */
    public DerivableImageIcon(final ImageIcon icon) {
        this(icon, DEFAULT_SCALING_MODE);
    }

    /**
     * Create a new derivable image icon with specified scalingMode.
     *
     * @param icon the source icon.
     * @param scalingMode One of {@link Image#SCALE_DEFAULT}, {@link Image#SCALE_FAST},
     *        {@link Image#SCALE_REPLICATE}, {@link Image#SCALE_AREA_AVERAGING},
     *        {@link Image#SCALE_SMOOTH}
     */
    public DerivableImageIcon(final ImageIcon icon, final int scalingMode) {
        this(icon != null ? icon.getImage() : null, scalingMode);
    }

    /**
     * Create a new derivable image icon with specified scalingMode.
     *
     * @param icon the source icon.
     * @param width the icon width.
     * @param height the icon height.
     */
    public DerivableImageIcon(final ImageIcon icon, final int width, final int height) {
        this(icon, width, height, DEFAULT_SCALING_MODE);
    }

    /**
     * Create a new derivable image icon with specified scalingMode. If the source image doesnt match
     * the specified dimensions it will be scaled accordingly.
     *
     * @param icon the source icon.
     * @param width the icon width.
     * @param height the icon height.
     * @param scalingMode One of {@link Image#SCALE_DEFAULT}, {@link Image#SCALE_FAST},
     *        {@link Image#SCALE_REPLICATE}, {@link Image#SCALE_AREA_AVERAGING},
     *        {@link Image#SCALE_SMOOTH}
     */
    public DerivableImageIcon(final ImageIcon icon, final int width, final int height, final int scalingMode) {
        this(icon != null ? icon.getImage() : null, width, height, scalingMode);
        if (icon != null) setDescription(icon.getDescription());
    }

    /**
     * Create a new derivable image.
     *
     * @param img the source image.
     */
    public DerivableImageIcon(final Image img) {
        this(img, DEFAULT_SCALING_MODE);
    }

    /**
     * Create a new derivable image icon with specified scalingMode.
     *
     * @param img the source image.
     * @param scalingMode One of {@link Image#SCALE_DEFAULT}, {@link Image#SCALE_FAST},
     *        {@link Image#SCALE_REPLICATE}, {@link Image#SCALE_AREA_AVERAGING},
     *        {@link Image#SCALE_SMOOTH}
     */
    public DerivableImageIcon(final Image img, final int scalingMode) {
        this(img, img != null ? img.getWidth(null) : 0, img != null ? img.getHeight(null) : 0, scalingMode);
    }

    /**
     * Create a new derivable image icon with default scalingMode. If the source image doesnt match the
     * specified dimensions it will be scaled accordingly.
     *
     * @param img the source image.
     * @param width the icon width.
     * @param height the icon height.
     */
    public DerivableImageIcon(final Image img, final int width, final int height) {
        this(img, width, height, DEFAULT_SCALING_MODE);
    }

    /**
     * Create a new derivable image icon with specified scalingMode. If the source image doesnt match
     * the specified dimensions it will be scaled accordingly.
     *
     * @param img the source image.
     * @param width the icon width.
     * @param height the icon height.
     * @param scalingMode One of {@link Image#SCALE_DEFAULT}, {@link Image#SCALE_FAST},
     *        {@link Image#SCALE_REPLICATE}, {@link Image#SCALE_AREA_AVERAGING},
     *        {@link Image#SCALE_SMOOTH}
     */
    public DerivableImageIcon(final Image img, final int width, final int height, final int scalingMode) {
        this.width = width;
        this.height = height;
        this.scalingMode = scalingMode;
        this.original = new LazyValue<>(img);
        this.image = new LazyImageValue(this);
    }

    protected DerivableImageIcon(final DerivableImageIcon parent, final int width, final int height) {
        this.width = width;
        this.height = height;
        this.scalingMode = parent.scalingMode;
        this.original = parent.original;
        this.description = parent.description;
        this.image = parent.image.derive(this);
    }

    @Override
    public DerivableImageIcon derive(final int width, final int height) {
        if (sameIconRequested(width, height)) return this;
        return new DerivableImageIcon(this, width, height);
    }

    private boolean sameIconRequested(final int width, final int height) {
        if (this.width == width && this.height == height) return true;
        if (width < 0 && height < 0) {
            // Original image size is requested.
            if (this.width < 0 && this.height < 0) return true;
            // We have a size specified. Check if it matches the original.
            if (original.isInitialized()) {
                // If the original isn't loaded don't force it to.
                Image img = original.get();
                return this.width == img.getWidth(null)
                        && this.height == img.getHeight(null);
            }
        }
        return false;
    }

    @Override
    public void paintIcon(final Component c, final Graphics g, final int x, final int y) {
        Image img = image.get();
        if (img != null) {
            if (c == null) {
                // Component is null and can't be used as an image observer for painting.
                // To ensure the image is painted anyway we force load it.
                if (!ensureImageLoaded(img)) {
                    LOGGER.warning("Image could not be loaded in time for painting.");
                    return;
                }
            }
            g.drawImage(img, x, y, width, height, c);
        }
    }

    private boolean ensureImageLoaded(final Image img) {
        MediaTracker tracker = new MediaTracker(new Component() {});
        tracker.addImage(img, 0);
        try {
            tracker.waitForAll();
        } catch (final InterruptedException ex) {
            return false;
        }
        return true;
    }

    /**
     * Get the underlying {@link Image}.
     *
     * @return the image.
     */
    public Image getImage() {
        return getImage(null);
    }

    /**
     * Get the underlying {@link Image}.
     *
     * @param observer the {@link ImageObserver}.
     * @return the image.
     */
    public Image getImage(final ImageObserver observer) {
        return image.get(observer);
    }

    protected Image getOriginal() {
        Image img = original.get();
        calculateSize();
        return img;
    }

    private void calculateSize() {
        Image originalImage = original.get();
        if (originalImage != null && (width < 0 || height < 0)) {
            if (width < 0) width = originalImage.getWidth(null);
            if (height < 0) height = originalImage.getHeight(null);
            if (iconKey != null) {
                if (width >= 0) iconKey.w = width;
                if (height >= 0) iconKey.h = height;
            }
        }
    }

    @Override
    public int getIconWidth() {
        if (width < 0) calculateSize();
        return width;
    }

    @Override
    public int getIconHeight() {
        if (height < 0) calculateSize();
        return height;
    }

    @Override
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleDerivableImageIcon(this);
        }
        return accessibleContext;
    }

    /**
     * Gets the description of the image. This is meant to be a brief textual description of the object.
     * For example, it might be presented to a blind user to give an indication of the purpose of the
     * image. The description may be null.
     *
     * @return a brief textual description of the image
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the image. This is meant to be a brief textual description of the object.
     * For example, it might be presented to a blind user to give an indication of the purpose of the
     * image.
     *
     * @param description a brief textual description of the image
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public Image createImage(final Dimension size) {
        return getOriginal().getScaledInstance(size.width, size.height, scalingMode);
    }

    @Override
    public void setCacheKey(final IconLoader.IconKey key) {
        this.iconKey = key;
        if (iconKey != null && width >= 0 && height >= 0) {
            iconKey.w = width;
            iconKey.h = height;
        }
    }

    IconLoader.IconKey getCacheKey() {
        return iconKey;
    }

    protected static class AccessibleDerivableImageIcon extends AccessibleContext implements AccessibleIcon {

        private final DerivableImageIcon icon;

        public AccessibleDerivableImageIcon(final DerivableImageIcon icon) {
            this.icon = icon;
        }

        @Override
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.ICON;
        }

        @Override
        public AccessibleStateSet getAccessibleStateSet() {
            return null;
        }

        @Override
        public int getAccessibleIndexInParent() {
            return -1;
        }

        @Override
        public int getAccessibleChildrenCount() {
            return 0;
        }

        @Override
        public Accessible getAccessibleChild(final int i) {
            return null;
        }

        @Override
        public Locale getLocale() throws IllegalComponentStateException {
            return null;
        }

        @Override
        public String getAccessibleIconDescription() {
            return icon.getDescription();
        }

        @Override
        public void setAccessibleIconDescription(final String description) {
            icon.setDescription(description);
        }

        @Override
        public int getAccessibleIconWidth() {
            return icon.getIconWidth();
        }

        @Override
        public int getAccessibleIconHeight() {
            return icon.getIconHeight();
        }
    }

    protected static class LazyImageValue extends LazyValue<Image> {

        private final DerivableImageIcon icon;

        public LazyImageValue(final DerivableImageIcon icon) {
            super((Image) null);
            this.icon = icon;
        }

        public LazyImageValue derive(final DerivableImageIcon icon) {
            if (this.icon.width == icon.width && this.icon.height == icon.height) {
                // Make sure all icons with the same dimension share one image.
                return this;
            }
            return new LazyImageValue(icon);
        }

        public Image get(final ImageObserver observer) {
            if (value == null) set(load(observer));
            return value;
        }

        @Override
        public Image get() {
            return get(null);
        }

        protected Image load(final ImageObserver observer) {
            Image originalImage = icon.getOriginal();
            int width = icon.getIconWidth();
            int height = icon.getIconHeight();
            if (originalImage != null && width > 0 && height > 0) {
                if (originalImage.getWidth(observer) != width || originalImage.getHeight(observer) != height) {
                    return originalImage.getScaledInstance(width, height, icon.scalingMode);
                } else {
                    return originalImage;
                }
            }
            return null;
        }
    }
}
