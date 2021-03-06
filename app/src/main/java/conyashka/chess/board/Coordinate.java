package conyashka.chess.board;

/**
 * Created by admin on 06.04.2016.
 */
class Coordinate {

    private static final String FILES = "ABCDEFGH";
    private static final String RANKS = "12345678";
    private final int file;
    private final int rank;

    Coordinate(int file, int rank) {
        this.file = 7 - file;
        this.rank = 7 - rank;
        //check();
    }

    private void check() {
        if (file < 0 || rank < 0 || file > 7 || rank > 7)
            throw new IllegalArgumentException(toString());
    }

    public int getFile() {
        return file;
    }

    public int getRank() {
        return rank;
    }

    @Override
    public String toString() {
        return "File " + file + " Rank " + rank;
    }

    public String getCoord() {

        return String.valueOf((FILES.charAt(file)) + String.valueOf(RANKS.charAt(rank)));
    }
}
