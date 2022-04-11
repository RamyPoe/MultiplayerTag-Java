import controlP5.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
  void display() {    
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
      text(text, width-16-bw/2-textWidth(text)/2, 170+bh/2-textAscent()*1.2/2);
      
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
      text("YES", width/2, (height/2+35)*0.984);
      
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
  
  void loadCP5(ControlP5 cp5) {
    this.cp5 = cp5;
  }
  
  void loadClient(Client myClient) {
    this.myClient = myClient;
  }
  
  //Must be called inside setup()
  void init() {
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
  
  void cp5Fade() {
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
        .setColorBackground(color(150, 150, 150, (fade+1)/2.55))
        .setColorActive(color(0, fade+1))
        .setColorForeground(color(140, fade+1));
        
    cp5.get(Textfield.class, "pass")
        .setColor(color(0, fade+1))
        .setColorBackground(color(150, 150, 150, (fade+1)/2.55))
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
  
  void startRecv() {
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
  
  void sendSignOut() {
    myClient.write("signout");
  }
  
  void resetEquipped() {
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
  boolean hasData() {
    if (dataIn.length() > 0) {
      return true;
    }
    return false;
  }
  
  String getData() {
    String temp = dataIn;
    dataIn = "";
    return temp;
  }
  
  void putData(String data) {
    dataIn = data;
  }
  
}
