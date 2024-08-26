package com.mrcrayfish.vehicle.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

public class DustParticle extends TextureSheetParticle {

    protected DustParticle(ClientLevel world, double x, double y, double z, double xd, double yd, double zd) {
        super(world, x, y, z);
        this.lifetime = 50 + this.random.nextInt(20);
        this.quadSize = 0.3F;
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        this.alpha = 0.45F;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ < this.lifetime) {
            this.xd *= 0.98;
            this.yd *= 0.98;
            this.zd *= 0.98;
            this.alpha *= 0.95;
            this.move(this.xd, this.yd, this.zd);
        } else {
            this.remove();
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;
        RandomSource source = RandomSource.create();
        public Factory(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }




        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            DustParticle $$8 = new DustParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
            $$8.setAlpha(0.9F);
            $$8.pickSprite(this.spriteSet);
            return $$8;
        }
    }
}
