package ch.korotkevics.play2048.domain.engine;

import java.util.ArrayList;
import java.util.List;

final class LineProcessor {

    ProcessedLine process(int[] line) {
        int size = line.length;
        int[] result = new int[size];
        List<TileMove> moves = new ArrayList<>();
        int resultIndex = 0;
        int scoreGained = 0;

        for (int index = 0; index < line.length; index++) {
            if (line[index] == 0) {
                continue;
            }

            if (index + 1 < line.length) {
                int nextIndex = nextNonZeroIndex(line, index + 1);
                if (nextIndex >= 0 && line[index] == line[nextIndex]) {
                    int newValue = line[index] * 2;
                    result[resultIndex] = newValue;
                    scoreGained += newValue;
                    
                    // Both original tiles move/merge into the new position
                    moves.add(new TileMove(index, resultIndex, line[index], true));
                    moves.add(new TileMove(nextIndex, resultIndex, line[nextIndex], true));
                    
                    resultIndex++;
                    index = nextIndex;
                    continue;
                }
            }

            result[resultIndex] = line[index];
            if (index != resultIndex) {
                moves.add(new TileMove(index, resultIndex, line[index], false));
            }
            resultIndex++;
        }

        return new ProcessedLine(result, scoreGained, moves);
    }

    private int nextNonZeroIndex(int[] line, int startIndex) {
        for (int index = startIndex; index < line.length; index++) {
            if (line[index] != 0) {
                return index;
            }
        }
        return -1;
    }

    record ProcessedLine(int[] values, int scoreGained, List<TileMove> moves) {
    }
}
