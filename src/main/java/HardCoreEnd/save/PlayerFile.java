package HardCoreEnd.save;


import java.io.File;

public abstract class PlayerFile extends SaveFile{
    private final String folder, filename;

    public PlayerFile(String folder, String filename){
        super("");
        this.folder = folder;
        this.filename = filename;
    }

    @Override
    protected File getFile(File root){
        File dir = new File(root, folder);
        if (!dir.exists())dir.mkdirs();

        return new File(dir, filename);
    }
}
