package net.firiz.ateliermodmanager.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.firiz.ateliermodmanager.file.FileManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

public class ModsJsonLoader implements TypeAdapterFactory {

    private static final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().registerTypeAdapterFactory(new ModsJsonLoader()).create();
    private static final FileManager fileManager = FileManager.INSTANCE;

    private ModsJsonLoader() {
    }

    @Nullable
    public static ModsJson fromJson(File file) {
        ModsJson json = null;
        try (final FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            json = gson.fromJson(reader, ModsJson.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;
    }

    public static String toJson(ModsJson object) {
        return gson.toJson(object);
    }

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        final Class<T> rawType = cast(typeToken.getRawType());
        if (rawType == File.class) {
            return cast(StringTypeAdapter.createAdapter(
                    gson,
                    File::getName,
                    fileName -> new File(fileManager.getModsFolder(), fileName)
            ));
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private <T> T cast(Object obj) {
        return (T) obj;
    }

    static class StringTypeAdapter<T> extends TypeAdapter<T> {

        private final TypeAdapter<String> elementAdapter;
        private final Function<T, String> serializer;
        private final Function<String, T> deserializer;

        public static <T> StringTypeAdapter<T> createAdapter(
                @NotNull final Gson gson,
                @NotNull final Function<T, String> write,
                @NotNull final Function<String, T> read
        ) {
            return new StringTypeAdapter<>(gson, write, read);
        }

        private StringTypeAdapter(Gson gson, Function<T, String> serializer, Function<String, T> deserializer) {
            this.elementAdapter = gson.getAdapter(String.class);
            this.serializer = serializer;
            this.deserializer = deserializer;
        }

        @Override
        public void write(JsonWriter jsonWriter, T t) throws IOException {
            elementAdapter.write(jsonWriter, serializer.apply(t));
        }

        @Override
        public T read(JsonReader jsonReader) throws IOException {
            return deserializer.apply(elementAdapter.read(jsonReader));
        }
    }

}
