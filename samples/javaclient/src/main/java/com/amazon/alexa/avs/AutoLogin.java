package com.amazon.alexa.avs;

import com.amazon.alexa.avs.config.DeviceConfig;

import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.*;

/**
 * Automatically authenticate with Amazon
 */

public class AutoLogin {

	private String autoLoginUsername;
	private String autoLoginPassword;

	/**
	 * @param deviceConfig
	 */
	public AutoLogin(DeviceConfig deviceConfig) {
		autoLoginUsername = deviceConfig.getAutoLoginUsername();
		autoLoginPassword = deviceConfig.getAutoLoginPassword();
	}

	/**
	 * @param url
	 */
	public void login(String url) {
		LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log",
				"org.apache.commons.logging.impl.NoOpLog");

		Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF);
		Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.OFF);

		WebDriver driver = new HtmlUnitDriver();
		driver.get(url);

		WebElement emailBox = driver.findElement(By.id("ap_email"));
		WebElement passwordBox = driver.findElement(By.id("ap_password"));
		WebElement loginButton = driver.findElement(By.id("signInSubmit"));

		if (autoLoginUsername == null || autoLoginUsername.isEmpty()) {
			System.out.print("Please enter your e-mail: ");
			if (System.console() != null){
//				System.console().printf("Please enter your e-mail: ");
				autoLoginUsername = System.console().readLine();
			}else{
				BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
				try {
					autoLoginUsername = reader.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		if (autoLoginPassword == null || autoLoginPassword.isEmpty()) {
			System.out.print("Please enter your password: ");
			if (System.console() != null){
//				System.console().printf("Please enter your password: ");
				char[] passwordChars = System.console().readPassword();
				autoLoginPassword = new String(passwordChars);
			}else{
				System.out.print("\rPlease enter your password (password is visible): ");
				BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
				try {
					autoLoginPassword = reader.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		emailBox.sendKeys(autoLoginUsername);
		passwordBox.sendKeys(autoLoginPassword);
		passwordBox.submit();

		driver.close();
	}
}