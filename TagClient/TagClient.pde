import processing.net.*; 
import controlP5.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import de.looksgood.ani.*; //For smooth animations

//-----------------------------------------------------------------------------------------------------------------

final String[] titleDict = {"The Great", "Legendary", "GOAT", "Undisputed", "Champion", "Ultimate", "Developer!?", "LOSER"};
final String[] chatDict = {"Good Game!", "Close one!", "Wow", "Nice move!", "YIKES", "00f", "RIP", "get good..."};

final int[] skinCosts = {0, 50, 50, 50};
final int[] titleCosts = {20, 20, 30, 30, 30, 50, 50, 120};
final int[] chatCosts = {0, 0, 0, 20, 40, 40, 40, 50};

//------------------------------------------------------------------------------------------------------------------

int codeKey;

Settings settings = new Settings();
SettingsMenu settingsMenu;

int MENU = 1; //Start at Main menu screen
int changeTo = 1; //The menu to change to
boolean gameStarted;
boolean stopCheckMatch = false;

ControlP5 cp5;
Client myClient;
AES_Cipher cipherGen = new AES_Cipher();
String dataIn = "";

Pattern validInput = Pattern.compile("[^a-zA-Z0-9]"); ///Anything that is not (^) a-z, A-Z, 0-9
boolean terminate = false; //if disconnected from server
int matchFound = 0; //Indiciate if a gameroom was found with another player, holds the id that the server sent

Profile stats = new Profile();
PlayerPreview playerPreview = new PlayerPreview(); //Will be passed to all other classes that need it
WarningPopup warningPopup = new WarningPopup(300, 150);
LoginPopup loginPopup = new LoginPopup(430, 270); //Size of login window popup
InventoryView inventoryView;
ShopView shopView;
GameWindow gameWindow;

int fade = 0;
boolean fadeIn = false;
boolean fadeOut = false;

Button queueButton = new Button("QUEUE", 30, 200+100*0);
Button customizeButton = new Button("INVENTORY", 30, 200+100*1);
Button shopButton = new Button("SHOP", 30, 200+100*2);
Button settingsButton = new Button("SETTINGS", 30, 200+100*3);
Button aboutButton = new Button("ABOUT", 30, 200+100*4);

//Animating gameStart
int sqSize = 230;
int xA = -sqSize; //595-sqSize
int yA = 400; 
int xB = 1280; //685
int yB = 400;
int panelY = 0;


QueueStatus queueStatus = new QueueStatus(450);
float popUpY = -35;

PImage logoImg;
PFont myFont;

//-----------------------------------------------------------------------------------------------------------

void setup() { 
  //Window dimensions
  size(1280, 720); 
    
  //Turn on anit-alias
  smooth(); 
  Ani.init(this);
  
  //Setup profile/stats
  logoImg = loadImage("assets/logo.png");
  stats.loadCoinsImage();
  stats.init();

  //Setup login popup
  ControlP5 cp5 = new ControlP5(this);
  loginPopup.loadCP5(cp5);
  loginPopup.init();
    
  // Connect to the server machine at port 5204.
  String serverIP = loadStrings("code/server.txt")[0].trim();
  
  myClient = new Client(this, serverIP, 5204);
  if (!myClient.active()) {
    terminate = true;
  } else {
    //Loading client into login class
    loginPopup.loadClient(myClient);
  
    //Make key Exchange with server
    thread("setupCipher");
  }
  
  background(205);
  
  //Set new default font
  myFont = createFont("assets/font.ttf", 32, true);
  textFont(myFont);
} 
 
void draw() {
//---------------------------------------------------------------------------------------
  if (terminate) {
    MENU = 9999;
    
    background(205);
    textAlign(CENTER, CENTER);
    fill(255);
    textSize(250);
    text("Server\nOffline!", width/2, height/2-20);
    
    delay(200);
  }
  
//---------------------------------------------------------------------------------------

  if (MENU == 1) {
    background(230);
    image(logoImg, 30, 30);
    
    //Cannot access the buttons without being logged in, display warning popup if attempted
    if (!stats.loggedIn) {
      if (queueButton.clicked() || customizeButton.clicked() || shopButton.clicked() || settingsButton.clicked() || aboutButton.clicked()) {
        println("[MENU BUTTON] Clicked!");
        warningPopup.enable("Please LOGIN to\naccess that feature");
      }
    }
    
    //While a popup menu is active, the buttons shouldn't be interactive
    queueButton.display(warningPopup.isActive || loginPopup.isActive);
    customizeButton.display(warningPopup.isActive || loginPopup.isActive);
    shopButton.display(warningPopup.isActive || loginPopup.isActive);
    settingsButton.display(warningPopup.isActive || loginPopup.isActive);
    aboutButton.display(warningPopup.isActive || loginPopup.isActive);
    
    //Shows if player is in queue or not
    queueStatus.display();
    
    //Animation for queue status
    if (popUpY != queueStatus.y) {queueStatus.y = popUpY;} 
    
    //If we are logged in, what we actually want the buttons to do
    if (stats.loggedIn) {
      //A Match has been found, switch to game screen
      if (queueStatus.y > 0 && matchFound != 0) {
        Ani.to(this, 1.5, "popUpY", -35);
        
        gameWindow = new GameWindow(matchFound, myClient, stats, titleDict);
        matchFound = 0;
        changeTo = 6;
        fadeOut = true;
        
        //SETUP FOR GAME
        sqSize = 230;
        xA = -sqSize; //595-sqSize
        yA = 400; 
        xB = 1280; //685
        yB = 400;
        panelY = 0;
      }
      
      if (queueButton.clicked()) {
        println("Queue button clicked");
        
        //We want to join queue
        if (queueStatus.y < -30) { 
          thread("enterQueue");
          stopCheckMatch = false;
          thread("checkMatch");

        //We want to leave queue
        } else { 
          thread("leaveQueue");
          stopCheckMatch = true;

        }
      } else if (customizeButton.clicked() && changeTo == 1) {
          
          if (queueStatus.y > -35) {
             warningPopup.enable("Please leave the queue\nto access that feature");
          } else {
            println("Customize Button clicked!");
            fadeOut = true;
            fadeIn = false;
            //2 is the inventory MENU
            changeTo = 2;
          }
          
      } else if (shopButton.clicked() && changeTo == 1) {
        
          if (queueStatus.y > -35) {
             warningPopup.enable("Please leave the queue\nto access that feature");
          } else {
            println("Shop Button clicked!");    
            fadeOut = true;
            fadeIn = false;
            //3 is the shop MENU
            changeTo = 3;
          }
          
      } else if (settingsButton.clicked()) {
        
          if (queueStatus.y > -35) {
             warningPopup.enable("Please leave the queue\nto access that feature");
          } else {
            println("Settings Button clicked!");
            fadeOut = true;
            fadeIn = false;
            //4 is the settings menu
            changeTo = 4;
          }
        
          
      } else if (aboutButton.clicked()) {
        
          if (queueStatus.y > -35) {
             warningPopup.enable("Please leave the queue\nto access that feature");
          } else {
            // ADD CREDITS !!!
            //Do stuff
          }
        
          println("About Button clicked!");
      }
    }

    //Top right profile
    stats.display();
    //Since stats will use a different font, we must change the font back
    textFont(myFont);
    
    //Draw player skin preview
    if (stats.loggedIn) {
      playerPreview.display();
    } else {
      playerPreview.reset();
    }
    
    
    loginPopup.display();
    warningPopup.display();
    
    //FADE
    if (fadeOut) {
      if (fade < 255) {
        fade += 5;
      } else {
        println("[DEBUG] Changed menu to: " + changeTo);
        
        //We are going to the inventory screen so we must setup everything for it
        if (changeTo == 2) {
          inventoryView = new InventoryView(stats.parsedCosmetics());
          inventoryView.init();
          
        //Exchange cosmetic info between clients before starting match
        } else if (changeTo == 6) {
          gameWindow.infoExchange.run();
          println("[GAME] Exchanged Info");
          gameWindow.setupGame();
          
        //Setup shop window instance before moving to that screen
        } else if (changeTo == 3) {
          shopView = new ShopView(stats.parsedCosmetics());
          shopView.init();
        } else if (changeTo == 4) {
          settingsMenu = new SettingsMenu();
        }
        
        MENU = changeTo;
        fade = 255;
        if (changeTo != 6) { fadeIn = true; }
        fadeOut = false;
      }
    }
    
    if (fadeIn) {
      if (fade > 0) {
        fade -= 5;
      } else {
        fadeIn = false;
        fadeOut = false;
        fade = 0;
      }
    }
    
    
    if (fadeIn || fadeOut) {
      rectMode(CORNER);
      noStroke();
      fill(0, fade);
      rect(0, 0, width, height);
    }
    
    if (!myClient.active()) {
      terminate = true;
    }

  } 
  if (MENU == 2) { //Inventory menu
    background(230);
       
    imageMode(CORNER);
    image(logoImg, 30, 30);
    
    //Since stats will use a different font we must change the font back
    stats.display();
    textFont(myFont);
    
    inventoryView.display();
    if (inventoryView.mainMenuPressed() && changeTo == 2 && !warningPopup.isActive) {
      changeTo = 1;
      fadeOut = true;
      fadeIn = false;
    }
    
    fadeAnim();
    
  }
  if (MENU == 3) { //Shop Menu
    background(230);
       
    imageMode(CORNER);
    image(logoImg, 30, 30);
    
    //Since stats will use a different font we must change the font back
    stats.display();
    textFont(myFont);
    
    shopView.display();
    if (shopView.mainMenuPressed() && changeTo == 3) {
      changeTo = 1;
      fadeOut = true;
      fadeIn = false;
    }
    
    fadeAnim();
    
  }
  
  if (MENU == 4) {  //Settings Menu
    background(230);
    imageMode(CORNER);
    image(logoImg, 30, 30);
    
    //Since stats will use a different font we must change the font back
    stats.display();
    textFont(myFont);
    
    settingsMenu.display();
    
    if (settingsMenu.mainMenuPressed() && changeTo == 4 && settingsMenu.getKeybind.fade == 0) {
      changeTo = 1;
      fadeOut = true;
      fadeIn = false;
    }
    
    fadeAnim();
    
  }
  
  
  if (MENU == 6) { //ACTUAL GAME
    background(255);
    
    if (!gameStarted) {
      gameWindow.titleBar.display();
      
      fill(180);
      rectMode(CORNER);
      noStroke();
      rect(0, panelY, width, height);
      
      //Player Outline
      if (settings.playerOutline) {
        fill(100);
        noStroke();
        rect(xA-1, yA-1, sqSize+2, sqSize+2);
        rect(xB-1, yB-1, sqSize+2, sqSize+2);
      }
     
      if (gameWindow.ID == 1) {
        image(gameWindow.player.skinR, xA, yA, sqSize, sqSize);
        image(gameWindow.opponent.skinB, xB, yB, sqSize, sqSize);
      } else {
        image(gameWindow.player.skinB, xA, yA, sqSize, sqSize);
        image(gameWindow.opponent.skinR, xB, yB, sqSize, sqSize);
      }
      
      if (xA == -sqSize) {
        Ani.to(this, 2, "xA", 595-sqSize);
        Ani.to(this, 2, "xB", 685);
      }
      if (xB == 685) {
        Ani.to(this, 1, "panelY", height);
        Ani.to(this, 1.6, "sqSize", 100);
        Ani.to(this, 1.6, "xA", gameWindow.player.px);
        Ani.to(this, 1.6, "yA", gameWindow.player.py);
        Ani.to(this, 1.6, "xB", gameWindow.opponent.px);
        Ani.to(this, 1.6, "yB", gameWindow.opponent.py);
      }
      if (sqSize == 100) {
        myClient.write("ready");
        panelY = height+100;
        gameStarted = true;
      }
      
    } else {
      gameWindow.display();
      gameWindow.titleBar.oChat.display();
      gameWindow.titleBar.pChat.display();
      
      //Game has ended, display final screen
      if (!gameWindow.outStream.gameRunning) {
        rectMode(CORNER);
        noStroke();
        fill(180, 200);
        rect(0, panelY, width, height);
        
        //println(panelY);
        
        //Used as a delay
        if (sqSize == 100) {
          Ani.to(this, 2, "sqSize", 720);
        }
        
        if (sqSize == 720) {
          Ani.to(this, 1, "panelY", 0);
        }
        
        if (panelY == 0 && !fadeOut) {
          //+30 for win, +10 for loss
          stats.updateCoins(stats.coins + (gameWindow.outStream.gameWon ? 30 : 10));
          
          
          changeTo = 1;
          fadeOut = true;
          fade = 0;
        }
        
      }
    }
    
    
    fadeAnim();
  
  }
}

//---------------------------------------------------------------------------------------

//Fading animation between window changes
void fadeAnim() {
    //FADE
    if (fadeIn) {
      if (fade > 0) {
        fade -= 5;
      } else {
        fade = 0;
        fadeIn = false;
        fadeOut = false;
      }
    }
    
    if (fadeOut) {
      if (fade < 255) {
        fade += 5;
      } else {
        MENU = changeTo;
        fadeOut = false;
        fadeIn = true;
        fade = 255;
      }
    }
    
    if (fadeIn || fadeOut) {
      rectMode(CORNER);
      noStroke();
      fill(0, fade);
      rect(0, 0, width, height);
    }
}


//Check to see if server sent that we in a match
void checkMatch() {
  String dataIn;
  while (myClient.available() == 0) {if (stopCheckMatch) return;}

  dataIn = myClient.readStringUntil(10); //Newline terminating character (10)
  dataIn = dataIn.replace("\n", ""); //Remove trailing character
  println("[CHECK MATCH] " + dataIn);
  
  if (dataIn.startsWith("gameStart")) {
    matchFound = Integer.parseInt(dataIn.replace("gameStart", ""));
  } else {
    //This shouldn't happen, just kill client if it does
    terminate = true;
  }

}

void getInv() {
  //We know that inventory is being called because we successfully logged in
  stats.loggedIn = true;
  stats.username = loginPopup.username;
  stats.fetchInventory(myClient);
}

void enterQueue() {
  myClient.write("joinqueue");
  Ani.to(this, 1.5, "popUpY", 30);
}

void leaveQueue() {
  myClient.write("leavequeue");
  Ani.to(this, 1.5, "popUpY", -35);
}

void setupCipher() {
  String toWrite = cipherGen.getPublickeyString();
  println("[CLIENT KEY] " + toWrite);
  myClient.write(toWrite);
  
  while (myClient.available() == 0) {}
  String serverPubkey = myClient.readStringUntil(10); //Newline terminating character (10)
  println("[SERVER KEY] " + serverPubkey);
  serverPubkey = serverPubkey.replace("\n", ""); //Remove trailing character
  
  cipherGen.setReceiverPublicKeyString(serverPubkey);
  cipherGen.ready = true;
}

//CP5 submit button
void submit() {
  loginPopup.submitPressed();
}

int itemValue(int item) {
  if (item >= 100) {
    return chatCosts[item-100];
  } else if (item >= 50) {
    return titleCosts[item-50];
  } else {
    return skinCosts[item-1];
  }
}

void stop() {
  myClient.write("-1"); //Disconnect signal
}
