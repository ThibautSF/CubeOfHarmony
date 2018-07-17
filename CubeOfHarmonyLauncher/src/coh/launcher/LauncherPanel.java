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

import fr.litarvan.openauth.AuthenticationException;
import fr.theshark34.openlauncherlib.LaunchException;
import fr.theshark34.openlauncherlib.util.Saver;
import fr.theshark34.openlauncherlib.util.ramselector.RamSelector;
import fr.theshark34.swinger.Swinger;
import fr.theshark34.swinger.animation.Animator;
import fr.theshark34.swinger.colored.SColoredBar;
import fr.theshark34.swinger.colored.SColoredButton;
import fr.theshark34.swinger.event.SwingerEvent;
import fr.theshark34.swinger.event.SwingerEventListener;
import fr.theshark34.swinger.textured.STexturedButton;

/**
 * @author tsimo
 *
 */
public class LauncherPanel extends JPanel implements SwingerEventListener {
	private static final long serialVersionUID = 4943345214107518384L;
	private Saver userSaver = new Saver(Launcher.COH_PARAMS);
	private RamSelector ramSelector = new RamSelector(Launcher.COH_RAM_SELECTOR);
	
	private Image background = Swinger.getResource("background.png");
	private SColoredButton ramButton = new SColoredButton(Swinger.getTransparentWhite(100),Swinger.getTransparentWhite(175));
	private STexturedButton quitButton = new STexturedButton(Swinger.getResource("quit.png"));
	private STexturedButton hideButton = new STexturedButton(Swinger.getResource("hide.png"));
	private JTextField userField = new JTextField(userSaver.get("username"));
	private JPasswordField passField = new JPasswordField();
	private STexturedButton playButton = new STexturedButton(Swinger.getResource("playBtn.png"));
	private JLabel infoLabel = new JLabel("Clique sur Bisougai", SwingConstants.CENTER);
	private SColoredBar progressBar = new SColoredBar(Color.WHITE, Color.CYAN);
	
	
	public LauncherPanel() {
		this.setLayout(null);
		
		ramButton.setBounds(830, 18, 38, 38);
		this.ramButton.addEventListener(this);
		this.add(ramButton);
		
		hideButton.setBounds(880, 18);
		hideButton.addEventListener(this);
		this.add(hideButton);
		
		quitButton.setBounds(940, 18);
		quitButton.addEventListener(this);
		this.add(quitButton);
		
		/* *
		//Font style
		userField.setForeground(Color.WHITE);
		userField.setCaretColor(Color.WHITE);
		userField.setFont(userField.getFont().deriveFont(20F));
		//Fields Style
		userField.setOpaque(false);
		userField.setBorder(null);
		/* */
		userField.setBounds(564, 254, 266, 39);
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
		passField.setBounds(564, 375, 266, 39);
		this.add(passField);
		
		playButton.setBounds(562, 420);
		playButton.addEventListener(this);
		this.add(playButton);
		
		progressBar.setBounds(12, 593, 951, 20);
		this.add(progressBar);
		
		infoLabel.setBounds(12, 560, 951, 25);
		infoLabel.setForeground(Color.WHITE);
		infoLabel.setFont(userField.getFont());
		this.add(infoLabel);
	}
	
	@Override
	public void onEvent(SwingerEvent e) {
		if (e.getSource() == playButton) {
			setFieldsEnabled(false);
			
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
