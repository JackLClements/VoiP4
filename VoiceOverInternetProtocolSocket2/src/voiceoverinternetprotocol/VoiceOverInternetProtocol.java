/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package voiceoverinternetprotocol;

import javax.sound.sampled.LineUnavailableException;

/**
 *
 * @author dha13jyu
 */
public class VoiceOverInternetProtocol {

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) throws LineUnavailableException {
		// TODO code application logic here
		AudioSender sender = new AudioSender();
		AudioReciever reciever = new AudioReciever();
		reciever.start();
		sender.start();
		
	}
	
}
