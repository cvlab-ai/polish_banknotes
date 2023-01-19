# Polish banknotes Android app
The project is based on https://github.com/tensorflow/examples/tree/master/lite/examples/image_classification/android
on the license shown in variuos files (http://www.apache.org/licenses/LICENSE-2.0). 
Every change has been made or approved by me (https://github.com/jankejc).
Copyright.

## Basic overwiew.
The app has two modes: dev and user. To go into user mode you need to click TensorFlow image at the top of the screen. 

## Modes description.
In user mode the app scans for the first instance of something that's not `None`. While scanning vibration should be in cyclic mode. On detection `Haptizer` stops and the label is said. To scan again just tap the camera (screen).

In dev mode `Haptizer` don't stop. You don't need to tap the screen to get an app scanning again. In the bottom sheet controls you can play with some options implemented at the beginning.