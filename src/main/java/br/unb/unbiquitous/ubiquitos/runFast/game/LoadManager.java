package br.unb.unbiquitous.ubiquitos.runFast.game;

import java.awt.Image;

import javax.swing.ImageIcon;

/**
 * Loads some used images.
 *
 */
public class LoadManager {

	//Paths
	private static final String EXPLOSION_PATH = "images/cars/explosion/explosion";
	private static final String GENERAL_FORMAT = ".png";
	
	private static Image[] explosion = null;
	
	public LoadManager(){
		if(explosion==null)
			loadExplosion();
	}
	
	/**
	 * Loads the explosion images
	 */
	private void loadExplosion(){
		explosion = new Image[15];
		for(int i=0; i<15; ++i){
			ImageIcon ii = new ImageIcon(getClass().getClassLoader().getResource(EXPLOSION_PATH+(i+1)+GENERAL_FORMAT));
			explosion[i] = ii.getImage();
		}
	}

	/**
	 * @return the explosion
	 */
	public static Image[] getExplosion() {
		return explosion;
	}
	
}
