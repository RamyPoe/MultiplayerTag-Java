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
  
  String toChar(int code) {
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
        return String.valueOf(char(code));
    }
  }
  
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
    text("Move Left", x+400, leftKey.y*0.984);
    
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
    text("Move Up", x+400, upKey.y*0.984);
    
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
    text("Move Right", x+400, rightKey.y*0.984);
    
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
    text("Move Down", x+400, downKey.y*0.984);
    
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
    text("Chat slot 1", x+800, chat1Key.y*0.984);
    
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
    text("Chat slot 2", x+800, chat2Key.y*0.984);
    
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
    text("Chat slot 3", x+800, chat3Key.y*0.984);
    
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
  
  boolean mainMenuPressed() {
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
  
  void display() {
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
  
  boolean mouseOver() {
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
  
  void display() {
    //Button outline
    rectMode(RADIUS);
    fill(170);
    strokeWeight(3);
    rect(x, y, w, h);
    
    //Current bind
    textAlign(CENTER, CENTER);
    fill(0);
    textSize(23);
    text(bind, x, y*0.99);
  }
  
  boolean mouseOver() {
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
  
  
  void display() {
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
    fill(0, 255*(fade+1/181.0));
    text("Press a Key...", width/2, height/2);
    
    //Get the keypress
    if (isActive && keyPressed) {
      code = codeKey;
      
      isActive = false;
      println("GOT CODE", code);
    }
  }
  
  void enable() {
    isActive = true;
  }

}
