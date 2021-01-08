package elucent.eidolon.world;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.gen.feature.structure.IStructurePieceType;
import net.minecraft.world.gen.feature.structure.TemplateStructurePiece;
import net.minecraft.world.gen.feature.template.BlockIgnoreStructureProcessor;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;

import java.util.Random;

public class BasicPiece extends TemplateStructurePiece {
    private ResourceLocation loc;
    Rotation rotation;
    Mirror mirror;

    void modifyPlacement(PlacementSettings settings, BlockPos pos) {
        //
    }

    void init(BlockPos pos, Random random) {
        //
    }

    public BasicPiece(IStructurePieceType structurePieceTypeIn, CompoundNBT nbt) {
        super(structurePieceTypeIn, nbt);
        this.loc = new ResourceLocation(nbt.getString("template"));
        this.rotation = Rotation.valueOf(nbt.getString("rot"));
        this.mirror = Mirror.valueOf(nbt.getString("mirror"));
    }

    public BasicPiece(IStructurePieceType type, ResourceLocation key, TemplateManager templateManager, CompoundNBT nbt) {
        this(type, nbt);
        Template part = templateManager.getTemplateDefaulted(key);
        PlacementSettings placement = (new PlacementSettings().setRotation(rotation).setMirror(mirror)).addProcessor(BlockIgnoreStructureProcessor.STRUCTURE_BLOCK);
        this.setup(part, this.templatePosition, placement);
        this.template = templateManager.getTemplate(key);
    }

    public BasicPiece(IStructurePieceType type, ResourceLocation key, TemplateManager templateManager, BlockPos pos, Random random) {
        this(type, key, templateManager, pos, Rotation.NONE, Mirror.NONE, random);
    }

    public BasicPiece(IStructurePieceType type, ResourceLocation key, TemplateManager templateManager, BlockPos pos, Rotation rot, Mirror mirror, Random random) {
        super(type, 0);
        this.templatePosition = pos;
        this.loc = key;
        this.rotation = rot;
        this.mirror = mirror;

        Template part = templateManager.getTemplateDefaulted(key);
        BlockPos d = part.getSize();
        BlockPos o = new BlockPos(-((d.getX() - 1) / 2), 0, -(d.getZ() - 1) / 2);
        templatePosition = templatePosition.add(o.rotate(this.rotation)).subtract(o);

        PlacementSettings placement = (new PlacementSettings().setRotation(rotation).setMirror(Mirror.NONE)).addProcessor(BlockIgnoreStructureProcessor.STRUCTURE_BLOCK);
        this.setup(part, this.templatePosition, placement);
        this.template = templateManager.getTemplate(key);
    }

    @Override
    protected void readAdditional(CompoundNBT tagCompound) {
        super.readAdditional(tagCompound);
        tagCompound.putString("template", loc.toString());
        tagCompound.putString("rot", rotation.name());
        tagCompound.putString("mirror", mirror.name());
    }

    @Override
    protected void handleDataMarker(String function, BlockPos pos, IServerWorld worldIn, Random rand, MutableBoundingBox sbb) {}
}
