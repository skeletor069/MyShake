% disp('Chamba : ');
chamba = GetPeakTabbed('chamba.txt', true);
% disp('DLU : ');
dlu = GetPeakTabbed('DLU.txt', true);
% disp('IGN : ');
ign = GetPeakTabbed('IGN.txt', true);
% disp('VCD : ');
vcd = GetPeakTabbed('VCD.txt', true);
% disp('JHR : ');
jhr = GetPeakTabbed('JHR.txt', true);
% disp('POR : ');
por = GetPeakTabbed('POR.txt', true);
% disp('Chamba 2: ');
chm2 = GetPeakTabbed('CHM2.txt', true);
% disp('HAM : ');
ham = GetPeakTabbed('HAM.txt', true);
% disp('JMU : ');
jmu = GetPeakTabbed('JMU.txt', true);

% disp('#################');
% disp('Random : ');
rand = GetPeakTabbed('random.txt', false);
% disp('Table : ');
table = GetPeakTabbed('table.txt', false);
% disp('Walk : ');
walk = GetPeakTabbed('walk.txt', false);


figure; 
%  [r,c] = size(chamba)
xAxis = 1:600;
plot(xAxis, chamba(1:600), 'k');
hold on;
% [r,c] = size(dlu)
%  xAxis = 1:r;
plot(xAxis, dlu(1:600), 'r');
% [r,c] = size(ign)
%  xAxis = 1:r;
plot(xAxis, ign(1:600), 'g');
%  [r,c] = size(vcd)
%  xAxis = 1:r;
plot(xAxis, vcd(1:600), 'b');
%  [r,c] = size(jhr)
%  xAxis = 1:r;
plot(xAxis, jhr(1:600), 'c');
%  [r,c] = size(por)
%  xAxis = 1:r;
plot(xAxis, por(1:600), 'm');
%  [r,c] = size(chm2)
%  xAxis = 1:r;
plot(xAxis, chm2(1:600), 'y');
%  [r,c] = size(ham)
%  xAxis = 1:r;
plot(xAxis, ham(1:600), 'o');
%  [r,c] = size(jmu)
%  xAxis = 1:r;
plot(xAxis, jmu(1:600), 'k');
[r,c] = size(rand)
xAxis = 1:r;
plot(xAxis, rand, 'r');
[r,c] = size(table)
xAxis = 1:r;
plot(xAxis, table, 'g');
[r,c] = size(walk)
xAxis = 1:r;
plot(xAxis, walk, 'b');
hold off;
xlabel('Time') 
ylabel('FFT') 
legend('CHAMBA', 'DLU', 'IGN', 'VCD', 'JHR', 'POR', 'CHM2', 'HAM', 'JMU');