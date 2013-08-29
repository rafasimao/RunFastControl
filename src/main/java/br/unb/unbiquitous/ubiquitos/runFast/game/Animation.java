package br.unb.unbiquitous.ubiquitos.runFast.game;

import java.awt.Graphics2D;
import java.awt.Image;

import javax.swing.JPanel;

/**
 * Class used to make image animation.
 *
 */
public class Animation {

	//Animation frame time
	private static final int DEFAULT_FRAME_TIME = 100;
	
	private float x,y;
	private int width,height;
	private Image[] image;
	private int time,frameTime,frame;
	
	public Animation(float x, float y){
		this.x = x;
		this.y = y;
		
		load();
	}

	public void load(){
		//LoadManager loader = new LoadManager();
		new LoadManager();
		
		time=frame=0;

		image = LoadManager.getExplosion();
		frameTime = DEFAULT_FRAME_TIME;
		
		this.width = image[0].getWidth(null);
		this.height = image[0].getHeight(null);
	}
	
	/**
	 * Updates the animation based in the time dt.
	 * @param dt
	 */
	public void update(int dt){

		time += dt;

	    int frameSkip;
	    
	    frameSkip = time/frameTime;
	    time = time%frameTime;
		    
	    frame += frameSkip;
		    
	    if(frame >= image.length){
	    	frame = (frame - image.length)%image.length;
	    }
	    
	}
	
	/**
	 * Renders this animation in the graphic received.
	 * @param g
	 * @param cameraX
	 * @param cameraY
	 * @param panel
	 */
	public void render(Graphics2D g, int cameraX, int cameraY, JPanel panel){
		Graphics2D gAnimation = (Graphics2D) g.create();
		gAnimation.translate(getX()+cameraX ,getY()+cameraY);
		gAnimation.drawImage(image[frame], -width/2, -height*3/4, panel);
		gAnimation.dispose();
	}
	
	/**
	 * Verifies if it is touching some Cartesian point.
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean isTouching(float x, float y){
		if((x >= this.x)&&(x <= this.x+width)
				&&(y >= this.y)&&(y <= this.y+height))
			return true;
		return false;
	}

	/**
	 * Verifies if the animation was completed.
	 * @return true if it ended and false otherwise.
	 */
	public boolean isDone(){
		if(frame == image.length-1)
			return true;
		return false;
	}
	
	/**
	 * Restarts the animation.
	 */
	public void resetAnimation(){
		frame = 0;
		time = 0;
	}
	
	/**
	 * @return the x
	 */
	public float getX() {
		return x;
	}

	/**
	 * @return the y
	 */
	public float getY() {
		return y;
	}
	
	/**
	 * @param x the x to set
	 */
	public void setX(float x) {
		this.x = x;
	}

	/**
	 * @param y the y to set
	 */
	public void setY(float y) {
		this.y = y;
	}

	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

}
