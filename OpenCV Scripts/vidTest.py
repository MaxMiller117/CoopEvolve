import numpy as np
import cv2
import imutils
import urllib

url = "http://192.168.137.60:25565/shot.jpg"
cap = cv2.VideoCapture(1)

while(True):
    # Capture frame-by-frame
    #ret, frame = cap.read()
    imgResp=urllib.urlopen(url)
    imgNp=np.array(bytearray(imgResp.read()),dtype=np.uint8)
    frame=cv2.imdecode(imgNp,-1)
    
    
    # Our operations on the frame come here
    gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
    
    hsv = cv2.cvtColor(frame, cv2.COLOR_BGR2HSV)
    
    # Blue filter
    lower_blue = np.array([90,50,50])
    upper_blue = np.array([100,255,255])
    blueMask = cv2.inRange(hsv, lower_blue, upper_blue)
    blueMask = cv2.GaussianBlur(blueMask,(5,5),0)
    
    # Green filter
    lower_green = np.array([60,10,10])
    upper_green = np.array([80,255,255])
    greenMask = cv2.inRange(hsv, lower_green, upper_green)
    greenMask = cv2.GaussianBlur(greenMask,(5,5),0)
    
    # find contours in the binary image
    im2, contours, hierarchy = cv2.findContours(blueMask,cv2.RETR_TREE,cv2.CHAIN_APPROX_SIMPLE)
    for c in contours:
        if cv2.contourArea(c)<500:
            continue
        # calculate moments for each contour
        M = cv2.moments(c)
    
        # calculate x,y coordinate of center
        if(M["m00"]!=0.0):
            cX = int(M["m10"] / M["m00"])
            cY = int(M["m01"] / M["m00"])
        else:
            continue
        cv2.circle(frame, (cX, cY), 5, (255, 255, 255), -1)
        cv2.putText(frame, "centroid", (cX - 25, cY - 25),cv2.FONT_HERSHEY_SIMPLEX, 0.5, (255, 255, 255), 2)
 

    # Display the resulting frame
    cv2.imshow('frame',frame)
    cv2.imshow('blue',blueMask)
    cv2.imshow('green',greenMask)
    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

# When everything done, release the capture
cap.release()
cv2.destroyAllWindows()