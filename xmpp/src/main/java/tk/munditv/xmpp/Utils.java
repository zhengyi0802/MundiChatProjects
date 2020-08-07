package tk.munditv.xmpp;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import java.io.ByteArrayOutputStream;

/**
 * Utility methods.
 * @author gotev (Aleksandar Gotev)
 */
public class Utils {

    /**
     * Private constructor to avoid instantiation.
     */
    private Utils() {}

    public static Bitmap getScaledBitmap(final Bitmap bitmap, final int maxDp) {
        // Get current dimensions
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        //Determine which dimension requires less scaling
        //and perform scaling based on this result
        float xScale = ((float) maxDp) / width;
        float yScale = ((float) maxDp) / height;
        float scale = (xScale <= yScale) ? xScale : yScale;

        // Create a matrix for the scaling and add the scaling data
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        return Bitmap.createBitmap(bitmap, 0, 0,
                width, height,
                matrix, true);
    }

    public static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }
}
