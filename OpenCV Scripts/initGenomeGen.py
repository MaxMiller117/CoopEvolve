import sys

def write(str):
    if file == None:
        print(str)
    else:
        file.write(str+"\n")

# python initGenomeGen.py <num_input_nodes> <num_output_nodes> <optional filename>
N = int(sys.argv[1]) # num_input_nodes
M = int(sys.argv[2]) # num_output_nodes

file = None
if(len(sys.argv)>=4):
    print("Printing to file...")
    file = sys.argv[3]
    file = open(file,'w')

# Tool to create initial genome
write(" genomestart  1")

for i in range(0,N+M+1):
    write(" trait "+str(i)+" 0."+str(i)+" 0 0 0 0 0 0 0")

for i in range(1,N+1):
    write(" node "+str(i)+" 0 1 1")
for i in range(N+1,N+M+1):
    write(" node "+str(i)+" 0 0 2")
for i in range(1,N+1):
    for j in range(N+1,N+M+1):
        write(" gene "+str(i)+" "+str(i)+" "+str(j)+" 0.0 0 "+str(i)+" 0 1")


write(" genomeend 1")

if(not file == None):
    file.close()