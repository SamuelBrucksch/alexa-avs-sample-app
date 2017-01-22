package com.amazon.alexa.avs;

import com.amazon.alexa.avs.config.DeviceConfig;

import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.StaleElementReferenceException;
import com.gargoylesoftware.htmlunit.BrowserVersion;

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

	private static final int TIMEOUT = 10;
    
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
		//prevent htmlunit client logging as it prints a lot of CSS warnings
		LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log",
				"org.apache.commons.logging.impl.NoOpLog");

		Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF);
		Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.OFF);
			    
	    HtmlUnitDriver driver = new HtmlUnitDriver(BrowserVersion.CHROME);
		//WebDriver driver = new ChromeDriver();
		
		driver.get(url);
		
		if (autoLoginUsername == null || autoLoginUsername.isEmpty()) {
			System.out.print("Please enter your e-mail: ");
			if (System.console() != null) {
				autoLoginUsername = System.console().readLine();
			} else {
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
			if (System.console() != null) {
				char[] passwordChars = System.console().readPassword();
				autoLoginPassword = new String(passwordChars);
			} else {
				System.out.print("\rPlease enter your password (password is visible): ");
				BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
				try {
					autoLoginPassword = reader.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		WebElement emailBox = driver.findElement(By.id("ap_email"));	
		//emailBox.click();
		emailBox.sendKeys(autoLoginUsername);
		
		WebElement passwordBox = driver.findElement(By.id("ap_password"));
		passwordBox.sendKeys(autoLoginPassword);

		driver.setJavascriptEnabled(true);
		passwordBox.submit();
		
		// wait for page to load
		try {
			(new WebDriverWait(driver, TIMEOUT)).until(stalenessOf(passwordBox));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//this somehow causes the login to time out?
		/*try {
			WebElement okayButton = driver.findElement(By.className("signin-button-text"));
			okayButton.click();
			
			(new WebDriverWait(driver, TIMEOUT)).until(stalenessOf(okayButton));
		} catch (Exception e) {
			e.printStackTrace();
		}*/
		
		driver.close();
	}
	
	private ExpectedCondition<Boolean> stalenessOf(final WebElement element) {
	    return new ExpectedCondition<Boolean>() {
	      public Boolean apply(WebDriver ignored) {
	        try {
	          element.isEnabled();
	          return false;
	        } catch (StaleElementReferenceException expected) {
	          return true;
	        }
	      }
	    };
	}
}