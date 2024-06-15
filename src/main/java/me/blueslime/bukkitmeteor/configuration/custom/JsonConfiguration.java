package me.blueslime.bukkitmeteor.configuration.custom;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSerializer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;

public class JsonConfiguration {

    private static final Gson json = new GsonBuilder().serializeNulls().setPrettyPrinting().registerTypeAdapter(
            CustomConfiguration.class,
            (JsonSerializer<CustomConfiguration>) (src, typeOfSrc, context) -> context.serialize(src.self)
    ).create();

    public static void save(CustomConfiguration config, File file) throws IOException {
        save(config, file, Charsets.UTF_8);
    }

    public static void save(CustomConfiguration config, File file, Charset charset) throws IOException {
        try (Writer writer = new OutputStreamWriter(
                Files.newOutputStream(file.toPath()),
                charset)
        ) {
            save(config, writer);
        }
    }

    private static void save(CustomConfiguration config, Writer writer) {
        json.toJson(config.self, writer);
    }

    public static CustomConfiguration load(File file) throws IOException {
        return load(file, null);
    }

    public static CustomConfiguration load(File file, CustomConfiguration defaults) throws IOException {
        try (FileInputStream is = new FileInputStream(file)) {
            return load(is, defaults);
        }
    }

    @SuppressWarnings("unchecked")
    private static CustomConfiguration load(Reader reader, CustomConfiguration defaults) {
        Map<String, Object> map = json.fromJson(reader, LinkedHashMap.class);
        if (map == null) {
            map = new LinkedHashMap<>();
        }
        return new CustomConfiguration(map, defaults);
    }

    private static CustomConfiguration load(InputStream is, CustomConfiguration defaults) {
        return load(new InputStreamReader(is, Charsets.UTF_8), defaults);
    }
}
