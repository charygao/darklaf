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
package com.github.weisj.darklaf.theme.info;

import java.awt.*;

public class AccentColorRule {

    private static final AccentColorRule DEFAULT = new AccentColorRule(null, null);
    private final Color accentColor;
    private final Color selectionColor;

    public AccentColorRule(final Color accentColor) {
        this(accentColor, null);
    }

    protected AccentColorRule(final Color accentColor, final Color selectionColor) {
        this.accentColor = accentColor;
        this.selectionColor = selectionColor;
    }

    public static AccentColorRule getDefault() {
        return DEFAULT;
    }

    public static AccentColorRule fromColor(final Color accentColor, final Color selectionColor) {
        return new AccentColorRule(accentColor, selectionColor);
    }

    public static AccentColorRule fromColor(final Color accentColor) {
        return fromColor(accentColor, null);
    }

    public Color getAccentColor() {
        return accentColor;
    }

    public Color getSelectionColor() {
        return selectionColor;
    }

    @Override
    public String toString() {
        return "AccentColorRule{" +
               "accentColor=" + accentColor +
               ", selectionColor=" + selectionColor +
               '}';
    }
}
