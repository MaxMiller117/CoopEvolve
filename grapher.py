import matplotlib.pyplot as plt
import os, fnmatch, csv, statistics
import pandas as pd

__directory__ = './'

def find(pattern, path):
    result = []
    dirs = os.listdir(path)
    for file in dirs:
        if fnmatch.fnmatch(file, pattern):
            result.append(file)
    return result
    
def get_summary_name(file):
    return file[:len(file)-4]+"_summary.csv"
    
def filter_summarized(file_list):
    for file in file_list:
        if os.path.isfile(get_summary_name(file)):
            file_list.remove(file)
    return file_list
    
def row_avg_max(row):
    max = 0
    sum = 0
    count = 0
    for fit in row:
        fit = float(fit)
        sum += fit
        count += 1
        if fit > max:
            max = fit
    avg = sum/count
    return avg,max

#Summarized format for a single test is
#Generation,Avg,Max
def summarize(filename):
    csvfile = open(filename,'rt',newline='')
    reader = csv.reader(csvfile, delimiter=',')
    sum_filename = get_summary_name(filename)
    sum_file = open(sum_filename,'wt',newline='')
    writer = csv.writer(sum_file,delimiter=',')
    gen = 1
    for row in reader:
        avg,max = row_avg_max(row)
        writer.writerow([gen,avg,max])
        gen+=1
    
    csvfile.close()
    sum_file.close() 

def search_and_summarize(overwrite=False):
    input_list = find('*_generation_data.csv','.')
    if not overwrite:
        input_list = filter_summarized(input_list)
    
    for file in input_list:
        summarize(__directory__+file)
        
def get_all_opt(opt):
    file_list = find('opt'+opt+'.*data_summary.csv',__directory__)
    return file_list
    
#Gen,Avg-2stdev,Avg,Avg+2stdev,Max-2stdev,Max,Max+2stdev
def sum_opt(opt):
    file_list = get_all_opt(opt)
    reader_list = []
    for file_name in file_list:
        file = open(__directory__+file_name,'rt',newline='')
        reader = csv.reader(file,delimiter=',')
        reader_list.append(reader)
    
    sum_file = open(__directory__+'opt'+opt+'sum.csv','wt',newline='')
    writer = csv.writer(sum_file,delimiter=',')
    
    while(True):
        try:
            data_avg = []
            data_max = []
            gen = 1
            for reader in reader_list:
                row = next(reader)
                print(row)
                gen = row[0]
                data_avg.append(float(row[1]))
                data_max.append(float(row[2]))
            if len(data_avg) > 1:
                avg_avg = statistics.mean(data_avg)
                stdev_avg = statistics.stdev(data_avg)
                avg_max = statistics.mean(data_max)
                stdev_max = statistics.stdev(data_max)
            elif len(data_avg) == 1:
                avg_avg = data_avg[0]
                stdev_avg = 0
                avg_max = data_max[0]
                stdev_max = 0
            else:
                #print("ERROR: No data found!")
                avg_avg = 0
                stdev_avg = 0
                avg_max = 0
                stdev_max = 0
                
            sum_avg = [avg_avg-2*stdev_avg,avg_avg,avg_avg+2*stdev_avg]
            sum_max = [avg_max-2*stdev_max,avg_max,avg_max+2*stdev_max]
            
            writer.writerow([gen]+sum_avg+sum_max)
            
        except StopIteration:
            #print("Reached end of file")
            break
    
def graph_opt(opt,show=True):
    sum_file = open(__directory__+'opt'+opt+'sum.csv','rt',newline='')
    reader = csv.reader(sum_file,delimiter=',')
    gen = []
    avg_low = []
    avg = []
    avg_high = []
    max_low = []
    max = []
    max_high = []
    
    data = []
    
    for row in reader:
        gen.append(int(row[0]))
        avg_low.append(round(float(row[1]),3))
        avg.append(round(float(row[2]),3))
        avg_high.append(round(float(row[3]),3))
        max_low.append(round(float(row[4]),3))
        max.append(round(float(row[5]),3))
        max_high.append(round(float(row[6]),3))
        
    data = ({'gen':gen,\
        'avg_low':avg_low,\
        'avg':avg,\
        'avg_high':avg_high,\
        'max_low':max_low,\
        'max':max,\
        'max_high':max_high})
            
        
    for i in range(min(10,len(avg))):
        print(str(gen[i])+" : "+str(avg[i]))
        
    #plt.scatter(gen,avg)
    plt.plot('gen','avg_low',data=data,color='xkcd:light red',linewidth=1,linestyle='dashed')
    plt.plot('gen','avg',data=data,color='xkcd:red',linewidth=2,label="$avg \pm 2\sigma$")
    plt.plot('gen','avg_high',data=data,color='xkcd:light red',linewidth=1,linestyle='dashed')
    
    plt.plot('gen','max_low',data=data,color='xkcd:light blue',linewidth=1,linestyle='dashed')
    plt.plot('gen','max',data=data,color='xkcd:blue',linewidth=2,label="$max \pm 2\sigma$")
    plt.plot('gen','max_high',data=data,color='xkcd:light blue',linewidth=1,linestyle='dashed')
    
    plt.title('Data for opt '+str(opt))
    plt.xlabel('generation')
    plt.ylabel('fitness')
    
    plt.legend()
    if show:
        plt.show()
    
def do_all_opt(opt):
    print(get_all_opt(opt))
    sum_opt(opt)
    graph_opt(opt,show=False)

def main():
    search_and_summarize(True)
    plt.figure(1)
    plt.subplot(211)
    do_all_opt('4')
    plt.subplot(212)
    do_all_opt('5')
    plt.show()

if __name__ == "__main__":
    main()









