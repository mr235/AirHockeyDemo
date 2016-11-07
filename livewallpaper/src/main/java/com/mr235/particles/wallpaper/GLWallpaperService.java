package com.mr235.particles.wallpaper;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;
import android.widget.Toast;

import com.mr235.particles.ParticlesRenderer;

public class GLWallpaperService extends WallpaperService {

    private GLEngine.WallpaperGLSurfaceView glSurfaceView;
    private boolean rendererSet;

    @Override
    public Engine onCreateEngine() {
        return new GLEngine();
    }

    public class GLEngine extends Engine {

        private ParticlesRenderer particlesRenderer;

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            glSurfaceView = new WallpaperGLSurfaceView(GLWallpaperService.this);

            // Check if the system supports OpenGL ES 2.0
            ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
            boolean supportsES2 = configurationInfo.reqGlEsVersion >= 0x20000
                    // Check for emulator.
                    || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
            &&(Build.FINGERPRINT.startsWith("generic")
            || Build.FINGERPRINT.startsWith("unknown")
            || Build.MODEL.contains("google_sdk")
            || Build.MODEL.contains("Emulator")
            || Build.MODEL.contains("Android SDK built for x86")));

            particlesRenderer = new ParticlesRenderer(GLWallpaperService.this);

            if (supportsES2) {
                glSurfaceView.setEGLContextClientVersion(2);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    glSurfaceView.setPreserveEGLContextOnPause(true);
                }
                glSurfaceView.setRenderer(particlesRenderer);
                rendererSet = true;
            } else {
                Toast.makeText(GLWallpaperService.this, "This device does not support OpenGL ES 2.0.", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            if (rendererSet) {
                if (visible) {
                    glSurfaceView.onResume();
                } else {
                    glSurfaceView.onPause();
                }
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            glSurfaceView.onWallpaperDestroy();
        }

        @Override
        public void onOffsetsChanged(final float xOffset, final float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {
            glSurfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    particlesRenderer.handleOffsetsChanged(xOffset, yOffset);
                }
            });
        }

        class WallpaperGLSurfaceView extends GLSurfaceView {

            public WallpaperGLSurfaceView(Context context) {
                super(context);
            }

            @Override
            public SurfaceHolder getHolder() {
                return getSurfaceHolder();
            }

            public void onWallpaperDestroy() {
                super.onDetachedFromWindow();
            }
        }
    }
}
