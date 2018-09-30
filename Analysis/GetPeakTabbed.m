function [ peak ] = GetPeakTabbed( filename )
%     fid = fopen(filename,'rt');
%     tmp = textscan(fid, '%s\t%f\t%f\t%f', 'Headerlines', 10);
%     % tmp = textscan(fid, '%s %f %f %f');
%     fclose(fid);
    [x, y, z] = LoadFile(filename);
    quake_magnitude = sqrt(x.^2 + y.^2 + z.^2);
    oddRows = quake_magnitude(1:2:end,:);
    
    quake_fft = abs(fft(oddRows(500:564)));
    peak = max(quake_fft);
    disp(peak);
end

