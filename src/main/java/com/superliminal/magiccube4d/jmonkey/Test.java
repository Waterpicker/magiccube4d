package com.superliminal.magiccube4d.jmonkey;

import com.donhatchsw.util.Triangulator;
import com.donhatchsw.util.VecMath;
import com.jme3.app.FlyCamAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Triangle;
import com.jme3.math.Vector3f;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.control.CameraControl;
import com.jme3.scene.control.LightControl;
import com.jme3.util.BufferUtils;
import com.jme3.util.TangentBinormalGenerator;
import com.superliminal.magiccube4d.PuzzleDescription;
import com.superliminal.magiccube4d.PuzzleManager;
import com.superliminal.magiccube4d.Vec_h;

import javax.swing.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

public class Test extends SimpleApplication {
    private float[][] verts;
    private float direction;

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

        FloatBuffer positions = BufferUtils.createFloatBuffer(verts.length * 3);
        FloatBuffer texCoords = BufferUtils.createFloatBuffer(verts.length * 2);
        IntBuffer indices = BufferUtils.createIntBuffer(216 * 6 * 6);
        FloatBuffer normals = BufferUtils.createFloatBuffer(216 * 6 * 6);

        for(int i = 0; i < verts.length; ++i) {
            float w = 1.5f - verts[i][3];
            for(int j = 0; j < 3; ++j)
                verts[i][j] *= 1.5 / w;
            verts[i][3] = w; // keep this for future reference
        }

        Arrays.stream(verts).forEach(v -> positions.put(v[0]).put(v[1]).put(v[2]));

        Vector3f v0 = new Vector3f();
        Vector3f v1 = new Vector3f();
        Vector3f v2 = new Vector3f();
        Vector3f v3 = new Vector3f();

        for (int[][] cubie : manager.getStickerInds()) {
            for(int[] face : cubie) {
                populateVector3f(v0, verts[face[0]]);
                populateVector3f(v1, verts[face[1]]);
                populateVector3f(v2, verts[face[2]]);
                populateVector3f(v3, verts[face[3]]);

                fillNormals(normals, FastMath.computeNormal(v0, v1, v2).negateLocal());
                fillNormals(normals, FastMath.computeNormal(v2, v3, v0).negateLocal());

                indices.put(face[0]).put(face[1]).put(face[2]).put(face[2]).put(face[3]).put(face[0]);
            }
        }

        viewPort.setBackgroundColor(new ColorRGBA(0.078431375f, 0.6666667f, 0.92156863f, 1.0f));

        Mesh b = new Mesh();
        b.setBuffer(VertexBuffer.Type.Position, 3, positions);
        b.setBuffer(VertexBuffer.Type.TexCoord, 2, texCoords);
        b.setBuffer(VertexBuffer.Type.Index, 3, indices);
        b.setBuffer(VertexBuffer.Type.Normal, 3, normals);

        b.updateBound();
        Geometry geom = new Geometry("Tesseract", b);  // create cube geometry from the shape
        Material mat = new Material(assetManager,
                "Common/MatDefs/Light/Lighting.j3md");  // create a simple material
        mat.setBoolean("UseMaterialColors",true);  // Set some parameters, e.g. blue.
        mat.setBoolean("VertexLighting", true);
        mat.setColor("Ambient", ColorRGBA.Blue);   // ... color of this object
        mat.setColor("Diffuse", ColorRGBA.Blue);   // ... color of light being reflected
        geom.setMaterial(mat);                   // set the cube's material
        rootNode.attachChild(geom);              // make the cube appear in the scene

        sun.setColor(ColorRGBA.White);
        sun.setDirection(new Vector3f(-.5f,-.5f,-.5f).normalizeLocal());
        rootNode.addLight(sun);
    }

    @Override
    public void update() {
        super.update();

        direction += 0.1f;
        direction %= 1.0f;
        sun.setDirection(new Vector3f(-direction,-direction,-direction).normalizeLocal());
    }

    private void fillNormals(FloatBuffer normals, Vector3f computeNormal) {
        normals.put(computeNormal.x).put(computeNormal.y).put(computeNormal.z);
    }

    public void populateVector3f(Vector3f vert, float[] data) {
        vert.set(data[0], data[1], data[2]);
    }
}
