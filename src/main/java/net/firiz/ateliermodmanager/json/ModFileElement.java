package net.firiz.ateliermodmanager.json;

import com.google.gson.annotations.Expose;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.io.File;
import java.util.List;

public class ModFileElement {
    @Expose
    private final File file;
    @Expose
    private boolean active;
    private boolean changed;
    private int priority;

    public ModFileElement(File file) {
        this.file = file;
        this.active = false;
    }

    public ModFileElement(File file, boolean active, int priority) {
        this.file = file;
        this.active = active;
        this.priority = priority;
    }

    public List<File> overrideFiles() {
        final List<File> files = new ObjectArrayList<>();
        file(files, file);
        return files;
    }

    private void file(List<File> list, File file) {
        if (file.isDirectory()) {
            final File[] fs = file.listFiles();
            if (fs != null) {
                for (final File f : fs) {
                    file(list, f);
                }
            }
        } else if (file.isFile()) {
            list.add(file);
        }
    }

    public String getName() {
        return file.getName();
    }

    public File getFile() {
        return file;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public String toString() {
        return String.format("Mod[%s]", file.getName());
    }
}
