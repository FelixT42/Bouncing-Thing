package de.hs_kl.gatav.flyingsaucerfull.objects;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

public class SpaceShip extends SpaceObject {

    private static float FULL_HEALTH=1.0f;

    private float rotation = 0.0f;
    private float angularVelocity = 10.0f;
    private float deathRotationVelocity = 1.0f;




    private static float[] colorBody = { 0.8f, 0.8f, 0.8f };



    private static final float body_vertices[] = {
            -0.5f, 0.5f, -0.5f,	// btl 0
            -0.5f, -0.5f, -0.5f,// bbl 1
            0.5f, -0.5f, -0.5f,	// bbr 2
            0.5f, 0.5f, -0.5f,	// btr 3
            -0.5f, 0.5f, 0.5f,	// ftl 4
            -0.5f, -0.5f, 0.5f,	// fbl 5
            0.5f, -0.5f, 0.5f,	// fbr 6
            0.5f, 0.5f, 0.5f	// ftr 7
    };
    private static final short body_quads[] = {
            3, 2, 1, 0, // back
            4, 5, 6, 7, // front
            0, 4, 7, 3,	// top
            1, 2, 6, 5,	// bottom
            7, 6, 2, 3,	// right
            4, 0, 1, 5	// left
    };



    private static FloatBuffer bodyVerticesBuffer;
    private static ShortBuffer bodyQuadsBuffer;
    private static ShortBuffer bodyTrianglesBuffer;

    private static boolean buffersInitialized = false;

    public SpaceShip() {
        // set default scale to 2
        scale = 2;

        if (!buffersInitialized) {
            // Initialize buffers


            ByteBuffer bodyVerticesBB = ByteBuffer.allocateDirect(body_vertices.length * 4);
            bodyVerticesBB.order(ByteOrder.nativeOrder());
            bodyVerticesBuffer = bodyVerticesBB.asFloatBuffer();
            bodyVerticesBuffer.put(body_vertices);
            bodyVerticesBuffer.position(0);

            ByteBuffer bodyQuadsBB = ByteBuffer.allocateDirect(body_quads.length * 2);
            bodyQuadsBB.order(ByteOrder.nativeOrder());
            bodyQuadsBuffer = bodyQuadsBB.asShortBuffer();
            bodyQuadsBuffer.put(body_quads);
            bodyQuadsBuffer.position(0);



            buffersInitialized = true;
        }
    }


    @Override
    public void draw(GL10 gl) {
        gl.glMatrixMode(GL10.GL_MODELVIEW);

        gl.glPushMatrix();
        {
            gl.glMultMatrixf(transformationMatrix, 0);

            gl.glScalef(scale, scale, scale);

            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

            gl.glLineWidth(2.0f);


            // draw body
            gl.glPushMatrix();
            {
                gl.glRotatef(rotation, 0, 1, 0);
                gl.glVertexPointer(3, GL10.GL_FLOAT, 0, bodyVerticesBuffer);
                gl.glColor4f(colorBody[0], colorBody[1], colorBody[2], 0);

                for (int i = 0; i < (body_quads.length / 4); i++) {
                    bodyQuadsBuffer.position(4 * i);
                    gl.glDrawElements(GL10.GL_LINE_LOOP, 4, GL10.GL_UNSIGNED_SHORT, bodyQuadsBuffer);
                }
                bodyQuadsBuffer.position(0);

            }
            gl.glPopMatrix();


            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        }
        gl.glPopMatrix();
    }



    @Override
    public void update(float fracSec) {
        updatePosition(fracSec);

      //  rotation += fracSec * angularVelocity * deathRotationVelocity;
        rotation = 0;

    }

}

