//import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Comparator;

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
  void display() {
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
    text("MAIN MENU", bx, by*0.92);
    
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
  
  void buyItemServer(int item) {
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
  
  void buyItemLocal(int item) {
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
  
  boolean mainMenuPressed() {
    if (mousePressed && mouseX > bx-100 && mouseX < bx+100 && mouseY > by-40 && mouseY < by+40 && !popupWindow.isActive) {
      return true;
    }
    return false;
  }
  
  int checkCategoryClick() {
    //We know that one of the buttons were pressed
    if (mousePressed && mouseX > Mx && mouseX < Mx+arcLength*3 && mouseY > My-20-arcHeight/2 && mouseY < My) {
      //Calculate which button was pressed
      return (int) ((mouseX-Mx)/arcLength);
    }
    //No button pressed
    return -1;
  }
  
  void init() {
    optionImages[0] = loadImage("assets/hanger.png");
    optionImages[1] = loadImage("assets/banner.png");
    optionImages[2] = loadImage("assets/chatIcon.png");
  }
  
  void fillSkinFrames() {
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
  
  void fillTitleFrames() {
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

  void fillChatFrames() {
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
  
  boolean inArray(int element, int[] array) {
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
  
  void display() {
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
      text("BUY", width/2, (height/2+35)*0.984);
      
    //Not enough coins, notify the player
    } else {
      textSize(30);
      fill(0, fade);
      textAlign(CENTER, CENTER);
      text("You need " + coins + " more coins before you can purchase this item", width/2, height/2, 400, 250);
    }
  }
  
  boolean closePressed() {
    if (mousePressed == true && mouseX > width/2+w/2-27 && mouseX < width/2+w/2-7 && mouseY > height/2-h/2+7 && mouseY < height/2-h/2+32) {
      return true;
    }
    return false;
  }
  
  boolean buyButtonHovered() {
    if (isActive && mouseX > width/2-120 && mouseX < width/2+120 && mouseY > height/2+35-40 && mouseY < height/2+35+40) {
      return true;
    }
    return false;
  }
  
  void enable(boolean mode, int coins, int id) {
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
