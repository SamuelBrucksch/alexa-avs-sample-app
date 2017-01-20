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
//import java.net.URI;
import java.util.Properties;

import com.amazon.alexa.avs.wakeword.WakeWordIPCFactory;

@SuppressWarnings("serial")
public class AVSApp implements ExpectSpeechListener, RecordingRMSListener, RegCodeDisplayHandler, AccessTokenListener, ExpectStopCaptureListener, WakeWordDetectedHandler {

	private static final Logger log = LoggerFactory.getLogger(AVSApp.class);

	private static final String APP_TITLE = "Alexa Voice Service";
	private final AVSController controller;
	private final DeviceConfig deviceConfig;

	private AuthSetup authSetup;

	private enum ButtonState {
		START, STOP, PROCESSING;
	}

	private ButtonState buttonState;

	public static void main(String[] args) throws Exception {
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
		deviceConfig = config;
		controller = new AVSController(this, new AVSAudioPlayerFactory(), new AlertManagerFactory(), getAVSClientFactory(deviceConfig), DialogRequestIdAuthority.getInstance(),
				config.getWakeWordAgentEnabled(), new WakeWordIPCFactory(), this);

		authSetup = new AuthSetup(config, this);
		authSetup.addAccessTokenListener(this);
		authSetup.addAccessTokenListener(controller);
		authSetup.startProvisioningThread();

		// addDeviceField();
		// addTokenField();
		// addVisualizerField();
		// addActionField();
		// addPlaybackButtons();

		buttonState = ButtonState.START;

		controller.initializeStopCaptureHandler(this);
		controller.startHandlingDirectives();
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

	private String getAppTitle() {
		String version = getAppVersion();
		String title = APP_TITLE;
		if (version != null) {
			title += " - v" + version;
		}
		return title;
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
		log.info("RMS changed: " + rms);
	}

	@Override
	public void onExpectSpeechDirective() {
		log.info("onExpectSpeechDirective");
		Thread thread = new Thread() {
			@Override
			public void run() {
				while (buttonState != ButtonState.START || controller.isSpeaking()) {
					try {
						Thread.sleep(500);
					} catch (Exception e) {
					}
				}
				startRecording();
			}
		};
		thread.start();
	}

	@Override
	public void onStopCaptureDirective() {
		if (buttonState == ButtonState.STOP) {
			startRecording();
		}
	}

	@Override
	public void displayRegCode(String regCode) {
		String regUrl = deviceConfig.getCompanionServiceInfo().getServiceUrl() + "/provision/" + regCode;

		if (deviceConfig.isAutoLoginEnabled()) {
			AutoLogin autoLogin = new AutoLogin(deviceConfig);
			autoLogin.login(regUrl);
		} else {
			log.info("Can not start java client for alexa!");
		}
	}

	@Override
	public synchronized void onAccessTokenReceived(String accessToken) {
		log.info("Access token received: " + accessToken);
	}

	@Override
	public synchronized void onWakeWordDetected() {
		if (buttonState == ButtonState.START) { // if in idle mode
			log.info("Wake Word was detected");
			startRecording();
		}
	}

	private void startRecording() {
		controller.onUserActivity();

		if (buttonState == ButtonState.START) { // if in idle mode
			buttonState = ButtonState.STOP;
			// setPlaybackControlEnabled(false);

			RequestListener requestListener = new RequestListener() {

				@Override
				public void onRequestSuccess() {
					finishProcessing();
				}

				@Override
				public void onRequestError(Throwable e) {
					log.error("An error occured creating speech request", e);

					// TODO why start again?
					startRecording();
					finishProcessing();
				}
			};
			controller.startRecording(this, requestListener);
		} else { // else we must already be in listening
			buttonState = ButtonState.PROCESSING;
			controller.stopRecording(); // stop the recording so the request can complete
		}
	}
}
