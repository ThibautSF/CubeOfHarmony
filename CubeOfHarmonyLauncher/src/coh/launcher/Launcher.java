/**
 * 
 */
package coh.launcher;

import java.io.File;
import java.util.Arrays;

import fr.litarvan.openauth.AuthPoints;
import fr.litarvan.openauth.AuthenticationException;
import fr.litarvan.openauth.Authenticator;
import fr.litarvan.openauth.model.AuthAgent;
import fr.litarvan.openauth.model.response.AuthResponse;
import fr.theshark34.openlauncherlib.LaunchException;
import fr.theshark34.openlauncherlib.external.ExternalLaunchProfile;
import fr.theshark34.openlauncherlib.external.ExternalLauncher;
import fr.theshark34.openlauncherlib.minecraft.AuthInfos;
import fr.theshark34.openlauncherlib.minecraft.GameFolder;
import fr.theshark34.openlauncherlib.minecraft.GameInfos;
import fr.theshark34.openlauncherlib.minecraft.GameTweak;
import fr.theshark34.openlauncherlib.minecraft.GameType;
import fr.theshark34.openlauncherlib.minecraft.GameVersion;
import fr.theshark34.openlauncherlib.minecraft.MinecraftLauncher;
import fr.theshark34.openlauncherlib.util.CrashReporter;
import fr.theshark34.openlauncherlib.util.ProcessLogManager;
import fr.theshark34.supdate.BarAPI;
import fr.theshark34.supdate.SUpdate;
import fr.theshark34.supdate.application.integrated.FileDeleter;
import fr.theshark34.swinger.Swinger;

/**
 * @author Thibaut SIMON-FINE (alias Bisougai)
 * 
 * Based on code by Adrien Navratil (alias Litarvan)
 * Link : https://github.com/Litarvan/
 *
 */
public class Launcher {
	public static final GameVersion COH_VERSION = new GameVersion("1.12", GameType.V1_8_HIGHER);
	public static final GameTweak[] TWEAKS = { GameTweak.FORGE }; 
	public static final GameInfos COH_INFOS = new GameInfos("Cube_of_Harmony_v3", COH_VERSION, TWEAKS);
	public static final File COH_DIR = COH_INFOS.getGameDir();
	public static final File COH_PARAMS = new File(Launcher.COH_DIR, "launcher.properties");
	public static final File COH_RAM_SELECTOR = new File(COH_DIR, "cohram.txt");
	public static final File COH_CRASH_DIR = new File(COH_DIR, "coh-crashes");
	//public static final GameFolder COH_FOLDER = new GameFolder("resources/assets", "resources/libraries", "resources/natives", "jar/COH.jar");
	
	private static AuthInfos authInfos;
	private static Thread updateThread;
	
	private static CrashReporter COH_REPORTER = new CrashReporter("Cube_of_Harmony_v3_launcher", COH_CRASH_DIR);
	
	public static void auth(String username, String password) throws AuthenticationException {
		Authenticator auth = new Authenticator(Authenticator.MOJANG_AUTH_URL, AuthPoints.NORMAL_AUTH_POINTS);
		AuthResponse response = auth.authenticate(AuthAgent.MINECRAFT, username, password, "");
		authInfos = new AuthInfos(response.getSelectedProfile().getName(), response.getAccessToken(), response.getSelectedProfile().getId());
	}
	
	public static void update() throws Exception {
		//String url = "http://127.0.0.1/CubeOfHarmony/";
		String url = "http://update.cubeofharmony.fr/";
		SUpdate su = new SUpdate(url, COH_DIR);
		su.addApplication(new FileDeleter());
		su.getServerRequester().setRewriteEnabled(true);
		
		updateThread = new Thread() {
			int val;
			int max;
			
			public void run() {
				while (!this.isInterrupted()) {
					if (BarAPI.getNumberOfFileToDownload() == 0) {
						LauncherFrame.getInstance().getPanel().setInfoText("Vérification des fichiers");
						continue;
					}
					
					//val = (int) BarAPI.getNumberOfTotalDownloadedBytes() / 1000;
					//max = (int) BarAPI.getNumberOfTotalBytesToDownload() / 1000;
					
					val = BarAPI.getNumberOfDownloadedFiles();
					max = BarAPI.getNumberOfFileToDownload();
					
					LauncherFrame.getInstance().getPanel().getProgressBar().setMaximum(max);
					LauncherFrame.getInstance().getPanel().getProgressBar().setValue(val);
					
					if (val == max) {
						LauncherFrame.getInstance().getPanel().setInfoText("Finalisation et vérification de l'installation");
					} else {
						LauncherFrame.getInstance().getPanel().setInfoText("Téléchargement des fichiers " +
								BarAPI.getNumberOfDownloadedFiles() + "/" + BarAPI.getNumberOfFileToDownload() + " " +
								Swinger.percentage(val, max) + "%");
					}
				}
			};
		};
		updateThread.start();
		
		su.start();
		
		interruptThread();
	}
	
	public static void launch() throws LaunchException {
		ExternalLaunchProfile profile = MinecraftLauncher.createExternalProfile(COH_INFOS, GameFolder.BASIC, authInfos);
		profile.getVmArgs().addAll(Arrays.asList(LauncherFrame.getInstance().getPanel().getRamSelector().getRamArguments()));
		ExternalLauncher gameLauncher = new ExternalLauncher(profile);
		
		Process p = gameLauncher.launch();
		
		ProcessLogManager logManager = new ProcessLogManager(p.getInputStream(),new File(COH_DIR, "cohlogs.txt"));
		logManager.start();
		
		try {
			Thread.sleep(5000);
			LauncherFrame.getInstance().setVisible(false);
			p.waitFor();
		} catch (InterruptedException e) {
			//
		}
		System.exit(0);
	}
	
	public static void interruptThread() {
		updateThread.interrupt();
	}
	
	public static CrashReporter getCrashReporter() {
		return COH_REPORTER;
	}
}
