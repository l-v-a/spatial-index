package lva.spatialindex.viewer.controller;

import com.google.common.collect.ImmutableMap;
import lva.spatialindex.viewer.storage.CircleShape;
import lva.spatialindex.viewer.storage.RectangleShape;
import lva.spatialindex.viewer.storage.Shape;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;

/**
 * @author vlitvinenko
 */
class ShapeParser {
    private static final Pattern SHAPE_FORMAT_PATTERN = Pattern.compile("(\\w+):(.+)");
    private static final Map<String, Function<List<Integer>, Shape>> CONVERTERS_MAP =
            ImmutableMap.<String, Function<List<Integer>, Shape>>builder()
                    .put("rect", ShapeParser::toRectangle)
                    .put("circle", ShapeParser::toCircle)
                    .build();

    static Optional<Shape> parseShape(String str) {
        return Optional.of(SHAPE_FORMAT_PATTERN.matcher(str))
                .filter(Matcher::matches)
                .flatMap(ShapeParser::toShape);
    }

    private static Optional<Shape> toShape(Matcher matcher) {
        String type = matcher.group(1).trim().toLowerCase();
        String params = matcher.group(2).trim();
        List<Integer> args = Arrays.stream(params.split("\\s*,\\s*"))
                .map(Integer::valueOf)
                .collect(toList());
        return Optional.ofNullable(CONVERTERS_MAP.get(type))
                .map(parser -> parser.apply(args));
    }

    private static Shape toRectangle(List<Integer> args) {
        return new RectangleShape(args.get(0), args.get(1), args.get(2), args.get(3));
    }

    private static Shape toCircle(List<Integer> args) {
        return new CircleShape(args.get(0), args.get(1), args.get(2));
    }
}
