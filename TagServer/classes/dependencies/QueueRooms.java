package dependencies;

import java.util.*;


public class QueueRooms {
    //Hold all Room objects to see if we can get a match together
    private Vector<Room> queueRooms = new Vector<Room>();

    //Will return room if it fills a lobby
    public Room addPlayer(int accountNum) {
        //If there are no rooms make one
        if (queueRooms.isEmpty()) {
            queueRooms.addElement(new Room());
            //Join the one existing room
            queueRooms.elementAt(0).addPlayer(accountNum);
            //Return empty room since we haven't filled a lobby;
            return new Room();
        }

        //Check last room to see if its open
        if (!queueRooms.lastElement().isFull()) {
            if (queueRooms.lastElement().hasPlayer(accountNum)) {
                System.out.println("[QUEUE ROOMS] Player :" + accountNum + ": is already in queue");
                return new Room();
            }

            queueRooms.lastElement().addPlayer(accountNum);
            //Guaranteed to be full
            Room result = queueRooms.lastElement();
            //Remove the room from the list
            queueRooms.removeElementAt(queueRooms.size()-1);
            //Return it so a game can be started between them
            return result;
        //If the last room isn't open then open a new room
        } else {
            queueRooms.addElement(new Room());
            queueRooms.lastElement().addPlayer(accountNum);
            //Return empty room since we haven't filled a lobby;
            return new Room();
        }
    }

    public void removePlayer(int accountNum) {
        //Go through all rooms to find the player
        for (int i = 0; i < queueRooms.size(); i++) {
            //Find room that has the player
            if (queueRooms.elementAt(i).hasPlayer(accountNum)) {
                //Remove the player from that room
                queueRooms.elementAt(i).removePlayer(accountNum);
                //If the room is empty delete the room
                if (queueRooms.elementAt(i).isEmpty()) {
                    queueRooms.removeElementAt(i);
                    //No point in checking any other rooms
                    return;
                }
                //If there were two people, and one person remains
                else {
                    //If last room has a spot
                    if (!queueRooms.lastElement().isFull()) {
                        //Make sure YOU aren't the last room
                        if (queueRooms.size()-1 != i) {
                            int account = queueRooms.elementAt(i).getOnePlayer();
                            queueRooms.removeElementAt(i);
                            queueRooms.lastElement().addPlayer(account);
                            //No point in checking any other rooms
                            return;
                        }
                    }
                    //Last room doesn't have spot, make new room at end to move the player to
                    else {
                        queueRooms.addElement(new Room());
                        int account = queueRooms.elementAt(i).getOnePlayer();
                        queueRooms.removeElementAt(i);
                        queueRooms.lastElement().addPlayer(account);
                        //No point in checking any other rooms
                        return;
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        String result = "";
        result += "{";
        for (int i = 0; i < queueRooms.size(); i++) {
            result += queueRooms.elementAt(i).toString();
            if (i != queueRooms.size()-1) {
                result += ", ";
            }
        }
        result += "}";
        return result;
    }
    
}
