//import com.google.common.collect.ImmutableMap;
import java.util.Map;

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
  
  boolean mainMenuPressed() {
    if (mousePressed && mouseX > bx-100 && mouseX < bx+100 && mouseY > by-40 && mouseY < by+40) {
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
  
  void fillTitleFrames() {
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
  
  void fillChatFrames() {
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
  void display(boolean equipped, boolean colour) {
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
      text(selectString, x+w/2, (y+h-38)*1.014);
      
    } else {
      fill(78, 194, 52);
      rect(x+w/2, y+h-38, 40, 12, 30);
      //Button Text
      textAlign(CENTER);
      fill(255);
      textSize(18);
      text(unselectString, x+w/2, (y+h-38)*1.014);
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
  
  boolean clicked() {
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
  void display(boolean equipped) {
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
      text(selectString, x+w/2, (y+h-35)*1.014);
      
    } else {
      fill(78, 194, 52);
      rect(x+w/2, y+h-35, 40, 12, 30);
      //Button Text
      textAlign(CENTER);
      fill(255);
      textSize(18);
      text(unselectString, x+w/2, (y+h-35)*1.014);
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
  
  boolean clicked() {
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
  void display(boolean equipped) {
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
      text(selectString, x+w/2, (y+h-40)*1.014);
      
    } else {
      fill(78, 194, 52);
      rect(x+w/2, y+h-40, 40, 12, 30);
      //Button Text
      textAlign(CENTER);
      fill(255);
      textSize(18);
      text(unselectString, x+w/2, (y+h-40)*1.014);
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
  
  boolean clicked() {
    if (mousePressed && mouseX > x+w/2-40 && mouseX < x+w/2+40 && mouseY > y+h-35-12 && mouseY < y+h-35+12) {
      return true;
    }
    return false;
  }
  
}
