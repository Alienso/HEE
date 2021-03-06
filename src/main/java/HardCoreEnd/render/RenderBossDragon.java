package HardCoreEnd.render;
import java.nio.Buffer;
import java.util.Random;

import HardCoreEnd.entity.EntityBossDragon;
import HardCoreEnd.proxy.ModClientProxy;
import HardCoreEnd.proxy.ModCommonProxy;
import HardCoreEnd.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderEnderCrystal;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.model.SimpleModelFontRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBossDragon extends RenderLiving{
    private static final ResourceLocation texDragon = new ResourceLocation("textures/entity/enderdragon/dragon.png");
    private static final ResourceLocation texDragonEyes = new ResourceLocation("textures/entity/enderdragon/dragon_eyes.png");
    private static final ResourceLocation texCrystalBeam = new ResourceLocation("textures/entity/endercrystal/endercrystal_beam.png");
    private static final ResourceLocation texDeathExplosions = new ResourceLocation("textures/entity/enderdragon/dragon_exploding.png");

    public RenderBossDragon(RenderManager renderManager){
        super(renderManager,new ModelEnderDragon(), 0.5F);
        //setRenderPassModel(mainModel);
    }

    protected void rotateDragonBody(EntityBossDragon dragon, float entityTickTime, float yawOffset, float partialTickTime){
        GL.rotate(-(float)dragon.getMovementOffsets(7, partialTickTime)[0], 0F, 1F, 0F);
        GL.rotate(10F*((float)(dragon.getMovementOffsets(5, partialTickTime)[1]-dragon.getMovementOffsets(10, partialTickTime)[1])), 1F, 0F, 0F);
        GL.translate(0F, 0F, 1F);

        if (dragon.deathTime > 0){
            GL.rotate(Math.min(1F, MathHelper.sqrt((dragon.deathTime+partialTickTime-1F)/20F*1.6F))*getDeathMaxRotation(dragon), 0F, 0F, 1F);
        }
    }

    protected void renderDragonModel(EntityBossDragon dragon, float limbSwing, float limbSwingAngle, float entityTickTime, float rotationYaw, float rotationPitch, float unitPixel){
        if (dragon.deathTicks > 0){
            GL.setDepthFunc(GL.LEQUAL);
            GL.enableAlphaTest(GL.GREATER, dragon.deathTicks*0.005F);
            bindTexture(texDeathExplosions);
            mainModel.render(dragon, limbSwing, limbSwingAngle, entityTickTime, rotationYaw, rotationPitch, unitPixel);
            GL.setAlphaFunc(GL.GREATER, 0.1F);
            GL.setDepthFunc(GL.EQUAL);
        }

        bindEntityTexture(dragon);
        mainModel.render(dragon, limbSwing, limbSwingAngle, entityTickTime, rotationYaw, rotationPitch, unitPixel);

        if (dragon.hurtTime > 0){
            GL.setDepthFunc(GL.EQUAL);
            GL.disableTexture2D();
            GL.enableBlendAlpha();
            GL.color(1F, 0F, 0F, 0.5F);
            mainModel.render(dragon, limbSwing, limbSwingAngle, entityTickTime, rotationYaw, rotationPitch, unitPixel);
            GL.enableTexture2D();
            GL.disableBlend();
            GL.setDepthFunc(GL.LEQUAL);
        }
    }

    public void renderDragon(EntityBossDragon dragon, double x, double y, double z, float yaw, float partialTickTime){
        dragon.bossInfo.setName((dragon.isAngry() ? new TextComponentString("Dragon") : new TextComponentString("Angry Dragon")));
        //EndMusicType.update(dragon.isAngry() ? EndMusicType.DRAGON_ANGRY : EndMusicType.DRAGON_CALM);
        super.doRender(dragon, x, y, z, yaw, partialTickTime);

        if (dragon.healingEnderCrystal != null){
            float animRot = dragon.healingEnderCrystal.innerRotation+partialTickTime;
            float yCorrection = MathHelper.sin(animRot*0.2F)*0.5F+0.5F;
            yCorrection = (yCorrection*yCorrection+yCorrection)*0.2F;
            float diffX = (float)(dragon.healingEnderCrystal.posX-dragon.posX-(dragon.prevPosX-dragon.posX)*(1F-partialTickTime));
            float diffY = (float)(yCorrection+dragon.healingEnderCrystal.posY-1D-dragon.posY-(dragon.prevPosY-dragon.posY)*(1F-partialTickTime));
            float diffZ = (float)(dragon.healingEnderCrystal.posZ-dragon.posZ-(dragon.prevPosZ-dragon.posZ)*(1F-partialTickTime));
            float distXZ = MathHelper.sqrt(diffX*diffX+diffZ*diffZ);
            float distXYZ = MathHelper.sqrt(diffX*diffX+diffY*diffY+diffZ*diffZ);
            GL.pushMatrix();
            GL.translate((float)x, (float)y+2F, (float)z);
            GL.rotate(MathUtil.toDeg((float)-Math.atan2(diffZ, diffX))-90F, 0F, 1F, 0F);
            GL.rotate(MathUtil.toDeg((float)-Math.atan2(distXZ, diffY))-90F, 1F, 0F, 0F);
            Tessellator tessellator = Tessellator.getInstance();
            RenderHelper.disableStandardItemLighting();
            GL.disableCullFace();
            bindTexture(texCrystalBeam);
            GL.setShadeModel(GL.SMOOTH);
            float animTime = -(dragon.ticksExisted+partialTickTime)*0.01F;
            float textureV = MathHelper.sqrt(diffX*diffX+diffY*diffY+diffZ*diffZ)*0.03125F-(dragon.ticksExisted+partialTickTime)*0.01F;
            tessellator.draw();
            byte sideAmount = 8;

            for(int i = 0; i <= sideAmount; ++i){
                float f11 = MathHelper.sin((i%sideAmount)*(float)Math.PI*2F/sideAmount)*0.75F;
                float f12 = MathHelper.cos((i%sideAmount)*(float)Math.PI*2F/sideAmount)*0.75F;
                float f13 = (i%sideAmount)/sideAmount;
                tessellator.getBuffer().pos(0,0,0).tex(0,0).color(0,0,0,0).endVertex();
                tessellator.getBuffer().pos(f11*0.2F, f12*0.2F, 0D).tex( f13, textureV).endVertex();
                //tessellator.setColorOpaque_I(0);
                //tessellator.addVertexWithUV(f11*0.2F, f12*0.2F, 0D, f13, textureV);
                tessellator.getBuffer().pos(f11, f12, distXYZ).tex(f13, animTime).endVertex();
                //tessellator.setColorOpaque_I(16777215);
                //tessellator.addVertexWithUV(f11, f12, distXYZ, f13, animTime);
            }

            tessellator.draw();
            GL.enableCullFace();
            GL.setShadeModel(GL.FLAT);
            RenderHelper.enableStandardItemLighting();
            GL.popMatrix();
        }
    }

    protected void renderDragonDying(EntityBossDragon dragon, float partialTickTime){
        //super.renderEquippedItems(dragon, partialTickTime);

        if (dragon.deathTicks > 0){
            Tessellator tessellator = Tessellator.getInstance();
            RenderHelper.disableStandardItemLighting();
            float animPerc = (dragon.deathTicks+partialTickTime)*0.005F;
            float fade = animPerc > 0.8F ? (animPerc-0.8F)*5F : 0F;

            Random rand = ModClientProxy.seedableRand;
            rand.setSeed(432L);

            GL.disableTexture2D();
            GL.setShadeModel(GL.SMOOTH);
            GL.enableBlend(GL.SRC_ALPHA, GL.ONE);
            GL.disableAlphaTest();
            GL.enableCullFace();
            GL.disableDepthMask();
            GL.pushMatrix();
            GL.translate(0F, -1F, -2F);

            /*for(int beam = 0; beam < (animPerc+animPerc*animPerc)/2F*60F; ++beam){
                GL.rotate(rand.nextFloat()*360F, 1F, 0F, 0F);
                GL.rotate(rand.nextFloat()*360F, 0F, 1F, 0F);
                GL.rotate(rand.nextFloat()*360F, 0F, 0F, 1F);
                GL.rotate(rand.nextFloat()*360F, 1F, 0F, 0F);
                GL.rotate(rand.nextFloat()*360F, 0F, 1F, 0F);
                GL.rotate(rand.nextFloat()*360F+animPerc*90F, 0F, 0F, 1F);
                tessellator.draw();
                float yRot = rand.nextFloat()*20F+5F+fade*10F;
                float xzRot = rand.nextFloat()*2F+1F+fade*2F;
                tessellator.setColorRGBA_I(16777215, (int)(255F*(1F-fade)));
                tessellator.addVertex(0D, 0D, 0D);
                tessellator.setColorRGBA_I(16711935, 0);
                tessellator.addVertex(-0.866D*xzRot, yRot, -0.5F*xzRot);
                tessellator.addVertex(0.866D*xzRot, yRot, -0.5F*xzRot);
                tessellator.addVertex(0D, yRot, xzRot);
                tessellator.addVertex(-0.866D*xzRot, yRot, -0.5F*xzRot);
                tessellator.draw();
            }*/

            GL.popMatrix();
            GL.enableDepthMask();
            GL.disableCullFace();
            GL.disableBlend();
            GL.setShadeModel(GL.FLAT);
            GL.color(1F, 1F, 1F, 1F);
            GL.enableTexture2D();
            GL.enableAlphaTest();
            RenderHelper.enableStandardItemLighting();
        }
    }

    protected int renderGlow(EntityBossDragon dragon, int pass, float partialTickTime){
        if (pass == 1)GL.setDepthFunc(GL.LEQUAL);
        if (pass != 0 || ModCommonProxy.hardcoreEnderbacon)return -1;
        else{
            bindTexture(texDragonEyes);
            GL.enableBlend(GL.ONE, GL.ONE);
            GL.disableAlphaTest();
            GL.disableLighting();
            GL.setDepthFunc(GL.EQUAL);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 61680%65536, 61680/65536);
            GL.color(1F, 1F, 1F, 1F);
            GL.enableLighting();
            GL.color(1F, 1F, 1F, 1F);
            return 1;
        }
    }

    @Override
    public void doRender(EntityLiving entity, double x, double y, double z, float yaw, float partialTickTime){
        renderDragon((EntityBossDragon)entity, x, y, z, yaw, partialTickTime);
    }

    /*@Override
    protected int shouldRenderPass(EntityLivingBase entity, int pass, float partialTickTime){
        return renderGlow((EntityBossDragon)entity, pass, partialTickTime);
    }*/

    /*@Override
    protected void renderEquippedItems(EntityLivingBase entity, float partialTickTime){
        renderDragonDying((EntityBossDragon)entity, partialTickTime);
    }*/

    /*@Override
    protected void rotateCorpse(EntityLivingBase entity, float entityTickTime, float yawOffset, float partialTickTime){
        rotateDragonBody((EntityBossDragon)entity, entityTickTime, yawOffset, partialTickTime);
    }*/

    @Override
    protected void renderModel(EntityLivingBase entity, float limbSwing, float limbSwingAngle, float entityTickTime, float rotationYaw, float rotationPitch, float unitPixel){
        renderDragonModel((EntityBossDragon)entity, limbSwing, limbSwingAngle, entityTickTime, rotationYaw, rotationPitch, unitPixel);
    }

    @Override
    public void doRender(EntityLivingBase entity, double x, double y, double z, float yaw, float partialTickTime){
        renderDragon((EntityBossDragon)entity, x, y, z, yaw, partialTickTime);
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity){
        return texDragon;
    }

    @Override
    public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTickTime){
        renderDragon((EntityBossDragon)entity, x, y, z, yaw, partialTickTime);
    }
}
