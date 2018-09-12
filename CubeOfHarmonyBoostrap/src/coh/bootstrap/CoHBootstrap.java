/**
 * 
 */
package coh.bootstrap;

import java.awt.Color;
import java.io.File;

import fr.theshark34.openlauncherlib.LaunchException;
import fr.theshark34.openlauncherlib.external.ClasspathConstructor;
import fr.theshark34.openlauncherlib.external.ExternalLaunchProfile;
import fr.theshark34.openlauncherlib.external.ExternalLauncher;
import fr.theshark34.openlauncherlib.minecraft.util.GameDirGenerator;
import fr.theshark34.openlauncherlib.util.CrashReporter;
import fr.theshark34.openlauncherlib.util.SplashScreen;
import fr.theshark34.openlauncherlib.util.explorer.ExploredDirectory;
import fr.theshark34.openlauncherlib.util.explorer.Explorer;
import fr.theshark34.supdate.BarAPI;
import fr.theshark34.supdate.SUpdate;
import fr.theshark34.supdate.application.integrated.FileDeleter;
import fr.theshark34.swinger.Swinger;
import fr.theshark34.swinger.colored.SColoredBar;

/**
 * @author Thibaut SIMON-FINE (alias Bisougai)
 * 
 * Based on code by Adrien Navratil (alias Litarvan)
 * Link : https://github.com/Litarvan/
 *
 */
public class CoHBootstrap {
	
	private static SplashScreen splash;
	//private static STexturedProgressBar bar;
	private static SColoredBar bar;
	private static Thread barThread;
	
	private static final File COH_B_DIR = new File(GameDirGenerator.createGameDir("Cube_of_Harmony_v3"), "launcher");
	
	private static CrashReporter COH_B_REPORTER = new CrashReporter("Cube_of_Harmony_v3_bootstrap", COH_B_DIR);
	
	public static void main(String[] args) {
		Swinger.setSystemLookNFeel();
		Swinger.setResourcePath("/coh/bootstrap/resources/");
		
		displaySplash();
		try {
			doUpdate();
		} catch (Exception e) {
			COH_B_REPORTER.catchError(e, "Impossible de mettre à jour le launcher Cube of Harmony");
			barThread.interrupt();
		}
		
		try {
			launchLauncher();
		} catch (LaunchException e) {
			COH_B_REPORTER.catchError(e, "Impossible de lancer le launcher Cube of Harmony");
		}
	}
	
	private static void displaySplash() {
		splash = new SplashScreen("Cube of Harmony", Swinger.getResource("splash.png"));
		splash.setLayout(null);
		//bar = new STexturedProgressBar(Swinger.getResource("icon.png"), Swinger.getResource("icon.png"));
		bar = new SColoredBar(Color.WHITE, Color.GREEN);
		bar.setBounds(10, 10, 380, 80);
		splash.add(bar);
		splash.setVisible(true);
	}
	
	private static void doUpdate() throws Exception {
		//String url = "http://127.0.0.1/CubeOfHarmonyBootstrap/";
		String url = "http://bootstrap.cubeofharmony.fr/";
		SUpdate su = new SUpdate(url, COH_B_DIR);
		su.addApplication(new FileDeleter());
		su.getServerRequester().setRewriteEnabled(true);
		
		barThread = new Thread() {
			@Override
			public void run() {
				while(!this.isInterrupted()) {
					bar.setValue((int) (BarAPI.getNumberOfTotalDownloadedBytes() / 1000));
					bar.setMaximum((int) (BarAPI.getNumberOfTotalBytesToDownload() / 1000));
				}
			}
		};
		barThread.start();
		
		su.start();
		barThread.interrupt();
	}
	
	private static void launchLauncher() throws LaunchException {
		ClasspathConstructor constructor = new ClasspathConstructor();
		ExploredDirectory gameDir = Explorer.dir(COH_B_DIR);
		constructor.add(gameDir.sub("libs").allRecursive().files().match("^(.*\\.((jar)$))*$"));
		constructor.add(gameDir.get("launcher.jar"));
		
		ExternalLaunchProfile profile = new ExternalLaunchProfile("coh.launcher.LauncherFrame", constructor.make());
		ExternalLauncher launcher = new ExternalLauncher(profile);
		
		Process p = launcher.launch();
		splash.setVisible(false);
		
		try {
			p.waitFor();
		} catch (InterruptedException e) {
			//
		}
		
		System.exit(0);
	}
}
