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
 */
package com.github.weisj.darklaf.components.color;

import com.github.weisj.darklaf.components.tooltip.ToolTipContext;
import com.github.weisj.darklaf.ui.DarkPopupFactory;
import com.github.weisj.darklaf.ui.tooltip.DarkTooltipBorder;
import com.github.weisj.darklaf.ui.tooltip.DarkTooltipUI;
import com.github.weisj.darklaf.util.Alignment;
import com.github.weisj.darklaf.util.DarkUIUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class PopupColorChooser extends JToolTip {

    protected DarkTooltipBorder border;
    protected static SmallColorChooser chooser;
    protected static ToolTipContext context;

    protected SmallColorChooser getChooser(final Color initial, final Consumer<Color> callback) {
        if (chooser == null) chooser = new SmallColorChooser(initial, callback);
        SmallColorChooser smallColorChooser = chooser;
        if (chooser.getParent() != null) {
            // Already in use. Create new one.
            smallColorChooser = new SmallColorChooser(initial, callback);
        }
        smallColorChooser.reset(initial, callback);
        smallColorChooser.setOpaque(false);
        return smallColorChooser;
    }

    protected ToolTipContext getContext() {
        if (context == null) context = createToolTipContext();
        return context;
    }

    public PopupColorChooser(final JComponent parent, final Color initial,
                             final Consumer<Color> callback) {
        setComponent(parent);
        setLayout(new BorderLayout());
        add(getChooser(initial, callback), BorderLayout.CENTER);
    }

    @Override
    public void updateUI() {
        putClientProperty(DarkPopupFactory.KEY_FOCUSABLE_POPUP, true);
        putClientProperty(DarkTooltipUI.KEY_CONTEXT, getContext());
        super.updateUI();
    }

    public static void showColorChooser(final JComponent parent, final Color initial,
                                        final Consumer<Color> callback,
                                        final Runnable onClose) {
        JToolTip toolTip = new PopupColorChooser(parent, initial, callback);
        final Popup popup = PopupFactory.getSharedInstance().getPopup(parent, toolTip, 0, 0);
        popup.show();
        Window window = DarkUIUtil.getWindow(parent);
        AtomicReference<Runnable> close = new AtomicReference<>();
        ComponentListener windowListener = new ComponentAdapter() {
            @Override
            public void componentMoved(final ComponentEvent e) {
                close.get().run();
            }

            @Override
            public void componentResized(final ComponentEvent e) {
                close.get().run();
            }
        };
        AWTEventListener listener = event -> {
            if (!(DarkUIUtil.hasFocus(toolTip, (FocusEvent) event) || DarkUIUtil.hasFocus(toolTip))) {
                close.get().run();
            }
        };
        close.set(() -> {
            popup.hide();
            Toolkit.getDefaultToolkit().removeAWTEventListener(listener);
            if (window != null) window.removeComponentListener(windowListener);
            if (onClose != null) onClose.run();
        });
        SwingUtilities.invokeLater(() -> {
            window.addComponentListener(windowListener);
            Toolkit.getDefaultToolkit().addAWTEventListener(listener, AWTEvent.FOCUS_EVENT_MASK);
        });
    }

    protected ToolTipContext createToolTipContext() {
        return new ToolTipContext()
            .setAlignment(Alignment.CENTER)
            .setCenterAlignment(Alignment.SOUTH)
            .setUseBestFit(true)
            .setToolTipInsets(new Insets(2, 2, 2, 2))
            .setFallBackPositionProvider(c -> {
                Window window = DarkUIUtil.getWindow(c.getTarget());
                Dimension size = c.getToolTip().getPreferredSize();
                Rectangle bounds = window.getBounds();
                return new Point(bounds.x + (bounds.width - size.width) / 2,
                                 bounds.y + (bounds.height - size.height) / 2);
            });
    }

    @Override
    public String getTipText() {
        return "";
    }

    @Override
    public Dimension getPreferredSize() {
        if (isPreferredSizeSet()) {
            return super.getPreferredSize();
        }
        if (getLayout() != null) {
            return getLayout().preferredLayoutSize(this);
        }
        return super.getPreferredSize();
    }
}
