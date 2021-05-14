package net.firiz.ateliermodmanager.gui;

import net.firiz.ateliermodmanager.file.FileManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import net.firiz.ateliermodmanager.json.ModFileElement;

public class ModTableItem extends TableItem {

    private static final FileManager fileManager = FileManager.INSTANCE;
    private final ModFileElement mod;

    private static final Color COLOR_NONE = new Color(null, 255, 255, 255);
    private static final Color COLOR_OVERWRITTEN = new Color(null, 255, 100, 100);
    private static final Color COLOR_OVERWRITING = new Color(null, 100, 255, 100);

    public ModTableItem(Table parent, ModFileElement mod) {
        super(parent, SWT.NONE);
        this.mod = mod;
        setBackground(COLOR_NONE);
        refresh();
    }

    public ModFileElement getMod() {
        return mod;
    }

    public void refresh() {
        setText(0, mod.getName());
        setText(1, conflictingText());
        setText(2, "x");
        setText(3, String.valueOf(mod.getPriority()));
        setChecked(mod.isActive());
    }

    private String conflictingText() {
        boolean overwritten = false;
        boolean overwriting = false;
        for (final ModFileElement conflictingMod : fileManager.getModsJson().conflictingFiles(mod, false)) {
            final boolean _overwritten = mod.getPriority() < conflictingMod.getPriority();
            if (_overwritten) {
                overwritten = true;
            } else {
                overwriting = true;
            }
        }
        final String message;
        if (overwritten && overwriting) {
            message = "+-";
        } else if (overwritten) {
            message = "-";
        } else if (overwriting) {
            message = "+";
        } else {
            message = "";
        }
        return message;
    }

    public void conflictingFileSelect() {
        fileManager.getModsJson().conflictingFiles(mod, false).forEach(conflictingMod -> {
            for (final TableItem tableItem : getParent().getItems()) {
                if (tableItem instanceof ModTableItem) {
                    final ModTableItem modTableItem = (ModTableItem) tableItem;
                    if (conflictingMod.equals(modTableItem.mod)) {
                        final boolean overwritten = mod.getPriority() < conflictingMod.getPriority();
                        if (overwritten) {
                            modTableItem.setBackground(COLOR_OVERWRITTEN);
                        } else {
                            modTableItem.setBackground(COLOR_OVERWRITING);
                        }
                    }
                }
            }
        });
    }

    public void resetSelectColor() {
        setBackground(COLOR_NONE);
    }

    @Override
    protected void checkSubclass() {
        // ignored
    }

    public enum Column {
        MOD_NAME("Mod名", 460),
        CONFLICT("競合", 200),
        CATEGORY("カテゴリー", 80),
        PRIORITY("優先度", 80);

        private final String name;
        private final int width;

        Column(String name, int width) {
            this.name = name;
            this.width = width;
        }

        public String getName() {
            return name;
        }

        public int getWidth() {
            return width;
        }
    }

}
