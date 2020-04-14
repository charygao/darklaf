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
package com.github.weisj.darklaf.ui.table;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.UIResource;

import com.github.weisj.darklaf.components.border.MutableLineBorder;
import com.github.weisj.darklaf.util.DarkUIUtil;

/**
 * @author Jannis Weis
 */
public class DarkTableHeaderBorder extends MutableLineBorder implements UIResource {

    public DarkTableHeaderBorder() {
        super(0, 0, 1, 0, null);
        setColor(UIManager.getColor("TableHeader.borderColor"));
    }

    @Override
    public void paintBorder(final Component c, final Graphics g, final int x, final int y,
                            final int width, final int height) {
        adjustTop(c);
        super.paintBorder(c, g, x, y, width, height);
    }

    @Override
    public Insets getBorderInsets(final Component c, final Insets insets) {
        adjustTop(c);
        return super.getBorderInsets(c, insets);
    }

    protected void adjustTop(final Component c) {
        Component parent = DarkUIUtil.getUnwrappedParent(c.getParent());
        top = 0;
        if (parent instanceof JComponent) {
            if (hasBorderAbove((JComponent) parent, c)) return;
            Border border = ((JComponent) parent).getBorder();
            if (border instanceof EmptyBorder || border == null) {
                top = 1;
            }
        }
    }

    protected boolean hasBorderAbove(final JComponent c, final Component child) {
        JComponent comp = c;
        Component prev = child;
        while (comp instanceof JScrollPane
               || (comp != null
                   && comp.getLayout() instanceof BorderLayout
                   && ((BorderLayout) comp.getLayout()).getConstraints(prev) == BorderLayout.CENTER)
                  && ((BorderLayout) comp.getLayout()).getLayoutComponent(BorderLayout.NORTH) == null) {
            if (comp instanceof JTabbedPane) return true;
            prev = comp;
            comp = (JComponent) comp.getParent();
        }
        return comp instanceof JTabbedPane;
    }
}
