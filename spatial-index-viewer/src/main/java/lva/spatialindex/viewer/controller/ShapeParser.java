package lva.spatialindex.viewer.controller;

import com.google.common.collect.ImmutableMap;
import lva.spatialindex.viewer.storage.CircleShape;
import lva.spatialindex.viewer.storage.RectangleShape;
import lva.spatialindex.viewer.storage.Shape;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author vlitvinenko
 */
class ShapeParser {
    private static final Pattern SHAPE_FORMAT_PATTERN = Pattern.compile("(\\w+):(.+)");

    @FunctionalInterface
    private interface Parser {
        Shape parse(List<Integer> args);
    }

    private static final Map<String, Parser> PARSER_MAP = ImmutableMap.<String, Parser>builder()
            .put("rect", ShapeParser::parseRectangle)
            .put("circle", ShapeParser::parseCircle)
            .build();

    static Shape parseShape(String str) {
        Matcher matcher= SHAPE_FORMAT_PATTERN.matcher(str);
        if (matcher.matches()) {
            String type = matcher.group(1).trim().toLowerCase();
            String params = matcher.group(2).trim();
            List<Integer> args = Arrays.stream(params.split("\\s*,\\s*"))
                    .map(Integer::valueOf)
                    .collect(Collectors.toList());

            Parser parser = PARSER_MAP.get(type);
            if (parser != null) {
                return parser.parse(args);
            }
        }

        throw new IllegalArgumentException(String.format("Unable to parse shape from input string: %s", str));
    }

    private static Shape parseRectangle(List<Integer> args) {
        int x = args.get(0);
        int y = args.get(1);
        int w = args.get(2);
        int h = args.get(3);
        return new RectangleShape(x, y, w, h);
    }

    private static Shape parseCircle(List<Integer> args) {
        int x = args.get(0);
        int y = args.get(1);
        int r = args.get(2);
        return new CircleShape(x, y, r);
    }
}
