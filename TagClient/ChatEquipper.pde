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
    slot1 = new Rect(center.x, center.y-radius/1.6, radius/2, radius/3.2);
    slot2 = new Rect(center.x+radius/1.7, center.y/0.85, radius/3.2, radius/2);
    slot3 = new Rect(center.x-radius/1.7, center.y/0.85, radius/3.2, radius/2);
  }
  
  void display() {
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
    text(chatDict[stats.equipped[3]-100], slot2.x*0.99, slot2.y*0.94);
    text(chatDict[stats.equipped[4]-100], slot3.x*1.01, slot3.y*0.95);
    
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
  
  boolean collidePoint(int px, int py) {
    if (px > x-w && px < x+w && py > y-h && py < y+h) {
      return true;
    }
    return false;
  }
  
  void displayOutline() {
    rectMode(RADIUS);
    stroke(255, 20, 20);
    strokeWeight(3);
    fill(0, 0);
    rect(x, y, w, h);
  }
}
