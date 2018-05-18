#!/usr/bin/env python

import sys


def main(args):
    output_file = args[0]
    max_rows = 100
    max_cols = 100
    num_of_shapes = max_rows * max_cols

    print('saving results to "%s", num of elements: %s' % (output_file, num_of_shapes))

    with open(output_file, 'wt') as f:
        for row in xrange(0, max_rows):
            for col in xrange(0, max_cols):
                # f.write('Rect: %s, %s, %s, %s\n' % (3 + row * 35, 3 + col * 35, 50, 50))
                f.write('Circle: %s, %s, %s\n' % (3 + row * 35, 3 + col * 35, 50))


if __name__ == "__main__":
    main(sys.argv[1:])
