package ch.korotkevics.play2048.domain.engine;

import java.util.List;

public record GlobalTileMove(
        int fromRow,
        int fromCol,
        int toRow,
        int toCol,
        int value,
        boolean merged,
        boolean isNew // For spawned tiles
) {
}
