# WebDisplay
A minecraft plugin providing web browsing feature

# Commands
`/webdisplay`: gives the player webdisplay map

# About the webdisplay
- you need to place webdisplay map in 3x1 configuration to get it to work.
- only the person with op permission can interact with webdisplay
- webdisplay tabs are saved on rebbot

# How to install
1. Download the latest webdisplay plugin from https://github.com/cyoung06/WebDisplay/releases
2. put the plugin into your server's plugins folder

3. Download BKCommonLib from https://ci.mg-dev.eu/job/BKCommonLib/
4. put BKCommonLib into your server's plugins folder

3. Start the server and enjoy!

# Trouble shooting

## Unable to open X display and crash
 run following commands to install necessary packages
 
```
sudo apt-get -y install xorg xvfb gtk2-engines-pixbuf
sudo apt-get -y install dbus-x11 xfonts-base xfonts-100dpi xfonts-75dpi xfonts-cyrillic xfonts-scalable
```

Before starting your server, run following commands
```
Xfvd :1 &
export DISPLAY=:1
```

##  Inconsistency detected by ld.so: dl-lookup.c: 111: ....

Downgrade to openjdk-8