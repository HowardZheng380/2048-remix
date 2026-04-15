package game2048_remix;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public class GameUI2048 extends JFrame implements KeyListener {
    private static final int GRID_SIZE = 4;
    private static final int ANIMATION_DURATION_MS = 120;
    private static final int ANIMATION_FRAMES = 8;
    private static final Color WINDOW_BACKGROUND = new Color(36, 25, 52);
    private static final Color BOARD_BACKGROUND = new Color(74, 52, 102);
    private static final Color EMPTY_TILE_COLOR = new Color(97, 74, 126);
    private static final Color BUTTON_COLOR = new Color(109, 75, 150);
    private static final Color TITLE_COLOR = new Color(228, 214, 255);
    private static final Color DARK_TEXT = new Color(235, 230, 245);

    private Game2048 game;
    private RoundedTileLabel[][] labels = new RoundedTileLabel[4][4];
    private int[][] previousBoard = new int[4][4];
    private boolean endOverlayShown;
    private JPanel boardPanel;
    private JPanel animationLayer;
    private JPanel overlayPanel;
    private JLabel overlayMessageLabel;
    private boolean animationInProgress;

    public GameUI2048() {
        game = new Game2048();
        copyBoard(game.getBoard(), previousBoard);
        setupGUI();
    }

    private void setupGUI() {
        setTitle("2048");
        setSize(700, 760);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(WINDOW_BACKGROUND);

        JLabel titleLabel = new JLabel("2048", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 54));
        titleLabel.setForeground(TITLE_COLOR);

        JButton newGameButton = createRoundedButton("New Game", new Dimension(170, 55), 22);
        newGameButton.addActionListener(e -> restartGame());


        JPanel controlsPanel = new JPanel();
        controlsPanel.setBackground(WINDOW_BACKGROUND);
        controlsPanel.add(newGameButton);
        controlsPanel.add(Box.createHorizontalStrut(12));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(WINDOW_BACKGROUND);
        titlePanel.setBorder(new EmptyBorder(24, 0, 0, 0));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        controlsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(30));
        titlePanel.add(controlsPanel);

        boardPanel = new JPanel(new GridLayout(4, 4, 5, 5));
        boardPanel.setBackground(BOARD_BACKGROUND);
        boardPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                labels[i][j] = new RoundedTileLabel();
                labels[i][j].setFont(new Font("Arial", Font.BOLD, 24));
                labels[i][j].setForeground(DARK_TEXT);
                boardPanel.add(labels[i][j]);
            }
        }

        Dimension boardSize = new Dimension(500, 500);
        JLayeredPane layeredBoard = new JLayeredPane();
        layeredBoard.setPreferredSize(boardSize);
        layeredBoard.setOpaque(true);
        layeredBoard.setBackground(BOARD_BACKGROUND);

        boardPanel.setBounds(0, 0, boardSize.width, boardSize.height);
        layeredBoard.add(boardPanel, Integer.valueOf(0));

        animationLayer = new JPanel(null);
        animationLayer.setOpaque(false);
        animationLayer.setBounds(0, 0, boardSize.width, boardSize.height);
        layeredBoard.add(animationLayer, Integer.valueOf(1));

        JPanel boardWrapper = new JPanel();
        boardWrapper.setOpaque(true);
        boardWrapper.setBackground(WINDOW_BACKGROUND);
        boardWrapper.setBorder(new EmptyBorder(40, 0, 10, 0));
        boardWrapper.add(layeredBoard);

        add(titlePanel, BorderLayout.NORTH);
        add(boardWrapper, BorderLayout.CENTER);
        setupOverlay();

        int[][] emptyBoard = new int[4][4];
        updateDisplay(false, emptyBoard);
        addKeyListener(this);
        setFocusable(true);
        setVisible(true);
        requestFocusInWindow();
    }

    private void setupOverlay() {
        overlayPanel = new JPanel();
        overlayPanel.setLayout(new BoxLayout(overlayPanel, BoxLayout.Y_AXIS));
        overlayPanel.setBackground(new Color(73, 47, 96, 205));
        overlayPanel.setBorder(new EmptyBorder(90, 50, 90, 50));

        overlayMessageLabel = new JLabel("", SwingConstants.CENTER);
        overlayMessageLabel.setFont(new Font("Arial", Font.BOLD, 40));
        overlayMessageLabel.setForeground(Color.WHITE);
        overlayMessageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton playAgainButton = createRoundedButton("Play Again", new Dimension(220, 62), 24);
        playAgainButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        playAgainButton.addActionListener(e -> restartGame());

        

        overlayPanel.add(Box.createVerticalGlue());
        overlayPanel.add(overlayMessageLabel);
        overlayPanel.add(Box.createVerticalStrut(24));
        overlayPanel.add(playAgainButton);
        overlayPanel.add(Box.createVerticalStrut(12));
        overlayPanel.add(Box.createVerticalGlue());
        overlayPanel.setVisible(false);

        setGlassPane(overlayPanel);
    }

    private JButton createRoundedButton(String text, Dimension size, int fontSize) {
        JButton button = new RoundedButton(text);
        button.setFont(new Font("Arial", Font.BOLD, fontSize));
        button.setPreferredSize(size);
        button.setForeground(Color.WHITE);
        button.setBackground(BUTTON_COLOR);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        return button;
    }

    private void copyBoard(int[][] source, int[][] dest) {
        for (int i = 0; i < 4; i++) {
            dest[i] = source[i].clone();
        }
    }

    private void updateDisplay(boolean animate, int[][] boardBeforeMove) {
        int[][] board = game.getBoard();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (board[i][j] == 0) {
                    labels[i][j].setText("");
                    labels[i][j].setBackground(EMPTY_TILE_COLOR);
                    labels[i][j].setForeground(new Color(214, 203, 232));
                } else {
                    labels[i][j].setFont(getFontForValue(board[i][j]));
                    labels[i][j].setBackground(getTileColor(board[i][j]));
                    labels[i][j].setForeground(getTextColor(board[i][j]));
                    labels[i][j].setText(String.valueOf(board[i][j]));
                }
                labels[i][j].revalidate();
                labels[i][j].repaint();
                if (animate && board[i][j] > boardBeforeMove[i][j] && board[i][j] > 4) {
                    animateTile(labels[i][j]);
                }
            }
        }
        revalidate();
        repaint();
    }

    private void animateTile(JLabel label) {
        Font originalFont = label.getFont();
        Color originalForeground = label.getForeground();
        Font largeFont = new Font(originalFont.getName(), originalFont.getStyle(), 32);
        label.setFont(largeFont);
        javax.swing.Timer timer = new javax.swing.Timer(150, e -> {
            label.setFont(originalFont);
            label.setForeground(originalForeground);
            ((javax.swing.Timer) e.getSource()).stop();
        });
        timer.setRepeats(false);
        timer.start();
    }

    private Font getFontForValue(int value) {
        int size;
        if (value < 10) {
            size = 24;
        } else if (value < 100) {
            size = 22;
        } else if (value < 1000) {
            size = 20;
        } else {
            size = 18;
        }
        return new Font("Arial", Font.BOLD, size);
    }

    private Color getTextColor(int value) {
        return value > 0 ? Color.WHITE : DARK_TEXT;
    }

    private Color getTileColor(int value) {
        if (value <= 0) {
            return EMPTY_TILE_COLOR;
        }

        if (value == 2) {
            return new Color(152, 125, 186);
        }

        if (value == 4) {
            return new Color(182, 128, 214);
        }
        if (value == 8) {
            return new Color(104, 154, 225);
        }
        if (value == 16) {
            return new Color(78, 178, 210);
        }
        if (value == 32) {
            return new Color(62, 186, 155);
        }
        if (value == 64) {
            return new Color(90, 191, 110);
        }
        if (value == 128) {
            return new Color(180, 185, 74);
        }
        if (value == 256) {
            return new Color(222, 164, 66);
        }
        if (value == 512) {
            return new Color(221, 113, 72);
        }
        if (value == 1024) {
            return new Color(204, 76, 103);
        }
        return new Color(176, 63, 140);
    }

    private void animateMove(int[][] boardBeforeMove, int[][] boardAfterMove, Direction direction) {
        animationInProgress = true;
        animationLayer.removeAll();
        boardPanel.doLayout();

        List<TileMotion> motions = buildTileMotions(boardBeforeMove, boardAfterMove, direction);
        for (TileMotion motion : motions) {
            motion.label.setBounds(motion.startBounds);
            animationLayer.add(motion.label);
        }

        updateDisplay(false, boardBeforeMove);
        animationLayer.revalidate();
        animationLayer.repaint();

        final int[] currentFrame = {0};
        Timer timer = new Timer(ANIMATION_DURATION_MS / ANIMATION_FRAMES, e -> {
            currentFrame[0]++;
            float progress = Math.min(1.0f, currentFrame[0] / (float) ANIMATION_FRAMES);
            for (TileMotion motion : motions) {
                motion.update(progress);
            }
            animationLayer.repaint();

            if (progress >= 1.0f) {
                ((Timer) e.getSource()).stop();
                animationLayer.removeAll();
                game.addRandomTile();
                updateDisplay(true, boardAfterMove);
                copyBoard(game.getBoard(), previousBoard);
                animationInProgress = false;

                if (game.checkWin()) {
                    showOverlay("You win!");
                } else if (game.checkLose()) {
                    showOverlay("Game over");
                }
            }
        });
        timer.start();
    }

    private List<TileMotion> buildTileMotions(int[][] boardBeforeMove, int[][] boardAfterMove, Direction direction) {
        List<TileMotion> motions = new ArrayList<>();
        for (int line = 0; line < GRID_SIZE; line++) {
            List<CellValue> sourceTiles = collectLine(boardBeforeMove, direction, line);
            List<CellValue> targetTiles = collectLine(boardAfterMove, direction, line);
            int sourceIndex = 0;

            for (CellValue target : targetTiles) {
                if (sourceIndex + 1 < sourceTiles.size()
                        && sourceTiles.get(sourceIndex).value == sourceTiles.get(sourceIndex + 1).value
                        && sourceTiles.get(sourceIndex).value * 2 == target.value) {
                    motions.add(createMotion(sourceTiles.get(sourceIndex), target.row, target.col));
                    motions.add(createMotion(sourceTiles.get(sourceIndex + 1), target.row, target.col));
                    sourceIndex += 2;
                } else if (sourceIndex < sourceTiles.size()) {
                    motions.add(createMotion(sourceTiles.get(sourceIndex), target.row, target.col));
                    sourceIndex++;
                }
            }
        }
        return motions;
    }

    private List<CellValue> collectLine(int[][] board, Direction direction, int line) {
        List<CellValue> values = new ArrayList<>();
        for (int index = 0; index < GRID_SIZE; index++) {
            int row = switch (direction) {
                case LEFT, RIGHT -> line;
                case UP -> index;
                case DOWN -> GRID_SIZE - 1 - index;
            };
            int col = switch (direction) {
                case LEFT -> index;
                case RIGHT -> GRID_SIZE - 1 - index;
                case UP, DOWN -> line;
            };

            if (board[row][col] != 0) {
                values.add(new CellValue(row, col, board[row][col]));
            }
        }
        return values;
    }

    private TileMotion createMotion(CellValue source, int toRow, int toCol) {
        JLabel movingLabel = new RoundedTileLabel();
        movingLabel.setText(String.valueOf(source.value));
        movingLabel.setFont(getFontForValue(source.value));
        movingLabel.setForeground(getTextColor(source.value));
        movingLabel.setBackground(getTileColor(source.value));
        return new TileMotion(
                movingLabel,
                getCellBounds(source.row, source.col),
                getCellBounds(toRow, toCol));
    }

    private Rectangle getCellBounds(int row, int col) {
        Rectangle bounds = labels[row][col].getBounds();
        Point point = SwingUtilities.convertPoint(labels[row][col].getParent(), bounds.x, bounds.y, animationLayer);
        return new Rectangle(point.x, point.y, bounds.width, bounds.height);
    }

    private void restartGame() {
        game.reset();
        endOverlayShown = false;
        animationInProgress = false;
        animationLayer.removeAll();
        int[][] emptyBoard = new int[4][4];
        copyBoard(game.getBoard(), previousBoard);
        updateDisplay(false, emptyBoard);
        hideOverlay();
        requestFocusInWindow();
    }


    private void showOverlay(String message) {
        overlayMessageLabel.setText(message);
        overlayPanel.setVisible(true);
        endOverlayShown = true;
    }

    private void hideOverlay() {
        overlayPanel.setVisible(false);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }

            GameUI2048 frame = new GameUI2048();
            frame.setVisible(true);
        });
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (endOverlayShown || animationInProgress) {
            return;
        }

        int[][] boardBeforeMove = new int[4][4];
        int[][] currentBoard = game.getBoard();
        for (int i = 0; i < 4; i++) {
            boardBeforeMove[i] = currentBoard[i].clone();
        }

        boolean moved = false;
        Direction direction = null;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W:
            case KeyEvent.VK_UP:
                moved = game.moveUp();
                direction = Direction.UP;
                break;
            case KeyEvent.VK_S:
            case KeyEvent.VK_DOWN:
                moved = game.moveDown();
                direction = Direction.DOWN;
                break;
            case KeyEvent.VK_A:
            case KeyEvent.VK_LEFT:
                moved = game.moveLeft();
                direction = Direction.LEFT;
                break;
            case KeyEvent.VK_D:
            case KeyEvent.VK_RIGHT:
                moved = game.moveRight();
                direction = Direction.RIGHT;
                break;
            default:
                break;
        }

        if (moved && direction != null) {
            int[][] boardAfterMove = new int[4][4];
            copyBoard(game.getBoard(), boardAfterMove);
            Direction moveDirection = direction;
            SwingUtilities.invokeLater(() -> animateMove(boardBeforeMove, boardAfterMove, moveDirection));
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

    private enum Direction {
        UP,
        DOWN,
        LEFT,
        RIGHT
    }

    private static class CellValue {
        private final int row;
        private final int col;
        private final int value;

        private CellValue(int row, int col, int value) {
            this.row = row;
            this.col = col;
            this.value = value;
        }
    }

    private static class TileMotion {
        private final JLabel label;
        private final Rectangle startBounds;
        private final Rectangle endBounds;

        private TileMotion(JLabel label, Rectangle startBounds, Rectangle endBounds) {
            this.label = label;
            this.startBounds = startBounds;
            this.endBounds = endBounds;
        }

        private void update(float progress) {
            int x = Math.round(startBounds.x + (endBounds.x - startBounds.x) * progress);
            int y = Math.round(startBounds.y + (endBounds.y - startBounds.y) * progress);
            label.setBounds(x, y, startBounds.width, startBounds.height);
        }
    }

    private static class RoundedButton extends JButton {
        private RoundedButton(String text) {
            super(text);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getModel().isArmed() ? getBackground().darker() : getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 28, 28);
            g2.dispose();
            super.paintComponent(graphics);
        }

    }

    private static class RoundedTileLabel extends JLabel {
        private static final int ARC = 28;

        private RoundedTileLabel() {
            super("", SwingConstants.CENTER);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARC, ARC);
            g2.dispose();
            super.paintComponent(graphics);
        }
    }
}
