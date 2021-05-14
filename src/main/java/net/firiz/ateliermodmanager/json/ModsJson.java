package net.firiz.ateliermodmanager.json;

import com.google.gson.annotations.Expose;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.firiz.ateliermodmanager.file.FileManager;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ModsJson {

    private static final FileManager fileManager = FileManager.INSTANCE;

    @Expose
    private final ObjectLinkedOpenHashSet<ModFileElement> files = new ObjectLinkedOpenHashSet<>();

    private int lastPriority;

    public void load() {
        int i = 0;
        for (final ModFileElement mod : new ObjectArrayList<>(files)) {
            if (mod.getFile().exists()) {
                mod.setPriority(i);
                i++;
            } else {
                files.remove(mod);
            }
        }
        for (final File file : Objects.requireNonNull(fileManager.getModsFolder().listFiles(File::isDirectory))) {
            if (!fileManager.findPak(file).isEmpty() && files.stream().noneMatch(m -> file.equals(m.getFile()))) {
                files.add(new ModFileElement(file, false, i));
                i++;
            }
        }
        lastPriority = i;
        save();
    }

    public int createPriority() {
        final int priority = lastPriority;
        lastPriority++;
        return priority;
    }

    public ObjectCollection<File> getSelectedFiles() {
        return files.stream()
                .filter(entry -> entry.isActive() && entry.getFile().exists())
                .map(ModFileElement::getFile)
                .collect(Collectors.toCollection(ObjectArrayList::new));
    }

    public ObjectCollection<ModFileElement> getSelectedFileElements() {
        return files.stream()
                .filter(entry -> entry.isActive() && entry.getFile().exists())
                .collect(Collectors.toCollection(ObjectArrayList::new));
    }

    public ObjectLinkedOpenHashSet<ModFileElement> getFileElements() {
        return files;
    }

    public boolean addFile(File file, String name) {
        try {
            final File toDirectory = new File(fileManager.getModsFolder(), name);
            FileUtils.copyDirectory(file, toDirectory);
            files.add(new ModFileElement(toDirectory, false, createPriority()));
            FileUtils.deleteDirectory(file);
            save();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void save() {
        fileManager.save(this);
    }

    public Collection<ModFileElement> conflictingFiles(ModFileElement mod) {
        return conflictingFiles(mod, false);
    }

    public Collection<ModFileElement> conflictingFiles(ModFileElement mod, boolean priority) {
        final Set<ModFileElement> conflictingMods = new ObjectLinkedOpenHashSet<>();
        for (final File file : mod.overrideFiles()) {
            for (final ModFileElement m : files) {
                if (m != mod && (!priority || (m.getPriority() > mod.getPriority()))) {
                    m.overrideFiles().forEach(f -> {
                        if (file.getName().equals(f.getName())) {
                            conflictingMods.add(m);
                        }
                    });
                }
            }
        }
        return conflictingMods;
    }

}
