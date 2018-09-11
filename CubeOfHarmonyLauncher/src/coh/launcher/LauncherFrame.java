/**
 * 
 */
package coh.launcher;

import java.awt.HeadlessException;

import javax.swing.JFrame;

import coh.launcher.resources.Resources;
import fr.theshark34.swinger.Swinger;
import fr.theshark34.swinger.util.WindowMover;

/**
 * @author Thibaut SIMON-FINE (alias Bisougai)
 * 
 * Based on code by Adrien Navratil (alias Litarvan)
 * Link : https://github.com/Litarvan/
 *
 */
public class LauncherFrame extends JFrame {
	private static final long serialVersionUID = 6793456547271921881L;
	private static LauncherFrame instance;
	private LauncherPanel panel;

	/**
	 * @throws HeadlessException
	 */
	public LauncherFrame() throws HeadlessException {
		this.setTitle("Launcher Cube of Harmony V3");
		this.setSize(600, 650);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
		this.setUndecorated(true);
		this.setIconImage(Swinger.getResource(Resources.APPICON_IMG));
		
		panel = new LauncherPanel();
		this.setContentPane(panel);
		
		WindowMover mover = new WindowMover(this);
		this.addMouseListener(mover);
		this.addMouseMotionListener(mover);
		
		this.setVisible(true);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Swinger.setSystemLookNFeel();
		Swinger.setResourcePath("/coh/launcher/resources/");
		
		Launcher.COH_CRASH_DIR.mkdirs();
		
		instance = new LauncherFrame();
	}
	
	public static LauncherFrame getInstance() {
		return instance;
	}
	
	public LauncherPanel getPanel() {
		return panel;
	}
}
