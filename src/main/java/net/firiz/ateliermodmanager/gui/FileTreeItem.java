package net.firiz.ateliermodmanager.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import java.io.File;

public class FileTreeItem extends TreeItem {

    private final boolean root;
    private final File file;

    public FileTreeItem(Tree parent, File file) {
        super(parent, SWT.NONE);
        this.root = true;
        this.file = file;
        setText(file.getName());
    }

    public FileTreeItem(TreeItem parent, File file) {
        super(parent, SWT.NONE);
        this.root = false;
        this.file = file;
        setText(file.getName());
    }

    @Override
    protected void checkSubclass() {
        // ignored
    }

    public boolean isRoot() {
        return root;
    }

    public File getFile() {
        return file;
    }
}
