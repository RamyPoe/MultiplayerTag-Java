/*
UNUSED IMPORT
-------------------------------------------
import java.net.*;  
import java.io.*;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;  
import java.io.FileInputStream;  
import org.apache.poi.ss.usermodel.Cell;  
import org.apache.poi.ss.usermodel.Row;  
import org.apache.poi.xssf.usermodel.XSSFSheet;  
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.IOException;
import java.net.Socket;
*/

import dependencies.*;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import java.util.*;
import java.net.InetAddress;

public class Server
{
    //Game constants
    final static int[] skinCosts = {0, 50, 50, 50};
    final static int[] titleCosts = {20, 20, 30, 30, 30, 50, 50, 120};
    final static int[] chatCosts = {0, 0, 0, 20, 40, 40, 40, 50};

    //Server constants
    public static String HOSTNAME = "127.0.0.1";
    public final static int PORT = 5204;
    public final static long TIMEOUT = 5000;

    private static ServerSocketChannel serverChannel;
    private static Selector selector;
    
    //All GameRooms of active games, handled by seperate thread
    private volatile static Vector<GameRoom> gameRooms = new Vector<GameRoom>();
    //Keep track of queue rooms to match two players into lobby
    private static QueueRooms queueRooms = new QueueRooms();
    //Keep track of messages to be written to the clients
    public static Map<SocketChannel, byte[]> dataTracking = new HashMap<>();
    //Hold a "logged in" user's index in the authTable
    public static Map<SocketChannel, Integer> accountTracking = new HashMap<>();
    //Hold authentication info on all users
    static LogTable userAccounts;

    
    public static void main(String[] args)
    {
        try {
            //Dynamically set host up
            HOSTNAME = InetAddress.getLocalHost().toString().split("/")[1];
            
            //Display Server IP for connections
            System.out.println(HOSTNAME + "   |   " + PORT);
            System.out.println(InetAddress.getLocalHost() + "\n\n");

            //Create log table instance with max number of accounts
            userAccounts = new LogTable(40);

            //Create a server socket
            serverChannel = ServerSocketChannel.open();
            //Set as non-blocking so we can take advantage of selector
            serverChannel.configureBlocking(false);
            //Bind to address
            serverChannel.socket().bind(new InetSocketAddress(HOSTNAME, PORT));

            //Setup necessary selectors
            selector = Selector.open();

            //Register ServerSocket for incoming connections
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("[SERVER] Now Accepting Connections");

        while (true) {
            try {

                System.out.println("\n-----------------------------\n");
                System.out.println(queueRooms);


                //Wait for a bit, wait will end if there is a new event
                selector.select(TIMEOUT);

                //If we are here then we have events
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

                //Loop through each event/key/token
                while (keys.hasNext()) {
                    //Store key for processing
                    SelectionKey key = keys.next();
                    //Remove key so we don't loop through it again
                    keys.remove();

                    //Don't want to process invalid key, could happen if client disconnects
                    if (key.isValid() == false) {
                        continue;
                    }

                //Checking the key's event

                    //Wants to make connection (must be ServerSocket key)
                    if (key.isAcceptable()) {
                        //Accept Client connection
                        SocketChannel socketChannel = serverChannel.accept();
                        socketChannel.configureBlocking(false);
                        //Register client to selector so we can read from him
                        SelectionKey clientKey = socketChannel.register(selector, SelectionKey.OP_READ);
                        //Each client will have a unique encryption handshake with server
                        clientKey.attach(new AES_Cipher());
                    }

                    //Client is ready to be written to
                    if (key.isWritable()) {
                        SocketChannel channel = (SocketChannel) key.channel();

                        //There is nothing available to write to the client
                        if (!dataTracking.containsKey(channel)) {
                            continue;
                        }

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
                        System.out.println("[CLIENT] Writing --> " + new String(data));
                        //Get ready to read again, don't want to write twice in a row
                        key.interestOps(SelectionKey.OP_READ);
                    }

                    //Client sent data and can be read from
                    if (key.isReadable()) {
                        //Grab client's channel to read from
                        SocketChannel channel = (SocketChannel) key.channel();
                        String msg = readChannel(channel);
                        if (msg == "-1") {
                            key.cancel();
                            System.out.println("[CLIENT] Disconnected");
                            continue;
                        }

                    //-------------------------------------------------------------------
                        msg = msg.trim();
                        System.out.println("[MSG] " + msg);

                        AES_Cipher cipherAttachment = (AES_Cipher) key.attachment();
                        if (!cipherAttachment.ready) {
                            //Client must be sending their key
                            cipherAttachment.setReceiverPublicKeyString(msg.trim());
                            //Server cipher is now complete
                            cipherAttachment.ready = true;
                            //Add new attachment back to key
                            key.attach(cipherAttachment);
                            //Now send them your key
                            dataTracking.put(channel, cipherAttachment.getPublickeyString().getBytes());
                            key.interestOps(SelectionKey.OP_WRITE);
                        } else {

                            if (msg.startsWith("login: ") || msg.startsWith("signup: ")) {
                                //Parse client ip
                                String parsedIP = channel.getRemoteAddress().toString().replace("/", "").split(":")[0];
                                //We know that we'll have to write a response back
                                key.interestOps(SelectionKey.OP_WRITE);
                                String response = "";
                                String username = ""; //We might need this info later

                                if (msg.startsWith("login: ")) {
                                    //Remove prefix
                                    msg = msg.replace("login: ", "");
                                    //Decrypt the info
                                    msg = cipherAttachment.decrypt(msg.trim());
                                    
                                    //Process
                                    System.out.println("[LOGIN REQUEST] " + parsedIP + "   |   " + msg);

                                    String data[] = msg.split("\\.\\."); //You have to escape the characters
                                    System.out.println(Arrays.toString(data));
                                    username = data[0];

                                    response = userAccounts.authenticateLogin(data[0], data[1], parsedIP);

                                } else if (msg.startsWith("signup: ")) {
                                    //Remove prefix
                                    msg = msg.replace("signup: ", "");
                                    //Decrypt the info
                                    msg = cipherAttachment.decrypt(msg.trim());

                                    //Process
                                    System.out.println("[SIGN UP REQUEST] " + parsedIP + "   |   " + msg);

                                    String data[] = msg.split("\\.\\."); //You have to escape the characters
                                    System.out.println(Arrays.toString(data));

                                    response = userAccounts.authenticateSignup(data[0], data[1], parsedIP);

                                } else {
                                    //Sending illegal input, disconnect them
                                    key.cancel();
                                    disconnectClient(channel);
                                }

                                System.out.println("[RESPONSE] " + response);
                                dataTracking.put(channel, response.getBytes());
                                
                                if (response.startsWith("Successfully Logged in")) {
                                    //Add it to the logged in accounts list
                                    accountTracking.put(channel, userAccounts.getUserIndex(username));

                                    System.out.println("[SUCCESSFUL LOGIN] User: " + username + " is now logged in");
                                }

                            //Player pull request for their inventory once signed in
                            } else if (msg.startsWith("inventory")) {
                                try {
                                    dataTracking.put(channel, userAccounts.fetchInventory(accountTracking.get(channel)).getBytes());
                                    key.interestOps(SelectionKey.OP_WRITE);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    //Cancel the user since this error shouldn't happen normally
                                    disconnectClient(channel);
                                    key.cancel();
                                }

                            //Player signs out of currently logged in account
                            } else if (msg.startsWith("signout")) {
                                try {
                                    //If user was in a queue remove them
                                    queueRooms.removePlayer(accountTracking.get(channel));
                                    //Log user out and remove from hashMap
                                    userAccounts.logOut(accountTracking.get(channel));
                                    accountTracking.remove(channel);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    //Since this error shouldn't normally happen, kick the user if it does
                                    disconnectClient(channel);
                                    key.cancel();
                                }

                            //Player joins queueroom to start gameroom match
                            } else if (msg.startsWith("joinqueue")) {
                                //Add client to queue
                                Room roomPlayers = queueRooms.addPlayer(accountTracking.get(channel));
                                //If after the client was added, a room is now full, start a game with that room
                                if (roomPlayers.isFull()) {
                                    startGame(roomPlayers);
                                }

                            //Player leaves queue
                            } else if (msg.startsWith("leavequeue")) {
                                //Remove client from queue
                                try {
                                    queueRooms.removePlayer(accountTracking.get(channel));
                                } catch (Exception e) {
                                    System.out.println("[LEAVE QUEUE] Player is not logged in");
                                }
                                
                            //Player wants to buy an item
                            } else if (msg.startsWith("buy")) {
                                //Parse item id from the message
                                int item = Integer.parseInt(msg.split("buy ")[1]);

                                //Verify that the purchase was possible to make
                                if (itemValue(item) > Integer.parseInt(userAccounts.authTable[accountTracking.get(channel)].coins)) {
                                    continue;
                                }


                                //Item can be afforded, subtract from coins
                                userAccounts.authTable[accountTracking.get(channel)].coins = String.valueOf(Integer.parseInt(userAccounts.authTable[accountTracking.get(channel)].coins)-itemValue(item));
                                //Write new coins to file
                                userAccounts.WriteCellData(userAccounts.authTable[accountTracking.get(channel)].coins, accountTracking.get(channel), 3);

                                //Add skin to player inventory
                                String inv = userAccounts.authTable[accountTracking.get(channel)].skins;
                                int invArray[] = new int[(inv.length()-1)/3 + 1];

                                //Create an array for the skins
                                inv = inv.replace("s", "");
                                for (int i = 0; i < invArray.length-1; i++) {
                                    String itemId = "";

                                    itemId += String.valueOf(inv.charAt(3*i+0));
                                    itemId += String.valueOf(inv.charAt(3*i+1));
                                    itemId += String.valueOf(inv.charAt(3*i+2));

                                    invArray[i] = Integer.parseInt(itemId);
                                }

                                //Append the new item
                                invArray[invArray.length-1] = item;

                                //Sort array
                                Arrays.sort(invArray);

                                //Change back to string array
                                String[] newInv = new String[invArray.length];
                                for (int i = 0; i < newInv.length; i++) {
                                    newInv[i] = String.valueOf(invArray[i]);

                                    //Pad zeroes
                                    int len = newInv[i].length();
                                    for (int j = 0; j < 3-len; j++) {
                                        newInv[i] = "0" + newInv[i];
                                    }
                                }

                                //Change array to string
                                String finalInv = "s";
                                for (int i = 0; i < newInv.length; i++) {
                                    finalInv += newInv[i];
                                }

                                //Write new inv locally
                                userAccounts.authTable[accountTracking.get(channel)].skins = finalInv;

                                //Write new inv on file
                                userAccounts.WriteCellData(finalInv, accountTracking.get(channel), 4);

                                //Debug print
                                System.out.println("[NEW INVENTORY] " + finalInv);

                            }
                        }

                    }

                }

                handleRooms();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static int itemValue(int item) {
        if (item >= 100) {
            return chatCosts[item-100];
        } else if (item >= 50) {
            return titleCosts[item-50];
        } else {
            return skinCosts[item-1];
        }
    }

    private static void startGame(Room players) {
        //Get the socket channels from accountTracking from the room object
        SocketChannel channel1 = null;
        SocketChannel channel2 = null;

        for (Map.Entry<SocketChannel, Integer> entry : accountTracking.entrySet()) {
            if (channel1 == null && Objects.equals(players.accountNum1, entry.getValue())) {
                channel1 = entry.getKey();
            }
            if (channel2 == null && Objects.equals(players.accountNum2, entry.getValue())) {
                channel2 = entry.getKey();
            } else if (channel1 != null && channel2 != null) {
                break;
            }
        }
        
        //Get keys for the channels in main selector
        SelectionKey kp1 = channel1.keyFor(selector);
        SelectionKey kp2 = channel2.keyFor(selector);

        //Hold the client cipher info in the game thread temporarily
        AES_Cipher ep1 = (AES_Cipher) kp1.attachment();
        AES_Cipher ep2 = (AES_Cipher) kp2.attachment();
        
        //"Un-register" the channels from the main selector so it doesn't eat inputs
        kp1.interestOps(0);
        kp2.interestOps(0);
            
        //Create new thread of game room object
        gameRooms.addElement(new GameRoom(channel1, ep1, channel2, ep2, players));

        //Start the GameRoom thread
        Thread thread = new Thread(gameRooms.lastElement());
        thread.start();
        System.out.println("[GAME ROOM] Starting MATCH");


    }

    private static void handleRooms() {
        if (gameRooms.isEmpty()) return;

        System.out.println(gameRooms);

        Iterator<GameRoom> rooms = gameRooms.iterator();

        while (rooms.hasNext()) {
            GameRoom room = rooms.next();
            if (room.gameDone == false) {
                continue;
            }

            //Game is done
            try {

                //Write the rewards locally
                userAccounts.authTable[accountTracking.get(room.p1)].coins = String.valueOf(Integer.parseInt(userAccounts.authTable[accountTracking.get(room.p1)].coins) + (room.winner.equals(room.p1) ? 30 : 10));
                userAccounts.authTable[accountTracking.get(room.p2)].coins = String.valueOf(Integer.parseInt(userAccounts.authTable[accountTracking.get(room.p2)].coins) + (room.winner.equals(room.p2) ? 30 : 10));
                //Write rewards on file (column 3 represents coins)
                userAccounts.WriteCellData(userAccounts.authTable[accountTracking.get(room.p1)].coins, accountTracking.get(room.p1), 3);
                userAccounts.WriteCellData(userAccounts.authTable[accountTracking.get(room.p2)].coins, accountTracking.get(room.p2), 3);


                //Re-Register to main selector & Re-attach cipher
                if (room.winner != null) {
                    try {
                        SelectionKey key = room.winner.keyFor(selector);
                        key.interestOps(SelectionKey.OP_READ);
                        key.attach(room.winner.equals(room.p1) ? room.ep1 : room.ep2);
                    } catch (Exception e) {
                        //They disconnected, log them out
                        disconnectClient(room.winner);
                    }
                }
                if (room.loser != null) {
                    try {
                        SelectionKey key = room.loser.keyFor(selector);
                        key.interestOps(SelectionKey.OP_READ);
                        key.attach(room.loser.equals(room.p1) ? room.ep1 : room.ep2);
                    } catch (Exception e) {
                        //They disconnected, log them out
                        disconnectClient(room.loser);
                    }
                } 

                //Remove room since game is done
                rooms.remove();

            } catch (Exception e) {
                e.printStackTrace();
                rooms.remove();
            }

        }
    }

//---------------------------------------------------------------------------------------------------------------------

    private static String readChannel(SocketChannel channel) {
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
        return new String(data);
    }
    
    private static void disconnectClient(SocketChannel channel) {
        try {
            //Remove from the hashmaps
            dataTracking.remove(channel);
            if (accountTracking.containsKey(channel)) {
                userAccounts.logOut(accountTracking.get(channel));

                //If user was in a queue remove them
                queueRooms.removePlayer(accountTracking.get(channel));
                
                accountTracking.remove(channel);
            }
            //Close Channel
            channel.close();
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }

}