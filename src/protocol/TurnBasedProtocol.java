package protocol;

import java.util.Random;

/**
 * A fairly trivial Medium Access Control scheme.
 *
 * @author Jaco ter Braak, Twente University
 * @version 05-12-2013
 */
public class TurnBasedProtocol implements IMACProtocol {

    // Optimal so far is: 7
    private static final int QUEUE_LENGTH = 8;
    // Control Signal that announces the next turn
    private static final int NEXT_TURN = -1;
    // Needs to match the amount of clients you are working with!
    private static int NODE_COUNT = 4;
    // Reusing the random we have
    private Random random;
    // this node's id
    private int id;
    // next id (for id-allocation)
    private int nextId;
    // if currentTurn == id then it's your turn
    private int currentTurn;
    // this is true when all clients have an id
    private boolean turnMode;

    // Some other helper fields
    private int partialQueue;
    private Action lastAction;
    private boolean turnInitialized;
    private boolean justHadMyTurn;

    TurnBasedProtocol() {
        // set default values
        this.random = new Random();
        this.id = 0;
        this.nextId = 1;
        this.currentTurn = 1;
        this.partialQueue = 0;
        this.turnMode = false;
        this.turnInitialized = false;
    }

    public static void setNodeCount(int nodeCount) {
        // method used by a custom starter I wrote, because I was too lazy to start 4 programs,
        // so instead I just start my program once.
        NODE_COUNT = nodeCount;
    }

    @Override
    public TransmissionInfo TimeslotAvailable(MediumState previousMediumState, int controlInformation, int localQueueLength) {
        // if id's are resolved, this is true
        if (turnMode) {
            // run my turn-based-protocol
            return sendTurnBased(previousMediumState, controlInformation, localQueueLength);
        }
        // the control information is the last id assigned, so when all ids are assigned, start
        // the turnMode.
        if (controlInformation == NODE_COUNT) {
            turnMode = true;
        }
        // if id is unassigned
        if (id == 0) {
            // if the previous message was successfully sent
            if (previousMediumState.equals(MediumState.Succes)) {
                // if my last action was sending
                if (lastAction == Action.SENDING) {
                    // then I'm now this id
                    this.id = controlInformation;
                    System.out.println("I'm ID " + id);
                    // during the rest of the initialisation-phase, I will wait
                    lastAction = Action.WAITING;
                    return new TransmissionInfo(TransmissionType.Silent, 0);
                } else {
                    //someone else was just assigned an id
                    nextId++;
                    return sendRandom(previousMediumState, controlInformation, localQueueLength);
                }
            } else {
                // no one was assigned an id, retry
                return sendRandom(previousMediumState, controlInformation, localQueueLength);
            }
        } else if (nextId != NODE_COUNT + 1) {
            //someone else was just assigned an id
            if (previousMediumState.equals(MediumState.Succes)) {
                nextId++;
            }
        }
        System.out.println("STATE: " + previousMediumState + "    Info: " + controlInformation +
                                   "    Queue: " + localQueueLength + "    Waiting for others...");
        lastAction = Action.WAITING;
        return new TransmissionInfo(TransmissionType.Silent, 0);
    }

    private TransmissionInfo sendRandom(MediumState previousMediumState, int controlInformation, int localQueueLength) {
        System.out.print("STATE: " + previousMediumState + "    Info: " + controlInformation +
                                 "    Queue: " + localQueueLength + "    ID: null    Sending: ");

        // send with a random chance that increases the less clients have to send
        if (random.nextInt(100) <= (100 / NODE_COUNT) * nextId) {
            System.out.println("true");
            lastAction = Action.SENDING;
            // send the next id, which is going to be this node's id, if it goes through
            // if it has data, it doesn't hurt to already send some
            if (localQueueLength > 0) {
                return new TransmissionInfo(TransmissionType.Data, nextId);
            } else {
                return new TransmissionInfo(TransmissionType.NoData, nextId);
            }
        } else {
            System.out.println("false");
            lastAction = Action.WAITING;
            return new TransmissionInfo(TransmissionType.Silent, 0);
        }
    }

    private TransmissionInfo sendTurnBased(MediumState previousMediumState, int controlInformation, int localQueueLength) {
        System.out.print("STATE: " + previousMediumState + "    Info: " + controlInformation +
                                 "    Queue: " + localQueueLength);
        // if it's my turn
        if (currentTurn == id) {
            // run my algorithm
            return doMyTurn(localQueueLength);
        } else {
            // reset this variable
            // this is false when it's the first packet that is sent by this node in a row
            turnInitialized = false;
            // if someone other than myself sent a NEXT_TURN-flag
            if (controlInformation == NEXT_TURN && !justHadMyTurn) {
                System.out.print("    Old Turn: " + currentTurn);
                // increase the turn-counter
                currentTurn = currentTurn + 1 == NODE_COUNT + 1 ? 1 : currentTurn + 1;
                System.out.println("    New Turn: " + currentTurn);
            } else {
                // reset this variable
                justHadMyTurn = false;
                System.out.println();
            }
            // if it is now my turn
            if (currentTurn == id) {
                // run my algorithm
                return doMyTurn(localQueueLength);
            }
            // otherwise keep silent
            return new TransmissionInfo(TransmissionType.Silent, 0);
        }
    }

    private TransmissionInfo doMyTurn(int localQueueLength) {
        // set this to true, so other parts of my code know not to listen for end-turn signals
        justHadMyTurn = true;
        // if this is the first time in this series, that this node sends
        if (!turnInitialized) {
            turnInitialized = true;
            // set the queue size
            partialQueue = QUEUE_LENGTH > localQueueLength ? localQueueLength : QUEUE_LENGTH;
        }
        System.out.println("    My turn: " + id + "    Queue: " + partialQueue);
        // if the following packet is the last one
        if (--partialQueue <= 0 || localQueueLength <= 1) {
            // then set the currentTurn to the next node's id
            currentTurn = currentTurn + 1 == NODE_COUNT + 1 ? 1 : currentTurn + 1;
        }
        // if there is data to send
        if (localQueueLength > 0) {
            // send it
            return new TransmissionInfo(TransmissionType.Data, partialQueue == 0 ? NEXT_TURN : partialQueue);
        } else {
            // send no data and the end turn flag
            return new TransmissionInfo(TransmissionType.NoData, NEXT_TURN);
        }
    }

    private enum Action {
        SENDING,
        WAITING
    }
}
