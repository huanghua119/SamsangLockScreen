/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huanghua.rs;

import static android.renderscript.ProgramStore.DepthFunc.ALWAYS;
import static android.renderscript.Sampler.Value.CLAMP;
import static android.renderscript.Sampler.Value.LINEAR;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.renderscript.Allocation;
import android.renderscript.Mesh;
import android.renderscript.ProgramFragment;
import android.renderscript.ProgramFragmentFixedFunction;
import android.renderscript.ProgramStore;
import android.renderscript.ProgramStore.BlendDstFunc;
import android.renderscript.ProgramStore.BlendSrcFunc;
import android.renderscript.ProgramVertex;
import android.renderscript.Sampler;
import android.renderscript.ScriptC;
import android.util.DisplayMetrics;

import com.huanghua.samsanglockscreen.R;

import java.util.TimeZone;

class FallRS extends RenderScriptScene {
    private static final int MESH_RESOLUTION = 48;

    private ProgramFragment mPfBackground;
    private ProgramStore mPfsBackground;
    private ProgramVertex mPvWater;
    private Sampler mSampler;

    private int mMeshWidth;
    private Allocation mUniformAlloc;

    private int mMeshHeight;
    private Mesh mMesh;
    private WorldState mWorldState;

    private ScriptC_fall mScript;

    private ScriptField_Constants mConstants;
    private Bitmap mBitmap;
    private Context mContext;

    public FallRS(int width, int height, Context context) {
        super(width, height);
        mContext = context;
    }

    @Override
    public void start() {
        super.start();
        final WorldState worldState = mWorldState;
        final int width = worldState.width;
        final int x = width / 4 + (int) (Math.random() * (width / 2));
        final int y = worldState.height / 4
                + (int) (Math.random() * (worldState.height / 2));
        addDrop(x, y);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);

        mWorldState.width = width;
        mWorldState.height = height;
        mWorldState.rotate = width > height ? 1 : 0;

        mScript.set_g_rotate(mWorldState.rotate);

    }

    @Override
    protected ScriptC createScript() {
        mScript = new ScriptC_fall(mRS, mResources, R.raw.fall);

        createMesh();
        createState();
        createProgramVertex();
        createProgramFragmentStore();
        createProgramFragment();
        loadTextures();

        mScript.setTimeZone(TimeZone.getDefault().getID());

        mScript.bind_g_Constants(mConstants);

        return mScript;
    }

    private void createMesh() {
        Mesh.TriangleMeshBuilder tmb = new Mesh.TriangleMeshBuilder(mRS, 2, 0);

        final int width = mWidth > mHeight ? mHeight : mWidth;
        final int height = mWidth > mHeight ? mWidth : mHeight;

        int wResolution = MESH_RESOLUTION;
        int hResolution = (int) (MESH_RESOLUTION * height / (float) width);

        wResolution += 2;
        hResolution += 2;

        for (int y = 0; y <= hResolution; y++) {
            final float yOffset = (((float) y / hResolution) * 2.f - 1.f)
                    * height / width;
            for (int x = 0; x <= wResolution; x++) {
                tmb.addVertex(((float) x / wResolution) * 2.f - 1.f, yOffset);
            }
        }

        for (int y = 0; y < hResolution; y++) {
            final boolean shift = (y & 0x1) == 0;
            final int yOffset = y * (wResolution + 1);
            for (int x = 0; x < wResolution; x++) {
                final int index = yOffset + x;
                final int iWR1 = index + wResolution + 1;
                if (shift) {
                    tmb.addTriangle(index, index + 1, iWR1);
                    tmb.addTriangle(index + 1, iWR1 + 1, iWR1);
                } else {
                    tmb.addTriangle(index, iWR1 + 1, iWR1);
                    tmb.addTriangle(index, index + 1, iWR1 + 1);
                }
            }
        }

        mMesh = tmb.create(true);

        mMeshWidth = wResolution + 1;
        mMeshHeight = hResolution + 1;

        mScript.set_g_WaterMesh(mMesh);
    }

    static class WorldState {
        public int width;
        public int height;
        public int meshWidth;
        public int meshHeight;
        public float glHeight;
        public int rotate;
    }

    private void createState() {
        mWorldState = new WorldState();
        mWorldState.width = mWidth;
        mWorldState.height = mHeight;
        mWorldState.meshWidth = mMeshWidth;
        mWorldState.meshHeight = mMeshHeight;
        mWorldState.rotate = mWidth > mHeight ? 1 : 0;

        mScript.set_g_meshHeight(mWorldState.meshHeight);
        mScript.set_g_xOffset(0);
        mScript.set_g_rotate(mWorldState.rotate);

    }

    private void loadTextures() {
        WallpaperManager wm = WallpaperManager.getInstance(mContext);
        Bitmap WallpaperBitmap = ((BitmapDrawable) wm.getDrawable()).getBitmap();

        DisplayMetrics dm = mResources.getDisplayMetrics();
        if (WallpaperBitmap != null) {
            if (mBitmap != null) {
                mBitmap.recycle();
                mBitmap = null;
            }
            mBitmap = Bitmap.createBitmap(dm.widthPixels, dm.heightPixels, Bitmap.Config.ARGB_8888);
            Canvas localCanvas = new Canvas();
            localCanvas.setBitmap(mBitmap);
            localCanvas.drawBitmap(WallpaperBitmap, 0, 0, null);
        }
        mScript.set_g_TRiverbed(loadTexture(R.drawable.pond));
    }

    private Allocation loadTexture(int id) {
        final Allocation allocation = Allocation.createFromBitmapResource(mRS, mResources, id);
        return allocation;
    }

    private Allocation loadTexture(Bitmap bitmap) {
        final Allocation allocation = Allocation.createFromBitmap(mRS, bitmap);
        return allocation;
    }

    private void createProgramFragment() {
        Sampler.Builder sampleBuilder = new Sampler.Builder(mRS);
        sampleBuilder.setMinification(LINEAR);
        sampleBuilder.setMagnification(LINEAR);
        sampleBuilder.setWrapS(CLAMP);
        sampleBuilder.setWrapT(CLAMP);
        mSampler = sampleBuilder.create();

        ProgramFragmentFixedFunction.Builder builder = new ProgramFragmentFixedFunction.Builder(
                mRS);
        builder.setTexture(
                ProgramFragmentFixedFunction.Builder.EnvMode.REPLACE,
                ProgramFragmentFixedFunction.Builder.Format.RGBA, 0);
        mPfBackground = builder.create();
        mPfBackground.bindSampler(mSampler, 0);

        mScript.set_g_PFBackground(mPfBackground);

    }

    private void createProgramFragmentStore() {
        ProgramStore.Builder builder = new ProgramStore.Builder(mRS);
        builder.setDepthFunc(ALWAYS);
        builder.setBlendFunc(BlendSrcFunc.ONE, BlendDstFunc.ONE);
        builder.setDitherEnabled(false);
        builder.setDepthMaskEnabled(true);
        mPfsBackground = builder.create();

        mScript.set_g_PFSBackground(mPfsBackground);
    }

    private void createProgramVertex() {
        mConstants = new ScriptField_Constants(mRS, 1);
        mUniformAlloc = mConstants.getAllocation();

        ProgramVertex.Builder sb = new ProgramVertex.Builder(mRS);

        String t = "\n" +
                "varying vec4 varColor;\n" +
                "varying vec2 varTex0;\n" +

                "vec2 addDrop(vec4 d, vec2 pos, float dxMul) {\n" +
                "  vec2 ret = vec2(0.0, 0.0);\n" +
                "  vec2 delta = d.xy - pos;\n" +
                "  delta.x *= dxMul;\n" +
                "  float dist = length(delta);\n" +
                "  if (dist < d.w) { \n" +
                "    float amp = d.z * dist;\n" +
                "    amp /= d.w * d.w;\n" +
                "    amp *= sin(d.w - dist);\n" +
                "    ret = delta * amp;\n" +
                "  }\n" +
                "  return ret;\n" +
                "}\n" +

                "void main() {\n" +
                "  vec2 pos = ATTRIB_position.xy;\n" +
                "  gl_Position = vec4(pos.x, pos.y, 0.0, 1.0);\n" +
                "  float dxMul = 1.0;\n" +

                "  varTex0 = vec2((pos.x + 1.0), (pos.y + 1.6666));\n" +

                "  if (UNI_Rotate < 0.9) {\n" +
                "    varTex0.xy *= vec2(0.25, 0.33);\n" +
                "    varTex0.x += UNI_Offset.x * 0.5;\n" +
                "    pos.x += UNI_Offset.x * 2.0;\n" +
                "  } else {\n" +
                "    varTex0.xy *= vec2(0.5, 0.3125);\n" +
                "    dxMul = 2.5;\n" +
                "  }\n" +

                "  varColor = vec4(1.0, 1.0, 1.0, 1.0);\n" +
                "  pos.xy += vec2(1.0, 1.0);\n" +
                "  pos.xy *= vec2(25.0, 42.0);\n" +

                "  varTex0.xy += addDrop(UNI_Drop01, pos, dxMul);\n" +
                "  varTex0.xy += addDrop(UNI_Drop02, pos, dxMul);\n" +
                "  varTex0.xy += addDrop(UNI_Drop03, pos, dxMul);\n" +
                "  varTex0.xy += addDrop(UNI_Drop04, pos, dxMul);\n" +
                "  varTex0.xy += addDrop(UNI_Drop05, pos, dxMul);\n" +
                "  varTex0.xy += addDrop(UNI_Drop06, pos, dxMul);\n" +
                "  varTex0.xy += addDrop(UNI_Drop07, pos, dxMul);\n" +
                "  varTex0.xy += addDrop(UNI_Drop08, pos, dxMul);\n" +
                "  varTex0.xy += addDrop(UNI_Drop09, pos, dxMul);\n" +
                "  varTex0.xy += addDrop(UNI_Drop10, pos, dxMul);\n" +
                "}\n";

        sb.setShader(t);
        sb.addConstant(mUniformAlloc.getType());
        sb.addInput(mMesh.getVertexAllocation(0).getType().getElement());
        mPvWater = sb.create();
        mPvWater.bindConstants(mUniformAlloc, 0);
        mScript.set_g_PVWater(mPvWater);
    }

    void addDrop(float x, float y) {
        int dropX = (int) ((x / mWidth) * mMeshWidth);
        int dropY = (int) ((y / mHeight) * mMeshHeight);
        mScript.invoke_addDrop(dropX, dropY);
    }
}
