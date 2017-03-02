package protocol;

import client.MACChallengeClient;

import java.io.IOException;

/**
 * Entry point of the program. Starts the client and links the used MAC
 * protocol.
 *
 * @author Jaco ter Braak, Twente University
 * @version 05-12-2013
 */
public class MyProgram {

    public static final int NODE_COUNT = 4;

    private static MACChallengeClient[] clients = new MACChallengeClient[NODE_COUNT];

    public static void main(String[] args) {
        String password = "cfvhujawslp;lp;e4rnjmgyhe4r";
        String serverAddress = "netsys.student.utwente.nl";
        int groupId = 1802607;
        int serverPort = 8001;
        TurnBasedProtocol.setNodeCount(NODE_COUNT);

        try {
            System.out.print("Starting client... ");

            // Create the client

            for (int i = 0; i < clients.length; i++) {
                clients[i] = new MACChallengeClient(serverAddress, serverPort, groupId, password);
                clients[i].setListener(new TurnBasedProtocol());
            }
            System.out.println("Done.");

            // Start System
            clients[0].requestStart();

            System.out.println("Simulation started!");

            // Wait until the simulation is finished
            while (!clients[0].isSimulationFinished()) {
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

            for (MACChallengeClient client : clients) {
                if (client != null) {
                    System.out.print("Shutting down client0... ");
                    client.stop();
                    System.out.println("Done.");
                }
            }
            System.out.println("Terminating program.");
        }
    }
}
