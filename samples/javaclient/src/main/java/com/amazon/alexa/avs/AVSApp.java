/** 
 * Copyright 2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Amazon Software License (the "License"). You may not use this file 
 * except in compliance with the License. A copy of the License is located at
 *
 *   http://aws.amazon.com/asl/
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, express or implied. See the License for the 
 * specific language governing permissions and limitations under the License.
 */
package com.amazon.alexa.avs;

import com.amazon.alexa.avs.auth.AccessTokenListener;
import com.amazon.alexa.avs.auth.AuthSetup;
import com.amazon.alexa.avs.auth.companionservice.RegCodeDisplayHandler;
import com.amazon.alexa.avs.config.DeviceConfig;
import com.amazon.alexa.avs.config.DeviceConfigUtils;
import com.amazon.alexa.avs.http.AVSClientFactory;
import com.amazon.alexa.avs.wakeword.WakeWordDetectedHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import javax.sound.sampled.LineUnavailableException;

import com.amazon.alexa.avs.wakeword.WakeWordIPCFactory;

public class AVSApp implements ExpectSpeechListener, RecordingRMSListener, RegCodeDisplayHandler, AccessTokenListener,
		ExpectStopCaptureListener, WakeWordDetectedHandler {

	private static final Logger log = LoggerFactory.getLogger(AVSApp.class);

	private final AVSAudioPlayer player;
	private final AVSController controller;
	private final DeviceConfig deviceConfig;

	private AuthSetup authSetup;
	private boolean tokenReceived = false;

	private enum ButtonState {
		START, STOP, PROCESSING;
	}

	private ButtonState buttonState;

	public static void main(String[] args) throws Exception {
		// kill any running wake word service
		try {
			// Execute command
			System.out.println("Stopping wake word service");
			String command = "sudo service wakeword stop";
			Process child = Runtime.getRuntime().exec(command);
			child.waitFor();
		} catch (IOException e) {
		}

		if (args.length == 1) {
			new AVSApp(args[0]);
		} else {
			new AVSApp();
		}
	}

	public AVSApp() throws Exception {
		this(DeviceConfigUtils.readConfigFile());
	}

	public AVSApp(String configName) throws Exception {
		this(DeviceConfigUtils.readConfigFile(configName));
	}

	private AVSApp(DeviceConfig config) throws Exception {
		// TODO configure logger so that we can use info instead of sysout
		System.out.println("AVS App starting...");
		System.out.println("Version " + getAppVersion());

		tokenReceived = false;

		deviceConfig = config;
		AVSAudioPlayerFactory factory = new AVSAudioPlayerFactory();
		controller = new AVSController(this, factory, new AlertManagerFactory(), getAVSClientFactory(deviceConfig),
				DialogRequestIdAuthority.getInstance(), new WakeWordIPCFactory(), deviceConfig, this);
		player = factory.getAudioPlayer(controller);

		authSetup = new AuthSetup(config, this);
		authSetup.addAccessTokenListener(this);
		authSetup.addAccessTokenListener(controller);
		authSetup.startProvisioningThread();

		buttonState = ButtonState.START;

		controller.initializeStopCaptureHandler(this);
		controller.startHandlingDirectives();
		System.out.println("AVS App running...");

		System.out.println("Current locale is " + deviceConfig.getLocale());
//		Locale locale = Locale.GERMAN;
//		deviceConfig.setLocale(locale);
//		DeviceConfigUtils.updateConfigFile(deviceConfig);
//		controller.setLocale(locale);
	}

	private String getAppVersion() {
		final Properties properties = new Properties();
		try (final InputStream stream = getClass().getResourceAsStream("/res/version.properties")) {
			properties.load(stream);
			if (properties.containsKey("version")) {
				return properties.getProperty("version");
			}
		} catch (IOException e) {
			log.warn("version.properties file not found on classpath");
		}
		return null;
	}

	protected AVSClientFactory getAVSClientFactory(DeviceConfig config) {
		return new AVSClientFactory(config);
	}

	/**
	 * Respond to a music button press event
	 *
	 * @param action
	 *            Playback action to handle
	 */
	// private void musicButtonPressedEventHandler(final PlaybackAction action)
	// {
	// SwingWorker<Void, Void> alexaCall = new SwingWorker<Void, Void>() {
	// @Override
	// public Void doInBackground() throws Exception {
	// visualizer.setIndeterminate(true);
	// controller.handlePlaybackAction(action);
	// return null;
	// }
	//
	// @Override
	// public void done() {
	// visualizer.setIndeterminate(false);
	// }
	// };
	// alexaCall.execute();
	// }

	// private void createMusicButton(Container container, String label, final
	// PlaybackAction action) {
	// JButton button = new JButton(label);
	// button.setEnabled(true);
	// button.addActionListener(new ActionListener() {
	// @Override
	// public void actionPerformed(ActionEvent e) {
	// controller.onUserActivity();
	// musicButtonPressedEventHandler(action);
	// }
	// });
	// container.add(button);
	// }

	// private void setPlaybackControlEnabled(boolean enable) {
	// setComponentsOfContainerEnabled(playbackPanel, enable);
	// }

	/**
	 * Recursively Enable/Disable components in a container
	 *
	 * @param container
	 *            Object of type Container (like JPanel).
	 * @param enable
	 *            Set true to enable all components in the container. Set to
	 *            false to disable all.
	 */
	// private void setComponentsOfContainerEnabled(Container container, boolean
	// enable) {
	// for (Component component : container.getComponents()) {
	// if (component instanceof Container) {
	// setComponentsOfContainerEnabled((Container) component, enable);
	// }
	// component.setEnabled(enable);
	// }
	// }

	/**
	 * Add music control buttons
	 */
	// private void addPlaybackButtons() {
	// playbackPanel = new JPanel();
	// playbackPanel.setLayout(new GridLayout(1, 5));
	//
	// playPauseButton = new JButton(PLAY_LABEL + "/" + PAUSE_LABEL);
	// playPauseButton.setEnabled(true);
	// playPauseButton.addActionListener(new ActionListener() {
	// @Override
	// public void actionPerformed(ActionEvent e) {
	//
	// controller.onUserActivity();
	// if (controller.isPlaying()) {
	// musicButtonPressedEventHandler(PlaybackAction.PAUSE);
	// } else {
	// musicButtonPressedEventHandler(PlaybackAction.PLAY);
	// }
	// }
	// });
	//
	// createMusicButton(playbackPanel, PREVIOUS_LABEL,
	// PlaybackAction.PREVIOUS);
	// playbackPanel.add(playPauseButton);
	//
	// createMusicButton(playbackPanel, NEXT_LABEL, PlaybackAction.NEXT);
	// getContentPane().add(playbackPanel);
	// }

	public void finishProcessing() {
		buttonState = ButtonState.START;
		controller.processingFinished();
	}

	@Override
	public void rmsChanged(int rms) { // AudioRMSListener callback
		System.out.print("\rVoice Level: " + rms);
	}

	@Override
	public void onExpectSpeechDirective() {
		Thread thread = new Thread() {
			@Override
			public void run() {
				while (buttonState != ButtonState.START || controller.isSpeaking()) {
					try {
						Thread.sleep(500);
					} catch (Exception e) {
					}
				}
				doAction();
			}
		};
		thread.start();
	}

	@Override
	public void onStopCaptureDirective() {
		if (buttonState == ButtonState.STOP) {
			doAction();
		}
	}

	@Override
	public void displayRegCode(String regCode) {
		String regUrl = deviceConfig.getCompanionServiceInfo().getServiceUrl() + "/provision/" + regCode;

		if (deviceConfig.isAutoLoginEnabled()) {
			System.out.println("Logging in to Alexa Service...");
			AutoLogin autoLogin = new AutoLogin(deviceConfig);
			autoLogin.login(regUrl);
			System.out.println("Logging in to Alexa Service finished...");
		} else {
			System.out.println("Manual login not supported!");
		}
	}

	private String token = null;

	@Override
	public synchronized void onAccessTokenReceived(String accessToken) {
		if (accessToken == null || accessToken.isEmpty()) {
			System.out.println("Access token is empty or null!");
			tokenReceived = false;
			this.token = accessToken;
			return;
		}
		// this actually means that we are connected now and can use alexa
		System.out.println("Access token received: " + accessToken + "\nConnected to Alexa Service.");

		// if this.accessToken == null then we receive it for the first time
		if (this.token == null) {
			try {
				// Execute command
				System.out.println("Starting wake word service");
				String command = "sudo service wakeword start";
				Runtime.getRuntime().exec(command);
			} catch (IOException e) {
				log.error("Exception during wakeword service start: " + e.getMessage());
				e.printStackTrace();
				// if wakeword service does not start no need to continue
				System.exit(0);
			}

			// play sound when token is received so we know we can start using
			// it
			// TODO change alarm.mp3 to something like "Hello, this is Alexa,
			// how can i help you today"
			tokenReceived = true;
			player.playMp3FromResource("res/alarm.mp3");
		}
		this.token = accessToken;
	}

	@Override
	public synchronized void onWakeWordDetected() {
		// prevent wake word action if not connected yet
		if (!tokenReceived)
			return;

		if (buttonState == ButtonState.START) { // if in idle mode
			System.out.println("Wake Word detected.");
			doAction();
		}
	}

	private RequestListener requestListener = new RequestListener() {

		@Override
		public void onRequestSuccess() {
			if (buttonState == ButtonState.STOP) 
				doAction();
			System.out.println("\r\nRequest success.");
			finishProcessing();
		}

		@Override
		public void onRequestError(Throwable e) {
			// TODO currently alexa service hangs up if e returns null request
			// error...
			if (e instanceof TimeoutException)
				System.out.println("\r\nRequest timed out.");
			else if (e instanceof LineUnavailableException)
				System.out.println("\r\nMic unavailable...");
			else {
				System.out.println("\r\nRequest error: " + e.getMessage());
				e.printStackTrace();
			}

			doAction();
			finishProcessing();
		}
	};

	private void doAction() {
		controller.onUserActivity();

		if (buttonState == ButtonState.START) { // if in idle mode
			buttonState = ButtonState.STOP;
			// setPlaybackControlEnabled(false);

			System.out.println("Start recording");
			controller.startRecording(this, requestListener);
		} else { // else we must already be in listening
			buttonState = ButtonState.PROCESSING;
			controller.stopRecording(); // stop the recording so the request can
										// complete
		}
	}
}
