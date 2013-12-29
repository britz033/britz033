package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Log;

public class ImageUtil {
	
	private static final String TAG = "imageUtil";
	
	private File mTargetFile;
	private Context mContext;
	
	public ImageUtil(Context context){
		mContext = context;
	}
	
	public void setTargetFile(File target){
		mTargetFile = target;
	}
	
	public void startCamera(){
		correctCameraOrientation(mTargetFile);
	}
	
	public void startGallery(Uri galleryUri){
		copyUriToFile(galleryUri, mTargetFile);
		Bitmap bitmap = loadImageWithSampleSize(mTargetFile);
		bitmap = resizeImageWithinBoundary(bitmap);
		saveBitmapToFile(bitmap);
	}
	

	/** 이미지 사이즈 수정 후, 카메라 rotation 정보가 있으면 회전 보정함. */
	private void correctCameraOrientation(File imgFile) {
		Bitmap bitmap = loadImageWithSampleSize(imgFile); // 크기보정
		
		// 회전보정
		try {
			ExifInterface exif = new ExifInterface(imgFile.getAbsolutePath());
			int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
			int exifRotateDegree = exifOrientationToDegrees(exifOrientation);
			bitmap = rotateImage(bitmap, exifRotateDegree);
			bitmap = resizeImageWithinBoundary(bitmap);
			saveBitmapToFile(bitmap);	// 세이브
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/** 원하는 크기의 이미지로 options 설정. */
	private Bitmap loadImageWithSampleSize(File file) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(file.getAbsolutePath(), options);
		final int width = options.outWidth;
		final int height = options.outHeight;
		int longSide = Math.max(width, height);
		int sampleSize = 1;
		if (longSide > mImageSizeBoundary) {
			sampleSize = longSide / mImageSizeBoundary;
		}
		options.inJustDecodeBounds = false;
		options.inSampleSize = sampleSize;
//		options.inSampleSize = 28;
		options.inPurgeable = true;
		options.inDither = false;

		Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
		return bitmap;
	}
	
	/**
	 * mImageSizeBoundary 크기로 이미지 크기 조정. mImageSizeBoundary 보다 작은 경우 resize하지
	 * 않음.
	 */
	private Bitmap resizeImageWithinBoundary(Bitmap bitmap) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();

		if (width > height) {
			if (width > mImageSizeBoundary) {
				bitmap = resizeBitmapWithWidth(bitmap, mImageSizeBoundary);
			}
		} else {
			if (height > mImageSizeBoundary) {
				bitmap = resizeBitmapWithHeight(bitmap, mImageSizeBoundary);
			}
		}
		return bitmap;
	}

	private Bitmap resizeBitmapWithHeight(Bitmap source, int wantedHeight) {
		if (source == null)
			return null;

		int width = source.getWidth();
		int height = source.getHeight();

		float resizeFactor = wantedHeight * 1f / height;

		int targetWidth, targetHeight;
		targetWidth = (int) (width * resizeFactor);
		targetHeight = (int) (height * resizeFactor);

		Bitmap resizedBitmap = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, true);

		return resizedBitmap;
	}

	private Bitmap resizeBitmapWithWidth(Bitmap source, int wantedWidth) {
		if (source == null)
			return null;

		int width = source.getWidth();
		int height = source.getHeight();

		float resizeFactor = wantedWidth * 1f / width;

		int targetWidth, targetHeight;
		targetWidth = (int) (width * resizeFactor);
		targetHeight = (int) (height * resizeFactor);

		Bitmap resizedBitmap = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, true);

		return resizedBitmap;
	}
	
	/**
	 * EXIF정보를 회전각도로 변환하는 메서드
	 * 
	 * @param exifOrientation
	 *            EXIF 회전각
	 * @return 실제 각도
	 */
	private int exifOrientationToDegrees(int exifOrientation) {
		if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
			return 90;
		} else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
			return 180;
		} else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
			return 270;
		}
		return 0;
	}
	
	private Bitmap rotateImage(Bitmap bitmap, int degrees) {
		if (degrees != 0 && bitmap != null) {
			Matrix m = new Matrix();
			m.setRotate(degrees, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);
			try {
				Bitmap converted = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
				if (bitmap != converted) {
					bitmap.recycle();
					bitmap = converted;
				}
			} catch (OutOfMemoryError ex) {
			}
		}
		return bitmap;
	}
	
	private void saveBitmapToFile(Bitmap bitmap) {
		File target = mTargetFile;
		try {
			FileOutputStream fos = new FileOutputStream(target, false);
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	private int mImageSizeBoundary = 500;

	/**
	 * 사용할 이미지의 최대 크기 설정. 가로, 세로 지정한 크기보다 작은 사이즈로 이미지 크기를 조절함. default size :
	 * 500
	 * 
	 * @param sizePixel
	 *            기본 500
	 * @param quality 그림의 퀄리티를 나타낸다 숫자가 커질수록 낮아짐
	 */
	public void setImageSizeBoundary(int sizePixel, float quality) {
		mImageSizeBoundary = (int)(sizePixel/quality);
		Log.d(TAG,"이미지뷰 크기 : " + mImageSizeBoundary);
	}
	
	private void copyUriToFile(Uri srcUri, File target) {
		FileInputStream inputStream = null;
		FileOutputStream outputStream = null;
		FileChannel fcin = null;
		FileChannel fcout = null;
		try {
			// 스트림 생성
			inputStream = (FileInputStream) mContext.getContentResolver().openInputStream(srcUri);
			outputStream = new FileOutputStream(target);

			// 채널 생성
			fcin = inputStream.getChannel();
			fcout = outputStream.getChannel();

			// 채널을 통한 스트림 전송
			long size = fcin.size();
			fcin.transferTo(0, size, fcout);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				fcout.close();
			} catch (IOException ioe) {
			}
			try {
				fcin.close();
			} catch (IOException ioe) {
			}
			try {
				outputStream.close();
			} catch (IOException ioe) {
			}
			try {
				inputStream.close();
			} catch (IOException ioe) {
			}
		}
	}
}
