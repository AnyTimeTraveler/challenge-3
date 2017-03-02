package protocol;

import java.util.Random;

/**
 * A fairly trivial Medium Access Control scheme.
 *
 * @author Jaco ter Braak, Twente University
 * @version 05-12-2013
 */
public class TurnBasedProtocol implements IMACProtocol {

    public static int NODE_COUNT = -1;
    private static final int QUEUE_LENGTH = 5;
    private static final int NEXT_TURN = -1;
    private boolean turnInitialized;

    private Random random;
    private int id;
    private int nextId;
    private int currentTurn;
    private Action lastAction;
    private int partialQueue;
    private boolean turnMode;
    private boolean justHadMyTurn;

    TurnBasedProtocol() {
        this.random = new Random();
        this.id = 0;
        this.nextId = 1;
        this.currentTurn = 1;
        this.partialQueue = 0;
        this.turnMode = false;
        this.turnInitialized = false;
    }

    public static void setNodeCount(int nodeCount){
        NODE_COUNT = nodeCount;
    }

    @Override
    public TransmissionInfo TimeslotAvailable(MediumState previousMediumState, int controlInformation, int localQueueLength) {
        if (turnMode) {
            return sendTurnBased(previousMediumState, controlInformation, localQueueLength);
        }
        if (controlInformation == NODE_COUNT) {
            turnMode = true;
        }
        if (id == 0) {
            if (previousMediumState.equals(MediumState.Succes)) {
                if (lastAction == Action.SENDING) {
                    this.id = controlInformation;
                    System.out.println("I'm ID " + id);
                    lastAction = Action.WAITING;
                    return new TransmissionInfo(TransmissionType.Silent, 0);
                } else {
                    nextId++;
                    return sendRandom(previousMediumState, controlInformation, localQueueLength);
                }
            } else {
                return sendRandom(previousMediumState, controlInformation, localQueueLength);
            }
        } else if (nextId != NODE_COUNT + 1) {
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
        if (random.nextInt(100) <= (100 / NODE_COUNT) * nextId) {
            System.out.println("true");
            lastAction = Action.SENDING;
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

    public TransmissionInfo sendTurnBased(MediumState previousMediumState, int controlInformation, int localQueueLength) {
        System.out.print("STATE: " + previousMediumState + "    Info: " + controlInformation +
                                 "    Queue: " + localQueueLength);
        if (currentTurn == id) {
            TransmissionInfo transmissionInfo = doMyTurn(localQueueLength);
            System.out.println(currentTurn == id ? "Still my turn" : "I'm done with my turn");
            justHadMyTurn = true;
            return transmissionInfo;
        } else {
            turnInitialized = false;
            if (controlInformation == NEXT_TURN && !justHadMyTurn) {
                System.out.print("    Old Turn: " + currentTurn);
                currentTurn = currentTurn + 1 == NODE_COUNT + 1 ? 1 : currentTurn + 1;
                System.out.println("    New Turn: " + currentTurn);
            } else {
                justHadMyTurn = false;
                System.out.println();
            }
            if (currentTurn == id) {
                justHadMyTurn = true;
                return doMyTurn(localQueueLength);
            }
            return new TransmissionInfo(TransmissionType.Silent, 0);
        }
    }

    private TransmissionInfo doMyTurn(int localQueueLength) {
        if (!turnInitialized) {
            turnInitialized = true;
            partialQueue = QUEUE_LENGTH > localQueueLength ? localQueueLength : QUEUE_LENGTH;
        }
        System.out.println("    My turn: " + id + "    Queue: " + partialQueue);
        if (--partialQueue <= 0 || localQueueLength <= 1) {
            currentTurn = currentTurn + 1 == NODE_COUNT + 1 ? 1 : currentTurn + 1;
        }
        if (localQueueLength > 0) {
            return new TransmissionInfo(TransmissionType.Data, partialQueue == 0 ? NEXT_TURN : partialQueue);
        } else {
            return new TransmissionInfo(TransmissionType.NoData, NEXT_TURN);
        }
    }

    private enum Action {
        SENDING,
        WAITING
    }
}
