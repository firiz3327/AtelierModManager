package net.firiz.ateliermodmanager.file.pak;

import net.firiz.ateliermodmanager.json.ModFileElement;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

public class PakManager {

    public enum Type {
        RYZA2(PakRyza2.class);

        private final Class<? extends Pak> pakClass;
        private Method findPak;

        Type(Class<? extends Pak> pakClass) {
            this.pakClass = pakClass;
            try {
                this.findPak = pakClass.getMethod("findPak", ModFileElement.class);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }

        Set<Pak> findPak(ModFileElement element) {
            try {
                return (Set<Pak>) findPak.invoke(null, element);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException("illegal error findPak");
            }
        }

    }

    public Set<Pak> findPak(Type type, ModFileElement element) {
        return type.findPak(element);
    }

}
