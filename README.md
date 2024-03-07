# JSQ
Matthew Michaud (matthew.michaud@alumni.ucalgary.ca)

## Overview
Java Sound Cue (JSQ) will be a desktop application for setting up and executing lists of sound cues for live performances. I was inspired to develop this application by using the Mac-exclusive [QLab](https://qlab.app/) while taking technical theatre classes in high school. QLab's features encompass the current goals of this project and much more, offering what I consider an excellent product for planning and playing live sound and video, if not for those who don't own a Mac. My desire to create a similar cross-platform tool was my primary motivation to use Java in this project since any reasonably capable desktop computer should be able to run it.

The current goal is to implement just the simplest features: the ability to create a list of cues that can play and stop sound. In addition, the current scope of work will also incorporate features to loop sound, delay the cue actions after they're triggered, and adjust the sound levels of cues independently. All other features necessary to make the application usable, like project saving and an effective UI, are also under development.

The application currently implements most of the UI and organizational framework to construct cue lists. There are still a few details to handle in this area, like sound file management, cue delays, loops, and levels, and some QoL features mocked by the UI. Once all the legwork to make the application "usable" is complete, I will implement the mechanisms to trigger and play the sounds.

I'm using Java 21 and trying out [JavaFX](https://openjfx.io/) and [Maven](https://maven.apache.org/) for the first time.
