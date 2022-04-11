import java.util.LinkedList;
import java.util.Queue;
import java.util.Iterator;
import java.util.List;

class GameWindow {
  //Atrributes
  Client myClient;
  Profile stats;
  int ID;
  
  SetupGameInfo infoExchange;
  SendInputStream outStream;
  
  Player player;
  Player opponent;
  
  String[] titleDict;
  TitleBar titleBar;
  
  //Constructor
  GameWindow(int ID, Client myClient, Profile stats, String[] titleDict) {
    this.myClient = myClient;
    this.stats = stats;
    this.ID = ID;
    this.titleDict = titleDict;
    
    infoExchange = new SetupGameInfo(myClient);
    outStream = new SendInputStream(myClient);
  }
  
  //Methods
  void display() {
    //WHILE GAME RUNNING
    player.display();
    opponent.display();
    titleBar.display();
  }
  
  void setupGame() {
    println("[GAME WINDOW] SETTING UP GAME");
    titleBar = new TitleBar(stats.username, stats.title+" ", infoExchange.oppData[0], (Integer.parseInt(infoExchange.oppData[1]) != 0 ? titleDict[Integer.parseInt(infoExchange.oppData[1])-50] : " "));
    if (ID == 1) {
      player = new Player(100, 185, stats.equipped[0], true); //P1 starts it
      opponent = new Player(1080, 520, Integer.parseInt(infoExchange.oppData[2]), false);
    } else if (ID == 2) {
      player = new Player(1080, 520, stats.equipped[0], false); //P2 starts Not-it
      opponent = new Player(100, 185, Integer.parseInt(infoExchange.oppData[2]), true);
    }
    startOutStream();
  }
  
  void startOutStream() {
    Thread t = new Thread(outStream);
    t.start();
  }
  
}

class SendInputStream implements Runnable {
  public volatile boolean gameRunning = false;
  boolean startedSending;
  private int tickTime = millis();
  boolean[] keys = new boolean[5];  // [0] == LEFT, [1] == UP, [2] == RIGHT, [3] == DOWN, [4] == QUICK CHAT
  int chatId = 100;
  
  int tick = 0;
  int playerVelocity = 10;
  
  Client myClient;
  public volatile boolean gameWon;
  
  SendInputStream(Client myClient) {
    this.myClient = myClient;
  }
  
  void run() {
    gameRunning = true;
    String outputMsg;
    Queue<String> inputList = new LinkedList<String>();
    Queue<int[]> interpolationSteps = new LinkedList<int[]>();
    int lastRecvTime = 0;
    double ping = 17f;
    
    while (gameRunning) {
      //println("SENDING", startedSending);
      if (myClient.available() > 0) {
        //Begin sending our input once we get a gameState (i.e server is ready)
        if (startedSending == false) {
          //We can start interpolating after receiving one state since we know the starting position
          interpolationSteps.add(new int[]{gameWindow.opponent.px, gameWindow.opponent.py});
          startedSending = true;
          lastRecvTime = millis() + (int) ping;
        }
        
        String[] data = myClient.readStringUntil(10).replace("\n", "").split(","); //Newline terminating character (10)
        
        //Check if game has ended-------------------
        if (data[0].startsWith("gamefinish")) {
          if (Integer.parseInt(data[1]) == 1) {
            if (gameWindow.ID == 1) {
              //LOST
              gameWon = false;
            } else {
              //WON
              gameWon = true;
            }
          } else {
            if (gameWindow.ID == 1) {
              //WON
              gameWon = true;
            } else {
              //LOST
              gameWon = false;
            }
          }
          
          //Close thread
          System.out.println("GAME DONE, WON GAME: " + gameWon);
          gameRunning = false;
          startedSending = false;
          return;
        }
        //-------------------------------------------
        
        int[] sPos = new int[2];
        int[] oPos = new int[2];
        
        if (gameWindow.ID == 1) {
          sPos[0] = Integer.parseInt(data[0]);
          sPos[1] = Integer.parseInt(data[1]);
          oPos[0] = Integer.parseInt(data[2]);
          oPos[1] = Integer.parseInt(data[3]);
          gameWindow.player.it = Integer.parseInt(data[4]) == 1 ? true : false;
          gameWindow.opponent.it = !gameWindow.player.it;
        } else if (gameWindow.ID == 2) {
          oPos[0] = Integer.parseInt(data[0]);
          oPos[1] = Integer.parseInt(data[1]);
          sPos[0] = Integer.parseInt(data[2]);
          sPos[1] = Integer.parseInt(data[3]);
          gameWindow.player.it = Integer.parseInt(data[4]) == 1 ? false : true;
          gameWindow.opponent.it = !gameWindow.player.it;
        }
        
        //println("[SERVER] " + Arrays.toString(data));
        
        //QUICK CHAT
        if (Integer.parseInt(data[7]) != 0) {
          //println("[QUICK CHAT] " + chatDict[Integer.parseInt(data[7])-100]);
          gameWindow.titleBar.oChat.quickChat(chatDict[Integer.parseInt(data[7])-100]);
        }
        
        
        //DISPLAY
        gameWindow.titleBar.it = gameWindow.player.it;
        gameWindow.titleBar.countdown = Double.parseDouble(data[6])/10.0;
        
        //INTERPOLATION-------------------------------------------------------------------------
        interpolationSteps.add(oPos);

        
        //SERVER RECONCILIATION------------------------------------------------------------------
        for (Iterator<String> iterator = inputList.iterator(); iterator.hasNext(); ) {
          String item = iterator.next();
          if (Integer.parseInt(item.split(",")[0]) <= Integer.parseInt(data[5])) {
            iterator.remove();
          } else {
            break;
          }
        }
        
        int[] newPos = sPos;
        for (String input : inputList) {
          newPos = applyMovement(newPos, input.split(",")[1]);
        }
        
        gameWindow.player.px = newPos[0];
        gameWindow.player.py = newPos[1];
      }
      
      
      //INTERPOLATION-------------------------------------------------------------------------

      ///*
        if (millis()-lastRecvTime >= ping) {
          //println("T: " + tick + "  |  SIZE: " + interpolationSteps.size() + "  |  PING: " + ping);
          
          if (interpolationSteps.size() >= 2) {
            
            //Remove step from queue
            interpolationSteps.poll();
            lastRecvTime = millis();
                        
            //Snap to position
            gameWindow.opponent.px = interpolationSteps.peek()[0];
            gameWindow.opponent.py = interpolationSteps.peek()[1];
            
            //If we have too many steps, skip one
            if (interpolationSteps.size() > 2) {
              interpolationSteps.poll();
              gameWindow.opponent.px = interpolationSteps.peek()[0];
              gameWindow.opponent.py = interpolationSteps.peek()[1];
            }
            
          }
          
        }
        
        if (interpolationSteps.size() >= 2) {
          List<int[]> steps = (List<int[]>) interpolationSteps;
          gameWindow.opponent.px = (int) lerp(steps.get(0)[0], steps.get(1)[0],  (float) Math.min((millis()-lastRecvTime)/ping, 1));
          gameWindow.opponent.py = (int) lerp(steps.get(0)[1], steps.get(1)[1], (float) Math.min((millis()-lastRecvTime)/ping, 1));
        }
        
        //*/
      
      
      //SEND INPUT TO SERVER---------------------------------------------------------------------------------
      
      if (!startedSending) {
        continue;
      }
      
      if (millis()-tickTime > 20) {
        tickTime += 20;
      } else {
        continue;
      }
      
      
      //Runs at 50hz
      String output = "";
      for (int i = 0; i < 4; i++) { 
        output += keys[i] ? "1" : "0";
      }
      
      outputMsg = (keys[4] && settings.chatEnabled ? String.valueOf(chatId) : "0") + ":" + String.valueOf(tick) + "," + String.valueOf(Integer.parseInt(output, 2)) + ";";
      //println("[WRITING TO SERVER] " + outputMsg);
      myClient.write(outputMsg);
      
      //Add Input to list
      inputList.add(String.valueOf(tick) + "," + output);
        
        
      //APPLY CLIENT-SIDE PREDICTION----------------------------------------------------------------------------
      
      int[] newPos = applyMovement(new int[]{gameWindow.player.px, gameWindow.player.py}, output);
      gameWindow.player.px = newPos[0];
      gameWindow.player.py = newPos[1];
      
      //Show quickChat
      if (keys[4] && settings.chatEnabled) gameWindow.titleBar.pChat.quickChat(chatDict[chatId-100]);
      
      
      //Advance tick
      tick++;
    }
  
  }
  
  private int[] applyMovement(int[] pos, String binary) {
      //CLIENT-SIDE PREDICTION
      int px = pos[0];
      int py = pos[1];
      
      for (int i = 0; i < binary.length(); i++) {
          if (binary.charAt(i) == '0') {
              continue;
          }

          switch (i) {
              case 0:
                  px -= playerVelocity;
                  break;
              case 1:
                  py -= playerVelocity;
                  break;
              case 2:
                  px += playerVelocity;
                  break;
              case 3:
                  py += playerVelocity;
                  break;
            }
        }

        //Make sure player is within game bounds
        if (px > 1280-gameWindow.player.size) {
            px = 1280-gameWindow.player.size;
        } else if (px < 0) {
            px = 0;
        }
        if (py < 85) {
            py = 85;
        } else if (py > 720-gameWindow.player.size) {
            py = 720-gameWindow.player.size;
        }
        
        return new int[]{px, py};
  }
  
}

class SetupGameInfo {
  //Attributes
  private Client myClient;
  
  //Expect to get 3 pieces of info
  public String[] oppData = new String[3]; 
  
  //Constructor
  SetupGameInfo(Client myClient) {
    this.myClient = myClient;
  }
  
  //Main method
  public void run() {
    System.out.println("SENDING INFO TO SERVER");
    //Send the server your cosmetics first
    myClient.write("cosmetic " + stats.username + ":" + stats.equipped[1] + ":" + stats.equipped[0]);
    System.out.println("INFO SENT: " + "cosmetic " + stats.username + ":" + stats.equipped[1] + ":" + stats.equipped[0]);
    //Recieve opponent's cosmetic info
    String oppCosmetic = clientRecv(myClient);
    
    oppData = oppCosmetic.replace("cosmetic ", "").split(":");
    
    oppData[0] = oppData[0].replaceAll("[^a-zA-Z0-9;, ]", "");
    oppData[1] = oppData[1].replaceAll("[^a-zA-Z0-9;, ]", "");
    oppData[2] = oppData[2].replaceAll("[^a-zA-Z0-9;, ]", "");
    println("[SERVER] " + Arrays.toString(oppData));
  }
  
  //Methods
  private String clientRecv(Client myClient) {
    String dataIn;
    while (myClient.available() == 0) {}
  
    dataIn = myClient.readStringUntil(10); //Newline terminating character (10)
    dataIn = dataIn.replace("\n", ""); //Remove trailing character
    println("[SERVER] " + dataIn);
    
    return dataIn;
  }
}

class TitleBar {
  //For displaying quick chat
  class QuickChat {
    //TEXT FOR CHAT
    String text;
    
    //FOR ANIMATION
    final float max_size = 120;
    int size = 0;
    long time_start;
    boolean active;
    
    //CENTER
    int x, y;
    
    //FOR DRAWING
    boolean direction;
    
    
    QuickChat(int x, int y) {
      this.x = x;
      this.y = y;
    }
    
    void display() {
      if (!active) return;
      
      if (millis()-time_start > 3000) {
        active = false;
        return;
      }
      
      //============================
      //Animation
      //============================
      
      //Grow animation 0.5s
      long difference = millis()-time_start;
      if (difference < 500) {
        size = (int)(max_size * (difference / 500.0));
      
      //Show for 2s
      } else if (difference >= 500 && difference < 2500) {
        size = (int) max_size;
        
      //Shrink animation 0.5s
      } else if (difference <= 3000) {
        size = (int)(100.0 * (1-((difference-2500) / 500.0)));
      }
      
      //========================
      //Drawing chat text
      //========================
      
      //Background rectangle
      rectMode(CENTER);
      strokeWeight(2);
      stroke(130);
      fill(240);
      rect(x, y, size, size/2);
      
      //Text in the chat box
      textAlign(CENTER, CENTER);
      fill(5);
      textSize(((size+1)/max_size)*20);
      text(text, x, y);
      
      //Circles to make like chat box
      ellipseMode(RADIUS);
      fill(240);
      if (!direction) {
        ellipse(x-size/2*1.3, y+size/4, size/13, size/13);
        ellipse(x-size/2*1.7, y+size/4, size/13, size/13);
      } else {
        ellipse(x+size/2*1.3, y+size/4, size/13, size/13);
        ellipse(x+size/2*1.7, y+size/4, size/13, size/13);
      }
      
      
    }
    
    void quickChat(String text) {
      if (active) return;
      
      println("[CHAT] " + text);
      time_start = millis();
      this.text = text;
      active = true;
    }
  }
  
  
  //Attributes
  int w, h;
  boolean it;
  
  String username, title, oppUsername, oppTitle;
  
  double countdown;
  
  //FOR QUICKCHAT
  QuickChat pChat = new QuickChat(200+120/2, 80/2);
  QuickChat oChat = new QuickChat(1080-120/2, 80/2);
  
  //Constructor
  TitleBar(String username, String title, String oppUsername, String oppTitle) {
    h = 85;
    w = width;
    countdown = 5.999999;
    
    oChat.direction = true;
        
    this.username = username;
    this.title = title;
    
    this.oppUsername = oppUsername;
    this.oppTitle = oppTitle;
    
    if (gameWindow.ID == 1) {
      it = true;
    } else {
      it = false;
    }
  }
  
  //Methods
  void display() {    
    //Rect Background
    rectMode(CORNER);
    fill(0);
    noStroke();
    rect(0, 0, w, h);
    
    //Player names
    textSize(40);
    if (title == " ") {
        //YOU
      textAlign(LEFT, CENTER);
      if (it) { fill(272, 73, 73); } else { fill(40, 116, 247); }
      text(username, 0+7, h/2*0.75);
        //OPPONENT
      textAlign(RIGHT, CENTER);
      if (!it) { fill(272, 73, 73); } else { fill(40, 116, 247); }
      text(oppUsername, w-7, h/2*0.75);
    } else {
        //YOU
      textAlign(LEFT, CENTER);
      if (it) { fill(272, 73, 73); } else { fill(40, 116, 247); }
      text(username, 0+7, h*2/3*0.8);
        //OPPONENT
      textAlign(RIGHT, CENTER);
      if (!it) { fill(272, 73, 73); } else { fill(40, 116, 247); }
      text(oppUsername, w-7, h*2/3*0.8);
      
      //TITLES
      fill(180);
      textSize(24);
      textAlign(LEFT, CENTER);
      text(title, 0+7, h/3*0.8);
      
      textAlign(RIGHT, CENTER);
      text(oppTitle, w-7, h/3*0.8);
    }
    
    //Countdown
    fill(255);
    textSize(50);
    textAlign(CENTER, CENTER);
    text(String.valueOf((int)(Math.ceil(countdown))), width/2, h/2);
    
  }
  
}


void keyPressed() {
   //Overwrite default escape key functionality
   if (keyCode == 27) {
    key = 0;
    println ("Escape key pressed");
  }
  
  //For getting input
  if (MENU == 4) {
    codeKey = keyCode;
  }
  
  if (MENU != 6 || !gameWindow.outStream.gameRunning) {
    return;
  }
  
  //PLAYER MOVEMENT
  if (keyCode == settings.leftKey) {
    gameWindow.outStream.keys[0] = true;
  } else if (keyCode == settings.upKey) {
    gameWindow.outStream.keys[1] = true;
  } else if (keyCode == settings.rightKey) {
    gameWindow.outStream.keys[2] = true;
  } else if (keyCode == settings.downKey) {
    gameWindow.outStream.keys[3] = true;
    
  //QUICK CHAT
  } else if (keyCode == settings.chat1) {
    gameWindow.outStream.keys[4] = true;
    gameWindow.outStream.chatId = stats.equipped[2];
  } else if (keyCode == settings.chat2) {
    gameWindow.outStream.keys[4] = true;
    gameWindow.outStream.chatId = stats.equipped[3];
  } else if (keyCode == settings.chat3) {
    gameWindow.outStream.keys[4] = true;
    gameWindow.outStream.chatId = stats.equipped[4];
  }
}

void keyReleased() {
  if (MENU != 6 || !gameWindow.outStream.gameRunning) {
    return;
  }
  
  //PLAYER MOVEMENT
  if (keyCode == settings.leftKey) {
    gameWindow.outStream.keys[0] = false;
  } else if (keyCode == settings.upKey) {
    gameWindow.outStream.keys[1] = false;
  } else if (keyCode == settings.rightKey) {
    gameWindow.outStream.keys[2] = false;
  } else if (keyCode == settings.downKey) {
    gameWindow.outStream.keys[3] = false;
    
  //QUICK CHAT
  } else if (keyCode == settings.chat1) {
    gameWindow.outStream.keys[4] = false;
  } else if (keyCode == settings.chat2) {
    gameWindow.outStream.keys[4] = false;
  } else if (keyCode == settings.chat3) {
    gameWindow.outStream.keys[4] = false;
  }
   
  
  
}
