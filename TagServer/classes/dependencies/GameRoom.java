package dependencies;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.ByteBuffer;
import java.util.*;

public class GameRoom implements Runnable {
    Selector selector;
    public AES_Cipher ep1, ep2;

    boolean gameStarted = false;
    // int p1ID, p2ID;
    public SocketChannel p1, p2;
    boolean cosmeticSent;
    boolean p1Ready, p2Ready;

    String p1Buffer = ""; String p2Buffer = "";
    String p1Cosmetic, p2Cosmetic;
    //------------------------------

    GameState gameState = new GameState(new Coordinate(100, 185), new Coordinate(1080, 520));
    long tickTime = 0;

    Room players;
    public volatile boolean gameDone = false;
    public volatile SocketChannel winner;
    public volatile SocketChannel loser;

    //------------------------------
    //Keep track of messages to be written to the clients
    public static Map<SocketChannel, byte[]> dataTracking = new HashMap<>();

    //Constructor
    public GameRoom(SocketChannel p1, AES_Cipher ep1, SocketChannel p2, AES_Cipher ep2, Room players) {
        try {
            //Open new selector for this game
            selector = Selector.open();
            this.ep1 = ep1;
            this.ep2 = ep2;

            //Store channels
            this.p1 = p1;
            this.p2 = p2;
            this.players = players;

            //Setup cosmetic storage
            p1Cosmetic = null;
            p2Cosmetic = null;

            //Register the players to the selector so we can communicate with them
            p1.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            p2.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        //Want to send a "gameStart" signal to clients first with their id (Starting it or not-it)
        dataTracking.put(p1, "gameStart1".getBytes()); //Starts being it
        dataTracking.put(p2, "gameStart2".getBytes()); //Starts being not-it
        System.out.println("[GAME SERVER] Sent start signal");

        while (true) {
            try {
                //16ms max wait time per iteration
                selector.select(16);
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

                //--------------------------------------------------------------------------

                //Tell players that game has started/match found and exchange cosmetic info
                if (!gameStarted) {
                    //If both players have sent their skins, we must send them the other person's skin
                    if (p1Cosmetic != null && p2Cosmetic != null) {
                        dataTracking.put(p1, p2Cosmetic.getBytes());
                        dataTracking.put(p2, p1Cosmetic.getBytes());

                        System.out.println("[COSMETIC DATA] P1: " + p1Cosmetic + "  P2: " + p2Cosmetic);

                        //Reset so we don't send that info again
                        p1Cosmetic = null;
                        p2Cosmetic = null;
                        //Remember that this step has been completed
                        cosmeticSent = true;
                    }

                    //Writes whatever is in the hashmap
                    if (dataTracking.containsKey(p1)) {
                        write(p1);
                    }
                    if (dataTracking.containsKey(p2)) {
                        write(p2);
                    }

                    //Check if both players have sent all info/are ready to start game
                    if (p1Ready && p2Ready) {
                        gameStarted = true;
                        //Start the first frame 200ms late
                        tickTime = System.nanoTime()+200_000_000l;
                    }
                }

                //-----------------------------------------------------------------------------------


                while (keys.hasNext()) {

                    SelectionKey key = keys.next(); 
                    keys.remove();
        
                    //Make sure key is valid
                    if (!key.isValid()) {continue;}
        
                    SocketChannel channel = (SocketChannel) key.channel();
        
                    //Tell players that game has started/match found and exchange cosmetic info
                    if (!gameStarted) {

                        //Client is sending their cosmetic info
                        if (key.isReadable()) {
                            // System.out.println("READING");
                            String info = readChannel(channel);

                            if (info.startsWith("cosmetic")) {
                                //Store the player's cosmetic info to be sent later
                                if (p1.equals(channel)) {
                                    p1Cosmetic = info;
    
                                //Store the player's cosmetic info to be sent later
                                } else if (p2.equals(channel)) {
                                    p2Cosmetic = info;
                                }
                            } else if (info.startsWith("ready")) { //Client ready to start game
                                //Check which player is ready since we need both
                                if (p1.equals(channel)) {
                                    p1Ready = true;
                                } else if(p2.equals(channel)) {
                                    p2Ready = true;
                                }
                            }

                        }

                    }
                    //Both clients are now ready, we apply their inputs to the game
                    else if (gameStarted) {
                        if (key.isReadable()) {

                            if (p1.equals(channel)) {
                                String incoming = readChannel(channel); 
                                //Handle player disconnect
                                if (incoming == "-1") {
                                    //p1 disconnected so p2 wins
                                    endGame(2);
                                    return;
                                }
                                
                                p1Buffer += incoming;
                                //Process complete messages into gameState
                                String[] inputs = getInputs(p1Buffer);
                                // System.out.print("[P1 BUFFER] BEFORE:" + p1Buffer);
                                //"Subtract" the complete inputs from the buffer
                                p1Buffer = p1Buffer.replace(String.join(";", inputs)+";", "");
                                //Go through the inputs and apply them to the game
                                for (String input : inputs) {
                                    gameState.movePlayer(1, input);
                                }
                                // System.out.println("  |  AFTER: " + p1Buffer);
    
                            } else if (p2.equals(channel)) {
                                String incoming = readChannel(channel);

                                //Handle player disconnect
                                if (incoming == "-1") {
                                    //p2 disconncted so p1 wins
                                    endGame(1);
                                    return;
                                }

                                p2Buffer += incoming;
                                //Process complete messages into gameState
                                String[] inputs = getInputs(p2Buffer);
                                // System.out.print("[P2 BUFFER] BEFORE:" + p2Buffer);
                                //"Subtract" the complete inputs from the buffer
                                p2Buffer = p2Buffer.replace(String.join(";", inputs)+";", "");
                                //Go through the inputs and apply them to the game
                                for (String input : inputs) {
                                    gameState.movePlayer(2, input);
                                }
                                // System.out.println("  |  AFTER: " + p2Buffer);
                            }

                        }
                    }
                }
                
                //Don't want to send gameState if game hasn't started
                if (!gameStarted) {
                    continue;
                }


                //Every (17ms) (58.8hz)
                if (System.nanoTime()-tickTime >= 17_000000) {
                    tickTime += 17_000000;
                    
                    if (gameState.startCountdown) gameState.advanceClock();

                    //Send the gamestate to each client
                    // System.out.println("[GAME SERVER] P1 TICK: " + gameState.getState(1) + "   |   P2 TICK: " + gameState.getState(2));
                    dataTracking.put(p1, gameState.getState(1).getBytes());
                    write(p1);

                    dataTracking.put(p2, gameState.getState(2).getBytes());
                    write(p2);

                    //END THE GAME, DECIDE WINNER, DISTRIBUTE REWARDS
                    if (gameState.secCountdown == 0) {
                        //If player one is it, then player two wins (vice versa)
                        endGame(gameState.p1It ? 2 : 1);
                        return;
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


//--------------------------------------------------------------------------------------------------------------------------------

    private void endGame(int pWin) {
        if (pWin != 1 && pWin != 2) {
            System.out.println("[END GAME] BAD INPUT");
            return;
        }

        String win = (pWin == 1 ? "0" : "1");

        //Tell players how the game ended
        byte[] result = ("gamefinish," + win).getBytes();
        //Send game result
        dataTracking.put(p1, result);
        write(p1);
        dataTracking.put(p2, result);
        write(p2);
        //Distribute rewards
        winner = pWin == 1 ? p1 : p2;
        loser = pWin == 2 ? p1: p2;
        gameDone = true;
        //End game thread after calling this
    }


    private String[] getInputs(String inputs) {
        //We don't have any complete messages
        if (!inputs.contains(";")) {
            return new String[0];
        }
        //We have at least one complete message
        else {
            //Find the index of the LAST delimeter (reverse and find the first one)
            int endIndex = new StringBuilder(inputs).reverse().toString().indexOf(";");
            endIndex = inputs.length()-1-endIndex;

            //Make an array consisting of complete messages
            String[] temp = inputs.substring(0, endIndex).split(";");
            //Return the array of complete messages
            // System.out.println("[GET INPUTS] " + Arrays.toString(temp));
            return temp;
        }
    }

    private void write(SocketChannel channel) {
        try {
            //Get the data to write
            byte[] temp = dataTracking.get(channel);
            //Add terminating character
            byte[] data = new byte[temp.length+1];
            System.arraycopy(temp, 0, data, 0, temp.length);
            data[data.length-1] = (byte) '\n';
            //Remove this entry since we don't want to send info twice
            dataTracking.remove(channel);

            //Put data in ByteBuffer for the channel to write
            channel.write(ByteBuffer.wrap(data));
            // System.out.println("[GameServer] Writing --> " + new String(data));

        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    private String readChannel(SocketChannel channel) {
        //Alocate bytes of space to read (Max amount)
        ByteBuffer readBuffer = ByteBuffer.allocate(1024);
        readBuffer.clear();

        int read = 0; //Number of bytes read
        try {
            read = channel.read(readBuffer);
        } catch (Exception e) {
            System.out.println("[SERVER] Reading error, disconnecting client...");
            e.printStackTrace();
            //Disconnect the client
            disconnectClient(channel);
            return "-1";
            // continue;
        }

        if (read == -1 || read == 0) {
            System.out.println("[SERVER] Reading error, disconnecting client...");
            //Disconnect the client
            disconnectClient(channel);
            return "-1";
            // continue;
        }

        //If writing to buffer from channel was successful, change buffer to read mode
        readBuffer.flip();
        byte[] data = new byte[1024];
        //Read from beginning up until the amount that was written to buffer (read var)
        readBuffer.get(data, 0, read);
        // System.out.println("[CLIENT] " + new String(data));
        return new String(data);
    }

    private void disconnectClient(SocketChannel channel) {
        //Handle client disconnect/error
        try {
            channel.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

}

class GameState {
    Coordinate p1Pos, p2Pos;
    int pSize = 100; //Length/Width of player
    int playerVelocity = 10;
    boolean touching; //if players are stacked
    
    //We only need to know if p1 is it (p2 will be opposite of p1)
    boolean p1It = true;

    //Time needed to evade for win
    final int TIME_WIN = 6;

    long startTime;
    double secCountdown = TIME_WIN-0.1;
    boolean startCountdown = false;

    //For client side predicition/server reconciliation
    int p1Tick = 0;
    int p2Tick = 0;

    //Quick chat
    int p1ChatId = 0;
    int p2ChatId = 0;

    GameState(Coordinate p1, Coordinate p2) {
        p1Pos = p1;
        p2Pos = p2;
    }

    public String getState(int player) {
        return p1Pos.toString() + "," + p2Pos.toString() + "," + (p1It ? "1" : "0") + "," + String.valueOf(player == 1 ? p1Tick : p2Tick) + "," + getCountdown() + "," + (player == 1 ? p2ChatId : p1ChatId);
    }
    

    public void movePlayer(int player, String input) {

        //Change the player position and save quick chat id
        Coordinate pos;
        if (player == 1) {
            pos = p1Pos;
            p1Tick = Integer.parseInt(input.split(",")[0].split(":")[1].trim());
            p1ChatId = Integer.parseInt(input.split(",")[0].split(":")[0].trim());
        } else { //Player == 2
            pos = p2Pos;
            p2Tick = Integer.parseInt(input.split(",")[0].split(":")[1].trim());
            p2ChatId = Integer.parseInt(input.split(",")[0].split(":")[0].trim());
        }

        input = input.split(",")[1];

        //Move the character according to client input
        input = paddZero(Integer.toBinaryString(Integer.parseInt(input.trim())), 4);
        for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i) == '0') {
                continue;
            }

            switch (i) {
                case 0:
                    pos.x -= playerVelocity;
                    break;
                case 1:
                    pos.y -= playerVelocity;
                    break;
                case 2:
                    pos.x += playerVelocity;
                    break;
                case 3:
                    pos.y += playerVelocity;
                    break;
            }
        }

        //Make sure player is within game bounds
        if (pos.x > 1280-pSize) {
            pos.x = 1280-pSize;
        } else if (pos.x < 0) {
            pos.x = 0;
        }
        if (pos.y < 85) {
            pos.y = 85;
        } else if (pos.y > 720-pSize) {
            pos.y = 720-pSize;
        }

        //Check for collision
        if (collideRect(p1Pos, p2Pos, pSize)) {
            if (!startCountdown) 
                startCountdown = true;

            if (touching == false) {
                p1It = !p1It;
                touching = true;
                resetClock();
            }
        } else if (touching == true) {
            touching = false;
        }

    }

    private boolean collideRect(Coordinate p1, Coordinate p2, int pSize) {
        if (p1.x < p2.x+pSize && p1.x+pSize > p2.x && p1.y < p2.y+pSize && p1.y+pSize > p2.y) {
            return true;
        }
        return false;
    }

    public static String paddZero(String input, int numZero) {
        int amount = numZero-input.length();
        for (int i = 0; i < amount; i++) {
            input = "0" + input;
        }
        return input;
    }

    private void resetClock() {
        secCountdown = TIME_WIN-0.0000001;
        startTime = System.nanoTime();
    }

    public void advanceClock() {
        // System.out.println((System.nanoTime() - startTime));
        secCountdown = (TIME_WIN-0.001) - (System.nanoTime() - startTime)/1_000_000_000.0;
        if (secCountdown < 0) secCountdown = 0;
    }

    public String getCountdown() {
        //Serialized to send over to client
        return String.valueOf((int)(secCountdown*10));
    }

}

class Coordinate {
    int x;
    int y;

    Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return x + "," + y;
    }
}