package HardCoreEnd.world.end;


import HardCoreEnd.util.MathUtil;
import HardCoreEnd.util.Vec;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.EnumSet;
import java.util.Random;

public abstract class TerritoryEnvironment{
    public static final TerritoryEnvironment defaultEnvironment = new TerritoryEnvironment(){};

    @SideOnly(Side.CLIENT)
    protected static EnumSet<? extends Enum<?>> currentVariations;

    @SideOnly(Side.CLIENT)
    public static final void updateCurrentVariations(EnumSet<? extends Enum<?>> newVariations){
        currentVariations = newVariations;
    }

    @SideOnly(Side.CLIENT)
    protected static final float getRenderDistanceMp(){
        return MathUtil.clamp(1F-MathUtil.square(Minecraft.getMinecraft().gameSettings.renderDistanceChunks)/300F, 0F, 1F); // 16 chunks -> max 256
    }

    protected Vec3d fogColor = Vec.zero().toVec3();

    /**
     * Returns true to apply default vanilla values to fog position. Can be overridden to configure fog GL settings.
     */
    @SideOnly(Side.CLIENT)
    public boolean setupFog(){
        float voidFactor = (float)EndTerritory.getVoidFactor(Minecraft.getMinecraft().player);
        float fogMultiplier = Math.max(1F, MathUtil.square(voidFactor*0.8F+0.8F));

        GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP2);
        GL11.glFogf(GL11.GL_FOG_DENSITY, getFogDensity()*fogMultiplier);
        GL11.glHint(GL11.GL_FOG_HINT, GL11.GL_DONT_CARE);
        return true;
    }

    /*@SideOnly(Side.CLIENT)
    public final Vec3d getFogColor(){
        return fogColor;
    }

    // Main methods for overriding
    //TODO return vec since x,y,z is final

    @SideOnly(Side.CLIENT)
    public void updateFogColor(){
        float r = 160F/255F;
        float g = 128F/255F;
        float b = 160F/255F;

        r *= 0.015F;
        g *= 0.015F;
        b *= 0.015F;

        fogColor.xCoord = r;
        fogColor.yCoord = g;
        fogColor.zCoord = b;
    }*/

    @SideOnly(Side.CLIENT)
    public float getFogDensity(){
        return 0.0025F+0.02F*getRenderDistanceMp();
    }

    @SideOnly(Side.CLIENT)
    public int getSkyColor(){
        return (40<<16)|(40<<8)|40;
    }

    /*@SideOnly(Side.CLIENT)
    public SkyTexture getSkyTexture(){
        return SkyTexture.DEFAULT;
    }*/

    @SideOnly(Side.CLIENT)
    public void generatePortalColor(float[] color, int layer, Random rand){
        color[0] = 1F;
        color[1] = 1F;
        color[2] = 1F;
    }
}
