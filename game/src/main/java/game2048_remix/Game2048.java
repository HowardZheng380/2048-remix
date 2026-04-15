package game2048_remix;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Game2048 {
    private int[][] board = new int[4][4];
    private Random random = new Random();

    public Game2048() {
        reset();
    }

    public int[][] getBoard() {
        return board;
    }

    public void reset() {
        board = new int[4][4];
        addRandomTile();
        addRandomTile();
    }

    public void addRandomTile() {
        List<Point> emptyCells = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (board[i][j] == 0) {
                    emptyCells.add(new Point(i, j));
                }
            }
        }
        if (!emptyCells.isEmpty()) {
            Point p = emptyCells.get(random.nextInt(emptyCells.size()));
            // 90% chance to spawn 2, 10% chance to spawn 4
            board[p.x][p.y] = random.nextDouble() < 0.9 ? 2 : 4;
        }
    }

    public boolean moveUp() {
        boolean moved = false;
        for (int j = 0; j < 4; j++) {
            int[] column = new int[4];
            for (int i = 0; i < 4; i++) {
                column[i] = board[i][j];
            }
            int[] newColumn = slideAndMerge(column);
            for (int i = 0; i < 4; i++) {
                if (board[i][j] != newColumn[i]) moved = true;
                board[i][j] = newColumn[i];
            }
        }
        return moved;
    }

    public boolean moveDown() {
        boolean moved = false;
        for (int j = 0; j < 4; j++) {
            int[] column = new int[4];
            for (int i = 0; i < 4; i++) {
                column[i] = board[3 - i][j];
            }
            int[] newColumn = slideAndMerge(column);
            for (int i = 0; i < 4; i++) {
                if (board[3 - i][j] != newColumn[i]) moved = true;
                board[3 - i][j] = newColumn[i];
            }
        }
        return moved;
    }

    public boolean moveLeft() {
        boolean moved = false;
        for (int i = 0; i < 4; i++) {
            int[] row = board[i].clone();
            int[] newRow = slideAndMerge(row);
            if (!java.util.Arrays.equals(board[i], newRow)) moved = true;
            board[i] = newRow;
        }
        return moved;
    }

    public boolean moveRight() {
        boolean moved = false;
        for (int i = 0; i < 4; i++) {
            int[] row = new int[4];
            for (int j = 0; j < 4; j++) {
                row[j] = board[i][3 - j];
            }
            int[] newRow = slideAndMerge(row);
            for (int j = 0; j < 4; j++) {
                if (board[i][3 - j] != newRow[j]) moved = true;
                board[i][3 - j] = newRow[j];
            }
        }
        return moved;
    }

    private int[] slideAndMerge(int[] line) {
        int[] result = new int[4];
        int index = 0;
        for (int num : line) {
            if (num != 0) {
                result[index++] = num;
            }
        }
        for (int i = 0; i < 3; i++) {
            if (result[i] == result[i + 1] && result[i] != 0) {
                result[i] *= 2;
                result[i + 1] = 0;
            }
        }
        index = 0;
        int[] finalResult = new int[4];
        for (int num : result) {
            if (num != 0) {
                finalResult[index++] = num;
            }
        }
        return finalResult;
    }

    public boolean checkWin() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (board[i][j] == 2048) return true;
            }
        }
        return false;
    }

    public boolean checkLose() {
        // check if any empty
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (board[i][j] == 0) return false;
            }
        }
        // check if any adjacent same
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (i < 3 && board[i][j] == board[i + 1][j]) return false;
                if (j < 3 && board[i][j] == board[i][j + 1]) return false;
            }
        }
        return true;
    }
}
