function [ quake_fft ] = GetPeakTabbed( filename , trim)
    [x, y, z] = LoadFile(filename);
    quake_magnitude = sqrt(x.^2 + y.^2 + z.^2);
    if(trim)
        quake_magnitude = quake_magnitude(1:1200);
    end
    oddRows = quake_magnitude(1:2:end,:);
    quake_fft = abs(fft(oddRows));
    disp(filename);
    disp(max(quake_fft));
    
end

