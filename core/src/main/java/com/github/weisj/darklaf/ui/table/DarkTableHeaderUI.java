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
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.github.weisj.darklaf.util.DarkUIUtil;
import com.github.weisj.darklaf.util.GraphicsContext;

/**
 * @author Jannis Weis
 */
public class DarkTableHeaderUI extends DarkTableHeaderUIBridge {

    public static final String KEY_IS_HEADER_RENDERER = "JComponent.isHeaderRenderer";
    private static final int HEADER_HEIGHT = 26;
    protected Color borderColor;
    protected Color background;

    public static ComponentUI createUI(final JComponent c) {
        return new DarkTableHeaderUI();
    }

    @Override
    public void installUI(final JComponent c) {
        super.installUI(c);
        Dimension dim = header.getPreferredSize();
        int headerHeight = UIManager.getInt("TableHeader.height");
        if (headerHeight < 0) {
            headerHeight = HEADER_HEIGHT;
        }
        header.setPreferredSize(new Dimension(dim.width, Math.max(dim.height, headerHeight)));
        TableCellRenderer defaultRenderer = header.getDefaultRenderer();
        if (defaultRenderer instanceof DefaultTableCellRenderer) {
            DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) header.getDefaultRenderer();
            renderer.setHorizontalAlignment(SwingConstants.LEADING);
        }
    }

    @Override
    protected void installDefaults() {
        super.installDefaults();
        background = UIManager.getColor("TableHeader.background");
        borderColor = UIManager.getColor("TableHeader.borderColor");
        LookAndFeel.installBorder(header, "TableHeader.border");
    }

    @Override
    protected void uninstallDefaults() {
        super.uninstallDefaults();
        if (header.getBorder() instanceof UIResource) {
            LookAndFeel.uninstallBorder(header);
        }
    }

    @Override
    public void paint(final Graphics g2, final JComponent c) {
        final Graphics2D g = (Graphics2D) g2;
        final GraphicsContext config = new GraphicsContext(g);

        int h = c.getHeight();
        int w = c.getWidth();

        g.setColor(getHeaderBackground());
        g.fillRect(0, 0, w, h);

        if (header.getColumnModel().getColumnCount() <= 0) {
            return;
        }
        boolean ltr = header.getComponentOrientation().isLeftToRight();

        Rectangle clip = g.getClipBounds();
        Point left = clip.getLocation();
        Point right = new Point(clip.x + clip.width - 1, clip.y);
        TableColumnModel cm = header.getColumnModel();
        int cMin = header.columnAtPoint(ltr ? left : right);
        int cMax = header.columnAtPoint(ltr ? right : left);
        // This should never happen.
        if (cMin == -1) {
            cMin = 0;
        }
        // If the table does not have enough columns to fill the view we'll get -1.
        // Replace this with the index of the last column.
        if (cMax == -1) {
            cMax = cm.getColumnCount() - 1;
        }

        Color borderColor = c.getBorder() instanceof DarkTableScrollPaneBorder
                                                                               ? ((DarkTableScrollPaneBorder) c.getBorder()).getBorderColor()
                            : getBorderColor();
        g.setColor(borderColor);

        TableColumn draggedColumn = header.getDraggedColumn();
        int columnWidth;
        Rectangle cellRect = header.getHeaderRect(ltr ? cMin : cMax);
        TableColumn aColumn;
        if (ltr) {
            for (int column = cMin; column <= cMax; column++) {
                aColumn = cm.getColumn(column);
                columnWidth = aColumn.getWidth();
                cellRect.width = columnWidth;
                if (aColumn != draggedColumn) {
                    paintCell(g, cellRect, column);
                }
                cellRect.x += columnWidth;
                if (column != cMax) {
                    g.setColor(borderColor);
                    g.fillRect(cellRect.x - 1, 0, 1, h);
                }
            }
        } else {
            for (int column = cMax; column >= cMin; column--) {
                aColumn = cm.getColumn(column);
                columnWidth = aColumn.getWidth();
                cellRect.width = columnWidth;
                if (aColumn != draggedColumn) {
                    paintCell(g, cellRect, column);
                }
                cellRect.x += columnWidth;
                if (column != cMin) {
                    g.setColor(borderColor);
                    g.fillRect(cellRect.x - 1, 0, 1, h);
                }
            }
        }

        // Paint the dragged column if we are dragging.
        if (draggedColumn != null) {
            int draggedColumnIndex = viewIndexForColumn(draggedColumn);
            boolean scrollPaneRtl = isScrollPaneRtl();
            Rectangle draggedCellRect = header.getHeaderRect(draggedColumnIndex);
            int dist = DarkTableUI.adjustDistance(header.getDraggedDistance(), draggedCellRect, header.getTable());
            // Draw a gray well in place of the moving column.
            g.setColor(header.getParent().getBackground());
            g.fillRect(draggedCellRect.x, draggedCellRect.y,
                       draggedCellRect.width, draggedCellRect.height);
            g.setColor(borderColor);
            if (scrollBarVisible()) {
                if (ltr) {
                    if (!scrollPaneRtl) {
                        g.fillRect(draggedCellRect.x + draggedCellRect.width - 1, draggedCellRect.y,
                                   1, draggedCellRect.height);
                    } else {
                        if (draggedColumnIndex != cMax) {
                            g.fillRect(draggedCellRect.x + draggedCellRect.width - 1, draggedCellRect.y,
                                       1, draggedCellRect.height);
                        } else {
                            g.fillRect(draggedCellRect.x + draggedCellRect.width, draggedCellRect.y,
                                       1, draggedCellRect.height);
                        }
                        if (draggedColumnIndex == cMin) {
                            g.fillRect(draggedCellRect.x, draggedCellRect.y, 1, draggedCellRect.height);
                        }
                    }
                } else {
                    if (!scrollPaneRtl) {
                        g.fillRect(draggedCellRect.x + draggedCellRect.width - 1, draggedCellRect.y,
                                   1, draggedCellRect.height);
                    } else {
                        if (draggedColumnIndex != cMin) {
                            g.fillRect(draggedCellRect.x + draggedCellRect.width - 1, draggedCellRect.y,
                                       1, draggedCellRect.height);
                        }
                        if (draggedColumnIndex == cMax) {
                            g.fillRect(draggedCellRect.x, draggedCellRect.y, 1, draggedCellRect.height);
                        }
                    }
                }
            } else {
                if (ltr) {
                    if (draggedColumnIndex != cMax) {
                        g.fillRect(draggedCellRect.x + draggedCellRect.width - 1, draggedCellRect.y,
                                   1, draggedCellRect.height);
                    } else {
                        g.fillRect(draggedCellRect.x + draggedCellRect.width, draggedCellRect.y,
                                   1, draggedCellRect.height);
                    }
                } else {
                    if (draggedColumnIndex != cMin) {
                        g.fillRect(draggedCellRect.x + draggedCellRect.width - 1, draggedCellRect.y,
                                   1, draggedCellRect.height);
                    }
                }
            }

            draggedCellRect.x += dist;

            // Fill the background.
            g.setColor(header.getBackground());
            g.fillRect(draggedCellRect.x, draggedCellRect.y,
                       draggedCellRect.width, draggedCellRect.height);
            paintCell(g, draggedCellRect, draggedColumnIndex);

            g.setColor(borderColor);

            boolean onLeftEdge = ltr ? draggedColumnIndex == cMin : draggedColumnIndex == cMax;
            boolean onRightEdge = ltr ? draggedColumnIndex == cMax : draggedColumnIndex == cMin;
            //left
            if (dist != 0 || !onLeftEdge) {
                g.fillRect(draggedCellRect.x - 1, draggedCellRect.y, 1, draggedCellRect.height);
            }
            //right
            if (dist != 0 || !onRightEdge) {
                g.fillRect(draggedCellRect.x + draggedCellRect.width - 1, draggedCellRect.y,
                           1, draggedCellRect.height);
            }
        }

        // Remove all components in the rendererPane.
        rendererPane.removeAll();
        config.restore();
    }

    @Override
    protected void paintCell(final Graphics g, final Rectangle cellRect, final int columnIndex) {
        Component component = getHeaderRenderer(columnIndex);
        if (component instanceof JComponent) ((JComponent) component).putClientProperty(KEY_IS_HEADER_RENDERER, true);
        rendererPane.paintComponent(g, component, header, cellRect.x, cellRect.y,
                                    cellRect.width, cellRect.height, true);
    }

    protected Color getHeaderBackground() {
        return background;
    }

    protected Color getBorderColor() {
        return borderColor;
    }

    protected boolean isScrollPaneRtl() {
        if (!isInScrollPane()) return false;
        Container comp = DarkUIUtil.getUnwrappedParent(header).getParent();
        return !comp.getComponentOrientation().isLeftToRight();
    }

    protected boolean scrollBarVisible() {
        Component comp = DarkUIUtil.getUnwrappedParent(header);
        if (comp != null) {
            comp = comp.getParent();
        }
        return comp instanceof JScrollPane && ((JScrollPane) comp).getVerticalScrollBar().isVisible();
    }

    protected boolean isInScrollPane() {
        Component comp = DarkUIUtil.getUnwrappedParent(header);
        if (comp != null) {
            comp = comp.getParent();
        }
        return comp instanceof JScrollPane;
    }
}
