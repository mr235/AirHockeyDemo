package com.mr235.airhockey;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.mr235.airhockey.util.ShaderHelper;
import com.mr235.airhockey.util.TextResourceReader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.logging.SocketHandler;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Administrator on 2016/9/26.
 */
public class AirHockeyRenderer implements GLSurfaceView.Renderer {

    private static final int POSITION_COMPONENT_COUNT = 2;

    private static final int BYTES_PER_FLOAT = 4;
    private final FloatBuffer vertexData;
    private int program;

    private static final String A_COLOR = "a_Color";
    private static final int COLOR_COMPONENT_COUNT = 3;
    private static final int STRIDE = (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * BYTES_PER_FLOAT;
    private int aColorLocation;

    private static final String A_POSITION = "a_Position";
    private int aPositionLocation;

    private Context mContext;

    public AirHockeyRenderer(Context context) {
        mContext = context;
        float[] tableVerticesWithTriangles = {
                // Triangle Fan
                0f, 0f, 1f, 1f, 1f,
                -0.5f, -0.5f, 0.7f, 0.7f, 0.7f,
                0.5f, -0.5f, 0.7f, 0.7f, 0.7f,
                0.5f, 0.5f, 0.7f, 0.7f, 0.7f,
                -0.5f, 0.5f, 0.7f, 0.7f, 0.7f,
                -0.5f, -0.5f, 0.7f, 0.7f, 0.7f,

                // Line 1
                -0.5f, 0f, 1f, 0f, 0f,
                0.5f, 0f, 0f, 1f, 0f,

                // Mallets
                0f, -0.25f, 0f, 0f, 1f,
                0f, 0.25f, 1f, 0f, 0f,

        };

        vertexData = ByteBuffer
                .allocateDirect(tableVerticesWithTriangles.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();

        vertexData.put(tableVerticesWithTriangles);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        String vertexShaderSource = TextResourceReader.readTextFileFromResource(mContext, R.raw.simple_vertex_shader);
        String fragmentShaderSource = TextResourceReader.readTextFileFromResource(mContext, R.raw.simple_fragment_shader);

        int vertexShader = ShaderHelper.compileVertexShader(vertexShaderSource);
        int fragmentShader = ShaderHelper.compileFragmentShader(fragmentShaderSource);
        program = ShaderHelper.linkProgram(vertexShader, fragmentShader);

        if (BuildConfig.DEBUG) {
            ShaderHelper.validateProgram(program);
        }

        GLES20.glUseProgram(program);

        aColorLocation = GLES20.glGetAttribLocation(program, A_COLOR);
        aPositionLocation = GLES20.glGetAttribLocation(program, A_POSITION);

        vertexData.position(0);
        GLES20.glVertexAttribPointer(aPositionLocation, POSITION_COMPONENT_COUNT, GLES20.GL_FLOAT, false, STRIDE, vertexData);
        GLES20.glEnableVertexAttribArray(aPositionLocation);

        vertexData.position(POSITION_COMPONENT_COUNT);
        GLES20.glVertexAttribPointer(aColorLocation, COLOR_COMPONENT_COUNT, GLES20.GL_FLOAT, false, STRIDE, vertexData);
        GLES20.glEnableVertexAttribArray(aColorLocation);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glLineWidth(2f);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 6);

        GLES20.glDrawArrays(GLES20.GL_LINES, 6, 2);

        GLES20.glDrawArrays(GLES20.GL_POINTS, 8, 1);

        GLES20.glDrawArrays(GLES20.GL_POINTS, 9, 1);
    }
}
