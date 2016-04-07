package conyashka.chess.board;

class CellBounds {
    private final float left;
    private final float right;
    private final float top;
    private final float bottom;

    public CellBounds(Coordinate c, float padding, float cellSize) {
        left = padding + (cellSize * (c.getFile() - 1));
        right = padding + (cellSize * c.getFile());
        top = padding + (cellSize * (8 - c.getRank()));
        bottom = padding + (cellSize * (9 - c.getRank()));
    }

    public float getLeft() {
        return left;
    }

    public float getRight() {
        return right;
    }

    public float getTop() {
        return top;
    }

    public float getBottom() {
        return bottom;
    }

    @Override
    public String toString() {
        return String.format("left: %s. right: %s. bottom: %s. top: %s", left, right, bottom, top);
    }
}
