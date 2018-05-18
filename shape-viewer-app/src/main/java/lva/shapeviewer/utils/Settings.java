package lva.shapeviewer.utils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class Settings {
    public static Path getShapesPath() {
        String path = System.getProperty("lva.shapesPath");
        return Objects.isNull(path) ? Paths.get(System.getProperty("user.home"), "shapes.txt") : Paths.get(path);
    }

    public static Path getDbPath() {
        String path = System.getProperty("lva.dbPath");
        return Objects.isNull(path) ? Paths.get(System.getProperty("user.home")) : Paths.get(path);
    }
}
