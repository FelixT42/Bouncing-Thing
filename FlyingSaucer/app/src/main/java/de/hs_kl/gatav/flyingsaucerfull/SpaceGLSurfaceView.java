package de.hs_kl.gatav.flyingsaucerfull;



import static de.hs_kl.gatav.flyingsaucerfull.util.Utilities.normalize;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;
import de.hs_kl.gatav.flyingsaucerfull.objects.Asteroid;
import de.hs_kl.gatav.flyingsaucerfull.objects.BorgCube;
import de.hs_kl.gatav.flyingsaucerfull.objects.Obstacle;
import de.hs_kl.gatav.flyingsaucerfull.objects.SpaceObject;
import de.hs_kl.gatav.flyingsaucerfull.objects.SpaceShip;
import de.hs_kl.gatav.flyingsaucerfull.objects.Starship;

public class SpaceGLSurfaceView extends GLSurfaceView {

    private SpaceRenderer renderer;
    public Context context;  // activity context

    private static final int obstacleCount = 15;
    private static final float minSpawnDistanceToPlayer = 1.5f;
    private static final float minSpawnDistanceBetweenObstacles = 1.5f;
    private static final float asteroidMinScale = 0.8f;
    private static final float asteroidMaxScale = 1.0f;

    private ArrayList<Obstacle> obstacles = new ArrayList<Obstacle>();
    private SpaceShip ship = new SpaceShip();
    private Starship starship = new Starship();
    { obstacles.add(starship); }

    public SpaceGLSurfaceView(Context context) {
        super(context);
        renderer = new SpaceRenderer();
        setRenderer(renderer);

        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    // called from sensor
    public void setShipVelocity(float vx, float vy, float vz) {
        ship.setVelocity(vx, vy, vz);
    }

    // enable support from starship enterprise
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                starship.enabled = true;
                Toast.makeText(context, "support granted", Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }

    private class SpaceRenderer implements Renderer {
        private float[] modelViewScene = new float[16];

        public float boundaryTop, boundaryBottom, boundaryLeft, boundaryRight;

        long lastFrameTime;

        public SpaceRenderer() {
            lastFrameTime = System.currentTimeMillis();
        }

        @Override
        public void onDrawFrame(GL10 gl) {

            // update time calculation
            long delta = System.currentTimeMillis() - lastFrameTime;
            float fracSec = (float) delta / 1000;
            lastFrameTime = System.currentTimeMillis();

            // scene updates
            updateShip(fracSec);
            updateObstacles(fracSec);


            // clear screen and depth buffer
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

            GL11 gl11 = (GL11) gl;

            // load local system to draw scene items
            gl.glMatrixMode(GL10.GL_MODELVIEW);
            gl11.glLoadMatrixf(modelViewScene, 0);

            ship.draw(gl);
            for (Obstacle obstacle: obstacles) {
                obstacle.draw(gl);
            }
        }


        private void updateShip(float fracSec) {
            ship.update(fracSec);
            // keep ship within window boundaries
            if (ship.getX() < boundaryLeft + ship.scale / 2)
                ship.setX(boundaryLeft + ship.scale / 2);
            if (ship.getX() > boundaryRight - ship.scale / 2)
                ship.setX(boundaryRight - ship.scale / 2);
            if (ship.getZ() < boundaryBottom + ship.scale / 2)
                ship.setZ(boundaryBottom + ship.scale / 2);
            if (ship.getZ() > boundaryTop - ship.scale / 2)
                ship.setZ(boundaryTop - ship.scale / 2);
        }


        private boolean areColliding(SpaceObject obj1, SpaceObject obj2) {
            float obj1X = obj1.getX();
            float obj1Z = obj1.getZ();
            float obj2X = obj2.getX();
            float obj2Z = obj2.getZ();
            float squaredHitDistance = ((obj1.scale + obj2.scale) / 2) * ((obj1.scale + obj2.scale) / 2);
            float squaredDistance = (obj1X - obj2X) * (obj1X - obj2X) + (obj1Z - obj2Z) * (obj1Z - obj2Z);

            if(squaredDistance < squaredHitDistance)
                return true;
            return false;
        }


        private void updateObstacles(float fracSec) {
            ArrayList<Obstacle> obstaclesToBeRemoved = new ArrayList<Obstacle>();

            // position update on all obstacles
            for(Obstacle obstacle : obstacles) {
                obstacle.update(fracSec);
            }


            // check for obstacles that flew out of the viewing area and remove
            // or deactivate them
            for (Obstacle obstacle : obstacles) {
                // offset makes sure that the obstacles don't get deleted or set
                // inactive while visible to the player.
                float offset = obstacle.scale;

                if ((obstacle.getX() > boundaryRight + offset)
                        || (obstacle.getX() < boundaryLeft - offset)
                        || (obstacle.getZ() > boundaryTop + offset)
                        || (obstacle.getZ() < boundaryBottom - offset)) {
                    if (obstacle instanceof Starship) {
                        ((Starship) obstacle).setInactive();
                    }
                    else
                        obstaclesToBeRemoved.add(obstacle);
                }
            }
            // remove obsolete obstacles
            for (Obstacle obstacle : obstaclesToBeRemoved) {
                obstacles.remove(obstacle);
            }
            obstaclesToBeRemoved.clear();


            // obstacle collision with space ship
            for (Obstacle obstacle : obstacles) {
                if (areColliding(ship, obstacle)) {
                    if (obstacle instanceof Asteroid) {
                        // add some damage to the ship
                        obstaclesToBeRemoved.add(obstacle);
                    }
                    if (obstacle instanceof BorgCube) {
                         // add some more because the borg are
                        // mighty!
                        obstaclesToBeRemoved.add(obstacle);
                    }
                    if (obstacle instanceof Starship){

                    }
                    //ODOT
                }
            }
            // remove obsolete obstacles
            for (Obstacle obstacle: obstaclesToBeRemoved) {
                obstacles.remove(obstacle);
            }
            obstaclesToBeRemoved.clear();


            // obstacle collision handling with each other
            for (int i=0; i<=obstacles.size()-2; i++) {
                Obstacle obstacle = obstacles.get(i);
                float ax = obstacle.getX();
                float az = obstacle.getZ();

                // check for collision with other obstacle
                for(int j=i+1; j<=obstacles.size()-1; j++) {
                    Obstacle otherObstacle = obstacles.get(j);
                    float oax = otherObstacle.getX();
                    float oaz = otherObstacle.getZ();

                    if(areColliding(obstacle, otherObstacle)) {
                        // collisions: let them bounce off each other
                        // http://de.wikipedia.org/wiki/ -> ElastischerStoß

                        float cv1[] = obstacle.velocity;
                        float cv2[] = otherObstacle.velocity;
                        float csv1 = cv1[2] / cv1[0]; // slope of velocity 1
                        float csv2 = cv2[2] / cv2[0]; // slope of velocity 2

                        float csz = (oaz - az) / (oax - ax); // central slope between centers
                        float cst = -1.0f / csz; // tangent slope perpendicular to central line

                        // calculate vt for both velocities
                        float cvt1[] = new float[3];
                        float cvt2[] = new float[3];
                        cvt1[0] = cv1[0] * (csz - csv1) / (csz - cst);
                        cvt1[2] = cvt1[0] * cst;

                        cvt2[0] = cv2[0] * (csz - csv2) / (csz - cst);
                        cvt2[2] = cvt2[0] * cst;

                        // calculate vz for both velocities
                        float cvz1[] = new float[3];
                        float cvz2[] = new float[3];
                        cvz1[0] = cv1[0] * (cst - csv1) / (cst - csz);
                        cvz1[2] = cvz1[0] * csz;

                        cvz2[0] = cv2[0] * (cst - csv2) / (cst - csz);
                        cvz2[2] = cvz2[0] * csz;

                        // asteroid-asteroid or borg-borg
                        // => inclined central elastic collision with identical masses
                        if(obstacle.getClass() == otherObstacle.getClass()) {
                            cv1[0] = cvt1[0] + cvz2[0];
                            cv1[2] = cvt1[2] + cvz2[2];

                            cv2[0] = cvt2[0] + cvz1[0];
                            cv2[2] = cvt2[2] + cvz1[2];
                        }
                        else{  // different scenarios

                            // one obstacle is borg cube, one is asteroid
                            // let the borg cube push the little asteroid out of the way - borg is much stronger than asteroid
                            // => inclined central elastic collision with one superior mass
                            if(otherObstacle instanceof BorgCube && obstacle instanceof Asteroid) {
                                cv1[0] = cvt1[0] - cvz1[0] + cvz2[0]*2.0f;
                                cv1[2] = cvt1[2] - cvz1[2] + cvz2[2]*2.0f;
                            }else if(obstacle instanceof BorgCube && otherObstacle instanceof Asteroid){
                                cv2[0] = cvt2[0] - cvz2[0] + cvz1[0]*2.0f;
                                cv2[2] = cvt2[2] - cvz2[2] + cvz1[2]*2.0f;
                            }

                            //ODOT
                            //Starship Asteroids kollision
                            if(otherObstacle instanceof Starship && obstacle instanceof Asteroid) {
                                cv1[0] = cvt1[0] - cvz1[0] + cvz2[0]*2.0f;
                                cv1[2] = cvt1[2] - cvz1[2] + cvz2[2]*2.0f;
                            }else if(obstacle instanceof Starship && otherObstacle instanceof Asteroid){
                                cv2[0] = cvt2[0] - cvz2[0] + cvz1[0]*2.0f;
                                cv2[2] = cvt2[2] - cvz2[2] + cvz1[2]*2.0f;
                            }

                            if(otherObstacle instanceof BorgCube && obstacle instanceof Starship) {
                                obstaclesToBeRemoved.add(otherObstacle);
                            }else if(obstacle instanceof BorgCube && otherObstacle instanceof Starship){
                                obstaclesToBeRemoved.add(otherObstacle);
                            }


                        }

                        if(obstacle instanceof Asteroid) ((Asteroid)obstacle).angularVelocity *= -1.0f;
                        if(otherObstacle instanceof Asteroid) ((Asteroid)otherObstacle).angularVelocity *= -1.0f;
                    }
                }
            }
            // remove obsolete obstacles
            for (Obstacle obstacle: obstaclesToBeRemoved) {
                obstacles.remove(obstacle);
            }
            obstaclesToBeRemoved.clear();


            // Spawn new borg or asteroid obstacles to match the target obstacle count
            if (obstacleCount > obstacles.size()) {
                for (int i = 0; i < obstacleCount - obstacles.size(); ++i) {
                    // determine what kind of obstacle is spawned next
                    int type = Math.random() < 0.85?1:2;  // 1 Asteroid, 2 BorgCube

                    float scale = 1.0f;
                    if(type == 1) {
                        scale = (float)Math.random() * (asteroidMaxScale - asteroidMinScale) + asteroidMinScale;
                    }

                    float spawnX = 0.0f;
                    float spawnZ = 0.0f;
                    float spawnOffset = scale * 0.5f;
                    float velocity[] = new float[3];

                    // determine source and destination quadrant
                    int sourceCode = ((Math.random()<0.5?0:1)<<1) | (Math.random()<0.5?0:1);  // source quadrant
                    int destCode = sourceCode ^ 3;	// destination quadrant is opposite of source
                    //Log.d("Code", sourceCode+" "+destCode);

					/* sourceCode, destCode
					 * +----+----+
					 * | 00 | 01 |
					 * +----+----+
					 * | 10 | 11 |
					 * +----+----+
					 */

                    // calculate source vertex position, <0.5 horizontal, else vertical
                    if(Math.random()<0.5){  // horizontal placing, top or bottom
                        spawnZ = (sourceCode&2)>0?boundaryBottom-spawnOffset : boundaryTop+spawnOffset;
                        spawnX = (sourceCode&1)>0?boundaryRight*(float)Math.random() : boundaryLeft*(float)Math.random();
                    }
                    else{  // vertical placing, left or right
                        spawnZ = (sourceCode&2)>0?boundaryBottom*(float)Math.random() : boundaryTop*(float)Math.random();
                        spawnX = (sourceCode&1)>0?boundaryRight+spawnOffset : boundaryLeft-spawnOffset;
                    }

                    // calculate destination vertex position, <0.5 horizontal, else vertical
                    if(Math.random()<0.5){  // horizontal placing, top or bottom
                        velocity[2] = (destCode&2)>0?boundaryBottom-spawnOffset : boundaryTop+spawnOffset;
                        velocity[0] = (destCode&1)>0?boundaryRight*(float)Math.random() : boundaryLeft*(float)Math.random();
                    }
                    else{  // vertical placing, left or right
                        velocity[2] = (destCode&2)>0?boundaryBottom*(float)Math.random() : boundaryTop*(float)Math.random();
                        velocity[0] = (destCode&1)>0?boundaryRight+spawnOffset : boundaryLeft-spawnOffset;
                    }

                    // calculate velocity
                    velocity[0] -= spawnX;
                    velocity[2] -= spawnZ;
                    normalize(velocity);


                    boolean positionOk = true;

                    // check distance to other obstacles
                    for(Obstacle obstacle: obstacles) {
                        float minDistance = 0.5f * scale + 0.5f * obstacle.scale + minSpawnDistanceBetweenObstacles;
                        if(Math.abs(spawnX - obstacle.getX()) < minDistance
                                && Math.abs(spawnZ - obstacle.getZ()) < minDistance)
                            positionOk = false;	// Distance too small -> invalid position
                    }

                    // check distance to player
                    float minPlayerDistance = 0.5f * scale + 0.5f * ship.scale + minSpawnDistanceToPlayer;
                    if(Math.abs(spawnX - ship.getX()) < minPlayerDistance &&
                            Math.abs(spawnZ - ship.getZ()) < minPlayerDistance)
                        positionOk = false;	// Distance to player too small -> invalid position

                    if (!positionOk)
                        continue; // Invalid spawn position -> try again next time

                    if(type == 1) {
                        Asteroid newAsteroid = new Asteroid();
                        newAsteroid.scale = scale;
                        newAsteroid.randomizeRotationAxis();
                        newAsteroid.angularVelocity = 50;
                        newAsteroid.setPosition(spawnX, 0, spawnZ);
                        newAsteroid.velocity = velocity;
                        obstacles.add(newAsteroid);
                    }
                    if(type == 2){
                        BorgCube newBorgCube = new BorgCube();
                        newBorgCube.scale = scale;
                        newBorgCube.velocity = velocity;
                        newBorgCube.setPosition(spawnX, 0, spawnZ);
                        obstacles.add(newBorgCube);
                    }
                }
            }

            // check for reactivation of Starship enterprise
            if(starship.enabled && starship.reActivate()) {
                float x1, z1, x2, z2, v[]=new float[2];

                x1=boundaryLeft-starship.scale*0.5f;
                x2=boundaryRight+starship.scale;
                z1=((float)(Math.random())<0.5f?-1f:1f)*(float)(Math.random())*boundaryTop*1.5f;
                z2=((float)(Math.random())<0.5f?-1f:1f)*(float)(Math.random())*boundaryTop*1.5f;
                v[0]=x2-x1; v[1]=z2-z1;
                normalize(v);
                v[0]*=4; v[1]*=4;

                starship.setPosition(x1, 0, z1);
                starship.setVelocity(v[0], 0.0f, v[1]);
            }
        }


        @Override
        // Called when surface is created or the viewport gets resized
        // set projection matrix
        // precalculate modelview matrix
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            GL11 gl11 = (GL11) gl;
            gl.glViewport(0, 0, width, height);

            float aspectRatio = (float) width / height;
            float fovy = 45.0f;

            // set up projection matrix for scene
            gl.glMatrixMode(GL10.GL_PROJECTION);
            gl.glLoadIdentity();
            GLU.gluPerspective(gl, fovy, aspectRatio, 0.001f, 100.0f);

            // set up modelview matrix for scene
            gl.glMatrixMode(GL10.GL_MODELVIEW);
            gl.glLoadIdentity();

            float desired_height=10.0f;
            // We want to be able to see the range of 5 to -5 units at the y
            // axis (height=10).
            // To achieve this we have to pull the camera towards the positive z axis
            // based on the following formula:
            // z = (desired_height / 2) / tan(fovy/2)
            float z = (float) (desired_height / 2 / Math.tan(fovy / 2 * (Math.PI / 180.0f)));
            // forward for the camera is backward for the scene
            gl.glTranslatef(0.0f, 0.0f, -z);
            // rotate local to achive top down view from negative y down to xz-plane
            // z range is the desired height
            gl.glRotatef(-90.0f, 1.0f, 0.0f, 0.0f);
            // save local system as a basis to draw scene items
            gl11.glGetFloatv(GL11.GL_MODELVIEW_MATRIX, modelViewScene, 0);

            // window boundaries
            // z range is the desired height
            boundaryTop = desired_height/2;
            boundaryBottom = -desired_height/2;
            // x range is the desired width
            boundaryLeft = -(desired_height/2 * aspectRatio);
            boundaryRight = (desired_height/2 * aspectRatio);
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            gl.glDisable(GL10.GL_DITHER);
            gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);

            gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            gl.glEnable(GL10.GL_CULL_FACE);
            gl.glShadeModel(GL10.GL_FLAT);
            gl.glEnable(GL10.GL_DEPTH_TEST);
            gl.glDepthFunc(GL10.GL_LEQUAL);
            gl.glShadeModel(GL10.GL_SMOOTH);
            gl.glEnable(GL10.GL_DEPTH_TEST);
        }

    }

}

