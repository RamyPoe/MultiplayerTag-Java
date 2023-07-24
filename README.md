#### Online Multiplayer game using authoritative server model. Can handle concurrent games with an encrypted Login/Signup system and user queueing functionality. Takes advantage of Entity Interpolation, Server Reconciliation, and Client-side prediction.

___

## Project Screen Shots

![ALT](https://github.com/RamyPoe/MultiplayerTag-Java/blob/main/images/1.png?raw=true)
![ALT](https://github.com/RamyPoe/MultiplayerTag-Java/blob/main/images/2.png?raw=true)
![ALT](https://github.com/RamyPoe/MultiplayerTag-Java/blob/main/images/3.png?raw=true)
![ALT](https://github.com/RamyPoe/MultiplayerTag-Java/blob/main/images/4.png?raw=true)
![ALT](https://github.com/RamyPoe/MultiplayerTag-Java/blob/main/images/5.png?raw=true)
![ALT](https://github.com/RamyPoe/MultiplayerTag-Java/blob/main/images/6.png?raw=true)
![ALT](https://github.com/RamyPoe/MultiplayerTag-Java/blob/main/images/7.png?raw=true)


## Installation and Setup Instructions

Clone down this repository. Tested on `Windows 10` and `Ubuntu`. Setup instructions will be for `Windows 10`

#### Server

Has been compiled to `TagServer/TagServer.jar`. Should be ran using `TagServer/run.bat` (or from console) otherwise there will be no console output.

Server IP will be displayed on the console output. Runs on PORT `5204`.

#### Client

Has been compiled to `TagClient/application.windowsXX/TagClient.exe`
Alternatively can be run/re-compiled using Processing 3

Before launching, configure the Server IP in `TagClient/code/server.txt`


## Reflection

This was a 5 week long project built for ICS3U culminating. Project goals included:  
 - Applying Java fundamentals
 - Familiarizing myself with 2D rendering
 - Learning Processing framework
 - Learning Server/Client models
 - Lag compenstation techniques for fast paced network games
 - Encryption and Key handshakes for login/signup
 - Local database for Player inventory management and Login verification

## Documentation
> Project Outline and Documentation can be found [here](https://docs.google.com/document/d/1DpyZdfKOhErycgIaeVq2_0QCtqmpy3ROW7JpzybAZ4s/edit?usp=sharing): 
