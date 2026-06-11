package ch.korotkevics.play2048.domain;

final class LineProcessor {

    ProcessedLine process(int[] line) {
        int size = line.length;
        int[] result = new int[size];
        int resultIndex = 0;
        int scoreGained = 0;

        for (int index = 0; index < line.length; index++) {
            if (line[index] == 0) {
                continue;
            }

            if (index + 1 < line.length) {
                int nextIndex = nextNonZeroIndex(line, index + 1);
                if (nextIndex >= 0 && line[index] == line[nextIndex]) {
                    result[resultIndex++] = line[index] * 2;
                    scoreGained += line[index] * 2;
                    index = nextIndex;
                    continue;
                }
            }

            result[resultIndex++] = line[index];
        }

        return new ProcessedLine(result, scoreGained);
    }

    private int nextNonZeroIndex(int[] line, int startIndex) {
        for (int index = startIndex; index < line.length; index++) {
            if (line[index] != 0) {
                return index;
            }
        }
        return -1;
    }

    record ProcessedLine(int[] values, int scoreGained) {
    }
}
