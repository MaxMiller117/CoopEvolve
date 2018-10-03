import cv2
import numpy as np
from matplotlib import pyplot as plt
import time

print cv2.__version__

#pointList = []

def getClick(event,x,y,flags,param):
    if event == cv2.EVENT_LBUTTONDOWN:
        #pointList.append((x,y))
        print([x,y])
        
def fixPerspective(img,inTopLeft,inTopRight,inBotLeft,inBotRight,outTopLeft,outTopRight,outBotLeft,outBotRight,width,height):
    pts1 = np.float32([inTopLeft,inTopRight,inBotLeft,inBotRight])
    pts2 = np.float32([outTopLeft,outTopRight,outBotLeft,outBotRight])

    M = cv2.getPerspectiveTransform(pts1,pts2)

    return cv2.warpPerspective(img,M,(width,height))

def shortSort(pointPair,axis):
    newPointPair = []
    if pointPair[0][axis] > pointPair[1][axis]:
        newPointPair.append(pointPair[1])
        newPointPair.append(pointPair[0])
    else:
        newPointPair.append(pointPair[0])
        newPointPair.append(pointPair[1])
    return newPointPair
    
def stripInsulation(pointList):
    newPoints = []
    for p in pointList:
        newPoints.append(p[0])
    return newPoints
    
def sortPoints(pointList): # Takes in 4 point list, outputs topLeft,topRight,bottomLeft,bottomRight
    highestTwo = [pointList[0],pointList[1]]
    highestTwo = shortSort(highestTwo,1)
    lowestTwo = []
    for p in pointList[2:]:
        if p[1] > highestTwo[0][1]:
            lowestTwo.append(highestTwo[0])
            highestTwo[0] = p
            highestTwo = shortSort(highestTwo,1)
        else:
            lowestTwo.append(p)
    highestTwo = shortSort(highestTwo,0)
    lowestTwo = shortSort(lowestTwo,0)
    
    return [lowestTwo[0],lowestTwo[1],highestTwo[0],highestTwo[1]]
    
def findCorners(img):  #returns [topLeft,topRight,bottomLeft,bottomRight,boundingBoxTopLeft,boundingBoxTopRight,boundingBoxBotLeft,boundingBoxBotRight]
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    blurred = cv2.GaussianBlur(gray,(5,5),0)
    
    blurred = cv2.bitwise_not(blurred)
    
    cv2.imshow("Gray Blurred",blurred)
    cv2.waitKey(0)
    
    thresh = cv2.threshold(blurred, 120, 140, cv2.THRESH_BINARY)[1]
    
    contourList = cv2.findContours(thresh.copy(),cv2.RETR_EXTERNAL,cv2.CHAIN_APPROX_SIMPLE)[1]
    
    biggestContour = contourList[0]
    maxArea = 0
    for contour in contourList:
        area = cv2.contourArea(contour)
        if area > maxArea:
            maxArea = area
            biggestContour = contour
    
    cv2.drawContours(img,[contour],-1,(0,255,0),30)
    
    perim = cv2.arcLength(contour, True)
    approx = cv2.approxPolyDP(contour,0.04*perim,True)
    boundingBox = cv2.boundingRect(approx)
    #print(approx)
    approx = stripInsulation(approx)
    #print(approx)
    approx = sortPoints(approx)
    print(approx)
    print(boundingBox)
    bbTopLeft = [boundingBox[0],boundingBox[1]]
    bbTopRight = [boundingBox[0]+boundingBox[2],boundingBox[1]]
    bbBotLeft = [boundingBox[0],boundingBox[1]+boundingBox[3]]
    bbBotRight = [boundingBox[0]+boundingBox[2],boundingBox[1]+boundingBox[3]]
    return [approx[0],approx[1],approx[2],approx[3],bbTopLeft,bbTopRight,bbBotLeft,bbBotRight]
    #return [approx[0],approx[1],approx[2],approx[3],[boundingBox[0],boundingBox[1]],[boundingBox[3],boundingBox[1]],[boundingBox[0],boundingBox[3]],[boundingBox[2],boundingBox[3]]]
    #return [approx[0],approx[1],approx[2],approx[3],approx[0],[approx[3][0],approx[0][1]],[approx[0][0],approx[3][1]],approx[3]]
    
    
img = cv2.imread('ContourMax.jpg')

points = findCorners(img)

cv2.imshow('image',img)
cv2.setMouseCallback('image',getClick)

#rows,cols,ch = img.shape

height, width = img.shape[:2]
#dst = fixPerspective(img,pointList[0],pointList[1],pointList[2],pointList[3],pointList[4],[pointList[5][0],pointList[4][1]],[pointList[4][0],pointList[5][1]],pointList[5],width,height)
dst = fixPerspective(img,points[0],points[1],points[2],points[3],points[4],points[5],points[6],points[7],width,height)

plt.subplot(121),plt.imshow(img),plt.title('Input')
plt.subplot(122),plt.imshow(dst),plt.title('Output')
cv2.imshow('Out',dst)
plt.show()

#time.sleep(5)

cv2.waitKey(0)
cv2.destroyAllWindows()