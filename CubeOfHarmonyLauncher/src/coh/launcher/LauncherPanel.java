/**
 * 
 */
package coh.launcher;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import coh.launcher.resources.Resources;
import fr.litarvan.openauth.AuthenticationException;
import fr.theshark34.openlauncherlib.LaunchException;
import fr.theshark34.openlauncherlib.util.Saver;
import fr.theshark34.openlauncherlib.util.ramselector.RamSelector;
import fr.theshark34.swinger.Swinger;
import fr.theshark34.swinger.animation.Animator;
import fr.theshark34.swinger.colored.SColoredBar;
import fr.theshark34.swinger.event.SwingerEvent;
import fr.theshark34.swinger.event.SwingerEventListener;
import fr.theshark34.swinger.textured.STexturedButton;

/**
 * @author Thibaut SIMON-FINE (alias Bisougai)
 * 
 * Based on code by Adrien Navratil (alias Litarvan)
 * Link : https://github.com/Litarvan/
 *
 */
public class LauncherPanel extends JPanel implements SwingerEventListener {
	private static final long serialVersionUID = 4943345214107518384L;
	private Saver userSaver = new Saver(Launcher.COH_PARAMS);
	private RamSelector ramSelector = new RamSelector(Launcher.COH_RAM_SELECTOR);
	
	private Image background = Swinger.getResource("background.png");
	private STexturedButton ramButton = new STexturedButton(Swinger.getResource(Resources.PARAM_IMG));
	private STexturedButton quitButton = new STexturedButton(Swinger.getResource(Resources.QUIT_IMG));
	private STexturedButton hideButton = new STexturedButton(Swinger.getResource(Resources.HIDE_IMG));
	private JTextField userField = new JTextField(userSaver.get("username"));
	private JPasswordField passField = new JPasswordField();
	private STexturedButton playButton = new STexturedButton(Swinger.getResource(Resources.PLAY_IMG));
	private JLabel infoLabel = new JLabel("Entrez vous identifiants et cliquez sur \"JOUER\"", SwingConstants.CENTER);
	private SColoredBar progressBar = new SColoredBar(Color.WHITE, Color.GREEN);
	
	
	public LauncherPanel() {
		this.setLayout(null);
		
		ramButton.setBounds(Resources.PARAM_X, Resources.PARAM_Y);
		this.ramButton.addEventListener(this);
		this.add(ramButton);
		
		hideButton.setBounds(Resources.HIDE_X, Resources.HIDE_Y);
		hideButton.addEventListener(this);
		this.add(hideButton);
		
		quitButton.setBounds(Resources.QUIT_X, Resources.QUIT_Y);
		quitButton.addEventListener(this);
		this.add(quitButton);
		
		JLabel userlbl = new JLabel("Nom utilisateur / email", SwingConstants.CENTER);
		JLabel passlbl = new JLabel("Mot de passe", SwingConstants.CENTER);
		
		userlbl.setBounds(35, 175, 300, 25);
		userlbl.setForeground(Color.WHITE);
		userlbl.setFont(userlbl.getFont());
		this.add(userlbl);
		
		passlbl.setBounds(35, 275, 300, 25);
		passlbl.setForeground(Color.WHITE);
		passlbl.setFont(passlbl.getFont());
		this.add(passlbl);
		
		/* *
		//Font style
		userField.setForeground(Color.WHITE);
		userField.setCaretColor(Color.WHITE);
		userField.setFont(userField.getFont().deriveFont(20F));
		//Fields Style
		userField.setOpaque(false);
		userField.setBorder(null);
		/* */
		userField.setBounds(35, 200, 300, 40);
		this.add(userField);
		
		/* *
		//Font style
		passField.setForeground(Color.WHITE);
		passField.setCaretColor(Color.WHITE);
		passField.setFont(passField.getFont().deriveFont(20F));
		//Fields Style
		passField.setOpaque(false);
		passField.setBorder(null);
		/* */
		passField.setBounds(35, 300, 300, 40);
		this.add(passField);
		
		playButton.setBounds(Resources.PLAY_X, Resources.PLAY_Y);;
		playButton.addEventListener(this);
		this.add(playButton);
		
		progressBar.setBounds(20, 600, 560, 30);
		this.add(progressBar);
		
		infoLabel.setBounds(20, 570, 560, 25);
		infoLabel.setForeground(Color.WHITE);
		infoLabel.setFont(userField.getFont());
		this.add(infoLabel);
	}
	
	@Override
	public void onEvent(SwingerEvent e) {
		if (e.getSource() == playButton) {
			setFieldsEnabled(false);
			ramSelector.save();
			
			if(userField.getText().replaceAll(" ", "").length() == 0 || passField.getText().length() == 0) {
				JOptionPane.showMessageDialog(this, "Erreur, veuillez entrer un pseudo et un mot de passe valides", "Erreur", JOptionPane.ERROR_MESSAGE);
				setFieldsEnabled(true);
				return;
			}
			
			Thread t = new Thread() {
				@Override
				public void run() {
					try {
						Launcher.auth(userField.getText(), passField.getText());
					} catch (AuthenticationException e) {
						JOptionPane.showMessageDialog(LauncherPanel.this, "Erreur, impossible de se connecter : " + e.getErrorModel().getErrorMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
						setFieldsEnabled(true);
						return;
					}
					
					userSaver.set("username",userField.getText());
					
					try {
						Launcher.update();
					} catch (Exception e) {
						Launcher.interruptThread();
						Launcher.getCrashReporter().catchError(e, "Impossible de mettre à jour Cube of Harmony");
						//JOptionPane.showMessageDialog(LauncherPanel.this, "Erreur, impossible de mettre le jeu à jour : " + e, "Erreur", JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					try {
						Launcher.launch();
					} catch (LaunchException e) {
						Launcher.getCrashReporter().catchError(e, "Impossible de lancer Minecraft");
						//JOptionPane.showMessageDialog(LauncherPanel.this, "Erreur, impossible de lancer le jeu : " + e, "Erreur", JOptionPane.ERROR_MESSAGE);
						setFieldsEnabled(true);
					}
				};
			};
			t.start();
		} else if (e.getSource() == quitButton) {
			Animator.fadeOutFrame(LauncherFrame.getInstance(), Animator.FAST, new Runnable() {
				@Override
				public void run() {
					System.exit(0);
				}
			});
		} else if (e.getSource() == hideButton) {
			LauncherFrame.getInstance().setState(JFrame.ICONIFIED);
		} else if (e.getSource() == ramButton) {
			ramSelector.display();
		}
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		g.drawImage(background, 0, 0, this.getWidth(), this.getHeight(), this);
		g.setColor(Color.GRAY);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
	}
	
	private void setFieldsEnabled(boolean b) {
		userField.setEnabled(b);
		passField.setEnabled(b);
		playButton.setEnabled(b);
	}
	
	public SColoredBar getProgressBar() {
		return progressBar;
	}
	
	public void setInfoText(String s) {
		infoLabel.setText(s);
	}
	
	public RamSelector getRamSelector() {
		return ramSelector;
	}
}
