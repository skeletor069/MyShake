function [ peak ] = GetPeakSpaced( filename )
    fid = fopen(filename,'rt');
    tmp = textscan(fid, '%s %f %f %f', 'Headerlines', 10);
    % tmp = textscan(fid, '%s %f %f %f');
    fclose(fid);
    x = tmp{2};
    y = tmp{3};
    z = tmp{4};
%     x(mod(x,2)==0)=x(mod(x,2)==0) + .02;
%     x(x<-.018 | x>.018) = x(x<-.018 | x>.018) * 13; 
%     y(mod(y,2)==0)=y(mod(y,2)==0) + .02;
%     y(y<-.018 | y>.018) = y(y<-.018 | y>.018) * 13; 
%     z(mod(z,2)==0)=(z(mod(z,2)==0) - .687)*10;
    quake_magnitude = sqrt(x.^2 + y.^2 + z.^2);
%     quake_fft = abs(fft(quake_magnitude(1:32)));
%     [r,c] = size(quake_magnitude)
    quake_fft = abs(fft(quake_magnitude(500:564)));
    disp(quake_fft(2));
    peak = max(quake_fft);
    disp(peak);
end

