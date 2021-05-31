package HardCoreEnd.save;

import java.io.File;

public interface ISaveDataHandler{
    void register();
    void clear(File root);
    void save();
}
