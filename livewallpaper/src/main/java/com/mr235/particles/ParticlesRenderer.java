package com.mr235.particles;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

import com.mr235.particles.objects.Heightmap;
import com.mr235.particles.objects.ParticleShooter;
import com.mr235.particles.objects.ParticleSystem;
import com.mr235.particles.objects.Skybox;
import com.mr235.particles.programs.HeightmapShaderProgram;
import com.mr235.particles.programs.ParticleShaderProgram;
import com.mr235.particles.programs.SkyboxShaderProgram;
import com.mr235.particles.util.Geometry;
import com.mr235.particles.util.LogUtil;
import com.mr235.particles.util.TextureHelper;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Administrator on 2016/9/26.
 */
public class ParticlesRenderer implements GLSurfaceView.Renderer {
    private final Context context;

    private final float[] modelViewMatrix = new float[16];
    private final float[] it_modelViewMatrix = new float[16];

    private final float[] modelMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private final float[] viewMatrixForSkybox = new float[16];

    private final float[] tempMatrix = new float[16];
    private final float[] modelViewProjectionMatrix = new float[16];

    private HeightmapShaderProgram heightmapProgram;
    private Heightmap heightmap;

    private SkyboxShaderProgram skyboxProgram;
    private Skybox skybox;
    private int skyboxTexture;

    private ParticleShaderProgram particleProgram;
    private ParticleSystem particleSystem;
    private ParticleShooter redParticleShooter;
    private ParticleShooter greenParticleShooter;
    private ParticleShooter blueParticleShooter;
    private long globalStartTime;
    private int particleTexture;

//    private final Geometry.Vector vectorToLight = new Geometry.Vector(0.61f, 0.64f, -0.47f).normalize();
//    private final Geometry.Vector vectorToLight = new Geometry.Vector(0.3f, 0.35f, -0.89f).normalize();
    private final float[] vectorToLight = {0.3f, 0.35f, -0.89f, 0f};
    private final float[] pointLightPositions = {
                                            -1f, 1f, 0f, 1f,
                                            0f, 1f, 0f, 1f,
                                            1f, 1f, 0f, 1f};
    private final float[] pointLightColors = {
                                            1f, 0.2f, 0.02f,
                                            0.02f, 0.25f, 0.02f,
                                            0.02f, 0.2f, 1f};

    private long frameStartTimeMs;
    private static final String TAG = ParticlesRenderer.class.getSimpleName();
    private long startTimeMs;
    private int frameCount;

    public ParticlesRenderer(Context context) {
        this.context = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0f, 0f, 0f, 0f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_CULL_FACE);

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE);

        particleProgram = new ParticleShaderProgram(context);
        particleSystem = new ParticleSystem(10000);
        globalStartTime = System.nanoTime();

        Geometry.Vector particleDirection = new Geometry.Vector(0f, 0.5f, 0f);

        float angleVarianceInDegrees = 5f;
        float speedVariance = 1f;
        redParticleShooter = new ParticleShooter(
                new Geometry.Point(-1f, 0f, 0f),
                particleDirection,
                Color.rgb(255, 50, 5),
                angleVarianceInDegrees,
                speedVariance
        );

        greenParticleShooter = new ParticleShooter(
                new Geometry.Point(0f, 0f, 0f),
                particleDirection,
                Color.rgb(25, 255, 25),
                angleVarianceInDegrees,
                speedVariance
        );

        blueParticleShooter = new ParticleShooter(
                new Geometry.Point(1f, 0f, 0f),
                particleDirection,
                Color.rgb(5, 50, 255),
                angleVarianceInDegrees,
                speedVariance
        );

        particleTexture = TextureHelper.loadTexture(context, R.drawable.particle_texture);

        skyboxProgram = new SkyboxShaderProgram(context);
        skybox = new Skybox();
        skyboxTexture = TextureHelper.loadCubeMap(context,
                new int[] {
                        R.drawable.night_left, R.drawable.night_right,
                        R.drawable.night_bottom, R.drawable.night_top,
                        R.drawable.night_front, R.drawable.night_back
                });

        heightmapProgram = new HeightmapShaderProgram(context);
        heightmap = new Heightmap(((BitmapDrawable)context.getResources().getDrawable(R.drawable.heightmap)).getBitmap());
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

//        Matrix.perspectiveM(projectionMatrix, 0, 45f, (float) width / (float) height, 1f, 10f);
        Matrix.perspectiveM(projectionMatrix, 0, 45f, (float) width / (float) height, 1f, 100f);
        updateViewMatrices();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        limitFrameRate(24);
        logFrameRate();
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        drawHeightmap();
        drawSkybox();
        drawParticles();
    }

    private void logFrameRate() {
        if (BuildConfig.DEBUG) {
            long elapsedRealtimeMs = SystemClock.elapsedRealtime();
            double elapsedSeconds = (elapsedRealtimeMs - startTimeMs) / 1000.0;

            if (elapsedSeconds >= 1) {
                LogUtil.i(TAG, frameCount / elapsedSeconds + "fps");
                startTimeMs = SystemClock.elapsedRealtime();
                frameCount = 0;
            }
            frameCount++;
        }
    }

    private void limitFrameRate(int framesPerSecond) {
        long elapsedFrameTimeMs = SystemClock.elapsedRealtime() - frameStartTimeMs;
        long expectedFrameTimeMs = 1000 / framesPerSecond;
        long timeToSleepMs = expectedFrameTimeMs - elapsedFrameTimeMs;

        if (timeToSleepMs > 0) {
            SystemClock.sleep(timeToSleepMs);
        }
        frameStartTimeMs = SystemClock.elapsedRealtime();
    }

    private void drawHeightmap() {
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.scaleM(modelMatrix, 0, 100f, 10f, 100f);

        updateMvpMatrix();

        heightmapProgram.useProgram();
        /*
        heightmapProgram.setUniforms(modelViewProjectionMatrix, vectorToLight);
         */

        // Put the light positions into eye space.
        final float[] vectorToLightInEyeSpace = new float[4];
        final float[] pointPositionsInEyeSpace = new float[12];
        Matrix.multiplyMV(vectorToLightInEyeSpace, 0, viewMatrix, 0, vectorToLight, 0);
        Matrix.multiplyMV(pointPositionsInEyeSpace, 0, viewMatrix, 0, pointLightPositions, 0);
        Matrix.multiplyMV(pointPositionsInEyeSpace, 4, viewMatrix, 0, pointLightPositions, 4);
        Matrix.multiplyMV(pointPositionsInEyeSpace, 8, viewMatrix, 0, pointLightPositions, 8);

        heightmapProgram.setUniforms(modelViewMatrix,
                it_modelViewMatrix,
                modelViewProjectionMatrix,
                vectorToLightInEyeSpace,
                pointPositionsInEyeSpace,
                pointLightColors);
        heightmap.bindData(heightmapProgram);
        heightmap.draw();
    }

    private void drawSkybox() {
        Matrix.setIdentityM(modelMatrix, 0);
        updateMvpMatrixForSkybox();

        GLES20.glDepthFunc(GLES20.GL_LEQUAL);
        skyboxProgram.useProgram();
        skyboxProgram.setUniforms(modelViewProjectionMatrix, skyboxTexture);
        skybox.bindData(skyboxProgram);
        skybox.draw();
        GLES20.glDepthFunc(GLES20.GL_LESS);
    }

    private void drawParticles() {
        float currentTime = (System.nanoTime() - globalStartTime) / (1000f * 1000 * 1000);

        redParticleShooter.addParticles(particleSystem, currentTime, 1);
        greenParticleShooter.addParticles(particleSystem, currentTime, 1);
        blueParticleShooter.addParticles(particleSystem, currentTime, 1);

        Matrix.setIdentityM(modelMatrix, 0);
        updateMvpMatrix();

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE);

        GLES20.glDepthMask(false);
        particleProgram.useProgram();
        particleProgram.setUniforms(modelViewProjectionMatrix, currentTime, particleTexture);
        particleSystem.bindData(particleProgram);
        particleSystem.draw();
        GLES20.glDepthMask(true);

        GLES20.glDisable(GLES20.GL_BLEND);
    }

    private float xRotation;
    private float yRotation;
    public void handleTouchDrag(float deltaX, float deltaY) {
        xRotation += deltaX / 16f;
        yRotation += deltaY / 16f;

        if (yRotation < -90) {
            yRotation = -90;
        } else if (yRotation > 90) {
            yRotation = 90;
        }

        updateViewMatrices();
    }

    private void updateViewMatrices() {
        Matrix.setIdentityM(viewMatrix, 0);
        Matrix.rotateM(viewMatrix, 0, -yRotation, 1f, 0f, 0f);
        Matrix.rotateM(viewMatrix, 0, -xRotation, 0f, 1f, 0f);
        System.arraycopy(viewMatrix, 0, viewMatrixForSkybox, 0, viewMatrix.length);

        // We want the translation to apply to the regular view matrix, and not the skybox.
        Matrix.translateM(viewMatrix, 0, 0 - xOffset, -1.5f - yOffset, -5f);
    }

    private void updateMvpMatrix() {
        Matrix.multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0);
        Matrix.invertM(tempMatrix, 0, modelViewMatrix, 0);
        Matrix.transposeM(it_modelViewMatrix, 0, tempMatrix, 0);
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0);
    }

    private void updateMvpMatrixForSkybox() {
        Matrix.multiplyMM(tempMatrix, 0, viewMatrixForSkybox, 0, modelMatrix, 0);
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, tempMatrix, 0);
    }

    private float xOffset;
    private float yOffset;
    public void handleOffsetsChanged(float xOffset, float yOffset) {
        // Offsets range from 0 to 1.
        this.xOffset = (xOffset - 0.5f) * 2.5f;
        this.yOffset = (yOffset - 0.5f) * 2.5f;
        updateViewMatrices();
    }
}
