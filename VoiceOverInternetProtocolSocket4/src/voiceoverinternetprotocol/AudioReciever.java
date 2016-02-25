/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voiceoverinternetprotocol;

import CMPC3M06.AudioPlayer;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;
import javax.sound.sampled.LineUnavailableException;
import uk.ac.uea.cmp.voip.DatagramSocket2;
import uk.ac.uea.cmp.voip.DatagramSocket4;

/**
 *
 * @author dha13jyu
 */
public class AudioReciever implements Runnable {

    static DatagramSocket4 receiving_socket;
    AudioPlayer player;

    public AudioReciever() throws LineUnavailableException {
        player = new AudioPlayer();
    }

    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }

    public void run() {

		//***************************************************
        //Port to open socket on
        int PORT = 8000;
        //***************************************************

		//***************************************************
        //Open a socket to receive from on port PORT
        //DatagramSocket receiving_socket;
        try {
            receiving_socket = new DatagramSocket4(PORT);
        } catch (SocketException e) {
            System.out.println("ERROR: Audio Receiver: Could not open UDP socket to receive from.");
            e.printStackTrace();
            System.exit(0);
        }
        //***************************************************

		//***************************************************
        //Main loop.
        boolean running = true;
        DatagramPacket[] orderedPackets = new DatagramPacket[256];
        int burst = 0;
        DatagramPacket lastWorkingPacket = null;
        int x = 0;
        while (running) {
            try {

                byte[] recieve = new byte[517];
                DatagramPacket packet = new DatagramPacket(recieve, 0, 517);
                //Add data to packet
                receiving_socket.receive(packet);

				//Unshuffle packet
                //Get checksum
                byte[] checksums = new byte[4];
                for (int i = 0; i < 4; i++) {
                    checksums[i] = recieve[i];
                }
                int checksum = java.nio.ByteBuffer.wrap(checksums).getInt();

                //Get Header
                byte header = recieve[4]; //get header

                //Get Payload
                byte[] payload = new byte[512];
                for (int i = 5; i < recieve.length; i++) {
                    payload[i - 5] = recieve[i];
                }
                DatagramPacket packet2 = new DatagramPacket(payload, payload.length);

                //Re-calc checksum
                int checksum2 = 0;
                for (int i = 0; i < payload.length; i++) {
                    Byte byteEqv = (Byte) payload[i];
                    checksum2 += byteEqv.hashCode();
                }
                //Put packets in order via header
                int orderedHeader;
                int correctHeader = (int) header;
                if (correctHeader < 0) {
                    orderedHeader = 256 + correctHeader;
                } else {
                    orderedHeader = correctHeader;
                }
                System.out.println(checksum);
                System.out.println(checksum2);
                burst++;
                if (checksum == checksum2) {
                    orderedPackets[orderedHeader] = packet2;
                }
                else{
                    System.out.println("Packet corrupted!");
                }
                if (burst >= 16) {
                    for (int i = 16 * x; i < (16 * (x + 1)); i++) {
                        if (orderedPackets[i] == null) {
                            if (lastWorkingPacket != null) {
                                player.playBlock(lastWorkingPacket.getData());
                            }
                        } else {
                            player.playBlock(orderedPackets[i].getData());
                            lastWorkingPacket = orderedPackets[i];
                            orderedPackets[i] = null;
                        }
                    }
                    burst = 0;
                    if (x < 15) {
                        x++;
                    } else {
                        x = 0;
                    }
                    //Arrays.fill(orderedPackets, null);
                }

				//if (burst < 16) { //wrong, as this means 16 packets reciveved, does not compensate for packet loss
				/*} else {
                 for(int i = 16*x; i < (16*(x+1)); i++){ //265 IS WRONG, I NEEDS TO START AT THE PREVIOUS ENDPOINT, STORE THIS
                 if(orderedPackets[i] == null){
                 System.out.println("PACKET LOST No. " + i);
                 }
                 else{
                 System.out.println("PACKET No. " + i);
                 player.playBlock(orderedPackets[i].getData());
                 }
                 }
                 System.out.println("NEW PACKET BURST");
                 if(x < 15){
                 System.out.println("X IS " + x);
                 x++;
                 }
                 else{
                 x = 0;
                 System.out.println("RESET ARRAY");
                 for(int i = 0; i < 256; i++){
                 orderedPackets[i] = null;
                 }
                 }
                 burst = 0;
                 }*/
            } catch (IOException e) {
                System.out.println("ERROR: Audio Receiver: Some random IO error occured!");
                e.printStackTrace();
            }
        }
        //Close the socket
        player.close();
        receiving_socket.close();
        //***************************************************
    }
}
