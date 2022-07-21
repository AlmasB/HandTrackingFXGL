This repo is work-in-progress!

# HandTrackingFXGL
A simple FXGL game that makes use of MediaPipe hand tracking for controls

### Instructions

Run with JDK 17. Ensure the camera is allowed access.

1. `mvn javafx:run`
2. A browser page will open requesting access to the camera. Allow.

### Current Gestures

- OK Sign (Circle with thumb and index and other fingers up)
- Peace Sign (Index and middle finger in V Shape)
- Finger Gun (Thumb up, index finger pointing) - Primarily works side to side currently
- Thumbs Up (All fingers closed, thumb pointing upwards)
- Thumbs Down (All fingers closed, thumb pointing downwards)

There is some struggle when the fingers are curled behind the hand.
