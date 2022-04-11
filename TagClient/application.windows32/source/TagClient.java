import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.net.*; 
import controlP5.*; 
import java.util.regex.Matcher; 
import java.util.regex.Pattern; 
import de.looksgood.ani.*; 
import java.util.Base64; 
import java.security.spec.X509EncodedKeySpec; 
import javax.crypto.*; 
import javax.crypto.spec.SecretKeySpec; 
import java.security.*; 
import java.util.LinkedList; 
import java.util.Queue; 
import java.util.Iterator; 
import java.util.List; 
import java.util.Map; 
import controlP5.*; 
import java.util.regex.Matcher; 
import java.util.regex.Pattern; 
import java.util.Arrays; 
import java.util.Map; 
import java.util.Comparator; 

import com.google.common.annotations.*; 
import com.google.common.base.internal.*; 
import com.google.common.base.*; 
import com.google.common.collect.*; 
import javax.crypto.interfaces.*; 
import javax.crypto.*; 
import javax.crypto.spec.*; 
import sun.security.internal.interfaces.*; 
import sun.security.internal.spec.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class TagClient extends PApplet {

 



 //For smooth animations

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

public void setup() { 
  //Window dimensions
   
    
  //Turn on anit-alias
   
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
 
public void draw() {
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
        Ani.to(this, 1.5f, "popUpY", -35);
        
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
  
  if (MENU == 4) {
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
        Ani.to(this, 1.6f, "sqSize", 100);
        Ani.to(this, 1.6f, "xA", gameWindow.player.px);
        Ani.to(this, 1.6f, "yA", gameWindow.player.py);
        Ani.to(this, 1.6f, "xB", gameWindow.opponent.px);
        Ani.to(this, 1.6f, "yB", gameWindow.opponent.py);
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
        
        gameStarted = false;
      }
    }
    
    if (fadeIn || fadeOut) {
      rectMode(CORNER);
      noStroke();
      fill(0, fade);
      rect(0, 0, width, height);
    }
  }
}

//---------------------------------------------------------------------------------------

//Check to see if server sent that we in a match
public void checkMatch() {
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

public void getInv() {
  //We know that inventory is being called because we successfully logged in
  stats.loggedIn = true;
  stats.username = loginPopup.username;
  stats.fetchInventory(myClient);
}

public void enterQueue() {
  myClient.write("joinqueue");
  Ani.to(this, 1.5f, "popUpY", 30);
}

public void leaveQueue() {
  myClient.write("leavequeue");
  Ani.to(this, 1.5f, "popUpY", -35);
}

public void setupCipher() {
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
public void submit() {
  loginPopup.submitPressed();
}

public int itemValue(int item) {
  if (item >= 100) {
    return chatCosts[item-100];
  } else if (item >= 50) {
    return titleCosts[item-50];
  } else {
    return skinCosts[item-1];
  }
}

public void stop() {
  myClient.write("-1"); //Disconnect signal
}








public class AES_Cipher {
    //If keys have been exchanged
    public boolean ready = false;
  
    private PublicKey publickey;
    KeyAgreement keyAgreement;
    byte[] sharedsecret;

    String ALGO = "AES";

    AES_Cipher() {
        makeKeyExchangeParams();
    }

    private void makeKeyExchangeParams() {
        KeyPairGenerator kpg = null;
        try {
            kpg = KeyPairGenerator.getInstance("EC");
            kpg.initialize(128);
            KeyPair kp = kpg.generateKeyPair();
            publickey = kp.getPublic();
            keyAgreement = KeyAgreement.getInstance("ECDH");
            keyAgreement.init(kp.getPrivate());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setReceiverPublicKey(PublicKey publickey) {
        try {
            keyAgreement.doPhase(publickey, true);
            sharedsecret = keyAgreement.generateSecret();
            ready = true;
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    public String encrypt(String msg) {
        try {
            Key key = generateKey();
            Cipher c = Cipher.getInstance(ALGO);
            c.init(Cipher.ENCRYPT_MODE, key);
            byte[] encVal = c.doFinal(msg.getBytes());
            return Base64.getEncoder().encodeToString(encVal);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return msg;
    }

    public String decrypt(String encryptedData) {
        try {
            Key key = generateKey();
            Cipher c = Cipher.getInstance(ALGO);
            c.init(Cipher.DECRYPT_MODE, key);
            byte[] decValue = c.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(decValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encryptedData;
    }

    public PublicKey getPublickey() {
        return publickey;
    }

    public Key generateKey() {
        return new SecretKeySpec(sharedsecret, ALGO);
    }

//------------------------------------------------------------------------------------------------------------------------------------------

    public String getPublickeyString() {
        return Base64.getEncoder().encodeToString(getPublickey().getEncoded());
    }

    public void setReceiverPublicKeyString(String keyString) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            setReceiverPublicKey(keyFactory.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(keyString))));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
class Button {
  String writing;
  int x, y;
  int w, h;
  int size;
  
  boolean last = false;
  boolean clicking = false;
  boolean clicked = false;
  
  boolean isActive = false;
  
  Button (String text, int x, int y) {
    writing = text;
    this.x = x;
    this.y = y;
    
    size = 40;
    
    w = 0; //Temp, will be changed in loop
    h = 0; //Temp, will be changed in loop
  }
  
  public boolean mouseOver() {
    if (mouseX > x && mouseX < (x+w) && mouseY > y && mouseY < (y+h)) {
      return true;
    }
    return false;
  }
  
  public boolean clicked() {
    if (isActive) {
      return clicked;
    }
    //Can't be clicked if you're not active
    return false;
  }
  
  public void display(boolean activity) {
     //We want button to be active if the warning is not active
     isActive = !activity;
    
     if (w == 0) {
      textSize(size);
      w = (int) textWidth(writing) + 50; //Padding 25 each side
     }
     if (h == 0) {
       textSize(size);
       h = (int) (textAscent()*1.3f)+20;
     }
    
    //Inner rect
    rectMode(CORNER);
    stroke(0); //Black
    strokeWeight(4);
    fill(255, 0); //Transparent
    rect(x, y, w, h, 10);
    
    //Text
    textSize(size);
    fill(0);
    textAlign(LEFT, TOP);
    text(writing, x+25, y+10);
    
    //Hover effect
    if (isActive) {
      if (mouseOver()) {
        stroke(0); //Black
        strokeWeight(3);
        fill(255, 0); //Transparent
        rect(x-12, y-12, w+24, h+24, 15);
      }
    }
    
    //Check for clicking
    clicked = false;
    if (mousePressed == true) {
      if (last == false && mouseOver()) {
        clicking = true;
      }
    } else {
      if (last == true) {
        if (mouseOver()) {
          if (clicking) {
            //println("Clicked!");
            clicked = true;
          }
        } else {
          clicking = false;
        }
      }
    }
    
    last = mousePressed;
    
  }
}
class ChatEquipper {
  //highlighted field, 0 means none
  int selected = 0;
  PVector center;
  int radius = 190;
  
  //Hitboxes for the slots
  Rect slot1, slot2, slot3;
  
  ChatEquipper(int x, int y) {
    center = new PVector(x, y);
    
    //Hitboxes
    slot1 = new Rect(center.x, center.y-radius/1.6f, radius/2, radius/3.2f);
    slot2 = new Rect(center.x+radius/1.7f, center.y/0.85f, radius/3.2f, radius/2);
    slot3 = new Rect(center.x-radius/1.7f, center.y/0.85f, radius/3.2f, radius/2);
  }
  
  public void display() {
    //Main circle in the background
    ellipseMode(RADIUS);
    fill(100);
    noStroke();
    ellipse(center.x, center.y, radius-2, radius-2);
    
    //println(selected);
    switch(selected) {
      case 1:
        //TOP HIGHLIGHT
        fill(160);
        noStroke();
        arc(center.x, center.y, radius-2, radius-2, radians(210), radians(330));
        
        break;
      case 2:
        //BOTTOM RIGHT HIGHLIGHT
        fill(160);
        noStroke();
        arc(center.x, center.y, radius-2, radius-2, radians(330), radians(450));
        
        break;
      case 3:
        //BOTTOM LEFT HIGHLIGHT
        fill(160);
        noStroke();
        arc(center.x, center.y, radius-2, radius-2, radians(90), radians(210));
        
        break;
    }
    
    
    //===============================
    //Lines to hide parts of circle
    //===============================
    
    float x, y;
    strokeWeight(10);
    stroke(230);
    
    //Top right
    x = center.x + cos(radians(330))*radius;
    y = center.y + sin(radians(330))*radius;
    line(center.x, center.y, x, y);
    
    //Top left
    x = center.x + cos(radians(210))*radius;
    y = center.y + sin(radians(210))*radius;
    line(center.x, center.y, x, y);
    
    //Bottom
    x = center.x + cos(radians(90))*radius;
    y = center.y + sin(radians(90))*radius;
    line(center.x, center.y, x, y);
    
    //Hide center
    fill(100);
    stroke(230);
    ellipse(center.x, center.y, 40, 40);
    
    
    
    //Displaying the currently equipped fields
    textSize(30);
    fill(255);
    textAlign(CENTER, CENTER);
    rectMode(RADIUS);
    
    text(chatDict[stats.equipped[2]-100], slot1.x, slot1.y);
    text(chatDict[stats.equipped[3]-100], slot2.x*0.99f, slot2.y*0.94f);
    text(chatDict[stats.equipped[4]-100], slot3.x*1.01f, slot3.y*0.95f);
    
    //Small text to indicate position of each field
    textSize(15);
    fill(255);
    textAlign(CENTER, CENTER);
    
    text("1", center.x, center.y-18);
    text("2", center.x+16, center.y+9);
    text("3", center.x-16, center.y+9);
    
    //Draw hitboxes
      /*
    slot1.displayOutline();
    slot2.displayOutline();
    slot3.displayOutline();
      */
    
    
    
    //===================================
    //Checking for clicks to change slot
    //===================================
    
    if (warningPopup.isActive) return;
    
    if (mousePressed && slot1.collidePoint(mouseX, mouseY)) {
      selected = 1;
    } else if (mousePressed && slot2.collidePoint(mouseX, mouseY)) {
      selected = 2;
    } else if (mousePressed && slot3.collidePoint(mouseX, mouseY)) {
      selected = 3;
    } else if (mousePressed) {
      selected = 0;
    }
    
  }
}

class Rect {
  float x, y, w, h;
  
  Rect(float x, float y, float w, float h) {
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
  }
  
  public boolean collidePoint(int px, int py) {
    if (px > x-w && px < x+w && py > y-h && py < y+h) {
      return true;
    }
    return false;
  }
  
  public void displayOutline() {
    rectMode(RADIUS);
    stroke(255, 20, 20);
    strokeWeight(3);
    fill(0, 0);
    rect(x, y, w, h);
  }
}





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
  public void display() {
    //WHILE GAME RUNNING
    player.display();
    opponent.display();
    titleBar.display();
  }
  
  public void setupGame() {
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
  
  public void startOutStream() {
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
  
  public void run() {
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
        gameWindow.titleBar.countdown = Double.parseDouble(data[6])/10.0f;
        
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
    
    public void display() {
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
        size = (int)(max_size * (difference / 500.0f));
      
      //Show for 2s
      } else if (difference >= 500 && difference < 2500) {
        size = (int) max_size;
        
      //Shrink animation 0.5s
      } else if (difference <= 3000) {
        size = (int)(100.0f * (1-((difference-2500) / 500.0f)));
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
        ellipse(x-size/2*1.3f, y+size/4, size/13, size/13);
        ellipse(x-size/2*1.7f, y+size/4, size/13, size/13);
      } else {
        ellipse(x+size/2*1.3f, y+size/4, size/13, size/13);
        ellipse(x+size/2*1.7f, y+size/4, size/13, size/13);
      }
      
      
    }
    
    public void quickChat(String text) {
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
    countdown = 5.999999f;
    
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
  public void display() {    
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
      text(username, 0+7, h/2*0.75f);
        //OPPONENT
      textAlign(RIGHT, CENTER);
      if (!it) { fill(272, 73, 73); } else { fill(40, 116, 247); }
      text(oppUsername, w-7, h/2*0.75f);
    } else {
        //YOU
      textAlign(LEFT, CENTER);
      if (it) { fill(272, 73, 73); } else { fill(40, 116, 247); }
      text(username, 0+7, h*2/3*0.8f);
        //OPPONENT
      textAlign(RIGHT, CENTER);
      if (!it) { fill(272, 73, 73); } else { fill(40, 116, 247); }
      text(oppUsername, w-7, h*2/3*0.8f);
      
      //TITLES
      fill(180);
      textSize(24);
      textAlign(LEFT, CENTER);
      text(title, 0+7, h/3*0.8f);
      
      textAlign(RIGHT, CENTER);
      text(oppTitle, w-7, h/3*0.8f);
    }
    
    //Countdown
    fill(255);
    textSize(50);
    textAlign(CENTER, CENTER);
    text(String.valueOf((int)(Math.ceil(countdown))), width/2, h/2);
    
  }
  
}


public void keyPressed() {
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

public void keyReleased() {
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
//import com.google.common.collect.ImmutableMap;


class InventoryView {
  //Attributes
  int bx, by; //Back to main menu button x and y
  int Mx, My, Mw, Mh; //Menu x and y
  int arcLength, arcHeight; //The length of each category button
  
  //For equipping quick chat items
  ChatEquipper chatEquipper;
  
  //Which color to render the skin in (false = red, true = blue);
  private boolean colour; 
  
  PImage[] optionImages = new PImage[3]; //Image for each button
  int[][] allInventory;
  int[] equipped; // [0] = skin, [1] = title, [2][3][4] = chat items
  
  SkinItemFrame[] skinItemFrames;
  TitleItemFrame[] titleItemFrames;
  ChatItemFrame[] chatItemFrames;
  
  int optionSelected = 0; //Switching between the three cosmetic categories
  boolean mouseDown;
  
  //Constructor
  InventoryView(int[][] allInventory) { 
    bx = 577;
    by = 70;
    
    Mx = 30;
    My = 320;
    Mw = 800;
    Mh = 360;
    
    arcLength = 110;
    arcHeight = arcLength-40;
    
    //-------------------------------
    
    this.equipped = stats.equipped;
    this.allInventory = allInventory;
    
    skinItemFrames = new SkinItemFrame[allInventory[0].length];
    fillSkinFrames();
    
    titleItemFrames = new TitleItemFrame[allInventory[1].length];
    fillTitleFrames();
    
    chatItemFrames = new ChatItemFrame[allInventory[2].length];
    fillChatFrames();
    
    chatEquipper = new ChatEquipper(1070, height/2+65);
  }
  
  //Methods
  public void display() {
    //Main menu button to go back
    rectMode(RADIUS);
    strokeWeight(3);
    stroke(0);
    fill(255, 0); //Transparent
    rect(bx, by, 100, 40);
    
    //Text for button
    textSize(35);
    textAlign(CENTER, CENTER);
    fill(0);
    text("MAIN MENU", bx, by*0.92f);
    
    //Page Title
    textSize(70);
    textAlign(LEFT, CENTER);
    fill(0);
    text("Inventory", 36, 170);
    
    //Main box
    rectMode(CORNER);
    strokeWeight(5);
    stroke(0);
    fill(255, 0); //Transparent filling
    rect(Mx, My, Mw, Mh);
    
    //Cosmetic category button (arcs)
    ellipseMode(CORNER);
    arc(Mx+arcLength*0, My-arcHeight/2-20, arcLength, arcHeight, radians(180), radians(360));
    arc(Mx+arcLength*1, My-arcHeight/2-20, arcLength, arcHeight, radians(180), radians(360));
    arc(Mx+arcLength*2, My-arcHeight/2-20, arcLength, arcHeight, radians(180), radians(360));
    
    //Images
    imageMode(CENTER);
    image(optionImages[0], Mx+arcLength/2, My-arcHeight/2+6);
    image(optionImages[1], Mx+arcLength+arcLength/2, My-arcHeight/2+9);
    image(optionImages[2], Mx+arcLength*2+arcLength/2, My-arcHeight/2+9);
    
    //Lines connecting arcs to the base
    for (int i = 0; i < 4; i++) {
      line(Mx+arcLength*i, My-20, Mx+arcLength*i, My);
    }
    
    //Show that the selected option is actually selected
    rectMode(CORNER);
    noStroke();
    fill(230); //Color of the background
    rect(Mx+3+arcLength*optionSelected, My-2, arcLength-5, 7);
    
    //See if another menu is being clicked
    int checkClick = checkCategoryClick();
    if (checkClick >= 0 && !warningPopup.isActive) { //We know a button was pressed (-1 means no click)
      if (checkClick != optionSelected && mouseDown == false) {
        optionSelected = checkClick;
      }
    }
    
    //Drawing category specific stuff
    if (optionSelected == 0) {
      //Text to show name of selected category
      textAlign(LEFT, BOTTOM);
      textSize(22);
      fill(0);
      text("SKINS", Mx+arcLength*3+20, My-10);
      
      //Button to choose color of preview
      rectMode(RADIUS);
      if (colour) {
        //BLUE SELECTED
        noStroke();
        fill(255, 0, 0);
        rect(Mx+arcLength*3+103, My-22, 12, 12);
        
        fill(0, 0, 255);
        stroke(0);
        strokeWeight(5);
        rect(Mx+arcLength*3+138, My-22, 12, 12);
        
        //Check if red clicked
        if(!mouseDown && mousePressed && mouseX > Mx+arcLength*3+91 && mouseX < Mx+arcLength*3+115 && mouseY > My-34 && mouseY < My-10) {
          //Make red the selected colour
          colour = false;
        }
      } else {
        //RED SELECTED
        fill(255, 0, 0);
        stroke(0);
        strokeWeight(5);
        rect(Mx+arcLength*3+103, My-22, 12, 12);
        
        fill(0, 0, 255);
        noStroke();
        rect(Mx+arcLength*3+138, My-22, 12, 12);
        
        //Check if blue clicked
        if(!mouseDown && mousePressed && mouseX > Mx+arcLength*3+126 && mouseX < Mx+arcLength*3+150 && mouseY > My-34 && mouseY < My-10) {
          //Make blue the selected colour
          colour = true;
        }
      }
      
      //Draw each item
      for (int i = 0; i < skinItemFrames.length; i++) {
        
        if (!skinItemFrames[i].equipped && !mouseDown && skinItemFrames[i].clicked()) {
          //Set it as equipped
          equipped[0] = skinItemFrames[i].id; 
        }
        
        if (equipped[0] == skinItemFrames[i].id) {
          skinItemFrames[i].display(true, colour);
          //Make sure preview is same as equipped skin
          if (playerPreview.id != skinItemFrames[i].id || playerPreview.skin != (colour ? skinItemFrames[i].imgB : skinItemFrames[i].imgR)) {
            playerPreview.setSkin(colour ? skinItemFrames[i].imgB : skinItemFrames[i].imgR, skinItemFrames[i].id);
          }
        } else {
          skinItemFrames[i].display(false, colour);
        }
      }
      
      //Render skin preview
      playerPreview.display();
      
    } else if (optionSelected == 1) {
      //Text to show name of selected category
      textAlign(LEFT, BOTTOM);
      textSize(22);
      fill(0);
      text("TITLES", Mx+arcLength*3+20, My-10);
      
      for (int i = 0; i < titleItemFrames.length; i++) {
        
        if (!mouseDown && titleItemFrames[i].clicked()) {
          if (titleItemFrames[i].equipped) {
            equipped[1] = 0;
          } else {
            equipped[1] = titleItemFrames[i].id;
          }
          
        }
        
        if (equipped[1] == titleItemFrames[i].id) {
          titleItemFrames[i].display(true);
        } else {
          titleItemFrames[i].display(false);
        }
        
      }
      
      //Render skin preview
      playerPreview.display();
    } else if (optionSelected == 2) {
      //Text to show name of selected category
      textAlign(LEFT, BOTTOM);
      textSize(22);
      fill(0);
      text("QUICK CHAT", Mx+arcLength*3+20, My-10);
      
      for (int i = 0; i < chatItemFrames.length; i++) {
        
        if (!mouseDown && chatItemFrames[i].clicked() && !warningPopup.isActive && !chatItemFrames[i].equipped) {
          if (chatEquipper.selected != 0) {
            stats.equipped[chatEquipper.selected+1] = chatItemFrames[i].id;
          } else {
            warningPopup.enable("Please select a slot\nto equip to first");
          }
        }
        
        //3 quick chat slots
        if (equipped[2] == chatItemFrames[i].id || equipped[3] == chatItemFrames[i].id || equipped[4] == chatItemFrames[i].id) {
          chatItemFrames[i].display(true);
        } else {
          chatItemFrames[i].display(false);
        }
        
      }
      
      //For showing equipped and to help equip to slot
      chatEquipper.display();
      
      //In case a popup is made
      warningPopup.display();
    }
    
    
    //For click checking
    if (mousePressed) {
      mouseDown = true;
    } else {
      mouseDown = false;
    }
  }
  
  public boolean mainMenuPressed() {
    if (mousePressed && mouseX > bx-100 && mouseX < bx+100 && mouseY > by-40 && mouseY < by+40) {
      return true;
    }
    return false;
  }
  
  public int checkCategoryClick() {
    //We know that one of the buttons were pressed
    if (mousePressed && mouseX > Mx && mouseX < Mx+arcLength*3 && mouseY > My-20-arcHeight/2 && mouseY < My) {
      //Calculate which button was pressed
      return (int) ((mouseX-Mx)/arcLength);
    }
    //No button pressed
    return -1;
  }
  
  public void init() {
    optionImages[0] = loadImage("assets/hanger.png");
    optionImages[1] = loadImage("assets/banner.png");
    optionImages[2] = loadImage("assets/chatIcon.png");
  }
  
  public void fillSkinFrames() {
    int paddingX = 28;
    int paddingY = 17;
    int offsetX = Mx+40;
    int offsetY = My+paddingY;
    
    int frameW = SkinItemFrame.w;
    int frameH = SkinItemFrame.h;
    
    for (int i = 0; i < skinItemFrames.length; i++) {
      
      SkinItemFrame frame = new SkinItemFrame(offsetX, offsetY, allInventory[0][i]);
      skinItemFrames[i] = frame;
      
      if (offsetX+frameW+paddingX > Mx+Mw) {
        offsetX = Mx+40;
        offsetY += frameH+paddingY;
      } else {
        offsetX += frameW+paddingX;
      }

    }
  }
  
  public void fillTitleFrames() {
    int paddingX = 33;
    int paddingY = 17;
    int offsetX = Mx+40;
    int offsetY = My+paddingY;
    
    int frameW = TitleItemFrame.w;
    int frameH = TitleItemFrame.h;
    
    for (int i = 0; i < titleItemFrames.length; i++) {
      
      TitleItemFrame frame = new TitleItemFrame(offsetX, offsetY, allInventory[1][i], titleDict[allInventory[1][i]-50]);
      titleItemFrames[i] = frame;
      
      if (offsetX+frameW+paddingX > Mx+Mw) {
        offsetX = Mx+40;
        offsetY += frameH+paddingY;
      } else {
        offsetX += frameW+paddingX;
      }

    }
  }
  
  public void fillChatFrames() {
    int paddingX = 35;
    int paddingY = 15;
    int offsetX = Mx+40;
    int offsetY = My+paddingY;
    
    int frameW = ChatItemFrame.w;
    int frameH = ChatItemFrame.h;
    
    for (int i = 0; i < chatItemFrames.length; i++) {
      
      ChatItemFrame frame = new ChatItemFrame(offsetX, offsetY, allInventory[2][i], chatDict[allInventory[2][i]-100]);
      chatItemFrames[i] = frame;
      
      if (offsetX+frameW+paddingX > Mx+Mw) {
        offsetX = Mx+40;
        offsetY += frameH+paddingY;
      } else {
        offsetX += frameW+paddingX;
      }

    }
  }
  
}

//FRAME CLASSES---------------------------------------------------------

class SkinItemFrame {
  //Constants
  String selectString = "EQUIPPED";
  String unselectString = "EQUIP";
  
  //If we are in shop
  int cost;
  
  //Attribtues
  int x, y;
  final static int w = 100;
  final static int h = 150;
  int id;
  PImage imgR, imgB;
  int tx;
  
  boolean equipped;
  
  //Constructor
  SkinItemFrame(int x, int y, int id) {
    this.x = x;
    this.y = y;
    this.id = id;
    
    imgR = loadImage("assets/pskins/"+id+"r.png");
    if (imgR == null) {imgR = loadImage("assets/pskins/uk.png");} //Unknown skin
    
    imgB = loadImage("assets/pskins/"+id+"b.png");
    if (imgB == null) {imgB = loadImage("assets/pskins/uk.png");} //Unknown skin
    
    //To avoid re-calculations
    textSize(20);
    tx = 13 + 3 + (int) textWidth(String.valueOf(skinCosts[id-1]));
    cost =  skinCosts[id-1];
  }
  
  //Methods
  public void display(boolean equipped, boolean colour) {
    this.equipped = equipped;
    
    //OUTLINE
    rectMode(CORNER);
    strokeWeight(3);
    stroke(100);
    fill(150);
    rect(x, y, w, h);
    
    //Skin image
    imageMode(CORNER);
    image(colour ? imgB : imgR, x+(w-70)/2, y+15, 70, 70);
    
    //Button
    rectMode(RADIUS);
    strokeWeight(1);
    stroke(255);
    
    if (equipped) {
      fill(172, 183, 191);
      rect(x+w/2, y+h-38, 40, 12, 30);
      //Button Text
      textAlign(CENTER);
      fill(255);
      textSize(18);
      text(selectString, x+w/2, (y+h-38)*1.014f);
      
    } else {
      fill(78, 194, 52);
      rect(x+w/2, y+h-38, 40, 12, 30);
      //Button Text
      textAlign(CENTER);
      fill(255);
      textSize(18);
      text(unselectString, x+w/2, (y+h-38)*1.014f);
    }

    
    //===============================
    //Dependant on shop or inventory
    //===============================
    
    if (unselectString.equals("EQUIP") && selectString.equals("EQUIPPED")) { //Inventory
      textSize(20);
      textAlign(CENTER);
      fill(255);
      text(String.valueOf(id), x+w/2, y+h-5);
    } else { //Shop (show value in coins instead of id)
      //Use coins image from profile
      imageMode(CENTER);
      image(stats.coinsImg, x+w/2-tx/2, y+h-11, 13, 15);
      
      //Write the cost
      textSize(20);
      textAlign(LEFT, CENTER);
      fill(255);
      text(String.valueOf(skinCosts[id-1]), x+w/2-tx/2+13+3, y+h-16);
    }
    
  }
  
  public boolean clicked() {
    if (mousePressed && mouseX > x+w/2-40 && mouseX < x+w/2+40 && mouseY > y+h-38-12 && mouseY < y+h-38+12) {
      return true;
    }
    return false;
  }
  
}

class TitleItemFrame {
  //Constants
  String selectString = "EQUIPPED";
  String unselectString = "EQUIP";
  
  //If we are in shop
  int cost;
  
  //Attribtues
  int x, y;
  final static int w = 120;
  final static int h = 80;
  int id;
  String text; //Text for the actual title
  int tx;
  
  //Switch between two modes
  boolean equipped;
  
  //Constructor
  TitleItemFrame(int x, int y, int id, String text) {
    this.x = x;
    this.y = y;
    this.id = id;
    this.text = text;
    
    //To avoid re-calculations
    textSize(20);
    tx = 13 + 3 + (int) textWidth(String.valueOf(titleCosts[id-50]));
    cost = titleCosts[id-50];
  }
  
  //Methods
  public void display(boolean equipped) {
    this.equipped = equipped;
    
    //OUTLINE
    rectMode(CORNER);
    strokeWeight(3);
    stroke(100);
    fill(150);
    rect(x, y, w, h);
    
    //Title text
    textAlign(CENTER);
    textSize(17);
    fill(255);
    text(text, x+w/2, y+23);
    
    //Button
    rectMode(RADIUS);
    strokeWeight(1);
    stroke(255);
    
    if (equipped) {
      fill(172, 183, 191);
      rect(x+w/2, y+h-35, 40, 12, 30);
      //Button Text
      textAlign(CENTER);
      fill(255);
      textSize(18);
      text(selectString, x+w/2, (y+h-35)*1.014f);
      
    } else {
      fill(78, 194, 52);
      rect(x+w/2, y+h-35, 40, 12, 30);
      //Button Text
      textAlign(CENTER);
      fill(255);
      textSize(18);
      text(unselectString, x+w/2, (y+h-35)*1.014f);
    }
    
    //===============================
    //Dependant on shop or inventory
    //===============================
    
    if (unselectString.equals("EQUIP") && selectString.equals("EQUIPPED")) { //Inventory
      textSize(20);
      textAlign(CENTER);
      fill(255);
      text(String.valueOf(id), x+w/2, y+h-5);
    } else { //Shop (show value in coins instead of id)
      //Use coins image from profile
      imageMode(CENTER);
      image(stats.coinsImg, x+w/2-tx/2, y+h-11, 13, 15);
      
      //Write the cost
      textSize(20);
      textAlign(LEFT, CENTER);
      fill(255);
      text(String.valueOf(titleCosts[id-50]), x+w/2-tx/2+13+3, y+h-16);
    }
  }
  
  public boolean clicked() {
    if (mousePressed && mouseX > x+w/2-40 && mouseX < x+w/2+40 && mouseY > y+h-35-12 && mouseY < y+h-35+12) {
      return true;
    }
    return false;
  }
  
}

class ChatItemFrame {
  //Constants
  String selectString = "EQUIPPED";
  String unselectString = "EQUIP";
  
  //If we are in shop
  int cost;
  
  //Attribtues
  int x, y;
  final static int w = 120;
  final static int h = 90;
  int id;
  String text; //Text for the actual title
  int tx;
  
  //Switch between two modes
  boolean equipped;
  
  //Constructor
  ChatItemFrame(int x, int y, int id, String text) {
    this.x = x;
    this.y = y;
    this.id = id;
    this.text = text;
    
    //To avoid re-calculations
    textSize(20);
    tx = 13 + 3 + (int) textWidth(String.valueOf(chatCosts[id-100]));
    cost = chatCosts[id-100];
  }
  
  //Methods
  public void display(boolean equipped) {
    this.equipped = equipped;
    
    //OUTLINE
    rectMode(CORNER);
    strokeWeight(3);
    stroke(100);
    fill(150);
    rect(x, y, w, h);
    
    //Chat text
    textAlign(CENTER);
    textSize(17);
    fill(255);
    text(text, x+w/2, y+28);
    
    //Button
    rectMode(RADIUS);
    strokeWeight(1);
    stroke(255);
    
    if (equipped) {
      fill(172, 183, 191);
      rect(x+w/2, y+h-40, 40, 12, 30);
      //Button Text
      textAlign(CENTER);
      fill(255);
      textSize(18);
      text(selectString, x+w/2, (y+h-40)*1.014f);
      
    } else {
      fill(78, 194, 52);
      rect(x+w/2, y+h-40, 40, 12, 30);
      //Button Text
      textAlign(CENTER);
      fill(255);
      textSize(18);
      text(unselectString, x+w/2, (y+h-40)*1.014f);
    }
    
    //===============================
    //Dependant on shop or inventory
    //===============================
    
    if (unselectString.equals("EQUIP") && selectString.equals("EQUIPPED")) { //Inventory
      textSize(20);
      textAlign(CENTER);
      fill(255);
      text(String.valueOf(id), x+w/2, y+h-6);
    } else { //Shop (show value in coins instead of id)
      //Use coins image from profile
      imageMode(CENTER);
      image(stats.coinsImg, x+w/2-tx/2, y+h-11, 13, 15);
      
      //Write the cost
      textSize(20);
      textAlign(LEFT, CENTER);
      fill(255);
      text(String.valueOf(chatCosts[id-100]), x+w/2-tx/2+13+3, y+h-16);
    }
  }
  
  public boolean clicked() {
    if (mousePressed && mouseX > x+w/2-40 && mouseX < x+w/2+40 && mouseY > y+h-35-12 && mouseY < y+h-35+12) {
      return true;
    }
    return false;
  }
  
}




class LoginPopup {
  //Attributes
  int w, h;
  final int bw = 150; //Width and Height for the button
  final int bh = 60;
  
  private boolean mousePressing = false;
  
  String text = "LOGIN";
  boolean isActive = false;
  private boolean signup = false;
  private boolean loggedIn = false;
  private boolean changeMode = false;
  int fade = 0; //For animation
  
  Pattern validInput = Pattern.compile("[^a-zA-Z0-9]"); ///Anything that is not (^) a-z, A-Z, 0-9
  Client myClient;
  ClientRecv clientRecv = new ClientRecv();
  
  String response = ""; //Recieved from server
  String username = ""; //Will be passed on to profile class for render once logged in
  int[] equipped = stats.equipped; //Holds the player equipped inventory
  
  //CP5
  PFont font;
  PFont passfont;
  ControlP5 cp5;
  
  
  //Constructor
  LoginPopup(int w, int h) {
    this.w = w;
    this.h = h;
  }
  
  //Methods
  public void display() {    
    if (isActive) {
      if (fade < 255) {
        fade += 17;
      }
    } else {
      if (fade > 0) {
        fade -= 17;
      }
    }
    
    if (loginClicked()) {
      isActive = true;
    }
    
    
    //DATA
    if (clientRecv.hasData()) {
      response = clientRecv.getData();
      if (response.startsWith("Successfully Logged in")) {
        username = cp5.get(Textfield.class, "name").getText();
        //Runs function from main class
        thread("getInv");
        //Want to change from logged in mode to sign out mode
        changeMode = true;
        //Close the window
        isActive = false;
      }
    }
    
     //Button
      //Outline
      rectMode(CORNER);
      stroke(0);
      strokeWeight(2);
      fill(255, 0); //Transparent filling
      rect(width-bw-16, 170, bw, bh);
      
      //Text
      text = loggedIn ? "SIGN OUT" : "LOGIN";
      textSize(30);
      fill(0);
      text(text, width-16-bw/2-textWidth(text)/2, 170+bh/2-textAscent()*1.2f/2);
      
      //Fade the cp5 elements
      if (!loggedIn) {
        cp5Fade();
      }
    
    //logged out, allow player to login
    //If fade is 0 (everything is transparent) no point in drawing
    if (!loggedIn && fade != 0) {
      
   //Login Window
      //Background greyness
      noStroke();
      rectMode(CORNER); //Default
      fill(130, fade/2);
      rect(0, 0, width, height);
      
      //Login window popup rectangle frame
      stroke(0, fade);
      strokeWeight(2);
      fill(200, fade);
      rectMode(CENTER);
      rect(width/2, height/2, w, h);
      
      //Draw the close button
      textSize(30);
      textAlign(RIGHT, TOP);
      fill(0, fade);
      text("x", width/2+w/2-7, height/2-h/2+5);
      
      //Textbox labels (username, password, title(sign up, login))
      textSize(30);
      fill(255, fade);
      textAlign(CENTER, TOP);
      if (signup) {
        text("SIGN UP", width/2, height/2-h/2+5);
      } else {
        text("LOGIN", width/2, height/2-h/2+5);
      }
      
      //"LINK" to switch between login mode and sign up mode
      textSize(20);
      fill(255, fade);
      textAlign(LEFT, CENTER);
      if (signup) {
        text("Already have an account? ", width/2-textWidth("Already have an account? Log in here")/2, height/2+h/2-18);
        fill(6, 69, 173, fade);
        text("Log in here", width/2-textWidth("Already have an account? Log in here")/2+textWidth("Already have an account? "), height/2+h/2-18);
        
        if (mouseX > width/2-textWidth("Already have an account? Log in here")/2+textWidth("Already have an account? ") && mouseX < width/2+textWidth("Already have an account? Log in here")/2 && mouseY > height/2+h/2-24 && mouseY < height/2+h/2-8) {
          cursor(HAND);
          if (mousePressed) {
            mousePressing = true;
          }
          
          if (mousePressed == false && mousePressing == true) {
            signup = !signup;
            mousePressing = false;
          }
        } else {
          cursor(ARROW); //Default cursor
          if (mousePressed == false) {
            mousePressing = false;
          }
        }
        
      } else {
        text("Don't have an account? ", width/2-textWidth("Already have an account? Log in here")/2, height/2+h/2-18);
        fill(6, 69, 173, fade);
        text("Sign up here", width/2-textWidth("Don't have an account? Sign up here")/2+textWidth("Don't have an account? "), height/2+h/2-18);
        
        if (mouseX > width/2-textWidth("Don't have an account? Sign up here")/2+textWidth("Don't have an account? ") && mouseX < width/2+textWidth("Don't have an account? Sign up here")/2 && mouseY > height/2+h/2-24 && mouseY < height/2+h/2-8) {
          cursor(HAND);
          if (mousePressed) {
            mousePressing = true;
          }
          
          if (mousePressed == false && mousePressing == true) {
            signup = !signup;
            mousePressing = false;
          }
        } else {
          cursor(ARROW); //Default cursor
          if (mousePressed == false) {
            mousePressing = false;
          }
        }
        
      }
      
      //Textbox captions
      textSize(20);
      fill(255, fade);
      textAlign(CENTER);
      text("USERNAME", width/2, 303);
      
      textSize(20);
      fill(255, fade);
      textAlign(CENTER);
      text("PASSWORD", width/2, 378);
      
      //Server response display
      if (response.length() > 0) {
        textAlign(CENTER);
        textSize(16);
        if (response.startsWith("Successfully")) {
          fill(10, 168, 89); //Green
        } else {
          fill(235, 64, 52); //Red
        }
        text(response, width/2, height/2+h/2-40);
      }
      
      if (closeClicked()) {
        isActive = false;
      }
    
    //logged in, allow player to log out
    } else if (loggedIn && fade != 0) {
      //Background greyness
      noStroke();
      rectMode(CORNER); //Default
      fill(130, fade/2);
      rect(0, 0, width, height);
      
      //Login window popup rectangle frame
      stroke(0, fade);
      strokeWeight(2);
      fill(200, fade);
      rectMode(CENTER);
      rect(width/2, height/2, w, h);
      
      //Draw the close button
      textSize(30);
      textAlign(RIGHT, TOP);
      fill(0, fade);
      text("x", width/2+w/2-7, height/2-h/2+5);
      
      textSize(25);
      textAlign(CENTER, TOP);
      fill(0, fade);
      text("Are you sure you want to sign out?", width/2, height/2-h/2+55);
      
      //Button for final signout
      rectMode(RADIUS);
      if (mouseX > width/2-120 && mouseX < width/2+120 && mouseY > height/2+35-40 && mouseY < height/2+35+40) {
        fill(78, 194, 52, fade);
        
        if (mousePressed && isActive) {
          isActive = false;
          changeMode = true;
          //Tell server you signed out
          sendSignOut();
          //Also tell stats object that it should be in loggedOut mode
          stats.loggedIn = false;
        }
        
      } else {
        fill(87, 219, 57, fade);
      }
      
      rect(width/2, height/2+35, 120, 40);
      //Writing on top of button
      textSize(40);
      fill(0, fade);
      textAlign(CENTER, CENTER);
      text("YES", width/2, (height/2+35)*0.984f);
      
      if (closeClicked()) {
        isActive = false;
      }
    }
    
    //Trigger to switch between "login" and "sign out"
    if (fade == 0 && changeMode) {
      changeMode = false;
      loggedIn = !loggedIn;
      if (!loggedIn) {
        thread("leaveQueue");
        resetEquipped();
        println("[DEBUG] Signed out so leaving queue");
      } else {
        playerPreview.init();
      }
    }
    if (fade == 0 && isActive == false) {
      //We have closed the window, reset some params
      response = "";
      cp5.get(Textfield.class, "name").setText("");
      cp5.get(Textfield.class, "pass").setText("");
    }
    
    //println("Change: " + changeMode + "    loggedIn: " + loggedIn + "    fade: " + fade + "    isActive: " + isActive);
  }
  
  private boolean loginClicked() {
    if (!isActive) {
      if (mousePressed == true && mouseX > width-16-bw && mouseX < width-16 && mouseY > 170 && mouseY < 170+bh) {
        isActive = true;
        return true;
      } 
    }
    return false; //Any other possiblity will lead to false
  }
  
  private boolean closeClicked() {
    if (isActive) {
      if (mousePressed == true && mouseX > width/2+w/2-27 && mouseX < width/2+w/2-7 && mouseY > height/2-h/2+5 && mouseY < height/2-h/2+30) {
        return true;
      }
    }
    return false;
  }
  
  public void loadCP5(ControlP5 cp5) {
    this.cp5 = cp5;
  }
  
  public void loadClient(Client myClient) {
    this.myClient = myClient;
  }
  
  //Must be called inside setup()
  public void init() {
    font = createFont("arial", 20, true);
    passfont = createFont("assets/Password.otf", 14, true);
    
    cp5.addTextfield("name")
        .setPosition(width/2-200/2, 310)
        .setSize(200,40)
        .setFont(font)
        .setAutoClear(false) //So it doesn't delete when you press enter
        //.setFocus(true) //Selected/ready to type
        .setColor(color(0))
        .setColorBackground(color(150, 150, 150, 100))
        .setColorActive(color(0))
        .setColorForeground(color(140));
       ;
    cp5.get(Textfield.class, "name").setCaptionLabel("");
    
    cp5.addTextfield("pass")
        .setPosition(width/2-200/2, 385)
        .setSize(160,40)
        .setFont(passfont)
        .setAutoClear(false) //So it doesn't delete when you press enter
        //.setFocus(true) //Selected/ready to type
        .setColor(color(0))
        .setColorBackground(color(150, 150, 150, 100))
        .setColorActive(color(0))
        .setColorForeground(color(140));
       ;
    cp5.get(Textfield.class, "pass").setCaptionLabel("");
      
    cp5.addBang("submit") //Button
      .setPosition(width/2-200/2+161, 385)
      .setSize(40, 40)
      .setColorForeground(color(255, 166, 0))
      //.setColorBackground(color(235, 146, 0)) //Color on hover
      .setColorActive(color(235, 146, 0))
      .getCaptionLabel()
      .align(ControlP5.CENTER, ControlP5.CENTER)
      ;
      
    cp5.get(Textfield.class, "name").setVisible(false);
    cp5.get(Textfield.class, "pass").setVisible(false);
    cp5.get(Bang.class, "submit").setVisible(false);
  }
  
  public void cp5Fade() {
    if (fade == 0) {
      cp5.get(Textfield.class, "name").setVisible(false);
      cp5.get(Textfield.class, "pass").setVisible(false);
      cp5.get(Bang.class, "submit").setVisible(false);
      return;
    } else {
      cp5.get(Textfield.class, "name").setVisible(true);
      cp5.get(Textfield.class, "pass").setVisible(true);
      cp5.get(Bang.class, "submit").setVisible(true);
    }
    
    cp5.get(Textfield.class, "name")
        .setColor(color(0, fade+1))
        .setColorBackground(color(150, 150, 150, (fade+1)/2.55f))
        .setColorActive(color(0, fade+1))
        .setColorForeground(color(140, fade+1));
        
    cp5.get(Textfield.class, "pass")
        .setColor(color(0, fade+1))
        .setColorBackground(color(150, 150, 150, (fade+1)/2.55f))
        .setColorActive(color(0, fade+1))
        .setColorForeground(color(140, fade+1));
        
    cp5.get(Bang.class, "submit")
      .setColorForeground(color(255, 166, 0))
      .setColorBackground(color(235, 146, 0)) //Color on hover
      .setColorActive(color(235, 146, 0));
  }
  
  public void submitPressed() {

    if (cp5.get(Textfield.class, "name").getText().length() > 10 || cp5.get(Textfield.class, "name").getText().length() < 1 || cp5.get(Textfield.class, "pass").getText().length() > 10 || cp5.get(Textfield.class, "pass").getText().length() < 1) {
         response = "USERNAME or PASSWORD is too long or NULL!";
         cp5.get(Textfield.class, "pass").setText("");
    } else {
      Matcher userMatch = validInput.matcher(cp5.get(Textfield.class, "name").getText());
      Matcher passMatch = validInput.matcher(cp5.get(Textfield.class, "pass").getText());
      
      if (userMatch.find() || passMatch.find()) {
        response = "USERNAME or PASSWORD contains special characters";
        cp5.get(Textfield.class, "pass").setText("");
      } else { //Make sure crypto is ready
        //Send login request to server
        String credentials = cp5.get(Textfield.class, "name").getText() + ".." + cp5.get(Textfield.class, "pass").getText();
        String prefix = !signup ? "login: " : "signup: ";
        
        cp5.get(Textfield.class, "pass").setText("");
        
        myClient.write(prefix + cipherGen.encrypt(credentials));
        //Since we know that the server will have to send back a response
        startRecv();
      }
    }

  }
  
  public void startRecv() {
    class OneShotTask implements Runnable {
        Client myClient;
        
        OneShotTask (Client myClient) {
          this.myClient = myClient;
        }
        public void run() {
            recvData();
        }
        private void recvData() {
          while (myClient.available() == 0) {}
  
          dataIn = myClient.readStringUntil(10); //Newline terminating character (10)
          dataIn = dataIn.replace("\n", ""); //Remove trailing character
          println("[SERVER] " + dataIn);
          clientRecv.putData(dataIn);
        }
    }
    
    Thread t1 = new Thread(new OneShotTask(myClient));
    t1.start();
  }
  
  public void sendSignOut() {
    myClient.write("signout");
  }
  
  public void resetEquipped() {
    equipped[0] = 1; //Default skin
    equipped[1] = 0; //No title
    
    equipped[2] = 100; //Default chat is slot 1
    equipped[3] = 101; //Default chat is slot 2
    equipped[4] = 102; //Default chat is slot 3
  }
  
}

class ClientRecv {
  //Attributes
  public String dataIn = "";
  Client myClient;
  
  
  //Methods
  public boolean hasData() {
    if (dataIn.length() > 0) {
      return true;
    }
    return false;
  }
  
  public String getData() {
    String temp = dataIn;
    dataIn = "";
    return temp;
  }
  
  public void putData(String data) {
    dataIn = data;
  }
  
}
class Player { 
  //Vars
  int velocity = 10;
  int size = 100;
  
  boolean it = false;
  int px, py;
  
  PImage skinR;
  PImage skinB;
  
  //Constructor
  Player (int x, int y, int skinId, boolean it) {  
    px = x; 
    py = y;
    this.it = it;
    
    //Load player skins
    skinR = loadImage("assets/pskins/"+skinId+"r.png");
    skinB = loadImage("assets/pskins/"+skinId+"b.png");
    
    if (skinR == null) {
      skinR = loadImage("assets/pskins/uk.png");
    }
    if (skinB == null) {
      skinB = loadImage("assets/pskins/uk.png");
    }
  } 
  
  //Methods
  public void display() {
    //Player outline
    if (settings.playerOutline) {
      rectMode(CORNER);
      fill(100);
      noStroke();
      rect(px-1, py-1, size+2, size+2);
    }
    
    
    imageMode(CORNER);
    if (it) {
      image(skinR, px, py);
    } else {
      image(skinB, px, py);
    }
  }
  
  
}
class PlayerPreview {
  //Attributes
  boolean showing; //Enabled or disabled
  
  PImage skin;
  int id; //Id of skin so we know what we're drawing
  
  PVector leftEye;
  PVector rightEye;
  
  int x, y, w, h;
  
  //Constructor
  PlayerPreview() {
    x = 920;
    y = 380;
    w = 300;
    h = 300;
    
    leftEye = new PVector(x+w/3, y+20);
    rightEye = new PVector(x+w*2/3, y+20);
  }
  
  //Methods
  public void display() {
    if (skin == null) {return;}
    
    imageMode(CORNER);
    image(skin, x, y, w, h);
    
    //EYEBALLS-------------------------------------------------------------
    
    PVector mouseVector = new PVector(mouseX, mouseY);
    
    PVector leftPupil = leftEye.copy().add(mouseVector.copy().sub(leftEye).setMag(10));
    PVector rightPupil = rightEye.copy().add(mouseVector.copy().sub(rightEye).setMag(10));
    
    ellipseMode(CENTER);
    fill(255);
    stroke(50);
    strokeWeight(5);
    ellipse(leftEye.x, leftEye.y, 80, 80);
    ellipse(rightEye.x, rightEye.y, 80, 80);
  
    fill(0);
    noStroke();
    ellipse(leftPupil.x, leftPupil.y, 30, 30);
    ellipse(rightPupil.x, rightPupil.y, 30, 30);
    
  }
  
  public void hide() {
    showing = false;
  }
  
  public void show() {
    showing = true;
  }
  
  public void setSkin(PImage skin, int id) {
    this.skin = skin;
    this.id = id;
  }
  
  public void reset() {
    skin = null;
  }
  
  public void init() {
    //Get the default skin
    skin = loadImage("assets/pskins/1r.png");
  }
  
}


class Profile {
  int x, y;
  int w, h;
  
  boolean loggedIn = false;
  private PFont font;
  
  //Currently equipped items
  int[] equipped = {1, 0, 100, 101, 102}; // [0] = skin, [1] = title, [2][3][4] = chat items
  
  //For animation
  int goalCoins; 
  int increment = 0;
  
  int coins;
  String username;
  String title;
  private String inventory; //Hold all information to be parsed
  private int[] allInv = new int[150]; //Unorganized list of all cosmetics, will be parsed later
  
  PImage coinsImg;
  
  final int hookLength = 40; //For drawing
  final int titleColor = color(52);

  
  Profile () {
    coins = 0;
    title = "";
    username = "";

    w = 500;
    h = 110;
    x = 1280-w-15;
    y = 15;
  }
  
  public void updateTitle(String title) {
    this.title = title;
  }
  
  public void updateCoins(int goal) {
    goalCoins = goal;
    increment = (goalCoins - coins)/240;
    
    //To guarantee at least some movement
    if (goal > coins) {
      increment++;
    } else {
      increment--;
    }
    
    println("INCREMENT: " + increment);
  }
  
  public void updateUsername(String username) {
    this.username = username;
  }
  
  public void loadCoinsImage() {
    coinsImg = loadImage("assets/coins.png");
  }
  
  public void display() {
    if (!loggedIn) {
      allInv = new int[allInv.length];
      username = "---";
      title = "";
    }
    
  //Outline------------------------------------------
    strokeWeight(4);
    stroke(0);
    //Left half
    line(x, y, (x+hookLength), y);
    line(x, y, x, (y+h));
    line(x, (y+h), (x+hookLength), (y+h));
    //Right half
    line((x+w-hookLength), y, (x+w), y);
    line((x+w), y, (x+w), (y+h));
    line((x+w), (y+h), (x+w-hookLength), (y+h));
  //-------------------------------------------------
    
    //println(x + "  " + y + "  " + w + "  " + h);
    
    //Will be changed back in main loop
    textFont(font);
    textAlign(LEFT, TOP);
    
    //Coin increase/decrease animation
    if (goalCoins == coins) {
      increment = 0;
    } else {
      if ((increment > 0 && coins+increment > goalCoins) || (increment < 0 && coins-increment < goalCoins)) {
        increment = 0;
        coins = goalCoins;
      } else {
        coins += increment;
      }
    }
    
    //Coins
    imageMode(CORNER);
    image(coinsImg, x+(w*2/3)+5, y+h/2-25, 47, 50);
    textSize(30);
    fill(230, 182, 50); //Gold color
    if (loggedIn) {
      text(String.valueOf(coins), x+(w*2/3)+60, y+h/2-textAscent()+10);
    } else {
      text("---", x+(w*2/3)+60, y+h/2-textAscent()+10);
      coins = 0;
    }
    
    if (equipped[1] == 0) {
      updateTitle("");
    } else {
      updateTitle(titleDict[equipped[1]-50]);
    }

    //Title and username
    if (title != "") { //Has a title equipped
      textSize(20);
      fill(titleColor);
      text(title, x+(w*2/3/2-textWidth(title)/2), y+10);
      
      textSize(45);
      fill(0);
      text(username, x+(w*2/3/2-textWidth(username)/2), y+35);
    } else {
      textSize(45);
      fill(0);
      text(username, x+(w*2/3/2-textWidth(username)/2), y+25);
    }      
  
  }
  
  public void fetchInventory(Client myClient) {
    //Pull request for server to send the users inventory
    myClient.write("inventory");
    
    inventory = clientRecv(myClient); //coins:skins
    String data[] = inventory.split(":");
    updateCoins(Integer.parseInt(data[0]));
    String temp = data[1];
    

    temp = temp.replace("s", ""); //character "s" to make it a string, not needed no more
    for (int i = 0; i < temp.length()/3; i++) { //Each skin id is 3 characters
       allInv[i] = Integer.parseInt(temp.substring(3*i, 3*i+3));
    }
    
    println("[COSMETICS] " + Arrays.toString(allInv));
  }
  
  private String clientRecv(Client myClient) {
    String dataIn;
    while (myClient.available() == 0) {}
  
    dataIn = myClient.readStringUntil(10); //Newline terminating character (10)
    dataIn = dataIn.replace("\n", ""); //Remove trailing character
    println("[SERVER] " + dataIn);
    
    return dataIn;
  }
  
  public int[][] parsedCosmetics() {
    int[][] cosmetics = new int[3][];
    
    //Skins 0-49 (inclusive)
    int[] skins; int count = 0;
    for (int i = 0; i < allInv.length; i++) {
      if (allInv[i] == 0) {
        break;
      }
      if (allInv[i] > 49) {
        break;
      }
      count++;
    }
    skins = new int[count];
    for (int i = 0; i < allInv.length; i++) {
      if (allInv[i] == 0) {
        break;
      }
      if (allInv[i] > 49) {
        break;
      }
      skins[i] = allInv[i];
    }
    
    //Titles 50-99 (inclusive)
    int[] titles; count = 0;
    for (int i = skins.length; i < allInv.length; i++) {
      if (allInv[i] == 0) {
        break;
      }
      if (allInv[i] > 99) {
        break;
      }
      count++;
    }
    titles = new int[count];
    for (int i = skins.length; i < allInv.length; i++) {
      if (allInv[i] == 0) {
        break;
      }
      if (allInv[i] > 99) {
        break;
      }
      titles[i-skins.length] = allInv[i];
    }
    
    //Chats 100-149 (inclusive)
    int[] chats; count = 0;
    for (int i = skins.length+titles.length; i < allInv.length; i++) {
      if (allInv[i] == 0) {
        break;
      }
      if (allInv[i] > 149) {
        break;
      }
      count++;
    }
    chats = new int[count];
    for (int i = skins.length+titles.length; i < allInv.length; i++) {
      if (allInv[i] == 0) {
        break;
      }
      if (allInv[i] > 149) {
        break;
      }
      chats[i-(skins.length+titles.length)] = allInv[i];
    }
    
    //Culminate them into var to return
    cosmetics[0] = skins;
    cosmetics[1] = titles;
    cosmetics[2] = chats;
    println("[COSMETICS] [" + Arrays.toString(cosmetics[0]) + ", " + Arrays.toString(cosmetics[1]) + ", " + Arrays.toString(cosmetics[2]) + "]");
    return cosmetics;
  }
  
  public void init() {
    //We just need to setup the font
    font = createFont("Calibri", 45, true);
    equipped[0] = 1; //Default skin
    //equipped[1] = 
  }

}
class QueueStatus {
  //Attributes
  
  int x; //X is set, Y will change for the animation
  float y;
  String text = "Searching for players"; //Base
  String displayText; //Will actually be shown
  
  float frame;
  int goalY = 25;
  
  
  //Contructor
  QueueStatus(int x) {
    this.x = x;
    y = -35; //temp, animation will be added to control this
  }

  //Methods
  public void display() {
    if (y > -30) {
      displayText = text;
      for (int i = 0; i < (int) frame; i++) {
        displayText += ".";
      }
      
      fill(0);
      strokeWeight(3);
      line(x, y, x+60, y);
      line(x, y, x, y+30);
      
      fill(0);
      textSize(20);
      text(displayText, x+10, y+10);
      
      frame+=0.05f;
      if (frame >= 4) {
        frame = 1;
      }
    }
  }
}
class Settings {
  //Preferences
  boolean playerOutline = false;
  
  //Game Controls
  int leftKey = 37; //left arrow
  int upKey = 38; //up arrow
  int rightKey = 39; //right arrow
  int downKey = 40; //down arrow
  
  //Quick chat
  boolean chatEnabled = true;
  int chat1 = 49; //1
  int chat2 = 50; //2
  int chat3 = 51; //3
}
class SettingsMenu {
  //Global value for popup, used for keybind popup
  boolean popup = false;
  
  //Main menu button
  int bx, by;
  
  //For input control
  boolean pressing;
  GetKeyBind getKeybind = new GetKeyBind();
  int codeKey;
  
  //For main content
  int x, y;
  
  //SETTINGS
  Switch pOutline;
  Switch chatEnabled;
  KeyBindButton leftKey, upKey, rightKey, downKey;
  KeyBindButton chat1Key, chat2Key, chat3Key;
  
  SettingsMenu() {
    //Main menu button
    bx = 577;
    by = 70;
    
    //For main content
    x = 36;
    y = 220;
    
    //Actual settings
    pOutline = new Switch(x+90, y+165); pOutline.state = settings.playerOutline;
    chatEnabled = new Switch(x+90, pOutline.y+pOutline.h+115); chatEnabled.state = settings.chatEnabled;
    
    leftKey = new KeyBindButton(toChar(settings.leftKey), x+550, y+105);
    upKey = new KeyBindButton(toChar(settings.upKey), x+550, leftKey.y+80);
    rightKey = new KeyBindButton(toChar(settings.rightKey), x+550, upKey.y+80);
    downKey = new KeyBindButton(toChar(settings.downKey), x+550, rightKey.y+80);
    
    chat1Key = new KeyBindButton(toChar(settings.chat1), x+950, y+105);
    chat2Key = new KeyBindButton(toChar(settings.chat2), x+950, chat1Key.y+80);
    chat3Key = new KeyBindButton(toChar(settings.chat3), x+950, chat2Key.y+80);
  }
  
  public String toChar(int code) {
    switch(code) {
      case 37:
        return "a-L";
      case 38:
        return "a-U";
      case 39:
        return "a-R";
      case 40:
        return "a-D";
      default:
        return String.valueOf(PApplet.parseChar(code));
    }
  }
  
  public void display() {
    //Main menu button to go back
    rectMode(RADIUS);
    strokeWeight(3);
    stroke(0);
    fill(255, 0); //Transparent
    rect(bx, by, 100, 40);
    
    //Text for button
    textSize(35);
    textAlign(CENTER, CENTER);
    fill(0);
    text("MAIN MENU", bx, by*0.92f);
    
    //Page Title
    textSize(70);
    textAlign(LEFT, CENTER);
    fill(0);
    text("Settings", 36, 170);
    
    
    //========================
    //Main rectangle frame
    //========================
    
    rectMode(CORNER);
    fill(255, 0);
    strokeWeight(5);
    stroke(0);
    rect(x, y, 1100, 470);
    
    //=======================
    //ACTUAL SETTINGS
    //=======================
    
    //Player Outline setting
    textSize(25);
    fill(0);
    textAlign(CENTER, BOTTOM);
    text("Player Outline", pOutline.x, pOutline.y-pOutline.h-8);
    
    pOutline.display();
    
    if (pOutline.mouseOver() && mousePressed && !pressing && !getKeybind.isActive) {
      pOutline.state = !pOutline.state;
      settings.playerOutline = pOutline.state;
    }
    
    //Chat enabled switch setting
    textSize(25);
    fill(0);
    textAlign(CENTER, BOTTOM);
    text("Quick-Chat", pOutline.x, chatEnabled.y-chatEnabled.h-8);
    
    chatEnabled.display();
    
    if (chatEnabled.mouseOver() && mousePressed && !pressing && !getKeybind.isActive) {
      chatEnabled.state = !chatEnabled.state;
      settings.chatEnabled = chatEnabled.state;
    }
    
//========================================================================================================
    
    //Move left keybind
    textSize(25);
    fill(0);
    textAlign(CENTER, CENTER);
    text("Move Left", x+400, leftKey.y*0.984f);
    
    leftKey.display();
    
    if (leftKey.mouseOver() && mousePressed && !pressing && !getKeybind.isActive) {
      //Popup
      getKeybind.enable();
      println("enabled");
      leftKey.requested = true;
    }
    
    if (getKeybind.isActive == false && leftKey.requested == true) {
      settings.leftKey = getKeybind.code;
      leftKey.bind = toChar(settings.leftKey);
      leftKey.requested = false;
    }
    
    //Move up keybind
    textSize(25);
    fill(0);
    textAlign(CENTER, CENTER);
    text("Move Up", x+400, upKey.y*0.984f);
    
    upKey.display();
    
    if (upKey.mouseOver() && mousePressed && !pressing && !getKeybind.isActive) {
      //Popup
      getKeybind.enable();
      upKey.requested = true;
    }
    
    if (getKeybind.isActive == false && upKey.requested == true) {
      settings.upKey = getKeybind.code;
      upKey.bind = toChar(settings.upKey);
      upKey.requested = false;
    }
    
    //Move right keybind
    textSize(25);
    fill(0);
    textAlign(CENTER, CENTER);
    text("Move Right", x+400, rightKey.y*0.984f);
    
    rightKey.display();
    
    if (rightKey.mouseOver() && mousePressed && !pressing && !getKeybind.isActive) {
      //Popup
      getKeybind.enable();
      rightKey.requested = true;
    }
    
    if (getKeybind.isActive == false && rightKey.requested == true) {
      settings.rightKey = getKeybind.code;
      rightKey.bind = toChar(settings.rightKey);
      rightKey.requested = false;
    }
    
    
    //Move down keybind
    textSize(25);
    fill(0);
    textAlign(CENTER, CENTER);
    text("Move Down", x+400, downKey.y*0.984f);
    
    downKey.display();
    
    if (downKey.mouseOver() && mousePressed && !pressing && !getKeybind.isActive) {
      //Popup
      getKeybind.enable();
      downKey.requested = true;
    }
    
    if (getKeybind.isActive == false && downKey.requested == true) {
      settings.downKey = getKeybind.code;
      downKey.bind = toChar(settings.downKey);
      downKey.requested = false;
    }
    
    
    //Chat1 keybind
    textSize(25);
    fill(0);
    textAlign(CENTER, CENTER);
    text("Chat slot 1", x+800, chat1Key.y*0.984f);
    
    chat1Key.display();
    
    if (chat1Key.mouseOver() && mousePressed && !pressing && !getKeybind.isActive) {
      //Popup
      getKeybind.enable();
      chat1Key.requested = true;
    }
    
    if (getKeybind.isActive == false && chat1Key.requested == true) {
      settings.chat1 = getKeybind.code;
      chat1Key.bind = toChar(settings.chat1);
      chat1Key.requested = false;
    }
    
    //Chat2 keybind
    textSize(25);
    fill(0);
    textAlign(CENTER, CENTER);
    text("Chat slot 2", x+800, chat2Key.y*0.984f);
    
    chat2Key.display();
    
    if (chat2Key.mouseOver() && mousePressed && !pressing && !getKeybind.isActive) {
      //Popup
      getKeybind.enable();
      chat2Key.requested = true;
    }
    
    if (getKeybind.isActive == false && chat2Key.requested == true) {
      settings.chat2 = getKeybind.code;
      chat2Key.bind = toChar(settings.chat2);
      chat2Key.requested = false;
    }
    
    //Chat3 keybind
    textSize(25);
    fill(0);
    textAlign(CENTER, CENTER);
    text("Chat slot 3", x+800, chat3Key.y*0.984f);
    
    chat3Key.display();
    
    if (chat3Key.mouseOver() && mousePressed && !pressing && !getKeybind.isActive) {
      //Popup
      getKeybind.enable();
      chat3Key.requested = true;
    }
    
    if (getKeybind.isActive == false && chat3Key.requested == true) {
      settings.chat3 = getKeybind.code;
      chat3Key.bind = toChar(settings.chat3);
      chat3Key.requested = false;
    }
    
    
    getKeybind.display();
//-----------------------------------------------------
    
    //For click checking
    if (mousePressed) {
      pressing = true;
    } else {
      pressing = false;
    }
  }
  
  public boolean mainMenuPressed() {
    if (mousePressed && mouseX > bx-100 && mouseX < bx+100 && mouseY > by-40 && mouseY < by+40) {
      return true;
    }
    return false;
  }
}

class Switch {
  //On or off
  boolean state;
  
  int x, y, w, h;
  
  Switch(int x, int y) {
    this.x = x;
    this.y = y;
    
    w = 60;
    h = 25;
  }
  
  public void display() {
    strokeWeight(2);
    stroke(70);
    
    rectMode(RADIUS);
    if (state) fill(10, 255, 10); else fill (255, 10, 10);
    rect(x, y, w, h, 25);
    
    //Show the circle on either side
    noStroke();
    
    ellipseMode(CENTER);
    fill(100);
    if (state) {
      ellipse(x+w-17, y, 27, 27);
    } else {
      ellipse(x-w+17, y, 27, 27);
    }
    
  }
  
  public boolean mouseOver() {
    if (mouseX > x-w && mouseX < x+w && mouseY > y-h && mouseY < y+h) {
      return true;
    }
    return false;
  }
  
}

class KeyBindButton {
  String bind;
  int x, y, w, h;
  
  boolean requested;
  
  KeyBindButton(String bind, int x, int y) {
    this.bind = bind;
    this.x = x;
    this.y = y;
    
    w = 30;
    h = 30;
  }
  
  public void display() {
    //Button outline
    rectMode(RADIUS);
    fill(170);
    strokeWeight(3);
    rect(x, y, w, h);
    
    //Current bind
    textAlign(CENTER, CENTER);
    fill(0);
    textSize(23);
    text(bind, x, y*0.99f);
  }
  
  public boolean mouseOver() {
    if (mouseX > x-w && mouseX < x+w && mouseY > y-h && mouseY < y+h) {
      return true;
    }
    return false;
  }
}

class GetKeyBind {
  boolean isActive;
  int fade = 0;
  int code;
  
  KeyBindButton k;
  
  
  public void display() {
    if (isActive && fade < 180) {
      fade += 10;
    }
    if (!isActive && fade > 0) {
      fade -= 10;
    }
    
    if (fade == 0) return;
    
    //Draw overlay
    rectMode(CORNER);
    fill(180, fade);
    rect(0, 0, width, height);
    
    textAlign(CENTER, CENTER);
    textSize(35);
    fill(0, 255*(fade+1/181.0f));
    text("Press a Key...", width/2, height/2);
    
    //Get the keypress
    if (isActive && keyPressed) {
      code = codeKey;
      
      isActive = false;
      println("GOT CODE", code);
    }
  }
  
  public void enable() {
    isActive = true;
  }

}
//import com.google.common.collect.ImmutableMap;



class ShopView {
  //Attributes
  int bx, by; //Back to main menu button x and y
  int Mx, My, Mw, Mh; //Menu x and y
  int arcLength, arcHeight; //The length of each category button
  
  //Number of skins available in the game, to give options for buying
  final private int maxNumSkins = 4;
  
  //Which color to render the skins in (false = red, true = blue);
  private boolean colour;
  
  PImage[] optionImages = new PImage[3]; //Image for each button
  int[][] allInventory;
  int[] equipped; // [0] = skin, [1] = title, [2][3][4] = chat items
  
  SkinItemFrame[] skinItemFrames;
  TitleItemFrame[] titleItemFrames;
  ChatItemFrame[] chatItemFrames;
  
  int optionSelected = 0; //Switching between the three cosmetic categories
  boolean mouseDown;
  PopupWindow popupWindow = new PopupWindow();
  
  //Constructor
  ShopView(int[][] allInventory) { 
    bx = 577;
    by = 70;
    
    Mx = 30;
    My = 320;
    Mw = 1120;
    Mh = 360;
    
    arcLength = 110;
    arcHeight = arcLength-40;
    
    this.allInventory = allInventory;
    
    //All items in the game right now
    skinItemFrames = new SkinItemFrame[maxNumSkins];
    fillSkinFrames();
    
    //All items in the game right now
    titleItemFrames = new TitleItemFrame[titleDict.length];
    fillTitleFrames();
    
    //All items in the game right now
    chatItemFrames = new ChatItemFrame[chatDict.length];
    fillChatFrames();
  }
  
  //Methods
  public void display() {
    //Main menu button to go back
    rectMode(RADIUS);
    strokeWeight(3);
    stroke(0);
    fill(255, 0); //Transparent
    rect(bx, by, 100, 40);
    
    //Text for button
    textSize(35);
    textAlign(CENTER, CENTER);
    fill(0);
    text("MAIN MENU", bx, by*0.92f);
    
    //Page Title
    textSize(70);
    textAlign(LEFT, CENTER);
    fill(0);
    text("Item Shop", 36, 170);
    
    //Main box
    rectMode(CORNER);
    strokeWeight(5);
    stroke(0);
    fill(255, 0); //Transparent filling
    rect(Mx, My, Mw, Mh);
    
    //Cosmetic category button (arcs)
    ellipseMode(CORNER);
    arc(Mx+arcLength*0, My-arcHeight/2-20, arcLength, arcHeight, radians(180), radians(360));
    arc(Mx+arcLength*1, My-arcHeight/2-20, arcLength, arcHeight, radians(180), radians(360));
    arc(Mx+arcLength*2, My-arcHeight/2-20, arcLength, arcHeight, radians(180), radians(360));
    
    //Images
    imageMode(CENTER);
    image(optionImages[0], Mx+arcLength/2, My-arcHeight/2+6);
    image(optionImages[1], Mx+arcLength+arcLength/2, My-arcHeight/2+9);
    image(optionImages[2], Mx+arcLength*2+arcLength/2, My-arcHeight/2+9);
    
    //Lines connecting arcs to the base
    for (int i = 0; i < 4; i++) {
      line(Mx+arcLength*i, My-20, Mx+arcLength*i, My);
    }
    
    //Show that the selected option is actually selected
    rectMode(CORNER);
    noStroke();
    fill(230); //Color of the background
    rect(Mx+3+arcLength*optionSelected, My-2, arcLength-5, 7);
    
    //See if another menu is being clicked
    int checkClick = checkCategoryClick();
    if (checkClick >= 0 && !popupWindow.isActive) { //We know a button was pressed (-1 means no click)
      if (checkClick != optionSelected && mouseDown == false) {
        optionSelected = checkClick;
      }
    }
    
    //Drawing category specific stuff
    if (optionSelected == 0) {
      //Text to show name of selected category
      textAlign(LEFT, BOTTOM);
      textSize(22);
      fill(0);
      text("SKINS", Mx+arcLength*3+20, My-10);
      
      //Button to choose color of preview
      rectMode(RADIUS);
      if (colour) {
        //BLUE SELECTED
        noStroke();
        fill(255, 0, 0);
        rect(Mx+arcLength*3+103, My-22, 12, 12);
        
        fill(0, 0, 255);
        stroke(0);
        strokeWeight(5);
        rect(Mx+arcLength*3+138, My-22, 12, 12);
        
        //Check if red clicked
        if(!mouseDown && mousePressed && mouseX > Mx+arcLength*3+91 && mouseX < Mx+arcLength*3+115 && mouseY > My-34 && mouseY < My-10 && !popupWindow.isActive) {
          //Make red the selected colour
          colour = false;
        }
      } else {
        //RED SELECTED
        fill(255, 0, 0);
        stroke(0);
        strokeWeight(5);
        rect(Mx+arcLength*3+103, My-22, 12, 12);
        
        fill(0, 0, 255);
        noStroke();
        rect(Mx+arcLength*3+138, My-22, 12, 12);
        
        //Check if blue clicked
        if(!mouseDown && mousePressed && mouseX > Mx+arcLength*3+126 && mouseX < Mx+arcLength*3+150 && mouseY > My-34 && mouseY < My-10 && !popupWindow.isActive) {
          //Make blue the selected colour
          colour = true;
        }
      }
      
      //Draw each item
      //-----------------------------------------------------
      for (int i = 0; i < skinItemFrames.length; i++) {
        
        if (!skinItemFrames[i].equipped && !mouseDown && skinItemFrames[i].clicked() && !popupWindow.isActive) {
          //OnClick make popup to confirm purchase, or not enough coins
          if (!popupWindow.isActive) {
            //Have enough coins, confirm purchase
            if (skinItemFrames[i].cost <= stats.coins) {
              popupWindow.enable(true, skinItemFrames[i].cost, skinItemFrames[i].id);
              mouseDown = true;
            //Not enough coins, make popup to let user know
            } else {
              popupWindow.enable(false, skinItemFrames[i].cost-stats.coins, skinItemFrames[i].id);
              mouseDown = true;
            }
            
          }
        }
        
        skinItemFrames[i].display(skinItemFrames[i].equipped, colour);
          
      }
      //-----------------------------------------------------
      
      
      
      
    } else if (optionSelected == 1) {
      //Text to show name of selected category
      textAlign(LEFT, BOTTOM);
      textSize(22);
      fill(0);
      text("TITLES", Mx+arcLength*3+20, My-10);
      
      
      //Draw each item
      //-----------------------------------------------------
      for (int i = 0; i < titleItemFrames.length; i++) {
        
        if (!mouseDown && titleItemFrames[i].clicked() && !titleItemFrames[i].equipped && !popupWindow.isActive) {
          //OnClick make popup to confirm purchase, or not enough coins
          if (!popupWindow.isActive) {
            //Have enough coins, confirm purchase
            if (titleItemFrames[i].cost <= stats.coins) {
              popupWindow.enable(true, titleItemFrames[i].cost, titleItemFrames[i].id);
              mouseDown = true;
            //Not enough coins, make popup to let user know
            } else {
              popupWindow.enable(false, titleItemFrames[i].cost-stats.coins, titleItemFrames[i].id);
              mouseDown = true;
            }
            
          }
        }
        
        titleItemFrames[i].display(titleItemFrames[i].equipped);
        
      }
      //-----------------------------------------------------
      
    } else if (optionSelected == 2) {
      //Text to show name of selected category
      textAlign(LEFT, BOTTOM);
      textSize(22);
      fill(0);
      text("QUICK CHAT", Mx+arcLength*3+20, My-10);
      
      
      //Draw each item
      //-----------------------------------------------------
      for (int i = 0; i < chatItemFrames.length; i++) {
        
        if (!mouseDown && chatItemFrames[i].clicked() && !chatItemFrames[i].equipped && !popupWindow.isActive) {
          //OnClick make popup to confirm purchase, or not enough coins
          if (!popupWindow.isActive) {
            //Have enough coins, confirm purchase
            if (chatItemFrames[i].cost <= stats.coins) {
              popupWindow.enable(true, chatItemFrames[i].cost, chatItemFrames[i].id);
              mouseDown = true;
            //Not enough coins, make popup to let user know
            } else {
              popupWindow.enable(false, chatItemFrames[i].cost-stats.coins, chatItemFrames[i].id);
              mouseDown = true;
            }
            
          }
        }
        
        chatItemFrames[i].display(chatItemFrames[i].equipped);
        
      }
      //-----------------------------------------------------
    }
    
    
    //Popup window
    popupWindow.display();
    
    
    //If an item was purchased, send request to server
    if (popupWindow.id > 0 && !popupWindow.isActive) {
      //Send request to server using a thread
      println("[ITEM PURCHASED] " + popupWindow.id);
      buyItemServer(popupWindow.id);
      buyItemLocal(popupWindow.id);
      
      popupWindow.id = 0;
    }
    
    
    //For click checking
    if (mousePressed) {
      mouseDown = true;
    } else {
      mouseDown = false;
    }
    
  }
  
  public void buyItemServer(int item) {
    //Create thread model to send item to server
    class buyItem implements Runnable {
      int item;
      buyItem(int item) {
        this.item = item;
      }
      public void run() {
        myClient.write("buy " + item);
      }
    }
    
    //Create instance and send action to server
    buyItem buy = new buyItem(item);
    Thread t1 = new Thread(buy);
    t1.start();
    
  }
  
  public void buyItemLocal(int item) {
    //Make effects of a purchase visible locally
    for (int i = 0; i < stats.allInv.length; i++) {
      if (stats.allInv[i] == 0) {
        stats.allInv[i] = item;
        break;
      }
    }
    
    //Sort it to allow it to be parsed properly later
    int numItemsInv = 0;
    for (int i = 0; i < stats.allInv.length; i++) {
      if (stats.allInv[i] != 0) {
        numItemsInv++;
      } else {
        break;
      }
    }
    
    println("[NUM ITEMS] " + numItemsInv);
    
    //Retreive the items from the inventory
    int[] temp = new int[numItemsInv];
    for (int i = 0; i < temp.length; i++) {
      temp[i] = stats.allInv[i];
    }
    Arrays.sort(temp);
    
    //Reset inventory
    for (int i = 0; i < stats.allInv.length; i++) {stats.allInv[i] = 0;}
    
    //Add sorted item back in
    for (int i = 0; i < temp.length; i++) {
      stats.allInv[i] = temp[i];
    }
    
    println("[COSMETICS] " + Arrays.toString(stats.allInv));
    
    //Show the change in coins with animation
    stats.updateCoins(stats.coins-itemValue(item));
    
    //Show the change in the shop
    if (item >= 100) {
      for (int i = 0; i < chatItemFrames.length; i++) {
        if (chatItemFrames[i].id == item) {
          chatItemFrames[i].equipped = true;
          break;
        }
      }
    } else if (item >= 50) {
      for (int i = 0; i < titleItemFrames.length; i++) {
        if (titleItemFrames[i].id == item) {
          titleItemFrames[i].equipped = true;
          break;
        }
      }
    } else {
      for (int i = 0; i < skinItemFrames.length; i++) {
        if (skinItemFrames[i].id == item) {
          skinItemFrames[i].equipped = true;
          break;
        }
      }
    }
  }
  
  public boolean mainMenuPressed() {
    if (mousePressed && mouseX > bx-100 && mouseX < bx+100 && mouseY > by-40 && mouseY < by+40 && !popupWindow.isActive) {
      return true;
    }
    return false;
  }
  
  public int checkCategoryClick() {
    //We know that one of the buttons were pressed
    if (mousePressed && mouseX > Mx && mouseX < Mx+arcLength*3 && mouseY > My-20-arcHeight/2 && mouseY < My) {
      //Calculate which button was pressed
      return (int) ((mouseX-Mx)/arcLength);
    }
    //No button pressed
    return -1;
  }
  
  public void init() {
    optionImages[0] = loadImage("assets/hanger.png");
    optionImages[1] = loadImage("assets/banner.png");
    optionImages[2] = loadImage("assets/chatIcon.png");
  }
  
  public void fillSkinFrames() {
    int paddingX = 28;
    int paddingY = 17;
    int offsetX = Mx+40;
    int offsetY = My+paddingY;
    
    int frameW = SkinItemFrame.w;
    int frameH = SkinItemFrame.h;
    
    for (int i = 0; i < skinItemFrames.length; i++) {
      
      SkinItemFrame frame = new SkinItemFrame(offsetX, offsetY, i+1);
      
      //Change some params to make it work for the shop
      frame.selectString = "OWNED";
      frame.unselectString = "PURCHASE";
      
      //See if this item is owned
      frame.equipped = inArray(i+1, allInventory[0]);
      
      //Add to the list to be drawn
      skinItemFrames[i] = frame;
      
      if (offsetX+frameW+paddingX > Mx+Mw) {
        offsetX = Mx+40;
        offsetY += frameH+paddingY;
      } else {
        offsetX += frameW+paddingX;
      }

    }
  }
  
  public void fillTitleFrames() {
    int paddingX = 35;
    int paddingY = 17;
    int offsetX = Mx+40;
    int offsetY = My+paddingY;
    
    int frameW = TitleItemFrame.w;
    int frameH = TitleItemFrame.h;
    
    for (int i = 0; i < titleItemFrames.length; i++) {
      
      TitleItemFrame frame = new TitleItemFrame(offsetX, offsetY, 50+i, titleDict[i]);
      
      //Change some params to make it work for the shop
      frame.selectString = "OWNED";
      frame.unselectString = "PURCHASE";
      
      //See if this item is equipped
      frame.equipped = inArray(i+50, allInventory[1]);
      
      //Add to the list to be drawn
      titleItemFrames[i] = frame;
      
      if (offsetX+frameW+paddingX > Mx+Mw) {
        offsetX = Mx+40;
        offsetY += frameH+paddingY;
      } else {
        offsetX += frameW+paddingX;
      }

    }
  }

  public void fillChatFrames() {
    int paddingX = 35;
    int paddingY = 15;
    int offsetX = Mx+40;
    int offsetY = My+paddingY;
    
    int frameW = ChatItemFrame.w;
    int frameH = ChatItemFrame.h;
    
    for (int i = 0; i < chatItemFrames.length; i++) {
      
      ChatItemFrame frame = new ChatItemFrame(offsetX, offsetY, i+100, chatDict[i]);
      
      //Change some stuff to make it work with the shop
      frame.selectString = "OWNED";
      frame.unselectString = "PURCHASE";
      
      //See if the item is owned
      frame.equipped = inArray(i+100, allInventory[2]);
      
      //Add item to the list to be drawn
      chatItemFrames[i] = frame;
      
      if (offsetX+frameW+paddingX > Mx+Mw) {
        offsetX = Mx+40;
        offsetY += frameH+paddingY;
      } else {
        offsetX += frameW+paddingX;
      }

    }
  }
  
  public boolean inArray(int element, int[] array) {
    for (int i : array) {
      if (i == element) {
        return true;
      }
    }
    return false;
  }
  
  
}


class PopupWindow {
  //Dimensions
  int w, h;
  
  //Info about the item
  int coins;
  int id;
  
  //Two mode, differentiated by boolean
  private boolean mode;
  
  //For showing and hiding
  private boolean isActive;
  private int fade = 0;
  
  
  PopupWindow () {
    //Dimensions
    w = 430;
    h = 270;
  }
  
  public void display() {
    //FADE
    if (isActive) {
      if (fade < 255) {
        fade += 17;
      }
    } else {
      if (fade > 0) {
        fade -= 17;
      }
    }
    
    //No point in drawing anything if everything is transparent
    if (fade == 0) {
      return;
    }
    
    //Make background darker/inaccessible
    noStroke();
    rectMode(CORNER); //Default
    fill(130, fade/2);
    rect(0, 0, width, height);
    
    //Draw the warning window
    strokeWeight(3);
    stroke(0, fade); //Black
    rectMode(CENTER);
    fill(180, fade); //Gray
    rect(width/2, height/2, w, h);
    
    //Draw the close button
    fill(0, fade);
    textSize(30);
    textAlign(RIGHT, TOP);
    text("x", width/2+w/2-7, height/2-h/2+7);
    
    if (closePressed()) {
      
      id = 0;
      isActive = false;
    }
    
    //=================================================
    //Case specific
    //=================================================
    
    //Has enough coins, confirm yes
    if (mode) {
      textSize(25);
      fill(230, fade);
      textAlign(CENTER, TOP);
      text("Are you sure you want to purchase this item for " + coins + " coins?", width/2, height/2-h/2+60, 400, 80);
     
      //Check for button hover to change color
      if (buyButtonHovered()) {
        fill(78, 194, 52, fade);
        if (mousePressed && isActive && !shopView.mouseDown) {
          isActive = false;
        }
      } else {
        fill(87, 219, 57, fade);
      }
      
      //Confirmation button
      rectMode(RADIUS);
      rect(width/2, height/2+35, 120, 40);
      
      //Writing on top of button
      textSize(40);
      fill(0, fade);
      textAlign(CENTER, CENTER);
      text("BUY", width/2, (height/2+35)*0.984f);
      
    //Not enough coins, notify the player
    } else {
      textSize(30);
      fill(0, fade);
      textAlign(CENTER, CENTER);
      text("You need " + coins + " more coins before you can purchase this item", width/2, height/2, 400, 250);
    }
  }
  
  public boolean closePressed() {
    if (mousePressed == true && mouseX > width/2+w/2-27 && mouseX < width/2+w/2-7 && mouseY > height/2-h/2+7 && mouseY < height/2-h/2+32) {
      return true;
    }
    return false;
  }
  
  public boolean buyButtonHovered() {
    if (isActive && mouseX > width/2-120 && mouseX < width/2+120 && mouseY > height/2+35-40 && mouseY < height/2+35+40) {
      return true;
    }
    return false;
  }
  
  public void enable(boolean mode, int coins, int id) {
    //So that we know which item we are pruchasing
    this.id = id;
    //So that we know it is showing
    isActive = true;
    //True --> has enough coins, False --> not enough coins
    this.mode = mode;
    //Used for confirmation in text
    this.coins = coins;
  }
}
class WarningPopup {
  //Attributes
  boolean isActive;
  int w, h; //x and y not needed as it will be centered
  private int fade = 0; //For animation
  String text;
  
  //Constructor
  WarningPopup(int w, int h) {
    this.w = w;
    this.h = h;
  }
  
  //Methods
  public void display() {
    if (isActive) {
      if (fade < 255) {
        fade += 17;
      }
    } else {
      if (fade > 0) {
        fade -= 17;
      }
    }
    
    //No point in drawing anything if everything is transparent
    if (fade == 0) {
      return;
    }
    //Make background darker/inaccessible
    noStroke();
    rectMode(CORNER); //Default
    fill(130, fade/2);
    rect(0, 0, width, height);
    
    //Draw the warning window
    strokeWeight(3);
    stroke(0); //Black
    rectMode(CENTER);
    fill(150, fade); //Gray
    rect(width/2, height/2, w, h);
    
    //Text for warning window
    textSize(20);
    fill(0, fade);
    textAlign(CENTER, CENTER);
    text(text, width/2, height/2);
    
    //Draw the close button
    textSize(30);
    textAlign(RIGHT, TOP);
    text("x", width/2+w/2-7, height/2-h/2+7);
    
    if (closePressed()) {
      isActive = false;
    }
  }
  
  public void enable(String text) {
    this.text = text;
    isActive = true;
  }
  
  public boolean closePressed() {    
    if (mousePressed == true) {
      //println(mouseX + "   |   " + mouseY);
      
      if (mouseX > width/2+w/2-27 && mouseX < width/2+w/2-7 && mouseY > height/2-h/2+7 && mouseY < height/2-h/2+32) {
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }
  
}
  public void settings() {  size(1280, 720);  smooth(); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "TagClient" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
