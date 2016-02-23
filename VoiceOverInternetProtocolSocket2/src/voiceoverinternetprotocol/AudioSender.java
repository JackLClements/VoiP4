/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voiceoverinternetprotocol;

import CMPC3M06.AudioPlayer;
import CMPC3M06.AudioRecorder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Vector;
import javax.sound.sampled.LineUnavailableException;
import uk.ac.uea.cmp.voip.DatagramSocket2;

/**
 *
 * @author dha13jyu
 */
public class AudioSender implements Runnable {

	//static DatagramSocket sending_socket;
	static DatagramSocket2 sending_socket;
	Vector<byte[]> voiceVector;
	AudioRecorder recorder;
	//AudioPlayer player;

	public AudioSender() throws LineUnavailableException {
		recorder = new AudioRecorder();
		voiceVector = new Vector<byte[]>();
		//player = new AudioPlayer();
	}

	public void start() {
		Thread thread = new Thread(this);
		thread.start();
	}

	public void run() {

		//***************************************************
		//Port to send to
		int PORT = 8000;
		//IP ADDRESS to send to
		InetAddress clientIP = null;
		try {
			clientIP = InetAddress.getByName("localhost");  //CHANGE localhost to IP or NAME of client machine
		} catch (UnknownHostException e) {
			System.out.println("ERROR: Audio Sender: Could not find client IP");
			e.printStackTrace();
			System.exit(0);
		}
        //***************************************************

		//***************************************************
		//Open a socket to send from
		//We dont need to know its port number as we never send anything to it.
		//We need the try and catch block to make sure no errors occur.
		//DatagramSocket sending_socket;
		try {
			sending_socket = new DatagramSocket2();
		} catch (SocketException e) {
			System.out.println("ERROR: Audio Sender: Could not open UDP socket to send from.");
			e.printStackTrace();
			System.exit(0);
		}
        //***************************************************

		//***************************************************
		//Get a handle to the Standard Input (console) so we can read user input
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        //***************************************************

		//***************************************************
		//Main loop.
		boolean running = true;
		int burst = 0;
		byte header = 0;
		DatagramPacket[] packets = new DatagramPacket[16];
		DatagramPacket[] packets2 = new DatagramPacket[16]; //reordered packets
		while (running) {
			try {
				if (burst < 16) {
					//PAYLOAD CREATION
					//Read in data and add to vector for potential re-transmission
					byte[] payload = recorder.getBlock();
					voiceVector.add(payload);
		

					//PACKET CREATION
					byte[] packetData = new byte[1 + payload.length];
					//HEADER CREATION
					packetData[0] = header;
					header++;
					for (int r = 1; r < packetData.length; r++) {
						packetData[r] = payload[r - 1];
					}

					//Make a DatagramPacket from it, with client address and port number
					DatagramPacket packet = new DatagramPacket(packetData, packetData.length, clientIP, PORT);
					
					//add to packet array
					packets[burst] = packet;
					burst++;
				} else {
					//Send it
					int d = 4;
					int r0 = 0;
					int r1 = 12;
					int r2 = 8;
					int r3 = 4;
					for (int j = 0; j < 16; j++) {
						int h = j + 1;
						if (h % d == 0) {
							packets2[r0] = packets[j];
							r0++;
						} else if (h % d == 1) {
							packets2[r1] = packets[j];
							r1++;
						} else if (h % d == 2) {
							packets2[r2] = packets[j];
							r2++;
						} else if (h % d == 3) {
							packets2[r3] = packets[j];
							r3++;
						}
					}
					for (int b = 0; b < 16; b++) {
						sending_socket.send(packets2[b]);
					}
					burst = 0;
				}

			} catch (IOException e) {
				System.out.println("ERROR: Audio Sender: Some random IO error occured!");
				e.printStackTrace();
			}
		}
		//Close the socket
		recorder.close();
		sending_socket.close();
		//***************************************************
	}

}
