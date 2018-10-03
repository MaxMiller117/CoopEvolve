import cv2

print cv2.__version__

img = cv2.imread('photo0.jpg')

g = img.copy()
g[:, :, 0]=0
g[:, :, 2]=0


cv2.imshow('image',g)
cv2.waitKey(0)
cv2.destroyAllWindows()