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
package com.github.weisj.darklaf.task;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.text.AttributedCharacterIterator;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javax.swing.*;
import javax.swing.plaf.UIResource;

import com.github.weisj.darklaf.DarkLaf;
import com.github.weisj.darklaf.PropertyLoader;
import com.github.weisj.darklaf.graphics.GraphicsUtil;
import com.github.weisj.darklaf.theme.Theme;
import com.github.weisj.darklaf.theme.info.FontSizeRule;
import com.github.weisj.darklaf.uiresource.DarkFontUIResource;
import com.github.weisj.darklaf.util.LogUtil;
import com.github.weisj.darklaf.util.PropertyUtil;
import com.github.weisj.darklaf.util.SystemInfo;

public class FontDefaultsInitTask implements DefaultsInitTask {

    private static final Logger LOGGER = LogUtil.getLogger(FontDefaultsInitTask.class);
    private static final String FONT_PROPERTY_PATH = "properties/";
    private static final String FONT_SIZE_DEFAULTS_NAME = "font_sizes";
    private static final String FONT_DEFAULTS_NAME = "font";
    private static final String KERNING_LIST = "fontList.kerningEnabled";

    private static final String ALL_FONTS = "__all__";

    private static final Map<AttributedCharacterIterator.Attribute, Integer> ENABLE_KERNING = Collections.singletonMap(TextAttribute.KERNING,
                                                                                                                       TextAttribute.KERNING_ON);
    private static final Map<AttributedCharacterIterator.Attribute, Integer> DISABLE_KERNING = Collections.singletonMap(TextAttribute.KERNING,
                                                                                                                        null);
    private static final String MAC_OS_CATALINA_FONT_NAME = ".AppleSystemUIFont";
    private static final String WINDOWS_10_FONT_NAME = "Segoe UI";
    private static final String MAC_OS_FONT_NAME = ".SF NS Text";

    @Override
    public void run(final Theme currentTheme, final UIDefaults defaults) {
        loadFontProperties(defaults);

        if (SystemInfo.isMac) {
            patchOSFonts(defaults, this::mapMacOSFont);
        } else if (SystemInfo.isWindows) {
            patchOSFonts(defaults, this::mapWindowsFont);
        }

        if (systemKerningEnabled()) {
            List<String> kerningFontsList = PropertyUtil.getList(defaults, KERNING_LIST, String.class);
            if (!kerningFontsList.isEmpty()) {
                Set<String> kerningFonts = new HashSet<>(kerningFontsList);
                boolean enabledAll = ALL_FONTS.equals(kerningFontsList.get(0));

                setupKerningPerFont(defaults, key -> {
                    if (enabledAll) return true;
                    return kerningFonts.contains(key);
                });
            }
        }

        applyFontRule(currentTheme, defaults);
        setupRenderingHints(defaults);
    }

    private boolean systemKerningEnabled() {
        if (SystemInfo.isMac) return SystemInfo.isMacOSMojave;
        if (SystemInfo.isWindows) return SystemInfo.isWindowsVista;
        return false;
    }

    private void setupRenderingHints(final UIDefaults defaults) {
        if (!SystemInfo.isMacOSMojave) {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Map<?, ?> desktopHints = (Map<?, ?>) toolkit.getDesktopProperty(GraphicsUtil.DESKTOP_HINTS_KEY);

            Object aaHint = (desktopHints == null) ? null : desktopHints.get(RenderingHints.KEY_TEXT_ANTIALIASING);
            if (aaHint != null
                && aaHint != RenderingHints.VALUE_TEXT_ANTIALIAS_OFF
                && aaHint != RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT) {
                defaults.put(RenderingHints.KEY_TEXT_ANTIALIASING, aaHint);
                defaults.put(RenderingHints.KEY_TEXT_LCD_CONTRAST,
                             desktopHints.get(RenderingHints.KEY_TEXT_LCD_CONTRAST));
            }
        }
    }

    private void loadFontProperties(final UIDefaults defaults) {
        Properties fontSizeProps = PropertyLoader.loadProperties(DarkLaf.class,
                                                                 FONT_SIZE_DEFAULTS_NAME,
                                                                 FONT_PROPERTY_PATH);
        PropertyLoader.putProperties(fontSizeProps, defaults);
        Properties fontProps = PropertyLoader.loadProperties(DarkLaf.class,
                                                             FONT_DEFAULTS_NAME,
                                                             FONT_PROPERTY_PATH);
        PropertyLoader.putProperties(fontProps, defaults);
    }

    private void patchOSFonts(final UIDefaults defaults, final Function<Font, Font> mapper) {
        PropertyLoader.replacePropertiesOfType(Font.class, defaults, mapper);
    }

    private Font mapMacOSFont(final Font font) {
        String fontName = SystemInfo.isMacOSCatalina ? MAC_OS_CATALINA_FONT_NAME : MAC_OS_FONT_NAME;
        Font macFont = new Font(fontName, font.getStyle(), font.getSize());
        if (SystemInfo.isMacOSMojave) macFont = macFont.deriveFont(ENABLE_KERNING);
        if (font instanceof UIResource) {
            macFont = new DarkFontUIResource(macFont);
        }
        return macFont == null ? font : macFont;
    }

    private Font mapWindowsFont(final Font font) {
        if (!SystemInfo.isWindowsVista) return font;
        Font windowsFont = new Font(WINDOWS_10_FONT_NAME, font.getStyle(), font.getSize());
        if (font instanceof UIResource) {
            windowsFont = new DarkFontUIResource(windowsFont);
        }
        return windowsFont;
    }

    private void setupKerningPerFont(final UIDefaults defaults, final Predicate<String> kerningPredicate) {
        PropertyLoader.replacePropertiesOfType(Font.class, defaults,
                                               e -> kerningPredicate.test(e.getKey().toString()),
                                               f -> {
                                                   Font font = f.deriveFont(ENABLE_KERNING);
                                                   if (f instanceof UIResource) font = new DarkFontUIResource(font);
                                                   return font;
                                               });
    }

    private void applyFontRule(final Theme currentTheme, final UIDefaults defaults) {
        FontSizeRule rule = currentTheme.getFontSizeRule();
        if (rule == null || rule.getType() == FontSizeRule.AdjustmentType.NO_ADJUSTMENT) return;
        PropertyLoader.replacePropertiesOfType(Font.class, defaults, f -> fontWithRule(f, rule));
    }

    private Font fontWithRule(final Font font, final FontSizeRule rule) {
        if (font == null) return null;
        float size = font.getSize2D();
        float newSize = rule.adjustFontSize(size);
        if (newSize == size) return font;
        if (newSize <= 0) {
            LOGGER.warning("Font " + font + " would be invisible after applying " + rule + ". Font won't be changed!");
            return font;
        }
        Font withRule = font.deriveFont(newSize);
        if (font instanceof UIResource
            && !(withRule instanceof UIResource)) {
            withRule = new DarkFontUIResource(withRule);
        }
        return withRule;
    }
}
