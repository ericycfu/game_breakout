import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.text.*;


public class Breakout extends Application {
    public static final String TITLE = "Breakout Game";
    public static final int SIZE = 500;
    public static final int FRAMES_PER_SECOND = 60;
    public static final int MILLISECOND_DELAY = 1000 / FRAMES_PER_SECOND;
    public static final double SECOND_DELAY = 1.0 / FRAMES_PER_SECOND;
    public static final Paint BACKGROUND = Color.AZURE;
    public static final Paint HIGHLIGHT = Color.OLIVEDRAB;    
    public static final String BOUNCER_IMAGE = "ball.gif";
    public static final String PADDLE_IMAGE = "paddle.gif";
    public static int PADDLE_SPEED = 15;
    public static int BOUNCER_SPEED_X = 0;
    public static int BOUNCER_SPEED_Y = 0;
    public static int LEVEL = 1;
    public static int POWERUP_TIME = 0;
    public static boolean hasCollided = false;
    public static boolean canBounce = false;


  
    // some things we need to remember during our game
    private Scene myScene;
    private ImageView myBouncer;
    private ImageView myPaddle;
    private ArrayList<Block> blocks = new ArrayList<Block>();
    private Text myText;
    private int myLives;
    private Text myLivesText;
    
    /**
     * Initialize what will be displayed and how it will be updated.
     */
    @Override
    public void start (Stage stage) {
        // attach scene to the stage and display it
        myScene = setupGame(SIZE, SIZE, BACKGROUND);
        stage.setScene(myScene);
        stage.setTitle(TITLE);
        stage.show();
        // attach "game loop" to timeline to play it
        KeyFrame frame = new KeyFrame(Duration.millis(MILLISECOND_DELAY),
                                      e -> step(SECOND_DELAY));
        Timeline animation = new Timeline();
        animation.setCycleCount(Timeline.INDEFINITE);
        animation.getKeyFrames().add(frame);
        animation.play();
    }

    // Create the game's "scene": what shapes will be in the game and their starting properties
    private Scene setupGame (int width, int height, Paint background) {
        myLives = 3;
    	// create one top level collection to organize the things in the scene
        Group root = new Group();
        // create a place to see the shapes
        Scene scene = new Scene(root, width, height, background);
        // make some shapes and set their properties
        Image image = new Image(getClass().getClassLoader().getResourceAsStream(PADDLE_IMAGE));
        myPaddle = new ImageView(image);
        myPaddle.setX(width/2 - myPaddle.getBoundsInLocal().getWidth()/2);
        myPaddle.setY(height-40);
        myPaddle.setFitWidth(80);
        Image image2 = new Image(getClass().getClassLoader().getResourceAsStream(BOUNCER_IMAGE));
        myBouncer = new ImageView(image2);
        // x and y represent the top left corner, so center it
        myBouncer.setX(myPaddle.getX() + myPaddle.getFitWidth()/2);
        myBouncer.setY(myPaddle.getY() - 12);
        //add bricks
        blocks = createBlocks();
        myText = new Text(50,50,"Press Space to Start");
        myText.setFont(new Font(20));
        myLivesText = new Text(50, SIZE-10, "Lives: " + Integer.toString(myLives));
        myLivesText.setFont(new Font(12));
        // order added to the group is the order in which they are drawn
        root.getChildren().add(myPaddle);
        root.getChildren().add(myBouncer);
        for (ImageView myBlock: blocks) {
        	root.getChildren().add(myBlock);
        }
        root.getChildren().add(myText);
        root.getChildren().add(myLivesText);
        // respond to input
        scene.setOnKeyPressed(e -> handleKeyInput(e.getCode()));
        return scene;
    }

    // Change properties of shapes to animate them 
    private void step (double elapsedTime) {
        // update attributes
    	if (leftRightCollision()) {
    		BOUNCER_SPEED_X *= -1;
    	}
    	if (upCollision()) {
    		BOUNCER_SPEED_Y *= -1;
    	} 
    	if (canBounce) {
    		if (myBouncer.getY()>=SIZE){
    			BOUNCER_SPEED_Y *= -1;
    		}
    	}
    	if (isDead()&& !canBounce) {
    		myLives -= 1;
    		//reset paddle, ball, and labels
    		resetPaddle();
    		resetBouncer();
    		resetLabels();
    	}
    	if (blocks.size() == 0) {
    		winLevel();
    	}
    	if (myLives == 0) {
    		gameOver();
    	}
    	if (LEVEL == 3) {
    		winLevel();
    		myText.setText("You have beat the game!");
    	}
    	// check intersection of paddle and ball
    	if (myPaddle.getBoundsInParent().intersects(myBouncer.getBoundsInParent())) {
    		myBouncer.setY(myBouncer.getY()-5);
            BOUNCER_SPEED_Y *= -1;
            if (POWERUP_TIME != 0) {
            	POWERUP_TIME -= 1;
            }
            if (POWERUP_TIME == 0) {
            	PADDLE_SPEED = 15;
            	myPaddle.setFitWidth(80);
            }
        }

        //check intersection of block and ball
    	if (LEVEL == 1) {
    		hasCollided = false;
	    	Block collision = blockCollision();
	    	if (collision!= null) {
		    	specialBlock(collision);
		    	if(hasCollided == false) {
		    		blocks.remove(collision);
		    		collision.setImage(null);
		    		if(BOUNCER_SPEED_Y < 0)
		    			myBouncer.setY(myBouncer.getY()+5);
		    		else
		    			myBouncer.setY(myBouncer.getY()-5);
		        	BOUNCER_SPEED_Y *= -1;
		    	}
	        }
    	}
    	if (LEVEL == 2) {
    		hasCollided = false;
    		Block collision = blockCollision();
    		if (collision != null) {
    			//if block is part of lowest row
    			if (collision.getRow() == lowestRow()) {
    				//if block is red
    				if (collision.getColor() == "red") {
    			    	specialBlock(collision);
    					if (hasCollided == false) {
					    	blocks.remove(collision);
							collision.setImage(null);
							if(BOUNCER_SPEED_Y < 0)
				    			myBouncer.setY(myBouncer.getY()+5);
				    		else
				    			myBouncer.setY(myBouncer.getY()-5);
				        	BOUNCER_SPEED_Y *= -1;
    					}
    				}
    				else if (collision.getColor() == "blue") {
        				//if block is blue but red still remaining in that row
    					for (Block block : blocks) {
    						if (block.getColor() == "red" && block.getRow() == collision.getRow()){
	    						if(BOUNCER_SPEED_Y < 0)
	        		    			myBouncer.setY(myBouncer.getY()+5);
	        		    		else
	        		    			myBouncer.setY(myBouncer.getY()-5);
	        		        	BOUNCER_SPEED_Y *= -1;
    						}
    					}
        				//if block is blue and no red in that row
    					ArrayList<String> colors = new ArrayList<String>();
    					for (Block block : blocks) {
    						if (block.getRow() == collision.getRow()) {
    							colors.add(block.getColor());
    						}
    					}
						if (!colors.contains("red")) {
					    	specialBlock(collision);
					    	if (hasCollided == false) {
		    					blocks.remove(collision);
		    					collision.setImage(null);
		    					if(BOUNCER_SPEED_Y < 0)
		    		    			myBouncer.setY(myBouncer.getY()+5);
		    		    		else
		    		    			myBouncer.setY(myBouncer.getY()-5);
		    		        	BOUNCER_SPEED_Y *= -1;
					    	}
						}
    				
    				}
    			}
    			//if block is not part of lowest row
    			if(BOUNCER_SPEED_Y < 0)
	    			myBouncer.setY(myBouncer.getY()+15);
	    		else
	    			myBouncer.setY(myBouncer.getY()-15);
	        	BOUNCER_SPEED_Y *= -1;
    		}
    		
    		
    	}
        myBouncer.setX(myBouncer.getX() + BOUNCER_SPEED_X * elapsedTime);
        myBouncer.setY(myBouncer.getY() + BOUNCER_SPEED_Y * elapsedTime);
    }
    //returns true if ball hits the left or right walls
    private boolean leftRightCollision() {
    	return (myBouncer.getX()>=myScene.getWidth() || (myBouncer.getX()<=0));
    }
    private boolean upCollision() {
    	return (myBouncer.getY()<=0);
    }
    private boolean isDead() {
    	return (myBouncer.getY()>=myScene.getHeight());
    }
    
    private void winLevel() {
    	for(Block block: blocks) {
    		block.setImage(null);
    	}
    	blocks.clear();
    	resetPaddle();
    	resetBouncer();
    	myText.setText("You beat the level!");
    	myLivesText.setText(null);
    	LEVEL+= 1;
    	createBlocks();
    	//to move onto next level, doesnt work
    	//myScene = setupGame(SIZE, SIZE, BACKGROUND);
    }
    
    
    
    private void resetLabels() {
    	 myLivesText.setText("Lives: " + Integer.toString(myLives));
    	 myText.setText("Press Space to Start");
    }
    private void resetPaddle() {
    	 myPaddle.setX(SIZE/2 - myPaddle.getBoundsInLocal().getWidth()/2);
         myPaddle.setY(SIZE-40);
    }
    
    private void resetBouncer() {
    	BOUNCER_SPEED_X = 0;
    	BOUNCER_SPEED_Y = 0;
    	myBouncer.setX(myPaddle.getX() + myPaddle.getFitWidth()/2);
        myBouncer.setY(myPaddle.getY() - 12);
    }
    
    private void gameOver() {
    	for(ImageView block: blocks) {
    		block.setImage(null);
    	}
    	blocks.clear();
    	myText.setText("Game Over! Press K to restart");
    	myLivesText.setText(null);
    }
    
    private ArrayList<Block> createBlocks(){
    	switch(LEVEL) {
    	case 1: return levelOneBlocks();
    	case 2: return levelTwoBlocks();
    	default: return levelOneBlocks();
    	}
    	
    }
    
    private ArrayList<Block> levelOneBlocks(){
    	Image image = new Image(getClass().getClassLoader().getResourceAsStream("brick1.gif"));
    	ArrayList<Block> blocks = new ArrayList<Block>();
    	for (int j = 0; j < 3; j++) {
	    	for(int i = 0; i < 10; i++) {
	    		Block block = new Block(image);
	    		block.setX(i*SIZE/10);
	    		block.setY(100-j*11);
	    		block.setFitHeight(10);
	    		block.setFitWidth(SIZE/10-1);
	    		blocks.add(block);
	    	}
    	}
    	return blocks;
    }
    
    private ArrayList<Block> levelTwoBlocks(){
    	ArrayList<Block> blocks = new ArrayList<Block>();
    	for (int j = 1; j <= 3; j++) {
	    	for(int i = 0; i < 10; i++) {
	    		Image image;
	    		Block block;
	    		if (i%2 == 0) {
	    			image = new Image(getClass().getClassLoader().getResourceAsStream("brick1.gif"));
	    			block = new Block(image, "blue", j);
	    		}
	    		else {
	    			image = new Image(getClass().getClassLoader().getResourceAsStream("brick9.gif"));
	    			block = new Block(image, "red", j);
	    		}
	    		block.setX(i*SIZE/10);
	    		block.setY(100-j*11);
	    		block.setFitHeight(10);
	    		block.setFitWidth(SIZE/10-1);
	    		blocks.add(block);
	    	}
    	}
    	return blocks;	
    }
    
    private int lowestRow(){
    	HashSet<Integer> rows = new HashSet<Integer>();
    	for(Block block: blocks) {
    		rows.add(block.getRow());
    	}
    	return Collections.min(rows);
    }
    
    
    private void specialBlock(Block block) {
    	Random rand = new Random();
    	int number = rand.nextInt(30)+1;
    	switch (number) {
    	case 1: myLives += 1;
    		myLivesText.setText("Lives: " + Integer.toString(myLives));
    		break;
    	case 2:	destroyAdjacentBlock(block);
    		break;
    	case 3: givePowerup(block);
    		break;
    	}
    }
    
    private void destroyAdjacentBlock(Block block) {
    	Block deleteBlock= null;
    	for (Block myBlock : blocks) {
    		if((myBlock.getY() == block.getY()) && (myBlock.getX() == (block.getX()+SIZE/10) || (myBlock.getX() == (block.getX()-SIZE/10)))){
				deleteBlock = myBlock;
				hasCollided = true; 
				if(BOUNCER_SPEED_Y < 0)
	    			myBouncer.setY(myBouncer.getY()+5);
	    		else
	    			myBouncer.setY(myBouncer.getY()-5);
	        	BOUNCER_SPEED_Y *= -1;
	        	break;
    		}
    	}
    	if (deleteBlock != null) {
    		blocks.remove(deleteBlock);
    		deleteBlock.setImage(null);
    	}
    	blocks.remove(block);
		block.setImage(null);
    }
    
    private void givePowerup(Block block) {
    	if (POWERUP_TIME == 0) {
	    	Random rand = new Random();
	    	int number = rand.nextInt(2)+1;
	    	switch(number) {
	    	case 1: myPaddle.setFitWidth(160);
	    		break;
	    	case 2: PADDLE_SPEED = 25;
	    		break; 
	    	}
	    	POWERUP_TIME = 10;
    	}
    }
    
    
    
    //returns a block if collides with bouncer
    private Block blockCollision() {
    	for(Block block: blocks) {
    		if (block.getBoundsInParent().intersects(myBouncer.getBoundsInParent())) {
    			return block;
    	    }
    	}
    	return null;
    }
    
    
    // What to do each time a key is pressed
    private void handleKeyInput (KeyCode code) {
        if (code == KeyCode.RIGHT) {
        	if (myPaddle.getX()+myPaddle.getFitWidth()<myScene.getWidth())
        		myPaddle.setX(myPaddle.getX() + PADDLE_SPEED);
        }
        else if (code == KeyCode.LEFT) {
        	if (myPaddle.getX()>=0)
        		myPaddle.setX(myPaddle.getX() - PADDLE_SPEED);
        }
        if (code == KeyCode.SPACE) {
        	Random rand = new Random();
        	if (BOUNCER_SPEED_X == 0 || (BOUNCER_SPEED_Y == 0)) {
        		BOUNCER_SPEED_X = (int) (150*Math.pow(1.1, LEVEL-1) - (rand.nextInt(10)+1));
        		BOUNCER_SPEED_Y = (int) (210*Math.pow(1.1,  LEVEL-1) - (rand.nextInt(10)+1));
        	}
        	myText.setText(null);
        }
        //to reset game, doesn't work
        if (code == KeyCode.K) {
        	//myScene = setupGame(SIZE, SIZE, BACKGROUND);
        	/*myLives = 3;
        	resetPaddle();
        	resetBouncer();
        	resetLabels();
        	blocks = createBlocks();
        	*/
        }
        //cheatcode for (basically) unlimited lives
        if (code == KeyCode.L) {
        	myLives = 10000000;
        	myLivesText.setText("Lives: " + Integer.toString(myLives));
        }
        //cheatcode so ball will bounce off floor
        if (code == KeyCode.P) {
        	canBounce = !canBounce;
        }
    }

    public static void main (String[] args) {
        launch(args);
    }
}
