package twilightforest.client.model.entity;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import twilightforest.client.JappaPackReloadListener;
import twilightforest.entity.boss.HydraHead;

public class HydraHeadModel<T extends HydraHead> extends ListModel<T> {
    private final ModelPart head;
    private final ModelPart jaw;

    public HydraHeadModel(ModelPart root) {
        this.head = root.getChild("head");
        this.jaw = this.head.getChild("jaw");
    }

    public static LayerDefinition checkForPack() {
        return JappaPackReloadListener.INSTANCE.isJappaPackLoaded() ? createJappaModel() : create();
    }

    private static LayerDefinition create() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(272, 0).addBox(-16.0F, -14.0F, -16.0F, 32.0F, 24.0F, 32.0F, new CubeDeformation(0.01F))
                        .texOffs(272, 56).addBox(-15.0F, -2.0F, -40.0F, 30.0F, 12.0F, 24.0F)
                        .texOffs(272, 132).addBox(-15.0F, 9.0F, -4.0F, 30.0F, 8.0F, 16.0F)
                        .texOffs(128, 200).addBox(-2.0F, -30.0F, 4.0F, 4.0F, 24.0F, 24.0F)
                        .texOffs(272, 156).addBox(-12.0F, 9.0F, -33.0F, 2.0F, 5.0F, 2.0F)
                        .texOffs(272, 156).addBox(10.0F, 9.0F, -33.0F, 2.0F, 5.0F, 2.0F)
                        .texOffs(280, 156).addBox(-8.0F, 8.0F, -33.0F, 16.0F, 2.0F, 2.0F)
                        .texOffs(280, 160).addBox(-10.0F, 8.0F, -29.0F, 2.0F, 2.0F, 16.0F)
                        .texOffs(280, 160).addBox(8.0F, 8.0F, -29.0F, 2.0F, 2.0F, 16.0F),
                PartPose.ZERO);
        head.addOrReplaceChild("jaw", CubeListBuilder.create()
                        .texOffs(272, 92).addBox(-15.0F, 0.0F, -26.0F, 30.0F, 8.0F, 32.0F)
                        .texOffs(272, 156).addBox(-10.0F, -5.0F, -23.0F, 2.0F, 5.0F, 2.0F)
                        .texOffs(272, 156).addBox(8.0F, -5.0F, -23.0F, 2.0F, 5.0F, 2.0F)
                        .texOffs(280, 156).addBox(-8.0F, -1.0F, -23.0F, 16.0F, 2.0F, 2.0F)
                        .texOffs(280, 160).addBox(-10.0F, -1.0F, -19.0F, 2.0F, 2.0F, 16.0F)
                        .texOffs(280, 160).addBox(8.0F, -1.0F, -19.0F, 2.0F, 2.0F, 16.0F),
                PartPose.offset(0.0F, 10.0F, -10.0F));
        head.addOrReplaceChild("frill", CubeListBuilder.create()
                        .texOffs(272, 200).addBox(-24.0F, -50.0F, 16.0F, 48.0F, 48.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, -14.0F, -0.5235988F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 512, 256);
    }

    private static LayerDefinition createJappaModel() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(260, 64).addBox(-16.0F, -16.0F, -16.0F, 32.0F, 32.0F, 32.0F)
                        .texOffs(236, 128).addBox(-16.0F, -2.0F, -40.0F, 32.0F, 10.0F, 24.0F)
                        .texOffs(356, 70).addBox(-12.0F, 8.0F, -36.0F, 24.0F, 6.0F, 20.0F),
                PartPose.ZERO);
        head.addOrReplaceChild("jaw", CubeListBuilder.create()
                        .texOffs(240, 162).addBox(-15.0F, 0.0F, -24.0F, 30.0F, 8.0F, 24.0F),
                PartPose.offset(0.0F, 10.0F, -14.0F));
        head.addOrReplaceChild("plate", CubeListBuilder.create()
                        .texOffs(388, 0).addBox(-24.0F, -48.0F, 0.0F, 48.0F, 48.0F, 6.0F)
                        .texOffs(220, 0).addBox(-4.0F, -32.0F, -8.0F, 8.0F, 32.0F, 8.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7853982F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 512, 256);
    }

    @Override
    public Iterable<ModelPart> parts() {
        return ImmutableList.of(this.head);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void prepareMobModel(T entity, float limbSwing, float limbSwingAmount, float partialTicks) {
        this.head.yRot = Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) * Mth.DEG_TO_RAD;
        this.head.xRot = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot()) * Mth.DEG_TO_RAD;
        float mouthOpen = Mth.lerp(partialTicks, entity.getMouthOpenLast(), entity.getMouthOpen());
        this.head.xRot -= mouthOpen * (Mth.PI / 12.0F);
        this.jaw.xRot = mouthOpen * (Mth.PI / 3.0F);
    }
}
