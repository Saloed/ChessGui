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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import api.ChessAPI;
import conyashka.chess.R;
import conyashka.chess.activities.GameActivity;
import conyashka.chess.database.BoardAdapter;
import core.BitBoard;
import core.EngineHelper;
import model.Piece;

public class Board extends View {
    private static final String TAG = "APPLICATION_DEBUG";
    //private final static AtomicInteger indexer = new AtomicInteger(0);
    private final ExecutorService engineExecutor = Executors.newSingleThreadExecutor();
    private final ChessAPI chessAPI = new ChessAPI();
    private final int NUM_CELLS = 8;
    private final int numberPaddingFactor = 20;
    private final int highlightFactor = 8;
    private final Bitmap backBitmap;
    private final Paint paintGrid = new Paint();
    private final Rect drawRect = new Rect();
    private final Paint paintLineNumbers = new Paint();
    private final Paint paintPieces = new Paint();
    private final Paint paintHighlightCell = new Paint();
    private final Paint mypaint = new Paint();
    private final Paint borderPaint = new Paint();
    private final List<Bitmap> pieceBitmaps;
    private final MediaPlayer mediaPlayer;
    private final GameActivity gameActivity;
    private final Runnable engineCalculatingTask = new Runnable() {
        @Override
        public void run() {

//            Thread.currentThread().setName("Engine Thread " + indexer.getAndIncrement());
//            Log.i(TAG, "Thread renamed");

            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

            //Log.i(TAG, "start move calculating");
            chessAPI.setEnginePowerfull(2, 1);
            String move = chessAPI.computerMove();
            //Log.i(TAG, "move calculating is near end");
            Message msg = (engineResultHandler.obtainMessage());
            Bundle bundle = new Bundle();
            bundle.putString("calculatedMove", move);
            msg.setData(bundle);
            engineResultHandler.sendMessage(msg);
            //Log.i(TAG, "run() ended, move calculated");
        }
    };
    public boolean isEngineCalculate = false;
    private boolean playVsComputer = false;
    private boolean playDemo = false;
    private float soundVolume = 0;
    private boolean finished = false;
    private int cellSize;
    private int numberPadding;
    private Bitmap backScaledBitmap;
    private int minSizeForNumbers = 800;
    private Coordinate currentPieceCoordinate = null;
    private Coordinate lastMoveStart = null;
    private Coordinate lastMoveEnd = null;
    private boolean waitForRepaint = false;
    //private final Thread engineThread = new Thread(engineCalculatingTask);
    private List<Bitmap> pieceScaledBitmaps;
    private Map<Long, List<BitBoard.BitBoardMove>> possibleMoves;
    private boolean helpWasRequested = false;
    private final Handler engineResultHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //Log.i(TAG, "result move handled");
            Bundle bundle = msg.getData();
            String calculatedMove = bundle.getString("calculatedMove");
            afterComputerCalc(calculatedMove);
        }
    };

    public Board(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(0);

        chessAPI.startPosition();
        possibleMoves = chessAPI.allPossibleMoves();

        backBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.plank);


        paintPieces.setStyle(Paint.Style.FILL);
        paintPieces.setStrokeWidth(4);
        paintPieces.setTextAlign(Paint.Align.CENTER);

        paintLineNumbers.setStyle(Paint.Style.FILL);
        paintLineNumbers.setStrokeWidth(4);
        paintPieces.setTextAlign(Paint.Align.CENTER);
        paintLineNumbers.setColor(Color.GRAY);

        mediaPlayer = MediaPlayer.create(context, R.raw.tick);
        mediaPlayer.setVolume(soundVolume, soundVolume);
        mediaPlayer.setLooping(false);

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


        gameActivity = (GameActivity) getContext();

    }

    public void setPreferences(int soundVolume) {
        this.soundVolume = soundVolume / 100.0f;
        this.mediaPlayer.setVolume(soundVolume, soundVolume);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //  Log.i(TAG, "draw start");

        if (isEngineCalculate)
            return;

        // try {


        CellBounds temp;

        mypaint.reset();
        mypaint.setColor(Color.RED);
        mypaint.setAntiAlias(true);
        mypaint.setFilterBitmap(true);
        mypaint.setDither(true);

        canvas.drawBitmap(backScaledBitmap, numberPadding, numberPadding, mypaint);

        // Log.i(TAG, "fon drowed");
        //try {
        //Draw the squares
        for (int x = 0; x < NUM_CELLS; x++) {
            for (int y = 0; y < NUM_CELLS; y++) {
                temp = getCellBounds(new Coordinate(x, y));

                drawRect.set((int) temp.getLeft(), (int) temp.getTop(), (int) temp.getRight(), (int) temp.getBottom());
                if ((((x & 1) ^ 1) ^ (y & 1)) != 1) {
                    paintGrid.setColor(Color.parseColor("#80efebe8"));
                } else {
                    paintGrid.setColor(Color.parseColor("#80b8c1c0"));
                }
                canvas.drawRect(drawRect, paintGrid);
            }
        }
        //  Log.i(TAG, "squares drawed");
//            } catch (Exception e) {
//                Log.i(TAG, "board fill " + e.toString());
//            }
        borderPaint.reset();
        borderPaint.setColor(Color.parseColor("#423f3b"));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(10);
        canvas.drawRect(-2 + numberPadding, -2 + numberPadding,
                cellSize * 8 + 2 + numberPadding,
                cellSize * 8 + 2 + numberPadding, borderPaint);

        //Draw the edge of the board for clarity
        //If there is enough space
        //Draw the left numbers
        for (int i = 0; i < 8; i++) {
            canvas.drawText(String.format("%s", 8 - i),
                    numberPadding * 0.25f,
                    numberPadding + paintLineNumbers.getTextSize() * 0.25f + cellSize * (i + 0.5f),
                    paintLineNumbers);
        }

        //Draw the right numbers
        for (int i = 0; i < 8; i++) {
            canvas.drawText(String.format("%s", 8 - i),
                    cellSize * 8 + numberPadding * 1.25f,
                    numberPadding + paintLineNumbers.getTextSize() * 0.25f + cellSize * (i + 0.5f),
                    paintLineNumbers);
        }

        //Draw the top letters
        for (int i = 0; i < 8; i++) {
            canvas.drawText(numberToChar(i + 1),
                    numberPadding + cellSize * (i + 0.5f) - paintLineNumbers.getTextSize() * 0.5f,
                    numberPadding * 0.75f,
                    paintLineNumbers);
        }


        //Draw the bottom letters
        for (int i = 0; i < 8; i++) {
            canvas.drawText(numberToChar(i + 1),
                    numberPadding + cellSize * (i + 0.5f) - paintLineNumbers.getTextSize() * 0.5f,
                    canvas.getHeight() - paintLineNumbers.getTextSize() * 0.25f,
                    paintLineNumbers);
        }

//        Log.i(TAG, "line numbers drowed");


        if (this.lastMoveStart != null && this.lastMoveEnd != null) {
            highlightCell(canvas, lastMoveStart, Color.parseColor("#bdb29d"));
            highlightCell(canvas, lastMoveEnd, Color.parseColor("#bdb29d"));
        }

        // Log.i(TAG, "dont know drowed");

        //Draw highlights
        if ((currentPieceCoordinate != null) && !possibleMoves.isEmpty()) {
            List<BitBoard.BitBoardMove> legalMoves = possibleMoves.get(
                    chessAPI.getPosition(currentPieceCoordinate.getFile(),
                            currentPieceCoordinate.getRank()));

            if (legalMoves != null) {
                for (BitBoard.BitBoardMove move : legalMoves) {
                    Coordinate to = new Coordinate(chessAPI.getFile(move.getToSquare()),
                            chessAPI.getRank(move.getToSquare()));

//                    if (move.getCastle()) {
//                        Log.i(TAG, currentPieceCoordinate.toString() + to.toString());
//                    }

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

        //try {
        for (ChessAPI.SquareAndPiece piece : boardPieces)

        {
            if (piece.getColor() == Piece.INSTANCE.getWHITE()) {
                paintPieces.setColor(Color.BLUE);
            } else {
                paintPieces.setColor(Color.RED);
            }

            Coordinate pos = new Coordinate(piece.getFile(), piece.getRank());
            CellBounds bounds = getCellBounds(pos);

            mypaint.reset();
            mypaint.setColor(Color.RED);
            mypaint.setAntiAlias(true);
            mypaint.setFilterBitmap(true);
            mypaint.setDither(true);

            Bitmap scaledBitmap = getImage(piece.getPiece(), piece.getColor());

            canvas.drawBitmap(scaledBitmap, bounds.getLeft() - cellSize * 0.01f, bounds.getBottom() - cellSize, mypaint);

            //canvas.drawText(p.getString(), bounds.getLeft() + cellSize * 0.5f, bounds.getBottom() - cellSize * 0.4f, paintPieces);

        }
//            } catch (Exception e) {
//                Log.i(TAG, "piece draw " + e.toString());
//            }
//        Log.i(TAG, "pieces drowed");
//
//        Log.i(TAG, "draw end");
//        } catch (Exception e) {
//            Log.i(TAG, "onDraw: " + e.toString());
//        }


        if (waitForRepaint) {
            waitForRepaint = false;
            computerMove();
        }

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
        numberPadding = (size - 2 * largestPadding) / numberPaddingFactor;
        /*} else {
            numberPadding = 0;
        }
*/
        cellSize = (size - 2 * largestPadding - 2 * numberPadding) / NUM_CELLS;
        paintPieces.setTextSize(cellSize * 0.5f);
        paintHighlightCell.setStrokeWidth(cellSize / highlightFactor);
        paintLineNumbers.setTextSize(numberPadding * 0.75f);

        backScaledBitmap = Bitmap.createScaledBitmap(backBitmap, cellSize * 8, cellSize * 8, true);


        ArrayList<Bitmap> bitmaps = new ArrayList<>(12);
        for (Bitmap bitmap : pieceBitmaps) {
            bitmaps.add(Bitmap.createScaledBitmap(bitmap, cellSize, cellSize, true));
        }
        pieceScaledBitmaps = bitmaps;

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isEngineCalculate) {
            gameActivity.showBusyMessage();
            return true;
        }
        //Log.i(TAG, "board touch");
        //try {
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
//        } catch (Exception e) {
//            Log.i(TAG, e.toString());
//        }
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

                String moveString = oldPosition.getCoord() + c.getCoord();
                BitBoard.BitBoardMove theMove = chessAPI.strToMove(moveString);

                List<BitBoard.BitBoardMove> legalMoves = possibleMoves.get(theMove.getFromSquare());
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

                //System.out.println("MoveString: " + moveString);


                // Log.i(TAG, moveString);

//                Log.i(TAG, moveString);
//                Log.i(TAG, theMove.toString());
//                Log.i(TAG, "Legal Moves Contains this move:  " + legalMoves.contains(theMove));
//                boolean flag = false;
//                for (List<BitBoard.BitBoardMove> b : possibleMoves.values()) {
//                    for (BitBoard.BitBoardMove a : b)
//                        if (a.getCastle()) {
//                            Log.i(TAG, "Can castle");
//                            Log.i(TAG, a.toString());
//
//                            Log.i(TAG, "tryed move");
//                            Log.i(TAG, theMove.toString());
//
//                            if (a.equals(theMove))
//                                Log.i(TAG, "Moves equals");
//
//                            if (theMove.equals(a))
//                                Log.i(TAG, "inverse moves equals");
//
//                            if (a.getFromSquare() != theMove.getFromSquare())
//                                Log.i(TAG, "from square ");
//
//                            if (a.getToSquare() != theMove.getToSquare())
//                                Log.i(TAG, "to square ");
//
//                            if (a.getCastle() != theMove.getCastle())
//                                Log.i(TAG, "castle ");
//
//                            if (a.getEnpassant() != theMove.getEnpassant())
//                                Log.i(TAG, "enpassant ");
//
//                            if (a.isCapture() != theMove.isCapture())
//                                Log.i(TAG, "capture ");
//
//                            if (a.getPromote() != theMove.getPromote())
//                                Log.i(TAG, "promote ");
//
//                            if (a.getColorIndex() != theMove.getColorIndex())
//                                Log.i(TAG, "color ");
//
//                            if (a.getPieceIndex() != theMove.getPieceIndex())
//                                Log.i(TAG, "piece ");
//
//
//                            flag = true;
//                            break;
//                        }
//                    if (flag)
//                        break;
//                }
//
//                if (!flag)
//                    Log.i(TAG, "Cant castle");


                if (legalMoves.contains(theMove)) {

                    chessAPI.makeMove(moveString);
                    // Log.i(TAG, "Move done");

                }
                if (oldPlayerToMove != chessAPI.currentPlayer()) {
                    //A move was made
                    mediaPlayer.setVolume(soundVolume, soundVolume);
                    mediaPlayer.start();

                    this.lastMoveStart = oldPosition;
                    this.lastMoveEnd = c;

                    possibleMoves = chessAPI.allPossibleMoves();

                    if (gameWon()) {
                        gameActivity.newTurn(moveString);
                        gameActivity.playerwon(chessAPI.currentPlayer());
                    } else {
                        gameActivity.newTurn(moveString);
                        if (playVsComputer) {
                            waitForRepaint = true;
                        }
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


        int boardRange = getHeight() - 2 * numberPadding;

        // Log.i(TAG, "Width " + getWidth() + " Height " + getHeight() + " Padding " + numberPadding);

        int c = x - numberPadding;
        int r = y - numberPadding;

        // Log.i(TAG, "X " + x + " Y " + y + " C " + c + " R " + r);


        if (c < 0 || c > boardRange || r < 0 || r > boardRange) {
            return null;
        }

        c = c / cellSize;
        r = r / cellSize;

        //Log.i(TAG, " Final   C " + c + " R " + r);

//        Log.i(TAG, " c " + c + " r " + r);
//        Log.i(TAG, (new Coordinate(8 - c, r + 1)).toString());

        return new Coordinate(7 - c, r);
    }

    private CellBounds getCellBounds(Coordinate c) {
        return new CellBounds(c, numberPadding, cellSize);
    }

    private void highlightCell(Canvas canvas, Coordinate cell, int color) {
        CellBounds theBounds = getCellBounds(cell);

        paintHighlightCell.setColor(color);
        paintHighlightCell.setStyle(Paint.Style.STROKE);
        Path thePath = new Path();
        float offset = paintHighlightCell.getStrokeWidth() / 2;
        thePath.moveTo(theBounds.getLeft() + offset, theBounds.getBottom() - offset);
        thePath.lineTo(theBounds.getLeft() + offset, theBounds.getTop() + offset);
        thePath.lineTo(theBounds.getRight() - offset, theBounds.getTop() + offset);
        thePath.lineTo(theBounds.getRight() - offset, theBounds.getBottom() - offset);
        thePath.lineTo(theBounds.getLeft(), theBounds.getBottom() - offset);
        canvas.drawPath(thePath, paintHighlightCell);
    }

    private void fillCell(Canvas canvas, Coordinate cell, int color) {
        CellBounds theBounds = getCellBounds(cell);

        paintHighlightCell.setColor(color);
        paintHighlightCell.setStyle(Paint.Style.FILL);
        Path thePath = new Path();
        thePath.moveTo(theBounds.getLeft(), theBounds.getBottom());
        thePath.lineTo(theBounds.getLeft(), theBounds.getTop());
        thePath.lineTo(theBounds.getRight(), theBounds.getTop());
        thePath.lineTo(theBounds.getRight(), theBounds.getBottom());
        thePath.lineTo(theBounds.getLeft(), theBounds.getBottom());
        canvas.drawPath(thePath, paintHighlightCell);
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

    //FIXME exception can appear after help. Don't know why
    public void helpRequest() {
        if (isEngineCalculate || helpWasRequested) {
            gameActivity.showBusyMessage();
            return;
        }
        helpWasRequested = true;
        computerMove();
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

    public void setEnginePower(int power) {
        chessAPI.setEnginePowerfull(power % 5, power / 5);
    }

    public void computerMove() {

        // Log.i(TAG, "compute method call");

        if (isEngineCalculate) {
            //   Log.i(TAG, "compute method returned without running");
            return;

        }
        isEngineCalculate = true;
        //Log.i(TAG, "compute method request running");

        engineExecutor.execute(engineCalculatingTask);


//        engineThread.setPriority(Thread.MAX_PRIORITY);
//        engineThread.start();

    }

    private void playDemo() {

        waitForRepaint = true;
        invalidate();

    }

    private void afterComputerCalc(String move) {

//        Log.i(TAG, "after compute method calls");

        BitBoard.BitBoardMove m = chessAPI.strToMove(move);
        lastMoveStart = new Coordinate(chessAPI.getFile(m.getFromSquare()),
                chessAPI.getRank(m.getFromSquare()));
        lastMoveEnd = new Coordinate(chessAPI.getFile(m.getToSquare()),
                chessAPI.getRank(m.getToSquare()));
//        Log.i(TAG, "End move calculating");

        possibleMoves = chessAPI.allPossibleMoves();

        isEngineCalculate = false;

        gameActivity.newTurn(move);


        if (helpWasRequested) {
            helpWasRequested = false;
            if (playVsComputer)
                waitForRepaint = true;

        }


        invalidate();


        if (playDemo)
            playDemo();

    }

    public void stopRequest() {
        if (!isEngineCalculate)
            return;
//        if (!engineThread.isAlive())
//            return;
//
//        engineThread.interrupt();

        engineExecutor.shutdownNow();
        isEngineCalculate = false;
        if (playDemo) {
            playDemo = false;
        }
    }

    public String getGameState() {
//        Log.i(TAG, chessAPI.getFen());
        return chessAPI.getFen();
    }

    public void setGameState(String board, boolean finished) {
        chessAPI.setPosition(board);
        this.finished = finished;
        possibleMoves = chessAPI.allPossibleMoves();
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

    public void acceptGameMode(boolean playVsComputer, boolean playDemo) {
        this.playVsComputer = playVsComputer;
        this.playDemo = playDemo;

        if (playDemo)
            playDemo();

    }


    public void backTrack() {
        if (isEngineCalculate) {
            gameActivity.showBusyMessage();
            return;
        }
        if (chessAPI.unmakeMove()) {

            this.lastMoveStart = null;
            this.lastMoveEnd = null;
            this.finished = false;

            possibleMoves = chessAPI.allPossibleMoves();

            //this.lastMoveStart = null;
            //this.lastMoveEnd = null;

            gameActivity.newTurn("");

            invalidate();
        }
    }


    public void saveToDatabase(String saveName) {
        if (isEngineCalculate) {
            gameActivity.showBusyMessage();
            return;
        }

        BoardAdapter adapter = new BoardAdapter(getContext());
        adapter.insertBoard(saveName, getGameState(), finished ? 1 : 0);


    }

    public void loadFromDatabase() {
        if (isEngineCalculate) {
            gameActivity.showBusyMessage();
            return;
        }
    }

    public void editSettings() {
        if (isEngineCalculate) {
            gameActivity.showBusyMessage();
            return;
        }
    }

}

