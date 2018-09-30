function [ x, y, z ] = LoadFile( filename )
    fid = fopen(filename,'rt');
    tmp = textscan(fid, '%s\t%f\t%f\t%f', 'Headerlines', 10);
    fclose(fid);
    x = tmp{2};
    y = tmp{3};
    z = tmp{4};

end

