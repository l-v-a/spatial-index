package lva.shapeviewer.utils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class Settings {
    public static Path getShapesPath() {
        return getPath("lva.shapesPath", Paths.get(System.getProperty("user.home"), "shapes.txt"));
    }

    public static Path getDbPath() {
        return getPath("lva.dbPath", Paths.get(System.getProperty("user.home")));
    }

    private static Path getPath(String propertyName, Path defaultPath) {
        String path = System.getProperty(propertyName);
        return Objects.isNull(path) ? defaultPath : Paths.get(path);
    }
}
