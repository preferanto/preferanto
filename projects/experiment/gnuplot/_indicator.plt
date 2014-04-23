# Usage example: call "_indicator.plt" 5

set title "Nondominance ratio indicator\nafter $0000 evaluations" enhanced font "Tahoma,14" offset 0,-7

#set logscale x

set size square 1
set border 15
# set xtics nomirror
# set ytics nomirror

# set tics scale 0.6
set tics font "Tahoma,12"


# set key height 10 sample 3.8
# set key font "Tahoma,10"


set xlabel "selectivity" enhanced font "Tahoma,14"
set ylabel "nondominance ratio" enhanced font "Tahoma,14" offset (0,0,0.6)

# set style data lines

# set style line 1 lt 3 lc rgb "black" lw 2 pt -1 ps default pi 0 
# set style line 2 lt 1 lc rgb "black" lw 2 pt -1 ps default pi 0 
# set style line 3 lt 2 lc rgb "black" lw 2 pt -1 ps default pi 0 
set style line 1 lt 1 lc rgb "black" lw 2
set style line 2 lt 2 lc rgb "black" lw 2
set style line 3 lt 3 lc rgb "black" lw 2

# plot "../pf/$0.dat" u 1:2 t "ref" ls 1, "../dat/$0-result-0.dat" with points "std" ls 2, "../dat/$0-result-1.dat" with points;

plot "data/indicator.objCount.2.txt" u 1:1+$0 t "2 objectives" ls 1 with linespoints, \
     "data/indicator.objCount.3.txt" u 1:1+$0 t "3 objectives" ls 2 with linespoints, \
     "data/indicator.objCount.10.txt" u 1:1+$0 t "10 objectives" ls 3 with linespoints;



set terminal push
#set term postscript eps enhanced palfuncparam 2000,0.003
#set term postscript eps enhanced "Helvetica" 10

# set terminal epslatex 8
# set output "indicator.nfe.$0000.eps"

#set term png enhanced size 1024,1024
#set terminal pngcairo enhanced fontscale 1.0 dashed size 512,512
set terminal pngcairo enhanced fontscale 1.0 dashed size 500,500
set output "indicator.nfe.$0000.png"

replot
set output
set terminal pop
