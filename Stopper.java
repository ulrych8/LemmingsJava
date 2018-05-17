import java.util.ArrayList;
import java.awt.Color;
import java.awt.Graphics2D;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;


public class Stopper extends Lemmings implements Affecter{
/*this class is a Lemmings sub class, its job is to force other Lemmings to change direction*/

//==================== ATTRIBUTES ========================
	
	//Stopper images
	private static BufferedImage image0;
	private static BufferedImage image1;
	private static BufferedImage image2;
	private static BufferedImage image3;
	
	//basic lemmings images changed to his color
	private static BufferedImage imageRightStopper;
	private static BufferedImage imageRightStepStopper;
	private static BufferedImage imageLeftStopper;
	private static BufferedImage imageLeftStepStopper;
	
	private static final int STOP_BEGIN_MAX = 20;	//constant to know which state of the blocking starting animation, the lower the faster the animation will be.
	private int iStopBegin;				//counter for starting to block animation
	private static final int STOP_MAX = 80;		//constant to know which state of the blocking animation we are in, the lower the faster the animation will be.
	private int iStop;				//counter for blocking animation
	

//================== CONSTRUCTORS ======================

	public static void loadAssets(){
		//loading Stopper assets
		try{
			image0 = ImageIO.read(new File("lemmings/stopper0.png"));
			image1 = ImageIO.read(new File("lemmings/stopper1.png"));
			image2 = ImageIO.read(new File("lemmings/stopper2.png"));
			image3 = ImageIO.read(new File("lemmings/stopper3.png"));
			
			imageRightStopper = ImageIO.read(new File("lemmings/lemmings1Stopper.png"));
			imageRightStepStopper = ImageIO.read(new File("lemmings/lemmings1stepStopper.png"));
			imageLeftStopper = ImageIO.read(new File("lemmings/lemmings2Stopper.png"));
			imageLeftStepStopper = ImageIO.read(new File("lemmings/lemmings2stepStopper.png"));
			
		}catch(Exception e){e.printStackTrace();}
	}
	
	public Stopper(Lemmings l){
		super(l);
		
		this.height = image0.getHeight();
		this.width = image0.getWidth();
	}

//===================== METHODS =========================
	
	public void move(){
	//move method, describing the way the Stopper moves
		if (!inWorld) return;
		if (!alive) return;
		if(!action){							//if he hasn't started his job
			if (fall()) return;					//and he is on a the ground
			if (haveEnoughPlace()){					//with sufficient space to block others without issues
				this.job = World.STOPPER;
				this.action = true;				//then he starts blocking others
				iStopBegin = STOP_BEGIN_MAX;
				iStop = STOP_MAX;
				affectMap();
				direction = -direction;				//and prepares to change direction when his job is done
			}
			else{							//if he didn't have sufficient place
				if (walk()) return;				//he searches for that place by walking
				if (climbUp()) return;				//climbing up
				if (climbDown()) return;			//or climbing down
				direction = -direction;				//or even changing direction until he finds enough place
			}
			
		}
		else{								//during his job
			if (fall()){						//if he can suddenly fall (example : a bomb)
				posY--;						//he resets the map according to his old position
				resetMap();
				posY++;
				w.changeJob(this,World.WALKER);			//and stops blocking
				return;
			}
			if (iStop == 0) iStop = STOP_MAX;
			else iStop--;
		}
		
	}
	
	public void drawAction(Graphics2D g){
		//drawAction method describes the way the Basher is drawn during his job
		//starting to block animation
		if (iStopBegin>0){		
			g.drawImage(image0,posX-width/2,posY-height,null);
			iStopBegin--;
		}
		//block animation
		else if (iStop<=STOP_MAX/4) g.drawImage(image1,posX-width/2,posY-height,null);
		else if (iStop<=2*STOP_MAX/4) g.drawImage(image3,posX-width/2,posY-height,null);
		else if (iStop<=3*STOP_MAX/4) g.drawImage(image2,posX-width/2,posY-height,null);
		else g.drawImage(image3,posX-width/2,posY-height,null);
	}
	
	public void affectMap(){
		//affectMap method generates the hidden walls
		int xLeft = posX-(width/2);								//left wall position
		int xRight = posX+(width/2);								//right wall position
		int y = posY-height;									//starting point on Y axis
		boolean wallRightCanGo = true;								//boolean to know if ground has been reached
		boolean wallLeftCanGo = true;
		while(wallRightCanGo || wallLeftCanGo) {						//if any of both walls can keep going
			if (wallLeftCanGo && w.getPos(xLeft,y) == World.AIR_CST){					//check if left wall can continue
				w.setMapTypeAtPos(xLeft,y,w.STOPPER_WALL_LEFT_CST);			//and set a wall there
				w.setMapPixelColor(xLeft,y,Color.red);					//and refresh boolean depending on whether it could continue or not
			}
			else wallLeftCanGo = false;
			
			
			if (wallRightCanGo && w.getPos(xRight,y) == World.AIR_CST){					//same as left wall but on right
				w.setMapTypeAtPos(xRight,y,w.STOPPER_WALL_RIGHT_CST);
				w.setMapPixelColor(xRight,y,Color.red);
			}
			else wallRightCanGo = false;
			
			y++;										//update height at which walls generate
		}
	}
	
	public void resetMap(){
		//resetMap method removes generated walls
		int xLeft = posX-(width/2);								//everything works the same as generating the wall
		int xRight = posX+(width/2);
		int y = posY-height;
		boolean wallRightCanGo = true;
		boolean wallLeftCanGo = true;
		while(wallRightCanGo || wallLeftCanGo) {
			if (wallLeftCanGo && w.getPos(xLeft,y) == World.STOPPER_WALL_LEFT_CST){
				w.setMapTypeAtPos(xLeft,y,w.AIR_CST);					//except walls are replaced by the old air there was
				w.setMapPixelColor(xLeft,y,Color.blue);
			}
			else wallLeftCanGo = false;
			
			
			if (wallRightCanGo && w.getPos(xRight,y) == World.STOPPER_WALL_RIGHT_CST){
				w.setMapTypeAtPos(xRight,y,w.AIR_CST);
				w.setMapPixelColor(xRight,y,Color.blue);
			}
			else wallRightCanGo = false;
			
			y++;
		}
	}
	
	public boolean haveEnoughPlace(){
		//haveEnoughPlace method checks if stopper has enough place in this position to block others
		for (int i=posX-(width/2);i<=posX+(width/2);i++){
			for (int j=posY-height;j<=posY;j++){
				if(w.getPos(i,j) != World.AIR_CST) return false;
			}
		}
		return true;
	}
	
	
	public BufferedImage getImageRight(){
		return imageRightStopper;
	}
	public BufferedImage getImageRightStep(){
		return imageRightStepStopper;
	}
	public BufferedImage getImageLeft(){
		return imageLeftStopper;
	}
	public BufferedImage getImageLeftStep(){
		return imageLeftStepStopper;
	}
}
