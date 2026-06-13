package ch.korotkevics.play2048.domain.engine;

public record TileMove(
        int fromIndex,
        int toIndex,
        int value,
        boolean merged
) {
}
