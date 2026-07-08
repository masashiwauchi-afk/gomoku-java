import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

public class Gomoku extends JFrame {
    private static final int BOARD_SIZE = 15;
    private static final int CELL_SIZE = 40;
    private static final int MARGIN = 40;
    private static final int BOARD_PX = MARGIN * 2 + CELL_SIZE * (BOARD_SIZE - 1);

    private int[][] board = new int[BOARD_SIZE][BOARD_SIZE]; // 0=empty, 1=black, 2=white
    private int currentPlayer = 1;
    private boolean gameOver = false;
    private int[] lastMove = null;
    private String statusMessage = "黒番の手番です";

    private BoardPanel boardPanel;
    private JLabel statusLabel;
    private JButton newGameButton;

    private CardLayout cardLayout;
    private JPanel rootPanel;

    public Gomoku() {
        setTitle("五目並べ");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        cardLayout = new CardLayout();
        rootPanel = new JPanel(cardLayout);

        rootPanel.add(new TitlePanel(), "title");

        // Game panel placeholder — built lazily on first start
        rootPanel.add(new JPanel(), "game");

        add(rootPanel);
        cardLayout.show(rootPanel, "title");

        setSize(BOARD_PX, BOARD_PX + 80);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void showGame() {
        JPanel gamePanel = buildGamePanel();
        rootPanel.remove(1);
        rootPanel.add(gamePanel, "game", 1);
        rootPanel.revalidate();
        cardLayout.show(rootPanel, "game");
        pack();
        setLocationRelativeTo(null);
    }

    // ── Title Screen ────────────────────────────────────────────────────────────
    class TitlePanel extends JPanel {

        TitlePanel() {
            setBackground(new Color(18, 12, 6));
            setLayout(new GridBagLayout());
            buildContent();
        }

        private void buildContent() {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0; gbc.gridy = GridBagConstraints.RELATIVE;
            gbc.insets = new Insets(0, 0, 0, 0);
            gbc.anchor = GridBagConstraints.CENTER;

            // Title label
            JLabel title = new JLabel("五目並べ");
            title.setFont(new Font("Serif", Font.BOLD, 80));
            title.setForeground(new Color(230, 195, 130));
            gbc.insets = new Insets(40, 0, 8, 0);
            add(title, gbc);

            // Subtitle
            JLabel sub = new JLabel("GOMOKU");
            sub.setFont(new Font("Serif", Font.ITALIC, 22));
            sub.setForeground(new Color(160, 130, 80));
            gbc.insets = new Insets(0, 0, 50, 0);
            add(sub, gbc);

            // Start button
            JButton startBtn = makeButton("ゲーム開始", new Color(55, 38, 14), new Color(230, 195, 130));
            startBtn.addActionListener(e -> {
                resetGame();
                showGame();
            });
            gbc.insets = new Insets(0, 0, 16, 0);
            add(startBtn, gbc);

            // Quit button
            JButton quitBtn = makeButton("終　　了", new Color(35, 20, 8), new Color(180, 140, 90));
            quitBtn.addActionListener(e -> System.exit(0));
            gbc.insets = new Insets(0, 0, 60, 0);
            add(quitBtn, gbc);
        }

        private JButton makeButton(String text, Color bg, Color fg) {
            JButton btn = new JButton(text);
            btn.setFont(new Font("Serif", Font.BOLD, 20));
            btn.setForeground(fg);
            btn.setBackground(bg);
            btn.setPreferredSize(new Dimension(220, 52));
            btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(120, 90, 40), 1),
                BorderFactory.createEmptyBorder(8, 32, 8, 32)
            ));
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { btn.setBackground(bg.brighter()); }
                public void mouseExited(MouseEvent e)  { btn.setBackground(bg); }
            });
            return btn;
        }

    }

    // ── Game UI ─────────────────────────────────────────────────────────────────
    private JPanel buildGamePanel() {  // returns JPanel for CardLayout
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(new Color(18, 12, 6));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(18, 12, 6));
        header.setBorder(BorderFactory.createEmptyBorder(16, 20, 8, 20));

        JLabel titleLabel = new JLabel("五目並べ");
        titleLabel.setFont(new Font("Serif", Font.BOLD, 28));
        titleLabel.setForeground(new Color(230, 195, 130));

        statusLabel = new JLabel(statusMessage, SwingConstants.RIGHT);
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        statusLabel.setForeground(new Color(200, 185, 160));

        header.add(titleLabel, BorderLayout.WEST);
        header.add(statusLabel, BorderLayout.EAST);

        // Board
        boardPanel = new BoardPanel();
        boardPanel.setPreferredSize(new Dimension(BOARD_PX, BOARD_PX));
        boardPanel.setBackground(new Color(18, 12, 6));

        JPanel boardWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        boardWrapper.setBackground(new Color(18, 12, 6));
        boardWrapper.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        boardWrapper.add(boardPanel);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setBackground(new Color(18, 12, 6));
        footer.setBorder(BorderFactory.createEmptyBorder(8, 20, 16, 20));

        newGameButton = new JButton("新しいゲーム");
        newGameButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        newGameButton.setForeground(new Color(230, 195, 130));
        newGameButton.setBackground(new Color(45, 30, 12));
        newGameButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(120, 90, 40), 1),
            BorderFactory.createEmptyBorder(8, 24, 8, 24)
        ));
        newGameButton.setFocusPainted(false);
        newGameButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        newGameButton.addActionListener(e -> resetGame());
        newGameButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                newGameButton.setBackground(new Color(70, 48, 18));
            }
            public void mouseExited(MouseEvent e) {
                newGameButton.setBackground(new Color(45, 30, 12));
            }
        });

        footer.add(newGameButton);

        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(boardWrapper, BorderLayout.CENTER);
        mainPanel.add(footer, BorderLayout.SOUTH);

        return mainPanel;
    }

    private void resetGame() {
        board = new int[BOARD_SIZE][BOARD_SIZE];
        currentPlayer = 1;
        gameOver = false;
        lastMove = null;
        statusMessage = "黒番の手番です";
        if (statusLabel != null) statusLabel.setText(statusMessage);
        if (boardPanel != null) boardPanel.repaint();
    }

    private boolean checkWin(int row, int col, int player) {
        int[][] dirs = {{1,0},{0,1},{1,1},{1,-1}};
        for (int[] d : dirs) {
            int count = 1;
            for (int sign : new int[]{1, -1}) {
                int r = row + sign * d[0];
                int c = col + sign * d[1];
                while (r >= 0 && r < BOARD_SIZE && c >= 0 && c < BOARD_SIZE && board[r][c] == player) {
                    count++;
                    r += sign * d[0];
                    c += sign * d[1];
                }
            }
            if (count >= 5) return true;
        }
        return false;
    }

    class BoardPanel extends JPanel {
        private BufferedImage boardCache;

        BoardPanel() {
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    if (gameOver) return;
                    int col = Math.round((float)(e.getX() - MARGIN) / CELL_SIZE);
                    int row = Math.round((float)(e.getY() - MARGIN) / CELL_SIZE);
                    if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE) return;
                    if (board[row][col] != 0) return;

                    board[row][col] = currentPlayer;
                    lastMove = new int[]{row, col};

                    if (checkWin(row, col, currentPlayer)) {
                        gameOver = true;
                        String winner = (currentPlayer == 1) ? "黒" : "白";
                        statusMessage = winner + "の勝ち！🎉";
                        statusLabel.setText(statusMessage);
                    } else {
                        currentPlayer = (currentPlayer == 1) ? 2 : 1;
                        statusMessage = (currentPlayer == 1) ? "黒番の手番です" : "白番の手番です";
                        statusLabel.setText(statusMessage);
                    }
                    boardCache = null;
                    repaint();
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                int lastHoverRow = -1, lastHoverCol = -1;
                public void mouseMoved(MouseEvent e) {
                    if (gameOver) return;
                    int col = Math.round((float)(e.getX() - MARGIN) / CELL_SIZE);
                    int row = Math.round((float)(e.getY() - MARGIN) / CELL_SIZE);
                    if (row != lastHoverRow || col != lastHoverCol) {
                        lastHoverRow = row;
                        lastHoverCol = col;
                        repaint();
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            // Board background
            drawBoard(g2);
            // Pieces
            drawPieces(g2);
        }

        private void drawBoard(Graphics2D g2) {
            int boardLeft = MARGIN;
            int boardTop = MARGIN;
            int boardRight = MARGIN + CELL_SIZE * (BOARD_SIZE - 1);
            int boardBottom = MARGIN + CELL_SIZE * (BOARD_SIZE - 1);

            // Wood texture background
            GradientPaint woodGrad = new GradientPaint(
                boardLeft, boardTop, new Color(195, 145, 70),
                boardRight, boardBottom, new Color(170, 118, 45)
            );
            g2.setPaint(woodGrad);
            g2.fillRoundRect(boardLeft - 18, boardTop - 18,
                CELL_SIZE * (BOARD_SIZE - 1) + 36, CELL_SIZE * (BOARD_SIZE - 1) + 36, 12, 12);

            // Subtle wood grain
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.07f));
            g2.setColor(new Color(120, 70, 10));
            for (int i = 0; i < 18; i++) {
                int y = boardTop - 18 + i * 18;
                g2.drawLine(boardLeft - 18, y, boardRight + 18, y + 8);
            }
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

            // Board shadow
            g2.setColor(new Color(0,0,0,60));
            g2.fillRoundRect(boardLeft - 16, boardTop - 14,
                CELL_SIZE * (BOARD_SIZE - 1) + 36, CELL_SIZE * (BOARD_SIZE - 1) + 36, 14, 14);
            // Redraw board on top
            g2.setPaint(woodGrad);
            g2.fillRoundRect(boardLeft - 18, boardTop - 18,
                CELL_SIZE * (BOARD_SIZE - 1) + 36, CELL_SIZE * (BOARD_SIZE - 1) + 36, 12, 12);

            // Grid lines
            g2.setStroke(new BasicStroke(0.8f));
            g2.setColor(new Color(100, 68, 22, 180));
            for (int i = 0; i < BOARD_SIZE; i++) {
                int x = MARGIN + i * CELL_SIZE;
                int y = MARGIN + i * CELL_SIZE;
                g2.drawLine(x, MARGIN, x, MARGIN + CELL_SIZE * (BOARD_SIZE - 1));
                g2.drawLine(MARGIN, y, MARGIN + CELL_SIZE * (BOARD_SIZE - 1), y);
            }

            // Star points
            int[] starPoints = {3, 7, 11};
            g2.setColor(new Color(90, 55, 15));
            for (int sr : starPoints) {
                for (int sc : starPoints) {
                    int px = MARGIN + sc * CELL_SIZE;
                    int py = MARGIN + sr * CELL_SIZE;
                    g2.fillOval(px - 4, py - 4, 8, 8);
                }
            }
            // Center point
            g2.fillOval(MARGIN + 7 * CELL_SIZE - 4, MARGIN + 7 * CELL_SIZE - 4, 8, 8);
        }

        private void drawPieces(Graphics2D g2) {
            for (int row = 0; row < BOARD_SIZE; row++) {
                for (int col = 0; col < BOARD_SIZE; col++) {
                    if (board[row][col] != 0) {
                        drawPiece(g2, row, col, board[row][col],
                            lastMove != null && lastMove[0] == row && lastMove[1] == col);
                    }
                }
            }
        }

        private void drawPiece(Graphics2D g2, int row, int col, int player, boolean isLast) {
            int cx = MARGIN + col * CELL_SIZE;
            int cy = MARGIN + row * CELL_SIZE;
            int r = (int)(CELL_SIZE * 0.44);

            // Drop shadow
            g2.setColor(new Color(0, 0, 0, 80));
            g2.fillOval(cx - r + 2, cy - r + 3, r * 2, r * 2);

            if (player == 1) {
                // Black piece with highlight
                RadialGradientPaint blackGrad = new RadialGradientPaint(
                    cx - r/3f, cy - r/3f, r * 1.2f,
                    new float[]{0f, 0.45f, 1f},
                    new Color[]{new Color(90, 90, 90), new Color(20, 20, 20), new Color(5, 5, 5)}
                );
                g2.setPaint(blackGrad);
            } else {
                // White piece with highlight
                RadialGradientPaint whiteGrad = new RadialGradientPaint(
                    cx - r/3f, cy - r/3f, r * 1.2f,
                    new float[]{0f, 0.5f, 1f},
                    new Color[]{Color.WHITE, new Color(220, 220, 215), new Color(180, 175, 165)}
                );
                g2.setPaint(whiteGrad);
            }
            g2.fillOval(cx - r, cy - r, r * 2, r * 2);

            // Last move indicator
            if (isLast) {
                g2.setColor(player == 1 ? new Color(255, 80, 80) : new Color(200, 60, 60));
                g2.setStroke(new BasicStroke(1.5f));
                int ir = r / 3;
                g2.drawOval(cx - ir, cy - ir, ir * 2, ir * 2);
            }
        }
    }

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        SwingUtilities.invokeLater(Gomoku::new);
    }
}
