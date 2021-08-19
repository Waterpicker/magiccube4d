package com.superliminal.magiccube4d.jmonkey;

import com.donhatchsw.util.Triangulator;
import com.donhatchsw.util.VecMath;
import com.jme3.app.FlyCamAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.*;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.control.CameraControl;
import com.jme3.scene.control.LightControl;
import com.jme3.util.BufferUtils;
import com.jme3.util.TangentBinormalGenerator;
import com.superliminal.magiccube4d.MagicCube;
import com.superliminal.magiccube4d.PuzzleDescription;
import com.superliminal.magiccube4d.PuzzleManager;
import com.superliminal.magiccube4d.Vec_h;

import javax.swing.*;
import java.awt.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

public class Test extends SimpleApplication {
    private float[][] verts;
    private float direction;

    Geometry puzzle;

    DirectionalLight sun = new DirectionalLight();

    public static void main(String[] args) {
//        PuzzleDescription manager = new PuzzleManager("{4,3,3}", 3, new JProgressBar()).puzzleDescription;
//
//        System.out.println(manager.nDims());
//        System.out.println(manager.nVerts());
//        System.out.println(manager.nFaces());
//        System.out.println(manager.nCubies());
//        System.out.println(manager.nStickers());
//        System.out.println(manager.nGrips());
//        System.out.println();

        Test app = new Test();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        PuzzleDescription manager = new PuzzleManager("{4,3,3}", 3, new JProgressBar()).puzzleDescription;

        verts = manager.getStandardStickerVertsAtRest();
        verts = Arrays.copyOf(verts, verts.length);
        manager.computeStickerVertsAtRest(verts, 0.4f, 0.5f);

        int constant = 216 * 6;

        FloatBuffer positions = BufferUtils.createFloatBuffer(constant * 24);
        FloatBuffer texCoords = BufferUtils.createFloatBuffer(constant * 16);
        IntBuffer indices = BufferUtils.createIntBuffer(constant * 6);
        FloatBuffer normals = BufferUtils.createFloatBuffer(constant * 24);
        FloatBuffer colors = BufferUtils.createFloatBuffer(constant * 32);

        for(int i = 0; i < verts.length; ++i) {
            float w = 1.5f - verts[i][3];
            for(int j = 0; j < 3; ++j)
                verts[i][j] *= 1.5 / w;
            verts[i][3] = w; // keep this for future reference
        }

        Vector3f v0 = new Vector3f();
        Vector3f v1 = new Vector3f();
        Vector3f v2 = new Vector3f();
        Vector3f v3 = new Vector3f();

        Matrix3f matrix3f = new Matrix3f();
        Vector3f result =new Vector3f();
        int index = 0;

        int[] stickerColors = manager.getSticker2Face();

        int[][][] stickerInds = manager.getStickerInds();

        for (int i = 0, stickerIndsLength = stickerInds.length; i < stickerIndsLength; i++) {
            int[][] cubie = stickerInds[i];

            Color color = MagicCube.DEFAULT_FACE_COLORS[stickerColors[i] % 8];

            populateVector3f(v0, verts[cubie[0][0]]);
            populateVector3f(v1, verts[cubie[0][1]]);
            populateVector3f(v2, verts[cubie[0][2]]);
            populateVector3f(v3, verts[cubie[1][0]]);

            matrix3f.setRow(0, v1.subtract(v0, result));
            matrix3f.setRow(1, v2.subtract(v0, result));
            matrix3f.setRow(2, v3.subtract(v0, result));

            if (matrix3f.determinant() < 0.0f) {
                for (int[] face : cubie) {
                    populateVector3f(v0, verts[face[0]]);
                    populateVector3f(v1, verts[face[1]]);
                    populateVector3f(v2, verts[face[2]]);
                    populateVector3f(v3, verts[face[3]]);

                    Vector3f normal = FastMath.computeNormal(v0, v1, v2);

                    fillNormals(normals, normal);
                    fillNormals(normals, normal);
                    fillNormals(normals, normal);
                    fillNormals(normals, normal);

                    fillColors(colors, color);
                    fillColors(colors, color);
                    fillColors(colors, color);
                    fillColors(colors, color);

                    positions
                            .put(v0.x).put(v0.y).put(v0.z)
                            .put(v1.x).put(v1.y).put(v1.z)
                            .put(v2.x).put(v2.y).put(v2.z)
                            .put(v3.x).put(v3.y).put(v3.z);

                    indices.put(index + 0).put(index + 1).put(index + 2).put(index + 2).put(index + 3).put(index + 0);
                    index += 4;
                }
            }
        }

//        positions.limit(index * 3);
//        texCoords.limit(index * 2 );
//        indices.limit(index);
//        normals.limit(index * 3);

        viewPort.setBackgroundColor(new ColorRGBA(0.078431375f, 0.6666667f, 0.92156863f, 1.0f));

        Mesh b = new Mesh();
        b.setBuffer(VertexBuffer.Type.Position, 3, positions);
        b.setBuffer(VertexBuffer.Type.TexCoord, 2, texCoords);
        b.setBuffer(VertexBuffer.Type.Index, 3, indices);
        b.setBuffer(VertexBuffer.Type.Normal, 3, normals);
        b.setBuffer(VertexBuffer.Type.Color, 4, colors);

        b.updateBound();
        puzzle = new Geometry("Tesseract", b);  // create cube geometry from the shape
        Material mat = new Material(assetManager,
                "Common/MatDefs/Light/Lighting.j3md");  // create a simple material
        mat.setBoolean("UseMaterialColors",true);  // Set some parameters, e.g. blue.
        mat.setBoolean("UseVertexColor", true);
        mat.setColor("Ambient", ColorRGBA.White);   // ... color of this object
        mat.setColor("Diffuse", ColorRGBA.White);   // ... color of light being reflected
        puzzle.setMaterial(mat);                   // set the cube's material
        rootNode.attachChild(puzzle);              // make the cube appear in the scene

        sun.setColor(ColorRGBA.White);
        sun.setDirection(new Vector3f(-.5f,-.5f,-.5f).normalizeLocal());
        rootNode.addLight(sun);
    }

    private void fillColors(FloatBuffer colors, Color color) {
        colors.put(color.getRed() / 255f).put(color.getGreen() / 255f).put(color.getBlue() / 255f).put(color.getAlpha() / 255f);
    }

    @Override
    public void update() {
        super.update();
        float amount = 2 * FastMath.PI / (360f * 10);

        puzzle.rotate(amount, amount, amount);
    }

    private void fillNormals(FloatBuffer normals, Vector3f computeNormal) {
        normals.put(computeNormal.x).put(computeNormal.y).put(computeNormal.z);
    }

    public void populateVector3f(Vector3f vert, float[] data) {
        vert.set(data[0], data[1], data[2]);
    }

    public static void _VMV3(Matrix3f af, int row, Vector3f af1, Vector3f af2) {
        float amount = af1.x - af2.x;
        af.set(row, 0, amount);
        amount -= af1.y - af2.y;
        af.set(row, 1, amount);
        amount -= af1.y - af2.y;
        af.set(row, 2, amount);
    }
}
