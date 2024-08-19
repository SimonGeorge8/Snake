import java.awt.*;           
import javax.swing.*;        
import javax.swing.Timer;
import java.util.*;          
import java.awt.event.*;     
/**
 * Main class to start the Snake game.
 */
public class Main {
    /**
     * The main method to launch the game.
     *
     * @param args Command line arguments (not used).
     * @throws InterruptedException if the thread is interrupted while waiting.
     */
    public static void main(String[] args) throws InterruptedException {
        // Initialize the game with a specific height and width
        Game.initialize(15, 25);
    }
}

/**
 * The Game class manages the overall game mechanics and UI.
 */
class Game extends JFrame {
    static int height;
    static int width;

    // Direction vectors for snake movement
    private Point UP = new Point(0, -1);
    private Point DOWN = new Point(0, 1);
    private Point RIGHT = new Point(1, 0);
    private Point LEFT = new Point(-1, 0);

    JTextArea textArea;       // Text area to display game state
    private boolean gameStarted = false; // Game start flag
    Snake game_snake;         // The snake object
    private Timer gameTimer;          // Timer for game loop
     Apple apple;      // The apple object (not fully integrated in the code)

    // Initial snake position
    Point[] start_snake = new Point[]{
        new Point(5, 5),
        new Point(6, 5),
        new Point(7, 5),
        new Point(8, 5),
    };

    /**
     * Constructor for the Game class.
     *
     * @param height The height of the game board.
     * @param width The width of the game board.
     */
    public Game(int height, int width) {
        Game.height = height - 2;
        Game.width = width;
        game_snake = new Snake(start_snake, RIGHT);
        apple = new Apple(height, width);

        setTitle("Snake Game");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Setup the text area for game display
        textArea = new JTextArea();
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        textArea.setEditable(false);
        textArea.setBackground(Color.BLACK);
        textArea.setForeground(Color.GREEN);
        textArea.setMargin(new Insets(10, 10, 10, 10));

        add(textArea);

        // Initial key listener to start the game
        addKeyListener(new StartGameKeyListener());

        setFocusable(true);
    }

    /**
     * Starts the game and sets up the game loop.
     */
    private void startGame() {
        textArea.setText("Starting the game...\n");

        // Timer for game loop with 200 ms delay
        gameTimer = new Timer(200, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                game_snake.take_step();
                Render();

                // Check if the snake is still in the game area
                if (!game_snake.inGame(height, width)) {
                    gameTimer.stop();
                    displayEndScreen();
                    removeKeyListener(getKeyListeners()[0]);
                    addKeyListener(new EndGameKeyListener());
                
                } else if(apple.getPosition().equals(game_snake.get_head())) {
                    game_snake.eat();
                    apple.placeNewApple(height, width);

                }
            }
        });
        gameTimer.start();

        // Replace initial key listener with snake control listener
        removeKeyListener(getKeyListeners()[0]);
        addKeyListener(new SnakeControlKeyListener());
    }

    /**
     * Initializes the game and displays the game window.
     *
     * @param height The height of the game board.
     * @param width The width of the game board.
     * @throws InterruptedException if the thread is interrupted while waiting.
     */
    public static void initialize(int height, int width) throws InterruptedException {
        SwingUtilities.invokeLater(() -> {
            Game game = new Game(height, width);
            game.setVisible(true);
            SwingUtilities.invokeLater(game::displayStartScreen);
        });
    }

    /**
     * Generates the initial game board matrix.
     *
     * @return A 2D array representing the game board.
     */
    public String[][] board_matrix() {

        String[][] Matrix = new String[height][width];
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                Matrix[j][i] = " ";
            }
        }
        for (int i = 0; i < width; i++) {
            Matrix[0][i] = "-";
            Matrix[height - 1][i] = "-";
        }
        for (int i = 0; i < height; i++) {
            Matrix[i][0] = "|";
            Matrix[i][width - 1] = "|";
        }
        Matrix[0][0] = "+";
        Matrix[0][width - 1] = "+";
        Matrix[height - 1][0] = "+";
        Matrix[height - 1][width - 1] = "+";
        return Matrix;
    }

    /**
     * Renders the game board and snake on the screen.
     */
    public void Render() {
        // Create the game board matrix
        String[][] Matrix = board_matrix();
    
        // Place the snake's body on the matrix
        for (int i = 0; i < game_snake.get_size(); i++) {
            Point p = game_snake.body[i];
    
            // Check if the snake's position is within the bounds before placing it
            if (p.x >= 0 && p.x < width && p.y >= 0 && p.y < height) {
                Matrix[p.y][p.x] = (i == game_snake.get_size() - 1) ? "X" : "O";
            } else {
                // If the snake is out of bounds, we should stop rendering to prevent a crash
                System.out.println("Snake out of bounds: (" + p.x + ", " + p.y + ")");
                gameTimer.stop();
                displayEndScreen();
                removeKeyListener(getKeyListeners()[0]);
                addKeyListener(new EndGameKeyListener());
            }
        }
    
        // Place the apple on the matrix
        Point applePos = apple.getPosition();
        if (applePos.x >= 0 && applePos.x < width && applePos.y >= 0 && applePos.y < height) {
            Matrix[applePos.y][applePos.x] = "*";
        }
    
        // Calculate text area dimensions in characters
        FontMetrics fm = textArea.getFontMetrics(textArea.getFont());
        int charWidth = fm.charWidth('m');
        int charHeight = fm.getHeight();
        int textWidth = (textArea.getWidth() - textArea.getInsets().left - textArea.getInsets().right) / charWidth;
        int textHeight = (textArea.getHeight() - textArea.getInsets().top - textArea.getInsets().bottom) / charHeight;
    
        StringBuilder sb = new StringBuilder();
        String line = "*".repeat(textWidth);
        String scoreText = "Score: " + game_snake.counter;
        int scorePadding = Math.max(0, textWidth - scoreText.length() - 2); // Subtract 2 for the border
    
        sb.append("*").append(" ".repeat(scorePadding)).append(scoreText).append(" *").append("\n");
    
       
    
        // Top border
        sb.append(line).append("\n");
    
        // Padding for centering the game board
        int topPadding = (textHeight - height) / 2;
        for (int i = 0; i < topPadding; i++) {
            sb.append("*").append(" ".repeat(textWidth - 2)).append("*\n");
        }
    
        // Render the game board
        for (int j = 0; j < height; j++) {
            sb.append("*");
            int leftPadding = (textWidth - width) / 2;
            sb.append(" ".repeat(leftPadding));
            for (int i = 0; i < width; i++) {
                sb.append(Matrix[j][i]);
            }
            sb.append(" ".repeat(Math.max(0, textWidth - width - leftPadding - 2)));
            sb.append("*\n");
        }
    
        // Bottom padding
        for (int i = 0; i < textHeight - height - topPadding - 1; i++) {
            sb.append("*").append(" ".repeat(textWidth - 2)).append("*\n");
        }
    
        // Bottom border
        sb.append(line);
    
        // Display the score at the bottom
        sb.append("\nScore: ").append(game_snake.counter);
    
        // Update the text area with the game board
        textArea.setText(sb.toString());
    }
    
    /**
     * Displays the start screen with the title and instructions.
     */
    public void displayStartScreen() {
        SwingUtilities.invokeLater(() -> {
            if (textArea.getWidth() == 0 || textArea.getHeight() == 0) {
                SwingUtilities.invokeLater(this::displayStartScreen);
                return;
            }

            FontMetrics fm = textArea.getFontMetrics(textArea.getFont());
            int charWidth = fm.charWidth('m');
            int charHeight = fm.getHeight();

            int width = (textArea.getWidth() - textArea.getInsets().left - textArea.getInsets().right) / charWidth;
            int height = (textArea.getHeight() - textArea.getInsets().top - textArea.getInsets().bottom) / charHeight;

            StringBuilder sb = new StringBuilder();
            String line = "*".repeat(width);

            sb.append(line).append("\n");

            String[] snakeArt = {
                "   _____             _        ",
                "  /  ___|           | |       ",
                "  \\ --. _ __   __ _| | _____ ",
                "   --. \\ '_ \\ / _ | |/ / _ \\",
                "  /\\__/ / | | | (_| |   <  __/",
                "  \\____/|_| |_|\\__,_|_|\\_\\___|"
            };

            int contentHeight = snakeArt.length + 5; // Snake art + extra lines
            int topPadding = (height - contentHeight) / 2;

            for (int i = 0; i < topPadding; i++) {
                sb.append("*").append(" ".repeat(width - 2)).append("*\n");
            }

            for (String artLine : snakeArt) {
                int padding = (width - artLine.length()) / 2;
                sb.append("*").append(" ".repeat(padding)).append(artLine)
                  .append(" ".repeat(Math.max(0, width - artLine.length() - padding - 2))).append("*\n");
            }

            sb.append("*").append(" ".repeat(width - 2)).append("*\n");

            String pressAnyKey = "Press any key to continue...";
            int keyPadding = (width - pressAnyKey.length()) / 2;
            sb.append("*").append(" ".repeat(keyPadding)).append(pressAnyKey)
              .append(" ".repeat(Math.max(0, width - pressAnyKey.length() - keyPadding - 2))).append("*\n");

            String createdBy = "Created by Simon Abadi";
            int createdPadding = (width - createdBy.length()) / 2;
            sb.append("*").append(" ".repeat(createdPadding)).append(createdBy)
              .append(" ".repeat(Math.max(0, width - createdBy.length() - createdPadding - 2))).append("*\n");

            for (int i = 0; i < height - topPadding - contentHeight - 1; i++) {
                sb.append("*").append(" ".repeat(width - 2)).append("*\n");
            }

            sb.append(line);

            textArea.setText(sb.toString());
        });
    }
    /**
     * Displays the end screen with the title and instructions.
     */
    public void displayEndScreen() {
        SwingUtilities.invokeLater(() -> {
            if (textArea.getWidth() == 0 || textArea.getHeight() == 0) {
                SwingUtilities.invokeLater(this::displayEndScreen);
                return;
            }
    
            FontMetrics fm = textArea.getFontMetrics(textArea.getFont());
            int charWidth = fm.charWidth('m');
            int charHeight = fm.getHeight();
    
            int width = (textArea.getWidth() - textArea.getInsets().left - textArea.getInsets().right) / charWidth;
            int height = (textArea.getHeight() - textArea.getInsets().top - textArea.getInsets().bottom) / charHeight;
    
            StringBuilder sb = new StringBuilder();
            String line = "*".repeat(width);
    
            sb.append(line).append("\n");
    
            String[] gameOverArt = {
                "   _____                         ____                 ",
                "  / ____|                       / __ \\                ",
                " | |  __  __ _ _ __ ___   ___  | |  | |_   _____ _ __ ",
                " | | |_ |/ _ | '_  _ \\ / _ \\ | |  | \\ \\ / / _ \\ '__|",
                " | |__| | (_| | | | | | |  __/ | |__| |\\ V /  __/ |   ",
                "  \\_____|\\__,_|_| |_| |_|\\___|  \\____/  \\_/ \\___|_|   "
            };
    
            int contentHeight = gameOverArt.length + 5; // Game over art + extra lines
            int topPadding = (height - contentHeight) / 2;
    
            for (int i = 0; i < topPadding; i++) {
                sb.append("*").append(" ".repeat(width - 2)).append("*\n");
            }
    
            for (String artLine : gameOverArt) {
                int padding = (width - artLine.length()) / 2;
                sb.append("*").append(" ".repeat(padding)).append(artLine)
                  .append(" ".repeat(Math.max(0, width - artLine.length() - padding - 2))).append("*\n");
            }
    
            sb.append("*").append(" ".repeat(width - 2)).append("*\n");
    
            String pressAnyKey = "Press q to exit.";
            int keyPadding = (width - pressAnyKey.length()) / 2;
            sb.append("*").append(" ".repeat(keyPadding)).append(pressAnyKey)
              .append(" ".repeat(Math.max(0, width - pressAnyKey.length() - keyPadding - 2))).append("*\n");
    
            String createdBy = "Press Enter to try again...";
            int createdPadding = (width - createdBy.length()) / 2;
            sb.append("*").append(" ".repeat(createdPadding)).append(createdBy)
              .append(" ".repeat(Math.max(0, width - createdBy.length() - createdPadding - 2))).append("*\n");
    
            for (int i = 0; i < height - topPadding - contentHeight - 1; i++) {
                sb.append("*").append(" ".repeat(width - 2)).append("*\n");
            }
    
            sb.append(line);
    
            textArea.setText(sb.toString());
        });
    }
    /**
     * Key listener for starting the game.
     */
    private class EndGameKeyListener extends KeyAdapter {
        /**
         * Handles key press events to start the game.
         *
         * @param e The KeyEvent triggered by a key press.
         */
        @Override
        public void keyPressed(KeyEvent e) {
            if(e.getKeyCode() == KeyEvent.VK_Q){            
                System.exit(0);
            } else if(e.getKeyCode() == KeyEvent.VK_ENTER){
                    dispose();
                try {
                    initialize(height, width);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                startGame();
            }
        }
    }

    /**
     * Key listener for starting the game.
     */
    private class StartGameKeyListener extends KeyAdapter {
        /**
         * Handles key press events to start the game.
         *
         * @param e The KeyEvent triggered by a key press.
         */
        @Override
        public void keyPressed(KeyEvent e) {
            if (!gameStarted) {
                gameStarted = true;
                startGame();
            }
        }
    }

    /**
     * Key listener for controlling the snake.
     */
    private class SnakeControlKeyListener extends KeyAdapter {
        /**
         * Handles key press events to control the snake's direction.
         *
         * @param e The KeyEvent triggered by a key press.
         */
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_W:
                    if (!game_snake.direction.equals(DOWN)) {
                        game_snake.set_direction(UP);
                    }
                    break;
                case KeyEvent.VK_S:
                    if (!game_snake.direction.equals(UP)) {
                        game_snake.set_direction(DOWN);
                    }
                    break;
                case KeyEvent.VK_A:
                    if (!game_snake.direction.equals(RIGHT)) {
                        game_snake.set_direction(LEFT);
                    }
                    break;
                case KeyEvent.VK_D:
                    if (!game_snake.direction.equals(LEFT)) {
                        game_snake.set_direction(RIGHT);
                    }
                    break;
                default:
                    break;
            }
        }
    }
}

/**
 * The Snake class represents the snake in the game.
 */
class Snake {
    Point[] body;         // Array of Points representing the snake's body segments
    Point direction;      // The current direction of the snake
    int counter;          // The length of the snake
    boolean ingame = true; // Flag to check if the snake is in-game

    /**
     * Constructor for the Snake class.
     *
     * @param body The initial body of the snake.
     * @param direction The initial direction of the snake.
     */
    public Snake(Point[] body, Point direction) {
        this.counter = body.length;
        this.body = body;
        this.direction = direction;
    }

    /**
     * Gets the current size of the snake.
     *
     * @return The size of the snake.
     */
    public int get_size() {
        return counter;
    }

    /**
     * Increases the size of the snake by one segment when it eats an apple.
     */
    public void eat() {
        int n = body.length;
        Point[] resize = new Point[n + 1];
        for (int i = 0; i < resize.length - 1; i++) {
            resize[i] = body[i];
        }

        // Calculate the position for the new tail segment
        Point tail = new Point(body[n - 1].x, body[n - 1].y);
        resize[n] = tail;

        // Update the body to the new resized array
        body = resize;
        counter++;
    }

    /**
     * Sets the direction of the snake.
     *
     * @param direction The new direction of the snake.
     */
    public void set_direction(Point direction) {
        this.direction = direction;
    }

    /**
     * Moves the snake one step in the current direction.
     */
    public void take_step() {
        Point head = get_head();
        Point newHead = new Point(head.x + direction.x, head.y + direction.y);

        for (int i = 0; i < body.length - 1; i++) {
            body[i] = body[i + 1];
        }

        body[body.length - 1] = newHead;
    }

    /**
     * Gets the current position of the snake's head.
     *
     * @return The position of the head as a Point.
     */
    public Point get_head() {
        return body[body.length - 1];
    }

    /**
     * Checks if the snake's head is within the playable area.
     *
     * @param height The height of the game board.
     * @param width The width of the game board.
     * @return True if the snake is within the game area, false otherwise.
     */
    public boolean inGame(int height, int width) {
        int x = get_head().x;
        int y = get_head().y;

        // Check if the snake's head is within the playable area
        if (x <= 0 || x >= width || y <= 0 || y >= height) {
            return false;
        } 
        for (int i = 0 ; i < body.length - 1; i++) {
            if (get_head().equals(body[i])){
                return false;
            }
        }

        return true;
    }
}

/**
 * The Apple class represents the apple in the game.
 */
class Apple {
    private Point position;  // The position of the apple

    /**
     * Constructor for the Apple class.
     *
     * @param height The height of the game board.
     * @param width The width of the game board.
     */
    public Apple(int height, int width) {
        // Generate a random position within the field boundaries
        Random rand = new Random();
        int x = rand.nextInt(width - 2) + 1;
        int y = rand.nextInt(height - 2) + 1;
        position = new Point(x, y);
    }

    /**
     * Gets the current position of the apple.
     *
     * @return The position of the apple as a Point.
     */
    public Point getPosition() {
        return position;
    }

    /**
     * Places a new apple at a random position on the game board.
     *
     * @param height The height of the game board.
     * @param width The width of the game board.
     */
    public void placeNewApple(int height, int width) {
        // Generate a new random position for the apple
        Random rand = new Random();
        int x = rand.nextInt(width - 2) + 1;
        int y = rand.nextInt(height - 2) + 1;
        while(x <= 0 || x >= width || y <= 0 || y >= height) {
             x = rand.nextInt(width - 2) + 1;
             y = rand.nextInt(height - 2) + 1;
        } 
        position = new Point(x, y);
    }

}