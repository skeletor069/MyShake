function [ oddRows ] = GetMagnitudes( filename )
    [x, y, z] = LoadFile(filename);
    quake_magnitude = sqrt(x.^2 + y.^2 + z.^2);
    oddRows = quake_magnitude(1:2:end,:);
end

