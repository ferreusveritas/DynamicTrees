package com.ferreusveritas.dynamictrees.special;

import java.util.List;

import com.ferreusveritas.dynamictrees.api.IGenFeature;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.MathHelper;

import net.minecraft.block.BlockVine;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class GenFeatureVine implements IGenFeature {

	protected final PropertyBool vineMap[] = new PropertyBool[] {null, null, BlockVine.NORTH, BlockVine.SOUTH, BlockVine.WEST, BlockVine.EAST};
	protected int qty = 4;
	protected int maxLength = 8;
	protected Species species;
	
	public GenFeatureVine(Species species) {
		this.species = species;
	}
	
	public GenFeatureVine setQuantity(int qty) {
		this.qty = qty;
		return this;
	}
	
	public GenFeatureVine setMaxLength(int length) {
		this.maxLength = length;
		return this;
	}
	
	@Override
	public void gen(World world, BlockPos treePos, List<BlockPos> endPoints) {
		if(!endPoints.isEmpty()) {
			for(int i = 0; i < qty; i++) {
				BlockPos endPoint = endPoints.get(world.rand.nextInt(endPoints.size()));
				addVine(world, species, treePos, endPoint);
			}
		}
	}
	
	protected void addVine(World world, Species species, BlockPos treePos, BlockPos branchPos) {
		
		RayTraceResult result = species.branchRayTrace(world, treePos, branchPos, 90, 40, 5);
		
		if(result != null) {
			BlockPos vinePos = result.getBlockPos().offset(result.sideHit);
			PropertyBool vineSide = vineMap[result.sideHit.getOpposite().getIndex()];
			if(vineSide != null) {
				IBlockState vineState = Blocks.VINE.getDefaultState().withProperty(vineSide, Boolean.valueOf(true));
				int len = MathHelper.clamp(world.rand.nextInt(maxLength) + 3, 3, maxLength);
				for(int i = 0; i < len; i++) {
					if(world.isAirBlock(vinePos)) {
						world.setBlockState(vinePos, vineState);
						vinePos = vinePos.down();
					} else {
						break;
					}
				}
			}
		}
	}
	
	public static int coordHashCode(BlockPos pos) {
		int hash = (pos.getX() * 4111 ^ pos.getY() * 271 ^ pos.getZ() * 3067) >> 1;
		return hash & 0xFFFF;
	}
	
}
