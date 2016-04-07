package conyashka.chess.board;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import api.ChessAPI;
import conyashka.chess.R;
import conyashka.chess.activities.GameActivity;
import core.BitBoard;
import core.EngineHelper;
import model.Piece;

public class Board extends View {
    private static final String TAG = "APPLICATION_DEBUG";

    private final ChessAPI chessAPI = new ChessAPI();

    private final int NUM_CELLS = 8;
    private final int m_numberPaddingFactor = 20;
    private final int m_highlightFactor = 8;
    private final Bitmap backBitmap;
    private final Paint m_paintGrid = new Paint();
    private final Rect drawRect = new Rect();
    private final Paint m_paintLineNumbers = new Paint();
    private final Paint m_paintPieces = new Paint();
    private final Paint m_paintHighlightCell = new Paint();

    private final Paint mypaint = new Paint();
    private final Paint borderPaint = new Paint();
    private final List<Bitmap> pieceBitmaps;
    private final MediaPlayer m_mediaPlayer;

    private float soundVolume = 0;

    private boolean finished = false;
    private int m_cellSize;
    private int m_numberPadding;
    private Bitmap backScaledBitmap;
    private int minSizeForNumbers = 800;
    private Coordinate currentPieceCoordinate = null;
    private Coordinate lastMoveStart = null;
    private Coordinate lastMoveEnd = null;
    private List<Bitmap> pieceScaledBitmaps;

    private Map<Long, List<BitBoard.BitBoardMove>> possibleMoves;


    public Board(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(0);

        chessAPI.startPosition();
        possibleMoves = chessAPI.allPossibleMoves();

        backBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.plank);


        m_paintPieces.setStyle(Paint.Style.FILL);
        m_paintPieces.setStrokeWidth(4);
        m_paintPieces.setTextAlign(Paint.Align.CENTER);

        m_paintLineNumbers.setStyle(Paint.Style.FILL);
        m_paintLineNumbers.setStrokeWidth(4);
        m_paintPieces.setTextAlign(Paint.Align.CENTER);
        m_paintLineNumbers.setColor(Color.GRAY);

        m_mediaPlayer = MediaPlayer.create(context, R.raw.tick);
        m_mediaPlayer.setVolume(soundVolume, soundVolume);
        m_mediaPlayer.setLooping(false);

        ArrayList<Bitmap> bitmaps = new ArrayList<>(13);
        bitmaps.add(BitmapFactory.decodeResource(getResources(), R.drawable.chessklt60));
        bitmaps.add(BitmapFactory.decodeResource(getResources(), R.drawable.chessqlt60));
        bitmaps.add(BitmapFactory.decodeResource(getResources(), R.drawable.chessnlt60));
        bitmaps.add(BitmapFactory.decodeResource(getResources(), R.drawable.chessblt60));
        bitmaps.add(BitmapFactory.decodeResource(getResources(), R.drawable.chessrlt60));
        bitmaps.add(BitmapFactory.decodeResource(getResources(), R.drawable.chessplt60));
        bitmaps.add(BitmapFactory.decodeResource(getResources(), R.drawable.chesskdt60));
        bitmaps.add(BitmapFactory.decodeResource(getResources(), R.drawable.chessqdt60));
        bitmaps.add(BitmapFactory.decodeResource(getResources(), R.drawable.chessndt60));
        bitmaps.add(BitmapFactory.decodeResource(getResources(), R.drawable.chessbdt60));
        bitmaps.add(BitmapFactory.decodeResource(getResources(), R.drawable.chessrdt60));
        bitmaps.add(BitmapFactory.decodeResource(getResources(), R.drawable.chesspdt60));
        bitmaps.add(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
        pieceBitmaps = bitmaps;
        pieceScaledBitmaps = new ArrayList<>(13);

    }

    public void setPreferences(int soundVolume) {
        this.soundVolume = soundVolume / 100.0f;
        this.m_mediaPlayer.setVolume(soundVolume, soundVolume);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //  Log.i(TAG, "draw start");

        CellBounds temp;

        mypaint.reset();
        mypaint.setColor(Color.RED);
        mypaint.setAntiAlias(true);
        mypaint.setFilterBitmap(true);
        mypaint.setDither(true);

        canvas.drawBitmap(backScaledBitmap, m_numberPadding, m_numberPadding, mypaint);

        // Log.i(TAG, "fon drowed");

        //Draw the squares
        for (int x = 0; x < NUM_CELLS; x++) {
            for (int y = 0; y < NUM_CELLS; y++) {
                temp = getCellBounds(new Coordinate(x, y));

                drawRect.set((int) temp.getLeft(), (int) temp.getTop(), (int) temp.getRight(), (int) temp.getBottom());
                if ((((x & 1) ^ 1) ^ (y & 1)) != 1) {
                    m_paintGrid.setColor(Color.parseColor("#80efebe8"));
                } else {
                    m_paintGrid.setColor(Color.parseColor("#80b8c1c0"));
                }
                canvas.drawRect(drawRect, m_paintGrid);
            }
        }
        //  Log.i(TAG, "squares drawed");

        borderPaint.reset();
        borderPaint.setColor(Color.parseColor("#423f3b"));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(10);
        canvas.drawRect(-2 + m_numberPadding, -2 + m_numberPadding, m_cellSize * 8 + 2 + m_numberPadding, m_cellSize * 8 + 2 + m_numberPadding, borderPaint);

        //Draw the edge of the board for clarity
        //If there is enough space
        //Draw the left numbers
        for (int i = 0; i < 8; i++) {
            canvas.drawText(String.format("%s", 8 - i), m_numberPadding * 0.25f, m_numberPadding + m_paintLineNumbers.getTextSize() * 0.25f + m_cellSize * (i + 0.5f), m_paintLineNumbers);
        }

        //Draw the right numbers
        for (int i = 0; i < 8; i++) {
            canvas.drawText(String.format("%s", 8 - i), m_cellSize * 8 + m_numberPadding * 1.25f, m_numberPadding + m_paintLineNumbers.getTextSize() * 0.25f + m_cellSize * (i + 0.5f), m_paintLineNumbers);
        }

        //Draw the top letters
        for (int i = 0; i < 8; i++) {
            canvas.drawText(numberToChar(i + 1), m_numberPadding + m_cellSize * (i + 0.5f) - m_paintLineNumbers.getTextSize() * 0.5f, m_numberPadding * 0.75f, m_paintLineNumbers);
        }


        //Draw the bottom letters
        for (int i = 0; i < 8; i++) {
            canvas.drawText(numberToChar(i + 1), m_numberPadding + m_cellSize * (i + 0.5f) - m_paintLineNumbers.getTextSize() * 0.5f, canvas.getHeight() - m_paintLineNumbers.getTextSize() * 0.25f, m_paintLineNumbers);
        }

//        Log.i(TAG, "line numbers drowed");


//        if (this.lastMoveStart != null) {
//            highlightCell(canvas, lastMoveStart, Color.parseColor("#bdb29d"));
//            highlightCell(canvas, lastMoveEnd, Color.parseColor("#bdb29d"));
//        }

        // Log.i(TAG, "dont know drowed");

        //Draw highlights
        if ((currentPieceCoordinate != null) && !possibleMoves.isEmpty()) {
            List<BitBoard.BitBoardMove> legalMoves = possibleMoves.get(
                    chessAPI.getPosition(currentPieceCoordinate.getFile(), currentPieceCoordinate.getRank()));

            if (legalMoves != null) {
                for (BitBoard.BitBoardMove move : legalMoves) {
                    Coordinate to = new Coordinate(chessAPI.getFile(move.getToSquare()),
                            chessAPI.getRank(move.getToSquare()));


                    if (move.getCastle())
                        highlightCell(canvas, to, Color.parseColor("#B3094809"));
                    else if (move.isCapture())
                        highlightCell(canvas, to, Color.parseColor("#B37e0404"));
                    else if (move.getEnpassant())
                        highlightCell(canvas, to, Color.parseColor("#B3094809"));
                    else if (move.getPromote())
                        highlightCell(canvas, to, Color.parseColor("#B3094809"));
                    else
                        highlightCell(canvas, to, Color.parseColor("#B31d1f63"));


                }
            }
        }
        //Log.i(TAG, "highlights drowed");

        List<ChessAPI.SquareAndPiece> boardPieces = chessAPI.getBoardSituation();

        // Draw the pieces
        //for(Piece p : gameState.getPieces()){
        for (ChessAPI.SquareAndPiece piece : boardPieces)

        {
            if (piece.getColor() == Piece.INSTANCE.getWHITE()) {
                m_paintPieces.setColor(Color.BLUE);
            } else {
                m_paintPieces.setColor(Color.RED);
            }

            Coordinate pos = new Coordinate(piece.getFile(), piece.getRank());
            CellBounds bounds = getCellBounds(pos);

            mypaint.reset();
            mypaint.setColor(Color.RED);
            mypaint.setAntiAlias(true);
            mypaint.setFilterBitmap(true);
            mypaint.setDither(true);

            Bitmap scaledBitmap = getImage(piece.getPiece(), piece.getColor());

            canvas.drawBitmap(scaledBitmap, bounds.getLeft() - m_cellSize * 0.01f, bounds.getBottom() - m_cellSize, mypaint);

            //canvas.drawText(p.getString(), bounds.getLeft() + m_cellSize * 0.5f, bounds.getBottom() - m_cellSize * 0.4f, m_paintPieces);

        }
//        Log.i(TAG, "pieces drowed");
//
//        Log.i(TAG, "draw end");

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        int height = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
        int size = Math.min(width, height);
        setMeasuredDimension(size + getPaddingLeft() + getPaddingRight(),
                size + getPaddingTop() + getPaddingBottom());

    }

    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
        int size = Math.min(xNew, yNew);
        int largestPadding = Math.max(Math.max(getPaddingBottom(), getPaddingTop()), Math.max(getPaddingLeft(), getPaddingRight()));
        /*if (this.useLineNumbers == LineNumberOption.YES ||
                (this.useLineNumbers == LineNumberOption.IF_BIG_ENOUGH && (size - 2 * largestPadding) >= minSizeForNumbers)) {
          */
        m_numberPadding = (size - 2 * largestPadding) / m_numberPaddingFactor;
        /*} else {
            m_numberPadding = 0;
        }
*/
        m_cellSize = (size - 2 * largestPadding - 2 * m_numberPadding) / NUM_CELLS;
        m_paintPieces.setTextSize(m_cellSize * 0.5f);
        m_paintHighlightCell.setStrokeWidth(m_cellSize / m_highlightFactor);
        m_paintLineNumbers.setTextSize(m_numberPadding * 0.75f);

        backScaledBitmap = Bitmap.createScaledBitmap(backBitmap, m_cellSize * 8, m_cellSize * 8, true);


        ArrayList<Bitmap> bitmaps = new ArrayList<>(12);
        for (Bitmap bitmap : pieceBitmaps) {
            bitmaps.add(Bitmap.createScaledBitmap(bitmap, m_cellSize, m_cellSize, true));
        }
        pieceScaledBitmaps = bitmaps;

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //Log.i(TAG, "board touch");
        if (!finished) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                pressed(event);
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                heldDown(event);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                released(event);
            }
            return true;
        }
        return true;
    }

    /**
     * Run when the finger touches the screen
     *
     * @param event
     */
    private void pressed(MotionEvent event) {

        //Log.i(TAG, "pressed start");

        int x = (int) event.getX();

        int y = (int) event.getY();
        //Log.i(TAG, "Pressed: x" + x);
        //Log.i(TAG, "Pressed: y " + y);


        Coordinate c = getCoordinate(x, y);
        if (c != null) {
            // Log.i(TAG, "Pressed: " + c.toString());

            //Log.i(TAG, "C!=null");
            Coordinate oldPosition = this.currentPieceCoordinate;
            int oldPlayerToMove = chessAPI.currentPlayer();

            if (oldPosition != null) {
                // Log.i(TAG, "oldPos!=null");

                List<BitBoard.BitBoardMove> legalMoves = possibleMoves.get(
                        chessAPI.getPosition(oldPosition.getFile(), oldPosition.getRank()));
//                for (Move m : legalMoves) {
//                    if (oldPosition.equals(chessState.sqrToStr(((ChessMove) m).getFrom()))
//                            && c.equals(chessState.sqrToStr(((ChessMove) m).getTo()))) {
//                        if (((ChessMove) m).getType() == ChessMove.Movetype.Promotion) {
//                            promotionBool = true;
//                            System.out.println("There is a promotion");
//                        }
//                    }
//                }

                if (legalMoves == null) {
                    currentPieceCoordinate = c;
                    invalidate();
                    return;
                }

                String moveString = oldPosition.getCoord() + c.getCoord();
                //System.out.println("MoveString: " + moveString);


                // Log.i(TAG, moveString);
                BitBoard.BitBoardMove theMove = chessAPI.strToMove(moveString);
                if (legalMoves.contains(theMove)) {

                    chessAPI.makeMove(moveString);
                    // Log.i(TAG, "Move done");

                }
                if (oldPlayerToMove != chessAPI.currentPlayer()) {
                    //A move was made
                    m_mediaPlayer.setVolume(soundVolume, soundVolume);
                    m_mediaPlayer.start();
                    GameActivity activity = (GameActivity) getContext();

                    this.lastMoveStart = oldPosition;
                    this.lastMoveEnd = c;

                    possibleMoves = chessAPI.allPossibleMoves();

                    if (gameWon()) {
                        activity.newTurn();
                        activity.playerwon(oldPlayerToMove);
                    } else {
                        activity.newTurn();
                    }
                    currentPieceCoordinate = null;
                } else {
                    //No move was made
                    currentPieceCoordinate = c;
                }
            } else {
                currentPieceCoordinate = c;
            }
        }
        //Log.i(TAG, "pressed end");
        invalidate();
    }

    private boolean gameWon() {
        return possibleMoves.isEmpty();
    }

    /**
     * Run while the finger is held down
     *
     * @param event
     */
    private void heldDown(MotionEvent event) {

    }

    /**
     * Run when the finger is released
     *
     * @param event
     */
    private void released(MotionEvent event) {

    }

    private Coordinate getCoordinate(int x, int y) {
        int boardRange = getHeight() - 2 * m_numberPadding;

        int c = x - m_numberPadding;
        int r = y - m_numberPadding;

        if (c < 0 || c > boardRange || r < 0 || r > boardRange) {
            return null;
        }

        c = c / m_cellSize;
        r = r / m_cellSize;
//        Log.i(TAG, " c " + c + " r " + r);
//        Log.i(TAG, (new Coordinate(8 - c, r + 1)).toString());

        return new Coordinate(8 - c, r + 1);
    }

    private CellBounds getCellBounds(Coordinate c) {
        return new CellBounds(c, m_numberPadding, m_cellSize);
    }

    private void highlightCell(Canvas canvas, Coordinate cell, int color) {
        CellBounds theBounds = getCellBounds(cell);

        m_paintHighlightCell.setColor(color);
        m_paintHighlightCell.setStyle(Paint.Style.STROKE);
        Path thePath = new Path();
        float offset = m_paintHighlightCell.getStrokeWidth() / 2;
        thePath.moveTo(theBounds.getLeft() + offset, theBounds.getBottom() - offset);
        thePath.lineTo(theBounds.getLeft() + offset, theBounds.getTop() + offset);
        thePath.lineTo(theBounds.getRight() - offset, theBounds.getTop() + offset);
        thePath.lineTo(theBounds.getRight() - offset, theBounds.getBottom() - offset);
        thePath.lineTo(theBounds.getLeft(), theBounds.getBottom() - offset);
        canvas.drawPath(thePath, m_paintHighlightCell);
    }

    private void fillCell(Canvas canvas, Coordinate cell, int color) {
        CellBounds theBounds = getCellBounds(cell);

        m_paintHighlightCell.setColor(color);
        m_paintHighlightCell.setStyle(Paint.Style.FILL);
        Path thePath = new Path();
        thePath.moveTo(theBounds.getLeft(), theBounds.getBottom());
        thePath.lineTo(theBounds.getLeft(), theBounds.getTop());
        thePath.lineTo(theBounds.getRight(), theBounds.getTop());
        thePath.lineTo(theBounds.getRight(), theBounds.getBottom());
        thePath.lineTo(theBounds.getLeft(), theBounds.getBottom());
        canvas.drawPath(thePath, m_paintHighlightCell);
    }

    private String numberToChar(int number) {
        switch (number) {
            case (1):
                return "A";
            case (2):
                return "B";
            case (3):
                return "C";
            case (4):
                return "D";
            case (5):
                return "E";
            case (6):
                return "F";
            case (7):
                return "G";
            case (8):
                return "H";
            default:
                throw new IllegalArgumentException("Illegal coordinate");
        }
    }

    public void reset() {
        this.currentPieceCoordinate = null;

        chessAPI.startPosition();

        invalidate();
    }

    public void finished() {
        finished = true;
    }

    public boolean getFinished() {
        return this.finished;
    }

    private Bitmap getImage(int piece, int color) {
        //White
        //int color = EngineHelper.INSTANCE.getColor(piece);
        int type = EngineHelper.INSTANCE.getType(piece);
        if (color == Piece.INSTANCE.getWHITE()) {

            if (Piece.INSTANCE.getKING() == type)
                return pieceScaledBitmaps.get(0);
            if (Piece.INSTANCE.getQUEEN() == type)
                return pieceScaledBitmaps.get(1);
            if (Piece.INSTANCE.getKNIGHT() == type)
                return pieceScaledBitmaps.get(2);
            if (Piece.INSTANCE.getBISHOP() == type)
                return pieceScaledBitmaps.get(3);
            if (Piece.INSTANCE.getROOK() == type)
                return pieceScaledBitmaps.get(4);
            if (Piece.INSTANCE.getPAWN() == type)
                return pieceScaledBitmaps.get(5);
        }
        //Black
        else {
            if (Piece.INSTANCE.getKING() == type)
                return pieceScaledBitmaps.get(6);
            if (Piece.INSTANCE.getQUEEN() == type)
                return pieceScaledBitmaps.get(7);
            if (Piece.INSTANCE.getKNIGHT() == type)
                return pieceScaledBitmaps.get(8);
            if (Piece.INSTANCE.getBISHOP() == type)
                return pieceScaledBitmaps.get(9);
            if (Piece.INSTANCE.getROOK() == type)
                return pieceScaledBitmaps.get(10);
            if (Piece.INSTANCE.getPAWN() == type)
                return pieceScaledBitmaps.get(11);

        }
        return pieceScaledBitmaps.get(12);
    }

    public String getGameState() {
        return chessAPI.getFen();
    }

    public void setGameState(String board) {
        chessAPI.setPosition(board);
    }

	/*
    public String promotionPrompt(Player thePlayer){
		PopupMenu theMenu = new PopupMenu(this.getContext(), this);

		theMenu.getMenu().add("Queen");
		theMenu.getMenu().add("Knight");
		theMenu.getMenu().add("Rook");
		theMenu.getMenu().add("Bishop");

		theMenu.show();


		//theMenu.setVisibility(VISIBLE);
		return "";
	}
	*/


    public void backTrack() {

        chessAPI.unmakeMove();

        this.lastMoveStart = null;
        this.lastMoveEnd = null;
        this.finished = false;


        //this.lastMoveStart = null;
        //this.lastMoveEnd = null;

        GameActivity theActivity = (GameActivity) getContext();
        theActivity.newTurn();

        invalidate();
    }


}

