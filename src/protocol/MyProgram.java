package protocol;

import client.MACChallengeClient;

import java.io.IOException;
import java.util.Scanner;

/**
 * Entry point of the program. Starts the client and links the used MAC
 * protocol.
 *
 * @author Jaco ter Braak, Twente University
 * @version 05-12-2013
 */
public class MyProgram {

    public static void main(String[] args) {
        boolean isRunning = true;
        Scanner sc = new Scanner(System.in);
        while (isRunning) {
            MACChallengeClient client0 = null;
            MACChallengeClient client1 = null;
            MACChallengeClient client2 = null;
            MACChallengeClient client3 = null;
            MACChallengeClient client4 = null;
            MACChallengeClient client5 = null;
            try {
                System.out.print("Starting client... ");

                // Create the client
                String password = "cfvhujawslp;lp;e4rnjmgyhe4r";
                String serverAddress = "netsys.student.utwente.nl";
                int groupId = 1802607;
                int serverPort = 8001;
                client0 = new MACChallengeClient(serverAddress, serverPort, groupId, password);
                client1 = new MACChallengeClient(serverAddress, serverPort, groupId, password);
                client2 = new MACChallengeClient(serverAddress, serverPort, groupId, password);
                client3 = new MACChallengeClient(serverAddress, serverPort, groupId, password);
                client4 = new MACChallengeClient(serverAddress, serverPort, groupId, password);
                client5 = new MACChallengeClient(serverAddress, serverPort, groupId, password);

                System.out.println("Done.");

                // Set protocol
                client0.setListener(new TurnBasedProtocol());
                client1.setListener(new TurnBasedProtocol());
                client2.setListener(new TurnBasedProtocol());
                client3.setListener(new TurnBasedProtocol());
                client4.setListener(new TurnBasedProtocol());
                client5.setListener(new TurnBasedProtocol());

                // Start System
                client0.requestStart();

                System.out.println("Simulation started!");

                // Wait until the simulation is finished
                while (!client0.isSimulationFinished()) {
                    Thread.sleep(100);
                }

                System.out
                        .println("Simulation finished! Check your performance on the server web interface.");

            } catch (IOException e) {
                System.out.print("Could not start the client, because: ");
                e.printStackTrace();
            } catch (InterruptedException e) {
                System.out.println("Operation interrupted.");
                e.printStackTrace();
            } catch (Exception e) {
                System.out.print("Unexpected Exception: ");
                e.printStackTrace();
            } finally {
                if (client0 != null) {
                    System.out.print("Shutting down client0... ");
                    client0.stop();
                    System.out.println("Done.");
                }
                if (client1 != null) {
                    System.out.print("Shutting down client1... ");
                    client1.stop();
                    System.out.println("Done.");
                }
                if (client2 != null) {
                    System.out.print("Shutting down client2... ");
                    client2.stop();
                    System.out.println("Done.");
                }
                if (client3 != null) {
                    System.out.print("Shutting down client3... ");
                    client3.stop();
                    System.out.println("Done.");
                }
                if (client4 != null) {
                    System.out.print("Shutting down client4... ");
                    client4.stop();
                    System.out.println("Done.");
                }
                if (client5 != null) {
                    System.out.print("Shutting down client5... ");
                    client5.stop();
                    System.out.println("Done.");
                }
                System.out.println("Terminating program.");
                System.out.println();
                System.out.print("Press any key to restart or type q to quit...");
                if (sc.nextLine().contains("q")) {
                    isRunning = false;
                }
            }
        }
    }
}
