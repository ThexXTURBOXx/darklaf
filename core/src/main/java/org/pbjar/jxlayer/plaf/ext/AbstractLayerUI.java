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
 */
package org.pbjar.jxlayer.plaf.ext;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;
import javax.swing.plaf.LayerUI;

/**
 * The {@code AbstractLayerUI} provided default implementation for most of the abstract methods in
 * the {@link LayerUI} class. It takes care of the management of {@code LayerItemListener}s and
 * defines the hook method to configure the {@code Graphics2D} instance specified in the
 * {@link #paint(Graphics,JComponent)} method. It also provides convenient methods named
 * {@code process<eventType>Event} to process the given class of event.
 * <p/>
 * If state of the {@code AbstractLayerUI} is changed, call {@link #setDirty(boolean)} with
 * {@code true} as the parameter, it will repaint all {@code JLayer}s connected with this
 * {@code AbstractLayerUI}
 *
 * @see JLayer#setUI(LayerUI)
 */
public abstract class AbstractLayerUI<V extends JComponent>
        extends LayerUI<V> {
    private static final Map<RenderingHints.Key, Object> emptyRenderingHintMap =
            Collections.unmodifiableMap(new HashMap<>(0));

    private boolean dirty;
    private LayoutManager layoutManager;
    private JComponent installedComponent;
    private final PropertyChangeListener dirtyListener = e -> {
        if (installedComponent != null && (!"dirty".equals(e.getPropertyName())
                || e.getNewValue() == Boolean.TRUE)) {
            installedComponent.repaint();
        }
    };

    @Override
    public void installUI(final JComponent c) {
        super.installUI(c);
        installedComponent = c;
        addPropertyChangeListener(dirtyListener);
        ((JLayer<?>) c).setLayerEventMask(getLayerEventMask());
    }

    @Override
    public void uninstallUI(final JComponent c) {
        super.uninstallUI(c);
        ((JLayer<?>) c).setLayerEventMask(0);
        removePropertyChangeListener(dirtyListener);
        installedComponent = null;
    }

    /**
     * Returns the "dirty bit". If {@code true}, then the {@code AbstractLayerUI} is considered dirty
     * and in need of being repainted.
     *
     * @return {@code true} if the {@code AbstractLayerUI} state has changed and the {@link JLayer}s it
     *         is set to need to be repainted.
     */
    protected boolean isDirty() {
        return dirty;
    }

    /**
     * Sets the "dirty bit". If {@code isDirty} is {@code true}, then the {@code AbstractLayerUI} is
     * considered dirty and it triggers the repainting of the {@link JLayer}s this
     * {@code AbstractLayerUI} it is set to.
     *
     * @param isDirty whether this {@code AbstractLayerUI} is dirty or not.
     */
    protected void setDirty(boolean isDirty) {
        boolean oldDirty = this.dirty;
        this.dirty = isDirty;
        firePropertyChange("dirty", oldDirty, isDirty);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * <b>Note:</b> It is rarely necessary to override this method, for custom painting override
     * {@link #paintLayer(Graphics2D,JLayer)} instead
     * <p/>
     * This method configures the passed {@code Graphics} with help of the
     * {@link #configureGraphics(Graphics2D,JLayer)} method, then calls
     * {@code paintLayer(Graphics2D,JLayer)} and resets the "dirty bit" at the end.
     *
     * @see #configureGraphics(Graphics2D,JLayer)
     * @see #paintLayer(Graphics2D,JLayer)
     * @see #setDirty(boolean)
     */
    @Override
    @SuppressWarnings("unchecked")
    public void paint(Graphics g, JComponent c) {
        if (g instanceof Graphics2D) {
            Graphics2D g2 = (Graphics2D) g.create();
            JLayer<V> l = (JLayer<V>) c;
            configureGraphics(g2, l);
            paintLayer(g2, l);
            g2.dispose();
            setDirty(false);
        }
    }

    /**
     * Subclasses should implement this method and perform custom painting operations here.
     * <p/>
     * The default implementation paints the passed {@code JLayer} as is.
     *
     * @param g2 the {@code Graphics2D} context in which to paint
     * @param l the {@code JLayer} being painted
     */
    protected void paintLayer(Graphics2D g2, JLayer<? extends V> l) {
        l.paint(g2);
    }

    /**
     * This method is called by the {@link #paint} method prior to any drawing operations to configure
     * the {@code Graphics2D} object. The default implementation sets the {@link Composite}, the clip,
     * {@link AffineTransform} and rendering hints obtained from the corresponding hook methods.
     *
     * @param g2 the {@code Graphics2D} object to configure
     * @param l the {@code JLayer} being painted
     *
     * @see #getComposite(JLayer)
     * @see #getClip(JLayer)
     * @see #getTransform(JLayer)
     * @see #getRenderingHints(JLayer)
     */
    protected void configureGraphics(Graphics2D g2, JLayer<? extends V> l) {
        Composite composite = getComposite(l);
        if (composite != null) {
            g2.setComposite(composite);
        }
        Shape clip = getClip(l);
        if (clip != null) {
            g2.clip(clip);
        }
        AffineTransform transform = getTransform(l);
        if (transform != null) {
            g2.transform(transform);
        }
        Map<RenderingHints.Key, Object> hints = getRenderingHints(l);
        if (hints != null) {
            for (RenderingHints.Key key : hints.keySet()) {
                Object value = hints.get(key);
                if (value != null) {
                    g2.setRenderingHint(key, hints.get(key));
                }
            }
        }
    }

    /**
     * Returns the {@link Composite} to be used during painting of this {@code JLayer}, the default
     * implementation returns {@code null}.
     *
     * @param l the {@code JLayer} being painted
     *
     * @return the {@link Composite} to be used during painting for the {@code JLayer}
     */
    protected Composite getComposite(JLayer<? extends V> l) {
        return null;
    }

    /**
     * Returns the {@link AffineTransform} to be used during painting of this {@code JLayer}, the
     * default implementation returns {@code null}.
     *
     * @param l the {@code JLayer} being painted
     *
     * @return the {@link AffineTransform} to be used during painting of the {@code JLayer}
     */
    protected AffineTransform getTransform(JLayer<? extends V> l) {
        return null;
    }

    /**
     * Returns the {@link Shape} to be used as the clip during painting of this {@code JLayer}, the
     * default implementation returns {@code null}.
     *
     * @param l the {@code JLayer} being painted
     *
     * @return the {@link Shape} to be used as the clip during painting of the {@code JLayer}
     */
    protected Shape getClip(JLayer<? extends V> l) {
        return null;
    }

    /**
     * Returns the map of rendering hints to be used during painting of this {@code JLayer}, the default
     * implementation returns the empty unmodifiable map.
     *
     * @param l the {@code JLayer} being painted
     *
     * @return the map of rendering hints to be used during painting of the {@code JLayer}
     */
    protected Map<RenderingHints.Key, Object> getRenderingHints(JLayer<? extends V> l) {
        return emptyRenderingHintMap;
    }

    public void setLayoutManager(final LayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }

    protected LayoutManager getLayout() {
        return layoutManager;
    }

    @Override
    public void doLayout(JLayer<? extends V> l) {
        LayoutManager layoutManager = getLayout();
        if (layoutManager != null) {
            layoutManager.layoutContainer(l);
        } else {
            super.doLayout(l);
        }
    }

    @Override
    public Dimension getPreferredSize(final JComponent c) {
        LayoutManager layoutManager = getLayout();
        if (layoutManager != null) {
            return layoutManager.preferredLayoutSize(c);
        } else {
            return super.getPreferredSize(c);
        }
    }

    @Override
    public Dimension getMinimumSize(final JComponent c) {
        LayoutManager layoutManager = getLayout();
        if (layoutManager != null) {
            return layoutManager.minimumLayoutSize(c);
        } else {
            return super.getMinimumSize(c);
        }
    }

    /**
     * By default only mouse, mouse motion, mouse wheel, keyboard and focus events are supported, if you
     * need to catch any other type of events, override this method to return the different mask
     *
     * @see JLayer#setLayerEventMask(long)
     */
    public long getLayerEventMask() {
        return AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK
                | AWTEvent.MOUSE_WHEEL_EVENT_MASK
                | AWTEvent.KEY_EVENT_MASK | AWTEvent.FOCUS_EVENT_MASK;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * This method calls the appropriate {@code process<eventType>Event} method for the given class of
     * event.
     */
    @Override
    public void eventDispatched(AWTEvent e, JLayer<? extends V> l) {
        if (e instanceof FocusEvent) {
            processFocusEvent((FocusEvent) e, l);
        } else if (e instanceof MouseEvent) {
            switch (e.getID()) {
                case MouseEvent.MOUSE_PRESSED:
                case MouseEvent.MOUSE_RELEASED:
                case MouseEvent.MOUSE_CLICKED:
                case MouseEvent.MOUSE_ENTERED:
                case MouseEvent.MOUSE_EXITED:
                    processMouseEvent((MouseEvent) e, l);
                    break;
                case MouseEvent.MOUSE_MOVED:
                case MouseEvent.MOUSE_DRAGGED:
                    processMouseMotionEvent((MouseEvent) e, l);
                    break;
                case MouseEvent.MOUSE_WHEEL:
                    processMouseWheelEvent((MouseWheelEvent) e, l);
                    break;
            }
        } else if (e instanceof KeyEvent) {
            processKeyEvent((KeyEvent) e, l);
        }
    }

    /**
     * Processes {@code FocusEvent} occurring on the {@link JLayer} or any of its subcomponents.
     *
     * @param e the {@code FocusEvent} to be processed
     * @param l the layer this LayerUI is set to
     */
    @Override
    protected void processFocusEvent(FocusEvent e, JLayer<? extends V> l) {}

    /**
     * Processes {@code MouseEvent} occurring on the {@link JLayer} or any of its subcomponents.
     *
     * @param e the {@code MouseEvent} to be processed
     * @param l the layer this LayerUI is set to
     */
    @Override
    protected void processMouseEvent(MouseEvent e, JLayer<? extends V> l) {}

    /**
     * Processes mouse motion events occurring on the {@link JLayer} or any of its subcomponents.
     *
     * @param e the {@code MouseEvent} to be processed
     * @param l the layer this LayerUI is set to
     */
    @Override
    protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends V> l) {}

    /**
     * Processes {@code MouseWheelEvent} occurring on the {@link JLayer} or any of its subcomponents.
     *
     * @param e the {@code MouseWheelEvent} to be processed
     * @param l the layer this LayerUI is set to
     */
    @Override
    protected void processMouseWheelEvent(MouseWheelEvent e, JLayer<? extends V> l) {}

    /**
     * Processes {@code KeyEvent} occurring on the {@link JLayer} or any of its subcomponents.
     *
     * @param e the {@code KeyEvent} to be processed
     * @param l the layer this LayerUI is set to
     */
    @Override
    protected void processKeyEvent(KeyEvent e, JLayer<? extends V> l) {}
}
