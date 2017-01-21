## Raspberry-Pi Instructions:
1. git clone https://github.com/SamuelBrucksch/alexa-avs-sample-app/
2. cd alexa-avs-sample-app
3. enter Device ID, and the other 2 Strings like in the original instructions
4. run automated_install.sh
5. if you want to make use of the additional features added here you need to select auto start and enter working credentials
6. configure you alsa configuration. you can test with aplay test.wav and arecord -d 10 test.wav, both should work without any additional parameters. If both commands work then alexa will work as well (at least audio wise).
7. reboot

![](../../wiki/assets/alexa-logo-3.png)

## About the project

This project provides a step-by-step walkthrough to help you build a **hands-free** [Alexa Voice Service](https://developer.amazon.com/avs) (AVS) prototype in 60 minutes, using wake word engines from [Sensory](https://github.com/Sensory/alexa-rpi) or [KITT.AI](https://github.com/Kitt-AI/snowboy). Now, in addition to pushing a button to "start listening", you can now also just say the wake word "Alexa", much like the [Amazon Echo](https://amazon.com/echo). You can find step-by-step instructions to set up the hands-free prototype on [Raspberry Pi](../../wiki/Raspberry-Pi), or follow the instructions to set up the push-to-talk only prototype on [Linux](../../wiki/Linux), [Mac](../../wiki/Mac), or [Windows](../../wiki/Windows).

*NEW!* - [Click here](../../wiki/Conexant2Mic-Raspberry-Pi) for instructions to build the AVS Prototype using a Raspberry Pi and the Conexant 2-Mic Development Kit for Amazon AVS

---

## What is AVS?

[Alexa Voice Service](https://developer.amazon.com/avs) (AVS) is Amazon’s intelligent voice recognition and natural language understanding service that allows you as a developer to voice-enable any connected device that has a microphone and speaker.

---

## What's new?

**December 8, 2016:**

The AVS java sample app for Raspberry Pi has been updated for two changes:  

1. The sample app now supports GPIO triggers for use with the Conexant 2-Mic Development Kit for Amazon AVS. [Click here](../../wiki/Conexant2Mic-Raspberry-Pi) for step-by-step instructions.  
2. The sample app is now officially certified for media and music services.  

**October 6, 2016:**

How is this different from the [last Amazon Alexa Pi project](https://github.com/alexa/alexa-avs-raspberry-pi/tree/79b7df7aaa4c5304446f59c0bd3ee2589b245115)? That's easy, two wake word engines are included with this project - [TrulyHandsFree](https://github.com/Sensory/alexa-rpi) from [Sensory](http://www.sensory.com/) and [Snowboy](https://github.com/Kitt-AI/snowboy) from  [KITT.AI](http://kitt.ai). When you're finished, you'll have a working voice-enabled AVS prototype.

---

## Important considerations

* The earcons associated with the sample project are for **prototyping purposes only**. For implementation and design guidance for commercial products, please see [Designing for AVS](https://developer.amazon.com/public/solutions/alexa/alexa-voice-service/content/designing-for-the-alexa-voice-service) and [AVS UX Guidelines](https://developer.amazon.com/public/solutions/alexa/alexa-voice-service/content/alexa-voice-service-ux-design-guidelines).

* Alexa Voice Service [Terms & Agreements](https://developer.amazon.com/public/solutions/alexa/alexa-voice-service/support/terms-and-agreements)

* **Usage of Sensory & KITT.AI wake word engines**: The wake word engines included with this project (Sensory and KITT.AI) are intended to be used for **prototyping purposes only**. If you are building a commercial product with either solution, please use the contact information below to enquire about commercial licensing -
  * [Contact Sensory](http://www.sensory.com/support/contact/us-sales/) for information on TrulyHandsFree licensing.
  * [Contact KITT.AI](mailto:snowboy@kitt.ai) for information on SnowBoy licensing.

* **IMPORTANT**: The Sensory wake word engine included with this project is time-limited: code linked against it will stop working when the library expires. The library included in this repository will, at all times, have an expiration date that is at least **120 days** in the future. See Sensory's [GitHub](https://github.com/Sensory/alexa-rpi#license) page for more information on how to renew the license for non-commercial use.

---

## Get Started

You can set up this project on the following platforms. Please choose the platform you'd like to set this up on -

* [Raspberry Pi](../../wiki/Raspberry-Pi), or
* [Linux](../../wiki/Linux), or
* [Mac](../../wiki/Mac), or
* [Windows](../../wiki/Windows)
* *New!* [Raspberry Pi + Conexant 2-Mic Development Kit for Amazon AVS](../../wiki/Conexant2Mic-Raspberry-Pi)

---
