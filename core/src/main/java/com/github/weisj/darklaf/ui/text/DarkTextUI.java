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
 *
 */
package com.github.weisj.darklaf.ui.text;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.ActionMapUIResource;
import javax.swing.plaf.ComponentInputMapUIResource;
import javax.swing.plaf.InputMapUIResource;
import javax.swing.plaf.basic.BasicTextUI;
import javax.swing.text.*;

import sun.awt.SunToolkit;
import sun.swing.DefaultLookup;

import com.github.weisj.darklaf.components.border.MarginBorderWrapper;
import com.github.weisj.darklaf.graphics.GraphicsContext;
import com.github.weisj.darklaf.graphics.GraphicsUtil;
import com.github.weisj.darklaf.graphics.PaintUtil;
import com.github.weisj.darklaf.ui.list.DarkListUI;
import com.github.weisj.darklaf.ui.table.DarkTableUI;
import com.github.weisj.darklaf.ui.table.TextTableCellEditorBorder;
import com.github.weisj.darklaf.ui.text.action.DarkKeyTypedAction;
import com.github.weisj.darklaf.ui.text.action.ToggleInsertAction;
import com.github.weisj.darklaf.ui.tree.DarkTreeUI;
import com.github.weisj.darklaf.util.DarkUIUtil;
import com.github.weisj.darklaf.util.PropertyUtil;

/**
 * @author Jannis Weis
 */
public abstract class DarkTextUI extends BasicTextUI implements PropertyChangeListener {

    protected static final String KEY_PREFIX = "JTextComponent.";
    public static final String KEY_ROUNDED_SELECTION = KEY_PREFIX + "roundedSelection";
    public static final String KEY_EXTEND_LINE_SELECTION = KEY_PREFIX + "extendSelection";
    public static final String KEY_HAS_ERROR = KEY_PREFIX + "hasError";
    public static final String KEY_IS_TREE_EDITOR = DarkTreeUI.KEY_IS_TREE_EDITOR;
    public static final String KEY_IS_TABLE_EDITOR = DarkTableUI.KEY_IS_TABLE_EDITOR;
    public static final String KEY_IS_LIST_EDITOR = DarkListUI.KEY_IS_LIST_EDITOR;
    public static final String KEY_DEFAULT_TEXT = KEY_PREFIX + "defaultText";

    protected static final String TOGGLE_INSERT = "toggle_insert";

    protected JTextComponent editor;
    private FocusListener focusListener = new FocusListener() {
        @Override
        public void focusGained(final FocusEvent e) {
            Caret caret = editor.getCaret();
            if (caret instanceof DarkCaret) {
                ((DarkCaret) caret).setPaintSelectionHighlight(true);
            }
            editor.repaint();
        }

        @Override
        public void focusLost(final FocusEvent e) {
            Caret caret = editor.getCaret();
            if (caret instanceof DarkCaret) {
                ((DarkCaret) caret).setPaintSelectionHighlight(false);
            }
            editor.repaint();
        }
    };
    protected DefaultTextRenderer defaultTextRenderer;
    protected DarkCaret darkCaret;

    @Override
    protected Caret createCaret() {
        return getDarkCaret();
    }

    protected DarkCaret getDarkCaret() {
        if (darkCaret == null) darkCaret = createDarkCaret();
        return darkCaret;
    }

    protected DarkCaret createDarkCaret() {
        DarkCaret c = new DarkCaret(getDefaultCaretStyle(), getDefaultInsertCaretStyle());
        c.setLineExtendingEnabled(PropertyUtil.getBooleanProperty(editor, KEY_EXTEND_LINE_SELECTION));
        c.setRoundedSelectionEdges(PropertyUtil.getBooleanProperty(editor, KEY_ROUNDED_SELECTION));
        return c;
    }

    protected abstract DarkCaret.CaretStyle getDefaultCaretStyle();

    protected DarkCaret.CaretStyle getDefaultInsertCaretStyle() {
        return DarkCaret.CaretStyle.BLOCK_STYLE;
    }

    protected Color disabledColor;
    protected Color inactiveColor;
    protected boolean uninstalling;

    @Override
    protected void installDefaults() {
        super.installDefaults();
        // OpenJDK BorderlessTextField has a bug with its setBorder implementation
        // so we reset the border
        // See https://mail.openjdk.java.net/pipermail/swing-dev/2020-March/010226.html
        if (editor != null && "javax.swing.plaf.basic.BasicComboBoxEditor$BorderlessTextField".equals(editor.getClass()
                                                                                                            .getName())) {
            editor.setBorder(null);
        }
        if (editor != null) {
            PropertyUtil.installBooleanProperty(editor, KEY_ROUNDED_SELECTION, "TextComponent.roundedSelection");
            PropertyUtil.installBooleanProperty(editor, KEY_EXTEND_LINE_SELECTION,
                                                getPropertyPrefix() + ".extendSelection");
        }
        disabledColor = UIManager.getColor(getPropertyPrefix() + ".disabledBackground");
        inactiveColor = UIManager.getColor(getPropertyPrefix() + ".inactiveBackground");

        installBorder();
    }

    protected void installBorder() {
        if (uninstalling) return;
        MarginBorderWrapper.installBorder(editor);
    }

    protected void uninstallBorder() {
        MarginBorderWrapper.uninstallBorder(editor);
    }

    protected DefaultTextRenderer createDefaultTextRenderer() {
        return new LabelDefaultTextRenderer();
    }

    @Override
    protected void uninstallDefaults() {
        super.uninstallDefaults();
        uninstalling = true;
        uninstallBorder();
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        String key = evt.getPropertyName();
        if (KEY_ROUNDED_SELECTION.equals(key)) {
            boolean rounded = PropertyUtil.getBooleanProperty(editor, DarkTextUI.KEY_ROUNDED_SELECTION);
            getDarkCaret().setRoundedSelectionEdges(rounded);
            editor.repaint();
        } else if (KEY_HAS_ERROR.equals(key)) {
            editor.repaint();
        } else if (KEY_EXTEND_LINE_SELECTION.equals(key)) {
            boolean extendLines = PropertyUtil.getBooleanProperty(editor, DarkTextUI.KEY_EXTEND_LINE_SELECTION);
            getDarkCaret().setLineExtendingEnabled(extendLines);
            editor.repaint();
        } else if ("border".equals(key)) {
            installBorder();
        }
    }

    @Override
    protected void installListeners() {
        super.installListeners();
        editor.addFocusListener(focusListener);
    }

    @Override
    protected void uninstallListeners() {
        super.uninstallListeners();
        editor.removeFocusListener(focusListener);
        focusListener = null;
    }

    protected Color getBackground(final JTextComponent c) {
        if (!c.isEnabled()) {
            return disabledColor;
        } else if (!c.isEditable()) {
            return inactiveColor;
        } else {
            return c.getBackground();
        }
    }

    @Override
    public Dimension getPreferredSize(final JComponent c) {
        Dimension dim = super.getPreferredSize(c);
        if (isEmpty()) {
            String defaultText = getDefaultText();
            if (!defaultText.isEmpty()) {
                Component renderer = getDefaultTextRenderer().getRendererComponent(editor, defaultText);
                Dimension prefSize = renderer.getPreferredSize();
                Insets i = c.getInsets();
                prefSize.width += i.left + i.right;
                prefSize.height += i.top + i.bottom;
                dim.width = Math.max(dim.width, prefSize.width);
                dim.height = Math.max(dim.height, prefSize.height);
            }
        }
        dim.width += getCaretWidth();
        return dim;
    }

    protected int getCaretWidth() {
        Caret caret = editor.getCaret();
        if (caret instanceof DefaultCaret) {
            return (int) ((DefaultCaret) caret).getWidth();
        }
        return 1;
    }

    @Override
    protected void paintBackground(final Graphics g) {
        final Container parent = getRelevantParent(editor);
        if (parent != null) {
            g.setColor(parent.getBackground());
            if (parent instanceof JTextComponent) {
                if (!parent.isEnabled()) {
                    g.setColor(disabledColor);
                } else if (!((JTextComponent) parent).isEditable()) {
                    g.setColor(inactiveColor);
                }
            }
            g.fillRect(0, 0, editor.getWidth(), editor.getHeight());
        }

        if (!editor.isEnabled()) {
            return;
        }

        if (editor.isOpaque() && isInCell(editor)) {
            g.fillRect(0, 0, editor.getWidth(), editor.getHeight());
        }

        Border border = getBorder(editor);

        g.setColor(getBackground(editor));
        if (border instanceof DarkTextBorder) {
            paintBorderBackground((Graphics2D) g, editor);
        } else {
            Insets ins = null;
            if (border instanceof TextTableCellEditorBorder) {
                ins = new Insets(0, 0, 0, 0);
            } else if (border != null) {
                ins = border.getBorderInsets(editor);
            }
            if (ins == null) ins = new Insets(0, 0, 0, 0);
            g.fillRect(ins.left, ins.top, editor.getWidth() - ins.left - ins.right,
                       editor.getHeight() - ins.top - ins.bottom);
        }
    }

    protected boolean isInCell(final JComponent c) {
        if (getBorder(c) instanceof DarkTextBorder) return false;
        return DarkUIUtil.getParentOfType(JSpinner.class, c, 2) != null
               || DarkUIUtil.isInCell(c)
               || PropertyUtil.getBooleanProperty(c, KEY_IS_TREE_EDITOR)
               || PropertyUtil.getBooleanProperty(c, KEY_IS_TABLE_EDITOR)
               || PropertyUtil.getBooleanProperty(c, KEY_IS_LIST_EDITOR);
    }

    protected Container getRelevantParent(final Component comp) {
        Container parent = comp.getParent();
        if (parent instanceof JSpinner.DefaultEditor) {
            JSpinner spinner = DarkUIUtil.getParentOfType(JSpinner.class, comp, 2);
            if (spinner != null) parent = spinner.getParent();
        } else if (parent instanceof JComboBox) {
            parent = parent.getParent();
        }
        return DarkUIUtil.getParentMatching(parent, c -> c.isOpaque() || c instanceof JTextComponent);
    }

    @Override
    protected void paintSafely(final Graphics g) {
        GraphicsContext config = GraphicsUtil.setupAntialiasing(g);
        super.paintSafely(g);
        config.restoreClip();
        paintDefaultText(g);
        config.restore();
    }

    protected void paintDefaultText(final Graphics g) {
        if (isEmpty()) {
            String defaultText = getDefaultText();
            if (!defaultText.isEmpty()) {
                Rectangle rect = getVisibleEditorRect();
                g.translate(rect.x, rect.y);
                Component renderer = getDefaultTextRenderer().getRendererComponent(editor, defaultText);
                renderer.setBounds(rect);
                renderer.paint(g);
            }
        }
    }

    protected String getDefaultText() {
        return PropertyUtil.getString(editor, KEY_DEFAULT_TEXT, "");
    }

    protected boolean isEmpty() {
        Document doc = editor.getDocument();
        return doc == null || doc.getLength() == 0;
    }

    protected DefaultTextRenderer getDefaultTextRenderer() {
        if (defaultTextRenderer == null) defaultTextRenderer = createDefaultTextRenderer();
        return defaultTextRenderer;
    }

    protected void paintBorderBackground(final Graphics2D g, final JTextComponent c) {
        Rectangle r = getDrawingRect(c);
        int arc = getArcSize(c);
        PaintUtil.fillRoundRect(g, r.x, r.y, r.width, r.height, arc);
    }

    protected Border getBorder(final JComponent c) {
        return MarginBorderWrapper.getBorder(c);
    }

    public Rectangle getDrawingRect(final JTextComponent c) {
        Border border = getBorder(c);
        Rectangle r = new Rectangle(0, 0, c.getWidth(), c.getHeight());
        if (border instanceof DarkTextBorder) {
            int bw = ((DarkTextBorder) border).getBorderSize();
            r.x += bw;
            r.y += bw;
            r.width -= 2 * bw;
            r.height -= 2 * bw;
        } else {
            DarkUIUtil.applyInsets(r, border.getBorderInsets(c));
        }
        return r;
    }

    protected int getArcSize(final JComponent c) {
        Border border = getBorder(c);
        if (border instanceof DarkTextBorder) {
            return ((DarkTextBorder) border).getArcSize(c);
        }
        return 0;
    }

    protected void installKeyboardActions() {
        // backward compatibility support... keymaps for the UI
        // are now installed in the more friendly input map.
        editor.setKeymap(createKeymap());

        InputMap km = getInputMap();
        if (km != null) {
            SwingUtilities.replaceUIInputMap(editor, JComponent.WHEN_FOCUSED,
                                             km);
        }

        ActionMap map = getActionMap();
        if (map != null) {
            SwingUtilities.replaceUIActionMap(editor, map);
        }

        updateFocusAcceleratorBinding(false);
    }

    @Override
    public void installUI(final JComponent c) {
        if (c instanceof JTextComponent) {
            editor = (JTextComponent) c;
        }
        super.installUI(c);
    }

    @Override
    protected Highlighter createHighlighter() {
        return new DarkHighlighter();
    }

    /*
     * Implementation of BasicTextUI.
     */
    /**
     * Get the InputMap to use for the UI.
     *
     * @return the input map
     */
    protected InputMap getInputMap() {
        InputMap map = new InputMapUIResource();

        InputMap shared = (InputMap) DefaultLookup.get(editor, this, getPropertyPrefix() + ".focusInputMap");
        if (shared != null) {
            map.setParent(shared);
        }
        map.put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0), TOGGLE_INSERT);
        return map;
    }

    protected ActionMap getActionMap() {
        String mapName = getPropertyPrefix() + ".actionMap";
        ActionMap map = (ActionMap) UIManager.get(mapName);

        if (map == null) {
            map = createActionMap();
            if (map != null) {
                UIManager.getLookAndFeelDefaults().put(mapName, map);
            }
        }
        ActionMap componentMap = new ActionMapUIResource();
        componentMap.put("requestFocus", new FocusAction());
        /*
         * fix for bug 4515750
         * JTextField & non-editable JTextArea bind return key - default btn not accessible
         *
         * Wrap the return action so that it is only enabled when the
         * component is editable. This allows the default button to be
         * processed when the text component has focus and isn't editable.
         *
         */
        if (getEditorKit(editor) instanceof DefaultEditorKit) {
            if (map != null) {
                Object obj = map.get(DefaultEditorKit.insertBreakAction);
                if (obj instanceof DefaultEditorKit.InsertBreakAction) {
                    Action action = new TextActionWrapper((TextAction) obj);
                    componentMap.put(action.getValue(Action.NAME), action);
                }
                map.put(TOGGLE_INSERT, new ToggleInsertAction());
            }
        }
        if (map != null) {
            componentMap.setParent(map);
        }
        return componentMap;
    }

    @Override
    protected Keymap createKeymap() {
        Keymap km = super.createKeymap();
        km.setDefaultAction(new DarkKeyTypedAction());
        return km;
    }

    /**
     * Invoked when the focus accelerator changes, this will update the key bindings as necessary.
     *
     * @param changed the changed
     */
    @SuppressWarnings("MagicConstant")
    protected void updateFocusAcceleratorBinding(final boolean changed) {
        char accelerator = editor.getFocusAccelerator();

        if (changed || accelerator != '\0') {
            InputMap km = SwingUtilities.getUIInputMap(editor, JComponent.WHEN_IN_FOCUSED_WINDOW);

            if (km == null && accelerator != '\0') {
                km = new ComponentInputMapUIResource(editor);
                SwingUtilities.replaceUIInputMap(editor, JComponent.WHEN_IN_FOCUSED_WINDOW, km);
                ActionMap am = getActionMap();
                SwingUtilities.replaceUIActionMap(editor, am);
            }
            if (km != null) {
                km.clear();
                if (accelerator != '\0') {
                    km.put(KeyStroke.getKeyStroke(accelerator, getFocusAcceleratorKeyMask()), "requestFocus");
                    km.put(KeyStroke.getKeyStroke(accelerator,
                                                  DarkUIUtil.setAltGraphMask(getFocusAcceleratorKeyMask())),
                           "requestFocus");
                }
            }
        }
    }

    /**
     * Create a default action map. This is basically the set of actions found exported by the component.
     *
     * @return the action map
     */
    public ActionMap createActionMap() {
        ActionMap map = new ActionMapUIResource();
        Action[] actions = editor.getActions();
        for (Action a : actions) {
            map.put(a.getValue(Action.NAME), a);
        }
        map.put(TransferHandler.getCutAction().getValue(Action.NAME), TransferHandler.getCutAction());
        map.put(TransferHandler.getCopyAction().getValue(Action.NAME), TransferHandler.getCopyAction());
        map.put(TransferHandler.getPasteAction().getValue(Action.NAME), TransferHandler.getPasteAction());
        return map;
    }

    protected static int getFocusAcceleratorKeyMask() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        if (tk instanceof SunToolkit) {
            return ((SunToolkit) tk).getFocusAcceleratorKeyMask();
        }
        return ActionEvent.ALT_MASK;
    }

    /**
     * Invoked when editable property is changed.
     * <p>
     * removing 'TAB' and 'SHIFT-TAB' from traversalKeysSet in case editor is editable adding 'TAB' and 'SHIFT-TAB' to
     * traversalKeysSet in case editor is non editable
     */
    @SuppressWarnings("deprecation")
    protected void updateFocusTraversalKeys() {
        /*
         * Fix for 4514331 Non-editable JTextArea and similar
         * should allow Tab to keyboard - accessibility
         */
        EditorKit editorKit = getEditorKit(editor);
        if (editorKit instanceof DefaultEditorKit) {
            Set<AWTKeyStroke> storedForwardTraversalKeys = editor.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
            Set<AWTKeyStroke> storedBackwardTraversalKeys = editor.getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS);
            Set<AWTKeyStroke> forwardTraversalKeys = new HashSet<>(storedForwardTraversalKeys);
            Set<AWTKeyStroke> backwardTraversalKeys = new HashSet<>(storedBackwardTraversalKeys);
            if (editor.isEditable()) {
                forwardTraversalKeys.remove(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0));
                backwardTraversalKeys.remove(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_MASK));
            } else {
                forwardTraversalKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0));
                backwardTraversalKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_MASK));
            }
            LookAndFeel.installProperty(editor, "focusTraversalKeysForward", forwardTraversalKeys);
            LookAndFeel.installProperty(editor, "focusTraversalKeysBackward", backwardTraversalKeys);
        }
    }

    public class FocusAction extends AbstractAction {

        public void actionPerformed(final ActionEvent e) {
            editor.requestFocus();
        }

        public boolean isEnabled() {
            return editor.isEditable();
        }
    }

    /**
     * Wrapper for text actions to return isEnabled false in case editor is non editable
     */
    public class TextActionWrapper extends TextAction {
        final TextAction action;

        public TextActionWrapper(final TextAction action) {
            super((String) action.getValue(Action.NAME));
            this.action = action;
        }

        /**
         * The operation to perform when this action is triggered.
         *
         * @param e the action event
         */
        public void actionPerformed(final ActionEvent e) {
            action.actionPerformed(e);
        }

        public boolean isEnabled() {
            return (editor == null || editor.isEditable()) && action.isEnabled();
        }
    }
}
