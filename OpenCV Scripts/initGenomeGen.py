

# Tool to create initial genome
print(" genomestart  1")
for i in range(1,7):
    print(" node "+str(i)+" 0 1 1")
for i in range(7,22):
    print(" node "+str(i)+" 0 0 2")
for i in range(1,7):
    for j in range(7,22):
        print(" gene "+str(i)+" "+str(i)+" "+str(j)+" 0.0 0 "+str(i)+" 0 1")


print(" genomeend 1")