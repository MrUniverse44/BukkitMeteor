package me.blueslime.bukkitmeteor.configuration.custom;

import com.google.common.base.Charsets;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.representer.Representer;

public class YamlConfiguration {

    private static final ThreadLocal<Yaml> YAML_THREAD_LOCAL = ThreadLocal.withInitial(() -> {
        Representer representer = new Representer() {
            {
                representers.put(CustomConfiguration.class, data -> represent(((CustomConfiguration) data).self));
            }
        };

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        return new Yaml(new SafeConstructor(), representer, options);
    });

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
        YAML_THREAD_LOCAL.get().dump(config.self, writer);
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
    private static CustomConfiguration load(InputStream is, CustomConfiguration defaults) {
        Map<String, Object> map = YAML_THREAD_LOCAL.get().loadAs(is, LinkedHashMap.class);

        if (map == null) {
            map = new LinkedHashMap<>();
        }

        return new CustomConfiguration(map, defaults);
    }
}
