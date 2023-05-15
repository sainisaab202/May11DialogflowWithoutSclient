# May11DialogflowWithoutSclient

Specifically for Android 6.0
As, Google built library have some dependencies that are NOT available in Android 6 to connect with Dialogflow CX.

This app can be a basic model/sample for the app you want to develop. 
Functionality of this app is you just need to click on the start button and you can start talking or giving command.
When ever app will detect some silence in your sentences, it will automatically trigger the request send command to Dialogflow.
Using audio received from Dialgflow will be played via speakers and the conversation can go on until you click on stop button.

This solution is using following approach:
| App |                                    | Google Servers |
1. Creating and Sign JWT                       ---
2. Use JWT to request token       --->         ---
3.                                <---         Token response
4. Use token to call google API   --->         Token response
