package net.firiz.ateliermodmanager.file;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import net.firiz.ateliermodmanager.file.pak.Pak;
import net.firiz.ateliermodmanager.json.ModFileElement;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.FileUtils;

import net.firiz.ateliermodmanager.file.pak.PakManager;
import net.firiz.ateliermodmanager.json.ModsJson;
import net.firiz.ateliermodmanager.json.ModsJsonLoader;
import org.apache.commons.io.IOUtils;

public enum FileManager {
    INSTANCE;

    private final File targetGame = new File("D:\\SteamLibrary\\steamapps\\common\\Atelier Ryza 2");
    private final File targetGameData = new File(targetGame, "Data");
    private final File backup = new File("backup");
    private final File temp = new File("temp");
    private final File mods = new File("mods");
    private final File modsJsonFile = new File(mods, "mods.json");
    private final File unpacker = new File("gust_tools/gust_pak.exe");
    private final PakManager pakManager = new PakManager();

    private ModsJson modsJson;

    public void load() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                FileUtils.deleteDirectory(temp);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
        mods.mkdirs();
        if (modsJsonFile.exists()) {
            modsJson = ModsJsonLoader.fromJson(modsJsonFile);
        } else {
            modsJson = new ModsJson();
        }
        modsJson.load();
    }

    public void save(ModsJson modsJson) {
        write(modsJsonFile, ModsJsonLoader.toJson(modsJson));
    }

    public ModsJson getModsJson() {
        return modsJson;
    }

    public File getModsFolder() {
        return mods;
    }

    public void startGame() {
        try {
            final Process process = new ProcessBuilder("cmd", "/c", new File(targetGame, "Atelier_Ryza_2.exe").getAbsolutePath()).start();
            process.waitFor();
            process.destroy();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void overwrite(ModFileElement mod) {
        if (mod.isActive()) {
            unpack(mod);
            for (final File folder : Objects.requireNonNull(mod.getFile().listFiles())) {
                if (folder.isDirectory()) {
                    if (folder.getName().equalsIgnoreCase("data")) {
                        for (final File file : Objects.requireNonNull(folder.listFiles())) {
                            backup(file, mod.getFile());
                            try {
                                FileUtils.copyDirectoryToDirectory(file, targetGameData);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        backup(folder, mod.getFile());
                        try {
                            FileUtils.copyDirectoryToDirectory(folder, targetGame);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            System.out.println("overwritten " + mod.getFile());
            modsJson.conflictingFiles(mod, true).stream()
                    .filter(ModFileElement::isActive)
                    .forEach(this::overwrite);
        } else {
            for (final File folder : Objects.requireNonNull(mod.getFile().listFiles())) {
                if (folder.isDirectory()) {
                    if (folder.getName().equalsIgnoreCase("data")) {
                        for (final File file : Objects.requireNonNull(folder.listFiles())) {
                            back(file, mod.getFile());
                        }
                    } else {
                        back(folder, mod.getFile());
                    }
                }
            }
            System.out.println("back " + mod.getFile());
            modsJson.conflictingFiles(mod).stream()
                    .filter(ModFileElement::isActive)
                    .forEach(this::overwrite);
        }
    }

    private void backup(File file, File parent) {
        final List<File> overrideFiles = overrideFiles(file);
        for (File f : overrideFiles) {
            final String path = f.getPath().substring(parent.getPath().length());
            final File backupFile = new File(backup, path);
            final File backupTargetFile = new File(targetGame, path);
            if (!backupFile.exists() && backupTargetFile.exists()) {
                System.out.println("backup " + backupTargetFile);
                try {
                    FileUtils.copyFile(backupTargetFile, backupFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void back(File file, File parent) {
        final List<File> overrideFiles = overrideFiles(file);
        for (File f : overrideFiles) {
            final String path = f.getPath().substring(parent.getPath().length());
            final File backupTargetFile = new File(backup, path);
            final File targetFile = new File(targetGame, path);
            if (backupTargetFile.exists()) {
                try {
                    FileUtils.copyFile(backupTargetFile, targetFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                targetFile.delete();
            }
        }
    }

    private List<File> overrideFiles(File file) {
        return new ModFileElement(file).overrideFiles();
    }

    public Set<Pak> findPak(File file) {
        return findPak(new ModFileElement(file));
    }

    public Set<Pak> findPak(ModFileElement mod) {
        return pakManager.findPak(PakManager.Type.RYZA2, mod);
    }

    public void unpack(ModFileElement mod) {
        pakManager.findPak(PakManager.Type.RYZA2, mod).forEach(pak -> unpack(pak.getPakName()));
    }

    private void unpack(String pakName) {
        final File pakFile = new File(targetGameData, pakName);
        if (pakFile.exists()) {
            System.out.println("unpack " + pakName);
            try {
                final Process process = new ProcessBuilder(unpacker.getAbsolutePath(), pakFile.getAbsolutePath()).start();
                new ReadProcessThread(process).start();
                process.waitFor();
                process.destroy();
                final File tempDataFolder = new File(targetGameData, "data");
                if (tempDataFolder.exists()) {
                    FileUtils.copyDirectory(tempDataFolder, targetGameData);
                    FileUtils.deleteDirectory(tempDataFolder);
                } else {
                    for (final File folder : Objects.requireNonNull(targetGameData
                            .listFiles(file -> file.isDirectory() && !file.getName().equalsIgnoreCase("x64")))) {
                        FileUtils.copyDirectoryToDirectory(folder, targetGame);
                        FileUtils.deleteDirectory(folder);
                    }
                }
                pakFile.renameTo(new File(targetGameData, pakName + ".bak"));
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void write(File file, String str) {
        try (final FileOutputStream fileOut = new FileOutputStream(file);
             final BufferedOutputStream out = new BufferedOutputStream(fileOut)) {
            out.write(str.getBytes(StandardCharsets.UTF_8));
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File unarchive(File file) {
        final String name = file.getName();
        if (file.isFile()) {
            temp.mkdirs();
            try {
                final File tempArchive = new File(temp, name);
                FileUtils.copyFile(file, tempArchive);
                final int index = name.lastIndexOf(".");
                switch (name.substring(index + 1).toLowerCase()) {
                    case "zip":
                        unzip(tempArchive);
                        break;
                    case "7z":
                        un7z(tempArchive);
                        break;
                    default: // ignored
                        throw new IllegalArgumentException(name + " is not support.");
                }
                tempArchive.delete();
                final File result = new File(temp, name.substring(0, index));
                System.out.println("unarchived " + result.getName());
                return result;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        throw new IllegalArgumentException(name + " is not support.");
    }

    private void unzip(File file) {
        try (final ZipArchiveInputStream archive = new ZipArchiveInputStream(new FileInputStream(file), "Windows-31J")) {
            ZipArchiveEntry entry;
            while ((entry = archive.getNextZipEntry()) != null) {
                if (!archive.canReadEntryData(entry)) {
                    continue;
                }
                final File f = new File(temp, entry.getName());
                if (entry.isDirectory()) {
                    if (!f.isDirectory() && !f.mkdirs()) {
                        throw new IOException("failed to create directory " + f);
                    }
                } else {
                    final File parent = f.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("failed to create directory " + parent);
                    }
                    try (final OutputStream o = Files.newOutputStream(f.toPath())) {
                        IOUtils.copy(archive, o);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void un7z(File file) {
        try (final SevenZFile sevenZFile = new SevenZFile(file)) {
            SevenZArchiveEntry entry;

            while ((entry = sevenZFile.getNextEntry()) != null) {
                final File f = new File(temp, entry.getName());
                if (entry.isDirectory()) {
                    if (!f.isDirectory() && !f.mkdirs()) {
                        throw new IOException("failed to create directory " + f);
                    }
                } else {
                    final File parent = f.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("failed to create directory " + parent);
                    }
                    try (final OutputStream out = new FileOutputStream(new File(temp, entry.getName()))) {
                        final byte[] content = new byte[(int) entry.getSize()];
                        sevenZFile.read(content, 0, content.length);
                        out.write(content);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File getTemp() {
        return temp;
    }
}
