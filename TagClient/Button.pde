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
  
  boolean mouseOver() {
    if (mouseX > x && mouseX < (x+w) && mouseY > y && mouseY < (y+h)) {
      return true;
    }
    return false;
  }
  
  boolean clicked() {
    if (isActive) {
      return clicked;
    }
    //Can't be clicked if you're not active
    return false;
  }
  
  void display(boolean activity) {
     //We want button to be active if the warning is not active
     isActive = !activity;
    
     if (w == 0) {
      textSize(size);
      w = (int) textWidth(writing) + 50; //Padding 25 each side
     }
     if (h == 0) {
       textSize(size);
       h = (int) (textAscent()*1.3)+20;
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
