package net.firiz.ateliermodmanager.file.pak;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.firiz.ateliermodmanager.json.ModFileElement;

import java.io.File;
import java.util.Set;

enum PakRyza2 implements Pak {
    PACK00_01("PACK00_01.PAK", true, "camera", "effect", "posteffect", "res_cmn", "shader", "shaderoriginal", "sky"),
    PACK00_02("PACK00_02.PAK", true, "character"),
    PACK00_03("PACK00_03.PAK", true, "field"),
    PACK01("PACK01.PAK", false, "dlc", "event", "script"),
    PACK02("PACK02.PAK", false, "saves"),
    PACK03("PACK03.PAK", true, "character", "field");

    private final String pakName;
    private final boolean x64; // x64/<folder>
    private final String[] folders;

    PakRyza2(String pakName, boolean x64, String... folders) {
        this.pakName = pakName;
        this.x64 = x64;
        if (x64) {
            this.folders = new String[folders.length];
            for (int i = 0, len = folders.length; i < len; i++) {
                this.folders[i] = "x64\\" + folders[i];
            }
        } else {
            this.folders = folders;
        }
    }

    public static Set<Pak> findPak(ModFileElement element) {
        final Set<Pak> pakSet = new ObjectOpenHashSet<>();
        for (final File file : element.overrideFiles()) {
            String path = file.getPath().substring(element.getFile().getPath().length() + 1);
//            if (path.toLowerCase().startsWith("data")) {
//                path = path.substring(5);
//            }
            for (final PakRyza2 pak : values()) {
                for (final String folder : pak.folders) {
                    if (path.toLowerCase().startsWith((pak.x64 ? "data\\" : "") + folder)) {
                        pakSet.add(pak);
                        break;
                    }
                }
            }
        }
        return pakSet;
    }

    public boolean isX64() {
        return x64;
    }

    @Override
    public String getPakName() {
        return pakName;
    }
}
